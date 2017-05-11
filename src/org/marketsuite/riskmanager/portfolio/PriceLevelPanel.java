package org.marketsuite.riskmanager.portfolio;

import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.GraphUtil;
import org.marketsuite.resource.ApolloConstants;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

//container for analysis _Chart using JFreeChart
// use a DataSet to represent underline data
public class PriceLevelPanel extends AbstractGraphPanel {
    public PriceLevelPanel() {
        setLayout(new BorderLayout());

        //north - icon bar for chart options
//        JPanel west_pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));  west_pnl.setOpaque(false);
//        JPanel east_pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));  east_pnl.setOpaque(false);
//        JPanel tool_pnl = new JPanel(new BorderLayout()); tool_pnl.setOpaque(false);
//        west_pnl.add(_btnATRTrail);
//        west_pnl.add(_spnATRMultiple);
//        tool_pnl.add(west_pnl, BorderLayout.WEST);
//
//        //east tool buttons
//        east_pnl.add(_btnCursor);
//        tool_pnl.add(east_pnl, BorderLayout.EAST);
//        _btnCursor.setDisabledIcon(new DisabledIcon(FrameworkIcon.CURSOR.getImage()));
//        _btnCursor.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent actionEvent) {
//                toggleCursor();
//            }
//        });
//        add(tool_pnl, BorderLayout.NORTH);

        //center - chart
        _Chart = ChartFactory.createTimeSeriesChart(
            "", // title
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
            renderer.setSeriesStroke(0, new BasicStroke(1.2f));
            renderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
            renderer.setSeriesStroke(1, new BasicStroke(0.7f));
            renderer.setSeriesPaint(1, Color.red.darker());//main data, blue/green
        }

        //axis
        _LinearRangeAxis = _Plot.getRangeAxis();//save default one for later
        _LogRangeAxis = new LogarithmicAxis("");

        //create two empty data sets
        _pnlChart = new ChartPanel(_Chart);
//        _pnlChart.setRangeZoomable(false);
        _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out
//        _pnlChart.setHorizontalAxisTrace(true);//cross hair cursor
//        _pnlChart.setVerticalAxisTrace(true);
        add(_pnlChart, BorderLayout.CENTER);

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

    public void clear() {
        _DataSet.removeAllSeries();
        _Chart.setTitle("");
    }

    public void setTitle(String title) {
        _Chart.setTitle(title);
    }

    //use XY line annotations to show levels
    public void drawLevels(ArrayList<StopLevel> stop_levels) {
        //calculate full range y value, assume stop levels are sorted
        double lvl_1 = stop_levels.get(0).getLevel();
        double lvl_n = stop_levels.get(stop_levels.size() - 1).getLevel();
        double range = lvl_1 - lvl_n;

        //find most recent data point in time series as starting point of lines
        TimeSeries ts = (TimeSeries)_DataSet.getSeries().get(0);
        int end_idx = ts.getItemCount() - 1;
        TimeSeriesDataItem data_item = ts.getDataItem(end_idx);//first item
        RegularTimePeriod period = data_item.getPeriod();
        long x_begin = period.getFirstMillisecond();

        //last one
        int length = ts.getItemCount();
        long x_end;

        //plot annotations (horizontal lines and text)
        _Plot.clearAnnotations();
        int index = 0;  float stroke_width = 1.0f;
        XYLineAnnotation[] levels = new XYLineAnnotation[stop_levels.size()];
        XYTextAnnotation[] labels = new XYTextAnnotation[stop_levels.size()];
        for (StopLevel lvl : stop_levels) {
            String id = lvl.getId();
            Color c = Color.lightGray;//default
            if (StopLevelInfo.isAtrMethod(id)) {
                c = Color.magenta;
                end_idx = length / 4;
                stroke_width = 0.5f;
            }
            else if (StopLevelInfo.isSwpMethod(id)) {
                c = Color.green.darker();
                end_idx = length / 2;
                stroke_width = 0.8f;
            }
            else if (StopLevelInfo.isPercentMethod(id)) {
                if (StopLevelInfo.isBreakEven(id)) {//break even is part of percent method
                    c = Color.cyan.darker();
                    end_idx = length / 12;
                    stroke_width = 1.4f;
                }
                else {
                    c = Color.blue;
                    end_idx = length * 2 / 3;
                    stroke_width = 1.2f;
                }
            }
            else if (StopLevelInfo.isCurrentStop(id)) {
                c = Color.red.darker();
                end_idx = length / 10;
                stroke_width = 1.7f;
            }
            else {//cost
                end_idx = length / 10;
            }
            data_item = ts.getDataItem(end_idx);
            period = data_item.getPeriod();
            x_end = period.getLastMillisecond();//period.getFirstMillisecond();TODO no difference
            levels[index] = new XYLineAnnotation(x_end, lvl.getLevel(), x_begin, lvl.getLevel(),
                new BasicStroke(stroke_width), c);
            if (id.equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]))
                id = "Last Close";//borrow this to show close

            //adjust y coordinates by 1/150th of full range such that text doesn't cross line
            float lbl_y = (float)(lvl.getLevel()/* - range / 150*/);
            labels[index] = new XYTextAnnotation(id, x_end, lbl_y);
            _Plot.addAnnotation(levels[index]);
            _Plot.addAnnotation(labels[index]);
            index++;
        }
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

    //-----instance variables-----
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    private JFreeChart _Chart;
    protected XYPlot _Plot;
    private TimeSeriesCollection _DataSet;
    private DefaultHighLowDataset _CandleDataSet;
    private boolean _bCandleChart;// = true;
    private JButton _btnATRTrail = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_12"), FrameworkIcon.PRICE_CHART);
    private JSpinner _spnATRMultiple = new JSpinner();
    private JButton _btnCursor = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_9"), FrameworkIcon.CURSOR);
}