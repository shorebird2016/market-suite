package org.marketsuite.framework.util;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.awt.*;

/**
 * Collection of static methods for graphing needs
 */
public class GraphUtil {
    public static XYPointerAnnotation createArrow(String date, double price, boolean entry, String[] labels) {
        long x = AppUtil.stringToDay(date).getFirstMillisecond();
        XYPointerAnnotation ret = new XYPointerAnnotation(entry ? labels[0] : labels[1], x, price, entry ? Math.PI / 2 : Math.PI * 3 / 2);
        ret.setArrowPaint(entry ? new Color(100, 0, 250) : new Color(200, 0, 100));
        ret.setArrowStroke(new BasicStroke(1.2f));
        ret.setTipRadius(1);//tip from coordinate
        ret.setArrowLength(1.5);
        ret.setArrowWidth(3);
        ret.setToolTipText(date + " : " + FrameworkConstants.DOLLAR_FORMAT.format(price));
        return ret;
    }

    public static XYPointerAnnotation createMarker(String date, double price, String label, Color color, double angle) {
        long x = AppUtil.stringToDay(date).getFirstMillisecond();
        XYPointerAnnotation ret = new XYPointerAnnotation(label, x, price, angle);//direction of arrow
        ret.setArrowPaint(color);
        ret.setArrowStroke(new BasicStroke(3f));//1.2f
        ret.setTipRadius(5);//tip from coordinate 1
        ret.setArrowLength(3.5);//1.5
        ret.setArrowWidth(5);//3
        ret.setToolTipText(date + " : " + FrameworkConstants.DOLLAR_FORMAT.format(price));
        return ret;
    }

    public static XYPointerAnnotation createArrow(String date, double price, boolean entry) {
        long x = AppUtil.stringToDay(date).getFirstMillisecond();
        String lbl = entry ? "Entry" : "Exit";
        XYPointerAnnotation ret = new XYPointerAnnotation(lbl, x, price, entry ? Math.PI / 2 : Math.PI * 3 / 2);
        ret.setArrowPaint(entry ? new Color(100, 0, 250) : new Color(200, 0, 100));
        ret.setArrowStroke(new BasicStroke(1.2f));
        ret.setTipRadius(1);//tip from coordinate
        ret.setArrowLength(1.5);
        ret.setArrowWidth(3);
        ret.setToolTipText(date + " : " + FrameworkConstants.DOLLAR_FORMAT.format(price));
        return ret;
    }
    public static XYPointerAnnotation createTip(String date, double price, boolean entry) {
        long x = AppUtil.stringToDay(date).getFirstMillisecond();
        String lbl = entry ? " " : " ";//Must have at least one char in label, otherwise strange exception occurs
        XYPointerAnnotation ret = new XYPointerAnnotation(lbl, x, price, Math.PI * 1 / 2);
        ret.setArrowPaint(Color.red);
        ret.setArrowStroke(new BasicStroke(2f));
        ret.setTipRadius(3);//tip from coordinate
        ret.setArrowLength(5.5);
        ret.setArrowWidth(4);
        ret.setToolTipText(date + " : " + FrameworkConstants.DOLLAR_FORMAT.format(price));
        return ret;
    }

//
//    public static JFreeChart createPriceVolumeChart(XYDataset price_set, XYDataset volume_set,
//            XYDataset ind1_set, XYDataset ind2_set, XYDataset ind3_set) {
//        //create two axes
//        final ValueAxis time_axis = new DateAxis();
//        time_axis.setLowerMargin(0.01); // reduce the default margins
//        time_axis.setUpperMargin(0.01);
//        final NumberAxis price_axis = new NumberAxis(Constants.COMPONENT_BUNDLE.getString("price"));
//        price_axis.setAutoRangeIncludesZero(false);     // override default
//        price_axis.setLowerMargin(0.20);                // to leave room for volume bars, 40% from bottom of data
//        DecimalFormat format = new DecimalFormat("00.00");
//        price_axis.setNumberFormatOverride(format);
//        StandardXYToolTipGenerator tip_price = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
//            new SimpleDateFormat("MM/dd"), FrameworkConstants.DOLLAR_FORMAT);
//        StandardXYToolTipGenerator tip_vol = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
//            new SimpleDateFormat("MM/dd"), FrameworkConstants.INT_FORMAT);
//
//        //create plot with 2 data sets and renderer
//        XYItemRenderer price_renderer = new XYLineAndShapeRenderer(true, false);
//        price_renderer.setSeriesStroke(0, new BasicStroke(2.0f));
//        price_renderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
//        price_renderer.setSeriesStroke(1, new BasicStroke(1.0f));
//        price_renderer.setSeriesPaint(1, Color.red.brighter());//cyan (10SMA)
//        price_renderer.setSeriesStroke(2, new BasicStroke(1.0f));
//        price_renderer.setSeriesPaint(2, Color.green.darker());//orange (30SMA)
//        price_renderer.setSeriesStroke(3, new BasicStroke(1.0f));
//        price_renderer.setSeriesPaint(3, Color.cyan.darker());//green (50SMA)
//        price_renderer.setSeriesStroke(4, new BasicStroke(1.2f));
//        price_renderer.setSeriesPaint(4, Color.orange.darker());//red (200SMA)
//        price_renderer.setBaseToolTipGenerator(tip_price);
//        XYPlot plot = new XYPlot(price_set, time_axis, price_axis, null);
//        plot.setRenderer(0, price_renderer);
//        Color edge_color = new Color(0xF0, 0xFF, 0xFF);
//        plot.setBackgroundPaint(new GradientPaint(0, 0, edge_color, 500, 500, edge_color));
//        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
//
//        //create volume plot with axis, even though not shown, must have it to map w dataset
//        NumberAxis volume_axis = new NumberAxis();
//        volume_axis.setUpperMargin(1.5);  //ratio between volume and price, to leave room for price line
//        volume_axis.setTickLabelsVisible(false);
//        plot.setRangeAxis(1, volume_axis);//without this, volume bar doesn't show
//        plot.setDataset(1, volume_set);
//        plot.mapDatasetToRangeAxis(1, 1);
//
//        //volume renderer
//        XYBarRenderer volume_renderer = new XYBarRenderer(0.2);//20% bar width is trimmed
//        volume_renderer.setSeriesPaint(0, Color.green.darker());//volume
//// TODO switch between green and red for up/down day
//        volume_renderer.setSeriesPaint(1, Color.yellow.darker());//volume average
//        volume_renderer.setBaseToolTipGenerator(tip_vol);
//        volume_renderer.setShadowVisible(false);
//        plot.setRenderer(1, volume_renderer);
//
//        //combine two plots
//        CombinedDomainXYPlot combined_plot = new CombinedDomainXYPlot(time_axis);
//        combined_plot.add(plot, 4);
//        combined_plot.setGap(0.5);
//
//        //indicator plot w renderer + options
//        XYItemRenderer ind_renderer = new StandardXYItemRenderer();
//        Color ind_color = new Color(66, 4, 101);
//        ind_renderer.setSeriesPaint(0, ind_color);
//        ind_renderer.setSeriesStroke(0, new BasicStroke(1.0f));
//
//        //tooltip for indicator
//        StandardXYToolTipGenerator ttg = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
//                new SimpleDateFormat("MM/dd"), FrameworkConstants.PRICE_FORMAT);
//        ind_renderer.setSeriesToolTipGenerator(0, ttg);
//        NumberAxis ind1_axis = new NumberAxis("MACD(12,26)");
//        XYPlot _IndicatorPlot1 = new XYPlot(ind1_set, null, ind1_axis, ind_renderer);//does not need time axis
//        _IndicatorPlot1.setBackgroundPaint(new Color(0xEB, 0xFF, 0xFF));
//        _IndicatorPlot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
//        _IndicatorPlot1.setRangeZeroBaselineVisible(true);
//        combined_plot.add(_IndicatorPlot1, 1);
//
//        //indicator #2
//        NumberAxis ind2_axis = new NumberAxis("RSI(14)");
//        XYPlot _IndicatorPlot2 = new XYPlot(ind2_set, null, ind2_axis, ind_renderer);//does not need time axis
//        _IndicatorPlot2.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
//        _IndicatorPlot2.setBackgroundPaint(new Color(0xE6, 0xFF, 0xFF));
//        combined_plot.add(_IndicatorPlot2, 1);
//
//        //indicator #3
//        NumberAxis ind3_axis = new NumberAxis("DSTO(14)");
//        XYPlot _IndicatorPlot3 = new XYPlot(ind3_set, null, ind3_axis, ind_renderer);//does not need time axis
//        _IndicatorPlot3.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
//        _IndicatorPlot3.setBackgroundPaint(new Color(0xE0, 0xFF, 0xFF));
//        combined_plot.add(_IndicatorPlot3, 1);
//
//
//
//        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, combined_plot, false);
////        org.yccheok.jstock.charting.Utils.applyChartThemeEx(chart);
//        // Only do it after applying chart theme.
////        org.yccheok.jstock.charting.Utils.setPriceSeriesPaint(renderer1);
////        org.yccheok.jstock.charting.Utils.setVolumeSeriesPaint(volume_renderer);
//
//        // Handle zooming event.
////        chart.addChangeListener(this.getChartChangeListner());
//        return chart;
//    }
}