package org.marketsuite.framework.strategy.connor;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;

/**
 * Container for the strategy back-testing from Larry Connor's book
 * "High Probability ETF Trading"
 * The result is compared with SP500.
 */
public class ConnorPanel extends AbstractStrategyPanel {
    public ConnorPanel() {
        //custom title strip - result file selection, check box
        //replace west panel of existing title strip
        JPanel west_pnl = new JPanel();  west_pnl.setOpaque(false);
        west_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("strategy")));
        west_pnl.add(_cmbStrategy);
        WidgetUtil.attachToolTip(_cmbStrategy, FrameworkConstants.FRAMEWORK_BUNDLE.getString("connor_tip_1"),
            SwingConstants.LEFT, SwingConstants.BOTTOM);
        west_pnl.add(Box.createHorizontalStrut(10));
        
        //symbol choice
        west_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("connor_lbl_17")));
        west_pnl.add(_cmbSymbol = new JComboBox(FrameworkConstants.LIST_CONNOR_ETF));
        WidgetUtil.attachToolTip(_cmbSymbol, FrameworkConstants.FRAMEWORK_BUNDLE.getString("connor_tip_2"),
            SwingConstants.LEFT, SwingConstants.BOTTOM);
        west_pnl.add(Box.createHorizontalStrut(10));
        
        //year selection combo
        west_pnl.add(_txtBeginDate);
        west_pnl.add(_txtEndDate);
        west_pnl.add(Box.createHorizontalStrut(10));

        //aggressive strategy modification
        west_pnl.add(_chkAggressive); _chkAggressive.setOpaque(false); _chkAggressive.setSelected(true);
        _chkAggressive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            _Engine.setAggressive(_chkAggressive.isSelected());
            }
        });
        WidgetUtil.attachToolTip(_chkAggressive, FrameworkConstants.FRAMEWORK_BUNDLE.getString("connor_tip_5"),
            SwingConstants.RIGHT, SwingConstants.BOTTOM);
        west_pnl.add(Box.createHorizontalStrut(20));

        //start sim
        west_pnl.add(_btnSimulate);
        _btnSimulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                try {
                    simulate();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        JPanel east_pnl = createCgarEquityPanel();
        replaceTitleStrip(WidgetUtil.createTitleStrip(west_pnl, null, east_pnl));

        //initialize combo with first symbol's quote range
        selectSymbol();
    }

    public AbstractEngine getEngine() { return _Engine; }

    public void simulate() throws ParseException {
        //base on user selection, start simulation
        _Engine.setStrategy((String)_cmbStrategy.getSelectedItem());
        try {
            //if same fund, don't load again
            String sel = (String) _cmbSymbol.getSelectedItem();
            if (_sCurrentFund == null || !_sCurrentFund.equals(sel)) {
                _sCurrentFund = sel;
                _Engine.loadFund(sel);
            }
            _Engine.simulate("", null);
            //after simulation, if last transaction is open, close it
            int tran_len = _Engine.getTransactionLog().size();
            Transaction last_trade = _Engine.getTransactionLog().get(tran_len - 1);
            if (last_trade.getExitDate() == null) {
                FundData fund = _Engine.getFund();
                last_trade.setExitDate(fund.getDate(0));
                last_trade.setExitPrice(fund.getPrice(0));
                last_trade.calcPerformance();
            }
            SimReport rpt = _Engine.genReport();
            rpt.printReport();
            //update tables and graphs
            _pnlTrade.populate(rpt.getTransLog());
            _txtEndEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(rpt.getEndEquity()));
            _txtCagr.setText(FrameworkConstants.ROI_FORMAT.format(rpt.getCagr()));
            try {
                _pnlEquityGraph.getChart().setTitle(_cmbStrategy.getSelectedItem() + " " +
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("eqty_curve"));
                SimUtil.plotEquityGraph(_pnlEquityGraph, rpt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            _pnlStat.updateStat(rpt, false);
            plotAnnualReturnGraph(rpt.getAnnualReturns());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //-----instance variables-----
    private JComboBox _cmbStrategy = new JComboBox(FrameworkConstants.LIST_CONNOR_STRATEGY);
    private JCheckBox _chkAggressive = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("connor_ttl_2"));
    private ConnorEngine _Engine = new ConnorEngine();
    private String _sCurrentFund;
}