package org.marketsuite.framework.strategy.lsquare;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimOptionDialog extends JDialog {
    public SimOptionDialog() {
        super(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("l2_ttlopt"), false);
        JPanel content = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0")); content.setOpaque(false);
        content.setBorder(new BevelBorder(BevelBorder.LOWERED));
        setContentPane(content);

        //various options
        content.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("l2_ratingthresh")), "gapx 25, split");
        content.add(_fldRatingThreshold);
        content.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("l2_highrating")), "gapx 50, split");
        content.add(_fldHighRating, "wrap");
        content.add(_chkBuyEqComposite, "wrap"); _chkBuyEqComposite.setOpaque(false);
        content.add(_chkBuyEqRs, "wrap"); _chkBuyEqRs.setOpaque(false);
        content.add(_chkSellEqCr, "wrap"); _chkSellEqCr.setOpaque(false);

        //small panel for 3 items, straight layout not easy
        JPanel pnl = new JPanel(new MigLayout("insets 0")); pnl.setOpaque(false);//use pnl to layout nicer
        pnl.add(_chkCostStop); pnl.add(_fldCostStop); pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("l2_cost2")));
        content.add(pnl, "wrap"); _chkCostStop.setOpaque(false); _fldCostStop.setEnabled(false);
        _chkCostStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _fldCostStop.setEnabled(_chkCostStop.isSelected()); }
        });

        //small panel for 3 items, straight layout not easy
        pnl = new JPanel(new MigLayout("insets 0")); pnl.setOpaque(false);//use pnl to layout nicer
        pnl.add(_chkWeekDrop); pnl.add(_fldWeekDrop); pnl.add(new JLabel("%"));
        content.add(pnl, "wrap"); _chkWeekDrop.setOpaque(false); _fldWeekDrop.setEnabled(false);
        _chkWeekDrop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _fldWeekDrop.setEnabled(_chkWeekDrop.isSelected()); }
        });

        //small panel for 3 items, straight layout not easy
        pnl = new JPanel(new MigLayout("insets 0")); pnl.setOpaque(false);//use pnl to layout nicer
        pnl.add(_chkHighStop); pnl.add(_fldHighStop); pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("l2_highstop")));
        content.add(pnl, "wrap"); _chkHighStop.setOpaque(false); _fldHighStop.setEnabled(false); _chkHighStop.setEnabled(false);
        _chkHighStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fldHighStop.setEnabled(_chkHighStop.isSelected());
            }
        });
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), true, MdiMainFrame.getInstance(), WindowConstants.HIDE_ON_CLOSE, false);
    }

    public LSquareParam getOptions() {
        return new LSquareParam(
            (int)_fldRatingThreshold.getValue(), (int)_fldHighRating.getValue(), _chkBuyEqComposite.isSelected(),
            _chkBuyEqRs.isSelected(), _chkSellEqCr.isSelected(),
            _chkCostStop.isSelected(), (float)_fldCostStop.getValue(), _chkWeekDrop.isSelected(), (float)_fldWeekDrop.getValue(),
            _chkHighStop.isSelected(), (float)_fldHighStop.getValue()
        );
    }

    //----- variables -----
    private LongIntegerField _fldRatingThreshold = new LongIntegerField(50, 3, 1, 99);
    private LongIntegerField _fldHighRating = new LongIntegerField(95, 3, 1, 99);//if greater than this, ignore its up/down movement
    private JCheckBox _chkBuyEqComposite = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("l2_eqcomp"));
    private JCheckBox _chkBuyEqRs = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("l2_eqrs"));
    private JCheckBox _chkSellEqCr = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("l2_eqcr2"));
    private JCheckBox _chkWeekDrop = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("l2_wkdrop"));
    private DecimalField _fldWeekDrop = new DecimalField(10, 5, 0, 50, null);
    private JCheckBox _chkCostStop = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("l2_cost1"));
    private DecimalField _fldCostStop = new DecimalField(3, 5, 0, 50, null);
    private JCheckBox _chkHighStop = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("l2_sellrule"));
    private DecimalField _fldHighStop = new DecimalField(15, 5, 0, 50, null);
    private JCheckBox _chkAtrStop = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("l2_atrstop"));//TODO ATR based stop later....
}
