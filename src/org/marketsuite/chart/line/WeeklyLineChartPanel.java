package org.marketsuite.chart.line;

import org.marketsuite.component.UI.MagnifierUI;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
//TODO add button to show/hide IBD lines
//container for displaying IBD's proprietary indicators vs weekly price info
public class WeeklyLineChartPanel extends AbstractGraphPanel {
    public WeeklyLineChartPanel() {
        setLayout(new MigLayout("insets 0"));

        //north - title strip
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[][]20[]10[]20[]push[]push[]15[]", "3[]3"));
        north_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_01")));
        north_pnl.add(_fldSymbol); _fldSymbol.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _fldSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = _fldSymbol.getText().toUpperCase();
                _fldSymbol.select(0, symbol.length());//highlight symbol for easy typing next one
                if (symbol.equalsIgnoreCase(_sCurrentSymbol))//already plotted
                    return;
                plotGraph(symbol);
            }
        });
        north_pnl.add(_btnNext);
        _btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showNextPrev(true);
            }
        });
        north_pnl.add(_btnPrev);
        _btnPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showNextPrev(false);
            }
        });
        north_pnl.add(_btnEmphasize);
        _btnEmphasize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _bEmphasizePrice = !_bEmphasizePrice;
                setLineWidth();
            }
        });
        north_pnl.add(_lblTitle); _lblTitle.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        north_pnl.add(_cmbZoom); _cmbZoom.setSelectedIndex(1);//default 1 year
        _cmbZoom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                plotGraph(_sCurrentSymbol);
            }
        });
        north_pnl.add(_btnSim); _btnSim.setToolTipText(ApolloConstants.APOLLO_BUNDLE.getString("qc_05"));
        _btnSim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_prices == null) return;
                if (_nSimEndIndex < 0) return;//nop done

                //based on values stored in _nSimStartIndex and _nSimEndIndex to plot
                plotRange(_nSimStartIndex, _nSimEndIndex);
                _nSimEndIndex--;//one more point next time
            }
        });
        add(north_pnl, "dock north");

        //center - chart
        _Chart = ChartFactory.createTimeSeriesChart(
            "", // title
            "", // x-axis label
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_2"), // y-axis label TODO: customize title
            _PriceDataSet,//main data set for price data
            true,        // create legend?
            true,        // generate tooltips?
            false        // generate URLs?
        );
        _Chart.getLegend().setPosition(RectangleEdge.TOP);
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot
        _Plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

        //setup right axis (default comes with left axis)
        _Plot.setDataset(1, _RatingDataset);//IBD indicator
        NumberAxis right_axis = new NumberAxis("Rating");//TODO: pass in
        right_axis.setRange(0, 100);
        _Plot.setRangeAxis(1, right_axis);
        _Plot.mapDatasetToRangeAxis(1, 1);

        //change various attributes - color, shape, stroke, paint, tooltip for main series
        Color edge_color1 = new Color(255, 255, 0xFF);
        Color edge_color2 = new Color(207, 232, 0xFF);
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, edge_color2, 500, 500, edge_color1));
        StandardXYToolTipGenerator tip_price = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
            new SimpleDateFormat("MM/dd/yyyy"), FrameworkConstants.DOLLAR_FORMAT);
        XYItemRenderer r = _Plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            _PriceRenderer = (XYLineAndShapeRenderer) r;
            _PriceRenderer.setBaseShapesVisible(false);//default false, set true show square data point
            _PriceRenderer.setBaseShapesFilled(false);
            _PriceRenderer.setSeriesShapesVisible(0, true);
            _PriceRenderer.setDrawSeriesLineAsPath(false);
            _PriceRenderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
            _PriceRenderer.setSeriesPaint(1, Color.cyan.darker());
            _PriceRenderer.setSeriesPaint(2, Color.orange.darker());
            _PriceRenderer.setBaseToolTipGenerator(tip_price);
        }

        //rendering rating information
        _RatingRenderer = new XYLineAndShapeRenderer();
        _RatingRenderer.setBaseShapesFilled(false);
        _RatingRenderer.setSeriesShapesVisible(3, false);
        _RatingRenderer.setSeriesPaint(0, new Color(221, 89, 5));//composite
        _RatingRenderer.setSeriesPaint(1, new Color(21, 144, 175));//RS
        _RatingRenderer.setSeriesPaint(2, new Color(38, 222, 49));//EPS
        _RatingRenderer.setSeriesPaint(3, Color.yellow);//marker
        _RatingRenderer.setSeriesStroke(3, new BasicStroke(8.0f));
        StandardXYToolTipGenerator tip_rank = new StandardXYToolTipGenerator("{1}  {0}={2}",//{0} is symbol
            new SimpleDateFormat("MM/dd"), FrameworkConstants.TWO_DIGIT_FORMAT);
        _RatingRenderer.setBaseToolTipGenerator(tip_rank);
        _Plot.setRenderer(1, _RatingRenderer);
        setLineWidth();

        //main axis
        _LinearRangeAxis = _Plot.getRangeAxis();//save default one for later
        _LogRangeAxis = new LogarithmicAxis("");

        //put jfc into panel for layout, set attributes
        _pnlChart = new ChartPanel(_Chart);
        _pnlChart.setRangeZoomable(false);
        _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out

        //add magnifier layer
        JXLayer layer = new JXLayer(_pnlChart);
        MagnifierUI ui = new MagnifierUI();
        layer.setUI(ui);
        add(/*layer*/_pnlChart, "dock center");
    }

    //-----public methods-----
    public void plotGraph(String symbol) {
        if (symbol == null) {//no symbol, user deselect
            clearChart();
            return;
        }
        _fldSymbol.setText("");

        //update company's full name
        int num_quotes = computeNumQuotes();//based on current combo selection

        //if symbol changes, update title from fundamental info
        if (!symbol.equals(_sCurrentSymbol)) {
            StringBuilder sb = new StringBuilder(symbol);
            Fundamental fundamental = MainModel.getInstance().getFundamentals().get(symbol);
            if (fundamental != null)//can't find
                sb.append(": ").append(fundamental.getFullName());
            _lblTitle.setText(sb.toString());
            _sCurrentSymbol = symbol;
        }

        //plot left side with price/2 SMA, right side with IBD composite
        FundData fund;
        try {
            if (num_quotes == -1)
                fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);//read full range
            else
                fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, num_quotes + 2);//2 extra line for comments in file
        } catch (IOException e) {//can't read quotes, clear chart
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_01") + " " + symbol, LoggingSource.WEEKLY_CHART);
            clearChart();
            return;
        }

        DataUtil.adjustForSplits(fund, fund.getSize() - 1, 0);
//        _weeklyQuote = new WeeklyQuote(fund, fund.getSize() - 1, 0);
        _weeklyQuote = new WeeklyQuote(fund, 200);

        //compute moving avg, covert quotes into float array
        int size = _weeklyQuote.getSize();
        _prices = new float[size];
        for (int idx = 0; idx < size; idx++)
            _prices[idx] = _weeklyQuote.getQuotes().get(idx).getClose();
        _sma50 = new SMA(10, _prices);//10 week = 50 day
        _sma200 = new SMA(40, _prices);//40 week = 200 day

        //limit number of points plotted
        int show_bars = _prices.length;//40;//since IBD rating only recent
        if (show_bars > _prices.length)
            show_bars = _prices.length;
        TimeSeries price_series = new TimeSeries("Price");
        TimeSeries sma1_series = new TimeSeries("10 Week SMA");
        TimeSeries sma2_series = new TimeSeries("40 Week SMA");
        for (int i = 0; i < show_bars; i++) {
            Calendar cal = AppUtil.stringToCalendarNoEx(_weeklyQuote.getQuotes().get(i).getDate());
            Day day = new Day(cal.getTime());
            price_series.add(day, _prices[i]);
            float v = _sma50.getSma()[i];
            if (v > 0)
                sma1_series.add(day, v);
            v = _sma200.getSma()[i];
            if (v > 0)
                sma2_series.add(day, v);
        }
        _PriceDataSet.removeAllSeries();
        _PriceDataSet.addSeries(price_series);
        _PriceDataSet.addSeries(sma1_series);
        _PriceDataSet.addSeries(sma2_series);

        //read rating database
        _RatingDataset.removeAllSeries();
        try {
            _ibdRatings = IbdRating.readIbdWeeklyRating(symbol, FrameworkConstants.DATA_FOLDER_IBD_RATING, _weeklyQuote);
        } catch (IOException e) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_02") + " " + symbol, LoggingSource.WEEKLY_CHART);
            return;
        }
//TODO        IbdRating.fillGaps(_ibdRatings, _weeklyQuote);

        TimeSeries composite_series = new TimeSeries("Composite");
        TimeSeries rs_series = new TimeSeries("RS");
        TimeSeries eps_series = new TimeSeries("EPS");
        for (int i = 0; i < _ibdRatings.size(); i++) {
            Day day = new Day(_ibdRatings.get(i).getDate().getTime());
            composite_series.add(day, _ibdRatings.get(i).getComposite());
            rs_series.add(day, _ibdRatings.get(i).getRsRating());
            eps_series.add(day, _ibdRatings.get(i).getEpsRating());
        }
        _RatingDataset.addSeries(composite_series);
        _RatingDataset.addSeries(rs_series);
        _RatingDataset.addSeries(eps_series);
        setLineWidth();
        _nSimStartIndex = _ibdRatings.size() - 1;
        if (_ibdRatings.size() > size) _nSimStartIndex = size - 1;//don't exceed price length
        _nSimEndIndex = _nSimStartIndex - 3;//only 3 week data initially
    }
    public void setTitle(String title) {
        _Chart.setTitle(title);
    }
    public void setLogScale(boolean log_scale) {
        if (log_scale)
            _Plot.setRangeAxis(_LogRangeAxis);
        else
            _Plot.setRangeAxis(_LinearRangeAxis);
    }

    //-----private methods-----
    private int computeNumQuotes() {//number of days into the past
        String sel = (String)_cmbZoom.getSelectedItem();
        ZoomLevel zl = ZoomLevel.findZoomLevel(sel);
        switch (zl) {
            case Months6:
            default:
                return DAYS_PER_YEAR / 2;

            case Year1:
                return DAYS_PER_YEAR;

            case Year2:
                return DAYS_PER_YEAR * 2;

            case Year3:
                return DAYS_PER_YEAR * 3;

            case Year5:
                return DAYS_PER_YEAR * 5;

            case Max:
                return -1;
        }
    }
    private void showNextPrev(boolean next) {
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        if (wlm == null) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_04"), LoggingSource.WEEKLY_CHART);
            return;
        }
        String sym = wlm.getNextPrevSymbol(next);
        if (sym == null) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_05") + " " + sym, LoggingSource.WEEKLY_CHART);
            return;
        }
        plotGraph(sym);
    }
    private void setLineWidth() {
        _PriceRenderer.setSeriesStroke(0, _bEmphasizePrice ? BRUSH_THICK_PRICE : BRUSH_THIN_PRICE);
        _PriceRenderer.setSeriesStroke(1, _bEmphasizePrice ? BRUSH_THICK_MA : BRUSH_THIN);
        _PriceRenderer.setSeriesStroke(2, _bEmphasizePrice ? BRUSH_THICK_MA : BRUSH_THIN);
        _RatingRenderer.setSeriesStroke(0, _bEmphasizePrice ? BRUSH_THIN : BRUSH_THICK_RATING);
        _RatingRenderer.setSeriesStroke(1, _bEmphasizePrice ? BRUSH_THIN : BRUSH_THICK_RATING);
//        _RatingRenderer.setSeriesStroke(2, _bEmphasizePrice ? BRUSH_THIN : BRUSH_THICK_RATING);
    }
    //plot data points only between these 2 dates specified by weekly quote array index, weekly is backwards
    private void plotRange(int start_idx, int end_idx) {
        ArrayList<FundQuote> wqs = _weeklyQuote.getQuotes();
        TimeSeries price_series = new TimeSeries("Price");
        TimeSeries sma1_series = new TimeSeries("10 Week SMA");
        TimeSeries sma2_series = new TimeSeries("40 Week SMA");
        TimeSeries composite_series = new TimeSeries("Composite");
        TimeSeries rs_series = new TimeSeries("RS");
        TimeSeries eps_series = new TimeSeries("EPS");
        TimeSeries marker_series = new TimeSeries("Marker");

        //loop thru all indices in weekly quote, find match IBD rating, copy into dataset
        for (int active_idx = start_idx; active_idx >= end_idx; active_idx--) {
            FundQuote active_quote = wqs.get(active_idx);
            String active_date = active_quote.getDate();

            //find matching index in rating array
            for (IbdRating rating : _ibdRatings) {
                String rating_date = AppUtil.calendarToString(rating.getDate());
                if (rating_date.equals(active_date)) {//matched date
                    Calendar cal = AppUtil.stringToCalendarNoEx(rating_date);
                    Day day = new Day(cal.getTime());
                    price_series.add(day, active_quote.getClose());
                    float v = _sma50.getSma()[active_idx];
                    if (v > 0)
                        sma1_series.add(day, v);
                    v = _sma200.getSma()[active_idx];
                    if (v > 0)
                        sma2_series.add(day, v);
                    composite_series.add(day, rating.getComposite());
                    rs_series.add(day, rating.getRsRating());
                    eps_series.add(day, rating.getEpsRating());
                    marker_series.add(day, 50);
                    break;
                }
            }

        }

        //plotting
        _PriceDataSet.removeAllSeries();
        _PriceDataSet.addSeries(price_series);
        _PriceDataSet.addSeries(sma1_series);
        _PriceDataSet.addSeries(sma2_series);
        _RatingDataset.removeAllSeries();
        _RatingDataset.addSeries(composite_series);
        _RatingDataset.addSeries(rs_series);
        _RatingDataset.addSeries(eps_series);
        _RatingDataset.addSeries(marker_series);
        setLineWidth();

}
    private void clearChart() {
        _lblTitle.setText("");
        _fldSymbol.setText("");
        _sCurrentSymbol = null;
        _PriceDataSet.removeAllSeries();
        _RatingDataset.removeAllSeries();
    }

    //-----inner classes-----
    private enum ZoomLevel {
        Months6,
        Year1,
        Year2,
        Year3,
        Year5,
        Max;

        //convert all into human readable string
        public static String[] toStrings() { return displayStrings; }
        private final static String[] displayStrings = {
            "6 Month", "1 Year", "2 Year", "3 Year", "5 Year", "Full Length"
        };
        public static ZoomLevel findZoomLevel(String display_string) {
            for (int i = 0; i < displayStrings.length; i++) {
                if (displayStrings[i].equals(display_string))
                    return values()[i];
            }
            return Months6;
        }
    }

    //-----instance variables-----
    private String _sCurrentSymbol;
    private NameField _fldSymbol = new NameField(5);
    private JButton _btnPrev = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_3"), FrameworkIcon.ARROW_3D_LEFT);
    private JButton _btnNext = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_2"), FrameworkIcon.ARROW_3D_RIGHT);
    private JButton _btnEmphasize = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_5"), FrameworkIcon.THUMB_TACK);
    private boolean  _bEmphasizePrice = true;
    private JComboBox<String> _cmbZoom = new JComboBox<>(ZoomLevel.toStrings());
    protected JLabel _lblTitle = new JLabel();
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    private XYLineAndShapeRenderer _PriceRenderer, _RatingRenderer;
    private JFreeChart _Chart;
    protected XYPlot _Plot;
    private TimeSeriesCollection _PriceDataSet = new TimeSeriesCollection();//left axis
    private TimeSeriesCollection _RatingDataset = new TimeSeriesCollection();//right axis
    private JButton _btnSim = WidgetUtil.createIconButton(null, FrameworkIcon.RIGHT_ARROW);
    private WeeklyQuote _weeklyQuote;
    private float[] _prices;
    private SMA _sma50, _sma200;
    private ArrayList<IbdRating> _ibdRatings;
    private int _nSimStartIndex;
    private int _nSimEndIndex;

    //-----literals-----
    private static final int DAYS_PER_YEAR = 250;
    private Stroke BRUSH_THICK_PRICE = new BasicStroke(2.0f);
    private Stroke BRUSH_THICK_MA = new BasicStroke(1.5f);
    private Stroke BRUSH_THICK_RATING = new BasicStroke(3.0f);
    private Stroke BRUSH_THIN_PRICE = new BasicStroke(1.2f);
    private Stroke BRUSH_THIN = new BasicStroke(0.5f);
}