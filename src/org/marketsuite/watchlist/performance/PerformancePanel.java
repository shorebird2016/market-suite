package org.marketsuite.watchlist.performance;

import org.marketsuite.component.Constants;
import org.marketsuite.component.comparator.DoubleComparator;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.marektview.performance.PerformanceViewPanel;
import org.marketsuite.marektview.performance.SectorInfo;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.thumbnail.ThumbnailPanel;
import org.marketsuite.watchlist.mgr.ViewSymbolsDialog;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.Constants;
import org.marketsuite.component.comparator.DoubleComparator;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.marektview.performance.PerformanceViewPanel;
import org.marketsuite.marektview.performance.SectorInfo;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.mgr.ViewSymbolsDialog;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

//Relative performance of various time periods. eg. 2 weeks, 3 months..etc
public class PerformancePanel extends JPanel {
    //CTOR - use card layout for two panels, one is strategy simulator, another is scanner
    public PerformancePanel() {
        setLayout(new MigLayout());
        setOpaque(false);
        setBorder(new BevelBorder(BevelBorder.LOWERED));

        //center - result table and price chart
        _TableModel = new PerformanceTableModel();
        _tblPerf = new JTable(_TableModel);
        WidgetUtil.initDynaTable(_tblPerf, _TableModel, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
                new SortHeaderRenderer(), true, new PerfRenderer());
        _tblPerf.setOpaque(false);
        WidgetUtil.forceColumnWidth(_tblPerf.getColumnModel().getColumn(PerformanceTableModel.COLUMN_SYMBOL), 45);
        _tblPerf.setAutoCreateRowSorter(true);
        _Sorter = _tblPerf.getRowSorter();
        _tblPerf.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int[] rows = _tblPerf.getSelectedRows();
                int sel_cnt = rows.length;
                _btnRemoveSymbol.setEnabled(sel_cnt > 0);
                //nothing selected, disable delete, close button
                if (sel_cnt == 0) {//de-selection
                    Props.SymbolSelection.setValue("");
//TODO de-emphasize
                    return;
                }
                //emphasize chart
                int model_index = _tblPerf.convertRowIndexToModel(rows[0]);
                String symbol = (String) _TableModel.getCell(model_index, PerformanceTableModel.COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(symbol);
                _pnlThumbnail.emphasizeSymbol(symbol);
            }
        });
        JScrollPane scr = new JScrollPane(_tblPerf); scr.getViewport().setOpaque(false);
        add(scr, "dock center");

        //handle table header click - change bold hyperlink
        _tblPerf.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mev) {
                super.mouseClicked(mev);
                _nCurrentTimeframe = _tblPerf.columnAtPoint(mev.getPoint());
                Props.TimeFrameChanged.setValue(null, new Integer(_nCurrentTimeframe));
                _pnlThumbnail.renderThumbnails(_TableModel.getSortedSymbols(_nCurrentTimeframe));
            }
        });

        //a key listener for DEL
        _tblPerf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_SYMBOL");
        _tblPerf.getActionMap().put("DELETE_SYMBOL", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedSymbols();
                _btnAllSymbols.setEnabled(true);
            }
        });
        _tblPerf.getSelectionModel().clearSelection();//force user to select then delete

        //south - tool bar
        JPanel tool_pnl = new JPanel(new MigLayout("", "5[][][][]push[][][][]push[]5[]15[]10[]5[]5", "3[]3"));
        tool_pnl.add(new JLabel("Show Top")); tool_pnl.add(_fldTopCount); tool_pnl.add(new JLabel("Symbols"));
        _btnTopN.setDisabledIcon(new DisabledIcon(FrameworkIcon.SEARCH.getImage()));
        tool_pnl.add(_btnTopN); _btnTopN.setEnabled(false); _btnTopN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int count = (int) _fldTopCount.getValue();
                int row_cnt = _TableModel.getRowCount();
                if (row_cnt <= count) {
                    WidgetUtil.showMessageInEdt("Please enter number smaller than " + row_cnt);
                    return;
                }
                ArrayList<String> symbols = showTopN(count);
                _btnAllSymbols.setEnabled(true);
                _pnlThumbnail.renderThumbnails(symbols);
            }
        });
        tool_pnl.add(new JLabel("Filter By Top")); tool_pnl.add(_fldTopSectorPct); tool_pnl.add(new JLabel("% Sectors"));
        _btnFilterBySector.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILTER.getImage()));
        tool_pnl.add(_btnFilterBySector); _btnFilterBySector.setEnabled(false); _btnFilterBySector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                ArrayList<RankElement> sec_ranks = SectorInfo.computeSectorRanking(PerformanceTableModel.timeCodeToEnum(_nCurrentTimeframe));
                float topsect_cnt = _rnkSectors.size() * (int) _fldTopSectorPct.getValue() / 100.0F;
                ArrayList<String> sec_syms = new ArrayList<>();
                for (int idx = 0; idx < topsect_cnt; idx++)//only IG under these sectors
                    sec_syms.add(_rnkSectors.get(idx).symbol);
                System.err.println("==SECTORS==> " + sec_syms);
//TODO may show a tooltip like IJ for sectors
                //from current watch list, only plot those symbols that belong to top ranking sectors
                ArrayList<String> ig_syms = new ArrayList<>();
                for (String ig_sym : _WatchlistModel.getMembers()) {
                    String sec = SectorInfo.findSectorByIGroup(ig_sym);
                    if (sec == null) continue;
                    if (sec_syms.contains(sec))
                        ig_syms.add(ig_sym);
                }
                _TableModel.populate(_WatchlistModel, ig_syms, _bBaselineMode);
                _TableModel.computeRanking(PerformanceTableModel.COLUMN_1_WEEK_PCT, PerformanceTableModel.COLUMN_CUSTOM_PCT, new DoubleComparator());
                WatchListModel wlm = _pnlThumbnail.renderThumbnails(_TableModel.getSortedSymbols(_nCurrentTimeframe));
                Props.PlotWatchlist.setValue(null, wlm);
            }
        });
        _btnRemoveSymbol.setDisabledIcon(new DisabledIcon(LazyIcon.MINUS_SIGN.getImage()));
        tool_pnl.add(_btnRemoveSymbol); _btnRemoveSymbol.setEnabled(false);
        _btnRemoveSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedSymbols();
                _btnAllSymbols.setEnabled(true);
            }
        });
//TODO may allow multi-row select for deletion ???
        _btnAllSymbols.setDisabledIcon(new DisabledIcon(FrameworkIcon.REFRESH.getImage()));
        tool_pnl.add(_btnAllSymbols); //_btnAllSymbols.setEnabled(false);
        _btnAllSymbols.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Props.RestoreAllSymbols.setChanged();
            }
        });
        _btnCreateWatchList.setDisabledIcon(new DisabledIcon(FrameworkIcon.WATCH.getImage()));
        tool_pnl.add(_btnCreateWatchList); _btnCreateWatchList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("mkt_53"), "");
                ArrayList<String> symbols = _TableModel.getSymbols();
                GroupStore.getInstance().addGroup(dlg.getEntry(), symbols);
                Props.WatchListsChange.setChanged();
            }
        });
        tool_pnl.add(_btnExport); _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_EXPORT));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int rsp = fc.showSaveDialog(null);//todo MainFrame.getInstance()
                if (rsp == JFileChooser.APPROVE_OPTION) {
                    File output_path = fc.getSelectedFile();
                    if (output_path.exists()) { //warn user if file exist
                        if (MessageBox.messageBox(
                                MdiMainFrame.getInstance(),
                                "warning", "<html>This File will be <b>WRITTEN !</b><br><br>Continue?",
                                MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                            return;
                    }
                    FileUtil.exportSheet(_TableModel, output_path);
                }
            }
        });
        tool_pnl.add(_btnReport); _btnReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> lst = new ArrayList<>();
                for (int row = 0; row < _TableModel.getRowCount(); row++) {
                    int sel = _tblPerf.convertRowIndexToModel(row);
                    lst.add((String)_TableModel.getCell(sel, PerformanceTableModel.COLUMN_SYMBOL).getValue());
                }
                new ViewSymbolsDialog(lst, true);
            }
        });
        add(tool_pnl, "dock south");

        //show pop up information about sector ranking of this time frame
        tool_pnl.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                StringBuilder buf = new StringBuilder("<html>Sector Ranking:<br>");
                for (int idx = 0; idx < _rnkSectors.size(); idx++) {
                    RankElement re = _rnkSectors.get(idx);
                    String pct = FrameworkConstants.PCT_FORMAT.format(re.pct);
                    buf.append(re.rank).append("  ").append(re.symbol)
                       .append("  ").append(pct).append("<br>");
                }
                WidgetUtil.attachToolTip(tool_pnl, buf.toString(), SwingConstants.RIGHT, SwingConstants.TOP);
            }
        });
        _rnkSectors = SectorInfo.computeSectorRanking(PerformanceTableModel.timeCodeToEnum(_nCurrentTimeframe));
    }

    //----- public methods -----
    public void findSymbol(String symbol) {
        if (symbol.equals(""))  return;//de-select sends empty string

        //find which row and scroll into view
        int row = _TableModel.findSymbol(symbol);
        if (row < 0) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_5") + " " + symbol, LoggingSource.L_SQUARE_PERFORMANCE);
            return;
        }
        row = _tblPerf.convertRowIndexToView(row);
        WidgetUtil.scrollCellVisible(_tblPerf, row, PerformanceTableModel.COLUMN_SYMBOL);
        _tblPerf.getSelectionModel().setSelectionInterval(row, row);

    }
    //when new watch list is loaded, (1)refresh table (2)plot thumbnail
    public void populate(WatchListModel model, boolean baseline_mode) {
        _WatchlistModel = model; _bBaselineMode = baseline_mode;
        _tblPerf.clearSelection();
        _TableModel.populate(model, baseline_mode);
        if (_TableModel.getRowCount() > 0) {
            _tblPerf.getSelectionModel().setSelectionInterval(0, 0);
            computeRanking();
            autoSort(_nCurrentTimeframe);
            _btnTopN.setEnabled(true);
            //for industrial group watch list, enable special menu
            _btnFilterBySector.setEnabled(model.getWatchlistName().equals("ETF - 4 Industry"));
            _pnlThumbnail.renderThumbnails(_TableModel.getSortedSymbols(_nCurrentTimeframe), _TableModel.getEndIndex());
        }
    }
    public void populate(WatchListModel model, boolean baseline_mode, int end_index) {
        _TableModel.setEndIndex(end_index);
        populate(model, baseline_mode);
    }
    public void showHideSymbol(String symbol, boolean show_symbol) {
        _TableModel.showHideSymbol(symbol, show_symbol);
        computeRanking();
        if (show_symbol) findSymbol(symbol);
    }
    //update custom column
    public void updateCustomPercents(int origin_index, WatchListModel wlm) {
        ArrayList<String> members = wlm.getMembers();
        for (String symbol : members) {
            MarketInfo mki = wlm.getMarketInfo(symbol);
            if (mki == null)
                continue;//some symbols may not have mki due to calculation
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            FundQuote end_quote = quotes.get(0);
            FundQuote origin_quote = quotes.get(origin_index);
            float pct = (end_quote.getClose() - origin_quote.getClose()) / origin_quote.getClose();

            //look up symbol in table, update value
            for (int row = 0; row < _TableModel.getRowCount(); row++) {
                if (symbol.equals(_TableModel.getCell(row, PerformanceTableModel.COLUMN_SYMBOL).getValue())) {
                    _TableModel.getCell(row, PerformanceTableModel.COLUMN_CUSTOM_PCT).setValue(new Double(pct));
                    break;
                }
            }
        }
        _TableModel.computeRanking(PerformanceTableModel.COLUMN_CUSTOM_PCT, PerformanceTableModel.COLUMN_CUSTOM_PCT, new DoubleComparator());
        _TableModel.fireTableDataChanged();
    }

    //----- inner classes -----
    private class PerfRenderer extends DynaTableCellRenderer {
        private PerfRenderer() {
            super(_TableModel);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = _tblPerf.convertRowIndexToModel(row);
            column = _tblPerf.convertColumnIndexToModel(column);
            Object val = _TableModel.getCell(row, column).getValue();
            JLabel lbl = (JLabel)comp;
            double price = (Double)_TableModel.getCell(row, PerformanceTableModel.COLUMN_PRICE).getValue();
            if (price == 0) {
                if (column != PerformanceTableModel.COLUMN_SYMBOL)
                    lbl.setText("");
                return comp;
            }
            switch (column) {
                case PerformanceTableModel.COLUMN_SYMBOL://use default
                    break;

                case PerformanceTableModel.COLUMN_PRICE:
                    lbl.setText(FrameworkConstants.DOLLAR_FORMAT.format((Double)val));
                    break;

                default://these are percentages
                    lbl.setText(FrameworkConstants.ROI_FORMAT.format((Double)val));
                    break;
            }

            //if 3 or less rows in the table, skip highlighting
            if (_TableModel.getRowCount() <= 3)
                return comp;

            //is this cell rank top 3 of this column?  highlight green if so TODO make this a utility re-useable
            ArrayList<Integer> rank_idx = _TableModel.getRankingMap().get(new Integer(column));
            if (rank_idx != null && rank_idx.size() > 0) {
                int end_idx = rank_idx.size();
                for (int i=0; i<5; i++) {//top 3
                    if (row == rank_idx.get(i))
                        lbl.setBackground(FrameworkConstants.COLOR_MEDIUM_GREEN);
                }
                for (int i=end_idx-1; i>=(end_idx-5); i--) {//bottom 3
                    if (row == rank_idx.get(i))
                        lbl.setBackground(Constants.CELL_EDITING_BACKGROUND);//red
                }
            }
            return comp;
        }
    }

    //----- private methods -----
    private void removeSelectedSymbols() {
        //remove from table
        int[] sel = _tblPerf.getSelectedRows();
        int[] model_indicies = new int[sel.length];
        for (int idx = 0; idx < sel.length; idx++) {
            int row = sel[idx];//array contains row indices
            row = _tblPerf.convertRowIndexToModel(row);//model index (since sorting changes the mapping)
            model_indicies[idx] = row;
        }
        _TableModel.deleteRows(model_indicies);
        computeRanking();

        //notify frame about symbol removal and thus remove from graph
//        Props.SymbolRemoved.setValue(sym);
        _pnlThumbnail.renderThumbnails(_TableModel.getSortedSymbols(_nCurrentTimeframe));
    }
    //hide all symbols that are among top N performance in current time frame, return = visible symbols
    private ArrayList<String> showTopN(int num_top) {
        ArrayList<Integer> rank_idx = _TableModel.getRankingMap().get(new Integer(_nCurrentTimeframe));
        if (num_top >= rank_idx.size())
            num_top = rank_idx.size();
        ArrayList<String> exclude_symbols = new ArrayList<>();
        ArrayList<Integer> exclude_rows = new ArrayList<>();
        for (int i = num_top; i < rank_idx.size(); i++) {//exclude low rank symbols
            int row_index = rank_idx.get(i);
            String symbol = (String)_TableModel.getCell(row_index, PerformanceTableModel.COLUMN_SYMBOL).getValue();
            exclude_symbols.add(symbol);
            exclude_rows.add(row_index);
        }

        //delete rows in table by symbol
        for (String sym : exclude_symbols) {
            for (int row = 0; row < _TableModel.getRowCount(); row++) {
                String symbol = (String)_TableModel.getCell(row, PerformanceTableModel.COLUMN_SYMBOL).getValue();
                if (sym.equals(symbol)) {
                    int rows[] = new int[1];
                    rows[0] = row;
                    _TableModel.setSelectedRows(rows);
                    _TableModel.delete();
                    break;
                }
            }
        }
        _TableModel.fireTableDataChanged();
        computeRanking();

        //redraw lines
        for (String sym : exclude_symbols)
            Props.SymbolRemoved.setValue(sym);

        //return visible symbols
//        ArrayList<String> ret = new ArrayList<>();
//        for (String sym : _WatchlistModel.getMembers())
//            if (!exclude_symbols.contains(sym))
//                ret.add(sym);
        return _TableModel.getSortedSymbols(_nCurrentTimeframe);
    }
    //return plus number = normal; negative = error or cancel
    private void computeRanking() {
        //sets up performance information, ranking must be computed after populate
        _TableModel.computeRanking(PerformanceTableModel.COLUMN_1_WEEK_PCT, PerformanceTableModel.COLUMN_CUSTOM_PCT, new DoubleComparator());
    }
    private void autoSort(int time_frame) {
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(time_frame, SortOrder.DESCENDING));
        _Sorter.setSortKeys(keys);
    }

    //----- accessor -----
    public void setCurrentTimeframe(int timeframe) {
        _nCurrentTimeframe = timeframe;
        //for Max time frame, disable "show Top N" menu since there is no ranking information
        boolean full_range = timeframe == PerformanceViewPanel.FULL_RANGE;
        if (!full_range) //also don't allow auto sort since there is no such column
            autoSort(_nCurrentTimeframe);
//        _pnlThumbnail.renderThumbnails(_TableModel.getSortedSymbols(_nCurrentTimeframe));//render based on new order
    }
    public void setThumbnailPanel(ThumbnailPanel pnl) { _pnlThumbnail = pnl; }

    //-----instance variables-----
    private JTable _tblPerf;
    private PerformanceTableModel _TableModel;
    private boolean _bBaselineMode;
    private int _nCurrentTimeframe = PerformanceTableModel.COLUMN_1_MONTH_PCT;
    private RowSorter _Sorter;
    private WatchListModel _WatchlistModel;
    private LongIntegerField _fldTopCount = new LongIntegerField(10, 3, 3, 100);
    private JButton _btnTopN = WidgetUtil.createIconButton("Find Top N Symbols", FrameworkIcon.SEARCH);
    private LongIntegerField _fldTopSectorPct = new LongIntegerField(20, 3, 5, 100);
    private JButton _btnFilterBySector = WidgetUtil.createIconButton("Filter Per Strong Sectors", FrameworkIcon.FILTER);
    private JButton _btnRemoveSymbol = WidgetUtil.createIconButton("Remove Selected Symbol", LazyIcon.MINUS_SIGN);
    private JButton _btnAllSymbols = WidgetUtil.createIconButton("Restore All Symbols", FrameworkIcon.REFRESH);
    private JButton _btnCreateWatchList = WidgetUtil.createIconButton("Create Watch List", FrameworkIcon.WATCH);
    private JButton _btnExport = WidgetUtil.createIconButton("Export Table to .xls File", FrameworkIcon.EXPORT);
    private JButton _btnReport = WidgetUtil.createIconButton("Show Symbols in Performance Order for Copying", FrameworkIcon.REPORT);
    private ThumbnailPanel _pnlThumbnail;//easy access
    private ArrayList<RankElement> _rnkSectors;
}
//TODO after sorting by timeframe, change sector tooltip calculation