package org.marketsuite.marektview.valuation;

import javax.swing.*;

//container of both single year and multi-year
public class ValuationViewPanel extends JTabbedPane {
    public ValuationViewPanel() {
        addTab("Single Year", new SingleYearPanel());
        addTab("Multi-Year", new MultiYearPanel());
    }
}
