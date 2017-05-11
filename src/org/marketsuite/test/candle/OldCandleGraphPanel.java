package org.marketsuite.test.candle;

import org.marketsuite.main.datapad.AbstractGraphPanel;
import org.marketsuite.component.Constants;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.watchlist.model.WatchListModel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.watchlist.model.WatchListModel;

import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OldCandleGraphPanel extends AbstractGraphPanel {
    /**
     * Create this object based on type to be either DCOM or EMAC.
     * @param dcom_type true for DCOM
     */
    public OldCandleGraphPanel(boolean dcom_type) {
        super(dcom_type);
        _pnlChart.setChart(createCandlestickChart(_OhlcDataset, _VolumeDataSet,
            _IndicatorDataSet1, _IndicatorDataSet2, _IndicatorDataSet3));
        drawGraph(_sCurrentSymbol, _bDcomType);
    }

    //----- public methods -----
    public void clearGraph() {
//        _PricePlot.clearAnnotations();
        _PriceDataSet.removeAllSeries();
        _VolumeDataSet.removeAllSeries();
        _IndicatorDataSet1.removeAllSeries();
        _IndicatorDataSet2.removeAllSeries();
        _IndicatorDataSet3.removeAllSeries();
    }
    public void drawGraph(String symbol, boolean dcom_type) {
        if (symbol == null || symbol.equals("")) //no symbol
            return;
        _sCurrentSymbol = symbol;
        clearGraph();
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        MarketInfo mki = wlm.getMarketInfo(symbol);
        if (mki == null)
            try {
                mki = MarketUtil.calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
            } catch (Exception e) {
                e.printStackTrace();
//TODO popup warning dialog
                return;
            }

        //setup data structure for datapad and zoom ranges
        buildChartDatas(mki); _OhlcDataset = createOhlcDataset(mki.getFund().getQuote());

        FundData fund = mki.getFund();
        ArrayList<FundQuote> quotes = fund.getQuote();
        int start_idx = mki.getSma10().length - 1;
        float[] vol_avg = mki.getVolumeAverage();
        float[] macd = dcom_type ? mki.getMacd() : mki.getMacdStd();
        float[] rsi = dcom_type ? mki.getRsi() : mki.getRsiStd();
        float[] dsto = dcom_type ? mki.getDsto() : mki.getDstoStd();
        TimeSeries price_series = new TimeSeries(symbol);
        TimeSeries macd_series = new TimeSeries("MACD");
        TimeSeries rsi_series = new TimeSeries("RSI");
        TimeSeries dsto_series = new TimeSeries("DSTO");
        TimeSeries volume_series = new TimeSeries(Constants.COMPONENT_BUNDLE.getString("volume"));
        TimeSeries volume_avg_series = new TimeSeries(Constants.COMPONENT_BUNDLE.getString("volume_average"));

        //create time series from these arrays
        Day begin_day = new Day(); Day end_day = new Day();
        for (int index = start_idx/*fund.getSize() - 1*/; index >= 0; index--) {//last first (Yahoo data)
            Calendar cal = null;
            try {
                cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Day day = new Day(cal.getTime());
            if (index == start_idx)  begin_day = day;
            if (index == 0)  end_day = day;
            price_series.add(day, quotes.get(index).getClose());
            volume_series.add(day, quotes.get(index).getVolume());
            if (vol_avg[index] > 0)
                volume_avg_series.add(day, vol_avg[index]);
            if (macd[index] != 0)
                macd_series.add(day, macd[index]);
            if (rsi[index] > 0)
                rsi_series.add(day, rsi[index]);
            if (dsto[index] > 0)
                dsto_series.add(day, dsto[index]);
        }
        _PriceDataSet.addSeries(price_series);//no need to createPriceVolumeChart() again
        _VolumeDataSet.addSeries(volume_series);
        _VolumeDataSet.addSeries(volume_avg_series);
        _IndicatorDataSet1.addSeries(macd_series);
        _IndicatorDataSet2.addSeries(rsi_series);
        _IndicatorDataSet3.addSeries(dsto_series);

        //split between DCOM or EMAC
        if (dcom_type) {
            float[] sma10 = mki.getSma10();
            float[] sma30 = mki.getSma30();
            float[] sma50 = mki.getSma50();
            float[] sma200 = mki.getSma200();
            TimeSeries sma10_series = new TimeSeries("10 SMA");
            TimeSeries sma30_series = new TimeSeries("30 SMA");
            TimeSeries sma50_series = new TimeSeries("50 SMA");
            TimeSeries sma200_series = new TimeSeries("200 SMA");
            for (int index = start_idx/*fund.getSize() - 1*/; index >= 0; index--) {//last first (Yahoo data)
                Calendar cal = null;
                try {
                    cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Day day = new Day(cal.getTime());

                //skipping 0 elements in MAs, they were dummies
                if (sma10[index] > 0)
                    sma10_series.add(day, sma10[index]);
                if (sma30[index] > 0)
                    sma30_series.add(day, sma30[index]);
                if (sma50[index] > 0)
                    sma50_series.add(day, sma50[index]);
                if (sma200[index] > 0)
                    sma200_series.add(day, sma200[index]);
            }
            _PriceDataSet.addSeries(sma10_series);
            _PriceDataSet.addSeries(sma30_series);
            _PriceDataSet.addSeries(sma50_series);
            _PriceDataSet.addSeries(sma200_series);
        }
        else {//EMAC special
            float[] ema50 = mki.getEma50();
            float[] ema120 = mki.getEma120();
            float[] ema200 = mki.getEma200();
            TimeSeries ema50_series = new TimeSeries("50 EMA");
            TimeSeries ema120_series = new TimeSeries("120 EMA");
            TimeSeries ema200_series = new TimeSeries("200 EMA");
            for (int index = start_idx/*fund.getSize() - 1*/; index >= 0; index--) {//last first (Yahoo data)
                Calendar cal = null;
                try {
                    cal = AppUtil.stringToCalendar(quotes.get(index).getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Day day = new Day(cal.getTime());

                //skipping 0 elements in MAs, they were dummies
                if (ema50[index] > 0)
                    ema50_series.add(day, ema50[index]);
                if (ema120[index] > 0)
                    ema120_series.add(day, ema120[index]);
                if (ema200[index] > 0)
                    ema200_series.add(day, ema200[index]);
            }
            _PriceDataSet.addSeries(ema50_series);
            _PriceDataSet.addSeries(ema120_series);
            _PriceDataSet.addSeries(ema200_series);
        }

        //draw annotations lines on RSI and DSTO
        long first_point = begin_day.getFirstMillisecond();
        XYLineAnnotation rsi_20 = new XYLineAnnotation(first_point, 20,
                end_day.getFirstMillisecond(), 20, new BasicStroke(0.8f), Color.green.darker());
        XYLineAnnotation rsi_80 = new XYLineAnnotation(first_point, 80,
                end_day.getFirstMillisecond(), 80, new BasicStroke(0.8f), Color.magenta);
//        _IndicatorPlot2.addAnnotation(rsi_20);
//        _IndicatorPlot2.addAnnotation(rsi_80);
//        _IndicatorPlot3.addAnnotation(rsi_20);
//        _IndicatorPlot3.addAnnotation(rsi_80);
    }
    public void zoom(int field, int amount) {
        _pnlChart.restoreAutoBounds();
        final int itemCount = _PriceDataSet.getItemCount(0);
        final Day day = (Day)_PriceDataSet.getSeries(0).getDataItem(itemCount - 1).getPeriod();

        // Candle stick takes up half day space.
        // Volume price chart's volume information takes up whole day space.
        final long end = day.getFirstMillisecond() + (/*_CurrentType == Type.Candlestick ?*/ (1000 * 60 * 60 * 12) /*: (1000 * 60 * 60 * 24 - 1)*/);
        final Calendar calendar = Calendar.getInstance();

        // -1. Calendar's month is 0 based but JFreeChart's month is 1 based.
        calendar.set(day.getYear(), day.getMonth() - 1, day.getDayOfMonth(), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(field, amount);//find starting point

        // Candle stick takes up half day space.
        // Volume price chart's volume information does not take up any space.
        final long start = Math.max(0, calendar.getTimeInMillis() - (/*_CurrentType == Type.Candlestick ?*/ (1000 * 60 * 60 * 12) /*: 0*/));
        final ValueAxis time_axis = getMainPlot().getDomainAxis();
        if (_PriceDataSet.getSeries(0).getItemCount() > 0) {
            if (start < _PriceDataSet.getSeries(0).getTimePeriod(0).getFirstMillisecond()) {
                // To prevent zoom-out too much.
                // This happens when user demands for 10 years zoom, where we
                // are only having 5 years data.
                return;
            }
        }
        time_axis.setRange(start, end);

        //find min, max on price axis, max volume for volume axis
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double max_volume = Double.MIN_VALUE;
        for (int i = itemCount - 1; i >= 0; i--) {//from most recent to oldest
            final TimeSeriesDataItem item = _PriceDataSet.getSeries(0).getDataItem(i);
            final Day d = (Day)item.getPeriod();
            if (d.getFirstMillisecond() < start)
                break;
//TODO get H L from mki
            final DefaultHighLowDataset defaultHighLowDataset = (DefaultHighLowDataset)_OhlcDataset;
            final double high = defaultHighLowDataset.getHighValue(0, i);
            final double low = defaultHighLowDataset.getLowValue(0, i);
            final double volume = defaultHighLowDataset.getVolumeValue(0, i);
            if (max < high)
                max = high;
            if (min > low)
                min = low;
            if (max_volume < volume)
                max_volume = volume;
        }
        if (min > max)
            return;

        final ValueAxis price_axis = getMainPlot().getRangeAxis();
        final Range rangeAxisRange = price_axis.getRange();

        //increase each side by 1%
        double tolerance = 0.01 * (max - min);

        //tolerance must within range [0.01, 1.0]
        tolerance = Math.min(Math.max(0.01, tolerance), 1.0);

        //range must within the original chart range.
        min = Math.max(rangeAxisRange.getLowerBound(), min - tolerance);
        max = Math.min(rangeAxisRange.getUpperBound(), max + tolerance);
        getMainPlot().getRangeAxis().setRange(min, max);
        if (getMainPlot().getRangeAxisCount() > 1) {
            final double volumeUpperBound = getMainPlot().getRangeAxis(1).getRange().getUpperBound();
            final double suggestedVolumneUpperBound = max_volume * 4;

            //to prevent over zoom-in.
            if (suggestedVolumneUpperBound < volumeUpperBound)
                getMainPlot().getRangeAxis(1).setRange(0, suggestedVolumneUpperBound);
        }
    }
    //dataset [0] = earliest, [size -1] = most recent
    public OHLCDataset createOhlcDataset(ArrayList<FundQuote> quotes) {
        final int size = quotes.size();
        Date[] date = new Date[size];
        double[] high = new double[size];
        double[] low = new double[size];
        double[] open = new double[size];
        double[] close = new double[size];
        double[] volume = new double[size];
        int i = size - 1;//highest index of data = most recent date
        for(FundQuote quote : quotes) {//first quote = most recent (YAHOO)
            try {
                date[i] = AppUtil.stringToCalendar(quote.getDate()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                date[i] = Calendar.getInstance().getTime();
                return null;
            }
            high[i] = quote.getHigh();
            low[i] = quote.getLow();
            open[i] = quote.getOpen();
            close[i] = quote.getClose();
            volume[i] = quote.getVolume();
            i--;
        }
        return new DefaultHighLowDataset(Constants.COMPONENT_BUNDLE.getString("price"), date, high, low, open, close, volume);
    }
    //----- private methods -----
    private JFreeChart createCandlestickChart(OHLCDataset priceOHLCDataset, XYDataset volume_set,
            XYDataset ind1_set, XYDataset ind2_set, XYDataset ind3_set) {
        final ValueAxis time_axis = new DateAxis();
        final NumberAxis price_axis = new NumberAxis(Constants.COMPONENT_BUNDLE.getString("price"));
        price_axis.setAutoRangeIncludesZero(false);
        price_axis.setUpperMargin(0.0);
        price_axis.setLowerMargin(0.0);
        _PricePlot = new XYPlot(priceOHLCDataset, time_axis, price_axis, null);
        final CandlestickRenderer candlestickRenderer = new CandlestickRenderer();
        _PricePlot.setRenderer(candlestickRenderer);

        // Give good width when zoom in, but too slow in calculation.
        ((CandlestickRenderer)_PricePlot.getRenderer()).setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
        CombinedDomainXYPlot combo_plot = new CombinedDomainXYPlot(time_axis);
        combo_plot.add(_PricePlot, 3);
        combo_plot.setGap(8.0);
        JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combo_plot, true);
//        org.yccheok.jstock.charting.Utils.applyChartThemeEx(chart);

        // Handle zooming event.
        chart.addChangeListener(this.getChartChangeListner());
        return chart;
    }

    //----- variables -----
    protected XYPlot _PricePlot, _IndicatorPlot1, _IndicatorPlot2, _IndicatorPlot3;
    private TimeSeriesCollection _PriceDataSet = new TimeSeriesCollection();
    private TimeSeriesCollection _VolumeDataSet = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet1 = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet2 = new TimeSeriesCollection();
    private TimeSeriesCollection _IndicatorDataSet3 = new TimeSeriesCollection();
    private OHLCDataset _OhlcDataset;

}