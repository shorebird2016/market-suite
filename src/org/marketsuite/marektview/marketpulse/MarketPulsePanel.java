package org.marketsuite.marektview.marketpulse;

import org.marketsuite.component.UI.MagnifierUI;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.ZoomLevel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.GraphUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import jxl.Sheet;
import jxl.Workbook;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.marketsuite.component.UI.MagnifierUI;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

//A simple, quick to create daily chart with basic indicators, allow typing any symbol, plot also driven from other lists
public class MarketPulsePanel extends JPanel {
    public MarketPulsePanel() {
        setLayout(new MigLayout("insets 0"));

        //title strip, symbol entry
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[][]push[]push[]15[]5", "3[]3"));
        north_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_01")));
        north_pnl.add(_fldSymbol); _fldSymbol.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _fldSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = _fldSymbol.getText().toUpperCase();
                _fldSymbol.select(0, symbol.length());//highlight symbol for easy typing next one
                if (symbol.equalsIgnoreCase(_sCurrentSymbol))
                    return;
                plot(symbol);
            }
        });
        north_pnl.add(_lblTitle); _lblTitle.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        north_pnl.add(_btnIbdMarketPulse);
        _btnIbdMarketPulse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //open market pulse file, read all the transition point
                ArrayList<MarketPulseSignal> mkp = readMarketPulse();

                //plot them on the chart as annotations
                plotMarketPulseSignal(mkp);
            }
        });
        north_pnl.add(_btnClear); _btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _Plot.clearAnnotations();
            }
        });
        ZoomLevel[] levels = ZoomLevel.class.getEnumConstants();
        String[] list = new String[levels.length];
        int i = 0;
        for (ZoomLevel lvl : levels)
            list[i++] = lvl.displayString;
        north_pnl.add(_cmbZoom = new JComboBox<>(list)); _cmbZoom.setSelectedIndex(4);//default 9 month
        _cmbZoom.setFocusable(false);
        _cmbZoom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                plot(_sCurrentSymbol);
            }
        });
        add(north_pnl, "dock north");

        //center - chart
        JFreeChart jfc = ChartFactory.createTimeSeriesChart("", "", "", _PriceDataset, true, true, false);
        _Plot = jfc.getXYPlot();//time series is a type of XY plot
        _Plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
//        _Plot.setDataset(1, _RatingDataset);//IBD indicator
//        NumberAxis right_axis = new NumberAxis("Composite");
//        right_axis.setRange(0, 100);
//        _Plot.setRangeAxis(1, right_axis);
        _Plot.mapDatasetToRangeAxis(1, 1);
        jfc.getLegend().setPosition(RectangleEdge.TOP);
        StandardXYToolTipGenerator tip_gen = new StandardXYToolTipGenerator("{0}: {1}  {2}",//{0}symbol {1}date {2}vale
            new SimpleDateFormat("MM/dd"), FrameworkConstants.FORMAT_NUMBERS);
        _PriceRenderer = (XYLineAndShapeRenderer)_Plot.getRenderer();
        _PriceRenderer.setBaseToolTipGenerator(tip_gen);
        _PriceRenderer.setSeriesShapesFilled(0, false);//price shows shape but not filled
        _PriceRenderer.setSeriesShapesVisible(0, false);
        _PriceRenderer.setSeriesStroke(0, new BasicStroke(1.2f));
        _PriceRenderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
        _PriceRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
        _PriceRenderer.setSeriesPaint(1, Color.pink.darker());//10SMA
        _PriceRenderer.setSeriesStroke(2, new BasicStroke(1.2f));
        _PriceRenderer.setSeriesPaint(2, Color.green);//30SMA
        _PriceRenderer.setSeriesStroke(3, new BasicStroke(1.0f));
        _PriceRenderer.setSeriesPaint(3, Color.cyan.darker());//50SMA
        _PriceRenderer.setSeriesStroke(4, new BasicStroke(1.2f));
        _PriceRenderer.setSeriesPaint(4, new Color(208, 180, 11));//brown (200SMA)
        Color edge_color1 = new Color(255, 255, 0xFF);
        Color edge_color2 = new Color(207, 232, 0xFF);
        _Plot.setBackgroundPaint(new GradientPaint(0, 0, edge_color2, 500, 500, edge_color1));

        //rendering rating information
//        _RatingRenderer = new XYLineAndShapeRenderer();
//        _RatingRenderer.setBaseShapesFilled(false);
//        _RatingRenderer.setSeriesShapesVisible(3, false);
//        _RatingRenderer.setSeriesPaint(0, new Color(221, 89, 5));//Composite
//        _RatingRenderer.setSeriesPaint(1, new Color(21, 144, 175));//RS
//        _RatingRenderer.setSeriesPaint(2, new Color(38, 222, 49));//EPS
//        _RatingRenderer.setSeriesStroke(0, new BasicStroke(2.5f));
//        _RatingRenderer.setSeriesStroke(1, new BasicStroke(2.5f));
//        StandardXYToolTipGenerator tip_rank = new StandardXYToolTipGenerator("{1}  {0}={2}",//{0} is symbol
//            new SimpleDateFormat("MM/dd"), FrameworkConstants.TWO_DIGIT_FORMAT);
//        _RatingRenderer.setBaseToolTipGenerator(tip_rank);
//        _Plot.setRenderer(1, _RatingRenderer);

        //add magnifier layer
        JXLayer layer = new JXLayer(_pnlChart = new ChartPanel(jfc));
        MagnifierUI ui = new MagnifierUI();
        layer.setUI(ui);
        add(layer, "dock center");
    }

    //----- public methods ------
    public void plot(String symbol) {
        if (symbol == null) {
            clearChart();
            return;
        }

        //read company's full name
        StringBuilder sb = new StringBuilder(symbol);
        Fundamental fundamental = MainModel.getInstance().getFundamentals().get(symbol);
        if (fundamental != null)//cant'find
            sb.append(": ").append(fundamental.getFullName());
        _lblTitle.setText(sb.toString());
        _sCurrentSymbol = symbol;
        int num_quotes = computeNumQuotes();
        try {
            if (num_quotes != -1)
                _Fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, num_quotes + 2);//2 extra line for comments in file
            else
                _Fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
        } catch (IOException e) {
            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_01") + " " + symbol, LoggingSource.DAILY_CHART);
            clearChart();
            return;
        }
        DataUtil.adjustForSplits(_Fund, num_quotes - 1, 0);

        //compute moving avg, covert quotes into float array
        int size = _Fund.getSize();
        float[] prices = new float[size];
        for (int idx = 0; idx < size; idx++)
            prices[idx] = _Fund.getQuote().get(idx).getClose();
        SMA sma10 = new SMA(10, prices);
        SMA sma30 = new SMA(30, prices);
        SMA sma50 = new SMA(50, prices);
        TimeSeries price_series = new TimeSeries("Price");
        TimeSeries sma1_series = new TimeSeries("10 SMA");
        TimeSeries sma2_series = new TimeSeries("30 SMA");
        TimeSeries sma3_series = new TimeSeries("50 SMA");
        for (int i = 0; i < prices.length; i++) {
            Calendar cal = AppUtil.stringToCalendarNoEx(_Fund.getQuote().get(i).getDate());
            Day day = new Day(cal.getTime());
            price_series.add(day, prices[i]);
            float v = sma10.getSma()[i];
            if (v > 0)
                sma1_series.add(day, v);
            v = sma30.getSma()[i];
            if (v > 0)
                sma2_series.add(day, v);
            v = sma50.getSma()[i];
            if (v > 0)
                sma3_series.add(day, v);
        }
        _PriceDataset.removeAllSeries();
        _PriceDataset.addSeries(price_series);
        _PriceDataset.addSeries(sma1_series);
        _PriceDataset.addSeries(sma2_series);
        _PriceDataset.addSeries(sma3_series);

        //read rating database if checkbox is checked
//        _RatingDataset.removeAllSeries();
//        Calendar qcal = AppUtil.stringToCalendarNoEx(_Fund.getDate(size - 1));
//        try {
//            _ibdRatings = IbdRating.readIbdRating(symbol, FrameworkConstants.DATA_FOLDER_IBD_RATING, num_quotes);
//        } catch (IOException e) {
//            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_02") + " " + symbol, LoggingSource.DAILY_CHART);
//            return;
//        }
//        TimeSeries composite_series = new TimeSeries("Composite");
//        TimeSeries rs_series = new TimeSeries("RS");
//        TimeSeries eps_series = new TimeSeries("EPS");
//        for (int i = 0; i < _ibdRatings.size(); i++) {
//            //if date older than last quote date, skip
//            Calendar rcal = _ibdRatings.get(i).getDate();
//            if (rcal.compareTo(qcal) <= 0) continue;
//            Day day = new Day(rcal.getTime());
//            composite_series.add(day, _ibdRatings.get(i).getComposite());
//            rs_series.add(day, _ibdRatings.get(i).getRsRating());
//            eps_series.add(day, _ibdRatings.get(i).getEpsRating());
//        }
//        _RatingDataset.addSeries(composite_series);
//        _RatingDataset.addSeries(rs_series);
//        _RatingDataset.addSeries(eps_series);
    }

    //----- private methods -----
    private int computeNumQuotes() {//number of days into the past
        int sel = _cmbZoom.getSelectedIndex();
        ZoomLevel zl = ZoomLevel.findLevel(sel);
        switch (zl) {
            case Month3:
            default: return DAYS_PER_MONTH * 3;
            case Month1: return DAYS_PER_MONTH * 1;
            case Month6: return DAYS_PER_MONTH * 6;
            case Month9: return DAYS_PER_MONTH * 9;
            case Year1: return DAYS_PER_MONTH * 12;
            case Year1_Half: return DAYS_PER_MONTH *18;
            case Max: return -1;
        }
    }
    private void clearChart() {
        _lblTitle.setText("");
        _sCurrentSymbol = null;
        _PriceDataset.removeAllSeries();
//        _RatingDataset.removeAllSeries();
    }
    private ArrayList<MarketPulseSignal> readMarketPulse() {
//        JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_IMPORT));
//        fc.setFileFilter(new FileFilter() {
//            public boolean accept(File file) {
//                if (file.isDirectory())
//                    return true;
//                int pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_XLS);
//                return (pos > 0);
//            }
//            public String getDescription() {//this shows up in description field of dialog
//                return ApolloConstants.APOLLO_BUNDLE.getString("act_lbl_2");
//            }
//        });
//        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        fc.setMultiSelectionEnabled(false);
//        fc.setAcceptAllFileFilterUsed(false);
//        fc.setDialogTitle(ApolloConstants.APOLLO_BUNDLE.getString("imp_mkt_pulse"));
//        int rsp = fc.showOpenDialog(MdiMainFrame.getInstance());
//        if (rsp != JFileChooser.APPROVE_OPTION) return null;
//        File file = fc.getSelectedFile();
        ArrayList<MarketPulseSignal> ret = new ArrayList<>();
        Workbook wb = null;
        try { //open this sheet, read one row at a time
            wb = Workbook.getWorkbook(new File(FrameworkConstants.DATA_FOLDER + File.separator + "Market Pulse.xls"));
            Sheet sheet = wb.getSheet(0);
            int row = 5;//0 based, starting row
            String prev_date = null;  String prev_code = null; int rows = sheet.getRows();
            do {
                String date = sheet.getCell(0, row).getContents();
                String code = sheet.getCell(1, row).getContents();
                if (prev_code == null) prev_code = code;//this only happen once
                if (prev_date == null) prev_date = date;
                if (!prev_code.equals(code)) {//state change
                    ret.add(new MarketPulseSignal(prev_date, prev_code));//record previous one since data goes back in time
                }
                prev_code = code;
                prev_date = date;

                //add the last row as a starting point
                if (row == (rows - 1))
                    ret.add(new MarketPulseSignal(date, code));//record final one
                row++;
            } while (row < rows);//use blank date to finish reading
            wb.close();
        }catch (Exception e) {
            e.printStackTrace();
            if (wb != null) wb.close();
        }
        return ret;//maybe empty
    }
    private void plotMarketPulseSignal(ArrayList<MarketPulseSignal> signals) {
        _Plot.clearAnnotations();
        for (MarketPulseSignal mps : signals) {
            FundQuote quote = _Fund.findQuoteByDate(mps.date);
            Color color = Color.red;//MC
            double angle = Math.PI * 1 / 4;
            if (mps.code.equals("CU")) { color = Color.green.darker(); angle = Math.PI * 7 / 4; }
            else if (mps.code.equals("RU")) { color = Color.blue; angle = Math.PI * 3 / 2; }
            else if (mps.code.equals("UU")) { color = Color.orange; angle = Math.PI * 2 / 3; }
            if (quote == null) continue;//date outside _Fund data range, don't mark
            XYPointerAnnotation anno = GraphUtil.createMarker(mps.date, quote.getClose(), mps.code, color, angle);
            _Plot.addAnnotation(anno);
        }
    }

    //----- inner classes -----
//    private enum ZoomLevel {
//        Month1("1 Month"),
//        Month3("3 Month"),
//        Month6("6 Month"),
//        Month9("9 Month"),
//        Year1("1 Year"),
//        Year1_Half("1.5 Year"),
//        Max("Full Range");
//
//        ZoomLevel(String display_str) { displayString = display_str; }
//        static ZoomLevel findLevel(int order) {
//            ZoomLevel[] consts = ZoomLevel.class.getEnumConstants();
//            return consts[order];
//        }
//        private String displayString;
//
//    }
    private class MarketPulseSignal {
        private MarketPulseSignal(String date, String code) {
            this.date = date; this.code = code;
        }
        private String date;
        private String code;
    }

    //----- variables -----
    private String _sCurrentSymbol;
    private NameField _fldSymbol = new NameField(5);
    private JButton _btnIbdMarketPulse = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_5"), FrameworkIcon.THUMB_TACK);
    private JButton _btnClear = WidgetUtil.createIconButton("Clear Annotations", FrameworkIcon.CLEAR);
    private JComboBox<String> _cmbZoom;
    protected JLabel _lblTitle = new JLabel();
    private TimeSeriesCollection _PriceDataset = new TimeSeriesCollection();//main
//    private TimeSeriesCollection _RatingDataset = new TimeSeriesCollection();//right axis
    private ChartPanel _pnlChart;
    private XYPlot _Plot;
    private XYLineAndShapeRenderer _PriceRenderer, _RatingRenderer;
    private FundData _Fund;
    private ArrayList<IbdRating> _ibdRatings;
    private static final int DAYS_PER_MONTH = 20;
}