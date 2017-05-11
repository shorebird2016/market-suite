package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Container for managing database definitions.
 */
public class IndicatorSimPanel extends SkinPanel {
    public IndicatorSimPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());

        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_6") + "  ", _pnlReportSetup = new ReportSetupPanel());
        tabs.addTab(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_7") + "  ", _pnlRunReport = new RunReportPanel());
        add(tabs, BorderLayout.CENTER);

        //tab change to Run tab: refresh drop down
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                if (tabs.getSelectedIndex() == 1) {//change to Run tab
                    _pnlReportSetup.settingToModel();
                }
            }
        });
    }

    //----- accessors -----
    public ReportSetupPanel getReportSetupPanel() { return _pnlReportSetup; }

    //----- instance variables -----
    private ReportSetupPanel _pnlReportSetup;
    private RunReportPanel _pnlRunReport;
}
