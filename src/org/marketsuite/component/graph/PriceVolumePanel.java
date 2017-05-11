package org.marketsuite.component.graph;

import org.marketsuite.component.Constants;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class PriceVolumePanel extends ChartPanel {
    public PriceVolumePanel(String title, XYDataset price_set, XYDataset volume_set) {
        super(null, true, true, true, true, true);
        setLayout(new MigLayout());
        setFocusable(true);
        requestFocus();

        //create two axes
        final ValueAxis time_axis = new DateAxis(Constants.COMPONENT_BUNDLE.getString("date"));
        time_axis.setLowerMargin(0.02);                  // reduce the default margins
        time_axis.setUpperMargin(0.02);
        final NumberAxis range_axis1 = new NumberAxis(Constants.COMPONENT_BUNDLE.getString("price"));
        range_axis1.setAutoRangeIncludesZero(false);     // override default
        range_axis1.setLowerMargin(0.40);                // to leave room for volume bars
        DecimalFormat format = new DecimalFormat("00.00");
        range_axis1.setNumberFormatOverride(format);

        //create plot with 2 data sets and renderer
        XYPlot plot = new XYPlot(price_set, time_axis, range_axis1, null);
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
        renderer1.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
            new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00#")));
        plot.setRenderer(0, renderer1);

        //create volume plot
        final NumberAxis range_axis2 = new NumberAxis(Constants.COMPONENT_BUNDLE.getString("volume"));
        range_axis2.setUpperMargin(1.00);  // to leave room for price line
        plot.setRangeAxis(1, range_axis2);
        plot.setDataset(1, volume_set);
        plot.mapDatasetToRangeAxis(1, 1);
        XYBarRenderer renderer2 = new XYBarRenderer(0.20);//volume use bar renderer
        renderer2.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
            new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0,000.00")));
        plot.setRenderer(1, renderer2);

        //combine two plots
        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(time_axis);
        cplot.add(plot, 1);
        cplot.setGap(8.0);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, cplot, true);
        setChart(chart);
    }
}
