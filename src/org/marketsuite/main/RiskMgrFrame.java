package org.marketsuite.main;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.riskmgr.model.StopLevel;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.account.AccountViewPanel;
import org.marketsuite.riskmgr.model.StopLevelInfo;
import org.marketsuite.riskmgr.matrix.MatrixPanel;
import org.marketsuite.riskmgr.volatility.VolatilityPanel;
import org.marketsuite.riskmgr.portfolio.PortfolioPanel;
import jxl.Sheet;
import jxl.Workbook;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.riskmgr.account.AccountViewPanel;
import org.marketsuite.riskmgr.matrix.MatrixPanel;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevel;
import org.marketsuite.riskmgr.model.StopLevelInfo;
import org.marketsuite.riskmgr.portfolio.PortfolioPanel;
import org.marketsuite.riskmgr.volatility.VolatilityPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class RiskMgrFrame extends JInternalFrame {
    public RiskMgrFrame() {
        super(ApolloConstants.APOLLO_BUNDLE.getString("rm_16"), true, true, true, false);
        setName("Main");//for MainTabUI to recognize
        setFrameIcon(ApolloIcon.APP_ICON);

        //background
        JPanel content_pane = new JPanel(new MigLayout("insets 0")) {
            Color bg = new Color(32, 3, 2); // from the right end of the brand bar image
            ApolloIcon bkgnd = ApolloIcon.DEFAULT_BACKGROUND;
            final int icWidth = bkgnd.getIconWidth() - 1;   // the image has a darker 1 pixel border
            final int icHeight = bkgnd.getIconHeight();
            public void paintComponent(Graphics g) {
                Dimension size = getSize();
                int x = icWidth - 50;//100
                int y = icHeight - 2;//6
                // fill lower part of panel from row 2 tab down to status bar
                //   with color from small rectangle in upper left corner
                g.drawImage(bkgnd.getIcon().getImage(),
                    0, icHeight, size.width, size.height,
                    x, y, x + 2, y + 2, this);
                // draw regular image
                g.drawImage(bkgnd.getIcon().getImage(), 0, 0, this);
                // fill with right end image color in case window is wider than image
                // draw on top of original image since image has a 1 pixel darker border around it
                g.setColor(bg);
                g.fillRect(icWidth, 0, size.width - icWidth, icHeight);
            }
        };
        setContentPane(content_pane);

        //link - export to analyzer compatible files
        JPanel north_pnl = new JPanel(new MigLayout("insets 10 0 10 0", "push[][]10", "[]")); north_pnl.setOpaque(false);
        // height needs to be: (image height) - (tab height)
        north_pnl.add(_btnImportTsPos);
        WidgetUtil.attachToolTip(_btnImportTsPos, ApolloConstants.APOLLO_BUNDLE.getString("rm_02"), SwingConstants.LEFT, SwingConstants.TOP);
        _btnImportTsPos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //open multiple TradeStation positions files
                String trd_path = ApolloPreferenceStore.getPreferences().getTradeStationPath();
                JFileChooser fc = new JFileChooser(new File(trd_path == null ? FrameworkConstants.DATA_FOLDER_ACCOUNT : trd_path));
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory())
                            return true;

                        int pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_XLS);//spreadsheet
                        return (pos > 0 && file.getName().startsWith("Position"));
                    }

                    public String getDescription() {//this shows up in description field of dialog
                        return ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_70");
                    }
                });
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(true);
                fc.setAcceptAllFileFilterUsed(false);
                int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
                if (ret == JFileChooser.APPROVE_OPTION) {
                    ApolloPreferenceStore.getPreferences().setTradeStationPath(fc.getCurrentDirectory().getPath());
                    ApolloPreferenceStore.savePreferences();//flush
                    File[] sels = fc.getSelectedFiles();
                    importTradeStationPositions(sels);//this creates positions array

                    //refresh tabs with position data
                    _pnlAccount.populate();//generate portfolio matrix
                    _pnlMatrix.populate(RiskMgrModel.getInstance().getIndustryMatrix());
                }
            }
        });
        north_pnl.add(_btnImportTsCash);
        _btnImportTsCash.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {//read multiple files
                String trd_path = ApolloPreferenceStore.getPreferences().getTradeStationPath();
                JFileChooser fc = new JFileChooser(new File(trd_path == null ? FrameworkConstants.DATA_FOLDER_ACCOUNT : trd_path));
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory())
                            return true;

                        int pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_XLS);
                        return (pos > 0 && file.getName().startsWith("balance"));
                    }

                    public String getDescription() {//this shows up in description field of dialog
                        return ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_69");
                    }
                });
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(true);
                int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
                if (ret == JFileChooser.APPROVE_OPTION) {
                    //read files, update cash amount in summary strip
                    ApolloPreferenceStore.getPreferences().setTradeStationPath(fc.getCurrentDirectory().getPath());
                    ApolloPreferenceStore.savePreferences();//flush
                    File[] sels = fc.getSelectedFiles();
                    importTradeStationCashBalance(sels);//store in RiskMgrModel
                    _pnlAccount.updateSummary();
                }
            }
        });
        north_pnl.add(_btnSectorIndustry);
        _btnSectorIndustry.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pnlAccount.showSectorIndustry(_bShowSector);
                if (_bShowSector)
                    _pnlMatrix.populate(RiskMgrModel.getInstance().getSectorMatrix());
                else
                    _pnlMatrix.populate(RiskMgrModel.getInstance().getIndustryMatrix());
                _bShowSector = !_bShowSector;
RiskMgrModel.getInstance().setCashBalance(0);//may add preferred to total TODO temporary, remove later
            }
        });
        content_pane.add(north_pnl, "dock north");

        //center - tabs
        JTabbedPane tabs = new JTabbedPane(); tabs.setOpaque(false);
        tabs.add(ApolloConstants.APOLLO_BUNDLE.getString("rm_03"), _pnlAccount = new AccountViewPanel());
        tabs.add(ApolloConstants.APOLLO_BUNDLE.getString("rm_50"), _pnlMatrix = new MatrixPanel());
        tabs.add(ApolloConstants.APOLLO_BUNDLE.getString("rm_41"), _pnlVolatility = new VolatilityPanel());
        tabs.add(ApolloConstants.APOLLO_BUNDLE.getString("rm_21"), _pnlPort = new PortfolioPanel());
        content_pane.add(tabs, "dock center");
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_RISKMGR, MdiMainFrame.LOCATION_RISKMGR, MdiMainFrame.SIZE_RISKMGR);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);//NOTE: must have this to re-open window
        RiskMgrModel.getInstance();//pre-build custom group map
    }

    public boolean isDirty() { return _pnlAccount.isDirty(); }

    //-----private methods-----
    //read positions from selected files into memory stored in RiskMgrModel
    private void importTradeStationPositions(File[] files) {
        //read TradeStation files into memory
        RiskMgrModel.getInstance().clearPositions();//start out empty
        ArrayList<LogMessage> failed_msgs = new ArrayList<>();//might fail, keep a list of errors
        float cash_amt = 0;//RiskMgrModel.getInstance().getCashBalance();
        for (File pf : files) {
            Workbook wb;
            try {
                wb = Workbook.getWorkbook(pf);
            } catch (Exception e) {//fail to read somehow
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_17") + pf.getName() + "<br><br>", LoggingSource.RISKMGR_ACCOUNT, e);
                continue;
            }

            //read all rows from file, store info to RiskMgrModel's positions array
            ArrayList<Position> positions = RiskMgrModel.getInstance().getPositions();
            Sheet sheet = wb.getSheet(0);
            int row = ROW_TSP_SYMBOL;
            while (true) {
                String symbol = sheet.getCell(COLUMN_TSP_SYMBOL, row).getContents();
                if (symbol == null || symbol.equals(""))
                    break;

                //convert special symbols for Yahoo format, TradeStation calls BRK.B
                if (symbol.equals("BRK.B")) symbol = "BRK-B";

                try {//obtain market value first
                    String mkc = sheet.getCell(COLUMN_MARKET_VALUE, row).getContents();
                    Number mkt_val;
                    try {
                        mkt_val = DataUtil.CASH_POSITIVE_BALANCE_FORMAT.parse(mkc);
                    } catch (ParseException e) {//fail to parse
                        LogMessage lm = new LogMessage(LoggingSource.RISKMGR_ACCOUNT,
                            ApolloConstants.APOLLO_BUNDLE.getString("rm_70") + " " + symbol, e);
                        failed_msgs.add(lm);
                        row++;
                        continue;
                    }

                    //for preferred stocks, treat them as cash instead
                    boolean is_pfd = false;
                    for (String ps : DataUtil.PREFERRED_SYMBOLS) {
                        if (symbol.equals(ps)) {
                            cash_amt  += mkt_val.floatValue();//add to total cash
                            is_pfd = true;
                            break;
                        }
                    }
                    if (is_pfd) {
                        row++;
                        continue;//skip this symbol, go to next
                    }

                    //for normal symbols, read shares, cost, account, mkt value and stop
                    int shares = Integer.parseInt(sheet.getCell(COLUMN_TSP_QTY, row).getContents());
                    String contents = sheet.getCell(COLUMN_TSP_COST, row).getContents();
                    float cost = 0;
                    try {//wrong format, use 0 as cost
                        cost = FLOAT_FORMAT.parse(contents).floatValue();
                    } catch (ParseException e) {
                        LogMessage lm = new LogMessage(LoggingSource.RISKMGR_ACCOUNT,
                            ApolloConstants.APOLLO_BUNDLE.getString("rm_66") + " " + symbol, e);
                        failed_msgs.add(lm);
                    }
                    String acct = "xx1001";
                    if (pf.getName().contains("466")) acct = "xx466";
                    else if (pf.getName().contains("391")) acct = "xx391";
                    else if (pf.getName().contains("516")) acct = "xx516";
                    else if (pf.getName().contains("861")) acct = "xx861";

                    //look for stops from map read from file stored previously, symbol not found, use -5% as stop
                    float stop = (1 - RiskMgrModel.DEFAULT_STOP_PCT) * cost;
                    HashMap<String, StopLevel> stops = RiskMgrModel.getInstance().getStopMap();
                    StopLevel stored_stop = stops.get(symbol);
                    if (stored_stop != null) //found from previous
                        stop = (float)stored_stop.getLevel();
                    float risk = shares * (cost - stop);
                    if (cost <= stop)//in profit
                        risk = 0;
                    else
                        risk = -risk;
                    StopLevelInfo sli = new StopLevelInfo(symbol, cost, shares, stop, 150);//150 appropriate
                    Position pos = new Position(symbol, shares, cost, stop, risk, 0, mkt_val.floatValue(), acct, sli);
                    positions.add(pos);
                } catch (IOException e) {//can't read quotes
                    LogMessage lm = new LogMessage(LoggingSource.RISKMGR_ACCOUNT, symbol + ApolloConstants.APOLLO_BUNDLE.getString("rm_91"), e);
                    failed_msgs.add(lm);
                } catch (IllegalArgumentException iae) {
                    LogMessage lm = new LogMessage(LoggingSource.RISKMGR_ACCOUNT, symbol + ApolloConstants.APOLLO_BUNDLE.getString("rm_68"), iae);
                    failed_msgs.add(lm);
                }
                row++;
            }
            wb.close();
        }
        RiskMgrModel.getInstance().setCashBalance(cash_amt);//may add preferred to total

        //inform user about errors
        if (failed_msgs.size() > 0)
            Props.Log.setValue(null, failed_msgs);
    }
    private void importTradeStationCashBalance(File[] files) {
        float total_cash = RiskMgrModel.getInstance().getCashBalance();
        for (File pf : files) {
            Workbook wb;
            try {
                wb = Workbook.getWorkbook(pf);
                Sheet sheet = wb.getSheet(0);
                String str = sheet.getCell(RiskMgrModel.COLUMN_BALANCE_CELL, RiskMgrModel.ROW_BALANCE_CELL).getContents();
                float cash = DataUtil.CASH_POSITIVE_BALANCE_FORMAT.parse(str).floatValue();
                total_cash += cash;
            } catch (Exception e) {//fail to read somehow
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_17") + pf.getName(), LoggingSource.RISKMGR_ACCOUNT, e);
            }
        }
        RiskMgrModel.getInstance().setCashBalance(total_cash);
    }

    //-----variables-----
    private AccountViewPanel _pnlAccount;
    private VolatilityPanel _pnlVolatility;
    private PortfolioPanel _pnlPort;
    private JButton _btnImportTsPos = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_04"), FrameworkIcon.FILE_OPEN);
    private JButton _btnImportTsCash = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_05"), FrameworkIcon.FILE_OPEN);
    private JButton _btnSectorIndustry = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_60"));
    private boolean _bShowSector = true;//false = industry view
    private MatrixPanel _pnlMatrix;

    //assumptions for TradeStation position files
    public static final int COLUMN_TSP_SYMBOL = 0;
    public static final int COLUMN_TSP_QTY = 1;
    public static final int COLUMN_TSP_COST = 5;
    private static final int COLUMN_MARKET_VALUE = 7;//0 based
    public static final int ROW_TSP_SYMBOL = 4;//A4 to ...
    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("#,###.##");
//    private static final String[] PREFERRED_SYMBOLS = {
//        "ARR.PB", "NLY.PC", "PSA.PO", "PSA.PX", "PSB.PT", "JPM.PC"
//    };
}