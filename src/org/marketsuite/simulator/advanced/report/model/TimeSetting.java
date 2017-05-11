package org.marketsuite.simulator.advanced.report.model;

import org.jdom.Element;

/**
 * Time related parameters for simulation.
 */
public class TimeSetting {
    public TimeSetting() {} //need this to make preference to work

    /**
     * CTOR
     * @param type one of TIME_FULL_RANGE, TIME_PARTIAL_RANGE, TIME_SKIP_INITIAL
     * @param begin_date = -1 for full range, typically 4 digit number
     * @param end_date = -1 for full range
     * @param skipMonth number of months to skip from beeginning
     */
    public TimeSetting(String type, String begin_date, String end_date, int skipMonth) {
        this.type = type;
        this.beginDate = begin_date;
        this.endDate = end_date;
        this.skipMonth = skipMonth;
    }

    //CTOR: construct object from xml
    public TimeSetting(Element element) {
        type = element.getAttributeValue(TIME_SETTING);
        if (type.equals(TIME_PARTIAL_RANGE)) {
            beginDate = element.getAttributeValue(BEGIN_DATE);
            endDate = element.getAttributeValue(END_DATE);
        }
        else if (type.equals(TIME_SKIP_INITIAL))
            skipMonth = Integer.parseInt(element.getAttributeValue(SKIP_MONTH));
    }

    //-----public methods-----
    public Element objToXml() {
        Element ret = new Element(TIME_SETTING);
        ret.setAttribute(TYPE, type);
        if (type.equals(TIME_PARTIAL_RANGE)) {
            ret.setAttribute(BEGIN_DATE, String.valueOf(beginDate));
            ret.setAttribute(END_DATE, String.valueOf(endDate));
        }
        else if (type.equals(TIME_SKIP_INITIAL)) 
            ret.setAttribute(SKIP_MONTH, String.valueOf(skipMonth));
        return ret;
    }

    //-----instance variables / accessors-----
    private String type; //could be any one of the 3 choices TIME_FULL_RANGE, TIME_PARTIAL_RANGE, TIME_SKIP_INITIAL
    public String getType() {
        return type;
    }
    public void setType(String t) { type = t; } //need this to make preference to work

//    private int startYear;  //for TIME_PARTIAL_RANGE
//    public int getStartYear() {
//        return startYear;
//    } //need this to make preference to work
//    public void setStartYear(int sy) { startYear = sy; }
    
    private String beginDate;  //for TIME_PARTIAL_RANGE
    public String getBeginDate() {
        return beginDate;
    }
    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    private String endDate;  //for TIME_PARTIAL_RANGE
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }//need this to make preference to work

//    private int endYear;
//    public int getEndYear() {
//        return endYear;
//    }
//    public void setEndYear(int ey) { endYear = ey; }

    private int skipMonth; //for TIME_SKIP_INITIAL
    public int getSkipMonth() {
        return skipMonth;
    } //need this to make preference to work
    public void setSkipMonth(int sm) { skipMonth = sm; }

    //-----literals-----
    public static final String TIME_FULL_RANGE = "Full Range";
    public static final String TIME_PARTIAL_RANGE = "Partial Range";
    public static final String TIME_SKIP_INITIAL = "Skip Initial";
    
    //xml related
    public static final String TIME_SETTING = "time-setting";
    private static final String TYPE = "type";
    private static final String BEGIN_DATE = "begin-date";
    private static final String END_DATE = "end-date";
    private static final String SKIP_MONTH = "skip-month";

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String FULL_RANGE = "full-range";
    private static final String PARTIAL_RANGE = "partial-range";
    private static final String SKIP_START = "skip-start";
}
