package org.marketsuite.marektview.ranking;

import org.marketsuite.framework.model.FundData;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.marketsuite.framework.model.FundData;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Time series graph to show rank progression over time.
 */
public class RankProgressionPanel extends JPanel {
    public RankProgressionPanel() {
        setLayout(new MigLayout("insets 0"));//remove padding around object
        _Chart = ChartFactory.createBoxAndWhiskerChart("", "Symbol", "Rank", _Dataset, true);

        //center - jFreeChart of equity over time
//        _Chart = ChartFactory.createLineChart(
//                "", // title
//                "", // x-axis label
//                Constants.COMPONENT_BUNDLE.getString("rank"), // y-axis label
//                _DataSet,//empty by default
//                PlotOrientation.VERTICAL,
//                true,               // create legend?
//                true,               // generate tooltips?
//                false               // generate URLs?
//        );
//        _Plot = _Chart.getCategoryPlot();//time series is a type of XY plot
//        CategoryItemRenderer r = _Plot.getRenderer();
//        if (r instanceof XYLineAndShapeRenderer) {
//            _Renderer = (XYLineAndShapeRenderer) r;
//            _Renderer.setSeriesPaint(0, new Color(0, 100, 50));//light blue,green mix, emphasize first in series
//            _Renderer.setBaseShapesVisible(false);//default false, set true show square data point
//            _Renderer.setBaseShapesFilled(false);
//            _Renderer.setDrawSeriesLineAsPath(false);
////            _Renderer.setSeriesPaint(0, new Color(0, 100, 50));//light blue,green mix, emphasize first in series
////            _Renderer.setSeriesStroke(0, new BasicStroke(1.5F));
//            StandardXYToolTipGenerator tip_gen = new StandardXYToolTipGenerator("{0}: {1}  {2}",//{0}symbol {1}date {2}vale
//                    new SimpleDateFormat("MM/dd"), FrameworkConstants.ROI_FORMAT);
//            _Renderer.setBaseToolTipGenerator(tip_gen);
//        }
        ChartPanel _pnlChart = new ChartPanel(_Chart);
        add(_pnlChart, "dock center");

    }

    //given Ranking array, plot how it shifts over time for this symbol
    public void plot(String cur_symbol, ArrayList<Ranking> rankings, FundData fund) {
        byte byte0 = 3;
        byte byte1 = 5;
        byte byte2 = 20;
        for (int i = 0; i < byte0; i++) {
            for (int j = 0; j < byte1; j++) {
                java.util.List list = createValueList(0.0D, 20D, byte2);
                _Dataset.add(list, "Series " + i, "Category " + j);
            }
        }
    }

    private static java.util.List createValueList(double d, double d1, int i) {
        ArrayList arraylist = new ArrayList();
        for (int j = 0; j < i; j++) {
            double d2 = d + Math.random() * (d1 - d);
            arraylist.add(new Double(d2));
        }
        return arraylist;
    }

//        _DataSet.clear();
//        for (Ranking rnk : rankings) {
//            //calculate max, min, avg of each symbol's ranking
//            //convert to float array
//            ArrayList<Float> rf = new ArrayList<>();
//            for (int i=0; i<rnk.ranks.size(); i++)
//                rf.add((float)rnk.ranks.get(i));
//            float average = AppUtil.average(rf);
//            float max = AppUtil.max(rf);
//            float min = AppUtil.min(rf);
//            _DataSet.addValue(average, "AVG", rnk.symbol);
//            _DataSet.addValue(max, "MAX", rnk.symbol);
//            _DataSet.addValue(min, "MIN", rnk.symbol);
//        }
//            if (cur_symbol.equals(rnk.symbol)) {
//                for (int idx = rnk.ranks.size() - 1; idx >= 0; idx--) {
//                    _DataSet.addValue(rnk.ranks.get(idx), "", cur_symbol);
//                }
//                TimeSeries ret = new TimeSeries(cur_symbol);
//                for (int idx = rnk.ranks.size() - 1; idx >= 0; idx--) {
//                    Calendar cal = AppUtil.stringToCalendarNoEx(quotes.get(idx).getDate());
//                    Day day = new Day(cal.getTime());
//                    ret.add(day, rnk.ranks.get(idx));
//                }
//                _DataSet.addSeries(ret);
//                return;
//            }

    //----- variables -----
    private JFreeChart _Chart;
    protected XYLineAndShapeRenderer _Renderer;
    protected CategoryPlot _Plot;
    private DefaultBoxAndWhiskerCategoryDataset _Dataset = new DefaultBoxAndWhiskerCategoryDataset();
}
