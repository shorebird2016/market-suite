package org.marketsuite.watchlist.fundamental;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.riskmanager.RiskMgrModel;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

//Relative performance of various time periods. eg. 2weeks, 3 months..etc
public class FundamentalPanel extends JPanel /*implements PropertyChangeListener*/ {
    public FundamentalPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        //north - title strip with button to the right
        JPanel east_pnl = new JPanel();  east_pnl.setOpaque(false);
        east_pnl.add(_btnShowHideColumn);
        _btnShowHideColumn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] col_names = new String[FundamentalTableModel.TABLE_SCHEMA.length];
                for (int col = 0; col < FundamentalTableModel.TABLE_SCHEMA.length; col++)
                    col_names[col] = (String) FundamentalTableModel.TABLE_SCHEMA[col][0];
                SchemaColumnDialog dlg = new SchemaColumnDialog(
                        _tblFundamental, col_names,
                        RiskMgrModel.getInstance().getParent(),
                        LazyIcon.APP_ICON.getImage(), LOCKED_COLUMNS);
                dlg.setVisibleColumns(ApolloPreferenceStore.getPreferences().getFundamentalColumnVisible());
                dlg.setVisible(true);
                boolean[] cols = dlg.getResult();
                if (null != cols) {
                    ApolloPreferenceStore.getPreferences().setFundamentalColumnVisible(cols);
                    ApolloPreferenceStore.getPreferences().savePreferences();
                    TableUtil.setColumnsVisible(_tblFundamental, cols);
                }
            }
        });
        add(WidgetUtil.createTitleStrip(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_68")), null, east_pnl),
            BorderLayout.NORTH);

        //center - result table and price chart
        _tblFundamental = WidgetUtil.createDynaTable(_TableModel = new FundamentalTableModel(),
            ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new FundamentalRenderer());
        _tblFundamental.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblFundamental.setAutoCreateRowSorter(true); _Sorter = _tblFundamental.getRowSorter();
        _tblFundamental.setOpaque(false);
        JScrollPane scr = new JScrollPane(_tblFundamental); scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
        TableUtil.fixColumns(scr, _tblFundamental, LOCKED_COLUMNS);

        //add support for column dragging / hiding / ordering
        _tblFundamental.getColumnModel().addColumnModelListener(new TableUtil.TableColumnModelAdapter() {
            public void columnMoved(TableColumnModelEvent ev) {
                if (TableUtil.columnMoved(_tblFundamental, ev.getFromIndex(), ev.getToIndex())) {
                    ApolloPreferenceStore.getPreferences().setFundamentalColumnOrder(
                        TableUtil.getColumnOrder(_tblFundamental, LOCKED_COLUMNS));
                    ApolloPreferenceStore.savePreferences();
                }
            }
        });

        //load previous stored visible columns and column ordering
        boolean[] visibles = ApolloPreferenceStore.getPreferences().getFundamentalColumnVisible();
        if (null != visibles)
            TableUtil.setColumnsVisible(_tblFundamental, visibles);
        int[] orders = ApolloPreferenceStore.getPreferences().getFundamentalColumnOrder();
        if (orders != null)
            TableUtil.setColumnOrder(_tblFundamental, orders);

        //sets up listeners
        _tblFundamental.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //nothing selected, disable delete, close button
                int row = _tblFundamental.getSelectedRow();
                if (row == -1) {//de-selection, clear graph
                    Props.SymbolSelection.setValue("");
                    return;
                }

                //draw graph
                row = _tblFundamental.convertRowIndexToModel(row);
                String symbol = (String)_TableModel.getCell(row, Fundamental.TOKEN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(symbol);
            }
        });
    }

    //----- public methods -----
    public void populate(WatchListModel mode1) {
        _tblFundamental.clearSelection();
        _TableModel.populate(mode1);
//        autoSort();
    }
    public void findSymbol(String symbol) {
        if (symbol.equals(""))  return;//de-select sends empty string

        //find which row and scroll into view
        int row = _TableModel.findSymbol(symbol);
        if (row < 0) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_5") + " " + symbol, LoggingSource.L_SQUARE_PERFORMANCE);
            return;
        }
        row = _tblFundamental.convertRowIndexToView(row);
        WidgetUtil.scrollCellVisible(_tblFundamental, row, Fundamental.TOKEN_SYMBOL);
        _tblFundamental.getSelectionModel().setSelectionInterval(row, row);

    }
    public void showHideSymbol(String symbol, boolean show_symbol) {
        _TableModel.showHideSymbol(symbol, show_symbol);
        if (show_symbol) findSymbol(symbol);
    }

    //----- private methods -----
    private void autoSort() {
        //should auto-sort here
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(Fundamental.TOKEN_EPS_QTR, SortOrder.DESCENDING));
        _Sorter.setSortKeys(keys);
    }
    //TODO move to framework utilities
    private void exportReports() throws IOException {
        //ask user for file name and rsp
        JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rsp = fc.showSaveDialog(null);//todo MainFrame.getInstance() centering
        if (rsp == JFileChooser.APPROVE_OPTION) {
            File output_path = fc.getSelectedFile();
            if (output_path.exists()) { //warn user if file exist
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("exp_msg_1") + " " + fc.getName(), LoggingSource.L_SQUARE_PERFORMANCE);
//                if (MessageBox.messageBox(
//                        null,
//                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("exp_msg_1"),
//                        MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                    return;
            }

            //write lines into this file from table model
            PrintWriter pw = new PrintWriter(new FileWriter(output_path + FrameworkConstants.EXTENSION_TRADES));
            pw.println("NAME=MARKET SCAN");
            pw.println("Symbol, Phase, Condition, Recent Phase Change(date/from), Recent Condition Change(2->1), Recent Condition Change(4->3)");
            int row_cnt = _TableModel.getRowCount();
            for (int row=0; row<row_cnt; row++) {
                SimpleCell[] cells = _TableModel.getRow(row);
                StringBuilder sb = new StringBuilder();
                for (SimpleCell cell : cells)
                    sb.append(cell.getValue()).append(",");
                pw.println(sb.toString());
            }
            pw.flush();
            pw.close();
        }
    }

    //----- inner classes -----
    private class FundamentalRenderer extends DynaTableCellRenderer {
        private FundamentalRenderer() {
            super(_TableModel);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int model_row = table.convertRowIndexToModel(row);
            int model_column = table.convertColumnIndexToModel(column);
            Object val = _TableModel.getCell(model_row, model_column).getValue();
            JLabel lbl = (JLabel)comp;

            //if last update is in the future, flag indicating no fundamental data
            Calendar cal = (Calendar)_TableModel.getCell(model_row, Fundamental.TOKEN_LAST_UPDATE).getValue();
            Calendar cal1 = Calendar.getInstance();
            boolean no_data = cal.compareTo(cal1) > 0;//future
            lbl.setText("");
            if (no_data && model_column != Fundamental.TOKEN_SYMBOL) //keep symbol column showing
                return lbl;

            switch (model_column) {
                case Fundamental.TOKEN_SYMBOL://use default, string type
                case Fundamental.TOKEN_FULL_NAME:
                case Fundamental.TOKEN_SECTOR:
                case Fundamental.TOKEN_INDUSTRY:
                case Fundamental.TOKEN_EARNING_DATE:
                    lbl.setText((String)val);
                    break;

                case Fundamental.TOKEN_YIELD://percent formats
                case Fundamental.TOKEN_EPS_YTD:
                case Fundamental.TOKEN_EPS_5_YR:
                case Fundamental.TOKEN_EPS_QTR:
                case Fundamental.TOKEN_SALES_5YR:
                case Fundamental.TOKEN_SALES_QTR:
                case Fundamental.TOKEN_INSIDER_OWN:
                case Fundamental.TOKEN_INST_OWN:
                case Fundamental.TOKEN_SHARES_SHORT:
                case Fundamental.TOKEN_ROA:
                case Fundamental.TOKEN_ROE:
                case Fundamental.TOKEN_ROI:
                case Fundamental.TOKEN_GROSS_MARGIN:
                case Fundamental.TOKEN_OP_MARGIN:
                case Fundamental.TOKEN_PROFIT_MARGIN:
                    setDoubleCell(val, lbl, FrameworkConstants.ROI_FORMAT);
                    break;

                case Fundamental.TOKEN_LAST_UPDATE://Calendar type
                    cal = (Calendar)val;
                    cal1 = Calendar.getInstance();
                    boolean future_date = cal.compareTo(cal1) > 0;//future
                    lbl.setText("");
                    if (!future_date)
                        lbl.setText(FrameworkConstants.YAHOO_DATE_FORMAT.format(cal.getTime()));
                    else
                        lbl.setBackground(Constants.CELL_EDITING_BACKGROUND);
                    break;

                case Fundamental.TOKEN_MARKET_CAP:
                case Fundamental.TOKEN_SHARES_FLOAT:
                case Fundamental.TOKEN_SHARES_OUT:
                    setDoubleCell(val, lbl, FrameworkConstants.FORMAT_NUMBERS);
                    break;

                case Fundamental.TOKEN_EPS_TTM:
                    setDoubleCell(val, lbl, FrameworkConstants.DOLLAR_FORMAT);
                    break;

                default://these are decimal numbers
                    setDoubleCell(val, lbl, FrameworkConstants.PRICE_FORMAT);
                    break;
            }

            //if cell content is 0.0, make it blank
            return comp;
        }
    }

    private void setDoubleCell(Object value, JLabel lbl, DecimalFormat FORMAT) {
        double double_value = (Double) value;
        lbl.setText(FORMAT.format(double_value));
        if (double_value < 0)
            lbl.setBackground(Constants.CELL_EDITING_BACKGROUND);
        else if (double_value == 0)
            lbl.setText("");
    }

    //-----instance variables-----
    private JButton _btnShowHideColumn = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_13"), LazyIcon.TABLE_COLUMN_OP);
    private JButton _btnExport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_5"), FrameworkIcon.EXPORT);
    private JTable _tblFundamental;
    private FundamentalTableModel _TableModel;
    private RowSorter _Sorter;

    //-----literals-----
    private static final int LOCKED_COLUMNS = 1;
}


//TODO useful code
//        for (int col = TestTableModel.COLUMN_CUR_RATIO; col <= TestTableModel.COLUMN_EARNING_GROWTH; col++)
//            sorter.setComparator(col, new FundamentalComparator());
