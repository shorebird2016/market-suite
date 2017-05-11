package org.marketsuite.component.graph;

import jsc.descriptive.FrequencyTable;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;

//a basic bar graph with mostly default construction
public class SimpleBarGraph extends JPanel {
    public SimpleBarGraph(String x_label, String y_label) {
        setLayout(new MigLayout("insets 0"));
        JFreeChart jfc = ChartFactory.createBarChart(
            "", x_label, y_label, _Dataset, PlotOrientation.VERTICAL, true, true, false);
        jfc.getLegend().setPosition(RectangleEdge.TOP);
        CategoryPlot plot = (CategoryPlot)jfc.getPlot();
        BarRenderer renderer = (BarRenderer)plot.getRenderer();
        GradientPaint paint1 = new GradientPaint(0.0F, 0.0F, Color.green, 0.0F, 0.0F, new Color(0, 64, 0));
        renderer.setSeriesPaint(0, paint1);
//TODO tooltip
//        CategoryToolTipGenerator tip_gen = new StandardCategoryToolTipGenerator( "{1} {2}",
//        "{0}: {1}  {2}",//{0}symbol {1}date {2}value "", new SimpleDateFormat("MM/dd"));
//        renderer.setBaseToolTipGenerator(tip_gen);
        _pnlChart = new ChartPanel(jfc);
        add(_pnlChart, "dock center");
    }

    /**
     * Plot array of categories with counts of each category as histogram
     * @param name of plot
     * @param category array of numbers representing categories
     * @param counts frequency of each category
     */
    public void plot(String name, double[] category, int[] counts) {
        DefaultCategoryDataset data_set = (DefaultCategoryDataset)_Dataset;
        data_set.clear();
        for (int i = 0; i < category.length; i++)
            data_set.addValue(counts[i], name, String.valueOf(category[i]));
    }

    //simple application to plot frequency table
    public void plotFrequency(FrequencyTable fqt) {
        double[] boundary = new double[fqt.getNumberOfBins()];
        for (int i = 0; i < fqt.getNumberOfBins(); i++)
            boundary[i] = fqt.getBoundary(i);
        plot(fqt.getName(), boundary, fqt.getFrequencies());
    }

    private CategoryDataset _Dataset = new DefaultCategoryDataset();
    private ChartPanel _pnlChart;
}
