package org.marketsuite.framework.model.data;

import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import jxl.Sheet;
import jxl.Workbook;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import java.io.File;
import java.util.HashMap;

public class EquitySpeed {
    public EquitySpeed(String symbol, float monthlyPct, int monthlyBar, float weeklyPct, int weeklyBar) {
        this.symbol = symbol;
        this.monthlyPct = monthlyPct;
        this.monthlyBar = monthlyBar;
        this.weeklyPct = weeklyPct;
        this.weeklyBar = weeklyBar;
    }

    //read speed spreadsheet into a map
    public static HashMap<String, EquitySpeed> readSpeedSheet() {
        HashMap<String, EquitySpeed> ret = new HashMap<>();
        Workbook wb = null;
        try { //open this sheet, read one row at a time
            wb = Workbook.getWorkbook(new File(FrameworkConstants.DATA_FOLDER + File.separator + "speeds.xls"));
            readSheet(wb, 1, ret);
            readSheet(wb, 0, ret);
//            Sheet sheet = wb.getSheet(0);//This is ETF
//            int row = 1;//0 based, starting row
//            do {
//                String sym = sheet.getCell(SHEET_COLUMN_SYMBOL, row).getContents();
//                String mp = sheet.getCell(SHEET_COLUMN_MONTHLY_PCT, row).getContents();
//                String mb = sheet.getCell(SHEET_COLUMN_MONTHLY_BAR, row).getContents();
//                String wp = sheet.getCell(SHEET_COLUMN_WEEKLY_PCT, row).getContents();
//                String wkb = sheet.getCell(SHEET_COLUMN_WEEKLY_BAR, row).getContents();
//                if (sym.equals(""))// || mb.equals("") || wp.equals("") || wkb.equals(""))
//                    break;//finish at empty  symbol cell
//                ret.put(sym, new EquitySpeed(sym, Float.parseFloat(mp), Integer.parseInt(mb), Float.parseFloat(wp), Integer.parseInt(wkb)));
//                row++;
//            } while (true);//use blank symbol to finish reading
//
//            sheet = wb.getSheet(1);//This is stock sheet
            wb.close();
        }catch (Exception e) {
//            e.printStackTrace();
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("l2_rdspd"), LoggingSource.DAILY_CHART);
            if (wb != null) wb.close();
        }
        return ret;
     }

    //----private methods----
    private static void readSheet(Workbook wb, int sheet_num, HashMap<String, EquitySpeed> spd_map) {
        Sheet sheet = wb.getSheet(sheet_num);
        int row = 1;//0 based, starting row
        int rows = sheet.getRows();
    System.err.println("+++ ROWS ==> " + rows);
        do {
            String sym = sheet.getCell(SHEET_COLUMN_SYMBOL, row).getContents();
            String mp = sheet.getCell(SHEET_COLUMN_MONTHLY_PCT, row).getContents();
            String mb = sheet.getCell(SHEET_COLUMN_MONTHLY_BAR, row).getContents();
            String wp = sheet.getCell(SHEET_COLUMN_WEEKLY_PCT, row).getContents();
            String wkb = sheet.getCell(SHEET_COLUMN_WEEKLY_BAR, row).getContents();
            if (sym.equals(""))// || mb.equals("") || wp.equals("") || wkb.equals(""))
                break;//finish at empty  symbol cell
            spd_map.put(sym, new EquitySpeed(sym, Float.parseFloat(mp), Integer.parseInt(mb), Float.parseFloat(wp), Integer.parseInt(wkb)));
            row++;
        } while (row < rows);
    }
    //----accessor----
    public String getSymbol() { return symbol; }
    public float getMonthlyPct() { return monthlyPct; }
    public int getMonthlyBar() { return monthlyBar; }
    public float getWeeklyPct() { return weeklyPct; }
    public int getWeeklyBar() { return weeklyBar; }

    //----variables-----
    private String symbol;
    private float monthlyPct;
    private int monthlyBar;
    private float monthlySpeed;
    private float weeklyPct;
    private int weeklyBar;
    private float weeklySpeed;

    //-----literals-----
    public static final int SHEET_COLUMN_SYMBOL = 0;
    public static final int SHEET_COLUMN_MONTHLY_PCT = 1;
    public static final int SHEET_COLUMN_MONTHLY_BAR = 2;
    public static final int SHEET_COLUMN_WEEKLY_PCT = 3;
    public static final int SHEET_COLUMN_WEEKLY_BAR = 4;
}
