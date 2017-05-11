package org.marketsuite.framework.market;

import org.marketsuite.framework.model.TechnicalInfo;
import org.marketsuite.framework.model.type.Ibd50State;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.FileUtil;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.marketsuite.framework.model.type.Ibd50State;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Data class storing IBD export column information
 */
public class IbdInfo {
    public IbdInfo(String symbol, String name) { this.symbol = symbol; this.fullName = name; }
    //CTOR: from one line in IBD export csv file
    public IbdInfo(String csv_line) {
        if (csv_line == null || csv_line.equals(""))
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("wl_msg_1"));

        //parse line into variables
        String[] tokens = csv_line.split(",");
        if (tokens.length < TechnicalInfo.COLUMN_QTR_RISE_SPONSORSHIP + 1)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("wl_msg_2"));
        symbol = tokens[TechnicalInfo.COLUMN_SYMBOL];
        fullName = tokens[TechnicalInfo.COLUMN_FULL_NAME];
        rank = Integer.parseInt(tokens[TechnicalInfo.COLUMN_IBD50_RANK]);
        pctOffHigh = Float.parseFloat(tokens[TechnicalInfo.COLUMN_PCT_OFF_HIGH]);
        composite = Integer.parseInt(tokens[TechnicalInfo.COLUMN_COMPOSITE]);
        eps = Integer.parseInt(tokens[TechnicalInfo.COLUMN_EPS_RATING]);
        rs = Integer.parseInt(tokens[TechnicalInfo.COLUMN_RS_RATING]);
        smr = tokens[TechnicalInfo.COLUMN_SMR_RATING];
        accDis = tokens[TechnicalInfo.COLUMN_ACC_DIS_RATING];
        groupStrength = tokens[TechnicalInfo.COLUMN_GROUP_RATING];
        mgmtPercent = Float.parseFloat(tokens[TechnicalInfo.COLUMN_MGMT_OWN]);
        sponsorship = Integer.parseInt(tokens[TechnicalInfo.COLUMN_QTR_RISE_SPONSORSHIP]);
    }

    public IbdInfo(String symbol, String full_name, int rank, float pctOffHigh, int composite, int eps, int rs,
                   String smr, String accDis, String groupStrength, float mgmt_pct, int sponsor) {
        this.symbol = symbol;
        fullName = full_name;
        this.rank = rank;
        this.pctOffHigh = pctOffHigh;
        this.composite = composite;
        this.eps = eps;
        this.rs = rs;
        this.smr = smr;
        this.accDis = accDis;
        this.groupStrength = groupStrength;
        mgmtPercent = mgmt_pct;
        sponsorship = sponsor;
    }

    public void markState(ArrayList<IbdInfo> ibd_infos, Ibd50State state) {

    }

    /**
     * Match specified date with first occurrence in IbdInfo object.
     * @param info_objs list of objects
     * @param date of interest
     * @return null = not found
     */
    public static IbdInfo findByDate(ArrayList<IbdInfo> info_objs, Calendar date) {
        for (IbdInfo info : info_objs) {
            if (date.equals(info.getDate()))
                return info;
        }
        return null;
    }
    public static IbdInfo findBySymbol(ArrayList<IbdInfo> info_objs, String symbol) {
        for (IbdInfo info : info_objs)
            if (info.getSymbol().equals(symbol))
                return info;
        return null;
    }
    /**
     * Import symbols and information from one IBD50 excel sheet, sheet carries name of pattern "YYYY-MM-dd".
     *    analyze entry/exit list dates, store collection into IBD.db folder. Note that the "state" can not be calculated till later.
     * @param file_obj the excel sheet e.g. "2013-08-30"
     * @throws java.io.IOException can't read file
     * @throws jxl.read.biff.BiffException corrupted spreadsheet
     */
    public static ArrayList<IbdInfo> importSheet(File file_obj) throws IOException, BiffException {
        String date = FileUtil.removeExtension(file_obj.getName(), FrameworkConstants.EXTENSION_XLS);
        ArrayList<IbdInfo> ibd_infos = new ArrayList<>();
        Workbook wb = Workbook.getWorkbook(file_obj);
        Sheet sheet = wb.getSheet(0);

        //loop thru all rows with data, extract cell values into objects, build up ibd_infos
        for (int row = IbdInfo.ROW_IBD50_FIRST_DATA; row <= IbdInfo.ROW_IBD50_LAST_DATA; row++) {//sheet row 8 to 57 has real data
            String symbol = sheet.getCell(IbdInfo.COLUMN_IBD50_SYMBOL, row).getContents();
            if (symbol.equals("")) break;
            String full_name = sheet.getCell(IbdInfo.COLUMN_IBD50_FULL_NAME, row).getContents();
            IbdInfo ibd_info = new IbdInfo(symbol, full_name);
            ibd_info.setDate(AppUtil.stringToCalendarNoEx(date));
            ibd_info.setFullName(full_name);
            String rank = sheet.getCell(IbdInfo.COLUMN_IBD50_RANK, row).getContents();
            ibd_info.setRank(Integer.parseInt(rank));
            ibd_info.setPctOffHigh(Float.parseFloat(sheet.getCell(IbdInfo.COLUMN_IBD50_PCT_OFF_HIGH, row).getContents()));
            ibd_info.setComposite(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_COMPOSITE, row).getContents()));
            ibd_info.setEps(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_EPS_RATING, row).getContents()));
            ibd_info.setRs(Integer.parseInt(sheet.getCell(IbdInfo.COLUMN_IBD50_RS_RATING, row).getContents()));
            ibd_info.setSmr(sheet.getCell(IbdInfo.COLUMN_IBD50_SMR_RATING, row).getContents());
            ibd_info.setAccDis(sheet.getCell(IbdInfo.COLUMN_IBD50_ACC_DIS_RATING, row).getContents());
            ibd_info.setGroupStrength(sheet.getCell(IbdInfo.COLUMN_IBD50_GROUP_RATING, row).getContents());
            ibd_info.setMgmtPercent(Float.parseFloat(sheet.getCell(IbdInfo.COLUMN_IBD50_PCT_MGMT_OWN, row).getContents()));
            String sponsor = sheet.getCell(IbdInfo.COLUMN_IBD50_QTR_RISE_SPONSORSHIP, row).getContents();
            if (sponsor.equals("N/A") || sponsor.equals(""))
                ibd_info.setSponsorship(0);
            else
                ibd_info.setSponsorship(Integer.parseInt(sponsor));
            ibd_infos.add(ibd_info);
        }
        wb.close();
        return ibd_infos;
    }

    /**
     * To save/append a list of IbdInfo objects to files under IBD.db
     * @param ibd_infos array of IbdInfo objects
     */
    public static void persistIbdDb(ArrayList<IbdInfo> ibd_infos) throws IOException {
//TODO avoid duplicate dates written to the same file...
        //attempt to open file for append, when no folder, create one, no file, create new
        File folder_path = new File(FrameworkConstants.DATA_FOLDER_IBD_DB);
        if (!folder_path.exists())//create folder
            folder_path.mkdir();
        for (IbdInfo ibd_info : ibd_infos) {
            String symbol = ibd_info.getSymbol();
            boolean new_file = false;
            File file_path = new File(FrameworkConstants.DATA_FOLDER_IBD_DB + File.separator + symbol + FrameworkConstants.EXTENSION_IBD);
            if (!file_path.exists()) {
//                file_path.delete();//overwrite
                new_file = true;
            }
            PrintWriter pw = new PrintWriter(new FileWriter(file_path, true));//open for append
            if (new_file)//new file, add header
                pw.println("Symbol,Date,State,Name,IBD 50 Rank,% Off High,Composite Rating,EPS Rating,RS Rating,SMR Rating," +
                        "Acc/Dis Rating,Group Rel Str Rating,Mgmt Own %,Qtrs of Rising Sponsorship");
            StringBuilder buf = new StringBuilder(ibd_info.getSymbol());
            buf.append(",")
                .append(AppUtil.calendarToString(ibd_info.getDate())).append(",")
                .append(ibd_info.getState()).append(",")
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
                .append(ibd_info.getSponsorship());
            pw.println(buf);
            pw.close();
        }
    }

    //----- accessor -----
    public String getSymbol() { return symbol; }
    public String getFullName() { return fullName; }
    public int getRank() { return rank; }
    public float getPctOffHigh() { return pctOffHigh; }
    public int getComposite() { return composite; }
    public int getEps() { return eps; }
    public int getRs() { return rs; }
    public String getSmr() { return smr; }
    public String getAccDis() { return accDis; }
    public String getGroupStrength() { return groupStrength; }
    public float getMgmtPercent() { return mgmtPercent; }
    public int getSponsorship() { return sponsorship; }
    public Calendar getDate() { return date; }
    public Ibd50State getState() { return state; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRank(int rank) { this.rank = rank; }
    public void setPctOffHigh(float pctOffHigh) { this.pctOffHigh = pctOffHigh; }
    public void setComposite(int composite) { this.composite = composite; }
    public void setEps(int eps) { this.eps = eps; }
    public void setRs(int rs) { this.rs = rs; }
    public void setSmr(String smr) { this.smr = smr; }
    public void setAccDis(String accDis) { this.accDis = accDis; }
    public void setGroupStrength(String groupStrength) { this.groupStrength = groupStrength; }
    public void setMgmtPercent(float mgmtPercent) { this.mgmtPercent = mgmtPercent; }
    public void setSponsorship(int sponsorship) { this.sponsorship = sponsorship; }
    public void setDate(Calendar cal) { date = cal; }
    public void setState(Ibd50State cur_state) { this.state = cur_state; }

    //----- variables -----
    //for ETFs, most will be 0
    private String symbol;
    private String fullName = "";
    private int rank;
    private float pctOffHigh;
    private int composite;
    private int eps;
    private int rs;
    private String smr = "";
    private String accDis = "";
    private String groupStrength = "";
    private float mgmtPercent;
    private int sponsorship;
    private Calendar date;//of this data
    private Ibd50State state = Ibd50State.Inactive;

    //----- literals -----
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
