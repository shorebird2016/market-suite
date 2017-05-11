package org.marketsuite.component.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;

/**
 * The dialog to provide user with the ability to select a subset of columns in JTable for viewing
 */
public class SchemaColumnDialog extends JDialog {
    public SchemaColumnDialog(JTable _table, String[] columnNames, Frame frame, Image image, int lockedColumns) {
        super(frame, Constants.COMPONENT_BUNDLE.getString("info_dlg_1"), true);
        _Table = _table;
        setIconImage(image);

        //content pane
        JPanel content_pnl = (JPanel)getContentPane();
        content_pnl.setLayout(new BorderLayout());

        final ActionListener updateListener = new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               updateTable();
           }
       };
        //center - vertical list of checkboxes, one for each column
        Box center_pnl = Box.createVerticalBox();
        final int size = columnNames.length;
        _chkColumns = new JCheckBox[size];
        for (int i = 0; i < size; i++) {
            _chkColumns[i] = new JCheckBox(columnNames[i]);
            if (i >= lockedColumns) //skip locked columns
                center_pnl.add(_chkColumns[i]);
            _chkColumns[i].addActionListener(updateListener);
        }
        JScrollPane jsp = new JScrollPane(center_pnl);
        jsp.getVerticalScrollBar().setUnitIncrement(_chkColumns[0].getPreferredSize().height);
        content_pnl.add(jsp, BorderLayout.CENTER);

        //south - buttons to select all or none
        SkinPanel south_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER);
        south_pnl.add(_btnSelectAll = new JButton(Constants.COMPONENT_BUNDLE.getString("select_all")));
        _btnSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               setAllVisible(true);
               updateTable();
            }
        });
        _btnSelectAll.setToolTipText(Constants.COMPONENT_BUNDLE.getString("tip_select_all"));
        south_pnl.add(_btnClearAll = new JButton(Constants.COMPONENT_BUNDLE.getString("clear_all")));
        _btnClearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAllVisible(false);
                updateTable();
            }
        });
        _btnClearAll.setToolTipText(Constants.COMPONENT_BUNDLE.getString("tip_clear_all"));
        south_pnl.add(_btnReset= new JButton(Constants.COMPONENT_BUNDLE.getString("default")));
        _btnReset.setToolTipText(Constants.COMPONENT_BUNDLE.getString("tip_default"));
        _btnReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               setAllVisible(true);
               updateTable();
               TableUtil.resetOrder(_Table);
            }
        });
        content_pnl.add(south_pnl, BorderLayout.SOUTH);
        Dimension dim = new Dimension(250, 450);//set dimension first to allow centering
        WidgetUtil.setDialogProperties(this, dim, false, frame, WindowConstants.DISPOSE_ON_CLOSE, false);
    }

    public void setVisibleColumns(boolean[] b) {
        if (b == null || b.length == 0) {//no preference, check all
            setAllVisible(true);
            _btnSelectAll.setSelected(true);
            return;
        }


        if(b.length != _chkColumns.length) { // newer version of software may have more or less columns than before
           boolean[] b2 = new boolean[_chkColumns.length];
           int i=0 ;
           int len = Math.min(b.length,_chkColumns.length);
           for(;i < len ;i++) b2[i] = b[i];  // copy old values into new array
           for(;i < _chkColumns.length;i++) b2[i] = true;  // if any left over, set them all to 'true'
           b = b2;
        }
        
        //set checkbox state
        _bVisible = b;
        for (int i = 0; i < _bVisible.length; i++) {
            if (i < _chkColumns.length)
                _chkColumns[i].setSelected(_bVisible[i]);
        }
    }

    public boolean[] getResult() {
        if (!_bChanged)
            return null;
        return _bVisible;
    }

    //visually change table in the backgroud to new set of columns
    private void updateTable() {
        if (_Table == null)
            return;

        if (_bVisible == null || _bVisible.length == 0)
            _bVisible = new boolean[_chkColumns.length];

        for (int i = 0; i < _bVisible.length; i++) {
            boolean val = _chkColumns[i].isSelected();
            if (val != _bVisible[i]) {
                _bVisible[i] = val;
                _bChanged = true;
            }
        }
        TableUtil.setColumnsVisible(_Table, _bVisible);
    }

    private void setAllVisible(boolean visible) {
        for (JCheckBox check_box : _chkColumns)
            check_box.setSelected(visible);
    }

    //instance variables
    private JTable _Table;
    private JButton _btnSelectAll;
    private JButton _btnClearAll;
    private JButton _btnReset;
    private JCheckBox[] _chkColumns;
    private boolean _bChanged;
    private boolean[] _bVisible;
}