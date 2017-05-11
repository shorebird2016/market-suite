package org.marketsuite.main;

import org.marketsuite.datamgr.DataManagerPanel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;

public class DataMgrFrame extends JInternalFrame {
    public DataMgrFrame() {
        setName("Main");//for MainTabUI to recognize
        setResizable(true); setClosable(true); setMaximizable(true); setIconifiable(false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("dm_title"));
        setFrameIcon(ApolloIcon.APP_ICON);
        JPanel content_pane = new JPanel() {
            Color bg = new Color(32, 3, 2); // from the right end of the brand bar image
            ApolloIcon ic = ApolloIcon.DEFAULT_BACKGROUND;
            final int icWidth = ic.getIconWidth() - 1;   // the image has a darker 1 pixel border
            final int icHeight = ic.getIconHeight();
            public void paintComponent(Graphics g) {
                Dimension size = getSize();
                int x = icWidth - 50;//100
                int y = icHeight - 2;//6
                // fill lower part of panel from row 2 tab down to status bar todo: how to blend into brand bar.....
                //   with color from small rectangle in upper left corner
                g.drawImage(ic.getIcon().getImage(),
                        0, icHeight, size.width, size.height,
                        x, y, x + 2, y + 2, this);
                // draw regular image
                g.drawImage(ic.getIcon().getImage(), 0, 0, this);
                // fill with right end image color in case window is wider than image
                // draw on top of original image since image has a 1 pixel darker border around it
                g.setColor(bg);
                g.fillRect(icWidth, 0, size.width - icWidth, icHeight);
            }
        };
        content_pane.setLayout(new BorderLayout());
        setContentPane(content_pane);
        content_pane.add(_pnlMain = new DataManagerPanel(), BorderLayout.CENTER);
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_DATAMGR, MdiMainFrame.LOCATION_DATAMGR, MdiMainFrame.SIZE_DATAMGR);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    //----- accessors -----
    public DataManagerPanel getMainPanel() { return _pnlMain; }

    //----- instance variables-----
    private DataManagerPanel _pnlMain;
}