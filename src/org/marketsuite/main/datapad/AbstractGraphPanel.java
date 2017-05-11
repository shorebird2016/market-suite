package org.marketsuite.main.datapad;

import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.DatapadMode;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.datapad.ChartData;
import org.marketsuite.main.datapad.ChartLayerUI;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.type.DatapadMode;
import org.marketsuite.main.MainModel;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public abstract class AbstractGraphPanel extends JPanel {
    //----- CTOR -----
    public AbstractGraphPanel(boolean dcom_type) {
        _bDcomType = dcom_type;
        setLayout(new MigLayout("insets 0"));

        //create ChartPanel to hold plots that are changing
        _pnlChart = new ChartPanel(null, true, true, true, true, true);
        _pnlChart.setFocusable(true);
        _pnlChart.requestFocus();
        final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        _pnlChart.setMaximumDrawWidth((int)Math.round(dimension.getWidth()));
        _pnlChart.setMaximumDrawHeight((int)Math.round(dimension.getHeight()));

        //initialize layer of yellow datapad, need to be available before first draw
        JXLayer<ChartPanel> layer = new JXLayer<>(_pnlChart);
        _ChartLayerUI = new ChartLayerUI<>(this);
        layer.setUI(_ChartLayerUI);
        addComponentListener(new ComponentAdapter() {//resize
            public void componentResized(ComponentEvent e) {
                _ChartLayerUI.updateTraceInfos();
            }
        });

        //pick first member, draw price chart, once price/volume data sets are connected, later just change series within will refresh
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        ArrayList<String> members = wlm.getMembers();
        if (members.size() == 0) return;
        _sCurrentSymbol = members.get(0);
        add(layer, "dock center");//if don't want datapad, use _pnlChart instead
    }

    //----- abstract methods -----
    public abstract void clearGraph();
    public abstract void drawGraph(String symbol, boolean dcom_type);
    public abstract void zoom(int field, int amount);

    //----- methods -----
    public void toggleCursor() {
        _pnlChart.setHorizontalAxisTrace(!_pnlChart.getHorizontalAxisTrace());
        _pnlChart.setVerticalAxisTrace(!_pnlChart.getVerticalAxisTrace());
        _pnlChart.repaint();
    }
    public void setDataPadMode(DatapadMode mode) { _ChartLayerUI.setDatapadMode(mode); }
    protected void buildChartDatas(MarketInfo mki) {
        ArrayList<FundQuote> quotes = mki.getFund().getQuote();
        chartDatas = new ArrayList<>();
        double prevPrice = 0;
        for (int i=quotes.size()-1; i>=0; i--) {
            FundQuote quote = quotes.get(i);
            Calendar cal = Calendar.getInstance();
            try {//unlikely to have bad date
                cal = AppUtil.stringToCalendar(quote.getDate());
            } catch (ParseException e) {
                e.printStackTrace();//ignore bad date
            }
            ChartData cd = ChartData.newInstance(prevPrice, quote.getOpen(), quote.getClose(),
                    quote.getHigh(), quote.getLow(), (long)quote.getVolume(), cal.getTimeInMillis());
            chartDatas.add(cd);
            prevPrice = quote.getClose();
        }
    }
    /**
     * Returns chart change listener, which will be responsible for handling
     * zooming event.
     * @return chart change listener when mouse moves, clicks..etc
     */
    protected ChartChangeListener getChartChangeListner() {
        return new ChartChangeListener() {
            public void chartChanged(ChartChangeEvent event) {
                // Is weird. This works well for zoom-in. When we add new CCI or
                // RIS. This event function will be triggered too. However, the
                // returned draw area will always be the old draw area, unless
                // you move your move over.
                // Even I try to capture event.getType() == ChartChangeEventType.NEW_DATASET
                // also doesn't work.
                if (event.getType() == ChartChangeEventType.GENERAL) {
                    _ChartLayerUI.updateTraceInfos();
                    // Re-calculating high low value.
//TODO later                    updateHighLowJLabels();
                }
            }
        };
    }
    /**
     * Calculate and update high low value labels, according to current displayed
     * time range. This method will return immediately, as the calculating and
     * updating task by performed by user thread.
     */
//    private void updateHighLowJLabels() {
//        updateHighLowLabelsPool.execute(new Runnable() {
//            public void run() {
//                _updateHighLowJLabels();
//            }
//        });
//    }
    /**
     * Calculate and update high low value labels, according to current displayed
     * time range. This is a time consuming method, and shall be called by
     * user thread.
     */
//    private void _updateHighLowJLabels() {//TODO not sure jLable2 and jLabel4 are
////    SwingUtilities.invokeLater(new Runnable() {
////        public void run() {
////            ChartJDialog.jLabel2.setText("");
////            ChartJDialog.jLabel4.setText("");
////        }
////    });
//
//        final ValueAxis valueAxis = getMainPlot().getDomainAxis();
//        final Range range = valueAxis.getRange();
//        final long lowerBound = (long)range.getLowerBound();
//        final long upperBound = (long)range.getUpperBound();
//        if (_PriceDataSet.getSeriesCount() == 0) //chart cleared
//            return;
//
//        // Perform binary search, to located day in price time series, which
//        // is equal or lesser than upperBound.
//        int low = 0;
//        int high = _PriceDataSet.getSeries(0).getItemCount() - 1;
////    int high = priceTimeSeries.getItemCount() - 1;
//        long best_dist = Long.MAX_VALUE;
//        int best_mid = -1;
//        while (low <= high) {
//            int mid = (low + high) >>> 1;
////        final Day day = (Day)priceTimeSeries.getDataItem(mid).getFastPeriod();
//            final Day day = (Day)_PriceDataSet.getSeries(0).getDataItem(mid).getFastPeriod();
//            long v = day.getFirstMillisecond();
//            if (v > upperBound)
//                high = mid - 1;
//            else if (v < upperBound) {
//                low = mid + 1;
//                long dist = upperBound - v;
//                if (dist < best_dist) {
//                    best_dist = dist;
//                    best_mid = mid;
//                }
//            }
//            else {
//                best_dist = 0;
//                best_mid = mid;
//                break;
//            }
//        }
//        if (best_mid < 0)
//            return;
//
//        double high_last_price = Double.NEGATIVE_INFINITY;
//        double low_last_price = Double.MAX_VALUE;
//        for (int i = best_mid; i >= 0; i--) {
////        final TimeSeriesDataItem item = priceTimeSeries.getDataItem(i);
//            final TimeSeriesDataItem item = _PriceDataSet.getSeries(0).getDataItem(i);
//            final long time = ((Day)item.getFastPeriod()).getFirstMillisecond();
//            if (time < lowerBound)
//                break;
//
//            double value = (Double)item.getValue();
//            if (value == 0.0) {
//                /* Market closed during that time. Ignore. */
//                continue;
//            }
//            if (high_last_price < value)
//                high_last_price = value;
//            if (low_last_price > value)
//                low_last_price = value;
//        }
//
//        final double h = high_last_price;
//        final double l = low_last_price;
////    if (high_last_price >= low_last_price) {//TODO not sure jLable2 and jLabel4 are
////        SwingUtilities.invokeLater(new Runnable() {
////            @Override
////            public void run() {
////                ChartJDialog.jLabel2.setText(org.yccheok.jstock.gui.Utils.stockPriceDecimalFormat(h));
////                ChartJDialog.jLabel4.setText(org.yccheok.jstock.gui.Utils.stockPriceDecimalFormat(l));
////            }
////        });
////    }
//    }

    //----- accessors -----
    public ChartPanel getChartPanel() { return _pnlChart; }
    public XYPlot getPlot(int index) {
        final JFreeChart chart = _pnlChart.getChart();
        final CombinedDomainXYPlot combo_plot = (CombinedDomainXYPlot) chart.getPlot();
        final XYPlot plot = (XYPlot) combo_plot.getSubplots().get(index);
        return plot;
    }
    public int getPlotSize() {
        final JFreeChart chart = _pnlChart.getChart();
        final CombinedDomainXYPlot combo_plot = (CombinedDomainXYPlot) chart.getPlot();
        return combo_plot.getSubplots().size();
    }
    public XYPlot getMainPlot() {
        final JFreeChart chart = _pnlChart.getChart();
        final CombinedDomainXYPlot combo_plot = (CombinedDomainXYPlot) chart.getPlot();
        final XYPlot plot = (XYPlot) combo_plot.getSubplots().get(0);
        return plot;
    }
    public java.util.List<ChartData> getChartDatas() { return Collections.unmodifiableList(chartDatas); }

    //----- variables -----
    protected boolean _bDcomType;
    protected String _sCurrentSymbol;
    protected ChartPanel _pnlChart;
    private java.util.List<ChartData> chartDatas;
    protected ChartLayerUI<ChartPanel> _ChartLayerUI;
    /**
     * Thread pool, used to hold threads to update high low labels.
     */
//    private final Executor updateHighLowLabelsPool = Executors.newFixedThreadPool(1);
}
