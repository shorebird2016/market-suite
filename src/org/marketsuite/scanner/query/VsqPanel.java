package org.marketsuite.scanner.query;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

//VSQ - Volatility Squeeze
class VsqPanel extends JPanel {
    VsqPanel() {
        setLayout(new MigLayout("insets 0, gap 0", "[grow]"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]5[][][]", "3[]3"));
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_68"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl.add(_fldMa);
        ttl.add(_fldUpperBound);
        ttl.add(_fldLowerBound);
        add(ttl, "dock north");
        add(_chkSqueeze, "wrap");
        _chkSqueeze.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fldSqueezeThreshold.setEnabled(_chkSqueeze.isSelected());
            }
        });
        add(_fldSqueezeThreshold, "center, split"); _fldSqueezeThreshold.setText("6"); _fldSqueezeThreshold.setEnabled(false);
        add(new JLabel("%"));
    }

    //----- protected methods -----
    // macd1 = older, macd2 = newer
    boolean isSqueezed(ArrayList<FundQuote> quotes) {
        BollingerBand bb = new BollingerBand((int)_fldMa.getValue(), (int)_fldUpperBound.getValue(),
            (int)_fldLowerBound.getValue(), quotes);
        float pct = bb.getBandwidth()[0];
        boolean vsq = pct < _fldSqueezeThreshold.getValue() / 100;
        return !_chkSqueeze.isSelected() || (vsq && _chkSqueeze.isSelected());
    }
    boolean isSqueezed(MarketInfo mki) {
        float bw = mki.getBollingerBand().getBandwidth()[0];
        boolean vsq = bw < (_fldSqueezeThreshold.getValue() / 100);
        return !_chkSqueeze.isSelected() || (vsq && _chkSqueeze.isSelected());
    }

    void recalcBb(MarketInfo mki) {
        mki.recalcBollingerBand((int)_fldMa.getValue(), (int)_fldUpperBound.getValue(),
            (int)_fldLowerBound.getValue());
    }

    //----- variables -----
    private DecimalField _fldSqueezeThreshold = new DecimalField(16, 5, 0, 100, null);
    private LongIntegerField _fldMa = new LongIntegerField(20, 3, 1, 500);
    private LongIntegerField _fldUpperBound = new LongIntegerField(2, 3, 1, 5);
    private LongIntegerField _fldLowerBound = new LongIntegerField(2, 3, 1, 5);
    private JCheckBox _chkSqueeze = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_67"));
}
