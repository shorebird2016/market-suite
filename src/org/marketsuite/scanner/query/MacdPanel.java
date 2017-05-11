package org.marketsuite.scanner.query;

import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;

class MacdPanel extends JPanel {
    MacdPanel() {
        setLayout(new MigLayout("insets 0, flowy, gap 0", "[grow]"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]5[][][]", "3[]3"));
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_35"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl.add(_fldFastMa);
        ttl.add(_fldSlowMa);
        ttl.add(_fldSmoothMa);
        add(ttl, "dock north");
        add(_chkZeroCrossUp);
        add(_chkZeroCrossDn);
        add(_chkSigCrossUp);
        add(_chkSigCrossDn);
    }

    //----- protected methods -----
    // macd1 = older, macd2 = newer
    boolean isZeroCrossUp(float macd1, float macd2) {
        return !_chkZeroCrossUp.isSelected() || (macd2 > 0 && macd1 <= 0 && _chkZeroCrossUp.isSelected());
    }
    boolean isZeroCrossDown(float macd1, float macd2) {
        return !_chkZeroCrossDn.isSelected() || (macd2 < 0 && macd1 >= 0 && _chkZeroCrossDn.isSelected());
    }
    boolean isSigCrossUp(float macd1, float macd2, float macd_sig1, float macd_sig2) {
        return !_chkSigCrossUp.isSelected() || (macd1 <= macd_sig1 && macd2 > macd_sig2);
    }
    boolean isSigCrossDn(float macd1, float macd2, float macd_sig1, float macd_sig2) {
        return !_chkSigCrossDn.isSelected() || (macd1 >= macd_sig1 && macd2 < macd_sig2);
    }
    void updateMacd(MarketInfo mki) {
        MarketUtil.updateMacd(mki, (int) _fldFastMa.getValue(), (int) _fldSlowMa.getValue(), (int) _fldSmoothMa.getValue());
    }

    //----- variables -----
    private LongIntegerField _fldFastMa = new LongIntegerField(12, 3, 1, 500);
    private LongIntegerField _fldSlowMa = new LongIntegerField(26, 3, 1, 500);
    private LongIntegerField _fldSmoothMa = new LongIntegerField(9, 3, 1, 500);
    private JCheckBox _chkZeroCrossUp = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_36"));
    private JCheckBox _chkZeroCrossDn = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_37"));
    private JCheckBox _chkSigCrossUp = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_38"));
    private JCheckBox _chkSigCrossDn = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_39"));
}
