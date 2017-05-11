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
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

class DstoWstoPanel extends JPanel {
    DstoWstoPanel(boolean dsto) {
        _bDsto = dsto;
        setLayout(new MigLayout("insets 0, gap 0, wrap 2", "[grow]"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]5[][][]", "3[]3"));
        JLabel lbl = new JLabel(dsto ? ApolloConstants.APOLLO_BUNDLE.getString("qp_40") :
            ApolloConstants.APOLLO_BUNDLE.getString("qp_48"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl.add(_fldPeriod);
        ttl.add(_fldSmoothMa);
        add(ttl, "dock north");
        add(_chkOverbought); add(_fldOverboughtLevel);
        add(_chkOverSold); add(_fldOversoldLevel);
        add(_chkCrossAbove); add(_fldCrossAboveLevel);
        add(_chkCrossBelow); add(_fldCrossBelowLevel);
        add(_chkBewteen, "split 2"); add(_fldLowerRange); add(_fldUpperRange);
        add(_chkCrossAboveSigline, "span 2, wrap");
        add(_chkCrossBelowSigline, "span 2");

        if (!dsto) {
            _fldPeriod.setText("5");//5 week for WSTO
        for (Component cmp : this.getComponents())
            cmp.setEnabled(false);//TODO enable after implementation
        }
    }

    //----- protected methods -----
    void updateDsto(MarketInfo mki) {
        MarketUtil.updateDsto(mki, (int)_fldPeriod.getValue(), (int)_fldSmoothMa.getValue());
    }
    boolean isDstoOverbought(float dsto) {
        if (!_bDsto || !_chkOverbought.isSelected()) return true;//ignore this filter
        return dsto > _fldOverboughtLevel.getValue();
    }
    boolean isDstoOversold(float dsto) {
        if (!_bDsto || !_chkOverSold.isSelected()) return true;//ignore this filter
        return dsto < _fldOversoldLevel.getValue();
    }
    boolean isDstoCrossAbove(float dsto1, float dsto2) {
        if (!_bDsto || !_chkCrossAbove.isSelected()) return true;//ignore this filter
        return (dsto1 <= _fldCrossAboveLevel.getValue()) && (dsto2 > _fldCrossAboveLevel.getValue());
    }
    boolean isDstoCrossBelow(float dsto1, float dsto2) {
        if (!_bDsto || !_chkCrossBelow.isSelected()) return true;//ignore this filter
        return (dsto1 >= _fldCrossBelowLevel.getValue()) && (dsto2 < _fldCrossBelowLevel.getValue());
    }
    boolean isDstoBetween(float dsto) {
        if (!_bDsto || !_chkBewteen.isSelected()) return true;//ignore this filter
        return (dsto >= _fldLowerRange.getValue()) && (dsto <= _fldUpperRange.getValue());
    }

    //----- variables -----
    private boolean _bDsto;
    private LongIntegerField _fldPeriod = new LongIntegerField(14, 3, 1, 500);
    private LongIntegerField _fldSmoothMa = new LongIntegerField(3, 3, 1, 500);
    private JCheckBox _chkOverbought = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_41"));
    private LongIntegerField _fldOverboughtLevel = new LongIntegerField(80, 3, 1, 500);
    private JCheckBox _chkOverSold = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_42"));
    private LongIntegerField _fldOversoldLevel = new LongIntegerField(20, 3, 1, 500);
    private JCheckBox _chkCrossAbove = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_43"));
    private LongIntegerField _fldCrossAboveLevel = new LongIntegerField(50, 3, 1, 500);
    private JCheckBox _chkCrossBelow = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_44"));
    private LongIntegerField _fldCrossBelowLevel = new LongIntegerField(50, 3, 1, 500);
    private JCheckBox _chkBewteen = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_45"));
    private LongIntegerField _fldLowerRange = new LongIntegerField(20, 3, 1, 500);
    private LongIntegerField _fldUpperRange = new LongIntegerField(50, 3, 1, 500);
    private JCheckBox _chkCrossAboveSigline = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_46"));
    private JCheckBox _chkCrossBelowSigline = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_47"));
}
