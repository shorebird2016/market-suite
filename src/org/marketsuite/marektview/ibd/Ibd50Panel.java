package org.marketsuite.marektview.ibd;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.Ibd50State;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.scanner.common.TimeSeriesPanel;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.type.Ibd50State;
import org.marketsuite.resource.ApolloConstants;
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

public class Ibd50Panel extends JPanel {
    public Ibd50Panel() {
        setLayout(new MigLayout("insets 0"));

        //north - calendar and search fields
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[][]20[][]push[]10[]10[]10[]5", "3[]3"));
        north_pnl.add(_lblDate); //_lblDate.setEnabled(!_chkShowAll.isSelected());
        north_pnl.add(_fldDate); //_fldDate.setEnabled(!_chkShowAll.isSelected());

        //find last Friday with quote as default starting day
        Calendar cal = AppUtil.findRecentTradingFriday();
        _fldDate.setDate(cal.getTime());
        _fldDate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                populate();
            }
        });
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_lbl_3"));
        north_pnl.add(lbl);
        north_pnl.add(_fldSearch);
        _fldSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String txt = _fldSearch.getText().toUpperCase();
                if (txt.length() == 0)
                    return;

                findSymbol(txt);
                _fldSearch.setSelectionStart(0);//highlight symbol for ease of typing over
                _fldSearch.setSelectionEnd(5);//at most 5 letters in symbol
            }
        });
        north_pnl.add(_btnCreateDb);
        _btnCreateDb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                importSheets();
            }
        });
        north_pnl.add(_btnAppendSheet); _btnAppendSheet.setEnabled(false);
        _btnAppendSheet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    appendSheet();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        north_pnl.add(_btnGenWatchList);
        _btnGenWatchList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //check empty table
                if (_tmInfo.getRowCount() == 0) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                        Constants.COMPONENT_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("ibd_12"),
                        MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    return;
                }

                //ask name
                NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                if (dlg.isCancelled())
                    return;
                String name = dlg.getEntry();

                //check duplicate list name
                if (GroupStore.getInstance().isGroupExist(name)) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("trk_15"),
                            MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    return;
                }

                //collect symbols, remove duplicate
                ArrayList<String> list = new ArrayList<>();
                for (int row = 0; row < _tmInfo.getRowCount(); row++) {
                    String symbol = (String)_tmInfo.getCell(row, COLUMN_SYMBOL).getValue();
                    if (!list.contains(symbol))
                        list.add(symbol);
                }
                GroupStore.getInstance().addGroup(name, list);//empty group persist
                Props.WatchListsChange.setChanged();//notify watch list manager
            }
        });
        north_pnl.add(_btnThumbnail);
        _btnThumbnail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> symbols = _tmInfo.getSymbols();
                Props.PlotThumbnails.setValue(null, symbols);
            }
        });
        add(north_pnl, "dock north");

        //center table and graph, two split panes stacked vertically
//        JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//        spl.setDividerLocation(400);
//        spl.setContinuousLayout(true);
        _tblInfo = WidgetUtil.createDynaTable(_tmInfo, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new InfoRenderer(_tmInfo));
        _tblInfo.setAutoCreateRowSorter(true);
        _Sorter = _tblInfo.getRowSorter(); autoSort();
        _tblInfo.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;

                //nothing selected, disable delete, close button
                int row = _tblInfo.getSelectedRow();
                if (row == -1) {//de-selection
//                    _pnlTimeSeriesGraph.clear();
                    return;
                }

                //draw chart for this symbol, if mki exists in MainModel, use it
                row = _tblInfo.convertRowIndexToModel(row);
                String symbol = (String) _tmInfo.getCell(row, COLUMN_SYMBOL).getValue();
                if (symbol.equals(_sCurSymbol))//same symbol was selected already
                    return;
                _sCurSymbol = symbol;
                MainModel main_model = MainModel.getInstance();
                WatchListModel wlm = main_model.getWatchListModel();
//                HashMap<String,ArrayList<IbdInfo>> ibd_map = main_model.getIbdInfoMap();
                MarketInfo mki = wlm.getMarketInfo(symbol);
                try {
                    if (mki == null)
                        mki = MarketUtil.calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
//                    _pnlTimeSeriesGraph.plot(mki, ibd_map.get(symbol));
                } catch (Exception mke) {
                    LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("ibd50_20") + " " + symbol,
                        LoggingSource.MARKETVIEW_IBD50, mke);
                    return;
                }
                Props.SymbolSelection.setValue(symbol);//draw in chart window
            }
        });
//        spl.setTopComponent(new JScrollPane(_tblInfo));

        //bottom - graph
//        spl.setBottomComponent(_pnlTimeSeriesGraph = new TimeSeriesPanel());
//        add(spl, "dock center");
        add(new JScrollPane(_tblInfo), "dock center");
        populate();
    }

    //----- private methods -----
    private void populate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(_fldDate.getDate());
        _tmInfo.populate(cal);
        if (_tmInfo.getRowCount() > 0)//select first row, show graph
            _tblInfo.getSelectionModel().setSelectionInterval(0, 0);
    }
    private void findSymbol(String symbol) {
        //find which row and scroll into view
        int row = _tmInfo.findSymbol(symbol);
        if (row < 0) {
            MessageBox.messageBox(MdiMainFrame.getInstance(),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_5"), MessageBox.STYLE_OK,
                    MessageBox.WARNING_MESSAGE);
            return;
        }
        row = _tblInfo.convertRowIndexToView(row);
        WidgetUtil.scrollCellVisible(_tblInfo, row, COLUMN_SYMBOL);
        _tblInfo.getSelectionModel().setSelectionInterval(row, row);
    }
    //only allows 1 sheet later than records in IBD.db
    private void appendSheet() throws Exception {
        //open file dialog with .xls extension, use pref to get last stored import path
        String lip = ApolloPreferenceStore.getPreferences().getLastIbd50Path();
        if (lip == null)
            lip = FrameworkConstants.DATA_FOLDER_IMPORT;
        JFileChooser fc = new JFileChooser(new File(lip));
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;
                int pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_XLS);
                return (pos > 0);
            }
            public String getDescription() {//this shows up in description field of dialog
                return ApolloConstants.APOLLO_BUNDLE.getString("act_lbl_2");
            }
        });
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
        if (ret != JFileChooser.APPROVE_OPTION)
            return;

        //get new date (file name), make sure it doesn't exist
        _tmInfo.clear();//empty table
        ApolloPreferenceStore.getPreferences().setLastIbd50Path(fc.getCurrentDirectory().getPath());//save location
        ApolloPreferenceStore.savePreferences();//flush
        File sheet = fc.getSelectedFile();//only 1 file
        String file_date = FileUtil.removeExtension(sheet.getName(), FrameworkConstants.EXTENSION_XLS);
        ArrayList<IbdInfo> iis = IbdInfo.importSheet(sheet);//read this file into memory
        ArrayList<String> sheet_symbols = new ArrayList<>();
        for (IbdInfo ii : iis)//gather all symbols
            sheet_symbols.add(ii.getSymbol());

        //avoid import ones already done by comparing date with _mapIbdInfo
        HashMap<String, ArrayList<IbdInfo>> ibdinfo_map = MainModel.getInstance().getIbdInfoMap();
        File db_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_DB);
        String[] file_list = db_folder.list();
        String first_file = FileUtil.removeExtension(file_list[0], FrameworkConstants.EXTENSION_IBD);
        ArrayList<IbdInfo> ffiis = ibdinfo_map.get(first_file);
        Calendar file_cal = AppUtil.stringToCalendarNoEx(file_date);
        for (IbdInfo ii : ffiis) {
            if (ii.getDate().equals(file_cal)) {
                MessageBox.messageBox(MdiMainFrame.getInstance(),
                    Constants.COMPONENT_BUNDLE.getString("warning"),
                    ApolloConstants.APOLLO_BUNDLE.getString("ibd_16"),
                    MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                return;
            }
        }

        //examine all symbols in MainModel map, to determine new state for each on this date
        Iterator<String> itor = ibdinfo_map.keySet().iterator();
        while (itor.hasNext()) {
            //search existing symbols to see if any doesn't exist in new list, set to Offlist or Inactive
            String map_symbol = itor.next();
            IbdInfo cur_ii = IbdInfo.findBySymbol(iis, map_symbol);//may be null, no such symbol in new list
            ArrayList<IbdInfo> cur_info = ibdinfo_map.get(map_symbol);
            IbdInfo last_ii = cur_info.get(cur_info.size() - 1);//get last record (most recent)
            Ibd50State prev_state = last_ii.getState();
            if (cur_ii == null) {
                cur_ii = new IbdInfo(map_symbol, "");//empty one
                cur_ii.setState(prev_state.equals(Ibd50State.Onlist) || prev_state.equals(Ibd50State.Active)
                    ? Ibd50State.Offlist : Ibd50State.Inactive);
                cur_ii.setDate(file_cal);
                appendIbdInfo(cur_ii);
//System.err.println("--00000---" + map_symbol + " " + cur_ii.getDate() + " " + cur_ii.getState());
                continue;
            }

//            ArrayList<IbdInfo> infos = cur_info;//must exist
//            IbdInfo last_ii = infos.get(infos.size() - 1);//get last record (most recent)
//            Ibd50State prev_state = ii.getState();
            //symbol exists in new sheet
//            if (prev_state.equals(Ibd50State.Active) || prev_state.equals(Ibd50State.Onlist))
//                cur_ii.setState(Ibd50State.Active);
//            else
//                cur_ii.setState(Ibd50State.Onlist);
            cur_ii.setState(prev_state.equals(Ibd50State.Active) || prev_state.equals(Ibd50State.Onlist)
                ? Ibd50State.Active : Ibd50State.Onlist);
            cur_ii.setDate(file_cal);
            appendIbdInfo(cur_ii);
//System.err.println("--11111--" + map_symbol + " " + cur_ii.getDate() + " " + cur_ii.getState());
        }

        //examine all symbols in sheet, if not found in map, mark them Onlist
        for (String sym : sheet_symbols) {//none --> exist
            if (ibdinfo_map.get(sym) == null) {
                IbdInfo cur_ii = IbdInfo.findBySymbol(iis, sym);//must exist
                cur_ii.setState(Ibd50State.Onlist);
                cur_ii.setDate(file_cal);
                appendIbdInfo(cur_ii);
//System.err.println("-22222-" + sym + " " + cur_ii.getDate() + " " + cur_ii.getState());
            }
        }
        MainModel.getInstance().reloadIbdDb();
        _fldDate.setDate(file_cal.getTime());//set field
        populate();

        //get most recent Friday in the past
//            cal.setTime(_fldDate.getDate());
//        _chkShowAll.setSelected(false);
//        clickShowAll();
    }
    //helper to save code
    private void appendIbdInfo(IbdInfo ibd_info) throws IOException {
        ArrayList<IbdInfo> infos = new ArrayList<>(); infos.add(ibd_info);
        IbdInfo.persistIbdDb(infos);
    }
    //import user selected IBD50 sheets from a folder, re-create IBD.db
    private void importSheets() {
        //open file dialog with .xls extension, use pref to get last stored import path
        String lip = ApolloPreferenceStore.getPreferences().getLastIbd50Path();
        if (lip == null)
            lip = FrameworkConstants.DATA_FOLDER_IMPORT;
        JFileChooser fc = new JFileChooser(new File(lip));
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;
                int pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_XLS);
                return (pos > 0);
            }
            public String getDescription() {//this shows up in description field of dialog
                return ApolloConstants.APOLLO_BUNDLE.getString("act_lbl_2");
            }
        });
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
        if (ret == JFileChooser.APPROVE_OPTION) {
            _tmInfo.clear();//empty table
            ApolloPreferenceStore.getPreferences().setLastIbd50Path(fc.getCurrentDirectory().getPath());//save location
            ApolloPreferenceStore.savePreferences();//flush
            final File[] file_list = fc.getSelectedFiles();
            if (file_list.length < 2) {
                MessageBox.messageBox(MdiMainFrame.getInstance(),
                    Constants.COMPONENT_BUNDLE.getString("warning"),
                    ApolloConstants.APOLLO_BUNDLE.getString("ibd_14"),
                    MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                return;
            }
            MessageBox.messageBox(MdiMainFrame.getInstance(),
                Constants.COMPONENT_BUNDLE.getString("warning"),
                ApolloConstants.APOLLO_BUNDLE.getString("ibd_15"),
                MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);

            //start importing all selected sheets
            final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("ibd_13"));
            pb.setVisible(true);
            Thread import_thread = new Thread() {
                public void run() {
                    //remove IBD.db folder if applicable
                    File db_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_DB);
                    if (db_folder.exists()) {//remove all files in folder, keep folder
                        File[] files = db_folder.listFiles();
                        if (files != null) {
                            for (File f : files)
                                if (f.isFile()) {
                                    f.delete();
                                }
                        }
                    }
//                        FileUtil.delete(db_folder);
                    else {
                        db_folder.mkdir();
                    }
                    final ArrayList<String> failed_files = DataUtil.createIbdDb(file_list, pb);
                    final ArrayList<LogMessage> failed_msgs = new ArrayList<>();
                    for (String msg : failed_files)
                        failed_msgs.add(new LogMessage(LoggingSource.MARKETVIEW_IBD50, msg, null));
                    MainModel.getInstance().reloadIbdDb();//refresh in-memory data structure
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            pb.setVisible(false);
                            populate();
//                            _tmInfo.populate();
                            if (failed_msgs.size() > 0)
                                Props.Log.setValue(null, failed_msgs);
                        }
                    });
                }
            };
            import_thread.start();
        }
    }
    private void autoSort() {//sort by two columns
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(COLUMN_ONLIST_DATE, SortOrder.DESCENDING));
        keys.add(new RowSorter.SortKey(COLUMN_OFFLIST_DATE, SortOrder.DESCENDING));
        _Sorter.setSortKeys(keys);
    }

    //----- inner classes -----
    private class InfoTableModel extends DynaTableModel {
        private InfoTableModel() { super(DynaTableModel.generateSchema(TABLE_SCHEMA)); }

        public boolean isCellEditable(int row, int column) { return false; }

        //populate table based on .ibd files, show only onlist and offlist records
        public void populate() {
//            MainModel main_model = MainModel.getInstance();
//            HashMap<String,ArrayList<IbdInfo>> ibd_map = main_model.getIbdInfoMap();
//            Iterator<String> itor = ibd_map.keySet().iterator();
//            _lstRows.clear();
//            int seq = 1;  int cur_row = -1;//use this to guard off w/o on
//            while (itor.hasNext()) {
//                String symbol = itor.next();
//                ArrayList<IbdInfo> ibd_infos = ibd_map.get(symbol);
//                for (IbdInfo ii : ibd_infos) {
//                    if (ii.getState().equals(Ibd50State.Onlist)) {
//                        populateRow(ii, seq++);
//                        cur_row = getRowCount() - 1;//row > 0 indicates active interval started
//                    }
//                    else if (ii.getState().equals(Ibd50State.Offlist)) {
//                        getCell(cur_row, COLUMN_OFFLIST_DATE).setValue(AppUtil.calendarToString(ii.getDate()));
//                        cur_row = -1;//nothing to add
//                    }
//                }
//            }
//            fireTableDataChanged();
        }

        //only show rows with this day as entry, exit or no exit dates
        public void populate(Calendar cal) {
            MainModel main_model = MainModel.getInstance();
            HashMap<String,ArrayList<IbdInfo>> ibd_map = main_model.getIbdInfoMap();
            Iterator<String> itor = ibd_map.keySet().iterator();
            _lstRows.clear();
            int seq = 1;
            while (itor.hasNext()) {
                String sym = itor.next();
                ArrayList<IbdInfo> ibd_infos = ibd_map.get(sym);
                for (IbdInfo ii : ibd_infos) {
                    if (!cal.equals(ii.getDate()))
                        continue;

                    //only show ones that are Active, Onlist or Offlist
                    Ibd50State state = ii.getState();
                    if (state.equals(Ibd50State.Onlist))
                        populateRow(ii, seq++);
                    else if (state.equals(Ibd50State.Offlist))
                        populateRow(ii, seq++);
                    else if (state.equals(Ibd50State.Active))
                        populateRow(ii, seq++);
                }
            }
            fireTableDataChanged();
        }

        private void populateRow(IbdInfo ii, int seq) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            cells[COLUMN_SEQUENCE] = new SimpleCell(new Long(seq));
            cells[COLUMN_SYMBOL] = new SimpleCell(ii.getSymbol());
            cells[COLUMN_ONLIST_DATE] = new SimpleCell("");
            if (ii.getState().equals(Ibd50State.Onlist))
                cells[COLUMN_ONLIST_DATE] = new SimpleCell(AppUtil.calendarToString(ii.getDate()));
            cells[COLUMN_ONLIST_RANK] = new SimpleCell(new Long(ii.getRank()));
            cells[COLUMN_ONLIST_RS] = new SimpleCell(new Long(ii.getRs()));
            cells[COLUMN_ONLIST_EPS] = new SimpleCell(new Long(ii.getEps()));
            cells[COLUMN_ONLIST_SMR] = new SimpleCell(ii.getSmr());
            cells[COLUMN_ONLIST_COMPOSITE] = new SimpleCell(new Long(ii.getComposite()));
            cells[COLUMN_ONLIST_ACC_DIS] = new SimpleCell(ii.getAccDis());
            cells[COLUMN_OFFLIST_DATE] = new SimpleCell("");
            if (ii.getState().equals(Ibd50State.Offlist))
                cells[COLUMN_OFFLIST_DATE] = new SimpleCell(AppUtil.calendarToString(ii.getDate()));
            cells[COLUMN_STATE] = new SimpleCell(ii.getState());
            _lstRows.add(cells);
        }

        //find a symbol and return row index, return -1 if not found
        private int findSymbol(String symbol) {
            for (int row = 0; row < getRowCount(); row++) {
                String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
                if (sym.equals(symbol))
                    return row;
            }
            return -1;
        }
        private ArrayList<String> getSymbols() {
            ArrayList<String> ret = new ArrayList<>();
            for (int i=0; i<getRowCount(); i++) {
                String sym = (String)getCell(i, COLUMN_SYMBOL).getValue();
                ret.add(sym);
            }
            return ret;
        }
    }
    private class InfoRenderer extends DynaTableCellRenderer {
        public InfoRenderer(DynaTableModel model) { super(model); }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel comp = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = _tblInfo.convertRowIndexToModel(row); //for sortiing

            //change 0 values to empty
            if (value instanceof Long) {
                long v = (Long)value;
                if (v == 0)
                    comp.setText("");
                return comp;
            }

            //color green for matching entry date, color red for matching exit date
            if (column == COLUMN_ONLIST_DATE || column == COLUMN_OFFLIST_DATE) {
                String cell_date = (String)value;
                Date cur_date = _fldDate.getDate();
                Calendar cur_cal = Calendar.getInstance();
                cur_cal.setTime(cur_date);
                String date = AppUtil.calendarToString(cur_cal);
                if (cell_date.equals(date)) {
                    if (column == COLUMN_ONLIST_DATE) {
                        Object state = _tmInfo.getCell(row, COLUMN_STATE).getValue();
                        if (state.equals(Ibd50State.Onlist))
                            comp.setBackground(FrameworkConstants.COLOR_MEDIUM_GREEN);
                    }
                    else
                        comp.setBackground(FrameworkConstants.LIGHT_PINK);
                }
            }
            return comp;
        }
    }

    //----- variables -----
    private JLabel _lblDate = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("ibd_09"));
    private JXDatePicker _fldDate = new JXDatePicker();
    private JTextField _fldSearch = new JTextField(5);
    private JButton _btnCreateDb = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("ibd_11"), FrameworkIcon.FILES);
    private JButton _btnAppendSheet = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("ibd_17"), FrameworkIcon.FILE_ADD);
    private JButton _btnGenWatchList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_13"), FrameworkIcon.WATCH);
    private JButton _btnThumbnail = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scr_15"), FrameworkIcon.THUMBNAIL);
    private JTable _tblInfo;
    private InfoTableModel _tmInfo = new InfoTableModel();
    private RowSorter _Sorter;
//    private TimeSeriesPanel _pnlTimeSeriesGraph;
    private String _sCurSymbol;

    //----- literals -----
    private static final int COLUMN_SEQUENCE = 0;
    private static final int COLUMN_SYMBOL = 1;
    private static final int COLUMN_ONLIST_DATE = 2;
    private static final int COLUMN_ONLIST_RANK = 3;
    private static final int COLUMN_ONLIST_RS = 4;
    private static final int COLUMN_ONLIST_EPS = 5;
    private static final int COLUMN_ONLIST_SMR = 6;
    private static final int COLUMN_ONLIST_COMPOSITE = 7;
    private static final int COLUMN_ONLIST_ACC_DIS = 8;
    private static final int COLUMN_OFFLIST_DATE = 9;
    private static final int COLUMN_STATE = 10;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_55"), ColumnTypeEnum.TYPE_LONG, -1, 30, null, null, null},//sequence
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_30"), ColumnTypeEnum.TYPE_STRING, -1, 80, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_01"), ColumnTypeEnum.TYPE_STRING, -1, 80, null, null, null},//on list date
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_02"), ColumnTypeEnum.TYPE_LONG, -1, 60, null, null, null},//on list rank
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_03"), ColumnTypeEnum.TYPE_LONG, -1, 50, null, null, null},//on list RS
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_04"), ColumnTypeEnum.TYPE_LONG, -1, 50, null, null, null},//on list EPS
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_05"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//on list SMR
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_06"), ColumnTypeEnum.TYPE_LONG, -1, 90, null, null, null},//on list composite
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_07"), ColumnTypeEnum.TYPE_STRING, -1, 80, null, null, null},//on list acc/dis
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_08"), ColumnTypeEnum.TYPE_STRING, -1, 80, null, null, null},//off list date
        {ApolloConstants.APOLLO_BUNDLE.getString("ibd_10"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//state
    };
}
