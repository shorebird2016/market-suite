package org.marketsuite.test.line;

import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.watchlist.model.WatchListModel;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.*;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

//container for price/indicator graphs using JFreeChart
// use a DataSet to represent underline data
public class PricePanel extends AbstractGraphPanel {
    public PricePanel() {
        setLayout(new BorderLayout());

        //north - title strip
        JPanel east_pnl = new JPanel();  east_pnl.setOpaque(false);
        east_pnl.add(_btnPrev);  //_btnPrev.setEnabled(false);
        _btnPrev.setDisabledIcon(new DisabledIcon(FrameworkIcon.ARROW_3D_LEFT.getImage()));
        _btnPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    drawNextSymbol(false);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        east_pnl.add(Box.createGlue());
        east_pnl.add(_btnNext);  //_btnNext.setEnabled(false);
        _btnNext.setDisabledIcon(new DisabledIcon(FrameworkIcon.ARROW_3D_RIGHT.getImage()));
        _btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    drawNextSymbol(true);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        east_pnl.add(Box.createHorizontalStrut(5));
        east_pnl.add(_btnZoom);
        _btnZoom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
//TODO magnifier is wierd
//                if (_bMagnifierOn) {
//                    remove(_MagnifierLayer);
//                    _bMagnifierOn = false;
//                }
//                else {
//                    add(_MagnifierLayer);
//                    _bMagnifierOn = true;
//                }
            }
        });
        JPanel west_pnl = new JPanel();  west_pnl.setOpaque(false);
        _lblTitle = new JLabel();  _lblTitle.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        west_pnl.add(_lblTitle);
        add(WidgetUtil.createTitleStrip(west_pnl, null, east_pnl), BorderLayout.NORTH);

        //center - chart
        _Chart = ChartFactory.createTimeSeriesChart(
            "", // title
            "", // x-axis label
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_2"), // y-axis label
            _DataSet = new TimeSeriesCollection(),
            true,               // create legend?
            true,               // generate tooltips?
            false               // generate URLs?
        );

        //change plot attributes - inside plot area
        _Plot = _Chart.getXYPlot();//time series is a type of XY plot
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, Color.lightGray, 500, 500, new Color(250, 250, 250)));
        XYItemRenderer r = _Plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(false);//default false, set true show square data point
            renderer.setBaseShapesFilled(false);
            renderer.setDrawSeriesLineAsPath(false);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
            renderer.setSeriesStroke(1, new BasicStroke(1.0f));
            renderer.setSeriesPaint(1, Color.red.brighter());//cyan (10SMA)
            renderer.setSeriesStroke(2, new BasicStroke(1.0f));
            renderer.setSeriesPaint(2, Color.green.darker());//orange (30SMA)
            renderer.setSeriesStroke(3, new BasicStroke(1.0f));
            renderer.setSeriesPaint(3, Color.cyan.darker());//green (50SMA)
            renderer.setSeriesStroke(4, new BasicStroke(1.2f));
            renderer.setSeriesPaint(4, Color.orange.darker());//red (200SMA)
        }

        //axis
        _LinearRangeAxis = _Plot.getRangeAxis();//save default one for later
        _LogRangeAxis = new LogarithmicAxis("");

        //create two empty data sets
        _pnlChart = new ChartPanel(_Chart);
        _pnlChart.setRangeZoomable(false);
        _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out
        _pnlChart.setHorizontalAxisTrace(true);//cross hair cursor
        _pnlChart.setVerticalAxisTrace(true);
        add(_pnlChart, BorderLayout.CENTER);

//setup magnifier TODO if next line put in, charts dont' show at all
//        _MagnifierLayer = new JXLayer(_pnlChart);
//        MagnifierUI ui = new MagnifierUI();
//        _MagnifierLayer.setUI(ui);
//        add(_MagnifierLayer);
        _pnlChart.addChartMouseListener(new ChartMouseListener() {
            public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
            }

            public void chartMouseMoved(ChartMouseEvent cme) {
//                if (cme.getEntity() instanceof PlotEntity) //only inside plot
//                    System.out.println(cme.getTrigger().getX() + "   " + cme.getTrigger().getY() + "   " + cme.getEntity());
            }
        });
    }

    //----- public methods -----
    //update Equity _Chart data and repaint
    public void addSeries(TimeSeries[] serieses) {
        _DataSet.removeAllSeries();
        for (TimeSeries s : serieses)
            _DataSet.addSeries(s);
        _Chart.fireChartChanged();
    }

    public void clear() {
        _Plot.clearAnnotations();
        _DataSet.removeAllSeries();
        _Chart.setTitle("");
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

    public void drawPrice(String symbol) throws ParseException {
        _sCurrentSymbol = symbol;  _lblTitle.setText(symbol);
        clear();
        //must read funds from file, otherwise memory will be used up
        HashMap<String, MarketInfo> map = _WatchListModel.getMarketInfoMap();
        MarketInfo mki = map.get(symbol);
        if (mki != null) {//found
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            float[] sma10 = mki.getSma10();
            float[] sma30 = mki.getSma30();
            float[] sma50 = mki.getSma50();
            float[] sma200 = mki.getSma200();

            //first series: price, 2nd series: 10MA, 3rd series: lower envelope
            TimeSeries price_series = new TimeSeries(symbol);
            TimeSeries sma10_series = new TimeSeries("10 SMA");
            TimeSeries sma30_series = new TimeSeries("30 SMA");
            TimeSeries sma50_series = new TimeSeries("50 SMA");
            TimeSeries sma200_series = new TimeSeries("200 SMA");
            for (int index = 150; index >= 0; index--) {//last first (Yahoo data)
                Calendar cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                Day day = new Day(cal.getTime());
                price_series.add(day, quotes.get(index).getUnAdjclose());
                sma10_series.add(day, sma10[index]);
                sma30_series.add(day, sma30[index]);
                sma50_series.add(day, sma50[index]);
                sma200_series.add(day, sma200[index]);
            }

            //show all 4 MA series and price on graph
            TimeSeries[] ps;
            ps = new TimeSeries[5];
            ps[0] = price_series;
            ps[1] = sma10_series;
            ps[2] = sma30_series;
            ps[3] = sma50_series;
            ps[4] = sma200_series;
            addSeries(ps);
            return;
        }
    }

    //----- private methods -----
    private void drawNextSymbol(boolean forward) throws ParseException {
        //find out position of current symbol
        ArrayList<String> members = GroupStore.getInstance().getGroup(_WatchListModel.getWatchlistName());
        int pos = members.indexOf(_sCurrentSymbol);
        if (pos < 0)  return;
        if (forward) {
            pos++;  if (pos == members.size()) pos = 0;
        }
        else {
            pos--;  if (pos < 0) pos = members.size() - 1;
        }
        String next_sym = members.get(pos);
        drawPrice(next_sym);
    }

    //----- accessor -----
    public void setWatchListModel(WatchListModel model) { _WatchListModel = model; }

    //-----instance variables-----
    private JButton _btnPrev = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_3"), FrameworkIcon.ARROW_3D_LEFT);
    private JButton _btnNext = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_2"), FrameworkIcon.ARROW_3D_RIGHT);
    private JButton _btnZoom = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_1"), FrameworkIcon.MAGNIFIER);//magnifier
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    private JFreeChart _Chart;
    protected XYPlot _Plot;
    private TimeSeriesCollection _DataSet;
    private WatchListModel _WatchListModel;
    private String _sCurrentSymbol;
    private JLabel _lblTitle;
    private JXLayer _MagnifierLayer;
    private boolean _bMagnifierOn;
}