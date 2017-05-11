package org.marketsuite.chart.candle;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

class SignalPickerDialog extends JDialog {
    public static SignalPickerDialog getInstance() {//singleton
        if (_Instance == null) return _Instance = new SignalPickerDialog();
        else return _Instance;
    }
    private SignalPickerDialog() {
        super(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("cc_pickttl"), false);
        JPanel content = new JPanel(new MigLayout("insets 0, flowy, wrap 6", "push[]10[]10[]10[]10[]10[]push"));//6 columns
        content.setBorder(new BevelBorder(BevelBorder.LOWERED)); content.setOpaque(false);
        setContentPane(content);

        //checkboxes for all candle signals
        CandleSignal[] sigs = CandleSignal.values();
        _chkSignals = new JCheckBox[sigs.length];
        for (int i = 0; i < sigs.length; i++) {
            _chkSignals[i] = new JCheckBox(sigs[i].toString());
            _chkSignals[i].setFocusable(false);
            _chkSignals[i].setSelected(true);
            _chkSignals[i].addActionListener(_listener);
            content.add(_chkSignals[i]);
        }

        //productivity checkboxes
        JPanel prod_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[]10", "3[]3"));
        prod_pnl.add(_btnAllOff); _btnAllOff.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkSignals) chk.setSelected(false);
                triggerChartRepaint();
            }
        });
        prod_pnl.add(_btnSelectAll); _btnSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkSignals) chk.setSelected(true);
                triggerChartRepaint();
            }
        });
        prod_pnl.add(_btnBullish);
        _btnBullish.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkSignals) chk.setSelected(false);
                _chkSignals[CandleSignal.BullishEngulfing.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.BullishHarami.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.BullishPusher.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.BullishKicker.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.Hammer.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.InvertedHammer.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.Piercing.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.MorningStar.ordinal()].setSelected(true);
                triggerChartRepaint();
            }
        });
        prod_pnl.add(_btnBearish);
        _btnBearish.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkSignals) chk.setSelected(false);
                _chkSignals[CandleSignal.BearishEngulfing.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.BearishHarami.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.BearishPusher.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.BearishKicker.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.HangingMan.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.ShootingStar.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.DarkCloud.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.EveningStar.ordinal()].setSelected(true);
                triggerChartRepaint();
            }
        });
        prod_pnl.add(_btnContinuation);
        _btnContinuation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkSignals) chk.setSelected(false);
                _chkSignals[CandleSignal.DojiTop.ordinal()].setSelected(true);
                _chkSignals[CandleSignal.BullishWindows.ordinal()].setSelected(true);
                triggerChartRepaint();
            }
        });
        content.add(prod_pnl, "dock north");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { dispose(); } });
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false, MdiMainFrame.getInstance(),
                WindowConstants.DISPOSE_ON_CLOSE, false);
    }
    void toggleVisibility() {
        setVisible(!isVisible());}

    //----- private methods -----
    private void triggerChartRepaint() {
        ArrayList<CandleSignal> signals = new ArrayList<>();
        for (JCheckBox cb : _chkSignals) {
            if (cb.isSelected())
                signals.add(CandleSignal.toEnumConstant(cb.getText()));//no need to check null
        }
        Props.CandleSignal.setValue(null, signals);//send array of strings
    }

    //----- inner class -----
    private class CheckListener implements ActionListener {
        public void actionPerformed(ActionEvent aev) {//whenever any clicked, send list out
            triggerChartRepaint();
        }
    }

    //----- variables -----
    private static SignalPickerDialog _Instance;
    private JCheckBox[] _chkSignals;
    private CheckListener _listener = new CheckListener();
    private JButton _btnAllOff = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cc_picknone"), FrameworkIcon.CLEAR);
    private JButton _btnSelectAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cc_pickall"), FrameworkIcon.SELECT_ALL);
    private JButton _btnBullish = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cc_bullish"), FrameworkIcon.BULL);
    private JButton _btnBearish = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cc_bearish"), FrameworkIcon.BEAR);
    private JButton _btnContinuation = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("cc_trendup"), FrameworkIcon.TREND_UP);
}
