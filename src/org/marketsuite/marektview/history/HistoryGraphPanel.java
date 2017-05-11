package org.marketsuite.marektview.history;

import org.marketsuite.component.UI.MagnifierUI;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.GraphUtil;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
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
import org.marketsuite.component.UI.MagnifierUI;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple/easy to use undecorated graph built from basic JFreeChart to draw 1 or more time based data set.
 */
public class HistoryGraphPanel extends JTabbedPane {
    /**
     * CTOR: create 2 tab graph container with one or more data series sharing same horizontal time axis.
     *       first tab for yearly data, 2nd tab for monthly data
     * @param x_label horizontal label
     * @param y0_label primary vertical axis label
     * @param y1_label secondary axis vertical label, null = don't show right axis/no legend/no tooltip
     */
    public HistoryGraphPanel(String x_label, String y0_label, String y1_label) {
        //common for both plots
        Color edge_color1 = new Color(255, 255, 0xFF);
        Color edge_color2 = new Color(207, 232, 0xFF);
        Color series1paint = new Color(21, 109, 166, 213);
        StandardXYToolTipGenerator tipy_gen = new StandardXYToolTipGenerator("{1}  {2}",//{0}symbol {1}date {2}vale
            new SimpleDateFormat("yyyy"), FrameworkConstants.FORMAT_NUMBERS);//MMM-yyyy

        //first tab - yearly chart, single vertical axis
        JFreeChart jfc_yearly = ChartFactory.createTimeSeriesChart("", x_label, y0_label, _DatasetYearly0, false, false, false);
        _PlotYearly = jfc_yearly.getXYPlot();//time series is a type of XY plot
        _PlotYearly.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _PlotYearly.setBackgroundPaint(new GradientPaint(0, 0, edge_color2, 500, 500, edge_color1));
        _RendererYearly = (XYLineAndShapeRenderer)_PlotYearly.getRenderer();
        _RendererYearly.setBaseToolTipGenerator(tipy_gen);
        _RendererYearly.setSeriesPaint(0, series1paint);//dark blue
        _RendererYearly.setSeriesStroke(0, new BasicStroke(1.8F));//this affects axis label
        _RendererYearly.setSeriesShapesFilled(0, false);//price shows shape but not filled
        _RendererYearly.setSeriesShapesVisible(0, false);

        _LinearRangeAxisYearly = _PlotYearly.getRangeAxis();//save default one for later
        LogarithmicAxis[] yearly_axises = new LogarithmicAxis[1];
        yearly_axises[0] = new LogarithmicAxis("");
        _LogRangeAxisYearly = yearly_axises[0];
        _PlotYearly.setRangeAxis(_LogRangeAxisYearly);
        _pnlYearlyChart = new ChartPanel(jfc_yearly);
        _LogRangeAxisYearly.setLabel(y0_label);
        addTab("Yearly Chart", _pnlYearlyChart);

        //2nd tab - monthly chart
        JPanel monthly_pnl = new JPanel(new MigLayout("insets 0"));
        boolean dual_axis = y1_label != null;
        JFreeChart jfc = ChartFactory.createTimeSeriesChart("", x_label, y0_label, _DatasetMonthly0, dual_axis, dual_axis, false);
        _PlotMonthly = jfc.getXYPlot();//time series is a type of XY plot
        _PlotMonthly.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        if (dual_axis) {//setup 2nd axis
            jfc.getLegend().setPosition(RectangleEdge.TOP);
            _PlotMonthly.setDataset(1, _DatasetMonthly1);//supplement

            //decorate axis
            NumberAxis axis1 = new NumberAxis(y1_label);
            axis1.setAutoRangeIncludesZero(false);
            _PlotMonthly.setRangeAxis(1, axis1);
            _PlotMonthly.mapDatasetToRangeAxis(1, 1);
        }

        //renderers
        StandardXYToolTipGenerator tipm_gen = new StandardXYToolTipGenerator("{1}  {2}",//{0}symbol {1}date {2}vale
                new SimpleDateFormat("MMM-yyyy"), FrameworkConstants.FORMAT_NUMBERS);
        _PlotMonthly.setBackgroundPaint(new GradientPaint(0, 0, edge_color2, 500, 500, edge_color1));
        XYItemRenderer rdr1 = _PlotMonthly.getRenderer();
        rdr1.setBaseToolTipGenerator(tipm_gen);
        rdr1.setSeriesPaint(0, series1paint);//dark blue
        rdr1.setSeriesStroke(0, new BasicStroke(1.2F));
        _RendererMonthly = new XYLineAndShapeRenderer();
        _RendererMonthly.setBaseShapesVisible(false);
        if (dual_axis)
            _RendererMonthly.setBaseToolTipGenerator(tipm_gen);
        _PlotMonthly.setRenderer(1, _RendererMonthly);
        _RendererMonthly.setSeriesPaint(0, new Color(200, 100, 250));//pink
        _RendererMonthly.setSeriesStroke(0, new BasicStroke(1));

        //add magnifier layer
        JXLayer layer = new JXLayer(_pnlMonthlyChart = new ChartPanel(jfc));
        MagnifierUI ui = new MagnifierUI();
        layer.setUI(ui); monthly_pnl.add(layer, "dock center");
        addTab("Monthly Chart", layer/*_pnlYearlyChart = new ChartPanel(jfc)*/);//TODO maybe no layer????

        //setup two types of axis
        _LinearRangeAxisMonthly = _PlotMonthly.getRangeAxis();//save default one for later
        LogarithmicAxis[] axises = new LogarithmicAxis[1];
        axises[0] = new LogarithmicAxis("");
        _LogRangeAxisMonthly = axises[0];
        _LogRangeAxisMonthly.setLabel(y0_label);
    }

    //----- public/protected methods -----
    //plot 1 line
    void plot(boolean yearly, String name, Calendar[] time, double[] data) {
        TimeSeries series = new TimeSeries(name);
        for (int i = 0; i < time.length; i++) {
            Day day = new Day(time[i].getTime());
            try {
                series.add(day, data[i]);
            } catch (Exception e) {//somehow it gets this sometimes but nothing wrong
System.err.println("----STRANGE DUP DATES --> [" + i +"] " + day.getYear() + ":" + day.getMonth());
            }
        }
        if (yearly) {
            _DatasetYearly0.removeAllSeries();
            _DatasetYearly0.addSeries(series);
        }
        else {
            _DatasetMonthly0.removeAllSeries();
            _DatasetMonthly0.addSeries(series);
        }
    }
    //plot 2 lines, 1 axis
    void plot(String name, Calendar[] time, double[] data, double[] data1) {
        _PlotMonthly.setDataset(1, _DatasetMonthly1);//supplement
        TimeSeries series = new TimeSeries(name);
        TimeSeries series1 = new TimeSeries("");
        for (int i = 0; i < time.length; i++) {
            Day day = new Day(time[i].getTime());
            series.add(day, data[i]);//TODO sometimes get duplicate quote exception
            double v = data1[i];
            if (v > 0)//avoid drawing 0s
                series1.add(day, data1[i]);
        }
        _DatasetMonthly0.removeAllSeries();
        _DatasetMonthly0.addSeries(series);
        _DatasetMonthly1.removeAllSeries();
        _DatasetMonthly1.addSeries(series1);
    }
    //as an example, first data set price, 2nd data set velocity
    void plot(String[] names, Calendar[] time, double[] dataset0, double[] dataset1) {
        _DatasetMonthly0.removeAllSeries();
        TimeSeries series = new TimeSeries(names[0]);
        for (int i = 0; i < time.length; i++) {
            Day day = new Day(time[i].getTime());
            series.add(day, dataset0[i]);
        }
        _DatasetMonthly0.addSeries(series);
        _DatasetMonthly1.removeAllSeries();
        series = new TimeSeries(names[1]);
        for (int i = 0; i < time.length; i++) {
            Day day = new Day(time[i].getTime());
            series.add(day, dataset1[i]);
        }
        _DatasetMonthly1.addSeries(series);

        //draw line annotation
        TimeSeries ts = (TimeSeries) _DatasetMonthly0.getSeries().get(0);
        int end_idx = ts.getItemCount() - 1;
        long x_begin = ts.getDataItem(0).getPeriod().getFirstMillisecond();
        long x_end = ts.getDataItem(end_idx).getPeriod().getLastMillisecond();
        XYLineAnnotation annotation = new XYLineAnnotation(x_end, 0, x_begin, 0, new BasicStroke(1), Color.gray);
        _RendererMonthly.addAnnotation(annotation);
    }
    void setLogScale(boolean log_scale) {
        if (log_scale)
            _PlotMonthly.setRangeAxis(_LogRangeAxisMonthly);
        else
            _PlotMonthly.setRangeAxis(_LinearRangeAxisMonthly);
    }
    void plotAnnotations(ArrayList<HistoricalRecord> records) {
        _PlotYearly.clearAnnotations(); _PlotMonthly.clearAnnotations();
        for (HistoricalRecord hr : records) {
            XYPointerAnnotation pnt_an = GraphUtil.createTip(AppUtil.calendarToString(hr.getCalendar()), hr.getPrice(), true);
            _PlotYearly.addAnnotation(pnt_an);
            _PlotMonthly.addAnnotation(pnt_an);
        }
    }
    void clear() { _PlotYearly.clearAnnotations(); }

    //----- variables -----
    protected ValueAxis _LinearRangeAxisYearly, _LinearRangeAxisMonthly;
    protected ValueAxis _LogRangeAxisYearly, _LogRangeAxisMonthly;
    private TimeSeriesCollection _DatasetYearly0 = new TimeSeriesCollection();//main
    private TimeSeriesCollection _DatasetMonthly0 = new TimeSeriesCollection();//main
    private TimeSeriesCollection _DatasetMonthly1 = new TimeSeriesCollection();//supplement
    private ChartPanel _pnlYearlyChart, _pnlMonthlyChart;
    private XYPlot _PlotYearly, _PlotMonthly;
    private XYLineAndShapeRenderer _RendererYearly, _RendererMonthly;
}