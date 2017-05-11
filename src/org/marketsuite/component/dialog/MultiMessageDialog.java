package org.marketsuite.component.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import org.marketsuite.component.Constants;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;

/**
 * A container with table cells to describe result of bulk update action
 */
public class MultiMessageDialog extends JDialog {
    public MultiMessageDialog(JFrame parent, String title, ArrayList<String> msg_list) {
        super(parent, title, true);
        _MessageList = msg_list;
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5,2,5,2), BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
        //center - table
        ResultTableModel _TableModel = new ResultTableModel(DynaTableModel.generateSchema(TABLE_SCHEMA));
        JTable _tblMessage = WidgetUtil.createDynaTable(_TableModel, ListSelectionModel.SINGLE_SELECTION, new HeadingRenderer(), false, new DynaTableCellRenderer(_TableModel));
        JScrollPane scroll = new JScrollPane(_tblMessage);
        scroll.setPreferredSize(new Dimension(550, 250));//reduce basic height
        pnl.add(scroll, BorderLayout.CENTER);
        _TableModel.populate();//must populate before setVisible()

        //south - buttons
        JPanel btn_pnl = new JPanel();
        JButton btnOk = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        btn_pnl.add(btnOk);
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev){
                _bCancelled = false;
                dispose();
            }
        });
        pnl.add(btn_pnl, BorderLayout.SOUTH);
        setContentPane(pnl);

        //listeners
        //show tooltip
        _tblMessage.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent mev){
                JTable table = (JTable) mev.getSource();
                int col = table.columnAtPoint(mev.getPoint());
                int row = table.rowAtPoint(mev.getPoint());
                String msg = (String)table.getModel().getValueAt(row, col);
                table.setToolTipText(msg);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent wev) {
                _bCancelled = true;
                dispose();
            }
        });
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), true, parent, WindowConstants.DISPOSE_ON_CLOSE);
    }

    //inner classes
    private class ResultTableModel extends DynaTableModel {
		public ResultTableModel(ArrayList<ColumnSchema> schema) {
            remodel(schema);
        }

        public void populate() {
            _lstRows.clear();
            for (int row = 0; row < _MessageList.size(); row++) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_SEQUENCE] = new SimpleCell(String.valueOf(row + 1));
                cells[COLUMN_INDEX_MESSAGE] = new SimpleCell(_MessageList.get(row));
                _lstRows.add(cells);
            }
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    public boolean isCancelled() {
        return _bCancelled;
    }

    //instance variables
    private ArrayList<String> _MessageList;
    private boolean _bCancelled;

    //literals
    //table columns
    private static final int COLUMN_SEQUENCE = 0;
    private static final int COLUMN_INDEX_MESSAGE = 1;
    private static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { "#", ColumnTypeEnum.TYPE_STRING, 2, 50, null, null, null },//sequence number
        { Constants.COMPONENT_BUNDLE.getString("info_dlg_7"), ColumnTypeEnum.TYPE_STRING, 2, 500, null, null, null }//message
    };
}