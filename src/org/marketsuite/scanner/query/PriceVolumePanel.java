package org.marketsuite.scanner.query;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PriceVolumePanel extends JPanel {
    PriceVolumePanel() {
        setLayout(new MigLayout("insets 0", "[grow]"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]push", "3[]3"));
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_18"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        add(ttl, "dock north");
        add(_chkNoEtf, "wrap");
        add(_chkPrice);
        _chkPrice.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enablePriceRange();
            }
        });
        add(_fldPriceLow); _fldPriceLow.setText("10");
        add(new JLabel(" ~ "), "split");
        add(_fldPriceHigh, "wrap"); _fldPriceHigh.setText("1200");
        add(_chkVolume);
        _chkVolume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableVolume();
            }
        });
        add(_fldVolumeThreshold);
        add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_14")), "wrap");
        add(_chkOffHigh);// _chkOffHigh.setEnabled(false);
        _chkOffHigh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableOffHigh();
            }
        });
        add(_fldOffHigh); _fldOffHigh.setText("10");
        add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_27")), "wrap");
        add(_chkAth, "span 3"); _chkAth.setEnabled(false);//TODO remove
        enablePriceRange(); enableVolume(); enableOffHigh();//enable/disable fields
    }

    boolean isNoEtf() { return _chkNoEtf.isSelected(); }
    boolean isPriceInRange(float price) {
        if (!_chkPrice.isSelected()) return true;//don't check range
        if (price > _fldPriceLow.getValue() && price < _fldPriceHigh.getValue()) return true;
        return false;
    }
    boolean isVolumeSufficient(int volume) {
        if (!_chkVolume.isSelected()) return true;//don't check
        if (volume > _fldVolumeThreshold.getValue() * 1000) return true;
        return false;
    }
    boolean isOffHigh(float price, float high) {
        if (!_chkOffHigh.isSelected()) return true;
        double pct_off = _fldOffHigh.getValue();
        float pct = 100 * (high - price) / high;
        return pct < pct_off;
    }

    //----- private methods -----
    private void enablePriceRange() {
        boolean ckp = _chkPrice.isSelected();
        _fldPriceLow.setEnabled(ckp);
        _fldPriceHigh.setEnabled(ckp);
    }
    private void enableVolume() {
        boolean vck = _chkVolume.isSelected();
        _fldVolumeThreshold.setEnabled(vck);
    }
    private void enableOffHigh() {
        boolean oh = _chkOffHigh.isSelected();
        _fldOffHigh.setEnabled(oh);
    }

    //----- variables -----
    private JCheckBox _chkNoEtf = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_noetf"));
    private JCheckBox _chkPrice = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_10"));
    private DecimalField _fldPriceLow = new DecimalField(10, 3, 0, 5000, null);
    private DecimalField _fldPriceHigh = new DecimalField(350, 5, 0, 5000, null);
    private JCheckBox _chkVolume = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_11"));
    private LongIntegerField _fldVolumeThreshold = new LongIntegerField(100, 4, 1, 1000);
    private JCheckBox _chkOffHigh = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_26"));
    private DecimalField _fldOffHigh = new DecimalField(10, 4, 0, 100, null);
    private JCheckBox _chkAth = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_28"));
}
