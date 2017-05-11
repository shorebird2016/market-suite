package org.marketsuite.component.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import org.marketsuite.component.Constants;
import org.marketsuite.component.table.ColumnSchema;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.HeadingRenderer;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.component.table.ColumnTypeEnum;

/**
 * A container with table cells to describe result of bulk action
 */
public class MultiResultDialog extends JDialog {
    /**
     * CTOR: All three arguments share the same index
     *
     * @param parent      parent container
     * @param image       dialog icon
     * @param title       dialog title
     * @param action_list a list of actions taken by user
     * @param exceptions  null = for no exception
     */
    public MultiResultDialog(JFrame parent, Image image, String title, ArrayList<String> action_list, ArrayList<Exception> exceptions) {
        super(parent, title, true);
        setIconImage(image);
        _ChangeList = action_list;
        _Exceptions = exceptions;
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 2, 5, 2),
                BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
        //center - table
        ArrayList<ColumnSchema> schema = DynaTableModel.generateSchema(TABLE_SCHEMA);
        _TableModel = new ResultTableModel(schema);
        _Table = WidgetUtil.createDynaTable(_TableModel, ListSelectionModel.SINGLE_SELECTION, new HeadingRenderer(),
                false, new DynaTableCellRenderer(_TableModel));
        _Table.setRowSelectionAllowed(false);
        _Table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {//this only happens when mouse away from table area
                hideUnderline();
                _Table.repaint();//trigger renderer
            }

            public void mousePressed(MouseEvent e) {//click link opens detail dialog
                JDialog dlg = new JDialog(MultiResultDialog.this, Constants.COMPONENT_BUNDLE.getString("msg_title_1"), true);
                JTextArea txa = new JTextArea(100, 100);
                int row = _Table.rowAtPoint(e.getPoint());
                StringWriter wr = new StringWriter();
                _Exceptions.get(row).printStackTrace(new PrintWriter(wr));
                txa.setText(wr.toString());
                txa.setEditable(false);
                dlg.setContentPane(new JScrollPane(txa));
                WidgetUtil.setDialogProperties(dlg, new Dimension(600, 450), true, MultiResultDialog.this, WindowConstants.DISPOSE_ON_CLOSE);
            }
        });
        _Table.addMouseMotionListener(new MouseAdapter() {//handle underlining of cell

            public void mouseMoved(MouseEvent e) { //paint the link
                Point new_position = e.getPoint();//new point
                int col = _Table.columnAtPoint(new_position);
                if (COLUMN_INDEX_EXCEPTION == col) {
                    //turn off underline from last position
                    hideUnderline();

                    //turn on underline for new position
                    int new_row = _Table.rowAtPoint(new_position);
                    _TableModel.getCell(new_row, COLUMN_INDEX_EXCEPTION).setUnderline(true);
                    _LastRow = new_row;
                } else
                    hideUnderline();
                _Table.repaint();//trigger renderer
            }
        });
        pnl.add(new JScrollPane(_Table), BorderLayout.CENTER);
        setContentPane(pnl);
        //fill with data
        _TableModel.populate();
        WidgetUtil.setDialogProperties(this, new Dimension(600, 200), false, parent, WindowConstants.DISPOSE_ON_CLOSE);
    }

    //private methods
    private void hideUnderline() {
        if (_LastRow != -1)
            _TableModel.getCell(_LastRow, COLUMN_INDEX_EXCEPTION).setUnderline(false);
    }

    //inner classes
    private class ResultTableModel extends DynaTableModel {
        public ResultTableModel(ArrayList schema) {
            remodel(schema);
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void populate() {
            _lstRows.clear();
            for (int row = 0; row < _ChangeList.size(); row++) {
                SimpleCell[] cells = new SimpleCell[COLUMN_NAMES_KEY.length];
                cells[COLUMN_INDEX_ID] = new SimpleCell(Long.valueOf(row + 1));
                cells[COLUMN_INDEX_ACTION] = new SimpleCell(_ChangeList.get(row));
                cells[COLUMN_INDEX_EXCEPTION] = new SimpleCell(_Exceptions.get(row).getMessage());
                _lstRows.add(cells);
            }
        }
    }

    //instance variables
    private JTable _Table;
    private ResultTableModel _TableModel;
    private ArrayList<String> _ChangeList;
    private ArrayList<Exception> _Exceptions;
    private int _LastRow = -1;//last known row where mouse was positioned

    //literals
    //_Table columns
    private static final int COLUMN_INDEX_ID = 0;//sequence number
    private static final int COLUMN_INDEX_ACTION = 1;//user action
    private static final int COLUMN_INDEX_EXCEPTION = 2;//result ends in exception
    private static final String[] COLUMN_NAMES_KEY = {"#", "Changes", "Exception", "Detail"};
    //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
    private static final Object[][] TABLE_SCHEMA = {
            {COLUMN_NAMES_KEY[0], ColumnTypeEnum.TYPE_LONG, 2, 30, null, null, null},
            {COLUMN_NAMES_KEY[1], ColumnTypeEnum.TYPE_STRING, 2, 200, null, null, null},
            {COLUMN_NAMES_KEY[2], ColumnTypeEnum.TYPE_STRING, 2, 500, null, null, null}
    };
    private static final long serialVersionUID = 7464644811625148196L;
}