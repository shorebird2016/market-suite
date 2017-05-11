package org.marketsuite.main;

import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.type.PerfTimeframe;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.thumbnail.ThumbnailPanel;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class ThumbNailFrame extends JInternalFrame implements PropertyChangeListener {
    public ThumbNailFrame() {
        setName("Main");//for MainTabUI to recognize
        setResizable(true); setClosable(true); setMaximizable(true); setIconifiable(false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("tn_01"));
        setFrameIcon(ApolloIcon.APP_ICON);
        JPanel content_pane = new JPanel(); content_pane.setOpaque(false);
        content_pane.setLayout(new BorderLayout());
        setContentPane(content_pane);
        content_pane.add(_pnlThumb = new ThumbnailPanel(true), BorderLayout.CENTER);
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_THUMBNAIL, MdiMainFrame.LOCATION_THUMBNAIL,
                MdiMainFrame.SIZE_THUMBNAIL);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        Props.addWeakPropertyChangeListener(Props.PlotThumbnails, this);//handle symbol change
        Props.addWeakPropertyChangeListener(Props.GroupChange, this);//handle group change
        Props.addWeakPropertyChangeListener(Props.WatchListSelected, this);//handle group change
        Props.addWeakPropertyChangeListener(Props.SymbolSelection, this);//handle symbol selection
    }

    //interface/override methods
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;//setVisible(true);
        ArrayList<String> symbols;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case WatchListSelected:
                String group = (String)prop.getValue();
                symbols = GroupStore.getInstance().getMembers(group);
                _pnlThumb.changeTimeFrame(PerfTimeframe.ONE_YEAR);//default to 1 year
                _pnlThumb.renderThumbnails(symbols);//sort alphabatically
                break;

            case PlotThumbnails://passed in a list
                symbols = (ArrayList<String>)prop.getValue();
                _pnlThumb.changeTimeFrame(PerfTimeframe.ONE_YEAR);//default to 1 year
                _pnlThumb.renderThumbnails(symbols);//don't sort this
                break;

            case GroupChange:
                _pnlThumb.initGroupSelector();
                break;

            case SymbolSelection:
                _pnlThumb.emphasizeSymbol((String) prop.getValue());
                break;
        }
    }

    //----- instance variables-----
    private ThumbnailPanel _pnlThumb;
}