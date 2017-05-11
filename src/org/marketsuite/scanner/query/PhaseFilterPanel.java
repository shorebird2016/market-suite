package org.marketsuite.scanner.query;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//if not enabled, all phases are shown, if any number of phases are selected, those will be shown
class PhaseFilterPanel extends JPanel {
    PhaseFilterPanel() {
        setLayout(new MigLayout("insets 0, flowy, gap 0", "[grow]"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]push[]5[]5", "2[]2"));
        ttl.add(_chkEnable); _chkEnable.setFocusable(false); _chkEnable.setOpaque(false);
        _chkEnable.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        _chkEnable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableAll();
                if (_chkEnable.isSelected()) _chkBullish.setSelected(true);//force one selection
            }
        });
        ttl.add(_btnSelectAll);
        _btnSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkAll(true);
            }
        });
        ttl.add(_btnSelectNone);
        _btnSelectNone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkAll(false);
            }
        });
        add(ttl, "dock north");
        add(_chkBullish);
        add(_chkWeakWarning);
        add(_chkStrongWarning);
        add(_chkDistribution);
        add(_chkBearish);
        add(_chkRecovery);
        add(_chkAccumulation);
//        setPreferredSize(new Dimension(200, 400));
        enableAll();
    }

    //protected methods
    boolean showBullish(String phase) {
        return _chkBullish.isSelected() && phase.equals(MarketInfo.phaseIdToString(MarketInfo.PHASE_BULLISH))
           || !_chkEnable.isSelected();
    }
    boolean isWeakWarning(String phase) {
        return _chkWeakWarning.isSelected() && phase.equals(MarketInfo.phaseIdToString(MarketInfo.PHASE_WEAK_WARNING))
           || !_chkEnable.isSelected();
    }
    boolean isStrongWarning(String phase) {
        return _chkStrongWarning.isSelected() && phase.equals(MarketInfo.phaseIdToString(MarketInfo.PHASE_STRONG_WARNING))
           || !_chkEnable.isSelected();
    }
    boolean isDistribution(String phase) {
        return _chkDistribution.isSelected() && phase.equals(MarketInfo.phaseIdToString(MarketInfo.PHASE_DISTRIBUTION))
           || !_chkEnable.isSelected();
    }
    boolean isBearish(String phase) {
        return _chkBearish.isSelected() && phase.equals(MarketInfo.phaseIdToString(MarketInfo.PHASE_BEARISH))
           || !_chkEnable.isSelected();
    }
    boolean isRecovery(String phase) {
        return _chkRecovery.isSelected() && phase.equals(MarketInfo.phaseIdToString(MarketInfo.PHASE_RECOVERY))
           || !_chkEnable.isSelected();
    }
    boolean isAccumulation(String phase) {
        return _chkAccumulation.isSelected() && phase.equals(MarketInfo.phaseIdToString(MarketInfo.PHASE_ACCUMULATION))
           || !_chkEnable.isSelected();
    }
    boolean isNoSelection() {
        return !_chkBullish.isSelected() && !_chkWeakWarning.isSelected() && !_chkStrongWarning.isSelected() &&
                !_chkDistribution.isSelected() && !_chkBearish.isSelected() && !_chkRecovery.isSelected() &&
                !_chkAccumulation.isSelected() && _chkEnable.isSelected();
    }

    //private methods
    private void enableAll() {
        boolean enable = _chkEnable.isSelected();
        _chkBullish.setEnabled(enable);
        _chkWeakWarning.setEnabled(enable);
        _chkStrongWarning.setEnabled(enable);
        _chkDistribution.setEnabled(enable);
        _chkBearish.setEnabled(enable);
        _chkRecovery.setEnabled(enable);
        _chkAccumulation.setEnabled(enable);
    }
    private void checkAll(boolean checked) {
        _chkBullish.setSelected(checked);
        _chkWeakWarning.setSelected(checked);
        _chkStrongWarning.setSelected(checked);
        _chkDistribution.setSelected(checked);
        _chkBearish.setSelected(checked);
        _chkRecovery.setSelected(checked);
        _chkAccumulation.setSelected(checked);
    }

    //----- variables -----
    private JCheckBox _chkEnable = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_12"));
    private JCheckBox _chkBullish = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_19"));
    private JCheckBox _chkWeakWarning = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_20"));
    private JCheckBox _chkStrongWarning = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_21"));
    private JCheckBox _chkDistribution = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_22"));
    private JCheckBox _chkBearish = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_23"));
    private JCheckBox _chkRecovery = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_24"));
    private JCheckBox _chkAccumulation = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_25"));
    private JButton _btnSelectAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_09"), FrameworkIcon.SELECT_ALL);
    private JButton _btnSelectNone = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_10"), FrameworkIcon.CLEAR);
}
