package org.marketsuite.watchlist.speed;

import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

//Relative performance of various time periods. eg. 2weeks, 3 months..etc
public class SpeedViewPanel extends JPanel {
    //CTOR - use card layout for two panels, one is strategy simulator, another is scanner
    public SpeedViewPanel() {
        setLayout(new MigLayout("insets 0")); setOpaque(false); setBorder(new BevelBorder(BevelBorder.LOWERED));

        //center - result table and price chart
        _TableModel = new SpeedViewTableModel();
        _tblSpeed = new JTable(_TableModel);
        WidgetUtil.initDynaTable(_tblSpeed, _TableModel, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new SpeedRenderer());
        _tblSpeed.setOpaque(false);
        _tblSpeed.setAutoCreateRowSorter(true);
        _Sorter = _tblSpeed.getRowSorter();

        //handle row selection to display daily/weekly/analysis graphs
        _tblSpeed.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;

                //nothing selected, disable delete, close button
                int row = _tblSpeed.getSelectedRow();
                if (row == -1) {//de-selection
                    Props.SymbolSelection.setValue("");
                    return;
                }

                //notify other frames
                row = _tblSpeed.convertRowIndexToModel(row);
                String symbol = (String) _TableModel.getCell(row, SpeedViewTableModel.COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(symbol);
            }
        });
        _tblSpeed.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_A == e.getKeyCode() && (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
                    //refresh view for all symbols in map
                    _TableModel.showAllSymbols();
                    _lblCount.setText("#" + String.valueOf(_TableModel.getRowCount()));
                }
                //ctrl-I to read file again
                else if (KeyEvent.VK_I == e.getKeyCode() && (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
                    MainModel.getInstance().reloadSpeedMap();
                    _TableModel.populate();
                    _lblCount.setText("#" + String.valueOf(_TableModel.getRowCount()));
                }
            }
        });
        JScrollPane scr = new JScrollPane(_tblSpeed);
        scr.getViewport().setOpaque(false);
        add(scr, "dock center");

        //bottom - status, buttons
        JPanel south_pnl = new JPanel(new MigLayout("insets 0", "5[]5", "3[]3"));
        south_pnl.add(_lblCount);
        add(south_pnl, "dock south");
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
        row = _tblSpeed.convertRowIndexToView(row);
        WidgetUtil.scrollCellVisible(_tblSpeed, row, SpeedViewTableModel.COLUMN_SYMBOL);
        _tblSpeed.getSelectionModel().setSelectionInterval(row, row);

    }
    public void populate() {
        _tblSpeed.clearSelection();
        _TableModel.populate();
        if (_TableModel.getRowCount() > 0) {
            _tblSpeed.getSelectionModel().setSelectionInterval(0,0);
            autoSort();//default to 1 week, 2 week, 1 month
        }
        _lblCount.setText("#" + String.valueOf(_TableModel.getRowCount()));
    }

    //----- inner classes -----
    private class SpeedRenderer extends DynaTableCellRenderer {
        private SpeedRenderer() {
            super(_TableModel);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = _tblSpeed.convertRowIndexToModel(row);
            column = _tblSpeed.convertColumnIndexToModel(column);
            Object val = _TableModel.getCell(row, column).getValue();
            JLabel lbl = (JLabel)comp;
            switch (column) {
                case SpeedViewTableModel.COLUMN_MONTH_PCT:
                case SpeedViewTableModel.COLUMN_WEEK_PCT:
                case SpeedViewTableModel.COLUMN_MONTHLY_SPEED:
                case SpeedViewTableModel.COLUMN_WEEKLY_SPEED:
                    lbl.setText(FrameworkConstants.PCT_FORMAT.format((Float)val));
                    if (column == SpeedViewTableModel.COLUMN_MONTHLY_SPEED || column == SpeedViewTableModel.COLUMN_WEEKLY_SPEED)
                        lbl.setBackground(FrameworkConstants.COLOR_LITE_GREEN);
                    break;
            }
            return comp;
        }
    }

    //----- private methods -----
    //hide all symbols that are among top N performance in current time frame
    private void autoSort() {
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(SpeedViewTableModel.COLUMN_MONTHLY_SPEED, SortOrder.DESCENDING));
        keys.add(new RowSorter.SortKey(SpeedViewTableModel.COLUMN_WEEKLY_SPEED, SortOrder.DESCENDING));
        _Sorter.setSortKeys(keys);
    }

    //----- accessor -----

    //-----instance variables-----
    private JTable _tblSpeed;
    private JLabel _lblCount = new JLabel();
    private SpeedViewTableModel _TableModel;
    private RowSorter _Sorter;
}