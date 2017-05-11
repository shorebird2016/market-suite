package org.marketsuite.marektview.performance;

import org.marketsuite.component.Constants;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.GraphMode;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.watchlist.performance.PerformanceTableModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Continer for displaying percent based data with user selectable time frames.
 */
public class PercentGraphPanel extends JPanel {
    public PercentGraphPanel() {
        setLayout(new MigLayout("insets 0"));//remove padding around object

        //center - jFreeChart of equity over time
        _Chart = ChartFactory.createTimeSeriesChart(
            "", // title
            "", // x-axis label
            Constants.COMPONENT_BUNDLE.getString("relative_performance"), // y-axis label
            _DataSet,//empty by default
            false,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot

        //customize looks, background paint - default gray gradient, tooltip, axis new Color(224, 240, 235)
//        _Plot.setBackgroundPaint(new GradientPaint(0, 0, new Color(220, 240, 240), 500, 500, new Color(240, 240, 240)));
        _Plot.setBackgroundPaint(WidgetUtil.PAINT_BACKGROUND_LIGHT_GREEN);
        XYItemRenderer r = _Plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            _Renderer = (XYLineAndShapeRenderer) r;
            _Renderer.setBaseShapesVisible(false);//default false, set true show square data point
            _Renderer.setBaseShapesFilled(false);
            _Renderer.setDrawSeriesLineAsPath(false);
//            _Renderer.setSeriesPaint(0, new Color(0, 100, 50));//light blue,green mix, emphasize first in series
//            _Renderer.setSeriesStroke(0, new BasicStroke(1.5F));
            StandardXYToolTipGenerator tip_gen = new StandardXYToolTipGenerator("{0}: {1}  {2}",//{0}symbol {1}date {2}vale
                new SimpleDateFormat("MM/dd"), FrameworkConstants.ROI_FORMAT);
            _Renderer.setBaseToolTipGenerator(tip_gen);
        }
        _Plot.setDomainCrosshairVisible(true);
        _Plot.setDomainCrosshairLockedOnData(true);
//        ValueAxis time_axis = _Plot.getDomainAxis();
//        time_axis.setLowerMargin(0.02);
//        time_axis.setUpperMargin(0.05);
        NumberAxis pct_axis = (NumberAxis)_Plot.getRangeAxis();//save default one for later
        pct_axis.setUpperMargin(0.001);
        pct_axis.setLowerMargin(0.001);
        pct_axis.setNumberFormatOverride(FrameworkConstants.ROI_FORMAT);
        _Plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _Chart.setAntiAlias(true);
//        _Chart.getLegend().setPosition(RectangleEdge.TOP);

        //use special ChartPanel object to contain JFreeChart
        ChartPanel _pnlChart = new ChartPanel(_Chart);
        _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out
//        _pnlChart.setHorizontalAxisTrace(true);//cross hair cursor
//        _pnlChart.setVerticalAxisTrace(true);
        add(_pnlChart, "dock center");

        //add layer of cross hair and magnifier TODO how to add multiple layers
//        _MagnifierLayer = new JXLayer(_pnlChart);
//        _MagnifierLayer.setUI(new MagnifierUI());
        add(_pnlChart, "dock center");
    }

    //----- public/protected methods -----
    //look for matching time series and remove it, should auto refresh
    public void removeSeries(String symbol) {
        TimeSeries series = _DataSet.getSeries(symbol);
        if (series == null) {
            System.err.println("ERROR: Empty Series for " + symbol);
            return;
        }
        _DataSet.removeSeries(series);
    }
    //find starting index in quotes based on time frame and ending index
    public int calcOrigin(int timeframe_code, WatchListModel wlm, int end_index) {
        //use first symbol to find begin index with valid quote(may have weekend, holiday)
        String symbol0 = wlm.getMembers().get(0);
        MarketInfo mki = wlm.getMarketInfo(symbol0);
        if (mki == null) return -1;
        FundData fund = mki.getFund();
        ArrayList<FundQuote> quotes = fund.getQuote();
        Calendar end_cal = AppUtil.stringToCalendarNoEx(quotes.get(end_index).getDate());
        Calendar begin_cal = PerformanceTableModel.calcBeginTime(fund, end_cal, timeframe_code);//TODO maybe another way
        FundQuote origin_quote = AppUtil.findNearestQuote(fund, begin_cal);
        int origin_index = fund.findIndexByDate(origin_quote.getDate());//based on quotes

        //special handling for at cursor, find current cursor location, translate into index into quote array
        if (timeframe_code == PerformanceTableModel.COLUMN_CUSTOM_PCT) {
            double cursor_value = _Plot.getDomainCrosshairValue();

            //find this value in time series
            boolean found = false;
            List items = _DataSet.getSeries(0).getItems();
            for (int idx = 0; idx < items.size(); idx++) {
                TimeSeriesDataItem item = (TimeSeriesDataItem)items.get(idx);
                long origin_ms = item.getPeriod().getFirstMillisecond();
                if (origin_ms == cursor_value) {
                    //look up equivalent in quotes array, convert period into date string
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(origin_ms);
                    String origin_date = AppUtil.calendarToString(cal);
                    origin_index = fund.findIndexByDate(origin_date);
                    found = true;
                    break;
                }
            }
            if (!found) return -1;
        }
        return origin_index;
    }
//TODO do timeframe_code in enum
    //plot subset range of points with specified length
    public void plotByMode(GraphMode mode, int timeframe_code, WatchListModel wlm, String baseline_symbol, int end_index) {
        //check if vertical cursor is set for both modes
        int origin_index = calcOrigin(timeframe_code, wlm, end_index);
        if (origin_index == -1 && timeframe_code == PerformanceTableModel.COLUMN_CUSTOM_PCT) {
            WidgetUtil.showMessageNoEdt("Please click on the chart to set Vertical Cursor !");
            return;
        }
        if (mode.equals(GraphMode.ORIGIN_MODE))
            plotByOrigin(wlm, origin_index, end_index);
        else
            plotByBaseline(baseline_symbol, origin_index, wlm);
    }

    //----- private methods -----
    //use provided watch list model to draw percentage relative to origin
    private void plotByOrigin(WatchListModel wlm, int origin_index, int end_index) {
        _sBaselineSymbol = null;
        ArrayList<String> members = wlm.getMembers();
        if (members.size() == 0) return;
        _DataSet.removeAllSeries();
        int series_index = 0;
        for (String member : members) {
            MarketInfo mki = wlm.getMarketInfo(member);
            if (mki == null) continue;//no quote, no mki
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            TimeSeries series = quotesToOriginSeries(quotes,
                origin_index == PerformanceViewPanel.FULL_RANGE ? quotes.size() - 1 : origin_index, end_index);
            _Renderer.setSeriesStroke(series_index++, new BasicStroke(1));//this mode always 1
            _DataSet.addSeries(series);
        }
        emphasizeSelectedSymbol();
    }
    //use existing watch list model
    private void plotByBaseline(String baseline_symbol, int start_index, WatchListModel wlm) {
        //check if baseline_symbol is already loaded
        _sBaselineSymbol = baseline_symbol;
        MarketInfo mki = wlm.getMarketInfo(baseline_symbol);
        FundData fund;
        if (mki == null) {//read quotes if not
            try {
                fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, baseline_symbol,
                        FrameworkConstants.MARKET_QUOTE_LENGTH + 2);//2 extra line for comments in file
            } catch (IOException e) {
                e.printStackTrace();
//TODO warn user
                return;
            }
        }
        else
            fund = mki.getFund();

        //traverse all symbols, calculate time series for each, add to dataset
        ArrayList<String> members = wlm.getMembers();
        if (members.size() == 0)
            return;
        _DataSet.removeAllSeries();
        int series_index = 0;
        for (String member : members) {
            mki = wlm.getMarketInfo(member);
            if (mki == null)
                continue;//no quote, no mki
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            TimeSeries series = quotesToBaselineSeries(quotes, fund.getQuote(),
                start_index == PerformanceViewPanel.FULL_RANGE ? quotes.size() - 1 : start_index);
            if (baseline_symbol.equals(series.getKey()))
                _Renderer.setSeriesStroke(series_index, new BasicStroke(3));//emphasize baseline
            _DataSet.addSeries(series);
            series_index++;
        }
        emphasizeSelectedSymbol();
    }
    public void clear() { _DataSet.removeAllSeries(); }
    public void setMagnifier(boolean on) {
    }
    //turn quotes into time series under Origin Mode [% from origin]
    //input: quotes, output: time series of percents; origin: center of conversion
    // origin_index: beginning of data, end_index: end of data (inclusive)
    private TimeSeries quotesToOriginSeries(ArrayList<FundQuote> quotes, int origin_index, int end_index) {
        TimeSeries ret = new TimeSeries(quotes.get(0).getSymbol());
        if (origin_index > quotes.size())
            origin_index = quotes.size() - 1;//not enough data
        float origin_value = quotes.get(origin_index).getClose();
        for (int idx = origin_index; idx >= end_index; idx--) {
            Calendar cal = AppUtil.stringToCalendarNoEx(quotes.get(idx).getDate());
            Day day = new Day(cal.getTime());
            ret.add(day, quotes.get(idx).getClose() / origin_value - 1);
        }
        return ret;
    }
    //turn quotes into time series under Baseline Mode [% from every quote]
    //input: quotes for both symbols of interest and baseline symbol, start_index; output: time series;
    private TimeSeries quotesToBaselineSeries(ArrayList<FundQuote> quotes_symbol,
                                              ArrayList<FundQuote> quotes_baseline, int origin_index) {
        String series_symbol = quotes_symbol.get(0).getSymbol();
        TimeSeries ret = new TimeSeries(series_symbol);
        if (origin_index > quotes_symbol.size())
            origin_index = quotes_symbol.size() - 1;//not enough data
        float origin_symbol = quotes_symbol.get(origin_index).getClose();
        float origin_baseline = quotes_baseline.get(origin_index).getClose();
        for (int idx = origin_index; idx >= 0; idx--) {
            Calendar cal = AppUtil.stringToCalendarNoEx(quotes_symbol.get(idx).getDate());
            Day day = new Day(cal.getTime());
            float pct_symbol = quotes_symbol.get(idx).getClose() / origin_symbol - 1;
            float pct_baseline = quotes_baseline.get(idx).getClose() / origin_baseline - 1;
            ret.add(day, pct_symbol - pct_baseline);
        }
        return ret;
    }
    //find symbol in dataset and make its stroke heavier, also de-emphasize previous symbol
    public void emphasizeSelectedSymbol() {
        //find and make line thicker, baseline mode also make baseline thicker
        int series_index = 0;
        List<TimeSeries> series = (List<TimeSeries>)_DataSet.getSeries();
        for (TimeSeries ts : series) {
            Comparable key = ts.getKey();
            if (key.equals(_sEmphasizedSymbol)) {
//                _LastSavedPaint = _Renderer.getSeriesPaint(series_index);
//                _sLastEmphasizedSymbol = (String)key;
                _Renderer.setSeriesStroke(series_index, new BasicStroke(5));
//                _Renderer.setSeriesPaint(series_index, Color.BLUE);//use same color for bold
            }
            else if (!key.equals(_sBaselineSymbol)) {
                _Renderer.setSeriesStroke(series_index, new BasicStroke(1));
//                if (key.equals(_sLastEmphasizedSymbol))//restore previously emphasized color
//                    _Renderer.setSeriesPaint(series_index, _LastSavedPaint);
            }
            series_index++;
        }
    }
    public void emphasizeSelectedSymbol(String symbol) { _sEmphasizedSymbol = symbol; emphasizeSelectedSymbol(); }

    //----- accessor -----
    public void setEmphasizedSymbol(String symbol) { _sEmphasizedSymbol = symbol; }

    //----- variables -----
    private XYLineAndShapeRenderer _Renderer;
    private JFreeChart _Chart;
    private XYPlot _Plot;
    private JXLayer _MagnifierLayer;
    private TimeSeriesCollection _DataSet = new TimeSeriesCollection();
    private String _sBaselineSymbol;//null = origin mode
    private String _sEmphasizedSymbol;//null = nothing emphasized
}
//    private Paint _LastSavedPaint; private String _sLastEmphasizedSymbol;
//    private ArrayList<Paint> _SeriesPaints;
//make vertical axis log based TODO: add log axis causes auto range malfunction
//        LogarithmicAxis[] axises = new LogarithmicAxis[1];
//        axises[0] = new LogarithmicAxis("");
//        _LogRangeAxisMontly = axises[0];
////        TickUnitSource units = NumberAxis.createIntegerTickUnits();
////        _LogRangeAxisMontly.setStandardTickUnits(units);
//        axises[0].setAllowNegativesFlag(true);
//        _Plot.setRangeAxes(axises);
//    void setLogScale(boolean log_scale) {
//        if (log_scale)
//            _Plot.setRangeAxis(_LogRangeAxisMontly);
//        else
//            _Plot.setRangeAxis(pct_axis);
//    }
