package org.marketsuite.scanner.query;

import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

//container for IBD related strategy parameters (eg. L2 method)
class IbdFilterPanel extends JPanel {
    IbdFilterPanel() {
        setLayout(new MigLayout("insets 0, wrap 4, gap 0", "[grow]"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]push", "3[]3"));
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_ibd"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        add(ttl, "dock north");

        //hook up
        add(_chkHookup, "left");
        _chkHookup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { enableHookup(); }
        });
        add(_fldNumWeeksUp, "left"); add(_lblUp, "center, gapleft 5"); add(_fldMinRatingUp);
        WidgetUtil.attachToolTip(_fldMinRatingUp, ApolloConstants.APOLLO_BUNDLE.getString("qp_minthresh"),
            SwingConstants.RIGHT, SwingConstants.BOTTOM);
        JPanel pnl = new JPanel(new MigLayout("insets 0")); pnl.setOpaque(false);//use pnl to layout nicer
        pnl.add(_lblForUp); pnl.add(_chkCompUp); pnl.add(_chkRsUp); pnl.add(_chkPriceUp);
        add(pnl, "gapleft 30, wrap, span 3");

        //hook down
        add(_chkHookdn, "left");
        _chkHookdn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableHookdn();
            }
        });
        add(_fldNumWeeksDn, "left"); add(_lblDn, "center, gapleft 5"); add(_fldMaxRatingDn);
        WidgetUtil.attachToolTip(_fldMaxRatingDn, ApolloConstants.APOLLO_BUNDLE.getString("qp_maxthresh"),
            SwingConstants.RIGHT, SwingConstants.BOTTOM);
        pnl = new JPanel(new MigLayout("insets 0")); pnl.setOpaque(false);
        pnl.add(_lblForDn); pnl.add(_chkCompDn); pnl.add(_chkRsDn); pnl.add(_chkPriceDn);
        add(pnl, "gapleft 30, wrap, span 3");

        //composite/rs ranges, use inner panel to align nicer w outer panel
        pnl = new JPanel(new MigLayout("insets 0, wrap 4")); pnl.setOpaque(false);
        pnl.add(_chkCompRange);
        WidgetUtil.attachToolTip(_chkCompRange, ApolloConstants.APOLLO_BUNDLE.getString("qp_range"),
            SwingConstants.RIGHT, SwingConstants.BOTTOM);
        _chkCompRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableCompRange();
            }
        });
        pnl.add(_fldMinComp); pnl.add(new JLabel(" ~ ")); pnl.add(_fldMaxComp);
        pnl.add(_chkRsRange);
        _chkRsRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableRsRange();
            }
        });
        pnl.add(_fldMinRs); pnl.add(new JLabel(" ~ ")); pnl.add(_fldMaxRs);
        add(pnl, "span 3, wrap");

        //init
        disableFieldsTemp();//TODO remove later
        enableHookup(); enableHookdn(); enableCompRange(); enableRsRange();
    }

    //----- protected methods -----
    //checkbox checked, and at least one rating > 50
    boolean isHookup(ArrayList<IbdRating> ratings) {
        boolean over_threshold = ratings.get(0).getComposite() > _fldMinRatingUp.getValue() ||
            ratings.get(0).getRsRating() > _fldMinRatingUp.getValue();
        return _chkHookup.isSelected() && over_threshold;
    }
    //checkbox checked, at least one rating < 50
    boolean isHookdown(ArrayList<IbdRating> ratings) {
        boolean over_threshold = ratings.get(0).getComposite() > _fldMaxRatingDn.getValue() ||
            ratings.get(0).getRsRating() > _fldMaxRatingDn.getValue();
        return _chkHookdn.isSelected() && over_threshold;
    }
    boolean isCompositeInRange(IbdRating rating) {
        if (!_chkCompRange.isSelected()) return true;//feature not enabled
        int comp = rating.getComposite();
        return _chkCompRange.isSelected() && comp > 0
           && (comp >= _fldMinComp.getValue()) && (comp <= _fldMaxComp.getValue());
    }
    boolean isRsInRange(IbdRating rating) {
        if (!_chkRsRange.isSelected()) return true;//feature not enabled
        int rs = rating.getRsRating();
        return _chkRsRange.isSelected() && rs > 0
           && (rs >= _fldMinRs.getValue()) && (rs <= _fldMaxRs.getValue());
    }
    boolean isHookupSelected() { return _chkHookup.isSelected(); }
    boolean isHookdownSelected() { return _chkHookdn.isSelected(); }

    //----- private methods -----
    private void enableHookup() {
        boolean hku = _chkHookup.isSelected();
        _fldNumWeeksUp.setEnabled(hku); _lblUp.setEnabled(hku); _fldMinRatingUp.setEnabled(hku);
        _lblForUp.setEnabled(hku); //_chkCompUp.setEnabled(hku); _chkRsUp.setEnabled(hku); _chkPriceUp.setEnabled(hku);
    }
    private void enableHookdn() {
        boolean hku = _chkHookdn.isSelected();
        _fldNumWeeksDn.setEnabled(hku); _lblDn.setEnabled(hku); _fldMaxRatingDn.setEnabled(hku);
        _lblForDn.setEnabled(hku); //_chkCompDn.setEnabled(hku); _chkRsDn.setEnabled(hku); _chkPriceDn.setEnabled(hku);
    }
    private void enableCompRange() {
        boolean cp = _chkCompRange.isSelected();
        _fldMinComp.setEnabled(cp); _fldMaxComp.setEnabled(cp);
    }
    private void enableRsRange() {
        boolean rr = _chkRsRange.isSelected();
        _fldMinRs.setEnabled(rr); _fldMaxRs.setEnabled(rr);
    }
//TODO temporary, remove this method later
    private void disableFieldsTemp() {
        _chkCompUp.setSelected(true); _chkCompUp.setEnabled(false); _chkRsUp.setEnabled(false); _chkRsUp.setSelected(true);
        _chkCompDn.setSelected(true); _chkCompDn.setEnabled(false); _chkRsDn.setEnabled(false); _chkRsDn.setSelected(true);
        _chkPriceUp.setSelected(true); _chkPriceUp.setEnabled(false);
        _chkPriceDn.setSelected(true); _chkPriceDn.setEnabled(false);
    }

    //----- variables -----
    private JCheckBox _chkHookup = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_hkup"));
    private LongIntegerField _fldNumWeeksUp = new LongIntegerField(1, 4, 1, 20);
    private JLabel _lblUp = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_hkwk"));
    private LongIntegerField _fldMinRatingUp = new LongIntegerField(50, 4, 1, 100);
    private JLabel _lblForUp = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_for"));
    private JCheckBox _chkCompUp = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_comp"));
    private JCheckBox _chkRsUp = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_rs"));
    private JCheckBox _chkPriceUp = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_price"));

    private JCheckBox _chkHookdn = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_hkdn"));
    private JLabel _lblDn = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_hkwkdn"));
    private LongIntegerField _fldNumWeeksDn = new LongIntegerField(1, 4, 1, 20);
    private JLabel _lblForDn = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qp_for"));
    private LongIntegerField _fldMaxRatingDn = new LongIntegerField(50, 4, 1, 100);
    private JCheckBox _chkCompDn = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_comp"));
    private JCheckBox _chkRsDn = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_rs"));
    private JCheckBox _chkPriceDn = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_price"));

    private JCheckBox _chkCompRange = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_comprng"));
    private LongIntegerField _fldMinComp = new LongIntegerField(70, 4, 1, 100);
    private LongIntegerField _fldMaxComp = new LongIntegerField(99, 4, 1, 100);
    private JCheckBox _chkRsRange = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_rsrng"));
    private LongIntegerField _fldMinRs = new LongIntegerField(70, 4, 1, 100);
    private LongIntegerField _fldMaxRs = new LongIntegerField(99, 4, 1, 100);

}
