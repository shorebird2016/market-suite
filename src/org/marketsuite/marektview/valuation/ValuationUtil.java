package org.marketsuite.marektview.valuation;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.marektview.history.DividendRecord;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.marektview.history.DividendRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;

public class ValuationUtil {
    public static ArrayList<DividendRecord> readDividends(String symbol) throws Exception {
        //load dividend from file into DividendRecord
        ArrayList<DividendRecord> ret = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER_DIVIDEND
                + File.separator + symbol + FrameworkConstants.EXTENSION_DIVIDEND));
        String line;
        int line_num = 1;
        while ((line = br.readLine()) != null) {
            if (line_num == 1) {
                line_num++;
                continue;
            }
            String[] tokens = line.split(",");
            //first part date, 2nd part div
            String date = tokens[0];
            Calendar cal = AppUtil.stringToCalendarNoEx(date);
            if (cal.get(Calendar.YEAR) > 2014) continue;//skip 2015 for now TODO Use Variables from field
            String div_str = tokens[1];
            float div = Float.parseFloat(div_str);
            DividendRecord dr = new DividendRecord(symbol, cal, div);
            ret.add(dr);
        }
        return ret;
    }
    //combine quarterly dividends into annual dividend records, can be empty
    public static ArrayList<DividendRecord> toAnnualDividends(ArrayList<DividendRecord> quarterly_drs) {
        ArrayList<DividendRecord> ret = new ArrayList<>();
        int prev_yr = -1;
        float total_div = 0;
        Calendar prev_cal = null;
        for (DividendRecord dr : quarterly_drs) {
            int cur_yr = dr.getCal().get(Calendar.YEAR);
            if (prev_yr != cur_yr) {
                if (prev_yr == -1) {//first record
                    prev_yr = cur_yr;
                    prev_cal = dr.getCal();
                    total_div += dr.getDividend();
                } else {//save old record, start new one
                    ret.add(new DividendRecord(dr.getSymbol(), prev_cal, total_div));
                    prev_yr = cur_yr;
                    prev_cal = dr.getCal();
                    total_div = dr.getDividend();//first record
                }
            } else {//same year, add dividend
                total_div += dr.getDividend();
            }
        }
        return ret;
    }

}
