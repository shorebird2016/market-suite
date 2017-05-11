package org.marketsuite.scanner.tracking;

import org.marketsuite.component.Constants;
import org.marketsuite.component.comparator.YahooDateComparator;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.Divergence;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.marketsuite.component.comparator.YahooDateComparator;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//Relative performance of various time periods. eg. 2weeks, 3 months..etc
public class TrackerPanel extends JPanel {
    //CTOR - use card layout for two panels, one is strategy simulator, another is scanner
    public TrackerPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new BevelBorder(BevelBorder.LOWERED));

        //north - title strip with group selector, start date picker, setting button, run button, export button
        JPanel west_pnl = new JPanel(); west_pnl.setOpaque(false);
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_05"));
        lbl.setFont(Constants.LINK_FONT_BOLD);
        west_pnl.add(lbl);
        west_pnl.add(_txtStartDate); west_pnl.add(Box.createGlue());
//TODO: tooltip doesn't work for date picker
        WidgetUtil.attachToolTip(_txtStartDate, ApolloConstants.APOLLO_BUNDLE.getString("trk_06"),
                SwingConstants.RIGHT, SwingConstants.BOTTOM);

        //settings and scan buttons
        west_pnl.add(_chkShowAll);  _chkShowAll.setOpaque(false); west_pnl.add(Box.createHorizontalStrut(10));
        west_pnl.add(_btnOptions); west_pnl.add(Box.createGlue()); _btnOptions.setEnabled(false);
        _btnOptions.setDisabledIcon(new DisabledIcon(FrameworkIcon.SETTING.getImage()));
        _btnOptions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _dlgOption.setVisible(true);
                if (_dlgOption.isCancelled()) return;
                _Options = _dlgOption.getTrackerOption();
            }
        });

        //scan button
        west_pnl.add(_btnScan); west_pnl.add(Box.createGlue()); _btnScan.setEnabled(false);
        _btnScan.setDisabledIcon(new DisabledIcon(FrameworkIcon.RUN.getImage()));
        _btnScan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //skip empty group list
                int count = 0;
                for (JCheckBox chk : _chkGroups) {
                    if (chk.isSelected())
                        count++;
                }
                if (count == 1)//single watchlist, allow show all
                    scan();
                else if (count > 1) {
                    if (!_chkShowAll.isSelected())
                        scan();//launch thread, only show ones with signal
                    else {//disallow show all for multiple
                        MessageBox.messageBox(MdiMainFrame.getInstance(),
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("trk_25"),
                            MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    }
                }
            }
        });

        //east of title strip - create watch list and export buttons
        JPanel east_pnl = new JPanel(); east_pnl.setOpaque(false);

        //remove row button
        east_pnl.add(_btnRemoveRow); east_pnl.add(Box.createGlue());
        _btnRemoveRow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int sel[] = _tblTracker.getSelectedRows();
                if (sel == null) return;
                for (int i = 0; i < sel.length; i++) {//convert to real model index
                    int model_row = _tblTracker.convertRowIndexToModel(sel[i]);
                    sel[i] = model_row;
                }
                _TableModel.setSelectedRows(sel);
                _TableModel.delete();

                //remove also from watch list array too
                for (int i = 0; i < sel.length; i++) {
                    _WatchListNames.remove(sel[i]);
                    _MarketInfoList.remove(sel[i]);
                }
            }
        });
        //create watch list button
        east_pnl.add(_btnGenWatchList); east_pnl.add(Box.createGlue()); _btnGenWatchList.setEnabled(false);
        _btnGenWatchList.setDisabledIcon(new DisabledIcon(FrameworkIcon.WATCH.getImage()));
        _btnGenWatchList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //compile symbol list
                if (_MarketInfoList == null || _MarketInfoList.size() == 0) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                        Constants.COMPONENT_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("trk_14"),
                        MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    return;
                }

                //ask name
                NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                if (dlg.isCancelled())
                    return;
                String name = dlg.getEntry();

                //check duplicate list name
                if (GroupStore.getInstance().isGroupExist(name)) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                        Constants.COMPONENT_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("trk_15"),
                        MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    return;
                }

                //collect symbols, remove duplicate
                ArrayList<String> list = new ArrayList<>();
                for (MarketInfo mki : _MarketInfoList) {
                    String symbol = mki.getSymbol();
                    if (!list.contains(symbol))
                        list.add(symbol);
                }
                GroupStore.getInstance().addGroup(name, list);//empty group persist
                Props.WatchListsChange.setChanged();//notify watch list manager
            }
        });

        //export
        east_pnl.add(_btnExport); _btnExport.setEnabled(false);
        _btnExport.setDisabledIcon(new DisabledIcon(FrameworkIcon.EXPORT.getImage()));
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileUtil.exportSheet(_TableModel, new File(FrameworkConstants.DATA_FOLDER_EXPORT));
            }
        });
        add(WidgetUtil.createTitleStrip(west_pnl, null, east_pnl), BorderLayout.NORTH);

        //center - group selector and result table
        _splMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        _splMain.setContinuousLayout(true);
        int pos = ApolloPreferenceStore.getPreferences().getTrackerSplitterPosition();
        if (pos > 0)
            _splMain.setDividerLocation(pos);
        else
            _splMain.setDividerLocation(DEFAULT_POSITION);
        _splMain.setLeftComponent(createNavPane());
        _splMain.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String prop = evt.getPropertyName();
                if (prop.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
                    int cur_loc = _splMain.getDividerLocation();
                    ApolloPreferenceStore.getPreferences().setTrackerSplitterPosition(cur_loc);
                    ApolloPreferenceStore.savePreferences();
                }
            }
        });

        //right side - table
        _TableModel = new TrackerTableModel();
        _tblTracker = new JTable(_TableModel);
        WidgetUtil.initDynaTable(_tblTracker, _TableModel, ListSelectionModel.SINGLE_SELECTION, new SortHeaderRenderer(), true, null);
        _tblTracker.setOpaque(false);
//        _tblTracker.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        _tblTracker.setAutoCreateRowSorter(true);
        TableRowSorter sorter = (TableRowSorter)_tblTracker.getRowSorter();
        YahooDateComparator comparator = new YahooDateComparator();
        sorter.setComparator(TrackerTableModel.COLUMN_DVG_START, comparator);
        sorter.setComparator(TrackerTableModel.COLUMN_DVG_END, comparator);
        sorter.setComparator(TrackerTableModel.COLUMN_10x30, comparator);
        sorter.setComparator(TrackerTableModel.COLUMN_50x120, comparator);
//        MaCellRenderer mar = new MaCellRenderer();
//        _tblTracker.getColumnModel().getColumn(TrackerTableModel.COLUMN_NEAR_10SMA).setCellRenderer(mar);
//        _tblTracker.getColumnModel().getColumn(TrackerTableModel.COLUMN_NEAR_30SMA).setCellRenderer(mar);
//        _tblTracker.getColumnModel().getColumn(TrackerTableModel.COLUMN_NEAR_50SMA).setCellRenderer(mar);
//        _tblTracker.getColumnModel().getColumn(TrackerTableModel.COLUMN_NEAR_200SMA).setCellRenderer(mar);

        //handle row selection to display price graph via MarketInfo object
        _tblTracker.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                //nothing selected, disable delete, close button
                int row = _tblTracker.getSelectedRow();
                if (row == -1) {//de-selection, clear graph
                    Props.MarketInfoChange.setValue(null);
                    return;
                }

                //draw graph
                row = _tblTracker.convertRowIndexToModel(row);
                Props.MarketInfoChange.setValue(_MarketInfoList.get(row));
            }
        });
        JScrollPane scr = new JScrollPane(_tblTracker);
        scr.getViewport().setOpaque(false);
        TableUtil.fixColumns(scr, _tblTracker, LOCKED_COLUMNS);
        _splMain.setRightComponent(scr);
        add(_splMain, BorderLayout.CENTER);

        //set start date to past Monday
        Calendar cal = AppUtil.calcPastMonday(Calendar.getInstance());
        Date past_monday = cal.getTime();
        _txtStartDate.setDate(past_monday);
    }

    //----- public methods -----
    public void findSymbol(String symbol) {
        if (symbol.equals(""))  return;//de-select sends empty string

        //find which row and scroll into view
        int row = _TableModel.findSymbol(symbol);
        if (row < 0) {
            MessageBox.messageBox(MdiMainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                ApolloConstants.APOLLO_BUNDLE.getString("trk_01"), MessageBox.STYLE_OK,
                MessageBox.WARNING_MESSAGE);
            return;
        }
        row = _tblTracker.convertRowIndexToView(row);
        WidgetUtil.scrollCellVisible(_tblTracker, row, TrackerTableModel.COLUMN_SYMBOL);
        _tblTracker.getSelectionModel().setSelectionInterval(row, row);

    }

    public void layoutNavPane() {
        _splMain.setLeftComponent(createNavPane());
        _TableModel.clear();
        _splMain.repaint();
    }

    //----- private methods -----
    //vertical list of checkboxes, one for each group built from GroupStore
    private JPanel createNavPane() {
        JPanel ret = new JPanel(new BorderLayout());  ret.setOpaque(false);
        JPanel btn_pnl = new JPanel();  btn_pnl.setOpaque(false);
//        btn_pnl.add(_btnOpen);  btn_pnl.add(Box.createGlue());
//        btn_pnl.add(_btnSave);  btn_pnl.add(Box.createGlue());
        btn_pnl.add(_btnSelectAll);  btn_pnl.add(Box.createGlue());
        _btnSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkGroups)
                    chk.setSelected(true);
                _btnOptions.setEnabled(true);
                _btnScan.setEnabled(true);
            }
        });
        btn_pnl.add(_btnSelectNone);
        _btnSelectNone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkGroups)
                    chk.setSelected(false);
                _btnOptions.setEnabled(false);
                _btnScan.setEnabled(false);
            }
        });
        ret.add(WidgetUtil.createTitleStrip("Select Groups", btn_pnl), BorderLayout.NORTH);

        //use MigLayout to put down many checkboxes
        MigLayout layout = new MigLayout("insets 2 5 2 5,flowy, gapy 0");
        JPanel box = new JPanel(layout);
        ArrayList<String> names = GroupStore.getInstance().getGroupNames();
        final int size = names.size();
        _chkGroups = new JCheckBox[size];
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //are all checkboxes unchecked?
                boolean all_clear = true;
                for (JCheckBox chk : _chkGroups) {
                    if (chk.isSelected()) {
                        all_clear = false;
                        break;
                    }
                }
                _btnOptions.setEnabled(!all_clear);
                _btnScan.setEnabled(!all_clear);
            }
        };
        for (int i = 0; i < size; i++) {
            _chkGroups[i] = new JCheckBox(names.get(i));
            _chkGroups[i].setFocusable(false);
            box.add(_chkGroups[i]);
            _chkGroups[i].addActionListener(listener);
        }
        JScrollPane jsp = new JScrollPane(box);
        jsp.getVerticalScrollBar().setUnitIncrement(_chkGroups[0].getPreferredSize().height);
        ret.add(jsp, BorderLayout.CENTER);
        return ret;
    }

    private void scan() {
        //show initial progress bar
        _btnGenWatchList.setEnabled(false);
        _btnExport.setEnabled(false);
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(),
            ApolloConstants.APOLLO_BUNDLE.getString("trk_07"));
        pb.setVisible(true);

        //get a list of picked groups
        final ArrayList<String> group_list = new ArrayList<>();
        for (JCheckBox chk : _chkGroups) {
            if (chk.isSelected())
                group_list.add(chk.getText());
        }
        Date start_date = _txtStartDate.getDate();
        final Calendar start_cal = Calendar.getInstance();
        start_cal.setTime(start_date);
        final boolean show_all = _chkShowAll.isSelected();//avoid EDT in thread

        //scan inside a thread
        Thread scan_thread = new Thread() {
            public void run() {
                final ArrayList<LogMessage> failed_msgs = new ArrayList<>();//might fail, keep a list of errors
                _MarketInfoList = new ArrayList<>();
                _WatchListNames = new ArrayList<>();
                for (String group : group_list) {
                    ArrayList<MarketInfo> grp_mki = applyFilter(group, start_cal, show_all, failed_msgs);

                    //append result to list
                    for (MarketInfo m : grp_mki)
                        _MarketInfoList.add(m);

                    //update progress bar
                    final String name = group;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("trk_08") + " " + name);
                        }
                    });
                }

                //send to EDT, display in table
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        /*ArrayList<String> msgs = */_TableModel.populate(_MarketInfoList, _txtStartDate.getDate(), _WatchListNames, _Options);
                        if (failed_msgs.size() > 0) {
                            Props.Log.setValue(null, failed_msgs);
                        }
                        if (_MarketInfoList.size() > 0) {
                            _btnGenWatchList.setEnabled(true);
                            _btnExport.setEnabled(true);
                        }
                        pb.setVisible(false);
                    }
                });
            }
        };
        scan_thread.start();
    }

    //quick filter to remove symbols w/o information from a group
    private ArrayList<MarketInfo> applyFilter(String group_name, Calendar start_cal, boolean show_all, ArrayList<LogMessage> failed_msgs) {
        ArrayList<MarketInfo> ret = new ArrayList<>();
        ArrayList<String> members = GroupStore.getInstance().getMembers(group_name);
        for (String symbol : members) {
            MarketInfo mki;
            try {
                mki = MarketUtil.calcMarketInfo(symbol,
                        FrameworkConstants.MARKET_QUOTE_LENGTH, _Options.getDvgOption());
            } catch (IOException e) {
                LogMessage lm = new LogMessage(LoggingSource.SCANNER_STRATEGY, symbol + ": Fail to read quotes.", e);
                failed_msgs.add(lm);
                continue;
            } catch (ParseException e) {
                LogMessage lm = new LogMessage(LoggingSource.SCANNER_STRATEGY, symbol + ": " + e.getMessage(), e);
                failed_msgs.add(lm);
                continue;
            }
            if (mki == null)
                continue;//skip

            //handle show all case even row is blank
            if (show_all) {
                ret.add(mki); _WatchListNames.add(group_name);
                continue;
            }

            //check price/volume filter
            float close = mki.getFund().getQuote().get(0).getClose();
            if (close < _Options.getPriceThreshold()) {
                LogMessage lm = new LogMessage(LoggingSource.SCANNER_STRATEGY,
                    mki.getSymbol() + ":\t" + ApolloConstants.APOLLO_BUNDLE.getString("trk_38") + "\t$" + close, null);
                failed_msgs.add(lm);
                continue;
            }
            float avg_vol = IndicatorUtil.calcAverageVolume(mki.getFund(), 20);
            if (avg_vol < _Options.getAverageVolumeThreshold()) {
                LogMessage lm = new LogMessage(LoggingSource.SCANNER_STRATEGY,
                    mki.getSymbol() + ":\t" + ApolloConstants.APOLLO_BUNDLE.getString("trk_39") + "\t$" + close, null);
                failed_msgs.add(lm);
                continue;
            }

            //check DVG existence
            Divergence dvg = mki.getDvg();
            if (dvg != null) {
                ret.add(mki); _WatchListNames.add(group_name);
                continue;
            }

            //check 10x30 crossing
            ArrayList<String> cod = mki.getCrossOver10x30Dates();
            if (cod.size() > 0) {//filter out older crossings than specified in date picker
                String cross_date = cod.get(0);//only show the most recent

                //if 10x30 happened before start date, skip
                Calendar cal10x30 = AppUtil.stringToCalendarNoEx(cross_date);
                if (cal10x30.compareTo(start_cal) >= 0) {
                    ret.add(mki); _WatchListNames.add(group_name);
                    continue;
                }
            }

            //check 50x120 crossing
            cod = mki.getCrossOver50x120Dates();
            if (cod.size() > 0) {
                String cross_date = cod.get(0);

                //if 50x120 happened before start date, skip
                Calendar cal50x120 = AppUtil.stringToCalendarNoEx(cross_date);
                if (cal50x120.compareTo(start_cal) >= 0) {
                    ret.add(mki); _WatchListNames.add(group_name);
                    continue;
                }
            }
        }
        return ret;
    }

    //----- inner classes -----
//    private class MaCellRenderer extends DynaTableCellRenderer {
//        private MaCellRenderer() {
//            super(_TableModel);
//        }
//
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//            int model_col = _tblTracker.convertColumnIndexToModel(column);
//
//            //render percent near xMA with formatter
//            if (model_col >= TrackerTableModel.COLUMN_NEAR_10SMA && model_col <= TrackerTableModel.COLUMN_NEAR_200SMA) {
//                double v = (Double)value;
//                lbl.setText(FrameworkConstants.PCT_FORMAT.format(v));
//            }
//            return lbl;
//        }
//    }

    //-----instance variables-----
    private JTable _tblTracker;
    private JSplitPane _splMain;
    private TrackerTableModel _TableModel;
    private ArrayList<MarketInfo> _MarketInfoList;//use same model index as _WatchListNames
    private ArrayList<String> _WatchListNames;
    private JXDatePicker _txtStartDate = new JXDatePicker();
    private JCheckBox _chkShowAll = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("trk_26"));
    private JButton _btnOptions = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_02"), FrameworkIcon.SETTING);
    private JButton _btnScan = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_03"), FrameworkIcon.RUN);
    private JButton _btnRemoveRow = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_33"), LazyIcon.MINUS_SIGN);
    private JButton _btnGenWatchList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_13"), FrameworkIcon.WATCH);
    private JButton _btnExport = WidgetUtil.createIconButton(Constants.COMPONENT_BUNDLE.getString("exp_05"), FrameworkIcon.EXPORT);
    private JCheckBox[] _chkGroups;
    private JButton _btnSelectAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_09"), FrameworkIcon.SELECT_ALL);
    private JButton _btnSelectNone = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_10"), FrameworkIcon.CLEAR);
//    private JButton _btnOpen = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_11"), FrameworkIcon.FILE_OPEN);
//    private JButton _btnSave = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_12"), FrameworkIcon.FILE_SAVE);
    private TrackerOption _Options = new TrackerOption();
    private TrackerOptionDialog _dlgOption = new TrackerOptionDialog();

    //-----literals-----
    private static final int DEFAULT_POSITION = 200;
    private static final int LOCKED_COLUMNS = 1;
}