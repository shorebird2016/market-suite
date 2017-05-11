package org.marketsuite.scanner.query;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MovingAveragePanel extends JPanel {
    MovingAveragePanel() {
        setLayout(new MigLayout("insets 0, wrap 3, gap 0", "5[]5[]5[]push"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]push", "3[]3"));
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_34"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        add(ttl, "dock north");
        add(_chk10ma);  add(_fld10maPct); add(new JLabel("%"));    _fld10maPct.setText("3");
        add(_chk20ma);  add(_fld20maPct); add(new JLabel("%"));    _fld20maPct.setText("3");
        add(_chk30ma);  add(_fld30maPct); add(new JLabel("%"));    _fld30maPct.setText("3");
        add(_chk50ma);  add(_fld50maPct); add(new JLabel("%"), "split 3");    _fld50maPct.setText("3");
            add(_chkAbove50Sma); add(_chkBelow50Sma); _chkAbove50Sma.setSelected(true); _chkBelow50Sma.setSelected(true);
        add(_chk200ma); add(_fld200maPct); add(new JLabel("%"));   _fld200maPct.setText("3");
        add(_chk50ema); add(_fld50EmaPct); add(new JLabel("%"));   _fld50EmaPct.setText("3");
        add(_chk120ema); add(_fld120EmaPct); add(new JLabel("%")); _fld120EmaPct.setText("3");
        add(_chk200ema); add(_fld200EmaPct); add(new JLabel("%")); _fld200EmaPct.setText("3");
        _chk10ma.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fld10maPct.setEnabled(_chk10ma.isSelected());
            }
        });
        _chk20ma.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { _fld20maPct.setEnabled(_chk20ma.isSelected()); } });
        _chk30ma.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { _fld30maPct.setEnabled(_chk30ma.isSelected()); } });
        _chk50ma.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
            boolean sel = _chk50ma.isSelected();
            _fld50maPct.setEnabled(sel); _chkAbove50Sma.setEnabled(sel); _chkBelow50Sma.setEnabled(sel);
        } });
        _chkAbove50Sma.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!_chkAbove50Sma.isSelected() && !_chkBelow50Sma.isSelected())
                    _chkAbove50Sma.setSelected(true);//force at least 1 selection
            }
        });
        _chkBelow50Sma.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!_chkAbove50Sma.isSelected() && !_chkBelow50Sma.isSelected())
                    _chkBelow50Sma.setSelected(true);//force at least 1 selection
            }
        });
        _chk200ma.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { _fld200maPct.setEnabled(_chk200ma.isSelected()); } });
        _chk50ema.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { _fld50EmaPct.setEnabled(_chk50ema.isSelected()); } });
        _chk120ema.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { _fld120EmaPct.setEnabled(_chk120ema.isSelected()); } });
        _chk200ema.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { _fld200EmaPct.setEnabled(_chk200ema.isSelected()); } });
        enableAll();
    }

    //----- protected methods -----
    boolean is10MaInRange(float ma_pct) { return _chk10ma.isSelected() && ma_pct < _fld10maPct.getValue() || !_chk10ma.isSelected(); }
    boolean is20MaInRange(float ma_pct) { return _chk20ma.isSelected() && ma_pct < _fld20maPct.getValue() || !_chk20ma.isSelected(); }
    boolean is30MaInRange(float ma_pct) { return _chk30ma.isSelected() && ma_pct < _fld30maPct.getValue() || !_chk30ma.isSelected(); }
    boolean is50MaInRange(float ma_pct) { return _chk50ma.isSelected() && ma_pct < _fld50maPct.getValue() || !_chk50ma.isSelected(); }
    boolean is200MaInRange(float ma_pct) { return _chk200ma.isSelected() && ma_pct < _fld200maPct.getValue() || !_chk200ma.isSelected(); }
    boolean is50EmaInRange(float ma_pct) { return _chk50ema.isSelected() && ma_pct < _fld50EmaPct.getValue() || !_chk50ema.isSelected(); }
    boolean is120EmaInRange(float ma_pct) { return _chk120ema.isSelected() && ma_pct < _fld120EmaPct.getValue() || !_chk120ema.isSelected(); }
    boolean is200EmaInRange(float ma_pct) { return _chk200ema.isSelected() && ma_pct < _fld200EmaPct.getValue() || !_chk200ema.isSelected(); }
    boolean is50SmaPass(float cur_price, float sma_50) {
        if (!_chk50ma.isSelected()) return true;//inactive
        float spec = (float)_fld50maPct.getValue();
        if (_chkAbove50Sma.isSelected()) {//greater or equal 50SMA
            float pct = 100 * (cur_price - sma_50) / sma_50;
            if (pct >= 0 && pct <= spec) return true;//otherwise check next condition
        }
        if (_chkBelow50Sma.isSelected()) {//below 50SMA
            float pct = 100 * (sma_50 - cur_price) / cur_price;
            return pct > 0 && pct <= spec;
        }
        return false;
    }

    //----- private methods -----
    private void enableAll() {
        _fld10maPct.setEnabled(_chk10ma.isSelected());
        _fld20maPct.setEnabled(_chk20ma.isSelected());
        _fld30maPct.setEnabled(_chk30ma.isSelected());
        boolean sma50_sel = _chk50ma.isSelected();
        _fld50maPct.setEnabled(sma50_sel); _chkAbove50Sma.setEnabled(sma50_sel); _chkBelow50Sma.setEnabled(sma50_sel);
        _fld200maPct.setEnabled(_chk200ma.isSelected());
        _fld50EmaPct.setEnabled(_chk50ema.isSelected());
        _fld120EmaPct.setEnabled(_chk120ema.isSelected());
        _fld200EmaPct.setEnabled(_chk200ema.isSelected());
    }

    //----- variables -----
    private JCheckBox _chk10ma = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_29"));
    private JCheckBox _chk20ma = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_30"));
    private JCheckBox _chk30ma = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_31"));
    private JCheckBox _chk50ma = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_32"));
    private JCheckBox _chk200ma = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_33"));
    private JCheckBox _chk50ema = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_64"));
    private JCheckBox _chk120ema = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_65"));
    private JCheckBox _chk200ema = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_66"));
    private DecimalField _fld10maPct = new DecimalField(10, 3, 0, 100, null);
    private DecimalField _fld20maPct = new DecimalField(10, 3, 0, 100, null);
    private DecimalField _fld30maPct = new DecimalField(10, 3, 0, 100, null);
    private DecimalField _fld50maPct = new DecimalField(10, 3, 0, 100, null);
    private DecimalField _fld200maPct = new DecimalField(10, 3, 0, 100, null);
    private DecimalField _fld50EmaPct = new DecimalField(10, 3, 0, 100, null);
    private DecimalField _fld120EmaPct = new DecimalField(10, 3, 0, 100, null);
    private DecimalField _fld200EmaPct = new DecimalField(10, 3, 0, 100, null);
    private JCheckBox _chkAbove50Sma = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_above"));
    private JCheckBox _chkBelow50Sma = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_below"));
}
