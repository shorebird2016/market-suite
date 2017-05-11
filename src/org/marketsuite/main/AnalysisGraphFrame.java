package org.marketsuite.main;

import org.marketsuite.chart.line.AnalysisGraphPanel;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.type.ChartType;
import org.marketsuite.framework.model.type.DatapadMode;
import org.marketsuite.framework.model.type.StrategyType;
import org.marketsuite.framework.model.type.Zoom;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.type.ChartType;
import org.marketsuite.framework.model.type.DatapadMode;
import org.marketsuite.framework.model.type.Zoom;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class AnalysisGraphFrame extends JInternalFrame implements PropertyChangeListener {
    public AnalysisGraphFrame() {
        super(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_15"), true, true, true, false);
        setName("Main");//for MainTabUI to recognize
        setFrameIcon(ApolloIcon.APP_ICON);

        //paint background image
        JPanel content_pane = new JPanel();
        content_pane.setLayout(new MigLayout("insets 0"));
        setContentPane(content_pane);
        content_pane.add(createTitleStrip(), "dock north");

        //center - card layout with 2 AnalysisGraphPanel, DCOM / EMAC
        _pnlCard = new JPanel(new CardLayout()); _pnlCard.setOpaque(false);
        _pnlCard.add(_pnlDcomAnalysisGraph = new AnalysisGraphPanel(true), CARD_DCOM_LINE);
        _pnlCard.add(_pnlEmacAnalysisGraph = new AnalysisGraphPanel(false), CARD_EMAC_LINE);
//        _pnlCard.add(_pnlDcomCandleGraph = new OldCandleGraphPanel(true), CARD_DCOM_CANDLE);
//        _pnlCard.add(_pnlEmacCandleGraph = new OldCandleGraphPanel(false), CARD_EMAC_CANDLE);
        content_pane.add(_pnlCard, "dock center");
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_ANALYSIS_LINE_CHART, MdiMainFrame.LOCATION_ANALYSIS_LINE_CHART, MdiMainFrame.SIZE_ANALYSIS_LINE_CHART);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);//must use this to bring back after close
        Props.addWeakPropertyChangeListener(Props.SymbolSelection, this);//handle symbol change
        Props.addWeakPropertyChangeListener(Props.MarketInfoChange, this);//handle symbol change
        Props.addWeakPropertyChangeListener(Props.WatchListSelected, this);//handle symbol change from watch list selection
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        if (wlm != null && wlm.getMembers().size() > 0) {
//            MainModel.getInstance().reloadIbd50DatesDb();//this is used in plots
            String symbol = wlm.getMembers().get(0);
            setTitleInfo(symbol);
            drawAllGraphs(symbol);
        }
    }

    //----- interface, override -----
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible())
            return;//setVisible(true);
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case WatchListSelected:
                String grp = (String)prop.getValue();
                String sym = GroupStore.getInstance().getMembers(grp).get(0);
                drawAllGraphs(sym);
                setTitleInfo(sym);
                break;

            case SymbolSelection://update graph
                String symbol = (String)prop.getValue();
                if (symbol == null || symbol.equals("")) {//no symbol
                    _pnlDcomAnalysisGraph.clearGraph();
//                    _pnlDcomCandleGraph.clearGraph();
                    _pnlEmacAnalysisGraph.clearGraph();
//                    _pnlEmacCandleGraph.clearGraph();
                }
                else {//draw new symbol
                    drawAllGraphs(symbol);
//                    _pnlDcomAnalysisGraph.drawGraph(symbol, true);
//                    _pnlDcomCandleGraph.drawGraph(symbol, true);
//                    _pnlEmacAnalysisGraph.drawGraph(symbol, false);
//                    _pnlEmacCandleGraph.drawGraph(symbol, false);
                    String cur_sym = _sCurrentSymbol;
                    setTitleInfo(symbol);
                    _sCurrentSymbol = cur_sym;
                }
                break;

            case MarketInfoChange:
                MarketInfo mki = (MarketInfo)prop.getValue();
                if (mki == null)
                    return;
                String new_symbol = mki.getSymbol();
                drawAllGraphs(new_symbol);
//                _pnlDcomAnalysisGraph.drawGraph(new_symbol, true);
//                _pnlDcomCandleGraph.drawGraph(new_symbol, true);
//                _pnlEmacAnalysisGraph.drawGraph(new_symbol, false);
//                _pnlEmacCandleGraph.drawGraph(new_symbol, false);
                String cur_sym = _sCurrentSymbol;
                setTitleInfo(new_symbol);
                _sCurrentSymbol = cur_sym;
                break;
        }
    }

    //----- private methods -----
    private void setTitleInfo(String symbol) {
        //read company's full name
        StringBuilder sb = new StringBuilder(symbol);
        Fundamental fundamental = MainModel.getInstance().getFundamentals().get(symbol);
        if (fundamental != null)//cant'find
            sb.append(": ").append(fundamental.getFullName());
        _lblTitle.setText(sb.toString());
        _sCurrentSymbol = symbol;
    }
    private void drawAllGraphs(String symbol) {
        _pnlDcomAnalysisGraph.drawGraph(symbol, true);
        _pnlDcomAnalysisGraph.zoom(_nTimeField, _nTimeOffset);
//        _pnlDcomCandleGraph.drawGraph(symbol, true);
        _pnlEmacAnalysisGraph.drawGraph(symbol, false);
        _pnlEmacAnalysisGraph.zoom(_nTimeField, _nTimeOffset);
//        _pnlEmacCandleGraph.drawGraph(symbol, false);
    }
    private String getCardLayoutId() {//decide which card to show based on chart type and strategy selection
        ChartType ct = ChartType.findType((String)_cmbChartType.getSelectedItem());
        StrategyType st = (StrategyType)_cmbStrategyType.getSelectedItem();
        if (ct.equals(ChartType.LineChart) && st.equals(StrategyType.DCOM))
            return CARD_DCOM_LINE;
        else if (ct.equals(ChartType.CandleChart) && st.equals(StrategyType.DCOM))
            return CARD_DCOM_CANDLE;
        else if (ct.equals(ChartType.LineChart) && st.equals(StrategyType.EMAC))
            return CARD_EMAC_LINE;
        else //if (ct.equals(ChartType.CandleChart) && st.equals(StrategyType.EMAC))
            return CARD_EMAC_CANDLE;
    }
    private JPanel createTitleStrip() {
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
            new MigLayout("", "5px[]15px[][]15px[]5px[]push[]push[]10px[]10px[]10px[]5px", "5px[]5px"));
        ttl_pnl.add(_cmbStrategyType);
        _cmbStrategyType.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    return; //skip deselect, only process selection
                ((CardLayout) _pnlCard.getLayout()).show(_pnlCard, getCardLayoutId());
            }
        });
        WidgetUtil.attachToolTip(_cmbStrategyType, ApolloConstants.APOLLO_BUNDLE.getString("cw_03"),
                SwingConstants.CENTER, SwingConstants.BOTTOM);
        JLabel lbl = new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("hdr_01"));
        ttl_pnl.add(lbl);
        ttl_pnl.add(_txtSymbol);
        _txtSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = _txtSymbol.getText().toUpperCase();
                _txtSymbol.select(0, symbol.length());//highlight symbol for easy typing next one
                if (symbol.equalsIgnoreCase(_sCurrentSymbol))
                    return;

//                Object sel = _cmbStrategyType.getSelectedItem();
                drawAllGraphs(symbol);
//                if (sel.equals(StrategyType.DCOM))
//                    _pnlDcomAnalysisGraph.drawGraph(symbol, true);
//                else
//                    _pnlEmacAnalysisGraph.drawGraph(symbol, false);
                String cur_sym = _sCurrentSymbol;
                setTitleInfo(symbol);
                _sCurrentSymbol = cur_sym;
            }
        });
        ttl_pnl.add(_btnPrev);
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
        ttl_pnl.add(_btnNext);
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
        _lblTitle = new JLabel();  _lblTitle.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl_pnl.add(_lblTitle);
        ttl_pnl.add(_cmbChartType); _cmbChartType.setEnabled(false);
        _cmbChartType.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    return; //skip deselect, only process selection

                //switch to different card based on chart type and strategy
                ((CardLayout) _pnlCard.getLayout()).show(_pnlCard, getCardLayoutId());
            }
        });
        ttl_pnl.add(_cmbZoom);
        _cmbZoom.setSelectedIndex(2);
        _cmbZoom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    return; //skip deselect, only process selection
                Zoom sel = Zoom.findZoom((String) _cmbZoom.getSelectedItem());
                if (sel.equals(Zoom.Days7)) {
                    _nTimeField = Calendar.DATE;
                    _nTimeOffset = -7;
                }
                else if (sel.equals(Zoom.Month1)) {
                    _nTimeField = Calendar.MONTH;
                    _nTimeOffset = -1;
                }
                else if (sel.equals(Zoom.Months3)) {
                    _nTimeField = Calendar.MONTH;
                    _nTimeOffset = -3;
                }
                else if (sel.equals(Zoom.Months6)) {
                    _nTimeField = Calendar.MONTH;
                    _nTimeOffset = -6;
                }
                else if (sel.equals(Zoom.Year1)) {
                    _nTimeField = Calendar.YEAR;
                    _nTimeOffset = -1;
                }
                else {//TODO data is typically less than 5 years, this is actually max
                    _nTimeField = Calendar.YEAR;
                    _nTimeOffset = -5;
                }
                Object type = _cmbStrategyType.getSelectedItem();
                if (type.equals(StrategyType.DCOM)) {
                    _pnlDcomAnalysisGraph.zoom(_nTimeField, _nTimeOffset);
//                    _pnlDcomCandleGraph.zoom(_nTimeField, _nTimeOffset);
                }
                else {
                    _pnlEmacAnalysisGraph.zoom(_nTimeField, _nTimeOffset);
//                    _pnlEmacCandleGraph.zoom(_nTimeField, _nTimeOffset);
                }
            }
        });
        ttl_pnl.add(_btnMagnifier);
        _btnMagnifier.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
//TODO remove magnifier test code
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
        ttl_pnl.add(_btnCursor);
        _btnCursor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                toggleCursor();
            }
        });
        ttl_pnl.add(_btnDataPad);
        _btnDataPad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Object sel = _cmbStrategyType.getSelectedItem();
                DatapadMode m = DatapadMode.nextMode(_DatapadMode);
                _DatapadMode = m;
                if (sel.equals(StrategyType.DCOM)) {
                    _pnlDcomAnalysisGraph.setDataPadMode(_DatapadMode);
//                    _pnlDcomCandleGraph.setDataPadMode(_DatapadMode);
                }
                else {
                    _pnlEmacAnalysisGraph.setDataPadMode(_DatapadMode);
//                    _pnlEmacCandleGraph.setDataPadMode(_DatapadMode);
                }
            }
        });
        return ttl_pnl;
    }
    private void toggleCursor() {
        Object sel = _cmbStrategyType.getSelectedItem();
        if (sel.equals(StrategyType.DCOM))
            _pnlDcomAnalysisGraph.toggleCursor();
        else
            _pnlEmacAnalysisGraph.toggleCursor();
    }

    //look up next symbol in main model's watch list model, disregard symbol on screen
    private void drawNextSymbol(boolean forward) throws ParseException {
        WatchListModel wlm = MainModel.getInstance().getWatchListModel();
        if (wlm == null || wlm.getMarketInfoMap().size() == 0)
            return;
        _txtSymbol.setText("");//clear symbol box

        //find out position of current symbol
        ArrayList<String> members = GroupStore.getInstance().getGroup(wlm.getWatchlistName());
        int pos = members.indexOf(_sCurrentSymbol);
        if (pos < 0)  return;
        if (forward) {
            pos++;  if (pos == members.size()) pos = 0;
        }
        else {
            pos--;  if (pos < 0) pos = members.size() - 1;
        }
        String next_sym = members.get(pos);
        drawAllGraphs(next_sym);
        setTitleInfo(next_sym);
    }

    //----- instance variables-----
    private JPanel _pnlCard;
    private String _sCurrentSymbol;
    private JComboBox<StrategyType> _cmbStrategyType = new JComboBox<>(StrategyType.values());
    protected NameField _txtSymbol = new NameField(3);
    private JButton _btnPrev = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_3"), FrameworkIcon.ARROW_3D_LEFT);
    private JButton _btnNext = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_2"), FrameworkIcon.ARROW_3D_RIGHT);
    protected JLabel _lblTitle;
    private JComboBox<String> _cmbZoom = new JComboBox<>(Zoom.toStrings());
    private int _nTimeField = Calendar.MONTH;//used for zooming, default 3 months
    private int _nTimeOffset = -3;
    private JComboBox<String> _cmbChartType = new JComboBox<>(ChartType.toStrings());
    private JButton _btnCursor = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("graph_tip_9"), FrameworkIcon.CURSOR);
    private JButton _btnMagnifier = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_1"), FrameworkIcon.MAGNIFIER);
    private JButton _btnDataPad = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("btn_lbl_4"), FrameworkIcon.REPORT);//chart data
    private AnalysisGraphPanel _pnlDcomAnalysisGraph, _pnlEmacAnalysisGraph;
    private DatapadMode _DatapadMode = DatapadMode.FOLLOW_DOT;

    //----- literals -----
    private static final String CARD_DCOM_LINE = "CARD_DCOM_LINE";
    private static final String CARD_EMAC_LINE = "CARD_EMAC_LINE";
    private static final String CARD_DCOM_CANDLE = "CARD_DCOM_CANDLE";
    private static final String CARD_EMAC_CANDLE = "CARD_EMAC_CANDLE";
}