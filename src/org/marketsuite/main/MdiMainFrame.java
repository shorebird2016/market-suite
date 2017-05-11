package org.marketsuite.main;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.ManageDebugFiles;
import org.marketsuite.framework.util.Props;
import org.marketsuite.gap.GapAnalysisPanel;
import org.marketsuite.gap.GapStudyPanel;
import org.marketsuite.marektview.MarketViewFrame;
import org.marketsuite.planning.ReportPanel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.scanner.ScannerFrame;
import org.marketsuite.scanner.report.ScannerReportPanel;
import org.marketsuite.watchlist.WatchListFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class MdiMainFrame extends JFrame implements PropertyChangeListener {
    private static MdiMainFrame _Instance;
    public static MdiMainFrame getInstance() {
        if (_Instance == null)
            _Instance = new MdiMainFrame();
        return _Instance;
    }
    //singleton CTOR
    private MdiMainFrame() {
        super(ApolloConstants.APOLLO_BUNDLE.getString("main_title"));
        setIconImage(ApolloIcon.APP_ICON.getImage());
//FYI        setExtendedState(Frame.MAXIMIZED_BOTH);
        Point loc = ApolloPreferenceStore.getPreferences().getMainFrameLocation();
        if (loc != null) {
            setLocation(loc);
            Dimension dim = ApolloPreferenceStore.getPreferences().getMainFrameSize();
            if (dim != null)
                setSize(dim);
        }
        else {//default position and size Make the big window be indented 50 pixels from each edge of the screen.
            int vertical_inset = 70;
            int hor_inset = 200;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds(hor_inset, vertical_inset, screenSize.width - hor_inset*2, screenSize.height - vertical_inset*2);
            ApolloPreferenceStore.getPreferences().setMainFrameLocation(getLocation());
            ApolloPreferenceStore.getPreferences().setMainFrameSize(getSize());
            adjustLocation();
        }

        //update preferences based on size and position change
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                ApolloPreferenceStore.getPreferences().setMainFrameSize(getSize());
                ApolloPreferenceStore.savePreferences();
            }
            public void componentMoved(ComponentEvent e) {
                ApolloPreferenceStore.getPreferences().setMainFrameLocation(getLocation());
                ApolloPreferenceStore.savePreferences();
            }
        });

        //add device list and app icons
        _wDesktop = new JDesktopPane() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                _nViewWidth = this.getWidth();
                _nViewHeight = this.getHeight();
                g2d.drawImage(_imgBackground, 0, 0, _nViewWidth, _nViewHeight, this);
            }
        };

        //show watch list manager window and logging window by default
        WatchListMgrFrame grp_mgr = WatchListMgrFrame.getInstance();
        grp_mgr.setVisible(true);//when user clicks close window, this will be set automatically to false
        _wDesktop.add(grp_mgr);//must have this to show up
        LoggingFrame lf = LoggingFrame.getInstance();
        lf.setVisible(true);
        _wDesktop.add(lf);
        setContentPane(_wDesktop);

        //Make dragging a little faster but perhaps uglier.
        _wDesktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);//must have this such that close button can check dirty
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ApolloPreferenceStore.savePreferences();
                if (_frmRiskMgr != null && _frmRiskMgr.isDirty()) {
                    if (MessageBox.messageBox(MdiMainFrame.this,
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("rm_69"),
                            MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) == MessageBox.RESULT_OK)
                        System.exit(0);
                    return;//user doesn't want to exit
                }
                System.exit(0);
            }
            public void windowActivated(WindowEvent e) {
                super.windowActivated(e);
                CoreUtil.showTimeFromAppStart("<MdiMainFrame.windowActivated()>...DONE...");
            }
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                CoreUtil.showTimeFromAppStart("<MdiMainFrame.windowOpened()>...DONE...");
            }
        });

        //a timer that rotates background images every 30 minutes
        new Timer(300000, new ActionListener() {//every 30 minutes, change background
            public void actionPerformed(ActionEvent e) {
                File[] files = new File(FrameworkConstants.DATA_FOLDER_RESOURCE).listFiles();
                if (files == null) return;
                ArrayList<File> bkgnds = new ArrayList<>();
                for (File f : files) {
                    if (f.getName().endsWith(FrameworkConstants.EXTENSION_JPEG) || f.getName().endsWith(FrameworkConstants.EXTENSION_PNG))
                        bkgnds.add(f);
                }
                //use number to pick a file from array
                int idx = (int)(Math.random() * bkgnds.size());
                _imgBackground = new ImageIcon(bkgnds.get(idx).getPath()).getImage();
                _wDesktop.repaint();
            }
        }).start();
        InputMap inpMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK), "ShowStdio");
        getRootPane().getActionMap().put("ShowStdio", new javax.swing.AbstractAction() {
            public void actionPerformed(ActionEvent ev) {
//                ManageDebugFiles.showStdio(); TODO add this later
            }
        });
        ManageDebugFiles.initF4(this);

        //pre-create popup menu, build sub-menu items from enum for chart apps
        boolean basic_usr = MainModel.getInstance().isBasicUser();
        boolean xpr_usr = MainModel.getInstance().isExpertUer();
        boolean mid_usr = MainModel.getInstance().isMediumUser();
        for (final AppWindow app : AppWindow.values()) {
            if (app.equals(AppWindow.CANDLE_CHART) || app.equals(AppWindow.LINE_CHART)) {//two level menus
                if (!xpr_usr && app.equals(AppWindow.LINE_CHART)) continue;//don't show menu for line chart for basic user
                final boolean candle_chart = app.equals(AppWindow.CANDLE_CHART);
                JMenu candle_menu = new JMenu(app.toString());//sub-menu
                for (ChartType ct : ChartType.values()) {
                    JMenuItem mi = new JMenuItem(ct.toString());
                    mi.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent mie) {
                            //look up label, open corresponding internal frame
                            String cmd = mie.getActionCommand();
                            for (ChartType ct : ChartType.values()) {
                                if (cmd.equals(ct.toString()))
                                    startChartApp(ct, candle_chart);
                            }
                        }
                    });
                    candle_menu.add(mi);
                }
                _mnuPop.add(candle_menu);
            }
            else {//single level menu
                boolean add_menu = false;
                if (app.equals(AppWindow.MKT_VIEW) || app.equals(AppWindow.THUMBNAIL) || app.equals(AppWindow.DATA_MGR)) add_menu = true;//everybody
                else if (mid_usr) {
                    if (app.equals(AppWindow.SCANNER) || app.equals(AppWindow.SIMULATOR)) add_menu = true;
                }
                else if (xpr_usr) {
                    add_menu = true;
                }
                if (!add_menu) continue;//skip menu
                JMenuItem mi = new JMenuItem(app.toString());
                _mnuPop.add(mi);
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent mie) {
                        //look up label, open corresponding internal frame
                        String cmd = mie.getActionCommand();
                        for (AppWindow app : AppWindow.values()) {
                            if (cmd.equals(app.toString()))
                                startApp(app);
                        }
                    }
                });
            }
        }

        //at end, add report dialog/gap study dialog temporarily
        if (xpr_usr) {
            JMenuItem scn_mnu = new JMenuItem(ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_ttl"));
            _mnuPop.add(scn_mnu); scn_mnu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JDialog dlg = new JDialog(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_ttl")
                        + FrameworkConstants.YAHOO_DATE_FORMAT.format(Calendar.getInstance().getTime()));
                    dlg.setContentPane(new ScannerReportPanel());
                    WidgetUtil.setDialogProperties(dlg, new Dimension(800, 600), true, MdiMainFrame.getInstance(), JDialog.DISPOSE_ON_CLOSE);
                }
            });
            JMenuItem rpt_mnu = new JMenuItem("Report");
            _mnuPop.add(rpt_mnu); rpt_mnu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JDialog dlg = new JDialog(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("pw_rpttl"));
                    dlg.setContentPane(new ReportPanel());
                    WidgetUtil.setDialogProperties(dlg, new Dimension(1200, 600), true, MdiMainFrame.getInstance(), JDialog.DISPOSE_ON_CLOSE);
                }
            });

            //gap study dialog
            JMenuItem gap_mnu = new JMenuItem(ApolloConstants.APOLLO_BUNDLE.getString("gps_mnu")); _mnuPop.add(gap_mnu);
            gap_mnu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JDialog dlg = new JDialog(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("gps_ttl"));
                    dlg.setContentPane(new GapStudyPanel());
                    WidgetUtil.setDialogProperties(dlg, new Dimension(1500, 600), true, MdiMainFrame.getInstance(), JDialog.DISPOSE_ON_CLOSE);
                }
            });
//
//            //gap analysis dialog
//            JMenuItem gap_ana_mnu = new JMenuItem(ApolloConstants.APOLLO_BUNDLE.getString("gps_ana")); _mnuPop.add(gap_ana_mnu);
//            gap_ana_mnu.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    JDialog dlg = new JDialog(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("gps_ana_ttl"));
//                    dlg.setContentPane(new GapAnalysisPanel());
//                    WidgetUtil.setDialogProperties(dlg, new Dimension(800, 550), true, MdiMainFrame.getInstance(), JDialog.DISPOSE_ON_CLOSE);
//                }
//            });
        //}
//TODO temporarily change to basic user
            //gap analysis dialog
            JMenuItem gap_ana_mnu = new JMenuItem(ApolloConstants.APOLLO_BUNDLE.getString("gps_ana")); _mnuPop.add(gap_ana_mnu);
            gap_ana_mnu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JDialog dlg = new JDialog(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("gps_ana_ttl"));
                    dlg.setContentPane(new GapAnalysisPanel());
                    WidgetUtil.setDialogProperties(dlg, new Dimension(850, 650), true, MdiMainFrame.getInstance(), JDialog.DISPOSE_ON_CLOSE);
                }
            });
        }
        //listen to mouse right click to pop up menu
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me) || SwingUtilities.isLeftMouseButton(me))//MouseEvent.BUTTON3 works for MAC too
                    _mnuPop.show(me.getComponent(), me.getX(), me.getY());
            }
        });

        //pre-create app frames, nothing for now, example below
//        _frmDataMgr = new DataMgrFrame(this);  _wDesktop.add(_frmDataMgr);
        Props.addWeakPropertyChangeListener(Props.ShowApp, this);//handle symbol change
        CoreUtil.showTimeFromAppStart("<MdiMainFrame.CTOR()>.....DONE......");
    }

    //----- interface/override -----
    public void propertyChange(PropertyChangeEvent evt) {
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case ShowApp://update graph
                if (prop.getValue().equals(DataMgrFrame.class)) {
                    if (_frmDataMgr == null) {
                        _frmDataMgr = new DataMgrFrame();
                        _wDesktop.add(_frmDataMgr);
                    }
                    startApp(_frmDataMgr);
                }
                break;
        }
    }

    //----- public methods -----
    public void startWatchListFrame(String group_name) {
        if (_frmWatchList == null) {
            _frmWatchList = new WatchListFrame(this);
            _wDesktop.add(_frmWatchList);
        }
        else
            _frmWatchList.populate();
        _frmWatchList.setVisible(true);
        _wDesktop.getDesktopManager().activateFrame(_frmWatchList);//bring to front
    }
    public void closeWatchList() { _frmWatchList = null; }
    public DataMgrFrame findDataMgrFrame() {
        JInternalFrame[] iframes = _wDesktop.getAllFrames();
        for (JInternalFrame iframe : iframes)
            if (iframe instanceof DataMgrFrame)
                return (DataMgrFrame)iframe;
        return null;
    }
    public RiskMgrFrame findRiskMgrFrame() {
        JInternalFrame[] iframes = _wDesktop.getAllFrames();
        for (JInternalFrame iframe : iframes)
            if (iframe instanceof RiskMgrFrame)
                return (RiskMgrFrame)iframe;
        return null;
    }

    //----- private methods -----
    private void startApp(JInternalFrame frame) {
        frame.setVisible(true);
        _wDesktop.getDesktopManager().activateFrame(frame);//bring to front
    }
    private void startApp(AppWindow app_window) {
        switch (app_window) {
            case MKT_VIEW:
                if (_frmMktView == null) {
                    _frmMktView = new MarketViewFrame();
                    _wDesktop.add(_frmMktView);
                }
                startApp(_frmMktView);
                break;

            case SCANNER:
                if (_frmScanner == null) {
                    _frmScanner = new ScannerFrame();
                    _wDesktop.add(_frmScanner);
                }
                startApp(_frmScanner);
                break;

            case DATA_MGR:
                if (_frmDataMgr == null) {
                    _frmDataMgr = new DataMgrFrame();
                    _wDesktop.add(_frmDataMgr);
                }
                startApp(_frmDataMgr);
                break;

            case RISK_MGR:
                if (_frmRiskMgr == null) {
                    _frmRiskMgr = new RiskMgrFrame();
                    _wDesktop.add(_frmRiskMgr);
                }
                startApp(_frmRiskMgr);
                break;

            case SIMULATOR:
                if (_frmSimulator == null) {
                    _frmSimulator = SimulatorFrame.getInstance();
                    _wDesktop.add(_frmSimulator);
                }
                startApp(_frmSimulator);
                break;

            case THUMBNAIL:
                if (_frmThumbnail == null) {
                    _frmThumbnail = new ThumbNailFrame();
                    _wDesktop.add(_frmThumbnail);
                }
                startApp(_frmThumbnail);
                break;

            case PLAN_SHEET:
                if (_frmPlanSheet == null) {
                    _frmPlanSheet = new PlanSheetFrame();
                    _wDesktop.add(_frmPlanSheet);
                }
                startApp(_frmPlanSheet);
                break;
        }
    }
    private void startChartApp(ChartType ct_window, boolean candle) {//true=candle, false=line
        switch (ct_window) {
            case Daily:
                if (candle) {
                    if (_frmDailyCandleChart == null) {
                        _frmDailyCandleChart = new DailyCandleChartFrame();
                        _wDesktop.add(_frmDailyCandleChart);
                    }
                    startApp(_frmDailyCandleChart);
                }
                else {
                    if (_frmDailyLineChart == null) {
                        _frmDailyLineChart = new DailyLineChartFrame();
                        _wDesktop.add(_frmDailyLineChart);
                    }
                    startApp(_frmDailyLineChart);
                }
                break;

            case Weekly:
                if (candle) {
                    if (_frmWeeklyCandle == null) {
                        _frmWeeklyCandle = new WeeklyCandleChartFrame();
                        _wDesktop.add(_frmWeeklyCandle);
                    }
                    startApp(_frmWeeklyCandle);
                }
                else {
                    if (_frmWeeklyLineChart == null) {
                        _frmWeeklyLineChart = new WeeklyLineChartFrame();
                        _wDesktop.add(_frmWeeklyLineChart);
                    }
                    startApp(_frmWeeklyLineChart);
                }
                break;

//            case Monthly:
//                if (candle) break;//TODO
//                if (_frmMonthlyLineChart == null) {
//                    _frmMonthlyLineChart = new MonthlyLineChartFrame();
//                    _wDesktop.add(_frmMonthlyLineChart);
//                }
//                startApp(_frmMonthlyLineChart);
//                break;
//
//            case Analysis:
//                if (candle) break;//TODO
//                if (_frmAnalysis == null) {
//                    _frmAnalysis = new AnalysisGraphFrame();
//                    _wDesktop.add(_frmAnalysis);
//                }
//                startApp(_frmAnalysis);
//                break;
        }
    }

    //Adjust frame location based on number of monitors available
    // If the saved position is off the screen, recenter to the main screen
    // if the title bar is visible, the window can still be moved
    private void adjustLocation() {
        Area screen = new Area();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (int j = 0; j < gs.length; j++) {
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for (int i=0; i < gc.length; i++)
                screen.add(new Area(gc[i].getBounds()));
        }

        Rectangle mf = new Rectangle(getLocation(),getSize());
        Dimension contentPane = getContentPane().getSize();
        mf.height -= contentPane.height; // just the title bar
        if (!screen.intersects(mf)) {
            setSize(Constants.DEFAULT_FRAMESIZE);
            setLocationRelativeTo(null);
        }
    }

    //----- variables -----
    private JDesktopPane _wDesktop;
    private Image _imgBackground = FrameworkIcon.BACKGROUND_ATLANTIS.getImage();
    private int _nViewHeight, _nViewWidth;//dynamic changed after resize
    private WatchListFrame _frmWatchList;
    private DataMgrFrame _frmDataMgr;
    private RiskMgrFrame _frmRiskMgr;
    private ScannerFrame _frmScanner;
    private SimulatorFrame _frmSimulator;
    private MarketViewFrame _frmMktView;
    private AnalysisGraphFrame _frmAnalysis;
    private ThumbNailFrame _frmThumbnail;
    private PlanSheetFrame _frmPlanSheet;
    private DailyCandleChartFrame _frmDailyCandleChart;
    private WeeklyCandleChartFrame _frmWeeklyCandle;
    private DailyLineChartFrame _frmDailyLineChart;
    private WeeklyLineChartFrame _frmWeeklyLineChart;
    private MonthlyLineChartFrame _frmMonthlyLineChart;
    private JPopupMenu _mnuPop = new JPopupMenu();

    //----- literals enums -----
    private enum AppWindow {
        MKT_VIEW("Market View"),
        SCANNER("Scanner"),
        RISK_MGR("Risk Manager"),
        SIMULATOR("Simulator"),
        THUMBNAIL("Thumbnail"),
        DATA_MGR("Data Manager"),
        CANDLE_CHART("Candlestick Chart"),
        LINE_CHART("Line Chart"),
        PLAN_SHEET("Planning Sheet"),
        ;

        AppWindow(String disp) { displayString = disp; }
        public String toString() { return displayString; }
        private String displayString;
    }
    private enum ChartType {
        Daily,
        Weekly,
//        Monthly,
//        Analysis,
        ;
    }

    //initial/default app window locations, size, index into preference
    public static final Point LOCATION_WATCHLIST_MGR = new Point(0, 0);
    public static final Point LOCATION_WATCHLIST = new Point(10, 10);
    public static final Point LOCATION_LOGGER = new Point(10, 600);
    public static final Point LOCATION_MARKET_VIEW = new Point(20, 20);
    public static final Point LOCATION_SCANNER = new Point(30, 30);
    public static final Point LOCATION_DATAMGR = new Point(40, 40);
    public static final Point LOCATION_RISKMGR = new Point(50, 50);
    public static final Point LOCATION_SIMULATOR = new Point(60, 60);
    public static final Point LOCATION_THUMBNAIL = new Point(70, 70);
    public static final Point LOCATION_PLAN_SHEET = new Point(80, 80);
    public static final Point LOCATION_DAILY_CANDLE_CHART = new Point(100, 200);
    public static final Point LOCATION_WEEKLY_CANDLE_CHART = new Point(100, 400);
    public static final Point LOCATION_DAILY_LINE_CHART = new Point(100, 200);
    public static final Point LOCATION_WEEKLY_LINE_CHART = new Point(100, 400);
    public static final Point LOCATION_MONTHLY_LINE_CHART = new Point(100, 250);
    public static final Point LOCATION_ANALYSIS_LINE_CHART = new Point(100, 150);

    //default sizes of each window
    public static final Dimension SIZE_WATCHLIST_MGR = new Dimension(250, 350);
    public static final Dimension SIZE_WATCHLIST = new Dimension(900, 450);
    public static final Dimension SIZE_LOGGER = new Dimension(800, 150);
    public static final Dimension SIZE_MARKET = new Dimension(500, 300);
    public static final Dimension SIZE_SCANNER = new Dimension(1024, 768);
    public static final Dimension SIZE_DATAMGR = new Dimension(500, 600);
    public static final Dimension SIZE_RISKMGR = new Dimension(900, 700);
    public static final Dimension SIZE_SIMULATOR = new Dimension(1100, 700);
    public static final Dimension SIZE_THUMBNAIL = new Dimension(1100, 450);
    public static final Dimension SIZE_PLAN_SHEET = new Dimension(1100, 450);
    public static final Dimension SIZE_DAILY_CANDLE_CHART = new Dimension(500, 300);
    public static final Dimension SIZE_WEEKLY_CANDLE_CHART = new Dimension(500, 300);
    public static final Dimension SIZE_DAILY_LINE_CHART = new Dimension(500, 300);
    public static final Dimension SIZE_WEEKLY_LINE_CHART = new Dimension(500, 300);
    public static final Dimension SIZE_MONTHLY_LINE_CHART = new Dimension(500, 300);
    public static final Dimension SIZE_ANALYSIS_LINE_CHART = new Dimension(900, 600);

    //sequence: affects tooltip, icon position, icon image (below)
    public static final int INDEX_WATCHLIST_MGR = 0;
    public static final int INDEX_WATCHLIST = 1;
    public static final int INDEX_LOGGER = 2;
    public static final int INDEX_MARKET = 3;
    public static final int INDEX_SCANNER = 4;
    public static final int INDEX_RISKMGR = 5;
    public static final int INDEX_DATAMGR = 6;
    public static final int INDEX_SIMULATOR = 7;
    public static final int INDEX_THUMBNAIL = 8;
    public static final int INDEX_DAILY_CANDLE_CHART = 9;
    public static final int INDEX_WEEKLY_CANDLE_CHART = 10;
    public static final int INDEX_DAILY_LINE_CHART = 11;
    public static final int INDEX_WEEKLY_LINE_CHART = 12;
    public static final int INDEX_MONTHLY_LINE_CHART = 13;
    public static final int INDEX_ANALYSIS_LINE_CHART = 14;
    public static final int INDEX_PLAN_SHEET = 15;
    public static final int NUM_FRAMES = 16;

    //default app icon locations
//    public static final Icon[] LIST_APP_ICON = {
//        ApolloIcon.APP_MARKET, FrameworkIcon.RADAR, ApolloIcon.APP_RISK_MGR, ApolloIcon.APP_DATAMGR, ApolloIcon.APP_SIMULATOR,
//        ApolloIcon.APP_THUMBNAIL, FrameworkIcon.LINE_CHART_32, FrameworkIcon.BAR_CHART, FrameworkIcon.BAR_CHART, FrameworkIcon.BAR_CHART,
//    };
////    private static final String[] LIST_APP_TOOLTIP = {
////        ApolloConstants.APOLLO_BUNDLE.getString("app_01"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_02"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_03"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_04"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_05"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_06"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_07"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_08"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_09"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_10"),
////        ApolloConstants.APOLLO_BUNDLE.getString("app_11"),
////    };
////    //handles app icon dragging, save locations to preference
//////        _lblAppIcon = new JLabel[LIST_APP_ICON.length];
//////        Point[] icon_loc = ApolloPreferenceStore.getPreferences().getAppIconLocation();
//////        for (int i=0; i<LIST_APP_ICON.length; i++) {
//////            _lblAppIcon[i] = new JLabel(LIST_APP_ICON[i]);
//////            if (icon_loc == null || icon_loc[i] == null)
//////                _lblAppIcon[i].setLocation(LIST_ICON_LOCATION[i]);
//////            else
//////                _lblAppIcon[i].setLocation(icon_loc[i]);
//////            _lblAppIcon[i].addMouseListener(_IconMouseAdapter);
//////            _lblAppIcon[i].addMouseMotionListener(_IconMouseAdapter);
//////            _lblAppIcon[i].setSize(new Dimension(64, 64));
//////            WidgetUtil.attachToolTip(_lblAppIcon[i], LIST_APP_TOOLTIP[i], SwingConstants.CENTER, SwingConstants.TOP);
//////            _wDesktop.add(_lblAppIcon[i]);
//////        }
//////    private MouseAdapter _IconMouseAdapter = new MouseAdapter() {
//////        private int startx, starty;
//////        public void mousePressed(MouseEvent ev) {
//////            startx = ev.getX();
//////            starty = ev.getY();
//////        }
//////        public void mouseDragged(MouseEvent ev) {
//////            int dx = ev.getX() - startx;
//////            int dy = ev.getY() - starty;
//////            JComponent comp = (JComponent)ev.getSource();
//////            Point p = comp.getLocation();
//////            p.x += dx;
//////            p.y += dy;
//////            comp.setLocation(p);
//////
//////            //save to preference
//////            Point[] loc = new Point[_lblAppIcon.length];
//////            for (int i=0; i<loc.length; i++)
//////                loc[i] = _lblAppIcon[i].getLocation();
//////            ApolloPreferenceStore.getPreferences().setAppIconLocation(loc);
//////            ApolloPreferenceStore.savePreferences();
//////
//////        }
//////        public void mouseClicked(MouseEvent mev) {
//////            if (mev.getClickCount() == 2)//double click
//////                startApp(mev);
//////        }
//////    };
//////    private JLabel[] _lblAppIcon;
//////    private static final Point[] LIST_ICON_LOCATION = {
//////        new Point(0, 450), new Point(0, 500), new Point(0, 550), new Point(0, 600), new Point(0, 650),
//////        new Point(0, 700), new Point(0, 750), new Point(0, 800), new Point(0, 850), new Point(0, 950),
//////        new Point(0, 980)
//////    };
//    /**
//     * click app icon to activate different Apps
//     * @param mev MouseEvent associated with this app click
//     */
////    private void startApp(MouseEvent mev) {
////        JLabel lbl = (JLabel)mev.getSource();
////        for (int i = 0; i < LIST_APP_ICON.length; i++) {
////            if (lbl.getIcon().equals(FrameworkIcon.RADAR)) {
////                if (_frmScanner == null) {
////                    _frmScanner = new ScannerFrame();
////                    _wDesktop.add(_frmScanner);
////                }
////                startApp(_frmScanner);
////                return;
////            }
////            else if (lbl.getIcon().equals(ApolloIcon.APP_MARKET)) {
////                if (_frmMktView == null) {
////                    _frmMktView = new MarketViewFrame();
////                    _wDesktop.add(_frmMktView);
////                }
//////                _frmMktView.setVisible(true);
////                startApp(_frmMktView);
////                return;
////            }
////            else if (lbl.getIcon().equals(FrameworkIcon.GRAPH)) {
////                if (_frmAnalysis == null) {
////                    _frmAnalysis = new AnalysisGraphFrame();
////                    _wDesktop.add(_frmAnalysis);
////                }
////                _frmAnalysis.setVisible(true);
////                _wDesktop.getDesktopManager().activateFrame(_frmAnalysis);//bring to front
////                return;
////            }
////            else if (lbl.getIcon().equals(ApolloIcon.APP_DATAMGR)) {
////                if (_frmDataMgr == null) {
////                    _frmDataMgr = new DataMgrFrame();
////                    _wDesktop.add(_frmDataMgr);
////                }
////                startApp(_frmDataMgr);
////                return;
////            }
////            else if (lbl.getIcon().equals(ApolloIcon.APP_RISK_MGR)) {
////                if (_frmRiskMgr == null) {
////                    _frmRiskMgr = new RiskMgrFrame();
////                    _wDesktop.add(_frmRiskMgr);
////                }
////                startApp(_frmRiskMgr);
////                break;
////            }
////            else if (lbl.getIcon().equals(ApolloIcon.APP_SIMULATOR)) {
////                if (_frmSimulator == null) {
////                    _frmSimulator = SimulatorFrame.getInstance();
////                    _wDesktop.add(_frmSimulator);
////                }
////                startApp(_frmSimulator);
////                break;
////            }
////            else if (lbl.getIcon().equals(ApolloIcon.APP_THUMBNAIL)) {
////                if (_frmThumbnail == null) {
////                    _frmThumbnail = new ThumbNailFrame();
////                    _wDesktop.add(_frmThumbnail);
////                }
////                startApp(_frmThumbnail);
////                break;
////            }
////            else if (lbl.getIcon().equals(FrameworkIcon.LINE_CHART_32)) {
////                if (_frmWeeklyLineChart == null) {
////                    _frmWeeklyLineChart = new WeeklyLineChartFrame();
////                    _wDesktop.add(_frmWeeklyLineChart);
////                }
////                startApp(_frmWeeklyLineChart);
////            }
////            else if (lbl.getIcon().equals(FrameworkIcon.BAR_CHART)) {
////                if (_frmDailyLineChart == null) {
////                    _frmDailyLineChart = new DailyLineChartFrame();
////                    _wDesktop.add(_frmDailyLineChart);
////                }
////                startApp(_frmDailyLineChart);
////            }
////        }
////    }
//    //start app from popup menu
////    private void startApp(String menu_command) {
////        if (menu_command.equals(LIST_APP_TOOLTIP[0])) {
////            if (_frmMktView == null) {
////                _frmMktView = new MarketViewFrame();
////                _wDesktop.add(_frmMktView);
////            }
////            startApp(_frmMktView);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[1])) {
////            if (_frmScanner == null) {
////                _frmScanner = new ScannerFrame();
////                _wDesktop.add(_frmScanner);
////            }
////            startApp(_frmScanner);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[2])) {
////            if (_frmRiskMgr == null) {
////                _frmRiskMgr = new RiskMgrFrame();
////                _wDesktop.add(_frmRiskMgr);
////            }
////            startApp(_frmRiskMgr);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[3])) {
////            if (_frmDataMgr == null) {
////                _frmDataMgr = new DataMgrFrame();
////                _wDesktop.add(_frmDataMgr);
////            }
////            startApp(_frmDataMgr);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[4])) {
////            if (_frmSimulator == null) {
////                _frmSimulator = SimulatorFrame.getInstance();
////                _wDesktop.add(_frmSimulator);
////            }
////            startApp(_frmSimulator);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[5])) {
////            if (_frmThumbnail == null) {
////                _frmThumbnail = new ThumbNailFrame();
////                _wDesktop.add(_frmThumbnail);
////            }
////            startApp(_frmThumbnail);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[6])) {
////            if (_frmDailyLineChart == null) {
////                _frmDailyLineChart = new DailyLineChartFrame();
////                _wDesktop.add(_frmDailyLineChart);
////            }
////            startApp(_frmDailyLineChart);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[7])) {
////            if (_frmWeeklyLineChart == null) {
////                _frmWeeklyLineChart = new WeeklyLineChartFrame();
////                _wDesktop.add(_frmWeeklyLineChart);
////            }
////            startApp(_frmWeeklyLineChart);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[8])) {
////            if (_frmMonthlyLineChart == null) {
////                _frmMonthlyLineChart = new MonthlyLineChartFrame();
////                _wDesktop.add(_frmMonthlyLineChart);
////            }
////            startApp(_frmMonthlyLineChart);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[9])) {
////            if (_frmAnalysis == null) {
////                _frmAnalysis = new AnalysisGraphFrame();
////                _wDesktop.add(_frmAnalysis);
////            }
////            startApp(_frmAnalysis);
////        }
////        else if (menu_command.equals(LIST_APP_TOOLTIP[10])) {
////            if (_frmDailyCandleChart == null) {
////                _frmDailyCandleChart = new DailyCandleChartFrame();
////                _wDesktop.add(_frmDailyCandleChart);
////            }
////            startApp(_frmDailyCandleChart);
////        }
////    }
}