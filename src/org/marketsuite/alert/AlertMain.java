package org.marketsuite.alert;

import org.marketsuite.component.UI.CapLookAndFeel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//main entry point
public class AlertMain {
    public static void main(String[] args) {
        try {
            SyntheticaLookAndFeel.setWindowsDecorated(false);
            //this is necessary to allow silver theme to show up if windows desktop already has silver theme
            UIManager.setLookAndFeel(new CapLookAndFeel());
            UIManager.put("TabbedPane.tabsOpaque", Boolean.TRUE);
            //NOTE: this will remove title bar and thus not moveable or resize
//            JFrame.setDefaultLookAndFeelDecorated(true);//use swing components to build this frame
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        JFrame frame = new JFrame("Alert Manager");
        frame.setContentPane(new AlertPanel());

        //TODO read from preference to populate cells

        ToolTipManager.sharedInstance().setInitialDelay(2);
        ToolTipManager.sharedInstance().setDismissDelay(30000);
        WidgetUtil.setFrameProperties(frame, new Dimension(550, 350), true, null, WindowConstants.DISPOSE_ON_CLOSE);
    }

    //----- inner class -----
    private static class AlertPanel extends JPanel {
        private AlertPanel() {
            setLayout(new MigLayout("insets 0"));
            setBorder(new BevelBorder(BevelBorder.LOWERED));

            //title bar
            JPanel ttl_pnl = new JPanel(new MigLayout("insets 0", "5[]5[]push[]30[]5", "3[]3"));
            ttl_pnl.add(_btnAdd); _btnAdd.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { _tmAlert.addRow(); }
            });
            ttl_pnl.add(_btnDelete); _btnDelete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int sel = _tblAlert.getSelectedRow(); if (sel < 0) return;
                    _tmAlert.deleteRow(sel);
                }
            });
            ttl_pnl.add(_spnInterval);
            ttl_pnl.add(_btnRun); _btnRun.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //start timer to poll yahoo quotes
                    if (_bPolling) {
                        _btnRun.setIcon(FrameworkIcon.RUN);
                        _bPolling = false;
                        _tmrPollQuote.stop();
                        _tmAlert.decorate();
                    }
                    else {
                        _btnRun.setIcon(FrameworkIcon.PAUSE_MOVIE);
                        _bPolling = true;
                        _tmAlert.clearAlerts();
                        _tmAlert.decorate();
                        _tmrPollQuote.start();
                    }
                }
            });
            add(ttl_pnl, "dock north");

            //center - table
            _tmAlert = new AlertTableModel();
            _tblAlert = WidgetUtil.createDynaTable(_tmAlert, ListSelectionModel.SINGLE_INTERVAL_SELECTION,
                new HeadingRenderer(), false, new AlertRenderer(_tmAlert));
            _tblAlert.setRowHeight(20);
            add(new JScrollPane(_tblAlert), "dock center");
            _tmrPollQuote = new Timer(_spnInterval.getValue() * 1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //get yahoo quotes, check if any trigger occurs
                    _bPolling = true; int active_count = _tmAlert.getRowCount();
                    for (int row = 0; row < _tmAlert.getRowCount(); row++) {
                        String sym = (String)_tmAlert.getCell(row, COLUMN_SYMBOL).getValue();
                        double lvl1 = (Double)_tmAlert.getCell(row, COLUMN_STOP_LEVEL).getValue();
                        double lvl2 = (Double)_tmAlert.getCell(row, COLUMN_LIMIT_LEVEL).getValue();
                        if (sym.equals("") || (lvl1 == 0 && lvl2 == 0)) {//skip no symbol, no level set
                            active_count--;
                            continue;
                        }
                        AlertEntity ae = (AlertEntity)_tmAlert.getCell(row, COLUMN_STATUS).getValue();
                        AlertStatus stat = ae.getStatus();
                        if (!stat.equals(AlertStatus.NotTriggered)) continue;//only poll ones with this status
                        float price;
                        System.err.println("===> Polling....." + sym);
                        try {
                            FundQuote quote = DataUtil.getYahooQuote(sym);
                            price = quote.getClose();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            _tmAlert.getCell(row, COLUMN_STATUS).setValue(AlertStatus.Inactive);//fail to get quote, disable
                            continue;
                        }
                        if (_tmAlert.isStopTriggered(row, price)) {
                            ae.setStopTriggerTime(Calendar.getInstance());
                            ae.setStatus(AlertStatus.StopTriggered);
                            _tmAlert.fireTableRowsUpdated(row, row);
                            active_count--;
                        }
                        else if (_tmAlert.isLimitTriggered(row, price)) {
                            ae.setLimitTriggerTime(Calendar.getInstance());
                            ae.setStatus(AlertStatus.LimitTriggered);
                            _tmAlert.fireTableRowsUpdated(row, row);
                            active_count--;
                        }
                        if (active_count == 0) {
                            _btnRun.setIcon(FrameworkIcon.RUN);
                            _bPolling = false;
                            _tmrPollQuote.stop();
                            break;
                        }
                    }
                }
            });
        }

        //----- instance variable -----
        private JButton _btnAdd = WidgetUtil.createIconButton("Add Symbol Row", LazyIcon.PLUS_SIGN);
        private JButton _btnDelete = WidgetUtil.createIconButton("Delete Row", LazyIcon.MINUS_SIGN);
        private IntegerSpinner _spnInterval = new IntegerSpinner("Interval", 5, 3, 20, 1, "sec", null);
        private JButton _btnRun = WidgetUtil.createIconButton("Start Polling", FrameworkIcon.RUN);
        private JTable _tblAlert;
        private AlertTableModel _tmAlert;
        private Timer _tmrPollQuote;
    }
    private static class AlertTableModel extends DynaTableModel {
        private AlertTableModel() { super(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public void populate() {
            _lstRows.clear();
            initCells();

            fireTableDataChanged();
        }
        public boolean isCellEditable(int row, int column) { return getCell(row, column).isHighlight(); }
        public void setValueAt(Object value, int row, int column) {
            getCell(row, COLUMN_STATUS).setValue(new AlertEntity());
            switch (column) {
                case COLUMN_SYMBOL:
                    getCell(row, column).setHighlight(false);//disallow further change
                    String uv = ((String) value).toUpperCase();
                    super.setValueAt(uv, row, column);
                    return;

                case COLUMN_STOP_LEVEL:
                    getCell(row, COLUMN_LIMIT_LEVEL).setHighlight(false);
                    break;
                case COLUMN_LIMIT_LEVEL://once set, no change
                    getCell(row, COLUMN_STOP_LEVEL).setHighlight(false);
                    break;
            }
            super.setValueAt(value, row, column);
        }
        private SimpleCell[] initCells() {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            for (int col=0; col<TABLE_SCHEMA.length; col++) {
                switch (col) {
                    case COLUMN_SYMBOL:
                        cells[col] = new SimpleCell("");
                        cells[col].setHighlight(true);
                        break;

                    case COLUMN_STATUS:
                    default:
                        AlertEntity ae = new AlertEntity();
                        cells[col] = new SimpleCell(ae);
                        break;

                    case COLUMN_STOP_LEVEL:
                    case COLUMN_LIMIT_LEVEL:
                        cells[col] = new SimpleCell(new Double(0));
                        cells[col].setHighlight(true);
                        break;
                }
            }
            return cells;
        }
        private void addRow() {
            SimpleCell[] cells = initCells();
            _lstRows.add(cells);
            int row = getRowCount();
            fireTableRowsInserted(row, row);
        }
        private void deleteRow(int row_index) {
            _lstRows.remove(row_index);
            fireTableDataChanged();
        }
        private boolean isStopTriggered(int row, float quote) {
            double stop = (Double)getCell(row, COLUMN_STOP_LEVEL).getValue();
            return quote <= stop && stop > 0;
        }
        private boolean isLimitTriggered(int row, float quote) {
            double limit = (Double)getCell(row, COLUMN_LIMIT_LEVEL).getValue();
            return quote >= limit && limit > 0;
        }
        private void clearAlerts() {
            for (int row = 0; row < getRowCount(); row++) {
                AlertEntity ae = new AlertEntity(); ae.setStatus(AlertStatus.NotTriggered);
                getCell(row, COLUMN_STATUS).setValue(ae);
            }
            fireTableDataChanged();
        }
        private void decorate() {
            for (int row = 0; row < getRowCount(); row++) {
                getCell(row, COLUMN_SYMBOL).setHighlight(!_bPolling);
                getCell(row, COLUMN_STOP_LEVEL).setHighlight(!_bPolling);
                getCell(row, COLUMN_LIMIT_LEVEL).setHighlight(!_bPolling);
            }
        }
    }
    private static class AlertRenderer extends DynaTableCellRenderer {
        private AlertRenderer(DynaTableModel model) { super(model); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel lbl = (JLabel)comp;
            String symbol = (String)_Model.getCell(row, COLUMN_SYMBOL).getValue();
            AlertEntity ae = (AlertEntity)_Model.getCell(row, COLUMN_STATUS).getValue();
            AlertStatus stat = ae.getStatus();
            switch (column) {
                case COLUMN_SYMBOL:
                    if (symbol.equals("")) break;
                    lbl.setFont(FrameworkConstants.MEDIUM_FONT);
                    if (_bPolling && stat.equals(AlertStatus.NotTriggered))
                        lbl.setBackground(Color.yellow);//indicating ARMed
                    break;

                case COLUMN_STOP_LEVEL:
                    double lvl = (Double)value;
                    if (lvl == 0) {
                        lbl.setText(""); break;//don't show 0
                    }
                    if (stat.equals(AlertStatus.StopTriggered)) {
                        lbl.setBackground(new Color(250, 41, 23, 194)); lbl.setForeground(Color.white);
                        lbl.setFont(FrameworkConstants.MEDIUM_FONT);
                    }
                    break;

                case COLUMN_LIMIT_LEVEL:
                    lvl = (Double)value;
                    if (lvl == 0) {
                        lbl.setText(""); break;//don't show 0
                    }
                    if (stat.equals(AlertStatus.LimitTriggered)) {
                        lbl.setBackground(new Color(98, 250, 16, 194));
                        lbl.setFont(FrameworkConstants.MEDIUM_FONT);
                    }
                    break;

                case COLUMN_STATUS:
                    if (stat.equals(AlertStatus.StopTriggered))
                        lbl.setText(stat.toString() + " at " + FMT.format(ae.getStopTriggerTime().getTime()));
                    else if (stat.equals(AlertStatus.LimitTriggered))
                        lbl.setText(stat.toString() + " at " + FMT.format(ae.getLimitTriggerTime().getTime()));
                    else
                        lbl.setText("");
                    break;
            }
            return comp;
        }
    }

    //----- variables -----
    private static boolean _bPolling;

    //----- literals -----
    private static final SimpleDateFormat FMT = new SimpleDateFormat("HH:mm:ss");
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_STOP_LEVEL = 1;
    private static final int COLUMN_LIMIT_LEVEL = 2;
    private static final int COLUMN_STATUS = 3;
    private static final Object[][] TABLE_SCHEMA = {
        {"Symbol",        ColumnTypeEnum.TYPE_STRING, -1,  20,  new NameCellEditor(), null, null},//symbol
        {"Stop Level",    ColumnTypeEnum.TYPE_DOUBLE, -1,  20,  new DecimalCellEditor(0, 4, 0, 2000, null), null, null},//low
        {"Limit Level",   ColumnTypeEnum.TYPE_DOUBLE, -1,  20,  new DecimalCellEditor(0, 4, 0, 2000, null), null, null},//high
        {"Status",        ColumnTypeEnum.TYPE_STRING,  0,  300, null, null, null},//status
    };
}