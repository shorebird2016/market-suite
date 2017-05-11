package org.marketsuite.watchlist.technical;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

//Container that draws all IBD composite lines for this watch list
public class IbdRatingGraphPanel extends JPanel{
    public IbdRatingGraphPanel() {
        setLayout(new MigLayout("insets 0"));

        //center - chart
        _Chart = ChartFactory.createTimeSeriesChart(
                "", // title
                "", // x-axis label
                ApolloConstants.APOLLO_BUNDLE.getString("wl_tab_4"), // y-axis label
                _DataSet,//main data set for price data
                true,        // create legend?
                true,        // generate tooltips?
                false        // generate URLs?
        );
        _Chart.getLegend().setPosition(RectangleEdge.TOP);
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot
        _Plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _Plot.getRangeAxis().setRange(0, 100);
        Color edge_color1 = new Color(255, 255, 0xFF);
        Color edge_color2 = new Color(207, 232, 0xFF);
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, edge_color2, 500, 500, edge_color1));
        StandardXYToolTipGenerator tip_price = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
                new SimpleDateFormat("MM/dd/yyyy"), FrameworkConstants.DOLLAR_FORMAT);

        //set up line width / color
        _Renderer = (XYLineAndShapeRenderer)_Plot.getRenderer();
        _Renderer.setBaseShapesVisible(true);
        _Renderer.setBaseShapesFilled(false);
        _pnlChart = new ChartPanel(_Chart);
//        _pnlChart.setRangeZoomable(false);
//        _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out
        add(_pnlChart, "dock center");
    }

    //----- public protected methods -----
    public void plotGraph(ArrayList<String> excluded_symbols, String emphasized_symbol) {
        _sExcludedSymbols = excluded_symbols;
        _sEmphasizedSymbol = emphasized_symbol;
        plotGraph();
//        StringBuilder no_rating = new StringBuilder();
//        WatchListModel model = MainModel.getInstance().getWatchListModel();
//        _DataSet.removeAllSeries();
//        int series_idx = 0;
//        TreeMap<String,ArrayList<IbdRating>> ibd_rating = model.getIbdRatingMap();
//        for (String sym : model.getMembers()) {
//            if (excluded_symbols.contains(sym)) continue;//skip those
//            TimeSeries series = new TimeSeries(sym);//each symbol is a series
//            ArrayList<IbdRating> ratings = ibd_rating.get(sym);
//            if (ratings == null) {
//                no_rating.append(sym).append(" ");
//                continue;
//            }
//            for (IbdRating rating : ratings) {
//                Day day = new Day(rating.getDate().getTime());
//
//                //depending on plot type, extract different rating
//                int src;
//                switch (plotType) {
//                    case Composite:
//                    default:
//                        src = rating.getComposite();
//                        break;
//
//                    case EPS:
//                        src = rating.getEpsRating();
//                        break;
//
//                    case RS:
//                        src = rating.getRsRating();
//                        break;
//                }
//
////                int composite = rating.getComposite();
//                if (src == 0) continue;//skip ones with N/A and 0
//                series.add(day, src);
//            }
//            _DataSet.addSeries(series);
//
//            //use fix color for each symbol from map, emphasize
//            if (sym.equals(emphasized_symbol))
//                _Renderer.setSeriesStroke(series_idx, BRUSH_THICK);
//            else
//                _Renderer.setSeriesStroke(series_idx, BRUSH_THIN);
//            _Renderer.setSeriesPaint(series_idx, _mapLinePaint.get(sym));
//            series_idx++;
//        }
//        if (no_rating.length() > 0)
//            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wl_001") + no_rating.toString(), LoggingSource.L_SQUARE_IBD_RATING);
    }
    public void emphasize(String symbol) {
        //find and make line thicker, baseline mode also make baseline thicker
        int series_index = 0;
        java.util.List<TimeSeries> series = (java.util.List<TimeSeries>)_DataSet.getSeries();
        for (TimeSeries ts : series) {
            Comparable key = ts.getKey();
            if (key.equals(symbol))
                _Renderer.setSeriesStroke(series_index, BRUSH_THICK);
            else
                _Renderer.setSeriesStroke(series_index, BRUSH_THIN);
            series_index++;
        }

    }
    public void createPaintMap() {
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        int paint_idx = 0;//to PALETTE
        for (String sym : wlm.getMembers()) {
            if (paint_idx >= PALETTE.length) paint_idx = 0;
            _mapLinePaint.put(sym, PALETTE[paint_idx++]);
        }
    }
    public void setPlotType(PlotType plot_type) { plotType = plot_type; plotGraph(); }

    //----- private methods -----
    //refresh plot with saved parameters
    private void plotGraph() {
        StringBuilder no_rating = new StringBuilder();
        WatchListModel model = MainModel.getInstance().getWatchListModel();
        _DataSet.removeAllSeries();
        int series_idx = 0;
        TreeMap<String,ArrayList<IbdRating>> ibd_rating = model.getIbdRatingMap();
        for (String sym : model.getMembers()) {
            if (_sExcludedSymbols.contains(sym)) continue;//skip those
            TimeSeries series = new TimeSeries(sym);//each symbol is a series
            ArrayList<IbdRating> ratings = ibd_rating.get(sym);
            if (ratings == null) {
                no_rating.append(sym).append(" ");
                continue;
            }
            for (IbdRating rating : ratings) {
                Day day = new Day(rating.getDate().getTime());

                //depending on plot type, extract different rating
                int src;
                switch (plotType) {
                    case Composite:
                    default:
                        src = rating.getComposite();
                        break;

                    case EPS:
                        src = rating.getEpsRating();
                        break;

                    case RS:
                        src = rating.getRsRating();
                        break;
                }

//                int composite = rating.getComposite();
                if (src == 0) continue;//skip ones with N/A and 0
                series.add(day, src);
            }
            _DataSet.addSeries(series);

            //use fix color for each symbol from map, emphasize
            if (sym.equals(_sEmphasizedSymbol))
                _Renderer.setSeriesStroke(series_idx, BRUSH_THICK);
            else
                _Renderer.setSeriesStroke(series_idx, BRUSH_THIN);
            _Renderer.setSeriesPaint(series_idx, _mapLinePaint.get(sym));
            series_idx++;
        }
        if (no_rating.length() > 0)
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wl_001") + no_rating.toString(), LoggingSource.L_SQUARE_IBD_RATING);
    }

    //----- variables -----
    private ChartPanel _pnlChart;
    private JFreeChart _Chart;
    private XYPlot _Plot;
    private XYLineAndShapeRenderer _Renderer;
    private TimeSeriesCollection _DataSet = new TimeSeriesCollection();//left axis
    private TreeMap<String, Color> _mapLinePaint = new TreeMap<>();
    private PlotType plotType = PlotType.Composite;//default
    private ArrayList<String> _sExcludedSymbols;
    private String _sEmphasizedSymbol;

    //----- literals -----
    private Stroke BRUSH_THICK = new BasicStroke(5.5f);
    private Stroke BRUSH_THIN = new BasicStroke(1.5f);
    private static final Color PALETTE[] = {
            new Color(208, 180, 11),
            new Color(20, 20, 20),
            new Color(235, 161, 93),
            new Color(116, 127, 255),
            new Color(21, 255, 183),
            new Color(132, 53, 26),
            new Color(38, 216, 255),
            new Color(221, 104, 9),
            new Color(217, 118, 209),
            new Color(217, 19, 202),
            new Color(55, 183, 235),
            new Color(217, 45, 69),
            new Color(18, 218, 63),
            new Color(67, 69, 255),
            new Color(151, 146, 149),
            new Color(85, 104, 88),
            new Color(113, 170, 11),
            new Color(33, 33, 76),
            new Color(16, 218, 211),
            new Color(175, 23, 217),
            new Color(179, 174, 177),
            new Color(14, 130, 95),
    };
    public enum PlotType {
        Composite("IBD Composite"),
        EPS("IBD EPS"),
        RS("IBD RS");

        PlotType(String display) { displayString = display; }
        private String displayString;
    }
    public static final int PLOT_TYPE_COMPOSITE = 0;
    public static final int PLOT_TYPE_EPS = 1;
    public static final int PLOT_TYPE_RS = 2;
    public static final String LIST_PLOT_TYPE[] = {
        "IBD Composite", "IBD EPS", "IBD RS",
    };
}
