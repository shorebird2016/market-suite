package org.marketsuite.scanner.report;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.data.CandleSignals;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.data.IndicatorRepository;
import org.marketsuite.framework.model.indicator.CCI;
import org.marketsuite.framework.model.indicator.Ichimoku;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.*;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//container for reporting scanner output from a set of pre-defined criteria
public class ScannerReportPanel extends JPanel {
    public ScannerReportPanel() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
            new MigLayout("insets 0", "[][]10[][]push[]20[][]5", "3[]3")); north_pnl.setOpaque(false);
        north_pnl.add(_cmbWatchlist = new JComboBox<>(_cmlWatchlist));
        _cmbWatchlist.setMaximumSize(new Dimension(150, 20));//limit width of watch list
        _cmbWatchlist.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                _sCurWatchlist = (String) _cmbWatchlist.getSelectedItem();
            }
        });
        north_pnl.add(_cmbType);
        north_pnl.add(_fldPct); north_pnl.add(new JLabel("%")); _fldPct.setValue(2);
        north_pnl.add(_lblCount);
        north_pnl.add(_btnScan); _btnScan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { scanSymbols(); } });
        north_pnl.add(_btnGenWatchlist); _btnScan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        add(north_pnl, "dock north");

        //center - table
        _tmReport = new ReportTableModel();
        _tblReport = WidgetUtil.createDynaTable(_tmReport, ListSelectionModel.SINGLE_INTERVAL_SELECTION,
            new HeadingRenderer(), false, new ReportRenderer());
        _tblReport.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        add(new JScrollPane(_tblReport), "dock center");
        _tblReport.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int sel = _tblReport.getSelectedRow();
                if (sel < 0) return;
                sel = _tblReport.convertRowIndexToModel(sel);//for sorting

                //notify chart windows to change
                String sym = (String) _tmReport.getCell(sel, COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(sym);
            }
        });

        //prefill with all the watch list groups
        ArrayList<String> groups = GroupStore.getInstance().getGroupNames();
        _cmlWatchlist.removeAllElements();
        _cmlWatchlist.addElement("All Symbols");
        for (int idx = 0; idx < groups.size(); idx++)
            if (!groups.get(idx).equals(""))
                _cmlWatchlist.addElement(groups.get(idx));
    }

    //----- private methods -----
    private void scanSymbols() {
        //show initial progress bar
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(),
            ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_pb1"));
        pb.setVisible(true);
        final HashMap<String,Fundamental> fm = MainModel.getInstance().getFundamentals();

        //scan inside a thread
        Thread scan_thread = new Thread() {
            public void run() {
                boolean add_row = false;
                boolean scan_stock = _cmbType.getSelectedIndex() == 0;
                ArrayList<String> symbols;
                String wl_name = (String)_cmbWatchlist.getSelectedItem();
                if (wl_name.equals("All Symbols"))
                    symbols = DataUtil.getAllSymbolsInDb();
                else
                    symbols = GroupStore.getInstance().getMembers(wl_name);

                //filter by type
                _tmReport.clear();
                for (final String symbol : symbols) {
                    if (fm.get(symbol) != null) {
                        boolean etf = fm.get(symbol).isETF();
                        if (scan_stock && etf) continue;
                        else if (!scan_stock && !etf) continue;
                    }
                    final IndicatorRepository tr;
                    try {
                        tr = new IndicatorRepository(symbol, Timeframe.Daily, 480);
                    } catch (Exception e1) {
                        System.err.println(e1.getMessage());//skip exception
                        continue;
                    }
                    SMA sma200d = tr.getSma200d(); SMA sma50d = tr.getSma50d();
                    if (sma200d == null || sma50d == null) continue;//skip
                    MarketTrend trend_200sma = sma200d.getTrend(60, 0);//~3 month slope, need to be up
                    MarketTrend trend_50sma = sma50d.getTrend(60, 0);//same but allow flat slope
                    if (!trend_200sma.equals(MarketTrend.Up) || trend_50sma.equals(MarketTrend.Down))//filter out slope not up
                        continue;

                    //add new row in EDT
                    add_row = true;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
System.err.print("==> " + symbol);
                            _tmReport.addRow(tr, (float) _fldPct.getValue() / 100);
                        }
                    });
                }
                if (add_row) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() { _tmReport.fireTableDataChanged(); _lblCount.setText("#" + _tmReport.getRowCount()); } });
                }
                EventQueue.invokeLater(new Runnable() { public void run() { pb.setVisible(false); } });
            }
        };
        scan_thread.start();

    }

    //----- inner classes -----
    private class ReportTableModel extends DynaTableModel {
        private ReportTableModel() { super(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public void populate() {
            _lstRows.clear();
//            if (_bShowBullish) {
//                MovingAverageType mvt = (MovingAverageType)_cmbClusterMa.getSelectedItem();
//                populate(filterSymbols(mvt, 0, 1));
//                populate(filterSymbols(mvt, 1, 2));
//                populate(filterSymbols(mvt, 2, 3));
//            }
//            else {//all in the group
//                ArrayList<String> symbols = MainModel.getInstance().getWatchListModel().getMembers();
//                Collections.sort(symbols);
//                populate(symbols);
//            }
//            _tblReport.setRowHeight(25);//fit 32x32 icon
            fireTableDataChanged();
        }
        public boolean isCellEditable(int row, int column) { return false; }
        private SimpleCell[] initCells() {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            for (int col=0; col<TABLE_SCHEMA.length; col++) {
                switch (col) {
                    case COLUMN_SYMBOL:
                    case COLUMN_PHASE:
                    case COLUMN_FIBR_50:
                    case COLUMN_KUMOB:
                    default:
                        cells[col] = new SimpleCell("");
                        break;
                    case COLUMN_T_LINE:
                    case COLUMN_10MA:
                    case COLUMN_40MA:
                    case COLUMN_KIJUN:
                    case COLUMN_KUMOA:
                        cells[col] = new SimpleCell(new Double(0));
                        break;

                    case COLUMN_ABOVE_CLOUD:
                    case COLUMN_CCI_CROSS_OS:
                    case COLUMN_CCI_CROSS_ZERO:
                        cells[col] = new SimpleCell(new Boolean(false));
                        break;
                }
            }
            return cells;
        }
        //based on technical information, determine whether a new row is to be added
        private void addRow(IndicatorRepository tr, float threshold) {
            ArrayList<FundQuote> quotes = tr.getFund().getQuote();
            float price = quotes.get(0).getClose();
            SimpleCell[] cells = initCells();
            cells[COLUMN_SYMBOL].setValue(quotes.get(0).getSymbol());
//System.err.println("---> " + cells[COLUMN_SYMBOL].getValue());

            //any condition exceed threshold, add this row
            boolean hit = false;
            float sma50 = tr.getSma50d().getSma()[0];
            float delta = AppUtil.deltaExceedThreshold(price, sma50, threshold);
            if (delta > 0 /*!= Float.NaN*/) { cells[COLUMN_10MA].setValue(new Double(delta)); hit = true; }
            float sma200 = tr.getSma200d().getSma()[0];
            delta = AppUtil.deltaExceedThreshold(price, sma200, threshold);
            if (delta > 0 /*!= Float.NaN*/) { cells[COLUMN_40MA].setValue(new Double(delta)); hit = true; }
            MarketPhase phase = tr.getPhase()[0];
            if (phase != MarketPhase.Bearish && phase != MarketPhase.Distribution) { cells[COLUMN_PHASE].setValue(phase); /*hit = true;*/ }
            Ichimoku ichi = tr.getIchimoku();
//TODO ichi may not be correct
            if (ichi.isAboveCloud(0)) { cells[COLUMN_ABOVE_CLOUD].setValue(new Boolean(true)); /*hit = true;*/ }
            delta = AppUtil.deltaExceedThreshold(price, ichi.getKijun()[0], threshold);
            if (delta > 0 /*!= Float.NaN*/) { cells[COLUMN_KIJUN].setValue(new Double(delta)); hit = true; }
            delta = AppUtil.deltaExceedThreshold(price, ichi.getKumoA()[0], threshold);
            if (delta > 0 /*!= Float.NaN*/) { cells[COLUMN_KUMOA].setValue(new Double(delta)); hit = true; }
            delta = AppUtil.deltaExceedThreshold(price, ichi.getKumoB()[0], threshold);
            if (delta > 0 /*!= Float.NaN*/) { cells[COLUMN_KUMOB].setValue(new Double(delta)); hit = true; }
            CCI cci = tr.getCci();
            boolean cos = (cci.getCci()[1] < -100) && (cci.getCci()[0] >= -100);
            if (cos) { cells[COLUMN_CCI_CROSS_OS].setValue(new Boolean(cos)); hit = true; }
            boolean cz = (cci.getCci()[1] < 0) && (cci.getCci()[0] >= 0);
            if (cz) { cells[COLUMN_CCI_CROSS_ZERO].setValue(new Boolean(cz)); hit = true; }

            if (hit) {
                _lstRows.add(cells); //int row = getRowCount() - 1;
            }
        }

        //populate a set of symbols and a blank separator
        private void populate(ArrayList<String> symbols) {
            for (String symbol : symbols) {
                SimpleCell[] cells = initCells();
                cells[COLUMN_SYMBOL].setValue(symbol);
                WatchListModel wlm = MainModel.getInstance().getWatchListModel();
                MarketInfo mki = wlm.getMarketInfo(symbol); if (mki == null) continue;

                //candle cell
                CandleSignals css = new CandleSignals(mki.getFund().getQuote(), 20);//only scan past 20 days
                ArrayList<CandleSignal> signals = css.getSignals(5);//get 5 day signals
                cells[COLUMN_CCI_CROSS_OS].setValue(signals);

                //rating cells
                WeeklyQuote wq = new WeeklyQuote(mki.getFund(), 60);//2 months = 8 weeks
                ArrayList<IbdRating> ratings;
                try {
                    ratings = IbdRating.readIbdWeeklyRating(mki.getFund(), 60);
                    cells[COLUMN_CCI_CROSS_ZERO].setValue(ratings);
                } catch (IOException e) {
                    System.err.println(e.getMessage());//TODO send to log window
                    continue;//Ok no IBD rating file
                }

                //phase cell
                String ph = mki.getCurrentPhase();
                if (ph != null) cells[COLUMN_PHASE].setValue(ph);

                //mean reversion cell
//                int mrp = calcMeanReversion(MovingAverageType.T_LINE, symbol, true);
//                int mrm = calcMeanReversion(MovingAverageType.T_LINE, symbol, false);
//                StringBuilder buf = new StringBuilder();
//                if (mrp > 0) buf.append("+").append(String.valueOf(mrp));
//                else if (mrm > 0) buf.append("-").append(String.valueOf(mrm));
//                if (buf.length() > 0)
//                    cells[COLUMN_KUMOB].setValue(buf.toString());

                //MA cells
                FundData fund = mki.getFund();
                float close = fund.getQuote().get(0).getClose();
                float tline = mki.getEma8()[0];
                float pct = 100 * (close - tline) / close;
                cells[COLUMN_T_LINE].setValue(new Double(pct));
                float sma20 = mki.getSma20()[0];
                pct = 100 * (close - sma20) / close;
                cells[COLUMN_10MA].setValue(new Double(pct));
                float sma50 = mki.getSma50()[0];
                pct = 100 * (close - sma50) / close;//);
                cells[COLUMN_40MA].setValue(new Double(pct));
                if (mki.getSma200() != null) {
                    float sma200 = mki.getSma200()[0];
                    pct = 100 * (close - sma200) / close;
                    cells[COLUMN_KIJUN].setValue(new Double(pct));
                }

                //MACD and DSTO
                cells[COLUMN_ABOVE_CLOUD].setValue(new Double(mki.getDstoStd()[0]));
                cells[COLUMN_KUMOA].setValue(new Double(mki.getMacd()[0]));
                _lstRows.add(cells);
            }
        }
        //obtain a subset of symbols with closing price near 50SMA
        private ArrayList<String> filterSymbols(MovingAverageType mvt, float thresh_pct1, float thresh_pct2) {
            ArrayList<String> ret = new ArrayList<>();
            WatchListModel wlm = MainModel.getInstance().getWatchListModel();
            ArrayList<String> members = wlm.getMembers();
            for (String symbol : members) {
                MarketInfo mki = wlm.getMarketInfo(symbol); if (mki == null) continue;
                FundData fund = mki.getFund();
                float close = fund.getQuote().get(0).getClose();
                float ma;
                switch (mvt) {
                    case SMA_20:
                        ma = mki.getSma20()[0];
                        break;

                    case SMA_50:
                    default:
                        if (mki.getSma50() == null) continue;
                        ma = mki.getSma50()[0];
                        break;

                    case SMA_200:
                        if (mki.getSma200() == null) continue;
                        ma = mki.getSma200()[0];
                        break;
                }
                float pct = Math.abs(100 * (close - ma) / close);
                if (pct >= thresh_pct1 && pct <= thresh_pct2)
                    ret.add(symbol);
            }
            return ret;
        }
    }
    private class ReportRenderer extends DynaTableCellRenderer {
        private ReportRenderer() { super(_tmReport); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cmp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Double) {
                double v1 = (Double)value;
                if (v1 <= 0)
                    ((JLabel)cmp).setText("");
                else {
                    ((JLabel)cmp).setText(FrameworkConstants.PCT2_FORMAT.format(v1));
                }
            }
            return cmp;
        }
    }

    //----- variables -----
    private JComboBox<String> _cmbWatchlist, _cmbType = new JComboBox<>(TYPE);
    private DefaultComboBoxModel<String> _cmlWatchlist = new DefaultComboBoxModel<>();
    private DecimalField _fldPct = new DecimalField(2, 3, 0, 100, null);
    private JLabel _lblCount = new JLabel("#");
    private JButton _btnScan = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_scan"), FrameworkIcon.RUN);
    private JButton _btnGenWatchlist = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_wl"), FrameworkIcon.WATCH);
    private String _sCurWatchlist;
    private JTable _tblReport;
    private ReportTableModel _tmReport;

    //----- literals -----
    private static final String[] TYPE = new String[] {"Stocks", "ETF"};
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_10MA = 1;
    private static final int COLUMN_40MA = 2;
    private static final int COLUMN_KIJUN = 3;
    private static final int COLUMN_KUMOA = 4;
    private static final int COLUMN_KUMOB = 5;
    private static final int COLUMN_FIBR_50 = 6;
    private static final int COLUMN_PHASE = 7;
    private static final int COLUMN_ABOVE_CLOUD = 8;
    private static final int COLUMN_CCI_CROSS_OS = 9;
    private static final int COLUMN_CCI_CROSS_ZERO = 10;
    private static final int COLUMN_WSTO = 11;
    private static final int COLUMN_T_LINE = 12;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("scnprt_sym"),   ColumnTypeEnum.TYPE_STRING, 1, 50, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_50M"),   ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null},//50MA
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_200M"), ColumnTypeEnum.TYPE_DOUBLE, -1,  60, null, null, null},//200MA
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_kijun"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//kijun
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_ka"),     ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//kumo A
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_kb"),    ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//kumo B
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_fib50"),   ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//Fib 50%
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_ph"),    ColumnTypeEnum.TYPE_STRING, -1, 60, null, null, null},//phase
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_above_cloud"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 60, null, null, null},//> kumo
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_cci1"),    ColumnTypeEnum.TYPE_BOOLEAN, -1, 70, null, null, null},//CCI cross -100
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_cci2"),   ColumnTypeEnum.TYPE_BOOLEAN, -1, 60, null, null, null},//CCI cross zero
        {ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_wsto"),   ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//WSTO
    };
}

//TODO slope filtering still need work, use visual chart to compare slope visually
//TODO sorting
//TODO display slope in columns for checking
