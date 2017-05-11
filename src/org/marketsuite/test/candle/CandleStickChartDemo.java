package org.marketsuite.test.candle;

import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.resource.FrameworkConstants;

//to demostrate candle stick charting with JFreeChart
public class CandleStickChartDemo extends ApplicationFrame {
    public CandleStickChartDemo(String title) {
        super(title);
        DefaultHighLowDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createCandlestickChart("SPY Chart", "", "Price", dataset, false);//todo better legend
        XYPlot plot = (XYPlot)chart.getPlot();

        //use renderer to change default paints, gaps
        CandlestickRenderer renderer = (CandlestickRenderer)plot.getRenderer();
        renderer.setAutoWidthGap(0);//only this is necessary to remove spaces between candles, but not during zoom
        renderer.setDrawVolume(false);
        renderer.setDownPaint(new Color(255, 125, 225));//very close to pink
        renderer.setUpPaint(new Color(0, 225, 0));//close to green
        renderer.setUseOutlinePaint(true);

        //axis control
        NumberAxis y_axis = (NumberAxis)plot.getRangeAxis();
        y_axis.setAutoRangeIncludesZero(false);//without this, all bars are squeezed
        y_axis.setUpperMargin(0.02);//percentage of entire range
        y_axis.setLowerMargin(0.02);
        Color edge_color = new Color(0xF0, 0xFF, 0xFF);
        plot.setBackgroundPaint(new GradientPaint(0, 0, edge_color, 500, 500, new Color(0xFF, 0xFF, 0xFF)));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }

    //create candle stick chart via XYPlot using HighLowDataset
    public CandleStickChartDemo(String title, boolean use_factory) {
        super(title);

    }


    private DefaultHighLowDataset createDataset() {
        ArrayList<FundQuote> quotes = null;
        try {
            FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, "SPY", QUOTES_COUNT);
            quotes = fund.getQuote();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (quotes == null) return null;

        int len = quotes.size();
        Date[] date = new Date[len];
        double[] high = new double[len];
        double[] low = new double[len];
        double[] open = new double[len];
        double[] close = new double[len];
        double[] volume = new double[len];
        for (int i = 0; i < len; i++) {
            String date_str = quotes.get(len-1-i).getDate();

            try {
                Date date1 = AppUtil.stringToCalendar(date_str).getTime();
                date[i] = date1;//createDate(2008, 9, i + 1);
            } catch (ParseException e) {
                e.printStackTrace();
//                date[i] = createDate(2008, 9, i + 1);
            }
            high[i] = quotes.get(len-1-i).getHigh();
            low[i] = quotes.get(len-1-i).getLow();
            open[i] = quotes.get(len-1-i).getOpen();
            close[i] = quotes.get(len-1- i).getClose();
            volume[i] = quotes.get(len-1- i).getVolume();
        }
        DefaultHighLowDataset data = new DefaultHighLowDataset(
                "", date, high, low, open, close, volume);
        return data;
    }

    //literals
    private static final int QUOTES_COUNT = 100;

    //test code
    public static void main(String args[]) {
//        FrameworkConstants.DATA_FOLDER = FrameworkConstants.DATA_FOLDER_PC;//default MAC, turn on this line for PC
        FrameworkConstants.adjustDataFolder();
        CandleStickChartDemo chart = new CandleStickChartDemo("SPY");
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}

//        renderer.setAutoWidthFactor(0.5);
//        renderer.setCandleWidth(8);
//    private Date createDate(int year, int month, int date) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month - 1, date);
//        return calendar.getTime();
//    }


