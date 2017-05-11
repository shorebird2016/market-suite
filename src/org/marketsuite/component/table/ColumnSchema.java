package org.marketsuite.component.table;

import java.util.Comparator;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * A data class representing a table column with some viewing attributes.
 */
public class ColumnSchema {
    //CTOR 1
    public ColumnSchema(String name, int type, boolean visible, int width, TableCellEditor editor) {
        _sName = name;
        _nType = type;
        _ColumnClass = ColumnTypeEnum.getClassForType(type);
        _bVisible = visible;
        _nWidth = width;
        _Editor = editor;
    }
    //CTOR 2
    public ColumnSchema(
           String name, int type, boolean visible, boolean hideable, int alignment, int width,
           TableCellEditor editor, TableCellRenderer renderer, Comparator comparator) {
        _sName = name;
        _nType = type;
        _ColumnClass = ColumnTypeEnum.getClassForType(type);
        _bVisible = visible;
        _nAlignment = alignment;
        _nWidth = width;
        _Editor = editor;
        _Renderer = renderer;
        _Comparator = comparator;
    }
    //CTOR 3
    public ColumnSchema(
           String name, int type, int alignment, int width,
           TableCellEditor editor, TableCellRenderer renderer, Comparator comparator) {
        _sName = name;
        _nType = type;
        _ColumnClass = ColumnTypeEnum.getClassForType(type);
        _nAlignment = alignment;
        _nWidth = width;
        _Editor = editor;
        _Renderer = renderer;
        _Comparator = comparator;
        _bVisible = true;//must have this to show, todo: phase out this attribute later......
    }
    //CTOR 4 to avoid passing null editor/renderer/comparator
    public ColumnSchema( String name, int type, int alignment, int width) {
        _sName = name;
        _nType = type;
        _ColumnClass = ColumnTypeEnum.getClassForType(type);
        _nAlignment = alignment;
        _nWidth = width;
        _bVisible = true;//must have this to show, todo: phase out this attribute later......
    }

    //accessor / mutators
    public String getName() {
        return _sName;
    }

    public int getType() {
        return _nType;
    }

    public void setType(int t) {
        _nType = t;
    }

    public boolean isVisible() {
        return _bVisible;
    }

    public void setVisible(boolean visible) {
        _bVisible = visible;
    }

    public int getAlignment() {
        return _nAlignment;
    }

    public int getWidth() {
        return _nWidth;
    }

    public Class getColumnClass() {
        return _ColumnClass;
    }

    public TableCellEditor get_Editor() {
        return _Editor;
    }

    public void setEditor(TableCellEditor editor) {
        _Editor = editor;
    }

    public TableCellRenderer getRenderer() {
        return _Renderer;
    }

    public void setRenderer(TableCellRenderer renderer) {
        this._Renderer = renderer;
    }

    public Comparator getComparator() {
        return _Comparator;
    }

    public void setComparator(Comparator comparator) {
        this._Comparator = comparator;
    }

    //instance variables
    private String _sName;
    private int _nType;
    private boolean _bVisible;
    private int _nAlignment; //-1 = center align, 1..n = leading spaces
    private int _nWidth;
    private Class _ColumnClass;//can't use reserve word
    private TableCellEditor _Editor;
    private TableCellRenderer _Renderer;
    private Comparator _Comparator;
}
