package org.marketsuite.simulator.indicator.stochastic;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Container for Stochastic parameters.
 */
public class StochasticOptionPanel extends JPanel {
    public StochasticOptionPanel() {
        setOpaque(false);
        ((FlowLayout)getLayout()).setVgap(0);//this remove excess spaces vertically
        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sto_lbl_1")));
        SpinnerNumberModel len_model = new SpinnerNumberModel(14, 1, 100, 1);
        add(_spnLength = new JSpinner(len_model));
        add(Box.createGlue());

        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sto_lbl_2")));
        SpinnerNumberModel mam = new SpinnerNumberModel(3, 1, 50, 1);
        add(_spnMaPeriod = new JSpinner(mam));
        add(Box.createGlue());

        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_2")));
        SpinnerNumberModel osm = new SpinnerNumberModel(20, 1, 200, 1);
        add(_spnOversold = new JSpinner(osm));
        add(Box.createGlue());

        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_3")));
        SpinnerNumberModel obm = new SpinnerNumberModel(80, 1, 200, 1);
        add(_spnOverbought = new JSpinner(obm));
        add(Box.createHorizontalStrut(20));

        add(_chkLongTrade); _chkLongTrade.setOpaque(false); _chkLongTrade.setSelected(true);
        add(_chkShortTrade); _chkShortTrade.setOpaque(false);
//TODO fix short bug before allow public use
        _chkShortTrade.setEnabled(false);
    }

    public int getLength() { return (Integer) _spnLength.getValue(); }
    public int getMaPeriod() { return (Integer) _spnMaPeriod.getValue(); }
    public int getOversoldLevel() { return (Integer) _spnOversold.getValue(); }
    public int getOverboughtLevel() { return (Integer) _spnOverbought.getValue(); }
    public boolean isLong() { return _chkLongTrade.isSelected(); }
    public boolean isShort() { return _chkShortTrade.isSelected(); }
    public StochasticOption getOptions() {
        return new StochasticOption(getLength(), getMaPeriod(), getOversoldLevel(), getOverboughtLevel());
    }

    //-----instance variables-----
    private JSpinner _spnLength;
    private JSpinner _spnMaPeriod;//for %D
    private JSpinner _spnOversold;
    private JSpinner _spnOverbought;
    private JCheckBox _chkLongTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_1"));
    private JCheckBox _chkShortTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_2"));
}