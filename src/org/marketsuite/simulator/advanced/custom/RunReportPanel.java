package org.marketsuite.simulator.advanced.custom;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SimpleCell;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.analysis.AnalysisEngine;
import org.marketsuite.framework.strategy.analysis.AnalysisPanel;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.scanner.ScannerModel;
import org.jfree.data.general.SeriesException;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.SimReport;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.scanner.ScannerModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Sub-tab to run report from user selected report panels.
 */
class RunReportPanel extends SkinPanel {
    RunReportPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());

        //title strip - center combo box, run button
        JPanel west_pnl = new JPanel();  west_pnl.setOpaque(false);
        west_pnl.add(_cmbReports = new JComboBox(_Model));
        WidgetUtil.attachToolTip(_cmbReports, FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_run_report"),
                SwingConstants.RIGHT, SwingConstants.TOP);
        west_pnl.add(Box.createHorizontalGlue());
        west_pnl.add(_btnRunReport);
        _btnRunReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TradeDetailDialog.getInstance().clear();
                runReport();
            }
        });
        west_pnl.add(Box.createGlue());
        west_pnl.add(_btnMovie);
        _btnMovie.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //slightly smaller than default main frame size to give user a que
                if (_frmMovie == null) {
                    _frmMovie = new JFrame();
                    JPanel content_pnl = new JPanel(new BorderLayout());  content_pnl.setOpaque(false);
                    _pnlMovieAnalysis = new AnalysisPanel(true);
                    content_pnl.add(_pnlMovieAnalysis, BorderLayout.CENTER);
                    //south - pause/resume button
                    JPanel btn_pnl = new JPanel();
                    btn_pnl.add(_btnPauseResume);
                    _btnPauseResume.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            if (_bPaused) {
                                _btnPauseResume.setIcon(FrameworkIcon.PAUSE_MOVIE);
                                _btnPauseResume.setToolTipText(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_pause"));
                            } else {
                                _btnPauseResume.setIcon(FrameworkIcon.RESUME_MOVIE);
                                _btnPauseResume.setToolTipText(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_resume"));
                            }
                            _bPaused = !_bPaused;
                        }
                    });
                    content_pnl.add(btn_pnl, BorderLayout.SOUTH);
                    _frmMovie.setContentPane(content_pnl);
                    _frmMovie.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent windowEvent) {
                            _frmMovie = null;//re-create on next click
                            _bPaused = false;
                        }
                    });
                    WidgetUtil.setFrameProperties(_frmMovie, new Dimension(1000, 700), false,
                        MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);//if don't use this, app won't finish
                }

                //use a thread to run loop and calculate reports
                Thread rpt_th = new Thread() {
                    public void run() {
                        //for each transaction file, generate report, let analyzer display
                        String sel = (String) _cmbReports.getSelectedItem();
                        ArrayList<String> trans_file_list = null;
                        try {
                            trans_file_list = ReportUtil.openTemplate(
                                FrameworkConstants.DATA_FOLDER_REPORT + "/" + sel);
                            for (String tran_file : trans_file_list) {//read each transaction file
                                final String tf = tran_file;
                                //get symbol from transaction file name
                                final SimReport rpt = genReport(tran_file);
                                if (rpt == null) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
//todo display error message here
                                        }
                                    });
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        if (_frmMovie == null)
                                            return;

                                        _frmMovie.setTitle(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rpt_msg_1") + tf);
                                        try {
                                            _pnlMovieAnalysis.showReport(rpt);
                                        } catch (Exception e) {
                                            e.printStackTrace();
//todo display error message here
                                        }
                                    }
                                });

                                Thread.sleep(3000);//wait 3 seconds before starting next one
                                while(_bPaused) {//sleep 1 second check again if still paused
                                    Thread.sleep(1000);
                                    System.out.print("..");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                rpt_th.start();
            }
        });
        west_pnl.add(Box.createHorizontalStrut(15));

        //search field
        west_pnl.add(_txtSearch);
        WidgetUtil.attachToolTip(_txtSearch, FrameworkConstants.FRAMEWORK_BUNDLE.getString("search_1"),
                SwingConstants.RIGHT, SwingConstants.TOP);
        _txtSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String txt = _txtSearch.getText().toUpperCase();
                if (txt.length() == 0)
                    return;
                findSymbol(txt);
            }
        });

        //center panel - summary
        JPanel cen_pnl = new JPanel();  cen_pnl.setOpaque(false);
        cen_pnl.add(_lblSummary); _lblSummary.setFont(Constants.FONT_BOLD);

        //east - trade detail, export, show/hide columns
        JPanel east_pnl = new JPanel();  east_pnl.setOpaque(false);
        east_pnl.add(_btnDetail); _btnDetail.setEnabled(false);
        _btnDetail.setDisabledIcon(new DisabledIcon(FrameworkIcon.PRICE_CHART.getImage()));
        _btnDetail.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TradeDetailDialog dialog = TradeDetailDialog.getInstance();
                dialog.setVisible(true);
                try {
                    dialog.refreshGraph(findReportByIndex(_tblReport.getSelectedRow()));
                } catch (ParseException e1) {
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                } catch (IOException e1) {
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                }
            }
        });
        east_pnl.add(Box.createGlue());
        east_pnl.add(_btnExport);
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                FileUtil.exportSheet(_ReportTableModel, new File(FrameworkConstants.DATA_FOLDER_EXPORT));
//                try {
//                    exportReports();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });
        east_pnl.add(Box.createGlue());
        JLabel showhide_btn = new JLabel(LazyIcon.TABLE_COLUMN_OP);
        east_pnl.add(showhide_btn, BorderLayout.EAST);
        WidgetUtil.attachToolTip(showhide_btn, FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_1"),
            SwingConstants.RIGHT, SwingConstants.TOP);

        showhide_btn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mev) {//to show / hide columns
                Image image = LazyIcon.APP_ICON.getImage();
                //gather column names from schema
                String[] column_names = new String[ReportTableModel.TABLE_SCHEMA.length];
                for (int row = 0; row < ReportTableModel.TABLE_SCHEMA.length; row++)
                    column_names[row] = (String)ReportTableModel.TABLE_SCHEMA[row][0];
                SchemaColumnDialog dlg = new SchemaColumnDialog(_tblReport, column_names,
                    MdiMainFrame.getInstance(), image, LOCKED_COLUMNS);
                dlg.setVisibleColumns(ApolloPreferenceStore.getPreferences().getCustomReportColumnVisible());
                dlg.setVisible(true);
                boolean[] visible_columns = dlg.getResult();
                if (null != visible_columns) {
                    ApolloPreferenceStore.getPreferences().setCustomReportColumnVisible(visible_columns);
                    ApolloPreferenceStore.savePreferences();
                    TableUtil.setColumnsVisible(_tblReport, visible_columns);
                }
            }
        });
        add(WidgetUtil.createTitleStrip(//new JLabel("  " + FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_run_report")),
            west_pnl, cen_pnl, east_pnl), BorderLayout.NORTH);

        //report table
        _tblReport = WidgetUtil.createDynaTable(_ReportTableModel = new ReportTableModel(), ListSelectionModel.SINGLE_SELECTION,
            new SortHeaderRenderer(), true, new ReportRenderer());
        _tblReport.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblReport.setAutoCreateRowSorter(true);
        _tblReport.setOpaque(false);
        _tblReport.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //refresh graph use cached SimReport
                int sel = _tblReport.getSelectedRow();
                _btnDetail.setEnabled(sel != -1);
                if (sel == -1) //no selection
                    return;

                SimReport rpt = findReportByIndex(sel);
                try {
                    TradeDetailDialog dlg = TradeDetailDialog.getInstance();
                    if (dlg.isVisible())
                        dlg.refreshGraph(rpt);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                } catch (SeriesException e1) {
                    e1.printStackTrace();
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                }
            }
        });
        JScrollPane scr = new JScrollPane(_tblReport); scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
        TableUtil.fixColumns(scr, _tblReport, LOCKED_COLUMNS);

        //column order preference handler
        _tblReport.getColumnModel().addColumnModelListener(new TableUtil.TableColumnModelAdapter() {
            public void columnMoved(TableColumnModelEvent ev) {
                if (TableUtil.columnMoved(_tblReport, ev.getFromIndex(), ev.getToIndex())) {
                    ApolloPreferenceStore.getPreferences().setCustomReportColumnOrder(TableUtil.getColumnOrder(_tblReport, LOCKED_COLUMNS));
                    ApolloPreferenceStore.savePreferences();
                }
            }
        });
        initReportList();

        //initialze column preference
        boolean[] visible = ApolloPreferenceStore.getPreferences().getCustomReportColumnVisible();
        if (null != visible)
            TableUtil.setColumnsVisible(_tblReport, visible);
        int[] order = ApolloPreferenceStore.getPreferences().getCustomReportColumnOrder();
        if (null != order)
            TableUtil.setColumnOrder(_tblReport, order);
    }

    /**
     * When user clicks this tab, refresh file list in case add/delete took place prior.
     */
    public void initReportList() {
        File template_folder = new File(FrameworkConstants.DATA_FOLDER_REPORT);
        String[] file_list = template_folder.list(); if (file_list == null) return;
        _Model.removeAllElements();
        for (String f : file_list) {
            if (f.startsWith(".")) continue;//skip hidden files
            if (!f.endsWith(FrameworkConstants.EXTENSION_REPORTS)) continue; //skip wrong extension
            _Model.addElement(f);
        }
    }

    //necessary for app to finish
    public void closeMovie() {
        if (_frmMovie != null)
            _frmMovie.dispose();
    }

    //----- protected methods -----
    void setCurrentFile(String file) {
        _cmbReports.setSelectedItem(file);
    }

    //----- private methods -----
    //do this in the background to show progress bar
    private void runReport() {
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
        pb.setVisible(true);

        //start a thread to simulate trades
        Thread thread = new Thread() {
            public void run() {
                _ReportTableModel.clear();

                //open selected report template
                int counter = 0;
                String sel = (String) _cmbReports.getSelectedItem();
                ArrayList<String> trans_file_list = null;
                try {
                    trans_file_list = ReportUtil.openTemplate(FrameworkConstants.DATA_FOLDER_REPORT + "/" + sel);
                } catch (IOException e) {
                    MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_7") + " " + sel);
                    e.printStackTrace();
                    return;
                }

                //read each transaction file, extract transactions, perform analysis
                ArrayList<String> err_msgs = new ArrayList<String>();
                for (String tran_file : trans_file_list) {
                    //get symbol from transaction file name
                    int start_idx = tran_file.lastIndexOf("/");//file name start
                    int end_idx = tran_file.lastIndexOf(".");//extension start
                    final String tf = tran_file;

                    //not enough data
                    if (start_idx == -1 || end_idx == -1) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_3") + " " + tf + " " +
                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_4"));
                                pb.setVisible(false);
                            }
                        });
                        err_msgs.add(tran_file + ": " + FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_8"));
                        continue;
                    }

                    //extracting transactions for different type of file format
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("rrpt_msg_1") + " " + tf + "...");
                        }
                    });
                    String sym = tran_file.substring(start_idx + 1, end_idx);
                    _Engine.setSymbol(sym);
                    int file_type = FrameworkConstants.FILE_FORMAT_CUSTOM;

                    //Extract transations from file based on name
                    ArrayList<Transaction> translog = null;
                    if (tran_file.contains("/MDB/")) {
//                            file_type = FrameworkConstants.FILE_FORMAT_MDB;
//                            for (int i = 0; i < AnalysisPanel.LIST_MDB_SEGMENTS.length; i++) {
//                                _Engine.setSegment(AnalysisPanel.LIST_MDB_SEGMENTS[i]);
//                                translog = _Engine.extractLog(
//                                        FrameworkConstants.DATA_FOLDER + "/" + tran_file, file_type);
//                                if (translog == null) {
//                                    EventQueue.invokeLater(new Runnable() {
//                                        public void run() {
//                                            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_3") + " " + tf + " " +
//                                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_5"));
//                                        }
//                                    });
//                                    break;
//                                }
//                                else {//log is not empty
//                                    SimReport rpt = _Engine.genReport();
//                                    _ReportTableModel.addRow(
//                                        String.valueOf(i + 1),
//                                        _Engine.getSymbol() + ": " + _Engine.getSegment(),
//                                        "Million Dollar Blueprint Strategy", rpt);
//                                    _ReportTableModel.fireTableDataChanged();
//                                }
//                            }
//                            continue;
                    }
                    else if (tran_file.contains("/SMT/"))
                        file_type = FrameworkConstants.FILE_FORMAT_SMT;
                    else if (tran_file.contains("/TL/"))
                        file_type = FrameworkConstants.FILE_FORMAT_TEA_LEAF;
                    else if (tran_file.contains("MacOsc"))
                        file_type = FrameworkConstants.FILE_FORMAT_CUSTOM;
                    else
                        file_type = FrameworkConstants.FILE_FORMAT_CUSTOM;//default format
                    try {
                        translog = _Engine.extractLog(FrameworkConstants.DATA_FOLDER + "/" + tran_file, file_type);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        err_msgs.add(tran_file + ": " + FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_9"));
                        continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                        err_msgs.add(tran_file + ": " + FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_10"));
                        continue;
                    }

                    //no transcations
                    if (translog == null) {
                        MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_3") + " " + tran_file + " " +
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_5"));
                        err_msgs.add(tran_file + ": " + FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_11"));
                        continue;
                    }

System.out.println("--- Reporting ===> " + sym);
                    SimReport rpt = _Engine.genReport();
                    //find out first year of all transactions
//                    String yr = translog.get(0).getEntryDate().substring(0, 4);
                    //update table model to show on screen
                    if (tran_file.contains("/SMT/"))
                        _ReportTableModel.addRow(
                            String.valueOf(counter + 1),
                            _Engine.getSymbol(),
                            //"SMT", + _Engine.getSymbol() + " (" + yr + ")",
                            "Stock Market Timing Strategy",
                            _Engine.getStrategyInfo(),
                            rpt);//strategy: symbol (start year) ///todo put into bundle
                    else if (tran_file.contains("/TL/"))
                        _ReportTableModel.addRow(
                            String.valueOf(counter + 1),
                            _Engine.getSymbol(),// + " (" + yr + ")",
                            _Engine.getStrategyInfo(),
                            "Tea Leaf Portfolio Strategy", rpt);
                    else if (tran_file.contains("MacOsc")) {
                        _ReportTableModel.addRow(
                            String.valueOf(counter + 1),
                            _Engine.getSymbol(),// + " (" + yr + ")",
                            _Engine.getStrategy(), // + ": " + _Engine.getSymbol() + " (" + yr + ")",
                            _Engine.getStrategyInfo(), //+ " (" + yr + ")",
                            rpt);
                        _SimReports.put(_Engine.getSymbol() + "_" + _Engine.getStrategy(), rpt);//key is unique
                    }
                    else //CLEC format, custom format
                        _ReportTableModel.addRow(
                            String.valueOf(counter + 1), _Engine.getSymbol(),
                            _Engine.getStrategy(), // + ": " + _Engine.getSymbol() + " (" + yr + ")",
                            _Engine.getStrategyInfo(), rpt);
                    counter++;
                }

                //processing completed, refresh table, show outperformance, pop up error if necessary
                final ArrayList<String> errs = err_msgs;
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setVisible(false);
                        _ReportTableModel.fireTableDataChanged();
                        _lblSummary.setText("MacOsc2 Outperform = " + FrameworkConstants.PCT_FORMAT.format(
                            _ReportTableModel.calcOutperformPct("MACOSC2", "MAC")) );

                        //show all error messages
                        if (errs.size() > 0) {
                            StringBuilder buf = new StringBuilder("<html>");
                            for (String err: errs)
                                buf.append(err).append("<br>");
                            MessageBox.messageBox(ScannerModel.getInstance().getParent(),
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                buf.toString(), MessageBox.STYLE_OK,
                                MessageBox.WARNING_MESSAGE);
                        }
                    }
                });
            }
        };
        thread.start();

    }

    //find which row and scroll into view
    private void findSymbol(String symbol) {
        int row = _ReportTableModel.findSymbol(symbol);
        if (row < 0) {
            MessageBox.messageBox(ScannerModel.getInstance().getParent(),
                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                    ApolloConstants.APOLLO_BUNDLE.getString("scan_msg_5"), MessageBox.STYLE_OK,
                    MessageBox.WARNING_MESSAGE);
            return;
        }
        WidgetUtil.scrollCellVisible(_tblReport, row, ReportTableModel.COLUMN_SYMBOL);
        _tblReport.getSelectionModel().setSelectionInterval(row, row);
        _txtSearch.setSelectionStart(0);
        _txtSearch.setSelectionEnd(symbol.length());
    }

    private SimReport findReportByIndex(int row_index) {
        int sel = _tblReport.convertRowIndexToModel(row_index);
        String sym = (String)_ReportTableModel.getCell(sel, ReportTableModel.COLUMN_SYMBOL).getValue();
        String strategy = (String)_ReportTableModel.getCell(sel, ReportTableModel.COLUMN_STRATEGY).getValue();
        return _SimReports.get(sym + "_" + strategy);
    }

    //-----inner classes-----
    private class ReportRenderer extends DynaTableCellRenderer {
        private ReportRenderer() {
            super(_ReportTableModel, null);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            int modle_column = table.convertColumnIndexToModel(col);//after dragging
            int model_row = table.convertRowIndexToModel(row);
            JLabel comp = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            comp.setToolTipText("");
            switch (modle_column) {
                case  ReportTableModel.COLUMN_ID://use last column strategy info as tooltip
                    comp.setToolTipText(
                        "<html>" + _ReportTableModel.getCell(model_row, ReportTableModel.COLUMN_ID).getValue() +
                        "<br>" + _ReportTableModel.getCell(model_row, ReportTableModel.COLUMN_INFO).getValue());
                    break;

                case  ReportTableModel.COLUMN_CAGR:
                case  ReportTableModel.COLUMN_ROI:
                case  ReportTableModel.COLUMN_ANNUAL:
                case  ReportTableModel.COLUMN_WIN_RATIO:
                case  ReportTableModel.COLUMN_IN_MKT:
                case  ReportTableModel.COLUMN_AVG_GAIN_PCT:
                case  ReportTableModel.COLUMN_AVG_LOSS_PCT:
                case  ReportTableModel.COLUMN_AVG_DD_PCT:
                case  ReportTableModel.COLUMN_MAX_GAIN_PCT:
                case  ReportTableModel.COLUMN_MAX_LOSS_PCT:
                case  ReportTableModel.COLUMN_MAX_DD_PCT:
                case  ReportTableModel.COLUMN_MIN_GAIN_PCT:
                case  ReportTableModel.COLUMN_MIN_LOSS_PCT:
                case  ReportTableModel.COLUMN_MIN_DD_PCT:
                    comp.setText(FrameworkConstants.ROI_FORMAT.format(value));
                    break;

                case  ReportTableModel.COLUMN_PF:
                    comp.setText(FrameworkConstants.PRICE_FORMAT.format(value));
                    break;

                case  ReportTableModel.COLUMN_END_EQUITY:
                case  ReportTableModel.COLUMN_NET_GAIN:
                case  ReportTableModel.COLUMN_NET_LOSS:
                case  ReportTableModel.COLUMN_AVG_GAIN:
                case  ReportTableModel.COLUMN_AVG_LOSS:
                case  ReportTableModel.COLUMN_AVG_DD:
                case  ReportTableModel.COLUMN_MAX_GAIN:
                case  ReportTableModel.COLUMN_MAX_LOSS:
                case  ReportTableModel.COLUMN_MAX_DD:
                case  ReportTableModel.COLUMN_MIN_GAIN:
                case  ReportTableModel.COLUMN_MIN_LOSS:
                case  ReportTableModel.COLUMN_MIN_DD:
                    comp.setText(FrameworkConstants.DOLLAR_FORMAT.format(value));
                    break;
            }
            return comp;
        }
    }

    //-----private methods-----
    //run one report used by both run button and movie button
    // return null when error occurs, caller should stop
    private SimReport genReport(String tran_file) throws ParseException, IOException {
        int start_idx = tran_file.lastIndexOf("/");//file name start
        int end_idx = tran_file.lastIndexOf(".");//extension start
        if (start_idx == -1 || end_idx == -1) {
            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_3") + " " + tran_file + " " +
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_4"));
            return null;//todo return some error string inside SimReport instead
        }
        String sym = tran_file.substring(start_idx + 1, end_idx);
        _Engine.setSymbol(sym);
        int file_type;
        ArrayList<Transaction> translog;
        //MDB - generate one row for each segment
        if (tran_file.contains("/MDB/")) {
            file_type = FrameworkConstants.FILE_FORMAT_MDB;
            for (int i = 0; i < AnalysisPanel.LIST_MDB_SEGMENTS.length; i++) {
                _Engine.setSegment(AnalysisPanel.LIST_MDB_SEGMENTS[i]);
                translog = _Engine.extractLog(
                    FrameworkConstants.DATA_FOLDER + "/" + tran_file, file_type);
                if (translog == null) {
                    MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_3") + " " + tran_file + " " +
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_5"));
                    return null;//todo return some error string inside SimReport instead
                }
                else //log is not empty
                    return _Engine.genReport();//todo this should not stop, should show segment and not stop
            }
            return null;//todo this is not correct for MDB....
        }
        else if (tran_file.contains("/SMT/"))
            file_type = FrameworkConstants.FILE_FORMAT_SMT;
        else if (tran_file.contains("/TL/"))
            file_type = FrameworkConstants.FILE_FORMAT_TEA_LEAF;
        else
            file_type = FrameworkConstants.FILE_FORMAT_CLEC;
        translog = _Engine.extractLog(
            FrameworkConstants.DATA_FOLDER + "/" + tran_file, file_type);
        if (translog == null) {
            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_3") + " " + tran_file + " " +
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_5"));
            return null;//todo return some error string inside SimReport instead
        }
        return _Engine.genReport();
    }

    private void exportReports() throws IOException {
        //ask user for file name and rsp
        JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_REPORT));
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
            pw.println("NAME=" + _cmbReports.getSelectedItem());
            pw.println("Strategy, CAGR, ROI, Annual %, #Trade, Trades/Year, Win Ratio, PF, In Market%, " +
                    "End Equity, Net Gain$, Net Loss$, Avg Gain%, Avg Loss%, Avg DD%, Max Gain%, Max Loss%, " +
                    "Max DD%, Min Gain%, Min Loss%, Min DD%, Avg Gain$, Avg Loss$, Avg DD$, Max Gain$, Max Loss$, " +
                    "Max DD$, Min Gain$, Min Loss$, Min DD$, Strategy Detail");
            int row_cnt = _ReportTableModel.getRowCount();
            for (int row=0; row<row_cnt; row++) {
                SimpleCell[] cells = _ReportTableModel.getRow(row);
                StringBuilder sb = new StringBuilder();
                for (SimpleCell cell : cells)
                    sb.append(cell.getValue()).append(",");
                pw.println(sb.toString());
            }
            pw.flush();
            pw.close();
        }
    }

    //-----instance variables-----
    private JComboBox _cmbReports;
    private DefaultComboBoxModel _Model = new DefaultComboBoxModel();
    private JButton _btnRunReport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_run_report"), FrameworkIcon.RUN);
    private JButton _btnMovie = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_play_movie"), FrameworkIcon.PLAY_MOVIE);
    private JButton _btnDetail = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("runrpt_tip_2"), FrameworkIcon.PRICE_CHART);
    private JButton _btnExport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_5"), FrameworkIcon.EXPORT);
    private JTextField _txtSearch = new JTextField(5);
    private JLabel _lblSummary = new JLabel();
    private JTable _tblReport;
    private ReportTableModel _ReportTableModel;
    private AnalysisEngine _Engine = new AnalysisEngine();
    private JFrame _frmMovie;
    private AnalysisPanel _pnlMovieAnalysis;//inside movie frame
    private boolean _bPaused;
    private JButton _btnPauseResume = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_pause"), FrameworkIcon.PAUSE_MOVIE);
    private HashMap<String, SimReport> _SimReports = new HashMap<>();//for graphing

    //-----literals-----
    private static final int LOCKED_COLUMNS = 3;//first 2 columns are locked, ie.they cannot be hidden  todo.............
}
