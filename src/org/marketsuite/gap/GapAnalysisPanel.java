package org.marketsuite.gap;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.datamgr.dataimport.PickWatchlistDialog;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.data.GapInfo;
import org.marketsuite.framework.model.type.CalendarQuarter;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.marketsuite.datamgr.dataimport.PickWatchlistDialog;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.*;

/**
 * A view for user to analyze contribution of each variable toward earning gap behavior.  Left side presents a table of
 * various measuring points after earning gap, right side presents a list of variables that appear to affect earning gap performance.
 */
public class GapAnalysisPanel extends JPanel {
    public GapAnalysisPanel() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl_pnl = new JPanel(new MigLayout("", "5[][]30[][]20[]push[]5", "3[]3"));
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("gps_list")));
        ttl_pnl.add(_cmbSym); _cmbSym.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;//skip this one
                int sel = _cmbSym.getSelectedIndex();
                if (sel == 1) {
                    HashMap<String, ArrayList<String>> wl_map = GroupStore.getInstance().getGroups();
                    Iterator<String> itor = wl_map.keySet().iterator();
                    PickWatchlistDialog dlg = new PickWatchlistDialog(itor);
                    if (dlg.isCancelled()) return;
                    symbols = WatchListModel.mergeLists(dlg.getWatchlists());
                }
                else
                    symbols = DataUtil.getAllSymbolsInDb();
            }
        });
        ttl_pnl.add(_fldBeginDate);
        ttl_pnl.add(_fldEndDate);
        Calendar today = Calendar.getInstance();
        CalendarQuarter qtr = CalendarQuarter.findQuarter(today);
        if (qtr != null) _fldBeginDate.setDate(qtr.getStartDate().getTime());
        else {
            Calendar qtr_ago = Calendar.getInstance(); qtr_ago.add(Calendar.DAY_OF_YEAR, -90);
            _fldBeginDate.setDate(qtr_ago.getTime());
        }
        _fldEndDate.setDate(today.getTime());
        ttl_pnl.add(_btnRun); _btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //reject bad time frames, for now limit to about 2 years MARKET_QUOTE_LENGTH
                Calendar begin_cal = Calendar.getInstance(); begin_cal.setTime(_fldBeginDate.getDate());
                Calendar end_cal = Calendar.getInstance(); end_cal.setTime(_fldEndDate.getDate());
                if (begin_cal.compareTo(end_cal) >= 0) {
                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("gps_err_date1")); return; }
                ArrayList<FundQuote> sp500_quotes = FrameworkConstants.SP500_DATA.getQuote();
                _nBeginIndex = FundQuote.findIndexByDate(sp500_quotes, AppUtil.calendarToString(begin_cal));
                _nEndIndex = FundQuote.findIndexByDate(sp500_quotes, AppUtil.calendarToString(end_cal));
                if (_nBeginIndex >= FrameworkConstants.MARKET_QUOTE_LENGTH || _nEndIndex >= FrameworkConstants.MARKET_QUOTE_LENGTH) {
                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("gps_err_date2")); return; }
                new CategorizeTypeThread().start();
            }
        });
        ttl_pnl.add(_btnToXls);
        _btnToXls.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { generateXls(); }
        });
        add(ttl_pnl, "dock north");

        //center - splitter
        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitter.setDividerLocation(700);

        //right - sub-information for selected cell(symbols/performance/charts)
        splitter.setRightComponent(_pnlDetail = new PerfDetailPanel());

        //left center - table
        JPanel left_pnl = new JPanel(new MigLayout()); left_pnl.setOpaque(false);
        _tmAnalysis = new GapAnalysisTableModel();
        _tblAnalysis = WidgetUtil.createDynaTable(_tmAnalysis, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), false, new AnalysisRenderer());
        _tblAnalysis.setCellSelectionEnabled(true);
        left_pnl.add(new JScrollPane(_tblAnalysis), "dock center");

        //south - widgets adjusting factors affecting outcome
        JPanel south_pnl = new JPanel(new MigLayout("insets 0, flowy, wrap 4")); south_pnl.setOpaque(true);
        south_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
        CheckListener chk_lstnr = new CheckListener();
        for (GapFactor gf : GapFactor.values()) {
            JCheckBox chk = new JCheckBox(gf.toString());
            _chkFactor.add(chk);
            south_pnl.add(chk);
            chk.addActionListener(chk_lstnr);

            //temporarily disable un-implemented ones
            for (int i = 0; i < TBD_FACTORS.length; i++)
                if (gf.equals(TBD_FACTORS[i]))
                    chk.setEnabled(false);
        }
        left_pnl.add(south_pnl, "dock south");
        splitter.setLeftComponent(left_pnl);
        add(splitter, "dock center");

        //listener - detect both column and row selection
        _tblAnalysis.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) { selectCell(e); } });
        _tblAnalysis.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) { selectCell(e); } });

        //by default, pick master watch list to analyze, one factor at a time
        String wl_name = "+ Gap Master";
        symbols = GroupStore.getInstance().getMembers(wl_name);
        if (symbols == null) {
            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("gps_no_wl") + " " + wl_name); return; }
        ArrayList<GapInfo> gap_list = GapUtil.collectEarningGaps(symbols, CalendarQuarter.Q3_2014);
        _tmAnalysis.setGapList(gap_list); _chkFactor.get(0).doClick();
    }

    //----- private methods -----
    private void generateXls() {
        File output_file = new File(FrameworkConstants.DATA_FOLDER_EXPORT + File.separator + "Gap Analysis.xls");
        try {
            WritableWorkbook wb = Workbook.createWorkbook(output_file);
            WritableSheet ws = wb.createSheet(output_file.getName(), 0); _nWorkbookRow = 1;
            printTable(ws, GapFactor.PREGAP_PHASE); ws.setName(GapFactor.PREGAP_PHASE.toString());
            ws = wb.createSheet(output_file.getName(), 1);  _nWorkbookRow = 1;
            printTable(ws, GapFactor.AT_GAP_ROI); ws.setName(GapFactor.AT_GAP_ROI.toString());
            ws = wb.createSheet(output_file.getName(), 2);  _nWorkbookRow = 1;
            printTable(ws, GapFactor.AT_GAP_BB); ws.setName(GapFactor.AT_GAP_BB.toString());
            ws = wb.createSheet(output_file.getName(), 3);  _nWorkbookRow = 1;
            printTable(ws, GapFactor.POSTGAP_PHASE); ws.setName(GapFactor.POSTGAP_PHASE.toString());
            ws = wb.createSheet(output_file.getName(), 4);  _nWorkbookRow = 1;
            printTable(ws, GapFactor.POSTGAP_PULLBACK); ws.setName(GapFactor.POSTGAP_PULLBACK.toString());
            ws = wb.createSheet(output_file.getName(), 5);  _nWorkbookRow = 1;
            printTable(ws, GapFactor.POSTGAP_TYPE); ws.setName(GapFactor.POSTGAP_TYPE.toString());
            wb.write();
            wb.close();
        } catch (Exception e1) { e1.printStackTrace(); }//TODO logging

    }
    private void printTitle(WritableSheet ws, GapFactor gf) throws Exception {
        ws.addCell(new jxl.write.Label(0, _nWorkbookRow, "Factor:"));
        ws.addCell(new jxl.write.Label(1, _nWorkbookRow, gf.toString()));
    }
    private void printHeader1(WritableSheet ws) throws Exception {
        ws.addCell(new jxl.write.Label(1, _nWorkbookRow, "Factor"));
        ws.addCell(new jxl.write.Label(2, _nWorkbookRow, "2 Week ROI"));
        ws.addCell(new jxl.write.Label(3, _nWorkbookRow, "4 Week ROI"));
        ws.addCell(new jxl.write.Label(4, _nWorkbookRow, "8 Week ROI"));
        ws.addCell(new jxl.write.Label(5, _nWorkbookRow, "12 Week ROI"));
    }
    private void printCell(WritableSheet ws) throws Exception {
        for (int row = 0; row < _tmAnalysis.getRowCount(); row++) {
            int wb_col = 1;
            for (int col = 0; col < _tmAnalysis.getColumnCount(); col++) {
                Object obj = _tmAnalysis.getCell(row, col).getValue();
                if (obj instanceof String) {
                    ws.addCell(new jxl.write.Label(wb_col, _nWorkbookRow, (String)obj));
                }
                else {//double
                    String v = FrameworkConstants.PCT2_FORMAT.format((Double)obj);
                    ws.addCell(new jxl.write.Label(wb_col, _nWorkbookRow, v));
                }
                wb_col++;
            } _nWorkbookRow++;
        }
    }
    private void printTable(WritableSheet ws, GapFactor gf) throws Exception {
        printTitle(ws, gf); _nWorkbookRow += 2;
        JCheckBox chk = findBox(gf); chk.doClick();
        printHeader1(ws); _nWorkbookRow++; printCell(ws); _nWorkbookRow += 2;

        //print symbols related to each factor/duration
        //for each cell (eg. 2w / Recovery under Pre-Gap Phase), get all symbols/2wk ROIs
        HashMap<String, ArrayList<GapInfo>> factor_map = _tmAnalysis.getFactorMap();
        Set<String> keys = factor_map.keySet();
        for (String key : keys) {
            int wb_col = 2;
            ws.addCell(new jxl.write.Label(1, _nWorkbookRow, key));
            ArrayList<GapInfo> gap_infos = factor_map.get(key);
            for (GapInfo gi : gap_infos) {//print a row of symbols
                ws.addCell(new jxl.write.Label(wb_col++, _nWorkbookRow, gi.getQuote().getSymbol()));
            }
            _nWorkbookRow++;

            //print 2w, 4w, 8w, 12w ROI for each symbol

            ws.addCell(new jxl.write.Label(1, _nWorkbookRow, "2 Week"));
            wb_col = 2; for (GapInfo gi : gap_infos)
                ws.addCell(new jxl.write.Label(wb_col++, _nWorkbookRow, FrameworkConstants.PCT2_FORMAT.format(gi.getRoi2wk())));
            _nWorkbookRow++;
            ws.addCell(new jxl.write.Label(1, _nWorkbookRow, "4 Week"));
            wb_col = 2; for (GapInfo gi : gap_infos)
                ws.addCell(new jxl.write.Label(wb_col++, _nWorkbookRow, FrameworkConstants.PCT2_FORMAT.format(gi.getRoi4wk())));
            _nWorkbookRow++;
            ws.addCell(new jxl.write.Label(1, _nWorkbookRow, "8 Week"));
            wb_col = 2; for (GapInfo gi : gap_infos)
                ws.addCell(new jxl.write.Label(wb_col++, _nWorkbookRow, FrameworkConstants.PCT2_FORMAT.format(gi.getRoi8wk())));
            _nWorkbookRow++;
            ws.addCell(new jxl.write.Label(1, _nWorkbookRow, "12 Week"));
            wb_col = 2; for (GapInfo gi : gap_infos)
                ws.addCell(new jxl.write.Label(wb_col++, _nWorkbookRow, FrameworkConstants.PCT2_FORMAT.format(gi.getRoi12wk())));
            _nWorkbookRow += 2;
        }
    }
    private void unCheckOthers(JCheckBox checking) {
        for (JCheckBox chk : _chkFactor) {
            if (chk.equals(checking)) continue;
            chk.setSelected(false);
        }
    }
    private void selectCell(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int sel_col = _tblAnalysis.getSelectedColumn();
        int sel_row = _tblAnalysis.getSelectedRow(); if (sel_row < 0) return;
        sel_row = _tblAnalysis.convertRowIndexToModel(sel_row);
        HashMap<String, ArrayList<GapInfo>> factor_map = _tmAnalysis.getFactorMap();
        String key = (String) _tmAnalysis.getCell(sel_row, GapAnalysisTableModel.COLUMN_FACTOR).getValue();
        _pnlDetail.populate(factor_map.get(key), sel_col);
    }
    private JCheckBox findBox(GapFactor gf) {
        for (JCheckBox chk : _chkFactor) {
            if (gf.equals(GapFactor.toEnumConstant(chk.getText())))
                return chk;
        }
        return null;
    }

    //----- inner classes -----
    private class CheckListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JCheckBox chk = (JCheckBox)e.getSource();
            String label = chk.getText();//is also descr of enum
            GapFactor gf = GapFactor.toEnumConstant(label);
            switch (gf) {
                case PREGAP_PHASE:
                case POSTGAP_PHASE:
                case AT_GAP_ROI: case AT_GAP_BB:
                case POSTGAP_TYPE:
                case POSTGAP_PULLBACK:
                    _tmAnalysis.populate(gf);
                    TableUtil.updateRowHeights(_tblAnalysis);
                    break;
                default: _tmAnalysis.clear();
            }
            unCheckOthers(chk);
        }
    }
    private class AnalysisRenderer extends DynaTableCellRenderer {//show time
        private AnalysisRenderer() { super(_tmAnalysis); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setFont(FrameworkConstants.SMALL_FONT); lbl.setToolTipText("");
            if (column == GapAnalysisTableModel.COLUMN_FACTOR) return lbl;

            //other columns special rendering CellInfo object
            GapAnalysisTableModel.CellInfo ci = (GapAnalysisTableModel.CellInfo)value;
            float avg = ci.getAverageRoi(); if (avg < 0) lbl.setForeground(Color.magenta);
            StringBuilder buf = new StringBuilder("<html>");
            int pos_cnt = ci.getPositiveCount(), neg_cnt = ci.getNeagtiveCount();
            buf.append("[").append(pos_cnt).append(" : ")
               .append(neg_cnt).append("]<br>")
               .append(FrameworkConstants.PCT2_FORMAT.format(avg));
            lbl.setText(buf.toString());
            if (ci.isHighestOdds())
                lbl.setBackground(Color.yellow);
            return lbl;
        }
    }
    private class CategorizeTypeThread extends Thread {
        private CategorizeTypeThread() {
            pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
            pb.setVisible(true);
        }
        public void run() {
            final ArrayList<GapInfo> gap_list = GapUtil.collectEarningGaps(symbols, _nBeginIndex, _nEndIndex);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    pb.setVisible(false);
                    _tmAnalysis.setGapList(gap_list);
                }
            });
        }
        private ProgressBar pb;
    }

    //----- variables -----
    private JButton _btnRun = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("gps_run"), FrameworkIcon.RUN);
    private JButton _btnToXls = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("gps_xls"), FrameworkIcon.EXPORT_LIST);
    private JComboBox<String> _cmbSym = new JComboBox<>(new String[]
        { "+ Gap Master", ApolloConstants.APOLLO_BUNDLE.getString("gps_wl"),
          ApolloConstants.APOLLO_BUNDLE.getString("gps_all"), });
    private JXDatePicker _fldBeginDate = new JXDatePicker();
    private ArrayList<String> symbols; private int _nBeginIndex, _nEndIndex;
    private JXDatePicker _fldEndDate = new JXDatePicker();
    private ArrayList<JCheckBox> _chkFactor = new ArrayList<>();
    private PerfDetailPanel _pnlDetail;
    private JTable _tblAnalysis;
    private GapAnalysisTableModel _tmAnalysis;
    private int _nWorkbookRow;

    //----- literals -----
    private static final GapFactor[] TBD_FACTORS = {
        GapFactor.PREGAP_CANDLE, GapFactor.PREGAP_RATING, GapFactor.PREGAP_PATTERN,
        GapFactor.PREGAP_MKT_CONDITION, GapFactor.PREGAP_SECTOR_CONDITION, GapFactor.PREGAP_IG_CONDITION,
        GapFactor.PREGAP_VSQ, GapFactor.AT_GAP_CANDLE, GapFactor.AT_GAP_CLOUD,
        GapFactor.AT_GAP_ICHIMOKU, GapFactor.AT_GAP_LAGGING
    };
}