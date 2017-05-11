package org.marketsuite.marektview.ranking;

import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.type.RankingSamplePeriod;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.scanner.common.WatchlistSelectorWindow;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Container for scanning group rankings, automate market view activities.
 * Note: the calculations performed in this tab are based on "Fridays" only, thus it may be different from Marekt View
 */
public class RankPanel extends JPanel {
    public RankPanel() {
        setLayout(new MigLayout("insets 0"));
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
            new MigLayout("insets 0", "5[]push[]push[][]30[]5[]5", "3[]3")); ttl_pnl.setOpaque(false);
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scr_05")); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl_pnl.add(lbl);
        ttl_pnl.add(_spnTopPct);

        //two buttons for combine group report
        ttl_pnl.add(_btnSelect);
        _btnSelect.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (_wndSelector == null) {
                    _wndSelector = new WatchlistSelectorWindow();
                }
                else if (_wndSelector.isVisible()) {//close window if already out
                    _wndSelector.setVisible(false);
                    return;
                }
                _wndSelector.setVisible(true);
                int y = e.getYOnScreen() + 10;
                int x = e.getXOnScreen() - _wndSelector.getSize().width *4 / 5;
                _wndSelector.setLocation(x, y);
            }
        });
        ttl_pnl.add(_btnRunSelection);
        _btnRunSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_wndSelector == null) return;
                _wndSelector.setVisible(false);
                ArrayList<String> group_names = _wndSelector.getSelectedList();
                if (group_names.size() == 0) return;//skip no selection
                _tmRank.mergePopulate(group_names);
                TableUtil.updateRowHeights(_tblRank);
                if (_bFullRun) {
                    _tmRank.clear();//clear table if previous run was full run
                    _bFullRun = false;
                }
            }
        });

        //full run, clear and plot thumbnail
        ttl_pnl.add(_btnFullRun);
        _btnFullRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tmRank.populate();
                TableUtil.updateRowHeights(_tblRank);
                _bFullRun = true;
            }
        });
        ttl_pnl.add(_btnClearAll);
        ttl_pnl.add(_btnThumbnail);
        _btnThumbnail.setEnabled(false);
        _btnThumbnail.setDisabledIcon(new DisabledIcon(FrameworkIcon.THUMBNAIL.getImage()));
        _btnThumbnail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] row_sel = _tblRank.getSelectedRows();
                int[] col_sel = _tblRank.getSelectedColumns();

                //collect symbols from all CellInfo within selection, remove duplicate
                ArrayList<String> symbols = new ArrayList<>();
                for (int row_idx = 0; row_idx < row_sel.length; row_idx++) {
                    for (int col_idx = 0; col_idx < col_sel.length; col_idx++) {
                        Object obj = _tmRank.getCell(row_sel[row_idx], col_sel[col_idx]).getValue();
                        if (obj instanceof CellInfo) {
                            CellInfo ci = (CellInfo)obj;
                            float pct = (float)_spnTopPct.getValue() / 100;
                            int max = (int)(ci.recent_rank.size() * pct);
                            if (max < 3) max = 3;//minimum 3 symbols
                            int count = 0;
                            for (RankElement re : ci.recent_rank) {
                                if (!symbols.contains(re.symbol))//remove duplicate
                                    symbols.add(re.symbol);
                                count++;
                                if (count == max)
                                    break;
                            }
                        }
                    }
                }
                Props.PlotThumbnails.setValue(null, symbols);
            }
        });
        _btnClearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tmRank.clear();
                if (_wndSelector != null)
                    _wndSelector.setVisible(false);
            }
        });
        add(ttl_pnl, "dock north");

        //center - table
        _tmRank = new RankTableModel();
        DynaTableCellRenderer renderer = new DynaTableCellRenderer(_tmRank);
        _tblRank = WidgetUtil.createDynaTable(_tmRank, ListSelectionModel.SINGLE_SELECTION, new HeadingRenderer(), true, renderer);
        _tblRank.setCellSelectionEnabled(true);
        _tblRank.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _tblRank.setDefaultRenderer(String.class, new RankRenderer(_tmRank));

        //handle cell selection
        _tblRank.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                _btnThumbnail.setEnabled(!_tblRank.getSelectionModel().isSelectionEmpty());
            }
        });
        add(new JScrollPane(_tblRank), "dock center");
    }

    //for Scanner frame moving, tab changes
    public void closeOptionWindow() {
        if (_wndSelector != null)
            _wndSelector.setVisible(false);
    }

    //----- inner classes -----
    private class RankTableModel extends DynaTableModel {
        private RankTableModel() {
            super(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }
        public void populate() {
            _lstRows.clear();
//TODO no errors??????
//            final ArrayList<String> failed_lists = new ArrayList<>();//always such possibility
            ArrayList<String> wl_list = GroupStore.getInstance().getGroupNames();
            for (String wl : wl_list) {
                WatchListModel wlm = new WatchListModel(wl, false);//some members may not exist in DB, already has a thread
                populateCell(wlm, wl);
            }
//TODO why can't I update row one by one so that it can show while prog bar is up?????????
//                int row = getRowCount() - 1;
//                fireTableRowsUpdated(row, row);
//            }
            fireTableDataChanged();
        }
        public boolean isCellEditable(int row, int column) { return false; }

        //to merge all symbols from selected groups and report performance ranking
        private void mergePopulate(ArrayList<String> group_names) {
            //merge group names into one html string, each group one row
            StringBuilder name_list = new StringBuilder("<html>");

            //collect union of names from these groups w/o duplicate
            ArrayList<String> names = new ArrayList<>();
            for (String grp : group_names) {
                name_list.append(grp).append("<br>");
                ArrayList<String> members = GroupStore.getInstance().getMembers(grp);
                for (String member : members)
                    if (!names.contains(member)) {//remove duplicate
                        names.add(member);
                    }
            }
            WatchListModel wlm = new WatchListModel(names, "");//some members may not exist in DB, already has a thread
            _lstRows.clear();
            populateCell(wlm, name_list.toString());
        }

        //----- private methods -----
        //use wlm to calculate recent ranks and historical ranks, save them into CellInfo for renderer
        //  all columns in a row except COLUMN_SYMBOL are calculated/stored
        //  hist_map only use last 4 elements in this array -> most recent 4 weeks, bi-weeks, months...etc
        private void populateCell(WatchListModel wlm, String symbol) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
//TODO below calcRankMapBySegment() already does calcRank() for the 1st segment, I did twice there, SIMPLIFY...
            //weekly = 7 days, go back 5 quote points instead calendar Fridays
            HashMap<String, ArrayList<Integer>> hist_map = AppUtil.calcRankMapBySegment(RankingSamplePeriod.WEEKLY, wlm);
            ArrayList<RankElement> recent_rank = AppUtil.calcRank(5, 0, wlm);//sorted low to high(powerful first)
            cells[COLUMN_1_WEEK] = new SimpleCell(new CellInfo(recent_rank, hist_map));

            //bi-weekly, 10 trading days segment
            hist_map = AppUtil.calcRankMapBySegment(RankingSamplePeriod.BI_WEEKLY, wlm);
            recent_rank = AppUtil.calcRank(10, 0, wlm);
            cells[COLUMN_2_WEEK] = new SimpleCell(new CellInfo(recent_rank, hist_map));

            //monthly
            hist_map = AppUtil.calcRankMapBySegment(RankingSamplePeriod.MONTHLY, wlm);
            recent_rank = AppUtil.calcRank(20, 0, wlm);
            cells[COLUMN_1_MONTH] = new SimpleCell(new CellInfo(recent_rank, hist_map));
//            cells[] = new SimpleCell(new CellInfo());

//TODO:
            //3 month, 6 months
            cells[COLUMN_3_MONTH] = new SimpleCell(new CellInfo());//formatCell(ranks_wkly.get(tfc[COLUMN_3_MONTH - 1]), null, pct);
            cells[COLUMN_6_MONTH] = new SimpleCell(new CellInfo());//formatCell(ranks_wkly.get(tfc[COLUMN_6_MONTH - 1]), null, pct);
            _lstRows.add(cells);
            fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
        }
    }

    private void updateRowHeights() {
        for (int row = 0; row < _tblRank.getRowCount(); row++) {
            int rowHeight = _tblRank.getRowHeight();
            for (int column = 0; column < _tblRank.getColumnCount(); column++) {
                Component comp = _tblRank.prepareRenderer(_tblRank.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            _tblRank.setRowHeight(row, rowHeight);
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

    //----- inner classes -----
//    private class RankRenderer extends DynaTableCellRenderer {
//        private RankRenderer(DynaTableModel model) {
//            super(model);
//        }
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
////            if (column != COLUMN_SYMBOL)
//                lbl.setText((String)value);
//            return lbl;
//        }
//    }
    private class RankRenderer extends DynaTableCellRenderer {
        private RankRenderer(DynaTableModel model) {
            super(model);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == COLUMN_SYMBOL) //simply render HTML
                lbl.setText((String)value);
            else {//all other cells have CellInfo
                CellInfo ci = (CellInfo)value;
                float pct = (float)_spnTopPct.getValue() / 100;
if (ci == null)
    System.out.println();
if (ci != null)
                lbl.setText(cellToHtml(ci.recent_rank, ci.hist_map, pct));
            }
            return lbl;
        }
    }

    //a convinence container for storing into all columns except COLUMN_SYMBOL
    private class CellInfo {
        private CellInfo() {} //empty cell
        private CellInfo(ArrayList<RankElement> recent_rank, HashMap<String, ArrayList<Integer>> hist_map) {
            this.recent_rank = recent_rank;
            this.hist_map = hist_map;
        }
        private ArrayList<RankElement> recent_rank;
        private HashMap<String, ArrayList<Integer>> hist_map;
    }

    //----- variables -----
    private JButton _btnSelect = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scr_14"), FrameworkIcon.DOWN_ARROW);
    private JButton _btnRunSelection = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scr_13"), FrameworkIcon.REFRESH);
    private JButton _btnFullRun = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scr_07"), FrameworkIcon.RUN);
    private JButton _btnThumbnail = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scr_15"), FrameworkIcon.THUMBNAIL);
    private JButton _btnClearAll = WidgetUtil.createIconButton(Constants.COMPONENT_BUNDLE.getString("clear_all"), FrameworkIcon.CLEAR);
    private JTable _tblRank;
    private RankTableModel _tmRank;
    private IntegerSpinner _spnTopPct = new IntegerSpinner(ApolloConstants.APOLLO_BUNDLE.getString("scr_12"), 30, 5, 100, 5,
            ApolloConstants.APOLLO_BUNDLE.getString("scr_06"), null);
    private WatchlistSelectorWindow _wndSelector;
    private boolean _bFullRun;

    //----- literals -----
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_1_WEEK = 1;
    private static final int COLUMN_2_WEEK = 2;
    private static final int COLUMN_1_MONTH = 3;
    private static final int COLUMN_3_MONTH = 4;
    private static final int COLUMN_6_MONTH = 5;
    private static final Object[][] TABLE_SCHEMA = {
            {ApolloConstants.APOLLO_BUNDLE.getString("scr_00"), ColumnTypeEnum.TYPE_STRING, 5, 80, null, null, null},//symbol / group name
            {ApolloConstants.APOLLO_BUNDLE.getString("scr_01"), ColumnTypeEnum.TYPE_STRING, 0, 100, null, null, null},//1 week
            {ApolloConstants.APOLLO_BUNDLE.getString("scr_02"), ColumnTypeEnum.TYPE_STRING, 0, 100, null, null, null},//2 week
            {ApolloConstants.APOLLO_BUNDLE.getString("scr_03"), ColumnTypeEnum.TYPE_STRING, 0, 100, null, null, null},//1 month
            {ApolloConstants.APOLLO_BUNDLE.getString("scr_04"), ColumnTypeEnum.TYPE_STRING, 5, 20, null, null, null},//3 month
            {ApolloConstants.APOLLO_BUNDLE.getString("scr_11"), ColumnTypeEnum.TYPE_STRING, 5, 20, null, null, null},//6 month
    };
}