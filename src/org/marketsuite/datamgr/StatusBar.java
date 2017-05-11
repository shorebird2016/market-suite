package org.marketsuite.datamgr;

import org.marketsuite.framework.resource.FrameworkIcon;

import javax.swing.*;
import java.awt.*;

//status bar specifically designed for AGB240 device
public class StatusBar extends JPanel {
    public StatusBar() {
        setLayout(new BorderLayout());
        setOpaque(false);//show background image

        //east - time, connection status
        JPanel east_pnl = new JPanel();
        east_pnl.setLayout(new BoxLayout(east_pnl, BoxLayout.X_AXIS));
        east_pnl.setOpaque(false);
        east_pnl.add(new JLabel(FrameworkIcon.IMAGE_TOOLBAR_SEPARATOR2));
        east_pnl.add(_lbTime);
//        _lbTime.setToolTipText(AppConstants.APP_BUNDLE.getString("status_label_1"));
//        _lbTime.setForeground(Color.white);
//        _lblConn.setToolTipText(AppConstants.APP_BUNDLE.getString("status_tip_1"));
//        _lblConn.setOpaque(false);
//        east_pnl.add(_lblConn);
        add(east_pnl, BorderLayout.EAST);

//        _lbTime.setText(Calendar.getInstance().toString());
    }

    //instance variables
    private JLabel _lbTime = new JLabel();
//    private JLabel _lblConn = new JLabel(LazyIcon.ICON_CONN_HTTP);

    //literals
}