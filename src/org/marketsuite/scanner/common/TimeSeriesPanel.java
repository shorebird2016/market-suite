package org.marketsuite.scanner.common;

import org.marketsuite.component.UI.MagnifierUI;
import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.model.type.Ibd50State;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.GraphUtil;
import org.marketsuite.main.MainModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.marketsuite.component.UI.MagnifierUI;
import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.Ibd50State;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.GraphUtil;
import org.marketsuite.main.MainModel;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
//TODO MOVE to Framework level eventually...........
//container for analysis _Chart using JFreeChart
// use a DataSet to represent underline data
public class TimeSeriesPanel extends AbstractGraphPanel {
    public TimeSeriesPanel() {
        setLayout(new MigLayout("insets 0"));
        _Chart = ChartFactory.createTimeSeriesChart(
            "", // title
            "", // x-axis label
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_2"), // y-axis label TODO: customize title
            _LeftDataSet = new TimeSeriesCollection(),//main data set
            true,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );
        _Chart.getLegend().setPosition(RectangleEdge.TOP);
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot

        //setup right axis (default comes with left axis)
        _Plot.setDataset(1, _RiteDataset);//supplement
        NumberAxis right_axis = new NumberAxis("EPS / RS Rank");//TODO: pass in
        right_axis.setAutoRangeIncludesZero(false);
        _Plot.setRangeAxis(1, right_axis);
        _Plot.mapDatasetToRangeAxis(1, 1);

        //change various attributes - color, shape, stroke, paint, tooltip for main series
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, Color.lightGray, 500, 500, new Color(250, 250, 250)));
        StandardXYToolTipGenerator tip_price = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
            new SimpleDateFormat("MM/dd"), FrameworkConstants.DOLLAR_FORMAT);
        XYItemRenderer r = _Plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);//default false, set true show square data point
            renderer.setBaseShapesFilled(false);
            renderer.setDrawSeriesLineAsPath(false);
            renderer.setSeriesStroke(0, new BasicStroke(1.0f));
            renderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
            renderer.setSeriesStroke(1, new BasicStroke(1.0f));
            renderer.setSeriesPaint(1, Color.cyan.darker());
            renderer.setSeriesStroke(2, new BasicStroke(1.2f));
            renderer.setSeriesPaint(2, Color.orange.darker());
            renderer.setBaseToolTipGenerator(tip_price);
        }

        XYLineAndShapeRenderer rite_renderer = new XYLineAndShapeRenderer();
        rite_renderer.setBaseShapesFilled(false);
        rite_renderer.setSeriesPaint(0, Color.green);
        rite_renderer.setSeriesStroke(0, new BasicStroke(0.5f));
        rite_renderer.setSeriesPaint(1, Color.gray);
        rite_renderer.setSeriesStroke(1, new BasicStroke(0.5f));
        StandardXYToolTipGenerator tip_rank = new StandardXYToolTipGenerator("{1}  {0}={2}",//{0} is symbol
                new SimpleDateFormat("MM/dd"), FrameworkConstants.TWO_DIGIT_FORMAT);
        rite_renderer.setBaseToolTipGenerator(tip_rank);
        _Plot.setRenderer(1, rite_renderer);

        //main axis
        _LinearRangeAxis = _Plot.getRangeAxis();//save default one for later
        _LogRangeAxis = new LogarithmicAxis("");

        //put jfc into panel for layout, set attributes
        _pnlChart = new ChartPanel(_Chart);
        _pnlChart.setRangeZoomable(false);
        _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out
//        _pnlChart.setHorizontalAxisTrace(true);//cross hair cursor  TODO: may interfere with magnifier
//        _pnlChart.setVerticalAxisTrace(true);
        add(_pnlChart, BorderLayout.CENTER);

        //add magnifier layer
        JXLayer layer = new JXLayer(_pnlChart);
        MagnifierUI ui = new MagnifierUI();
        layer.setUI(ui);
        add(layer, "dock center");

        //listener for mouse movements
//        _pnlChart.addChartMouseListener(new ChartMouseListener() {
//            public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
//            }
//
//            public void chartMouseMoved(ChartMouseEvent cme) {
////                if (cme.getEntity() instanceof PlotEntity) //only inside plot
////                    System.out.println(cme.getTrigger().getX() + "   " + cme.getTrigger().getY() + "   " + cme.getEntity());
//            }
//        });
    }

    public void clear() {
        _Plot.clearAnnotations();
        _LeftDataSet.removeAllSeries();
        _RiteDataset.removeAllSeries();
        _Chart.setTitle("");
    }
    public void setTitle(String title) {
        _Chart.setTitle(title);
    }
    public void setLogScale(boolean log_scale) {
        if (log_scale)
            _Plot.setRangeAxis(_LogRangeAxis);
        else
            _Plot.setRangeAxis(_LinearRangeAxis);
    }

    public void plot(MarketInfo mki) throws Exception {
        //must read funds from file, otherwise memory will be used up
        clear();
        String symbol = mki.getSymbol();
        ArrayList<FundQuote> quotes = mki.getFund().getQuote();
//        float[] sma10 = mki.getSma10();
//        float[] sma30 = mki.getSma30();
        float[] sma50 = mki.getSma50();
        float[] sma200 = mki.getSma200();

        //first series: price, 2nd series: 10MA, 3rd series: lower envelope
        TimeSeries price_series = new TimeSeries(symbol);
        TimeSeries sma10_series = new TimeSeries("10 SMA");
        TimeSeries sma30_series = new TimeSeries("30 SMA");
        TimeSeries sma50_series = new TimeSeries("50 SMA");
        TimeSeries sma200_series = new TimeSeries("200 SMA");

//TODO add two more series for EPS and RS
        TimeSeries eps_series = new TimeSeries("EPS");
        TimeSeries rs_series = new TimeSeries("RS");
        for (int index = sma200.length - 1; index >= 0; index--) {//last first (Yahoo data)
            Calendar cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
            Day day = new Day(cal.getTime());
            price_series.add(day, quotes.get(index).getClose());
//            if (sma10[index] > 0)//skip cells w/o moving average
//                sma10_series.add(day, sma10[index]);
//            if (sma30[index] > 0)
//                sma30_series.add(day, sma30[index]);
            if (sma50[index] > 0)
                sma50_series.add(day, sma50[index]);
            if (sma200[index] > 0)
                sma200_series.add(day, sma200[index]);
        }

        //use 3 series if show envelope
        TimeSeries[] ps;
        ps = new TimeSeries[5];
        ps[0] = price_series;
//        ps[1] = sma10_series;
//        ps[2] = sma30_series;
        ps[1] = sma50_series;
        ps[2] = sma200_series;
        ps[3] = eps_series;
        ps[4] = rs_series;
        _LeftDataSet.removeAllSeries();
        for (TimeSeries s : ps)
            _LeftDataSet.addSeries(s);//internally it will plot right away

        //optionally draw annotation marks for IBD50 entry / exit dates
        _Plot.clearAnnotations();
        HashMap<String,ArrayList<IbdInfo>> ibd_map = MainModel.getInstance().getIbdInfoMap();
        ArrayList<IbdInfo> ibd_infos = ibd_map.get(symbol);//search all onlist and offlist dates
        for (IbdInfo ii : ibd_infos) {
            if (ii.getState().equals(Ibd50State.Onlist)) {
                String date = AppUtil.calendarToString(ii.getDate());
                FundQuote quote = mki.getFund().findQuoteByDate(date);
                XYPointerAnnotation anno = GraphUtil.createArrow(date, quote.getClose(), true);
                _Plot.addAnnotation(anno);
            }
            else if (ii.getState().equals(Ibd50State.Offlist)) {
                String date = AppUtil.calendarToString(ii.getDate());
                FundQuote quote = mki.getFund().findQuoteByDate(date);
                XYPointerAnnotation anno = GraphUtil.createArrow(date, quote.getClose(), false);
                _Plot.addAnnotation(anno);
            }
        }
    }

    //to plot additional EPS and RS lines, ibd_infos contains a list of records with those 2 values on particular dates
    public void plot(MarketInfo mki, ArrayList<IbdInfo> ibd_infos) throws Exception {
        clear();
        String symbol = mki.getSymbol();
        ArrayList<FundQuote> quotes = mki.getFund().getQuote();
        float[] sma50 = mki.getSma50();
        float[] sma200 = mki.getSma200();
        TimeSeries price_series = new TimeSeries(symbol);
        TimeSeries sma50_series = new TimeSeries("50 SMA");
        TimeSeries sma200_series = new TimeSeries("200 SMA");
        TimeSeries eps_series = new TimeSeries("EPS");
        TimeSeries rs_series = new TimeSeries("RS");

        //don't show too many points on screen, 1 year at most
        int num_points = sma200.length;
        if (num_points > 120) num_points = 120;
        for (int index = num_points - 1; index >= 0; index--) {//last first (Yahoo data)
            //search IbdInfo objects for matching date, ok if not found
            Calendar cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
            Day day = new Day(cal.getTime());
            for (IbdInfo ii : ibd_infos) {
                Calendar ii_date = ii.getDate();
                if (ii_date.equals(cal)) {
                    int eps = ii.getEps();
                    if (eps > 0) //skipping the gap
                        eps_series.add(day, eps);
                    int rs = ii.getRs();
                    if (rs > 0) //skipping the gap
                        rs_series.add(day, rs);
                }
            }
            price_series.add(day, quotes.get(index).getClose());
            if (sma50[index] > 0)
                sma50_series.add(day, sma50[index]);
            if (sma200[index] > 0)
                sma200_series.add(day, sma200[index]);
        }
        TimeSeries[] left_series = new TimeSeries[3];
        left_series[0] = price_series;
        left_series[1] = sma50_series;
        left_series[2] = sma200_series;
        TimeSeries[] rite_series = new TimeSeries[2];
        rite_series[0] = eps_series;
        rite_series[1] = rs_series;
        _LeftDataSet.removeAllSeries();
        for (TimeSeries s : left_series)
            _LeftDataSet.addSeries(s);//internally it will plot right away
        for (TimeSeries s : rite_series)
            _RiteDataset.addSeries(s);

        //optionally draw annotation marks for IBD50 entry / exit dates
        _Plot.clearAnnotations();
        String[] labels = new String[2]; labels[0] = Ibd50State.Onlist.toString(); labels[1] = Ibd50State.Offlist.toString();
        for (IbdInfo ii : ibd_infos) {
            if (ii.getState().equals(Ibd50State.Onlist)) {
                String date = AppUtil.calendarToString(ii.getDate());
                FundQuote quote = mki.getFund().findQuoteByDate(date);
                if (quote != null) {
                    XYPointerAnnotation anno = GraphUtil.createArrow(date, quote.getClose(), true, labels);
                    _Plot.addAnnotation(anno);
                }
            }
            else if (ii.getState().equals(Ibd50State.Offlist)) {
                String date = AppUtil.calendarToString(ii.getDate());
                FundQuote quote = mki.getFund().findQuoteByDate(date);
                if (quote != null) {
                    XYPointerAnnotation anno = GraphUtil.createArrow(date, quote.getClose(), false, labels);
                    _Plot.addAnnotation(anno);
                }
            }
        }
    }

    //-----instance variables-----
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    private JFreeChart _Chart;
    protected XYPlot _Plot;
    private TimeSeriesCollection _LeftDataSet = new TimeSeriesCollection();//left axis
    private TimeSeriesCollection _RiteDataset = new TimeSeriesCollection();//right axis
}
//TODO how to move y axis to the right and legend to left        _Plot.mapDatasetToRangeAxis(0, 0);
//use XY line annotations to show levels
//    public void drawLevels(ArrayList<StopLevel> stop_levels) {
//        //calculate full range y value, assume stop levels are sorted
//        double lvl_1 = stop_levels.get(0).getLevel();
//        double lvl_n = stop_levels.get(stop_levels.size() - 1).getLevel();
//        double range = lvl_1 - lvl_n;
//
//        //find most recent data point in time series as starting point of lines
//        TimeSeries ts = (TimeSeries)_DataSet.getSeries().get(0);
//        int end_idx = ts.getItemCount() - 1;
//        TimeSeriesDataItem data_item = ts.getDataItem(end_idx);//first item
//        RegularTimePeriod period = data_item.getFastPeriod();
//        long x_begin = period.getFirstMillisecond();
//
//        //last one
//        int length = ts.getItemCount();
//        long x_end;
//
//        //plot annotations (horizontal lines and text)
//        _Plot.clearAnnotations();
//        int index = 0;  float stroke_width = 1.0f;
//        XYLineAnnotation[] levels = new XYLineAnnotation[stop_levels.size()];
//        XYTextAnnotation[] labels = new XYTextAnnotation[stop_levels.size()];
//        for (StopLevel lvl : stop_levels) {
//            String id = lvl.getId();
//            Color c = Color.lightGray;//default
//            if (StopLevelInfo.isAtrMethod(id)) {
//                c = Color.magenta;
//                end_idx = length / 4;
//                stroke_width = 0.5f;
//            }
//            else if (StopLevelInfo.isSwpMethod(id)) {
//                c = Color.green.darker();
//                end_idx = length / 2;
//                stroke_width = 0.8f;
//            }
//            else if (StopLevelInfo.isPercentMethod(id)) {
//                if (StopLevelInfo.isBreakEven(id)) {//break even is part of percent method
//                    c = Color.cyan.darker();
//                    end_idx = length / 12;
//                    stroke_width = 1.4f;
//                }
//                else {
//                    c = Color.blue;
//                    end_idx = length * 2 / 3;
//                    stroke_width = 1.2f;
//                }
//            }
//            else if (StopLevelInfo.isCurrentStop(id)) {
//                c = Color.red.darker();
//                end_idx = length / 10;
//                stroke_width = 1.7f;
//            }
//            else {//cost
//                end_idx = length / 10;
//            }
//            data_item = ts.getDataItem(end_idx);
//            period = data_item.getFastPeriod();
//            x_end = period.getLastMillisecond();//period.getFirstMillisecond();TODO no difference
//            levels[index] = new XYLineAnnotation(x_end, lvl.getLevel(), x_begin, lvl.getLevel(),
//                new BasicStroke(stroke_width), c);
//            if (id.equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]))
//                id = "Last Close";//borrow this to show close
//
//            //adjust y coordinates by 1/150th of full range such that text doesn't cross line
//            float lbl_y = (float)(lvl.getLevel()/* - range / 150*/);
//            labels[index] = new XYTextAnnotation(id, x_end, lbl_y);
//            _Plot.addAnnotation(levels[index]);
//            _Plot.addAnnotation(labels[index]);
//            index++;
//        }
//    }
