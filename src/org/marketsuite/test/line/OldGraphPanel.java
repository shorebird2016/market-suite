package org.marketsuite.test.line;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.marketsuite.component.graph.AbstractGraphPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A Combined domain xy plot with price on top and indicator below, both share the same time axis.
 */
public class OldGraphPanel extends AbstractGraphPanel {
    public OldGraphPanel() {
        setLayout(new BorderLayout());

        //north - title strip, east - last flipchart
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

        //northeast - next flipchart
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
        east_pnl.add(Box.createHorizontalStrut(15));

        //northeast - show data window, show magnifier, toggle cross hair cursor
        east_pnl.add(_btnChartData);
        _btnChartData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (_dlgChartData != null && _dlgChartData.isVisible())
                    return;

                _dlgChartData = new ChartDataDialog(null);
            }
        });
        east_pnl.add(_btnMagnifier);
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
        east_pnl.add(_btnCursor);
        _btnCursor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                toggleCursor();
            }
        });
        JPanel cen_pnl = new JPanel();  cen_pnl.setOpaque(false);
        _lblTitle = new JLabel();  _lblTitle.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        cen_pnl.add(_lblTitle);
        add(WidgetUtil.createTitleStrip(null, cen_pnl, east_pnl), BorderLayout.NORTH);

        //center - two strip of charts - price chart on top
        CombinedDomainXYPlot combined_plot = new CombinedDomainXYPlot();

        //setup both axis
        DateAxis time_axis = new DateAxis("");//for combined plot is sufficient
        combined_plot.setDomainAxis(time_axis);//MUST have this to show X axis, otherwise default is used
        NumberAxis price_axis = new NumberAxis("Price");
        price_axis.setAutoRangeIncludesZero(false);//MUST have this, otherwise auto range is default with 0

        //price plot w renderer + options
        XYItemRenderer price_renderer = new StandardXYItemRenderer();
        price_renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        price_renderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
        price_renderer.setSeriesStroke(1, new BasicStroke(1.0f));
        price_renderer.setSeriesPaint(1, Color.red.brighter());//cyan (10SMA)
        price_renderer.setSeriesStroke(2, new BasicStroke(1.0f));
        price_renderer.setSeriesPaint(2, Color.green.darker());//orange (30SMA)
        price_renderer.setSeriesStroke(3, new BasicStroke(1.0f));
        price_renderer.setSeriesPaint(3, Color.cyan.darker());//green (50SMA)
        price_renderer.setSeriesStroke(4, new BasicStroke(1.2f));
        price_renderer.setSeriesPaint(4, Color.orange.darker());//red (200SMA)
        _PricePlot = new XYPlot(_PriceDataSet, null, price_axis, price_renderer);//does not need time axis
        Color edge_color = new Color(0xF0, 0xFF, 0xFF);
        _PricePlot.setBackgroundPaint(new GradientPaint(0, 0, edge_color, 500, 500, new Color(0xFF, 0xFF, 0xFF)));
        _PricePlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
//        _PricePlot.setDomainPannable(true);
        combined_plot.add(_PricePlot, 3);

        //indicator plot w renderer + options
        XYItemRenderer ind_renderer = new StandardXYItemRenderer();
        Color ind_color = new Color(66, 4, 101);
        ind_renderer.setSeriesPaint(0, ind_color);
        ind_renderer.setSeriesStroke(0, new BasicStroke(1.0f));
        NumberAxis ind1_axis = new NumberAxis("MACD");
        _IndicatorPlot1 = new XYPlot(_IndicatorDataSet1, null, ind1_axis, ind_renderer);//does not need time axis
        _IndicatorPlot1.setBackgroundPaint(new Color(0xEB, 0xFF, 0xFF));
        _IndicatorPlot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot1.setRangeZeroBaselineVisible(true);
        combined_plot.add(_IndicatorPlot1, 1);

        //indicator #2
        NumberAxis ind2_axis = new NumberAxis("RSI");
        _IndicatorPlot2 = new XYPlot(_IndicatorDataSet2, null, ind2_axis, ind_renderer);//does not need time axis
        _IndicatorPlot2.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot2.setBackgroundPaint(new Color(0xE6, 0xFF, 0xFF));
        combined_plot.add(_IndicatorPlot2, 1);

        //indicator #3
        NumberAxis ind3_axis = new NumberAxis("Stochastic");
        _IndicatorPlot3 = new XYPlot(_IndicatorDataSet3, null, ind3_axis, ind_renderer);//does not need time axis
        _IndicatorPlot3.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot3.setBackgroundPaint(new Color(0xE0, 0xFF, 0xFF));
        combined_plot.add(_IndicatorPlot3, 1);

        _Chart = new JFreeChart("",JFreeChart.DEFAULT_TITLE_FONT, combined_plot, false);
        _pnlChart = new ChartPanel(_Chart);
        _pnlChart.setRangeZoomable(false);
        add(_pnlChart, BorderLayout.CENTER);
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
    public void clearGraph() {
        _PricePlot.clearAnnotations();
        _PriceDataSet.removeAllSeries();
        _IndicatorDataSet1.removeAllSeries();
        _IndicatorDataSet2.removeAllSeries();
        _IndicatorDataSet3.removeAllSeries();
        _Chart.setTitle("");
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
    public void drawSymbol(String symbol) throws ParseException {
        _sCurrentSymbol = symbol;  _lblTitle.setText(symbol);
        clearGraph();
        HashMap<String, MarketInfo> map = _WatchListModel.getMarketInfoMap();
        MarketInfo mki = map.get(symbol);
        if (mki != null) {//found
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            float[] sma10 = mki.getSma10();
            float[] sma30 = mki.getSma30();
            float[] sma50 = mki.getSma50();
            float[] sma200 = mki.getSma200();
            float[] macd = mki.getMacd();
            float[] rsi = mki.getRsi();
            float[] dsto = mki.getDsto();

            //first series: price, 2nd series: 10MA, 3rd series: lower envelope
            TimeSeries price_series = new TimeSeries(symbol);
            TimeSeries sma10_series = new TimeSeries("10 SMA");
            TimeSeries sma30_series = new TimeSeries("30 SMA");
            TimeSeries sma50_series = new TimeSeries("50 SMA");
            TimeSeries sma200_series = new TimeSeries("200 SMA");
            TimeSeries macd_series = new TimeSeries("MACD");
            TimeSeries rsi_series = new TimeSeries("RSI");
            TimeSeries dsto_series = new TimeSeries("DSTO");
            Day begin_day = new Day(); Day end_day = new Day();
            for (int index = 150; index >= 0; index--) {//last first (Yahoo data)
                Calendar cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                Day day = new Day(cal.getTime());
                if (index == 150)  begin_day = day;
                if (index == 0)  end_day = day;
                price_series.add(day, quotes.get(index).getClose());
                sma10_series.add(day, sma10[index]);
                sma30_series.add(day, sma30[index]);
                sma50_series.add(day, sma50[index]);
                sma200_series.add(day, sma200[index]);
                macd_series.add(day, macd[index]);
                rsi_series.add(day, rsi[index]);
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

    public void drawGraph(MarketInfo mki) {
        clearGraph();
        if (mki != null) {//found
            _sCurrentSymbol = mki.getSymbol();  _lblTitle.setText(_sCurrentSymbol);
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            float[] sma10 = mki.getSma10();
            float[] sma30 = mki.getSma30();
            float[] sma50 = mki.getSma50();
            float[] sma200 = mki.getSma200();
            float[] macd = mki.getMacd();
            float[] rsi = mki.getRsi();
            float[] dsto = mki.getDsto();

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
            for (int index = 200; index >= 0; index--) {//last first (Yahoo data)
                Calendar cal = null;
                try {
                    cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                } catch (ParseException e) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("cw_lbl_1") + " " +
                            _sCurrentSymbol + ": " + quotes.get(index).getDate(),
                        MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                    e.printStackTrace();
                }
                Day day = new Day(cal.getTime());
                if (index == 150)  begin_day = day;
                if (index == 0)  end_day = day;
                price_series.add(day, quotes.get(index).getClose());
                sma10_series.add(day, sma10[index]);
                sma30_series.add(day, sma30[index]);
                sma50_series.add(day, sma50[index]);
                sma200_series.add(day, sma200[index]);
                macd_series.add(day, macd[index]);
                rsi_series.add(day, rsi[index]);
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
        _PricePlot.setDataset(_PriceDataSet);//NOTE: use this is sufficient to refresh chart
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
        _IndicatorPlot1.setDataset(_IndicatorDataSet1);//NOTE: use this is sufficient to refresh chart
        _IndicatorPlot2.setDataset(_IndicatorDataSet2);
        _IndicatorPlot3.setDataset(_IndicatorDataSet3);
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
        drawSymbol(next_sym);
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
    public void setWatchListModel(WatchListModel model) { _WatchListModel = model; }

    //-----instance variables-----
    private JButton _btnPrev = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_3"), FrameworkIcon.ARROW_3D_LEFT);
    private JButton _btnNext = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_2"), FrameworkIcon.ARROW_3D_RIGHT);
    private JButton _btnCursor = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_9"), FrameworkIcon.CURSOR);
    private JButton _btnMagnifier = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_1"), FrameworkIcon.MAGNIFIER);
    private JButton _btnChartData = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_4"), FrameworkIcon.REPORT);//chart data
    protected ValueAxis _LinearRangeAxis;
    protected ValueAxis _LogRangeAxis;
    private JFreeChart _Chart;
    private WatchListModel _WatchListModel;
    private String _sCurrentSymbol;
    private JLabel _lblTitle;
    private JXLayer _MagnifierLayer;
    private boolean _bMagnifierOn;
    private ChartDataDialog _dlgChartData;
    private XYPlot _PricePlot, _IndicatorPlot1, _IndicatorPlot2, _IndicatorPlot3;
    private TimeSeriesCollection _PriceDataSet = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet1 = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet2 = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet3 = new TimeSeriesCollection();

//    //----- literals -----
//    static final String[] PROPERTIES = {
//        "Date", "Open", "High", "Low", "Close", "Volume", "10 MA", "30 MA", "50 MA", "200 MA",
//        "MACD", "RSI", "Stochastic"
//    };
    //TODO REMOVE SAMPLE
//    private XYDataset createDataset1() {
//
//        // create dataset 1...
//        final XYSeries series1 = new XYSeries("Series 1");
//        series1.add(10.0, 12353.3);
//        series1.add(20.0, 13734.4);
//        series1.add(30.0, 14525.3);
//        series1.add(40.0, 13984.3);
//        series1.add(50.0, 12999.4);
//        series1.add(60.0, 14274.3);
//        series1.add(70.0, 15943.5);
//        series1.add(80.0, 14845.3);
//        series1.add(90.0, 14645.4);
//        series1.add(100.0, 16234.6);
//        series1.add(110.0, 17232.3);
//        series1.add(120.0, 14232.2);
//        series1.add(130.0, 13102.2);
//        series1.add(140.0, 14230.2);
//        series1.add(150.0, 11235.2);
//
//        final XYSeries series2 = new XYSeries("Series 2");
//        series2.add(10.0, 15000.3);
//        series2.add(20.0, 11000.4);
//        series2.add(30.0, 17000.3);
//        series2.add(40.0, 15000.3);
//        series2.add(50.0, 14000.4);
//        series2.add(60.0, 12000.3);
//        series2.add(70.0, 11000.5);
//        series2.add(80.0, 12000.3);
//        series2.add(90.0, 13000.4);
//        series2.add(100.0, 12000.6);
//        series2.add(110.0, 13000.3);
//        series2.add(120.0, 17000.2);
//        series2.add(130.0, 18000.2);
//        series2.add(140.0, 16000.2);
//        series2.add(150.0, 17000.2);
//
//        final XYSeriesCollection collection = new XYSeriesCollection();
//        collection.addSeries(series1);
//        collection.addSeries(series2);
//        return collection;
//
//    }
//    private XYDataset createDataset2() {
//
//        // create dataset 2...
//        final XYSeries series2 = new XYSeries("Series 3");
//
//        series2.add(10.0, 16853.2);
//        series2.add(20.0, 19642.3);
//        series2.add(30.0, 18253.5);
//        series2.add(40.0, 15352.3);
//        series2.add(50.0, 13532.0);
//        series2.add(100.0, 12635.3);
//        series2.add(110.0, 13998.2);
//        series2.add(120.0, 11943.2);
//        series2.add(130.0, 16943.9);
//        series2.add(140.0, 17843.2);
//        series2.add(150.0, 16495.3);
//        series2.add(160.0, 17943.6);
//        series2.add(170.0, 18500.7);
//        series2.add(180.0, 19595.9);
//
//        return new XYSeriesCollection(series2);
//
//    }

}