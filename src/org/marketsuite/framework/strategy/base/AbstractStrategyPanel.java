package org.marketsuite.framework.strategy.base;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.AnnualReturn;
import org.marketsuite.framework.model.Equity;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.analysis.AnnualReturnGraphPanel;
import org.marketsuite.framework.strategy.analysis.EquityGraphPanel;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Year;
import org.marketsuite.framework.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

/**
 * Base class container for all strategy tabs.  It consists of these replaceable sub-panels.
 * (1) trade results - upper left, entry/exit/performance/equity
 * (2) equity curve and other graphs - upper right (card layout)
 * (3) stats - lower left
 * (4) options - lower right
 * Each panel can be replaced via API with any other panels including title strip
 */
public abstract class AbstractStrategyPanel extends SkinPanel implements PropertyChangeListener {
    public AbstractStrategyPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());

        //north - title strip
        JPanel west_pnl = new JPanel(); west_pnl.setOpaque(false);
        west_pnl.add(Box.createHorizontalStrut(20));
        //year selection combo
        west_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_lbl_3")));
        west_pnl.add(_txtBeginDate);
        WidgetUtil.attachToolTip(_txtBeginDate, FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_tip_3"),
            SwingConstants.LEFT, SwingConstants.BOTTOM);
        west_pnl.add(Box.createHorizontalStrut(20));
        west_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_lbl_4")));
        _pnlTitleStrip = WidgetUtil.createTitleStrip(west_pnl, null, createCgarEquityPanel());
        add(_pnlTitleStrip, BorderLayout.NORTH);

        //a splitter inside center panel
        _splVertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        _splVertical.setContinuousLayout(true);
        _splVertical.setDividerLocation(400);//todo: use ratio.....

        //top - splitter for table vs graph
        _splHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        _splHorizontal.setContinuousLayout(true);
        _splHorizontal.setDividerLocation(650);//todo: use ratio.....
        _splHorizontal.setRightComponent(_pnlResultHolder); _pnlResultHolder.setOpaque(false);
        _pnlResultHolder.add(_pnlTrade = new TradePanel());

        //center - graph holder with title strip
        _pnlGraphHolder = new JPanel(new BorderLayout());

        //north - icon bar for chart options
        _pnlTool = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[]5[]5[]push[]push[]5", "3[]3"));//new BorderLayout());
//        JPanel tw_pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));  tw_pnl.setOpaque(false);

        //icon buttons - annual return, price, equity, zoom
        _btnEquityCurve = WidgetUtil.createToggleButton(FrameworkIcon.EQUITY_CURVE,
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_1"), null);
        _btnEquityCurve.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) _pnlGraphCard.getLayout()).show(_pnlGraphCard, EQUITY_GRAPH);
                refreshGraphButtonState(_btnEquityCurve);
                _sCurrentGraph = EQUITY_GRAPH;
                setGraphTitle();
            }
        });
        _pnlTool.add(_btnEquityCurve);
//        tw_pnl.add(_btnEquityCurve);
//        tw_pnl.add(Box.createGlue());

        //annual return
        _btnAnnualReturnGraph = WidgetUtil.createToggleButton(FrameworkIcon.BAR_CHART,
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_2"), null);
        _btnAnnualReturnGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                ((CardLayout) _pnlGraphCard.getLayout()).show(_pnlGraphCard, ANNUAL_RETURN_GRAPH);
                refreshGraphButtonState(_btnAnnualReturnGraph);
                _sCurrentGraph = ANNUAL_RETURN_GRAPH;
                setGraphTitle();
            }
        });
        _pnlTool.add(_btnAnnualReturnGraph);
//        tw_pnl.add(Box.createGlue());

        //price button - only visible for MAC strategy because others have multiple symbols
        _btnPriceGraph = WidgetUtil.createToggleButton(FrameworkIcon.PRICE_CHART,
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_3"), null);
            _btnPriceGraph.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ((CardLayout) _pnlGraphCard.getLayout()).show(_pnlGraphCard, PRICE_GRAPH);
                    refreshGraphButtonState(_btnPriceGraph);
                    _sCurrentGraph = PRICE_GRAPH;
                    setGraphTitle();
                }
            });
        _pnlTool.add(_btnPriceGraph);
//        tw_pnl.add(Box.createGlue());
//        _pnlTool.add(tw_pnl, BorderLayout.WEST);
//        //zoom in/out
////        JToggleButton _btnZoomIn = WidgetUtil.createToggleButton(FrameworkIcon.ZOOM_IN, FrameworkIcon.ZOOM_IN,
////            FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_4"), null);
////        _pnlTool.add(_btnZoomIn);
////        _pnlTool.add(Box.createHorizontalGlue());
////        JToggleButton _btnZoomOut = WidgetUtil.createToggleButton(FrameworkIcon.ZOOM_OUT, FrameworkIcon.ZOOM_OUT,
////            FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_5"), null);
////        _pnlTool.add(_btnZoomOut);
////        _pnlTool.add(Box.createHorizontalGlue());
//        //log scale toggle

//        JPanel tc_pnl = new JPanel();  tc_pnl.setOpaque(false);
        _pnlTool.add(_lblGraphTitle = new JLabel());  _lblGraphTitle.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
//        _pnlTool.add(tc_pnl, BorderLayout.CENTER);

        //east - log button
//        JPanel te_pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));  te_pnl.setOpaque(false);
        final JToggleButton log_btn = WidgetUtil.createToggleButton(FrameworkIcon.LOG_SCALE,
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_6"), null);
        log_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _pnlEquityGraph.setLogScale(log_btn.isSelected());
            }
        });
        _pnlTool.add(log_btn);
//        _pnlTool.add(te_pnl, BorderLayout.EAST);
        _pnlGraphHolder.add(_pnlTool, BorderLayout.NORTH);

        //center - card panel with several graphs
        _pnlGraphCard = new JPanel(new CardLayout());  _pnlGraphCard.setOpaque(false);
        _pnlGraphCard.add(EQUITY_GRAPH, _pnlEquityGraph = new EquityGraphPanel());
        _pnlGraphCard.add(ANNUAL_RETURN_GRAPH, _pnlAnnualReturnGraph = new AnnualReturnGraphPanel());
        _pnlGraphCard.add(PRICE_GRAPH, _pnlPriceGraph = new PriceGraphPanel());
        _pnlGraphHolder.add(_pnlGraphCard, BorderLayout.CENTER);
        _splHorizontal.setLeftComponent(_pnlGraphHolder); _pnlGraphHolder.setOpaque(false);
        _splVertical.setTopComponent(this._splHorizontal);

        //bottom - stat view
        _pnlStatHolder.add(_pnlStat = new StatPanel()); _pnlStatHolder.setOpaque(false);
        _splVertical.setBottomComponent(_pnlStatHolder);
        _pnlCenterHolder.add(_splVertical);

        //center - splitter
        add(_pnlCenterHolder, BorderLayout.CENTER); _pnlCenterHolder.setOpaque(false);
        Props.addWeakPropertyChangeListener(Props.SymbolSelection, this);//handle symbol change
    }

    public abstract AbstractEngine getEngine();
    public abstract void simulate() throws Exception;
    public void propertyChange(PropertyChangeEvent evt) {
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case SymbolSelection://this will update all panels that uses this combo, should clear content not to confuse user
                _cmbSymbol.setSelectedItem(prop.getValue());
                break;
        }
    }

    //to force date ranges to some predefined values
    public void setDateRange(String start_date, String end_date) {
        Calendar scal = AppUtil.stringToCalendarNoEx(start_date);
        _txtBeginDate.setDate(scal.getTime());
        scal = AppUtil.stringToCalendarNoEx(end_date);
        _txtEndDate.setDate(scal.getTime());
    }
    protected void replaceTitleStrip(JPanel title_strip) {
        remove(_pnlTitleStrip);
        add(_pnlTitleStrip = title_strip, BorderLayout.NORTH);
    }
    protected void replaceResultPanel(JPanel result_panel) {
        _pnlResultHolder.removeAll();
        _pnlResultHolder.add(result_panel);
    }
    protected void adjustTopDivider(int location) {
        _splHorizontal.setDividerLocation(location);
    }
    /**
     * Use equity log to build normalized SP500 equity time series for charting
     * @param log equity log from any system
     * @param start_date starting point's entry date, common starting point for fund and SP500
     * @return time series of matching SP500 data
     * @exception java.text.ParseException when quote data is not clean
     */
    protected TimeSeries buildSp500EquitySeries(ArrayList<Equity> log, String start_date) throws ParseException {
        if (log.size() <= 0)
            return null;

        TimeSeries ret = new TimeSeries("Buy/Hold SP500");
        //find first data point difference, apply to all SP500 data (normalize)
        float first_sp_close = FrameworkConstants.SP500_DATA.findQuoteByDate(start_date).getClose();
        float shares = FrameworkConstants.START_CAPITAL / first_sp_close;
        //match each equity log date, calc equivalent SP500 equity
        for (Equity eqty : log) {
            //detect no data due to holidays
//            String date = AppUtil.dailyToWeekly(eqty.getDate());
//            Calendar cal = AppUtil.stringToCalendar(date);
//            if (!AppUtil.isDataAvailable(cal)) {//add one more day, mondy is probably holiday
//                cal.add(Calendar.DAY_OF_MONTH, 1);
//                date = AppUtil.calendarToString(cal);
//                cal = AppUtil.stringToCalendar(date);
//                if (!AppUtil.isDataAvailable(cal)) {//add one more day, mondy is probably holiday
//                    cal.add(Calendar.DAY_OF_MONTH, 1);
//                    date = AppUtil.calendarToString(cal);
//                    //todo: if date exceeds SP500 data range, exit
//                    if (!AppUtil.isDataAvailable(cal))
//                        return ret;
//                }
//            }
//if (eqty.getDate().equals("2012-01-20"))
//System.out.println();

            try {//To handle bad or missing data from quote file
                float sp_close = FrameworkConstants.SP500_DATA.findQuoteByDate(eqty.getDate()).getClose();
                float sp_eqty = shares * sp_close;
                Day day = new Day(AppUtil.stringToCalendar(eqty.getDate()).getTime());
                ret.add(day, sp_eqty);//normalize
            } catch (Exception e) {
                System.err.println("=====> " + e.getMessage() + " [SP500.txt] " + eqty.getDate());
            }
        }
        return ret;
    }
    /**
     * Create panel to hold CGAR and Equity fields, usually upper right corner of title strip.
     * @return handle to this panel
     */
    protected JPanel createCgarEquityPanel() {
        JPanel ret = new JPanel(new MigLayout("insets 0", "[][]5[][]5[][]", "3[]3")); ret.setOpaque(false);
        ret.add(new JLabel("Initial Capital:")); ret.add(_txtEquity);
        _txtEquity.setEditable(false); _txtEquity.setText("$10,000");
        _txtEquity.setBackground(FrameworkConstants.COLOR_LITE_GREEN);
        WidgetUtil.attachToolTip(_txtEquity, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_2"),
            SwingConstants.LEFT, SwingConstants.BOTTOM);
        ret.add(new JLabel("CAGR")); ret.add(_txtCagr);
        _txtCagr.setEditable(false); _txtCagr.setForeground(Color.blue);
        ret.add(new JLabel("Equiy:")); ret.add(_txtEndEquity);
        _txtEndEquity.setEditable(false); _txtEndEquity.setForeground(Color.blue);
        return ret;
    }
    //plot annual return graph
    protected void plotAnnualReturnGraph(ArrayList<AnnualReturn> ars) {
        String sym = "Buy/Hold SP500";//for graph legend
        if (ars != null)
            sym = ars.get(0).getSymbol();
        TimeSeries ts = new TimeSeries(sym);
        TimeSeries sp = AppUtil.annualReturnToTimeSeries(FrameworkConstants.SP500_ANNUAL_RETURN);
        if (ars != null) {
            ts = annualReturnToTimeSeries(ars);

            //use partial SP500 data from ts first year
            int yr = ars.get(0).getYear();
            ArrayList<AnnualReturn> sp_ars = new ArrayList<AnnualReturn>();
            for (AnnualReturn ar : FrameworkConstants.SP500_ANNUAL_RETURN) {
                if (ar.getYear() < yr)
                    continue;
                sp_ars.add(ar);
            }
            sp = annualReturnToTimeSeries(sp_ars);
        }
        _pnlAnnualReturnGraph.updateGraph(ts, sp);
    }
    protected void run() {}
    protected void plotPriceGraph() throws IOException, ParseException {}
    //to load historical quotes from file, setup date range, combo ranges
    protected void selectSymbol() {
        _sCurrentSymbol = (String) _cmbSymbol.getSelectedItem();
        try {
            _Fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, _sCurrentSymbol);
            _sStartDate = _Fund.getDate(_Fund.getSize() - 1);
            _sEndDate = _Fund.getDate(0);
            _lblDateRange.setText("(" + _sStartDate + " --> " + _sEndDate + ")");

            //populate start/end combo using start/end date
            int start_yr = AppUtil.extractYear(_sStartDate);
            int end_yr = AppUtil.extractYear(_sEndDate);
            Vector<Integer> start_list = new Vector<>();
            Vector<Integer> end_list = new Vector<>();
            for (int i=start_yr; i<=end_yr; i++) {
                start_list.add(i);
                end_list.add(i);
            }
            _txtBeginDate.setDate(AppUtil.stringToCalendar(_sStartDate).getTime());
            _txtEndDate.setDate(AppUtil.stringToCalendar(_sEndDate).getTime());

            //clear all other panes
            _pnlEquityGraph.clear(); _pnlAnnualReturnGraph.clear(); _pnlPriceGraph.clear();
            _pnlTrade.clear(); _pnlStat.clearFields();
        } catch (ParseException pe) {
            WidgetUtil.showWarning(null, FrameworkConstants.FRAMEWORK_BUNDLE.getString("dme_txt_4") + " " + pe.getMessage());
            pe.printStackTrace();
        } catch (IOException e) {
            WidgetUtil.showWarning(null, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_3") + " " + _sCurrentSymbol);
            e.printStackTrace();
        }
    }
    protected void selectSymbol(String symbol) {
        _sCurrentSymbol = symbol;
        try {
            _Fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, _sCurrentSymbol);
            _sStartDate = _Fund.getDate(_Fund.getSize() - 1);
            _sEndDate = _Fund.getDate(0);
            _lblDateRange.setText("(" + _sStartDate + " : " + _sEndDate + ")");

            //populate start/end combo using start/end date
            int start_yr = AppUtil.extractYear(_sStartDate);
            int end_yr = AppUtil.extractYear(_sEndDate);
            Vector<Integer> start_list = new Vector<>();
            Vector<Integer> end_list = new Vector<>();
            for (int i=start_yr; i<=end_yr; i++) {
                start_list.add(i);
                end_list.add(i);
            }
            _txtBeginDate.setDate(AppUtil.stringToCalendar(_sStartDate).getTime());
            _txtEndDate.setDate(AppUtil.stringToCalendar(_sEndDate).getTime());

            //clear all other panes
            _pnlEquityGraph.clear(); _pnlAnnualReturnGraph.clear(); _pnlPriceGraph.clear();
            _pnlTrade.clear(); _pnlStat.clearFields();
        } catch (ParseException pe) {
            WidgetUtil.showWarning(null, FrameworkConstants.FRAMEWORK_BUNDLE.getString("dme_txt_4") + " " + pe.getMessage());
            pe.printStackTrace();
        } catch (IOException e) {
            WidgetUtil.showWarning(null, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_msg_3") + " " + _sCurrentSymbol);
            e.printStackTrace();
        }
    }
    //initialize symbol combo by reading list of files from downloads, start/end year combo too
    protected void populateSymbols() {
        File dir = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        Vector<String> symbol_list = new Vector<String>();
        for (String file : dir.list()) {//build up combo list
            if (file.endsWith(FrameworkConstants.EXTENSION_QUOTE) && !file.startsWith("."))
                symbol_list.add(file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE)));
        }
        if (symbol_list.size() > 0) {
            _cmbSymbol.setModel(new DefaultComboBoxModel<>(symbol_list));
            selectSymbol();
        }
        else
            _btnSimulate.setEnabled(false);
    }
    protected void populateSymbols(JComboBox<String> combo) {
        File dir = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        Vector<String> symbol_list = new Vector<>();
        for (String file : dir.list()) {//build up combo list
            if (file.endsWith(FrameworkConstants.EXTENSION_QUOTE) && !file.startsWith("."))
                symbol_list.add(file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE)));
        }
        boolean not_empty = symbol_list.size() > 0;
        if (not_empty) {
            combo.setModel(new DefaultComboBoxModel<>(symbol_list));
            selectSymbol(combo.getItemAt(0));
        }
        _btnSimulate.setEnabled(not_empty);
    }
    protected void setGraphTitle() {
        String prefix = getEngine().getId();
        if (_sCurrentGraph.equals(EQUITY_GRAPH))
            _lblGraphTitle.setText(prefix + " " + FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_lbl_1"));
        else if (_sCurrentGraph.equals(ANNUAL_RETURN_GRAPH))
            _lblGraphTitle.setText(prefix + " " + FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_lbl_2"));
        else if (_sCurrentGraph.equals(PRICE_GRAPH))
            _lblGraphTitle.setText(prefix + " " + FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_lbl_3"));
    }
    //plot equity curve, insert extra starting point using starting date in trans_log
    protected void plotEquityGraph(ArrayList<Equity> equity_log, ArrayList<Transaction> trans_log) throws ParseException {
        TimeSeries data_series = new TimeSeries(trans_log.get(0).getSymbol());
        String start_date = trans_log.get(0).getEntryDate();
        float price = trans_log.get(0).getEntryPrice();
        float shares = FrameworkConstants.START_CAPITAL / price;
        equity_log.add(0, new Equity(start_date, shares, price));
        for (Equity equity : equity_log) {
            try {
                Calendar cal = AppUtil.stringToCalendar(equity.getDate());
                Day day = new Day(cal.getTime());
                data_series.add(day, equity.getEquity());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
//TODO analyzer still depends on AppUtil, can't switch this to SimUtil till SimUtil is available at framework level
        _pnlEquityGraph.plotEquitySeries(data_series, AppUtil.buildSp500EquitySeries(equity_log, start_date));
    }
    /**
     * Create panel to hold CGAR and Equity fields, usually upper right corner of title strip.
     * @return handle to this panel
     */
    protected JPanel createCgarPanel() {
        JPanel ret = new JPanel(); ret.setOpaque(false);
//        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_lbl_4")));
//        ret.add(_txtEquity); _txtEquity.setEditable(false);
//        _txtEquity.setBackground(FrameworkConstants.COLOR_LITE_GREEN);
//        WidgetUtil.attachToolTip(_txtEquity, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_2"),
//            SwingConstants.LEFT, SwingConstants.BOTTOM);
//        ret.add(Box.createHorizontalGlue());
//        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_lbl_6")));
//        ret.add(_txtEndEquity); _txtEndEquity.setEditable(false); _txtEndEquity.setForeground(Color.blue);
//        ret.add(Box.createHorizontalGlue());
        ret.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_lbl_5")));
        ret.add(_txtCagr); _txtCagr.setEditable(false); _txtCagr.setForeground(Color.blue);
        WidgetUtil.attachToolTip(_txtCagr, FrameworkConstants.FRAMEWORK_BUNDLE.getString("ttl_tip_1"),
                SwingConstants.RIGHT, SwingConstants.TOP);
        ret.add(Box.createHorizontalGlue());
        return ret;
    }
    /**
     * Convert annual return array to plottable time series
     * @param ars array of AnnualReturn objects
     * @return corresponding TimeSeries objects.
     */
    public static TimeSeries annualReturnToTimeSeries(ArrayList<AnnualReturn> ars) {
        TimeSeries ts = new TimeSeries(ars.get(0).getSymbol());
        for (AnnualReturn ar : ars) {
            Year yr = new Year(ar.getYear());
            ts.add(yr, ar.getPerformance() * 100);
        }
        return ts;
    }

    //----- private methods -----
    //handle toggle button state for graphs
    private void refreshGraphButtonState(JToggleButton selected_button) {
        if (selected_button == _btnEquityCurve) {
            _btnPriceGraph.setSelected(false);
            _btnAnnualReturnGraph.setSelected(false);
        }
        else if (selected_button == _btnPriceGraph) {
            _btnEquityCurve.setSelected(false);
            _btnAnnualReturnGraph.setSelected(false);
        }
        else if (selected_button == _btnAnnualReturnGraph) {
            _btnEquityCurve.setSelected(false);
            _btnPriceGraph.setSelected(false);
        }
    }

    //-----instance variables-----
    protected JXDatePicker _txtBeginDate = new JXDatePicker();
    protected JXDatePicker _txtEndDate = new JXDatePicker();
    protected JTextField _txtEquity = new JTextField(6);
    protected JTextField _txtCagr = new JTextField(6);
    protected JTextField _txtEndEquity = new JTextField(8);
    protected EquityGraphPanel _pnlEquityGraph;
    protected AnnualReturnGraphPanel _pnlAnnualReturnGraph;
    protected PriceGraphPanel _pnlPriceGraph;
    protected TradePanel _pnlTrade;
    protected StatPanel _pnlStat;
    protected JPanel _pnlTool;
//    protected PriceIndicatorPanel _pnlPriceIndicator;
    protected String _sCurrentSymbol;
    protected JComboBox<String> _cmbSymbol = new JComboBox<>();
    protected FundData _Fund;
    protected String _sStartDate, _sEndDate;
    protected JLabel _lblDateRange = new JLabel();
    protected JCheckBox _chkAdjClose = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_3"));
    protected JButton _btnSimOption = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_9"), FrameworkIcon.SETTING);
    protected JButton _btnExport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_5"), FrameworkIcon.EXPORT);
    protected JButton _btnSimulate = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_4"), FrameworkIcon.RUN);
    protected JCheckBox _chkLongTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_1"));
    protected JCheckBox _chkShortTrade = new JCheckBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_chk_2"));
    protected JSplitPane _splHorizontal, _splVertical;
    private JPanel _pnlCenterHolder = new JPanel(new GridLayout(1,1));//entire center area can be replaced
    private JPanel _pnlResultHolder = new JPanel(new GridLayout(1,1));
    protected JPanel _pnlGraphHolder;//holds multiple cards, default equity and annual return
    private JPanel _pnlGraphCard;//card layout holding graphs
    private JPanel _pnlStatHolder = new JPanel(new GridLayout(1,1));
    private JToggleButton _btnEquityCurve;
    private JToggleButton _btnAnnualReturnGraph;
    protected JToggleButton _btnPriceGraph;
    protected JLabel _lblGraphTitle;
    protected String _sCurrentGraph = EQUITY_GRAPH;//one of the 3 below
    protected JPanel _pnlTitleStrip;

    //-----literals-----
    protected static final String EQUITY_GRAPH = "EQUITY_GRAPH";
    protected static final String PRICE_GRAPH = "PRICE_GRAPH";
    protected static final String ANNUAL_RETURN_GRAPH = "ANNUAL_RETURN_GRAPH";
}