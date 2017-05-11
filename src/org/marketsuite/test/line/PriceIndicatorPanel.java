package org.marketsuite.test.line;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Calendar;

/**
 * A Combined domain xy plot with price on top and indicator below, both share the same time axis.
 */
public class PriceIndicatorPanel extends JPanel {
    public PriceIndicatorPanel() {
        setLayout(new BorderLayout());

        //two strip of charts - price chart on top
        CombinedDomainXYPlot combined_plot = new CombinedDomainXYPlot();
        DateAxis time_axis = new DateAxis("");//for combined plot is sufficient
        combined_plot.setDomainAxis(time_axis);//MUST have this to show X axis, otherwise default is used

        //price plot
        XYItemRenderer price_renderer = new StandardXYItemRenderer();
        price_renderer.setSeriesPaint(0, Color.blue);
        try {
            _PriceDataSet.addSeries(buildSp500Series());
            NumberAxis price_axis = new NumberAxis("Price");
            price_axis.setAutoRangeIncludesZero(false);//MUST have this, otherwise auto range is default with 0
            _PricePlot = new XYPlot(_PriceDataSet, null, price_axis, price_renderer);//does not need time axis
            _PricePlot.setBackgroundPaint(new GradientPaint(0, 0, Color.lightGray, 500, 500, new Color(250, 250, 250)));
            _PricePlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
            _PricePlot.setDomainCrosshairVisible(true);
            _PricePlot.setRangeCrosshairVisible(true);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        combined_plot.add(_PricePlot, 3);

        //indicator plot
        XYItemRenderer ind_renderer = new StandardXYItemRenderer();
        ind_renderer.setSeriesPaint(0, new Color(2, 99, 39));
//        ind_renderer.setSeriesStroke(0, new BasicStroke(1.5f));
        ind_renderer.setSeriesPaint(1, Color.yellow);//new Color(243, 250, 115));
//        ind_renderer.setSeriesStroke(1, new BasicStroke(1.1f));
        ind_renderer.setSeriesPaint(2, Color.red);
        NumberAxis ind_axis = new NumberAxis("MACD");
        _IndicatorPlot = new XYPlot(_IndicatorDataSet, null, ind_axis, ind_renderer);//does not need time axis
        _IndicatorPlot.setBackgroundPaint(Color.lightGray);
        _IndicatorPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot.setDomainCrosshairVisible(true);
        _IndicatorPlot.setRangeCrosshairVisible(true);
//        XYTextAnnotation ind_annotation = new XYTextAnnotation("Hello!", 50.0, 10000.0);
//        ind_annotation.setFont(new Font("Arial", Font.PLAIN, 9));
//        ind_annotation.setRotationAngle(Math.PI / 4.0);
//        _IndicatorPlot.addAnnotation(ind_annotation);
        combined_plot.add(_IndicatorPlot, 1);

        _Chart = new JFreeChart("3 MACD",JFreeChart.DEFAULT_TITLE_FONT, combined_plot, false);
        ChartPanel pnl = new ChartPanel(_Chart);
        add(pnl, BorderLayout.CENTER);
    }

    //add all series to the plot and repaint
    public void addAll(TimeSeries price_series, TimeSeries[] indicator_series) {
        _PriceDataSet.removeSeries(0);
        _PriceDataSet.addSeries(price_series);
        _IndicatorDataSet.removeAllSeries();
        for (TimeSeries s : indicator_series)
            _IndicatorDataSet.addSeries(s);
    }

    public TimeSeries buildSp500Series() throws ParseException {
        TimeSeries ret = new TimeSeries("SP500");
        //find first data point difference, apply to all SP500 data (normalize)
        for (int index = 500; index >= 0; index--) {
            float sp_close = FrameworkConstants.SP500_DATA.getPrice(index);
            Calendar cal = AppUtil.stringToCalendar(FrameworkConstants.SP500_DATA.getDate(index));
            Day day = new Day(cal.getTime());
            ret.add(day, sp_close);//normalize
        }
        return ret;
    }


    //-----instance variables-----
    private JFreeChart _Chart;
    private XYPlot _PricePlot, _IndicatorPlot;
    private TimeSeriesCollection _PriceDataSet = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet = new TimeSeriesCollection();//for fast, medium, slow MACD
}
