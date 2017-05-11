package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.report.model.ReportTemplate;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Container for simulation related settings organized into several tabs.
 */
class SettingPanel extends JPanel {
    SettingPanel() {
        setOpaque(false);
        setLayout(new GridLayout(1,1));
        setBorder(new BevelBorder(BevelBorder.RAISED));
        JTabbedPane tabs = new JTabbedPane();
        tabs.add(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_5"), _pnlTimeSetting = new TimeSettingPanel());
        tabs.add(ApolloConstants.APOLLO_BUNDLE.getString("advsim_strategy"), _pnlOscSetting = new OscillatorSettingPanel());
        add(tabs);
    }

    //-----protected methods-----
    void clear() {
        _pnlTimeSetting.clearForm();
    }

    void populate(ReportTemplate report_template) {
        _pnlTimeSetting.populate(report_template);
        _pnlOscSetting.populate(report_template.getStrategySetting());
    }

    ReportSetting getReportSetting() {
        return new ReportSetting(_pnlTimeSetting.getTimeSetting(), _pnlOscSetting.getMacSetting(),
            _pnlOscSetting.getMzcSetting(), _pnlOscSetting.getRsiSetting(), _pnlOscSetting.getStoSetting() );
    }

    //-----instance variables-----
    private TimeSettingPanel _pnlTimeSetting;
    private OscillatorSettingPanel _pnlOscSetting;
}
