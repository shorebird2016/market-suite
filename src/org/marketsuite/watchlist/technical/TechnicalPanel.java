package org.marketsuite.watchlist.technical;

import org.marketsuite.component.Constants;
import org.marketsuite.component.comparator.DoubleComparator;
import org.marketsuite.component.comparator.IntegerComparator;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Container for "Market" tab which includes phase, condition, 10/30 MA cross, 50/120 EMA cross, cherrypicker..etc.
 */
public class TechnicalPanel extends SkinPanel {
    public TechnicalPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        setLayout(new BorderLayout());
        setOpaque(false);

        //center - result table and price chart
        _TableModel = new TechnicalTableModel();
        _tblTech = new JTable(_TableModel);
        WidgetUtil.initDynaTable(_tblTech, _TableModel, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new TechRenderer());
        _tblTech.setOpaque(false);
        _tblTech.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //nothing selected, disable delete, close button
                int row = _tblTech.getSelectedRow();
                if (row == -1) {//de-selection
                    Props.SymbolSelection.setValue("");
                    return;
                }

                //draw graph
                row = _tblTech.convertRowIndexToModel(row);
                String symbol = (String) _TableModel.getCell(row, TechnicalTableModel.COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(symbol);
            }
        });
        _tblTech.setAutoCreateRowSorter(true);
        _Sorter = (TableRowSorter) _tblTech.getRowSorter();//special comparator for Accumulation/Distribution
        _Sorter.setComparator(TechnicalTableModel.COLUMN_IBD_ACC_DIS, new IbdComparator());
        JScrollPane scr = new JScrollPane(_tblTech);
        scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
    }

    //----- public methods -----
    public void populate(WatchListModel parent_model) {
        _tblTech.clearSelection();
        _TableModel.populate(parent_model);
        computeRanking(); autoSort();
    }
    public void findSymbol(String symbol) {
        if (symbol.equals(""))  return;//de-select sends empty string

        //find which row and scroll into view
        int row = _TableModel.findSymbol(symbol);
        if (row < 0) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_5") + " " + symbol, LoggingSource.L_SQUARE_PERFORMANCE);
            return;
        }
        row = _tblTech.convertRowIndexToView(row);
        WidgetUtil.scrollCellVisible(_tblTech, row, TechnicalTableModel.COLUMN_SYMBOL);
        _tblTech.getSelectionModel().setSelectionInterval(row, row);

    }
    public void showHideSymbol(String symbol, boolean show_symbol) {
        _TableModel.showHideSymbol(symbol, show_symbol);
        computeRanking();
        if (show_symbol) findSymbol(symbol);
    }

    //----- private methods -----
    private void computeRanking() {
        _TableModel.computeRanking(TechnicalTableModel.COLUMN_NEAR_20SMA, TechnicalTableModel.COLUMN_NEAR_200SMA, new DoubleComparator());
        _TableModel.computeRanking(TechnicalTableModel.COLUMN_IBD_COMPOSITE, TechnicalTableModel.COLUMN_IBD_RS, new IntegerComparator());
    }
    private void autoSort() {
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(TechnicalTableModel.COLUMN_NEAR_50SMA, SortOrder.DESCENDING));
        keys.add(new RowSorter.SortKey(TechnicalTableModel.COLUMN_IBD_COMPOSITE, SortOrder.DESCENDING));
        _Sorter.setSortKeys(keys);
    }

    //----- inner classes -----
    //compare among A, A+, B, B+, C, C+, A-, B-, C-
    private class IbdComparator implements Comparator<Object>, Serializable {
        public int compare(Object o1, Object o2) {
            String s1 = (String)o1;
            String s2 = (String)o2;

            //empty strings
            if (s1.equals("")) {
                if (s2.equals("")) return 0;
                else return -1;
            }
            if (s2.equals("")) return 1;

            //first letter different, reverse natural order (A > B > C)
            String s11 = s1.substring(0,1);  String s21 = s2.substring(0,1);
            if (!s11.equals(s21))
                return -(s1.compareTo(s2));

            //both strings are the same
            if (s1.equals(s2))
                return 0;

            //both same length, must be the same if 1
            int len1 = s1.length(); int len2 = s2.length();
            if (len1 == len2) {
                if (len1 == 1)
                    return 0;
                else {//2 char long for both
                    String s22 = s2.substring(1,2);
                    if (s22.equals("+"))
                        return -1;//A+ compare A-
                    else
                        return 1;//A- compare A+
                }
            }

            // different length
            if (len1 == 1) {
                String s22 = s2.substring(1,2);
                if (s22.equals("+"))
                    return -1;
                else
                    return 1;
            }
            else {//2nd len is 1
                String s12 = s1.substring(1,2);
                if (s12.equals("+"))
                    return 1;
                else
                    return -1;
            }
        }
    }
    private class TechRenderer extends DynaTableCellRenderer {
        private TechRenderer() { super(_TableModel); }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = _tblTech.convertRowIndexToModel(row);
            column = _tblTech.convertColumnIndexToModel(column);
            Object val = _TableModel.getCell(row, column).getValue();
            JLabel lbl = (JLabel)comp;
//            double price = (Double)_TableModel.getCell(row, TechnicalTableModel.COLUMN_SMR).getValue();
//            if (price == 0) {//use SMR equal to 0 to blank out cells, since no data exist
//                if (column != TechnicalTableModel.COLUMN_SYMBOL)
//                    lbl.setText("");
//                return comp;
//            }

            switch (column) {
                case TechnicalTableModel.COLUMN_IBD_EPS://turn 0s into blanks
                case TechnicalTableModel.COLUMN_IBD_RS:
                case TechnicalTableModel.COLUMN_IBD_COMPOSITE:
                    if ((Long)val == 0)
                        lbl.setText("");
                    break;

                case TechnicalTableModel.COLUMN_NEAR_20SMA://these are percentages
                case TechnicalTableModel.COLUMN_NEAR_50SMA:
                case TechnicalTableModel.COLUMN_NEAR_200SMA:
                    lbl.setText(FrameworkConstants.ROI_FORMAT.format((Double)val));
                    break;
            }

            //if 3 or less rows in the table, skip highlighting
            if (_TableModel.getRowCount() <= 3)
                return comp;

            //is this cell rank top 3 of this column?  highlight green if so
            ArrayList<Integer> rank_idx = _TableModel.getRankingMap().get(new Integer(column));
            if (rank_idx != null && rank_idx.size() > 0) {
                for(int i=0; i<3; i++) {//top 3
                    if (row == rank_idx.get(i))
                        lbl.setBackground(FrameworkConstants.COLOR_MEDIUM_GREEN);
                }
                int end_idx = rank_idx.size();
                for (int i=end_idx-1; i>=(end_idx-3); i--) {//bottom 3
                    if (row == rank_idx.get(i))
                        lbl.setBackground(Constants.CELL_EDITING_BACKGROUND);//red
                }
            }
            return comp;
        }
    }

    //----- instance variables -----
    private JTable _tblTech;
    private TechnicalTableModel _TableModel;
    private TableRowSorter _Sorter;
}
