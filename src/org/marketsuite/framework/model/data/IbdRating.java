package org.marketsuite.framework.model.data;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.resource.ApolloConstants;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.resource.ApolloConstants;

import java.io.*;
import java.text.ParseException;
import java.util.*;

//data object encapsulates IBD proprietary ratings such as EPS RS..etc
public class IbdRating {
    //CTOR:
    public IbdRating(String sym, String date, int composite, int epsRating, int rsRating, String smr, String accDis, String groupRating) {
        this.symbol = sym;
        this.date = AppUtil.stringToCalendarNoEx(date);
        this.composite = composite;
        this.epsRating = epsRating;
        this.rsRating = rsRating;
        this.smr = smr;
        this.accDis = accDis;
        this.groupRating = groupRating;
    }

    //generate a list of ratings from daily quotes (into weekly dates)
    public static ArrayList<IbdRating> readIbdWeeklyRating(FundData fund, int num_days) throws IOException {
        WeeklyQuote wq = new WeeklyQuote(fund, num_days);//eg. 60 = 2 months = 8 weeks
        return readIbdWeeklyRating(fund.getSymbol(), FrameworkConstants.DATA_FOLDER_IBD_RATING, wq);
    }

    //-----public methods-----
    //Read from rating data base into memory, note that data file consists of comma separate strings ordered by descending dates
    //  and could contain identical rows, not particularly ordered
    public static ArrayList<IbdRating> readIbdRating(String symbol, String path, int length) throws IOException {
        int count = 0;
        ArrayList<IbdRating> ret = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path + File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE));
        String line;
        int line_num = 1;

        //read comma separated file line by line
        while ( (line = br.readLine()) != null ) {
            if (line_num == 1) {
                line_num++;
                continue;//skip first header row
            }

            //parse line
            String[] tokens = line.split(",");

            //if returning array already has record of same date, skip row
            String date = tokens[TOKEN_DATE];
            if (doesRatingExist(date, ret)) continue;//sometimes DB contains records that are the same
            int composite = skipIntegerNAs(tokens[TOKEN_COMPOSITE]);
            int eps = skipIntegerNAs(tokens[TOKEN_EPS]);
            int rs = Integer.parseInt(tokens[TOKEN_RS]);//RS should never have N/A
            String smr = skipStringNAs(tokens[TOKEN_SMR]);
            String acc_dis = skipStringNAs(tokens[TOKEN_ACC_DIS]);
            String grp = skipStringNAs(tokens[TOKEN_GROUP_STRENGTH]);
            IbdRating rating = new IbdRating(symbol, date, composite, eps, rs, smr, acc_dis, grp);
            ret.add(rating);
            if (count > length)
                break;
        }
        br.close();

        //sort records in descending date ordering
        Collections.sort(ret, new IbdRatingComparator());
        Collections.reverse(ret);
        return ret;
    }

    //read from rating data base into memory ONLY dates matching Fridays (ie. weekly data), for plotting on weekly chart
    public static ArrayList<IbdRating> readIbdWeeklyRating(String symbol, String path, WeeklyQuote wq) throws IOException {
        ArrayList<IbdRating> ret = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path + File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE));
        String line;
        int line_num = 1;

        //read comma separated file line by line
        while ( (line = br.readLine()) != null ) {
            if (line_num == 1) {
                line_num++;
                continue;//skip first header row
            }

            //for each line read from file, if date matches any in weekly quote, keep it
            String[] tokens = line.split(",");

            //if returning array already has record of same date, skip row
            String date = tokens[TOKEN_DATE];
            ArrayList<FundQuote> quotes = wq.getQuotes();
            boolean found = false;
            for (FundQuote q : quotes) {
                if (date.equals(q.getDate())) {
                    found = true;
                    break;
                }
            }
            if (!found) continue;//not weekly date
            if (doesRatingExist(date, ret)) continue;//sometimes DB contains records that are the same
            int composite = skipIntegerNAs(tokens[TOKEN_COMPOSITE]);
            int eps = skipIntegerNAs(tokens[TOKEN_EPS]);
            int rs = Integer.parseInt(tokens[TOKEN_RS]);//RS should never have N/A
            String smr = skipStringNAs(tokens[TOKEN_SMR]);
            String acc_dis = skipStringNAs(tokens[TOKEN_ACC_DIS]);
            String grp = skipStringNAs(tokens[TOKEN_GROUP_STRENGTH]);
            IbdRating rating = new IbdRating(symbol, date, composite, eps, rs, smr, acc_dis, grp);
            ret.add(rating);
        }
        br.close();

        //sort records in descending date ordering
        Collections.sort(ret, new IbdRatingComparator());
        Collections.reverse(ret);
        return ret;
    }

    /**
     * read from a list of IBD portfolio .xls files, merge into provided rating maps with duplicate dates skipped,
     *   duplicate rating records (same date) is skipped in this process, typically used inside a thread
     *   errors stored in log window, attempt to finish for all symbols
     * @param portfolio_files files from same folder, MUST start with "Por"
     * @param rating_map updated with new symbols/rating records from files, empty ok
     * @param current_time file folder name contains the date
     */
    public static void importIbdPortfolio(File[] portfolio_files, HashMap<String, ArrayList<IbdRating>> rating_map, Calendar current_time) {
        //for each .xls file on this list, read its 50 symbols, form a line for each, write/append to matching files
        for (File file_obj : portfolio_files) {
            if (!file_obj.getName().startsWith("Por")) continue;//only files start with Por, filter out junk
            Workbook wb = null;
            try { //open this sheet, read between row 5-59 to retrieve 6 ratings and symbol
                wb = Workbook.getWorkbook(file_obj);
                Sheet sheet = wb.getSheet(0);
                for (int row = IbdRating.ROW_IBDPORT_SYMBOL_BEGIN; row <= IbdRating.ROW_IBDPORT_SYMBOL_END; row++) {//IBD should fill between 6 and 60
                    String composite = sheet.getCell(IbdRating.COLUMN_IBDPORT_COMPOSITE, row).getContents();
                    if (composite.equals(""))//done reading here on empty row
                        break;

                    String date = FrameworkConstants.YAHOO_DATE_FORMAT.format(current_time.getTime());
                    final String symbol = sheet.getCell(IbdRating.COLUMN_IBDPORT_SYMBOL, row).getContents();
                    String eps = sheet.getCell(IbdRating.COLUMN_IBDPORT_EPS_RATING, row).getContents();
                    String rs = sheet.getCell(IbdRating.COLUMN_IBDPORT_RS_RATING, row).getContents();
                    String smr = sheet.getCell(IbdRating.COLUMN_IBDPORT_SMR_RATING, row).getContents();
                    String acc_dis = sheet.getCell(IbdRating.COLUMN_IBDPORT_ACC_DIS_RATING, row).getContents();
                    String grp = sheet.getCell(IbdRating.COLUMN_IBDPORT_GROUP_RATING, row).getContents();
                    IbdRating rating = new IbdRating(symbol, date, convertEmpty(composite),
                            convertEmpty(eps), convertEmpty(rs), smr, acc_dis, grp);

//                    IbdRating rating = new IbdRating(symbol, date, Integer.parseInt(composite), Integer.parseInt(eps), Integer.parseInt(rs), smr, acc_dis, grp);
                    mergeToRatingMap(rating_map, symbol, date, rating);
                }
                wb.close();
            } catch (Exception e) {//continue even with error
                //TODO log error , can't read this file.....
                System.err.println("File -----> " + file_obj.getName());
                e.printStackTrace();
                if (wb != null)
                    wb.close();
            }
        }
    }

    /**
     * read single IBD50 xls file, merge into provided rating map and eliminate duplicate ratings with same dates of same symbol.
     *   errors stored in log window, attempt to finish for all symbols, typically used inside a thread
     * @param file_obj xls file, name is the date
     * @param rating_map map to be merged into, empty ok
     */
    public static void importIbd50(File file_obj, HashMap<String, ArrayList<IbdRating>> rating_map) {
        String date = FileUtil.removeExtension(file_obj.getName(), FrameworkConstants.EXTENSION_XLS);//file name is date
        Workbook wb = null;
        try {
            wb = Workbook.getWorkbook(file_obj);
            Sheet sheet = wb.getSheet(0);

            //loop thru all rows with data, extract cell values into objects, build up ibd_infos
            for (int row = ROW_IBD50_FIRST_DATA; row <= ROW_IBD50_LAST_DATA; row++) {//sheet row 8 to 57 has real data
                String symbol = sheet.getCell(COLUMN_IBD50_SYMBOL, row).getContents();
                if (symbol.equals("")) break;//end of file
                int composite = Integer.parseInt(sheet.getCell(COLUMN_IBD50_COMPOSITE, row).getContents());
                int eps = Integer.parseInt(sheet.getCell(COLUMN_IBD50_EPS_RATING, row).getContents());
                int rs = Integer.parseInt(sheet.getCell(COLUMN_IBD50_RS_RATING, row).getContents());
                String smr = sheet.getCell(COLUMN_IBD50_SMR_RATING, row).getContents();
                String acc_dis = sheet.getCell(COLUMN_IBD50_ACC_DIS_RATING, row).getContents();
                String grp = sheet.getCell(COLUMN_IBD50_GROUP_RATING, row).getContents();
                String rank = sheet.getCell(COLUMN_IBD50_RANK, row).getContents();
                IbdRating rating = new IbdRating(symbol, date, composite, eps, rs, smr, acc_dis, grp);
                rating.setIbd50Rank(Integer.parseInt(rank));
                mergeToRatingMap(rating_map, symbol, date, rating);
            }
        } catch (IOException | BiffException e) {
            e.printStackTrace();//TODO log to log window....still continue on
        }
        wb.close();
    }

    //open IBD50 sheet, create rating map from its 50 symbols
    public static HashMap<String, IbdRating> ibd50SheetToMap(File sheet_file) {
        HashMap<String, IbdRating> ret = new HashMap<>();
        String date = FileUtil.removeExtension(sheet_file.getName(), FrameworkConstants.EXTENSION_XLS);//file name is date
        Workbook wb;
        try {
            wb = Workbook.getWorkbook(sheet_file);
        } catch (IOException | BiffException e) {
            e.printStackTrace();
            LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_write") + " " +
                    sheet_file.getAbsolutePath(), LoggingSource.DATAMGR_IMPORT);
            return null;
        }
        Sheet sheet = wb.getSheet(0);

        //loop thru all rows with data, extract cell values into objects, build up ibd_infos
        for (int row = ROW_IBD50_FIRST_DATA; row <= ROW_IBD50_LAST_DATA; row++) {//sheet row 8 to 57 has real data
            String symbol = sheet.getCell(COLUMN_IBD50_SYMBOL, row).getContents();
            if (symbol.equals("")) break;//end of file
            int composite = Integer.parseInt(sheet.getCell(COLUMN_IBD50_COMPOSITE, row).getContents());
            int eps = Integer.parseInt(sheet.getCell(COLUMN_IBD50_EPS_RATING, row).getContents());
            int rs = Integer.parseInt(sheet.getCell(COLUMN_IBD50_RS_RATING, row).getContents());
            String smr = sheet.getCell(COLUMN_IBD50_SMR_RATING, row).getContents();
            String acc_dis = sheet.getCell(COLUMN_IBD50_ACC_DIS_RATING, row).getContents();
            String grp = sheet.getCell(COLUMN_IBD50_GROUP_RATING, row).getContents();
            String rank = sheet.getCell(COLUMN_IBD50_RANK, row).getContents();
            IbdRating rating = new IbdRating(symbol, date, composite, eps, rs, smr, acc_dis, grp);
            rating.setIbd50Rank(Integer.parseInt(rank));
            ret.put(symbol, rating);
        }
        wb.close();
        return ret;
    }

    //open IBD portfolio folder files, create rating map from all 50 x N symbols
    public static HashMap<String, IbdRating> ibdPortfolioToMap(File file_obj, Calendar rating_date) throws IOException, BiffException{
        HashMap<String, IbdRating> ret = new HashMap<>();
            Workbook wb = Workbook.getWorkbook(file_obj);
            Sheet sheet = wb.getSheet(0);
            for (int row = IbdRating.ROW_IBDPORT_SYMBOL_BEGIN; row <= IbdRating.ROW_IBDPORT_SYMBOL_END; row++) {//IBD should fill between 6 and 60
                String composite = sheet.getCell(IbdRating.COLUMN_IBDPORT_COMPOSITE, row).getContents();
                if (composite.equals(""))//done reading here on empty row
                    break;
                String date = FrameworkConstants.YAHOO_DATE_FORMAT.format(rating_date.getTime());
                final String symbol = sheet.getCell(IbdRating.COLUMN_IBDPORT_SYMBOL, row).getContents();
                String eps = sheet.getCell(IbdRating.COLUMN_IBDPORT_EPS_RATING, row).getContents();
                String rs = sheet.getCell(IbdRating.COLUMN_IBDPORT_RS_RATING, row).getContents();
                String smr = sheet.getCell(IbdRating.COLUMN_IBDPORT_SMR_RATING, row).getContents();
                String acc_dis = sheet.getCell(IbdRating.COLUMN_IBDPORT_ACC_DIS_RATING, row).getContents();
                String grp = sheet.getCell(IbdRating.COLUMN_IBDPORT_GROUP_RATING, row).getContents();
                IbdRating rating = new IbdRating(symbol, date, convertEmpty(composite),
                    convertEmpty(eps), convertEmpty(rs), smr, acc_dis, grp);

                if (ret.get(symbol) != null)
                LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_dup_date") + " " +
                    symbol + " on " + FrameworkConstants.YAHOO_DATE_FORMAT.format(rating_date.getTime()), LoggingSource.DATAMGR_IMPORT);
                ret.put(symbol, rating);
            }
            wb.close();
        return ret;
    }

    //merge content of map (symbol -> array of ratings) into DB files
    //if rating's date already exists in DB, skip the entire map since it must have been done before
    public static void ratingMapToDb(HashMap<String, IbdRating> rating_map, Calendar rating_date) {
        Iterator<String> itor = rating_map.keySet().iterator();
        while (itor.hasNext()) {
            String symbol = itor.next();
            IbdRating cur_rating = rating_map.get(symbol);
            ArrayList<IbdRating> prev_ratings;
            try {
                prev_ratings = readIbdRating(symbol, FrameworkConstants.DATA_FOLDER_IBD_RATING_DB, 100);//todo use variable instead of 100
            } catch (IOException e) {//DB has no such symbol, start new
                ArrayList<IbdRating> rts = new ArrayList<>();
                rts.add(cur_rating);
                try {
                    createRatingDb(symbol, rts);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_write") + " " +
                        symbol, LoggingSource.DATAMGR_IMPORT);
                }
                continue;
            }

            //search match_date date in previous ratings
            boolean match_date = false;
            for (IbdRating rt : prev_ratings) {
                if (rt.getDate().equals(rating_date)) {
                    match_date = true;
                    break;
                }
            }
            if (match_date) {//if date already exist in one symbol, implies this sheet has been done before, quit
                LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_dup_date") + " " +
                    symbol + " on " + FrameworkConstants.YAHOO_DATE_FORMAT.format(rating_date.getTime()), LoggingSource.DATAMGR_IMPORT);
                return;
            }

            //unique date, add cur_rating, write back DB file
            prev_ratings.add(cur_rating);
            try {
                createRatingDb(symbol, prev_ratings);
            } catch (IOException e) {
                e.printStackTrace();
                LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_write") + " " +
                    symbol, LoggingSource.DATAMGR_IMPORT);
            }
        }
    }

    //create from scratch, all files from ibdrating.db folder are deleted first, typically used inside a thread
    public static void createRatingDb(HashMap<String, ArrayList<IbdRating>> rating_map) throws IOException {
        //managing files
        File db_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_RATING_DB);
        if (!db_folder.exists()) //create folder if not there
            db_folder.mkdir();
        else {//remove all files within
            File[] files = db_folder.listFiles();
            if (files != null) {
                for (File f : files)
                    if (f.isFile())
                        f.delete();
            }
        }

        //traverse rating map, create 1 file per map entry
        Set<String> symbols = rating_map.keySet();
        for (String sym : symbols) {
            String name = FrameworkConstants.DATA_FOLDER_IBD_RATING_DB + File.separator + sym + FrameworkConstants.EXTENSION_QUOTE;
            PrintWriter pw = new PrintWriter(new FileWriter(name));//each symbol is a new file
            pw.println(ApolloConstants.APOLLO_BUNDLE.getString("imp_rating_hdr"));//header
            ArrayList<IbdRating> ratings = rating_map.get(sym);

            //sort first by descending time
            IbdRating[] rts = new IbdRating[ratings.size()]; int i = 0;
            for (IbdRating rt : ratings)
                rts[i++] = rt;
            Arrays.sort(rts, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    IbdRating r1 = (IbdRating)o1;
                    IbdRating r2 = (IbdRating)o2;
                    return r2.getDate().compareTo(r1.getDate());
                }
            });
            for (IbdRating rt : rts) {//each rating record a separate line of same file
                StringBuilder buf = new StringBuilder();
                buf.append(AppUtil.calendarToString(rt.getDate())).append(",")
                    .append(rt.getComposite()).append(",").append(rt.getEpsRating()).append(",")
                    .append(rt.getRsRating()).append(",").append(rt.getSmr()).append(",").append(rt.getAccDis()).append(",")
                    .append(rt.getGroupRating());
                pw.println(buf.toString());
            }
            pw.close();
        }
    }
    //append new records in map into ibd.db for each symbol, involves skipping same symbol with duplicate dates, then write each file out
    //  typically used inside a thread
    public static void appendRatingToDb(HashMap<String, ArrayList<IbdRating>> rating_map) {

    }

    /**
     * does rating show 1 week hook up on weekly chart?
     * @param ratings of symbol of interest, assuming it is already sored in descending order
     * @param num_weeks consecutive hook up weeks, force to 1 for now
     * @param date at some point in time
     * @param hook_up true = look for hook up
     * @return false = not hookup, no data
     */
    public static boolean doCompRsHook(ArrayList<IbdRating> ratings, int num_weeks, Calendar date, boolean hook_up) {
        //find matching date
        for (int idx = 0; idx < ratings.size(); idx++) {//from more current to older days
            Calendar rating_date = ratings.get(idx).getDate();
            if (date.compareTo(rating_date) >= 0) {//found date
                //if less than N data points available, can't decide
                if ( (idx + num_weeks + 1) >= ratings.size() ) return false;

                //idx = point to look back N bars (num_weeks + 1)
                return doesCompositeHook(ratings, idx, hook_up) && doesRsHook(ratings, idx, hook_up);
            }
        }
        return false;//not found
    }

    //start_index = most recent index, if comp > 90 count as true
    //for buying
    public static boolean isCompositeUp(ArrayList<IbdRating> ratings, int start_index, int high_threshold) {
        int comp0 = ratings.get(start_index).getComposite();
        int comp1 = ratings.get(start_index + 1).getComposite();
        //if composite is greater or equal to high_threshold, for buying
        //   it's considered to be up regardless of it's real direction
        return (comp0 > comp1) || (comp0 >= high_threshold && comp1 >= high_threshold);
    }
    public static boolean isCompositeFlatOrUp(ArrayList<IbdRating> ratings, int start_index, int high_threshold) {
        int comp0 = ratings.get(start_index).getComposite();
        int comp1 = ratings.get(start_index + 1).getComposite();
        //if composite is greater or equal to high_threshold, for buying
        //   it's considered to be up regardless of it's real direction
        return (comp0 >= comp1) || (comp0 >= high_threshold && comp1 >= high_threshold);
    }
    public static boolean isRsUp(ArrayList<IbdRating> ratings, int start_index, int high_threshold) {
        int rs0 = ratings.get(start_index).getRsRating();
        int rs1 = ratings.get(start_index + 1).getRsRating();
        //if rs is greater or equal to high_threshold, for buying
        //   it's considered to be up regardless of it's real direction
        return (rs0 > rs1) || (rs0 >= high_threshold && rs1 >= high_threshold);
    }
    public static boolean isRsFlatOrUp(ArrayList<IbdRating> ratings, int start_index, int high_threshold) {
        int rs0 = ratings.get(start_index).getRsRating();
        int rs1 = ratings.get(start_index + 1).getRsRating();
        //if rs is greater or equal to high_threshold, for buying
        //   it's considered to be up regardless of it's real direction
        return (rs0 >= rs1) || (rs0 >= high_threshold && rs1 >= high_threshold);
    }

    //for selling
    public static boolean isCompositeDown(ArrayList<IbdRating> ratings, int start_index) {
        int comp0 = ratings.get(start_index).getComposite();
        int comp1 = ratings.get(start_index + 1).getComposite();
        return comp0 < comp1;
    }
    public static boolean isCompositeFlatOrDown(ArrayList<IbdRating> ratings, int start_index) {
        int comp0 = ratings.get(start_index).getComposite();
        int comp1 = ratings.get(start_index + 1).getComposite();
        return comp0 <= comp1;
    }
    public static boolean isRsDown(ArrayList<IbdRating> ratings, int start_index) {
        int rs0 = ratings.get(start_index).getRsRating();
        int rs1 = ratings.get(start_index + 1).getRsRating();
        return rs0 < rs1;
    }
    public static boolean isRsFlatOrDown(ArrayList<IbdRating> ratings, int start_index) {
        int rs0 = ratings.get(start_index).getRsRating();
        int rs1 = ratings.get(start_index + 1).getRsRating();
        return rs0 <= rs1;
    }
    public static boolean doCompRsHookup(ArrayList<IbdRating> ratings) {
        return ( highComposite(ratings) || (isCompositeUp(ratings, 0, 95) && !isCompositeUp(ratings, 1, 95)) ) &&
               ( (highRs(ratings)) || (isRsUp(ratings, 0, 95) && !isRsUp(ratings, 1, 95)) );
    }
    public static boolean doCompRsHookdown(ArrayList<IbdRating> ratings) {
        return isCompositeDown(ratings, 0) && !isCompositeDown(ratings, 1) &&
               isRsDown(ratings, 0) && !isRsDown(ratings, 1);
    }
    public static IbdRating findMatch(String date, ArrayList<IbdRating> ratings) {
        for (IbdRating rating : ratings)
            if (AppUtil.calendarToString(rating.getDate()).equals(date))
                return rating;
        return null;
    }

    //Repair rating data to fill in "gaps" (no data available for some dates) to make data continuous
    public static void fillGaps(ArrayList<IbdRating> ratings, WeeklyQuote wq) {
        //find out index of gaps from matching weekly quotes
//        ArrayList<FundQuote> quotes = wq.getQuotes();
//        ArrayList<Integer> gap_dates = new ArrayList<>();//based on wq
//        Calendar oldest_date = ratings.get(ratings.size() - 1).getDate();
//        for (int idx = 0; idx < quotes.size(); idx++) {
//            Calendar cal = AppUtil.stringToCalendarNoEx(quotes.get(idx).getDate());
//            if (cal.compareTo(oldest_date) < 0) break;//don't continue if no more rating data
//            IbdRating r = findMatch(quotes.get(idx).getDate(), ratings);
//            if (r == null) gap_dates.add(idx);
//        }
        ArrayList<String> gap_dates = findGaps(ratings, wq);
        if (gap_dates.size() == 0) return;//no filling needed

        //look for two types of gap (between 2 good data points R x R, or R x x R), interpolate
        ArrayList<FundQuote> quotes = wq.getQuotes();
        String sym = quotes.get(0).getSymbol();
if (sym.equals("EJ"))
    System.err.println("---------");
        for (String date : gap_dates) {
            //for this gap date, look at a week ago and a week after to see if R x R pattern
            //find from weekly quote the previous Friday and next Friday
            int widx = findQuoteIndexByDate(date, wq);
            if (widx == -1) continue;//ignore
            FundQuote qm1 = quotes.get(widx - 1); FundQuote qp1 = quotes.get(widx + 1);//prev week / next week
            if (qm1 == null || qp1 == null) continue;//both edge of wq, skip
            IbdRating rm1 = findMatch(qm1.getDate(), ratings);
            IbdRating rp1 = findMatch(qp1.getDate(), ratings);
            if (rm1 != null && rp1 != null) {//R x R found, insert into rating array
                IbdRating fake_rating = new IbdRating(sym, quotes.get(widx).getDate(),
                    (rm1.getComposite() + rp1.getComposite())/2, (rm1.getEpsRating() + rp1.getEpsRating())/2,
                    (rm1.getRsRating() + rp1.getRsRating())/2, "", "", "");
                ratings.add(fake_rating);
                LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("l2_fakerating") + " "
                        + sym + " on " + date + " (R x R)", LoggingSource.SIMULATOR_LSQUARE);
            }
            else {//look for R x x R only
                if (rp1 == null && rm1 == null) {//both prev week and next week have no rating, at least R x x x R case, give up
                    LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("l2_bighole") + " "
                        + sym + " near " + date, LoggingSource.SIMULATOR_LSQUARE);
                    return;//holes is too big, don't try to fill
                }
                else if (rm1 == null) {//prev week also has no rating, go back one more for  R x x R
                    qm1 = quotes.get(widx - 2);//TODO prevent qm1 == null, not likely for my data
                    rm1 = findMatch(qm1.getDate(), ratings);
                    if (rm1 == null) {
                        LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("l2_bighole") + " "
                                + sym + " near " + date, LoggingSource.SIMULATOR_LSQUARE);
                        return;//holes is too big, don't try to fill
                    }

                    //R x x R case, fill in fake ratings
                    IbdRating fake_rating = new IbdRating(sym, quotes.get(widx - 2).getDate(),
                        (rm1.getComposite() + rp1.getComposite())/3, (rm1.getEpsRating() + rp1.getEpsRating())/3,
                        (rm1.getRsRating() + rp1.getRsRating())/3, "", "", "");
                    ratings.add(fake_rating);
                    LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("l2_fakerating") + " "
                            + sym + " on " + quotes.get(widx - 2).getDate() + " (R x x R)", LoggingSource.SIMULATOR_LSQUARE);
                    fake_rating = new IbdRating(sym, quotes.get(widx - 1).getDate(),
                        (rm1.getComposite() + 2 * rp1.getComposite()) / 3, (rm1.getEpsRating() + 2 * rp1.getEpsRating()) / 3,
                        (rm1.getRsRating() + 2 * rp1.getRsRating()) / 3, "", "", "");
                    ratings.add(fake_rating);
                    LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("l2_fakerating") + " "
                            + sym + " on " + quotes.get(widx - 1).getDate() + " (R x x R)", LoggingSource.SIMULATOR_LSQUARE);
                }
                else {//next week also has no rating
                }
            }

        }
    }

    //TODO does rating show 1 week hook down on weekly chart?

    //Extract time part from portfolio folder name of "Portfolio YYYY-MM-DD" format
    // return null = fail to parse
    public static Calendar extractPortfolioTime(String folder_name) {
        int idx = folder_name.indexOf(" ");//get tail part of folder name = date
        String date_str = folder_name.substring(idx + 1, folder_name.length());//tail is date YYYY-MM-DD
        Date dt = null;
        try {
            dt = FrameworkConstants.YAHOO_DATE_FORMAT.parse(date_str);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal;
    }

    //-----inner classes-----
    public static class IbdRatingComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Calendar cal1 = ((IbdRating)o1).getDate();
            Calendar cal2 = ((IbdRating)o2).getDate();
            return cal1.compareTo(cal2);
        }
    }

    //-----private methods-----
    //find if element has the same date
    private static boolean doesRatingExist(String date, ArrayList<IbdRating> ratings) {
        for (IbdRating rating : ratings) {
            if (date.equals(AppUtil.calendarToString(rating.getDate())))
                return true;
        }
        return false;
    }
    private static String skipStringNAs(String token) { return token.equals("N/A") ? "" : token; }
    private static int skipIntegerNAs(String token) {
        if (token.equals("N/A")) return 0;
        return Integer.parseInt(token);
    }
    private static boolean doesCompositeHook(ArrayList<IbdRating> ratings, int index, boolean hook_up) {
        if (hook_up) {//up - this week
            int comp0 = ratings.get(index).getComposite();
            int comp1 = ratings.get(index + 1).getComposite();
            int comp2 = ratings.get(index + 2).getComposite();
            return (comp0 > comp1 && comp1 < comp2);
        }
        else {//down
            int comp0 = ratings.get(index).getComposite();
            int comp1 = ratings.get(index + 1).getComposite();
            int comp2 = ratings.get(index + 2).getComposite();
            return (comp0 < comp1 && comp1 > comp2);
        }
    }
    private static boolean doesRsHook(ArrayList<IbdRating> ratings, int index, boolean hook_up) {
        if (hook_up) {
            int rs0 = ratings.get(index).getRsRating();
            int rs1 = ratings.get(index + 1).getRsRating();
            int rs2 = ratings.get(index + 2).getRsRating();
            return (rs0 > rs1 && rs1 < rs2);
        }
        else {
            int rs0 = ratings.get(index).getRsRating();
            int rs1 = ratings.get(index + 1).getRsRating();
            int rs2 = ratings.get(index + 2).getRsRating();
            return (rs0 < rs1 && rs1 > rs2);
        }
    }
    private static boolean highComposite(ArrayList<IbdRating> ratings) {
        return (ratings.get(0).getComposite() > 95) && (ratings.get(1).getComposite() > 95) && (ratings.get(2).getComposite() > 95);
    }
    private static boolean highRs(ArrayList<IbdRating> ratings) {
        return (ratings.get(0).getRsRating() > 95) && (ratings.get(1).getRsRating() > 95) && (ratings.get(2).getRsRating() > 95);
    }
    private static ArrayList<String> findGaps(ArrayList<IbdRating> ratings, WeeklyQuote wq) {
        ArrayList<FundQuote> quotes = wq.getQuotes();
        ArrayList<String> gap_dates = new ArrayList<>();//based on wq
        Calendar oldest_date = ratings.get(ratings.size() - 1).getDate();
        for (int idx = 0; idx < quotes.size(); idx++) {
            String wdate = quotes.get(idx).getDate();
            Calendar cal = AppUtil.stringToCalendarNoEx(wdate);
            if (cal.compareTo(oldest_date) < 0) break;//don't continue if no more rating data
            IbdRating r = findMatch(wdate, ratings);
            if (r == null) gap_dates.add(wdate);
        }
        return gap_dates;
    }
    private static int findQuoteIndexByDate(String date, WeeklyQuote wq) {
        ArrayList<FundQuote> quotes = wq.getQuotes();
        for (int i = 0; i < quotes.size(); i++) {
            if (date.equals(quotes.get(i).getDate()))
                return i;
        }
        return -1;
    }
    private static void mergeToRatingMap(HashMap<String, ArrayList<IbdRating>> rating_map, String symbol, String date, IbdRating rating) {
        ArrayList<IbdRating> ratings = rating_map.get(symbol);
        if (ratings == null) {//not in map
            ratings = new ArrayList<>();
            ratings.add(rating);
            rating_map.put(symbol, ratings);//create new entry
        }
        else {//symbol is already there
            //find if any matching date, skip if found
            boolean match = false;
            for (IbdRating rt : ratings) {
                String dt = AppUtil.calendarToString(rt.getDate());
                if (dt.equals(date)) { //found one
                    match = true;
                    break;
                }
            }
            if (!match) {
                ratings.add(rating);
            }
        }
    }
    private static int convertEmpty(String inp) {
        if (inp.equals("N/A")) return 0;
        else return Integer.parseInt(inp);
    }

    //create a DB file for IBD rating for a symbol, no need to sort beforehand
    public static void createRatingDb(String symbol, ArrayList<IbdRating> ratings) throws IOException {
        //managing files
        File db_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_RATING_DB);
        if (!db_folder.exists()) //create folder if not there
            db_folder.mkdir();

        //sort first by descending time
        Collections.sort(ratings, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                IbdRating r1 = (IbdRating)o1;
                IbdRating r2 = (IbdRating)o2;
                return r2.getDate().compareTo(r1.getDate());
            }
        });
//        Collections.reverse(ratings);

        //write records in reverse chronological order
//        if (symbol.equals("BRKB"))
//            symbol = "BRK-B";
        String name = FrameworkConstants.DATA_FOLDER_IBD_RATING_DB + File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE;
        PrintWriter pw = new PrintWriter(new FileWriter(name));//each symbol is a new file
        pw.println(ApolloConstants.APOLLO_BUNDLE.getString("imp_rating_hdr"));//header
        for (IbdRating rt : ratings) {//each rating record a separate line of same file
            StringBuilder buf = new StringBuilder();
            buf.append(AppUtil.calendarToString(rt.getDate())).append(",")
               .append(rt.getComposite()).append(",").append(rt.getEpsRating()).append(",")
               .append(rt.getRsRating()).append(",").append(rt.getSmr()).append(",").append(rt.getAccDis()).append(",")
               .append(rt.getGroupRating());
            pw.println(buf.toString());
        }
        pw.close();
    }

    //-----accessor-----
    public Calendar getDate() { return date; }
    public int getComposite() { return composite; }
    public int getEpsRating() { return epsRating; }
    public int getRsRating() { return rsRating; }
    public String getSmr() { return smr; }
    public String getAccDis() { return accDis; }
    public String getGroupRating() { return groupRating; }
    public void setIbd50Rank(int ibd50Rank) { this.ibd50Rank = ibd50Rank; }

    //-----variables-----
    private String symbol;
    private Calendar date;
    private int composite;
    private int epsRating;
    private int rsRating;
    private String smr;
    private String accDis;
    private String groupRating;
    private int ibd50Rank;//-1 if not on the list, 1-50

    //-----literals-----
    private static final int TOKEN_DATE = 0;
    private static final int TOKEN_COMPOSITE = 1;
    private static final int TOKEN_EPS = 2;
    private static final int TOKEN_RS = 3;
    private static final int TOKEN_SMR = 4;
    private static final int TOKEN_ACC_DIS = 5;
    private static final int TOKEN_GROUP_STRENGTH = 6;

    //the following literals for IBD50 export format, some columns are skipped
    public static final int ROW_IBD50_EXPORT_DATE = 6;//index 6, file row 7
    public static final int ROW_IBD50_FIRST_DATA = 9;//index 9, file row 10
    public static final int ROW_IBD50_LAST_DATA = 58;//index 58, file row 59
    public static final int COLUMN_IBD50_EXPORT_DATE = 0;//index 0, file column 1
    public static final int COLUMN_IBD50_SYMBOL = 0;
    public static final int COLUMN_IBD50_FULL_NAME = 1;
    public static final int COLUMN_IBD50_RANK = 2;//1..50
    public static final int COLUMN_IBD50_PCT_OFF_HIGH = 6;
    public static final int COLUMN_IBD50_COMPOSITE = 9;//0-99
    public static final int COLUMN_IBD50_EPS_RATING = 10;//0-99
    public static final int COLUMN_IBD50_RS_RATING = 11;//0-99
    public static final int COLUMN_IBD50_SMR_RATING = 12;//A,B,C
    public static final int COLUMN_IBD50_ACC_DIS_RATING = 13;//A,B,C
    public static final int COLUMN_IBD50_GROUP_RATING = 14;//A,B,C
    public static final int COLUMN_IBD50_PCT_MGMT_OWN = 21;
    public static final int COLUMN_IBD50_QTR_RISE_SPONSORSHIP = 22;

    //for IBD ETF export format, all are index (base 0)
    public static final int ROW_IBDETF_EXPORT_DATE = 6;//index 6, file row 7
    public static final int ROW_IBDETF_FIRST_DATA = 9;//index 9, file row 10
    public static final int ROW_IBDETF_LAST_DATA = 295;//index 295, file row 296
    public static final int COLUMN_IBDETF_EXPORT_DATE = 0;//index 0, file column 1
    public static final int COLUMN_IBDETF_SYMBOL = 0;
    public static final int COLUMN_IBDETF_FULL_NAME = 1;
    public static final int COLUMN_IBDETF_ACC_DIS_RATING = 4;//A,B,C,A-,B+...etc
    public static final int COLUMN_IBDETF_RS_RATING = 5;//0-99
    public static final int COLUMN_IBDETF_YIELD = 6;//percent

    //for IBD Portfolio format
    public static final int COLUMN_IBDPORT_SYMBOL = 0;
    public static final int COLUMN_IBDPORT_FULL_NAME = 1;
    public static final int COLUMN_IBDPORT_COMPOSITE = 12;
    public static final int COLUMN_IBDPORT_EPS_RATING = 13;
    public static final int COLUMN_IBDPORT_RS_RATING = 14;
    public static final int COLUMN_IBDPORT_SMR_RATING = 15;
    public static final int COLUMN_IBDPORT_ACC_DIS_RATING = 16;
    public static final int COLUMN_IBDPORT_GROUP_RATING = 17;
    public static final int ROW_IBDPORT_SYMBOL_BEGIN = 5;//0 based row index
    public static final int ROW_IBDPORT_SYMBOL_END = 54;
    public static final String HEADER_IBDPORT = "date(YYYY-MM-DD), Composite, EPS, RS, SMR, Accu/Dis, Group Strength";

    //for IBD.db files format
    public static final int COLUMN_IBD_DB_SYMBOL = 0;
    public static final int COLUMN_IBD_DB_DATE = 1;
    public static final int COLUMN_IBD_DB_STATE = 2;//enum Ibd50State
    public static final int COLUMN_IBD_DB_FULL_NAME = 3;
    public static final int COLUMN_IBD_DB_IBD50_RANK = 4;
    public static final int COLUMN_IBD_DB_PCT_OFF_HIGH = 5;
    public static final int COLUMN_IBD_DB_COMPOSITE = 6;//0-99
    public static final int COLUMN_IBD_DB_EPS_RATING = 7;//0-99
    public static final int COLUMN_IBD_DB_RS_RATING = 8;//0-99
    public static final int COLUMN_IBD_DB_SMR_RATING = 9;//A,B,C
    public static final int COLUMN_IBD_DB_ACC_DIS_RATING = 10;//A,B,C
    public static final int COLUMN_IBD_DB_GROUP_RATING = 11;//A,B,C
    public static final int COLUMN_IBD_DB_MGMT_PCT = 12;
    public static final int COLUMN_IBD_DB_QTR_RISE_SPONSORSHIP = 13;
}
