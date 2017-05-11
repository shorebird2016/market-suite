package org.marketsuite.framework.strategy.base;

import org.marketsuite.framework.model.Transaction;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.HeadingRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.HeadingRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Container for trade list table.
 */
public class TradePanel extends JPanel {
    public TradePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        _TradeTableModel = new TradeTableModel();
        JTable tbl = WidgetUtil.createDynaTable(_TradeTableModel, ListSelectionModel.SINGLE_SELECTION,
                new HeadingRenderer(), false, new DynaTableCellRenderer(_TradeTableModel));
        tbl.setOpaque(false);
        JScrollPane scr = new JScrollPane(tbl); scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
    }
    public void populate(ArrayList<Transaction> trans) { _TradeTableModel.populate(trans); }
    void clear() { _TradeTableModel.clear(); }

    //-----inner classes-----
    private class TradeCellRenderer extends DynaTableCellRenderer {
        public TradeCellRenderer(DynaTableModel model) {
            super(model);
        }

        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int row, int column) {
//            String sym = (String)_Model.getCell(row, MarketTableModel.COLUMN_DIRECTION).getValue();
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(jTable, o, b, b1, row, column);
            if (column != TradeTableModel.COLUMN_DIRECTION)
                return lbl;
            lbl.setText("");
            return lbl;
        }
    }

    //-----instance variables-----
    private TradeTableModel _TradeTableModel;
}
