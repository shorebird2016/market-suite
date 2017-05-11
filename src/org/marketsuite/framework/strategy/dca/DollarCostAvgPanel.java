package org.marketsuite.framework.strategy.dca;

import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Stat;
import org.marketsuite.framework.model.type.DcaType;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.PeakValley;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.buyhold.BuyHoldEngine;
import org.marketsuite.framework.strategy.buyhold.EquityPanel;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Stat;
import org.marketsuite.framework.model.type.DcaType;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.PeakValley;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.strategy.buyhold.EquityPanel;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;

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
 * Container for dollar cost averaging strategy presentation and analysis.
 */
public class DollarCostAvgPanel extends AbstractStrategyPanel {
    public DollarCostAvgPanel() {
        //custom title strip panel with controls and results
        JPanel west_pnl = new JPanel(new MigLayout("insets 0", "5[][]10[][]20[][][]10[]", "3[]3")); west_pnl.setOpaque(false);
        west_pnl.add(_cmbSymbol);
        _cmbSymbol.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
                    return; //skip deselect, only process selection
                selectSymbol();

                //inform all listener about symbol change
                Props.SymbolSelection.setValue(_cmbSymbol.getSelectedItem());
            }
        });west_pnl.add(_lblDateRange);
        west_pnl.add(_txtBeginDate); west_pnl.add(_txtEndDate);
        west_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("sim_intv")));
        west_pnl.add(_cmbDcaType); west_pnl.add(_fldAmount); _fldAmount.setText("250");
        west_pnl.add(_btnSimulate); _btnSimulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try { simulate();
                    long cap = _Engine.getPurchaseCount() * _fldAmount.getValue();
                    _txtEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(cap));
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        //special east title strip, result, divider placement, hide price graph button
        replaceTitleStrip(WidgetUtil.createTitleStrip(west_pnl, null, createCgarEquityPanel()));
        replaceResultPanel(_pnlEquity = new EquityPanel());
        adjustTopDivider(820);
        _btnPriceGraph.setVisible(false);

        //initialize
        populateSymbols();
        _cmbSymbol.setSelectedItem(FrameworkConstants.SP500);
    }

    //----- interface implementation -----
    public AbstractEngine getEngine() { return _Engine; }

    //----- public methods -----
    //calculate result from underline strategy TODO printReport() for debugging
    public void simulate() throws IOException, ParseException {
        _Engine = new DcaEngine((String)_cmbSymbol.getSelectedItem());
        _Engine.setDcaAmount((int)_fldAmount.getValue());
        _Engine.setDcaType((DcaType)_cmbDcaType.getSelectedItem());
        FundData fund_data = _Engine.getFundData();
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        String start_date = AppUtil.calendarToString(cal);
        cal.setTime(_txtEndDate.getDate());
        String end_date = AppUtil.calendarToString(cal);
        _Engine.simulate(start_date, end_date);

        //show results in fields
        ArrayList<Equity> log = _Engine.getEquityLog();
        _pnlEquity.populate(log);//fill equity table
        int buy_cnt = _Engine.getPurchaseCount();
        long begin_equity = buy_cnt * _fldAmount.getValue();
        float end_equity = log.get(log.size() - 1).getEquity();
        double sp = SimUtil.calcCAGR(log.get(0).getDate(), log.get(log.size() - 1).getDate(),
                begin_equity, end_equity);
        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(sp));
        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(end_equity));

        //show result in table
        SimReport rpt = new SimReport();
        rpt.setEquityLog(log);
        rpt.setTotalReturn(end_equity / begin_equity - 1);
        rpt.setTotalGain(end_equity - begin_equity);
        _pnlStat.updateStat(rpt, true);

        //plot - create two arrays of Float, X and Y
        String sym = fund_data.getSymbol();
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
        _lblGraphTitle.setText(ApolloConstants.APOLLO_BUNDLE.getString("sim_dcattl"));
        if (sym.equals(FrameworkConstants.SP500))
            _pnlEquityGraph.plotEquitySeries(null, buildSp500EquitySeries(log, start_date));
        else
            _pnlEquityGraph.plotEquitySeries(data_series, null);
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
    private EquityPanel _pnlEquity;
    private DcaEngine _Engine;
    private JComboBox<DcaType> _cmbDcaType = new JComboBox<>(DcaType.values());
    private LongIntegerField _fldAmount = new LongIntegerField(250, 4, 100, 10000);
}