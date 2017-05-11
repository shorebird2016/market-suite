package org.marketsuite.component.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.resource.LazyIcon;

/**
 * A widget to allow show/hide table columns.
 */
public class ColumnSelector extends JLabel {
    public ColumnSelector(final JFrame parent, final JTable table, final Object[][] schema, final int locked_columns, final Method method) {
        setIcon(new JLabel(LazyIcon.TABLE_COLUMN_OP).getIcon());
        _tblSource = table;
        _sColumnNames = new String[schema.length];
        for (int idx = 0; idx < _sColumnNames.length; idx++)
            _sColumnNames[idx] = (String)schema[idx][0];

        //when user clicks icon, pop up dialog
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                _dlgSchema = new SchemaColumnDialog(_tblSource, _sColumnNames, parent,
                    LazyIcon.APP_ICON.getImage(), locked_columns);
//                _dlgSchema.setVisibleColumns(Prefs.getPrefs().getGigEOutputMuxVisible());//todo: add pref support later......
                _dlgSchema.setVisible(true);
                boolean[] visible_columns = _dlgSchema.getResult();
                if (null != visible_columns) { //todo: handle storing preferences..................
                    try {
                        method.invoke(visible_columns);
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    }
//                    Prefs.getPrefs().setGigEOutputMuxVisible(visible_columns);
//                    Prefs.savePrefs();
                    TableUtil.setColumnsVisible(_tblSource, visible_columns);//update table behind
                }
            }
        });
//        WidgetUtil.attachToolTip(this, Resource.DIALOG_BUNDLE.getString("tcs_tip_1"), //todo: add tooltip support later....
//            SwingConstants.RIGHT, SwingConstants.TOP);
    }

    /**
     * Obtain a list of visible columns after user selection
     */
    public boolean[] getVisibleColumns() {
        return _dlgSchema.getResult();
    }

    //-----instance variables-----
    private SchemaColumnDialog _dlgSchema;
    private JTable _tblSource;
    private String[] _sColumnNames;
}
/**

 //a method to be passed into column selector to save preferences after user actions
 public void updateColumnPref(boolean[] visible_columns) {
     Prefs.getPrefs().setGroomMuxColumnVisible(visible_columns);
     Prefs.savePrefs();
 }
 */