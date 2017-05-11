package org.marketsuite.component.table;

import org.marketsuite.component.Constants;
import org.marketsuite.component.util.ObjectCloner;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

/**
 * A general purpose dynamic table model to support on the fly column changes and data refreshes.
 */
public abstract class DynaTableModel extends AbstractTableModel {
	public DynaTableModel() {
		//
    }

    //----- abstract methods -----
    /**
     * To populate table rows with given data and stores data into _lstRows.
     */
    public abstract void populate();

    //----- interface and overrides -----
    public synchronized  Object getValueAt(int row, int column) {//no special handling
        SimpleCell[] cells = _lstRows.get(row);
        return cells[column].getValue();
    }
    public int getColumnCount() {
        int ret = 0;
        for (int i = 0;i < _TableSchema.size(); i++) {
            ColumnSchema column_schema = _TableSchema.get(i);
            if (column_schema.isVisible()) {
                ret++;
            }
        }
        return ret;
    }
    public int getRowCount() {
        return _lstRows.size();
    }
    public String getColumnName(int column) {
        ColumnSchema sch =  _TableSchema.get(column);
        return sch.getName();
    }
    public Class getColumnClass(int column) {
        ColumnSchema sch =  _TableSchema.get(column);
        return sch.getColumnClass();
    }
    public boolean isCellEditable(int row, int column) {
        SimpleCell[] cells = _lstRows.get(row);
        return cells[column].isEnabled();
    }
    public void setValueAt(Object value, int row, int column) {
       setValueAt(value, row, column, true);
   }

    //----- public methods -----
    // set value with explicit dirty argument
    public void setValueAt(Object value, int row, int column, boolean dirty) {
       SimpleCell[] cells = _lstRows.get(row);
       cells[column].setValue(value);
       cells[column].setDirty(dirty);
   }
    /**
     * To change column structures of this table.
     * @param master_schema a list of new column schema
     */
    public void remodel(List<ColumnSchema> master_schema) {
        //only use visible ones from master
        _TableSchema = new ArrayList<ColumnSchema>();
        for (int i=0; i<master_schema.size(); i++) {
            ColumnSchema column_schema = master_schema.get(i);
            if (column_schema.isVisible())
                _TableSchema.add(column_schema);
        }
        fireTableStructureChanged();
    }
    public DynaTableModel(List<ColumnSchema> schema) {
        _TableSchema = schema;
    }
    public TableCellRenderer getCellRenderer(int column) {
        ColumnSchema cs = _TableSchema.get(column);
        return cs.getRenderer();
    }
    public Comparator getCellComparator(int column) {
        ColumnSchema cs = _TableSchema.get(column);
        return cs.getComparator();
    }
    public void clear() {
        _lstRows.clear();
        fireTableDataChanged();
    }
    public static ArrayList<ColumnSchema> generateTableSchema(Object[][] schema) {
        ArrayList<ColumnSchema> table_schema = new ArrayList<ColumnSchema>();
        for (int column = 0; column < schema.length; column++) {//traverse all columns
            ColumnSchema sch = new ColumnSchema(
                ((String)schema[column][0]),//name
                ((Integer)schema[column][1]).intValue(),//type
                ((Boolean)schema[column][2]).booleanValue(),//visible
                ((Boolean)schema[column][3]).booleanValue(),//hideable
                ((Integer)schema[column][4]).intValue(),//alignment
                ((Integer)schema[column][5]).intValue(),//width
                ((TableCellEditor) schema[column][6]),//editor
                ((TableCellRenderer)schema[column][7]),//renderer
                ((Comparator) schema[column][8])//comparator
            );
            table_schema.add(sch);
        }
        return table_schema;
    }
    public static ArrayList<ColumnSchema> generateSchema(Object[][] schema) {
        ArrayList<ColumnSchema> table_schema = new ArrayList<ColumnSchema>();
        for (int column = 0; column < schema.length; column++) {//traverse all columns
            ColumnSchema sch = new ColumnSchema(
                ((String)schema[column][0]),//name
                ((Integer)schema[column][1]).intValue(),//type
                ((Integer)schema[column][2]).intValue(),//alignment
                ((Integer)schema[column][3]).intValue(),//width
                ((TableCellEditor) schema[column][4]),//editor
                ((TableCellRenderer)schema[column][5]),//renderer
                ((Comparator) schema[column][6])//comparator
            );
            table_schema.add(sch);
        }
        return table_schema;
    }
    //To add a row to the end of table.
    public final void add(SimpleCell[] cells) {
        _lstRows.add(cells);
        int row = getRowCount(); //new row index
        fireTableRowsInserted(row - 1, row - 1);
    }
    //To delete currently selected row(s) from table.
    public void delete() {
        if (_nSelectedRows == null) return;
        int sel_cnt = _nSelectedRows.length;
        if (sel_cnt <= 0) return;
        int start_row = _nSelectedRows[0];
        int end_row = _nSelectedRows[sel_cnt - 1];
        int count = end_row - start_row + 1;
        for (int i=0; i<count; i++)
            _lstRows.remove(start_row);//after remove, old pushed up, always the same index
        _nCurrentRow = -1;
        _nSelectedRows = null;
        fireTableRowsDeleted(start_row, end_row);
    }
    //to delete a number of rows from table (copy rows not marked for deletion to a new _lstRows)
    public void deleteRows(int[] row_indices) { //a list of indices to delete from _lstRows
        ArrayList<SimpleCell[]> tmp =  new ArrayList<>();
        for (int lr_idx = 0; lr_idx < _lstRows.size(); lr_idx++) {//perform copy
            //if lr_idx is in row_indices, skip copy
            boolean found = false;
            for (int idx = 0; idx < row_indices.length; idx++) {
                if (lr_idx == row_indices[idx]) {
                    found = true; break; }
            }
            if (!found)
                tmp.add(_lstRows.get(lr_idx));
            else
                System.err.println("-------" + _lstRows.get(lr_idx)[0].getValue());
        }
        _lstRows = tmp;//re-assign reference
        fireTableDataChanged();//visual refresh
    }
    //To duplicate(paste) currently selected row(s) to the end of table.
    public final void copy() {
        int sel_cnt = _nSelectedRows.length;
        if (sel_cnt <= 0)
            return;

        int last_row = _lstRows.size() - 1;
        int start_row = _nSelectedRows[0];
        int end_row = _nSelectedRows[sel_cnt - 1];
        for (int row = start_row; row <= end_row; row++) {
            SimpleCell[] src = _lstRows.get(row);
            SimpleCell[] dst_cell = (SimpleCell[]) ObjectCloner.copy(src);
            _lstRows.add(dst_cell);
        }
        _nCurrentRow = -1;
        _nSelectedRows = null;
        fireTableRowsInserted(last_row, _lstRows.size() - 1);
    }
    /**
     * To check/uncheck a range of selected cells of the same column anchored by mouse.
     * @param column where mouse is anchoring
     * @param check true = check all cells
     */
    public final void checkRange(int column, boolean check) {
        int sel_cnt = _nSelectedRows.length;
        if (sel_cnt <= 0)
            return;

        int start_row = _nSelectedRows[0];
        int end_row = _nSelectedRows[_nSelectedRows.length - 1];
        for (int row=start_row; row<=end_row; row++) {
            SimpleCell cell = getCell(row, column);
            if (!cell.isEnabled())//skip disabled cell
                continue;

            cell.setValue(Boolean.valueOf(check));
        }
        fireTableRowsUpdated(start_row, end_row);
    }
    /**
     * To perform copy down action for mouse anchored column using first selected row.
     * @param column
     */
    public final void copyDown(int column) {
        int sel_cnt = _nSelectedRows.length;
        if (sel_cnt <= 0)
            return;

        int start_row = _nSelectedRows[0];
        int end_row = _nSelectedRows[_nSelectedRows.length - 1];
        SimpleCell cell = getCell(start_row, column);
        for (int row=start_row + 1; row<=end_row; row++) {
            if (!cell.isEnabled())//skip disabled cell
                continue;

            getCell(row, column).setValue(cell.getValue());
        }
        fireTableRowsUpdated(start_row + 1, end_row);
    }
    /**
     * To perform fill inc/dec action for mouse anchored column.
     * @param column starting column number
     * @param increment true = increment, false = decrement
     * @param amount by this number up or down
     * @param byte_position see literal FIRST_OCTET...etc
     */
    public void fillIncDecRange(int column, boolean increment, long amount, int byte_position) {
        if (_nSelectedRows == null || _nSelectedRows.length == 0)
            return;

        int start_row = _nSelectedRows[0];
        int end_row = _nSelectedRows[_nSelectedRows.length - 1];

        //based on type of column, handle inc/dec
        ColumnSchema cs =  _TableSchema.get(column);
        int col_type = cs.getType();

        //based on type, prepare start value, low and high limit
        long base = 0;
        long lower_limit = 0;
        long upper_limit = 0;
        FillDownParam param = null;
        switch(col_type) {
            case ColumnTypeEnum.TYPE_LONG:
                base = ((Long)getCell(start_row, column).getValue()).longValue();
                lower_limit = Constants.MIN_NUMBER;
                upper_limit = Constants.MAX_NUMBER;
                break;

            case ColumnTypeEnum.TYPE_STRING:
                param = new FillDownParam((String)getCell(start_row, column).getValue(), FillDownParam.TRAILING_NUMBER, -1);
                base = param.getBaseNumber();
                lower_limit = Constants.MIN_STRING;
                upper_limit = Constants.MAX_STRING;
                break;

            case ColumnTypeEnum.TYPE_IPV4_ADDRESS:
                param = new FillDownParam((String)getCell(start_row, column).getValue(), FillDownParam.IP_OCTET, byte_position);
                base = param.getBaseNumber();
                lower_limit = Constants.MIN_BYTE;
                upper_limit = Constants.MAX_BYTE;
                break;

            default:
                return;//do nothing
        }

        for (int row = start_row + 1; row <= end_row; row++) {
            SimpleCell cell = getCell(row, column);
            if (!cell.isEnabled())
                continue;//skip disabled ones

            switch(col_type) {
                case ColumnTypeEnum.TYPE_LONG:
                    if (increment)
                        base += amount;
                    else
                        base -= amount;
                    //protect out of range
                    if (base > upper_limit)
                        base = lower_limit;
                    else if (base < lower_limit)
                        base = upper_limit;
                    cell.setValue(Long.valueOf(base));
                    break;

                case ColumnTypeEnum.TYPE_STRING:
                    if (increment)
                        base += amount;
                    else
                        base -= amount;
                    //protect out of range
                    if (base > upper_limit)
                        base = lower_limit;
                    else if (base < lower_limit)
                        base = upper_limit;
                    if(param != null){
                    	cell.setValue(param.getPrefix() + base);
                    }
                    break;

                case ColumnTypeEnum.TYPE_IPV4_ADDRESS:
                    if (increment) {
                        base += amount;
                        if (base > upper_limit)
                            base = lower_limit;
                    }
                    else {
                        base -= amount;
                        if (base < lower_limit)
                            base = upper_limit;
                    }
                    if(param != null){
                    	String new_ip = param.getPrefix() + String.valueOf(base) + param.getPostfix();
                    	cell.setValue(new_ip);
                    }
                    break;
            }
        }
        fireTableRowsUpdated(start_row + 1, end_row);
    }
    public final void init() {
        _nCurrentRow = -1;
        _nSelectedRows = null;
    }
    //To setup column width and cell editor
    public void initTable(TableColumnModel column_model){
        for (int col = 0; col < getColumnCount(); col++) {
            TableColumn column = column_model.getColumn(col);
            int with = getColumnWidth(col);
            column.setPreferredWidth(with);
        }
        //cell editors
        int max = getColumnCount();
        for (int i = 0; i < max ; i++) {
            TableColumn column = column_model.getColumn(i);
            TableCellEditor editor = ( _TableSchema.get(i)).get_Editor();
            if (editor != null)
                column.setCellEditor(editor);
        }
        setColumnModel(column_model);
    }
    public int getColumnType(int column) {
        ColumnSchema sch =  _TableSchema.get(column);
        return sch.getType();
    }
    //Find column index with specified column name.
    //return -1 if not found
    public int getColumnIndex(String column_name) {
        for (int col=0; col< _TableSchema.size(); col++) {
            ColumnSchema sch =  _TableSchema.get(col);
            if (column_name.equals(sch.getName()))
                return col;
        }
        return -1;
    }
    public void enableRow(boolean flag, int row) {
        if (row >= _lstRows.size())
            return;

        SimpleCell[] cells = _lstRows.get(row);
        for (int col=0; col< _TableSchema.size(); col++)
            cells[col].setEnabled(flag);
        fireTableRowsUpdated(row, row);
    }
    //to get alignment of a column from schema
    public int getAlignment(int col) {
        return (_TableSchema.get(col)).getAlignment();
    }
    public List getSelectedValues() {
        ArrayList ret = new ArrayList();
        if (_nSelectedRows == null || _nSelectedRows.length == 0)
            return ret;

        for (int i=0; i<_nSelectedRows.length; i++)
            ret.add(getCell(_nSelectedRows[i], 0).getValue());
        return ret;
    }
    //is table dirty? any cell dirty will do
    public boolean isDirty() {
        for (int row=0; row<_lstRows.size(); row++) {
            SimpleCell[] cells = _lstRows.get(row);
            for (int col=0; col<_TableSchema.size(); col++) {
                if (cells[col].isDirty())
                    return true;
            }
        }
        return false;
    }
    //pass-thru row related methods
    public int getCurrentRow() {
        return _nCurrentRow;
    }
    public void setCurrentRow(int new_row) {
        _nCurrentRow = new_row;
    }
    public SimpleCell[] getRow(int row) {
        return _lstRows.get(row);
    }
    public int[] getSelectedRows() {
        return _nSelectedRows;
    }
    public void setSelectedRows(int[] rows) {
        if (rows == null || rows.length == 0) {//nothing selected
            _nSelectedRows = null;
            return;
        }
        _nSelectedRows = rows;
    }
    public boolean isFirstSelectedRow(int row) {
        if (_nSelectedRows == null || _nSelectedRows.length == 0)
            return false;

        return (row == _nSelectedRows[0]);
    }
    public boolean isLastSelectedRow(int row) {
        if (_nSelectedRows == null || _nSelectedRows.length == 0)
            return false;

        return (row == _nSelectedRows[_nSelectedRows.length - 1]);
    }
    public void incCurrentRow() {
        _nCurrentRow++;//should never over max
        fireTableRowsUpdated(_nCurrentRow - 1, _nCurrentRow);
    }
    public void decCurrentRow() {
        _nCurrentRow--;//should never below 0
        fireTableRowsUpdated(_nCurrentRow, _nCurrentRow + 1);
    }
    public TableColumnModel getColumnModel() {
        return _ColumnModel;
    }
    public void setColumnModel(TableColumnModel cm) {
        _ColumnModel = cm;
    }
    public List getRows() {
        return _lstRows;
    }
    //a flag for cancelling edits when switch to other context like tabs, windows..etc
    public void setTableInEditing(JTable table) {
        _TableInEditing = table;
    }
    public void cancelTableInEditing() {
        if (_TableInEditing != null && _TableInEditing.getCellEditor() != null)
            _TableInEditing.getCellEditor().cancelCellEditing();
    }
    //generate ranking map _mapRanking for each column using model row ordering(not view)
    //  map key = model column number, value = array of model rows sorted by cell content
    public void computeRanking(int start_column, int end_column, Comparator comparator) {
        for(int col=start_column; col<=end_column; col++) {
            ArrayList<Integer> ranks = new ArrayList<>();//this has index to sorted objects
            ArrayList<Object> cell_values = new ArrayList<>();//the list to be sorted
            for (int row=0; row<_lstRows.size(); row++)
                cell_values.add(_lstRows.get(row)[col].getValue());

            //sort cells based on natural comparator
            Object[] tmps = cell_values.toArray();
            Arrays.sort(tmps, comparator);//ascending order
            try {
                Arrays.sort(tmps, Collections.reverseOrder());//descending order
            } catch (Exception e) {
                e.printStackTrace();  //TODO logger
            }

            //look up index of each sorted cell, create rank array
            for (int row=0; row<tmps.length; row++) {
                //find matching cell in _lstRows
                for (int model_row=0; model_row<_lstRows.size(); model_row++) {
                    if (tmps[row].equals(_lstRows.get(model_row)[col].getValue())) {
                        ranks.add(model_row);
                        break;
                    }
                }
            }
            _mapRanking.put(new Integer(col), ranks);
        }
    }

    //----- accessor / mutators -----
    public List<ColumnSchema> getTableSchema() {
        return _TableSchema;
    }
    public SimpleCell getCell(int row, int column) {
        if (column == -1)
            return null;
        return _lstRows.get(row)[column];
        //return _lstRows.get(row)[getVi(column)];
    }
    public int getColumnWidth(int column) {
        //ColumnSchema sch =  _TableSchema.get(getVi(column));
        ColumnSchema sch =  _TableSchema.get(column);
        return sch.getWidth();
    }
    public HashMap<Integer, ArrayList<Integer>> getRankingMap() { return _mapRanking; }

    //----- variables -----
    protected List<ColumnSchema> _TableSchema; //list of ColumnSchema
    protected ArrayList<SimpleCell[]> _lstRows = new ArrayList<>();//list of SimpleCell array, each element is a row
    protected int[] _nSelectedRows;
    protected int _nCurrentRow = -1;
    protected TableColumnModel _ColumnModel;
    private JTable _TableInEditing;
    //map to track each column's ranking, key=column index, value=list of ordered row indices from high to low
    private HashMap<Integer, ArrayList<Integer>> _mapRanking = new HashMap<>();
}