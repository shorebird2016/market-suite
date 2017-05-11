package org.marketsuite.marektview.valuation;

import org.marketsuite.component.graph.SimpleBarGraph;
import org.marketsuite.component.graph.SimpleXYGraph;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.spinner.DecimalSpinner;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.marektview.history.DividendRecord;
import jsc.descriptive.FrequencyTable;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.graph.SimpleXYGraph;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.spinner.DecimalSpinner;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.marektview.history.DividendRecord;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;

//to show single year's valuation with variation of expected return(r) and dividend growth rate (g)
// this is necessary since both variables are somewhat subjective
class SingleYearPanel extends JPanel {
    SingleYearPanel() {
        setLayout(new MigLayout("insets 0, fill"));
        JPanel tool_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
                new MigLayout("insets 0", "5[][][]push[][][][][][]5", "5[]5"));
        JLabel syb = new JLabel("Symbol:"); syb.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        tool_pnl.add(syb); tool_pnl.add(_fldSymbol); _fldSymbol.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _fldSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String sym = _fldSymbol.getText().toUpperCase();
                _fldSymbol.setText(sym);

                //special treatment for SP500 (not SPY)
                if (sym.equals(FrameworkConstants.SP500)) {//read from SP500 dividend records already in memory
                    _aryDivRecords = FrameworkConstants.SP500_DIVIDEND;
                }
                else {
                    try {
                        ArrayList<DividendRecord> drs = ValuationUtil.readDividends(sym);
                        _aryDivRecords = ValuationUtil.toAnnualDividends(drs);
                    } catch (Exception e1) {
                        WidgetUtil.showMessageInEdt("Unable to obtain dividend records for " + sym); return; }
                }
                _tmValue.populate();

                //plot growth distribution as frequency plot
                double[] growth_rates = _tmValue.divGrowthToArray();
                FrequencyTable fqt = new FrequencyTable("Growth Rate Distribution", 8, growth_rates);
                _pnlFreqency.plotFrequency(fqt);

                //calculate max, min, avg of rates
                ArrayList<Float> grs = new ArrayList<>();
                for (int i = 0; i < growth_rates.length; i++)
                    if (growth_rates[i] != 0)
                        grs.add(new Float(growth_rates[i]));
                float avg = AppUtil.average(grs); _dMinGrowth = AppUtil.min(grs); float max = AppUtil.max(grs);
                _fldAvg.setText(FrameworkConstants.PCT2_FORMAT.format(avg));
                _fldMin.setText(FrameworkConstants.PCT2_FORMAT.format(_dMinGrowth));
                _fldMax.setText(FrameworkConstants.PCT2_FORMAT.format(max));

                //plot valuation over expected return; x = r, y = value
                plotValues(_dMinGrowth, _dMinGrowth); _spnGrowthRate.setValue(_dMinGrowth * 100);
            }
        });
        tool_pnl.add(new JLabel("r = Expected Return"));

        //show growth rate stats
        JLabel lbl = new JLabel("Average Growth:"); //lbl.setFont(FrameworkConstants.MEDIUM_FONT);
        tool_pnl.add(lbl); tool_pnl.add(_fldAvg); _fldAvg.setEditable(false); _fldAvg.setFont(FrameworkConstants.MEDIUM_FONT);
        lbl = new JLabel("Min:"); //lbl.setFont(FrameworkConstants.MEDIUM_FONT);
        tool_pnl.add(lbl); tool_pnl.add(_fldMin); _fldMin.setEditable(false); _fldMin.setFont(FrameworkConstants.MEDIUM_FONT);
        lbl = new JLabel("Max:"); //lbl.setFont(FrameworkConstants.MEDIUM_FONT);
        tool_pnl.add(lbl); tool_pnl.add(_fldMax); _fldMax.setEditable(false); _fldMax.setFont(FrameworkConstants.MEDIUM_FONT);
        add(tool_pnl, "dock north");
        JSplitPane spl = new JSplitPane(JSplitPane.VERTICAL_SPLIT); spl.setDividerLocation(350);
        spl.setContinuousLayout(true); spl.setDividerSize(5);
        _tmValue = new ValueTableModel();
        _tblValue = WidgetUtil.createDynaTable(_tmValue, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new ValueRenderer());
        JScrollPane scr = new JScrollPane(_tblValue); spl.setTopComponent(scr);

        //bottom - bar graph and valuation graph in split pane
        JSplitPane bot_spl = new JSplitPane();
        bot_spl.setContinuousLayout(true); bot_spl.setDividerLocation(350); bot_spl.setDividerSize(5);
        bot_spl.setLeftComponent(_pnlFreqency);
        JPanel rite_pnl = new JPanel(new MigLayout("insets 0"));
        JPanel cntr_pnl = new JPanel(new MigLayout("insets 0", "push[][]5"));
        cntr_pnl.add(_spnGrowthRate);
        rite_pnl.add(cntr_pnl, "dock north");
        rite_pnl.add(_pnlValues, "dock center");
        bot_spl.setRightComponent(rite_pnl);
        spl.setBottomComponent(bot_spl);
        add(spl, "dock center");
    }

    //----- private methods -----
    //calculate CAGR of dividend between 2 years' dividends from _aryDivRecords, return = NaN can't compute
    private float calcDivRate(int year1, int year2) {
        DividendRecord dr1 = findRecordByYear(year1);
        DividendRecord dr2 = findRecordByYear(year2);
        if (dr1 == null || dr2 == null) return Float.NaN;//TODO NaN doesn't work
        return (float)SimUtil.calcCagr(Math.abs(year1 - year2), dr2.getDividend(), dr1.getDividend());
    }
    private double calcValue(double div0, double div_rate, double expected_return) {
        return div0 * (1 + div_rate) / (expected_return - div_rate);
    }
    private DividendRecord findRecordByYear(int year) {
        for (DividendRecord dr : _aryDivRecords)
            if (dr.getCal().get(Calendar.YEAR) == year)
                return dr;
        return null;
    }
    private void plotValues(double growth_rate, double min_expectation) {
        //plot valuation over expected return; x = r, y = value
        DividendRecord dr_2014 = findRecordByYear(2014); if (dr_2014 == null) return;//TODO may give warning
        int num_er = 40;
        double[] x_cor = new double[num_er], y_cor = new double[num_er];
        double exp_rate = min_expectation - 0.02;
        for (int i = 0; i < num_er; i++) {
            x_cor[i] = exp_rate * 100; y_cor[i] = calcValue(dr_2014.getDividend(), growth_rate, exp_rate);
            exp_rate += 0.0025; if (y_cor[i] < 0) y_cor[i] = 0;//don't show negative
        }
        _pnlValues.plot("", x_cor, y_cor);
    }

    //----- inner classes -----
    private class ValueTableModel extends DynaTableModel {
        private ValueTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        private void initCells(SimpleCell[] cells) {
            cells[TBL_COLUMN_YEARS] = new SimpleCell(new Long(0));
            cells[TBL_COLUMN_DIVIDEND] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_DIV_GROWTH] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_1] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_2] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_3] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_4] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_5] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_6] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_7] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_8] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_EXPECTED_PCT_9] = new SimpleCell(new Double(0));
        }
        public void populate() {
            _lstRows.clear();
            DividendRecord dr_2014 = findRecordByYear(2014); if (dr_2014 == null) return;//TODO may give warning
            int end_year = 2015;//for calculating growth TODO remove hard code

            //vary 2 factors: dividend growth rate (based on different number of years) and expected return rate
            for (int begin_year = end_year - 1; begin_year > 1993; begin_year--) {//TODO....
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                initCells(cells);
                cells[TBL_COLUMN_YEARS].setValue(new Long(begin_year));
                DividendRecord begin_rec = findRecordByYear(begin_year);
                if (begin_rec == null) {
                    _lstRows.add(cells); continue;//skip
                }
                cells[TBL_COLUMN_DIVIDEND].setValue(new Double(begin_rec.getDividend()));
                float div_growth = calcDivRate(end_year - 1, begin_year);
                if (Float.isNaN(div_growth)) {
                    _lstRows.add(cells); continue;//don't calculate
                }
                cells[TBL_COLUMN_DIV_GROWTH].setValue(new Double(div_growth));

                //vary expected return rate
                int col = TBL_COLUMN_EXPECTED_PCT_1;
                for (float er : _fExpectedReturn) {
                    double value = dr_2014.getDividend() * (1 + div_growth) / ((er / 100) - div_growth);
                    if (er < div_growth) value = 0;//can't compute
                    cells[col++].setValue(new Double(value));
                }
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
        private double[] divGrowthToArray() {
            double[] ret = new double[getRowCount()];
            for (int row = 0; row < getRowCount(); row++)
                ret[row] = (Double)getCell(row, TBL_COLUMN_DIV_GROWTH).getValue();
            return ret;
        }
    }
    private class ValueRenderer extends DynaTableCellRenderer {
        private ValueRenderer() { super(_tmValue); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setFont(FrameworkConstants.MEDIUM_FONT);
            switch (column) {
                case TBL_COLUMN_YEARS: break;//use default
                case TBL_COLUMN_DIVIDEND:
                    if ((Double)value <= 0) lbl.setText("");
                    else lbl.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    break;
                default://all other columns show valuations
                    if ((Double)value <= 0) lbl.setText("");
                    else lbl.setText(FrameworkConstants.DOLLAR_FORMAT2.format(value));
                    break;
                case TBL_COLUMN_DIV_GROWTH:
                    if ((Double)value == 0) lbl.setText("");
                    else lbl.setText(FrameworkConstants.PCT_FORMAT.format(value));
                    break;
            }
            return lbl;
        }
    }

    //----- variables -----
    private JTextField _fldSymbol = new JTextField(5);
    private JTextField
            _fldAvg = new JTextField(4), _fldMin = new JTextField(4),
            _fldMax = new JTextField(4), _fldMedian = new JTextField(5);//TODO mode
    private ValueTableModel _tmValue; private JTable _tblValue;
    private ArrayList<DividendRecord> _aryDivRecords = new ArrayList<>();
    private SimpleBarGraph _pnlFreqency = new SimpleBarGraph("","");
    private DecimalSpinner _spnGrowthRate = new DecimalSpinner("Dividend Growth: ", 8, 1, 20, 0.1F, " %", new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            double growth_rate = _spnGrowthRate.getValue() / 100;
            plotValues(growth_rate, _dMinGrowth);
        }
    });
    private SimpleXYGraph _pnlValues = new SimpleXYGraph("", "Valuation");
    private double _dMinGrowth;

    //----- literals -----
    private static final float[] _fExpectedReturn = new float[] { 3, 5, 6, 8, 10, 12, 15, 18, 20 };//in percent
    private static final int TBL_COLUMN_YEARS = 0;
    private static final int TBL_COLUMN_DIVIDEND = 1;
    private static final int TBL_COLUMN_DIV_GROWTH = 2;
    private static final int TBL_COLUMN_EXPECTED_PCT_1 = 3;
    private static final int TBL_COLUMN_EXPECTED_PCT_2 = 4;
    private static final int TBL_COLUMN_EXPECTED_PCT_3 = 5;
    private static final int TBL_COLUMN_EXPECTED_PCT_4 = 6;
    private static final int TBL_COLUMN_EXPECTED_PCT_5 = 7;
    private static final int TBL_COLUMN_EXPECTED_PCT_6 = 8;
    private static final int TBL_COLUMN_EXPECTED_PCT_7 = 9;
    private static final int TBL_COLUMN_EXPECTED_PCT_8 = 10;
    private static final int TBL_COLUMN_EXPECTED_PCT_9 = 11;
    private static final Object[][] TABLE_SCHEMA = {
        { "Begin Year",  ColumnTypeEnum.TYPE_LONG, -1,  40, null, null, null },//0
        { "Dividends",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },
        { "Growth Rate(g)",  ColumnTypeEnum.TYPE_DOUBLE, -1,  80, null, null, null },
        { "r = 3%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },
        { "r = 5%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },
        { "r = 6%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//5
        { "r = 8%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },
        { "r = 10%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },
        { "r = 12%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },
        { "r = 15%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },
        { "r = 18%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//10
        { "r = 20%",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },
    };
}
