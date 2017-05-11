package org.marketsuite.test.candle;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.XYDataset;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class Candle1 extends JFrame {
    public Candle1(String stockSymbol) {
        super("CandlestickDemo");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DateAxis    domainAxis       = new DateAxis("Date");
        NumberAxis rangeAxis        = new NumberAxis("Price");
        CandlestickRenderer renderer = new CandlestickRenderer();
        XYDataset   dataset          = getDataSet(stockSymbol);

        XYPlot mainPlot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);

        //Do some setting up, see the API Doc
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setDrawVolume(false);
        rangeAxis.setAutoRangeIncludesZero(false);
        domainAxis.setTimeline( SegmentedTimeline.newMondayThroughFridayTimeline() );

        //Now create the chart and chart panel
        JFreeChart chart = new JFreeChart(stockSymbol, null, mainPlot, false);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(600, 300));

        mainPlot.setDomainPannable(true);
        mainPlot.setRangePannable(true);

        this.add(chartPanel);
        this.pack();
    }
    protected AbstractXYDataset getDataSet(String stockSymbol) {
        //This is the dataset we are going to create
        DefaultOHLCDataset result = null;
        //This is the data needed for the dataset
        OHLCDataItem[] data;

        //This is where we go get the data, replace with your own data source
        data = getData(stockSymbol);

        //Create a dataset, an Open, High, Low, Close dataset
        result = new DefaultOHLCDataset(stockSymbol, data);

        return result;
    }

    //This method uses yahoo finance to get the OHLC data
    protected OHLCDataItem[] getData(String symbol) {
        java.util.List<OHLCDataItem> dataItems = new ArrayList<>();
        try {
            String strUrl= "http://ichart.finance.yahoo.com/table.csv?s=GOOG&a=2&b=1&c=2013&d=4&e=24&f=2013&g=d&ignore=.csv";
            URL url = new URL(strUrl);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            DateFormat df = new SimpleDateFormat("y-M-d");
            String inputLine;
            in.readLine();
            while ((inputLine = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(inputLine, ",");
                Date date       = df.parse( st.nextToken() );
                double open     = Double.parseDouble( st.nextToken() );
                double high     = Double.parseDouble( st.nextToken() );
                double low      = Double.parseDouble( st.nextToken() );
                double close    = Double.parseDouble( st.nextToken() );
                double volume   = Double.parseDouble( st.nextToken() );
                double adjClose = Double.parseDouble( st.nextToken() );
                OHLCDataItem item = new OHLCDataItem(date, open, high, low, close, volume);
                dataItems.add(item);
            }
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        OHLCDataItem[] data = dataItems.toArray(new OHLCDataItem[dataItems.size()]);
        return data;

//        //Data from Yahoo is from newest to oldest. Reverse so it is oldest to newest
//        Collections.reverse(dataItems);
//        try {
//            FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, 200 + 2);//2 extra line for comments in file
//
//            //convert quotes into dataset
//            ArrayList<OHLCDataItem> items = new ArrayList<>();
//            ArrayList<FundQuote> quotes = fund.getQuote();
//            for (FundQuote quote : quotes) {
//                String str = quote.getDate();
//                OHLCDataItem item = new OHLCDataItem(AppUtil.stringToCalendarNoEx(str).getTime(),
//                        quote.getOpen(), quote.getHigh(),
//                        quote.getLow(), quote.getClose(), quote.getVolume());
//                items.add(item);
//            }
//            //Convert the list into an array
//            OHLCDataItem[] data = items.toArray(new OHLCDataItem[items.size()]);
//            return data;
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        return null;
    }

    public static void main(String[] args) {
        FrameworkConstants.adjustDataFolder();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Candle1("GOOG").setVisible(true);
            }
        });
    }
}