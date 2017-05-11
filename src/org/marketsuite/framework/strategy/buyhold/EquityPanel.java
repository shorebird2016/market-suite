package org.marketsuite.framework.strategy.buyhold;

import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.marketsuite.framework.model.Equity;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.HeadingRenderer;
import org.marketsuite.component.util.WidgetUtil;

/**
 * Container for presenting equity information to user.
 */
public class EquityPanel extends JPanel {
    public EquityPanel() {
        setLayout(new GridLayout(1,1));
        setOpaque(false);
        _tblEquity = WidgetUtil.createDynaTable(_TableModel = new EquityTableModel(), ListSelectionModel.SINGLE_SELECTION,
             new HeadingRenderer(), false, new DynaTableCellRenderer(_TableModel));
        JScrollPane scr = new JScrollPane(_tblEquity); scr.getViewport().setOpaque(false);
        add(scr);
    }

    public void populate(ArrayList<Equity> log) {
        _TableModel.populate(log);
    }

    //-----instance variables-----
    private JTable _tblEquity;
    private EquityTableModel _TableModel;
}
