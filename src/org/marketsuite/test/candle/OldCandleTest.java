package org.marketsuite.test.candle;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.XYDataset;
import org.marketsuite.framework.model.FundData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class OldCandleTest extends JPanel {
    public OldCandleTest(FundData fund) {
        setLayout(new MigLayout());

        //init chart axis, renderer
        NumberAxis rangeAxis = new NumberAxis("Price");
        rangeAxis.setAutoRangeIncludesZero(false);
        DateAxis domainAxis = new DateAxis("Date");
        domainAxis.setTimeline( SegmentedTimeline.newMondayThroughFridayTimeline() );
        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setDrawVolume(false);

        //convert quotes into dataset
        ArrayList<OHLCDataItem> items = new ArrayList<>();
        ArrayList<FundQuote> quotes = fund.getQuote();
        for (FundQuote quote : quotes) {
            String str = quote.getDate();
            OHLCDataItem item = new OHLCDataItem(AppUtil.stringToCalendarNoEx(str).getTime(),
                quote.getOpen(), quote.getHigh(),
                quote.getLow(), quote.getClose(), quote.getVolume());
            items.add(item);
        }
        Collections.reverse(items);
        XYDataset ds = new DefaultOHLCDataset(fund.getSymbol(), items.toArray(new OHLCDataItem[items.size()]));
        XYPlot mainPlot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
        JFreeChart chart = new JFreeChart(fund.getSymbol(), null, mainPlot, false);
        ChartPanel jfree_pnl = new ChartPanel(chart, false);
        jfree_pnl.setPreferredSize(new Dimension(600, 300));
        mainPlot.setDomainPannable(true);
        mainPlot.setRangePannable(true);
        add(jfree_pnl, "dock center");
    }

    public static void main(String[] args) throws Exception {
        FrameworkConstants.adjustDataFolder();
        JFrame frm = new JFrame("TEST");
        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, "SPY", 200 + 2);//2 extra line for comments in file
        frm.setContentPane(new OldCandleTest(fund));
//        frm.pack();
        frm.setSize(new Dimension(500, 500));
        frm.setLocation(50, 50);
        frm.setVisible(true);
    }
}
