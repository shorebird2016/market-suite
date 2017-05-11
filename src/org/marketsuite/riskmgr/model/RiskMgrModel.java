package org.marketsuite.riskmgr.model;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import jxl.Sheet;
import jxl.Workbook;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class RiskMgrModel {
    //----- CTOR -----
    private static RiskMgrModel _Instance;
    public static RiskMgrModel getInstance() {
        if (_Instance == null)
            _Instance = new RiskMgrModel();
        return _Instance;
    }
    private RiskMgrModel() {
        industryMap = importIndustryGroups();
        try {
            readStops(new File(FrameworkConstants.STOPS_DB));
        } catch (IOException e) {
            ArrayList<LogMessage> msg = new ArrayList<>();//might fail, keep a list of errors
            msg.add(new LogMessage(LoggingSource.RISKMGR_ACCOUNT, ApolloConstants.APOLLO_BUNDLE.getString("rm_67"),
                e, Calendar.getInstance()));//ok to continue w/o map
            Props.Log.setValue(null, msg);
        }
    }

    //----- public methods -----
    //find belonging group name for a given symbol
    public String findGroup(String symbol) {
        Iterator itor = industryMap.keySet().iterator();
        while(itor.hasNext()) {
            String grp = (String)itor.next();
            ArrayList<String> symbols = industryMap.get(grp);
            if (symbols != null && symbols.contains(symbol))
                return grp;
        }
        return null;
    }

    //null = not found
    public Position findPosition(String symbol) {
        for (Position pos : positions)
            if (symbol.equals(pos.getSymbol()))
                return pos;
        return null;
    }

    //null = not found
    public StopLevelInfo getStopLevelInfo(String symbol) {
        for (Position pos : positions) {
            if (symbol.equals(pos.getSymbol()))
                return pos.getStopLevelInfo();
        }
        return null;
    }

    //calculate total cost
    public float calcTotalCost() {
        float ret = 0;
        for (Position pos : positions)
            ret += pos.getCost() * pos.getShares();
        return ret;
    }
    public float calcTotalRisk() {//skip ones with positive risks
        float ret = 0;
        for (Position pos : positions) {
            float risk = pos.getRisk();
            if (risk >= 0) continue;
            ret += risk;
        }
        return ret;
    }
    public float calcTotalValue() {//skip ones with positive risks
        float ret = 0;
        for (Position pos : positions) {
            float equity = pos.getShares() * pos.getStopLevelInfo().getQuotes().get(0).getClose();
            ret += equity;
        }
        return ret + cashBalance;
    }

    //----- private methods -----
    /**
     * Read custom industry group definitions from user defined excel file called "custom_group.xls"
     * @return map of group name vs array of symbols or null if sheet can't be opened
     */
    private HashMap<String, ArrayList<String>> importIndustryGroups() {
        File grp_file = new File(FrameworkConstants.DATA_FOLDER_ACCOUNT + File.separator + FrameworkConstants.CUSTOM_INDUSTRY_GROUP);
        Workbook wb;
        try {
            wb = Workbook.getWorkbook(grp_file);
        } catch (Exception e) {//fail to read somehow
            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("rm_17") + " " + grp_file.getName() + " " +
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("dme_txt_4") + e.getMessage());
            e.printStackTrace();
            return null;
        }

        //for each column, first row is group name, other rows are symbols
        HashMap<String, ArrayList<String>> ret = new HashMap<>();
        Sheet sheet = wb.getSheet(0);
        int col = 0;
        while(col < sheet.getColumns()) {
            String grp_name = sheet.getCell(col, 0).getContents();
            if (grp_name.equals(""))
                break;//exit point on empty cell
            int row = 1;
            while(row < sheet.getRows()) {
//if (row == 19)
//    System.err.println("HOHOHO");
                String sym = sheet.getCell(col, row).getContents();
                if (sym.equals("")) break;
                ArrayList<String> symbols = ret.get(grp_name);
                if (symbols == null)
                    symbols = new ArrayList<>();
                symbols.add(sym);
                ret.put(grp_name, symbols);
                row++;
            }
            col++;
        }
        wb.close();
        return ret;
    }

    //save and read back stops
    private void readStops(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ( (line = br.readLine()) != null ) {
            String[] tokens = line.split(",");
            String symbol = tokens[0];
            StopLevel sl = new StopLevel(symbol, Double.parseDouble(tokens[2]), tokens[1]);
            stopMap.put(symbol, sl);
        }
    }
    public void writeStops(File file) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(file));
        Iterator<String> itor = stopMap.keySet().iterator();
        while (itor.hasNext()) {
            String symbol = itor.next();
            pw.print(symbol);
            pw.print(",");
            pw.print(stopMap.get(symbol).getMethod());
            pw.print(",");
            pw.println(stopMap.get(symbol).getLevel());
        }
        pw.flush();
        pw.close();
    }

    //convert industryMap into sectorMap
    private void industryToSectorMap() {
        sectorMap = new HashMap<>(); //destination
        Iterator<String> itor = industryMap.keySet().iterator();
        while (itor.hasNext()) {
            String industry = itor.next();
            ArrayList<String> industry_symbols = industryMap.get(industry);
            String sector = industry.split(" ")[0]; //first 3 or 4 letters followed by space
            if (sectorMap.containsKey(sector)) {
                sectorMap.get(sector).addAll(industry_symbols);
            }
            else {
                sectorMap.put(sector, industry_symbols);
            }
        }
    }

    //----- variables/accessors -----
    private JFrame parent;
    public void setParent(JFrame p) { parent = p; }
    public JFrame getParent() { return parent; }

    private HashMap<String, StopLevel> stopMap = new HashMap<>();
    public HashMap<String, StopLevel> getStopMap() { return stopMap; }

    private ArrayList<Position> positions = new ArrayList<>();
    public ArrayList<Position> getPositions() { return positions; }
    public void clearPositions() { positions = new ArrayList<>(); }

    private HashMap<String, ArrayList<String>> industryMap;
    public HashMap<String, ArrayList<String>> getIndustryMap() { return industryMap; }

    private HashMap<String, ArrayList<String>> sectorMap;
    public HashMap<String, ArrayList<String>> getSectorMap() { return sectorMap; }

    private HashMap<String, ArrayList<MatrixElement>> industryMatrix;
    public HashMap<String, ArrayList<MatrixElement>> getIndustryMatrix() { return industryMatrix; }
    public void setIndustryMatrix(HashMap<String, ArrayList<MatrixElement>> matrix) { this.industryMatrix = matrix; }

    private HashMap<String, ArrayList<MatrixElement>> sectorMatrix;
    public HashMap<String, ArrayList<MatrixElement>> getSectorMatrix() { return sectorMatrix; }
    public void setSectorMatrix(HashMap<String, ArrayList<MatrixElement>> matrix) { this.sectorMatrix = matrix; }

    private float cashBalance;
    public void setCashBalance(float cashBalance) { this.cashBalance = cashBalance; }
    public float getCashBalance() { return cashBalance; }

    public static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("#,###.##");
    public static final float DEFAULT_STOP_PCT = 0.05F;
    public static final int ROW_BALANCE_CELL = 5;//coordinate of cash balance in balance files
    public static final int COLUMN_BALANCE_CELL = 1;
    public static final int COLUMN_TSP_QTY = 1;
    public static final int COLUMN_TSP_COST = 5;
    public static final int COLUMN_TRADESTATION_POSITION_SYMBOL = 0;
    public static final int ROW_TRADESTATION_POSITION_SYMBOL = 4;
    public static final int COLUMN_TRADESTATION_MARKET_VALUE = 7;
//    public static final DecimalFormat CASH_POSITIVE_BALANCE_FORMAT = new DecimalFormat("$##,###.##");
//    public static final DecimalFormat CASH_NEGATIVE_BALANCE_FORMAT = new DecimalFormat("($##,###.##)");
//    public static final String[] PREFERRED_SYMBOLS = {
//            "ARR.PB", "NLY.PC", "PSA.PO", "PSA.PX", "PSB.PT",
//            "INN.PC", "RSO.PB", "JPM.PA", "WFC.PO", "JPM.PC",
//            "PFF", "PGF", "PGX",
//    };
}
