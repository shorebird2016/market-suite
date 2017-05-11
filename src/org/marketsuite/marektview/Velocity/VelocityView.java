package org.marketsuite.marektview.Velocity;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.graph.SimpleBarGraph;
import org.marketsuite.component.graph.SimpleTimeSeriesGraph;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.RateInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import jsc.descriptive.FrequencyTable;
import jsc.descriptive.MeanVar;
import jsc.descriptive.OrderStatistics;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.graph.SimpleBarGraph;
import org.marketsuite.component.graph.SimpleTimeSeriesGraph;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.spinner.IntegerSpinner;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.RateInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

public class VelocityView extends JPanel {
    public VelocityView() {
        setLayout(new MigLayout("insets 0"));

        //north - group selector, date range selector, interval selector
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5px[][]20px[][]5px[][]10px[]push", "3px[]3px"));
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_07")));
        initGroupSelector();//must init first
        north_pnl.add(_cmbGrpSelector = new JComboBox<>(_cmlGrpSelector));
        _cmbGrpSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    return;

                //re-calculate watch list model, plot
                String sel_grp = (String) _cmbGrpSelector.getSelectedItem();
                if (GroupStore.getInstance().getMembers(sel_grp).size() == 0) {//no symbol for this group, do nothing
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("mkt_50") + " " + sel_grp + " " +
                                    ApolloConstants.APOLLO_BUNDLE.getString("mkt_51"),
                            MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                    return;
                }
                refresh();
            }
        });
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_08")));
        north_pnl.add(_fldSampleRange);
        _fldSampleRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_09")));
        north_pnl.add(_spnInterval);
        north_pnl.add(_spnBin);
        add(north_pnl, "dock north");

        //center table and graph, two split panes stacked vertically
        JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        spl.setDividerLocation(300);

        //top of split - left table, right frequency graph
        JSplitPane top_spl = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        top_spl.setDividerLocation(700);
        _tblVelocity = WidgetUtil.createDynaTable(_tmVelocity, ListSelectionModel.SINGLE_SELECTION,
                new SortHeaderRenderer(), false, new StatsCellRenderer());
        _tblVelocity.setAutoCreateRowSorter(true);
        _Sorter = _tblVelocity.getRowSorter();
        _tblVelocity.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //nothing selected, disable delete, close button
                int row = _tblVelocity.getSelectedRow();
                if (row == -1 || _tmVelocity.getRowCount() == 0) //de-selection, change nothing
                    return;
                row = _tblVelocity.convertRowIndexToModel(row);
                _sCurSymobl = (String) _tmVelocity.getCell(row, COLUMN_SYMBOL).getValue();
                ArrayList<RateInfo> ris = _Rates.get(_sCurSymobl);
                FrequencyTable fqt = new FrequencyTable(_sCurSymobl, _spnBin.getValue(), rateInfoToArray(ris));
                _pnlFreqency.plotFrequency(fqt);
                plotRateOverTime();
            }
        });
        top_spl.setLeftComponent(new JScrollPane(_tblVelocity));
        top_spl.setRightComponent(_pnlFreqency = new SimpleBarGraph(ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_05"),
            ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_06")));
        spl.setTopComponent(top_spl);

        //bottom - velocity over time w price ROC
        JSplitPane bot_spl = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bot_spl.setDividerLocation(500);
        bot_spl.setLeftComponent(_pnlVelocityGraph = new SimpleTimeSeriesGraph("",
            ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_15"), ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_05")));
        spl.setBottomComponent(bot_spl);
        add(spl, "dock center");
        String sel_grp = (String) _cmbGrpSelector.getSelectedItem();
//        if (sel_grp != null)
//            refresh();
    }

    //----- private methods -----
    private void initGroupSelector() {
        //prefill with all the watch list groups
        ArrayList<String> groups = GroupStore.getInstance().getGroupNames();
        _cmlGrpSelector.removeAllElements();
        for (int idx = 0; idx < groups.size(); idx++) {
//            if (groups.get(idx).startsWith("ETF - ") || groups.get(idx).startsWith("GRP - "))
                _cmlGrpSelector.addElement(groups.get(idx));
        }
    }
    private void initRatesMap(String watchlist_name, int range, int interval) {
        _Rates.clear();
        _WatchlistModel = new WatchListModel(watchlist_name, false);
        for (String symbol : _WatchlistModel.getMembers()) {
            MarketInfo mki = _WatchlistModel.getMarketInfo(symbol);
            if (mki == null)
                continue;
            ArrayList<RateInfo> ri = IndicatorUtil.calcPriceRates(mki.getFund(), range, 0, interval);
            _Rates.put(symbol, ri);
        }
    }
    private double[] rateInfoToArray(ArrayList<RateInfo> ris) {
        double[] rates = new double[ris.size()];
        for (int index = 0; index < rates.length; index++)
            rates[index] = ris.get(index).getRatePerDay();
        return rates;
    }
    private void plotFreq() {
        ArrayList<RateInfo> ris = _Rates.get(_sCurSymobl);
        FrequencyTable fqt = new FrequencyTable(_sCurSymobl, _spnBin.getValue(), rateInfoToArray(ris));
        double[] boundary = new double[fqt.getNumberOfBins()];
        for (int i = 0; i < fqt.getNumberOfBins(); i++)
            boundary[i] = fqt.getBoundary(i);
        _pnlFreqency.plot(_sCurSymobl, boundary, fqt.getFrequencies());
    }
    private void plotRateOverTime() {
        ArrayList<RateInfo> ris = _Rates.get(_sCurSymobl);
        int data_size = ris.size();
        Calendar[] dates = new Calendar[data_size];
        double[] prices = new double[data_size];
        double[] rates = new double[data_size];
        String[] names = new String[2];
        names[0] = _sCurSymobl + " " + ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_15");
        names[1] = ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_16");
        FundData fund = _WatchlistModel.getMarketInfo(_sCurSymobl).getFund();
        for (int i = 0; i < ris.size(); i++) {
            dates[i] = ris.get(i).getEndDate();
            FundQuote quote = fund.findQuoteByDate(AppUtil.calendarToString(dates[i]));
            prices[i] = quote.getClose();
            rates[i] = ris.get(i).getRatePerDay();
        }
        _pnlVelocityGraph.plot(names, dates, prices, rates);
    }
    private void plotRoc() {
        MarketInfo mki = _WatchlistModel.getMarketInfo(_sCurSymobl);
        FundData fund = mki.getFund();
        int range = (int)_fldSampleRange.getValue();
        int interval = _spnInterval.getValue();
        double[] rocs = IndicatorUtil.calcROC(fund, interval, range, 0);
        int data_size = rocs.length;
        Calendar[] dates = new Calendar[data_size];
        double[] prices = new double[data_size];
        String[] names = new String[2];
        names[0] = "ROC"; names[1] = "Price";
        for (int i = 0; i < data_size; i++) {
            dates[i] = AppUtil.stringToCalendarNoEx(fund.getDate(i));
            prices[i] = fund.getPrice(i);
        }
        _pnlMaRocGraph.plot(names, dates, rocs, prices);
    }
    private void plotMaRoc() {
        MarketInfo mki = _WatchlistModel.getMarketInfo(_sCurSymobl);
        float[] sma50 = mki.getSma10();
        double[] sma = new double[sma50.length];
        for (int i = 0; i < sma50.length; i++)
            sma[i] = sma50[i];
        FundData fund = mki.getFund();
        int range = (int)_fldSampleRange.getValue();
        int interval = _spnInterval.getValue();
        double[] rocs = IndicatorUtil.calcRateOfChange(sma, interval, range, 0);
        int data_size = rocs.length;
        Calendar[] dates = new Calendar[data_size];
        double[] mas = new double[data_size];
        String[] names = new String[2];
        names[0] = "ROC"; names[1] = "10 SMA";
        for (int i = 0; i < data_size; i++) {
            dates[i] = AppUtil.stringToCalendarNoEx(fund.getDate(i));
            mas[i] = fund.getPrice(i);
        }
        _pnlMaRocGraph.plot(names, dates, rocs, sma);
    }
    private void autoSort() {//only the mean column by default
        ArrayList<RowSorter.SortKey> keys = new ArrayList<>();
        keys.add(new RowSorter.SortKey(COLUMN_MEAN, SortOrder.DESCENDING));
        _Sorter.setSortKeys(keys);
    }
    private void refresh() { //create RateInfo map and update table and graph
        String sel_grp = (String) _cmbGrpSelector.getSelectedItem();
        int range = (int)_fldSampleRange.getValue();
        int interval = _spnInterval.getValue();
        initRatesMap(sel_grp, range, interval);
        _tmVelocity.populate(); autoSort();
        _tblVelocity.getSelectionModel().setSelectionInterval(0, 0);//triggers selection listener to plot
    }

    //----- inner classes -----
    //a table showing velocity statistics of all symbols in the group
    private class StatsTableModel extends DynaTableModel {
        private StatsTableModel() {
            super(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }
        public boolean isCellEditable(int row, int column) { return false; }
        public void populate() {
            _tmVelocity.clear();
            Iterator<String> itor = _Rates.keySet().iterator();
            while (itor.hasNext()) {
                String symbol = itor.next();
                ArrayList<RateInfo> ris = _Rates.get(symbol);
                _tmVelocity.populateRow(symbol, rateInfoToArray(ris));
            }
            _tmVelocity.fireTableDataChanged();
        }
        public void populateRow(String symbol, double[] rates) {
            MeanVar stat = new MeanVar(rates);
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            cells[COLUMN_SYMBOL] = new SimpleCell(symbol);
            cells[COLUMN_PRICE] = new SimpleCell(new Double(_WatchlistModel.getMarketInfo(symbol).getFund().getPrice(0)));
            cells[COLUMN_MEAN] = new SimpleCell(stat.getMean());
            cells[COLUMN_STD_DEVIATION] = new SimpleCell(stat.getSd());
            cells[COLUMN_VARIANCE] = new SimpleCell(stat.getVariance());

            //OrderStatistics re-arranged rates array, do this last
            cells[COLUMN_RECENT_VELOCITY] = new SimpleCell(rates[0]);
            OrderStatistics ord_stat = new OrderStatistics(rates);
            cells[COLUMN_MEDIAN] = new SimpleCell(ord_stat.getMedian());

            //most recent rate of change
            FundData fund = _WatchlistModel.getMarketInfoMap().get(symbol).getFund();
            int interval = _spnInterval.getValue();
            double[] rocs = IndicatorUtil.calcROC(fund, interval, 30, 0);//get the latest
            cells[COLUMN_ROC] = new SimpleCell(rocs[0]);
            _lstRows.add(cells);
        }
    }
    private class StatsCellRenderer extends DynaTableCellRenderer {
        private StatsCellRenderer() {
            super(_tmVelocity);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = _tblVelocity.convertRowIndexToModel(row);
            column = _tblVelocity.convertColumnIndexToModel(column);
            Object val = _tmVelocity.getCell(row, column).getValue();
            JLabel lbl = (JLabel)comp;
            lbl.setToolTipText(null);
            switch (column) {
                case COLUMN_SYMBOL:
                    return comp;
                case COLUMN_PRICE:
                    lbl.setText(FrameworkConstants.DOLLAR_FORMAT.format((Double)val));
                    break;
                case COLUMN_ROC:
                    lbl.setText(FrameworkConstants.ROI_FORMAT.format(val));
                    break;
                default:
                    if (val instanceof Double) {
                        double v = (Double)val;
                        lbl.setText(FrameworkConstants.PRICE_FORMAT.format(v));
                    }
            }
            return comp;
        }
    }

    //----- variables -----
    private JComboBox<String> _cmbGrpSelector;
    private LongIntegerField _fldSampleRange = new LongIntegerField(180, 3, 10, 250);
    private IntegerSpinner _spnInterval = new IntegerSpinner("", 5, 3, 15, 1,
            ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_10"), new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            refresh();
        }
    });
    private IntegerSpinner _spnBin = new IntegerSpinner(ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_11"), 8, 5, 20, 1,
            "", new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            refresh();
        }
    });
    private DefaultComboBoxModel<String> _cmlGrpSelector = new DefaultComboBoxModel<>();
    private JTable _tblVelocity;
    private StatsTableModel _tmVelocity = new StatsTableModel();
    private WatchListModel _WatchlistModel = MainModel.getInstance().getWatchListModel();
    private SimpleBarGraph _pnlFreqency;
    private SimpleTimeSeriesGraph _pnlVelocityGraph, _pnlMaRocGraph;
    private HashMap<String, ArrayList<RateInfo>> _Rates = new HashMap<>();
    private RowSorter _Sorter;
    private String _sCurSymobl;

    //----- literals -----
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_PRICE = 1;
    private static final int COLUMN_RECENT_VELOCITY = 2;
    private static final int COLUMN_MEAN = 3;
    private static final int COLUMN_MEDIAN = 4;
    private static final int COLUMN_STD_DEVIATION = 5;
    private static final int COLUMN_VARIANCE = 6;
    private static final int COLUMN_ROC = 7;
    private static final Object[][] TABLE_SCHEMA = {
            {ApolloConstants.APOLLO_BUNDLE.getString("mkt_30"), ColumnTypeEnum.TYPE_STRING, -1, 30, null, null, null},//symbol
            {ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_2"), ColumnTypeEnum.TYPE_DOUBLE, -1, 30, null, null, null},//price
            {ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_16"), ColumnTypeEnum.TYPE_DOUBLE, -1, 30, null, null, null},//recent velocity
            {ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_01"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//mean
            {ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_13"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//median
            {ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_02"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//std dev
            {ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_03"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//variance
            {ApolloConstants.APOLLO_BUNDLE.getString("vv_lbl_14"), ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},//ROC
    };
}
