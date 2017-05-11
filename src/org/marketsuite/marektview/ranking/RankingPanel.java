package org.marketsuite.marektview.ranking;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.type.RankingSamplePeriod;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.marektview.Velocity.FrequencyPlotPanel;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.*;

/**
 * Main container of all ranking related graphs.
 */
public class RankingPanel extends JPanel implements PropertyChangeListener {
    public RankingPanel() {
        setLayout(new MigLayout("insets 0"));

        //north - watch list selector
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("", "5px[]push[][]20px[][][]5px", "3px[]3px"));
        initGroupSelector();//must init first
        north_pnl.add(_cmbGrpSelector = new JComboBox<>(_cmlGrpSelector));
        _cmbGrpSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    return;

                //re-calculate watch list model, plot
                String sel_grp = (String) _cmbGrpSelector.getSelectedItem();
                if (GroupStore.getInstance().getMembers(sel_grp).size() == 0) {//no symbol for this group, do nothing
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("mkt_50") + " " + sel_grp + " " +
                                    ApolloConstants.APOLLO_BUNDLE.getString("mkt_51"),
                            MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                    return;
                }
                _WatchlistModel = new WatchListModel(sel_grp, false);
                populateRankingTable(_WatchlistModel);
                prepareAndPlot();
            }
        });
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rv_lbl_02")));
        north_pnl.add(_cmbPhaseSelector = new JComboBox<>(FrameworkConstants.LIST_PHASE));
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rv_lbl_03")));
        north_pnl.add(_cmbSampleTime = new JComboBox<>(LIST_SAMPLE_TIME));
        _cmbSampleTime.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    return;
                prepareAndPlot();
            }
        });
        add(north_pnl, "dock north");

        //center - split with graph and table
        JSplitPane split_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split_pane.setDividerLocation(450);

        //top - history ranking graph
        split_pane.setTopComponent(_pnlHistoryGraph = new HistoryGraphPanel());

        //bottom - top performing symbols with 2 calendar widgets and time frame selection button
        JPanel perf_pnl = new JPanel(new MigLayout()); perf_pnl.setOpaque(false);
        JPanel wig_pnl = new JPanel(); wig_pnl.setOpaque(false);
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("mkt_36") + ":");
        wig_pnl.add(lbl);
        WidgetUtil.attachToolTip(lbl, ApolloConstants.APOLLO_BUNDLE.getString("mkt_42"),
                SwingConstants.LEFT, SwingConstants.BOTTOM);
        wig_pnl.add(_calCustom1);
        _calCustom1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                populateRankingTable(_WatchlistModel);
            }
        });
        wig_pnl.add(Box.createHorizontalBox());
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("mkt_37") + ":");
        wig_pnl.add(lbl);
        WidgetUtil.attachToolTip(lbl, ApolloConstants.APOLLO_BUNDLE.getString("mkt_43"),
                SwingConstants.LEFT, SwingConstants.BOTTOM);
        wig_pnl.add(_calCustom2);
        _calCustom2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                populateRankingTable(_WatchlistModel);
            }
        });
        wig_pnl.add(_btnTimeFrame);
        _btnTimeFrame.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] col_names = new String[TABLE_SCHEMA.length];
                for (int col = 0; col < TABLE_SCHEMA.length; col++)
                    col_names[col] = (String)TABLE_SCHEMA[col][0];
                new RankingOptionDialog(_tblPerf, col_names, LazyIcon.APP_ICON.getImage(), RankingPanel.this);
            }
        });
        wig_pnl.add(Box.createHorizontalStrut(5));
        perf_pnl.add(WidgetUtil.createTitleStrip(wig_pnl, null, null), "dock north");
        _tblPerf = WidgetUtil.createDynaTable(_tmPerf, ListSelectionModel.SINGLE_SELECTION, new SortHeaderRenderer(), false, new PerfRenderer());
        _tblPerf.setAutoCreateRowSorter(true);
        TableRowSorter sorter = (TableRowSorter)_tblPerf.getRowSorter();
        _tblPerf.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //nothing selected, disable delete, close button
                int row = _tblPerf.getSelectedRow();
                if (row == -1) //de-selection, change nothing
                    return;

                //emphasize line
                row = _tblPerf.convertRowIndexToModel(row);
                String symbol = (String)_tmPerf.getCell(row, COLUMN_SYMBOL).getValue();
                _pnlHistoryGraph.emphasizeSymbol(symbol);
            }
        });
        for (int col = COLUMN_3_MONTH; col <= COLUMN_CUSTOM_2; col++)
            sorter.setComparator(col, new PerfComparator());
        perf_pnl.add(new JScrollPane(_tblPerf), "dock center");
        split_pane.setBottomComponent(perf_pnl);
        add(split_pane, "dock center");

        //initialize
        Calendar cal = Calendar.getInstance();
        _calCustom1.setDate(cal.getTime());
        _calCustom2.setDate(cal.getTime());
        String initial_group = (String)_cmbGrpSelector.getSelectedItem();
        if (initial_group == null) //no watch list at all
            return;

        //use watchlist already loaded in MainModel to avoid reloading
        _WatchlistModel = MainModel.getInstance().getWatchListModel();
        String wl_name = _WatchlistModel.getWatchlistName();

        //if name exist in combo, select combo item
        int index = WidgetUtil.findComboItem(_cmbGrpSelector, wl_name);
        if (index != -1)
            _cmbGrpSelector.setSelectedIndex(index);//use MainModel's WatchlistModel as start
        else {
            String first_wl = (String)_cmbGrpSelector.getSelectedItem();
            if (first_wl == null) //no watch list at all, stay blank
                return;
            _WatchlistModel = new WatchListModel(first_wl, false);//use first list
        }
        prepareAndPlot();
        populateRankingTable(_WatchlistModel);
    }

//    public void plot(String symbol, ArrayList<Ranking> rankings, FundData fund) {
//        _pnlFrequencyPlot.plot(symbol, rankings, fund);
////        _pnlProgress.plot(symbol, rankings, fund);
//        _tmPerf.populate();
//    }

//TODO not good OO design
    void populateRankingTable(int[] tfc) {
        HashMap<Integer, ArrayList<RankElement>> rank_map = calcRankByTimeFrames(tfc, _WatchlistModel);
        _tmPerf.populate(rank_map, _WatchlistModel.getMembers().size());
    }
    private void populateRankingTable(WatchListModel model) {
        if (model.getMembers().size() == 0)//no member
            return;
        _WatchlistModel = model;
        int[] tfc = new int[] {
            AppUtil.COLUMN_3_MONTH_PCT, AppUtil.COLUMN_2_MONTH_PCT,
            AppUtil.COLUMN_4_WEEK_PCT,  AppUtil.COLUMN_2_WEEK_PCT,
            AppUtil.COLUMN_1_WEEK_PCT, CUSTOM1, CUSTOM2
        };
        HashMap<Integer, ArrayList<RankElement>> rank_map = calcRankByTimeFrames(tfc, model);
        _tmPerf.populate(rank_map, model.getMembers().size());
    }

    //----- interface, override -----
    public void propertyChange(PropertyChangeEvent evt) {

        //compute ranking for RankingPanel
//TODO                ArrayList<Ranking> rankings;
//                FundData fund = mki.getFund();
//                rankings = calcRankings(mkis, fund.getSize() - 1);
//                if (rankings.size() > 0)
//                    for (Ranking rnk : rankings) {
//                        ArrayList<Integer> freqs = calcRankFrequency(rnk.ranks, rankings.size());
//                        rnk.freqs = freqs;
//                    }
//                _pnlRankingGraph.plot(sym, rankings, fund);

    }

    //----- public methods -----
    public void initGroupSelector() {
        //prefill with all the watch list groups
        ArrayList<String> groups = GroupStore.getInstance().getGroupNames();
        _cmlGrpSelector.removeAllElements();
        for (int idx = 0; idx < groups.size(); idx++) {
//            if (groups.get(idx).startsWith("ETF - ") || groups.get(idx).startsWith("GRP - "))
                _cmlGrpSelector.addElement(groups.get(idx));
        }
    }
    public void handleSymbolSelection(String symbol) {
        //compute ranking for RankingPanel
        HashMap<String, MarketInfo> mkis = _WatchlistModel.getMarketInfoMap();
        MarketInfo mki = mkis.get(symbol);
        if (mki == null)//no such symbol, skip
            return;
        ArrayList<Ranking> rankings;
        FundData fund = mki.getFund();
        rankings = AppUtil.calcRankings(mkis, fund.getSize() - 1);
        if (rankings.size() > 0)
            for (Ranking rnk : rankings) {
                ArrayList<Integer> freqs = AppUtil.calcRankFrequency(rnk.ranks, rankings.size());
                rnk.freqs = freqs;
            }
//TODO        _pnlRankingGraph.plot(sym, rankings, fund);

    }
    public void handleSymbolRemoval(String symbol) {
//TODO
    }

    //----- private methods -----
    //convert time frame codes into begin index from most recent quote date
    public static int timeCodeToBeginIndex(int timeframe_code, FundData fund) {
        Calendar end_cal = AppUtil.stringToCalendarNoEx(fund.getQuote().get(0).getDate());
        Calendar begin_cal = Calendar.getInstance();
        switch (timeframe_code) {
            case CUSTOM1:
                Date begin1_date = _calCustom1.getDate();
                begin_cal.setTime(begin1_date);
                FundQuote quote = AppUtil.findNearestQuote(fund, begin_cal);
                begin_cal = AppUtil.stringToCalendarNoEx(quote.getDate());
                break;

            case CUSTOM2:
                Date begin2_date = _calCustom2.getDate();
                begin_cal.setTime(begin2_date);
                quote = AppUtil.findNearestQuote(fund, begin_cal);
                begin_cal = AppUtil.stringToCalendarNoEx(quote.getDate());
                break;

            default:
                begin_cal = AppUtil.calcBeginTime(fund, end_cal, timeframe_code);
        }
        return fund.findIndexByDate(AppUtil.calendarToString(begin_cal));
    }

    //convert time frame codes into begin index from specified date (always has quote)
    public static int timeCodeToBeginIndexFromDate(int timeframe_code, Calendar end_cal, FundData fund) {
        Calendar begin_cal = AppUtil.calcBeginTime(fund, end_cal, timeframe_code);
        return fund.findIndexByDate(AppUtil.calendarToString(begin_cal));
    }

    //calculate rank arrays across specified time frames up to present
    public static HashMap<Integer, ArrayList<RankElement>> calcRankByTimeFrames(int[] timeframe_codes, WatchListModel model) {
        HashMap<Integer, ArrayList<RankElement>> ret = new HashMap<>();
        MarketInfo mki = model.getMarketInfo(model.getMembers().get(0));
        for (int idx = 0; idx < timeframe_codes.length; idx++) {
            if (timeframe_codes[idx] == 0)
                continue;//skip empty ones
if (mki == null)
    System.err.println("=====");
            int begin_idx = timeCodeToBeginIndex(timeframe_codes[idx], mki.getFund());
            ArrayList<RankElement> res = AppUtil.calcRank(begin_idx, 0, model);
            ret.put(new Integer(timeframe_codes[idx]), res);
        }
        return ret;
    }

    //prepare map and calendar array for plotting based on sample time in combo
    private void prepareAndPlot() {
        int st_idx = _cmbSampleTime.getSelectedIndex();
        switch (st_idx) {
            case 0://weekly
                ArrayList<Integer> fridays = AppUtil.collectQuoteIndices(RankingSamplePeriod.WEEKLY, 60);//TODO make it literal or user selectable, make sure it's in range
                HashMap<String, ArrayList<Integer>> wtw_ranks = AppUtil.calcStsRankingNewToOld(_WatchlistModel, fridays);
                ArrayList<Calendar> cals = new ArrayList<>();
                for (int i = 1; i < fridays.size(); i++)
                    cals.add(AppUtil.indexToCalendar(fridays.get(i)));
                _pnlHistoryGraph.plotRanks(wtw_ranks, cals);
                break;

            case 1://semi-monthly
                break;

            case 2://monthly
                ArrayList<Integer> month_ends = AppUtil.collectQuoteIndices(RankingSamplePeriod.MONTHLY, 200);//TODO make it literal or user selectable, make sure it's in range
                HashMap<String, ArrayList<Integer>> mtm_ranks = AppUtil.calcStsRankingNewToOld(_WatchlistModel, month_ends);
                cals = new ArrayList<>();
                for (Integer me_idx : month_ends)
                    cals.add(AppUtil.indexToCalendar(me_idx));
                _pnlHistoryGraph.plotRanks(mtm_ranks, cals);
                break;

            case 3://quarterly
                break;

            case 4://semi-annually
                break;

            case 5://annually
                break;
        }
    }

    //----- inner classes -----
    private class PerfTableModel extends DynaTableModel {
        private PerfTableModel() {
            super(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }
        public boolean isCellEditable(int row, int column) { return false; }
        public void populate() {}
        public void populate(HashMap<Integer, ArrayList<RankElement>> rank_map, int symbol_count) {
            //empty table with all empty cells except symbols from the first element
            _lstRows.clear();
            for (int row = 0; row < symbol_count; row++ ) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                for (int col = 0; col < TABLE_SCHEMA.length; col++)
                    cells[col] = new SimpleCell("");
                _lstRows.add(cells);
            }
            Iterator<Integer> itor = rank_map.keySet().iterator();
            while (itor.hasNext()) {
                int time_code = itor.next();
                ArrayList<RankElement> res = rank_map.get(time_code);
                int column;
                switch (time_code) {
                    case AppUtil.COLUMN_3_MONTH_PCT:
                        column = COLUMN_3_MONTH;
                        break;

                    case AppUtil.COLUMN_2_MONTH_PCT:
                        column = COLUMN_2_MONTH;
                        break;

                    case AppUtil.COLUMN_4_WEEK_PCT:
                        column = COLUMN_1_MONTH;
                        break;

                    case AppUtil.COLUMN_2_WEEK_PCT:
                        column = COLUMN_2_WEEK;
                        break;

                    case AppUtil.COLUMN_1_WEEK_PCT:
                        column = COLUMN_1_WEEK;
                        break;

                    case CUSTOM1:
                        column = COLUMN_CUSTOM_1;
                        break;

                    case CUSTOM2:
                        column = COLUMN_CUSTOM_2;
                        break;

                    default:
                        return;
                }
                for (RankElement re : res) {
                    //look up matching symbol row, put percent there, if empty symbol, use there
                    for (int row=0; row<getRowCount(); row++) {
                        String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
                        if (sym.equals("")) {
                            getCell(row, COLUMN_SYMBOL).setValue(re.symbol);
                            getCell(row, column).setValue(re);
                            break;
                        }
                        else if (sym.equals(re.symbol)) {
                            getCell(row, column).setValue(re);
                            break;
                        }
                    }
                }
            }

            //calculate top N frequency column
            for (int row = 0; row < getRowCount(); row++) {
                int top_freq = 0;
                for (int col = COLUMN_3_MONTH; col <= COLUMN_CUSTOM_2; col++) {
                    Object value = getCell(row, col).getValue();
                    if (value instanceof RankElement) {
                        RankElement re = (RankElement)value;
                        if (re.rank <= 3)
                            top_freq ++;
                    }
                }
                getCell(row, COLUMN_TOTAL).setValue(new Long(top_freq));
            }
            fireTableDataChanged();
        }
    }
    private class PerfRenderer extends DynaTableCellRenderer {
        private PerfRenderer() {
            super(_tmPerf);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            row = _tblPerf.convertRowIndexToModel(row);
            column = _tblPerf.convertColumnIndexToModel(column);
            Object val = _tmPerf.getCell(row, column).getValue();
            JLabel lbl = (JLabel)comp;
            lbl.setToolTipText(null);
            if (column == COLUMN_SYMBOL)
                return comp;

            if (val instanceof RankElement) {
                RankElement re = (RankElement)val;
                lbl.setText(String.valueOf(re.rank));
                lbl.setToolTipText(re.symbol + ": " + FrameworkConstants.ROI_FORMAT.format(((RankElement) val).pct));
            }
            return comp;
        }
    }
    private class PerfComparator implements Comparator<Object> , Serializable {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof String || o2 instanceof String)//skip empty columns
                return -1;
            RankElement r1 = (RankElement)o1;
            RankElement r2 = (RankElement)o2;
            if (r1.pct > r2.pct) return 1;
            else if (r1.pct < r2.pct) return -1;
            return 0;
        }
    }

    //----- variables -----
    private JComboBox<String> _cmbGrpSelector, _cmbPhaseSelector, _cmbSampleTime;
    private DefaultComboBoxModel<String> _cmlGrpSelector = new DefaultComboBoxModel<>();
    private HistoryGraphPanel _pnlHistoryGraph;
    private FrequencyPlotPanel _pnlFrequencyPlot;
    private static JXDatePicker _calCustom1 = new JXDatePicker();
    private static JXDatePicker _calCustom2 = new JXDatePicker();
    private JButton _btnTimeFrame = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("mkt_41"), FrameworkIcon.RANGE);
    private JTable _tblPerf;
    private PerfTableModel _tmPerf = new PerfTableModel();
//TODO    private RankProgressionPanel _pnlProgress;

    private WatchListModel _WatchlistModel;

    //----- literals -----
    private static final String[] LIST_SAMPLE_TIME = {
        "Weekly",
        "Semi-Monthly",
        "Monthly",
        "Quarterly",
        "Semi-Annually",
        "Yearly"
    };
            static final int CUSTOM1 = 10;
            static final int CUSTOM2 = 20;
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_3_MONTH = 1;
    private static final int COLUMN_2_MONTH = 2;
    private static final int COLUMN_1_MONTH = 3;
    private static final int COLUMN_2_WEEK = 4;
    private static final int COLUMN_1_WEEK = 5;
    private static final int COLUMN_CUSTOM_1 = 6;
    private static final int COLUMN_CUSTOM_2 = 7;
    private static final int COLUMN_TOTAL = 8;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_30"), ColumnTypeEnum.TYPE_STRING,  2, 30, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_31"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//3 month
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_32"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//2 month
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_33"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//1 month
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_34"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//2 week
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_35"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//1 week
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_36"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//custom 1
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_37"), ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},//custom 2
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_38"), ColumnTypeEnum.TYPE_LONG, -1, 50, null, null, null},//top N freq
    };
}