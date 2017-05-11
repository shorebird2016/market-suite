package org.marketsuite.datamgr;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.datamgr.dataimport.ImportPanel;
import org.marketsuite.datamgr.export.ExportPanel;
import org.marketsuite.datamgr.quote.QuotePanel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.datamgr.dataimport.ImportPanel;
import org.marketsuite.datamgr.export.ExportPanel;
import org.marketsuite.datamgr.quote.QuotePanel;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Container for managing database definitions.
 */
public class DataManagerPanel extends SkinPanel {
    public DataManagerPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(ApolloConstants.APOLLO_BUNDLE.getString("dm_tab_1") + "  ", _pnlQuote = new QuotePanel());
        tabs.addTab(ApolloConstants.APOLLO_BUNDLE.getString("dm_tab_2") + "  ", new ImportPanel());
        tabs.addTab(ApolloConstants.APOLLO_BUNDLE.getString("dm_tab_3") + "  ", new ExportPanel());
        add(tabs, BorderLayout.CENTER);
    }

    //----- accessors -----
    public QuotePanel getQuotePanel() {
        return _pnlQuote;
    }

    //----- instance variables -----
    private QuotePanel _pnlQuote;
}