package org.marketsuite.planning;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.Constants;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog to obtain several parameters from user while tracking signals.
 */
class ReportOptionDialog extends JDialog {
    ReportOptionDialog(boolean hide_doji, boolean show_bullish, boolean show_bearish) {
        super(MdiMainFrame.getInstance(), true);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_opt"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new MigLayout("insets 10 20 10 30, wrap 2"));
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
        content_pnl.add(_chkDoji, "wrap"); content_pnl.add(_chkBull); content_pnl.add(_chkBear);
        _chkDoji.setSelected(hide_doji); _chkBull.setSelected(show_bullish); _chkBear.setSelected(show_bearish);
        content_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_rtg_limit")), "right");
        content_pnl.add(_fldLowRating, "left");
        JPanel btn_pnl = new JPanel(new MigLayout("insets 0", "push[][]push")); btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                //persist pref
//                ApolloPreferenceStore.getPreferences().setTrackerOption(getTrackerOption());
//                ApolloPreferenceStore.savePreferences();
                _bCancelled = false;
                dispose();
            }
        });
        btn_pnl.add(ok_btn);
        JButton cancel_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_cancel"));
        cancel_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) { _bCancelled = true; dispose(); }
        });
        btn_pnl.add(cancel_btn);
        content_pnl.add(btn_pnl, "dock south");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        setContentPane(content_pnl);
        getRootPane().setDefaultButton(ok_btn);
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false,
                MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    //----- accessor -----
    boolean isCancelled() { return _bCancelled; }
    boolean showDoji() { return !_chkDoji.isSelected(); }
    boolean showBull() { return _chkBull.isSelected(); }
    boolean showBear() { return _chkBear.isSelected(); }

    //----- instance variables -----
    private JCheckBox _chkDoji = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_doji"));
    private JCheckBox _chkBull = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_bullish"));
    private JCheckBox _chkBear = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_bearish"));
    private LongIntegerField _fldLowRating = new LongIntegerField(80, 3, 1, 99);//both COMP and RS
    private boolean _bCancelled;
}