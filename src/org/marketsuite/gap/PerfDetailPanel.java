package org.marketsuite.gap;

import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.data.GapInfo;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;

class PerfDetailPanel extends JPanel {
    PerfDetailPanel() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        _tmDetail = new DetailTableModel();
        _tblDetail = WidgetUtil.createDynaTable(_tmDetail, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), false, new DetailRenderer());
        _tblDetail.setAutoCreateRowSorter(true);
        add(new JScrollPane(_tblDetail), "dock center");
        add(_lblInfo, "dock south");
        _tblDetail.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int sel = _tblDetail.getSelectedRow(); if (sel < 0) return;
                sel = _tblDetail.convertRowIndexToModel(sel);
                String sym = (String)_tmDetail.getCell(sel, COLUMN_SYMBOL).getValue();
                Props.SymbolSelection.setValue(null, sym);//refresh charts
            }
        });
    }

    //----- protected methods -----
    void populate(ArrayList<GapInfo> gap_infos, int column) {
        _tmDetail.populate(gap_infos, column);
        _lblInfo.setText("# " + _tmDetail.getRowCount() + " / " + _tmDetail.getNoRoiCount());
    }

    //----- inner classes -----
    private class DetailTableModel extends DynaTableModel {
        private DetailTableModel() { super(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public boolean isCellEditable(int row, int column) { return false; }
        public void populate() {}
        private void populate(ArrayList<GapInfo> gap_infos, int column) {
            _GapInfos = gap_infos; _nCurrentColumn = column;
            _lstRows.clear();
            for (GapInfo gi : gap_infos) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_SYMBOL] = new SimpleCell(gi.getQuote().getSymbol());
                float roi = 0;
                switch (column) {
                    case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_1WK:
                        roi = gi.getRoi1wk();
                        break;

                    case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_2WK:
                        roi = gi.getRoi2wk();
                        break;

                    case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_4WK:
                        roi = gi.getRoi4wk();
                        break;

                    case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_6WK:
                        roi = gi.getRoi6wk();
                        break;

                    case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_8WK:
                        roi = gi.getRoi8wk();
                        break;

                    case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_12WK:
                        roi = gi.getRoi12wk();
                        break;
                }
                cells[COLUMN_ROI] = new SimpleCell(new Double(roi));
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
        int getNoRoiCount() {
            int ret = 0;
            for (int row = 0; row < getRowCount(); row++) {
                double v = (Double)getCell(row, COLUMN_ROI).getValue();
                if (v == 0) ret++;
            }
            return ret;
        }
    }
    private class DetailRenderer extends DynaTableCellRenderer {
        private DetailRenderer() { super(_tmDetail); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == COLUMN_ROI) {
                Double roi = (Double) value; lbl.setText(FrameworkConstants.PCT2_FORMAT.format(roi));
                row = _tblDetail.convertRowIndexToModel(row);
                String sym = (String)_tmDetail.getCell(row, COLUMN_SYMBOL).getValue();
                for (GapInfo gi : _GapInfos) {
                    if (gi.getQuote().getSymbol().equals(sym)) {
                        if (roi == 0) {//conditionally hide 0
                            switch (_nCurrentColumn) {
                                case GapAnalysisTableModel.COLUMN_FACTOR:
                                    lbl.setText(""); break;
                                case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_1WK:
                                    if (gi.getRoi2wk() == 0) lbl.setText(""); break;
                                case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_2WK:
                                    if (gi.getRoi4wk() == 0) lbl.setText(""); break;
                                case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_4WK:
                                    if (gi.getRoi6wk() == 0) lbl.setText(""); break;
                                case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_6WK:
                                    if (gi.getRoi8wk() == 0) lbl.setText(""); break;
                                case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_8WK:
                                    if (gi.getRoi12wk() == 0) lbl.setText(""); break;
                                case GapAnalysisTableModel.COLUMN_POSTGAP_PERF_12WK:
                                    lbl.setText(""); break;
                            }
                        }
                    }
                }
            }
            return lbl;
        }
    }

    //----- variables -----
    private DetailTableModel _tmDetail; private JTable _tblDetail;
    private JLabel _lblInfo = new JLabel();//symbol count + no ROI count
    private ArrayList<GapInfo> _GapInfos; private int _nCurrentColumn;

    //----- literals -----
    static final int COLUMN_SYMBOL = 0;
    static final int COLUMN_ROI = 1;
    static final Object[][] TABLE_SCHEMA = {
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_sym"),  ColumnTypeEnum.TYPE_STRING, -1, 50, null, null, null},
        {ApolloConstants.APOLLO_BUNDLE.getString("gps_roi"),  ColumnTypeEnum.TYPE_DOUBLE, -1, 50, null, null, null},
    };
}
