package org.marketsuite.planning;

import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.datamgr.dataimport.PickWatchlistDialog;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.data.CandleSignals;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.TradeAction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.watchlist.model.WatchListModel;
import jxl.Sheet;
import jxl.Workbook;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.datamgr.dataimport.PickWatchlistDialog;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.data.CandleSignals;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.TradeAction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlanSheetPanel extends JPanel {
    public PlanSheetPanel() {
        setLayout(new MigLayout("insets 0"));

        //title strip - buttons
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[]10[]10[]10[]push[]10[]5", "3[]3"));
        ttl_pnl.add(_btnOpenTs);
        _btnOpenTs.addActionListener(new ActionListener() {
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
                if (ret != JFileChooser.APPROVE_OPTION) return;
                ApolloPreferenceStore.getPreferences().setTradeStationPath(fc.getCurrentDirectory().getPath());
                ApolloPreferenceStore.savePreferences();//flush
                ArrayList<Position> positions = DataUtil.obtainTradeStationPositions(fc.getSelectedFiles());
                _tmPortfolio.populate(positions);

                //ask name of watch list
                NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                if (dlg.isCancelled()) return;
                String name = dlg.getEntry();
                ArrayList<String> list = new ArrayList<>();
                for (Position pos : positions) {
                    if (!list.contains(pos.getSymbol()))//make sure the uniqueness
                        list.add(pos.getSymbol());
                }
                GroupStore.getInstance().addGroup(name, list);//empty group persist
                Props.WatchListsChange.setChanged();//notify watch list manager
                WatchListModel model = new WatchListModel(name, false);//guarantee wlm exist in MainModel
                MainModel.getInstance().setWatchListModel(model);
                mergeSheetCandleRating();
            }
        });
        ttl_pnl.add(_btnOpenWatch);
        _btnOpenWatch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HashMap<String, ArrayList<String>> wl_map = GroupStore.getInstance().getGroups();
                Iterator<String> itor = wl_map.keySet().iterator();
                PickWatchlistDialog dlg = new PickWatchlistDialog(itor);
                if (dlg.isCancelled()) return;
                java.util.List<String> watch_lists = dlg.getWatchlists();
                ArrayList<String> merged_symbols = new ArrayList<>();
                for (String wl : watch_lists) {//merge lists, remove duplicate
                    ArrayList<String> symbols = wl_map.get(wl);
                    for (String sym : symbols) {
                        if (!merged_symbols.contains(sym))
                            merged_symbols.add(sym);
                    }
                    _tmPortfolio.populateSymbols(merged_symbols);
                    WatchListModel model = new WatchListModel(wl, false);//guarantee wlm exist in MainModel
                    MainModel.getInstance().setWatchListModel(model);
                    mergeSheetCandleRating();
                    break;//TODO only do first one for now.....
                }
            }
        });
        ttl_pnl.add(_btnSavePlan);
        _btnSavePlan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//TODO debug, symbols not sorted
                //save plan to spreadsheet, all columns but close
                FileUtil.exportSheet(_tmPortfolio, new File(FrameworkConstants.DATA_FOLDER_PORTFOLIO));
            }
        });
        ttl_pnl.add(_lblPosCount);
        ttl_pnl.add(_btnUpdateQuote);
        _btnUpdateQuote.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tmPortfolio.updateQuotes();
            }
        });
        ttl_pnl.add(_btnWatchlist);
        _btnWatchlist.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //ask name
                NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                if (dlg.isCancelled())
                    return;
                String name = dlg.getEntry();

                //check duplicate list name
                if (GroupStore.getInstance().isGroupExist(name)) {
                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("rm_89"));
                    return;
                }

                //collect symbols, remove duplicate
                ArrayList<String> list = _tmPortfolio.getSymbols();
                GroupStore.getInstance().addGroup(name, list);//empty group persist
                Props.WatchListsChange.setChanged();//notify watch list manager
            }
        });
        ttl_pnl.add(_btnThumbnail);
        _btnThumbnail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { Props.PlotThumbnails.setValue(null, _tmPortfolio.getSymbols()); }
        });
        add(ttl_pnl, "dock north");

        //table
        _tblPortfolio = WidgetUtil.createDynaTable(_tmPortfolio, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new SheetRenderer());
        _tblPortfolio.setAutoCreateRowSorter(true);
        _tblPortfolio.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblPortfolio.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int sel = _tblPortfolio.getSelectedRow();
                sel = _tblPortfolio.convertRowIndexToModel(sel);
                Props.SymbolSelection.setValue(null, _tmPortfolio.getCell(sel, COLUMN_SYMBOL).getValue());
            }
        });
        _Sorter = _tblPortfolio.getRowSorter(); autoSort();
        JScrollPane scr = new JScrollPane(_tblPortfolio);
        TableUtil.fixColumns(scr, _tblPortfolio, LOCKED_COLUMNS);
        add(scr, "dock center");
    }

    //----- inner class -----
    private class SheetTableModel extends DynaTableModel {
        private SheetTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public void populate() {}
        public boolean isCellEditable(int row, int column) { return getCell(row, column).isHighlight(); }
        public void setValueAt(Object value, int row, int column) {
            super.setValueAt(value, row, column);
        }
        private ArrayList<String> getSymbols() {
            ArrayList<String> ret = new ArrayList<>();
            for (int row = 0; row < getRowCount(); row++)
                ret.add((String)getCell(row, COLUMN_SYMBOL).getValue());
            return ret;
        }
        private SimpleCell[] initCells() {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            for (int col=0; col<TABLE_SCHEMA.length; col++) {
                switch (col) {
                    case COLUMN_SYMBOL:
                    case COLUMN_CLOSE:
                    case COLUMN_TRIGGER:
                    case COLUMN_RISK:
                    case COLUMN_CANDLE_SIGNAL:
                    case COLUMN_RATING:
                    case COLUMN_TECHNICAL:
                        cells[col] = new SimpleCell("");
                        break;
                    case COLUMN_SHARE:
                    case COLUMN_ACTION_SHARE:
                        cells[col] = new SimpleCell(new Long(0));
                        break;
                    case COLUMN_ACTION:
                        cells[col] = new SimpleCell(TradeAction.None);
                        break;
                    case COLUMN_COST:
                    case COLUMN_STOP:
                        cells[col] = new SimpleCell(new Double(0));
                        break;
                }
            }
            return cells;
        }
        private void populate(ArrayList<Position> positions) {
            clear();
            for (Position pos : positions) {
                SimpleCell[] cells = initCells();
                cells[COLUMN_SYMBOL].setValue(pos.getSymbol());
                cells[COLUMN_SHARE].setValue(new Long(pos.getShares()));
                cells[COLUMN_COST].setValue(new Double(pos.getCost()));//FrameworkConstants.DOLLAR_FORMAT.format(pos.getCost()));
                decorate(cells);
                _lstRows.add(cells);
            }
//            updateQuotes();
            fireTableDataChanged();
            _lblPosCount.setText("#" + positions.size());
        }
        private void updateQuotes() {
            try {
                ArrayList<FundQuote> quotes = DataUtil.quickQuote(getSymbols());
                for (int row = 0; row < quotes.size(); row++) //should match symbol array sequence
                    getCell(row, COLUMN_CLOSE).setValue(FrameworkConstants.DOLLAR_FORMAT.format(quotes.get(row).getClose()));
            } catch (IOException e1) {
                e1.printStackTrace();
                WidgetUtil.showWarning(e1.getMessage());
            }
            fireTableDataChanged();
        }
        private void populateSymbols(ArrayList<String> symbols) {
            clear();
            for (String sym : symbols) {
                SimpleCell[] cells = initCells();
                cells[COLUMN_SYMBOL].setValue(sym);
                decorate(cells);
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
        private void decorate(SimpleCell[] cells) {
            cells[COLUMN_SHARE].setHighlight(true);
            cells[COLUMN_COST].setHighlight(true);
            cells[COLUMN_ACTION].setHighlight(true);
            cells[COLUMN_TRIGGER].setHighlight(true);
            cells[COLUMN_ACTION_SHARE].setHighlight(true);
            cells[COLUMN_STOP].setHighlight(true);
            cells[COLUMN_TECHNICAL].setHighlight(true);
        }
        private int findSymbol(String symbol) {
            for (int row = 0; row < getRowCount(); row++)
                if (getCell(row, COLUMN_SYMBOL).getValue().equals(symbol))
                    return row;
            return -1;
        }
        private void updateRow(int sheet_row, int tbl_row, Sheet sheet) {//table sheet_row is one less than sheet sheet_row
            String action = sheet.getCell(COLUMN_ACTION, sheet_row).getContents();
            TradeAction act = TradeAction.valueOf(action);
            getCell(tbl_row, COLUMN_ACTION).setValue(act);
//            getCell(tbl_row, COLUMN_CLOSE).setValue(sheet.getCell(COLUMN_CLOSE, sheet_row).getContents());
            getCell(tbl_row, COLUMN_TRIGGER).setValue(sheet.getCell(COLUMN_TRIGGER, sheet_row).getContents());
            String action_share = sheet.getCell(COLUMN_ACTION_SHARE, sheet_row).getContents();
            getCell(tbl_row, COLUMN_ACTION_SHARE).setValue(Long.parseLong(action_share));
            String stop = sheet.getCell(COLUMN_STOP, sheet_row).getContents();
            getCell(tbl_row, COLUMN_STOP).setValue(Double.parseDouble(stop));
//            getCell(tbl_row, COLUMN_CANDLE_SIGNAL).setValue(calcCandleCell(tbl_row));
            getCell(tbl_row, COLUMN_TECHNICAL).setValue(sheet.getCell(COLUMN_TECHNICAL, sheet_row).getContents());
            fireTableRowsUpdated(tbl_row, tbl_row);
        }
        //for each row in table, look up matching row in sheet, copy sheet content over to table
        private void mergePlanSheet(Sheet sheet) {
            int sheet_rows = sheet.getRows();
            for (int row = 0; row < getRowCount(); row++) {
                int tbl_row = _tblPortfolio.convertRowIndexToModel(row);
                String sym = (String)getCell(tbl_row, COLUMN_SYMBOL).getValue();
                int sheet_row = 1;//skip header
                do {
                    String sheet_sym = sheet.getCell(COLUMN_SYMBOL, sheet_row).getContents();//same column order
                    if (sym.equals(sheet_sym))
                        updateRow(sheet_row, tbl_row, sheet);
                    sheet_row++;
                }while (sheet_row < sheet_rows);
            }
        }
        //calculate candle information for all cells
        private void populateCandleCells() {
            //borrow mki from watch list model
            WatchListModel wlm = MainModel.getInstance().getWatchListModel();
            for (int row = 0; row < getRowCount(); row++) {
                String symbol = (String)_tmPortfolio.getCell(row, COLUMN_SYMBOL).getValue();
                MarketInfo mki = wlm.getMarketInfo(symbol);
                CandleSignals css = new CandleSignals(mki.getFund().getQuote(), 20);//only scan past 20 days
                ArrayList<CandleSignal> signals = css.getSignals(5);//get 5 day signals
                StringBuilder buf = new StringBuilder("");
                for (CandleSignal sig : signals)
                    buf.append(sig.getCode()).append(" ");
                getCell(row, COLUMN_CANDLE_SIGNAL).setValue(buf.toString());
            }
        }
        //calculate rating information for all cells
        private void populateRatingCells() {
            WatchListModel wlm = MainModel.getInstance().getWatchListModel();
            for (int row = 0; row < getRowCount(); row++) {
                String symbol = (String)_tmPortfolio.getCell(row, COLUMN_SYMBOL).getValue();
                MarketInfo mki = wlm.getMarketInfo(symbol);
                WeeklyQuote wq = new WeeklyQuote(mki.getFund(), 60);//2 months = 8 weeks
                ArrayList<IbdRating> ratings;
                try {
                    ratings = IbdRating.readIbdWeeklyRating(mki.getFund(), 60);
                } catch (IOException e) {
                    System.err.println(e.getMessage());//TODO send to log window
                    continue;
                }
                if (ratings.size() == 0) continue;
                boolean hook_up = IbdRating.doCompRsHookup(ratings)
                        && WeeklyQuote.doesPriceHookup(wq.getQuotes(), ratings.get(0).getDate());
                if (hook_up)
                    getCell(row, COLUMN_RATING).setValue("Up");
            }

        }
    }
    private class SheetRenderer extends DynaTableCellRenderer {
        private SheetRenderer() { super(_tmPortfolio); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int model_row = _tblPortfolio.convertRowIndexToModel(row);
            switch (column) {
                case COLUMN_SYMBOL:
                    if (table.equals(_tblPortfolio)) return comp;//skip processing normal tab, only color locked symbol
                    JLabel label = (JLabel)comp;
                    TradeAction action = (TradeAction)_tmPortfolio.getCell(model_row, COLUMN_ACTION).getValue();
                    if (action.equals(TradeAction.Buy) || action.equals(TradeAction.Addon)) label.setBackground(new Color(40, 255, 100, 51));
                    else if (action.equals(TradeAction.Sell) || action.equals(TradeAction.Trim)) label.setBackground(new Color(217, 118, 136, 69));
                    else if (action.equals(TradeAction.Watch)) label.setBackground(new Color(255, 240, 49, 191));
                    break;
            }
            return comp;
        }
    }

    //----- private methods -----
    private void autoSort() {//only the mean column by default
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(COLUMN_SYMBOL, SortOrder.ASCENDING));
        _Sorter.setSortKeys(keys);
    }
    private void mergePlanSheet() {
        //merge with existing plan sheet to capture previous entries
        File plan_file = new File(FrameworkConstants.DATA_FOLDER_PORTFOLIO + File.separator + FrameworkConstants.PLAN_SHEET);
        if (plan_file.exists()) {
            try { //open this sheet, read one row at a time
                Workbook wb = Workbook.getWorkbook(new File(FrameworkConstants.DATA_FOLDER_PORTFOLIO + File.separator + FrameworkConstants.PLAN_SHEET));
                Sheet sheet = wb.getSheet(0);
                _tmPortfolio.mergePlanSheet(sheet);
                wb.close();
            }catch (Exception e1) { e1.printStackTrace(); }
        }
    }
    private void mergeSheetCandleRating() {
        mergePlanSheet();
        _tmPortfolio.populateCandleCells();
        _tmPortfolio.populateRatingCells();
        _tmPortfolio.fireTableDataChanged();
    }

    //----- variables -----
    private JButton _btnOpenTs = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("pw_rdts"), FrameworkIcon.FILE_OPEN);
    private JButton _btnOpenWatch = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("pw_rdwl"), FrameworkIcon.WATCH);
    private JButton _btnSavePlan = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("pw_save"), FrameworkIcon.FILE_SAVE);
    private JButton _btnThumbnail = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("pw_thumb"), FrameworkIcon.THUMBNAIL);
    private JButton _btnUpdateQuote = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("pw_upd"), FrameworkIcon.REFRESH);
    private JButton _btnWatchlist = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("pw_wl"), FrameworkIcon.WATCH);
//    private JButton _btnReport = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("pw_rpt"), FrameworkIcon.REPORT);
    private JLabel _lblPosCount = new JLabel("#");
    private JTable _tblPortfolio;
    private SheetTableModel _tmPortfolio = new SheetTableModel();
    private RowSorter _Sorter;
    private static JComboBox<TradeAction> _cmbAction = new JComboBox<>(TradeAction.values());

    //----- literals -----
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_CANDLE_SIGNAL = 1;
    private static final int COLUMN_RATING = 2;
    private static final int COLUMN_TECHNICAL = 3;
    private static final int COLUMN_CLOSE = 4;
    private static final int COLUMN_SHARE = 5;
    private static final int COLUMN_COST = 6;
    private static final int COLUMN_ACTION = 7;
    private static final int COLUMN_TRIGGER = 8;
    private static final int COLUMN_ACTION_SHARE = 9;
    private static final int COLUMN_STOP = 10;
    private static final int COLUMN_RISK = 11;
    static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_sym"),  ColumnTypeEnum.TYPE_STRING,   -1,  80, null, null, null },//symbol
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_cdl"),  ColumnTypeEnum.TYPE_STRING,    2, 100, null, null, null },//candle
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_rt"),   ColumnTypeEnum.TYPE_STRING,   -1,  60, null, null, null },//rating
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_ta"),   ColumnTypeEnum.TYPE_STRING,   2,  250, null, null, null },//technical
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_close"),  ColumnTypeEnum.TYPE_STRING, -1,  60, null, null, null },//close
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_sh"),   ColumnTypeEnum.TYPE_LONG,   -1,    60, new LongIntegerCellEditor(0, 4, 0, 2000), null, null },//shares
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_cost"), ColumnTypeEnum.TYPE_DOUBLE, -1,    60, new DecimalCellEditor(0, 4, 0, 2000, null), null, null },//cost
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_act"),  ColumnTypeEnum.TYPE_STRING, -1,    60, new ComboCellEditor(_cmbAction), null, null },//action
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_cond"), ColumnTypeEnum.TYPE_STRING, 0,    150, null, null, null },//condition
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_actsh"),ColumnTypeEnum.TYPE_LONG,   -1,    50, new LongIntegerCellEditor(0, 4, 0, 2000), null, null },//action shares
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_stop"), ColumnTypeEnum.TYPE_DOUBLE, -1,    60, new DecimalCellEditor(0, 4, 0, 2000, null), null, null },//new stop
        { ApolloConstants.APOLLO_BUNDLE.getString("pw_rsk"),  ColumnTypeEnum.TYPE_STRING, -1,    60, null, null, null },//new risk
    };
    private static final int LOCKED_COLUMNS = 1;
}