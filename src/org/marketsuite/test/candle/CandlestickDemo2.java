package org.marketsuite.test.candle;

import org.jfree.chart.axis.DateAxis;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * stackoverflow.com/a/18421887/230513
 * www.jfree.org/forum/viewtopic.php?f=10&t=24521
 */
public class CandlestickDemo2 extends JFrame {

    public CandlestickDemo2(String stockSymbol) {
        super("CandlestickDemo2");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");

        CandlestickRenderer renderer = new CandlestickRenderer();
        XYDataset dataset = getDataSet(stockSymbol);
        XYPlot mainPlot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);

        //Do some setting up, see the API Doc
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setDrawVolume(false);
        rangeAxis.setAutoRangeIncludesZero(false);

        //Now create the chart and chart panel
        JFreeChart chart = new JFreeChart(stockSymbol, null, mainPlot, false);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(600, 300));
        mainPlot.setDomainPannable(true);
        mainPlot.setRangePannable(true);
        this.add(chartPanel);
        // Add tiemline toggle
        final Timeline oldTimeline = domainAxis.getTimeline();
        final Timeline newTimeline = SegmentedTimeline.newMondayThroughFridayTimeline();
        this.add(new JCheckBox(new AbstractAction("Segmented Timeline") {
            public void actionPerformed(ActionEvent e) {
                JCheckBox jcb = (JCheckBox) e.getSource();
                if (jcb.isSelected()) {
                    domainAxis.setTimeline(newTimeline);
                } else {
                    domainAxis.setTimeline(oldTimeline);
                }
            }
        }), BorderLayout.SOUTH);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    private AbstractXYDataset getDataSet(String stockSymbol) {
        //This is the dataset we are going to create
        DefaultOHLCDataset result;
        //This is the data needed for the dataset
        OHLCDataItem[] data;
        //This is where we go get the data, replace with your own data source
        data = getData(stockSymbol);
        //Create a dataset, an Open, High, Low, Close dataset
        result = new DefaultOHLCDataset(stockSymbol, data);
        return result;
    }
    //This method uses yahoo finance to get the OHLC data

    protected OHLCDataItem[] getData(String stockSymbol) {
        java.util.List<OHLCDataItem> dataItems = new ArrayList<OHLCDataItem>();
        try {
            String strUrl = "http://ichart.finance.yahoo.com/table.csv?s=" + stockSymbol
                    + "&a=4&b=1&c=2013&d=6&e=1&f=2013&g=d&ignore=.csv";
            URL url = new URL(strUrl);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            DateFormat df = new SimpleDateFormat("y-M-d");
            String inputLine;
            in.readLine();
            while ((inputLine = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(inputLine, ",");
                Date date = df.parse(st.nextToken());
                double open = Double.parseDouble(st.nextToken());
                double high = Double.parseDouble(st.nextToken());
                double low = Double.parseDouble(st.nextToken());
                double close = Double.parseDouble(st.nextToken());
                double volume = Double.parseDouble(st.nextToken());
                double adjClose = Double.parseDouble(st.nextToken());
                OHLCDataItem item = new OHLCDataItem(date, open, high, low, close, volume);
                dataItems.add(item);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        //Data from Yahoo is from newest to oldest. Reverse so it is oldest to newest
        Collections.reverse(dataItems);
        //Convert the list into an array
        OHLCDataItem[] data = dataItems.toArray(new OHLCDataItem[dataItems.size()]);
        return data;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CandlestickDemo2("AAPL").setVisible(true);
            }
        });
    }
}
