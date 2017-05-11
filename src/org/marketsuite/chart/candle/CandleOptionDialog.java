package org.marketsuite.chart.candle;

import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.Constants;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.spinner.IntegerSpinner;
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
class CandleOptionDialog extends JDialog {
    CandleOptionDialog() {
        super(MdiMainFrame.getInstance(), true);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("cdl_option"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new MigLayout("insets 10 20 10 30, wrap 3", "[right][left][left]"));
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
        content_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("cdl_djbody_pct")));
        content_pnl.add(_fldDojiBodyPct); content_pnl.add(new JLabel("%"));
        content_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("cdl_upshadow_pct")));
        content_pnl.add(_fldUpperShadowPct); content_pnl.add(new JLabel("%"));
        content_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("cdl_lowshadow_pct")));
        content_pnl.add(_fldLowerShadowPct); content_pnl.add(new JLabel("%"));
        content_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("cdl_egf_pct")));
        content_pnl.add(_fldMaxEngulfPct); content_pnl.add(new JLabel("%"));
        content_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("cdl_harami_pct")));
        content_pnl.add(_fldMinHaramiPct); content_pnl.add(new JLabel("%"));
        content_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("cdl_trnd_lookback")));
        content_pnl.add(_spnTrendLookbackBar, "wrap");
        setContentPane(content_pnl);

        //buttons
//        JPanel btn_pnl = new JPanel(new MigLayout("insets 0, center")); btn_pnl.setOpaque(false);
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
        add(ok_btn);
        JButton cancel_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_cancel"));
        cancel_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) { _bCancelled = true; dispose(); }
        });
        add(cancel_btn);
//        content_pnl.add(btn_pnl, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        setContentPane(content_pnl);
        getRootPane().setDefaultButton(ok_btn);

        //populate from preference
//        TrackerOption opt = ApolloPreferenceStore.getPreferences().getTrackerOption();
//        if (opt != null) {
//            _txtDstoLow.setText(String.valueOf(opt.getDstoLow()));
//            _txtDstoHigh.setText(String.valueOf(opt.getDstoHigh()));
//            _fldTrendLookbackBars.setText(String.valueOf(opt.getDvgOption().getBarPerSegment()));
//            _txtLookback.setText(String.valueOf(opt.getDvgOption().getLookback()));
//            _spnRecentFilter.setValue(opt.getDvgOption().getRecentFilter());
//            _fldDojiBodyPct.setText(String.valueOf(opt.getPercent10x30()));
//            _txtPercent50x120.setText(String.valueOf(opt.getPercent50x120()));
//            _fldEngulfPct.setText(String.valueOf(opt.getPriceThreshold()));
//            _txtVolumeThreshold.setText(String.valueOf(opt.getAverageVolumeThreshold()));
//        }
//        else {//not in pref, create a new set for it
//            opt = new TrackerOption();
//            ApolloPreferenceStore.getPreferences().setTrackerOption(opt);
//            ApolloPreferenceStore.savePreferences();
//        }
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false,
            MdiMainFrame.getInstance(), WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    //----- accessor -----
    boolean isCancelled() { return _bCancelled; }

    //----- instance variables -----
//    private LongIntegerField _txtLookback = new LongIntegerField(90, 3, 30, 150);
    private IntegerSpinner _spnTrendLookbackBar = new IntegerSpinner("", 3, 1, 20, 1, "", null);
    private DecimalField _fldDojiBodyPct = new DecimalField(5, 5, 1, 20, null);
    private DecimalField _fldUpperShadowPct = new DecimalField(5, 5, 1, 20, null);
    private DecimalField _fldLowerShadowPct = new DecimalField(5, 5, 1, 20, null);
    private DecimalField _fldMaxEngulfPct = new DecimalField(5, 5, 1, 50, null);
    private DecimalField _fldMinHaramiPct = new DecimalField(5, 5, 1, 30, null);
    private boolean _bCancelled;
}