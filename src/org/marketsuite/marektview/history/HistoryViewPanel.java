package org.marketsuite.marektview.history;

import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.graph.SimpleBarGraph;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.quote.MonthlyQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import jsc.descriptive.FrequencyTable;
import jxl.Sheet;
import jxl.Workbook;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.model.quote.MonthlyQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//display line chart showing long term history of SPX500
//  source: an excel file with monthly SPX500 closing prices(from Schilling) since YAHOO only starts 1/31/1950
//  JFreeChart does not plot data prior to 1900, so add a statement to filter it out
//  Monthly quote does not seem to agree with YAHOO data,
//  only use data from 1900 to 1969 from  assuming they are "closing price" for each year
//  after 1970, use YAHOO quotes
//  Other data such as CPI, PE..etc use everything till there is better source.
public class HistoryViewPanel extends JPanel {
    public HistoryViewPanel() {
        setLayout(new MigLayout("insets 0"));
        JPanel tool_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[][]10[][]push[][][]5[]5", "3[]3"));
        tool_pnl.add(new JLabel("Symbol:")); tool_pnl.add(_cmbSymbol); _cmbSymbol.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                //TODO switch dataset
            }
        });
        tool_pnl.add(new JLabel("Scenario:")); tool_pnl.add(_cmbScenario);
        _cmbScenario.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                _tmRoi.populate();
                if (_tmpRecords.size() > 0) {
                    showStats();
                    calcFrequency();
                    _pnlChart.plotAnnotations(_tmpRecords);
                } else {
                    clearStats();
                    _pnlChart.clear();
                }

            }
        });
        tool_pnl.add(_fldStartYear); tool_pnl.add(_fldEndYear);
        tool_pnl.add(_btnZoom); _btnZoom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { zoom(); } });
        tool_pnl.add(_btnClear); _btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { _pnlChart.clear(); } });
        add(tool_pnl, "dock north");

        //center - split pane for table and _pnlChart
        JSplitPane splt = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); splt.setDividerLocation(300);
        splt.setContinuousLayout(true);
        _pnlChart = new HistoryGraphPanel("", "SP500 History", null);
        _pnlChart.setLogScale(true);
        splt.setRightComponent(_pnlChart);

        //left - split pane, top : table + stat
        JSplitPane lsplt = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        lsplt.setContinuousLayout(true); lsplt.setDividerLocation(350);
        JPanel top_pnl = new JPanel(new MigLayout("insets 0"));
        _tmRoi = new RoiTableModel();
        _tblRoi = WidgetUtil.createDynaTable(_tmRoi, ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new RoiCellRenderer());
        JScrollPane scr = new JScrollPane(_tblRoi);
        top_pnl.add(scr, "dock center");
        JPanel stat_pnl = new JPanel(new MigLayout("insets 0, wrap 4"));
        stat_pnl.add(new JLabel("# Winnning Years:")); stat_pnl.add(_fldCount, "wrap");
        _fldCount.setEditable(false); WidgetUtil.attachToolTip(_fldCount, "Winning Years, Losing Years, Total",
                SwingConstants.TOP, SwingConstants.LEFT);
        stat_pnl.add(new JLabel("Winning Percent:")); stat_pnl.add(_fldPct); _fldPct.setEditable(false);
        stat_pnl.add(new JLabel("Average Gain:")); stat_pnl.add(_fldAvg); _fldAvg.setEditable(false);
        stat_pnl.add(new JLabel("Minimum Gain:")); stat_pnl.add(_fldMin); _fldMin.setEditable(false);
        stat_pnl.add(new JLabel("Maximum Gain:")); stat_pnl.add(_fldMax); _fldMax.setEditable(false);
        top_pnl.add(stat_pnl, "dock south");
        lsplt.setTopComponent(top_pnl);
        splt.setLeftComponent(lsplt);

        //left split, bottom : bar chart
        lsplt.setBottomComponent(_pnlFreqency);
        add(splt, "dock center");

        //read monthly data from file/memory into array of objects
        try {
            _Workbook = Workbook.getWorkbook(new File(FrameworkConstants.DATA_FILE_MARKET_HISTORY));
            Sheet sheet = _Workbook.getSheet(SP500_SHEET_NUMBER);//2nd sheet has SPX history from 1870s
            int row = SP500_SHEET_FIRST_DATA_ROW; //boolean found_end = false;
            do {
                String date_str = sheet.getCell(SP500_SHEET_COLUMN_DATE, row).getContents();
                if (date_str.equals("")) break;
                String quote_str = sheet.getCell(SP500_SHEET_COLUMN_QUOTE, row++).getContents();

                //parse date string into Calendar
                Date date_obj = SP500_SHEET_MONTH_FORMAT.parse(date_str);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date_obj);
                int yr = cal.get(Calendar.YEAR);
                if (yr < 1900) continue;
                if (yr >= 1950) continue; //after 1950 use Yahoo data in memory
                float quote = Float.parseFloat(quote_str);//convert quote into float
                _SpxMonthlyQuotes.add(new HistoricalQuote(cal, quote));
            } while (true);
        } catch (Exception e) {
            e.printStackTrace(); //TODO print to log window
        }//don't close sheet till political data is read

        //from 1950 to now, read Yahoo data (SPX is already in memory)
        MonthlyQuote monthly = new MonthlyQuote(FrameworkConstants.SP500, FrameworkConstants.SP500_DATA.getSize() - 1);
//System.err.println("===> " + _SpxMonthlyQuotes.get(_SpxMonthlyQuotes.size() - 1).getCalendar() + "  # = " + _SpxMonthlyQuotes.size());
        //traverse from old to new
        for (int idx = monthly.getSize() - 1; idx >=0; idx--) {
            float close = monthly.getClose(idx);
            String date = monthly.getDate(idx);
            _SpxMonthlyQuotes.add(new HistoricalQuote(AppUtil.stringToCalendarNoEx(date), close));
        }
        calcYearlyReturn(); populatePoliticalData(); _Workbook.close();
        _tmRoi.populate(); showStats();
        if (_tmpRecords.size() > 0)
            calcFrequency();
        plot(true); plot(false);//yearly / monthly
    }

    //-----private methods-----
    //traverse monthly quotes, produce yearly data with ROI, fill in some information
    private void calcYearlyReturn() {
        Calendar tody = Calendar.getInstance(); int this_yr = tody.get(Calendar.YEAR); int this_month = tody.get(Calendar.MONTH);
        float start_price = 0; _YearlyRecords.clear(); _SpxYearlyQuotes.clear();
        for (HistoricalQuote smq : _SpxMonthlyQuotes) {
            int month = smq.getCalendar().get(Calendar.MONTH);
            int yr = smq.getCalendar().get(Calendar.YEAR);
            if (yr == 1900) {//special case for 1900 ROI
                if (month == 0)
                    start_price = smq.getPrice();
                else if (month == 11) {
                    float roi = (smq.getPrice() - start_price) / start_price;
                    _SpxYearlyQuotes.add(new HistoricalQuote(smq.getCalendar(), smq.getPrice(),roi));
                    _YearlyRecords.add(new HistoricalRecord(smq.getCalendar(), smq.getPrice(), roi));
//    System.err.println("Add [YR]= " + yr + " [END, START]= " + smq.getPrice() + ", " + start_price + " [ROI]= " + roi * 100);
                    start_price = smq.getPrice();
                }
                continue;
            }

            //other years
            if (month == 11) {//found end of year
                float roi = (smq.getPrice() - start_price) / start_price;
                _SpxYearlyQuotes.add(new HistoricalQuote(smq.getCalendar(), smq.getPrice(), roi));
                _YearlyRecords.add(new HistoricalRecord(smq.getCalendar(), smq.getPrice(), roi));
//System.err.println("Add [YR]= " + yr + " [END, START]= " + smq.getPrice() + ", " + start_price + " [ROI]= " + roi * 100);
                start_price = smq.getPrice();
            }

            //recent partial year, not at December
            else if (this_yr == yr && this_month == month) {
                float roi = (smq.getPrice() - start_price) / start_price;
                _SpxYearlyQuotes.add(new HistoricalQuote(smq.getCalendar(), smq.getPrice(), roi));
                _YearlyRecords.add(new HistoricalRecord(smq.getCalendar(), smq.getPrice(), roi));
            }
        }

        //gather stats
        int gain_cnt = 0, loss_cnt = 0, flat_cnt = 0;
        int gain_ey_cnt = 0, loss_ey_cnt = 0, flat_ey_cnt = 0; //election yr
        int gain_eym1_cnt = 0, loss_eym1_cnt = 0, flat_eym1_cnt = 0; //election yr - 1
        int gain_e5_cnt = 0, loss_e5_cnt = 0, flat_e5_cnt = 0; //ending 5 yr
        int gain_eym1e5_cnt = 0, loss_eym1e5_cnt = 0, flat_eym1e5_cnt = 0; //ending 5 yr
        ArrayList<Integer> gain_yrs = new ArrayList<>(), loss_yrs = new ArrayList<>(), gain_ey_yrs = new ArrayList<>(), loss_ey_yrs = new ArrayList<>(),
            gain_e5_yrs = new ArrayList<>(), loss_e5_yrs = new ArrayList<>(), gain_eym1_yrs = new ArrayList<>(), loss_eym1_yrs = new ArrayList<>(),
            gain_eym1e5_yrs = new ArrayList<>(), loss_eym1e5_yrs = new ArrayList<>();
        int yr = 1900;
//        for (HistoricalQuote hq : _SpxYearlyQuotes) {
        for (HistoricalRecord hq : _YearlyRecords) {
//if (yr == 1954)
//System.err.println("???????");
            float roi = hq.getRoi();
            if (roi > 0) {
                gain_cnt++; gain_yrs.add(yr);
            } else if (roi < 0) {
                loss_cnt++; loss_yrs.add(yr);
            } else flat_cnt++;

            //election year
            if (yr % 4 == 0) {
                if (roi > 0) {
                    gain_ey_cnt++; gain_ey_yrs.add(yr);
                } else if (roi < 0) {
                    loss_ey_cnt++; loss_ey_yrs.add(yr);
                } else flat_ey_cnt++; }

            //election year - 1
            if (yr % 4 == 3) {
                if (roi > 0) {
                    gain_eym1_cnt++; gain_eym1_yrs.add(yr);
                } else if (roi < 0) {
                    loss_eym1_cnt++; loss_eym1_yrs.add(yr);
                } else flat_eym1_cnt++; }

            //ending 5 year
            String str = String.valueOf(yr); String last_dig = str.substring(str.length() - 1, str.length());
            if (last_dig.equals("5")) {
                if (roi > 0) {
                    gain_e5_cnt++; gain_e5_yrs.add(yr);
                } else if (roi < 0) {
                    loss_e5_cnt++; loss_e5_yrs.add(yr);
                } else flat_e5_cnt++; }

            //election yr -1 and ending 5
            if (yr % 4 == 3 && last_dig.equals("5")) {
                if (roi > 0) {
                    gain_eym1e5_cnt++; gain_eym1e5_yrs.add(yr);
                } else if (roi < 0) {
                    loss_eym1e5_cnt++; loss_eym1e5_yrs.add(yr);
                } else flat_eym1e5_cnt++; }
            yr++;
        }
//        System.err.println("ALL Years      ===> " + gain_cnt + "  " + loss_cnt + " " + (flat_cnt > 0 ? flat_cnt : "") + " "
//            + (gain_cnt * 100) / (gain_cnt + loss_cnt) + "% ");// + gain_yrs.toString() + " - " + loss_yrs.toString());
//
//        System.err.println("Election Years ===> " + gain_ey_cnt + "  " + loss_ey_cnt + " " + (flat_ey_cnt > 0 ? flat_ey_cnt : "" ) + " "
//            + (gain_ey_cnt * 100) / (gain_ey_cnt + loss_ey_cnt) + "% " + gain_ey_yrs.toString() + " - " + loss_ey_yrs.toString());
//
//        System.err.println("Election - 1 ===> " + gain_eym1_cnt + "  " + loss_eym1_cnt + " " + (flat_eym1_cnt > 0 ? flat_eym1_cnt : "" ) + " "
//                + (gain_eym1_cnt * 100) / (gain_eym1_cnt + loss_eym1_cnt) + "% " + gain_eym1_yrs.toString() + "-" + loss_eym1_yrs.toString());
//
//        System.err.println("Ending 5 Years ===> " + gain_e5_cnt + "  " + loss_e5_cnt + " " + (flat_e5_cnt > 0 ? flat_e5_cnt : "" ) + " "
//            + (gain_e5_cnt * 100) / (gain_e5_cnt + loss_e5_cnt) + "% " + gain_e5_yrs.toString() + "-" + loss_e5_yrs.toString());
//
//        System.err.println("Election - 1 + Ending 5 Years ===> " + gain_eym1e5_cnt + "  " + loss_eym1e5_cnt + " " + (flat_eym1e5_cnt > 0 ? flat_eym1e5_cnt : "") + " "
//                + (gain_eym1e5_cnt * 100) / (gain_eym1e5_cnt + loss_eym1e5_cnt) + "% " + gain_eym1e5_yrs.toString()
//        );
    }
    //read political data from shee #3 and use it to populate same yearly data
    private void populatePoliticalData() {
        Sheet political_sheet = _Workbook.getSheet(POLITICAL_SHEET_NUMBER);
        HistoricalRecord prev_record = null;
        int row = POLITICAL_SHEET_FIRST_DATA_ROW;
        do {
            if (row == 45 && prev_record != null) {//last president, fill in year_count after sworn in till now  TODO why do I need this check?????
                int yr = prev_record.getSwornInDate().get(Calendar.YEAR);
                Calendar today = Calendar.getInstance();
                int this_yr = today.get(Calendar.YEAR);
                for (int y = yr + 1; y <= this_yr; y++) {
                    HistoricalRecord rec = findRecord(y); if (rec == null) continue;
                    rec.setPresident(prev_record.getPresident());
                    rec.setSwornInDate(prev_record.getSwornInDate());
                    rec.setPresidentParty(prev_record.getPresidentParty());
                    rec.setSenateMajority(prev_record.getSenateMajority());
                }
                break;
            }
            String president = political_sheet.getCell(POLITICAL_SHEET_COLUMN_PRESIDENT, row).getContents();
            if (president.equals("")) break;//empty cell to end
            String date_str = political_sheet.getCell(POLITICAL_SHEET_COLUMN_SWORN_IN_DATE, row).getContents();
            Calendar cal = Calendar.getInstance();
            Date date_obj;
            try {
                date_obj = FrameworkConstants.YAHOO_DATE_FORMAT.parse(date_str);
                cal.setTime(date_obj);
            } catch (ParseException e) {
                e.printStackTrace();//should never be here since I control data closely, if not use today's date
            }
            String president_party = political_sheet.getCell(POLITICAL_SHEET_COLUMN_PRESIDENT_PARTY, row).getContents();
            String senate_party = political_sheet.getCell(POLITICAL_SHEET_COLUMN_SENATE_PARTY, row).getContents();

            //with this political cur_record, update historical data array with information
            int sworn_in_yr = cal.get(Calendar.YEAR);

            //fill in records after prev_record till this year
            if (prev_record != null) {
                int prev_year = prev_record.getSwornInDate().get(Calendar.YEAR);
                for (int yr = prev_year + 1; yr < sworn_in_yr; yr++) {
                    HistoricalRecord between_record = findRecord(yr);
                    between_record.setPresident(prev_record.getPresident());
                    between_record.setSwornInDate(prev_record.getSwornInDate());
                    between_record.setPresidentParty(prev_record.getPresidentParty());
                    between_record.setSenateMajority(prev_record.getSenateMajority());
                }
                if (sworn_in_yr - prev_year == 8) {//2 term +, 7th year
                    HistoricalRecord rec = findRecord(sworn_in_yr - 2);
                    rec.setSeventhYear(true);
                }
            }

            //start new president
            HistoricalRecord cur_record = findRecord(sworn_in_yr); if (cur_record == null) {
                /*System.err.println("????? Can't Find This Year in History: " + sworn_in_yr);*/ row++; continue; }
            cur_record.setPresident(president); cur_record.setSwornInDate(cal);
            cur_record.setPresidentParty(PoliticalParty.valueOf(president_party));
            cur_record.setSenateMajority(senate_party.equals("") ? PoliticalParty.Other : PoliticalParty.valueOf(senate_party));
            prev_record = cur_record;
            row++;
        } while (true);
    }
    private HistoricalRecord findRecord(int year) {
        for (HistoricalRecord hd : _YearlyRecords)
            if (year == hd.getCalendar().get(Calendar.YEAR)) return hd;
        return null;
    }
    private void showStats() {
        int win_yr_cnt = _tmRoi.getRowCount();
        int total = win_yr_cnt + _nLossYrCount;
        _fldCount.setText(String.valueOf(win_yr_cnt) + ", " + _nLossYrCount + ", " + total);
        _fldPct.setText(FrameworkConstants.PCT2_FORMAT.format( (float) win_yr_cnt / total));
        _tmRoi.calcStat();
        _fldAvg.setText(FrameworkConstants.PCT2_FORMAT.format(_fAvg));
        _fldMin.setText(FrameworkConstants.PCT2_FORMAT.format(_fMin));
        _fldMax.setText(FrameworkConstants.PCT2_FORMAT.format(_fMax));
    }
    private void clearStats() { _fldCount.setText(""); _fldPct.setText(""); _fldAvg.setText("");
        _fldMax.setText(""); _fldMin.setText(""); }
    private void calcFrequency() {
        FrequencyTable fqt = new FrequencyTable("Gain Distribution", 5, _tmRoi.roiToArray());
        _pnlFreqency.plotFrequency(fqt);
    }
    private void plot(boolean yearly) {
        int size = yearly ?_SpxYearlyQuotes.size() : _SpxMonthlyQuotes.size();
        Calendar[] dates = new Calendar[size];
        double[] quotes = new double[size];
        if (size > 0) {
            int index = 0;
            if (yearly) {
                for (HistoricalRecord smq : _YearlyRecords) {
                    dates[index] = smq.getCalendar();
                    quotes[index++] = smq.getPrice();
                }
                _pnlChart.plot(true, "SP500 Yearly History", dates, quotes);
            }
            else {
                for (HistoricalQuote smq : _SpxMonthlyQuotes) {
                    dates[index] = smq.getCalendar();
                    quotes[index++] = smq.getPrice();
                }
                _pnlChart.plot(false, "SP500 Monthly History", dates, quotes);
            }
        }
    }
    private void zoom() {//use start and end years from field
        long start_yr = _fldStartYear.getValue();
        long end_yr = _fldEndYear.getValue(); int size = (int)(end_yr - start_yr); if (size < 0) return;
        int this_yr = Calendar.getInstance().get(Calendar.YEAR);
        if (end_yr > this_yr) return;
        Calendar[] dates = new Calendar[size + 1];
        double[] quotes = new double[size + 1];
        int index = 0;
        for (HistoricalRecord hr : _YearlyRecords) {
            int yr = hr.getYear();
            if (yr < start_yr || yr > end_yr) continue;
            dates[index] = hr.getCalendar();
            quotes[index++] = hr.getPrice();
        }
        _pnlChart.plot(true, "SP500 Yearly", dates, quotes);
    }

    //inner class
    private class RoiTableModel extends DynaTableModel {
        private RoiTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public boolean isCellEditable(int row, int col) { return false; }
        public void populate() {
            //based on selected factor, perform different calculation, also generate _tmpQuotes for plot
            int sel = _cmbScenario.getSelectedIndex();
            _lstRows.clear(); _tmpQuotes.clear(); _tmpRecords.clear();
            _nLossYrCount = 0;
            switch (sel) {
                case 0://all years
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.getRoi() > 0) {
                            addRow(hr.getYear(), hr.getRoi()); _tmpRecords.add(hr); }
                        else
                            _nLossYrCount++;
                    }
                    break;

                case 1://election years
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isElectionYear())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi()); _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 2://pre-election years
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPreElectionYear())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 3://post-election years
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPostElectionYear())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 4://ending in 5 years
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isYearEnding5())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 5://ending in 0 years
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isYearEnding0())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 6://year of lamb
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isLambYear())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 7://7th year
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isSeventhYear())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 8://democratic president
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isDemocraticPresident())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 9://republican president
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isRepublicanPresident()) {
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                        }
                    }
                    break;

                case 10://democratic senate
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isDemocraticSenate())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 11://republican senate
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isRepublicanSenate())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 12://pre-election and ending in 5 years
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPreElectionYear() && hr.isYearEnding5())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 13://pre-election and democratic president
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPreElectionYear() && hr.isDemocraticPresident())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 14://pre-election and republican president
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPreElectionYear() && hr.isRepublicanPresident())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 15://pre-election, ending in 5 years, democratic president
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPreElectionYear() && hr.isYearEnding5() && hr.isDemocraticPresident())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 16://pre-election, ending in 5 years, republican president
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPreElectionYear() && hr.isYearEnding5() && hr.isRepublicanPresident())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 17://pre-election, ending in 5 years, republican president
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPreElectionYear() && hr.isDemocraticPresident() && hr.isRepublicanSenate())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;

                case 18://pre-election, ending in 5 years, republican president, year of lamb
                    for (HistoricalRecord hr : _YearlyRecords) {
                        if (hr.isPreElectionYear() && hr.isDemocraticPresident() && hr.isLambYear())
                            if (hr.getRoi() > 0) {
                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
                            else _nLossYrCount++;//including flat years
                    }
                    break;
//
//                case 14://pre-election, ending in 5 years, republican president
//                    for (HistoricalRecord hr : _YearlyRecords) {
//                        if (hr.isPreElectionYear() && hr.isYearEnding5() && hr.isRepublicanPresident())
//                            if (hr.getRoi() > 0) {
//                                addRow(hr.getYear(), hr.getRoi());  _tmpRecords.add(hr); }
//                            else _nLossYrCount++;//including flat years
//                    }
//                    break;

                default:
                    break;
            }
            _tmRoi.fireTableDataChanged();
        }
        //helper to create table row
        private void addRow(int yr, float roi) {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
            cells[TBL_COLUMN_YEAR] = new SimpleCell(new Long(yr));
            cells[TBL_COLUMN_ROI] = new SimpleCell(new Double(roi));
            _lstRows.add(cells);
        }
        //find average on each add
        private void calcStat() {
            double sum = 0, min = 1.2F, max = -1;
            for (int row = 0; row < getRowCount(); row++) {
                double v   = (Double)getCell(row, TBL_COLUMN_ROI).getValue();
                sum += v;
                if (v < min) min = v;
                else if (v > max) max = v;
            }
            _fAvg = sum / getRowCount(); _fMin = min; _fMax = max;
        }
        //convert ROI cells into array
        private double[] roiToArray() {
            double[] ret = new double[getRowCount()];
            for (int row = 0; row < getRowCount(); row++)
                ret[row] = (Double)getCell(row, TBL_COLUMN_ROI).getValue();
            return ret;
        }
    }
    private class RoiCellRenderer extends DynaTableCellRenderer {
        private RoiCellRenderer() { super(_tmRoi); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == TBL_COLUMN_ROI) {
                double v = (Double)value;
                lbl.setText(FrameworkConstants.PCT2_FORMAT.format(v));
            }
            else if (column == TBL_COLUMN_YEAR)
                lbl.setText(String.valueOf(value));
            return lbl;
        }
    }

    //variables
    private JComboBox<String> _cmbSymbol = new JComboBox<>(LIST_INDEX);
    private LongIntegerField _fldStartYear = new LongIntegerField(1900, 5, 1900, 2050),
            _fldEndYear = new LongIntegerField(2015, 5, 1900, 2050);
    private JButton _btnZoom = WidgetUtil.createIconButton("Zoom In/Out", FrameworkIcon.ZOOM_IN);
    private JButton _btnClear = WidgetUtil.createIconButton("Clear Annotations", FrameworkIcon.CLEAR);
    private Workbook _Workbook;
    private ArrayList<HistoricalQuote> _SpxMonthlyQuotes = new ArrayList<>(), _SpxYearlyQuotes = new ArrayList<>();
    private ArrayList<HistoricalQuote> _tmpQuotes = new ArrayList<>();//temporary list for plotting and table
    private ArrayList<HistoricalRecord> _YearlyRecords = new ArrayList<>();
    private ArrayList<HistoricalRecord> _tmpRecords = new ArrayList<>();
    private JTable _tblRoi;
    private RoiTableModel _tmRoi;
    private JComboBox<String> _cmbScenario = new JComboBox<>(SCENARIO);
    private JTextField _fldCount = new JTextField(8), _fldPct = new JTextField(5),
        _fldAvg = new JTextField(5), _fldMin = new JTextField(5),
        _fldMax = new JTextField(5), _fldMedian = new JTextField(5);
    private double _fAvg, _fMin, _fMax, _fMedian;
    private int _nLossYrCount;
    private SimpleBarGraph _pnlFreqency = new SimpleBarGraph("","");
    private HistoryGraphPanel _pnlChart;
    //literals
    private static final String[] SCENARIO = {
        "All Years", "Election Years", "Pre Election Years", "Post Election Years",//3
        "Ending 5 Years", "Ending 0 Years", "Year of the Lamb", "7th Year President",//7
        "Democratic President", "Republican President", "Democratic Senate", "Republican Senate",//11
        "Pre-Election, Ending in 5 Years",//12
        "Pre-Election, Democratic President", "Pre-Election, Republican President",//14
        "Pre-Election, Ending 5, Democratic President",
        "Pre-Election, Ending 5, Republican President",//16
        "Pre-Election, Democratic President, Republican Senate",//17
        "Pre-Election, Democratic President, Year of Lamb",//18
    };
    private static final int SP500_SHEET_NUMBER = 2;//0 based
    private static final int SP500_SHEET_COLUMN_DATE = 0;//0 based
    private static final int SP500_SHEET_COLUMN_QUOTE = 1;
    private static final int SP500_SHEET_FIRST_DATA_ROW = 9;//from sheet 2 for SP500 data
    private static final DateFormat SP500_SHEET_MONTH_FORMAT = new SimpleDateFormat("yyyy.MM");
    private static final int POLITICAL_SHEET_NUMBER = 3;//0 based
    private static final int POLITICAL_SHEET_COLUMN_PRESIDENT = 0;
    private static final int POLITICAL_SHEET_COLUMN_SWORN_IN_DATE = 1;
    private static final int POLITICAL_SHEET_COLUMN_PRESIDENT_PARTY = 2;
    private static final int POLITICAL_SHEET_COLUMN_SENATE_PARTY = 3;
    private static final int POLITICAL_SHEET_FIRST_DATA_ROW = 1;//from sheet 3 for political data (1 based)
    private static final int TBL_COLUMN_YEAR = 0;
    private static final int TBL_COLUMN_ROI = 1;
    private static final Object[][] TABLE_SCHEMA = {
        { "Year",  ColumnTypeEnum.TYPE_LONG, -1,  30, null, null, null },//0, year
        { "ROI",  ColumnTypeEnum.TYPE_DOUBLE, -1,  50, null, null, null },//1, ROI
    };
    private static final String LIST_INDEX[] = new String[] { "S&P 500", "Dow Jones Industrial Average", "Nasdaq Composite" };

    public static void main(String[] args) {
        FrameworkConstants.adjustDataFolder();
        new HistoryViewPanel();
    }
}