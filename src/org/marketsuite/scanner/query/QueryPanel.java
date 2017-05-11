package org.marketsuite.scanner.query;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class QueryPanel extends JPanel implements PropertyChangeListener {
    public QueryPanel() {
        setLayout(new MigLayout("insets 0"));

        //north - title strip, calender widget, strategy type, parameter widgets
        JPanel ttl_pnl = new JPanel(new MigLayout("insets 0", "5[][]20[]20[]push[]5[]5[]5", "3[]3")); ttl_pnl.setOpaque(false);
//        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_lbl_1"));
//        lbl.setFont(Constants.FONT_BOLD);
//        ttl_pnl.add(lbl);
//        ttl_pnl.add(_fldStartDate);

        //find last monday as default starting day
//        Calendar cal = AppUtil.calcPastMonday(Calendar.getInstance());
//        Date past_monday = cal.getTime();
//        _fldStartDate.setDate(past_monday); _fldStartDate.setEnabled(false);//TODO maybe later
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_find")); lbl.setFont(Constants.FONT_BOLD);
        ttl_pnl.add(lbl); ttl_pnl.add(_fldSearch); _fldSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String txt = _fldSearch.getText().toUpperCase();
                if (txt.length() == 0) return;
                findSymbol(txt);
            }
        });
        ttl_pnl.add(_lblCount); ttl_pnl.add(_btnRun); _btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _pnlForm.runQuery();
            }
        });
//        ttl_pnl.add(_btnChart); _btnChart.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                if (!_dlgCandleChart.isVisible()) _dlgCandleChart.setVisible(true);
//                _dlgCandleChart = new JDialog(MdiMainFrame.getInstance(), "Candlestick Signals");
//                _pnlCandleCanvas = new JPanel();
//                _pnlCandleCanvas.addMouseMotionListener(new TableUtil.MouseMotionAdapter() {
//                    public void mouseMoved(MouseEvent e) {
//                        //from mouse position, figure out which segment of window, derive date
//                        //then pop up a tooltip showing detail
//                        int width = _pnlCandleCanvas.getWidth();
//                        int unit_width = width / 20;//TODO make it variable
//                        int xcoor = e.getX();
//                        int seg = 20 - xcoor / unit_width - 1; //from latest date
//                        FundQuote quote = _curMarketInfo.getFund().getQuote().get(seg);
//                        StringBuilder buf = new StringBuilder("<html>");
//                        buf.append("Symbol  ").append(_sCurSymbol).append("<br>").append(quote.getDate())
//                            .append("<br>Open ").append(quote.getOpen())
//                            .append("<br>High").append(quote.getHigh());
//                        _pnlCandleCanvas.setToolTipText(buf.toString());
//                    }
//                });
////                _dlgCandleChart.setAlwaysOnTop(true);
//                _dlgCandleChart.setContentPane(_pnlCandleCanvas);
////                _dlgCandleChart.setLocation(e.getX(), e.getY());
//                WidgetUtil.setDialogProperties(_dlgCandleChart, new Dimension(700, 400), true, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);
//            }
//        });
        ttl_pnl.add(_btnGenWatchList); _btnGenWatchList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //compile symbol list
                if (_tmQuery.getRowCount() == 0) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("qp_70"),
                            MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    return;
                }

                //ask name
                NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                if (dlg.isCancelled())
                    return;
                String name = dlg.getEntry();

                //check duplicate list name
                if (GroupStore.getInstance().isGroupExist(name)) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("trk_15"),
                            MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    return;
                }

                //collect symbols, remove duplicate
                ArrayList<String> list = new ArrayList<>();
                for (int row = 0; row < _tmQuery.getRowCount(); row++) {
                    String symbol = (String) _tmQuery.getCell(row, COLUMN_SYMBOL).getValue();
                    if (!list.contains(symbol))//remove duplicate
                        list.add(symbol);
                }
                GroupStore.getInstance().addGroup(name, list);//empty group persist
                Props.WatchListsChange.setChanged();//notify watch list manager
            }
        });
        ttl_pnl.add(_btnExport); _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                FileUtil.exportSheet(_tmQuery, new File(FrameworkConstants.DATA_FOLDER_EXPORT));
            }
        });
        ttl_pnl.add(_btnShowHideColumn); _btnShowHideColumn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Image image = LazyIcon.APP_ICON.getImage();
                //gather column names from schema
                String[] column_names = new String[TABLE_SCHEMA.length];
                for (int row = 0; row < TABLE_SCHEMA.length; row++)
                    column_names[row] = (String) TABLE_SCHEMA[row][0];
                SchemaColumnDialog dlg = new SchemaColumnDialog(_tblQuery, column_names, MdiMainFrame.getInstance(), image, LOCKED_COLUMNS);
                dlg.setVisibleColumns(ApolloPreferenceStore.getPreferences().getQueryColumnVisible());
                dlg.setVisible(true);
                boolean[] visible_columns = dlg.getResult();
                if (null != visible_columns) {
                    ApolloPreferenceStore.getPreferences().setQueryColumnVisible(visible_columns);
                    ApolloPreferenceStore.savePreferences();
                    TableUtil.setColumnsVisible(_tblQuery, visible_columns);
                }
            }
        });
        add(ttl_pnl, "dock north");

        //center - split pane
        JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        spl.setContinuousLayout(true);
        spl.setDividerLocation(400);
        _tblQuery = new JTable(_tmQuery);
        WidgetUtil.initDynaTable(_tblQuery, _tmQuery, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new QueryCellRenderer());
        _tblQuery.setAutoCreateRowSorter(true);
        _tblQuery.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblQuery.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int sel = _tblQuery.getSelectedRow();
                if (sel < 0) return;
                sel = _tblQuery.convertRowIndexToModel(sel);//for sorting

                //notify chart windows to change
                String sym = (String) _tmQuery.getCell(sel, COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(sym);
            }
        });
        spl.setTopComponent(new JScrollPane(_tblQuery));
        spl.setBottomComponent(_pnlForm = new QueryFormPanel());
        add(spl, "dock center");

        //NOTE: only motion listener will make it show up, MouseListener no good
//        _pnlCandleCanvas.addMouseMotionListener(new MouseMotionAdapter() {
//            public void mouseMoved(MouseEvent e) {
//                String ci = _pnlCandleCanvas.getCandleInfo(e.getX());//TODO consider a structure back instead
//
//                //also get candle signals from table model
//                StringBuilder buf = new StringBuilder(ci); buf.append("<br><br>");
//                Calendar cal = _pnlCandleCanvas.getCandleDate(e.getX());
//                ArrayList<CandlePattern> candle_sigs = _pnlForm.getCandlePatterns(_pnlCandleCanvas.getCurSymbol());
//                if (candle_sigs != null) {
//                    for (CandlePattern cp : candle_sigs)
//                        if (cp.getStartDate().equals(cal))
//                            buf.append(cp.getSignal()).append("<br>");
//                }
//                _pnlCandleCanvas.setToolTipText(buf.toString());//TODO later w ballon tip
//            }
//        });
//        _dlgCandleChart.setContentPane(_pnlCandleCanvas);
//        WidgetUtil.setDialogProperties(_dlgCandleChart, new Dimension(700, 400), true, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE, false);
        Props.addWeakPropertyChangeListener(Props.ScanComplete, this);//handle symbol change
    }

    //----- interface/overrides -----
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case ScanComplete:
                _marketInfos = (ArrayList<MarketInfo>)prop.getValue();
                _tmQuery.populate(_marketInfos);
                _lblCount.setText("# " + _tmQuery.getRowCount());
                break;
        }
    }

    //----- private methods -----
    //find and scroll view
    private void findSymbol(String symbol) { //find which row and scroll into view
        int row = _tmQuery.findSymbol(symbol);
        if (row < 0) { WidgetUtil.showMessageNoEdt("Symbol Not Found !"); return; }
        WidgetUtil.scrollCellVisible(_tblQuery, row, COLUMN_SYMBOL);
        _tblQuery.getSelectionModel().setSelectionInterval(row, row);
    }

    //----- inner classes -----
    private class QueryTableModel extends DynaTableModel {
        public QueryTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }

        //-----interface implementations-----
        //use _MarketInfo to populate table
        public void populate() { }
        private void populate(ArrayList<MarketInfo> mkis) {
            _lstRows.clear();
            for (MarketInfo mki : mkis) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                String symbol = mki.getSymbol();
                try {
                    ArrayList<IbdRating> ratings = IbdRating.readIbdRating(symbol, FrameworkConstants.DATA_FOLDER_IBD_RATING, 3);
                    cells[COLUMN_COMPOSITE] = new SimpleCell(new Long(ratings.get(0).getComposite()));
                    cells[COLUMN_RS] = new SimpleCell(new Long(ratings.get(0).getRsRating()));
                } catch (IOException e) {//can't read IBD rating
                    cells[COLUMN_COMPOSITE] = new SimpleCell(new Long(0));
                    cells[COLUMN_RS] = new SimpleCell(new Long(0));
                }
                FundData fund = mki.getFund();
                String high_date = mki.getHighestDate();
                float high_price = fund.getPrice(fund.findIndexByDate(high_date));
                cells[COLUMN_HIGH_PRICE] = new SimpleCell(new Double(high_price));
                cells[COLUMN_HIGH_DATE] = new SimpleCell(high_date);
                cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
                cells[COLUMN_PHASE] = new SimpleCell(mki.getCurrentPhase());

                //percentage near 10/30/50/200SMA
                float close = fund.getQuote().get(0).getClose();
                float ma10 = mki.getSma10()[0];
                cells[COLUMN_NEAR_10SMA] = new SimpleCell(new Double((close - ma10) / close));
                float ma20 = mki.getSma20()[0];
                cells[COLUMN_NEAR_20SMA] = new SimpleCell(new Double((close - ma20) / close));
                float ma30 = mki.getSma30()[0];
                cells[COLUMN_NEAR_30SMA] = new SimpleCell(new Double((close - ma30) / close));
                float ma50 = mki.getSma50()[0];
                cells[COLUMN_NEAR_50SMA] = new SimpleCell(new Double((close - ma50) / close));
                float ma200 = mki.getSma200()[0];
                cells[COLUMN_NEAR_200SMA] = new SimpleCell(new Double((close - ma200) / close));
                float pct = (high_price - close) / high_price;
                cells[COLUMN_PCT_OFF_HIGH] = new SimpleCell(new Double(pct));
                cells[COLUMN_PRICE] = new SimpleCell(new Double(fund.getPrice(0)));
                cells[COLUMN_AVG_VOLUME] = new SimpleCell(new Double(mki.getVolumeAverage()[0]));
                cells[COLUMN_DSTO] = new SimpleCell(new Double(mki.getDsto()[0]));
                cells[COLUMN_WSTO] = new SimpleCell(new Double(0));//TODO
                cells[COLUMN_MACD] = new SimpleCell(new Double(mki.getMacd()[0]));
                cells[COLUMN_MACD_SIG] = new SimpleCell(new Double(mki.getMacdSig()[0]));
                cells[COLUMN_ADX] = new SimpleCell(new Double(0));//TODO
                cells[COLUMN_DMI_PLUS] = new SimpleCell(new Double(0));//TODO
                cells[COLUMN_DMI_MINUS] = new SimpleCell(new Double(0));//TODO
                cells[COLUMN_BB_BANDWIDTH] = new SimpleCell(new Double(mki.getBollingerBand().getBandwidth()[0]));
                cells[COLUMN_BB_PLUS] = new SimpleCell(new Double(0));//TODO
                cells[COLUMN_BB_MINUS] = new SimpleCell(new Double(0));//TODO
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        //find a symbol and return row index, return -1 if not found
        private int findSymbol(String symbol) {
            for (int row = 0; row < getRowCount(); row++) {
                String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
                if (sym.equals(symbol))
                    return row;
            }
            return -1;
        }
    }
    private class QueryCellRenderer extends DynaTableCellRenderer {
        private QueryCellRenderer() { super(_tmQuery); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            int model_col = _tblQuery.convertColumnIndexToModel(column);
            lbl.setToolTipText("");

            //render percent near xMA with formatter
            if (model_col >= COLUMN_NEAR_10SMA && model_col <= COLUMN_PCT_OFF_HIGH || model_col == COLUMN_BB_BANDWIDTH) {
                double v = (Double)value;
                lbl.setText(FrameworkConstants.PCT_FORMAT.format(v));
            }
            else if (model_col == COLUMN_PRICE || model_col == COLUMN_HIGH_PRICE) {
                double v = (Double)value;
                lbl.setText(FrameworkConstants.DOLLAR_FORMAT.format(v));
            }
            else if (model_col == COLUMN_AVG_VOLUME) {
                double v = (Double)value;
                lbl.setText(FrameworkConstants.AVG_VOLUME_FORMAT.format(v));
            }
            else if (model_col >= COLUMN_DSTO && model_col <= COLUMN_BB_MINUS) {
                double v = (Double)value;
                lbl.setText(FrameworkConstants.PRICE_FORMAT.format(v));
            }
            else if (model_col == COLUMN_COMPOSITE || model_col == COLUMN_RS) {
                long v = (Long)value;
                if (v == 0)
                    lbl.setText("");
                else
                    lbl.setText(String.valueOf(v));
            }
            else if (model_col == COLUMN_SYMBOL) {
                String sym = (String) value;
                ArrayList<CandlePattern> patterns = _pnlForm.getCandlePatterns(sym);
                StringBuilder buf = new StringBuilder("<html><b>" + sym + "</b><br><br>");
                for (CandlePattern cp : patterns) {
                    buf.append(AppUtil.calendarToString(cp.getStartDate())).append("   ")
                       .append(cp.getSignal().toString())
                       .append("<br>");
                }
                lbl.setToolTipText(buf.toString());
            }
            return lbl;
        }
    }

    //----- variables -----
//    private JXDatePicker _fldStartDate = new JXDatePicker();  TODO an idea to go back in time
    private JTextField _fldSearch = new JTextField(5);
    private JLabel _lblCount = new JLabel();
    private JButton _btnRun = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("qp_59"), FrameworkIcon.RUN);
    private JButton _btnExport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_5"), FrameworkIcon.EXPORT);
    private JButton _btnGenWatchList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_13"), FrameworkIcon.WATCH);
    private JButton _btnShowHideColumn = WidgetUtil.createIconButton("Show/Hide Columns", LazyIcon.TABLE_COLUMN_OP);
    private JTable _tblQuery;
    private QueryTableModel _tmQuery = new QueryTableModel();
    private QueryFormPanel _pnlForm;
//    private JDialog _dlgCandleChart; TODO and idea to pop up tooltip of small candle chart just like finviz
    private ArrayList<MarketInfo> _marketInfos;

    //table columns
    private static final int LOCKED_COLUMNS = 1;//first column is locked, ie.they cannot be hidden
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_PHASE = 1;
    private static final int COLUMN_COMPOSITE = 2;
    private static final int COLUMN_RS = 3;
    private static final int COLUMN_PRICE = 4;
    private static final int COLUMN_AVG_VOLUME = 5;
    private static final int COLUMN_NEAR_10SMA = 6;
    private static final int COLUMN_NEAR_20SMA = 7;
    private static final int COLUMN_NEAR_30SMA = 8;
    private static final int COLUMN_NEAR_50SMA = 9;
    private static final int COLUMN_NEAR_200SMA = 10;
    private static final int COLUMN_PCT_OFF_HIGH = 11;
    private static final int COLUMN_HIGH_PRICE = 12;
    private static final int COLUMN_HIGH_DATE = 13;
    private static final int COLUMN_DSTO = 14;
    private static final int COLUMN_WSTO = 15;
    private static final int COLUMN_MACD = 16;
    private static final int COLUMN_MACD_SIG = 17;
    private static final int COLUMN_ADX = 18;
    private static final int COLUMN_DMI_PLUS = 19;
    private static final int COLUMN_DMI_MINUS = 20;
    private static final int COLUMN_BB_BANDWIDTH = 21;
    private static final int COLUMN_BB_PLUS = 22;//bollinger band upper
    private static final int COLUMN_BB_MINUS = 23;//bollinger band lower
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_01"), ColumnTypeEnum.TYPE_STRING, -1, 70, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_04"), ColumnTypeEnum.TYPE_STRING, -1, 100, null, null, null},//phase
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_comp"), ColumnTypeEnum.TYPE_LONG, -1, 90, null, null, null},//Composite
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_rs"), ColumnTypeEnum.TYPE_LONG,   -1, 50, null, null, null},//RS
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_60"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//price
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_61"), ColumnTypeEnum.TYPE_DOUBLE, -1, 90, null, null, null},//avg vol =5
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_05"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null},//10MA
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_13"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null},//20MA
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_06"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null},//30MA
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_07"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null},//50MA
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_08"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//200MA =10
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_off_hp"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//% off high
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_hp"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//high price
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_hp_date"), ColumnTypeEnum.TYPE_STRING, -1, 100, null, null, null},//high price date
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_40"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null},//dsto
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_48"), ColumnTypeEnum.TYPE_DOUBLE, -1, 70, null, null, null},//wsto =15
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_35"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//macd
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_macdsig"), ColumnTypeEnum.TYPE_DOUBLE, -1, 80, null, null, null},//macd sig
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_54"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//adx
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_dmip"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//dmi+
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_dmim"), ColumnTypeEnum.TYPE_DOUBLE, -1, 60, null, null, null},//dmi- =20
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_69"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//BB Bandwidt
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_bbup"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//BB +
        {ApolloConstants.APOLLO_BUNDLE.getString("qp_bbdn"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//BB -
    };
}
