package org.marketsuite.main;

import org.marketsuite.planning.PlanSheetPanel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PlanSheetFrame extends JInternalFrame implements PropertyChangeListener {
    public PlanSheetFrame() {
        setName("Main");//for MainTabUI to recognize
        setResizable(true); setClosable(true); setMaximizable(true); setIconifiable(false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("pw_title"));
        setFrameIcon(ApolloIcon.APP_ICON);
        JPanel content_pane = new JPanel(); content_pane.setOpaque(false);
        content_pane.setLayout(new MigLayout("insets 0"));
        setContentPane(content_pane);
        content_pane.add(_pnlPlanSheet = new PlanSheetPanel(), "dock center");
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_PLAN_SHEET, MdiMainFrame.LOCATION_PLAN_SHEET,
            MdiMainFrame.SIZE_PLAN_SHEET);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
//        Props.addWeakPropertyChangeListener(Props.GroupChange, this);//handle group change
//        Props.addWeakPropertyChangeListener(Props.WatchListSelected, this);//handle group change
    }

    //interface/override methods
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;//setVisible(true);
//        ArrayList<String> symbols;
//        Props prop = (Props) evt.getSource();
//        switch (prop) {
//            case WatchListSelected:
//                String group = (String)prop.getValue();
//                symbols = GroupStore.getInstance().getMembers(group);
//                _pnlPlanSheet.changeTimeFrame(ThumbnailPanel.TimeFrame.Daily1Year);//default
//                _pnlPlanSheet.renderThumbnails(symbols);
//                break;
//
//            case PlotThumbnails://passed in a list
//                symbols = (ArrayList<String>)prop.getValue();
//                _pnlPlanSheet.changeTimeFrame(ThumbnailPanel.TimeFrame.Daily1Year);//default
//                _pnlPlanSheet.renderThumbnails(symbols, false);//don't sort this
//                break;
//
//            case GroupChange:
//                _pnlPlanSheet.initGroupSelector();
//                break;
//        }
    }

    //----- instance variables-----
    private PlanSheetPanel _pnlPlanSheet;
}