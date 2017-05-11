package org.marketsuite.component.table;

import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import org.marketsuite.component.Constants;

//To render all cells in profile table with hint from column classes.
public class DynaTableCellRenderer implements TableCellRenderer {
    //CTOR:model contains leading spaces
    public DynaTableCellRenderer(DynaTableModel model) {
        _Model = model;
    }

    public DynaTableCellRenderer(DynaTableModel model, RowSorter sorter) {
        _Model = model;
        _Sorter = sorter;
    }

    //CTOR: specify whether to use interlaced color background or not
    public DynaTableCellRenderer(DynaTableModel model, boolean interlace_bkgnd) {
        this(model);
        _bInterlaceBkgnd = interlace_bkgnd;
    }

    //Method defining the renderer to be used when drawing the cells.
    public Component getTableCellRendererComponent(JTable table, Object value,
           boolean isSelected, boolean hasFocus, int row, int column) {
        _Model = (DynaTableModel) table.getModel();
        _Label.setIcon(null);
        _Label.setText("");
        column = table.convertColumnIndexToModel(column);
        SimpleCell cell;
        Class column_class;
        cell = _Model.getCell(table.convertRowIndexToModel(row), column);
        column_class = _Model.getColumnClass(column);

        //boolean class
        if (column_class.equals(Boolean.class)) {
            _Checkbox.setHorizontalAlignment(getCellAlignment(column));//SwingConstants.CENTER);
            _Checkbox.setSelected(((Boolean) value).booleanValue());
            if (isSelected)
                setSelectionBackground(table, row, _Checkbox);
            else {
                if (cell.isHighlight())
                    _Checkbox.setBackground(Constants.CELL_HIGHLIGHT_BACKGROUND);
                else
                    setNoSelectionBackground(row, _Checkbox);
            }
            if (!table.isEnabled() || !cell.isEnabled())
                _Checkbox.setBackground(Constants.CELL_DISABLED_BACKGROUND);
            else if (!cell.isShowContent())
                _Checkbox.setBackground(Constants.CELL_DISABLED_BACKGROUND);
            return _Checkbox;
        }

        //date class
        else if (column_class.equals(Date.class)) {
            String txt = "";
            //format txt using supplied date format or default if not supplied
            if (_DateFormat == null)
                txt = Constants.BASCI_DATE_FORMAT.format((Date)value);//todo: watch for value to be null.............it shouldn't be in theory.......
            else
                txt = _DateFormat.format((Date)value);
            _Label.setOpaque(true);//must have this, default is transparent
            _Label.setHorizontalAlignment(getCellAlignment(column));
            _Label.setForeground(Color.black);
            _Label.setFont(Constants.CELL_FONT);
            //change background color
            if (isSelected)
                setSelectionBackground(table, row, _Label);
            else {//when not selected, then highlight
                if (cell.isHighlight())
                    _Label.setBackground(Constants.CELL_HIGHLIGHT_BACKGROUND);
                else
                    setNoSelectionBackground(row, _Label);
            }
            //test table enablement
            if (!table.isEnabled())
                _Label.setBackground(Constants.CELL_DISABLED_BACKGROUND);
            //test cell enablement
            if (!cell.isEnabled()) {
                _Label.setBackground(Constants.CELL_DISABLED_BACKGROUND);
                _Label.setText(padLeadingSpaces(column, false) + txt);
            }
            else if (!cell.isShowContent()) {
                _Label.setBackground(Constants.CELL_DISABLED_BACKGROUND);
                _Label.setText(padLeadingSpaces(column, false) + txt);
            }
            else
                _Label.setText(padLeadingSpaces(column, false) + txt);
            return _Label;
        }
        //long class
        else if (column_class.equals(Long.class) || column_class.equals(Double.class)) {
            _Label.setOpaque(true);//must have this, default is transparent
            _Label.setHorizontalAlignment(getCellAlignment(column));//SwingConstants.CENTER);
            _Label.setForeground(Color.black);
            _Label.setFont(Constants.CELL_FONT);
            if (isSelected)
                setSelectionBackground(table, row, _Label);
            else {//when not selected, then highlight
                if (cell.isHighlight())
                    _Label.setBackground(Constants.CELL_HIGHLIGHT_BACKGROUND);
                else
                    setNoSelectionBackground(row, _Label);
            }
            if (!table.isEnabled() || !cell.isEnabled())
                _Label.setBackground(Constants.CELL_DISABLED_BACKGROUND);
            else if (!cell.isShowContent()) {
                _Label.setBackground(Constants.CELL_DISABLED_BACKGROUND);
                _Label.setText(padLeadingSpaces(column, false) + cell.toString());
            }
            else {
                if (cell.toString().length() > 0) {
                    if (column_class.equals(Long.class))
                        _Label.setText(padLeadingSpaces(column, false) + String.valueOf(Long.parseLong(cell.toString())));
                    else if (column_class.equals(Double.class)) {
                        double dbv = Double.parseDouble(cell.toString());
                        _Label.setText(padLeadingSpaces(column, false) + _decimalFormat.format(dbv));
                    } else {//big decimal
                        BigDecimal dbv = (BigDecimal)cell.getValue();
                        _Label.setText(padLeadingSpaces(column, false) + _decimalFormat.format(dbv));
                    }
                }
//                else
//                    _Label.setText(padLeadingSpaces(column, false));
            }
            return _Label;
        }
        //String and other types, will use the object toString() method to get the label text
        else {
            _Label.setOpaque(true);//must have this, default is transparent
            _Label.setHorizontalAlignment(getCellAlignment(column));
            _Label.setForeground(Color.black);
            _Label.setFont(Constants.CELL_FONT);
            //change background color
            if (isSelected)
                setSelectionBackground(table, row, _Label);
            else {//when not selected, then highlight
                if (cell.isHighlight())
                    _Label.setBackground(Constants.CELL_HIGHLIGHT_BACKGROUND);
                else
                    setNoSelectionBackground(row, _Label);
            }
            //test table enablement
            if (!table.isEnabled() || !cell.isEnabled())
                _Label.setBackground(Constants.CELL_DISABLED_BACKGROUND);
            else if (!cell.isShowContent())
                _Label.setBackground(Constants.CELL_DISABLED_BACKGROUND);
            else if (cell.isUnderline()) {
                String txt = "<html><u>" + padLeadingSpaces(column, true) + cell.getValue();
                _Label.setText(txt);
                _Label.setForeground(Color.blue);
            }
            else
                _Label.setText(padLeadingSpaces(column, false) + cell.getValue());
            return _Label;
        }
    }

    // set decimal precision for customizing sub classes
    public void setDecimalFormat(DecimalFormat fmt) {
        _decimalFormat = fmt;
    }

    //to determine cell alignment from model
    protected int getCellAlignment(int col) {
        if (-1 == _Model.getAlignment(col))
            return SwingConstants.CENTER;
        return SwingConstants.LEFT;
    }

    //to pad leading spaces for a column
    protected String padLeadingSpaces(int col, boolean html) {
        StringBuffer ret = new StringBuffer();
        int aln = _Model.getAlignment(col);
        for (int i = 0; i < aln; i++)//if -1, won't pad
            if (html)
                ret.append("&nbsp;");
            else
                ret.append(" ");
        return ret.toString();
    }

    //alternate color light background for ease of identification
    protected void setNoSelectionBackground(int row, JComponent ret1) {
        if (!_bInterlaceBkgnd) {
            ret1.setBackground(Constants.CELL_EVEN_UNSELECTED_BACKGROUND);
            return;
        }

        if (row % 2 == 0)
            ret1.setBackground(Constants.CELL_EVEN_UNSELECTED_BACKGROUND);
        else
            ret1.setBackground(Constants.CELL_ODD_UNSELECTED_BACKGROUND);
    }

    protected void setSelectionBackground(JTable table, int row, JComponent ret1) {
        int[] sel = table.getSelectedRows();
        if (sel.length == 0)
            return;

        if (sel.length > 1) {//multi-selection
            if (_Model.getCurrentRow() == row)
                ret1.setBackground(Constants.SINGLE_SELECTION_BACKGROUND);
            else
                ret1.setBackground(Constants.MULTI_SELECTION_BACKGROUND);
        }
        else //single selection
            ret1.setBackground(Constants.SINGLE_SELECTION_BACKGROUND);
    }

    public void setIcon(Icon icon) {
        _Label.setIcon(icon);
    }

    public void setText(String text) {
        _Label.setText(text);
    }

    //instance variables
    private boolean _bInterlaceBkgnd = true;
    protected DynaTableModel _Model;
    protected RowSorter _Sorter;
    private JCheckBox _Checkbox = new JCheckBox();
    private JLabel _Label = new JLabel();
    private DecimalFormat _decimalFormat = new DecimalFormat("###.###");
    private SimpleDateFormat _DateFormat;
}