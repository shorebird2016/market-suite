package org.marketsuite.framework.model.type;

import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.util.AppUtil;

import java.util.ArrayList;
import java.util.Calendar;

//imply time based on calendar (instead of number of days)
//  so use Calendar to compute start/end indicies in quote array
public enum PerfTimeframe {
    ONE_WEEK("1 Week"),
    TWO_WEEK("2 Week"),
    THREE_WEEK("3 Week"),
    ONE_MONTH("1 Month"),
    TWO_MONTH("2 Month"),
    THREE_MONTH("3 Month"),
    SIX_MONTH("6 Month"),
    NINE_MONTH("9 Month"),
    ONE_YEAR("1 Year"),
    TWO_YEAR("2 Year"),
    THREE_YEAR("3 Year"),
    FIVE_YEAR("5 Year"),
    YEAR_TO_DATE("YTD"),
    ;
    PerfTimeframe(String view_str) { viewString = view_str; }
    public String toString() { return viewString; }

    //given quotes and end index, calculate start index for a given time frame, -1 = can't compute
    public static int calcStartIndex(ArrayList<FundQuote> quotes, int end_index, PerfTimeframe tf) {
        String end_date = quotes.get(end_index).getDate();
        Calendar end_cal = AppUtil.stringToCalendarNoEx(end_date);
        Calendar start_cal = Calendar.getInstance(); start_cal.setTime(end_cal.getTime());
        switch (tf) {
            case ONE_WEEK: start_cal.add(Calendar.DAY_OF_YEAR, -7); break;
            case TWO_WEEK: start_cal.add(Calendar.DAY_OF_YEAR, -14); break;
            case THREE_WEEK: start_cal.add(Calendar.DAY_OF_YEAR, -21); break;
            case ONE_MONTH: start_cal.add(Calendar.MONTH, -1); break;
            case TWO_MONTH: start_cal.add(Calendar.MONTH, -2); break;
            case THREE_MONTH: start_cal.add(Calendar.MONTH, -3); break;
            case SIX_MONTH: start_cal.add(Calendar.MONTH, -6); break;
            case NINE_MONTH: start_cal.add(Calendar.MONTH, -9); break;
            case ONE_YEAR: start_cal.add(Calendar.YEAR, -1); break;
            case TWO_YEAR: start_cal.add(Calendar.YEAR, -2); break;
            case THREE_YEAR: start_cal.add(Calendar.YEAR, -3); break;
            case FIVE_YEAR: start_cal.add(Calendar.YEAR, -5); break;
            case YEAR_TO_DATE:
                FundQuote yq = AppUtil.findFirstQuoteInYear(quotes, AppUtil.extractYear(end_date));
                start_cal.setTime(AppUtil.stringToCalendarNoEx(yq.getDate()).getTime()); break;
        }
        FundQuote start_quote = AppUtil.findNearestQuote(quotes, start_cal);
        return start_quote.findIndexByDate(quotes, start_quote.getDate());
    }

    //----- variables -----
    private String viewString;
}
