package org.marketsuite.resource;

import java.awt.*;
import javax.swing.*;

//collection of images, icons
public enum ApolloIcon implements Icon {
    APP_ICON                ( "apollo_desktop.png"),
    MAINFRAME_BACKGROUND    ( "sim_bkgnd.png"),
    DEFAULT_BACKGROUND      ( "main_bkgnd.jpg"),
    RISKMGR_BACKGROUND      ( "riskmgr_bkgnd.jpg"),
    LAUNCH                  ( "launch.png" ),
    APP_SETUP               ( "app_setup.gif"),
    APP_DATAMGR             ( "app_data.png"),
    APP_THUMBNAIL           ( "app_scan.gif"),
    APP_RISK_MGR            ( "app_risk.png"),
    APP_SIMULATOR           ( "app_sim.png"),
    APP_MARKET              ( "app_market.png"),
    ;

    //CTOR
    ApolloIcon(String _file) {
        file = _file;
    }

    // Icon implementation
    public final int getIconHeight() {
        return getIcon().getIconHeight();
    }

    public final int getIconWidth() {
        return getIcon().getIconWidth();
    }

    public final void paintIcon(Component c, Graphics g, int x, int y) {
        getIcon().paintIcon(c, g, x, y);
    }

    public final Image getImage() {
       return getIcon().getImage();
    }

    public final ImageIcon getIcon() {
        if (icon == null)
            icon = new ImageIcon(ApolloIcon.class.getResource(file));
        return icon;
    }

    //instance variables
    private ImageIcon icon;
    private String file;
}

