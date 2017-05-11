package org.marketsuite.framework.strategy.base;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.framework.model.AnnualReturn;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.Stat;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.framework.model.*;
import org.marketsuite.framework.model.PeakValley;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;

import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * This object reports / stores all measurements after running simulation plus simulation parameters.
 */
public class SimReport implements Comparable {
    public SimReport() { }
    /**
     * CTOR: create a report for specified transaction log.
     * @param trans list of Transaction objects with buy sell dates
     * @param start_date user selected start date
     * @param finish_date user selected end date
     */
    public SimReport(ArrayList<Transaction> trans, String start_date, String finish_date, final ProgressBar prog_bar) {
        progBar = prog_bar;
        transLog = trans;
        createEquityLog();
        calcGainLossStat();
        calcDrawDown();

        //calc profit factor
        if (totalLoss > 0)
            profitFactor = totalGain / totalLoss;

        //calc win ratio
        //time in market - add up all days, divided by total number of days
        float sum_days = 0;
        winTrans = 0;
        try {
            for (Transaction tran : transLog) {
                if (tran.getPerformance() > 0)
                    winTrans++;

                float days = AppUtil.calcTranscationDuration(tran);
                sum_days += days;
            }
            numberTrades = transLog.size();
            winRatio = (float)winTrans / numberTrades;

            //get last date in quote - begin date
            float total_market_days = AppUtil.calcDuration(start_date, finish_date);
            timeInMarket = sum_days / total_market_days;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //average transaction per year
        String begin_date = transLog.get(0).getEntryDate();
        String end_date = transLog.get(transLog.size() - 1).getExitDate();
        try {
            avgTrans = numberTrades / (float)AppUtil.calcYear(begin_date, end_date);
            double num_yr = AppUtil.calcYear(begin_date, end_date);
            tradesPerYear = (float)(numberTrades / num_yr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //visual feedback
        if (prog_bar != null)
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    progBar.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_rpt_prg"));
                }
            });

        //calc CAGR
        if (equityLog.size() > 0) {
            cagr = (float) SimUtil.calcCAGR(begin_date, end_date, FrameworkConstants.START_CAPITAL,
                equityLog.get(equityLog.size() - 1).getEquity());

            //total return end equity - begin equity
            float end_equity = equityLog.get(equityLog.size() - 1).getEquity();
            totalReturn = (end_equity - FrameworkConstants.START_CAPITAL) / FrameworkConstants.START_CAPITAL;
        }

        //average return
        try {
            double yr = AppUtil.calcYear(trans.get(0).getEntryDate(), trans.get(transLog.size() - 1).getExitDate());
            avgReturn = (float)(totalReturn / yr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //calc annualized return
        try {
            annualReturns = SimUtil.calcAnnualReturn(transLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public SimReport(ArrayList<Transaction> trans, boolean one_symbol) throws ParseException, IOException {
        transLog = trans;
        oneSymbol = one_symbol;
        createEquityLog();
        calcGainLossStat();
        calcDrawDown();

        //calc profit factor
        if (totalLoss > 0)
            profitFactor = totalGain / totalLoss;

        //calc win ratio
        //time in market - add up all days, divided by total number of days
        float sum_days = 0;
        winTrans = 0;
        for (Transaction tran : transLog) {
            if (tran.getPerformance() > 0)
                winTrans++;

            float days = AppUtil.calcTranscationDuration(tran) + 1;
            sum_days += days;
        }
        numberTrades = transLog.size();
        winRatio = (float) winTrans / numberTrades;
        float total_market_days = AppUtil.calcDuration(trans.get(0).getEntryDate(), trans.get(trans.size() - 1).getExitDate()) + 1;
        timeInMarket = sum_days / total_market_days;

        //average transaction per year
        String begin_date = transLog.get(0).getEntryDate();
        String end_date = transLog.get(transLog.size() - 1).getExitDate();
        double num_yr = AppUtil.calcYear(begin_date, end_date);
        tradesPerYear = (float)(numberTrades / num_yr);

        //calc CAGR
        if (equityLog.size() > 0) {
            cagr = (float) IndicatorUtil.calcCAGR(begin_date, end_date, FrameworkConstants.START_CAPITAL,
                    equityLog.get(equityLog.size() - 1).getEquity());

            //total return end equity - begin equity
            float end_equity = equityLog.get(equityLog.size() - 1).getEquity();
            totalReturn = (end_equity - FrameworkConstants.START_CAPITAL) / FrameworkConstants.START_CAPITAL;

            //average return
            avgReturn = (float) (totalReturn / num_yr);
        }

        //calc annualized return
        if (one_symbol)
            annualReturns = IndicatorUtil.calcAnnualReturn(transLog);
    }

    //use CAGR as comparison field
    public int compareTo(Object o) {
        SimReport rpt = (SimReport)o;
        if (cagr > rpt.getCagr())
            return 1;
        else if (cagr < rpt.getCagr())
            return -1;
        return 0;
    }

    //ending equity
    public float getEndEquity() {
        return equityLog.size() > 0 ?
            equityLog.get(equityLog.size() - 1).getEquity() : 0;
    }

    /**
     * After simulation, report equity log, compute total gain and losses
     */
    private void createEquityLog() {
        totalGain = 0;
        totalLoss = 0;
        float capital = FrameworkConstants.START_CAPITAL;
        equityLog = new ArrayList<>();
        float last_equity = FrameworkConstants.START_CAPITAL;
        for (final Transaction tr : transLog) {
            //ignore same day buy and sell, not possible only occurs when buys on the most recent friday
            if (tr.getEntryDate().equals(tr.getExitDate()))
                continue;//skip this

            capital += capital * tr.getPerformance();
            Equity equity = new Equity(tr.getExitDate(), capital/tr.getExitPrice(), tr.getExitPrice());//equity calc'd
            float gain_loss = capital - last_equity;
            last_equity = capital;
            if (gain_loss > 0)
                totalGain += gain_loss;
            else
                totalLoss += gain_loss;
            equityLog.add(equity);

            //visual feedback
            if (progBar == null) continue;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    progBar.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("osc_rpt_log")
                        + " " + tr.getEntryDate());
                }
            });
        }
        totalLoss = -totalLoss;//remove negative sign
    }

    //calcFirstRSI average in percentage, inputs: transaction/equity transLog, outputs: supplied stat objects
    private void calcGainLossStat() {
        //percentages
        //split transactions performance attributes into two arrays, one for gain, one for loss
        ArrayList<Float> gain_pct = new ArrayList<Float>();
        ArrayList<Float> loss_pct = new ArrayList<Float>();
        for (Transaction tr : transLog) {
            if (tr.getPerformance() > 0)
                gain_pct.add(tr.getPerformance());
            else
                loss_pct.add(tr.getPerformance());
        }
        gain.setAvgPct(AppUtil.average(gain_pct));
        gain.setMaxPct(AppUtil.max(gain_pct));
        gain.setMinPct(AppUtil.min(gain_pct));
        gain.setMedianPct(AppUtil.median(gain_pct));
        loss.setAvgPct(AppUtil.average(loss_pct));
        loss.setMaxPct(AppUtil.min(loss_pct));//negative use smallest number as max
        loss.setMinPct(AppUtil.max(loss_pct));
        loss.setMedianPct(AppUtil.median(loss_pct));

        //dollar amounts
        //use both transactions and equity logs to split into two arrays, one for gain and one for loss
        ArrayList<Float> gain_amt = new ArrayList<Float>();
        ArrayList<Float> loss_amt = new ArrayList<Float>();
        float last_equity = FrameworkConstants.START_CAPITAL;
        for (int index = 0; index < equityLog.size(); index++) {
            float eqty = equityLog.get(index).getEquity();
            float gl = eqty - last_equity;
            if (gl > 0)
                gain_amt.add(gl);
            else
                loss_amt.add(gl);
            last_equity = eqty;
        }
        gain.setAvgAmount(AppUtil.average(gain_amt));
        gain.setMaxAmount(AppUtil.max(gain_amt));
        gain.setMinAmount(AppUtil.min(gain_amt));
        gain.setMedianAmount(AppUtil.median(gain_amt));

        loss.setAvgAmount(AppUtil.average(loss_amt));
        loss.setMaxAmount(AppUtil.min(loss_amt));//negative use smallest number as max
        loss.setMinAmount(AppUtil.max(loss_amt));
        loss.setMedianAmount(AppUtil.median(loss_amt));
    }

    //calculate draw down stats of a data series; draw down = each peak to valley decline on the curve
    //  algorithm: walk the series to find pairs of peak and valley, use their difference as draw down
    //  input: equity log, output: Stat object contains avg, min, max, median about draw down
    private void calcDrawDown() {
        //step 1: find pairs of peak and valley
        float cur_peak = FrameworkConstants.START_CAPITAL;
        float cur_valley = Float.MAX_VALUE;
        ArrayList<org.marketsuite.framework.model.PeakValley> pvs = new ArrayList<PeakValley>();
        //traverse each element of equity log, compare with current peak and valley
        for (Equity eqty : equityLog) {
            float cur_equity = eqty.getEquity();
            if (cur_equity > cur_peak) {
                if (cur_valley == Float.MAX_VALUE)//no valley yet, still rising
                    cur_peak = cur_equity;
                else {//already has valley, new peak, save pair away
                    PeakValley pv_pair = new PeakValley(cur_peak, cur_valley);
                    pvs.add(pv_pair);
                    cur_peak = cur_equity;
                    cur_valley = Float.MAX_VALUE;//start over, no valley yet, peak from last cycle
                }
            }
            else {//value is less than current peak
                if (cur_valley == Float.MAX_VALUE)
                    cur_valley = cur_equity;
                else if (cur_equity < cur_valley)//update current valley, still declining, no bottom yet
                    cur_valley = cur_equity;
            }
        }

        //handle last point: if no valley found, skip, otherwise save the open peak/valley pair
        if (cur_valley != Float.MAX_VALUE) {
            PeakValley pv_pair = new PeakValley(cur_peak, cur_valley);
            pvs.add(pv_pair);
        }

        //step 2: calc differences
        ArrayList<Float> dd_amt = new ArrayList<Float>();//amount
        ArrayList<Float> dd_pct = new ArrayList<Float>();//percent
        for (PeakValley pv : pvs) {
            float p2v = pv.getPeak() - pv.getValley();
            dd_amt.add(p2v);//delta
            dd_pct.add(p2v / pv.getPeak());//percent down from peak
        }
        if (pvs.size() == 0) {//only peak no valley, use last point at valley
            int es = equityLog.size();
            if (es >= 1) {
                float p2v = cur_peak - equityLog.get(es - 1).getEquity();//last equity
                if (cur_valley != Float.MAX_VALUE)
                    p2v = cur_peak - cur_valley;
                dd_amt.add(p2v);
                dd_pct.add(p2v / cur_peak);
            }
        }

        //step 3: calc stats
        drawDown = new Stat(-AppUtil.average(dd_amt), -AppUtil.min(dd_amt), -AppUtil.max(dd_amt), -AppUtil.median(dd_amt),
            -AppUtil.average(dd_pct), -AppUtil.min(dd_pct), -AppUtil.max(dd_pct), -AppUtil.median(dd_pct));
    }

    //-----instance variables and assessors-----
    private boolean _bValleySet, _bPeakSet;
    private ProgressBar progBar;

    //statistics of all gains
    private Stat gain = new Stat();
    public Stat getGain() {
        return gain;
    }

    //statistics of all losses
    private Stat loss = new Stat();
    public Stat getLoss() {
        return loss;
    }

    //statistics of draw-downs
    private Stat drawDown = new Stat();
    public void setDrawDown(Stat draw_down) {
        drawDown = draw_down;
    }
    public Stat getDrawDown() {
        return drawDown;
    }

    //total gain in dollars
    private float totalGain;
    public float getTotalGain() { return totalGain; }
    public void setTotalGain(float gain) { this.totalGain = gain; }

    //total loss
    private float totalLoss;
    public float getTotalLoss() {
        return totalLoss;
    }

    //final CAGR
    private float cagr;
    public float getCagr() {
        return cagr;
    }
    public void setCagr(float _cagr) { cagr = _cagr; }

    //winning counts divided by losing counts
    private float winRatio;
    public float getWinRatio() {
        return winRatio;
    }

    public float getAvgTrans() { return avgTrans; }
    private float avgTrans;//average number of transactions per year

    public int getWinTrans() { return winTrans; }
    private int winTrans;//winning transactions

    private int numberTrades;
    public int getNumberTrades() { return numberTrades; }

    //time in market
    private float timeInMarket;
    public float getTimeInMarket() {
        return timeInMarket;
    }

    //profits divided by losses
    private float profitFactor;
    public float getProfitFactor() {
        return profitFactor;
    }

    //transactions for a simulation run
    private ArrayList<Transaction> transLog;
    public ArrayList<Transaction> getTransLog() {
        return transLog;
    }

    //equity transLog for a run
    private ArrayList<Equity> equityLog;
    public void setEquityLog(ArrayList<Equity> log) throws ParseException {
        equityLog = log;
        //total return end equity - begin equity
        float end_equity = equityLog.get(equityLog.size() - 1).getEquity();
        totalReturn = (end_equity - FrameworkConstants.START_CAPITAL) / FrameworkConstants.START_CAPITAL;
        //average return
        double yr = AppUtil.calcYear(equityLog.get(0).getDate(), equityLog.get(equityLog.size() - 1).getDate());
        avgReturn = (float)(totalReturn / yr);
    }
    public ArrayList<Equity> getEquityLog() {
        return equityLog;
    }
    public ArrayList<Equity> genEquityLog() {
        createEquityLog();
        return equityLog;
    }

    private float totalReturn;
    public float getTotalReturn() { return totalReturn; }
    public void setTotalReturn(float ret) throws ParseException {
        totalReturn = ret;
        //average return
        double yr = AppUtil.calcYear(equityLog.get(0).getDate(), equityLog.get(equityLog.size() - 1).getDate());
        avgReturn = (float)(totalReturn / yr);
    }
    private float avgReturn;
    public float getAverageReturn() { return avgReturn; }

    //annualized return records
    private ArrayList<AnnualReturn> annualReturns;
    public ArrayList<AnnualReturn> getAnnualReturns() {
        return annualReturns;
    }

    private float tradesPerYear;
    public float getTradesPerYear() {
        return tradesPerYear;
    }

    //parameters used in simulation
//    private SimParam simParam = new SimParam();
//    public SimParam getSimParam() {
//        return simParam;
//    }
//    public void setSimParam(SimParam simParam) {
//        this.simParam = simParam;
//    }
    //skip annualized return calculation (eg. MDB)
    private boolean oneSymbol;
    public void setOneSymbol(boolean one_sym) {
        oneSymbol = one_sym;
    }

    //for debug logging
    public void printReport() {
//todo        if (!Main.logging)
//            return;

        StringBuilder buf = new StringBuilder();
        buf.append("CAGR = ").append(FrameworkConstants.ROI_FORMAT.format(cagr)).append(
                        "\nPF = " + FrameworkConstants.DOLLAR_FORMAT.format(profitFactor)
                        + "\nWin Ratio = " + FrameworkConstants.ROI_FORMAT.format(winRatio)
                        + "\nTotal Trades = " + numberTrades
                        + "\nWinning Trades = " + winTrans
                        + "\nAverage Trades/Year = " + FrameworkConstants.PRICE_FORMAT.format(tradesPerYear)

                        + "\nTotal Gain = " + FrameworkConstants.DOLLAR_FORMAT.format(totalGain)
                        + "\nTotal Loss = " + FrameworkConstants.DOLLAR_FORMAT.format(totalLoss)

                        + "\n[Gain] Max = " + FrameworkConstants.DOLLAR_FORMAT.format(gain.getMaxAmount())
                        + "  Min = " + FrameworkConstants.DOLLAR_FORMAT.format(gain.getMinAmount())
                        + "\n\tAverage = " + FrameworkConstants.DOLLAR_FORMAT.format(gain.getAvgAmount())
                        + "  Max Percent = " + FrameworkConstants.ROI_FORMAT.format(gain.getMaxPct())

                        + "\n[Loss] Max = " + FrameworkConstants.DOLLAR_FORMAT.format(loss.getMaxAmount())
                        + "  Min = " + FrameworkConstants.DOLLAR_FORMAT.format(loss.getMinAmount())
                        + "  Average = " + FrameworkConstants.DOLLAR_FORMAT.format(loss.getAvgAmount())
                        + "\n\tMax Percent = " + FrameworkConstants.ROI_FORMAT.format(loss.getMaxPct())
                        + "  Average Percent = " + FrameworkConstants.ROI_FORMAT.format(loss.getAvgPct())

                        + "\n[Draw Down] Max = " + FrameworkConstants.DOLLAR_FORMAT.format(drawDown.getMaxAmount())
                        + "  Min = " + FrameworkConstants.DOLLAR_FORMAT.format(drawDown.getMinAmount())
                        + "  Average = " + FrameworkConstants.DOLLAR_FORMAT.format(drawDown.getAvgAmount())
                        + "\n\tMax Percent = " + FrameworkConstants.ROI_FORMAT.format(drawDown.getMaxPct())
                        + "  Average Percent = " + FrameworkConstants.ROI_FORMAT.format(drawDown.getAvgPct()) + "\n");
        buf.append("---------- Transactions ------------\n");
        for (Transaction tr : transLog) {
            String roi = FrameworkConstants.ROI_FORMAT.format(tr.getPerformance());
            buf.append(tr.getSymbol() + "  " + tr.getEntryDate()
                    + " (" + FrameworkConstants.PRICE_FORMAT.format(tr.getEntryPrice()) + ")\t" + tr.getExitDate()
                    + " (" + FrameworkConstants.PRICE_FORMAT.format(tr.getExitPrice()) + ")\t" + roi + "\n");
        }
        buf.append("---------- Equity Log ------------\n");
        float last_eq = FrameworkConstants.START_CAPITAL;
        for (Equity eq : equityLog) {
            float gl = eq.getEquity() - last_eq;//gain/loss of each transaction
            buf.append(eq.getDate() + "\t" + FrameworkConstants.DOLLAR_FORMAT.format(eq.getEquity())
                + "\t" + FrameworkConstants.DOLLAR_FORMAT.format(gl) + "\n");
            last_eq = eq.getEquity();
        }

        System.out.println(buf);
    }
}
