package org.marketsuite.framework.strategy.buyhold;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.Stat;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.PeakValley;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.simulator.SimulatorPanel;
import org.marketsuite.simulator.basic.PickDateRangeDialog;
import net.miginfocom.swing.MigLayout;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Container for buy and hold strategy presentation and analysis.
 */
public class BuyHoldPanel extends AbstractStrategyPanel {
    public BuyHoldPanel(SimulatorPanel parent) {
        //custom title strip panel with controls and results
        JPanel west_pnl = new JPanel(new MigLayout("insets 0", "[]10[][]5[]20[]", "3[]3")); west_pnl.setOpaque(false);
        west_pnl.add(_cmbSymbolList);
        _cmbSymbolList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
                    return; //skip deselect, only process selection
                String sel_sym = (String) _cmbSymbolList.getSelectedItem();
                selectSymbol(sel_sym);//builds up historical quotes

                //inform all listener about symbol change
                Props.SymbolSelection.setValue(sel_sym);
            }
        });
        west_pnl.add(_txtBeginDate); west_pnl.add(_txtEndDate);
        west_pnl.add(_btnDateRanges); _btnDateRanges.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PickDateRangeDialog dlg = new PickDateRangeDialog();
                if (dlg.isCancelled()) return;
                String begin = dlg.getBeginDate();
                String end = dlg.getEndDate();
                if (begin == null || end == null) return;
                _txtBeginDate.setDate(AppUtil.stringToCalendarNoEx(begin).getTime());
                _txtEndDate.setDate(AppUtil.stringToCalendarNoEx(end).getTime());
                parent.setDateRange(begin, end);
            }
        });
        west_pnl.add(_btnSimulate); _btnSimulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try { simulate();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        //special east title strip, result, divider placement, hide price graph button
        replaceTitleStrip(WidgetUtil.createTitleStrip(west_pnl, null, createCgarEquityPanel()));
        replaceResultPanel(_pnlEquity = new EquityPanel());
        adjustTopDivider(820);
        _btnPriceGraph.setVisible(false);

        //initialize
        populateSymbols(_cmbSymbolList);
        _cmbSymbol.setSelectedItem(FrameworkConstants.SP500);
    }

    //----- interface implementation -----
    public AbstractEngine getEngine() { return _Engine; }

    //----- public methods -----
    //calculate result from underline strategy
    public void simulate() throws IOException, ParseException {
        _Engine = new BuyHoldEngine(_Fund);
//        FundData fund_data = _Engine.getFundData();
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        String start_date = AppUtil.calendarToString(cal);
        cal.setTime(_txtEndDate.getDate());
        String end_date = AppUtil.calendarToString(cal);
        _Engine.simulate(start_date, end_date);
        _Engine.printReport();

        //show results in fields
        ArrayList<Equity> log = _Engine.getEquityLog();
        _pnlEquity.populate(log);//fill equity table
        double sp = SimUtil.calcCAGR(log.get(0).getDate(), log.get(log.size() - 1).getDate(),
                FrameworkConstants.START_CAPITAL, log.get(log.size() - 1).getEquity());
        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(sp));
        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(log.get(log.size() - 1).getEquity()));

        //show result in table
        SimReport rpt = new SimReport();
        rpt.setEquityLog(log);
        rpt.setDrawDown(calcDrawDown(log));
        _pnlStat.updateStat(rpt, true);

        //plot - create two arrays of Float, X and Y
        String sym = _Fund.getSymbol();
        TimeSeries data_series = null;
        if (!sym.equals(FrameworkConstants.SP500)) {
            data_series = new TimeSeries(sym);
            for (Equity equity : log) {
                try {
                    cal = AppUtil.stringToCalendar(equity.getDate());
                    Day day = new Day(cal.getTime());
                    data_series.add(day, equity.getEquity());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        _lblGraphTitle.setText(FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_ch_1"));
        _pnlEquityGraph.plotEquitySeries(data_series, buildSp500EquitySeries(log, start_date));
//        plotPriceGraph();
        plotAnnualReturnGraph(null);//default has SP500 already
    }

    //calculate draw down stats of a data series; draw down = peak to valley decline on the curve
    //  algorithm: walk the series to find pairs of peak and valley, use their difference as draw down
    //  input: equity log, output: Stat object contains avg, min, max, median about draw down
    private Stat calcDrawDown(ArrayList<Equity> logs) {
        //step 1: find pairs of peak and valley
        ArrayList<PeakValley> pvs = new ArrayList<PeakValley>();
        PeakValley cur_pv = new PeakValley();//empty obj
        int log_idx = 0;//index of logs
        do {
            float data = logs.get(log_idx).getEquity();
            String data_date = logs.get(log_idx).getDate();
            if (data > cur_pv.getPeak()) {//new data point bigger than peak
                //must have peak first, if valley not set yet, skip this peak
                if (cur_pv.getValley() != Float.MAX_VALUE) {//valley set before
                    //new peak, advance index, start new pair
                    pvs.add(cur_pv = new PeakValley(data_date, data));
                }
                else {//no valley for current peak yet, replace this peak
                    cur_pv.setPeak(data);
                    cur_pv.setPeakDate(data_date);
                }
            }
            //new data less than exisiting valley
            else if (data < cur_pv.getValley()) {
                if (cur_pv.getPeak() != Float.MIN_VALUE) {//peak was recorded
                    cur_pv.setValley(data);
                    cur_pv.setValleyDate(data_date);
                }
            }
            //do nothing for values between existing peak and valley
            log_idx++;
        }while(log_idx < logs.size());
        //at end, if nothing recorded in pvs, use current peak/valley as the only one
        if (pvs.size() == 0) {
            pvs.add(new PeakValley(cur_pv));
        }

        //step 2: calc differences
        ArrayList<Float> dd_amt = new ArrayList<Float>();//amount
        ArrayList<Float> dd_pct = new ArrayList<Float>();//percent
        for (PeakValley pv : pvs) {
            dd_amt.add(-pv.getPeak() + pv.getValley());//use negative numbers
            dd_pct.add((-pv.getPeak() + pv.getValley()) / pv.getPeak());
        }
        //if the last point has no valley (still rising market), put peak into valley to make it 0 draw down
        if (pvs.size() > 1) {
            if (pvs.get(pvs.size() - 1).getValley() == Float.MAX_VALUE) {
                dd_amt.set(dd_amt.size() - 1, 0F);
                dd_pct.set(dd_pct.size() - 1, 0F);
            }
        }

        //step 3: calc stats
        return new Stat(AppUtil.average(dd_amt), AppUtil.min(dd_amt), AppUtil.max(dd_amt), AppUtil.median(dd_amt),
            AppUtil.average(dd_pct), AppUtil.min(dd_pct), AppUtil.max(dd_pct), AppUtil.median(dd_pct));
    }

    //-----instance variables-----
    private JComboBox<String> _cmbSymbolList = new JComboBox<>();//use separate combo for each tab
    private JButton _btnDateRanges = WidgetUtil.createIconButton("Select Date Ranges...", FrameworkIcon.FILTER);
    private EquityPanel _pnlEquity;
    private BuyHoldEngine _Engine;
}