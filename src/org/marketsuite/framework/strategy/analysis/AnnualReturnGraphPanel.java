package org.marketsuite.framework.strategy.analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;

/**
 * A container for holding various types of bar charts
 */
public class AnnualReturnGraphPanel extends JPanel {
    /**
     * CTOR: create time series based bar chart displaying percentage over time
     */
    public AnnualReturnGraphPanel() {//todo: pass title
        setLayout(new BorderLayout());
        _Chart = ChartFactory.createXYBarChart(
            "",
            "", //x axis
            true,
            "%",
            _DataSet = new TimeSeriesCollection(),
            PlotOrientation.VERTICAL,
            true,
            true, //tooltip
            false
        );
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, Color.lightGray, 500, 500, new Color(250, 250, 250)));
        //special renderer
        ClusteredXYBarRenderer renderer = new ClusteredXYBarRenderer(0.05, true);
//        XYBarRenderer renderer = new XYBarRenderer(0.05); for single data series only
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new Color(0, 100, 50));//light blue,green mix
        renderer.setSeriesPaint(1, new Color(200, 100, 0));//light blue,green mix
//        XYBarPainter bp = renderer.getBarPainter();
//        renderer.setSeriesPaint(0, Color.blue);//default 0 use lighter blue
//        renderer.setSeriesPaint(1, Color.red);//default 1 use lighter red

//        renderer.setAutoPopulateSeriesPaint(false);//w/o this, both series become blue
        _Plot.setRenderer(renderer);
        ChartPanel pnl = new ChartPanel(_Chart);
        pnl.setRangeZoomable(false);
        pnl.setDomainZoomable(false);
        pnl.setMouseWheelEnabled(true);//wheel zoom in/out
        add(pnl, BorderLayout.CENTER);
    }

    public void updateGraph(TimeSeries series, TimeSeries sp500) {
        _DataSet.removeAllSeries();
        if (!series.isEmpty())
            _DataSet.addSeries(series);
        _DataSet.addSeries(sp500);
        _Chart.fireChartChanged();
    }

    public void clear() {
        _DataSet.removeAllSeries();
    }

    //----- instance variables -----
    protected JFreeChart _Chart;
    protected XYPlot _Plot;
    protected TimeSeriesCollection _DataSet;
}
