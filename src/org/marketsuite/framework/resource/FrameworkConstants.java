package org.marketsuite.framework.resource;

import jxl.Sheet;
import jxl.Workbook;
import org.marketsuite.framework.model.AnnualReturn;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.type.RankingSamplePeriod;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.marektview.history.DividendRecord;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FrameworkConstants {
    public static final int MARKET_QUOTE_LENGTH = 450;//450;//about 1 year and half, 380 is good enough for 200EMA to agree with TOS
    public static final float START_CAPITAL = 10000;
    public final static ResourceBundle FRAMEWORK_BUNDLE =
       ResourceBundle.getBundle("org.marketsuite.framework.resource.FrameworkBundle", Locale.ENGLISH);
    public static final int CONDITION_NA = -1;
    public static final String[] LIST_PHASE = {
            "Bullish",
            "Recovery",
            "Accumulation",
            "Weak Warning",
            "Strong Warning",
            "Distribution",
            "Bearish",
    };

    //-----strategy list-----
    public static final String[] LIST_SIM_STRATEGY = {
        FRAMEWORK_BUNDLE.getString("advsim_lbl_2"),
        FRAMEWORK_BUNDLE.getString("advsim_lbl_3"),
        FRAMEWORK_BUNDLE.getString("advsim_lbl_4"),
        FRAMEWORK_BUNDLE.getString("advsim_lbl_5"),
    };
    public static final String[] LIST_STRATEGY = {
        FRAMEWORK_BUNDLE.getString("strategy_01"),
        FRAMEWORK_BUNDLE.getString("strategy_02"),
        FRAMEWORK_BUNDLE.getString("strategy_03"),
        FRAMEWORK_BUNDLE.getString("strategy_04"),
        FRAMEWORK_BUNDLE.getString("strategy_05"),
    };

    //-----File formats from different sources-----
    public static final int FILE_FORMAT_CUSTOM = 0;
    public static final int FILE_FORMAT_CLEC = 4;
    public static final int FILE_FORMAT_MDB = 1;
    public static final int FILE_FORMAT_SMT = 2;
    public static final int FILE_FORMAT_TEA_LEAF = 3;

    //-----Visual Definitions-----
    //colors
    public static final Color COLOR_LITE_GREEN = new Color(200, 255, 200);
    public static final Color COLOR_MEDIUM_GREEN = new Color(150, 255, 0);
    public static final Color LIGHT_PINK = new Color(252, 227, 250);
    public static final Color LIGHT_GREEN = new Color(227, 252, 230);
    //font
    public static final Font FONT_GIANT = new Font("Verdana", Font.PLAIN, 40);
    public static final Font FONT_VERY_BIG = new Font("Verdana", Font.PLAIN, 24);
    public static final Font BIG_FONT = new Font("Verdana", Font.PLAIN, 18);
    public static final Font MEDIUM_FONT = new Font("Verdana", Font.PLAIN, 14);
    public static final Font MEDIUM_BOLD_FONT = new Font("Verdana", Font.BOLD, 14);
    public static final Font MEDIUM_SMALL_FONT = new Font("Verdana", Font.BOLD, 12);
    public static final Font SMALL_FONT = new Font("Verdana", Font.PLAIN, 11);
    public static final Font SMALL_FONT_BOLD = new Font("Verdana", Font.BOLD, 11);
    public static final Font FONT_STANDARD = new Font("Verdana", Font.PLAIN, 10);
    public static final Font FONT_TINY = new Font("Verdana", Font.PLAIN, 9);
    public static final Font FONT_TINY_BOLD = new Font("Verdana", Font.BOLD, 9);
    //dimension
    public static final Dimension DEFAULT_FRAME_DIMENSION = new Dimension(1150, 800);

    //-----Output Format -----
    public static final DecimalFormat TWO_DIGIT_FORMAT = new DecimalFormat("00");
    public static final DecimalFormat INT_FORMAT = new DecimalFormat("##,##0,000");
    public static final DecimalFormat SMALL_INT_FORMAT = new DecimalFormat("##,##0");
    public static final DecimalFormat SHARE_FORMAT = new DecimalFormat("#00.000000");
    public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#0.00");
    public static final DecimalFormat PRICE_FORMAT2 = new DecimalFormat("#0.0");
    public static final DecimalFormat FORMAT_NUMBERS = new DecimalFormat("###,##0.00");
    public static final DecimalFormat DOLLAR_FORMAT = new DecimalFormat("$ ##,##0.00");
    public static final DecimalFormat DOLLAR_FORMAT2 = new DecimalFormat("$ ##,##0.0");
    public static final DecimalFormat NEGATIVE_CURRENCY_FORMAT = new DecimalFormat("$(##,##0.00)");
    public static final DecimalFormat MONEY_FORMAT = new DecimalFormat("$ #,##0.00");
    public static final DecimalFormat VOLUME_FORMAT = new DecimalFormat("####000000");
    public static final DecimalFormat VOLUME_FORMAT2 = new DecimalFormat("#,###,###,000");
    public static final DecimalFormat AVG_VOLUME_FORMAT = new DecimalFormat("######0,000");
    public static final DecimalFormat ROI_FORMAT = new DecimalFormat("#,##0.00 %");
    public static final DecimalFormat PCT_FORMAT = new DecimalFormat("#0.00%");
    public static final DecimalFormat PCT2_FORMAT = new DecimalFormat("#0.0%");
    public static final DecimalFormat DOLLAR2_FORMAT = new DecimalFormat("$#0");

    //----- YAHOO Input Format -----
    public static final SimpleDateFormat YAHOO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

    //----- Time FrameworkConstants -----
    public static final float SEC_PER_YEAR = 365 * 24 * 60 * 60;

    //----- Data Manager -----
//    public static final String SP500_YAHOO_SYMBOL = "^GSPC";
    public static final String DATABASE = "database";
    public static final String DATA_FOLDER_PC = "c:/database";
    public static String DATA_FOLDER_MAC = "/Users/chenat2012/database";
    public static String DATA_FOLDER = DATA_FOLDER_MAC;//this is calculated in Main based on runtime command line argument
    public static String DATA_FOLDER_QUOTE;
    public static String DATA_FOLDER_DAILY_QUOTE;
    public static String DATA_FOLDER_DAILY_SPLIT_INFO;
    public static String DATA_FOLDER_DAILY_SPLIT_QUOTE;
    public static String DATA_FOLDER_WEEKLY_QUOTE;
    public static String DATA_FOLDER_DIVIDEND;
    public static String DATA_FOLDER_IMPORT;
    public static String DATA_FOLDER_EXPORT;
    public static String DATA_FOLDER_REPORT;
    public static String DATA_FOLDER_ADVISER;
    public static String DATA_FOLDER_TRADE_LOG;
    public static String DATA_FOLDER_ACCOUNT;
    public static String DATA_FOLDER_PORTFOLIO;
    public static String DATA_FOLDER_FUNDAMENTAL_DB;
    public static String DATA_FOLDER_TECHNICAL_DB;
    public static String DATA_FOLDER_IBD50_DATES_DB;
    public static String DATA_FOLDER_IBD_DB;
    public static String DATA_FOLDER_IBD_RATING_DB;
    public static String DATA_FOLDER_RESOURCE;
    public static String DATA_FOLDER_BACKUP;
    public static String DATA_FOLDER_IBD_RATING;
    public static String DOWNLOAD_LIST;
    public static String EARNING_INFO_DB;
    public static String ACCOUNT_DB;
    public static String PORTFOLIO_DB;
    public static String STOPS_DB;
    public static String DATA_FILE_MARKET_HISTORY;
    //note: file name must use .txt or .csv extension, otherwise won't open, even though file manager doesn't show .txt in MAC
    public static final String EXTENSION_DIVIDEND = ".div";
    public static final String EXTENSION_QUOTE = ".txt";
    public static final String EXTENSION_ACCOUNT = ".act";
    public static final String EXTENSION_TRADES = ".csv";
    public static final String EXTENSION_CSV = ".csv";
    public static final String EXTENSION_XLS = ".xls";
    public static final String EXTENSION_XML = ".xml";
    public static final String EXTENSION_REPORTS = ".rpt";
    public static final String EXTENSION_TRADE_LOGS = ".trd";//open trades
    public static final String EXTENSION_JPEG = ".jpg";
    public static final String EXTENSION_PNG = ".png";
    public static final String EXTENSION_TRANSACTION = ".trn";//finished trades
    public static final String EXTENSION_IBD = ".ibd";//IbdInfo
    private static final String TECHNICAL_DATABASE = "technical.db";
    private static final String FUNDAMENTAL_DATABASE = "fundamental.db";
    private static final String IBD50_DATES_DATABASE = "IBD50Dates.db";
    private static final String ACCOUNT_DATABASE = "account.db";
    private static final String PORTFOLIO_DATABASE = "portfolio.db";
    private static final String IBD_RATING_DB_FOLDER = "ibdrating.db";
    private static final String IBD_DB_FOLDER = "IBD.db";
    public static final String IBD_RATING_FOLDER = "rating";
    public static final String IBD_RATING_SOURCE_PREFIX = "Portfolio ";
    private static final String RESOURCE = "resource";
    public static final String DUMP_FILE = "dump.xls";
    public static final String STOPS_DATABASE = "stops.db";
    public static final String CUSTOM_INDUSTRY_GROUP = "custom_group.xls";
    public static final String PLAN_SHEET = "plan_sheet.xls";
    public static void adjustDataFolder() {
        DATA_FOLDER_DIVIDEND = DATA_FOLDER + "/dividend";
        DATA_FOLDER_DAILY_QUOTE = DATA_FOLDER + "/quote/daily";
        DATA_FOLDER_QUOTE = DATA_FOLDER + "/quote";
        DATA_FOLDER_DAILY_SPLIT_INFO = DATA_FOLDER + "/quote/split-info";
        DATA_FOLDER_DAILY_SPLIT_QUOTE = DATA_FOLDER + "/quote/split-adjust";
        DATA_FOLDER_WEEKLY_QUOTE = DATA_FOLDER + "/quote/weekly";
        DATA_FOLDER_ADVISER = DATA_FOLDER + "/trade/adviser";
        DATA_FOLDER_IMPORT = DATA_FOLDER + "/import";
        DATA_FOLDER_EXPORT = DATA_FOLDER + "/export";
        DATA_FOLDER_REPORT = DATA_FOLDER + "/report";
        DOWNLOAD_LIST = DATA_FOLDER + "/quote/download_list.txt";
        DATA_FOLDER_TRADE_LOG = DATA_FOLDER + "/trade";
        DATA_FOLDER_ACCOUNT = DATA_FOLDER + "/account";
        DATA_FOLDER_PORTFOLIO = DATA_FOLDER + "/portfolio";
        DATA_FOLDER_TECHNICAL_DB = DATA_FOLDER + File.separator + TECHNICAL_DATABASE;
        DATA_FOLDER_FUNDAMENTAL_DB = DATA_FOLDER + File.separator + FUNDAMENTAL_DATABASE;
        DATA_FOLDER_IBD50_DATES_DB = DATA_FOLDER + File.separator + IBD50_DATES_DATABASE;
        DATA_FOLDER_IBD_DB = DATA_FOLDER + File.separator + IBD_DB_FOLDER;
        DATA_FOLDER_IBD_RATING_DB = DATA_FOLDER + File.separator + IBD_RATING_DB_FOLDER;
        DATA_FOLDER_IBD_RATING = DATA_FOLDER_IBD_DB + File.separator + IBD_RATING_FOLDER;
        DATA_FOLDER_RESOURCE = DATA_FOLDER + File.separator + RESOURCE;
        DATA_FOLDER_BACKUP = System.getProperty("user.home") + File.separator + "---BACKUP---";
        EARNING_INFO_DB = DATA_FOLDER + "/earning_info.db";
        ACCOUNT_DB = DATA_FOLDER + File.separator + ACCOUNT_DATABASE;
        PORTFOLIO_DB = DATA_FOLDER_ACCOUNT + File.separator + PORTFOLIO_DATABASE;
        STOPS_DB = DATA_FOLDER_ACCOUNT + File.separator + STOPS_DATABASE;
        DATA_FILE_MARKET_HISTORY = DATA_FOLDER + File.separator + MARKET_HISTORY_FILE;
    }

    //----- External Export File Types ----
    public static final int EXPORT_TYPE_IBD50 = 0;
    public static final int EXPORT_TYPE_IBD_PORTFOLIO = 1;
    public static final int EXPORT_TYPE_FINVIZ = 2;
    public static final int EXPORT_TYPE_BARCHART = 3;
    public static final int EXPORT_TYPE_DVG = 4;

    //----- SP500 Related Data -----
    public static final String SP500_YAHOO_SYMBOL = "^GSPC";
    public static final String SP500 = "SP500";
    public static final String MARKET_HISTORY_FILE = "Market History.xls";
    public static final String SP500_FIRST_FULL_YEAR = "1951";
    public static final int DIVIDEND_SHEET_NUMBER = 4;
    public static final int DIVIDEND_COLUMN_YEAR = 0;
    public static final int DIVIDEND_COLUMN_DIV = 1;
    public static FundData SP500_DATA;
    public static ArrayList<DividendRecord> SP500_DIVIDEND = new ArrayList<>();
    public static ArrayList<AnnualReturn> SP500_ANNUAL_RETURN = new ArrayList<>();
    //this gets called once in the beginning, false = fail to read
    public static boolean populateSp500Data(String quote_folder) {
        try {
            SP500_DATA = DataUtil.readFundHistory(quote_folder, SP500);

            //read SP500 dividend
            Workbook wb = Workbook.getWorkbook(new File(DATA_FILE_MARKET_HISTORY));
            Sheet sheet = wb.getSheet(DIVIDEND_SHEET_NUMBER);//4th sheet has SPX div history from 1870s
            int row = 1;
            do {
                if (row == sheet.getRows()) break;
                String year_str = sheet.getCell(DIVIDEND_COLUMN_YEAR, row).getContents();
                if (year_str.equals("")) break;
                String div_str = sheet.getCell(DIVIDEND_COLUMN_DIV, row++).getContents();

                //parse date string into Calendar
                Date date_obj = YEAR_FORMAT.parse(year_str);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date_obj);
                int yr = cal.get(Calendar.YEAR);
                if (yr < 1900) continue;//skip
                float div = Float.parseFloat(div_str);//convert quote into float
                SP500_DIVIDEND.add(new DividendRecord(SP500, cal, div));
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
//TODO add to last method: read/construct HistoricalRecord upon startup (from HistoryPanel)

    //search backwards from 1951 year by year, calculate each years gain/loss in percentage, store them in array of
    //  AnnualReturn for plotting and other comparisons, this assumes SP500_DATA quote array is filled up prior
    public static void calcSp500AnnualReturn() {
        String year = SP500_FIRST_FULL_YEAR;
        float begin_quote = -1;  float end_quote = -1;
        for (int index = SP500_DATA.getSize() - 1; index >= 0; index--) {//YAHOO data backwards
            String yr = SP500_DATA.getDate(index).substring(0, 4);
            int y = Integer.parseInt(year);
            //ignore any data prior to 1951
            if (yr.compareTo(year) < 0)
                continue;

            if (yr.equals(year)) {
                if (begin_quote == -1)
                    begin_quote = SP500_DATA.getPrice(index);//first day of a new year
            }
            else {//different year, wrap up previous year
                end_quote = SP500_DATA.getPrice(index + 1);//last index has last day from prev year
                float perf = (end_quote - begin_quote) / begin_quote;
                AnnualReturn ar = new AnnualReturn(SP500, y, perf);
                SP500_ANNUAL_RETURN.add(ar);
                year = SP500_DATA.getDate(index).substring(0, 4);
                begin_quote = SP500_DATA.getPrice(index);
            }
        }
        //remaining partial year, use the most recent data point as if it's end of year
        end_quote = SP500_DATA.getPrice(0);//most recent data point
        float perf = (end_quote - begin_quote) / begin_quote;
        AnnualReturn ar = new AnnualReturn(SP500, Integer.parseInt(year), perf);
        SP500_ANNUAL_RETURN.add(ar);
    }

    //calculate various Friday indices(into quote array) for ranking purposes, start index = use only this many quotes till present
    public static void calcFridays(int start_index) {
        _nWeeklyFridays = AppUtil.collectQuoteIndices(RankingSamplePeriod.WEEKLY, start_index);
        _nBiweeklyFridays = AppUtil.collectQuoteIndices(RankingSamplePeriod.BI_WEEKLY, start_index);
        _nMonthlyFridays = AppUtil.collectQuoteIndices(RankingSamplePeriod.MONTHLY, start_index);
    }
    public static ArrayList<Integer> getWeeklyFridays() { return _nWeeklyFridays; }
    public static ArrayList<Integer> getBiWeeklyFridays() { return _nBiweeklyFridays; }
    public static ArrayList<Integer> getMonthlyFridays() { return _nMonthlyFridays; }
    private static ArrayList<Integer> _nWeeklyFridays, _nBiweeklyFridays, _nMonthlyFridays;//pre-calculate

    //-----Fidelity sector funds: 2002 data useful for all funds-----
    private static final String[] FIDELITY_FUND_LIST = {
        "FBIOX", "FBMPX", "FBSOX", "FCYIX", "FDCPX",
        "FDFAX", "FDLSX", "FIDSX", "FIUIX", "FNARX",
        "FPHAX", "FSAIX", "FSAVX", "FSCGX", "FSAGX",
        "FSCHX", "FSCPX", "FSCSX", "FSDAX", "FSDCX",
        "FSDPX", "FSELX", "FSENX", "FSESX", "FSHCX",
        "FSHOX", "FSLBX", "FSLEX", "FSMEX", "FWRLX",
        "FSNGX", "FSPCX", "FSPHX", "FSPTX", "FSRBX",
        "FSRFX", "FSRPX", "FSTCX", "FSUTX", "FSVLX",
    };
    public static boolean isFidelityFund(String symbol) {
        for (String name : FIDELITY_FUND_LIST)
            if (name.equals(symbol))
                return true;
        return false;
    }

    //a list of years since 1950 (10 year increment) for combo for SP500 used in combo
    public static final String[] SP500_START_YEARS;//for start year combobox in buy/hold
    static {
        SP500_START_YEARS = new String[61];
        for (int yr=1950; yr<=2010; yr++)
            SP500_START_YEARS[yr-1950] = String.valueOf(yr);
    }

    public static final String[] LIST_CONNOR_STRATEGY = {
            FRAMEWORK_BUNDLE.getString("connor_lbl_1"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_2"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_3"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_4"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_5"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_6"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_11"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_12"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_13"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_14"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_15"),
            FRAMEWORK_BUNDLE.getString("connor_lbl_16"),
    };

    /**
     * Convert strategy string into index
     * @param strategy string representation
     * @return -1 if not found
     */
    public static int strategyStringToIndex(String strategy) {
        for (int i = 0; i<LIST_CONNOR_STRATEGY.length; i++)
            if (LIST_CONNOR_STRATEGY[i].equals(strategy))
                return i;
        return -1;
    }

    //----- Connor Strategy -----
    public static final int CONNOR_STRATEGY_3DAY_CHANNEL_LONG = 0;
    public static final int CONNOR_STRATEGY_RSI_25_LONG = 1;
    public static final int CONNOR_STRATEGY_R3_LONG = 2;
    public static final int CONNOR_STRATEGY_PERCENT_B_LONG = 3;
    public static final int CONNOR_STRATEGY_MUL_DAY_DOWN_LONG = 4;
    public static final int CONNOR_STRATEGY_RSI_10_6_LONG = 5;
    public static final int CONNOR_STRATEGY_3DAY_CHANNEL_SHORT = 6;
    public static final int CONNOR_STRATEGY_RSI_75_SHORT = 7;
    public static final int CONNOR_STRATEGY_R3_SHORT = 8;
    public static final int CONNOR_STRATEGY_PERCENT_B_SHORT = 9;
    public static final int CONNOR_STRATEGY_MUL_DAY_UP_SHORT = 10;
    public static final int CONNOR_STRATEGY_RSI_94_90_SHORT = 11;
    public static final String[] LIST_CONNOR_ETF = {//used for download
            //general market
            "DIA", "SPY", "RSP", "ONEQ", "QQQ", "MDY", "IWP", "IWM",
            "DDM", "SSO", "QLD", "MVV", "UWM", "UYG", "URE", "USD",
            "DOG", "SH", "PSQ", "DXD", "SDS", "QID", "TWM", "SRS",
            //sector
            "XLB", "XLE", "XLF", "XLI", "XLK", "XLP", "XLU", "XLV", "XLY",
            "IYM", "IYE", "IYW", "IYJ", "IYT", "IYZ", "IYF", "IYC", "IYR", "IAU", "IYH", "IDU",
            "UYM", "DIG", "LTL", "ROM", "UXI", "UPW", "UYG", "UCC", "URE", "UGE", "RXL", "USD",
            "DUG",
            //commodity
            "GLD", "SLV", "CEF", "PSLV", "PALL", "SLX", "CU", "REMX", "CORN", "JO", "USO",
            "AGQ", "ZSL",
            //world
            "VWO", "EEM", "EPP", "ILF", "RSX", "INP", "EPI", "FXI", "CAF", "EWZ", "VGK",
            "EWY", "EWT", "EWS", "EWH", "EWW", "EWJ", "EWM", "IF", "ECH", "EPU",
    };
}