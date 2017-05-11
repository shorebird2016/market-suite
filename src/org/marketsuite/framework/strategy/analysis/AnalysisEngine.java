package org.marketsuite.framework.strategy.analysis;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.framework.model.FrameworkModel;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.model.FrameworkModel;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Process user provided trading signals to perform uniform analysis and results.
 */
public class AnalysisEngine extends AbstractEngine {
    public boolean simulate() {
        return false;
    }
    public void simulate(String start_date, String end_date) { }
    private SimParam _param;
    public SimParam getParams() { return _param; }
    public void setSimParam(SimParam param) { _param = param; }
    public String getId() { return FrameworkConstants.FRAMEWORK_BUNDLE.getString("ana_lbl_1"); }
    public String getStrategy() { return strategy; }
    public String getStrategyInfo() { return strategyInfo; }
    public boolean isBuySetup() { return false; }
    public boolean isBuyTrigger() { return false; }
    public String getBuyTriggerDate() { return null; }
    public boolean isSellSetup() { return false; }
    public boolean isSellTrigger() { return false; }
    public String getSellTriggerDate() { return null; }

    //override this to accommodate different formats (eg. CLEC 1 trans log, MDB has 4 segments)
    public SimReport genReport() {
        try {
            switch (_nLogType) {
                case FrameworkConstants.FILE_FORMAT_CLEC:
                case FrameworkConstants.FILE_FORMAT_SMT:
                case FrameworkConstants.FILE_FORMAT_CUSTOM:
                default:
                    return new SimReport(_Transactions, true);//true = one symbol

                case FrameworkConstants.FILE_FORMAT_TEA_LEAF://todo: presently it doesn't handle multiple symbols..........
                    return new SimReport(_Transactions, false);

                //for transactions with multiple symbols, annual return can not be easily calculated todo: presently it doesn't handle multiple symbols..........
                case FrameworkConstants.FILE_FORMAT_MDB:
                    return new SimReport(_MdbLogs.get(_sSegment), false);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Transaction> getTransactionLog() {
        switch (_nLogType) {
            case FrameworkConstants.FILE_FORMAT_CLEC:
                return super.getTransactionLog();

            case FrameworkConstants.FILE_FORMAT_MDB:
                return _MdbLogs.get(_sSegment);
        }
        return super.getTransactionLog();    //To change body of overridden methods use File | Settings | File Templates.
    }

    //simply read the file and construct into transaction log, look up daily open price in memory
    // file type CLEC: direction(LONG/SHORT), Entry Date, Exit Date
    public ArrayList<Transaction> extractLog(String file_path, int file_type) throws IOException, ParseException {
        _nLogType = file_type;
        switch (file_type) {
            case FrameworkConstants.FILE_FORMAT_CLEC:
                return extractClecLog(file_path);

            case FrameworkConstants.FILE_FORMAT_MDB:
                _MdbLogs = extractMdbLog(file_path);
                return _MdbLogs.get(_sSegment);

            case FrameworkConstants.FILE_FORMAT_SMT:
                return extractSmtLog(file_path);

            case FrameworkConstants.FILE_FORMAT_TEA_LEAF:
                return extractTeaLeafLog(file_path);

            case FrameworkConstants.FILE_FORMAT_CUSTOM:
                return extractCustomLog(file_path);
        }
        return null;
    }

    //extract transaction log from CLEC format files into a transaction array
    public ArrayList<Transaction> extractClecLog(String file_path) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        _Transactions = new ArrayList<Transaction>();
        strategyInfo = "";

        //skip all comment lines
        String line;
        while ( (line = br.readLine()) != null ) {
            if (line.startsWith("#")) //comments
                continue;

            //CLEC format should not start with FORMAT=
            else if (line.startsWith("FORMAT=")) {
                if (!batchMode)
                    MessageBox.messageBox(FrameworkModel.getMainFrame(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_6") + " " + line,
                        MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE
                    );
                return null;
            }
            else if (line.startsWith("SYMBOL=")) {//todo move literals to constants....
                String[] tokens = line.split("=");
                if (!_sSymbol.equals(tokens[1])) {
                    if (!batchMode)
                        MessageBox.messageBox(FrameworkModel.getMainFrame(),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_002") + " " + _sSymbol + " " +
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_003") + tokens[1] + " " +
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_004"),
                            MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE
                        );
                    return null;
                }
                _sSymbol = tokens[1];
            }
            else if (line.startsWith("STRATEGY=")) {
                String[] tokens = line.split("=");
                strategy = tokens[1];
            }
            else if (line.startsWith("STRATEGY_INFO")) {
                String[] tokens = line.split("=");
                strategyInfo = tokens[1];
            }
            else if (line.startsWith("TIME_FRAME=")) {
                String[] tokens = line.split("=");
                _bDailyTimeFrame = tokens[1].equals("DAILY");
            }
            else if (line.startsWith("PRICE_KEY=")) {
                String[] tokens = line.split("=");
                _sEntryPriceKey = tokens[1].trim();
            }

            //parse each line into a transaction
            else if (line.startsWith("LONG,") || line.startsWith("SHORT,")) {
                String[] tokens = line.split(",");
                //if key is OPEN, look up daily open price, create transaction with it
                Transaction tr;
                float entry_price;
                float exit_price;
                if (_sEntryPriceKey.equals("OPEN")) {
                    //get month, day, year from token[1] and token[2]
                    entry_price = getQuote(true, tokens[1]);
                    exit_price = getQuote(true, tokens[2]);
                }
                else if (_sEntryPriceKey.equals("CLOSE")) {
                    entry_price = getQuote(false, tokens[1]);
                    exit_price = getQuote(false, tokens[2]);
                }
                else {//custom
                    entry_price = Float.parseFloat(tokens[3]);
                    exit_price = Float.parseFloat(tokens[4]);
                }
                tr = new Transaction(_sSymbol, tokens[1], tokens[2], entry_price, exit_price);
                if (line.startsWith("SHORT,")) {//short, reverse entry exit price
                    tr.setLongTrade(false);
                    tr.calcPerformance();
                }
                tr = adjustPerformance(tr);
//                //if stop loss is engaged and performance is worse than stop, fix performance at stop level
//                if (_param != null) {//use stop loss
//                    float pf = tr.getPerformance();
//                    if (pf < -_param.getInitialStop())
//                        tr.setPerformance(-(float)_param.getInitialStop());
//                }
                _Transactions.add(tr);
            }
            //error checking
            else {
                if (!batchMode)
                    MessageBox.messageBox(
                        FrameworkModel.getMainFrame(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_007") + line,
                        MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                return null;
            }
        }
        return _Transactions;
    }

    //extract log from MDB format into a hash table, table keys = S1,S2,S3,S4; each entry contains transaction log
    // for each segment S1,S2,S3,S4
    public HashMap<String, ArrayList<Transaction>> extractMdbLog(String file_path) throws IOException, ParseException {
        HashMap<String, ArrayList<Transaction>> ret = new HashMap<String, ArrayList<Transaction>>();
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        _Transactions = new ArrayList<Transaction>();
        String line = br.readLine();
        if (!batchMode)
            if (!line.startsWith("FORMAT=MDB")) {
                MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("tab_msg_1"));
                return null;
            }

        int count = 0;
        while ( (line = br.readLine()) != null ) {
            if (count++ < 4)
                continue;//skip first four lines

            line = preProcess(line);//remove quoted numbers and commas within

            //token is comma for csv files
            float entry_price;
            float exit_price;
            String[] tokens = line.split(",");
            if (tokens.length < 27) //if line ends ",,,,," pattern then last few won't be tokenized
                continue;

            //interestingly ,,,,,,,, kind of line returns null
            if (tokens == null || tokens.length == 0 || tokens[2] == null || tokens[2].length() == 0) //end of data rows
                break;

            String segment = tokens[23];//S1 - S4
            if (!segment.equals("")) {
                //process line, create transaction object
                //convert dates eg. 5/1/2006 to yahoo 2006-05-01
                Date dt = DATE_FMT.parse(tokens[5]);
                Calendar entry_cal = Calendar.getInstance();
                entry_cal.setTime(dt);
                dt = DATE_FMT.parse(tokens[11]);
                Calendar exit_cal = Calendar.getInstance();
                exit_cal.setTime(dt);
                String tk1 = tokens[6].trim();
                entry_price = Float.parseFloat(tk1.substring(1, tk1.length()));//skip leading $ sign
                tk1 = tokens[12].trim();
                exit_price = Float.parseFloat(tk1.substring(1, tk1.length()));
                Transaction tr = new Transaction(segment, AppUtil.calendarToString(entry_cal),
                    AppUtil.calendarToString(exit_cal), entry_price, exit_price);
                tr = adjustPerformance(tr);
                //look up from hash map
                if (!ret.containsKey(segment)) //not in map
                    ret.put(segment, new ArrayList<Transaction>());
                ret.get(segment).add(tr);//add to array
            }
        }
        return ret;
    }

    //set transaction log to a new object
    public void setTransactionLog(ArrayList<Transaction> trans) { _Transactions = trans; }

    //extract special format from SMT and generate transaction log
    private ArrayList<Transaction> extractSmtLog(String file_path) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        ArrayList<Transaction> raw = new ArrayList<Transaction>();

        //first two lines are specification
        String line = br.readLine();
        if (!line.startsWith("FORMAT=SMT")) {//todo move string literals to constants....
            if (!batchMode)
                MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_6") + " " + line);
            return null;
        }

        //skip all comment lines
        while ( (line = br.readLine()) != null ) {
            if (line.startsWith("#")) //comments
                continue;

            else if (line.startsWith("SYMBOL")) {//save symbol of this file
                String[] tokens = line.split("=");
                _sSymbol = tokens[1];
            }

            //parse each line into a transaction, ignoring line with "Cash"
            else if (line.startsWith("Long") || line.startsWith("Short")) {
                String[] tokens = line.split(",");

                //second column contains entry date and price
                String[] dp = tokens[1].split(" ");
                Calendar cal = Calendar.getInstance();
                cal.setTime(DATE_FMT.parse(dp[0]));
                String entry_date = AppUtil.calendarToString(cal);
                float entry_price = Float.parseFloat(dp[2]);
                //third column exit date/price
                dp = tokens[2].split(" ");
                cal = Calendar.getInstance();
                cal.setTime(DATE_FMT.parse(dp[0]));
                String exit_date = AppUtil.calendarToString(cal);
                float exit_price = Float.parseFloat(dp[2]);

                //if key is OPEN, look up daily open price, create transaction with it
                Transaction tr;
                tr = new Transaction(_sSymbol, entry_date, exit_date, entry_price, exit_price);
                if (line.startsWith("Short")) {//short, reverse entry exit price
                    tr.setLongTrade(false);
                    tr.calcPerformance();
                }
                tr = adjustPerformance(tr);
                raw.add(tr);
            }
        }

        //reverse transaction to ascending dates, raw is in descending order
        _Transactions = new ArrayList<Transaction>();
        for (int idx = raw.size() - 1; idx >= 0; idx--)
            _Transactions.add(raw.get(idx));
        return _Transactions;
    }

    //extract special format from SMT and generate transaction log
    private ArrayList<Transaction> extractTeaLeafLog(String file_path) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file_path));
       _Transactions = new ArrayList<Transaction>();

        //first two lines are specification
        String line = br.readLine();
        if (!batchMode)
            if (!line.startsWith("FORMAT=Tea Leaf")) {//todo move string literals to constants....
                MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_6") + " " + line);
                return null;
            }

        //skip all comment lines
        while ( (line = br.readLine()) != null ) {
            if (line.startsWith("#")) //comments skip
                continue;

            //parse each line into a transaction, each line probably have different symbol
            String[] tokens = line.split(",");
            _sSymbol = tokens[0];

            //second column contains entry date
            Calendar cal = Calendar.getInstance();
            cal.setTime(DATE_FMT.parse(tokens[1]));
            String entry_date = AppUtil.calendarToString(cal);
            cal = Calendar.getInstance();
            cal.setTime(DATE_FMT.parse(tokens[2]));
            String exit_date = AppUtil.calendarToString(cal);
            float entry_price = NUMBER_FMT.parse(tokens[4]).floatValue();
            float exit_price = NUMBER_FMT.parse(tokens[5]).floatValue();

            //if key is OPEN, look up daily open price, create transaction with it
            Transaction tr;
            tr = new Transaction(_sSymbol, entry_date, exit_date, entry_price, exit_price);
            tr = adjustPerformance(tr);
            _Transactions.add(tr);
        }
        return _Transactions;
    }

    //extract special CUSTOM format and generate transaction log
    public ArrayList<Transaction> extractCustomLog(String file_path) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        _Transactions = new ArrayList<>();
        strategyInfo = "";

        //first line must start with FORMAT property
        String line = br.readLine();
        if (!batchMode)//skip checking during batch mode
            if (!line.startsWith("FORMAT=CUSTOM")) {
                MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_6") + " " + line);
                return null;
            }

        //skip all comment lines
        FundData fund = null;
        while ( (line = br.readLine()) != null ) {
            //skip comments and blank lines
            if (line.startsWith("#") || line.equals("") || line.startsWith("FORMAT"))
                continue;

            else if (line.startsWith("SYMBOL=")) {
                String[] tokens = line.split("=");
                _sSymbol = tokens[1];
                fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, _sSymbol);
//                if (!_sSymbol.equals(tokens[1])) {//warn if symbol doesn't match file name
//                    if (!batchMode)
//                        MessageBox.messageBox(FrameworkModel.getMainFrame(),
//                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_002") + " " + _sSymbol + " " +
//                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_003") + tokens[1] + " " +
//                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_004"),
//                            MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE
//                        );
//                    return null;
//                }
            }
            else if (line.startsWith("STRATEGY=")) {
                String[] tokens = line.split("=");
                    strategy = tokens[1];//right side
            }
            else if (line.startsWith("STRATEGY_INFO")) {
                String[] tokens = line.split("=");
                strategyInfo = tokens[1];
            }
            else if (line.startsWith("TIME_FRAME=")) {
                String[] tokens = line.split("=");
                _bDailyTimeFrame = tokens[1].equals("DAILY");
            }
            else if (line.startsWith("ENTRY_PRICE_KEY=")) {
                String[] tokens = line.split("=");
                _sEntryPriceKey = tokens[1].trim();
            }
            else if (line.startsWith("EXIT_PRICE_KEY=")) {
                String[] tokens = line.split("=");
                _sExitPriceKey = tokens[1].trim();
            }

            //parse each line into a transaction
            else {
                String[] tokens = line.split(",");

                //if key is OPEN, look up daily open price of entry date, create transaction with it
                Transaction tr;
                float entry_price;
                float exit_price;
                try {
                    if (_sEntryPriceKey.equals("CUSTOM"))
                        entry_price = Float.parseFloat(tokens[1]);
                    else if (_sEntryPriceKey.equals("OPEN"))
                        entry_price = getPrice(fund, true, tokens[0]);
                    else //use close
                        entry_price = getPrice(fund, false, tokens[0]);
                    if (_sExitPriceKey.equals("CUSTOM"))
                        exit_price = Float.parseFloat(tokens[3]);
                    else if (_sExitPriceKey.equals("OPEN"))
                        exit_price = getPrice(fund, true, tokens[2]);
                    else //use close
                        exit_price = getPrice(fund, false, tokens[2]);
                    tr = new Transaction(_sSymbol, tokens[0], tokens[2], entry_price, exit_price);
                    tr = adjustPerformance(tr);
                    _Transactions.add(tr);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (tokens != null && tokens.length > 0)
                        System.out.println(_sSymbol + ": Bad Data at " + tokens[0]);
                }
            }
            //error checking
//            else {
//                if (!batchMode)
//                    MessageBox.messageBox(
//                            FrameworkModel.getMainFrame(),
//                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_007") + line,
//                            MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
//                return null;
//            }
        }
        return _Transactions;
    }

    //look up price of a given date, use_open = true, read back opening price, otherwise closing price
    private float getPrice(FundData fund, boolean use_open, String date) throws Exception {
        int index = fund.findIndexByDate(date);
        if (use_open)
            return fund.getQuote().get(index).getOpen();
        else
            return fund.getQuote().get(index).getClose();
    }

    //retrieve quote from YAHOO, tokens = data file line split
    private float getQuote(boolean for_open, String date_str) throws IOException {
        String[] date_tokens = date_str.split("-"); //eg 2011-05-23
        FundQuote quote = AppUtil.getYahooQuote(_sSymbol, "d", Integer.parseInt(date_tokens[1]) - 1,
                Integer.parseInt(date_tokens[2]), Integer.parseInt(date_tokens[0]));
        return for_open ? quote.getOpen() : quote.getClose();
    }

    //pre-process MDB data line to remove quoted numbers with comma (which messes up tokenizer)
    private String preProcess(String raw_line) {
        StringBuilder ret = new StringBuilder();
        boolean inside_quote = false;
        for (int cnt = 0; cnt < raw_line.length(); cnt++) {
            char c = raw_line.charAt(cnt);
            if (inside_quote) {
                if (c == ',')
                    continue;//skip this too
                else if (c == '"')
                    inside_quote = false;
                ret.append(c);
            }
            else {//outside quote
                if (c == '"') {
                    inside_quote = true;//skip this
                }
                ret.append(c);
            }
        }
        return ret.toString();
    }

    //adjust when using stop loss
    private Transaction adjustPerformance(Transaction trans) {
        //if stop loss is engaged and performance is worse than stop, fix performance at stop level
        if (_param != null) {//use stop loss
            float pf = trans.getPerformance();
            if (pf < -_param.getInitialStop())
                trans.setPerformance(-(float)_param.getInitialStop());
        }
        return trans;
    }

    //----instance variable/accessor----
    private String _sSymbol;
    public String getSymbol() {
        return _sSymbol;
    }
    public void setSymbol(String sym) {
        _sSymbol = sym;
    }

    private String _sEntryPriceKey;
    public String getEntryPriceKey() {
        return _sEntryPriceKey;
    }

    private String _sExitPriceKey;
    public String getExitPriceKey() {
        return _sExitPriceKey;
    }

    private String _sSegment;//for MDB
    public String getSegment() { return _sSegment; }
    public void setSegment(String seg_name) { _sSegment = seg_name; }

    private boolean _bDailyTimeFrame;//false = weekly
    private HashMap<String, ArrayList<Transaction>> _MdbLogs;
    private int _nLogType = FrameworkConstants.FILE_FORMAT_CLEC;

    private String strategy, strategyInfo;

    //-----literals-----
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MM/dd/yy");
    private static final DecimalFormat NUMBER_FMT = new DecimalFormat("$##.##");
    //unit test
    public static void main(String[] args) {//todo 2007, 2008
        try {
            ArrayList<Transaction> ret = new AnalysisEngine().
                extractSmtLog("/Users/shorebird2011/Documents/02 - Dev/database/export/SMT/DIA-RAW.csv");
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
