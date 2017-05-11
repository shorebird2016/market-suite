package org.marketsuite.gap;

import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;
import jxl.Workbook;
import jxl.write.*;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.marektview.ranking.RankElement;
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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A reporting tool to order the various symbols from specified watch list according to their
 * possibility of success based on C R T method.
 */
public class GapStudyPanel extends JPanel {
    public GapStudyPanel() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel north_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
            new MigLayout("insets 0", "5[][]10[]20[]push[][]5", "3[]3")); north_pnl.setOpaque(false);
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("tn_02")));
        north_pnl.add(_cmbGrpSelector = new JComboBox<>(_cmlGrpSelector));
        _cmbGrpSelector.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;

                //load next watch list
                _sCurWatchlist = (String) _cmbGrpSelector.getSelectedItem();
                WatchListModel model = new WatchListModel(_sCurWatchlist, false);
                _tmStudy.populate(model);
                _lblCount.setText("#" + _tmStudy.getRowCount());
                updateAveragePerf();
                updateRowHeights();
            }
        });
        north_pnl.add(_lblCount);
        north_pnl.add(_btnToXls); _btnToXls.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File output_file = new File(FrameworkConstants.DATA_FOLDER_EXPORT + File.separator + "Gap Return.xls");
                try {
                    WritableWorkbook wb = Workbook.createWorkbook(output_file);
                    WritableSheet ws = wb.createSheet(output_file.getName(), 0);

                    //header
                    ws.addCell(new jxl.write.Label(0, 0, "Symbol"));
                    ws.addCell(new jxl.write.Label(1, 0, "Pre-Gap Close"));
                    ws.addCell(new jxl.write.Label(2, 0, "Gap Date"));
                    ws.addCell(new jxl.write.Label(3, 0, "Gap Close"));
                    ws.addCell(new jxl.write.Label(4, 0, "File Type"));
                    ws.addCell(new jxl.write.Label(5, 0, "Gap Percent"));
                    ws.addCell(new jxl.write.Label(6, 0, "4 Week ROI"));
                    ws.addCell(new jxl.write.Label(7, 0, "8 Week ROI"));
                    ws.addCell(new jxl.write.Label(8, 0, "Normalized"));
                    ws.addCell(new jxl.write.Label(9, 0, "Today ROI"));

                    //values from table
                    String wl = (String)_cmbGrpSelector.getSelectedItem();
                    String type = wl.substring(7, wl.length());
                    for (int row = 1; row <= _tmStudy.getRowCount(); row++) {
                        String sym = (String)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_SYMBOL).getValue();
                        ws.addCell(new jxl.write.Label(0, row, sym));
                        double close = (Double)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_PREGAP_RATING).getValue();//TODO temporary use this to store pregap close
                        ws.addCell(new jxl.write.Label(1, row, FrameworkConstants.FORMAT_NUMBERS.format(close)));
                        String gdate = (String)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_ATGAP_DATE).getValue();
                        ws.addCell(new jxl.write.Label(2, row, gdate));
                        close = (Double)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_ATGAP_PRICE).getValue();
                        ws.addCell(new jxl.write.Label(3, row, FrameworkConstants.FORMAT_NUMBERS.format(close)));
                        ws.addCell(new jxl.write.Label(4, row, type));
                        double pct = (Double)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_ATGAP_PCT).getValue();
                        ws.addCell(new jxl.write.Label(5, row, FrameworkConstants.PCT2_FORMAT.format(pct)));
                        pct = (Double)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_POSTGAP_PERF_4WK).getValue();
                        ws.addCell(new jxl.write.Label(6, row, FrameworkConstants.PCT2_FORMAT.format(pct)));
                        pct = (Double)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_POSTGAP_PERF_8WK).getValue();
                        ws.addCell(new jxl.write.Label(7, row, FrameworkConstants.PCT2_FORMAT.format(pct)));
                        pct = (Double)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_POSTGAP_PERF_CURRENT_NORMALIZED).getValue();
                        ws.addCell(new jxl.write.Label(8, row, FrameworkConstants.PRICE_FORMAT2.format(pct)));
                        pct = (Double)_tmStudy.getCell(row - 1, GapStudyTableModel.COLUMN_ROI_TO_DATE).getValue();
                        ws.addCell(new jxl.write.Label(9, row, FrameworkConstants.PCT_FORMAT.format(pct)));
                    }
                    wb.write();
                    wb.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        north_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("gps_avgperf")));
        north_pnl.add(_lblAvgPerf);
        add(north_pnl, "dock north");

        //center - table
        _tmStudy = new GapStudyTableModel();
        _tblStudy = new JTable(_tmStudy) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                //traverse all rows and draw line at group boundaries
//                int y = -1; double prev_pct = 0;
//                int rows = getRowCount() - 1;
//                for (int row = 0; row < rows; row++) {
//                    int col;
//                    switch ((MovingAverageType)_cmbClusterMa.getSelectedItem()) {
//                        case SMA_20: col = COLUMN_ATGAP_PCT; break;
//                        case SMA_50: default: col = COLUMN_ATGAP_CANDLE; break;
//                        case SMA_200: col = COLUMN_ATGAP_PHASE; break;
//                    }
//                    double pct = Math.abs((Double) _tmStudy.getCell(row, col).getValue());
//                    if (pct >= 1 && prev_pct < 1 && _bShowBullish)
//                        g.drawLine(0, y, getSize().width, y);
//                    if (pct >= 2 && prev_pct < 2 && _bShowBullish)
//                        g.drawLine(0, y, getSize().width, y);
//                    y += getRowHeight(row);
//                    prev_pct = pct;
//                }
            }
        };
        _tblStudy.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblStudy.setAutoCreateRowSorter(true);
        WidgetUtil.initDynaTable(_tblStudy, _tmStudy, ListSelectionModel.SINGLE_INTERVAL_SELECTION,
                new SortHeaderRenderer(), false, new StudyRenderer(_tmStudy));
        _tblStudy.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int sel = _tblStudy.getSelectedRow(); if (sel < 0) return;
                sel = _tblStudy.convertRowIndexToModel(sel);
                Object val = _tmStudy.getCell(sel, GapStudyTableModel.COLUMN_SYMBOL).getValue();
                if (val.equals("")) return;
                Props.SymbolSelection.setValue(null, val);
            }
        });
        add(new JScrollPane(_tblStudy), "dock center");

        //initialize
        initGroupSelector();
        _sCurWatchlist = (String)_cmbGrpSelector.getSelectedItem();
        WatchListModel model = new WatchListModel(_sCurWatchlist, false);
        _tmStudy.populate(model); _lblCount.setText("#" + _tmStudy.getRowCount());
        updateAveragePerf(); updateRowHeights();
    }

    //----- private methods -----
    private void updateRowHeights() {
        for (int row = 0; row < _tblStudy.getRowCount(); row++) {
            int rowHeight = _tblStudy.getRowHeight();
            for (int column = 0; column < _tblStudy.getColumnCount(); column++) {
                Component comp = _tblStudy.prepareRenderer(_tblStudy.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            _tblStudy.setRowHeight(row, rowHeight);
        }
    }
    //format a string from CellInfo and percentage to nicely presented as HTML
    private String cellToHtml(ArrayList<RankElement> recent_rank, HashMap<String, ArrayList<Integer>> hist_map, float percentage) {
        if (recent_rank == null || hist_map == null)
            return "";
        StringBuilder buf = new StringBuilder("<html>");
        int max = (int)(recent_rank.size() * percentage);
        if (max < 3) max = 3;//minimum 3 symbols
        int count = 0;
        for (RankElement re : recent_rank) {
            //each row has symbol, percent
            buf.append("<b>").append(re.symbol).append("</b> :&nbsp;").
                append(FrameworkConstants.PCT_FORMAT.format(re.pct));

            //followed by historical ranks in parenthesis from earlier to recent
//TODO remove hard number 3
            ArrayList<Integer> hr = hist_map.get(re.symbol);
            int idx = 3;
            buf.append("&nbsp;&nbsp;(");
            do {
                buf.append(hr.get(idx));
                if (idx > 0)//skip last comma
                    buf.append(", ");
                idx--;
            }while (idx >= 0);
            buf.append(")");
            buf.append("<br>");//for next symbol
            count++;
            if (count >= max)
                break;
        }
        return buf.toString();
    }
    private void initGroupSelector() { //prefill with all the watch list groups
        ArrayList<String> groups = GroupStore.getInstance().getGroupNames();
        _cmlGrpSelector.removeAllElements();
        for (int idx = 0; idx < groups.size(); idx++) {
            String grp_name = groups.get(idx);
            if (grp_name.startsWith("--EG--"))
                _cmlGrpSelector.addElement(grp_name);
        }
    }
    private void updateAveragePerf() {
        _lblAvgPerf.setText(_tmStudy.getAvgPerf());
    }

    //----- inner classes -----
    private class StudyRenderer extends DynaTableCellRenderer {
        private StudyRenderer(DynaTableModel model) { super(model); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setFont(FrameworkConstants.SMALL_FONT); lbl.setToolTipText("");
            int model_row = table.convertRowIndexToModel(row);
            String symbol = (String) _tmStudy.getCell(model_row, GapStudyTableModel.COLUMN_SYMBOL).getValue();
            if (symbol.equals("")) {//TODO may not need this.....
                lbl.setText("");
                lbl.setToolTipText("");
                return lbl;
            }
            if (column >= GapStudyTableModel.COLUMN_PREGAP_CANDLE && column <= GapStudyTableModel.COLUMN_PREGAP_VSQ)
                lbl.setBackground(new Color(250, 41, 23, 54));
            else if (column >= GapStudyTableModel.COLUMN_ATGAP_DATE && column <= GapStudyTableModel.COLUMN_ATGAP_SECTOR_CONDITION)
                lbl.setBackground(new Color(33, 107, 244, 54));
            else if (column >= GapStudyTableModel.COLUMN_POSTGAP_PERF_1WK)
                lbl.setBackground(new Color(195, 244, 11, 54));
            if (isSelected)
                lbl.setBackground(Constants.CELL_HIGHLIGHT_BACKGROUND);
            switch (column) {
                case GapStudyTableModel.COLUMN_ATGAP_PCT:
                case GapStudyTableModel.COLUMN_POSTGAP_PERF_1WK:
                case GapStudyTableModel.COLUMN_POSTGAP_PERF_4WK:
                case GapStudyTableModel.COLUMN_POSTGAP_PERF_8WK:
                case GapStudyTableModel.COLUMN_ROI_TO_DATE:
                    double v = (Double)value;
                    if (v != 0)
                        lbl.setText(FrameworkConstants.PCT2_FORMAT.format((Double)value));
                    else
                        lbl.setText("");
                    break;

                case GapStudyTableModel.COLUMN_ATGAP_PRICE:
                    lbl.setText(FrameworkConstants.DOLLAR_FORMAT.format((Double)value)); break;
                case GapStudyTableModel.COLUMN_POSTGAP_PERF_CURRENT_NORMALIZED:
                    lbl.setText(FrameworkConstants.PRICE_FORMAT2.format((Double)value)); break;
                case GapStudyTableModel.COLUMN_PREGAP_CANDLE:
                    //add icons based on each signal to panel here as renderer
                    JPanel pnl = new JPanel(new MigLayout("insets 0", "[]")); pnl.setOpaque(true);
                    ArrayList<CandleSignal> signals = (ArrayList<CandleSignal>)value;//from cell
                    if (signals == null) break;
                    StringBuilder buf = new StringBuilder("");
                    for (CandleSignal cs : signals) {
//                        if (!_bShowDoji && cs.equals(CandleSignal.DojiTop)) continue;
//                        if (_bShowBullCandle && CandleSignal.isBearish(cs)) continue;
//                        if (_bShowBearCandle && CandleSignal.isBullish(cs)) continue;
                        pnl.add(new JLabel(cs.getCandleIcon()));
                        buf.append(cs.toString()).append(", ");
                    }
                    pnl.setToolTipText(CoreUtil.wrapText(buf.toString(), 20));
                    return pnl;
            }

//                case COLUMN_COMP_RS_RATING://use stored IbdRating object to decide hook-up or hook-down
//                    lbl.setText("");
//                    ArrayList<IbdRating> ratings = (ArrayList<IbdRating>)value;
//                    if (ratings.size() <= 2) break;
//                    if (ratings.size() == 0) break;
//                    if (IbdRating.doCompRsHookup(ratings))
//                        lbl.setIcon(FrameworkIcon.TREND_UP_1);
//                    else if (IbdRating.doCompRsHookdown(ratings))
//                        lbl.setIcon(FrameworkIcon.TREND_DOWN_1);
//                    break;
//
//                case COLUMN_ACC_DIS_RATING://color text w red if rating less than B
//                    ratings = (ArrayList<IbdRating>) _tmStudy.getCell(row, COLUMN_COMP_RS_RATING).getValue();
//                    if (ratings == null || ratings.size() == 0) break;
//                    buf = new StringBuilder(" "); boolean warn = false;
//                    String grp_rating = ratings.get(0).getGroupRating();
//                    if (grp_rating.compareTo("D") >= 0)
//                        warn = true;
//                    buf.append(grp_rating); if (grp_rating.length() == 1) buf.append(" ");//extra blank
//                    String acc_dis = ratings.get(0).getAccDis();
//                    if (acc_dis.compareTo("D") >= 0)
//                        warn = true;
//                    buf.append("   ").append(acc_dis);
//                    lbl.setFont(FrameworkConstants.FONT_STANDARD);
//                    lbl.setText(buf.toString());
//                    if (warn) lbl.setForeground(Color.red);
//                        else lbl.setForeground(new Color(15, 130, 40, 205));
//                    lbl.setToolTipText(ApolloConstants.APOLLO_BUNDLE.getString("plnrpt_rating") + " ==> " + buf.toString());
//                    break;
//
            return lbl;
        }
    }

    //----- variables -----
    private JComboBox<String> _cmbGrpSelector;//by default select first item
    private DefaultComboBoxModel<String> _cmlGrpSelector = new DefaultComboBoxModel<>();
    private JLabel _lblCount = new JLabel("#");
    private JButton _btnToXls = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("gps_xls"), FrameworkIcon.EXPORT_LIST);
    private JLabel _lblAvgPerf = new JLabel();
    private JTable _tblStudy;
    private GapStudyTableModel _tmStudy;
    private String _sCurWatchlist;
}