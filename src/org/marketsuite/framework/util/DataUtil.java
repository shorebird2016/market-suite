package org.marketsuite.framework.util;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.model.data.EarningInfo;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.type.Ibd50State;
import org.marketsuite.framework.model.type.ImportFileType;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevel;
import org.marketsuite.riskmgr.model.StopLevelInfo;
import jxl.*;
import jxl.read.biff.BiffException;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.model.*;
import org.marketsuite.framework.model.data.EarningInfo;
import org.marketsuite.framework.model.type.Ibd50State;
import org.marketsuite.framework.model.type.ImportFileType;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevel;
import org.marketsuite.riskmgr.model.StopLevelInfo;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * helpers to manage data files.
 */
public class DataUtil {
    //========== This group for quote database ==========
    //read entire quote database of this symbol, each line of .txt contains date, open, high, low, close, volume, adjust (YAHOO format)
    public static FundData readFundHistory(String path, String fund_symbol) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path + File.separator + fund_symbol + FrameworkConstants.EXTENSION_QUOTE));
        FundData fd = new FundData(fund_symbol);
        String line = "";
        StringTokenizer st = null;
        int line_num = 1, tok_num = 1;
        //read comma separated file line by line
        while ( (line = br.readLine()) != null ) {
            if (line_num == 1) {
                line_num++;
                continue;//skip first header row
            }
            //parse line
            FundQuote quote = new FundQuote(fund_symbol);//each line is one quote
            quote.setIndex(line_num - 2);//convenient storage
            st = new StringTokenizer(line, ",");
            while(st.hasMoreTokens()) {
                switch(tok_num) {
                    case 1: //date, covert string
                        String date = st.nextToken();
                        quote.setDate(date);
                        break;

                    case 2: //open
                        quote.setOpen(Float.parseFloat(st.nextToken()));
                        break;

                    case 3: //high
                        quote.setHigh(Float.parseFloat(st.nextToken()));
                        break;

                    case 4: //low
                        quote.setLow(Float.parseFloat(st.nextToken()));
                        break;

                    case 5: //close
                        float close = Float.parseFloat(st.nextToken());
                        quote.setClose(close);
                        quote.setUnAdjclose(close);
//todo rid of this un-adj later
                        break;

                    case 6://volume
                        quote.setVolume(Float.parseFloat(st.nextToken()));
                        break;

                    case 7: //adjusted close
                        float adj_close = Float.parseFloat(st.nextToken());
//todo separate adj close away from close
//                        quote.setAdjClose(adj_close);
                        quote.setClose(adj_close);
                        break;
                }
                tok_num++;
                if (tok_num > 7) {
                    fd.addQuote(quote);
                    break;
                }
            }
            line_num++;
            tok_num = 1;
        }
        return fd;
    }

    /**
     * Read raw quotes from symbol.txt file up to specified number of days. A smaller object is returned.
     * @param path of symbol.txt
     * @param fund_symbol target
     * @param num_days first row is most recent, retrieve num_days worth of quotes
     * @return FundData object
     * @throws IOException can't open file
     */
    public static FundData readFundHistory(String path, String fund_symbol, int num_days) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path + File.separator + fund_symbol + FrameworkConstants.EXTENSION_QUOTE));
        FundData fd = new FundData(fund_symbol);
        String line = "";
//        StringTokenizer st = null;
        int line_num = 1, tok_num = 1;
        //read comma separated file line by line
        while ( (line = br.readLine()) != null ) {
            if (line_num == 1) {
                line_num++;
                continue;//skip first header row
            }

            //parse line
            FundQuote quote = new FundQuote(fund_symbol);//each line is one quote
            quote.setIndex(line_num - 2);//convenient storage
            StringTokenizer st = new StringTokenizer(line, ",");
            while(st.hasMoreTokens()) {
                switch(tok_num) {
                    case 1: //date, covert string
                        String date = st.nextToken();
                        quote.setDate(date);
                        break;

                    case 2: //open
                        quote.setOpen(Float.parseFloat(st.nextToken()));
                        break;

                    case 3: //high
                        quote.setHigh(Float.parseFloat(st.nextToken()));
                        break;

                    case 4: //low
                        quote.setLow(Float.parseFloat(st.nextToken()));
                        break;

                    case 5: //close
                        float close = Float.parseFloat(st.nextToken());
                        quote.setClose(close);
                        quote.setUnAdjclose(close);
                        break;

                    case 6://volume
                        quote.setVolume(Float.parseFloat(st.nextToken()));
                        break;

                    case 7: //adjusted close
                        float adj_close = Float.parseFloat(st.nextToken());
//todo separate adj close away from close
                        quote.setAdjClose(adj_close);
                        quote.setClose(adj_close);
                        break;
                }
                tok_num++;
                if (tok_num > 7) {
                    fd.addQuote(quote);
                    break;
                }
            }
            line_num++;
            tok_num = 1;
            if (line_num == num_days)
                break;
        }
        return fd;
    }
//TODO add adjust split too
    public static FundData readHistory(String symbol, int num_days) throws IOException {
        if (num_days == -1)
            return readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
        return readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, num_days);
    }

    /**
     * Read all or subset of quotes from database. This method separates regular close from adjusted close
     *   unlike readFundHistory() mixed them together.
     * @param path location of database
     * @param fund_symbol symbol of fund
     * @param num_quotes number of points to retrieve
     * @return FundData object
     * @throws IOException fail to read file
     */
    public static FundData readQuotes(String path, String fund_symbol, int num_quotes) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path + File.separator
            + fund_symbol + FrameworkConstants.EXTENSION_QUOTE));
        FundData fd = new FundData(fund_symbol);
        String line;
        StringTokenizer st;
        int line_num = 1, tok_num = 1;
        //read comma separated file line by line
        while ( (line = br.readLine()) != null ) {
            if (line_num == 1) {
                line_num++;
                continue;//skip first header row
            }
            //parse line
            FundQuote quote = new FundQuote(fund_symbol);//each line is one quote
            quote.setIndex(line_num - 2);//convenient storage
            st = new StringTokenizer(line, ",");
            while(st.hasMoreTokens()) {
                switch(tok_num) {
                    case 1: //date, covert string
                        String date = st.nextToken();
                        quote.setDate(date);
                        break;

                    case 2: //open
                        quote.setOpen(Float.parseFloat(st.nextToken()));
                        break;

                    case 3: //high
                        quote.setHigh(Float.parseFloat(st.nextToken()));
                        break;

                    case 4: //low
                        quote.setLow(Float.parseFloat(st.nextToken()));
                        break;

                    case 5: //close
                        float close = Float.parseFloat(st.nextToken());
                        quote.setClose(close);
                        break;

                    case 6://volume
                        quote.setVolume(Float.parseFloat(st.nextToken()));
                        break;

                    case 7: //adjusted close
                        float adj_close = Float.parseFloat(st.nextToken());
                        quote.setAdjClose(adj_close);
                        break;
                }
                tok_num++;
                if (tok_num > 7) {
                    fd.addQuote(quote);
                    break;
                }
            }
            line_num++;
            if (line_num > num_quotes)
                break;

            tok_num = 1;
        }
        return fd;
    }

    /**
     * Adjust certain number of days of given daily quotes per split specification stored in split info.
     *   must use un-adjusted closing prices
     * Price related information such as high, low...etc are changed based on split ratio.
     * Sample split info line: 2001-10-01,3:2 (older dates first)
     * @param daily_quotes read from quote files
     * @param start_index oldest index to start adjustment
     */
    public static void adjustForSplits(ArrayList<FundQuote> daily_quotes, int start_index) throws IOException {
        String symbol = daily_quotes.get(0).getSymbol();
        ArrayList<SplitInfo> split_info = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER_DAILY_SPLIT_INFO +
            File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE));
        String line;
        while ( (line = br.readLine()) != null ) {//may have bad split info file
            if (line.startsWith("#") || line.equals(""))//skip comment line
                continue;
            String[] tokens = line.split(",");
            String[] ratios = tokens[1].trim().split(":");
            float ratio = ((float)Integer.parseInt(ratios[0].trim())) / Integer.parseInt(ratios[1]);
            split_info.add(new SplitInfo(tokens[0], ratio));
        }
        if (split_info.size() == 0)//no split info, empty info, done
            return;

        //start adjust based on SplitInfo array (earliest first)
        for (SplitInfo si : split_info) {
            Calendar start_cal = AppUtil.stringToCalendarNoEx(daily_quotes.get(start_index).getDate());
            Calendar split_cal = AppUtil.stringToCalendarNoEx(si.getSplitDate());
            if (split_cal.compareTo(start_cal) > 0) {//in range
                //adjust from start_index to split_index + 1
                int split_index = FundQuote.findIndexByDate(daily_quotes, si.getSplitDate());
                for (int idx = split_index + 1; idx <= start_index; idx++) {
                    FundQuote quote = daily_quotes.get(idx);
                    quote.setHigh(quote.getHigh() / si.getSplitRatio());
                    quote.setLow(quote.getLow() / si.getSplitRatio());
                    quote.setOpen(quote.getOpen() / si.getSplitRatio());
                    quote.setClose(quote.getClose() / si.getSplitRatio());
                    quote.setUnAdjclose(quote.getUnAdjclose() / si.getSplitRatio());
                }
                start_index = split_index;//next segment
            }
        }
    }

    /**
     * Adjust quotes based on split information for a subset of data points loaded in memory. Adjusted quote arrays are
     * in the same FundData object and written to separate file for reference.  Open, high, low, close prices are adjusted.
     * @param fund loaded symbol
     * @param start_index desirable earliest date in quote array
     * @param end_index desirable most recent date in quote array
     */
    public static void adjustForSplits(FundData fund, int start_index, int end_index) {
        //read split info into SplitInfo object
        String symbol = fund.getSymbol();
        BufferedReader br = null;
        ArrayList<SplitInfo> sis = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER_DAILY_SPLIT_INFO +
                File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE));
            String line;
            while ( (line = br.readLine()) != null ) {//may have bad split info file
                if (line.startsWith("#") || line.equals(""))//skip comment line
                    continue;
                String[] tokens = line.split(",");
                String[] ratios = tokens[1].trim().split(":");
                float ratio = ((float)Integer.parseInt(ratios[0].trim())) / Integer.parseInt(ratios[1]);
                sis.add(new SplitInfo(tokens[0], ratio));
            }
        } catch (IOException e) {//no split info or bad split info file
//            e.printStackTrace();
            //TODO log window
            return;
        }
        if (sis.size() == 0)//no split info, empty file
            return;

        //start adjust based on SplitInfo array (earliest first)
        Calendar start_cal = AppUtil.stringToCalendarNoEx(fund.getDate(start_index));
        for (SplitInfo si : sis) {
            Calendar split_cal = AppUtil.stringToCalendarNoEx(si.getSplitDate());
            if (split_cal.compareTo(start_cal) > 0) {//in range
                //convert split date + 1 to index
                int split_index = fund.findIndexByDate(si.getSplitDate());

                //adjust from start_index to split_index + 1
                for (int idx = split_index + 1; idx <= start_index; idx++) {
                    FundQuote quote = fund.getQuote().get(idx);
                    quote.setHigh(quote.getHigh() / si.getSplitRatio());
                    quote.setLow(quote.getLow() / si.getSplitRatio());
                    quote.setOpen(quote.getOpen() / si.getSplitRatio());
                    quote.setClose(/*quote.getUnAdjclose()*/quote.getClose() / si.getSplitRatio());
                    quote.setUnAdjclose(quote.getUnAdjclose() / si.getSplitRatio());
                }
            }
        }
    }

    /**
     * To validate the entire quote file is not corrupted per Yahoo format. This means every date, price, volume can be parsed.
     * @param path location of quote file
     * @return array of lines that's corrupted
     */
    public static ArrayList<String> validateQuotes(String path) throws IOException {
        ArrayList<String> ret = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        int line_num = 0;

        //read comma separated file line by line
        while ( (line = br.readLine()) != null ) {
            line_num++;
            if (line_num == 1)
                continue;//skip first header row

            int tok_num = 1;
            boolean corrupted = false;
            StringTokenizer st = new StringTokenizer(line, ",");
            while(st.hasMoreTokens()) {
                switch(tok_num) {
                    case 1: //date string
                        try {
                            String date = st.nextToken().trim();
                            AppUtil.stringToCalendar(date);
                        } catch (ParseException e) {
//                            e.printStackTrace();
                            ret.add("Line " + String.valueOf(line_num) + ":\t[" + line + "]");
                            corrupted = true;
                        }
                        break;

                    case 2: //open
                    case 3: //high
                    case 4: //low
                    case 5: //close
                    case 6: //volume
                    case 7: //adjusted close
                        try {
                            Float.parseFloat(st.nextToken());
                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
                            ret.add("Line " + String.valueOf(line_num) + ":\t[" + line + "]");
                            corrupted = true;
                        }
                        break;
                }
                if (corrupted)//next line
                    break;

                tok_num++;
                if (tok_num > 7)
                    break;
            }
        }
        br.close();
        return ret;
    }

    /**
     * Send a single request to retrieve one line quote from Yahoo using YAHOO_QUOTE_URL
     * @param symbol of interest
     * @return FundQuote object
     * @throws IOException no response or I/O problems
     *   sample line http://download.finance.yahoo.com/d/quotes.csv?s=SPY&f=l1ohgv&e=.csv
     *     &f parameter: l1 = closing price, o = open, h = high, g = low, v = volume, sl = full list
     *     the '1' in "l1" seems to indicate first value of 'l', w/o it, Feb 6 - <b>134.45</b>
     *     &f=sl1d1t1c1ohgv&e=.csv more complex, d, c, t not useful get this line
     *     "IBM",95.32,"1/16/2004","5:01pm",+1.30,95.00,95.35,94.71,9305000
     */
    public static FundQuote getYahooQuote(String symbol) throws IOException {
        StringBuilder req_buf = new StringBuilder(YAHOO_QUOTE_URL);
        req_buf.append("?s=").append(symbol).append("&f=l1ohgv&e=.csv");
        URL url = new URL(req_buf.toString());
//System.out.println(req_buf.toString());
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String line = in.readLine();
//System.out.println(line);
        in.close();
        if (line != null) {
            try {
                FundQuote q = parseYahooQuoteLine(symbol, line);
                return q;
            }catch(NumberFormatException nfe) {
                return null;
            }
        }
        else
            return null;
    }

    private static FundQuote parseYahooQuoteLine(String sym, String line) throws NumberFormatException {
        FundQuote quote = new FundQuote(sym);//each line is one quote
        String[] tokens = line.split(",");
//        quote.setDate(tokens[0]);
        quote.setOpen(Float.parseFloat(tokens[0]));
        quote.setHigh(Float.parseFloat(tokens[1]));
        quote.setLow(Float.parseFloat(tokens[2]));
        quote.setClose(Float.parseFloat(tokens[3]));
        quote.setVolume(Float.parseFloat(tokens[4]));
//        quote.setAdjClose(Float.parseFloat(tokens[6]));
        return quote;
    }

    private static FundQuote updateYahooQuoteLine(String sym, String line) throws NumberFormatException {
        FundQuote quote = new FundQuote(sym);//each line is one quote
        String[] tokens = line.split(",");
        quote.setDate(tokens[0]);
        quote.setOpen(Float.parseFloat(tokens[1]));
        quote.setHigh(Float.parseFloat(tokens[2]));
        quote.setLow(Float.parseFloat(tokens[3]));
        quote.setClose(Float.parseFloat(tokens[4]));
        quote.setVolume(Float.parseFloat(tokens[5]));
        quote.setAdjClose(Float.parseFloat(tokens[6]));
        return quote;
    }

    public static void downloadDailyQuote(String symbol) throws IOException {
        Calendar cal = Calendar.getInstance();
        int cur_month = cal.get(Calendar.MONTH);
        int cur_day = cal.get(Calendar.DAY_OF_MONTH);
        int cur_year = cal.get(Calendar.YEAR);
        downloadQuote(symbol, "d", 0, 1, 1950, cur_month, cur_day, cur_year);
    }

    //two different URL to get single quote or historical quotes
    private static final String YAHOO_QUOTE_URL = "http://finance.yahoo.com/d/quotes.csv";
    private static final String YAHOO_HISTORICAL_URL = "http://real-chart.finance.yahoo.com/table.csv";//ichart works too

    /**
     * Download closing price from YAHOO. yahoo is very strict on specified date, not sure exact rule is.
     * If dates specified wrong, file not found
     * Read and parse .txt files to build up database: all files listed in FUND_LIST
     * @param symbol to download
     * @param type = d for daily, w for weekly, m for monthly
     * @param start_month 0 based, 0 = Jan..11 = Dec
     * @param start_day 1 based, day of month
     * @param start_year 4 digit year eg. 2011
     * @param end_month same rule as start_month, if not sure, just use Dec 31 of this year
     * @param end_day then YAHOO will get us the most recent Friday quote
     * @param end_year same as above
     * @throws IOException IOException for no communication
     */
    public static void downloadQuote(String symbol, String type, int start_month, int start_day, int start_year,
        int end_month, int end_day, int end_year) throws IOException {
        StringBuilder req_buf = new StringBuilder();
        req_buf.append(YAHOO_HISTORICAL_URL).append("?s=").append(symbol)
               .append("&a=").append(start_month).append("&b=").append(start_day).append("&c=").append(start_year)
               .append("&d=").append(end_month).append("&e=").append(end_day).append("&f=").append(end_year)
               .append("&g=").append(type).append("&ignore=.csv");
        URL url = new URL(req_buf.toString());
//System.out.println("YAHOO Data Request --> " + req_buf);
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String cur_line;
        //change only SP500 symbol such that file name doesn't start from ^
        if (symbol.equals(FrameworkConstants.SP500_YAHOO_SYMBOL))
            symbol = FrameworkConstants.SP500;
        File file = new File(FrameworkConstants.DATA_FOLDER_WEEKLY_QUOTE + File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE);
        if (type.equals("d"))
            file = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE);
        PrintWriter pw = new PrintWriter(new FileWriter(file));
        String prev_line = "";
        while ((cur_line = in.readLine()) != null) {
//            System.out.println(cur_line);
            if (isIdenticalDate(prev_line, cur_line)) {
                System.out.println(symbol + ": Found duplicate date, skipping this line...\n\t" + cur_line);
                continue;
            }

            pw.println(cur_line);
            prev_line = cur_line;
        }
        in.close();
        pw.close();
        System.out.println(symbol + ": Download complete...");
    }
    public static void downloadDividend(String symbol, int start_month, int start_day, int start_year,
        int end_month, int end_day, int end_year) throws IOException {
        StringBuilder req_buf = new StringBuilder();
        req_buf.append(YAHOO_HISTORICAL_URL).append("?s=").append(symbol)
               .append("&a=").append(start_month).append("&b=").append(start_day).append("&c=").append(start_year)
               .append("&d=").append(end_month).append("&e=").append(end_day).append("&f=").append(end_year)
               .append("&g=v").append("&ignore=.csv");
        URL url = new URL(req_buf.toString());
//System.out.println("YAHOO Data Request --> " + req_buf);
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String cur_line;
        File file = new File(FrameworkConstants.DATA_FOLDER_DIVIDEND + File.separator + symbol + FrameworkConstants.EXTENSION_DIVIDEND);
        PrintWriter pw = new PrintWriter(new FileWriter(file));
        String prev_line = "";
        while ((cur_line = in.readLine()) != null) {
//            System.out.println(cur_line);
            if (isIdenticalDate(prev_line, cur_line)) {
                System.out.println(symbol + ": Found duplicate date, skipping this line...\n\t" + cur_line);
                continue;
            }
            pw.println(cur_line);
            prev_line = cur_line;
        }
        in.close();
        pw.close();
        System.out.println(symbol + ": Dividend Download complete...");
    }

    /**
     * Download SP500 historical daily quotes from YAHOO starting Jan. 1950 and ends today.
     * @throws IOException can't reach server
     */
    public static void downloadSP500Quotes() throws IOException {
        Calendar cal = Calendar.getInstance();
        int cur_month = cal.get(Calendar.MONTH);
        int cur_day = cal.get(Calendar.DAY_OF_MONTH);
        int cur_year = cal.get(Calendar.YEAR);
        downloadQuote(FrameworkConstants.SP500_YAHOO_SYMBOL, "d", 0, 1, 1950, cur_month, cur_day, cur_year);
    }

    //reverse order from old to new (YAHOO data is from new to old)
    public static FundData reverseQuotes(FundData fd) {
        FundData ret = new FundData(fd.getSymbol());
        ArrayList<FundQuote> quotes = fd.getQuote();
        for (int index = quotes.size() - 1; index >= 0; index--)//last int first out
            ret.addQuote(quotes.get(index));
        return ret;
    }

    //check if symbol exist in database
    public static boolean isSymbolExist(String symbol) {
        File dir = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        String[] file_names = dir.list();
        for (String fn : file_names)
            if (fn.equals(symbol))
                return true;
        return false;
    }

    private static FundData getAllQuotes(String path, String fund_symbol) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path + File.separator + fund_symbol + FrameworkConstants.EXTENSION_QUOTE));
        FundData fd = new FundData(fund_symbol);
        String line = "";
        StringTokenizer st;
        int line_num = 1, tok_num = 1;
        //read comma separated file line by line
        while ( (line = br.readLine()) != null ) {
            if (line_num == 1) {
                line_num++;
                continue;//skip first header row
            }
            //parse line
            FundQuote quote = new FundQuote(fund_symbol);//each line is one quote
            quote.setIndex(line_num - 2);//convenient storage
            st = new StringTokenizer(line, ",");
            while(st.hasMoreTokens()) {
                switch(tok_num) {
                    case 1: //date, covert string
                        String date = st.nextToken();
                        quote.setDate(date);
                        break;

                    case 2: //open
                        quote.setOpen(Float.parseFloat(st.nextToken()));
                        break;

                    case 3: //high
                        quote.setHigh(Float.parseFloat(st.nextToken()));
                        break;

                    case 4: //low
                        quote.setLow(Float.parseFloat(st.nextToken()));
                        break;

                    case 5: //close
                        quote.setClose(Float.parseFloat(st.nextToken()));
                        break;

                    case 6://volume
                        quote.setVolume(Float.parseFloat(st.nextToken()));
                        break;

                    case 7: //adjusted close
                        quote.setAdjClose(Float.parseFloat(st.nextToken()));
                        break;
                }
                tok_num++;
                if (tok_num > 7) {
                    fd.addQuote(quote);
                    break;
                }
            }
            line_num++;
            tok_num = 1;
        }
        return fd;
    }

    //write a FundQuote object to file using same Yahoo format
    private static void quotesToFile(ArrayList<FundQuote> quotes, boolean append) throws IOException {
        File file = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator
            + quotes.get(0).getSymbol() + FrameworkConstants.EXTENSION_QUOTE);
        PrintWriter pw = new PrintWriter(new FileWriter(file, append));
        if (!append) { //print header line for first file
            pw.println("Date,Open,High,Low,Close,Volume,Adj Close");
        }
        for (FundQuote quote : quotes) {
            StringBuilder buf = new StringBuilder();
            buf.append(quote.getDate()).append(",").append(quote.getOpen()).append(",")
               .append(quote.getHigh()).append(",").append(quote.getLow()).append(",")
               .append(quote.getClose()).append(",")
               .append(FrameworkConstants.VOLUME_FORMAT.format(quote.getVolume())).append(",")
               .append(quote.getAdjClose());
            pw.println(buf);
        }
        pw.close();
    }

    public static void dbSymbolsToFile() throws IOException {
        StringBuilder buf = new StringBuilder();
        String[] files = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE).list();
        for (String file : files) {
            if (file.endsWith(FrameworkConstants.EXTENSION_QUOTE)) {
                String name = file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE));
                buf.append(name).append("\r");
            }
        }
        PrintWriter pw = new PrintWriter(new FileWriter(new File(FrameworkConstants.DATA_FOLDER_EXPORT + File.separator + "DB.lst"), false));
        pw.println(buf.toString());
        pw.close();
    }

    //update quotes from Yahoo for one symbol, true = file changed, false = no change
    public static boolean updateQuoteFromYahoo(String symbol, JTextArea msg_outlet) throws IOException, ParseException {
        //read quote file into memory
        FundData fund = getAllQuotes(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);

        //if 0th element is not TODAY, create start date from 0th element's date plus 1 day
        String last_date = fund.getDate(0);
        Calendar last_cal = AppUtil.stringToCalendar(last_date);
        int start_month = last_cal.get(Calendar.MONTH);
        int start_day = last_cal.get(Calendar.DAY_OF_MONTH);
        int start_year = last_cal.get(Calendar.YEAR);
        Calendar today = Calendar.getInstance();
        int end_month = today.get(Calendar.MONTH);
        int end_day = today.get(Calendar.DAY_OF_MONTH);
        int end_year = today.get(Calendar.YEAR);
        if (start_year == end_year && start_month == end_month && start_day == end_day) {
            String msg = symbol + ":\tQuote is up to date" + "\n";
            System.out.print(msg);
            showMsgViaEdt(msg_outlet, msg);
            return false;
        }

        //has gap, need download
        last_cal.add(Calendar.DAY_OF_YEAR, 1);
        start_month = last_cal.get(Calendar.MONTH);
        start_day = last_cal.get(Calendar.DAY_OF_MONTH);
        start_year = last_cal.get(Calendar.YEAR);

        //special case for SP500, symbol is different on Yahoo
        String req_sym = symbol;
        if (symbol.equals(FrameworkConstants.SP500))
            req_sym = FrameworkConstants.SP500_YAHOO_SYMBOL;
        StringBuilder req_buf = new StringBuilder();
        req_buf.append(YAHOO_HISTORICAL_URL).append("?s=").append(req_sym)
            .append("&a=").append(start_month).append("&b=").append(start_day).append("&c=").append(start_year)
            .append("&d=").append(end_month).append("&e=").append(end_day).append("&f=").append(end_year)
            .append("&g=").append("d").append("&ignore=.csv");
        URL url = new URL(req_buf.toString());
//System.out.println("Request --> " + req_buf);

        //open connection, send request, loop till all lines are received, parse cur_line, add to array
        URLConnection yc = url.openConnection();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        } catch (FileNotFoundException e) {
            //Ok not finding any quotes
            String msg = symbol + ":\tNo quote returned... " + "\n";
            System.out.print(msg);
            showMsgViaEdt(msg_outlet, msg);
            return false;
        } catch (ConnectException ce) {
            ce.printStackTrace();
            return false;
        }

        //Yahoo returns some lines, skip first and parse the rest into FundQuote
        String cur_line = in.readLine();//first cur_line is heading
        String prev_line = "";
        ArrayList<FundQuote> new_quotes = new ArrayList<>();
        while ((cur_line = in.readLine()) != null) {
            if (isIdenticalDate(prev_line, cur_line)) { //skip when yahoo reports same date twice
                String msg = symbol + ":\tFound duplicate date, skipping this line...\n\t" + cur_line;
                System.err.println(msg);
                showMsgViaEdt(msg_outlet, msg);
                continue;
            }
            FundQuote quote = updateYahooQuoteLine(symbol, cur_line);
            new_quotes.add(quote);
            prev_line = cur_line;
        }
        if (new_quotes.size() == 0) {
            String msg = symbol + ":\tNo quote returned... " + "\n";
            System.out.print(msg);
            showMsgViaEdt(msg_outlet, msg);
            return false;
        }

        //write new quotes followed by old quotes to file
        quotesToFile(new_quotes, false);
        quotesToFile(fund.getQuote(), true);
        return true;
    }

    public static void showMsgViaEdt(final JTextArea msg_outlet, final String msg) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (msg_outlet != null)
                    msg_outlet.append(msg);
            }
        });
    }

    //does this raw quote line have same date as last line, used when Yahoo quote service duplicates same day
    private static boolean isIdenticalDate(String prev_line, String cur_line) {
        String[] tokens = prev_line.split(",");
        String prev_date = tokens[0];
        tokens = cur_line.split(",");
        String cur_date = tokens[0];
        return prev_date.equals(cur_date);
    }

    /**
     * To remove duplicate consecutive quote lines with same dates (Yahoo did this 7/2/2012)
     * Traverse all quote files in database, examine each, eliminate duplicate quote lines.
     */
    public static void removeDuplicateDates() throws IOException, FileNotFoundException {
        File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        String[] file_list = folder.list();
        for (String file_name : file_list) {
            if (!file_name.endsWith(FrameworkConstants.EXTENSION_QUOTE)) //only .txt files
                continue;

            //extract symbol from file name
            File tmp_file = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + "tmp.txt");
            PrintWriter pw = new PrintWriter(new FileWriter(tmp_file));
            BufferedReader br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE
                + File.separator + file_name));
            String cur_line = "", prev_line = "";
            int line_num = 1;
            while ( (cur_line = br.readLine()) != null ) {
                if (line_num == 1) {
                    line_num++;
                    pw.println(cur_line);//save to tmp file_name
                    continue;//skip first header row
                }

                //skip identical dates
                if (isIdenticalDate(prev_line, cur_line)) {
                    System.out.print(file_name + ": skipping " + cur_line.substring(0, 12) + "\t\t");
                    continue;
                }

                //write out to tmp file_name
                pw.println(cur_line);
                prev_line = cur_line;
            }

            //rename back to same file_name name
            pw.close();
            boolean ok = tmp_file.renameTo(new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + file_name));
            System.out.println("Finishing...." + file_name);
        }

    }

    /**
     * Populate specified combo box with all symbols in quote database
     * @param combo
     */
    public static void populateSymbolCombo(JComboBox combo) {
        File dir = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        Vector<String> symbol_list = new Vector<String>();
        for (String file : dir.list()) {//build up combo list
            if (file.endsWith(FrameworkConstants.EXTENSION_QUOTE) && !file.startsWith("."))
                symbol_list.add(file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE)));
        }
        if (symbol_list.size() > 0)
            combo.setModel(new DefaultComboBoxModel/*<String>*/(symbol_list));
    }

    //========== This group manages watch lists ==========
    /**
     * Read a csv file with symbols from first column into an array of Strings.
     * @param files File objects from user selection
     * @param file_type see ImportFileType
     * @return array of strings
     * @throws IOException can't read file
     */
    public static ArrayList<String> readSymbolToWatchList(File[] files, ImportFileType file_type) throws Exception {
        ArrayList<String> ret = new ArrayList<>();
        if (file_type == ImportFileType.IBD50_XLS) {
            for (File file_obj : files) {
                Workbook wb = Workbook.getWorkbook(file_obj);
                Sheet sheet = wb.getSheet(0);
                for (int row = IbdInfo.ROW_IBD50_FIRST_DATA; row <= IbdInfo.ROW_IBD50_LAST_DATA; row++) {//sheet row 8 to 57 has real data
                    String sym = sheet.getCell(IbdInfo.COLUMN_IBD50_SYMBOL, row).getContents();
                    if (!ret.contains(sym)) //skip if already there
                        ret.add(sym);
                }
            }
            return ret;
        }

        //other types
        int skip_num = 1;//finviz, skip 1 line
        switch (file_type) {
            case BARCHAT_CSV://FrameworkConstants.EXPORT_TYPE_BARCHART:
            case DVG_CSV://FrameworkConstants.EXPORT_TYPE_DVG:
                skip_num = 2; break;
            case IBD50_CSV://FrameworkConstants.EXPORT_TYPE_IBD50:
                skip_num = 7; break;
            case IBD_PORTFOLIO_CSV://FrameworkConstants.EXPORT_TYPE_IBD_PORTFOLIO:
                skip_num = 6; break;
        }
        BufferedReader br = new BufferedReader(new FileReader(files[0]));
        for (int i=0; i<skip_num; i++)
            br.readLine();
        String line = "";
        while ( (line = br.readLine()) != null ) {
            String[] tokens = line.split(",");
            if (tokens.length < 2)
                continue;

            String token0 = tokens[0];
            if (token0.equals("") || token0.equals("DISCLAMER"))
                continue;
            switch (file_type) {
                case FINVIZ_CSV://FrameworkConstants.EXPORT_TYPE_FINVIZ://remove quotes around symbol
                    ret.add(token0.substring(1, token0.lastIndexOf("\"")));
                    break;

                default:
                    if (token0.startsWith("\""))
                        continue;
                    String symbol = tokens[0];
                    ret.add(symbol);
                    break;
            }
        }
        return ret;
    }

    //========== This group for technical database and IBD database ==========
    /**
     * To read/merge specified File object(IBD50 xls format) into internal technical database.
     * @param file_obj user chosen file
     * @param tech_map map of TechnicalInfo to be merged into
     * @throws IOException file can't be read,
     * @throws ParseException bad date format
     */
    public static void mergeIbd50XlsFile(File file_obj, HashMap<String, TechnicalInfo> tech_map) throws IOException, BiffException {
        Workbook wb = Workbook.getWorkbook(file_obj);
        Sheet sheet = wb.getSheet(0);
        Calendar file_date = extractIbdDate(sheet.getCell(IbdInfo.COLUMN_IBD50_EXPORT_DATE, IbdInfo.ROW_IBD50_EXPORT_DATE).getContents(), true);
//TODO use file name as file date
        //loop thru all rows with data, extract cell values into objects
        for (int row = IbdInfo.ROW_IBD50_FIRST_DATA; row <= IbdInfo.ROW_IBD50_LAST_DATA; row++) {//sheet row 8 to 57 has real data
            String symbol = sheet.getCell(IbdInfo.COLUMN_IBD50_SYMBOL, row).getContents();
            String full_name = sheet.getCell(IbdInfo.COLUMN_IBD50_FULL_NAME, row).getContents();
            IbdInfo ibd_info;
            TechnicalInfo tech_info = tech_map.get(symbol);
            if (tech_info != null) {//update existing symbol
                //if file date is older than symbol's last update date, skip
                ibd_info = tech_info.getIbdInfo();
                Calendar last_update = tech_info.getLastUpdate();
                if (last_update.compareTo(file_date) >= 0)//already have later info, skip this
                    continue;
            }
            else {//add new TechInfo object
                ibd_info = new IbdInfo(symbol, full_name);
                tech_info = new TechnicalInfo(ibd_info);
                tech_info.setSymbol(symbol);
                tech_info.setFullName(full_name);
                tech_map.put(symbol, tech_info);
            }
            tech_info.setLastUpdate(file_date);
            ibd_info.setFullName(full_name);
            ibd_info.setRank(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_RANK, row).getContents()));
            ibd_info.setPctOffHigh(Float.parseFloat(sheet.getCell(IbdInfo.COLUMN_IBD50_PCT_OFF_HIGH, row).getContents()));
            ibd_info.setComposite(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_COMPOSITE, row).getContents()));
            ibd_info.setEps(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_EPS_RATING, row).getContents()));
            ibd_info.setRs(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_RS_RATING, row).getContents()));
            ibd_info.setSmr(sheet.getCell(IbdInfo.COLUMN_IBD50_SMR_RATING, row).getContents());
            ibd_info.setAccDis(sheet.getCell(IbdInfo.COLUMN_IBD50_ACC_DIS_RATING, row).getContents());
            ibd_info.setGroupStrength(sheet.getCell(IbdInfo.COLUMN_IBD50_GROUP_RATING, row).getContents());
            ibd_info.setMgmtPercent(Float.parseFloat(sheet.getCell(IbdInfo.COLUMN_IBD50_PCT_MGMT_OWN, row).getContents()));
            String sponsor = sheet.getCell(IbdInfo.COLUMN_IBD50_QTR_RISE_SPONSORSHIP, row).getContents();
            if (sponsor.equals("N/A"))
                ibd_info.setSponsorship(0);
            else
                ibd_info.setSponsorship(Integer.parseInt(sponsor));
        }
        wb.close();
        writeTechnicalDb(tech_map);
    }
    /**
     * To read/merge specified File object(IBD50 csv format) into internal technical database.
     * @param file_obj user chosen file
     * @param tech_map map of TechnicalInfo to be merged into
     * @throws IOException file can't be read
     * @throws ParseException bad date format
     */
    public static void mergeIbd50CsvFile(File file_obj, HashMap<String, TechnicalInfo> tech_map) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file_obj));
        Calendar file_date = Calendar.getInstance();
        //skip 7 header lines, only parse date
        for (int i=0; i<7; i++) {
            String line = br.readLine();
            if (i == 4) {//this line has date information
                file_date = extractIbdDate(line, false);
//                String[] tokens = line.split(",");
//                String[] month_date = tokens[2].split(" ");
//                String m = month_date[1];
//                String month = "12";
//                if (m.equals("January")) month = "01";
//                else if (m.equals("February")) month = "02";
//                else if (m.equals("March")) month = "03";
//                else if (m.equals("April")) month = "04";
//                else if (m.equals("May")) month = "05";
//                else if (m.equals("June")) month = "06";
//                else if (m.equals("July")) month = "07";
//                else if (m.equals("August")) month = "08";
//                else if (m.equals("September")) month = "09";
//                else if (m.equals("October")) month = "10";
//                else if (m.equals("November")) month = "11";
//                String yr = tokens[3].substring(0, tokens[3].indexOf("\"")).trim();
//                String merge_date =  yr + "-" + month + "-" + month_date[2];
//                file_date = AppUtil.stringToCalendar(merge_date);
            }
        }
        String line = "";
        while ( (line = br.readLine()) != null ) {
            String[] tokens = line.split(",");
            if (tokens.length < IbdInfo.COLUMN_IBD50_QTR_RISE_SPONSORSHIP)
                continue;

            String token0 = tokens[0];
            if (token0.equals("") || token0.equals("DISCLAMER") || token0.startsWith("\""))
                continue;

            String symbol = tokens[IbdInfo.COLUMN_IBD50_SYMBOL];
            TechnicalInfo tech_info = tech_map.get(symbol);
            IbdInfo ibd_info;
            String full_name = tokens[IbdInfo.COLUMN_IBD50_FULL_NAME];
            if (tech_info != null) {//update existing symbol
                //if file date is older than symbol's last update date, skip
                ibd_info = tech_info.getIbdInfo();
                Calendar last_update = tech_info.getLastUpdate();
                if (last_update.compareTo(file_date) >=0)
                    continue;
            }
            else {//add new TechInfo object
                ibd_info = new IbdInfo(symbol, full_name);
                tech_info = new TechnicalInfo(ibd_info);
                tech_info.setSymbol(symbol);
                tech_info.setFullName(full_name);
                tech_map.put(symbol, tech_info);
            }
            tech_info.setLastUpdate(file_date);
            ibd_info.setFullName(full_name);
            ibd_info.setRank(Integer.parseInt(tokens[IbdInfo.COLUMN_IBD50_RANK]));
            ibd_info.setPctOffHigh(Float.parseFloat(tokens[IbdInfo.COLUMN_IBD50_PCT_OFF_HIGH]));
            ibd_info.setComposite(Integer.parseInt(tokens[IbdInfo.COLUMN_IBD50_COMPOSITE]));
            ibd_info.setEps(Integer.parseInt(tokens[IbdInfo.COLUMN_IBD50_EPS_RATING]));
            ibd_info.setRs(Integer.parseInt(tokens[IbdInfo.COLUMN_IBD50_RS_RATING]));
            ibd_info.setSmr(tokens[IbdInfo.COLUMN_IBD50_SMR_RATING]);
            ibd_info.setAccDis(tokens[IbdInfo.COLUMN_IBD50_ACC_DIS_RATING]);
            ibd_info.setGroupStrength(tokens[IbdInfo.COLUMN_IBD50_GROUP_RATING]);
            ibd_info.setMgmtPercent(Float.parseFloat(tokens[IbdInfo.COLUMN_IBD50_PCT_MGMT_OWN]));
            ibd_info.setSponsorship(Integer.parseInt(tokens[IbdInfo.COLUMN_IBD50_QTR_RISE_SPONSORSHIP]));
        }
        br.close();

        //write tech_map back to file
        writeTechnicalDb(tech_map);
    }

    public static void mergeIbdEtfXlsFile(File file_obj, HashMap<String, TechnicalInfo> tech_map) throws IOException, BiffException {
        Workbook wb = Workbook.getWorkbook(file_obj);
        Sheet sheet = wb.getSheet(0);
        Calendar file_cal = extractIbdDate(sheet.getCell(IbdInfo.COLUMN_IBDETF_EXPORT_DATE, IbdInfo.ROW_IBDETF_EXPORT_DATE).getContents(), true);
        for (int row = IbdInfo.ROW_IBDETF_FIRST_DATA; row <= IbdInfo.ROW_IBDETF_LAST_DATA; row++) {
            String symbol = sheet.getCell(IbdInfo.COLUMN_IBDETF_SYMBOL, row).getContents();
            if (symbol.equals(""))
                break;//no more data, sometimes export files have different rows
            String full_name = sheet.getCell(IbdInfo.COLUMN_IBDETF_FULL_NAME, row).getContents();
            TechnicalInfo tech_info = tech_map.get(symbol);
            IbdInfo ibd_info;
            if (tech_info != null) {//update old object, retrieve stored values that are not in this type of file
                ibd_info = tech_info.getIbdInfo();
                Calendar last_update = tech_info.getLastUpdate();
                if (last_update.compareTo(file_cal) >=0)//already have later info, skip this
                    continue;
            }
            else {//tech_info doesn't exist, add new TechInfo object, no rank, mgmt, sponsor
                ibd_info = new IbdInfo(symbol, full_name);
                tech_info = new TechnicalInfo(ibd_info);
                tech_info.setSymbol(symbol);
                tech_info.setFullName(full_name);
                tech_map.put(symbol, tech_info);
            }

            //set up information from previous update if any
            tech_info.setLastUpdate(file_cal);
            ibd_info.setFullName(full_name);
            ibd_info.setRs(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBDETF_RS_RATING, row).getContents()));
            ibd_info.setAccDis(sheet.getCell(IbdInfo.COLUMN_IBDETF_ACC_DIS_RATING, row).getContents());
        }
        wb.close();
        writeTechnicalDb(tech_map);
    }
    /**
     * To read/merge specified File object(IBD portfolio format in csv format) into internal technical database.
     * @param file_obj user chosen file
     * @param tech_map map of TechnicalInfo to be merged into
     * @throws IOException file can't be read
     */
    public static void mergeIbdCsvPortfolio(File file_obj, HashMap<String, TechnicalInfo> tech_map) throws IOException, ParseException {
        BufferedReader br = new BufferedReader(new FileReader(file_obj));
        Calendar merge_cal = Calendar.getInstance();//use today as last update time
        //skip 7 header lines, only parse date
        for (int i=0; i<6; i++) {
            String line = br.readLine();
            if (i == 2) {//this line has date information
                String[] tokens = line.split(",");
                String[] month_date = tokens[2].split(" ");
                String m = month_date[1];
                String month = "12";
                if (m.equals("January")) month = "01";
                else if (m.equals("February")) month = "02";
                else if (m.equals("March")) month = "03";
                else if (m.equals("April")) month = "04";
                else if (m.equals("May")) month = "05";
                else if (m.equals("June")) month = "06";
                else if (m.equals("July")) month = "07";
                else if (m.equals("August")) month = "08";
                else if (m.equals("September")) month = "09";
                else if (m.equals("October")) month = "10";
                else if (m.equals("November")) month = "11";
                String yr = tokens[3].substring(0, tokens[3].indexOf("\"")).trim();
                String merge_date =  yr + "-" + month + "-" + month_date[2];
                merge_cal = AppUtil.stringToCalendar(merge_date);
            }
        }

        String line = "";
        while ( (line = br.readLine()) != null ) {
            line = preprocessFinvizLine(line, false);//remove quotes, comma to empty if between quotes
            String[] tokens = line.split(",");
            if (tokens.length == 0)
                break;

            String token0 = tokens[0];
            if (token0.equals("") || token0.startsWith("Reproduction") || token0.startsWith("All prices")
                   || token0.startsWith("Data") || token0.startsWith("Investor's"))
                continue;

            String symbol = tokens[IbdInfo.COLUMN_IBDPORT_SYMBOL];
            TechnicalInfo tech_info = tech_map.get(symbol);
            IbdInfo ibd_info;
            String full_name = tokens[IbdInfo.COLUMN_IBDPORT_FULL_NAME];
            int rank = 0;  float pct_off_high = 0; float mgmt_pct = 0;  int sponsor = 0;
            if (tech_info != null) {//update old object
                ibd_info = tech_info.getIbdInfo();
                rank = ibd_info.getRank();
                pct_off_high = ibd_info.getPctOffHigh();
                mgmt_pct = ibd_info.getMgmtPercent();
                sponsor = ibd_info.getSponsorship();
            }
            else {//add new TechInfo object, no rank, mgmt, sponsor
                ibd_info = new IbdInfo(symbol, full_name);
                tech_info = new TechnicalInfo(ibd_info);
                tech_info.setSymbol(symbol);
                tech_info.setFullName(full_name);
                tech_map.put(symbol, tech_info);
            }
            tech_info.setLastUpdate(merge_cal);
            ibd_info.setFullName(full_name);
            ibd_info.setRank(rank);
            ibd_info.setPctOffHigh(pct_off_high);
            ibd_info.setComposite(Integer.parseInt(preProcessIntColumn(tokens[IbdInfo.COLUMN_IBDPORT_COMPOSITE])));
            ibd_info.setEps(Integer.parseInt(preProcessIntColumn(tokens[IbdInfo.COLUMN_IBDPORT_EPS_RATING])));
            ibd_info.setRs(Integer.parseInt(preProcessIntColumn(tokens[IbdInfo.COLUMN_IBDPORT_RS_RATING])));
            ibd_info.setSmr(preProcessStringColumn(tokens[IbdInfo.COLUMN_IBDPORT_SMR_RATING]));
            ibd_info.setAccDis(preProcessStringColumn(tokens[IbdInfo.COLUMN_IBDPORT_ACC_DIS_RATING]));
            ibd_info.setGroupStrength(preProcessStringColumn(tokens[IbdInfo.COLUMN_IBDPORT_GROUP_RATING]));
            ibd_info.setMgmtPercent(mgmt_pct);
            ibd_info.setSponsorship(sponsor);
        }
        br.close();

        //write tech_map back to file
        writeTechnicalDb(tech_map);
    }
    /**
     * To read/merge specified File object(IBD portfolio format in xls format) into internal technical database.
     * @param file_obj user chosen file
     * @param tech_map map of TechnicalInfo to be merged into
     * @throws IOException file can't be read
     */
    public static void mergeIbdXlsPortfolio(File file_obj, HashMap<String, TechnicalInfo> tech_map) throws IOException, BiffException {
        Workbook wb = Workbook.getWorkbook(file_obj);
        Sheet sheet = wb.getSheet(0);
        Calendar merge_cal = Calendar.getInstance();//use today as last update time

        //loop thru all rows with data, extract cell values into objects
        for (int row = 5; row <= 59; row++) {//IBD should fill between 6 and 60
            String str = sheet.getCell(IbdInfo.COLUMN_IBDPORT_COMPOSITE, row).getContents();
            if (str.equals(""))//done here
                break;

            String symbol = sheet.getCell(IbdInfo.COLUMN_IBDPORT_SYMBOL, row).getContents();
            String full_name = sheet.getCell(IbdInfo.COLUMN_IBDPORT_FULL_NAME, row).getContents();
            TechnicalInfo tech_info = tech_map.get(symbol);
            IbdInfo ibd_info;
            int rank = 0;  float pct_off_high = 0; float mgmt_pct = 0;  int sponsor = 0;
            if (tech_info != null) {//update old object, retrieve stored values that are not in this type of file
                ibd_info = tech_info.getIbdInfo();
                rank = ibd_info.getRank();
                pct_off_high = ibd_info.getPctOffHigh();
                mgmt_pct = ibd_info.getMgmtPercent();
                sponsor = ibd_info.getSponsorship();
            }
            else {//tech_info doesn't exist, add new TechInfo object, no rank, mgmt, sponsor
                ibd_info = new IbdInfo(symbol, full_name);
                tech_info = new TechnicalInfo(ibd_info);
                tech_info.setSymbol(symbol);
                tech_info.setFullName(full_name);
                tech_map.put(symbol, tech_info);
            }

            //set up information from previous update if any
            tech_info.setLastUpdate(merge_cal);
            ibd_info.setFullName(full_name);
            ibd_info.setRank(rank);
            ibd_info.setPctOffHigh(pct_off_high);
            ibd_info.setMgmtPercent(mgmt_pct);
            ibd_info.setSponsorship(sponsor);
            ibd_info.setComposite(Integer.parseInt(preProcessIntColumn(str)));
            str = sheet.getCell(IbdInfo.COLUMN_IBDPORT_EPS_RATING, row).getContents();
            ibd_info.setEps(Integer.parseInt(preProcessIntColumn(str)));
            str = sheet.getCell(IbdInfo.COLUMN_IBDPORT_RS_RATING, row).getContents();
            ibd_info.setRs(Integer.parseInt(preProcessIntColumn(str)));
            ibd_info.setSmr(preProcessStringColumn(sheet.getCell(IbdInfo.COLUMN_IBDPORT_SMR_RATING, row).getContents()));
            ibd_info.setAccDis(preProcessStringColumn(sheet.getCell(IbdInfo.COLUMN_IBDPORT_ACC_DIS_RATING, row).getContents()));
            ibd_info.setGroupStrength(preProcessStringColumn(sheet.getCell(IbdInfo.COLUMN_IBDPORT_GROUP_RATING, row).getContents()));
        }
        wb.close();

        //write tech_map back to file
        writeTechnicalDb(tech_map);
    }
    /**
     * collect a list of symbols from a give IBD80 export file.
     * @param file_obj a File object
     * @return list of symbols
     * @throws IOException can't read file
     */
    public static ArrayList<String> readIbdSymbols(File file_obj) throws IOException {
        ArrayList<String> ret = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(file_obj));
        for (int i=0; i<7; i++)
            br.readLine();
        String line = "";
        while ( (line = br.readLine()) != null ) {
            String[] tokens = line.split(",");
            if (tokens.length < IbdInfo.COLUMN_IBD50_QTR_RISE_SPONSORSHIP)
                continue;

            String token0 = tokens[0];
            if (token0.equals("") || token0.equals("DISCLAMER") || token0.startsWith("\""))
                continue;

            String symbol = tokens[IbdInfo.COLUMN_IBD50_SYMBOL];
            ret.add(symbol);
        }
        return ret;
    }
    /**
     * To read all technical information into a map. (csv format)
     * @return in memory map object of TechnicalInfo type
     * @throws IOException when file can't be read
     */
    public static HashMap<String, TechnicalInfo> readTechnicalDb() throws IOException, ParseException {
        HashMap<String, TechnicalInfo> ret = new HashMap<>();
        File db_file = new File(FrameworkConstants.DATA_FOLDER_TECHNICAL_DB);
        if (!db_file.exists()) {
            return ret;//empty map
        }

        BufferedReader br = new BufferedReader(new FileReader(db_file));
        String line;
        br.readLine();//skip first line header
        while ( (line = br.readLine()) != null) {
            String[] tokens = line.split(",");
            String symbol = tokens[TechnicalInfo.COLUMN_SYMBOL];
            String full_name = tokens[TechnicalInfo.COLUMN_FULL_NAME];
            String last_update = tokens[TechnicalInfo.COLUMN_LAST_UPDATE];
            int ibd50_rank = Integer.parseInt(tokens[TechnicalInfo.COLUMN_IBD50_RANK]);
            float pct_off_high = Float.parseFloat(tokens[TechnicalInfo.COLUMN_PCT_OFF_HIGH]);
            int composite = Integer.parseInt(tokens[TechnicalInfo.COLUMN_COMPOSITE]);
            int eps = Integer.parseInt(tokens[TechnicalInfo.COLUMN_EPS_RATING]);
            int rs = Integer.parseInt(tokens[TechnicalInfo.COLUMN_RS_RATING]);
            String smr = tokens[TechnicalInfo.COLUMN_SMR_RATING];
            String acc_dis_rank = tokens[TechnicalInfo.COLUMN_ACC_DIS_RATING];
            String group_rank = tokens[TechnicalInfo.COLUMN_GROUP_RATING];
            float mgmt = Float.parseFloat(tokens[TechnicalInfo.COLUMN_MGMT_OWN]);
            int qtr = Integer.parseInt(tokens[TechnicalInfo.COLUMN_QTR_RISE_SPONSORSHIP]);
            IbdInfo ii = new IbdInfo(symbol, full_name, ibd50_rank, pct_off_high, composite, eps, rs,
                smr, acc_dis_rank, group_rank, mgmt, qtr);
//TODO add barchart info also
            TechnicalInfo ti = new TechnicalInfo(ii); ti.setLastUpdate(AppUtil.stringToCalendar(last_update));
            ret.put(symbol, ti);
        }
        return ret;
    }
    /**
     * To write technical information back into database (technical.db) with supplied object.
     * Note the old database file is overwritten.
     * @param tech_map a TechnicalInfo hash map
     * @throws IOException file can not be written
     */
    public static void writeTechnicalDb(HashMap<String, TechnicalInfo> tech_map) throws IOException {
        //first backup old database file add "-OLD" at end
        String path = FrameworkConstants.DATA_FOLDER_TECHNICAL_DB;
        File file = new File(path);
        file.renameTo(new File(path + "-OLD"));

        //write to technical.db
        PrintWriter pw = new PrintWriter(new FileWriter(file, false));
        pw.println("Symbol,Name,Last Update,IBD 50 Rank,% Off High,Composite Rating,EPS Rating,RS Rating," +
                "SMR Rating,Acc/Dis Rating,Group Rel Str Rating,Mgmt Own %,Qtrs of Rising Sponsorship");
        Set<String> keys = tech_map.keySet();
        for (String key : keys) {
            TechnicalInfo tech_info = tech_map.get(key);
            IbdInfo ibd_info = tech_info.getIbdInfo();
            StringBuilder buf = new StringBuilder(ibd_info.getSymbol());
            String name = tech_info.getFullName();
            if (name == null) name = "";
            Calendar upd = tech_info.getLastUpdate();
            buf.append(",").append(name).append(",");
            if (upd != null)
                buf.append(AppUtil.calendarToString(upd)).append(",");
            else
                buf.append(",");
            buf.append(ibd_info.getRank()).append(",")
                .append(ibd_info.getPctOffHigh()).append(",")
                .append(ibd_info.getComposite()).append(",")
                .append(ibd_info.getEps()).append(",")
                .append(ibd_info.getRs()).append(",")
                .append(ibd_info.getSmr()).append(",")
                .append(ibd_info.getAccDis()).append(",")
                .append(ibd_info.getGroupStrength()).append(",")
                .append(ibd_info.getMgmtPercent()).append(",")
                .append(ibd_info.getSponsorship()).append(",");
            pw.println(buf);
        }
        pw.close();
    }
    /**
     * Read all files stored under IBD.db folder that ends with .ibd into memory structure.
     * @return map of map of IbdInfo
     */
    public static HashMap<String, ArrayList<IbdInfo>> readIbdDb() {
        HashMap<String, ArrayList<IbdInfo>> ret = new HashMap<>();
        File folder = new File(FrameworkConstants.DATA_FOLDER_IBD_DB);
        if (!folder.exists())
            return ret;

        //for each file in this folder, read its content into array of IbdInfo objects
        File[] ibd_files = folder.listFiles();
        if (ibd_files == null || ibd_files.length == 0) return ret;
        for (File ibd_file : ibd_files) {
            if (ibd_file.isDirectory() || !ibd_file.getName().endsWith(".ibd")) continue;//skip folders
            String symbol = FileUtil.removeExtension(ibd_file.getName(), FrameworkConstants.EXTENSION_IBD);
            ArrayList<IbdInfo> ibd_infos = new ArrayList<>();//for this symbol
            try {
                BufferedReader br = new BufferedReader(new FileReader(ibd_file));
                String line = br.readLine();//first line is header
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",");
                    String full_name = tokens[IbdInfo.COLUMN_IBD_DB_FULL_NAME];
                    String date = tokens[IbdInfo.COLUMN_IBD_DB_DATE];
                    int ibd50_rank = Integer.parseInt(tokens[IbdInfo.COLUMN_IBD_DB_IBD50_RANK]);
                    float pct_off_high = Float.parseFloat(tokens[IbdInfo.COLUMN_IBD_DB_PCT_OFF_HIGH]);
                    int composite = Integer.parseInt(tokens[IbdInfo.COLUMN_IBD_DB_COMPOSITE]);
                    int eps = Integer.parseInt(tokens[IbdInfo.COLUMN_IBD_DB_EPS_RATING]);
                    int rs = Integer.parseInt(tokens[IbdInfo.COLUMN_IBD_DB_RS_RATING]);
                    String smr = tokens[IbdInfo.COLUMN_IBD_DB_SMR_RATING];
                    String acc_dis_rank = tokens[IbdInfo.COLUMN_IBD_DB_ACC_DIS_RATING];
                    String group_rank = tokens[IbdInfo.COLUMN_IBD_DB_GROUP_RATING];
                    float mgmt = Float.parseFloat(tokens[IbdInfo.COLUMN_IBD_DB_MGMT_PCT]);
                    int qtr = Integer.parseInt(tokens[IbdInfo.COLUMN_IBD_DB_QTR_RISE_SPONSORSHIP]);
                    IbdInfo ibd_info = new IbdInfo(symbol, full_name, ibd50_rank, pct_off_high, composite, eps, rs,
                        smr, acc_dis_rank, group_rank, mgmt, qtr);
                    ibd_info.setDate(AppUtil.stringToCalendarNoEx(date));
                    ibd_info.setState(Ibd50State.findState(tokens[IbdInfo.COLUMN_IBD_DB_STATE]));
                    ibd_infos.add(ibd_info);
                }
                br.close();
                ret.put(symbol, ibd_infos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static void mergeIbdInfo(File file_obj) throws IOException, BiffException {
//TODO use FileUtil.removeExtension()
//        String name = file_obj.getName();
//        String date = name.substring(0, name.indexOf(FrameworkConstants.EXTENSION_XLS));
        String date = FileUtil.removeExtension(file_obj.getName(), FrameworkConstants.EXTENSION_XLS);
        ArrayList<IbdInfo> ibd_infos = new ArrayList<>();
        Workbook wb = Workbook.getWorkbook(file_obj);
        Sheet sheet = wb.getSheet(0);

        //loop thru all rows with data, extract cell values into objects, build up ibd_infos
        for (int row = IbdInfo.ROW_IBD50_FIRST_DATA; row <= IbdInfo.ROW_IBD50_LAST_DATA; row++) {//sheet row 8 to 57 has real data
            String symbol = sheet.getCell(IbdInfo.COLUMN_IBD50_SYMBOL, row).getContents();
            String full_name = sheet.getCell(IbdInfo.COLUMN_IBD50_FULL_NAME, row).getContents();
            IbdInfo ibd_info = new IbdInfo(symbol, full_name);
//            ibd_info.setDate(AppUtil.stringToCalendarNoEx(date));
            ibd_info.setFullName(full_name);
            ibd_info.setRank(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_RANK, row).getContents()));
            ibd_info.setPctOffHigh(Float.parseFloat(sheet.getCell(IbdInfo.COLUMN_IBD50_PCT_OFF_HIGH, row).getContents()));
            ibd_info.setComposite(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_COMPOSITE, row).getContents()));
            ibd_info.setEps(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_EPS_RATING, row).getContents()));
            ibd_info.setRs(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_RS_RATING, row).getContents()));
            ibd_info.setSmr(sheet.getCell(IbdInfo.COLUMN_IBD50_SMR_RATING, row).getContents());
            ibd_info.setAccDis(sheet.getCell(IbdInfo.COLUMN_IBD50_ACC_DIS_RATING, row).getContents());
            ibd_info.setGroupStrength(sheet.getCell(IbdInfo.COLUMN_IBD50_GROUP_RATING, row).getContents());
            ibd_info.setMgmtPercent(Float.parseFloat(sheet.getCell(IbdInfo.COLUMN_IBD50_PCT_MGMT_OWN, row).getContents()));
            String sponsor = sheet.getCell(IbdInfo.COLUMN_IBD50_QTR_RISE_SPONSORSHIP, row).getContents();
            if (sponsor.equals("N/A"))
                ibd_info.setSponsorship(0);
            else
                ibd_info.setSponsorship(Integer.parseInt(sponsor));
            ibd_infos.add(ibd_info);
        }
        wb.close();

        //attempt to open file for append, when no folder, create one, no file, create new
        File folder_path = new File(FrameworkConstants.DATA_FOLDER_IBD_DB);
        if (!folder_path.exists())//create folder
            folder_path.mkdir();
        for (IbdInfo ibd_info : ibd_infos) {
            String symbol = ibd_info.getSymbol();
            boolean new_file = false;
            File file_path = new File(FrameworkConstants.DATA_FOLDER_IBD_DB + File.separator + symbol + FrameworkConstants.EXTENSION_IBD);
            if (!file_path.exists())
                new_file = true;
            PrintWriter pw = new PrintWriter(new FileWriter(file_path, true));//open for append
            if (new_file)//new file, add header
                pw.println("Symbol,Date,Name,IBD 50 Rank,% Off High,Composite Rating,EPS Rating,RS Rating,SMR Rating," +
                    "Acc/Dis Rating,Group Rel Str Rating,Mgmt Own %,Qtrs of Rising Sponsorship");
            StringBuilder buf = new StringBuilder(ibd_info.getSymbol());
            buf.append(",")
                .append(date).append(",")
                .append(ibd_info.getFullName()).append(",")
                .append(ibd_info.getRank()).append(",")
                .append(ibd_info.getPctOffHigh()).append(",")
                .append(ibd_info.getComposite()).append(",")
                .append(ibd_info.getEps()).append(",")
                .append(ibd_info.getRs()).append(",")
                .append(ibd_info.getSmr()).append(",")
                .append(ibd_info.getAccDis()).append(",")
                .append(ibd_info.getGroupStrength()).append(",")
                .append(ibd_info.getMgmtPercent()).append(",")
                .append(ibd_info.getSponsorship()).append(",");
            pw.println(buf);
            pw.close();
        }
    }

    /**
     * To create a new IBD.db folder with files from scratch, delete this folder before calling this method.
     * @param sheets File objects representing IBD50 exports with names patterned "YYYY-MM-dd"
     * @param prog_bar print message to progress overlay if necessary, null = no prog bar
     * @throws Exception can't read some files
     */
    public static ArrayList<String> createIbdDb(File[] sheets, final ProgressBar prog_bar) {
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<String> prev_symbols = new ArrayList<>();
        ArrayList<String> inactive_symbols = new ArrayList<>();
        for (File sheet : sheets) {
            String date = FileUtil.removeExtension(sheet.getName(), FrameworkConstants.EXTENSION_XLS);
            ArrayList<IbdInfo> ibd_infos = null;
            try {
                ibd_infos = IbdInfo.importSheet(sheet);
            } catch (Exception e) {
                ret.add("File " + sheet.getName() + " can NOT be Read...  Possible Reason: " + e.getMessage());
                e.printStackTrace();
                continue;
            }
            ArrayList<String> cur_symbols = new ArrayList<>();
            for (IbdInfo ii : ibd_infos)
                cur_symbols.add(ii.getSymbol());

            //identify symbols go on list, go off list, active and inactive
            ArrayList<String> onlist_symbols = new ArrayList<>();
            ArrayList<String> offlist_symbols = new ArrayList<>();
            for (String sym : prev_symbols)
                if (!cur_symbols.contains(sym)) {
                    offlist_symbols.add(sym);//out of prev list, exiting...
                    cur_symbols.remove(sym);
                }
            for (String sym : cur_symbols)
                if (!prev_symbols.contains(sym))
                    onlist_symbols.add(sym);//new to current list, entering...
            for (String sym: onlist_symbols)
                cur_symbols.remove(sym);

            //cur_symbols now contains all active symbols, mark them in ibd_infos
            // also mark onlist from current file
            for (IbdInfo ii : ibd_infos) {
                String sym = ii.getSymbol();
                if (cur_symbols.contains(sym))
                    ii.setState(Ibd50State.Active);
                else if (onlist_symbols.contains(sym))
                    ii.setState(Ibd50State.Onlist);
            }

            //create empty Ibd50 objects, mark all offlist symbols, append to ibd_infos use current date
            for (String sym : offlist_symbols) {
                IbdInfo ii = new IbdInfo(sym, "");
                ii.setState(Ibd50State.Offlist);
                ii.setDate(AppUtil.stringToCalendarNoEx(date));
                ibd_infos.add(ii);
            }

            //remove symbols go from inactive(previous offlist) to onlist to avoid double entries
            for (String sym : onlist_symbols) {
                if (inactive_symbols.contains(sym))
                    inactive_symbols.remove(sym);
            }

            //mark inactive symbols from previously offlist symbols, append to ibd_infos
            for (String sym : inactive_symbols) {
                IbdInfo ii = new IbdInfo(sym, "");
                ii.setState(Ibd50State.Inactive);
                ii.setDate(AppUtil.stringToCalendarNoEx(date));
                ibd_infos.add(ii);
            }
            try {
                IbdInfo.persistIbdDb(ibd_infos);
            } catch (IOException e) {
                ret.add("Fail to Save IBD.db...  Possible Reason: " + e.getMessage());
                e.printStackTrace();
                continue;
            }

            //previous symbols = onlist + current, prep for next file
            prev_symbols = new ArrayList<>();
            for (String sym : onlist_symbols) prev_symbols.add(sym);
            for (String sym : cur_symbols) prev_symbols.add(sym);

            //inactive symbols = inactive symbols + offlist symbols
            for (String sym : offlist_symbols) inactive_symbols.add(sym);

            //show progress
            final File f = sheet;
            if (prog_bar != null)
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        prog_bar.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_3") + f.getAbsolutePath());
                    }
                });
        }
        return ret;
    }

    /**
     * Identify a list of Fridays in the past when each IBD50 list was created, read associated spreadsheets,
     * identify which symols go off list and go on list, store this information in Ibd50EntryExit.db
     * @param file_list list of IBD50 .xls files to discover entry onto list and exit off list dates
     * @exception Exception when file can't be read or spreadsheet corrupted
     */
    public static ArrayList<String> createIbd50EntryExitDb(File[] file_list, final ProgressBar prog_bar) throws Exception {
        //for each file with name of friday, sequentially walk thru chronologically two at a time
        // to identify a list of symbols that were removed or added
        ArrayList<String> ret = new ArrayList<>();
        ArrayList<String> prev_symbols = new ArrayList<>();
        ArrayList<EntryExitDates> ee_dates = new ArrayList<>();
        Calendar prev_cal = null;
        for (File file : file_list) {
            String friday_date = FileUtil.removeExtension(file.getName(), FrameworkConstants.EXTENSION_XLS);
            if (!FileUtil.isFridayFile(file, FrameworkConstants.EXTENSION_XLS))
                ret.add("File " + file.getName() + " does NOT correspond to a Friday !");//TODO move to bundle
            Calendar friday_cal = AppUtil.stringToCalendarNoEx(friday_date);
            ArrayList<String> cur_symbols = readSymbols(file);

            //find out entry and exit lists
            ArrayList<String> entry_symbols = new ArrayList<>();
            ArrayList<String> exit_symbols = new ArrayList<>();
            for (String sym : prev_symbols)
                if (!cur_symbols.contains(sym))
                    exit_symbols.add(sym);//out of prev list, exiting...
            for (String sym : cur_symbols)
                if (!prev_symbols.contains(sym))
                    entry_symbols.add(sym);//new to current list, entering...
            prev_symbols = cur_symbols;
System.out.println(new SimpleDateFormat("MM/dd").format(friday_cal.getTime()) + "=========================");
for (String s : entry_symbols) System.out.print("[" + s + "] "); System.out.println("---ENTRY---");
for (String s : exit_symbols) System.out.print("[" + s + "] "); System.out.println("----EXIT---");

            //add entry symbols to ee_dates
            boolean found = false;
            for (String sym : entry_symbols) {
                for (EntryExitDates ed : ee_dates) {
                    if (sym.equals(ed.getSymbol())) {//symbol is already in, add new pair
                        ed.addEntryDate(friday_cal);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    EntryExitDates ee_date = new EntryExitDates(sym, friday_cal);
                    ee_dates.add(ee_date);
                }
            }

            //for exit symbols, look up ee_dates and close open pairs
            for (String sym : exit_symbols) {
                for (EntryExitDates ee : ee_dates) {
                    if (sym.equals(ee.getSymbol())) {//can't find, skip
                        //use prev friday's date as exit such that IBD.db can pick up other info
                        ee.getCurrentPair().setPriorExit(prev_cal);
                        ee.setExitDate(friday_cal);//current pair null
                        break;
                    }
                }
            }
            prev_cal = friday_cal;
            final File f = file;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    prog_bar.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_3") + f.getAbsolutePath());
                }
            });
        }

        //persist ee_dates
        FileUtil.persistObject(ee_dates, new File(FrameworkConstants.DATA_FOLDER_IBD50_DATES_DB));
        return ret;
//TODO deal with exception, notify gap list
    }

    //xls sample: Export Date: 11:30 PM Eastern, Thursday, September 13, 2012
    //csv sample: "Export Date: 1:29 PM Eastern, Sunday, August 12, 2012",,,,,,,,,,,,,,,,,,,,,,,
    private static Calendar extractIbdDate(String date_str, boolean xls_file) {
        String[] tokens = date_str.split(",");
        String[] month_date = tokens[2].split(" ");
        String m = month_date[1];
        String month = "12";
        if (m.equals("January")) month = "01";
        else if (m.equals("February")) month = "02";
        else if (m.equals("March")) month = "03";
        else if (m.equals("April")) month = "04";
        else if (m.equals("May")) month = "05";
        else if (m.equals("June")) month = "06";
        else if (m.equals("July")) month = "07";
        else if (m.equals("August")) month = "08";
        else if (m.equals("September")) month = "09";
        else if (m.equals("October")) month = "10";
        else if (m.equals("November")) month = "11";
        String yr = tokens[3];
        if (!xls_file)
            yr = tokens[3].substring(0, tokens[3].indexOf("\"")).trim();
        String merge_date =  yr + "-" + month + "-" + month_date[2];
        return AppUtil.stringToCalendarNoEx(merge_date);
    }

    //filter out N/A in IBD portfolio imports
    public static String preProcessIntColumn(String cell_value) {
        if (cell_value.equals("N/A"))
            cell_value = "0";
        return cell_value;
    }
    public static String preProcessStringColumn(String cell_value) {
        if (cell_value.equals("N/A"))
            cell_value = "";
        return cell_value;
    }
    private static ArrayList<String> readSymbols(File ibd50_file) throws Exception{
        ArrayList<String> ret = new ArrayList<>();
        Workbook wb = Workbook.getWorkbook(ibd50_file);
        Sheet sheet = wb.getSheet(0);
        for (int row = IbdInfo.ROW_IBD50_FIRST_DATA; row <= IbdInfo.ROW_IBD50_LAST_DATA; row++)
            ret.add(sheet.getCell(IbdInfo.COLUMN_IBD50_SYMBOL, row).getContents());
        wb.close();
        return ret;
    }
    private boolean doesSymbolExist(String symbol, ArrayList<String> list) {
        for (String sym : list)
            if (sym.equals(symbol))
                return true;
        return false;
    }

    /**
     * Given a list of files that are IBD portfolio downloads(50 each), read each line, append to existing rating folder symbol files
     * if symbols don't exist in rating, new one will be created, all files take the form of XXX.txt
     *
     * @param portfolio_files list of files from this folder
     * @param current_time YAHOO date YYYY-MM-DD for files in this folder
     */
    public static void importSingleIbdPortfolio(File[] portfolio_files, Calendar current_time) {
        //  for each .xls file on this list, read its 50 symbols, form a line for each, write/append to matching files
        for (File file_obj : portfolio_files) {
            if (!file_obj.getName().startsWith("Por")) continue;//only files start with Por, filter out junk
//CoreUtil.setDeltaTimeStart("");
            Workbook wb = null;
            try { //open this sheet, read between row 5-59 to retrieve 6 ratings and symbol
                wb = Workbook.getWorkbook(file_obj);
                Sheet sheet = wb.getSheet(0);
                for (int row = IbdRating.ROW_IBDPORT_SYMBOL_BEGIN; row <= IbdRating.ROW_IBDPORT_SYMBOL_END; row++) {//IBD should fill between 6 and 60
                    String composite = sheet.getCell(IbdRating.COLUMN_IBDPORT_COMPOSITE, row).getContents();
                    if (composite.equals(""))//done here on empty row
                        break;

                    //read fields into a buffer
                    StringBuilder buf = new StringBuilder();
                    String symbol = sheet.getCell(IbdRating.COLUMN_IBDPORT_SYMBOL, row).getContents();
                    if (symbol.equals("BRKB"))
                        symbol = "BRK-B";
                    buf.append(FrameworkConstants.YAHOO_DATE_FORMAT.format(current_time.getTime())).append(",").append(composite).append(",")
                            .append(sheet.getCell(IbdRating.COLUMN_IBDPORT_EPS_RATING, row).getContents()).append(",")
                            .append(sheet.getCell(IbdRating.COLUMN_IBDPORT_RS_RATING, row).getContents()).append(",")
                            .append(sheet.getCell(IbdRating.COLUMN_IBDPORT_SMR_RATING, row).getContents()).append(",")
                            .append(sheet.getCell(IbdRating.COLUMN_IBDPORT_ACC_DIS_RATING, row).getContents()).append(",")
                            .append(sheet.getCell(IbdRating.COLUMN_IBDPORT_GROUP_RATING, row).getContents());

                    //rating folder should have already been created
                    File db_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_DB);
                    if (!db_folder.exists()) //remove all files in folder, keep folder
                        db_folder.mkdir();
                    File rating_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_RATING);
                    if (!rating_folder.exists())
                        rating_folder.mkdir();
                    String name = FrameworkConstants.DATA_FOLDER_IBD_RATING + File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE;
                    boolean exist = new File(name).exists();
                    if (!exist) {
                        PrintWriter pw = new PrintWriter(new FileWriter(name));//create
                        pw.println("Date(YYYY-MM-DD), Composite, EPS, RS, SMR, Acc-Dis, Group Strength");
                        pw.println(buf.toString());
                        pw.close();
                    }
                    else {
                        PrintWriter pw = new PrintWriter(new FileWriter(name, true));//append
                        pw.println(buf.toString());
                        pw.close();
                    }
//                    EventQueue.invokeLater(new Runnable() {
//                        public void run() {
//                            pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_5") + file_obj.getName() +
//                                    ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_6") + folder.getName() +
//                                    "] " + symbol + "...");
//                        }
//                    });
//                    sleep(1);//allow EDT to run and show message
//System.err.println(folder_name + " / " + file_obj.getName() + " : " + symbol + " --> " + buf.toString());
                }
                wb.close();
//CoreUtil.showDeltaTime("<ImportPanel.IbdThread().readSheet()> " + file_obj.getName());
            } catch (Exception e) {
                //TODO log error , can't read this file.....
                System.err.println("File -----> " + file_obj.getName());
                e.printStackTrace();
                if (wb != null)
                    wb.close();
            }
        }
    }

    //========== This group is for earning info database ==========
    public static void writeEarningInfoDb(ArrayList<EarningInfo> earning_info) throws IOException {
        String path = FrameworkConstants.EARNING_INFO_DB;
        File file = new File(path);
        PrintWriter pw = new PrintWriter(new FileWriter(file, true));
        if (!file.exists())
            pw.println("Ticker, Earning Date, Gap, Reaction");
        for (EarningInfo ei : earning_info) {
            StringBuilder sb = new StringBuilder();
            sb.append(ei.getSymbol()).append(",")
              .append(ei.getDate()).append(",")
              .append(ei.isGap() ? "Y" : "N").append(",")
              .append(ei.getReaction());
            pw.println(sb);
        }
        pw.close();
    }

    //========== This group is for fundamental database ==========
    /**
     * Read fundamental.db into memory
     * @return null = fail to read
     */
    public static HashMap<String, Fundamental> readFundamentalDb() {
        HashMap<String, Fundamental> ret = new HashMap<>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER_FUNDAMENTAL_DB));
        } catch (FileNotFoundException e) {
            MessageBox.messageBox(
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("ime_msg_1"),
                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
//            e.printStackTrace();
            return null;
        }
        try {
            String line;
            br.readLine();//skip first line header
            while ( (line = br.readLine()) != null ) {
                String[] tokens = line.split(",");
                ret.put(tokens[0], new Fundamental(line, false));
            }
            br.close();
        } catch (IOException e) {
            MessageBox.messageBox(
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("ime_msg_2"),
                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
            System.err.println("=====> " + e.getMessage());
//            e.printStackTrace();
        }
        return ret;
    }
    public static HashMap<String, Fundamental> mergeFinvizFile(File finviz_file, HashMap<String, Fundamental> db_map) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(finviz_file));
        String line;
        br.readLine();//skip first line header
        while ( (line = br.readLine()) != null ) {
            line = preprocessFinvizLine(line, true);
//            if (token0.startsWith("\'") && token0.endsWith("\"")) {//strip quotes from around symbol (finviz format)
            Fundamental finviz_record = new Fundamental(line, true);//fundamental DB has same column format as finviz
            String symbol = finviz_record.getSymbol();
            db_map.remove(symbol);//if not found ok
            db_map.put(symbol, finviz_record);
        }
        br.close();
        return db_map;
    }
    //replace comma within quote pairs, also remove quotes such that String.split() tokens don't have quotes
    public static String preprocessFinvizLine(String line, boolean comma_to_dot) {
        char[] in_chars = line.toCharArray();
        char[] out_chars = new char[in_chars.length];
        int in_idx = 0;
        int out_idx = 0;
        boolean quote_started = false;
        while (in_idx < in_chars.length) {//traverse input array
            if (!quote_started) {
                if (in_chars[in_idx] == '"')
                    quote_started = true;
                else //copy all others except quote
                    out_chars[out_idx++] = in_chars[in_idx];
            }
            else {//find matching quote, replace comma in between quote pairs
                if (in_chars[in_idx] == '"') {
                    quote_started = false;
                }
                else if (in_chars[in_idx] == ',')
                    out_chars[out_idx++] = '.';//replace
                else
                    out_chars[out_idx++] = in_chars[in_idx];
            }
            in_idx++;
        }
        return new String(out_chars, 0, out_idx);
    }
    /**
     * To write technical information back into database (technical.db) with supplied object.
     * Note the old database file is overwritten.
     * @param fund_map a TechnicalInfo hash map
     * @throws IOException file can not be written
     */
    public static void writeFundamentlDb(HashMap<String, Fundamental> fund_map) throws IOException {
        //first backup old database file add "-OLD" at end
        String path = FrameworkConstants.DATA_FOLDER_FUNDAMENTAL_DB;// + File.separator + FrameworkConstants.TECHNICAL_DATABASE;
        File file = new File(path);
        file.renameTo(new File(path + "-OLD"));

        //write to technical.db
        PrintWriter pw = new PrintWriter(new FileWriter(file, false));
        pw.println("Ticker,Company,Sector,Industry,Market Cap,P/E,PEG,P/S,P/B" +
                ",P/Cash,P/Free Cash Flow,Dividend Yield,EPS (ttm),EPS growth this year" +
                ",EPS growth past 5 years,Sales growth past 5 years,EPS growth quarter over quarter" +
                ",Sales growth quarter over quarter,Shares Outstanding,Shares Float,Insider Ownership" +
                ",Institutional Ownership,Float Short,Short Ratio,Return on Assets,Return on Equity" +
                ",Return on Investment,Current Ratio,Quick Ratio,LT Debt/Equity,Total Debt/Equity" +
                ",Gross Margin,Operating Margin,Profit Margin,Beta,Earnings Date, Last Update Date");
        Set<String> keys = fund_map.keySet();
        for (String key : keys) {
            Fundamental fund_info = fund_map.get(key);
            StringBuilder buf = new StringBuilder(fund_info.getSymbol());
            buf.append(",").append(fund_info.getFullName()).append(",")
            .append(fund_info.getSector()).append(",")
            .append(fund_info.getIndustry()).append(",")
            .append(fund_info.getMarketCap()).append(",")
            .append(fund_info.getPE()).append(",")
            .append(fund_info.getPEG()).append(",")
            .append(fund_info.getPS()).append(",")
            .append(fund_info.getPB()).append(",")
            .append(fund_info.getPCF()).append(",")
            .append(fund_info.getPFCF()).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getYield())).append(",")
            .append(fund_info.getEps()).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getEpsYtd())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getEps5Yr())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getSales5Yr())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getEpsQtr())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getSalesQtr())).append(",")
            .append(fund_info.getShares()).append(",")
            .append(fund_info.getSharesFloat()).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getInsiderPct())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getInstPct())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getSharesShort())).append(",")
            .append(fund_info.getShortRatio()).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getROA())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getROE())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getROI())).append(",")
            .append(fund_info.getCurrentRatio()).append(",")
            .append(fund_info.getQuickRatio()).append(",")
            .append(fund_info.getDebtToEquityLt()).append(",")
            .append(fund_info.getDebtToEquityTotal()).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getGrossMargin())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getOperatingMargin())).append(",")
            .append(FrameworkConstants.PCT_FORMAT.format(fund_info.getProfitMargin())).append(",")
            .append(fund_info.getBeta()).append(",")
            .append(fund_info.getEarningDate()).append(",");
            Calendar upd = fund_info.getLastUpdate();
            if (upd != null)
                buf.append(FrameworkConstants.YAHOO_DATE_FORMAT.format(upd.getTime())).append(",");
            else
                buf.append(",");
            pw.println(buf);
        }
        pw.close();
    }

    //========== For data base file access ==========
    /**
     * display a given file in provided text area, file must be text file
     * @param file File object
     * @param text_area JTextArea for showing
     * @throws IOException can't read file
     */
    public static void displayFile(File file, JTextArea text_area) throws IOException{
        text_area.setText("");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ( (line = br.readLine()) != null )
            text_area.append(line + "\n");
        text_area.setCaretPosition(0);//cursor back to beginning
    }

    public static ArrayList<String> getAllSymbolsInDb() {
        ArrayList<String> ret = new ArrayList<>();
        File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        String[] file_list = folder.list();//read file list
        for (String f : file_list) {
            //skip hidden files or non .txt files
            if (f.endsWith(FrameworkConstants.EXTENSION_QUOTE) && !f.startsWith("."))
                ret.add(f.substring(0, f.indexOf(FrameworkConstants.EXTENSION_QUOTE)));
        }
        return ret;
    }

    //========== For Regression ==========

    /**
     * Create trendline from linear regression.
     * @param data_index indicies of quotes from FundQuote array to be used to create trend line
     * @return a regression line
     */
    public static SimpleRegression createTrendline(ArrayList<Integer> data_index) {
        return  null;
    }

    /**
     * Search from a given starting date till now to find highest N quotes for a given symbol
     * @param fund specific quotes of a symbol
     * @param start_date starting data
     * @param num_highs N highest quotes
     * @return array of indices to FundData
     */
    public static ArrayList<Integer> findHighQuotes(FundData fund, Calendar start_date, int num_highs) {
        ArrayList<Integer> ret = new ArrayList<>(num_highs);//sorted from high_idx value to low value order
        for (int ret_idx = 0; ret_idx < num_highs; ret_idx++)  ret.add(-1);//-1 = empty
        int start_idx = fund.findIndexByDate(AppUtil.calendarToString(start_date));
        ArrayList<FundQuote> quotes = fund.getQuote();
        for (int quote_idx = start_idx; quote_idx >= 0; quote_idx--) {
            float quote_high = quotes.get(quote_idx).getHigh();
            int ret_idx = 0;

            //traverse ret array, find slot to insert quote_idx
            while (ret_idx < num_highs) {
                Integer high_idx = ret.get(ret_idx);
                if (high_idx != -1) {//not empty
                    FundQuote quote = quotes.get(high_idx);
                    if (quote_high <= quote.getHigh()) {
                        ret_idx++;
                        continue;
                    }
                }

                //found slot at ret_idx, insert by pushing all elements downward
                for (int idx = ret.size() - 1; idx > ret_idx; idx--)
                    ret.set(idx, ret.get(idx - 1));
                ret.set(ret_idx, quote_idx);
                break;
            }
        }
        return ret;
    }

    /**
     * If online, connect to Yahoo to get most recent quotes for a list of symbols (usually 10-15 minutes delay)
     *   If market is not open today, last quotes is given
     *   Some symbols may fail, results are logged in logging window
     * @param symbols list of symbols
     */
    public static ArrayList<FundQuote> quickQuote(ArrayList<String> symbols) throws IOException {
        ArrayList<FundQuote> ret = new ArrayList<>();
        for (String sym : symbols) {
            FundQuote quote = getYahooQuote(sym);
            if (quote != null) ret.add(quote);
            else
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("pw_nq") + " " + sym, LoggingSource.PLANNING_SHEET);
        }
        return ret;
    }

    public static ArrayList<Position> obtainTradeStationPositions(File[] files) {
        //read TradeStation files into memory
        ArrayList<Position> ret = new ArrayList<>();
        ArrayList<LogMessage> failed_msgs = new ArrayList<>();//might fail, keep a list of errors
        for (File pf : files) {
            Workbook wb;
            try {
                wb = Workbook.getWorkbook(pf);
            } catch (Exception e) {//fail to read somehow
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_17") + pf.getName() + "<br><br>", LoggingSource.RISKMGR_ACCOUNT, e);
                continue;
            }

            //read all rows from file, store info to positions array
            Sheet sheet = wb.getSheet(0);
            int row = ROW_TSP_SYMBOL;
            while (true) {
                String symbol = sheet.getCell(COLUMN_TSP_SYMBOL, row).getContents();
                if (symbol == null || symbol.equals(""))
                    break;

                //convert special symbols for Yahoo format, TradeStation calls BRK.B
                if (symbol.equals("BRK.B")) symbol = "BRK-B";
                try {//obtain market value first
                    String mkc = sheet.getCell(COLUMN_TSP_MARKET_VALUE, row).getContents();
                    Number mkt_val;
                    try {
                        mkt_val = CASH_POSITIVE_BALANCE_FORMAT.parse(mkc);
                    } catch (ParseException e) {//fail to parse
                        failed_msgs.add(new LogMessage(LoggingSource.RISKMGR_ACCOUNT,
                                ApolloConstants.APOLLO_BUNDLE.getString("rm_70") + " " + symbol, e));
                        row++;
                        continue;
                    }

                    //for preferred stocks, treat them as cash instead
                    boolean is_pfd = false;
                    for (String ps : PREFERRED_SYMBOLS) {
                        if (symbol.equals(ps)) {
                            is_pfd = true;
                            break;
                        }
                    }
                    if (is_pfd) {
                        row++;
                        continue;//skip this symbol, go to next
                    }

                    //for normal symbols, read shares, cost, account, mkt value and stop
                    int shares = Integer.parseInt(sheet.getCell(COLUMN_TSP_QTY, row).getContents());
                    String contents = sheet.getCell(COLUMN_TSP_COST, row).getContents();
                    float cost = 0;
                    try {//wrong format, use 0 as cost
                        cost = FLOAT_FORMAT.parse(contents).floatValue();
                    } catch (ParseException e) {
                        failed_msgs.add(new LogMessage(LoggingSource.RISKMGR_ACCOUNT,
                                ApolloConstants.APOLLO_BUNDLE.getString("rm_66") + " " + symbol, e));
                    }
                    String acct = "xx1001";
                    if (pf.getName().contains("466")) acct = "xx466";
                    else if (pf.getName().contains("391")) acct = "xx391";
                    else if (pf.getName().contains("516")) acct = "xx516";
                    else if (pf.getName().contains("861")) acct = "xx861";
//TODO change how to do stops, basically merge with what's stored in previous files
                    //look for stops from map read from file stored previously, symbol not found, use -5% as stop
                    float stop = (1 - RiskMgrModel.DEFAULT_STOP_PCT) * cost;
                    HashMap<String, StopLevel> stops = RiskMgrModel.getInstance().getStopMap();
                    StopLevel stored_stop = stops.get(symbol);
                    if (stored_stop != null) //found from previous
                        stop = (float)stored_stop.getLevel();
                    float risk = shares * (cost - stop);
                    if (cost <= stop)//in profit
                        risk = 0;
                    else
                        risk = -risk;
                    StopLevelInfo sli = new StopLevelInfo(symbol, cost, shares, stop, 150);//150 appropriate
                    ret.add(new Position(symbol, shares, cost, stop, risk, 0, mkt_val.floatValue(), acct, sli));
                } catch (IOException e) {//can't read quotes
                    failed_msgs.add(new LogMessage(LoggingSource.RISKMGR_ACCOUNT,
                        symbol + ApolloConstants.APOLLO_BUNDLE.getString("rm_91"), e));
                } catch (IllegalArgumentException iae) {
                    failed_msgs.add(new LogMessage(LoggingSource.RISKMGR_ACCOUNT,
                        symbol + ApolloConstants.APOLLO_BUNDLE.getString("rm_68"), iae));
                }
                row++;
            }
            wb.close();
        }
        //inform user about errors
        if (failed_msgs.size() > 0) Props.Log.setValue(null, failed_msgs);
        return ret;
    }
    private static final int COLUMN_TSP_SYMBOL = 0;
    private static final int COLUMN_TSP_QTY = 1;
    private static final int COLUMN_TSP_COST = 5;
    private static final int COLUMN_TSP_MARKET_VALUE = 7;//0 based
    private static final int ROW_TSP_SYMBOL = 4;//A4 to ...
    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("#,###.##");
    public static final DecimalFormat CASH_POSITIVE_BALANCE_FORMAT = new DecimalFormat("$##,###.##");
    public static final DecimalFormat CASH_NEGATIVE_BALANCE_FORMAT = new DecimalFormat("($##,###.##)");
    public static final String[] PREFERRED_SYMBOLS = {
            "ARR.PB", "NLY.PC", "PSA.PO", "PSA.PX", "PSB.PT",
            "INN.PC", "RSO.PB", "JPM.PA", "WFC.PO", "JPM.PC",
            "PFF", "PGF", "PGX",
    };
}