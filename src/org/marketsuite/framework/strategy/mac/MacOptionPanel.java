package org.marketsuite.framework.strategy.mac;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;
//todo substitute basic mode mac panel parameter  with this class...
/**
 * Small container with various MAC options used with IndicatorPanel
 */
public class MacOptionPanel extends JPanel {
    public MacOptionPanel() {
        setOpaque(false);
        ((FlowLayout)getLayout()).setVgap(0);//this remove excess spaces vertically
        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_spn_1")));
        SpinnerNumberModel entry1_model = new SpinnerNumberModel(MacEngine._nEntryMA1, 1, 300, 1);
        add(_spnEntryMA1 = new JSpinner(entry1_model));
        SpinnerNumberModel entry2_model = new SpinnerNumberModel(MacEngine._nEntryMA2, 1, 300, 1);
        add(_spnEntryMA2 = new JSpinner(entry2_model));
        add(Box.createGlue());

        //two more spinners for exit
        add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_spn_3")));
        SpinnerNumberModel exit1_model = new SpinnerNumberModel(MacEngine._nEntryMA2, 1, 300, 1);
        add(_spnExitMA1 = new JSpinner(exit1_model));
        SpinnerNumberModel exit2_model = new SpinnerNumberModel(MacEngine._nExitMA2, 1, 300, 1);
        add(_spnExitMA2 = new JSpinner(exit2_model));
        add(_chkLongTrade); _chkLongTrade.setOpaque(false); _chkLongTrade.setSelected(true);
        add(_chkShortTrade); _chkShortTrade.setOpaque(false);
//TODO fix short bug before allow public use
        _chkShortTrade.setEnabled(false);
    }

    public int getEntryMA1() {
        return (Integer)_spnEntryMA1.getValue();
    }

    public int getEntryMA2() {
        return (Integer)_spnEntryMA2.getValue();
    }

    public int getExitMA1() {
        return (Integer)_spnExitMA1.getValue();
    }

    public int getExitMA2() {
        return (Integer)_spnExitMA2.getValue();
    }

    public boolean isLong() { return _chkLongTrade.isSelected(); }
    public boolean isShort() { return _chkShortTrade.isSelected(); }

    //-----instance variables-----
    private JSpinner _spnEntryMA1;
    private JSpinner _spnEntryMA2;
    private JSpinner _spnExitMA1;
    private JSpinner _spnExitMA2;
    private JCheckBox _chkLongTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_1"));
    private JCheckBox _chkShortTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_2"));
}
