package org.marketsuite.framework.strategy.base;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Stat;
import org.marketsuite.framework.resource.FrameworkConstants;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Stat;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * General purpose view to present trade stats with form on left and table on right.
 */
public class StatPanel extends JPanel {
    public StatPanel() {
        setLayout(new BorderLayout()); setOpaque(false);
        SkinPanel ttl = WidgetUtil.createTitleStrip(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_lbl_1")));
        add(ttl, BorderLayout.NORTH);
        WidgetUtil.attachToolTip(ttl, FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_tip_06"),
            SwingConstants.CENTER, SwingConstants.BOTTOM);

        //right side - form of individual stat, simple label and value
        FormLayout layout = new FormLayout(
            "2dlu, right:pref, 2dlu, left:pref, 5dlu,  right:pref, 2dlu, left:pref, 5dlu",//columns
            "2dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int col = 2, row = 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_01"), cc.xy(col, row));
        builder.add(_txtTotalReturn, cc.xy(col + 2, row)); _txtTotalReturn.setEditable(false);
        WidgetUtil.attachToolTip(_txtTotalReturn, FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_tip_01"),
                SwingConstants.RIGHT, SwingConstants.TOP);
        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_02"), cc.xy(col, row));
        builder.add(_txtAnnualReturn, cc.xy(col + 2, row)); _txtAnnualReturn.setEditable(false);
        WidgetUtil.attachToolTip(_txtAnnualReturn, FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_tip_02"),
                SwingConstants.RIGHT, SwingConstants.TOP);
        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_10"), cc.xy(col, row));
        builder.add(_txtNumTrade, cc.xy(col+2, row)); _txtNumTrade.setEditable(false);
        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_11"), cc.xy(col, row));
        builder.add(_txtWinRatio, cc.xy(col+2, row)); _txtWinRatio.setEditable(false);
        WidgetUtil.attachToolTip(_txtWinRatio, FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_tip_03"),
                SwingConstants.RIGHT, SwingConstants.TOP);

        row = 2; col = 6;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_05"), cc.xy(col, row));
        builder.add(_txtNetProfit, cc.xy(col + 2, row)); _txtNetProfit.setEditable(false);
        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_06"), cc.xy(col, row));
        builder.add(_txtNetLoss, cc.xy(col + 2, row)); _txtNetLoss.setEditable(false);
        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_12"), cc.xy(col, row));
        builder.add(_txtProfitFactor, cc.xy(col + 2, row)); _txtProfitFactor.setEditable(false);
        WidgetUtil.attachToolTip(_txtProfitFactor, FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_tip_04"),
            SwingConstants.RIGHT, SwingConstants.TOP);
        row += 2;
        builder.addLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_07"), cc.xy(col, row));
        builder.add(_txtTimeInMkt, cc.xy(col+2, row)); _txtTimeInMkt.setEditable(false);
        WidgetUtil.attachToolTip(_txtTimeInMkt, FrameworkConstants.FRAMEWORK_BUNDLE.getString("stat_tip_05"),
                SwingConstants.RIGHT, SwingConstants.TOP);
        JPanel form_pnl = builder.getPanel(); form_pnl.setOpaque(false);
        WidgetUtil.setMaxMinPrefSize(form_pnl, 350, 20);//control width
        form_pnl.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        add(form_pnl, BorderLayout.WEST);

        //center - table of max, min, avg, std dev
        _tblStat = WidgetUtil.createDynaTable(_TableModel = new StatTableModel(), -1,
            new HeadingRenderer(), false, new DynaTableCellRenderer(_TableModel)); _tblStat.setOpaque(false);
        JScrollPane scr = new JScrollPane(_tblStat); scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
        _TableModel.populate();
    }

    /**
     * Update stats using supplied SimReport object.
     * @param draw_down_only = true if only show draw down (like buy and hold)
     * @param report a SimReport object
     */
    public void updateStat(SimReport report, boolean draw_down_only) {
        //update fields
        clearFields();
        _txtTotalReturn.setText(FrameworkConstants.ROI_FORMAT.format(report.getTotalReturn()));
        _txtAnnualReturn.setText(FrameworkConstants.ROI_FORMAT.format(report.getAverageReturn()));
        float value = report.getTotalGain();
        if (value >= 0)
            _txtNetProfit.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
        value = report.getTotalLoss();
        if (value != 0)
            _txtNetLoss.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
        value = report.getWinRatio();
        if (value > 0)
            _txtWinRatio.setText(FrameworkConstants.ROI_FORMAT.format(value));
        value = report.getProfitFactor();
        if (value >= 0)
            _txtProfitFactor.setText(FrameworkConstants.PRICE_FORMAT.format(value));
        else
            _txtProfitFactor.setText("");
        value = report.getNumberTrades();
        if (value > 0)
            _txtNumTrade.setText(FrameworkConstants.SMALL_INT_FORMAT.format(value));
        value = report.getTimeInMarket();
        if (value > 0)
            _txtTimeInMkt.setText(FrameworkConstants.ROI_FORMAT.format(value));
        //update table
        if (!draw_down_only) {
            _TableModel.updateStats(COLUMN_GAIN_PERCENT, report.getGain());
            _TableModel.updateStats(COLUMN_GAIN_AMOUNT, report.getGain());
            _TableModel.updateStats(COLUMN_LOSS_PERCENT, report.getLoss());
            _TableModel.updateStats(COLUMN_LOSS_AMOUNT, report.getLoss());
        }
        _TableModel.updateStats(COLUMN_DRAW_DOWN_PERCENT, report.getDrawDown());
        _TableModel.updateStats(COLUMN_DRAW_DOWN_AMOUNT, report.getDrawDown());
    }

    public void clearFields() {
        _TableModel.populate();
        _txtTotalReturn.setText("");
        _txtAnnualReturn.setText("");
        _txtNetProfit.setText("");
        _txtNetLoss.setText("");
        _txtNumTrade.setText("");
        _txtWinRatio.setText("");
        _txtProfitFactor.setText("");
        _txtTimeInMkt.setText("");
    }

    //-----inner classes-----
    private class StatTableModel extends DynaTableModel {
        private StatTableModel() {
            remodel(DynaTableModel.generateSchema(TABLE_SCHEMA));
        }

        public void populate() {
            _lstRows.clear();
            for (int row = 0; row < 4; row++) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                switch(row) {
                    case 0:
                        cells[COLUMN_STAT_TYPE] = new SimpleCell(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_stat_1"));
                        break;

                    case 1:
                        cells[COLUMN_STAT_TYPE] = new SimpleCell(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_stat_2"));
                        break;

                    case 2:
                        cells[COLUMN_STAT_TYPE] = new SimpleCell(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_stat_3"));
                        break;

                    case 3:
                        cells[COLUMN_STAT_TYPE] = new SimpleCell(FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_stat_4"));
                        break;
                }
                cells[COLUMN_GAIN_PERCENT] = new SimpleCell("");
                cells[COLUMN_GAIN_AMOUNT] = new SimpleCell("");
                cells[COLUMN_LOSS_PERCENT] = new SimpleCell("");
                cells[COLUMN_LOSS_AMOUNT] = new SimpleCell("");
                cells[COLUMN_DRAW_DOWN_PERCENT] = new SimpleCell("");
                cells[COLUMN_DRAW_DOWN_AMOUNT] = new SimpleCell("");
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }

        public boolean isCellEditable(int i, int i1) {
            return false;
        }

        void updateStats(int col, Stat stat) {
            switch(col) {
                case COLUMN_GAIN_PERCENT:
                case COLUMN_LOSS_PERCENT:
                    getCell(ROW_AVERAGE, col).setValue(FrameworkConstants.ROI_FORMAT.format(stat.getAvgPct()));
                    getCell(ROW_MEDIAN, col).setValue(FrameworkConstants.ROI_FORMAT.format(stat.getMedianPct()));
                    getCell(ROW_MAX, col).setValue(FrameworkConstants.ROI_FORMAT.format(stat.getMaxPct()));
                    getCell(ROW_MIN, col).setValue(FrameworkConstants.ROI_FORMAT.format(stat.getMinPct()));
                    break;

                case COLUMN_DRAW_DOWN_PERCENT://show draw downs in negative, internal is already negative
                    getCell(ROW_AVERAGE, col).setValue(FrameworkConstants.ROI_FORMAT.format(stat.getAvgPct()));
                    getCell(ROW_MEDIAN, col).setValue(FrameworkConstants.ROI_FORMAT.format(stat.getMedianPct()));
                    getCell(ROW_MAX, col).setValue(FrameworkConstants.ROI_FORMAT.format(stat.getMaxPct()));
                    getCell(ROW_MIN, col).setValue(FrameworkConstants.ROI_FORMAT.format(stat.getMinPct()));
                    break;

                case COLUMN_GAIN_AMOUNT:
                case COLUMN_LOSS_AMOUNT:
                    getCell(ROW_AVERAGE, col).setValue(FrameworkConstants.DOLLAR_FORMAT.format(stat.getAvgAmount()));
                    getCell(ROW_MEDIAN, col).setValue(FrameworkConstants.DOLLAR_FORMAT.format(stat.getMedianAmount()));
                    getCell(ROW_MAX, col).setValue(FrameworkConstants.DOLLAR_FORMAT.format(stat.getMaxAmount()));
                    getCell(ROW_MIN, col).setValue(FrameworkConstants.DOLLAR_FORMAT.format(stat.getMinAmount()));
                    break;

                case COLUMN_DRAW_DOWN_AMOUNT://show draw downs in negative
                    getCell(ROW_AVERAGE, col).setValue(FrameworkConstants.DOLLAR_FORMAT.format(stat.getAvgAmount()));
                    getCell(ROW_MEDIAN, col).setValue(FrameworkConstants.DOLLAR_FORMAT.format(stat.getMedianAmount()));
                    getCell(ROW_MAX, col).setValue(FrameworkConstants.DOLLAR_FORMAT.format(stat.getMaxAmount()));
                    getCell(ROW_MIN, col).setValue(FrameworkConstants.DOLLAR_FORMAT.format(stat.getMinAmount()));
                    break;
            }
            fireTableDataChanged();
        }

//        void clearGraph() {
//            _lstRows.clearGraph();
//            fireTableDataChanged();
//        }
    }

    //-----instance variables-----
    private JTextField _txtTotalReturn = new JTextField(FIELD_WIDTH +2);
    private JTextField _txtAnnualReturn = new JTextField(FIELD_WIDTH);
    private JTextField _txtNetProfit = new JTextField(FIELD_WIDTH + 2);
    private JTextField _txtNetLoss = new JTextField(FIELD_WIDTH + 2);
    private JTextField _txtNumTrade = new JTextField(FIELD_WIDTH);
    private JTextField _txtWinRatio = new JTextField(FIELD_WIDTH);
    private JTextField _txtProfitFactor = new JTextField(FIELD_WIDTH);
    private JTextField _txtTimeInMkt = new JTextField(FIELD_WIDTH);
    private JTable _tblStat;
    private StatTableModel _TableModel;

    //-----literals-----
    private static final int FIELD_WIDTH = 6;
    private static final int ROW_AVERAGE = 0;
    private static final int ROW_MEDIAN = 1;
    private static final int ROW_MAX = 2;
    private static final int ROW_MIN = 3;
    static final int COLUMN_STAT_TYPE = 0;
    static final int COLUMN_GAIN_PERCENT = 1;
    static final int COLUMN_GAIN_AMOUNT = 2;
    static final int COLUMN_LOSS_PERCENT = 3;
    static final int COLUMN_LOSS_AMOUNT = 4;
    static final int COLUMN_DRAW_DOWN_PERCENT = 5;
    static final int COLUMN_DRAW_DOWN_AMOUNT = 6;
    private static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { "",                                          ColumnTypeEnum.TYPE_STRING,  5, 30, null, null, null },//stat type
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_hdr_1"), ColumnTypeEnum.TYPE_STRING,  2, 30, null, null, null },//gain percent
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_hdr_2"), ColumnTypeEnum.TYPE_STRING,  2, 40, null, null, null },//gain amount
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_hdr_3"), ColumnTypeEnum.TYPE_STRING,  2, 30, null, null, null },//loss percent
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_hdr_4"), ColumnTypeEnum.TYPE_STRING,  2, 40, null, null, null },//loss amount
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_hdr_5"), ColumnTypeEnum.TYPE_STRING,  2, 30, null, null, null },//draw down percent
        { FrameworkConstants.FRAMEWORK_BUNDLE.getString("sum_hdr_6"), ColumnTypeEnum.TYPE_STRING,  2, 40, null, null, null },//draw down amount
    };
}
