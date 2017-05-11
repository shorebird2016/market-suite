package org.marketsuite.riskmgr.account;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog to quickly help user to calculate number of shares for add-on.
 */
class AddSharesDialog extends JDialog {
    AddSharesDialog(String symbol) {
        super(MdiMainFrame.getInstance(), false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("rm_01"));
        JPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new MigLayout("insets 0 50 0 50, flowy"));
        content_pnl.setOpaque(false); content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //north - symbol + capital + button
        JPanel north_pnl = new JPanel(new MigLayout("insets 0", "10[]push[]push[]10")); north_pnl.setOpaque(false);
        _lblSymbol.setFont(FrameworkConstants.FONT_VERY_BIG);
        north_pnl.add(_lblSymbol);
        north_pnl.add(_fldCurCapital); _fldCurCapital.setEditable(false);
        _fldCurCapital.setFont(FrameworkConstants.BIG_FONT);
        north_pnl.add(_btnNewOrAdd); _btnNewOrAdd.setFont(FrameworkConstants.BIG_FONT);
        _btnNewOrAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean open_new = _btnNewOrAdd.isSelected();
                _fldCurShares.setEnabled(!open_new);
                _fldCurCost.setEnabled(!open_new);
                _fldCurStop.setEnabled(!open_new);
                _btnNewOrAdd.setText(open_new ? ApolloConstants.APOLLO_BUNDLE.getString("rm_98") :
                        ApolloConstants.APOLLO_BUNDLE.getString("rm_99"));
                calcShares();
            }
        });
        WidgetUtil.attachToolTip(_btnNewOrAdd, ApolloConstants.APOLLO_BUNDLE.getString("rm_102"),
                SwingConstants.RIGHT, SwingConstants.TOP);
        content_pnl.add(north_pnl, "dock north");

        //3 strips of fields for calculation, strip 1 - reduce risk budget by used risk
        JPanel pnl1 = new JPanel(new MigLayout()); pnl1.setOpaque(false);
        pnl1.add(_fldRiskBudget = new LongIntegerField(150, 4, 50, 300, true), "gapy 20");
        _fldRiskBudget.setFont(FrameworkConstants.BIG_FONT);
        _fldRiskBudget.setHorizontalAlignment(JTextField.CENTER);
        WidgetUtil.attachToolTip(_fldRiskBudget, ApolloConstants.APOLLO_BUNDLE.getString("rm_103"),
                SwingConstants.LEFT, SwingConstants.TOP);
        _fldRiskBudget.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calcShares();
            }
        });
        JLabel lbl = new JLabel(" - "); lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl1.add(lbl, "center");
        pnl1.add(_fldCurShares);
        _fldCurShares.setHorizontalAlignment(JTextField.CENTER);
        _fldCurShares.setFont(FrameworkConstants.BIG_FONT); _fldCurShares.setEditable(false);
        WidgetUtil.attachToolTip(_fldCurShares, ApolloConstants.APOLLO_BUNDLE.getString("rm_104"),
                SwingConstants.LEFT, SwingConstants.TOP);
        lbl = new JLabel(" X  ( ");  lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl1.add(lbl, "right");
        pnl1.add(_fldCurCost);
        _fldCurCost.setHorizontalAlignment(JTextField.CENTER);
        _fldCurCost.setFont(FrameworkConstants.BIG_FONT); _fldCurCost.setEditable(false);
        WidgetUtil.attachToolTip(_fldCurCost, ApolloConstants.APOLLO_BUNDLE.getString("rm_105"),
                SwingConstants.LEFT, SwingConstants.TOP);
        lbl = new JLabel(" - "); lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl1.add(lbl, "center");
        pnl1.add(_fldCurStop);
        _fldCurStop.setHorizontalAlignment(JTextField.CENTER);
        _fldCurStop.setFont(FrameworkConstants.BIG_FONT); _fldCurStop.setEditable(false);
        WidgetUtil.attachToolTip(_fldCurStop, ApolloConstants.APOLLO_BUNDLE.getString("rm_106"),
                SwingConstants.LEFT, SwingConstants.TOP);
        lbl = new JLabel(" ) "); lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl1.add(lbl, "center, wrap");
        content_pnl.add(pnl1, "gapy 0 20");

        //strip 2 - divisor of newly incurred risk per share
        JPanel pnl2 = new JPanel(new MigLayout()); pnl2.setOpaque(false);
        lbl = new JLabel(" /   ( ");  lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl2.add(lbl, "gapx 50, right");
        pnl2.add(_fldCurAsk = new DecimalField(0, 4, 0, 2000, null));
        _fldCurAsk.setFont(FrameworkConstants.BIG_FONT);
        _fldCurAsk.setHorizontalAlignment(JTextField.CENTER);
        WidgetUtil.attachToolTip(_fldCurAsk, ApolloConstants.APOLLO_BUNDLE.getString("rm_107"),
                SwingConstants.LEFT, SwingConstants.TOP);
        _fldCurAsk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calcShares();
            }
        });
        lbl = new JLabel(" - "); lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl2.add(lbl, "center");
        pnl2.add(_fldNewStop);
        _fldNewStop.setHorizontalAlignment(JTextField.CENTER);
        _fldNewStop.setFont(FrameworkConstants.BIG_FONT);
        WidgetUtil.attachToolTip(_fldNewStop, ApolloConstants.APOLLO_BUNDLE.getString("rm_108"),
                SwingConstants.LEFT, SwingConstants.TOP);
        _fldNewStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calcShares();
            }
        });
        lbl = new JLabel(" ) "); lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl2.add(lbl, "wrap");
        content_pnl.add(pnl2, "gapy 0 20");

        //strip 3 - calculated shares
        JPanel pnl3 = new JPanel(new MigLayout("", "[][][]50[]")); pnl3.setOpaque(false);
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_100") + " ");
        lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl3.add(lbl);
        pnl3.add(_fldNewShares); _fldNewShares.setFont(FrameworkConstants.FONT_VERY_BIG);
        _fldNewShares.setHorizontalAlignment(JTextField.CENTER);
        WidgetUtil.attachToolTip(_fldNewShares, ApolloConstants.APOLLO_BUNDLE.getString("rm_109"),
            SwingConstants.LEFT, SwingConstants.TOP);
        lbl = new JLabel(" " + ApolloConstants.APOLLO_BUNDLE.getString("rm_101"));
        lbl.setFont(FrameworkConstants.FONT_VERY_BIG);
        pnl3.add(lbl);
        pnl3.add(_fldNewCapital); _fldNewCapital.setFont(FrameworkConstants.BIG_FONT); _fldNewCapital.setEditable(false);
        WidgetUtil.attachToolTip(_fldNewCapital, ApolloConstants.APOLLO_BUNDLE.getString("rm_110"),
                SwingConstants.LEFT, SwingConstants.TOP);
        content_pnl.add(pnl3);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        setContentPane(content_pnl);
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false,
            RiskMgrModel.getInstance().getParent(), WindowConstants.HIDE_ON_CLOSE);
        populate(symbol);//update all fields matching this symbol
    }

    void populate(String symbol) {
        if (symbol == null) return;
        _lblSymbol.setText(symbol);
        _CurPosition = RiskMgrModel.getInstance().findPosition(symbol);
        int shares = _CurPosition.getShares();
        float cost = _CurPosition.getCost();
        _fldCurCapital.setText(FrameworkConstants.DOLLAR_FORMAT.format(shares * cost));
        _fldCurShares.setText(FrameworkConstants.SMALL_INT_FORMAT.format(shares));
        _fldCurCost.setText(FrameworkConstants.PRICE_FORMAT.format(cost));
        _fldCurStop.setText(FrameworkConstants.PRICE_FORMAT.format(_CurPosition.getStop()));
        float close = _CurPosition.getStopLevelInfo().getQuotes().get(0).getClose();
        _fldCurAsk.setText(String.valueOf(close));
        _fldNewStop.setText(FrameworkConstants.PRICE_FORMAT.format(_CurPosition.getStop()));
        calcShares();
    }
    void calcShares() {
        //without risk, skip existing calculation
        float used_risk = _CurPosition.getCost() - _CurPosition.getStop();
        if (used_risk < 0)
            used_risk = 0;

        //or for opening new positions
        if (_btnNewOrAdd.isSelected())
            used_risk = 0;
        float available_risk = _fldRiskBudget.getValue() - _CurPosition.getShares() * used_risk;
        double new_risk_per_share = _fldCurAsk.getValue() - _fldNewStop.getValue();
        double sh = available_risk / new_risk_per_share;
        if (sh < 0) sh = 0;
        _fldNewShares.setText(String.valueOf((int) sh));
        _fldNewCapital.setText(FrameworkConstants.DOLLAR_FORMAT.format(_fldCurAsk.getValue() * (sh + _CurPosition.getShares())));
    }

    //----- instance variables -----
    private JLabel _lblSymbol = new JLabel();
    private JTextField _fldCurCapital = new JTextField(6);
    private JToggleButton _btnNewOrAdd = new JToggleButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_99"));
    private LongIntegerField _fldRiskBudget;
    private JTextField _fldCurShares = new JTextField(4);
    private JTextField _fldCurCost = new JTextField(4);
    private JTextField _fldCurStop = new JTextField(4);
    private DecimalField _fldCurAsk;
    private DecimalField _fldNewStop = new DecimalField(0, 4, 0, 2000, null);
    private JTextField _fldNewShares = new JTextField(3);
    private JTextField _fldNewCapital = new JTextField(6);
    private Position _CurPosition;

    private boolean _bCancelled;
}
//    boolean isCancelled() { return _bCancelled; }

//buttons TODO maybe sending it back to the table
//        JPanel btn_pnl = new JPanel();
//        btn_pnl.setOpaque(false);
//        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
//        ok_btn.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent aev) {
//                //write a line to selected file
//
//
//                //if file doesn't exist, create first
//
////                _bCancelled = false;
//                dispose();
//            }
//        });
//        btn_pnl.add(ok_btn);
//        JButton cancel_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_cancel"));
//        cancel_btn.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent aev) {
//                _bCancelled = true;
//                dispose();
//            }
//        });
//        btn_pnl.add(cancel_btn);
//        content_pnl.add(btn_pnl, "dock south");
//        getRootPane().setDefaultButton(ok_btn);
