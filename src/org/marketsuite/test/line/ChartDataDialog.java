package org.marketsuite.test.line;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Set;

/**
 * A container with table cells to show chart data at cursor location
 */
public class ChartDataDialog extends JDialog {
    /**
     * CTOR: display initial chart data values
     * @param value_map a hash map with key = property name, value = property value
     */
    public ChartDataDialog(HashMap<String, String> value_map) {
        super(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("cw_lbl_2"), false);
        setIconImage(ApolloIcon.APP_ICON.getImage());
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 2, 5, 2),
                BorderFactory.createBevelBorder(BevelBorder.LOWERED)));

        //center - table
        _TableModel = new ResultTableModel();
        _Table = WidgetUtil.createDynaTable(_TableModel, ListSelectionModel.SINGLE_SELECTION, new HeadingRenderer(),
                false, new DynaTableCellRenderer(_TableModel));
        _Table.setRowSelectionAllowed(false);
        pnl.add(new JScrollPane(_Table), BorderLayout.CENTER);
        setContentPane(pnl);

        //fill with data
        if (value_map != null)
            _TableModel.populate(value_map);
        WidgetUtil.setDialogProperties(this, new Dimension(180, 280), true, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    //----- public/protected methods ------
    public void populate(HashMap<String, String> value_map) { _TableModel.populate(value_map); }

    //----- inner classes -----
    private class ResultTableModel extends DynaTableModel {
        public ResultTableModel() {
            remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }
        public boolean isCellEditable(int row, int col) { return false; }
        public void populate() { }
        public void populate(HashMap<String, String> value_map) {
            _lstRows.clear();
            for (String key : PROPERTIES) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_PROPERTY] = new SimpleCell(key);
                cells[COLUMN_VALUE] = new SimpleCell(value_map.get(key));
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
    }

    //----- variables -----
    private JTable _Table;
    private ResultTableModel _TableModel;

    //----- literals -----
    //Table columns
    private static final int COLUMN_PROPERTY = 0;
    private static final int COLUMN_VALUE = 1;
    //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
    private static final Object[][] TABLE_SCHEMA = {
        {"", ColumnTypeEnum.TYPE_STRING, 2, 100, null, null, null},
        {"", ColumnTypeEnum.TYPE_STRING, 2, 120, null, null, null}
    };
    public static final String[] PROPERTIES = {//for data window
        "Date", "Open", "High", "Low", "Close", "Volume", "10 MA", "30 MA", "50 MA", "200 MA",
        "MACD", "RSI", "Stochastic"
    };
}