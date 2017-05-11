package org.marketsuite.marektview.ranking;

import org.marketsuite.component.Constants;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Container for history ranking graph of watch lists.
 */
public class HistoryGraphPanel extends JPanel {
    public HistoryGraphPanel() {
        setLayout(new MigLayout("insets 0"));//remove padding around object

        //center - jFreeChart of rankings over time
        _Chart = ChartFactory.createTimeSeriesChart(
                "", // title
                "", // x-axis label
                ApolloConstants.APOLLO_BUNDLE.getString("rv_lbl_01"), // y-axis label
                _DataSet,//empty by default
                true,    // create legend?
                true,    // generate tooltips?
                false    // generate URLs?
        );
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot
//        _Plot.setBackgroundPaint(Color.black);
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, new Color(200, 250, 180), 500, 500, Color.white));
        XYItemRenderer r = _Plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer)
            _Renderer = (XYLineAndShapeRenderer) r;
        _Plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _Chart.getLegend().setPosition(RectangleEdge.TOP);
        ChartPanel _pnlChart = new ChartPanel(_Chart);
        add(_pnlChart, "dock center");
    }

    //----- protected methods -----
    void plotRanks(HashMap<String, ArrayList<Integer>> rankings, ArrayList<Calendar> calendars) {
        _DataSet.removeAllSeries();
        int num_symbols = rankings.size();
        Iterator<String> itor = rankings.keySet().iterator();
        while (itor.hasNext()) {
            String sym = itor.next();
            TimeSeries ts = new TimeSeries(sym);
            ArrayList<Integer> ranks = rankings.get(sym);
            int ranking_idx = 0;
            for (Calendar cal : calendars) {
                if (ranking_idx == ranks.size()) break;
                Day day = new Day(cal.getTime());
                int rank = ranks.get(ranking_idx++);
                ts.add(day, num_symbols - rank + 1);//reverse scale, small on top
            }
            _DataSet.addSeries(ts);
        }
    }

    //find symbol in data set and make its stroke heavier, also de-emphasize previous symbol
    public void emphasizeSymbol(String symbol) {
        //find and make line thicker, baseline mode also make baseline thicker
        int series_index = 0;
        List<TimeSeries> series = (List<TimeSeries>)_DataSet.getSeries();
        for (TimeSeries ts : series) {
            Comparable key = ts.getKey();
            if (key.equals(symbol))
                _Renderer.setSeriesStroke(series_index, new BasicStroke(3));
            else
                _Renderer.setSeriesStroke(series_index, new BasicStroke(1));
            series_index++;
        }
        _sEmphasizedSymbol = symbol;
    }

    //----- accessor -----

    //----- variables -----
    private JFreeChart _Chart;
    private XYPlot _Plot;
    private XYLineAndShapeRenderer _Renderer;
    private TimeSeriesCollection _DataSet = new TimeSeriesCollection();
    private String _sEmphasizedSymbol;//null = nothing emphasized
}