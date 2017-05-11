package org.marketsuite.component.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableUtil {
    private static final String PROPERTY_COLUMNS = "columns";
    private static final String PROPERTY_FIXED_COLUMNS = "fixedColumns";

    public enum Limit {MIN, MAX, BOTH, NONE}

    public static final LayerUI dirtyCellUI = new LayerUI() {
        private static final long serialVersionUID = 1L;
        Color bgColor = new Color(255, 0, 0, 192);
        Path2D.Float triangle = new Path2D.Float();

        {
            triangle.moveTo(0, 0);
            triangle.lineTo(3, -5);
            triangle.lineTo(6, 0);
            triangle.closePath();
        }

        public void paint(Graphics g1, JComponent c) {
            Graphics2D g = (Graphics2D) g1;
            super.paint(g, c);
            Object dirty = c.getClientProperty("dirty");
            if (Boolean.TRUE.equals(dirty)) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.translate(c.getWidth() - 7, 7);
                //g.translate(0,6);
                g.setColor(bgColor);
                g.fill(triangle);
            }
        }
    };

    public static JTable fixColumns(JScrollPane jsp, final JTable origTable, int num) {
        return fixColumns(jsp, origTable, num, null);
    }

    public static JTable fixColumns(JScrollPane jsp, final JTable origTable, int num, KeyListener kListener) {
        TableColumnModel origColumnModel = origTable.getColumnModel();
        TableColumnModel fixedColumnModel = new DefaultTableColumnModel();
        TableColumn col;
        for (int i = 0; i < num; i++) {
            col = origColumnModel.getColumn(0);
            origColumnModel.removeColumn(col);
            fixedColumnModel.addColumn(col);
        }

        final ArrayList<ColumnPosition> _columnList = new ArrayList<ColumnPosition>();
        int size = origColumnModel.getColumnCount();
        for (int i = 0; i < size; i++) {
            _columnList.add(new ColumnPosition(origColumnModel.getColumn(i)));
        }

        origTable.putClientProperty(PROPERTY_COLUMNS, _columnList);  // client property used in 'setColumnsVisible'
        origTable.putClientProperty(PROPERTY_FIXED_COLUMNS, num);  // client property used in 'setColumnsVisible'

        JTable fixedTable = new JTable(origTable.getModel(), fixedColumnModel);
        fixedTable.setRowHeight(origTable.getRowHeight());

        if (kListener != null) {
            fixedTable.addKeyListener(kListener);
        }

        fixedTable.setRowSorter(origTable.getRowSorter());
        fixedTable.getTableHeader().setReorderingAllowed(false);
        fixedTable.getTableHeader().setResizingAllowed(true);
        fixedTable.getTableHeader().setDefaultRenderer(origTable.getTableHeader().getDefaultRenderer());
        fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());
        fixedTable.setSelectionMode(origTable.getSelectionModel().getSelectionMode());

        // Copy renderers from original table
        TableModel tm = origTable.getModel();
        for (int i = 0; i < tm.getColumnCount(); i++) {
            Class klass = tm.getColumnClass(i);
            fixedTable.setDefaultRenderer(klass, origTable.getDefaultRenderer(klass));
            fixedTable.setDefaultEditor(klass, origTable.getDefaultEditor(klass));
        }

        // add the new fixed table and header to scroll pane
        final JViewport origVp = jsp.getViewport();
        JViewport jvp = new JViewport() {
            private static final long serialVersionUID = 1L;

            public void scrollRectToVisible(Rectangle rect) {
                // delegate scrolling to the scrollpane to keep drag selecting in sync
                origVp.scrollRectToVisible(rect);
            }
        };
        jvp.setView(fixedTable);
        jsp.setRowHeader(jvp);

        jsp.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedTable.getTableHeader());

        JLabel lab = new JLabel();
        lab.setOpaque(true);
        lab.setBackground(jsp.getViewport().getBackground());
        jsp.setCorner(JScrollPane.LOWER_LEFT_CORNER, lab);

        // use the same selection model to keep selections in sync between the 2 tables
        fixedTable.setSelectionModel(origTable.getSelectionModel());

        // add drop target if original table had one
        final DropTarget origTarget = origTable.getDropTarget();
        if (origTarget != null) {
            DropTarget fixedTarget = new DropTarget() {
                private static final long serialVersionUID = 1L;

                public void drop(DropTargetDropEvent e) {
                    origTarget.drop(e);
                }

                public void dragOver(DropTargetDragEvent dtde) {
                    origTarget.dragOver(dtde);
                }
            };
            fixedTable.setDropTarget(fixedTarget);
        }

        // copy listeners from original table
        for (MouseListener ml : origTable.getMouseListeners()) {
            //if(ml instanceof TableUtil.MouseAdapter)
            if (ml instanceof TableUtil.FixedTableAdapter)
                fixedTable.addMouseListener(ml);
        }

        for (MouseMotionListener ml : origTable.getMouseMotionListeners()) {
            //if(ml instanceof TableUtil.MouseMotionAdapter)
            if (ml instanceof TableUtil.FixedTableAdapter)
                fixedTable.addMouseMotionListener(ml);
        }

        return fixedTable;
    }

    public static boolean columnMoved(JTable table, int from, int to) {
        ColumnPosition cp = null;
        //System.out.println(from + "  " + to);
        if (from == to) return false;
        ArrayList<ColumnPosition> columnList = (ArrayList<ColumnPosition>) table.getClientProperty(PROPERTY_COLUMNS);
        if (columnList == null) return false;
        int temp = -1;
        int pos;
        // find 'from' in the columns list
        for (pos = 0; pos < columnList.size(); pos++) {
            cp = columnList.get(pos);
            if (!cp.visible) continue;
            temp++;
            if (temp == from) {
                columnList.remove(pos);
                break;
            }
        }

        // Assumption: 'to' and 'from' are adjacent
        if (to < from)
            for (--pos; pos >= 0; pos--) {
                if (columnList.get(pos).visible) {
                    // add to the left of this column
                    columnList.add(pos, cp);
                    return true;
                }
            }

        for (; pos < columnList.size(); pos++) {
            if (columnList.get(pos).visible) {
                // add to the right of this column
                columnList.add(pos + 1, cp);
                return true;
            }
        }
        return false;
    }

    public static int[] getColumnOrder(JTable table, int fixedColumns) {
        ArrayList<ColumnPosition> columnList = (ArrayList<ColumnPosition>) table.getClientProperty(PROPERTY_COLUMNS);
        if (columnList == null) return null;
        int[] order = new int[columnList.size()];
        for (int i = 0; i < order.length; i++) {
            ColumnPosition cp = columnList.get(i);
            order[i] = cp.tc.getModelIndex() - fixedColumns;
        }
        return order;
    }

    public static void setColumnOrder(JTable table, int[] order) {
        ArrayList<ColumnPosition> columnList = (ArrayList<ColumnPosition>) table.getClientProperty(PROPERTY_COLUMNS);
        ArrayList<ColumnPosition> temp = new ArrayList<ColumnPosition>();
        if (order == null) return;
        TableColumnModel tcm = table.getColumnModel();
        int count = tcm.getColumnCount();
        if (order.length != count) return;
        for (int i = 0; i < order.length; i++) {
            if (order[i] >= count) continue;
            if (columnList == null)
                temp.add(new ColumnPosition(tcm.getColumn(order[i])));
            else
                temp.add(columnList.get(order[i]));
        }
        for (int i = 0; i < count; i++) tcm.removeColumn(tcm.getColumn(0));
        for (ColumnPosition cp : temp)
            tcm.addColumn(cp.tc);
        table.putClientProperty(PROPERTY_COLUMNS, temp);
    }

    public static void setColumnsVisible(JTable table, boolean[] vis) {
        ArrayList<ColumnPosition> columnList = (ArrayList<ColumnPosition>) table.getClientProperty(PROPERTY_COLUMNS);
        if (vis == null) return;
        TableColumnModel tcm = table.getColumnModel();
        if (columnList == null) {
            columnList = new ArrayList<ColumnPosition>();
            for (int i = 0; i < vis.length; i++) columnList.add(new ColumnPosition(tcm.getColumn(i)));
            table.putClientProperty(PROPERTY_COLUMNS, columnList);
        }
        // remove all columns from the model
        int size = tcm.getColumnCount();
        for (int i = 0; i < size; i++) tcm.removeColumn(tcm.getColumn(0));

        for (ColumnPosition cp : columnList) {
            TableColumn tc = cp.tc;
            // visibility array is always in model order
            int index = tc.getModelIndex();
            if (index >= vis.length)
                cp.visible = true;
            else
                cp.visible = vis[index];
            if (cp.visible) {
                tcm.addColumn(cp.tc);
                //System.out.println("vis: " + "  " + cp.tc.getHeaderValue());
            }
        }

        // add a blank column so that the header stays visible
        if (tcm.getColumnCount() == 0) {
            TableColumn tc = new TableColumn(0, 0);
            tc.setHeaderValue("Dummy");
            tcm.addColumn(tc);
        }

    }

    private static ColumnPosition getColumn(int i, ArrayList<ColumnPosition> columnList) {
        for (ColumnPosition cp : columnList) {
            if (cp.tc.getModelIndex() == i) return cp;
        }
        return null;
    }

    public static void resetOrder(JTable table) {
        ArrayList<ColumnPosition> columnList = (ArrayList<ColumnPosition>) table.getClientProperty(PROPERTY_COLUMNS);
        if (columnList == null) return;
        int fixedColumns = (Integer) table.getClientProperty(PROPERTY_FIXED_COLUMNS);
        ArrayList<ColumnPosition> temp = new ArrayList<ColumnPosition>();
        int size = columnList.size();
        for (int i = 0; i < size; i++) {
            temp.add(getColumn(i + fixedColumns, columnList));
        }

        DefaultTableColumnModel tcm = (DefaultTableColumnModel) table.getColumnModel();
        int count = tcm.getColumnCount();
        for (int i = 0; i < count; i++) tcm.removeColumn(tcm.getColumn(0));
        for (int i = 0; i < size; i++) {
            if (temp.get(i).visible)
                tcm.addColumn(temp.get(i).tc);
        }
        table.putClientProperty(PROPERTY_COLUMNS, temp);
        tcm.moveColumn(0, 1); // trigger columnMoved callback to save the preferences
        tcm.moveColumn(0, 1); // move column back

    }

    // use these classes for mouse listeners that should be applied to the fixed columns table also
    public interface FixedTableAdapter {/**/
    }

    public static class MouseAdapter extends java.awt.event.MouseAdapter implements FixedTableAdapter { /* */
    }

    public static class MouseMotionAdapter extends java.awt.event.MouseMotionAdapter implements FixedTableAdapter { /* */
    }

    // class to add visibility to a TableColumn
    private static class ColumnPosition {
        private boolean visible;
        private TableColumn tc;

        public ColumnPosition(TableColumn _tc) {
            this(_tc, true);
        }

        public ColumnPosition(TableColumn _tc, boolean _visible) {
            tc = _tc;
            visible = _visible;
        }
    }

    public static class TableColumnModelAdapter implements TableColumnModelListener {
        public void columnMoved(TableColumnModelEvent ev) { /* NA */ }

        public void columnAdded(TableColumnModelEvent ev) { /* NA */ }

        public void columnMarginChanged(ChangeEvent ev) { /* NA */ }

        public void columnRemoved(TableColumnModelEvent ev) { /* NA */ }

        public void columnSelectionChanged(ListSelectionEvent ev) { /* NA */ }
    }

    // set the column width to fit the larger of the header or a cell populated with 'val'
    public static int setColumnWidth(JTable table, int colnum, Object val, int pad, Limit limit) {
        return setColumnWidth(table, table.getColumnModel().getColumn(colnum), val, pad, limit);
    }

    public static int setColumnWidth(JTable table, int colnum, Object val, int pad, boolean limit) {
        return setColumnWidth(table, table.getColumnModel().getColumn(colnum), val, pad, limit ? Limit.MAX : Limit.NONE);
    }

    public static int setColumnWidth(JTable table, TableColumn col, Object val, int pad, boolean limit) {
        return setColumnWidth(table, col, val, pad, limit ? Limit.MAX : Limit.NONE);
    }

    public static int setColumnWidth(JTable table, TableColumn col, Object val, int pad, Limit limit) {
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
        int width = comp.getPreferredSize().width + pad;

        renderer = col.getCellRenderer();
        if (renderer == null)
            renderer = table.getDefaultRenderer(val.getClass());
        comp = renderer.getTableCellRendererComponent(table, val, false, false, 0, col.getModelIndex());
        int width2 = comp.getPreferredSize().width + pad;
        width = Math.max(width, width2);


        col.setPreferredWidth(width);
        switch (limit) {
            case BOTH:
                col.setMaxWidth(width);
                col.setMinWidth(width);
                break;
            case MIN:
                col.setMinWidth(width);
                break;
            case MAX:
                col.setMaxWidth(width);
                break;
            case NONE:
                //
        }
        return width;
    }

    public static void updateRowHeights(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = table.getRowHeight();
            for (int column = 0; column < table.getColumnCount(); column++) {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            table.setRowHeight(row, rowHeight);
        }
    }

}
