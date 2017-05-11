package org.marketsuite.test.line;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.*;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A component producing combined domain xy plot with price on top and 3 indicators below, both share the same time axis.
 * Functions:
 *   Clear chart
 *   Draw chart for a given symbol with SMA, MACD, DSTO, RSI based on DCOM or EMAC
 *   Draw chart for a given MarketInfo similarly
 *   Manage Accessories - data window, cursor, zoom
 *   Flip charting - draw next / previous chart in the watch list
 *
 */
public abstract class AbstractChartPanel extends JPanel {
    /**
     * CTOR: construct object of either DCOM type of EMAC type.
     * @param dcom_type true = DCOM type, false = EMAC type
     */
    public AbstractChartPanel(boolean dcom_type) {
        _bDcomType = dcom_type;
        setLayout(new MigLayout());

        //title strip
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
            new MigLayout("", "5px[][]10px[]5px[]push[]push[]5px[]5px[]5px", "5px[]5px"));
        JLabel lbl = new JLabel("Symbol:");
        ttl_pnl.add(lbl);
        ttl_pnl.add(_txtSymbol);
        ttl_pnl.add(_btnPrev);
        ttl_pnl.add(_btnNext);
        _lblTitle = new JLabel();  _lblTitle.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl_pnl.add(_lblTitle);
        ttl_pnl.add(_btnChartData);
        ttl_pnl.add(_btnMagnifier);
        ttl_pnl.add(_btnCursor);
        _txtSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //check if MarketInfo is already calculated
                String sym = _txtSymbol.getText().toUpperCase();
                WatchListModel wlm = MainModel.getInstance().getWatchListModel();
                MarketInfo mki = wlm.getMarketInfo(sym);
                if (mki == null)
                    try {
                        mki = MarketUtil.calcMarketInfo(sym, FrameworkConstants.MARKET_QUOTE_LENGTH,
                                new DivergenceOption(5, 90, 3));
                        drawGraph(mki);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                _txtSymbol.select(0, sym.length());
                _btnNext.setEnabled(false);
                _btnPrev.setEnabled(false);
            }
        });

        //north - title strip, east - previous/next flip chart
        _btnPrev.setEnabled(false);
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
        _btnNext.setEnabled(false);
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

        //show data window, show magnifier, toggle cross hair cursor
        _btnChartData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (_dlgChartData != null && _dlgChartData.isVisible())
                    return;

                _dlgChartData = new ChartDataDialog(null);
            }
        });
        _btnMagnifier.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
//TODO remove test code
//                int idx = 0;
//                ArrayList<String> values = new ArrayList<String>();
//                values.add("2012-11-26");
//                values.add("545.5");
//                values.add("547.4");
//                values.add("53.18");
//                values.add("536.88");
//                values.add("1,1233,99");
//                values.add("554.11");
//                values.add("523.76");
//                values.add("515.23");
//                values.add("53.45");
//                values.add("-0.87");
//                values.add("11.34");
//                values.add("34.45");
//                HashMap<String, String> cursor_values = new HashMap<String, String>();
//                for (String key : PROPERTIES)
//                    cursor_values.put(key, values.get(idx++));
//                _dlgChartData = new ChartDataDialog(cursor_values);
////TODO magnifier is wierd
////                if (_bMagnifierOn) {
////                    remove(_MagnifierLayer);
////                    _bMagnifierOn = false;
////                }
////                else {
////                    add(_MagnifierLayer);
////                    _bMagnifierOn = true;
////                }
            }
        });
        _btnCursor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                toggleCursor();
            }
        });
        JPanel cen_pnl = new JPanel();  cen_pnl.setOpaque(false);
        add(ttl_pnl, "dock north");

        //center - two strip of charts - price chart on top
        CombinedDomainXYPlot combined_plot = new CombinedDomainXYPlot();

        //setup both axis
        DateAxis time_axis = new DateAxis("");//for combined plot is sufficient
        time_axis.setLowerMargin(0.005);// reduce the default margins
        time_axis.setUpperMargin(0.005);
        combined_plot.setDomainAxis(time_axis);//MUST have this to show X axis, otherwise default is used
        NumberAxis price_axis = new NumberAxis(Constants.COMPONENT_BUNDLE.getString("price"));
        price_axis.setLowerMargin(0.20);// to leave room for volume bars, % from bottom of data
        price_axis.setAutoRangeIncludesZero(false);//MUST have this, otherwise auto range is default with 0
        price_axis.setNumberFormatOverride(new DecimalFormat("00.00"));

        //price plot w renderer + options
        XYItemRenderer price_renderer = new StandardXYItemRenderer();
        price_renderer.setSeriesStroke(0, new BasicStroke(1.8f));
        price_renderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
        price_renderer.setSeriesStroke(1, new BasicStroke(1.0f));
        price_renderer.setSeriesPaint(1, Color.red.brighter());//red (10SMA)
        price_renderer.setSeriesStroke(2, new BasicStroke(1.0f));
        price_renderer.setSeriesPaint(2, Color.green.darker());//green (30SMA)
        price_renderer.setSeriesStroke(3, new BasicStroke(1.0f));
        price_renderer.setSeriesPaint(3, Color.cyan.darker());//cyan (50SMA)
        price_renderer.setSeriesStroke(4, new BasicStroke(1.2f));
        price_renderer.setSeriesPaint(4, Color.orange.darker());//orange (200SMA)

        //tooltip for price and MA
        StandardXYToolTipGenerator tg1 = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
            new SimpleDateFormat("MM/dd"), FrameworkConstants.DOLLAR_FORMAT);
        price_renderer.setBaseToolTipGenerator(tg1);
//        price_renderer.setSeriesToolTipGenerator(1, tg1);
//        price_renderer.setSeriesToolTipGenerator(2, tg1);
//        price_renderer.setSeriesToolTipGenerator(3, tg1);
//        price_renderer.setSeriesToolTipGenerator(4, tg1);

        //plot
        _PricePlot = new XYPlot(_PriceDataSet, time_axis, price_axis, price_renderer);//must have price renderer
        Color edge_color = new Color(0xF0, 0xFF, 0xFF);
        _PricePlot.setBackgroundPaint(new GradientPaint(0, 0, edge_color, 500, 500, new Color(0xFF, 0xFF, 0xFF)));
        _PricePlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

        NumberAxis volume_axis = new NumberAxis(Constants.COMPONENT_BUNDLE.getString("volume"));
        volume_axis.setUpperMargin(5);  //ratio between volume and price, to leave room for price line
        volume_axis.setTickLabelsVisible(false);
        _PricePlot.setRangeAxis(1, volume_axis);//must specify this to show volume bars
        _PricePlot.setDataset(1, _VolumeDataSet);
        _PricePlot.mapDatasetToDomainAxis(1, 1);//must map to make volume appear

        //volume renderer
        XYBarRenderer volume_renderer = new XYBarRenderer(0.2);//20% bar width is trimmed
        volume_renderer.setSeriesPaint(0, Color.green.darker());//volume
// TODO switch between green and red for up/down day
        volume_renderer.setSeriesPaint(1, Color.yellow.darker());//volume average
        StandardXYToolTipGenerator tip_vol = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
                new SimpleDateFormat("MM/dd"), FrameworkConstants.INT_FORMAT);
        volume_renderer.setBaseToolTipGenerator(tip_vol);
        volume_renderer.setShadowVisible(false);
        _PricePlot.setRenderer(1, volume_renderer);
//        _PricePlot.setDomainPannable(true);
        combined_plot.add(_PricePlot, 4);
        combined_plot.setGap(0.5);

        //indicator plot w renderer + options
        XYItemRenderer ind_renderer = new StandardXYItemRenderer();
        Color ind_color = new Color(66, 4, 101);
        ind_renderer.setSeriesPaint(0, ind_color);
        ind_renderer.setSeriesStroke(0, new BasicStroke(1.0f));

        //tooltip for indicator
        StandardXYToolTipGenerator ttg = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
            new SimpleDateFormat("MM/dd"), FrameworkConstants.PRICE_FORMAT);
        ind_renderer.setSeriesToolTipGenerator(0, ttg);
        NumberAxis ind1_axis = new NumberAxis(_bDcomType ? "MACD(6,9)" : "MACD(12,26)");
        _IndicatorPlot1 = new XYPlot(_IndicatorDataSet1, null, ind1_axis, ind_renderer);//does not need time axis
        _IndicatorPlot1.setBackgroundPaint(new Color(0xEB, 0xFF, 0xFF));
        _IndicatorPlot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot1.setRangeZeroBaselineVisible(true);
        combined_plot.add(_IndicatorPlot1, 1);

        //indicator #2
        NumberAxis ind2_axis = new NumberAxis(_bDcomType ? "RSI(9)" : "RSI(14)");
        _IndicatorPlot2 = new XYPlot(_IndicatorDataSet2, null, ind2_axis, ind_renderer);//does not need time axis
        _IndicatorPlot2.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot2.setBackgroundPaint(new Color(0xE6, 0xFF, 0xFF));
        combined_plot.add(_IndicatorPlot2, 1);

        //indicator #3
        NumberAxis ind3_axis = new NumberAxis(_bDcomType ? "DSTO(9)" : "DSTO(14)");
        _IndicatorPlot3 = new XYPlot(_IndicatorDataSet3, null, ind3_axis, ind_renderer);//does not need time axis
        _IndicatorPlot3.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot3.setBackgroundPaint(new Color(0xE0, 0xFF, 0xFF));
        combined_plot.add(_IndicatorPlot3, 1);

        _Chart = new JFreeChart("",JFreeChart.DEFAULT_TITLE_FONT, combined_plot, false);
        _pnlChart = new ChartPanel(_Chart);
        _pnlChart.setRangeZoomable(false);
        add(_pnlChart, "dock center");
        _pnlChart.addChartMouseListener(new ChartMouseListener() {
            public void chartMouseClicked(ChartMouseEvent event) {
//                int mouseX = event.getTrigger().getX();
//                int mouseY = event.getTrigger().getY();
//                Point2D p = _pnlChart.translateScreenToJava2D(new Point(mouseX, mouseY));
//                CombinedDomainXYPlot plot = (CombinedDomainXYPlot) _Chart.getPlot();
//                PlotRenderingInfo pri = _pnlChart.getChartRenderingInfo().getPlotInfo();
//                int subplotindex = pri.getSubplotIndex(p);
//                PlotRenderingInfo subplotinfo = pri.getSubplotInfo(subplotindex);
//                Rectangle2D plotArea = subplotinfo.getDataArea();
//                ValueAxis rangeAxis = _PricePlot.getRangeAxis();
//                RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
//                double chartY = rangeAxis.java2DToValue(p.getY(), plotArea, rangeAxisEdge);
//                System.out.println("Chart: y = " + chartY + " Yl: "+rangeAxis.getLowerBound()+" Yu: "+rangeAxis.getUpperBound());
//                System.out.println("Y cross hair: "+_PricePlot.getRangeCrosshairValue());
            }
            public void chartMouseMoved(ChartMouseEvent event) {
                refreshDataWindow(event);
//                int mouseX = event.getTrigger().getX();
//                int mouseY = event.getTrigger().getY();
//                Point2D p = _pnlChart.translateScreenToJava2D(new Point(mouseX, mouseY));
//                CombinedDomainXYPlot plot = (CombinedDomainXYPlot) _Chart.getPlot();
//                PlotRenderingInfo pri = _pnlChart.getChartRenderingInfo().getPlotInfo();
//                int subplotindex = pri.getSubplotIndex(p);
//                if (subplotindex == -1)
//                    return;
//
//                PlotRenderingInfo subplotinfo = pri.getSubplotInfo(subplotindex);
//                Rectangle2D plotArea = subplotinfo.getDataArea();
//                ValueAxis rangeAxis = _PricePlot.getRangeAxis();
//                RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
//                double chartY = rangeAxis.java2DToValue(p.getY(), plotArea, rangeAxisEdge);
//                System.out.println("Chart: y = " + chartY + " Yl: "+rangeAxis.getLowerBound()+" Yu: "+rangeAxis.getUpperBound());
//                System.out.println("Y cross hair: "+_PricePlot.getRangeCrosshairValue());
            }
        });
    }

    //----- public methods -----
    public abstract void drawGraph(String symbol) throws ParseException;
    public abstract void drawGraph(MarketInfo mki) throws ParseException;
    public void clearGraph() {
        _PricePlot.clearAnnotations();
        _PriceDataSet.removeAllSeries();
        _VolumeDataSet.removeAllSeries();
        _IndicatorDataSet1.removeAllSeries();
        _IndicatorDataSet2.removeAllSeries();
        _IndicatorDataSet3.removeAllSeries();
        _Chart.setTitle("");
        _lblTitle.setText("");
    }
    public void setTitle(String title) {
        _Chart.setTitle(title);
    }
    public void setLogScale(boolean log_scale) {
        if (log_scale)
            _PricePlot.setRangeAxis(_LogRangeAxis);
        else
            _PricePlot.setRangeAxis(_LinearRangeAxis);
    }
    public void toggleCursor() {
        _pnlChart.setHorizontalAxisTrace(!_pnlChart.getHorizontalAxisTrace());
        _pnlChart.setVerticalAxisTrace(!_pnlChart.getVerticalAxisTrace());
        _pnlChart.repaint();
    }

    //----- protected methods -----
    protected void drawDcomGraph(MarketInfo mki) throws ParseException {
        clearGraph();
        if (mki != null) {//found
            _sCurrentSymbol = mki.getSymbol();  _lblTitle.setText(_sCurrentSymbol);
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            float[] sma10 = mki.getSma10();
            float[] sma30 = mki.getSma30();
            float[] sma50 = mki.getSma50();
            float[] sma200 = mki.getSma200();
            float[] macd = _bDcomType ? mki.getMacd() : mki.getMacdStd();
            float[] rsi = _bDcomType ? mki.getRsi() : mki.getRsiStd();
            float[] dsto = _bDcomType ? mki.getDsto() : mki.getDstoStd();

            //first series: price, 2nd series: 10MA, 3rd series: lower envelope
            TimeSeries price_series = new TimeSeries(_sCurrentSymbol);
            TimeSeries sma10_series = new TimeSeries("10 SMA");
            TimeSeries sma30_series = new TimeSeries("30 SMA");
            TimeSeries sma50_series = new TimeSeries("50 SMA");
            TimeSeries sma200_series = new TimeSeries("200 SMA");
            TimeSeries macd_series = new TimeSeries("MACD");
            TimeSeries rsi_series = new TimeSeries("RSI");
            TimeSeries dsto_series = new TimeSeries("DSTO");
            Day begin_day = new Day(); Day end_day = new Day();
            int start_idx = mki.getSma10().length - 1;
            if (start_idx == (FrameworkConstants.MARKET_QUOTE_LENGTH - 1))
                start_idx = 200;
            for (int index = start_idx; index >= 0; index--) {//last first (Yahoo data)
                Calendar cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                Day day = new Day(cal.getTime());
                if (index == start_idx)  begin_day = day;
                if (index == 0)  end_day = day;
                price_series.add(day, quotes.get(index).getClose());

                //skipping 0 elements, they were dummies
                if (sma10[index] > 0)
                    sma10_series.add(day, sma10[index]);
                if (sma30[index] > 0)
                    sma30_series.add(day, sma30[index]);
                if (sma50[index] > 0)
                    sma50_series.add(day, sma50[index]);
                if (sma200[index] > 0)
                    sma200_series.add(day, sma200[index]);
                if (macd[index] != 0)
                    macd_series.add(day, macd[index]);
                if (rsi[index] > 0)
                    rsi_series.add(day, rsi[index]);
                if (dsto[index] > 0)
                    dsto_series.add(day, dsto[index]);
            }

            //show all 4 MA series and price on graph
            TimeSeries[] ps;
            ps = new TimeSeries[5];
            ps[0] = price_series;
            ps[1] = sma10_series;
            ps[2] = sma30_series;
            ps[3] = sma50_series;
            ps[4] = sma200_series;
            addPriceSeries(ps);

            //add indicator series
            TimeSeries[] is = new TimeSeries[3];
            is[0] = macd_series;
            is[1] = rsi_series;
            is[2] = dsto_series;
            addIndicatorSeries(is);

            //draw annotations lines on RSI and DSTO
            long first_point = begin_day.getFirstMillisecond();
            XYLineAnnotation rsi_20 = new XYLineAnnotation(first_point, 20,
                end_day.getFirstMillisecond(), 20, new BasicStroke(0.5f), Color.gray);
            XYLineAnnotation rsi_80 = new XYLineAnnotation(first_point, 80,
                end_day.getFirstMillisecond(), 80, new BasicStroke(0.5f), Color.gray);
            _IndicatorPlot2.addAnnotation(rsi_20);
            _IndicatorPlot2.addAnnotation(rsi_80);
            _IndicatorPlot3.addAnnotation(rsi_20);
            _IndicatorPlot3.addAnnotation(rsi_80);

            //draw text legend for indicators
//            String note1 = "MACD (6,9,3)", note2 = "RSI (9,3)", note3 = "DSTO (9,3)";
//            if (!_bDcomType) {
//                note1 = "MACD (12,26,9)"; note2 = "RSI (14,3)"; note3 = "DSTO (14,3)";
//            }
//            _IndicatorPlot1.addAnnotation(new XYTextAnnotation(note1, first_point, 0.2));
//            _IndicatorPlot2.addAnnotation(new XYTextAnnotation(note2, first_point, 80));
//            _IndicatorPlot3.addAnnotation(new XYTextAnnotation(note3, first_point, 90));
        }
    }
    protected void drawEmacGraph(MarketInfo mki) throws ParseException {
        clearGraph();
        if (mki != null) {//found
            //title with full name
            _sCurrentSymbol = mki.getSymbol();
            StringBuilder sb = new StringBuilder(_sCurrentSymbol);
            Fundamental fundamental = MainModel.getInstance().getFundamentals().get(_sCurrentSymbol);
            if (fundamental != null)
                sb.append(": ").append(fundamental.getFullName());
            _lblTitle.setText(sb.toString());
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            float[] ema50 = mki.getEma50();
            float[] ema120 = mki.getEma120();
            float[] ema200 = mki.getEma200();
            float[] macd = mki.getMacdStd();
            float[] rsi = mki.getRsiStd();
            float[] dsto = mki.getDstoStd();
            float[] vol_avg = mki.getVolumeAverage();

            //series: price, 50EMA, 120EMA, 200EMA
            TimeSeries price_series = new TimeSeries(_sCurrentSymbol);
            TimeSeries ema50_series = new TimeSeries("50 EMA");
            TimeSeries ema120_series = new TimeSeries("120 EMA");
            TimeSeries ema200_series = new TimeSeries("200 EMA");
            TimeSeries macd_series = new TimeSeries("MACD");
            TimeSeries rsi_series = new TimeSeries("RSI");
            TimeSeries dsto_series = new TimeSeries("DSTO");
            TimeSeries volume_series = new TimeSeries(Constants.COMPONENT_BUNDLE.getString("volume"));
            TimeSeries volume_avg_series = new TimeSeries(Constants.COMPONENT_BUNDLE.getString("volume_average"));
            Day begin_day = new Day(); Day end_day = new Day();
            int start_idx = mki.getSma10().length - 1;
            for (int index = start_idx; index >= 0; index--) {//last first (Yahoo data)
                Calendar cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                Day day = new Day(cal.getTime());
                if (index == start_idx)  begin_day = day;
                if (index == 0)  end_day = day;
                price_series.add(day, quotes.get(index).getClose());

                //skipping 0 elements, they were dummies
                if (ema50[index] > 0)
                    ema50_series.add(day, ema50[index]);
                if (ema120[index] > 0)
                    ema120_series.add(day, ema120[index]);
                if (ema200[index] > 0)
                    ema200_series.add(day, ema200[index]);
                if (macd[index] != 0)
                    macd_series.add(day, macd[index]);
                if (rsi[index] > 0)
                    rsi_series.add(day, rsi[index]);
                if (dsto[index] > 0)
                    dsto_series.add(day, dsto[index]);
                volume_series.add(day, quotes.get(index).getVolume());
                if (vol_avg[index] > 0)
                    volume_avg_series.add(day, vol_avg[index]);
            }

            //show all 3 MA series and price on graph
            TimeSeries[] ps = new TimeSeries[4];
            ps[0] = price_series;
            ps[1] = ema50_series;
            ps[2] = ema120_series;
            ps[3] = ema200_series;
            addPriceSeries(ps);

            TimeSeries[] vs = new TimeSeries[2];//volume and average
            vs[0] = volume_series;
            vs[1] = volume_avg_series;
            addVolumeSeries(vs);

            //add indicator series
            TimeSeries[] is = new TimeSeries[3];
            is[0] = macd_series;
            is[1] = rsi_series;
            is[2] = dsto_series;
            addIndicatorSeries(is);

            //draw annotations on RSI and DSTO
            XYLineAnnotation rsi_20 = new XYLineAnnotation(begin_day.getFirstMillisecond(), 20,
                end_day.getFirstMillisecond(), 20, new BasicStroke(0.5f), Color.gray);
            XYLineAnnotation rsi_80 = new XYLineAnnotation(begin_day.getFirstMillisecond(), 80,
                end_day.getFirstMillisecond(), 80, new BasicStroke(0.5f), Color.gray);
            _IndicatorPlot2.addAnnotation(rsi_20);
            _IndicatorPlot2.addAnnotation(rsi_80);
            _IndicatorPlot3.addAnnotation(rsi_20);
            _IndicatorPlot3.addAnnotation(rsi_80);
        }
    }

    //----- private methods -----
    private void addPriceSeries(TimeSeries[] serieses) {
        _PriceDataSet.removeAllSeries();
        for (TimeSeries s : serieses)
            _PriceDataSet.addSeries(s);

        //NOTE: this call automatically refreshes chart with the assumption that _PriceDataSet MUST
        //  have already had time series in it.  If other type of dataset was used to construct
        //  _PricePlot, then this call won't refresh chart till later zoom/un-zoom
//        _PricePlot.setDataset(_PriceDataSet);//NOTE: use this is sufficient to refresh chart
    }
    private void addVolumeSeries(TimeSeries[] serieses) {
        _VolumeDataSet.removeAllSeries();
        for (TimeSeries s : serieses)
            _VolumeDataSet.addSeries(s);
//        _PricePlot.setDataset(_PriceDataSet);//NOTE: use this is sufficient to refresh chart
    }
    private void addIndicatorSeries(TimeSeries[] serieses) {
        _IndicatorDataSet1.removeAllSeries();
        _IndicatorDataSet2.removeAllSeries();
        _IndicatorDataSet3.removeAllSeries();
        _IndicatorDataSet1.addSeries(serieses[0]);
        _IndicatorDataSet2.addSeries(serieses[1]);
        _IndicatorDataSet3.addSeries(serieses[2]);

        //NOTE: this call automatically refreshes chart with the assumption that _PriceDataSet MUST
        //  have already had time series in it.  If other type of dataset was used to construct
        //  _PricePlot, then this call won't refresh chart till later zoom/un-zoom
//        _IndicatorPlot1.setDataset(_IndicatorDataSet1);//NOTE: use this is sufficient to refresh chart
//        _IndicatorPlot2.setDataset(_IndicatorDataSet2);
//        _IndicatorPlot3.setDataset(_IndicatorDataSet3);
    }
    private void drawNextSymbol(boolean forward) throws ParseException {
        if (_WatchListModel == null)
            return;

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
        drawGraph(next_sym);
    }
    private TimeSeries buildSp500Series() throws ParseException {
        TimeSeries ret = new TimeSeries("SP500");
        //find first data point difference, apply to all SP500 data (normalize)
        for (int index = 500; index >= 0; index--) {
            float sp_close = FrameworkConstants.SP500_DATA.getPrice(index);
            Calendar cal = AppUtil.stringToCalendar(FrameworkConstants.SP500_DATA.getDate(index));
            Day day = new Day(cal.getTime());
            ret.add(day, sp_close);//normalize
        }
        return ret;
    }
    private void refreshDataWindow(ChartMouseEvent mouse_event) {
        if (_dlgChartData == null || !_dlgChartData.isVisible())
            return;

        //translate x screen coordinates into actual date
        int mouseX = mouse_event.getTrigger().getX();
        int mouseY = mouse_event.getTrigger().getY();
        Point2D p = _pnlChart.translateScreenToJava2D(new Point(mouseX, mouseY));
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) _Chart.getPlot();
        PlotRenderingInfo ri = _pnlChart.getChartRenderingInfo().getPlotInfo();
        int plot_idx = ri.getSubplotIndex(p);
        if (plot_idx == -1)//outside plot area
            return;

        PlotRenderingInfo sub_info = ri.getSubplotInfo(plot_idx);
        Rectangle2D plotArea = sub_info.getDataArea();
        ValueAxis domain_axis = _PricePlot.getDomainAxis();
        RectangleEdge domain_edge = plot.getDomainAxisEdge();
        double cursor_milli = domain_axis.java2DToValue(p.getX(), plotArea, domain_edge);
        if (_PriceDataSet.getSeriesCount() == 0)
            return;//no data yet

        int itemCount = _PriceDataSet.getItemCount(0);

        //loop thru data items using X value to find nearest match
//        double matched_milli = 0;
        int idx, data_index;
        for (idx = 0; idx < itemCount; idx++) {
            double cur_milli = _PriceDataSet.getStartXValue(plot_idx, idx);
            if (cursor_milli >= cur_milli)
                continue;//test next

            if (idx == 0)
                return;//before first data point, skip

            double last_milli = _PriceDataSet.getStartXValue(plot_idx, idx-1);
            double to_last = cursor_milli - last_milli;
            double to_cur = cur_milli - cursor_milli;
            if (to_cur < to_last) {//nearest is cur_milli
//                matched_milli = cur_milli;
                data_index = idx;
            }
            else {
//                matched_milli = last_milli;
                data_index = idx - 1;
            }
//            System.out.println(" Idx / Index = " + idx + " / " + data_index + "  Y = " + _PriceDataSet.getY(0, data_index));

            //look up all other values, update data window
            MarketInfo mki = _WatchListModel.getMarketInfo(_sCurrentSymbol);
            FundData fund = mki.getFund();
            int di = 150 - data_index;//Yahoo data inversed
            FundQuote quote = fund.getQuote().get(di);
            String date = quote.getDate();
            String open = FrameworkConstants.DOLLAR_FORMAT.format(quote.getOpen());
            String high = FrameworkConstants.DOLLAR_FORMAT.format(quote.getHigh());
            String low = FrameworkConstants.DOLLAR_FORMAT.format(quote.getLow());
            String volume = FrameworkConstants.FORMAT_NUMBERS.format(quote.getVolume());

            //locate time series, use index to get values
            String close = FrameworkConstants.DOLLAR_FORMAT.format(_PriceDataSet.getY(0, data_index).doubleValue());
            String ma_10 = FrameworkConstants.DOLLAR_FORMAT.format(_PriceDataSet.getY(1, data_index).doubleValue());
            String ma_30 = FrameworkConstants.DOLLAR_FORMAT.format(_PriceDataSet.getY(2, data_index).doubleValue());
            String ma_50 = FrameworkConstants.DOLLAR_FORMAT.format(_PriceDataSet.getY(3, data_index).doubleValue());
            String ma_200 = FrameworkConstants.DOLLAR_FORMAT.format(_PriceDataSet.getY(4, data_index).doubleValue());
            String macd = FrameworkConstants.PRICE_FORMAT.format(_IndicatorDataSet1.getY(0, data_index).doubleValue());
            String rsi = FrameworkConstants.PRICE_FORMAT.format(_IndicatorDataSet2.getY(0, data_index).doubleValue());
            String dsto = FrameworkConstants.PRICE_FORMAT.format(_IndicatorDataSet3.getY(0, data_index).doubleValue());
            ArrayList<String> values = new ArrayList<String>();
            values.add(date); values.add(open); values.add(high); values.add(low); values.add(close);
            values.add(volume); values.add(ma_10); values.add(ma_30); values.add(ma_50); values.add(ma_200);
            values.add(macd); values.add(rsi); values.add(dsto);
            HashMap<String, String> value_map = new HashMap<String, String>();
            int vidx = 0;
            for (String key : ChartDataDialog.PROPERTIES)
                value_map.put(key, values.get(vidx++));
            _dlgChartData.populate(value_map);
            return;
        }
    }

    //----- accessor -----
    public void setWatchListModel(WatchListModel model) {
        _WatchListModel = model;
        boolean no_grp = _WatchListModel.getMarketInfoMap().size() <= 1;
        _btnPrev.setEnabled(!no_grp);
        _btnNext.setEnabled(!no_grp);
    }

    public void enableNextPrev(boolean enable) {
        _btnNext.setEnabled(enable);
        _btnPrev.setEnabled(enable);
    }

    //-----instance variables-----
    protected boolean _bDcomType;//DCOM or EMAC
    protected ChartPanel _pnlChart;
    protected NameField _txtSymbol = new NameField(5);
    private JButton _btnPrev = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_3"), FrameworkIcon.ARROW_3D_LEFT);
    private JButton _btnNext = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_2"), FrameworkIcon.ARROW_3D_RIGHT);
    private JButton _btnCursor = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_9"), FrameworkIcon.CURSOR);
    private JButton _btnMagnifier = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_1"), FrameworkIcon.MAGNIFIER);
    private JButton _btnChartData = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_4"), FrameworkIcon.REPORT);//chart data
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    private JFreeChart _Chart;
    protected WatchListModel _WatchListModel;
    private String _sCurrentSymbol;
    protected JLabel _lblTitle;
    private JXLayer _MagnifierLayer;
    private boolean _bMagnifierOn;
    private ChartDataDialog _dlgChartData;
    protected XYPlot _PricePlot, _IndicatorPlot1, _IndicatorPlot2, _IndicatorPlot3;
    private TimeSeriesCollection _PriceDataSet = new TimeSeriesCollection();
    private TimeSeriesCollection _VolumeDataSet = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet1 = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet2 = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet3 = new TimeSeriesCollection();
}