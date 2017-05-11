package org.marketsuite.framework.strategy.analysis;

import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.GraphUtil;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.marketsuite.framework.model.Transaction;

import java.awt.*;
import java.util.ArrayList;

//container for analysis _Chart using JFreeChart
// use a DataSet to represent underline data
public class PriceGraphPanel extends AbstractGraphPanel {
    public PriceGraphPanel() {
        setLayout(new BorderLayout() );

        //create a fake high low dataset initially for candle stick chart
//        int len = 2;
//        Date[] date = new Date[len];
//            Calendar cal = Calendar.getInstance();
//            cal.set(2011, 11, 8); date[0] = cal.getTime();
//            cal.set(2011, 10, 22); date[1] = cal.getTime();
//        double[] high = new double[len];   high[0] = 5; high[1] = 10;
//        double[] low = new double[len];    low[0] = 0; low[1] = 3;
//        double[] open = new double[len];   open[0] = 2; open[1] = 9;
//        double[] close = new double[len];  close[0] = 4; close[1] = 6;
//        double[] volume = new double[len]; volume[0] = 245000; volume[1] = 3670004;
//        _CandleDataSet = new DefaultHighLowDataset("", date, high, low, open, close, volume);
//        _Chart = ChartFactory.createCandlestickChart(
//            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_1"), // title
//            "",
//            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_2"), // y-axis label
//            _CandleDataSet,
//            true //show legend
//        );

//regular line chart
        _Chart = ChartFactory.createTimeSeriesChart(
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_1"), // title
            "", // x-axis label
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_2"), // y-axis label
            _DataSet = new TimeSeriesCollection(),
            true,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );

        //change plot attributes - inside plot area
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, Color.lightGray, 500, 500, new Color(250, 250, 250)));
        XYItemRenderer r = _Plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);//default false, set true show square data point
            renderer.setBaseShapesFilled(false);
            renderer.setDrawSeriesLineAsPath(false);
            renderer.setSeriesStroke(0, new BasicStroke(1f));
            renderer.setSeriesPaint(0, Color.gray);//main data
//            renderer.setSeriesPaint(1, new Color(120, 0, 100));//fast MA
        }

        //axis
        _LinearRangeAxis = _Plot.getRangeAxis();//save default one for later
        _LogRangeAxis = new LogarithmicAxis("");

        //create two empty data sets
        _pnlChart = new ChartPanel(_Chart);
//        _pnlChart.setRangeZoomable(false);
        _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out
        _pnlChart.setHorizontalAxisTrace(true);//cross hair cursor
        _pnlChart.setVerticalAxisTrace(true);
        add(_pnlChart);

//        JXLayer layer = new JXLayer(_pnlChart);
//        MagnifierUI ui = new MagnifierUI();
//        layer.setUI(ui);
//        add(layer);

        _pnlChart.addChartMouseListener(new ChartMouseListener() {
            public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
            }

            public void chartMouseMoved(ChartMouseEvent cme) {
//                if (cme.getEntity() instanceof PlotEntity) //only inside plot
//                    System.out.println(cme.getTrigger().getX() + "   " + cme.getTrigger().getY() + "   " + cme.getEntity());
            }
        });
    }

    //update Equity _Chart data and repaint
    public void addSeries(TimeSeries[] serieses) {
        _DataSet.removeAllSeries();
        for (TimeSeries s : serieses)
            _DataSet.addSeries(s);
        _Chart.fireChartChanged();
    }

    //mark entry/exit points via annotation on chart
    public void drawEntryExits(ArrayList<Transaction> trans) {
        _Plot.clearAnnotations();
        XYPointerAnnotation[] entry_arrows = new XYPointerAnnotation[trans.size()];
        XYPointerAnnotation[] exit_arrows = new XYPointerAnnotation[trans.size()];
        int index = 0;
        for (Transaction tr : trans) {
            entry_arrows[index] = GraphUtil.createArrow(tr.getEntryDate(), tr.getEntryPrice(), true);
            _Plot.addAnnotation(entry_arrows[index]);
            exit_arrows[index] = GraphUtil.createArrow(tr.getExitDate(), tr.getExitPrice(), false);
            _Plot.addAnnotation(exit_arrows[index]);
            index++;
        }
    }

    public void setLogScale(boolean log_scale) {
        if (log_scale)
            _Plot.setRangeAxis(_LogRangeAxis);
        else
            _Plot.setRangeAxis(_LinearRangeAxis);
    }

    private TimeSeriesCollection createEmptyTimeSeries() {
        _DataSet = new TimeSeriesCollection();
        return _DataSet;
    }

    public void drawCandleChart(DefaultHighLowDataset data_set) {
//        _CandleDataSet = data_set;
        _Chart = ChartFactory.createCandlestickChart(
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_1"), // title
            "",
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_2"), // y-axis label
            data_set,
            true //show legend
        );
        _pnlChart.setChart(_Chart);
        _pnlChart.repaint();
    }

    public JFreeChart getChart() {
        return _Chart;
    }

    public boolean isCandleChart() {
        return _bCandleChart;
    }

    public void clear() {
        _DataSet.removeAllSeries();
    }

    //-----instance variables-----
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    private JFreeChart _Chart;
    protected XYPlot _Plot;
    private TimeSeriesCollection _DataSet;
    private DefaultHighLowDataset _CandleDataSet;
    private boolean _bCandleChart;// = true;
//    private ChartPanel _pnlChart;
}