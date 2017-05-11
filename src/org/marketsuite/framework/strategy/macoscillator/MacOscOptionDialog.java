package org.marketsuite.framework.strategy.macoscillator;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A floating window for configuring simulation options of Mac + Oscillator type.
 */
class MacOscOptionDialog extends JDialog {
    //singleton CTOR
    private static MacOscOptionDialog _Instance;
    public static MacOscOptionDialog getInstance() {
        if (_Instance == null)
            _Instance = new MacOscOptionDialog();
        return _Instance;
    }
    /**
     * CTOR: create this dialog
     */
    private MacOscOptionDialog() {
        super(/*MainFrame.getInstance()*/(JFrame)null, false);//non-modal, use this to keep dialog always in front
        setTitle(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_1"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
        
        //2 sections - entry, exit
        Box cen_pnl = Box.createVerticalBox(); cen_pnl.setOpaque(false);
        cen_pnl.setBorder(new TitledBorder(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_10")));
        cen_pnl.add(createMacParamPanel());
        cen_pnl.add(createDstoParamPanel());
        cen_pnl.add(createWstoParamPanel());
        content_pnl.add(cen_pnl, BorderLayout.CENTER);
        Box south_pnl = Box.createVerticalBox();  south_pnl.setOpaque(false);
        south_pnl.setBorder(new TitledBorder(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_11")));
        south_pnl.add(createExitParamPanel());
        content_pnl.add(south_pnl, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                dispose();
            }
        });
        setContentPane(content_pnl);
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), true, null/*MainFrame.getInstance()*/,
                WindowConstants.DISPOSE_ON_CLOSE);
    }

    //----- protected methods -----
    MacOscillatorOption getOptions() {
        return new MacOscillatorOption(
            _cmbMa.getSelectedIndex() == 1,
            (Integer)_spnEntryFastMa.getValue(),
            (Integer)_spnEntrySlowMa.getValue(),
            _cmbDsto.getSelectedIndex() == 1,
            (Integer)_spnDstoParam1.getValue(),
            (Integer)_spnDstoParam2.getValue(),
            _chkWeekly.isSelected(),
            _cmbWsto.getSelectedIndex() == 1,
            (Integer)_spnWstoParam1.getValue(),
            (Integer)_spnWstoParam2.getValue(),
            _cmbExitMa.getSelectedIndex() == 1,
            (Integer)_spnExitMaLength.getValue()
        );
    }

    //several methods to create parameter panels below graph
    private JPanel createMacParamPanel() {
        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ret.add(Box.createHorizontalStrut(10));
        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_5")));
        ret.add(_cmbMa);
        SpinnerNumberModel sp_model = new SpinnerNumberModel(MacOscillatorEngine._nEntryMA1, 1, 100, 1);
        ret.add(_spnEntrySlowMa = new JSpinner(sp_model));
        sp_model = new SpinnerNumberModel(MacOscillatorEngine._nEntryMA2, 1, 200, 1);
        ret.add(_spnEntryFastMa = new JSpinner(sp_model));
        return ret;
    }
    private JPanel createDstoParamPanel() {
        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ret.add(Box.createHorizontalStrut(10));
        ret.add(_chkDaily); _chkDaily.setEnabled(false);
        _chkDaily.setSelected(true);
        ret.add(Box.createHorizontalStrut(5));
        ret.add(_cmbDsto);
        SpinnerNumberModel sp_model = new SpinnerNumberModel(5, 1, 100, 1);
        ret.add(_spnDstoParam1 = new JSpinner(sp_model));
        sp_model = new SpinnerNumberModel(3, 1, 100, 1);
        ret.add(_spnDstoParam2 = new JSpinner(sp_model));
        return ret;
    }
    private JPanel createWstoParamPanel() {
        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ret.add(Box.createHorizontalStrut(10));
        ret.add(_chkWeekly);
        ret.add(_cmbWsto);
        SpinnerNumberModel sp_model = new SpinnerNumberModel(5, 1, 100, 1);
        ret.add(_spnWstoParam1 = new JSpinner(sp_model));
        sp_model = new SpinnerNumberModel(3, 1, 100, 1);
        ret.add(_spnWstoParam2 = new JSpinner(sp_model));
        return ret;
    }
    private JPanel createExitParamPanel() {
        JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ret.add(Box.createHorizontalStrut(10));
        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_8") + " "));
        SpinnerNumberModel sp_model = new SpinnerNumberModel(30, 1, 100, 1);
        ret.add(_spnExitMaLength = new JSpinner(sp_model));
        sp_model = new SpinnerNumberModel(3, 1, 100, 1);
        ret.add(_cmbExitMa);
        return ret;
    }

    //----- instance variables -----
    private JComboBox _cmbMa = new JComboBox(LIST_MA_TYPE);
    private JSpinner _spnEntrySlowMa, _spnEntryFastMa;
    private JCheckBox _chkDaily = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_6"));
    private JComboBox _cmbDsto = new JComboBox(LIST_STO_TYPE);
    private JSpinner _spnDstoParam1, _spnDstoParam2;
    private JCheckBox _chkWeekly = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_7"));
    private JComboBox _cmbWsto = new JComboBox(LIST_STO_TYPE);
    private JSpinner _spnWstoParam1, _spnWstoParam2;
    private JSpinner _spnExitMaLength;
    private JComboBox _cmbExitMa = new JComboBox(LIST_MA_TYPE);

    //----- literals -----
    public static final String[] LIST_MA_TYPE = { "SMA", "EMA" };
    public static final String[] LIST_STO_TYPE = { "Slow Stochastic", "Fast Stochastic" };
}