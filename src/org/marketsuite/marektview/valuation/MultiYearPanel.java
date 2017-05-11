package org.marketsuite.marektview.valuation;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.quote.MonthlyQuote;
import org.marketsuite.framework.model.quote.YearlyQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.marektview.history.DividendRecord;
import org.marketsuite.marektview.history.HistoricalQuote;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.quote.YearlyQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.marektview.history.DividendRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;

//to display market index valuations based on Gorden's DDM.
class MultiYearPanel extends JPanel {
    MultiYearPanel() {
        setLayout(new MigLayout("insets 0, fill"));
        JPanel tool_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
            new MigLayout("insets 0", "5[][]20[][]5[]push[][][]30[][]5", "5[]5"));
        JLabel syb = new JLabel("Symbol:"); syb.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        tool_pnl.add(syb); tool_pnl.add(_fldSymbol); _fldSymbol.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _fldSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //construct yearly quotes
                String sym = _fldSymbol.getText().toUpperCase(); _fldSymbol.setText(sym);
                _YearlyQuotes = new YearlyQuote(sym, DAYS_PER_YEAR * 120);
                if (_YearlyQuotes.getSize() == 0) { WidgetUtil.showMessageNoEdt("Can't Read Quotes for " + sym); return; }

                //load dividend from file into DividendRecord
                ArrayList<DividendRecord> drs = new ArrayList<>();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER_DIVIDEND
                            + File.separator + sym + FrameworkConstants.EXTENSION_DIVIDEND));
                    String line;
                    int line_num = 1;
                    while ((line = br.readLine()) != null) {
                        if (line_num == 1) {
                            line_num++;
                            continue;
                        }
                        String[] tokens = line.split(",");
                        //first part date, 2nd part div
                        String date = tokens[0];
                        Calendar cal = AppUtil.stringToCalendarNoEx(date);
                        if (cal.get(Calendar.YEAR) > 2014) continue;//skip 2015 TODO next year add 1
                        String div_str = tokens[1];
                        float div = Float.parseFloat(div_str);
                        DividendRecord dr = new DividendRecord(sym, cal, div);
                        drs.add(dr);
                    }
                } catch (Exception e1) {
//                    e1.printStackTrace();
                    WidgetUtil.showMessageNoEdt("Can't Read Dividend for " + sym); return; }

                //combine multiple dividends in any year into single yearly record
                _aryDivRecords = new ArrayList<>();
                int prev_yr = -1;
                float total_div = 0;
                Calendar prev_cal = null;
                for (DividendRecord dr : drs) {
                    int cur_yr = dr.getCal().get(Calendar.YEAR);
                    if (prev_yr != cur_yr) {
                        if (prev_yr == -1) {//first record
                            prev_yr = cur_yr;
                            prev_cal = dr.getCal();
                            total_div += dr.getDividend();
                        } else {//save old record, start new one
                            _aryDivRecords.add(new DividendRecord(sym, prev_cal, total_div));
                            prev_yr = cur_yr;
                            prev_cal = dr.getCal();
                            total_div = dr.getDividend();//first record
                        }
                    } else {//same year, add dividend
                        total_div += dr.getDividend();
                    }
                }
                _tmValue.populate();
            }
        });
        JLabel gro = new JLabel("Growth Rate Option:");
        tool_pnl.add(gro); gro.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        tool_pnl.add(_cmbGrowthMethod); _cmbGrowthMethod.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _cmbGrowthMethod.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                _tmValue.populate();
            }
        });
//        tool_pnl.add(_fldDivRate); _fldDivRate.setEditable(false); _fldDivRate.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        JLabel er = new JLabel("Expected Return(r):"); er.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
//        tool_pnl.add(er); tool_pnl.add(_fldExpectedReturn); _fldExpectedReturn.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _fldExpectedReturn.setText("4.5"); _fldExpectedReturn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tmValue.populate();
            }
        }); //tool_pnl.add(new JLabel("%"));
        JLabel lbl = new JLabel("DDM Value:"); lbl.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
//        tool_pnl.add(lbl); tool_pnl.add(_fldValue); _fldValue.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _fldValue.setEditable(false);
        add(tool_pnl, "dock north");

        //table of past and future years
        _tmValue = new ValueTableModel();
        _tblValue = WidgetUtil.createDynaTable(_tmValue, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new ValueRenderer());
        JScrollPane scr = new JScrollPane(_tblValue);
        add(scr, "dock center");
        _cmbGrowthMethod.setSelectedIndex(4);//10 year
    }

    //----- private methods -----

    //----- inner classes -----
    private class ValueTableModel extends DynaTableModel {
        private ValueTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public boolean isCellEditable(int row, int col) { return col == TBL_COLUMN_EXPECTED_RETURN; }
        public void setValueAt(Object value, int row, int column) {
            //only expected return comes here
            super.setValueAt(value, row, column);
            //calculate valuation
            calcValuation(row);
            fireTableCellUpdated(row, TBL_COLUMN_VALUE);
        }
        public void populate() {//get historical dividends, calculate valuation
            _lstRows.clear();

            //add existing dividend data to table (till 2014)
            for (DividendRecord dr : _aryDivRecords) {//TODO FrameworkConstants.SP500_DIVIDEND) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                int yr = dr.getCal().get(Calendar.YEAR);
                cells[TBL_COLUMN_YEAR] = new SimpleCell(new Long(yr));
                cells[TBL_COLUMN_DIVIDEND] = new SimpleCell(new Double(dr.getDividend()));

                //calculate dividend growth rate
                cells[TBL_COLUMN_DIV_RATE] = new SimpleCell(new Double(0));
                cells[TBL_COLUMN_EXPECTED_RETURN] = new SimpleCell(new Double(0));
                cells[TBL_COLUMN_EXPECTED_RETURN].setHighlight(true);
                cells[TBL_COLUMN_VALUE] = new SimpleCell(new Double(0));
                FundQuote quote = _YearlyQuotes.findRecord(yr);
                cells[TBL_COLUMN_QUOTE] = new SimpleCell(new Double(0));
                cells[TBL_COLUMN_YIELD] = new SimpleCell(new Double(0));
                if (quote != null) {
                    cells[TBL_COLUMN_QUOTE] = new SimpleCell(new Double(quote.getClose()));
                }
                _lstRows.add(cells);
            }
            if (getRowCount() == 0) { fireTableDataChanged(); return; } //no data

            //calculate dividend growth and valuation
            for (int row = 0; row < getRowCount(); row++) {
                long yr = (Long)getCell(row, TBL_COLUMN_YEAR).getValue();
                if (yr > _nLatestYear) { _nLatestYear = (int)yr; _nLatestYearRow = row; }
                calcDivRate(row); calcValuation(row);
                double div = (Double)getCell(row, TBL_COLUMN_DIVIDEND).getValue();
                double price = (Double)getCell(row, TBL_COLUMN_QUOTE).getValue();
                getCell(row, TBL_COLUMN_YIELD).setValue(new Double(div / price));
            }

//            double cur_rate = (Double)getCell(_nLatestYearRow, TBL_COLUMN_DIV_RATE).getValue();
            //also update fields on tool bar with most recent valuation (last row with real dividend)
//            double cur_div = (Double)getCell(_nLatestYearRow, TBL_COLUMN_DIVIDEND).getValue();
//            _fldDiv.setText(FrameworkConstants.DOLLAR_FORMAT.format(cur_div));
//            _fldDivRate.setText(FrameworkConstants.PCT_FORMAT.format(cur_rate));
//            _fldValue.setText(FrameworkConstants.DOLLAR_FORMAT.format(getCell(_nLatestYearRow, TBL_COLUMN_VALUE).getValue()));

            //make up future 25 year dividends from last real dividend (2014)
//            for (long yr = _nLatestYear + 1; yr < _nLatestYear + 25; yr++) {
//                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
//                initCells(cells);
//                cells[TBL_COLUMN_YEAR] = new SimpleCell(new Long(yr));
//                double div = cur_div * Math.pow(1 + cur_rate, yr - _nLatestYear);
//                cells[TBL_COLUMN_DIVIDEND] = new SimpleCell(new Double(div));
//                cells[TBL_COLUMN_DIV_RATE] = new SimpleCell(new Double(cur_rate));
//                double exp = _fldExpectedReturn.getValue() / 100;
//                double val = div * (1 + cur_rate) / (exp - cur_rate);
//                if (exp < cur_rate) val = 0;//can't compute
//                cells[TBL_COLUMN_VALUE] = new SimpleCell(new Double(val));
////TODO later                _lstRows.add(cells);
//            }
            fireTableDataChanged();
        }
        private void initCells(SimpleCell[] cells) {
            cells[TBL_COLUMN_YEAR] = new SimpleCell(new Long(0));
            cells[TBL_COLUMN_DIVIDEND] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_DIV_RATE] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_VALUE] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_QUOTE] = new SimpleCell(new Double(0));
            cells[TBL_COLUMN_YIELD] = new SimpleCell(new Double(0));
        }
        //use CAGR for long term dividend growth
        private void calcDivRate(int row) {//based on growth method in toolbar and data in table
            double cur_div = (Double)getCell(row, TBL_COLUMN_DIVIDEND).getValue();
            int method = _cmbGrowthMethod.getSelectedIndex();
            int prev_row;
            switch (method) {
                case 0: prev_row = row + 1; break;
                case 1: prev_row = row + 3; break;
                case 2: prev_row = row + 5; break;
                case 3: prev_row = row + 8; break;
                case 4: prev_row = row + 10; break;
                case 5: prev_row = row + 12; break;
                case 6: prev_row = row + 15; break;
                case 7: prev_row = row + 18; break;
                case 8: prev_row = row + 20; break;
                case 9: prev_row = row + 25; break;
                case 10: prev_row = row + 30; break;
                default: prev_row = row + 10;
            }
            if (prev_row < getRowCount() - 1) {
                double prev_div = (Double)getCell(prev_row, TBL_COLUMN_DIVIDEND).getValue();
                double cagr = SimUtil.calcCagr(prev_row, (float) prev_div, (float) cur_div);
                getCell(row, TBL_COLUMN_DIV_RATE).setValue(new Double(cagr));
                getCell(row, TBL_COLUMN_EXPECTED_RETURN).setValue(new Double(cagr + 0.02));//TODO make it variable
            }
        }
        //use Gorden's DDM formula
        private void calcValuation(int row) {
            double div = (Double)getCell(row, TBL_COLUMN_DIVIDEND).getValue();
            double rate = (Double)getCell(row, TBL_COLUMN_DIV_RATE).getValue();
            double exp = (Double)getCell(row, TBL_COLUMN_EXPECTED_RETURN).getValue();
            if (rate <= 0)//can't be negative or less than 0
                getCell(row, TBL_COLUMN_VALUE).setValue(new Double(0));
            else {
//TODO remove                double exp = _fldExpectedReturn.getValue() / 100;
                double val = div * (1 + rate) / (exp - rate);
                if (exp < rate) val = 0;//can't compute
                getCell(row, TBL_COLUMN_VALUE).setValue(new Double(val));
            }
        }

    }
    private class ValueRenderer extends DynaTableCellRenderer {
        private ValueRenderer() { super(_tmValue); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setFont(FrameworkConstants.MEDIUM_FONT);
            if (row == _nLatestYearRow) lbl.setBackground(new Color(18, 246, 14, 56));
            switch (column) {
                case TBL_COLUMN_YEAR:
                    long yr = (Long)value;
                    if (yr > _nLatestYear) lbl.setBackground(new Color(230, 249, 24, 110));
                    break;
                case TBL_COLUMN_VALUE:
                case TBL_COLUMN_DIVIDEND:
                case TBL_COLUMN_QUOTE:
                    if ((Double)value == 0) lbl.setText("");
                    else lbl.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    if (column == TBL_COLUMN_VALUE) lbl.setFont(FrameworkConstants.MEDIUM_BOLD_FONT);
                    break;
                case TBL_COLUMN_YIELD:
                case TBL_COLUMN_DIV_RATE:
                case TBL_COLUMN_EXPECTED_RETURN:
                    if ((Double)value == 0) lbl.setText("");
                    else lbl.setText(FrameworkConstants.PCT_FORMAT.format(value));
                    break;
            }
            return lbl;
        }
    }

    //variables
    private JTextField _fldSymbol = new JTextField(5);
    private JTextField _fldDiv = new JTextField(5), _fldYield = new JTextField(3),
            _fldDivRate = new JTextField(5), _fldValue = new JTextField(8);
    private DecimalField _fldExpectedReturn = new DecimalField(12, 5, 0, 50, null);
    private ArrayList<DividendRecord> _aryDivRecords = new ArrayList<>();
    private JComboBox<String> _cmbGrowthMethod = new JComboBox<>(LIST_DURATION);
    private JTable _tblValue;
    private ValueTableModel _tmValue;
    private long _nLatestYear;
    private int _nLatestYearRow;
    private YearlyQuote _YearlyQuotes;

    //literals
    private static final String LIST_INDEX[] = new String[] { "SP500"/*, "DJIA", "Nasdaq"*/ };
    private static final String LIST_DURATION[] = new String[] { "1 Year", "3 Year", "5 Year", "8 Year",
            "10 Year", "12 Year", "15 Year", "18 Year", "20 Year", "25 Year", "30 Year" };
    private static final int TBL_COLUMN_YEAR = 0;
    private static final int TBL_COLUMN_DIVIDEND = 1;
    private static final int TBL_COLUMN_DIV_RATE = 2;
    private static final int TBL_COLUMN_EXPECTED_RETURN = 3;
    private static final int TBL_COLUMN_VALUE = 4;
    private static final int TBL_COLUMN_QUOTE = 5;
    private static final int TBL_COLUMN_YIELD = 6;
    private static final Object[][] TABLE_SCHEMA = {
        { "Year",  ColumnTypeEnum.TYPE_LONG, -1,  30, null, null, null },//0
        { "Dividend",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//1
        { "Dividend Growth (g)",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//2
        { "Expected Return (r)",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, new DecimalCellEditor(0, 4, 0, 50, null), null, null },//3
        { "DDM Valuation",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//4
        { "Year End Price",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//5
        { "Yield",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//6
    };
    private static final int DAYS_PER_YEAR = 250;
}
