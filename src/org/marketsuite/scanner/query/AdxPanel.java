package org.marketsuite.scanner.query;

import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

class AdxPanel extends JPanel {
    AdxPanel() {
        setLayout(new MigLayout("insets 0, gap 0", "[grow]"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]5[][][]", "3[]3"));
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_54"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl.add(_fldPeriod);
        add(ttl, "dock north");
        add(_chkTrendLevel, "split"); add(_fldTrendLevel, "wrap");
        add(_chkPlusCrossAboveMinus, "wrap");
        add(_chkMinusCrossAbovePlus, "wrap");
        add(_chkAdxTurnDown, "wrap");
        add(_chkPlusCrossBelowAdx);
        for (Component cmp : this.getComponents())
            cmp.setEnabled(false);//TODO enable after implementation

//        _chkAdxTurnDown.setEnabled(false);
//        _chkMinusCrossAbovePlus.setEnabled(false);
//        _chkPlusCrossAboveMinus.setEnabled(false);
//        _chkPlusCrossBelowAdx.setEnabled(false);
//        _chkTrendLevel.setEnabled(false);
    }

    private LongIntegerField _fldPeriod = new LongIntegerField(14, 3, 1, 500);
    private JCheckBox _chkTrendLevel = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_49"));
    private LongIntegerField _fldTrendLevel = new LongIntegerField(20, 3, 1, 500);
    private JCheckBox _chkPlusCrossAboveMinus = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_50"));
    private JCheckBox _chkMinusCrossAbovePlus = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_51"));
    private JCheckBox _chkAdxTurnDown = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_52"));
    private JCheckBox _chkPlusCrossBelowAdx = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_53"));
}
