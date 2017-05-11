package org.marketsuite.simulator.advanced.custom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.framework.strategy.analysis.AnnualReturnGraphPanel;
import org.marketsuite.framework.strategy.analysis.EquityGraphPanel;
import org.marketsuite.framework.strategy.analysis.PriceGraphPanel;
import org.marketsuite.framework.model.AnnualReturn;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Year;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.marketsuite.framework.model.*;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.analysis.EquityGraphPanel;
import org.marketsuite.framework.strategy.analysis.PriceGraphPanel;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;

/**
 * Helpers used under simulator package
 */
public class CustomUtil {
    /**
     * calculate CAGR (Compound Annual Growth Rate) from begin to end
     * @param begin_date begin date yyyy-mm-dd
     * @param end_date end date
     * @param begin_value begin quote
     * @param end_value end quote
     * @return CAGR in double format
     */
    public static double calcCAGR(String begin_date, String end_date, float begin_value, float end_value) {
        try {
            double exponent = 1 / AppUtil.calcYear(begin_date, end_date);
            double base = end_value / begin_value;
            return Math.pow( base, exponent ) - 1;
        } catch (ParseException e) {
            System.out.println("Begin or End date may be bad.......");
            e.printStackTrace();
            return Double.MIN_VALUE;
        }
    }

    /**
     * Export transactions that is simulated recently to a CSV file.  Set no_message to true to skip warning message.
     * Used by both single export and batch export.
     * @param trans_log transaction log
     * @param engine simulator engine for current panel
     * @param no_message true = don't warn user about over-writing existing file
     * @param path target folder under "Export"
     */
    public static void exportTransaction(ArrayList<Transaction> trans_log, AbstractEngine engine,
                                         boolean no_message, String path) {
        try {
            String symbol = trans_log.get(0).getSymbol();
            if (no_message) {
                exportFile(symbol, new File(FrameworkConstants.DATA_FOLDER_EXPORT + path +
                    symbol + FrameworkConstants.EXTENSION_TRADES), trans_log, engine);
                return;
            }

            //ask user for file name and rsp
            JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_EXPORT));
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int rsp = fc.showSaveDialog(null);//todo MainFrame.getInstance()
            if (rsp == JFileChooser.APPROVE_OPTION) {
                File output_path = fc.getSelectedFile();
                if (output_path.exists()) { //warn user if file exist
                    if (MessageBox.messageBox(
                            null, //todo MainFrame.getInstance(),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("exp_msg_1"),
                            MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                        return;
                }
                exportFile(symbol, output_path, trans_log, engine);
            }
        } catch (IOException e) {
            e.printStackTrace();//todo warning user about error
        }
    }

    /**
     * Write transactions with name like "<symbol></symbol>.csv" to specified output_path.
     * @param symbol simulated ticker symbol
     * @param output_path location of file
     * @param trans_log transactions after simulation
     * @param engine instance of engine underneath
     * @throws java.io.IOException cannot write file
     */
    public static void exportFile(String symbol, File output_path, ArrayList<Transaction> trans_log,
                                   AbstractEngine engine) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(output_path));
        pw.println("SYMBOL=" + symbol);
        pw.println("STRATEGY=" + engine.getId());
        pw.println("STRATEGY_INFO=" + engine.getStrategyInfo());
        pw.println("PRICE_KEY=CUSTOM");
        pw.println("TIME_FRAME=DAILY");
        for (Transaction trans : trans_log) {
            StringBuilder sb = new StringBuilder();
            sb.append(trans.isLongTrade() ? "LONG," : "SHORT,")
                    .append(trans.getEntryDate()).append(",").append(trans.getExitDate()).append(",")
                    .append(trans.getEntryPrice()).append(",").append(trans.getExitPrice());
            pw.println(sb.toString());
        }
        pw.flush();
        pw.close();
    }

    /**
     * plot equity curve, insert extra starting point using starting date in trans_log
     * @param panel container of equity graph
     * @param sim_report the SimReport object with after simulation data
     * @throws java.text.ParseException fail to convert data to calendar
     * @throws org.jfree.data.general.SeriesException duplicate values in TimeSeries
     */
    public static void plotEquityGraph(EquityGraphPanel panel, SimReport sim_report) throws ParseException, SeriesException {
        ArrayList<Transaction> trans_log = sim_report.getTransLog();
        ArrayList<Equity> equity_log = sim_report.genEquityLog();//TODO: for some reason getEquityLog() will mysteriously become larger, invent genEquityLog()
        TimeSeries data_series = new TimeSeries(trans_log.get(0).getSymbol());
        String start_date = trans_log.get(0).getEntryDate();
        float price = trans_log.get(0).getEntryPrice();
        float shares = FrameworkConstants.START_CAPITAL / price;
        equity_log.add(0, new Equity(start_date, shares, price));
        for (Equity equity : equity_log) {
            try {
                Calendar cal = AppUtil.stringToCalendar(equity.getDate());
                Day day = new Day(cal.getTime());
                data_series.add(day, equity.getEquity());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        panel.plotEquitySeries(data_series, buildSp500EquitySeries(equity_log, start_date));
    }

    /**
     * plot annual return graph
     * @param panel container of equity graph
     * @param sim_report the SimReport object with after simulation data
     */
    public static void plotAnnualReturnGraph(AnnualReturnGraphPanel panel, SimReport sim_report) {
        ArrayList<AnnualReturn> ars = sim_report.getAnnualReturns();
        String sym = "SP500";//for graph legend
        if (ars != null)
            sym = ars.get(0).getSymbol();
        TimeSeries ts = new TimeSeries(sym);
        TimeSeries sp = annualReturnToTimeSeries(FrameworkConstants.SP500_ANNUAL_RETURN);
        if (ars != null) {
            ts = annualReturnToTimeSeries(ars);

            //use partial SP500 data between ts first year and last year
            int start_yr = ars.get(0).getYear();
            int end_yr = ars.get(ars.size() - 1).getYear();
            ArrayList<AnnualReturn> sp_ars = new ArrayList<AnnualReturn>();
            for (AnnualReturn ar : FrameworkConstants.SP500_ANNUAL_RETURN) {
                if (ar.getYear() < start_yr || ar.getYear() > end_yr)
                    continue;
                sp_ars.add(ar);
            }
            sp = annualReturnToTimeSeries(sp_ars);
        }
        panel.updateGraph(ts, sp);
    }

    /**
     * plot annual return graph
     * @param panel container of equity graph
     * @param sim_report the SimReport object with after simulation data
     * @throws java.text.ParseException fail to convert data to calendar
     * @throws java.io.IOException fail to read from quote database
     */
    public static void plotPriceGraph(PriceGraphPanel panel, SimReport sim_report) throws IOException, ParseException {
        ArrayList<Transaction> trans_log = sim_report.getTransLog();
        String sym = trans_log.get(0).getSymbol();

        //read historical data to avoid empty quote
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, sym);
        if (fund.getSize() == 0) {
            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_005") + " " + sym + ".  " +
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_006") + " " +
                            FrameworkConstants.DATA_FOLDER_WEEKLY_QUOTE);
            return;
        }

        //find start date
        String start_date = trans_log.get(0).getEntryDate();
        String end_date = trans_log.get(trans_log.size() - 1).getExitDate();
        int start_index = fund.findIndexByDate(start_date);
        int end_index = fund.findIndexByDate(end_date);
        if (start_index <0 || end_index < 0)
            throw new ParseException("Start date or End date does NOT exist in " + sym + " Quote file.", 0);

        //prepare time series for price graph
        TimeSeries price_series = new TimeSeries(sym);
        for (int index = start_index; index >= end_index; index--) {
            try {
                Calendar cal = AppUtil.stringToCalendar(fund.getDate(index));
                Day day = new Day(cal.getTime());
                price_series.add(day, fund.getPrice(index));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        TimeSeries[] ps = new TimeSeries[1]; ps[0] = price_series;
        if (panel.isCandleChart()) {//candle stick chart
            int len = start_index + 1;
            Date[] dates =  new Date[len];
            double[] high = new double[len];
            double[] low = new double[len];
            double[] open = new double[len];
            double[] close = new double[len];
            double[] volume = new double[len];
            for (int idx = start_index; idx >= 0; idx--) {
                ArrayList<FundQuote> quote = fund.getQuote();
                dates[idx] = AppUtil.stringToCalendar(quote.get(idx).getDate()).getTime();
                high[idx] = quote.get(idx).getHigh();
                low[idx] = quote.get(idx).getLow();
                open[idx] = quote.get(idx).getOpen();
                close[idx] = quote.get(idx).getClose();
                volume[idx] = quote.get(idx).getVolume();
            }
            DefaultHighLowDataset ds = new DefaultHighLowDataset(sym, dates, high, low, open, close, volume);
            panel.drawCandleChart(ds);
        }
        else {//line chart
            panel.drawEntryExits(trans_log);
            panel.addSeries(ps);
        }
    }

    /**
     * Use equity log to build normalized SP500 equity time series for charting
     * @param log equity log from any system
     * @param start_date starting point's entry date, common starting point for fund and SP500
     * @return time series of matching SP500 data
     * @exception java.text.ParseException when quote data is not clean
     */
    public static TimeSeries buildSp500EquitySeries(ArrayList<Equity> log, String start_date) throws ParseException {
        if (log.size() <= 0)
            return null;

        TimeSeries ret = new TimeSeries("SP500");
        //find first data point difference, apply to all SP500 data (normalize)
        float first_sp_close = FrameworkConstants.SP500_DATA.findQuoteByDate(start_date).getClose();
        float shares = FrameworkConstants.START_CAPITAL / first_sp_close;
        //match each equity log date, calc equivalent SP500 equity
        for (Equity eqty : log) {
            //detect no data due to holidays
//            String date = AppUtil.dailyToWeekly(eqty.getDate());
//            Calendar cal = AppUtil.stringToCalendar(date);
//            if (!AppUtil.isDataAvailable(cal)) {//add one more day, mondy is probably holiday
//                cal.add(Calendar.DAY_OF_MONTH, 1);
//                date = AppUtil.calendarToString(cal);
//                cal = AppUtil.stringToCalendar(date);
//                if (!AppUtil.isDataAvailable(cal)) {//add one more day, mondy is probably holiday
//                    cal.add(Calendar.DAY_OF_MONTH, 1);
//                    date = AppUtil.calendarToString(cal);
//                    //todo: if date exceeds SP500 data range, exit
//                    if (!AppUtil.isDataAvailable(cal))
//                        return ret;
//                }
//            }
//if (eqty.getDate().equals("2012-01-20"))
//System.out.println();
            float sp_close = FrameworkConstants.SP500_DATA.findQuoteByDate(eqty.getDate()).getClose();
            float sp_eqty = shares * sp_close;
            Day day = new Day(AppUtil.stringToCalendar(eqty.getDate()).getTime());
            ret.add(day, sp_eqty);//normalize
        }
        return ret;
    }

    /**
     * Convert annual return array to plottable time series
     * @param ars array of AnnualReturn objects
     * @return corresponding TimeSeries objects.
     */
    public static TimeSeries annualReturnToTimeSeries(ArrayList<AnnualReturn> ars) {
        TimeSeries ts = new TimeSeries(ars.get(0).getSymbol());
        for (AnnualReturn ar : ars) {
            Year yr = new Year(ar.getYear());
            ts.add(yr, ar.getPerformance() * 100);
        }
        return ts;
    }

    /**
     * Convert transaction logs into annualized return array.  MUST have daily quotes database.
     * (1)for each year, collect all transactions that begins this year, ends this year or all this year(multi-year)
     * (2)for each collection within the year
     *    (a)begins this year, ends future year:
     *    (b)begins prev year, ends this year:
     *    (c)begins prev year, ends future year:
     * @param trans logs
     * @return list of AR objects
     * @exception java.io.IOException can not read file
     */
    public static ArrayList<AnnualReturn> calcAnnualReturn(ArrayList<Transaction> trans) throws IOException {
        if (trans.size() == 0)
            return null;//empty

        ArrayList<AnnualReturn> ret = new ArrayList<AnnualReturn>();
        String yr_str = trans.get(0).getExitDate().substring(0, 4);//YAHOO format
        int cur_year = Integer.parseInt(yr_str);
        String final_str = trans.get(trans.size() - 1).getExitDate().substring(0, 4);
        int final_yr = Integer.parseInt(final_str);
        String sym = trans.get(0).getSymbol();
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, sym);

        //for each year, collect a list of transaction performances, add up to that year's performance
        for (int yr = cur_year; yr <= final_yr; yr++) {
            float yr_ret = 0;
            for (Transaction tr : trans)
                yr_ret += calcTransactionAR(tr, yr, fund);
            ret.add(new AnnualReturn(sym, yr, yr_ret));
        }
        return ret;
    }

    /**
     * calculate transaction's annualized return for a given year
     * each transaction maybe in one of the 4 situations:
     * (1) start / end in this year
     * (2) start before this year, end this year
     * (3) start this year, end in future year
     * (4) start before this year, end in future year
     * (5) start / end before this year
     * (6) start / end in future year
     * @param tr transaction in question
     * @param year for calculating return
     * @param fund MUST not be null or empty contain quotes for the symbol
     * @return annualized percentage return for given year for (1) thru (4). = -1 for (5) and (6)
     */
    private static float calcTransactionAR(Transaction tr, int year, FundData fund) {//todo short needs testing, very confusing.....
        //find out quotes for first and last day of this year
        FundQuote q1 = AppUtil.findFirstQuoteInYear(fund, year);
        FundQuote q2 = AppUtil.findLastQuoteInYear(fund, year);
        if (q1 == null || q2 == null)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_012")
                    + fund.getSymbol() + ": " + year);
        float begin_quote = q1.getClose();
        float end_quote = q2.getClose();
        int begin_yr = AppUtil.extractYear(tr.getEntryDate());
        int end_yr = AppUtil.extractYear(tr.getExitDate());
        if (begin_yr == year && end_yr == year) { //(1)
            return tr.getPerformance();
        }
        else if (begin_yr < year && end_yr == year) { //(2)
            if (tr.isLongTrade())
                return (tr.getExitPrice() - begin_quote) / begin_quote;
            else
                return (begin_quote - tr.getExitPrice()) / begin_quote;
        }
        else if (begin_yr == year && end_yr > year) { //(3)
            if (tr.isLongTrade())
                return (end_quote - tr.getEntryPrice()) / tr.getEntryPrice();
            else
                return (tr.getEntryPrice() - end_quote) / end_quote;
        }
        else if (begin_yr < year && end_yr > year) { //(4)
            if (tr.isLongTrade())
                return (end_quote - begin_quote) / begin_quote;
            else
                return (begin_quote - end_quote) / end_quote;
        }
        else {//outside transaction (5) and (6)
        }
        return 0;
    }
}
