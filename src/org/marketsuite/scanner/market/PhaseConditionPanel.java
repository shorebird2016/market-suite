package org.marketsuite.scanner.market;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.ChangeInfo;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.scanner.common.TimeSeriesPanel;
import org.jdesktop.swingx.JXDatePicker;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.framework.market.ChangeInfo;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

/**
 * Container for showing most recent phase change (Price/50SMA/200SMA relationship)
 *   and condition change (Price / 10SMA / 30SMA relationship)
 */
public class PhaseConditionPanel extends SkinPanel {
    public PhaseConditionPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        setLayout(new BorderLayout());
        setOpaque(false);

        //north - title strip, calender widget, strategy type, parameter widgets
        JPanel west_pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        west_pnl.setOpaque(false);
        west_pnl.add(Box.createGlue());
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_lbl_1"));
        lbl.setFont(Constants.FONT_BOLD);
        west_pnl.add(lbl);
        west_pnl.add(_txtStartDate);

        //find last monday as default starting day
        Calendar cal = AppUtil.calcPastMonday(Calendar.getInstance());
        Date past_monday = cal.getTime();
        _txtStartDate.setDate(past_monday);
        west_pnl.add(Box.createHorizontalStrut(5));

        //filter by phase
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_lbl_4"));
        lbl.setFont(Constants.FONT_BOLD);
        west_pnl.add(lbl);
        west_pnl.add(_cmbPhase);
        _cmbPhase.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
                    return;//skip this

                String phase = (String) _cmbPhase.getSelectedItem();
                if (phase.equals(LIST_FILTER[0]))
                    _TableModel.populate();
                else
                    _TableModel.populate(phase, _cmbCondition.getSelectedIndex());
            }
        });
        west_pnl.add(Box.createHorizontalStrut(5));

        //filter by condition
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_lbl_5"));
        lbl.setFont(Constants.FONT_BOLD);
        west_pnl.add(lbl);
        west_pnl.add(_cmbCondition);
        _cmbCondition.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED)
                    return;//skip this

                String phase = (String) _cmbPhase.getSelectedItem();
                _TableModel.populate(phase, _cmbCondition.getSelectedIndex());
            }
        });
        west_pnl.add(Box.createHorizontalStrut(5));

        //search field
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_lbl_3"));
        lbl.setFont(Constants.FONT_BOLD);
        west_pnl.add(lbl);
        west_pnl.add(_txtSearch);
        _txtSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String txt = _txtSearch.getText().toUpperCase();
                if (txt.length() == 0)
                    return;

                findSymbol(txt);
            }
        });
        west_pnl.add(_btnSearch);
        _btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String txt = _txtSearch.getText().toUpperCase();
                if (txt.length() == 0)
                    return;

                findSymbol(txt);
            }
        });

        //east - tool buttons
        JPanel east_pnl = new JPanel();
        east_pnl.setOpaque(false);
        east_pnl.add(_btnRefresh);
        _btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _TableModel.clear();
                scanMarket();
                _cmbPhase.setSelectedIndex(0);
                _cmbCondition.setSelectedIndex(0);
            }
        });
        east_pnl.add(Box.createGlue());
        east_pnl.add(_btnExport);
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                FileUtil.exportSheet(_TableModel, new File(FrameworkConstants.DATA_FOLDER_EXPORT));
            }
        });
        east_pnl.add(Box.createGlue());//nice space at end
        SkinPanel ttl_pnl = WidgetUtil.createTitleStrip(west_pnl, null, east_pnl);
        add(ttl_pnl, BorderLayout.NORTH);

        //center - result table and price chart
        JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        spl.setContinuousLayout(true);
        spl.setDividerLocation(350);
        _tblPhaseCondition = new JTable(_TableModel);
//        JTable tbl = new JTable(_TableModel) {
//            //paint a line after summary row to be more readable
//            public void paintComponent(Graphics g) {
//                super.paintComponent(g);
//                Graphics2D g2 = (Graphics2D) g;
//                Dimension size = getSize();
//                g2.setColor(Color.gray);
//
//                //look thru table, if symbol column goes from blank to non-blank, draw a line at bottom
//                String last_symbol = "-";//non-blank
//                for (int row = 0; row < _TableModel.getRowCount(); row++) {
//                    Object sym = _TableModel.getCell(row, COLUMN_SYMBOL).getValue();
//                    //from blank to non-blank or symbol A to symbol B, or last row
//                    if ((last_symbol.equals("") && !sym.equals(""))
//                            || (!last_symbol.equals("") && !sym.equals("") && !last_symbol.equals(sym))
//                            ) {
//                        int height = (row) * getRowHeight();
//                        g2.drawLine(0, height, size.width, height);
//                    }
//                    if (row == _TableModel.getRowCount() - 1) {
//                        int height = (row + 1) * getRowHeight();
//                        g2.drawLine(0, height, size.width, height);
//                    }
//                    last_symbol = (String) sym;
//                }
//            }
//        };
        WidgetUtil.initDynaTable(_tblPhaseCondition, _TableModel, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), false, new DynaTableCellRenderer(_TableModel));
        _tblPhaseCondition.setAutoCreateRowSorter(true);
        _tblPhaseCondition.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //nothing selected, disable delete, close button
                int row = _tblPhaseCondition.getSelectedRow();
                if (row == -1) {//de-selection
                    _pnlTimeSeriesGraph.clear();
                    return;
                }

                //draw graph
                try {
                    _pnlTimeSeriesGraph.plot(_MarketInfo.get(row));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        JScrollPane scr = new JScrollPane(_tblPhaseCondition);
        scr.getViewport().setOpaque(false);
        spl.setTopComponent(scr);
        spl.setBottomComponent(_pnlTimeSeriesGraph = new TimeSeriesPanel());
        add(spl, BorderLayout.CENTER);
    }

    //----- private methods -----
    //perform full scan to get information for all symbols, builds up local cache
    private void scanMarket() {
        //show initial progress bar
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(),
            ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_1") + " " +
            ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_6"));
        pb.setVisible(true);

        //scan inside a thread
        Thread scan_thread = new Thread() {
            public void run() {
                File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
                String[] file_list = folder.list();
                for (String file : file_list) {
                    if (!file.endsWith(FrameworkConstants.EXTENSION_QUOTE))//skip garbage
                        continue;

                    //extract symbol from file name
                    final String symbol = file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE));
                    MarketInfo mi = null;
                    try {
                        mi = MarketUtil.calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH,
                                new DivergenceOption(5, 90, 3));//TODO need to warn user heap space
                    } catch (Exception pe) {
                        if (mi == null)
                            continue;

                        mi.setCurrentPhase(ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_4"));
                        mi.setCurrentCondition(FrameworkConstants.CONDITION_NA);
//TODO: collect these symbols into a separate window to the right
//                        e.printStackTrace();
                    }
                    _MarketInfo.add(mi);//use 20 as small margin

                    //update progress bar
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_7") + " " + symbol);
                        }
                    });
                }

                //send to EDT, display in table
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        _TableModel.populate();
                        pb.setVisible(false);
                    }
                });
            }
        };
        scan_thread.start();
    }

    //find and scroll view
    private void findSymbol(String symbol) {
        //find which row and scroll into view
        int row = _TableModel.findSymbol(symbol);
        if (row < 0) {
            MessageBox.messageBox(MdiMainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_5"), MessageBox.STYLE_OK,
                MessageBox.WARNING_MESSAGE);
            return;
        }
        WidgetUtil.scrollCellVisible(_tblPhaseCondition, row, COLUMN_SYMBOL);
        _tblPhaseCondition.getSelectionModel().setSelectionInterval(row, row);
    }

    private void exportReports() throws IOException {
        //ask user for file name and rsp
        JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rsp = fc.showSaveDialog(null);//todo MainFrame.getInstance() centering
        if (rsp == JFileChooser.APPROVE_OPTION) {
            File output_path = fc.getSelectedFile();
            if (output_path.exists()) { //warn user if file exist
                if (MessageBox.messageBox(
                        null, //todo MainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("exp_msg_1"),
                        MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                    return;
            }

            //write lines into this file from table model
            PrintWriter pw = new PrintWriter(new FileWriter(output_path + FrameworkConstants.EXTENSION_TRADES));
            pw.println("NAME=MARKET SCAN");
            pw.println("Symbol, Phase, Condition, Recent Phase Change(date/from), Recent Condition Change(2->1), Recent Condition Change(4->3)");
            int row_cnt = _TableModel.getRowCount();
            for (int row=0; row<row_cnt; row++) {
                SimpleCell[] cells = _TableModel.getRow(row);
                StringBuilder sb = new StringBuilder();
                for (SimpleCell cell : cells)
                    sb.append(cell.getValue()).append(",");
                pw.println(sb.toString());
            }
            pw.flush();
            pw.close();
        }
    }

    //----- inner classes -----
    private class MarketTableModel extends DynaTableModel {
        public MarketTableModel() {
            remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }

        //-----interface implementations-----
        //use _MarketInfo to populate table
        public void populate() {
            if (_MarketInfo.size() <= 0)
                return;

            _lstRows.clear();
            for (MarketInfo mki : _MarketInfo) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_SYMBOL] = new SimpleCell(mki.getSymbol());
                cells[COLUMN_PHASE] = new SimpleCell(mki.getCurrentPhase());
                int condition = mki.getCurrentCondition();
                cells[COLUMN_CONDITION] = new SimpleCell(
                    condition == FrameworkConstants.CONDITION_NA ? "" : String.valueOf(condition));
                cells[COLUMN_PHASE_CHANGE] = new SimpleCell(getLastChange(true, mki.getPhaseChanges()));
                cells[COLUMN_CONDITION_CHANGE21] = new SimpleCell(getLastChange(false, mki.getConditionChanges21()));
                cells[COLUMN_CONDITION_CHANGE43] = new SimpleCell(getLastChange(false, mki.getConditionChanges43()));
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        //filter cache data using selected phase
        private void populate(String phase, int condition_index) {//filter = one item from combo
            if (_MarketInfo.size() <= 0)
                return;

            _lstRows.clear();
            for (MarketInfo mki : _MarketInfo) {
                int cur_condition = mki.getCurrentCondition();
                switch (condition_index) {
                    case 0://all conditions
                        break;

                    case 1://only condition 1
                        if (cur_condition != 1)
                            continue;
                        break;

                    case 2://only condition 2
                        if (cur_condition != 2)
                            continue;
                        break;

                    case 3://only condition 3
                        if (cur_condition != 3)
                            continue;
                        break;

                    case 4://only condition 4
                        if (cur_condition != 4)
                            continue;
                        break;

                    case 5://condition 2, 4
                        if (cur_condition != 2 && cur_condition != 4)
                            continue;
                        break;

                    default:
                        continue;
                }
                if (!mki.getCurrentPhase().equals(phase))
                    continue;

                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_SYMBOL] = new SimpleCell(mki.getSymbol());
                cells[COLUMN_PHASE] = new SimpleCell(mki.getCurrentPhase());
                int condition = mki.getCurrentCondition();
                cells[COLUMN_CONDITION] = new SimpleCell(
                        condition == FrameworkConstants.CONDITION_NA ? "" : String.valueOf(condition));
                cells[COLUMN_PHASE_CHANGE] = new SimpleCell(getLastChange(true, mki.getPhaseChanges()));
                cells[COLUMN_CONDITION_CHANGE21] = new SimpleCell(getLastChange(false, mki.getConditionChanges21()));
                cells[COLUMN_CONDITION_CHANGE43] = new SimpleCell(getLastChange(false, mki.getConditionChanges43()));
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }

        //find a symbol and return row index, return -1 if not found
        private int findSymbol(String symbol) {
            for (int row = 0; row < getRowCount(); row++) {
                String sym = (String)getCell(row, COLUMN_SYMBOL).getValue();
                if (sym.equals(symbol))
                    return row;
            }
            return -1;
        }

        private String getLastChange(boolean phase_change, ArrayList<ChangeInfo> change_list) {
            String val = "";
            if (change_list.size() > 0) {
                int last = change_list.size() - 1;
                ChangeInfo latest = change_list.get(last);
                Calendar cal = latest.getCalendar();
                String date = AppUtil.calendarToString(cal);
                if (phase_change)
                    return "[" + date + "]  " + FrameworkConstants.LIST_PHASE[latest.getFrom() - 100];
                else
                    return "[" + date + "]";// Condition " + latest.getFrom() + "     > > >    Condition " + latest.getTo();
            }
            return val;
        }
    }

    //----- instance variables -----
    private JXDatePicker _txtStartDate = new JXDatePicker();
    private JComboBox<String> _cmbPhase = new JComboBox<>(LIST_FILTER);
    private JComboBox<String> _cmbCondition = new JComboBox<>(LIST_CONDITION);
    private JButton _btnSearch = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scan_tip_4"), FrameworkIcon.SEARCH);
    private JTextField _txtSearch = new JTextField(8);
    private JButton _btnRefresh = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("scan_tip_3"), FrameworkIcon.REFRESH);
    private JButton _btnExport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_5"), FrameworkIcon.EXPORT);
    private JTable _tblPhaseCondition;
    private MarketTableModel _TableModel = new MarketTableModel();
    private ArrayList<MarketInfo> _MarketInfo = new ArrayList<>();
    private TimeSeriesPanel _pnlTimeSeriesGraph;

    //table columns
    private static final String[] LIST_FILTER = {
        "All Phases",
        FrameworkConstants.LIST_PHASE[0],
        FrameworkConstants.LIST_PHASE[1],
        FrameworkConstants.LIST_PHASE[2],
        FrameworkConstants.LIST_PHASE[3],
        FrameworkConstants.LIST_PHASE[4],
        FrameworkConstants.LIST_PHASE[5],
        FrameworkConstants.LIST_PHASE[6],
    };
    private static final String[] LIST_CONDITION = {
        "All Conditions", "Condition 1", "Condition 2", "Condition 3", "Condition 4", "Condition 2, 4",
    };
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_PHASE = 1;
    private static final int COLUMN_CONDITION = 2;
    private static final int COLUMN_PHASE_CHANGE = 3;
    private static final int COLUMN_CONDITION_CHANGE21 = 4;
    private static final int COLUMN_CONDITION_CHANGE43 = 5;
    private static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_hd_1"), ColumnTypeEnum.TYPE_STRING, -1, 5, null, null, null},//symbol
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_hd_2"), ColumnTypeEnum.TYPE_STRING,  5, 50, null, null, null},//phase
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_hd_3"), ColumnTypeEnum.TYPE_STRING, -1, 10, null, null, null},//condition
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_hd_4"), ColumnTypeEnum.TYPE_STRING, -1, 300, null, null, null},//phase change
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_hd_5"), ColumnTypeEnum.TYPE_STRING, -1, 200, null, null, null},//CC21
        {ApolloConstants.APOLLO_BUNDLE.getString("mkt_hd_6"), ColumnTypeEnum.TYPE_STRING, -1, 200, null, null, null},//CC43
    };
}
