package org.marketsuite.framework.util;

import org.marketsuite.framework.resource.FrameworkConstants;
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

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Calendar;

/**
 * A Combined domain xy plot with price on top and indicator below, both share the same time axis.
 */
public class SamplePanel extends JPanel {
    public SamplePanel() {
        setLayout(new BorderLayout() );
//        main_plot = new CombinedDomainXYPlot();

        //price plot
        XYItemRenderer price_renderer = new StandardXYItemRenderer();
        try {
            _PriceDataSet.addSeries(buildSp500Series());
            NumberAxis price_axis = new NumberAxis("");
            price_axis.setAutoRangeIncludesZero(false);//NOTE: this actually auto range ????
            DateAxis time_axis = new DateAxis("Time");
            _PricePlot = new XYPlot(_PriceDataSet, time_axis, price_axis, price_renderer);
            _PricePlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
            _PricePlot.setDomainCrosshairVisible(true);
            _PricePlot.setRangeCrosshairVisible(true);
        } catch (ParseException e) {
            e.printStackTrace();
        }

//            main_plot.add(_PricePlot, 3);
        //indicator plot
//        XYItemRenderer ind_renderer = new StandardXYItemRenderer();
//        NumberAxis ind_axis = new NumberAxis("Indicator");
//        _IndicatorPlot = new XYPlot(_PriceDataSet, null, ind_axis, ind_renderer);
//        _IndicatorPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
////        XYTextAnnotation ind_annotation = new XYTextAnnotation("Hello!", 50.0, 10000.0);
////        ind_annotation.setFont(new Font("Arial", Font.PLAIN, 9));
////        ind_annotation.setRotationAngle(Math.PI / 4.0);
////        _IndicatorPlot.addAnnotation(ind_annotation);
////        main_plot.add(_IndicatorPlot, 1);

        _Chart = new JFreeChart("3 MACD",JFreeChart.DEFAULT_TITLE_FONT, _PricePlot, false);
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
//        _Chart.fireChartChanged();
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
    CombinedDomainXYPlot main_plot;
    private XYPlot _PricePlot, _IndicatorPlot;
    private TimeSeriesCollection _PriceDataSet = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet = new TimeSeriesCollection();//for fast, medium, slow MACD
}
