package org.marketsuite.scanner;

import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MainUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.scanner.market.PhaseConditionPanel;
import org.marketsuite.scanner.query.QueryPanel;
import org.marketsuite.scanner.tracking.TrackerPanel;
import org.marketsuite.scanner.earning.EarningPanel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.scanner.query.QueryPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ScannerFrame extends JInternalFrame implements PropertyChangeListener {
    public ScannerFrame() {
        super(ApolloConstants.APOLLO_BUNDLE.getString("scn_lbl_1"), true, true, true, false);
        setFrameIcon(ApolloIcon.APP_ICON);
        JPanel content_pane = new JPanel() {
            public void paintComponent(Graphics g) {//draw background
                g.drawImage(FrameworkIcon.BACKGROUND_ATLANTIS.getIcon().getImage(), 0, 0, this);
            }
        };
        content_pane.setLayout(new MigLayout("insets 0"));
        setContentPane(content_pane);

        //paint brand bar as background image
//        JPanel content_pane = new JPanel() {
//            Color bg = new Color(32, 3, 2); // from the right end of the brand bar image
//            ApolloIcon ic = ApolloIcon.DEFAULT_BACKGROUND;
//            final int icWidth = ic.getIconWidth() - 1;   // the image has a darker 1 pixel border
//            final int icHeight = ic.getIconHeight();
//
//            public void paintComponent(Graphics g) {
//                Dimension size = getSize();
//                int x = icWidth - 50;//100
//                int y = icHeight - 2;//6
//                // fill lower part of panel from row 2 tab down to status bar
//                //   with color from small rectangle in upper left corner
//                g.drawImage(ic.getIcon().getImage(),
//                        0, icHeight, size.width, size.height,
//                        x, y, x + 2, y + 2, this);
//                // draw regular image
//                g.drawImage(ic.getIcon().getImage(), 0, 0, this);
//                // fill with right end image color in case window is wider than image
//                // draw on top of original image since image has a 1 pixel darker border around it
//                g.setColor(bg);
//                g.fillRect(icWidth, 0, size.width - icWidth, icHeight);
//            }
//        };
//        content_pane.setLayout(new BorderLayout());
//        setContentPane(content_pane);

        //link - export to analyzer compatible files
//        JPanel north_pnl = new JPanel(new BorderLayout());
//        north_pnl.setOpaque(false);
//        height needs to be: (image height) - (tab height)
//        north_pnl.add(Box.createRigidArea(new Dimension(0, 50)), BorderLayout.WEST);
//        content_pane.add(north_pnl, BorderLayout.NORTH);

        //north east - hyperlinks for export transaction files
//        JPanel east_pnl = new JPanel();
//        east_pnl.setOpaque(false);

        //center - main content
//        JPanel content_pnl = new JPanel(new MigLayout());
        _tabMain.setName("Main");//tell MainTabUI to use taller height
//        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("adv_lbl_6") + "  ", _pnlRank = new RankPanel());
        if (!MainModel.getInstance().isBasicUser()) {
            _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("adv_lbl_7") + "  ", _pnlQuery = new QueryPanel());
            _tabMain.addTab("Speeder", new SpeederPanel());
//            _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("adv_lbl_4") + "  ", new EarningPanel());
//            _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("adv_lbl_5") + "  ", _pnlTracker = new TrackerPanel());
//            _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("adv_lbl_1") + "  ", new PhaseConditionPanel());
        }
//        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("adv_lbl_2") + "  ", new SignalPanel());
//        _tabMain.addTab(ApolloConstants.APOLLO_BUNDLE.getString("adv_lbl_3") + "  ", new ScanResultPanel());
        content_pane.add(_tabMain, "dock center");
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_SCANNER, MdiMainFrame.LOCATION_SCANNER, MdiMainFrame.SIZE_SCANNER);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
//        _tabMain.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                _pnlRank.closeOptionWindow();
//            }
//        });
//        addComponentListener(new ComponentAdapter() {
//            public void componentMoved(ComponentEvent e) {
//                _pnlRank.closeOptionWindow();
//            }
//        });
    }

    //----- interfaces, overrides -----
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case SymbolSelection://update label
//                String sym = (String) prop.getValue();
//                _lblSymbol.setText(" " + sym);
                break;

            case GroupChange:
                _pnlTracker.layoutNavPane();
                break;
        }
    }

    //----- instance variables-----
    private JTabbedPane _tabMain = new JTabbedPane();
    private TrackerPanel _pnlTracker;
    private QueryPanel _pnlQuery;
//    private RankPanel _pnlRank;
}