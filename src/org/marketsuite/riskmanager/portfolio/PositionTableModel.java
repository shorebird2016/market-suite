package org.marketsuite.riskmanager.portfolio;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.main.RiskMgrFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.main.RiskMgrFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Table panel to present all trades with equity and performance.
 */
class PositionTableModel extends DynaTableModel {
    PositionTableModel() {
        remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
    }

    //-----interface implementations-----
    public boolean isCellEditable(int row, int col) {
        return getCell(row, col).isHighlight();

//        boolean fixed_col = (col == COLUMN_SHARES) ||
//           (col == COLUMN_ENTRY_PRICE) || (col == COLUMN_TARGET_METHOD) ||
//           (col == COLUMN_STOP_METHOD) || (col == COLUMN_CURRENT_PRICE);
//        Object stop_method = getCell(row, COLUMN_STOP_METHOD).getValue();
//        boolean custom_stop = stop_method.equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]);
//        if (col == COLUMN_ATR_MULTIPLIER) {//if method is ATR type, allow edit here
//            boolean atr_stop = false;
//            for (int i = StopLevelInfo.STOP_ATR_BEGIN_INDEX; i <= StopLevelInfo.STOP_ATR_END_INDEX; i++)
//                if (stop_method.equals(StopLevelInfo.LIST_STOP_METHOD[i])) {
//                    atr_stop = true;
//                    break;
//                }
//            return atr_stop;
//        }
//        else if (col == COLUMN_SWP || col == COLUMN_ATR)
//            return false;
//        return !isBlankRow(row) && (fixed_col || (custom_stop && col == COLUMN_STOP_PRICE));
    }

    public void populate() {}

    public void populate(String file_path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        String line;
        _lstRows.clear();//empty table
        int seq = 1;//only for non-blank rows
        int row = 0;//all visible row index

        //read line by line into buffer, populate one row each time
        while ( (line = br.readLine()) != null ) {
            if (line.startsWith("#") || line.equals("")) //comments skip
                continue;

            //split line into tokens using comma separator
            String[] tokens = line.split(",");
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            if (tokens.length == 0 || tokens[0].equals("")) {//indicate blank row
                for (int col = 0; col < cells.length; col++)
                    cells[col] = new SimpleCell("");
                _lstRows.add(cells);
                row++;
                continue;
            }
            cells[COLUMN_SEQUENCE] = new SimpleCell(String.valueOf(seq++));
            cells[COLUMN_DIRECTION] = new SimpleCell(tokens[0]);
            String symbol = tokens[1];
            cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
            long shares = Long.parseLong(tokens[2]);
            cells[COLUMN_SHARES] = new SimpleCell(shares);
            cells[COLUMN_ENTRY_DATE] = new SimpleCell(tokens[3]);
            double cost = Double.parseDouble(tokens[4]);
            cells[COLUMN_ENTRY_PRICE] = new SimpleCell(cost);
            cells[COLUMN_STOP_METHOD] = new SimpleCell(tokens[6]);
            cells[COLUMN_SWP] = new SimpleCell(Double.parseDouble(tokens[8]));
            cells[COLUMN_ATR_MULTIPLIER] = new SimpleCell(Double.parseDouble(tokens[11]));

            //if stop method = custom, use token[9]
            if (cells[COLUMN_STOP_METHOD].getValue().equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]))
                cells[COLUMN_STOP_PRICE] = new SimpleCell(Double.parseDouble(tokens[9]));
            else
                cells[COLUMN_STOP_PRICE] = new SimpleCell(new Double(0));
            cells[COLUMN_TRADE_RISK] = new SimpleCell(new Double(0));
            cells[COLUMN_RISK_PERCENT] = new SimpleCell(new Double(0));
            cells[COLUMN_CURRENT_EQITY] = new SimpleCell(new Double(0));
            cells[COLUMN_GAIN_LOSS_PERCENT] = new SimpleCell(new Double(0));
            cells[COLUMN_GAIN_LOSS_AMOUNT] = new SimpleCell(new Double(0));
            cells[COLUMN_TARGET_METHOD] = new SimpleCell(tokens[5]);
            cells[COLUMN_TARGET_PRICE] = new SimpleCell(new Double(0));
            if (tokens.length == 13)//in case of old trd file
                cells[COLUMN_NOTES] = new SimpleCell(tokens[12]);
            else
                cells[COLUMN_NOTES] = new SimpleCell("");

            //setup stopInfo object
            StopLevelInfo sli = new StopLevelInfo(symbol, (float)cost, (int)shares, 0, _nQuoteLookback);
            stopInfo.put(symbol, sli);
            cells[COLUMN_ATR] = new SimpleCell(new Double(sli.getATR()));
            cells[COLUMN_CURRENT_PRICE] = new SimpleCell(new Double(sli.getQuotes().get(0).getClose()));
            _lstRows.add(cells);
            decorateRow(row);
            calcTargetPrice(row);
            calcEquityPL(row);
            row++;
        }

        //fill stop price in
        for (int i = 0; i < getRowCount(); i++) {
            if (getCell(i, COLUMN_SYMBOL).getValue().equals(""))//skip separator
                continue;

            calcStopPrice(i);
            calcRisk(i);
        }
        calcTotalRisk();
        calcMarketValue();
        getRiskPercent();
        calcStatusBarFields();
        updateCurrentStop();
        fireTableDataChanged();
        _bDirty = false;
    }

    public void setValueAt(Object value, int row, int col) {
        //set to same value, do nothing
        if (value.equals(getCell(row, col).getValue()))
            return;

        super.setValueAt(value, row, col);//save first for various calcXXX methods
        Object symbol = getCell(row, COLUMN_SYMBOL).getValue();
        StopLevelInfo sli = stopInfo.get(symbol);
        switch(col) {
            case COLUMN_SHARES://affect risk, equity, P/L
                calcRisk(row);
                calcEquityPL(row);
                calcStatusBarFields();

                //find break even stop level, set to new break even level
                long sh = (Long)value;
                sli.setShares((int) sh);
                sli.getStopLevels().get(StopLevelInfo.BREAK_EVEN_INDEX)
                   .setLevel(sli.calcBreakEvenPrice());
                break;

            case COLUMN_ENTRY_DATE:
                super.setValueAt(AppUtil.calendarToString((Calendar) value), row, col);
                break;

            case COLUMN_ENTRY_PRICE://affect target, stop, risk, equity, P/L
                calcTargetPrice(row);

                //change stop levels
                double cost = (Double)getCell(row, COLUMN_ENTRY_PRICE).getValue();
                sli.setCost((float)cost);
                ArrayList<StopLevel> stop_levels = sli.getStopLevels();//already sorted

                //look for ones related to percentage, change each one
                for (StopLevel stop_level : stop_levels) {
                    float pct = sli.findPercentByMethod(stop_level.getId());
                    if (pct == 0) //not percent type, don't change
                        continue;

                    stop_level.setLevel(cost * (1 + pct));
                }

                //update current stop level for StopLevelInfo
                sli.setStop(calcStopPrice(row));
                calcRisk(row);
                calcEquityPL(row);
                calcStatusBarFields();
                sli.sort();
                refreshGraph(row);
                break;

            case COLUMN_STOP_METHOD://affect stop price, risk
                decorateRow(row);//allow edit if stop price set to "Custom"
                sli.setStop(calcStopPrice(row));
                calcRisk(row);
                calcEquityPL(row);
                calcStatusBarFields();
                sli.sort();
                refreshGraph(row);
                decorateRow(row);
                break;

            case COLUMN_ATR_MULTIPLIER://changeable only for ATR type of stop
                sli.setStop(calcStopPrice(row));
                calcRisk(row);
                calcStatusBarFields();
                refreshGraph(row);
                break;

            case COLUMN_STOP_PRICE://custom stop, affect risk
                double stop = (Double) value;
                sli.setStop((float) stop);
                calcRisk(row);
                calcStatusBarFields();
                sli.sort();
                refreshGraph(row);
                break;

            case COLUMN_TARGET_METHOD://affect target price
                calcTargetPrice(row);
                break;

            case COLUMN_CURRENT_PRICE://affect P/L, equity
                calcEquityPL(row);
                calcStatusBarFields();
                break;

            default:
                return;
        }
        _bDirty = true;
        RiskMgrFrame rmf = ((MdiMainFrame) RiskMgrModel.getInstance().getParent()).findRiskMgrFrame();
//        if (rmf != null)
//            rmf.getMainPanel().getActiveTab().markDirty(true);
    }

    //-----protected methods-----
    //add blank row before current selection in table, no selection won't come here
    void insertTradeRow(int sel) {
        //ask for symbol name, check database for existence, reject if doesn't exist
        String sym = JOptionPane.showInputDialog(RiskMgrModel.getInstance().getParent(),
            ApolloConstants.APOLLO_BUNDLE.getString("active_msg_12"));
        if (sym == null || sym.equals(""))
            return;

        String symbol = sym.toUpperCase();
        String name = symbol.toUpperCase() + FrameworkConstants.EXTENSION_QUOTE;
        if (!DataUtil.isSymbolExist(name)) {
            WidgetUtil.showWarning(RiskMgrModel.getInstance().getParent(),
                    ApolloConstants.APOLLO_BUNDLE.getString("active_msg_13") +
                            " " + symbol + ApolloConstants.APOLLO_BUNDLE.getString("active_msg_14"));
            return;
        }

        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        //find last non-empty row's sequence number
        cells[COLUMN_SEQUENCE] = new SimpleCell("");
        cells[COLUMN_DIRECTION] = new SimpleCell(LIST_TRADE_TYPE[0]);
        cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
        cells[COLUMN_SHARES] = new SimpleCell(new Long(0));
        Calendar cal = Calendar.getInstance();
        cells[COLUMN_ENTRY_DATE] = new SimpleCell(AppUtil.calendarToString(cal));//today
        cells[COLUMN_ENTRY_PRICE] = new SimpleCell(new Double(0));
        cells[COLUMN_STOP_METHOD] = new SimpleCell(StopLevelInfo.LIST_STOP_METHOD[0]);
        cells[COLUMN_ATR] = new SimpleCell(new Double(0));
        cells[COLUMN_ATR_MULTIPLIER] = new SimpleCell(new Double((0)));
        cells[COLUMN_SWP] = new SimpleCell(new Double(0));
        cells[COLUMN_STOP_PRICE] = new SimpleCell("");
        cells[COLUMN_TRADE_RISK] = new SimpleCell(new Double(0));
        cells[COLUMN_RISK_PERCENT] = new SimpleCell(new Double(0));
        cells[COLUMN_CURRENT_EQITY] = new SimpleCell(new Double(0));
        cells[COLUMN_GAIN_LOSS_PERCENT] = new SimpleCell("");
        cells[COLUMN_GAIN_LOSS_AMOUNT] = new SimpleCell(new Double(0));
        cells[COLUMN_TARGET_METHOD] = new SimpleCell(LIST_TARGET_METHOD[0]);
        cells[COLUMN_TARGET_PRICE] = new SimpleCell("");
        cells[COLUMN_NOTES] = new SimpleCell("");

        //setup stopInfo object
        try {
            StopLevelInfo sli = new StopLevelInfo(symbol, 0, 0, 0, _nQuoteLookback);
            stopInfo.put(symbol, sli);
            cells[COLUMN_ATR] = new SimpleCell(new Double(sli.getATR()));
            cells[COLUMN_CURRENT_PRICE] = new SimpleCell(new Double(sli.getQuotes().get(0).getClose()));
        } catch (IOException e) {
            e.printStackTrace();
            WidgetUtil.showWarning(RiskMgrModel.getInstance().getParent(),
                ApolloConstants.APOLLO_BUNDLE.getString("active_msg_15"));
            return;
        }
        _lstRows.add(sel, cells);
        decorateRow(sel);
        calcTargetPrice(sel);
        calcStopPrice(sel);
        calcRisk(sel);
        calcEquityPL(sel);
        calcSequence();
        updateCurrentStop();
        fireTableRowsInserted(sel, sel);
        calcStatusBarFields();
    }

    //add blank row to the end of table
    void addTradeRow() {
        int new_index = getRowCount();

        //find last non-empty sequence, add 1 for the new row
        int seq_row = getLastNonEmptyRow();
        if (seq_row == -1)
            seq_row = 1;//nothing in table, start at 1
        else {
            String s = (String)getCell(seq_row, COLUMN_SEQUENCE).getValue();
            seq_row = Integer.parseInt(s) + 1;
        }

        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        //find last non-empty row's sequence number
        cells[COLUMN_SEQUENCE] = new SimpleCell(String.valueOf(seq_row));
        cells[COLUMN_DIRECTION] = new SimpleCell(LIST_TRADE_TYPE[0]);
        cells[COLUMN_SYMBOL] = new SimpleCell("XYZ");
        cells[COLUMN_SHARES] = new SimpleCell(new Long(100));
        Calendar cal = Calendar.getInstance();
        cells[COLUMN_ENTRY_DATE] = new SimpleCell(AppUtil.calendarToString(cal));//today
        cells[COLUMN_ENTRY_PRICE] = new SimpleCell(new Double(50));
        cells[COLUMN_STOP_METHOD] = new SimpleCell(StopLevelInfo.LIST_STOP_METHOD[0]);
        cells[COLUMN_ATR] = new SimpleCell(new Double(0));
        cells[COLUMN_SWP] = new SimpleCell(new Double(0));
        cells[COLUMN_STOP_PRICE] = new SimpleCell("");
        cells[COLUMN_TRADE_RISK] = new SimpleCell(new Double(0));
        cells[COLUMN_RISK_PERCENT] = new SimpleCell(new Double(0));
        cells[COLUMN_CURRENT_PRICE] = new SimpleCell(new Double(60));
        cells[COLUMN_CURRENT_EQITY] = new SimpleCell(new Double(0));
        cells[COLUMN_GAIN_LOSS_PERCENT] = new SimpleCell("");
        cells[COLUMN_GAIN_LOSS_AMOUNT] = new SimpleCell(new Double(0));
        cells[COLUMN_TARGET_METHOD] = new SimpleCell(LIST_TARGET_METHOD[0]);
        cells[COLUMN_TARGET_PRICE] = new SimpleCell("");
        cells[COLUMN_NOTES] = new SimpleCell("");
        _lstRows.add(cells);
        decorateRow(new_index);
        calcTargetPrice(new_index);
        calcStopPrice(new_index);
        calcRisk(new_index);
        calcEquityPL(new_index);
        fireTableRowsInserted(new_index, new_index);
    }

    //add blank row in front of the selected row to increase readability, blank row stays blank, saved to file
    void insertBlankRow(int selected_row) {
        SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
        for (int col = 0; col < cells.length; col++)
            cells[col] = new SimpleCell("");
        _lstRows.add(selected_row, cells);
        fireTableRowsInserted(selected_row, selected_row);
  }

    void deleteRow(int row) {
        String symbol = (String)getCell(row, COLUMN_SYMBOL).getValue();

        //remove StopLevelInfo object from map, ok if symbol doesn't exist
        // check if this symbol has duplicate, if so, don't remove from StopLevelInfo
        if (!hasDuplicate(symbol))
            stopInfo.remove(symbol);

        //prepare visual deletion
        int[] rows = new int[1];
        rows[0] = row;
        _nSelectedRows = rows;
        delete();
        calcSequence();
        fireTableDataChanged();
        calcStatusBarFields();
    }

    boolean isBlankRow(int row) {
        return getCell(row, COLUMN_SEQUENCE).getValue().equals("");
    }

    //returns true if this symbol exists elsewhere
    boolean hasDuplicate(String symbol) {
        int count = 0;
        for (int row = 0; row < getRowCount(); row++) {
            if (symbol.equals(getCell(row, COLUMN_SYMBOL).getValue()))
                count++;
        }
        return count > 1;
    }

    //save sandbox to file of .trd type (pretty much like a .csv)
    void saveAccount(File output_path) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(output_path));
        pw.println("#FORMAT: Trade direction, Symbol, Shares, Entry date, " +
                "Entry price, Target method, Stop method, ATR, SWP, Stop price, Current Price, " +
                "ATR Multiplier, Notes");
        pw.println("# Empty first column indicates a separator.");
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < getRowCount(); row++) {
            sb.append(getCell(row, COLUMN_DIRECTION).getValue()).append(",")//token 0
              .append(getCell(row, COLUMN_SYMBOL).getValue()).append(",") //token 1
              .append(getCell(row, COLUMN_SHARES).getValue()).append(",") //token 2
              .append(getCell(row, COLUMN_ENTRY_DATE).getValue()).append(",") //token 3
              .append(getCell(row, COLUMN_ENTRY_PRICE).getValue()).append(",") //token 4
              .append(getCell(row, COLUMN_TARGET_METHOD).getValue()).append(",") //token 5
//            if (method.equals(LIST_TARGET_METHOD[0]))
//                sb.append(LIST_TARGET_METHOD_INTERNAL[0]).append(",");
//            else if (method.equals(LIST_TARGET_METHOD[1]))
//                sb.append(LIST_TARGET_METHOD_INTERNAL[1]).append(",");
//            else
//                sb.append(LIST_TARGET_METHOD_INTERNAL[2]).append(",");
//todo remove internal for target method
              .append(getCell(row, COLUMN_STOP_METHOD).getValue()).append(",") //token 6
              .append(getCell(row, COLUMN_ATR)).append(",") //token 7
              .append(getCell(row, COLUMN_SWP)).append(",") //token 8
              .append(getCell(row, COLUMN_STOP_PRICE)).append(",") //token 9
              .append(getCell(row, COLUMN_CURRENT_PRICE)).append(",") //token 10
              .append(getCell(row, COLUMN_ATR_MULTIPLIER)).append(",") //token 11
              .append(getCell(row, COLUMN_NOTES)).append("\n"); //token 12
        }
        pw.println(sb.toString());
        pw.flush();
        pw.close();
    }

    //exit a trade: write selected row and delete from table after appending to a file
    void exitTrade(File output_file, int row, String exit_date, double exit_price, double cost, double proceeds) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (!output_file.exists()) {//doesn't exist
            output_file = new File(FrameworkConstants.DATA_FOLDER_ACCOUNT + "/" + output_file.getName());
            output_file.createNewFile();
            sb.append("#FORMAT: Trade direction, Symbol, Shares, Entry date, " +
                    "Entry price, Exit Date, Exit Price, Cost, Proceeds\n")
              .append("#empty first column indicates a separator.\n\n");
        }

        //format a line
        PrintWriter pw = new PrintWriter(new FileWriter(output_file, true));
        sb.append(getCell(row, COLUMN_DIRECTION).getValue()).append(",")
            .append(getCell(row, COLUMN_SYMBOL).getValue()).append(",")
            .append(getCell(row, COLUMN_SHARES).getValue()).append(",")
            .append(getCell(row, COLUMN_ENTRY_DATE).getValue()).append(",")
            .append(getCell(row, COLUMN_ENTRY_PRICE).getValue()).append(",");
        sb.append(exit_date).append(",").append(String.valueOf(exit_price))
            .append(",").append(String.valueOf(cost))
            .append(",").append(String.valueOf(proceeds));
        pw.println(sb.toString());
        pw.flush();
        pw.close();

        //delete row
//        int[] rows = new int[1];
//        rows[0] = row;
        deleteRow(row);
    }

    //download current quotes for all from Yahoo
    void updateQuotes() {
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance(RiskMgrModel.getInstance().getParent(), "");
        pb.setVisible(true);

        //start a thread to simulate and export all files
        Thread thread = new Thread() {
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_msg_5"));
                    }
                });

                for (int row = 0; row < getRowCount(); row++) {
                    if (getCell(row, COLUMN_SEQUENCE).getValue().equals(""))
                        continue;//skip blank row

                    final String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
                    Calendar cal = Calendar.getInstance();

                    //loop up to 7 days till yahoo has good response, to avoid weekend, holiday no quotes
                    FundQuote quote;
                    int limit = 7;
                    do {
                        int cur_month = cal.get(Calendar.MONTH);
                        int cur_day = cal.get(Calendar.DAY_OF_MONTH);
                        int cur_year = cal.get(Calendar.YEAR);
                        final String dt = sym + ": " + cur_month + "/" + cur_day + "/" + cur_year;
                        System.out.print("\n....Get quote for " + dt + " =====> ");//todo better logging

                        //request current day quote
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_msg_5") + " " + sym + " on " + dt);
                            }
                        });
                        try {
                            quote = DataUtil.getYahooQuote(sym);
                            if (quote == null) {
                                cal.add(Calendar.DAY_OF_YEAR, -1);//go back one day
                                limit--;
                                System.out.println("\t??? NO Quote: " + cur_month + "/" + cur_day + "/" + cur_year);//todo better logging
                            }
                            else {//got quote
                                getCell(row, COLUMN_CURRENT_PRICE).setValue(new Double(quote.getClose()));
                                calcEquityPL(row);
                                break;
                            }
                        } catch (IOException e) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    MessageBox.messageBox(RiskMgrModel.getInstance().getParent(),
                                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                        ApolloConstants.APOLLO_BUNDLE.getString("active_msg_3") + ": " + sym,
                                        MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
                                    pb.setVisible(false);
                                }
                            });
                            e.printStackTrace();
                        }
                    }while (limit > 0);

                    //if 7 days back, still no quote, warn user
                    if (limit <= 0) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                MessageBox.messageBox(RiskMgrModel.getInstance().getParent(),
                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                    ApolloConstants.APOLLO_BUNDLE.getString("active_msg_2") + ": " + sym,
                                    MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
                                pb.setVisible(false);
                                return;
                            }
                        });
                    }
                }

                //update table and status bar
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        fireTableDataChanged();
                        calcStatusBarFields();
                        pb.setVisible(false);
                    }
                });
            }
        };
        thread.start();
    }

    //calculate total risk, return formatted string
    void calcTotalRisk() {
        double total_risk = 0;
//        double total_adj_risk = 0;
        for (int i=0; i<getRowCount(); i++) {
            Object dir = getCell(i, COLUMN_DIRECTION).getValue();
            if (!dir.equals("")) { //skip empty row
                double risk = (Double) getCell(i, COLUMN_TRADE_RISK).getValue();
                total_risk += risk;
//                risk = (Double) getCell(i, COLUMN_RISK_PERCENT).getValue();
//                total_adj_risk += risk;
            }
        }
        _dTotalRisk = total_risk;
//        _dTotalAdjRisk = total_adj_risk;
    }
    double getTotalRisk() {
        return _dTotalRisk;
    }
    double getAdjustedRisk() { return _dTotalAdjRisk; }

    //calculate market value, return formatted string
    void calcMarketValue() {
        _dMarketValue = 0;
        for (int i=0; i<getRowCount(); i++) {
            Object dir = getCell(i, COLUMN_DIRECTION).getValue();
            if (!dir.equals("")) {
                double equity = (Double) getCell(i, COLUMN_CURRENT_EQITY).getValue();
                _dMarketValue += equity;
            }
        }
        _dCashPercent = _dCashAmount / (_dMarketValue + _dCashAmount);
        calcStopEquity();
    }
    double getEquity() {
        return _dMarketValue;
    }
    double getTotalCost() { return _dTotalCost; }

    //what's the equity for everything sold at stop ?
    void calcStopEquity() {
        _dStopEquity = 0;
        for (int i=0; i<getRowCount(); i++) {
            Object dir = getCell(i, COLUMN_DIRECTION).getValue();
            if (dir.equals(""))
                continue;

            double stop = (Double)getCell(i, COLUMN_STOP_PRICE).getValue();
            long shares = (Long)getCell(i, COLUMN_SHARES).getValue();
            _dStopEquity += stop * shares;
        }
        _dMaxPullbackAmount = _dMarketValue - _dStopEquity;
        _dMaxPullbackPercent = _dMaxPullbackAmount / _dMarketValue;
    }
    double getStopEquity() { return _dStopEquity; }

    //calculate percent at risk
    double getAdjPercent() {
        double pct = _dTotalAdjRisk / _dTotalCost;
        if (_dTotalCost == 0)
            pct = 0;
        return pct;
    }
    double getRiskPercent() {
        double pct = _dTotalRisk / _dTotalCost;
        if (_dTotalCost == 0)
            pct = 0;
        return pct;
    }

    //calculate total profit
    double calcProfitLossAmount() {
        _dTotalCost = 0;
        for (int i=0; i<getRowCount(); i++) {
            Object dir = getCell(i, COLUMN_DIRECTION).getValue();//todo handle short transaction differently
            if (!dir.equals("")) {//non-empty row
                double entry_price = (Double) getCell(i, COLUMN_ENTRY_PRICE).getValue();
                long shares = (Long) getCell(i, COLUMN_SHARES).getValue();
                _dTotalCost += shares * entry_price;
            }
        }
        _dProfitLossAmount = _dMarketValue - _dTotalCost;
        _dProfitLossPct = _dProfitLossAmount / _dTotalCost;
        return _dProfitLossAmount;
    }
    double getProfitLossAmount() { return _dProfitLossAmount; }

    //calculate profit or loss percentage
    double getProfitLossPercent() {
        return _dProfitLossPct;//must be called after calcProfitLossAmount()
    }

    //cash
    double getCashPercent() { return _dCashPercent; }

    //max pullback
    double getMaxPullbackAmount() { return _dMaxPullbackAmount; }
    double getMaxPullbackPercent() { return _dMaxPullbackPercent; }

    //update status bar values
    void calcStatusBarFields() {
        calcTotalRisk();
        calcMarketValue();
        calcProfitLossAmount();
        RiskMgrFrame rmf = ((MdiMainFrame) RiskMgrModel.getInstance().getParent()).findRiskMgrFrame();
        if (rmf != null) {
//            rmf.getMainPanel().updateSummary(
//                _dTotalRisk, _dMarketValue, getRiskPercent(), getProfitLossAmount(),
//                getProfitLossPercent(), _dTotalAdjRisk, getAdjPercent(), getTotalCost(),
//                getCashPercent(), getMaxPullbackAmount(), getMaxPullbackPercent());
//            double risk_pct = getRiskPercent();
//            rmf.getMainPanel().getActiveTab().setRisk(
//                FrameworkConstants.ROI_FORMAT.format(risk_pct/* < 0 ? -risk_pct : risk_pct*/),
//            risk_pct > 0);
        }
    }

    void setCashAmount(double cash) {
        _dCashAmount = cash;
        _dCashPercent = cash /  (_dMarketValue + _dCashAmount);
    }
    double getCashAmount() { return _dCashAmount; }

    //number of open positions: sequence number of last symbol
    int getPositionCount() {
        String seq = (String)getCell(getRowCount() - 1, COLUMN_SEQUENCE).getValue();
        return Integer.parseInt(seq);
    }

    //-----private methods-----
    //calculate target price cell using entry price and target method, assuming _lstRows updated
    private void calcTargetPrice(int row) {
        String method = (String)getCell(row, COLUMN_TARGET_METHOD).getValue();
        double entry_price = (Double)getCell(row, COLUMN_ENTRY_PRICE).getValue();
        double tgt_price = entry_price * (1 + DEFAULT_TARGET_PCT);
        if (method.equals(LIST_TARGET_METHOD[1]))
            tgt_price = entry_price * (1 + TARGET_2_PCT);
        else if (method.equals(LIST_TARGET_METHOD[2]))
            tgt_price = entry_price * (1 + TARGET_3_PCT);
        else if (method.equals(LIST_TARGET_METHOD[3]))
            tgt_price = entry_price * (1 + TARGET_4_PCT);

        //None case
        else if (method.equals(LIST_TARGET_METHOD[4]))
            tgt_price = -1;//let renderer do it
        getCell(row, COLUMN_TARGET_PRICE).setValue(new Double(tgt_price));
    }

    //calculate break even value
//TODO sli has same method, merge them, don't duplicate
    private float calcBreakEven(double entry_price, long shares) {
        return (float)(entry_price * shares + 0.01 * 2 * shares + 3) / shares;//commision 1c/share + $3 fee
    }

//todo this part maybe just list lookup, no calculation needed any more stopInfo.stoplevels
    //calculate stop price cell using entry price and stop method
    private float calcStopPrice(int row) {
        String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
        String method_id = (String)getCell(row, COLUMN_STOP_METHOD).getValue();
        double entry_price = (Double)getCell(row, COLUMN_ENTRY_PRICE).getValue();
        double stop_price = entry_price * (1 - DEFAULT_STOP_PCT);//default to start
        long shares = (Long)getCell(row, COLUMN_SHARES).getValue();
        StopLevelInfo sli = stopInfo.get(sym);
        int method_index = sli.findMethodListIndex(method_id);
        double close = (Double)getCell(row, COLUMN_CURRENT_PRICE).getValue();

        //Percent method
        if (method_index >= StopLevelInfo.STOP_PCT_BEGIN_INDEX && method_index <= StopLevelInfo.STOP_PCT_END_INDEX){
            stop_price = entry_price * (1 + StopLevelInfo.LIST_STOP_PERCENT[method_index]);
            if (method_index == StopLevelInfo.BREAK_EVEN_INDEX)
                stop_price = calcBreakEven(entry_price, shares);
            getCell(row, COLUMN_STOP_PRICE).setValue(new Double(stop_price));
        }

        //ATR method
        else if (method_index >= StopLevelInfo.STOP_ATR_BEGIN_INDEX && method_index <= StopLevelInfo.STOP_ATR_END_INDEX) {
            double mul_factor = (Double)getCell(row, COLUMN_ATR_MULTIPLIER).getValue();
            Double stop_distance = (Double)getCell(row, COLUMN_ATR).getValue() * mul_factor;
            stop_price = close - stop_distance;
            getCell(row, COLUMN_STOP_PRICE).setValue(new Double(stop_price));
        }

        //SWP method
        else if (method_index >= StopLevelInfo.STOP_SWP_BEGIN_INDEX && method_index <= StopLevelInfo.STOP_SWP_END_INDEX) {
            int idx = method_index - StopLevelInfo.STOP_SWP_BEGIN_INDEX; //offset from beginning into swingPoints array
            ArrayList<FundQuote> swingPoints = stopInfo.get(sym).getSwingPoints();
            float low = swingPoints.get(/*getCell(row, COLUMN_SYMBOL).getValue()).get(*/idx).getLow();
            getCell(row, COLUMN_SWP).setValue(new Double(low));
            stop_price = low - IndicatorUtil.calcAfc(low);
            getCell(row, COLUMN_STOP_PRICE).setValue(new Double(stop_price));
        }
        else {//custom
            stop_price = (Double)getCell(row, COLUMN_STOP_PRICE).getValue();
        }
        return (float)stop_price;
    }

    //calculate risk from shares, entry and stop, assuming they are already in _lstRows
    private void calcRisk(int row) {
        long shares = (Long)getCell(row, COLUMN_SHARES).getValue();
        double entry_price = (Double)getCell(row, COLUMN_ENTRY_PRICE).getValue();
        double stop_price = (Double)getCell(row, COLUMN_STOP_PRICE).getValue();
        double risk = (stop_price - entry_price) * shares;
        getCell(row, COLUMN_TRADE_RISK).setValue(risk);
        double cost = shares * entry_price;
        double pct = risk / cost;
        getCell(row, COLUMN_RISK_PERCENT).setValue(pct);
    }

    //calculate cur equity, P/L from cells
    private void calcEquityPL(int row) {
        long shares = (Long)getCell(row, COLUMN_SHARES).getValue();
        double entry_price = (Double)getCell(row, COLUMN_ENTRY_PRICE).getValue();
        double cur_price = (Double)getCell(row, COLUMN_CURRENT_PRICE).getValue();
        getCell(row, COLUMN_CURRENT_EQITY).setValue(cur_price * shares);
        double pl = shares * (cur_price - entry_price);
        getCell(row, COLUMN_GAIN_LOSS_PERCENT).setValue(cur_price / entry_price - 1);
        getCell(row, COLUMN_GAIN_LOSS_AMOUNT).setValue(pl);
    }

    //find the last non-blank row, -1 means all blank rows, >=0 refers to index of row
    private int getLastNonEmptyRow() {
        for (int row = getRowCount() - 1; row >= 0; row--) {
            Object seq = getCell(row, COLUMN_SEQUENCE).getValue();
            if (seq.equals(""))
                continue;

            return row;
        }
        return -1;
    }

    //re-calculate sequence number for the entire table
    private void calcSequence() {
        //recalculate sequence number, skip blank rows
        int seq = 1;
        for (int i=0; i<getRowCount(); i++) {
            Object dir = getCell(i, COLUMN_DIRECTION).getValue();
            if (!dir.equals(""))
                getCell(i, COLUMN_SEQUENCE).setValue(String.valueOf(seq++));
        }
    }

    //initially current stop are 0s during CTOR, after populate(), call this to set each symbol's current stop in stopInfo
    private void updateCurrentStop() {
        for (int row = 0; row < getRowCount(); row++) {
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            if (sym.equals(""))
                continue;

            double cur_stop = (Double)getCell(row, COLUMN_STOP_PRICE).getValue();
            StopLevelInfo sli = stopInfo.get(sym);
            sli.setStop((float)cur_stop);
            sli.sort();
        }
    }

    private void refreshGraph(int row) {
        RiskMgrFrame rmf = ((MdiMainFrame) RiskMgrModel.getInstance().getParent()).findRiskMgrFrame();
//        if (rmf != null)
//            rmf.getMainPanel().getActiveTab().plotPriceData(getStopLevelInfo((String) getCell(row, COLUMN_SYMBOL).getValue()));
    }

    //based on stop method, decorate ATR, SWP cells dynamically for a given row
    private void decorateRow(int row) {
        getCell(row, COLUMN_SHARES).setEnableHighlight(true, true);
        getCell(row, COLUMN_ENTRY_DATE).setEnableHighlight(true, true);
        getCell(row, COLUMN_ENTRY_PRICE).setEnableHighlight(true, true);
        getCell(row, COLUMN_STOP_METHOD).setEnableHighlight(true, true);
        getCell(row, COLUMN_CURRENT_PRICE).setEnableHighlight(true, true);
        getCell(row, COLUMN_TARGET_METHOD).setEnableHighlight(true, true);
        getCell(row, COLUMN_NOTES).setEnableHighlight(true, true);

        //column interaction: change stop method decorate ATR, SWP cells differently
        String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
        String method = (String)getCell(row, COLUMN_STOP_METHOD).getValue();
        int method_index = stopInfo.get(sym).findMethodListIndex(method);
        boolean custom_stop = method_index == StopLevelInfo.STOP_CUSTOM_INDEX;
        boolean atr_stop = method_index == StopLevelInfo.STOP_ATR_BEGIN_INDEX && method_index <= StopLevelInfo.STOP_ATR_END_INDEX;
        boolean swp_stop = method_index >= StopLevelInfo.STOP_SWP_BEGIN_INDEX && method_index <= StopLevelInfo.STOP_SWP_END_INDEX;
        SimpleCell atr_cell = getCell(row, COLUMN_ATR);
        SimpleCell atr_mul_cell = getCell(row, COLUMN_ATR_MULTIPLIER);
        SimpleCell swp_cell = getCell(row, COLUMN_SWP);
        SimpleCell stop_cell = getCell(row, COLUMN_STOP_PRICE);

        //default to custom stop, all gray out
        atr_cell.setEnabled(false);
        atr_mul_cell.setEnabled(false);
        swp_cell.setEnabled(false);
        stop_cell.setHighlight(false);
        if (atr_stop) {
            atr_cell.setEnableHighlight(true, false);
            atr_mul_cell.setEnableHighlight(true, true);
            swp_cell.setEnableHighlight(false, false);
        }
        else if (swp_stop) {
            swp_cell.setEnableHighlight(true, false);
        }
        else if (custom_stop) {
            stop_cell.setEnableHighlight(true, true);
        }
    }

    //-----instance variables-----
    private double _dTotalRisk, _dTotalAdjRisk, _dMarketValue, _dCashAmount, _dCashPercent, _dStopEquity,
        _dMaxPullbackAmount, _dMaxPullbackPercent, _dProfitLossAmount, _dProfitLossPct, _dTotalCost,
        _dTotalAdjCost;
    private int _nATRLength = DEFAULT_ATR_LENGTH;
    public void setAtrLength(int len) {
        _nATRLength = len;
    }
    public int getAtrLength() { return _nATRLength; }

    private int _nQuoteLookback = DEFAULT_QUOTE_LOOKBACK;
    public void setQuoteLookback(int look_back) {
        _nQuoteLookback = look_back;
    }
    public int getQuoteLookback() { return _nQuoteLookback; }

    private double _dQtrAdjFactor = DEFAULT_QUARTERLY_ADJUSTMENT;
    public void setQtrAdjFactor(double adj) {
        _dQtrAdjFactor = adj;
    }
    public double getQtrAdjFactor() { return _dQtrAdjFactor; }

    ArrayList<FundQuote> getSwingPoints(String symbol) {
        return stopInfo.get(symbol).getSwingPoints();
    }

    private HashMap<String, StopLevelInfo> stopInfo = new HashMap<String, StopLevelInfo>();
    StopLevelInfo getStopLevelInfo(String symbol) {
        return stopInfo.get(symbol);
    }

    private boolean _bDirty;
    public boolean isDirty() {
        return _bDirty;
    }
    public void setDirty(boolean _bDirty) {
        this._bDirty = _bDirty;
    }

    //-----literals-----
    private static final int DEFAULT_ATR_LENGTH = 14;
    private static final int DEFAULT_QUOTE_LOOKBACK = 60;
    private static final double DEFAULT_QUARTERLY_ADJUSTMENT = 1.5;//percent per quarter
    private static final double DEFAULT_TARGET_PCT = 0.08;
    private static final double TARGET_2_PCT = 0.15;
    private static final double TARGET_3_PCT = 0.25;
    private static final double TARGET_4_PCT = 0.50;
    private static final double DEFAULT_STOP_PCT = 0.05;
            static final int COLUMN_SEQUENCE = 0;
            static final int COLUMN_DIRECTION = 1;
            static final int COLUMN_SYMBOL = 2;
            static final int COLUMN_SHARES = 3;
            static final int COLUMN_ENTRY_DATE = 4;
            static final int COLUMN_ENTRY_PRICE = 5;
            static final int COLUMN_STOP_METHOD = 6;//-5% or fixed value
            static final int COLUMN_ATR = 7;
            static final int COLUMN_ATR_MULTIPLIER = 8;
            static final int COLUMN_SWP = 9;
            static final int COLUMN_STOP_PRICE = 10;
            static final int COLUMN_TRADE_RISK = 11;//risk from cost in terms of dollar amount
            static final int COLUMN_RISK_PERCENT = 12;//risk from cost in terms of percent
            static final int COLUMN_CURRENT_PRICE = 13;
            static final int COLUMN_CURRENT_EQITY = 14;
            static final int COLUMN_GAIN_LOSS_PERCENT = 15;
            static final int COLUMN_GAIN_LOSS_AMOUNT = 16;
            static final int COLUMN_TARGET_METHOD = 17;
            static final int COLUMN_TARGET_PRICE = 18;
            static final int COLUMN_NOTES = 19;
//todo: add adjusted risk: original risk plus basis (fixed income return)

    private static final String[] LIST_TRADE_TYPE = { "Long", "Short" };
    private static final String[] LIST_TARGET_METHOD = {"8 %", "15 %", "25 %", "50 %", "None" };
//todo get rid of internal translation for target too
    static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_13"),  ColumnTypeEnum.TYPE_STRING, -1,  20, null, null, null },//0, sequence
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_2"),  ColumnTypeEnum.TYPE_STRING, -1,  30, new ComboCellEditor(new JComboBox(LIST_TRADE_TYPE)), null, null },//1, direction long/short
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_3"),  ColumnTypeEnum.TYPE_STRING, -1,  60, new NameCellEditor(false), null, null },//2, symbol
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_4"),  ColumnTypeEnum.TYPE_LONG,   -1,  60, new LongIntegerCellEditor(100, 0, 1, 5000), null, null },//3, shares
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_5"),  ColumnTypeEnum.TYPE_STRING, -1, 100, null, null, null },//4, entry date
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_6"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  80, new DecimalCellEditor(0, 3, 0, 1000, null), null, null },//5, entry price
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_11"), ColumnTypeEnum.TYPE_STRING, -1, 100, new ComboCellEditor(new JComboBox(StopLevelInfo.LIST_STOP_TYPES)), null, null },//6, stop method
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_18"), ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null },//7, ATR
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_20"), ColumnTypeEnum.TYPE_DOUBLE, -1, 100, new DecimalCellEditor(0, 3, 0, 1000, null), null, null },//8, ATR Multipler
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_19"), ColumnTypeEnum.TYPE_DOUBLE, -1,  60, new DecimalCellEditor(0, 3, 0, 1000, null), null, null },//9, SWP
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_14"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  60, new DecimalCellEditor(0, 3, 0, 1000, null), null, null },//10, stop price
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_15"), ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null },//11, trade risk
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_17"), ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null },//12, adjusted risk
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_13"), ColumnTypeEnum.TYPE_DOUBLE, -1,  80, new DecimalCellEditor(0, 3, 0, 1000, null), null, null },//13, cur price
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_14"), ColumnTypeEnum.TYPE_DOUBLE, -1,  80, null, null, null },//14, cur equity
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_15"), ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null },//15, P/L %
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_16"), ColumnTypeEnum.TYPE_DOUBLE, -1,  80, null, null, null },//16, P/L $
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_10"), ColumnTypeEnum.TYPE_STRING, -1, 100, new ComboCellEditor(new JComboBox(LIST_TARGET_METHOD)), null, null },//17, target method
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_7"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null },//18, target price
        { ApolloConstants.APOLLO_BUNDLE.getString("active_col_21"), ColumnTypeEnum.TYPE_STRING,  0, 300, new PlainCellEditor(), null, null },//19, notes
    };
}

//todo: later adjust risk = risk * (1 + adjustment) * number of years
//        try {
//            String start_date = (String)getCell(row, COLUMN_ENTRY_DATE).getValue();
//            String cur_date = AppUtil.calendarToString(Calendar.getInstance());
//            double num_yr = AppUtil.calcYear(start_date, cur_date);
//            entry_price += entry_price * _dQtrAdjFactor * 4 * num_yr;//raise entry price
//            double adj_risk = (entry_price - stop_price) * shares;
//            getCell(row, COLUMN_RISK_PERCENT).setValue(adj_risk);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
