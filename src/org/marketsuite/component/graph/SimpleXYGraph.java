package org.marketsuite.component.graph;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;

//a simple X-Y plot with one set of coordinates plotted
public class SimpleXYGraph extends JPanel {
    public SimpleXYGraph(String x_label, String y_label) {
        setLayout(new MigLayout("insets 0"));
        JFreeChart jfc = ChartFactory.createXYLineChart(
            "", x_label, y_label, _Dataset, PlotOrientation.VERTICAL, true, true, false);
        jfc.getLegend().setVisible(false);
        _Plot = jfc.getXYPlot();//time series is a type of XY plot
        _Plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        XYItemRenderer rdr1 = _Plot.getRenderer();
        rdr1.setSeriesPaint(0, new Color(10, 9, 216));
        rdr1.setSeriesStroke(0, new BasicStroke(3));
        _pnlChart = new ChartPanel(jfc);
        add(_pnlChart, "dock center");
    }

    public void plot(String name, double[] x_data, double[] y_data) {
        _Dataset.removeAllSeries();
        XYSeries series = new XYSeries(name);
        for (int i = 0; i < x_data.length; i++)
            series.add(x_data[i], y_data[i]);
        _Dataset.addSeries(series);
    }

    //----- variables -----
    private XYSeriesCollection _Dataset = new XYSeriesCollection();
    private XYPlot _Plot;
    private ChartPanel _pnlChart;
}
