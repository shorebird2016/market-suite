package org.marketsuite.framework.strategy.lsquare;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.Props;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.AbstractStrategyPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

//container for L2 method related visual components
public class LSquarePanel extends AbstractStrategyPanel {
    public LSquarePanel() {//don't need super() and it will automatically takes everything in abstract class
        //replace title strip with special fields and buttons
        JPanel ttl_pnl = new JPanel(new MigLayout("insets 0", "5[]10[]30[]20[]push[]5", "3[]3")); ttl_pnl.setOpaque(false);
        ttl_pnl.add(_cmbSymbol);
        _cmbSymbol.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
                    return; //skip deselect, only process selection
                selectSymbol();

                //inform all listener about symbol change
                Props.SymbolSelection.setValue(_cmbSymbol.getSelectedItem());
            }
        });
        WidgetUtil.attachToolTip(_cmbSymbol, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_3"),
                SwingConstants.LEFT, SwingConstants.BOTTOM);
        ttl_pnl.add(_lblDateRange);
        ttl_pnl.add(_btnSimOption);
        _btnSimOption.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                _dlgOption.setVisible(true);
            }
        });
        ttl_pnl.add(_btnSimulate);
        _btnSimulate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                    simulate();
            }
        });
        ttl_pnl.add(createCgarEquityPanel());
        replaceTitleStrip(ttl_pnl);
        adjustTopDivider(400);

        //initialize
        populateSymbols();
    }

    //----- interface, overrides -----
    public AbstractEngine getEngine() { return _Engine; }
    public void simulate() {//special algorithm buys and sells
        _Engine = new LSquareEngine(_Fund);//already set via populateSymbols() initially
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("l2_simstart") + " " + _Fund.getSymbol());
        pb.setVisible(true);

        //simulate algorithm inside a thread
        new Thread() {
            public void run() {
                //prepare parameters for simulation, put out messages to pb each iteration....
                LSquareParam param = _dlgOption.getOptions();
                _Engine.setSimParam(param);
                _Engine.simulate();
                ArrayList<Transaction> trans = _Engine.getTransactionLog();
                if (trans == null || trans.size() == 0) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("err_notrans") + " " + _Fund.getSymbol());
                            pb.setVisible(false);
                        }
                    });
                    return;
                }

                //create report
                final SimReport rpt = _Engine.genReport();
                if (rpt != null)
                    rpt.printReport();

                //update visual components on screen
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sim_pbmsg_tblchart"));
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
                    }
                });
            }
        }.start();
    }

    //----- variables -----
    private LSquareEngine _Engine;
    private SimOptionDialog _dlgOption = new SimOptionDialog();
}
