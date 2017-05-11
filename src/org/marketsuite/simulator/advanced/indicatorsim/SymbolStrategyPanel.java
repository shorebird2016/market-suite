package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Container for report option selection of symbol and strategy.  This is the right lower part of report setup panel.
 */
class SymbolStrategyPanel extends JPanel {
    SymbolStrategyPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());

        //north - title strip with text and tool buttons
        JPanel wpnl = new JPanel();  wpnl.setOpaque(false);
        wpnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_1")));
        JPanel btn_pnl = new JPanel();  btn_pnl.setOpaque(false);
        btn_pnl.add(_btnAddSymbolStrategy);  _btnAddSymbolStrategy.setEnabled(false);
        _btnAddSymbolStrategy.setDisabledIcon(new DisabledIcon(LazyIcon.PLUS_SIGN.getImage()));
        _btnAddSymbolStrategy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AddSymbolDialog dlg = new AddSymbolDialog(_SymbolMap);
                if (dlg.isCancelled())
                    return;

                //put selections in table, update symbol map
                HashMap<String, ArrayList<Boolean>> sel = dlg.getSelection();
                _TableModel.addRows(sel);
                Set<String> syms = sel.keySet();
                for(String sym : syms)
                    _SymbolMap.put(sym, sel.get(sym));
            }
        });
        btn_pnl.add(_btnDeleteSymbolStrategy);  _btnDeleteSymbolStrategy.setEnabled(false);
        btn_pnl.add(Box.createGlue());
        _btnDeleteSymbolStrategy.setDisabledIcon(new DisabledIcon(LazyIcon.MINUS_SIGN.getImage()));
        _btnDeleteSymbolStrategy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] indices = _tblSymbolStrategy.getSelectedRows();
                //collect all symbols in a list, because array list deletion moves index, use object instead
                ArrayList<String> symbols = new ArrayList<String>();
                for (int i = 0; i < indices.length; i++) {
                    int model_row = _tblSymbolStrategy.convertRowIndexToModel(indices[i]);
                    symbols.add((String) _TableModel.getCell(model_row, COLUMN_SYMBOL).getValue());
                }

                //delete table rows, update symbol map
                if (indices.length > 0) {
                    _TableModel.deleteRows(symbols);
                    for (String symbol : symbols)
                        _SymbolMap.remove(symbol);
                }
            }
        });
        SkinPanel ttl_pnl = WidgetUtil.createTitleStrip(wpnl, null, btn_pnl);
        add(ttl_pnl, BorderLayout.NORTH);

        //center - table
        _tblSymbolStrategy = WidgetUtil.createDynaTable(_TableModel = new SymbolStrategyTableModel(), ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
            new SortHeaderRenderer(), false, new DynaTableCellRenderer(_TableModel)); _tblSymbolStrategy.setOpaque(false);

        //sorting support
        _tblSymbolStrategy.setAutoCreateRowSorter(true);
        _tblSymbolStrategy.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                _btnDeleteSymbolStrategy.setEnabled(!_tblSymbolStrategy.getSelectionModel().isSelectionEmpty());
            }
        });
        JScrollPane scr = new JScrollPane(_tblSymbolStrategy); scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
    }

    //-----public methods-----
    void clearTable() {
        _btnAddSymbolStrategy.setEnabled(false);
        _btnDeleteSymbolStrategy.setEnabled(false);
        _TableModel.clear();
    }

    void populate(HashMap<String, ArrayList<Boolean>> sym_map) {
        _SymbolMap = sym_map;
        _TableModel.populate(sym_map);
        _btnAddSymbolStrategy.setEnabled(true);

        //sort based on symbol automatically
        TableRowSorter sorter = (TableRowSorter)_tblSymbolStrategy.getRowSorter();
        RowSorter.SortKey sk = new RowSorter.SortKey(COLUMN_SYMBOL, SortOrder.ASCENDING);
        ArrayList<RowSorter.SortKey> sks = new ArrayList<RowSorter.SortKey>();
        sks.add(sk);
        sorter.setSortKeys(sks);//must set sort key first
        sorter.sort();//after populate, always sort
        _TableModel.fireTableDataChanged();
    }

    HashMap<String,ArrayList<Boolean>> getSymbolMap() {
        return _SymbolMap;
//        return _TableModel.modelToMap();
    }

    boolean isDirty() {
        return _bDirty;
    }
    void setDirty(boolean dirty) { _bDirty = dirty; }

    //-----private methods-----

    //-----inner classes-----
    private class SymbolStrategyTableModel extends DynaTableModel {
        private SymbolStrategyTableModel() {
            remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }

        public void populate() {
//            _lstRows.clearGraph();
//            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
////            cells[COLUMN_DIRTY] = new SimpleCell(DirtyCellRenderer.NORMAL);
//            cells[COLUMN_SYMBOL] = new SimpleCell("AAPL");
//            cells[COLUMN_STRATEGY_1] = new SimpleCell(true);
//            cells[COLUMN_STRATEGY_2] = new SimpleCell(true);
//            cells[COLUMN_STRATEGY_3] = new SimpleCell(false);
//            cells[COLUMN_STRATEGY_4] = new SimpleCell(false);
////            decorate(cells);
//            _lstRows.add(cells);
//
//            cells = new SimpleCell[TABLE_SCHEMA.length];
////            cells[COLUMN_DIRTY] = new SimpleCell(DirtyCellRenderer.NORMAL);
//            cells[COLUMN_SYMBOL] = new SimpleCell("GOOG");
//            cells[COLUMN_STRATEGY_1] = new SimpleCell(false);
//            cells[COLUMN_STRATEGY_2] = new SimpleCell(true);
//            cells[COLUMN_STRATEGY_3] = new SimpleCell(false);
//            cells[COLUMN_STRATEGY_4] = new SimpleCell(true);
////            decorate(cells);
//            _lstRows.add(cells);
//
//            cells = new SimpleCell[TABLE_SCHEMA.length];
////            cells[COLUMN_DIRTY] = new SimpleCell(DirtyCellRenderer.NORMAL);
//            cells[COLUMN_SYMBOL] = new SimpleCell("ISRG");
//            cells[COLUMN_STRATEGY_1] = new SimpleCell(true);
//            cells[COLUMN_STRATEGY_2] = new SimpleCell(false);
//            cells[COLUMN_STRATEGY_3] = new SimpleCell(false);
//            cells[COLUMN_STRATEGY_4] = new SimpleCell(true);
////            decorate(cells);
//            _lstRows.add(cells);
//            fireTableDataChanged();
        }

        public boolean isCellEditable(int row, int col) {
            return col > COLUMN_SYMBOL;
        }

        //only checkboxes can be clicked on / off
        public void setValueAt(Object value, int row, int column) {
            //check to see if no checkbox is checked, provide warning if so
            boolean cur_off_to_on = (Boolean)value;
            if (!cur_off_to_on) {//current cell on --> off
                boolean no_other_checked = true;
                for (int col = COLUMN_STRATEGY_1; col <= COLUMN_STRATEGY_4; col++) {
                    if (col != column && (Boolean)getCell(row, col).getValue()) { //found one checked
                        no_other_checked = false;
                        break;
                    }
                }
                if (no_other_checked) {//leave the way it was, at least one checked
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_1"),
                        MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE
                    );
                    return;
                }
            }

            //persist value to model
            super.setValueAt(value, row, column);
            _bDirty = true;

            //update inherent model for parent panel to track templates
            String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
            _SymbolMap.get(sym).set(column - COLUMN_STRATEGY_1, (Boolean)value);
        }

        //populate all rows with supplied symbol map
        void populate(HashMap<String, ArrayList<Boolean>> symbol_map) {
//            _SymbolMap = symbol_map;
            _lstRows.clear();
            Set<String> symbols = symbol_map.keySet();
            Iterator<String> itor = symbols.iterator();
            while (itor.hasNext()) {
                String symbol = itor.next();
                ArrayList<Boolean> strategy_flags = symbol_map.get(symbol);
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
                for (int col = COLUMN_STRATEGY_1; col <= COLUMN_STRATEGY_4; col++ )
                    cells[col] = new SimpleCell(strategy_flags.get(col - COLUMN_STRATEGY_1));
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }

//        private void clearGraph() {
//            _lstRows.clearGraph();
//            fireTableDataChanged();
//        }
//todo
        private void decorate(SimpleCell[] cells) {
            cells[COLUMN_STRATEGY_1].setEnableHighlight(true, true);
            cells[COLUMN_STRATEGY_2].setHighlight(true);
            cells[COLUMN_STRATEGY_3].setHighlight(true);
            cells[COLUMN_STRATEGY_4].setHighlight(true);
        }

        //add new rows with specified map
        private void addRows(HashMap<String,ArrayList<Boolean>> symbol_map) {
            Set<String> keys = symbol_map.keySet();
            for (String key : keys) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
//                cells[COLUMN_DIRTY] = new SimpleCell(DirtyCellRenderer.ADD);
                cells[COLUMN_SYMBOL] = new SimpleCell(key);
                ArrayList<Boolean> strategy_sels = symbol_map.get(key);
                for (int col = COLUMN_STRATEGY_1; col <= COLUMN_STRATEGY_4; col++)
                    cells[col] = new SimpleCell(strategy_sels.get(col - COLUMN_STRATEGY_1));
                _lstRows.add(cells);
//decorate
            }
            fireTableDataChanged();
            _bDirty = true;
        }

        //delete selected rows, because list deletion moves index, use object deletion instead
        private void deleteRows(ArrayList<String> symbols) {
            for (String symbol : symbols) {
                for (int row = 0; row < getRowCount(); row++) {
                    if ( (getCell(row, COLUMN_SYMBOL).getValue()).equals(symbol) ) {
                        _lstRows.remove(row);
                        fireTableRowsDeleted(row, row);
                    }
                }
            }
            _bDirty = true;
        }

        //create hash map from table model
        private HashMap<String, ArrayList<Boolean>> modelToMap() {
            HashMap<String, ArrayList<Boolean>> ret = new HashMap<String, ArrayList<Boolean>>();
            for (int row = 0; row < getRowCount(); row++) {
                String key = ((String)getCell(row, COLUMN_SYMBOL).getValue());
                ArrayList<Boolean> value = new ArrayList<Boolean>();
                for (int col = COLUMN_STRATEGY_1; col <= COLUMN_STRATEGY_4; col++ )
                    value.add((Boolean)getCell(row, col).getValue());
                ret.put(key, value);
            }
            return ret;
        }
    }

    //-----instance variables-----
    private JTable _tblSymbolStrategy;
    private SymbolStrategyTableModel _TableModel;
    private JButton _btnAddSymbolStrategy = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_tip_5"), LazyIcon.PLUS_SIGN);
    private JButton _btnDeleteSymbolStrategy = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_tip_6"), LazyIcon.MINUS_SIGN);
    private HashMap<String, ArrayList<Boolean>> _SymbolMap = new HashMap<String, ArrayList<Boolean>>();
    private boolean _bDirty;

    //-----literals-----
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_STRATEGY_1 = 1;
    private static final int COLUMN_STRATEGY_2 = 2;
    private static final int COLUMN_STRATEGY_3 = 3;
    private static final int COLUMN_STRATEGY_4 = 4;
    private static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_1"), ColumnTypeEnum.TYPE_STRING,  -1, 20, null, null, null },//symbol
        { ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_2"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 40, null, null, null },//MAC
        { ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_3"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 40, null, null, null },//MACD zero cross
        { ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_4"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 40, null, null, null },//RSI
        { ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_5"), ColumnTypeEnum.TYPE_BOOLEAN, -1, 40, null, null, null },//Stochastic
    };
}
