package org.marketsuite.simulator.indicator.macd;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Small container with various MAC options used with IndicatorPanel
 */
public class MacdOptionPanel extends JPanel {
    public MacdOptionPanel() {
        setOpaque(false);
        ((FlowLayout)getLayout()).setVgap(0);//this remove excess spaces vertically
        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_spn_1")));
        SpinnerNumberModel slow_model = new SpinnerNumberModel(MacdZeroCrossEngine.FAST_MA, 1, 100, 1);
        add(_spnFastMA = new JSpinner(slow_model));
        add(Box.createGlue());

        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_spn_2")));
        SpinnerNumberModel fast_model = new SpinnerNumberModel(MacdZeroCrossEngine.SLOW_MA, 1, 200, 1);
        add(_spnSlowMA = new JSpinner(fast_model));
        add(Box.createGlue());

        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mzc_spn_3")));
        SpinnerNumberModel sig_model = new SpinnerNumberModel(9, 1, 200, 1);
        add(_spnSignalLength = new JSpinner(sig_model));
        add(Box.createHorizontalStrut(100));

        add(_chkLongTrade); _chkLongTrade.setOpaque(false); _chkLongTrade.setSelected(true);
        add(_chkShortTrade); _chkShortTrade.setOpaque(false);
//TODO fix short bug before allow public use
        _chkShortTrade.setEnabled(false);
    }

    public int getFastMA() { return (Integer)_spnFastMA.getValue(); }
    public int getSlowMA() { return (Integer)_spnSlowMA.getValue(); }
    public int getSignalLength() { return (Integer)_spnSignalLength.getValue(); }
    public boolean isLong() { return _chkLongTrade.isSelected(); }
    public boolean isShort() { return _chkShortTrade.isSelected(); }

    //-----instance variables-----
    private JSpinner _spnFastMA;
    private JSpinner _spnSlowMA;
    private JSpinner _spnSignalLength;
    private JCheckBox _chkLongTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_1"));
    private JCheckBox _chkShortTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_2"));
}
