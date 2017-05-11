package org.marketsuite.planning;

import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.data.CandleSignals;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.MovingAverageType;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.marektview.ranking.RankElement;
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
import java.util.Collections;
import java.util.HashMap;

/**
 * A reporting tool to order the various symbols from specified watch list according to their
 * possibility of success based on C R T method.
 */
public class ReportPanel extends JPanel {
    public ReportPanel() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
            new MigLayout("insets 0", "5[][]10[][][]push[]5[]5", "3[]3")); north_pnl.setOpaque(false);
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("tn_02")));
        north_pnl.add(_cmbGrpSelector = new JComboBox<>(_cmlGrpSelector));
        _cmbGrpSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                _sCurWatchlist = (String) _cmbGrpSelector.getSelectedItem();
                WatchListModel model = new WatchListModel(_sCurWatchlist, false);
                MainModel.getInstance().setWatchListModel(model);
                _tmReport.populate();
            }
        });
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_show")));
        north_pnl.add(_cmbGrpShow); _cmbGrpShow.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                _bShowBullish = _cmbGrpShow.getSelectedIndex() == 0;
                _cmbClusterMa.setEnabled(_bShowBullish);
                _tmReport.populate();
            }
        });
        north_pnl.add(_cmbClusterMa); _cmbClusterMa.setSelectedIndex(1);
        _cmbClusterMa.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                _tmReport.populate();
            }
        });
        north_pnl.add(_btnOption); _btnOption.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ReportOptionDialog dlg = new ReportOptionDialog(!_bShowDoji, _bShowBullCandle, _bShowBearCandle);
                if (dlg.isCancelled()) return;
                _bShowDoji = dlg.showDoji(); _bShowBullCandle = dlg.showBull(); _bShowBearCandle = dlg.showBear();
                repaint();
            }
        });
        north_pnl.add(_btnGenWatchList); _btnGenWatchList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //get list from table, save into watch list
                if (_tmReport.getRowCount() == 0) {
                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("ibd_12"));
                    return;
                }

                //ask name
                NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                if (dlg.isCancelled()) return;
                String name = dlg.getEntry();

                //check duplicate list name
                if (GroupStore.getInstance().isGroupExist(name)) {
                    if (!WidgetUtil.confirmOkCancel(MdiMainFrame.getInstance(),
                        ApolloConstants.APOLLO_BUNDLE.getString("trk_15"))) return;
                }

                //collect symbols, remove duplicate
                ArrayList<String> list = new ArrayList<>();
                for (int row = 0; row < _tmReport.getRowCount(); row++) {
                    String symbol = (String)_tmReport.getCell(row, COLUMN_SYMBOL).getValue();
                    if (!list.contains(symbol))
                        list.add(symbol);
                }
                GroupStore.getInstance().addGroup(name, list);//empty group persist
                Props.WatchListsChange.setChanged();//notify watch list manager
            }
        });
        add(north_pnl, "dock north");

        //center - table
        _tmReport = new ReportTableModel();
        _tblReport = new JTable(_tmReport) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                //traverse all rows and draw line at group boundaries: 20/50/200MA
                int y = -1; double prev_pct = 0;
                int rows = getRowCount() - 1;
                for (int row = 0; row < rows; row++) {
                    int col;
                    switch ((MovingAverageType)_cmbClusterMa.getSelectedItem()) {
                        case SMA_20: col = COLUMN_20MA; break;
                        case SMA_50: default: col = COLUMN_50MA; break;
                        case SMA_200: col = COLUMN_200MA; break;
                    }
                    double pct = Math.abs((Double)_tmReport.getCell(row, col).getValue());
                    if (pct >= 1 && prev_pct < 1 && _bShowBullish)
                        g.drawLine(0, y, getSize().width, y);
                    if (pct >= 2 && prev_pct < 2 && _bShowBullish)
                        g.drawLine(0, y, getSize().width, y);
                    y += getRowHeight(row);
                    prev_pct = pct;
                }
            }
            private static final long serialVersionUID = -1L;
        };
        WidgetUtil.initDynaTable(_tblReport, _tmReport, ListSelectionModel.SINGLE_INTERVAL_SELECTION,
                new HeadingRenderer(), false, new DynaTableCellRenderer(_tmReport));
        _tblReport.setDefaultRenderer(String.class, new ReportRenderer(_tmReport));
        _tblReport.setDefaultRenderer(Double.class, new ReportRenderer(_tmReport));

        //handle cell selection
        _tblReport.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int sel = _tblReport.getSelectedRow(); if (sel < 0) return;
                Object val = _tmReport.getCell(sel, COLUMN_SYMBOL).getValue();
                if (val.equals("")) return;
                Props.SymbolSelection.setValue(null, val);
            }
        });
        add(new JScrollPane(_tblReport), "dock center");

        //initialize
        initGroupSelector();
        _sCurWatchlist = (String)_cmbGrpSelector.getSelectedItem();
    }

    //----- private methods -----
    private void updateRowHeights() {
        for (int row = 0; row < _tblReport.getRowCount(); row++) {
            int rowHeight = _tblReport.getRowHeight();
            for (int column = 0; column < _tblReport.getColumnCount(); column++) {
                Component comp = _tblReport.prepareRenderer(_tblReport.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            _tblReport.setRowHeight(row, rowHeight);
        }
    }
    //format a string from CellInfo and percentage to nicely presented as HTML
    private String cellToHtml(ArrayList<RankElement> recent_rank, HashMap<String, ArrayList<Integer>> hist_map, float percentage) {
        if (recent_rank == null || hist_map == null)
            return "";
        StringBuilder buf = new StringBuilder("<html>");
        int max = (int)(recent_rank.size() * percentage);
        if (max < 3) max = 3;//minimum 3 symbols
        int count = 0;
        for (RankElement re : recent_rank) {
            //each row has symbol, percent
            buf.append("<b>").append(re.symbol).append("</b> :&nbsp;").
                append(FrameworkConstants.PCT_FORMAT.format(re.pct));

            //followed by historical ranks in parenthesis from earlier to recent
//TODO remove hard number 3
            ArrayList<Integer> hr = hist_map.get(re.symbol);
            int idx = 3;
            buf.append("&nbsp;&nbsp;(");
            do {
                buf.append(hr.get(idx));
                if (idx > 0)//skip last comma
                    buf.append(", ");
                idx--;
            }while (idx >= 0);
            buf.append(")");
            buf.append("<br>");//for next symbol
            count++;
            if (count >= max)
                break;
        }
        return buf.toString();
    }
    private void initGroupSelector() {
        //prefill with all the watch list groups
        ArrayList<String> groups = GroupStore.getInstance().getGroupNames();
        _cmlGrpSelector.removeAllElements();
        for (int idx = 0; idx < groups.size(); idx++)
            _cmlGrpSelector.addElement(groups.get(idx));
    }
    //calculate mean reversion from given moving average line for a symbol in watch list, return -1 = unable to calculate
    // above = true for consecutive recent days above T line including today, above = false for below days
    private int calcMeanReversion(MovingAverageType mat, String symbol, boolean above) {
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        MarketInfo mki = wlm.getMarketInfo(symbol); if (mki == null) return -1;
        switch (mat) {
            case T_LINE:
                float[] t_line = mki.getEma8(); int count = 0;
                ArrayList<FundQuote> quotes = mki.getFund().getQuote();
                if (above) {
                    if (quotes.get(0).getClose() < t_line[0]) return -1;//today p<t, found nothing
                    count = 1;//today already
                    int idx = 1;
                    do {
                        float price = quotes.get(idx).getClose();
                        if (price >= t_line[idx]) count++;
                        else break;//stop counting
                        idx++;
                    }while (idx < 10);
                }
                else {
                    if (quotes.get(0).getClose() > t_line[0]) return -1;//today p>t, found nothing
                    count = 1;//today already
                    int idx = 1;
                    do {
                        float price = quotes.get(idx).getClose();
                        if (price <= t_line[idx]) count++;
                        else break;//stop counting
                        idx++;
                    }while (idx < 10);
                }
                return count;
        }
        return -1;
    }

    //----- inner classes -----
    private class ReportTableModel extends DynaTableModel {
        private ReportTableModel() { super(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public void populate() {
            _lstRows.clear();
            if (_bShowBullish) {
                MovingAverageType mvt = (MovingAverageType)_cmbClusterMa.getSelectedItem();
                populate(filterSymbols(mvt, 0, 1));
                populate(filterSymbols(mvt, 1, 2));
                populate(filterSymbols(mvt, 2, 3));
            }
            else {//all in the group
                ArrayList<String> symbols = MainModel.getInstance().getWatchListModel().getMembers();
                Collections.sort(symbols);
                populate(symbols);
            }
            _tblReport.setRowHeight(25);//fit 32x32 icon
            fireTableDataChanged();
        }
        public boolean isCellEditable(int row, int column) { return false; }
        private SimpleCell[] initCells() {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            for (int col=0; col<TABLE_SCHEMA.length; col++) {
                switch (col) {
                    case COLUMN_SYMBOL:
                    case COLUMN_CANDLE_SIGNAL:
                    case COLUMN_PHASE:
                    case COLUMN_COMP_RS_RATING:
                    case COLUMN_ACC_DIS_RATING:
                    case COLUMN_MEAN_REVERSION:
                    default:
                        cells[col] = new SimpleCell("");
                        break;
                    case COLUMN_T_LINE:
                    case COLUMN_20MA:
                    case COLUMN_50MA:
                    case COLUMN_200MA:
                    case COLUMN_DSTO:
                    case COLUMN_MACD:
                        cells[col] = new SimpleCell(new Double(0));
                        break;
                }
            }
            return cells;
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
                cells[COLUMN_CANDLE_SIGNAL].setValue(signals);

                //rating cells
                WeeklyQuote wq = new WeeklyQuote(mki.getFund(), 60);//2 months = 8 weeks
                ArrayList<IbdRating> ratings;
                try {
                    ratings = IbdRating.readIbdWeeklyRating(mki.getFund(), 60);
                    cells[COLUMN_COMP_RS_RATING].setValue(ratings);
                } catch (IOException e) {
                    System.err.println(e.getMessage());//TODO send to log window
                    continue;//Ok no IBD rating file
                }

                //phase cell
                String ph = mki.getCurrentPhase();
                if (ph != null) cells[COLUMN_PHASE].setValue(ph);

                //mean reversion cell
                int mrp = calcMeanReversion(MovingAverageType.T_LINE, symbol, true);
                int mrm = calcMeanReversion(MovingAverageType.T_LINE, symbol, false);
                StringBuilder buf = new StringBuilder();
                if (mrp > 0) buf.append("+").append(String.valueOf(mrp));
                else if (mrm > 0) buf.append("-").append(String.valueOf(mrm));
                if (buf.length() > 0)
                    cells[COLUMN_MEAN_REVERSION].setValue(buf.toString());

                //MA cells
                FundData fund = mki.getFund();
                float close = fund.getQuote().get(0).getClose();
                float tline = mki.getEma8()[0];
                float pct = 100 * (close - tline) / close;
                cells[COLUMN_T_LINE].setValue(new Double(pct));
                float sma20 = mki.getSma20()[0];
                pct = 100 * (close - sma20) / close;
                cells[COLUMN_20MA].setValue(new Double(pct));
                float sma50 = mki.getSma50()[0];
                pct = 100 * (close - sma50) / close;//);
                cells[COLUMN_50MA].setValue(new Double(pct));
                if (mki.getSma200() != null) {
                    float sma200 = mki.getSma200()[0];
                    pct = 100 * (close - sma200) / close;
                    cells[COLUMN_200MA].setValue(new Double(pct));
                }

                //MACD and DSTO
                cells[COLUMN_DSTO].setValue(new Double(mki.getDstoStd()[0]));
                cells[COLUMN_MACD].setValue(new Double(mki.getMacd()[0]));
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
        private ReportRenderer(DynaTableModel model) { super(model); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setFont(FrameworkConstants.SMALL_FONT); lbl.setToolTipText("");
            String symbol = (String)_tmReport.getCell(row, COLUMN_SYMBOL).getValue();
            if (symbol.equals("")) {
                lbl.setText("");
                lbl.setToolTipText("");
                return lbl;
            }
            WatchListModel wlm = MainModel.getInstance().getWatchListModel();
            MarketInfo mki = wlm.getMarketInfo(symbol);
            if (mki == null) return lbl;//skip showing
            switch (column) {
                case COLUMN_CANDLE_SIGNAL:
                    //add icons based on each signal to panel here as renderer
                    JPanel pnl = new JPanel(new MigLayout("insets 0", "[]")); pnl.setOpaque(true);
                    ArrayList<CandleSignal> signals = (ArrayList<CandleSignal>)value;//from cell
                    if (signals == null) break;
                    StringBuilder buf = new StringBuilder("");
                    for (CandleSignal cs : signals) {
                        if (!_bShowDoji && cs.equals(CandleSignal.DojiTop)) continue;
                        if (_bShowBullCandle && CandleSignal.isBearish(cs)) continue;
                        if (_bShowBearCandle && CandleSignal.isBullish(cs)) continue;
                        pnl.add(new JLabel(cs.getCandleIcon()));
                        buf.append(cs.toString()).append(", ");
                    }
                    pnl.setToolTipText(CoreUtil.wrapText(buf.toString(), 20));
                    return pnl;

                case COLUMN_COMP_RS_RATING://use stored IbdRating object to decide hook-up or hook-down
                    lbl.setText("");
                    ArrayList<IbdRating> ratings = (ArrayList<IbdRating>)value;
                    if (ratings.size() <= 2) break;
                    if (ratings.size() == 0) break;
                    if (IbdRating.doCompRsHookup(ratings))
                        lbl.setIcon(FrameworkIcon.TREND_UP_1);
                    else if (IbdRating.doCompRsHookdown(ratings))
                        lbl.setIcon(FrameworkIcon.TREND_DOWN_1);
                    break;

                case COLUMN_ACC_DIS_RATING://color text w red if rating less than B
                    ratings = (ArrayList<IbdRating>)_tmReport.getCell(row, COLUMN_COMP_RS_RATING).getValue();
                    if (ratings == null || ratings.size() == 0) break;
                    buf = new StringBuilder(" "); boolean warn = false;
                    String grp_rating = ratings.get(0).getGroupRating();
                    if (grp_rating.compareTo("D") >= 0)
                        warn = true;
                    buf.append(grp_rating); if (grp_rating.length() == 1) buf.append(" ");//extra blank
                    String acc_dis = ratings.get(0).getAccDis();
                    if (acc_dis.compareTo("D") >= 0)
                        warn = true;
                    buf.append("   ").append(acc_dis);
                    lbl.setFont(FrameworkConstants.FONT_STANDARD);
                    lbl.setText(buf.toString());
                    if (warn) lbl.setForeground(Color.red);
                        else lbl.setForeground(new Color(15, 130, 40, 205));
                    lbl.setToolTipText(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_rating") + " ==> " + buf.toString());
                    break;

                case COLUMN_PHASE:
                    lbl.setForeground(new Color(53, 53, 53, 134)); lbl.setFont(FrameworkConstants.FONT_STANDARD);
                    if (value.equals(MarketInfo.LIST_PHASE[0])) lbl.setBackground(new Color(176, 250, 18, 178));
                    else if (value.equals(MarketInfo.LIST_PHASE[3])) lbl.setBackground(new Color(250, 250, 13, 222));
                    else if (value.equals(MarketInfo.LIST_PHASE[4])) lbl.setBackground(new Color(213, 140, 82, 134));
                    else if (value.equals(MarketInfo.LIST_PHASE[6])) lbl.setBackground(new Color(250, 41, 23, 186));
                    break;

                case COLUMN_MEAN_REVERSION:
                    String vs = (String)value;
                    if (vs.startsWith("+")) lbl.setForeground(Color.green.darker());
                    else lbl.setForeground(Color.red);
                    break;

                case COLUMN_T_LINE:
                case COLUMN_20MA:
                case COLUMN_50MA:
                case COLUMN_200MA:
                    boolean show_all = false;
                    lbl.setText("");
                    lbl.setToolTipText(FrameworkConstants.PRICE_FORMAT.format(value) + " %");
                    double v = (Double)value;
                    if (_cmbClusterMa.getSelectedItem() == MovingAverageType.SMA_20 && column == COLUMN_20MA) {
                        show_all = true;//show all squares for active SMA
                        lbl.setIcon(v >= 0 ? FrameworkIcon.SQUARE_GREEN : FrameworkIcon.SQUARE_RED);
                    }
                    else if (_cmbClusterMa.getSelectedItem() == MovingAverageType.SMA_50 && column == COLUMN_50MA) {
                        show_all = true;//show all squares for active SMA
                        lbl.setIcon(v >= 0 ? FrameworkIcon.SQUARE_GREEN : FrameworkIcon.SQUARE_RED);
                    }
                    else if (_cmbClusterMa.getSelectedItem() == MovingAverageType.SMA_200 && column == COLUMN_200MA) {
                        show_all = true;//show all squares for active SMA
                        lbl.setIcon(v >= 0 ? FrameworkIcon.SQUARE_GREEN : FrameworkIcon.SQUARE_RED);
                    }
                    else {//others show only if hit target
                        if (Math.abs(v) < 1.0) //TODO make it adjustable
                            lbl.setIcon(v >= 0 ? FrameworkIcon.SQUARE_GREEN : FrameworkIcon.SQUARE_RED);
                    }
                    break;

                case COLUMN_DSTO://overbought oversold
                    v = (Double)value;
                    if (v < 20) lbl.setIcon(FrameworkIcon.SQUARE_GREEN);//oversold
                    else if (v > 80) lbl.setIcon(FrameworkIcon.SQUARE_RED);//overbought
                    else lbl.setIcon(null);
                    lbl.setToolTipText(FrameworkConstants.PRICE_FORMAT.format(v));
                    lbl.setText("");
                    break;

                case COLUMN_MACD://cross zero
                    v = (Double)value;
                    if (v < 0) lbl.setIcon(FrameworkIcon.SQUARE_RED);
                    else lbl.setIcon(FrameworkIcon.SQUARE_GREEN);
                    lbl.setToolTipText(FrameworkConstants.PRICE_FORMAT.format(v));
                    lbl.setText("");
                    break;
            }
            return lbl;
        }
    }

    //----- variables -----
    private JComboBox<String> _cmbGrpSelector;//by default select first item
    private DefaultComboBoxModel<String> _cmlGrpSelector = new DefaultComboBoxModel<>();
    private JComboBox<String> _cmbGrpShow = new JComboBox<>(new String[]{"Bullish", "All"});//show bullish candidates or all in watchlist
    private JButton _btnGenWatchList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_13"), FrameworkIcon.WATCH);
    private JComboBox<MovingAverageType> _cmbClusterMa = new JComboBox<>(
        new MovingAverageType[]{MovingAverageType.SMA_20, MovingAverageType.SMA_50, MovingAverageType.SMA_200});//for bullish condition
    private boolean _bShowBullish = true;//default bullish
    private JButton _btnOption = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_opt"), FrameworkIcon.SETTING);
    private JTable _tblReport;
    private ReportTableModel _tmReport;
    private String _sCurWatchlist;
    private boolean _bShowDoji, _bShowBullCandle = true, _bShowBearCandle;

    //----- literals -----
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_CANDLE_SIGNAL = 1;
    private static final int COLUMN_COMP_RS_RATING = 2;
    private static final int COLUMN_ACC_DIS_RATING = 3;
    private static final int COLUMN_PHASE = 4;
    private static final int COLUMN_MEAN_REVERSION = 5;
    private static final int COLUMN_T_LINE = 6;
    private static final int COLUMN_20MA = 7;
    private static final int COLUMN_50MA = 8;
    private static final int COLUMN_200MA = 9;
    private static final int COLUMN_DSTO = 10;
    private static final int COLUMN_MACD = 11;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("pw_sym"),       ColumnTypeEnum.TYPE_STRING, 1,  30, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_cdl"),   ColumnTypeEnum.TYPE_STRING, 0, 100, null, null, null},//candle
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_cr"),    ColumnTypeEnum.TYPE_STRING, -1, 35, null, null, null},//COMP/RS rating hook/value
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_acc"),   ColumnTypeEnum.TYPE_STRING, 2,  35, null, null, null},//Acc/Dis rating
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_phase"), ColumnTypeEnum.TYPE_STRING, 0,  60, null, null, null},//phase
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_mean_rev"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null},//mean reversion
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_T"),     ColumnTypeEnum.TYPE_DOUBLE, -1, 5, null, null, null},//T line
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_20"),    ColumnTypeEnum.TYPE_DOUBLE, -1, 5, null, null, null},//20MA
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_50"),    ColumnTypeEnum.TYPE_DOUBLE, -1, 5, null, null, null},//50MA
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_200"),   ColumnTypeEnum.TYPE_DOUBLE, -1, 15, null, null, null},//200MA
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_sto"),   ColumnTypeEnum.TYPE_STRING, -1, 20, null, null, null},//STO
        {ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_macd"),  ColumnTypeEnum.TYPE_STRING, -1, 20, null, null, null},//MACD
    };
}
//TODO need a way to refresh watch list drop downs......