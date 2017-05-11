package org.marketsuite.marektview.Velocity;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.marektview.ranking.Ranking;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.marketsuite.framework.model.FundData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Container that holds various ranking related statistical graphs.
 */
public class FrequencyPlotPanel extends JPanel {
    public FrequencyPlotPanel() {
        setLayout(new MigLayout("insets 0"));
        JFreeChart jfc = ChartFactory.createBarChart(
            "", "Symbol", "Frequency", _Dataset, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot)jfc.getPlot();
//        jfc.getLegend().setPosition(RectangleEdge.RIGHT);

        //initialize plot parameters, cross-hair, axis
        plot.setDomainGridlinesVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairPaint(Color.blue);
        NumberAxis rank_axis = (NumberAxis)plot.getRangeAxis();
        rank_axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        //renderer, tooltip
        BarRenderer renderer = (BarRenderer)plot.getRenderer();
        renderer.setDrawBarOutline(false);
        GradientPaint paint1 = new GradientPaint(0.0F, 0.0F, Color.blue, 0.0F, 0.0F, new Color(0, 0, 64));
        GradientPaint paint2 = new GradientPaint(0.0F, 0.0F, Color.green, 0.0F, 0.0F, new Color(0, 64, 0));
        GradientPaint paint3 = new GradientPaint(0.0F, 0.0F, Color.red, 0.0F, 0.0F, new Color(64, 0, 0));
        renderer.setSeriesPaint(0, paint1);
        renderer.setSeriesPaint(1, paint2);
        renderer.setSeriesPaint(2, paint3);
        renderer.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("Tooltip: {0}"));
        CategoryAxis sym_axis = plot.getDomainAxis();
        sym_axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0.52359877559829882D));
        _pnlChart = new ChartPanel(jfc);
        add(_pnlChart, "dock center");
    }

    //assumption: values are calculated in all fields of this object
    //  note: just update _Dataset will auotmatically refresh chart
    public void plot(String cur_symbol, ArrayList<Ranking> rankings, FundData fund) {
        DefaultCategoryDataset data_set = (DefaultCategoryDataset)_Dataset;
        data_set.clear();

        //find frequency array for this symbol
        for (Ranking rnk : rankings) {
            if (cur_symbol.equals(rnk.symbol)) {
                ArrayList<Integer> freqs = rnk.freqs;
                int num = 1;
                for (Integer frq : freqs)
                    data_set.addValue(frq, cur_symbol, String.valueOf(num++));
                return;
            }
        }

        //obtain list of symbols
//        for (Ranking rnk : rankings) {
//            data_set.addValue(rnk.freqs.get(0), "0", rnk.symbol);
//            data_set.addValue(rnk.freqs.get(1), "1", rnk.symbol);
//            data_set.addValue(rnk.freqs.get(2), "2", rnk.symbol);
//            data_set.addValue(rnk.freqs.get(3), "3", rnk.symbol);
//            data_set.addValue(rnk.freqs.get(4), "4", rnk.symbol);
//        }
    }

    //----- variables -----
    private CategoryDataset _Dataset = new DefaultCategoryDataset();
    private ChartPanel _pnlChart;

    private static CategoryDataset createDataset() {
        String s = "First";
        String s1 = "Second";
        String s2 = "Third";
        String s3 = "Category 1";
        String s4 = "Category 2";
        String s5 = "Category 3";
        String s6 = "Category 4";
        String s7 = "Category 5";
        DefaultCategoryDataset data_set = new DefaultCategoryDataset();
        data_set.addValue(1.0D, s, s3);
        data_set.addValue(4D, s, s4);
        data_set.addValue(3D, s, s5);
        data_set.addValue(5D, s, s6);
        data_set.addValue(5D, s, s7);
        data_set.addValue(5D, s1, s3);
        data_set.addValue(7D, s1, s4);
        data_set.addValue(6D, s1, s5);
        data_set.addValue(8D, s1, s6);
        data_set.addValue(4D, s1, s7);
        data_set.addValue(4D, s2, s3);
        data_set.addValue(3D, s2, s4);
        data_set.addValue(2D, s2, s5);
        data_set.addValue(3D, s2, s6);
        data_set.addValue(6D, s2, s7);
        return data_set;
    }
}