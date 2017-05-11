package org.marketsuite.framework.strategy.base;

import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.GraphUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

//container for analysis _Chart using JFreeChart
// use a DataSet to represent underline data
public class PriceGraphPanel extends JPanel {
    public PriceGraphPanel() {
        setLayout(new BorderLayout() );
        _Chart = ChartFactory.createTimeSeriesChart(
            "", // title
            "", // x-axis label
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_ch_3"), // y-axis label
            _DataSet = new TimeSeriesCollection(),
            false,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );
        //entire chart - default background paint is white
        //  this changes all like axis and labels except plot itself
//        _Chart.setBackgroundPaint(new GradientPaint(0, 0, Color.cyan, 500, 500, Color.blue));
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, Color.lightGray, 500, 500, new Color(250, 250, 250)));
        //change plot attributes - inside plot area
        //  background paint - default gray
//        plot.setBackgroundPaint(new GradientPaint(0, 0, Color.cyan, 500, 500, Color.blue));
//        plot.setBackgroundPaint(new Color(220, 220, 220));
        XYItemRenderer r = _Plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);//default false, set true show square data point
            renderer.setBaseShapesFilled(false);
            renderer.setDrawSeriesLineAsPath(false);
            renderer.setSeriesStroke(0, new BasicStroke(1.02f));
            renderer.setSeriesPaint(0, Color.blue);//new Color(0, 120, 200));//main data
            renderer.setSeriesPaint(1, new Color(120, 0, 100));//fast MA
            renderer.setSeriesPaint(2, new Color(0, 120, 120));//medium MA
            renderer.setSeriesPaint(3, new Color(150, 130, 0));//slow MA
        }

        //make vertical axis log based
        LogarithmicAxis[] axises = new LogarithmicAxis[1];
        axises[0] = new LogarithmicAxis("Equity");
        axises[0].setAllowNegativesFlag(true);
        _Plot.setRangeAxes(axises);

        //create two empty data sets
        ChartPanel pnl = new ChartPanel(_Chart);
        pnl.setMouseWheelEnabled(true);
//        pnl.setRangeZoomable(false);
        add(pnl);
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
//        _Chart = ChartFactory.createCandlestickChart(
//            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_1"), // title
//            "",
//            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_2"), // y-axis label
//            data_set,
//            true //show legend
//        );
//        _pnlChart.setChart(_Chart);
//        _pnlChart.repaint();
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
