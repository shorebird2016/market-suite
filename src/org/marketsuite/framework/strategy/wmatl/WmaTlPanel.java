package org.marketsuite.framework.strategy.wmatl;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel1;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.main.MdiMainFrame;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.main.MdiMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;

//A strategy using WMA (Weekly Moving Average) and TL (TrendLine) to enter/exit market
//Container for WmaTl strategy simulation
public class WmaTlPanel extends AbstractStrategyPanel1 {
    public WmaTlPanel() {//mostly use base class to build, only add south side special param
        JPanel param_pnl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[][]10[][][]push[][]5[][]5", "3[]3"));
        param_pnl.add(new JLabel("Weekly MA:")); param_pnl.add(_fldWma);
        param_pnl.add(new JLabel("Trend Line Range:")); param_pnl.add(_fldTlRange); param_pnl.add(new JLabel("weeks"));
        param_pnl.add(_chkLongTrade); param_pnl.add(_chkAdjClose);
        _pnlGraphHolder.add(param_pnl, BorderLayout.SOUTH);
        _btnSimulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                simulate();
            }
        });   }
    public AbstractEngine getEngine() { return _Engine; }
    public void simulate() {
        final String sym = (String)_cmbSymbol.getSelectedItem();
        _Engine.setFund(_Fund);//already loaded via selectSymbol()
        //setup sim parame - start time, end time, symbol
        Calendar cal = Calendar.getInstance();
        cal.setTime(_txtBeginDate.getDate());
        String start_date = AppUtil.calendarToString(cal);
        cal.setTime(_txtEndDate.getDate());
        String end_date = AppUtil.calendarToString(cal);

//TODO stop if not enough data

        StandardOption std_opt = new StandardOption(sym, _chkLongTrade.isSelected(), _chkShortTrade.isSelected(),
            start_date, end_date, _chkAdjClose.isSelected());
        WmaTlOption wt_opt = new WmaTlOption((int)_fldWma.getValue(), (int)_fldTlRange.getValue());
        final SimParam param = new SimParam(std_opt, wt_opt);
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "Start Simulation...");
        pb.setVisible(true);

        //do this in a thread
        Thread sim_th = new Thread() {
            public void run() {
                _Engine.setSimParam(param);
                _Engine.simulate();

                //skip if no transaction generated
                ArrayList<Transaction> trans = _Engine.getTransactionLog();
                if (trans == null || trans.size() == 0) {
                    _Engine.getErrors().add("<html>This simulation generates <b>NO</b> transactions.<br><br>Please check your settings.");
                }
                else {
                    //generate reports, tables and graphs
                    EventQueue.invokeLater(new Runnable() { public void run() { pb.setLabel("Generating Reports..."); } });

                    //update graph, tables
                    final SimReport rpt = _Engine.genReport();
                    rpt.printReport();
                    EventQueue.invokeLater(new Runnable() { public void run() {
                        _pnlStat.updateStat(rpt, false);
                        _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
                        _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
                        ArrayList<Transaction> trans_log = rpt.getTransLog();
                        _pnlTrade.populate(trans_log);//table
                        try {
                            SimUtil.plotEquityGraph(_pnlEquityGraph, rpt);
                            SimUtil.plotAnnualReturnGraph(_pnlAnnualReturnGraph, rpt);
                            SimUtil.plotPriceGraph(_pnlPriceGraph, rpt);
                        } catch (Exception e) {
                            e.printStackTrace();
                            pb.setVisible(false);
                        }
                        setGraphTitle();
                        pb.setVisible(false);
                        //TODO display errors if any
                    }
                    });
                }

            }
        };
        sim_th.start();
    }

    //----- variables -----
    private LongIntegerField _fldWma = new LongIntegerField(20, 3, 5, 100);//Weekly Moving Average period
    private LongIntegerField _fldTlRange = new LongIntegerField(50, 3, 20, 100);//number of weeks look back to draw TL
    private WmaTlEngine _Engine = new WmaTlEngine();
}
