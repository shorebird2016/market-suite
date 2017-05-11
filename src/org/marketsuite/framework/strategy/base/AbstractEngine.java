package org.marketsuite.framework.strategy.base;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.framework.model.ActiveTrade;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;

import java.util.ArrayList;

/**
 * Common prototype of any system simulators.  All engine must sub-class from here.
 */
public abstract class AbstractEngine {
    /**
     * Run simulation between two dates.
     * @param start_date starting date inclusive with YAHOO format eg. 03-14-1984
     * @param end_date ending date inclusive with YAHOO format, null = most recent monday
     */
    public abstract void simulate(String start_date, String end_date);

    /**
     * Run simulation using underline alogorithm
     * @return true = success
     */
    public abstract boolean simulate();
    public abstract boolean isBuySetup();
    public abstract boolean isBuyTrigger();
    public abstract String getBuyTriggerDate();
    public abstract boolean isSellSetup();
    public abstract boolean isSellTrigger();
    public abstract String getSellTriggerDate();

    public Transaction execute() {
        Transaction ret = new Transaction();
        ActiveTrade trade = new ActiveTrade();
        boolean setup_occurred = false;
        if (!trade.isOpen()) {//looking to buy
            if (setup_occurred) {//check buy trigger condition
                if (isBuyTrigger()) {
                    trade.buy(_Fund, getBuyTriggerDate(), 0.05F);
                    setup_occurred = false;
                }
            }
            else {//check buy setup condition
                if (isBuySetup())
                    setup_occurred = true;
            }
        }
        else {//looking to sell
            if (setup_occurred) {//check sell trigger condition
                if (isSellTrigger()) {
                    trade.sell(getSellTriggerDate());
                    setup_occurred = false;
                    ret = new Transaction(trade);
                }
            }
            else {//check sell setup condition
                if (isSellSetup())
                    setup_occurred = true;
            }
        }
        return ret;
    }

    /**
     * Is position at this date "long" or "out"
     * @param cur_date date in YAHOO format
     * @return true = open long position
     */
//    public abstract boolean isLong(String cur_date);
    /**
     * Retrieve/Set simulation parameters
     * @return a SimParam object
     */
    public SimParam getSimParam() {
        return simParam;
    }
    public void setSimParam(SimParam param) { simParam = param; }
    protected SimParam simParam;

    //MUST return alphanumeric string w/o space
    public abstract String getId();
    public abstract String getStrategy();
    public abstract String getStrategyInfo();

    protected void executeTrades(int start_index, int end_index) {
        _Transactions = new ArrayList<>();
        ActiveTrade trade = new ActiveTrade();
        int loop_index = start_index;

        //determine trade options
        String type = LONG_ONLY;
        boolean long_option = simParam.getStdOptions().isLongTrade();
        boolean short_option = simParam.getStdOptions().isShortTrade();
        if (long_option && short_option) type = LONG_SHORT;
        else if (short_option) type = SHORT_ONLY;

        //loop from end of quotes to beginning (YAHOO format)
        while (loop_index >= end_index) {
            String trade_date = _Fund.getDate(loop_index);
            float trade_price = _Fund.getAdjustedClose(loop_index);

            //trade is NOT OPEN, look for ENTRY
            if (!trade.isOpen()) {//look to buy
                if (_EntryCrossing[loop_index] == CROSSING_ABOVE) {
                    if (type.equals(LONG_ONLY) || type.equals(LONG_SHORT)) {
                        trade.buy(_Fund, trade_date, 0.05f);
                        logTrade("Buy", trade_date, trade_price);
                    }
                }
                if (_ExitCrossing[loop_index] == CROSSING_BELOW) {//short rule
                    if (type.equals(SHORT_ONLY) || type.equals(LONG_SHORT)) {
                        trade.sellShort(_Fund, trade_date, 0f, false);
                        logTrade("Short", trade_date, trade_price);
                    }
                }
            }

            //trade is OPEN, look for EXIT
            else {
                //split decision making based on current trade type
                //long trade: look for EXIT (120MA cross below 200MA)
                if (trade.isLongType()) {
                    if (type.equals(LONG_ONLY)) {
                        if (_ExitCrossing[loop_index] == CROSSING_BELOW) {
                            trade.sell(trade_date);
                            _Transactions.add(new Transaction(trade));
                            logTrade("Sell", trade_date, trade_price);
                        }
                        //premature exit 50MA cross below 120MA, bear market todo possibly a short side too
//                        else if (_EntryCrossing[loop_index] == CROSSING_BELOW
//                                && _EntryMA1[loop_index] < _ExitMA2[loop_index]) {
//                            trade.sell(trade_date);
//                            _Transactions.add(new Transaction(trade));
//                            logTrade("Sell", trade_date, trade_price);
//                        }
                    }
                    else if (type.equals(SHORT_ONLY)) {} //not possible to be here
                    else {//both long and short
                        if (_ExitCrossing[loop_index] == CROSSING_BELOW) {
                            trade.sell(trade_date);
                            _Transactions.add(new Transaction(trade));
                            logTrade("Sell", trade_date, trade_price);
                            trade.sellShort(_Fund, trade_date, 0f, false);
                            logTrade("Short", trade_date, trade_price);
                        }
                        //premature exit 50MA cross below 120MA, bear market todo possibly a short side too
//                        else if (_EntryCrossing[loop_index] == CROSSING_BELOW
//                                && _EntryMA1[loop_index] < _ExitMA2[loop_index]) {
//                            trade.sell(trade_date);
//                            _Transactions.add(new Transaction(trade));
//                            logTrade("Sell", trade_date, trade_price);
//                        }
                    }

                }

                //short trade: look for EXIT (50MA cross above 120MA)
                else {
                    if (type.equals(LONG_ONLY)) {} //not possible to be here
                    else if (type.equals(SHORT_ONLY)) {
                        if (_EntryCrossing[loop_index] == CROSSING_ABOVE) {
                            trade.coverShort(trade_date, trade_price);
                            _Transactions.add(new Transaction(trade));
                            logTrade("Cover", trade_date, trade_price);
                        }
                    }
                    else {//both long and short
                        if (_EntryCrossing[loop_index] == CROSSING_ABOVE) {
                            trade.coverShort(trade_date, trade_price);
                            _Transactions.add(new Transaction(trade));
                            logTrade("Cover", trade_date, trade_price);
                            trade.buy(_Fund, trade_date, 0.05f);
                            logTrade("Buy", trade_date, trade_price);
                        }
                    }
                }
            }
            loop_index--;
        }

        //pick up last open trade if any, make up a pseudo transaction for up to date
        String final_date = _Fund.getDate(end_index);
        if (trade.isOpen()) {
            //close this trade with most recent price at index 0
            trade.sell(final_date);
            _Transactions.add(new Transaction(trade));

            //if trade's start date is the most recent fund date, then set entry signal
            entrySignal = trade.getEntryDate().equals(final_date);
        }
        else {//if trade's end date is the most recent fund date, then set exit signal
            String ed = trade.getExitDate();
            if (ed != null)
                exitSignal = ed.equals(final_date);
        }
    }

    /**
     * Common implementation of standard set of reports; max, min, dd stats, win ratio, PF, CAGR..etc
     * @return a SimReport object
     */
    public SimReport genReport() {
        return new SimReport(_Transactions, simParam.getStdOptions().getStartDate(),
            simParam.getStdOptions().getEndDate(), progBar);
    }

    //----- protected / private methods -----
    //calculate cross over between two data series (eg. short vs medium), two series must have the same length
    //  from specified starting point
    // return values: CROSSING_ABOVE for ma1 cross above ma2; CROSSING_BELOW for ma1 cross below ma2
    protected int[] calcCrossing(float[] ma1, float[] ma2, int start_index, int end_index) {
        if (ma1.length != ma2.length || ma1.length <= start_index || ma2.length <= start_index)
            throw new IllegalArgumentException(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_err_1"));
        int[] ret = new int[ma1.length];
        int loop_index = start_index + 1;

        //skip comparison for 0's due to max bars back
        boolean all_zeros = true;
        while (loop_index >= end_index) {
            if (ma1[loop_index] == 0 || ma2[loop_index] == 0) {
                loop_index--;//skipping 0s
                continue;
            }
            else {
                all_zeros = false;
                break;
            }
        }
        if (all_zeros)
            return ret;

        //with valid data, start comparison
        boolean ma1_smaller = ma1[loop_index] < ma2[loop_index];
        ret[loop_index--] = ma1_smaller ? CROSSING_NONE : CROSSING_ABOVE;//fake start if MA1 > MA2 to start
        while (loop_index >= end_index) {//data used up
            if (ma1_smaller) {//look for crossing above
                if (ma1[loop_index] >= ma2[loop_index]) {
                    ret[loop_index] = CROSSING_ABOVE;
                    ma1_smaller = false;
                }
            }
            else {//look for crossing below
                if (ma1[loop_index] < ma2[loop_index]) {
                    ret[loop_index] = CROSSING_BELOW;
                    ma1_smaller = true;
                }
            }
            loop_index--;
        }
        return ret;
    }
    private void logTrade(String type, String date, float price) {
//todo        if (!Main.logging)
//            return;

//        StringBuilder buf = new StringBuilder();
//        buf.append("Trade: ").append(type).append("\t").append(date).append(" | ").append(price);
//        System.out.println(buf);
    }

    //-----instance variables / assessors-----
    protected FundData _Fund;
    public void setFund(FundData fund) {
        _Fund = fund;
    }

    protected ArrayList<Transaction> _Transactions;
    public ArrayList<Transaction> getTransactionLog() { return _Transactions; }

    //whether fund is Fidelity Select Funds.
    protected boolean fidelityFund;
    protected void setFidelityFund(boolean fidelity_fund) {
        fidelityFund = fidelity_fund;
    }
    public boolean isFidelityFund() { return fidelityFund; }

    protected boolean entrySignal;//entry signal generated at end of simulation
    public boolean isEntrySignal() {
        return entrySignal;
    }
    public void setEntrySignal(boolean entrySignal) {
        this.entrySignal = entrySignal;
    }

    protected boolean exitSignal; //exit signal generated at end of simulation
    public boolean isExitSignal() {
        return exitSignal;
    }
    public void setExitSignal(boolean exitSignal) {
        this.exitSignal = exitSignal;
    }
    //batch mode or single run mode
    protected boolean batchMode; //run many reports all at once
    public boolean isBatchMode() {
        return batchMode;
    }
    public void setBatchMode(boolean batch_mode) {
        batchMode = batch_mode;
    }

    protected String actualStartDate = "";

    protected ProgressBar progBar;
    public void setProgBar(ProgressBar pb) { progBar = pb; }

    private ArrayList<String> errors = new ArrayList<>();
    public void setErrors(ArrayList<String> errors) { this.errors = errors; }
    public ArrayList<String> getErrors() { return errors; }

    protected int[] _EntryCrossing;//array of Short MA crossing Medium MA
    protected int[] _ExitCrossing;//array of Medium MA crossing Long MA

    //----- literals -----
    private final static int CROSSING_NONE = 0;
    private final static int CROSSING_ABOVE = 1;
    private final static int CROSSING_BELOW = -1;
    private final static String LONG_ONLY = "LONG_ONLY";
    private final static String SHORT_ONLY = "SHORT_ONLY";
    private final static String LONG_SHORT = "LONG_SHORT";
}
//TODO Create default engine to save writing code