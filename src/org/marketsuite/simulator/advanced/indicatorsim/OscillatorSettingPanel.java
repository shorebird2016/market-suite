package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.mac.MacEngine;
import org.marketsuite.framework.strategy.mac.MacOption;
import org.marketsuite.simulator.indicator.macd.MacdZeroCrossEngine;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiEngine;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticEngine;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.report.model.StrategySetting;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.mac.MacEngine;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.report.model.StrategySetting;
import org.marketsuite.simulator.indicator.macd.MacdZeroCrossEngine;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * container for oscillator related settings located as a tab under Advanced Simulation | Setup.
 */
class OscillatorSettingPanel extends JPanel {
    OscillatorSettingPanel() {
        setLayout(new GridLayout(1, 4));
        setBorder(new BevelBorder(BevelBorder.RAISED));
        WidgetUtil.setMaxMinPrefSize(this, 0, 150);
        JPanel mac_pnl = createMacSettingPanel();
        mac_pnl.setBorder(new TitledBorder(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_9")));
        add(mac_pnl);

        JPanel macd_pnl = createMzcSettingPanel();
        macd_pnl.setBorder(new TitledBorder(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_10")));
        add(macd_pnl);

        JPanel rsi_pnl = createRsiSettingPanel();
        rsi_pnl.setBorder(new TitledBorder(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_11")));
        add(rsi_pnl);

        JPanel sto_pnl = createStoSettingPanel();
        sto_pnl.setBorder(new TitledBorder(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_12")));
        add(sto_pnl);
    }

    //use str_setting to fill all fields, if null, default values are used
    void populate(StrategySetting str_setting) {
        initializeFields();
        if (str_setting == null)
            return;

        MacOption mac_setting = str_setting.getMacSetting();
        if (mac_setting != null) {
            _spnEntryMA1.setValue(mac_setting.getEntryMA1());
            _spnEntryMA2.setValue(mac_setting.getEntryMA2());
            _spnExitMA1.setValue(mac_setting.getExitMA1());
            _spnExitMA2.setValue(mac_setting.getExitMA2());
        }
        MzcOption mzc_setting = str_setting.getMzcSetting();
        if (mzc_setting != null) {
            _spnFastMA.setValue(mzc_setting.getFastMA());
            _spnSlowMA.setValue(mzc_setting.getSlowMA());
        }
        RsiOption rsi_setting = str_setting.getRsiSetting();
        if (rsi_setting != null) {
            _spnRsiLength.setValue(rsi_setting.getLength());
            _spnRsiOversold.setValue(rsi_setting.getOversold());
            _spnRsiOverbought.setValue(rsi_setting.getOverbought());
        }
        StochasticOption sto_setting = str_setting.getStochasticSetting();
        if (sto_setting != null) {
            _spnStoLength.setValue(sto_setting.getLength());
            _spnStoMaPeriod.setValue(sto_setting.getMaPeriod());
            _spnStoOversold.setValue(sto_setting.getOversold());
            _spnStoOverbought.setValue(sto_setting.getOverbought());
        }
    }

    //initialize to defaults
    private void initializeFields() {
        _spnEntryMA1.setValue(MacEngine._nEntryMA1);
        _spnEntryMA2.setValue(MacEngine._nEntryMA2);
        _spnExitMA1.setValue(MacEngine._nExitMA1);
        _spnExitMA2.setValue(MacEngine._nExitMA2);
        _spnFastMA.setValue(MacdZeroCrossEngine.FAST_MA);
        _spnSlowMA.setValue(MacdZeroCrossEngine.SLOW_MA);
        _spnRsiLength.setValue(RsiEngine.DEFAULT_LENGTH);
        _spnRsiOversold.setValue(RsiEngine.DEFAULT_OVERSOLD);
        _spnRsiOverbought.setValue(RsiEngine.DEFAULT_OVERBOUGHT);
        _spnStoLength.setValue(StochasticEngine.DEFAULT_LENGTH);
        _spnStoMaPeriod.setValue(StochasticEngine.DEFAULT_AVG_PERIOD);
        _spnStoOversold.setValue(StochasticEngine.DEFAULT_OVERSOLD);
        _spnStoOverbought.setValue(StochasticEngine.DEFAULT_OVERBOUGHT);
    }

    //----- protected methods -----
    MacOption getMacSetting() {
        return new MacOption((Integer)_spnEntryMA1.getValue(), (Integer)_spnEntryMA2.getValue(),
            (Integer)_spnExitMA1.getValue(), (Integer)_spnExitMA2.getValue());
    }
    MzcOption getMzcSetting() {
        return new MzcOption((Integer)_spnFastMA.getValue(), (Integer)_spnSlowMA.getValue() /*, (Integer)_spnSignalLength.getValue()*/);
    }
    RsiOption getRsiSetting() {
        return new RsiOption((Integer)_spnRsiLength.getValue(),
            (Integer)_spnRsiOversold.getValue(), (Integer)_spnRsiOverbought.getValue());
    }
    StochasticOption getStoSetting() {
        return new StochasticOption((Integer)_spnStoLength.getValue(), (Integer) _spnStoMaPeriod.getValue(),
            (Integer)_spnStoOversold.getValue(), (Integer)_spnStoOverbought.getValue());
    }

    private JPanel createMacSettingPanel() {
        FormLayout layout = new FormLayout(
            "2dlu, pref, 5dlu, pref, 5dlu,  pref, 5dlu, pref, 5dlu, pref, 50dlu",//columns
            "2dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int col = 2, row = 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_spn_1"), cc.xyw(col, row, 4));

        row += 2;
        SpinnerNumberModel entry1_model = new SpinnerNumberModel(MacEngine._nEntryMA1, 1, 300, 1);
        builder.add(_spnEntryMA1 = new JSpinner(entry1_model), cc.xy(col + 2, row));
        SpinnerNumberModel entry2_model = new SpinnerNumberModel(MacEngine._nEntryMA2, 1, 300, 1);
        builder.add(_spnEntryMA2 = new JSpinner(entry2_model), cc.xy(col + 4, row));

        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_spn_3"), cc.xyw(col, row, 4));

        row += 2;
        SpinnerNumberModel exit1_model = new SpinnerNumberModel(MacEngine._nExitMA1, 1, 300, 1);
        builder.add(_spnExitMA1 = new JSpinner(exit1_model), cc.xy(col + 2, row));
        SpinnerNumberModel exit2_model = new SpinnerNumberModel(MacEngine._nExitMA2, 1, 300, 1);
        builder.add(_spnExitMA2 = new JSpinner(exit2_model), cc.xy(col + 4, row));
        return builder.getPanel();
    }

    private JPanel createMzcSettingPanel() {
        FormLayout layout = new FormLayout(
            "2dlu, pref, 5dlu, pref, 5dlu,  pref, 5dlu, pref, 5dlu, pref, 50dlu",//columns
            "2dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int col = 2, row = 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_spn_1"), cc.xy(col, row));
        SpinnerNumberModel slow_model = new SpinnerNumberModel(MacdZeroCrossEngine.FAST_MA, 1, 100, 1);
        builder.add(_spnFastMA = new JSpinner(slow_model), cc.xy(col + 2, row));

        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_spn_2"), cc.xy(col, row));
        SpinnerNumberModel fast_model = new SpinnerNumberModel(MacdZeroCrossEngine.SLOW_MA, 1, 200, 1);
        builder.add(_spnSlowMA = new JSpinner(fast_model), cc.xy(col + 2, row));

        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_spn_3"), cc.xy(col, row));
        SpinnerNumberModel sig_model = new SpinnerNumberModel(9, 1, 200, 1);
        builder.add(_spnSignalLength = new JSpinner(sig_model), cc.xy(col + 2, row));

        return builder.getPanel();
    }

    private JPanel createRsiSettingPanel() {
        FormLayout layout = new FormLayout(
            "2dlu, pref, 5dlu, pref, 5dlu,  pref, 5dlu, pref, 5dlu, pref, 50dlu",//columns
            "2dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int col = 2, row = 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_1"), cc.xy(col, row));
        SpinnerNumberModel len_model = new SpinnerNumberModel(14, 1, 100, 1);
        builder.add(_spnRsiLength = new JSpinner(len_model), cc.xy(col + 2, row));

        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_2"), cc.xy(col, row));
        SpinnerNumberModel osm = new SpinnerNumberModel(30, 1, 200, 1);
        builder.add(_spnRsiOversold = new JSpinner(osm), cc.xy(col + 2, row));

        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_3"), cc.xy(col, row));
        SpinnerNumberModel obm = new SpinnerNumberModel(70, 1, 200, 1);
        builder.add(_spnRsiOverbought = new JSpinner(obm), cc.xy(col + 2, row));

        return builder.getPanel();
    }

    private JPanel createStoSettingPanel() {
        FormLayout layout = new FormLayout(
            "2dlu, pref, 5dlu, pref, 5dlu,  pref, 5dlu, pref, 5dlu, pref, 50dlu",//columns
            "2dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int col = 2, row = 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sto_lbl_1"), cc.xy(col, row));
        SpinnerNumberModel len_model = new SpinnerNumberModel(14, 1, 100, 1);
        builder.add(_spnStoLength = new JSpinner(len_model), cc.xy(col + 2, row));

        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sto_lbl_2"), cc.xy(col, row));
        SpinnerNumberModel mam = new SpinnerNumberModel(3, 1, 50, 1);
        builder.add(_spnStoMaPeriod = new JSpinner(mam), cc.xy(col + 2, row));

        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_2"), cc.xy(col, row));
        SpinnerNumberModel osm = new SpinnerNumberModel(20, 1, 200, 1);
        builder.add(_spnStoOversold = new JSpinner(osm), cc.xy(col + 2, row));

        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_3"), cc.xy(col, row));
        SpinnerNumberModel obm = new SpinnerNumberModel(80, 1, 200, 1);
        builder.add(_spnStoOverbought = new JSpinner(obm), cc.xy(col + 2, row));

        return builder.getPanel();
    }

    private JSpinner _spnEntryMA1;
    private JSpinner _spnEntryMA2;
    private JSpinner _spnExitMA1;
    private JSpinner _spnExitMA2;

    private JSpinner _spnFastMA;
    private JSpinner _spnSlowMA;
    private JSpinner _spnSignalLength;

    private JSpinner _spnRsiLength;
    private JSpinner _spnRsiOversold;
    private JSpinner _spnRsiOverbought;

    private JSpinner _spnStoLength;
    private JSpinner _spnStoMaPeriod;//for %D
    private JSpinner _spnStoOversold;
    private JSpinner _spnStoOverbought;
}
