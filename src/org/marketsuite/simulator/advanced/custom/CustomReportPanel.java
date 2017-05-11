package org.marketsuite.simulator.advanced.custom;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Container for "Custom Analysis" related functions.
 */
public class CustomReportPanel extends SkinPanel {
    public CustomReportPanel() {
        super(FrameworkIcon.BACKGROUND_CONTENT.getIcon(), new BorderLayout());

        //two sub-tabs
        final JTabbedPane report_tab = new JTabbedPane();
        report_tab.add(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tab_lbl_3"), _pnlSetup = new ReportSetupPanel());
        report_tab.add(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tab_lbl_4"), _pnlRunReport = new RunReportPanel());
        report_tab.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int sel = report_tab.getSelectedIndex();
                if (sel == TAB_RUN_REPORT) {//refresh combo list
                    _pnlRunReport.initReportList();
                    if (_pnlSetup.getCurrentFile() != null)
                        _pnlRunReport.setCurrentFile(_pnlSetup.getCurrentFile().getName());
                }
            }
        });
        add(report_tab, BorderLayout.CENTER);
    }

    //----- variables -----
    private ReportSetupPanel _pnlSetup;
    private RunReportPanel _pnlRunReport;
    private final static int TAB_RUN_REPORT = 1;
}
