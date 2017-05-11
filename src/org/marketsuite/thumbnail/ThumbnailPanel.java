package org.marketsuite.thumbnail;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.graph.SimpleTimeSeriesGraph;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.PerfTimeframe;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.graph.SimpleTimeSeriesGraph;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.PerfTimeframe;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Calendar;

//represent collection of thumbnails
public class ThumbnailPanel extends JPanel {
    public ThumbnailPanel(boolean stand_alone) {//true = separate internal frame
        _bStandAloneFrame = stand_alone;
        setOpaque(false); setLayout(new MigLayout("insets 0"));

        //title strip, group selection
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5[][]push[]10[]5", "3[]3"));
        if (!stand_alone) north_pnl.setLayout(new MigLayout("", "5[]20[]push[]5", "3[]3"));

        //only in stand alone frame, show watch list drop down - select group widget
        if (stand_alone) {
            north_pnl.add(new JLabel("Watch List:"));
            initGroupSelector();
            north_pnl.add(_cmbGrpSelector = new JComboBox<>(_cmlGrpSelector));
            _cmbGrpSelector.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) return;

                    //re-calculate watch list model, plot
                    String sel_grp = (String) _cmbGrpSelector.getSelectedItem();
                    ArrayList<String> members = GroupStore.getInstance().getMembers(sel_grp);
                    if (members == null || members.size() == 0) {//no symbol for this group, do nothing
                        MessageBox.messageBox(MdiMainFrame.getInstance(),
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                ApolloConstants.APOLLO_BUNDLE.getString("mkt_50") + " " + sel_grp + " " +
                                        ApolloConstants.APOLLO_BUNDLE.getString("mkt_51"),
                                MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                        return;
                    }
                    renderThumbnails(members);
                }
            });
        }
        north_pnl.add(_cmbTimeframe); _cmbTimeframe.setSelectedIndex(1);
        _cmbTimeframe.addItemListener(_Listener);
        north_pnl.add(_lblCount);
        north_pnl.add(_btnZoom); _btnZoom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_dimThumb == THUMB_SIZE1) {
                    _dimThumb = THUMB_SIZE2;
                    _pnlHolder.setLayout(new MigLayout("insets 0, wrap 2"));
                }
                else if (_dimThumb == THUMB_SIZE2) {
                    _dimThumb = THUMB_SIZE3;
                    _pnlHolder.setLayout(new MigLayout("insets 0, wrap 1"));
                }
                else if (_dimThumb == THUMB_SIZE3) {
                    _dimThumb = THUMB_SIZE1;
                    _pnlHolder.setLayout(new MigLayout("insets 0, wrap 3"));
                }
                renderThumbnails(_WatchListModel.getMembers());
            }
        });
        add(north_pnl, "dock north");

        //center - holder of all the thumb nails
        _pnlHolder = new JPanel(new MigLayout("insets 0, wrap 3")); _pnlHolder.setOpaque(false);
        _scrHolder = new JScrollPane(_pnlHolder);
        _scrHolder.getVerticalScrollBar().setUnitIncrement(10);
        add(_scrHolder, "dock center");//put inside scroll pane will make each graph go back to original size
        _WatchListModel = MainModel.getInstance().getWatchListModel();
    }

    //----- public/protected methods -----
    public void changeTimeFrame(PerfTimeframe timeFrame) {
        _cmbTimeframe.removeItemListener(_Listener);
        _cmbTimeframe.setSelectedItem(timeFrame);
        _cmbTimeframe.addItemListener(_Listener);
    }
    //plot _pnlGraphs for all the symbols
    public WatchListModel renderThumbnails(ArrayList<String> symbols) {
        //create watch list model from symbols
        _WatchListModel = new WatchListModel(symbols, "");
        _pnlHolder.removeAll(); _pnlGraphs.clear();
        ArrayList<LogMessage> failed_msgs = new ArrayList<>();//might fail, keep a list of errors
        for (String sym : symbols) {
            SimpleTimeSeriesGraph graph = new SimpleTimeSeriesGraph("", sym, null);
            graph.setPreferredSize(_dimThumb);//use this to fix size of plot
            _pnlHolder.add(graph); _pnlGraphs.add(graph);

            //get mki from watch list model, populate data series
            MarketInfo mki = _WatchListModel.getMarketInfoMap().get(sym);
            if (mki == null) {//can't find data, skip
                LogMessage lm = new LogMessage(LoggingSource.THUMBNAIL, sym + ApolloConstants.APOLLO_BUNDLE.getString("tn_03"), null);
                failed_msgs.add(lm);
                continue;
            }

            //for 3 year time frame, use weekly quotes
            String name = sym;
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            int data_size = mki.getFund().getSize();
            PerfTimeframe tf = (PerfTimeframe) _cmbTimeframe.getSelectedItem();
            int start_index = PerfTimeframe.calcStartIndex(quotes, _nEndIndex, tf);//TODO originally 0
            switch (tf) {
                //use daily for shorter time frame
                case THREE_MONTH:
                case SIX_MONTH:
                case ONE_YEAR: data_size = start_index + 1;
                    break;

                //use weekly for longer time frame
                case THREE_YEAR:
                case FIVE_YEAR:
                    WeeklyQuote weekly_quote = new WeeklyQuote(mki.getFund(), mki.getFund().getSize() - 1);
//TODO need to adjust for split as well
                    data_size = weekly_quote.getSize();
                    quotes = weekly_quote.getQuotes();
                    float[] p = new float[data_size];
                    for (int idx = 0; idx < data_size; idx++)
                        p[idx] = quotes.get(idx).getClose();
                    SMA sma50 = new SMA(10, p);//10 week = 50 day
                    Calendar[] dates = new Calendar[data_size];
                    double[] prices = new double[data_size];
                    double[] ma = new double[data_size];
                    for (int i = 0; i < data_size; i++) {
                        dates[i] = AppUtil.stringToCalendarNoEx(quotes.get(i).getDate());
                        prices[i] = quotes.get(i).getClose();
                        ma[i] = sma50.getSma()[i];
                    }
                    graph.plot(name, dates, prices, ma);
                    continue;
            }
            //for 1 year or less
            Calendar[] dates = new Calendar[data_size];
            double[] prices = new double[data_size];
            double[] ma = new double[data_size];
            for (int i = 0; i < data_size; i++) {
                dates[i] = AppUtil.stringToCalendarNoEx(quotes.get(i).getDate());
                prices[i] = quotes.get(i).getClose();
                float[] sma50 = mki.getSma50();
                if (sma50 != null)
                    ma[i] = sma50[i];
            }
            graph.plot(name, dates, prices, ma);
        }
        validate();//must redo layout, must do this to make thumbnails shown
        repaint();//must repaint to clear view first
        if (failed_msgs.size() > 0)
            Props.Log.setValue(null, failed_msgs);
        if (symbols.size() > 0) {
            emphasizeSymbol(symbols.get(0));//emphasize first one
            _lblCount.setText("#" + symbols.size());
        }
        return _WatchListModel;
    }
    public WatchListModel renderThumbnails(ArrayList<String> symbols, int end_index) {
        _nEndIndex = end_index;
        return renderThumbnails(symbols);
    }
    public void initGroupSelector() {
        //prefill with all the watch list groups
        ArrayList<String> groups = GroupStore.getInstance().getGroupNames();
        _cmlGrpSelector.removeAllElements();
        for (int idx = 0; idx < groups.size(); idx++) {
//            if (groups.get(idx).startsWith("ETF - ") || groups.get(idx).startsWith("GRP - "))
                _cmlGrpSelector.addElement(groups.get(idx));
        }
    }
    public void emphasizeSymbol(String new_sym) {//find chart and emphasize it
        for (SimpleTimeSeriesGraph graph : _pnlGraphs) {
            String graph_symbol = graph.getSymbol();
            if (graph_symbol.equals(curSymbol))//prev maybe null
                graph.emphasizeThumbnail(false);//turn off previously hilighted
            else if (graph_symbol.equals(new_sym)) {
                graph.emphasizeThumbnail(true);
//                _scrHolder.scrollRectToVisible(graph.getBounds());
//                this.scrollRectToVisible(graph.getBounds());
            }
        }
        curSymbol = new_sym;
    }

    //----- inner classes -----
    private class TimeframeListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) return;
            renderThumbnails(_WatchListModel.getMembers());
        }
    }

    //----- variables -----
    private JPanel _pnlHolder; private boolean _bStandAloneFrame; private JScrollPane _scrHolder;
    private ArrayList<SimpleTimeSeriesGraph> _pnlGraphs = new ArrayList<>();
    private WatchListModel _WatchListModel;//master within frame and sub-panels
    private JComboBox<String> _cmbGrpSelector;//by default select first item
    private DefaultComboBoxModel<String> _cmlGrpSelector = new DefaultComboBoxModel<>();
    private JLabel _lblCount = new JLabel();
    private JButton _btnZoom = WidgetUtil.createIconButton("Magnify Thumbnails", FrameworkIcon.MAGNIFIER);
    private JComboBox<PerfTimeframe> _cmbTimeframe = new JComboBox<>(LIST_TIMEFRAMES);
    private TimeframeListener _Listener = new TimeframeListener();
    private Dimension _dimThumb = THUMB_SIZE1;
    private String curSymbol;//previously emphasized symbol, null = none selected previously
    private int _nEndIndex = 0;

    //----- literals -----
    private static final Dimension THUMB_SIZE1 = new Dimension(200, 150);
    private static final Dimension THUMB_SIZE2 = new Dimension(300, 200);
    private static final Dimension THUMB_SIZE3 = new Dimension(500, 350);
    private static final PerfTimeframe[] LIST_TIMEFRAMES = { PerfTimeframe.THREE_MONTH, PerfTimeframe.SIX_MONTH,
        PerfTimeframe.ONE_YEAR, PerfTimeframe.THREE_YEAR, PerfTimeframe.FIVE_YEAR };
}
//TODO - (BUG)Emphasize 1st thumbnail after sorting(click column header, click link), loading new watch list,
//            remove symbol, top N, filter, reload all symbols
//TODO - (ENH)Replace fields with spinner, automatically change limit after remove symbol, reload all
//TODO - (BUG)Auto-scroll does NOT work for emphasizing thumbnail outside visible area
//TODO - (ENH)3 year or 5 year seems to make no difference : due to watch list only loaded 450 data points, load more dynamically