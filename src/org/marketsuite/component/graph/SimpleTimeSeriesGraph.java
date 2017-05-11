package org.marketsuite.component.graph;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
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
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A simple/easy to use undecorated graph built from basic JFreeChart to draw 1 or more time based data set.
 */
public class SimpleTimeSeriesGraph extends JPanel {
    //----- CTOR -----
    /**
     * CTOR: create graph container with one or more data series sharing same horizontal time axis.
     * @param x_label horizontal label
     * @param y0_label primary vertical axis label
     * @param y1_label secondary axis vertical label, null = don't show right axis/no legend/no tooltip
     */
    public SimpleTimeSeriesGraph(String x_label, String y0_label, String y1_label) {
        setLayout(new MigLayout("insets 0"));
        boolean dual_axis = y1_label != null;
        JFreeChart jfc = ChartFactory.createTimeSeriesChart("", x_label, y0_label, _Dataset0, dual_axis, dual_axis, false);
        _Plot = jfc.getXYPlot();//time series is a type of XY plot
        _Plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        if (dual_axis) {
            jfc.getLegend().setPosition(RectangleEdge.TOP);
            _Plot.setDataset(1, _Dataset1);//supplement

            //decorate axis
            NumberAxis axis1 = new NumberAxis(y1_label);
            axis1.setAutoRangeIncludesZero(false);
            _Plot.setRangeAxis(1, axis1);
            _Plot.mapDatasetToRangeAxis(1, 1);
        }
        _Plot.setBackgroundPaint(WidgetUtil.PAINT_BACKGROUND_LIGHT_GREEN);

        //renderers
        StandardXYToolTipGenerator tip_gen = new StandardXYToolTipGenerator("{0}: {1}  {2}",//{0}symbol {1}date {2}vale
            new SimpleDateFormat("MM/dd"), FrameworkConstants.FORMAT_NUMBERS);
        XYItemRenderer rdr1 = _Plot.getRenderer();
        if (dual_axis)
            rdr1.setBaseToolTipGenerator(tip_gen);
        rdr1.setSeriesPaint(0, new Color(0, 100, 50));//dark green
        rdr1.setSeriesStroke(0, new BasicStroke(1));
        _Renderer2 = new XYLineAndShapeRenderer();
        _Renderer2.setBaseShapesVisible(false);
        if (dual_axis)
            _Renderer2.setBaseToolTipGenerator(tip_gen);
        _Plot.setRenderer(1, _Renderer2);
        _Renderer2.setSeriesPaint(0, new Color(200, 100, 250));//pink
        _Renderer2.setSeriesStroke(0, new BasicStroke(1));
        add(_pnlChart = new ChartPanel(jfc), "dock center");

        //setup two types of axis
        _LinearRangeAxis = _Plot.getRangeAxis();//save default one for later
        LogarithmicAxis[] axises = new LogarithmicAxis[1];
        axises[0] = new LogarithmicAxis("");
        _LogRangeAxis = axises[0];
        _LogRangeAxis.setLabel(y0_label);
    }

    //----- methods -----
    //plot 1 line
    public void plot(String name, Calendar[] time, double[] data) {
        symbol = name;//save last one
        TimeSeries series = new TimeSeries(name);
        for (int i = 0; i < time.length; i++) {
            Day day = new Day(time[i].getTime());
            try {
                series.add(day, data[i]);
            } catch (Exception e) {//somehow it gets this sometimes but nothing wrong
                System.err.println("------> [" + i +"] " + day.getYear() + ":" + day.getMonth());
            }
        }
        _Dataset0.removeAllSeries();
        _Dataset0.addSeries(series);
    }
    //plot 2 lines, 1 axis
    public void plot(String name, Calendar[] time, double[] data, double[] data1) {
        symbol = name;//save last one
        _Plot.setDataset(1, _Dataset1);//supplement
        TimeSeries series = new TimeSeries(name);
        TimeSeries series1 = new TimeSeries("");
        for (int i = 0; i < time.length; i++) {
            Day day = new Day(time[i].getTime());
            series.add(day, data[i]);//TODO sometimes get duplicate quote exception
            double v = data1[i];
            if (v > 0)//avoid drawing 0s
                series1.add(day, data1[i]);
        }
        _Dataset0.removeAllSeries();
        _Dataset0.addSeries(series);
        _Dataset1.removeAllSeries();
        _Dataset1.addSeries(series1);
    }
    //as an example, first data set price, 2nd data set velocity
    public void plot(String[] names, Calendar[] time, double[] dataset0, double[] dataset1) {
        _Dataset0.removeAllSeries();
        TimeSeries series = new TimeSeries(names[0]);
        for (int i = 0; i < time.length; i++) {
            Day day = new Day(time[i].getTime());
            series.add(day, dataset0[i]);
        }
        _Dataset0.addSeries(series);
        _Dataset1.removeAllSeries();
        series = new TimeSeries(names[1]);
        for (int i = 0; i < time.length; i++) {
            Day day = new Day(time[i].getTime());
            series.add(day, dataset1[i]);
        }
        _Dataset1.addSeries(series);
    }
    public void emphasizeThumbnail(boolean emphasize) {
        _Plot.setBackgroundPaint(emphasize ? WidgetUtil.PAINT_BACKGROUND_LIGHT_YELLOW : WidgetUtil.PAINT_BACKGROUND_LIGHT_GREEN);
    }
    public void setLogScale(boolean log_scale) {
        if (log_scale)
            _Plot.setRangeAxis(_LogRangeAxis);
        else
            _Plot.setRangeAxis(_LinearRangeAxis);
    }
    public void plotPoints() {
        TimeSeries ts = (TimeSeries)_Dataset0.getSeries().get(0);
    }
    public String getSymbol() { return symbol; }

    //----- variables -----
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    private TimeSeriesCollection _Dataset0 = new TimeSeriesCollection();//main
    private TimeSeriesCollection _Dataset1 = new TimeSeriesCollection();//supplement
    private ChartPanel _pnlChart;
    private XYPlot _Plot;
    private XYLineAndShapeRenderer _Renderer2;
    private String symbol;
}