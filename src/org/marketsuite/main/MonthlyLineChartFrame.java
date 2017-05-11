package org.marketsuite.main;

import org.marketsuite.component.UI.MagnifierUI;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.quote.MonthlyQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.jxlayer.JXLayer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//monthly chart window for long term trends, only 200 SMA plotted
public class MonthlyLineChartFrame extends JInternalFrame implements PropertyChangeListener {
    public MonthlyLineChartFrame() {
        setName("Main");//for MainTabUI to recognize
        setResizable(true); setClosable(true); setMaximizable(true); setIconifiable(false);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("qc_04"));
        setFrameIcon(FrameworkIcon.LINE_CHART);
        setContentPane(_pnlMonthly = new MonthlyPanel());
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_MONTHLY_LINE_CHART, MdiMainFrame.LOCATION_MONTHLY_LINE_CHART, MdiMainFrame.SIZE_MONTHLY_LINE_CHART);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        Props.addWeakPropertyChangeListener(Props.SymbolSelection, this);//handle symbol selection
    }

    //----- interface/override methods -----
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case SymbolSelection://update graph
                String symbol = (String)prop.getValue();
                    _pnlMonthly.plotGraph(symbol);
                break;
        }
    }

    //----- inner classes -----
    private enum ZoomLevel {
        Year2,
        Year5,
        Year8,
        Year15,
        Max;

        //convert all into human readable string
        public static String[] toStrings() { return displayStrings; }
        private final static String[] displayStrings = {
            "2 Year", "5 Year", "8 Year", "15 Year", "Full Length"
        };
        public static ZoomLevel findZoomLevel(String display_string) {
            for (int i = 0; i < displayStrings.length; i++) {
                if (displayStrings[i].equals(display_string))
                    return values()[i];
            }
            return Year2;
        }
    }
    private class MonthlyPanel extends JPanel {
        private MonthlyPanel() {
            setLayout(new MigLayout("insets 0"));

            //north - title strip
            JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[][]push[]push[][]5", "3[]3"));
            north_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_01")));
            north_pnl.add(_txtSymbol);
            _txtSymbol.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String symbol = _txtSymbol.getText().toUpperCase();
                    _txtSymbol.select(0, symbol.length());//highlight symbol for easy typing next one
                    if (symbol.equalsIgnoreCase(_sCurrentSymbol))//already plotted
                        return;
                    plotGraph(symbol);
                }
            });
            north_pnl.add(_lblTitle); _lblTitle.setFont(FrameworkConstants.SMALL_FONT_BOLD);
            north_pnl.add(_cmbZoom);  _cmbZoom.setSelectedIndex(1);//default 5 year
            _cmbZoom.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) return;
                    plotGraph(_sCurrentSymbol);
                }
            });
//            north_pnl.add(_btnLine);
            add(north_pnl, "dock north");

            //center - chart
            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    "", // title
                    "", // x-axis label
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("pg_lbl_2"), // y-axis label TODO: customize title
                    _DataSet,//main data set for price data
                    true,        // create legend?
                    true,        // generate tooltips?
                    false        // generate URLs?
            );
            chart.getLegend().setPosition(RectangleEdge.TOP);
            XYPlot plot = chart.getXYPlot();//time series is a type of XY plot
            plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

            //change various attributes - color, shape, stroke, paint, tooltip for main series
            Color edge_color1 = new Color(255, 255, 0xFF);
            Color edge_color2 = new Color(207, 232, 0xFF);
            plot.setBackgroundPaint(new GradientPaint(0, 0, edge_color2, 500, 500, edge_color1));
//            plot.setBackgroundPaint(new GradientPaint(0, 0, Color.lightGray, 500, 500, new Color(250, 250, 250)));
            StandardXYToolTipGenerator tip_price = new StandardXYToolTipGenerator("{1}  {2}",//{0} is symbol
                new SimpleDateFormat("MM/dd/yyyy"), FrameworkConstants.DOLLAR_FORMAT);
            XYItemRenderer r = plot.getRenderer();
            if (r instanceof XYLineAndShapeRenderer) {
                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
                renderer.setBaseShapesVisible(false);//default false, set true show square data point
                renderer.setBaseShapesFilled(false);
                renderer.setDrawSeriesLineAsPath(false);
                renderer.setSeriesStroke(0, new BasicStroke(2.0f));
                renderer.setSeriesPaint(0, new Color(0, 100, 50));//main data, blue/green
                renderer.setSeriesStroke(1, new BasicStroke(1.0f));
                renderer.setSeriesPaint(1, Color.cyan.darker());
                renderer.setSeriesStroke(2, new BasicStroke(1.2f));
                renderer.setSeriesPaint(2, Color.orange.darker());
                renderer.setBaseToolTipGenerator(tip_price);
            }

            //put jfc into panel for layout, set attributes
            _pnlChart = new ChartPanel(chart);
            _pnlChart.setRangeZoomable(false);
            _pnlChart.setMouseWheelEnabled(true);//wheel zoom in/out
//            add(_pnlChart, "dock center");

            //add magnifier layer
            JXLayer layer = new JXLayer(_pnlChart);
            MagnifierUI ui = new MagnifierUI();
            layer.setUI(ui);
            add(layer, "dock center");
        }
        public void plotGraph(String symbol) {
            if (symbol == null) {//no symbol, user deselect
//                _lblTitle.setText("");
//                _sCurrentSymbol = null;
//                _DataSet.removeAllSeries();
                clearChart();
                return;
            }

            //update company's full name
            //if symbol changes, update title from fundamental info
            if (!symbol.equals(_sCurrentSymbol)) {
                StringBuilder sb = new StringBuilder(symbol);
                Fundamental fundamental = MainModel.getInstance().getFundamentals().get(symbol);
                if (fundamental != null)//cant'find
                    sb.append(": ").append(fundamental.getFullName());
                _lblTitle.setText(sb.toString());
                _sCurrentSymbol = symbol;
            }

            //plot left side with price/2 SMA, right side with IBD composite
            MonthlyQuote monthly_quote = new MonthlyQuote(symbol, computeNumQuotes());//based on current combo selection
            if (monthly_quote.getSize() == 0) return;

            //compute moving avg, covert monthly_quote into float array
            int size = monthly_quote.getSize();
            float[] prices = new float[size];
            for (int idx = 0; idx < size; idx++)
                prices[idx] = monthly_quote.getClose(idx);
            SMA sma200 = new SMA(10, prices);//10 month = 200 day

            //limit number of points plotted
            int show_bars = prices.length;//40;//since IBD rating only recent
            if (show_bars > prices.length)
                show_bars = prices.length;
            TimeSeries price_series = new TimeSeries("Price");
            TimeSeries sma_series = new TimeSeries("10 Month SMA");
            for (int i = 0; i < show_bars; i++) {
                Calendar cal = AppUtil.stringToCalendarNoEx(monthly_quote.getDate(i));
                Day day = new Day(cal.getTime());
                price_series.add(day, prices[i]);
                float v = sma200.getSma()[i];
                if (v > 0)
                    sma_series.add(day, v);
            }
            _DataSet.removeAllSeries();
            _DataSet.addSeries(price_series);
            _DataSet.addSeries(sma_series);
        }
    }
    private int computeNumQuotes() {//number of days into the past
        String sel = (String)_cmbZoom.getSelectedItem();
        ZoomLevel zl = ZoomLevel.findZoomLevel(sel);
        switch (zl) {
            case Year2:
            default:
                return DAYS_PER_YEAR * 2;

            case Year5:
                return DAYS_PER_YEAR * 5;

            case Year8:
                return DAYS_PER_YEAR * 8;

            case Year15:
                return DAYS_PER_YEAR * 15;

            case Max:
                return -1;
        }
    }
    private void clearChart() {
        _lblTitle.setText("");
        _sCurrentSymbol = null;
        _DataSet.removeAllSeries();
    }

    //----- variables-----
    private MonthlyPanel _pnlMonthly;
    private String _sCurrentSymbol;
    private NameField _txtSymbol = new NameField(4);
    private JComboBox<String> _cmbZoom = new JComboBox<>(ZoomLevel.toStrings());
    private JButton _btnLine = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("qc_02"), FrameworkIcon.LINE_CHART);
    private JLabel _lblTitle = new JLabel();
    private ChartPanel _pnlChart;
    private TimeSeriesCollection _DataSet = new TimeSeriesCollection();//left axis
    private static final int DAYS_PER_YEAR = 250;
}