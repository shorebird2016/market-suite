package org.marketsuite.framework.strategy.analysis;

import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.awt.*;
import java.text.SimpleDateFormat;

//container for analysis chart using JFreeChart
// use a DataSet to represent underline data
public class EquityGraphPanel extends AbstractGraphPanel {
    public EquityGraphPanel() {
        setLayout(new BorderLayout() );
        //center - jFreeChart of equity over time
        _Chart = ChartFactory.createTimeSeriesChart(
            "", // title
            "", // x-axis label
            "", // y-axis label
            _DataSet = new TimeSeriesCollection(),//empty by default
            true,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );
        //entire chart - default background paint is white
        //  this changes all like axis and labels except plot itself
//        _Chart.setBackgroundPaint(new GradientPaint(0, 0, Color.cyan, 500, 500, Color.blue));
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot
        //change plot attributes - inside plot area
        //  background paint - default gray
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, Color.lightGray, 500, 500, new Color(250, 250, 250)));
//        _Plot.setBackgroundPaint(new Color(222, 222, 222));//boring light grey
        XYItemRenderer r = _Plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            _Renderer = (XYLineAndShapeRenderer) r;
            _Renderer.setBaseShapesVisible(false);//default false, set true show square data point
            _Renderer.setBaseShapesFilled(false);
            _Renderer.setDrawSeriesLineAsPath(false);
            _Renderer.setSeriesPaint(0, new Color(0, 100, 50));//light blue,green mix
            _Renderer.setSeriesStroke(0, new BasicStroke(1F));//1.5 slightly thick
            _Renderer.setSeriesPaint(1, new Color(200, 100, 50));
            _Renderer.setSeriesStroke(1, new BasicStroke(1));
            _Renderer.setBaseToolTipGenerator(_TipGen);
        }
        _LinearRangeAxis = _Plot.getRangeAxis();//save default one for later

        //make vertical axis log based
        LogarithmicAxis[] axises = new LogarithmicAxis[1];
        axises[0] = new LogarithmicAxis("");
        _LogRangeAxis = axises[0];
//        TickUnitSource units = NumberAxis.createIntegerTickUnits();
//        _LogRangeAxisMontly.setStandardTickUnits(units);
        axises[0].setAllowNegativesFlag(true);
        _Plot.setRangeAxes(axises);
        _pnlChart = new ChartPanel(_Chart);
        _pnlChart.setRangeZoomable(false);
        _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out
//        _pnlChart.setHorizontalAxisTrace(true);//cross hair cursor
//        _pnlChart.setVerticalAxisTrace(true);
        add(_pnlChart, BorderLayout.CENTER);
    }

    //update Equity _Chart data and repaint, data_series maybe null only showing sp500
    public void plotEquitySeries(TimeSeries data_series, TimeSeries sp_series) {
        _DataSet.removeAllSeries();
        if (data_series != null)
            _DataSet.addSeries(data_series);
        if (sp_series != null)
            _DataSet.addSeries(sp_series);
        _Chart.fireChartChanged();
    }

    public JFreeChart getChart() {
        return _Chart;
    }

    public void setLogScale(boolean log_scale) {
        if (log_scale)
            _Plot.setRangeAxis(_LogRangeAxis);
        else
            _Plot.setRangeAxis(_LinearRangeAxis);
    }

    public void clear() {
        _DataSet.removeAllSeries();
    }

    //instance variables
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    protected XYLineAndShapeRenderer _Renderer;
    protected JFreeChart _Chart;
    protected XYPlot _Plot;
    protected TimeSeriesCollection _DataSet;
    protected StandardXYToolTipGenerator _TipGen = new StandardXYToolTipGenerator("[{1}]  {2}",
        new SimpleDateFormat("MM/dd/yyyy"), FrameworkConstants.DOLLAR_FORMAT);
}
