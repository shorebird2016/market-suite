package org.marketsuite.scanner.tracking;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.riskmanager.RiskMgrModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.marketsuite.component.Constants;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.riskmanager.RiskMgrModel;

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
class TrackerOptionDialog extends JDialog {
    TrackerOptionDialog() {
        super(MdiMainFrame.getInstance(), true);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("trk_17"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //form declaration
        FormLayout layout = new FormLayout(
            "10dlu, r:p, 5dlu, l:p, 2dlu, l:p, 5dlu, l:p, 10dlu, p, 10dlu",//3 components each row
            "10dlu, p, 2dlu, p, 10dlu, p, 2dlu, p, 2dlu, p, 10dlu, p, 2dlu, p, 10dlu, p, 2dlu, p, 5dlu"//9 rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int row = 2;

        //DSTO
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_18"), cc.xy(2, row));
        builder.add(_txtDstoLength, cc.xy(4, row)); _txtDstoLength.setText("9");
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_16"), cc.xy(2, row));
        builder.add(_txtDstoLow, cc.xy(4, row)); _txtDstoLow.setText("20");
        builder.add(_txtDstoHigh, cc.xy(6, row)); _txtDstoHigh.setText("50");
        row += 2;

        //DVG
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_22"), cc.xy(2, row));
        builder.add(_txtLookback, cc.xy(4, row)); _txtLookback.setText("90");
        WidgetUtil.attachToolTip(_txtLookback, ApolloConstants.APOLLO_BUNDLE.getString("trk_19"),
                SwingConstants.RIGHT, SwingConstants.BOTTOM);
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_23"), cc.xy(2, row));
        builder.add(_txtBps, cc.xy(4, row)); _txtBps.setText("5");
        WidgetUtil.attachToolTip(_txtBps, ApolloConstants.APOLLO_BUNDLE.getString("trk_20"),
                SwingConstants.CENTER, SwingConstants.BOTTOM);
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_24"), cc.xy(2, row));
        builder.add(_spnRecentFilter, cc.xy(4, row));
        WidgetUtil.attachToolTip(_spnRecentFilter, ApolloConstants.APOLLO_BUNDLE.getString("trk_21"),
                SwingConstants.CENTER, SwingConstants.BOTTOM);

        //percent of 10x30, 50x120
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_27"), cc.xy(2, row));
        builder.add(_txtPercent10x30, cc.xy(4, row));
        WidgetUtil.attachToolTip(_txtPercent10x30, ApolloConstants.APOLLO_BUNDLE.getString("trk_31"),
            SwingConstants.CENTER, SwingConstants.BOTTOM);
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_28"), cc.xy(2, row));
        builder.add(_txtPercent50x120, cc.xy(4, row));
        WidgetUtil.attachToolTip(_txtPercent50x120, ApolloConstants.APOLLO_BUNDLE.getString("trk_32"),
                SwingConstants.CENTER, SwingConstants.BOTTOM);

        //price and volume thresholds
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_29"), cc.xy(2, row));
        builder.add(_txtPriceThreshold, cc.xy(4, row));
        WidgetUtil.attachToolTip(_txtPriceThreshold, ApolloConstants.APOLLO_BUNDLE.getString("trk_35"),
                SwingConstants.CENTER, SwingConstants.BOTTOM);
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_30"), cc.xy(2, row));
        builder.add(_txtVolumeThreshold, cc.xy(4, row));
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_37"), cc.xy(6, row));
        WidgetUtil.attachToolTip(_txtVolumeThreshold, ApolloConstants.APOLLO_BUNDLE.getString("trk_36"),
                SwingConstants.CENTER, SwingConstants.BOTTOM);
        JPanel pnl = builder.getPanel();  pnl.setOpaque(false);
        content_pnl.add(pnl, BorderLayout.CENTER);

        //buttons
        JPanel btn_pnl = new JPanel();
        btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                //persist pref
                ApolloPreferenceStore.getPreferences().setTrackerOption(getTrackerOption());
                ApolloPreferenceStore.savePreferences();
                _bCancelled = false;
                dispose();
            }
        });
        btn_pnl.add(ok_btn);
        JButton cancel_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_cancel"));
        cancel_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                _bCancelled = true;
                dispose();
            }
        });
        btn_pnl.add(cancel_btn);
        content_pnl.add(btn_pnl, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        setContentPane(content_pnl);
        getRootPane().setDefaultButton(ok_btn);

        //populate from preference
        TrackerOption opt = ApolloPreferenceStore.getPreferences().getTrackerOption();
        if (opt != null) {
            _txtDstoLow.setText(String.valueOf(opt.getDstoLow()));
            _txtDstoHigh.setText(String.valueOf(opt.getDstoHigh()));
            _txtBps.setText(String.valueOf(opt.getDvgOption().getBarPerSegment()));
            _txtLookback.setText(String.valueOf(opt.getDvgOption().getLookback()));
            _spnRecentFilter.setValue(opt.getDvgOption().getRecentFilter());
            _txtPercent10x30.setText(String.valueOf(opt.getPercent10x30()));
            _txtPercent50x120.setText(String.valueOf(opt.getPercent50x120()));
            _txtPriceThreshold.setText(String.valueOf(opt.getPriceThreshold()));
            _txtVolumeThreshold.setText(String.valueOf(opt.getAverageVolumeThreshold()));
        }
        else {//not in pref, create a new set for it
            opt = new TrackerOption();
            ApolloPreferenceStore.getPreferences().setTrackerOption(opt);
            ApolloPreferenceStore.savePreferences();
        }
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false,
            RiskMgrModel.getInstance().getParent(), WindowConstants.DO_NOTHING_ON_CLOSE, false);
    }

    //----- accessor -----
    boolean isCancelled() { return _bCancelled; }
    int getDstoLength() { return (int) _txtDstoLow.getValue(); }
    int getDstoLow() { return (int) _txtDstoLow.getValue(); }
    int getDstoHigh() { return (int) _txtDstoHigh.getValue(); }
    int getLookback() { return (int) _txtLookback.getValue(); }
    int getBps() { return (int) _txtBps.getValue(); }
    int getFilter() { return _spnRecentFilter.getValue(); }
    float getPct10x30() { return (float)_txtPercent10x30.getValue(); }
    float getPct50x120() { return (float)_txtPercent50x120.getValue(); }
    float getPriceThreshold() { return (float)_txtPriceThreshold.getValue(); }
    int getVolumeThreshold() { return (int)_txtVolumeThreshold.getValue() * 1000; }
    DivergenceOption getDvgOption() { return new DivergenceOption(getBps(), getLookback(), getFilter()); }
    TrackerOption getTrackerOption() {
        TrackerOption ret = new TrackerOption(
            getDstoLow(), getDstoHigh(),
            getPct10x30(), getPct50x120(),
            getPriceThreshold(), getVolumeThreshold(), getDvgOption()
        );
        return ret;
    }

    //----- instance variables -----
    private LongIntegerField _txtDstoLow = new LongIntegerField(20, 5, 1, 100, true);
    private LongIntegerField _txtDstoHigh = new LongIntegerField(50, 5, 1, 100, true);
    private LongIntegerField _txtDstoLength = new LongIntegerField(14, 5, 1, 100, true);
    private LongIntegerField _txtLookback = new LongIntegerField(90, 3, 30, 150);
    private LongIntegerField _txtBps = new LongIntegerField(5, 3, 1, 20);
    private IntegerSpinner _spnRecentFilter = new IntegerSpinner("", 3, 1, 20, 1, "", null);
    private DecimalField _txtPercent10x30 = new DecimalField(5, 5, 1, 50, null);
    private DecimalField _txtPercent50x120 = new DecimalField(5, 5, 1, 50, null);
    private LongIntegerField _txtPriceThreshold = new LongIntegerField(20, 3, 1, 50);
    private LongIntegerField _txtVolumeThreshold = new LongIntegerField(250, 5, 1, 1000);//in 000s
    private boolean _bCancelled;
}