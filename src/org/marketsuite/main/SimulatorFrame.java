package org.marketsuite.main;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.simulator.SimulatorPanel;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimulatorFrame extends JInternalFrame {
    public static SimulatorFrame getInstance() {
        if (_Instance == null)
            _Instance = new SimulatorFrame();
        return _Instance;
    }
    private SimulatorFrame() {
        super(ApolloConstants.APOLLO_BUNDLE.getString("sim_lbl_1"), true, true, true, false);
        setName("Main");//for MainTabUI to recognize
        setFrameIcon(ApolloIcon.APP_ICON);

        //paint brand bar as background image
        JPanel content_pane = new JPanel() {
            Color bg = new Color(32, 3, 2); // from the right end of the brand bar image
            ApolloIcon ic = ApolloIcon.MAINFRAME_BACKGROUND;
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

        //link - export to analyzer compatible files
        JPanel north_pnl = new JPanel(new BorderLayout());
        north_pnl.setOpaque(false);
        // height needs to be: (image height) - (tab height)
        north_pnl.add(Box.createRigidArea(new Dimension(0, 50)), BorderLayout.WEST);
        content_pane.add(north_pnl, BorderLayout.NORTH);

        //north east - hyperlinks for export transaction files
        JPanel east_pnl = new JPanel();
        east_pnl.setOpaque(false);

        //basic mode - simulate each strategy manually
        east_pnl.add(_btnBasicMode);
        _btnBasicMode.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        _btnBasicMode.setFocusable(false);
        _btnBasicMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pnlSimulator.changeView(SimulatorPanel.CARD_BASIC_MODE);
                _btnAdvMode.setFont(FrameworkConstants.SMALL_FONT);
                _btnBasicMode.setFont(FrameworkConstants.SMALL_FONT_BOLD);
            }
        });
        east_pnl.add(Box.createHorizontalStrut(5));

        //advanced mode - simulate group of strategies
        east_pnl.add(_btnAdvMode);
        _btnAdvMode.setFont(FrameworkConstants.SMALL_FONT);
        _btnAdvMode.setFocusable(false);
        _btnAdvMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pnlSimulator.changeView(SimulatorPanel.CARD_ADVANCED_MODE);
                _btnAdvMode.setFont(FrameworkConstants.SMALL_FONT_BOLD);
                _btnBasicMode.setFont(FrameworkConstants.SMALL_FONT);
            }
        });
//        east_pnl.add(_btnDateRanges);
//        _btnDateRanges.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                //automatically set to pre-defined date ranges to save manual typing using current date ranges
//                _pnlSimulator.setDateRange(DATE_RANGES[_nCurRangeIndex], DATE_RANGES[_nCurRangeIndex + 1]);
//                _nCurRangeIndex++;
//                if (_nCurRangeIndex == DATE_RANGES.length - 1) _nCurRangeIndex = 0;
//            }
//        });
        north_pnl.add(east_pnl, BorderLayout.EAST);
        content_pane.add(north_pnl, BorderLayout.NORTH);

        //center - main content
        content_pane.add(_pnlSimulator = new SimulatorPanel(), BorderLayout.CENTER);
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_SIMULATOR, MdiMainFrame.LOCATION_SIMULATOR, MdiMainFrame.SIZE_SIMULATOR);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    //----- instance variables-----
    private static SimulatorFrame _Instance;
    private JButton _btnBasicMode = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("main_lbl_1"));
    private JButton _btnAdvMode = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("main_lbl_2"));
//    private JButton _btnDateRanges = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("sim_dtrng"), FrameworkIcon.FILTER);
    private SimulatorPanel _pnlSimulator;
//    private int _nCurRangeIndex;

    //----- literals -----
//    private final static String[] DATE_RANGES = {
//        "1950-01-03", "1963-12-31", "1980-06-02", "1997-01-03", "2013-03-01", "2014-10-15"
//    };
}