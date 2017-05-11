package org.marketsuite.chart.line;

import org.marketsuite.main.datapad.AbstractGraphPanel;
import org.marketsuite.component.Constants;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.GraphUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.watchlist.model.WatchListModel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.marketsuite.component.Constants;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.EntryExitPair;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.datapad.AbstractGraphPanel;
import org.marketsuite.watchlist.model.WatchListModel;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AnalysisGraphPanel extends AbstractGraphPanel {
    /**
     * Create this object based on type to be either DCOM or EMAC.
     * @param dcom_type true for DCOM
     */
    public AnalysisGraphPanel(boolean dcom_type) {
        super(dcom_type);
        _pnlChart.setChart(createPriceVolumeChart(_PriceDataSet, _VolumeDataSet,
            _IndicatorDataSet1, _IndicatorDataSet2, _IndicatorDataSet3));
        drawGraph(_sCurrentSymbol, _bDcomType);
    }

    //----- public methods -----
    public void clearGraph() {
        _PricePlot.clearAnnotations();
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
            if (vol_avg != null && vol_avg[index] > 0)
                volume_avg_series.add(day, vol_avg[index]);
            if (macd != null && macd[index] != 0)
                macd_series.add(day, macd[index]);
            if (rsi != null && rsi[index] > 0)
                rsi_series.add(day, rsi[index]);
            if (dsto != null && dsto[index] > 0)
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
                if (sma10 != null && sma200[index] > 0)
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
        _IndicatorPlot2.addAnnotation(rsi_20);
        _IndicatorPlot2.addAnnotation(rsi_80);
        _IndicatorPlot3.addAnnotation(rsi_20);
        _IndicatorPlot3.addAnnotation(rsi_80);

        //optionally draw annotation marks for IBD50 entry / exit dates
//        _PricePlot.clearAnnotations();
//        ArrayList<EntryExitDates> ibd50_dates = MainModel.getInstance().getIbd50Dates();
//        for (EntryExitDates eed : ibd50_dates) {
//            if (symbol.equals(eed.getSymbol())) {
//                createEntryExitAnnotation(eed.getPairs(), mki);
//                break;
//            }
//        }
    }

    public void zoom(int time_field, int time_offset) {
        _pnlChart.restoreAutoBounds();
        final int itemCount = _PriceDataSet.getItemCount(0);
        final Day day = (Day)_PriceDataSet.getSeries(0).getDataItem(itemCount - 1).getPeriod();

        // Candle stick takes up half day space.
        // Volume price chart's volume information takes up whole day space.
        final long end = day.getFirstMillisecond() + (/*_CurrentType == Type.Candlestick ? (1000 * 60 * 60 * 12) : */ (1000 * 60 * 60 * 24 - 1));
        final Calendar calendar = Calendar.getInstance();

        // -1. Calendar's month is 0 based but JFreeChart's month is 1 based.
        calendar.set(day.getYear(), day.getMonth() - 1, day.getDayOfMonth(), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(time_field, time_offset);//find starting point

        // Candle stick takes up half day space.
        // Volume price chart's volume information does not take up any space.
        final long start = Math.max(0, calendar.getTimeInMillis() - (/*_CurrentType == Type.Candlestick ? (1000 * 60 * 60 * 12) :*/ 0));
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
    private JFreeChart createPriceVolumeChart(XYDataset price_set, XYDataset volume_set,
        XYDataset ind1_set, XYDataset ind2_set, XYDataset ind3_set) {
        //create two axes
        DateAxis time_axis = new DateAxis();
//TODO this hangs forever            time_axis.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
        time_axis.setLowerMargin(0.01); // reduce the default margins
        time_axis.setUpperMargin(0.01);
        final NumberAxis price_axis = new NumberAxis(Constants.COMPONENT_BUNDLE.getString("price"));
        price_axis.setAutoRangeIncludesZero(false);     // override default
        price_axis.setLowerMargin(0.20);                // to leave room for volume bars, 40% from bottom of data
        DecimalFormat format = new DecimalFormat("00.00");
        price_axis.setNumberFormatOverride(format);
        StandardXYToolTipGenerator tip_price = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
            new SimpleDateFormat("MM/dd"), FrameworkConstants.DOLLAR_FORMAT);
        StandardXYToolTipGenerator tip_vol = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
            new SimpleDateFormat("MM/dd"), FrameworkConstants.INT_FORMAT);

        //create plot with 2 data sets and renderer
        XYItemRenderer price_renderer = new XYLineAndShapeRenderer(true, false);
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
        price_renderer.setBaseToolTipGenerator(tip_price);
        _PricePlot = new XYPlot(price_set, time_axis, price_axis, null);
        _PricePlot.setRenderer(0, price_renderer);
        Color edge_color = new Color(238, 0xFF, 232);
        _PricePlot.setBackgroundPaint(new GradientPaint(0, 0, edge_color, 500, 500, edge_color));
        _PricePlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

        //create volume plot with axis, even though not shown, must have it to map w dataset
        NumberAxis volume_axis = new NumberAxis();
        volume_axis.setUpperMargin(1.5);  //ratio between volume and price, to leave room for price line
        volume_axis.setTickLabelsVisible(false);
        _PricePlot.setRangeAxis(1, volume_axis);//without this, volume bar doesn't show
        _PricePlot.setDataset(1, volume_set);
        _PricePlot.mapDatasetToRangeAxis(1, 1);

        //volume renderer
        XYBarRenderer volume_renderer = new XYBarRenderer(0.2);//20% bar width is trimmed
        volume_renderer.setSeriesPaint(0, Color.green.darker());//volume
// TODO switch between green and red for up/down day
        volume_renderer.setSeriesPaint(1, Color.yellow.darker());//volume average
        volume_renderer.setBaseToolTipGenerator(tip_vol);
        volume_renderer.setShadowVisible(false);
        _PricePlot.setRenderer(1, volume_renderer);

        //combine two plots
        CombinedDomainXYPlot combined_plot = new CombinedDomainXYPlot(time_axis);
        combined_plot.add(_PricePlot, 4);
        combined_plot.setGap(0.5);

        //indicator plot w renderer + options
        XYItemRenderer ind_renderer = new StandardXYItemRenderer();
        Color ind_color = Color.blue;//new Color(66, 4, 101);
        ind_renderer.setSeriesPaint(0, ind_color);
        ind_renderer.setSeriesStroke(0, new BasicStroke(1.0f));

        //tooltip for indicator
        StandardXYToolTipGenerator ttg = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
            new SimpleDateFormat("MM/dd"), FrameworkConstants.PRICE_FORMAT);
        ind_renderer.setSeriesToolTipGenerator(0, ttg);
        NumberAxis ind1_axis = new NumberAxis(_bDcomType ? "MACD(6,9)" : "MACD(12,26)");
        _IndicatorPlot1 = new XYPlot(ind1_set, null, ind1_axis, ind_renderer);//does not need time axis
        _IndicatorPlot1.setBackgroundPaint(new Color(0xEB, 0xFF, 0xFF));
        _IndicatorPlot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot1.setRangeZeroBaselineVisible(true);
        combined_plot.add(_IndicatorPlot1, 1);

        //indicator #2
        NumberAxis ind2_axis = new NumberAxis(_bDcomType ? "RSI(9)" : "RSI(14)");
        _IndicatorPlot2 = new XYPlot(ind2_set, null, ind2_axis, ind_renderer);//does not need time axis
        _IndicatorPlot2.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot2.setBackgroundPaint(new Color(0xE6, 0xFF, 0xFF));
        combined_plot.add(_IndicatorPlot2, 1);

        //indicator #3
        NumberAxis ind3_axis = new NumberAxis(_bDcomType ? "DSTO(9)" : "DSTO(14)");
        _IndicatorPlot3 = new XYPlot(ind3_set, null, ind3_axis, ind_renderer);//does not need time axis
        _IndicatorPlot3.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        _IndicatorPlot3.setBackgroundPaint(new Color(0xE0, 0xFF, 0xFF));
        combined_plot.add(_IndicatorPlot3, 1);

        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, combined_plot, false);
//        org.yccheok.jstock.charting.Utils.applyChartThemeEx(chart);
        // Only do it after applying chart theme.
//        org.yccheok.jstock.charting.Utils.setPriceSeriesPaint(renderer1);
//        org.yccheok.jstock.charting.Utils.setVolumeSeriesPaint(volume_renderer);

        // Handle zooming event.
        chart.addChangeListener(getChartChangeListner());
        return chart;
    }

    private void createEntryExitAnnotation(ArrayList<EntryExitPair> ee_pairs, MarketInfo mki) {
        for (EntryExitPair eep : ee_pairs) {
            Calendar entry_cal = eep.getEntry();
            String entry_date = AppUtil.calendarToString(entry_cal);
            FundQuote quote = mki.getFund().findQuoteByDate(entry_date);
            XYPointerAnnotation entry_an = GraphUtil.createArrow(entry_date, quote.getClose(), true);
            _PricePlot.addAnnotation(entry_an);
            Calendar exit_cal = eep.getExit();
            if (exit_cal != null) {
                String exit_date = AppUtil.calendarToString(exit_cal);
                quote = mki.getFund().findQuoteByDate(exit_date);
                XYPointerAnnotation exit_an = GraphUtil.createArrow(exit_date, quote.getClose(), false);
                _PricePlot.addAnnotation(exit_an);
            }
        }
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