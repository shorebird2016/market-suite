package org.marketsuite.main;

import org.marketsuite.component.table.*;
import org.marketsuite.component.util.MenuPopupListener;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A container for managing watch lists.
 */
public class LoggingFrame extends JInternalFrame implements PropertyChangeListener {
    private static LoggingFrame _Instance;
    public static LoggingFrame getInstance() {
        if (_Instance == null)
            _Instance = new LoggingFrame();
        return _Instance;
    }
    private LoggingFrame() {
        super(ApolloConstants.APOLLO_BUNDLE.getString("lf_01"), true, false, false, true);
        setFrameIcon(ApolloIcon.APP_ICON);
        JPanel content = new JPanel(new MigLayout("insets 0"));

        //center - table with messages
        _tmLogging = new LoggingTableModel();
        _tblLogging = new JTable(_tmLogging);
        WidgetUtil.initDynaTable(_tblLogging, _tmLogging, ListSelectionModel.SINGLE_SELECTION,
                new SortHeaderRenderer(), true, new LoggingRenderer());
        _tblLogging.setOpaque(false);
        _tblLogging.setAutoCreateRowSorter(true);
        _tblLogging.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _Sorter = _tblLogging.getRowSorter();
        _tblLogging.addMouseListener(new MenuPopupListener() {
            public void showMenu(MouseEvent ev) {
                int row = _tblLogging.rowAtPoint(ev.getPoint());//force selection
                _tblLogging.getSelectionModel().setSelectionInterval(row, row);
                _mnuPopup.show(_tblLogging, ev.getX(), ev.getY());
            }
        });
        content.add(new JScrollPane(_tblLogging), "dock center");
        setContentPane(content);
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_LOGGER, MdiMainFrame.LOCATION_LOGGER, MdiMainFrame.SIZE_LOGGER);
        Props.addWeakPropertyChangeListener(Props.Log, this);//handle symbol change

        _mnuPopup = new JPopupMenu();
        _mnuPopup.add(_miClear);
        _miClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tmLogging.clear();
            }
        });
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case Log://array of log messages
                ArrayList<LogMessage> msgs = (ArrayList<LogMessage>)prop.getValue();
                _tmLogging.populate(msgs);
                break;

        }
    }

    //----- inner classes -----
    private class LoggingTableModel extends DynaTableModel {
        private LoggingTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public void populate() {}
        public boolean isCellEditable(int row, int col) { return false; }
        private void populate(ArrayList<LogMessage> msgs) {
            for (LogMessage msg : msgs) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_SOURCE] = new SimpleCell(msg.getSource());
                cells[COLUMN_MESSAGE] = new SimpleCell(msg.getMessage());
                cells[COLUMN_EXCEPTION] = new SimpleCell(msg.getException());//stores Exception object
                cells[COLUMN_TIME] = new SimpleCell(msg.getTime());//stores Calendar
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
    }
    private class LoggingRenderer extends DynaTableCellRenderer {
        private LoggingRenderer() { super(_tmLogging); }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = _tblLogging.convertRowIndexToModel(row);
            column = _tblLogging.convertColumnIndexToModel(column);
            JLabel lbl = (JLabel)comp;
            switch (column) {
                case COLUMN_SOURCE:
                    LoggingSource src = (LoggingSource)value;
                    lbl.setText(src.toString());
                    break;

                case COLUMN_EXCEPTION:
                    Exception exc = (Exception)value;
                    if (exc != null) lbl.setText(exc.getMessage());
//TODO: a big tooltip showing stack trace
                    else lbl.setText("");
                    break;

                case COLUMN_TIME:
                    Calendar cal = (Calendar)value;
                    SimpleDateFormat fmt = new SimpleDateFormat("hh : mm : ss a");
                    lbl.setText(fmt.format(cal.getTime()));
                    break;
            }
            return comp;
        }
    }

    //----- variables -----
    private JTable _tblLogging;
    private LoggingTableModel _tmLogging;
    private RowSorter _Sorter;
    private JPopupMenu _mnuPopup;
    private JMenuItem _miClear = new JMenuItem(ApolloConstants.APOLLO_BUNDLE.getString("lf_06"));
//    private JButton _btnAddGroup = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_3"), LazyIcon.PLUS_SIGN);
//    private JButton _btnDeleteWatchList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_4"), LazyIcon.MINUS_SIGN);
//    private JButton _btnRename = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_13"), FrameworkIcon.FILE_SAVE_AS);
//    private JButton _btnImport = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_9"), FrameworkIcon.IMPORT);
//    private JButton _btnExport = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_14"), FrameworkIcon.EXPORT);
//    private JButton _btnLaunch = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_5"), ApolloIcon.LAUNCH);
//    private JButton _btnDuplicate = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_6"), FrameworkIcon.DUPLICATE);
//    private JButton _btnMergeWatchList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_17"), FrameworkIcon.MERGE);
//    private JButton _btnAddSymbol = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_7"), LazyIcon.PLUS_SIGN);
//    private JButton _btnDeleteSymbol = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_8"), LazyIcon.MINUS_SIGN);
//    private JButton _btnViewEditSymbols = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_10"), FrameworkIcon.VIEW_EDIT);
//    private JButton _btnDownload = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lnk_2"), FrameworkIcon.DOWNLOAD);
//    private JButton _btnDump = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_19"), FrameworkIcon.DUMPER);
//    private String _sCurrentGroup;
//    private ListMgrPanel _pnlListMgr;
//    private boolean _bStartup = true;//true = first time application start

    //----- literals -----
    private static final int COLUMN_SOURCE = 0;
    private static final int COLUMN_MESSAGE = 1;
    private static final int COLUMN_EXCEPTION = 2;
    private static final int COLUMN_TIME = 3;
    private static final Object[][] TABLE_SCHEMA = {
            {ApolloConstants.APOLLO_BUNDLE.getString("lf_02"), ColumnTypeEnum.TYPE_STRING,  2, 150, null, null, null},//source
            {ApolloConstants.APOLLO_BUNDLE.getString("lf_03"), ColumnTypeEnum.TYPE_STRING,  0, 600, null, null, null},//message
            {ApolloConstants.APOLLO_BUNDLE.getString("lf_04"), ColumnTypeEnum.TYPE_STRING,  0, 200, null, null, null},//exception
            {ApolloConstants.APOLLO_BUNDLE.getString("lf_05"), ColumnTypeEnum.TYPE_STRING,  0, 100, null, null, null},//time
    };
    private static final int DEFAULT_POSITION = 300;
}