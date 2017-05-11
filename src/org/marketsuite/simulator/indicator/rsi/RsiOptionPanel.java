package org.marketsuite.simulator.indicator.rsi;

import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Small container with various MAC options used with IndicatorPanel
 */
public class RsiOptionPanel extends JPanel {
    public RsiOptionPanel() {
        setOpaque(false);
        ((FlowLayout)getLayout()).setVgap(0);//this remove excess spaces vertically
        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_1")));
        SpinnerNumberModel len_model = new SpinnerNumberModel(14, 1, 100, 1);
        add(_spnLength = new JSpinner(len_model));
        add(Box.createGlue());

        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_2")));
        SpinnerNumberModel osm = new SpinnerNumberModel(30, 1, 200, 1);
        add(_spnOversold = new JSpinner(osm));
        add(Box.createGlue());

        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rsi_lbl_3")));
        SpinnerNumberModel obm = new SpinnerNumberModel(70, 1, 200, 1);
        add(_spnOverbought = new JSpinner(obm));
        add(Box.createHorizontalStrut(100));

        add(_chkLongTrade); _chkLongTrade.setOpaque(false); _chkLongTrade.setSelected(true);
        add(_chkShortTrade); _chkShortTrade.setOpaque(false);
//TODO fix short bug before allow public use
        _chkShortTrade.setEnabled(false);
    }

    public int getLength() { return (Integer) _spnLength.getValue(); }
    public int getOversoldLevel() { return (Integer) _spnOversold.getValue(); }
    public int getOverboughtLevel() { return (Integer) _spnOverbought.getValue(); }
    public boolean isLong() { return _chkLongTrade.isSelected(); }
    public boolean isShort() { return _chkShortTrade.isSelected(); }

    public RsiOption getOptions() {
        return new RsiOption(getLength(), getOversoldLevel(), getOverboughtLevel());
    }

    //-----instance variables-----
    private JSpinner _spnLength;
    private JSpinner _spnOversold;
    private JSpinner _spnOverbought;
    private JCheckBox _chkLongTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_1"));
    private JCheckBox _chkShortTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_2"));
}
