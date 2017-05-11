package org.marketsuite.scanner.earning;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.data.EarningInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.scanner.ScannerModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXDatePicker;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EarningPanel extends JPanel {
    public EarningPanel() {
        setLayout(new MigLayout("insets 0"));
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
                new MigLayout("insets 0", "5[][]20[][]20[][]push[]push[][]5", "3[]3")); ttl_pnl.setOpaque(false);
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("ep_03")));
        ttl_pnl.add(_cmbGroup); _cmbGroup.setFocusable(false); _cmbGroup.setOpaque(false);
        _cmbGroup.setModel(_cmGroup);
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("ep_05")));
        ttl_pnl.add(_fldBeginDate);
        _fldBeginDate.setDate(Calendar.getInstance().getTime());//today
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("ep_11")));
        ttl_pnl.add(_fldEndDate);
        _fldEndDate.setDate(AppUtil.findFutureFriday().getTime());//coming Friday
        ttl_pnl.add(_btnRun);
        _btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tmEarning.populate();
                updateRowHeights();
            }
        });
//        ttl_pnl.add(_btnSave);
//        _btnSave.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    DataUtil.writeEarningInfoDb(_tmEarning.getEarningInfo());
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4"));
//                }
//            }
//        });
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_35")));
        ttl_pnl.add(_txtSearch);
        _txtSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String txt = _txtSearch.getText().toUpperCase();
                if (txt.length() == 0) return;
                findSymbol(txt);
            }
        });
        add(ttl_pnl, "dock north");

        //center - table
        _tmEarning = new EarningTableModel();
        DynaTableCellRenderer renderer = new DynaTableCellRenderer(_tmEarning);
        _tblEarning = WidgetUtil.createDynaTable(_tmEarning, ListSelectionModel.SINGLE_SELECTION, new SortHeaderRenderer(), true, renderer);
        _tblEarning.setAutoCreateRowSorter(true);
        _Sorter = _tblEarning.getRowSorter();
        autoSort();
        _tblEarning.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = _tblEarning.getSelectedRow();
                String sym = (String)_tmEarning.getCell(_tblEarning.convertRowIndexToModel(row), COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(sym);
            }
        });
        add(new JScrollPane(_tblEarning), "dock center");

        //init
        _cmGroup.addElement(ApolloConstants.APOLLO_BUNDLE.getString("ep_12"));
        _cmGroup.addElement(ApolloConstants.APOLLO_BUNDLE.getString("ep_13"));
        _cmGroup.addElement(ApolloConstants.APOLLO_BUNDLE.getString("ep_18"));
    }

    //----- inner classes -----
    private class EarningTableModel extends DynaTableModel {
        private EarningTableModel() {
            super(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }
        public void populate() {
            //builds up symbol list based on _chkSearchAll checked or not
            ArrayList<String> symbol_list = new ArrayList<>();
            int sel = _cmbGroup.getSelectedIndex();
            if (sel == INDEX_ALL_DB) {
                File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
                String[] names = folder.list();
                for (String name : names) {
                    String sym = name.substring(0, name.lastIndexOf("."));
                    if (!symbol_list.contains(sym))//remove dup
                        symbol_list.add(sym);
                }
            }
            else if (sel == INDEX_ALL_WATCHLISTS) {//from all watch lists
                ArrayList<String> wl_list = GroupStore.getInstance().getGroupNames();
                for (String wl : wl_list) {
                    ArrayList<String> members = GroupStore.getInstance().getMembers(wl);
                    for (String mem : members)
                        if (!symbol_list.contains(mem))//remove dup
                            symbol_list.add(mem);
                }
            }
            else {//my own portfolio
                ArrayList<String> members = GroupStore.getInstance().getMembers("GRP - My Holding");
                for (String mem : members)
                    symbol_list.add(mem);//not possible to have duplicate
            }

            //search fundamental DB for earning dates, some won't have earnings like ETF
            HashMap<String, Fundamental> fund_map = MainModel.getInstance().getFundamentals();
            Calendar begin_cal = Calendar.getInstance();
            begin_cal.setTime(_fldBeginDate.getDate());
            Calendar end_cal = Calendar.getInstance();
            end_cal.setTime(_fldEndDate.getDate());
            _lstRows.clear();
            for (String sym : symbol_list) {
                Fundamental fundamental = fund_map.get(sym);
                if (fundamental == null) continue; //not in map
                String ed = fundamental.getEarningDate();
                if (ed == null || ed.equals("")) continue;//skip

                //this string may have two parts date and time, if there is time part, then report in AM
                String rpt = "?";
                String[] parts = ed.split(" ");
                if (ed.endsWith("AM")) rpt = "AM";
                else if (ed.endsWith("PM")) rpt = "PM";
                try {
                    Calendar cal = Calendar.getInstance();
                    Date dt = new SimpleDateFormat("MM/dd/yy").parse(parts[0]);
                    cal.setTime(dt);
                    if (cal.compareTo(begin_cal) < 0 || cal.compareTo(end_cal) > 0)
                        continue;//before today or beyond desired end date, skip
System.out.println("Found Symbol: " + sym);
                    SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                    cells[COLUMN_DATE] = new SimpleCell(AppUtil.calendarToString(cal));
                    cells[COLUMN_SYMBOL] = new SimpleCell(sym);
                    cells[COLUMN_NAME] = new SimpleCell(fundamental.getFullName());
                    ArrayList<String> grps = GroupStore.getInstance().findGroupsByMember(sym);
                    if (grps.size() == 0)
                        cells[COLUMN_GROUP] = new SimpleCell(fundamental.getIndustry());
                    else {//multiple groups use HTML in labels
                        StringBuilder buf = new StringBuilder("<html><b>");
                        for (String grp : grps)
                            buf.append(grp).append("<br>");
                        cells[COLUMN_GROUP] = new SimpleCell(buf.toString());
                    }
                    cells[COLUMN_AM_PM] = new SimpleCell(rpt);
//                    cells[COLUMN_GAP] = new SimpleCell(false);
//                    cells[COLUMN_REACTION] = new SimpleCell("");

                    //decorate
//                    cells[COLUMN_GAP].setHighlight(true);
//                    cells[COLUMN_REACTION].setHighlight(true);
                    _lstRows.add(cells);
                } catch (ParseException e) { //not parseable, skip
                    continue;
                }
            }
            fireTableDataChanged();
        }
        public boolean isCellEditable(int row, int column) { return getCell(row, column).isHighlight(); }
        private int findSymbol(String symbol) {
            for (int row = 0; row < getRowCount(); row++) {
                String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
                if (sym.equals(symbol))
                    return row;
            }
            return -1;
        }
//        private ArrayList<EarningInfo> getEarningInfo() {
//            ArrayList<EarningInfo> ret = new ArrayList<>();
//            for (int row = 0; row < getRowCount(); row++) {
//                boolean has_gap = (Boolean)getCell(row, COLUMN_GAP).getValue();
//                String dt = (String)getCell(row, COLUMN_DATE).getValue();
//                String reaction = (String) getCell(row, COLUMN_REACTION).getValue();
//                if (!reaction.equals(""))//stores only ones with comment
//                    ret.add(new EarningInfo(
//                        (String)getCell(row, COLUMN_SYMBOL).getValue(), dt, has_gap, reaction));
//            }
//            return ret;
//        }
    }

    private class HtmlRenderer extends DynaTableCellRenderer {
        private HtmlRenderer(DynaTableModel model) {
            super(model);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == COLUMN_GROUP) //simply render HTML
                lbl.setText((String)value);
            return lbl;
        }
    }

    //----- private methods -----
    private void autoSort() {//only the mean column by default
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(COLUMN_DATE, SortOrder.ASCENDING));
        _Sorter.setSortKeys(keys);
    }
    private void updateRowHeights() {
        for (int row = 0; row < _tblEarning.getRowCount(); row++) {
            int rowHeight = _tblEarning.getRowHeight();
            for (int column = 0; column < _tblEarning.getColumnCount(); column++) {
                Component comp = _tblEarning.prepareRenderer(_tblEarning.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            _tblEarning.setRowHeight(row, rowHeight);
        }
    }
    //find and scroll view
    private void findSymbol(String symbol) {
        //find which row and scroll into view
        int row = _tmEarning.findSymbol(symbol);
        row = _tblEarning.convertRowIndexToView(row);
        if (row < 0) {
            MessageBox.messageBox(ScannerModel.getInstance().getParent(),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_5"), MessageBox.STYLE_OK,
                    MessageBox.WARNING_MESSAGE);
            return;
        }
        WidgetUtil.scrollCellVisible(_tblEarning, row, COLUMN_SYMBOL);
        _tblEarning.getSelectionModel().setSelectionInterval(row, row);
    }

    //----- variables -----
    private JComboBox<String> _cmbGroup = new JComboBox<>();
    private DefaultComboBoxModel<String> _cmGroup = new DefaultComboBoxModel<>();
    private JTable _tblEarning;
    private EarningTableModel _tmEarning;
    private JXDatePicker _fldBeginDate = new JXDatePicker();
    private JXDatePicker _fldEndDate = new JXDatePicker();
    private JButton _btnRun = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("ep_16"), FrameworkIcon.RUN);
//    private JButton _btnSave = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("ep_17"), FrameworkIcon.FILE_SAVE);
    private JTextField _txtSearch = new JTextField(8);
    private RowSorter _Sorter;

    //----- literals -----
    private static final int INDEX_ALL_DB = 0;
    private static final int INDEX_ALL_WATCHLISTS = 1;
    private static final int COLUMN_DATE = 0;
    private static final int COLUMN_AM_PM = 1;
    private static final int COLUMN_SYMBOL = 2;
    private static final int COLUMN_NAME = 3;
    private static final int COLUMN_GROUP = 4;
//    private static final int COLUMN_GAP = 5;
//    private static final int COLUMN_REACTION = 6;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("ep_06"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//date of earning
        {ApolloConstants.APOLLO_BUNDLE.getString("ep_10"), ColumnTypeEnum.TYPE_STRING, -1, 20, null, null, null},//AM, PM, unknown
        {ApolloConstants.APOLLO_BUNDLE.getString("ep_07"), ColumnTypeEnum.TYPE_STRING, -1, 20, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("ep_08"), ColumnTypeEnum.TYPE_STRING,  0, 150, null, null, null},//name
        {ApolloConstants.APOLLO_BUNDLE.getString("ep_09"), ColumnTypeEnum.TYPE_STRING,  0, 150, null, null, null},//group
//        {ApolloConstants.APOLLO_BUNDLE.getString("ep_14"), ColumnTypeEnum.TYPE_BOOLEAN,-1, 20, null, null, null},//gap
//        {ApolloConstants.APOLLO_BUNDLE.getString("ep_15"), ColumnTypeEnum.TYPE_STRING,  2, 50, new PlainCellEditor(), null, null},//reaction
    };
}