package org.marketsuite.main;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.NameDialog;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.datamgr.dataimport.PickWatchlistDialog;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.data.GapInfo;
import org.marketsuite.framework.model.indicator.Ichimoku;
import org.marketsuite.framework.model.type.CalendarQuarter;
import org.marketsuite.framework.model.type.GapType;
import org.marketsuite.framework.model.type.ImportFileType;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.gap.GapUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloIcon;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.watchlist.mgr.FileTypeDialog;
import org.marketsuite.watchlist.mgr.MergeWatchListDialog;
import org.marketsuite.watchlist.mgr.ViewSymbolsDialog;
import org.marketsuite.watchlist.model.WatchListModel;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.gap.GapUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * A container for managing watch lists.
 */
public class WatchListMgrFrame extends JInternalFrame implements PropertyChangeListener {
    private static WatchListMgrFrame _Instance;
    public static WatchListMgrFrame getInstance() {
        if (_Instance == null)
            _Instance = new WatchListMgrFrame();
        return _Instance;
    }
    private WatchListMgrFrame() {
        super(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_1"), true, false, false, true);
        setFrameIcon(ApolloIcon.APP_ICON);
        JPanel content_pane = new JPanel(new MigLayout("insets 0"));
        JPanel cen_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0"));
        cen_pnl.add(_pnlListMgr = new ListMgrPanel(), "dock center");
        JPanel btn_pnl = new JPanel(new MigLayout("insets 0", "5[]10[]10[]10[]10[]10[]10[]push[]5", "3[]3"));

        //duplicate and merge buttons
        btn_pnl.add(_btnDuplicate); //_btnDuplicate.setEnabled(false);
        _btnDuplicate.setDisabledIcon(new DisabledIcon(FrameworkIcon.DUPLICATE.getImage()));
        _btnDuplicate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String new_name = _sCurrentGroup + "(Copy)";
                ArrayList<String> members = GroupStore.getInstance().getMembers(_sCurrentGroup);
                _sCurrentGroup = new_name;
                GroupStore.getInstance().addGroup(new_name, members);
                _pnlListMgr.populateGroups();//refresh view, sort automatically
                _lstGroup.setSelectedValue(new_name, true);
            }
        });

        //import symbols into a group
        btn_pnl.add(_btnImport);
        _btnImport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //ask user about file type
                FileTypeDialog dlg = new FileTypeDialog();
                if (dlg.isCancelled())
                    return;
                ImportFileType type = dlg.getFileType();
                String ds, ext;
                if (type.equals(ImportFileType.IBD50_XLS)) {//only this needs .xls
                    ds = ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_70");
                    ext = FrameworkConstants.EXTENSION_XLS;
                }
                else  {//others use .csv
                    ds = ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_71");
                    ext = FrameworkConstants.EXTENSION_CSV;
                }
                final String descr = ds;
                final String extension = ext;

                //open file dialog with .csv or .xls extension
                JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER));
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory())
                            return true;

                        //only allow .csv or .xls extension
                        int ext_pos = file.getName().lastIndexOf(extension);
                        if (ext_pos > 0)
                            return true;
                        return false;
                    }

                    public String getDescription() {//this shows up in description field of dialog
                        return descr;
                    }
                });
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(true);
                int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
                if (ret == JFileChooser.APPROVE_OPTION) {//user pick, only first one
                    File[] msel = fc.getSelectedFiles();
                    File sel = fc.getSelectedFile();

                    //open file, read first column of each line into array of strings
                    String name = sel.getName();
                    String prefix = name.substring(0, name.indexOf("."));
                    try {
                        GroupStore.getInstance().addGroup(_sCurrentGroup = prefix,
                                DataUtil.readSymbolToWatchList(msel, type));
                        _pnlListMgr.populateGroups();
                        _lstGroup.setSelectedValue(prefix, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.messageBox(
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                ApolloConstants.APOLLO_BUNDLE.getString("wl_msg_9") + name,
                                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING
                        );
                    }
                }
            }
        });

        //merge two or more together
        btn_pnl.add(_btnMergeWatchList); //_btnMergeWatchList.setEnabled(false);
        _btnMergeWatchList.setDisabledIcon(new DisabledIcon(FrameworkIcon.MERGE.getImage()));
        _btnMergeWatchList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MergeWatchListDialog dlg = new MergeWatchListDialog(MdiMainFrame.getInstance());
                ArrayList<String> names = dlg.getWatchListNames();
                String merged_name = dlg.getMergedName();
                ArrayList<String> merge_list = WatchListModel.mergeLists(names);
                GroupStore.getInstance().addGroup(merged_name, merge_list);
                _pnlListMgr.populateGroups();
            }
        });
        btn_pnl.add(_btnDownloadAll);
        _btnDownloadAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> dld_list = new ArrayList<>();
                ArrayList<String> all_groups = GroupStore.getInstance().getGroupNames();
                for (String grp : all_groups) {
                    ArrayList<String> members = GroupStore.getInstance().getMembers(grp);
                    for (String sym : members) {
                        boolean exist = FileUtil.isSymbolExist(new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE), sym);
                        if (!exist)
                            dld_list.add(sym);
                    }
                }
                if (dld_list.size() == 0) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_7"),
                            MessageBox.OK_OPTION, MessageBox.IMAGE_QUESTION);
                    return;
                }

                //show this to user in a window
                StringBuilder buf = new StringBuilder();
                for (String str : dld_list)
                    buf.append(str).append("  ");
                String ms = WidgetUtil.getMultiLineString(buf);
                String msg = ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_6") + ms;
                if (WidgetUtil.confirmAction(MdiMainFrame.getInstance(), msg)) {
                    Props.ShowApp.setValue(null, DataMgrFrame.class);
                    Props.AddSymbols.setValue(null, dld_list);//informs listener to start download
                }
            }
        });

        //bulk delete watch lists
        btn_pnl.add(_btnBulkDelete);
        _btnBulkDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HashMap<String, ArrayList<String>> wl_map = GroupStore.getInstance().getGroups();
                Iterator<String> itor = wl_map.keySet().iterator();
                PickWatchlistDialog dlg = new PickWatchlistDialog(itor);
                if (!dlg.isCancelled()) {
                    List<String> watch_lists = dlg.getWatchlists();
                    for (String wl : watch_lists)
                        GroupStore.getInstance().removeGroup(wl);
                }
                _pnlListMgr.populateGroups();
            }
        });
        btn_pnl.add(_btnExportList);
        _btnExportList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HashMap<String, ArrayList<String>> wl_map = GroupStore.getInstance().getGroups();
                Iterator<String> itor = wl_map.keySet().iterator();
                PickWatchlistDialog dlg = new PickWatchlistDialog(itor);
                if (!dlg.isCancelled()) {
                    JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_EXPORT));//TODO maybe pref
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fc.setFileFilter(new FileFilter() {
                        public boolean accept(File file) {
                            if (file.isDirectory()) return true;
                            int pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_XML);
                            return (pos > 0);
                        }
                        public String getDescription() {//this shows up in description field of dialog
                            return "XML Files";
                        }
                    });
                    fc.setMultiSelectionEnabled(false);
                    fc.setAcceptAllFileFilterUsed(false);
                    fc.setDialogTitle(ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_9"));
                    int ret = fc.showSaveDialog(MdiMainFrame.getInstance());
                    if (ret != JFileChooser.APPROVE_OPTION) return;
                    File output_path = fc.getSelectedFile();
                    List<String> watch_lists = dlg.getWatchlists();
                    HashMap<String, ArrayList<String>> wmap = new HashMap<>();
                    for (String wl : watch_lists)
                        wmap.put(wl, GroupStore.getInstance().getMembers(wl));
                    GroupStore.saveGroups(wmap, output_path);
                }
            }
        });

        //generate gap groups
        if (MainModel.getInstance().isExpertUer()) {
            btn_pnl.add(_btnGap); _btnGap.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    HashMap<String, ArrayList<String>> wl_map = GroupStore.getInstance().getGroups();
                    GroupStore.getInstance().removeGroups("--EG-- Type");//remove all groups with --EG-- pattern
                    Iterator<String> itor = wl_map.keySet().iterator();
                    PickWatchlistDialog dlg = new PickWatchlistDialog(itor);
                    if (!dlg.isCancelled()) {
                        ArrayList<String> merge_list = WatchListModel.mergeLists(dlg.getWatchlists());
                        HashMap<GapType, ArrayList<GapInfo>> gap_map = GapUtil.categorizeEarningGaps(merge_list, CalendarQuarter.Q3_2014);

                        //create N watch lists +EG-1 ... EG-6
                        for (GapType gt : GapType.values()) {
                            ArrayList<String> members = new ArrayList<>();
                            ArrayList<GapInfo> gis = gap_map.get(gt);
                            if (gis != null) {//empty list for this type
                                for (GapInfo gi : gis) {
                                    members.add(gi.getQuote().getSymbol());
                                    GroupStore.getInstance().addGroup("--EG-- Type " + (gt.ordinal() + 1) + ":"
                                            + gt.toString(), members);
                                }
                            }
                        }
                        Props.WatchListsChange.setChanged();//refresh list
                    }
                }
            });
        }

        //dump internal data to .xls file
//        btn_pnl.add(_btnDump);
        _btnDump.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String sym = _lstSymbol.getSelectedValue();
                File output_file = new File(FrameworkConstants.DATA_FOLDER_EXPORT + File.separator + sym + "-" + FrameworkConstants.DUMP_FILE);
                try {
                    WritableWorkbook wb = Workbook.createWorkbook(output_file);
                    WritableSheet ws = wb.createSheet(output_file.getName(), 0);

                    //header
                    ws.addCell(new Label(0, 0, "Symbol"));
                    ws.addCell(new Label(1, 0, "Date"));
                    ws.addCell(new Label(2, 0, "Close"));
                    ws.addCell(new Label(3, 0, "10 SMA"));
                    ws.addCell(new Label(4, 0, "30 SMA"));
                    ws.addCell(new Label(5, 0, "50 SMA"));
                    ws.addCell(new Label(6, 0, "MACD (6,9,3)"));
                    ws.addCell(new Label(7, 0, "RSI (9,3)"));
                    ws.addCell(new Label(8, 0, "DSTO (9,3)"));
                    ws.addCell(new Label(9, 0, "50 EMA"));
                    ws.addCell(new Label(10, 0, "120 EMA"));
                    ws.addCell(new Label(11, 0, "200 EMA"));
                    ws.addCell(new Label(12, 0, "MACD (12,26,9)"));
                    ws.addCell(new Label(13, 0, "RSI (14,3)"));
                    ws.addCell(new Label(14, 0, "DSTO (14,3)"));

                    //values from MarketInfo
                    MarketInfo mki = MainModel.getInstance().getWatchListModel().getMarketInfo(sym);
                    int size = mki.getFund().getSize();
                    //save DCOM indicators
                    float[] macd_dcom = new float[size];
                    float[] rsi_dcom = new float[size];
                    float[] dsto_dcom = new float[size];
                    for (int i = 0; i < size; i++) {
                        macd_dcom[i] = mki.getMacd()[i];
                        rsi_dcom[i] = mki.getRsi()[i];
                        dsto_dcom[i] = mki.getDsto()[i];
                    }
                    float[] macd_emac = new float[size];
                    float[] rsi_emac = new float[size];
                    float[] dsto_emac = new float[size];
                    for (int i = 0; i < size; i++) {
                        macd_emac[i] = mki.getMacdStd()[i];
                        rsi_emac[i] = mki.getRsiStd()[i];
                        dsto_emac[i] = mki.getDstoStd()[i];
                    }
                    for (int row = 1; row < size + 1; row++) {
                        ws.addCell(new Label(0, row, sym));
                        ws.addCell(new Label(1, row, mki.getFund().getQuote().get(row - 1).getDate()));
                        ws.addCell(new Number(2, row, mki.getFund().getPrice(row - 1)));
                        ws.addCell(new Number(3, row, mki.getSma10()[row - 1]));
                        ws.addCell(new Number(4, row, mki.getSma30()[row - 1]));
                        ws.addCell(new Number(5, row, mki.getSma50()[row - 1]));
                        ws.addCell(new Number(6, row, macd_dcom[row - 1]));
                        ws.addCell(new Number(7, row, rsi_dcom[row - 1]));
                        ws.addCell(new Number(8, row, dsto_dcom[row - 1]));
                        ws.addCell(new Number(9, row, mki.getEma50()[row - 1]));
                        ws.addCell(new Number(10, row, mki.getEma120()[row - 1]));
                        ws.addCell(new Number(11, row, 0));//mki.getEma200()[row]));TODO........
                        ws.addCell(new Number(12, row, macd_emac[row - 1]));
                        ws.addCell(new Number(13, row, rsi_emac[row - 1]));
                        ws.addCell(new Number(14, row, dsto_emac[row - 1]));
                    }
                    wb.write();
                    wb.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        if (MainModel.getInstance().isExpertUer()) {
            btn_pnl.add(_btnTest); _btnTest.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //open group +++ with only SPY in it
                    WatchListModel wlm = MainModel.getInstance().getWatchListModel();
                    String sym0 = wlm.getMembers().get(0);
                    MarketInfo mki = wlm.getMarketInfo(sym0);
                    ArrayList<FundQuote> quotes = mki.getFund().getQuote();
                    Ichimoku ich = new Ichimoku(quotes, quotes.size() - 1, 0);
                }
            });
        }
        btn_pnl.add(_lblCount);
        cen_pnl.add(btn_pnl, "dock south");
        content_pane.add(cen_pnl, "dock center");
        JPanel srch_pnl = new JPanel(new MigLayout("insets 0, wrap 2", "[fill][grow]", "3[10][70]3"));//about 3 rows
        srch_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
        srch_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_77")), "gapx 2 5, right");
        srch_pnl.add(_fldSymbol, "left"); _fldSymbol.setFont(FrameworkConstants.MEDIUM_SMALL_FONT);
        _fldSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //search watch lists for symbol
                ArrayList<String> groups = GroupStore.getInstance().findGroupsByMember(_fldSymbol.getText().toUpperCase());
                if (groups.size() == 0) {
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_8"),
                            MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                    return;
                }
                _lmGroups.clear();
                for (String grp : groups)
                    _lmGroups.addElement(grp);
            }
        });
        srch_pnl.add(new JScrollPane(_lstSearchGroups), "gapx 10 10, span"); _lstSearchGroups.setModel(_lmGroups);
        _lstSearchGroups.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //find watch list in nav pane and select it
                _lstGroup.setSelectedValue(_lstSearchGroups.getSelectedValue(), true);
            }
        });
        _lstSearchGroups.setBorder(new EtchedBorder());
        content_pane.add(srch_pnl, "dock south");
        setContentPane(content_pane);
        MainUtil.handleLocationAndSize(this, MdiMainFrame.INDEX_WATCHLIST_MGR, MdiMainFrame.LOCATION_WATCHLIST_MGR, MdiMainFrame.SIZE_WATCHLIST_MGR);
        Props.addWeakPropertyChangeListener(Props.WatchListsChange, this);//handle symbol change
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case WatchListsChange://update graph
                _pnlListMgr.populateGroups();
                break;

        }
    }

    //----- inner classes -----
    private class ListMgrPanel extends JSplitPane {
        private ListMgrPanel() {
            super(JSplitPane.HORIZONTAL_SPLIT, true);
            setBorder(new BevelBorder(BevelBorder.LOWERED));
            int wlm_pos = ApolloPreferenceStore.getPreferences().getWatchListMgrSplitterPosition();
            if (wlm_pos > 0)
                setDividerLocation(wlm_pos);
            else {
                setDividerLocation(DEFAULT_POSITION);//left side emphasis
                ApolloPreferenceStore.getPreferences().setWatchListMgrSplitterPosition(DEFAULT_POSITION);
            }

            //divider movement -> update preference
            addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    String prop = evt.getPropertyName();
                    if (prop.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
                        int cur_loc = ListMgrPanel.this.getDividerLocation();
                        ApolloPreferenceStore.getPreferences().setWatchListMgrSplitterPosition(cur_loc);
                        ApolloPreferenceStore.savePreferences();
                    }
                }
            });

            //left - lists with buttons
            JPanel left_pnl = new JPanel(new BorderLayout());  left_pnl.setOpaque(false);
            JPanel btn_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[]10", "3[]3"));

            //add a new group
            btn_pnl.add(_btnAddGroup);
            _btnAddGroup.setDisabledIcon(new DisabledIcon(LazyIcon.PLUS_SIGN.getImage()));
            _btnAddGroup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                    if (dlg.isCancelled())
                        return;
                    String name = dlg.getEntry();

                    //check duplicate
                    for (int i = 0; i < _lmGroup.size(); i++) {
                        if (_lmGroup.getElementAt(i).equals(name)) {
                            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_5"));
                            return;
                        }
                    }
                    _lmSymbol.clear();
                    GroupStore.getInstance().addGroup(_sCurrentGroup = name, new ArrayList<String>());//empty group persist
                    populateGroups();//sort automatically
                    _lstGroup.setSelectedValue(name, true);
                }
            });

            //delete a group
            btn_pnl.add(_btnDeleteWatchList);
            _btnDeleteWatchList.setDisabledIcon(new DisabledIcon(LazyIcon.MINUS_SIGN.getImage()));
            _btnDeleteWatchList.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //ask confirmation
                    String name = _lstGroup.getSelectedValue();
                    if (MessageBox.messageBox(null, //TODO use passed parent in
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("trk_40") + ": " + name,
                            MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                        return;

                    _lmGroup.removeElement(name);
                    GroupStore.getInstance().removeGroup(name);
                    if (_lmGroup.size() == 0) {
                        _btnDeleteWatchList.setEnabled(false);
                        _btnMergeWatchList.setEnabled(false);
                    }
                }
            });

            //launch watch list window, not for basic user
            if (!MainModel.getInstance().isBasicUser()) {
                btn_pnl.add(_btnLaunch);
                _btnLaunch.setDisabledIcon(new DisabledIcon(ApolloIcon.LAUNCH.getImage()));
                _btnLaunch.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        MdiMainFrame.getInstance().startWatchListFrame(_sCurrentGroup);
                    }
                });
            }

            //rename a group
            btn_pnl.add(_btnRename);
            _btnRename.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILE_SAVE_AS.getImage()));
            _btnRename.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), _sCurrentGroup);
                    if (dlg.isCancelled()) return;
                    String new_name = dlg.getEntry();
                    ArrayList<String> members = GroupStore.getInstance().getMembers(_sCurrentGroup);
                    GroupStore.getInstance().removeGroup(_sCurrentGroup);
                    _sCurrentGroup = new_name;
                    GroupStore.getInstance().addGroup(new_name, members);
                    populateGroups();//refresh view, sort automatically
                    _lstGroup.setSelectedValue(new_name, true);
                }
            });
//
//            //duplicate and merge buttons
//            btn_pnl.add(_btnDuplicate); //_btnDuplicate.setEnabled(false);
//            _btnDuplicate.setDisabledIcon(new DisabledIcon(FrameworkIcon.DUPLICATE.getImage()));
//            _btnDuplicate.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    String new_name = _sCurrentGroup + "(Copy)";
//                    ArrayList<String> members = GroupStore.getInstance().getMembers(_sCurrentGroup);
//                    _sCurrentGroup = new_name;
//                    GroupStore.getInstance().addGroup(new_name, members);
//                    populateGroups();//refresh view, sort automatically
//                    _lstGroup.setSelectedValue(new_name, true);
//                }
//            });
//
//            //import symbols into a group
//            btn_pnl.add(_btnImport);
//            _btnImport.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent actionEvent) {
//                    //ask user about file type
//                    FileTypeDialog dlg = new FileTypeDialog();
//                    if (dlg.isCancelled())
//                        return;
//                    ImportFileType type = dlg.getFileType();
//                    String ds, ext;
//                    if (type.equals(ImportFileType.IBD50_XLS)) {//only this needs .xls
//                        ds = ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_70");
//                        ext = FrameworkConstants.EXTENSION_XLS;
//                    }
//                    else  {//others use .csv
//                        ds = ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_71");
//                        ext = FrameworkConstants.EXTENSION_CSV;
//                    }
//                    final String descr = ds;
//                    final String extension = ext;
//
//                    //open file dialog with .csv or .xls extension
//                    JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER));
//                    fc.setFileFilter(new FileFilter() {
//                        public boolean accept(File file) {
//                            if (file.isDirectory())
//                                return true;
//
//                            //only allow .csv or .xls extension
//                            int ext_pos = file.getName().lastIndexOf(extension);
//                            if (ext_pos > 0)
//                                return true;
//                            return false;
//                        }
//
//                        public String getDescription() {//this shows up in description field of dialog
//                            return descr;
//                        }
//                    });
//                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//                    fc.setMultiSelectionEnabled(true);
//                    int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
//                    if (ret == JFileChooser.APPROVE_OPTION) {//user pick, only first one
//                        File[] msel = fc.getSelectedFiles();
//                        File sel = fc.getSelectedFile();
//
//                        //open file, read first column of each line into array of strings
//                        String name = sel.getName();
//                        String prefix = name.substring(0, name.indexOf("."));
//                        try {
//                            GroupStore.getInstance().addGroup(_sCurrentGroup = prefix,
//                                DataUtil.readSymbolToWatchList(msel, type));
//                            populateGroups();
//                            _lstGroup.setSelectedValue(prefix, true);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            MessageBox.messageBox(
//                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                                    ApolloConstants.APOLLO_BUNDLE.getString("wl_msg_9") + name,
//                                    MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING
//                            );
//                        }
//                    }
//                }
//            });
//
//            //merge two or more together
//            btn_pnl.add(_btnMergeWatchList); //_btnMergeWatchList.setEnabled(false);
//            _btnMergeWatchList.setDisabledIcon(new DisabledIcon(FrameworkIcon.MERGE.getImage()));
//            _btnMergeWatchList.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    MergeWatchListDialog dlg = new MergeWatchListDialog(MdiMainFrame.getInstance());
//                    ArrayList<String> names = dlg.getWatchListNames();
//                    String merged_name = dlg.getMergedName();
//
//                    //start merging
//                    ArrayList<String> merge_lst = new ArrayList<>();
//                    for (String name : names) {
//                        ArrayList<String> members = GroupStore.getInstance().getMembers(name);
//                        for (String member : members) {
//                            if (!merge_lst.contains(member))
//                                merge_lst.add(member);
//                        }
//                    }
//                    GroupStore.getInstance().addGroup(merged_name, merge_lst);
//                    populateGroups();
//                }
//            });
//            btn_pnl.add(_btnDownloadAll);
//            _btnDownloadAll.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    ArrayList<String> dld_list = new ArrayList<>();
//                    ArrayList<String> all_groups = GroupStore.getInstance().getGroupNames();
//                    for (String grp : all_groups) {
//                        ArrayList<String> members = GroupStore.getInstance().getMembers(grp);
//                        for (String sym : members) {
//                            boolean exist = FileUtil.isSymbolExist(new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE), sym);
//                            if (!exist)
//                                dld_list.add(sym);
//                        }
//                    }
//                    if (dld_list.size() == 0) {
//                        MessageBox.messageBox(MdiMainFrame.getInstance(),
//                            Constants.COMPONENT_BUNDLE.getString("warning"),
//                            ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_7"),
//                            MessageBox.OK_OPTION, MessageBox.IMAGE_QUESTION);
//                        return;
//                    }
//
//                    //show this to user in a window
//                    StringBuilder buf = new StringBuilder();
//                    for (String str : dld_list)
//                        buf.append(str).append("  ");
//                    String ms = WidgetUtil.getMultiLineString(buf);
//                    String msg = ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_6") + ms;
//                    if (WidgetUtil.confirmAction(MdiMainFrame.getInstance(), msg)) {
//                        Props.ShowApp.setValue(null, DataMgrFrame.class);
//                        Props.AddSymbols.setValue(null, dld_list);//informs listener to start download
//                    }
//                }
//            });

            //symbol list pane
            left_pnl.add(btn_pnl, BorderLayout.NORTH);
            _lstGroup = new JList(_lmGroup);
            _lstGroup.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            _lstGroup.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) //this removes intermediate event during selection
                        return;

                    int sel = _lstGroup.getSelectedIndex();
                    if (sel >= 0) {
                        //refresh device list from GroupStore
                        String sel_grp = _lstGroup.getSelectedValue();
                        _sCurrentGroup = sel_grp;
                        ArrayList<String> symbols = GroupStore.getInstance().getMembers(sel_grp);
                        _lmSymbol.clear();
                        if (symbols.size() > 0) {
                            for (String symbol : symbols)
                                _lmSymbol.addElement(symbol);
                            sortSymbolList();
                        }
                        _btnDeleteWatchList.setEnabled(true);
                        _btnLaunch.setEnabled(true);
                        _btnDuplicate.setEnabled(true);
                        _btnRename.setEnabled(true);
                        _btnAddSymbol.setEnabled(true);
                        _btnViewEditSymbols.setEnabled(true);
                        _btnDldQuote.setEnabled(true); _btnDldDiv.setEnabled(true);

                        //prepare watch list model for selected group, first time startup, don't wait for populate
                        final String grp = _lstGroup.getSelectedValue();

                        //create model in a thread because there could be many symbols
//TODO Mysterious missing 450ms here
                        WatchListModel model = new WatchListModel(grp, false);
                        MainModel.getInstance().setWatchListModel(model);
                        _bStartup = false;
                        _lstSymbol.setSelectedIndex(0);
                        String sym = (String)_lstSymbol.getSelectedValue();
                        Props.SymbolSelection.setValue(sym);
                        Props.WatchListSelected.setValue(grp);
                        _lblCount.setText("#" + String.valueOf(symbols.size()));
                    } else {//no group selected
                        _lmSymbol.clear();
                        _btnDeleteWatchList.setEnabled(false);
                        _btnLaunch.setEnabled(false);
                        _btnDuplicate.setEnabled(false);
                        _btnRename.setEnabled(false);
                        _btnAddSymbol.setEnabled(false);
                        _btnDeleteSymbol.setEnabled(false);
                        _btnViewEditSymbols.setEnabled(false);
                        _btnDldQuote.setEnabled(false); _btnDldDiv.setEnabled(false);

                        //clear chart frame
                        Props.MarketInfoChange.setValue(null);
                    }
                }
            });
            _lstGroup.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {//double click+, launch watch list window
                        MdiMainFrame.getInstance().startWatchListFrame(_sCurrentGroup);
                    }
                }
            });
            left_pnl.add(new JScrollPane(_lstGroup), BorderLayout.CENTER);
            setLeftComponent(left_pnl);

            //right - list of symbols with buttons on title strip
            JPanel rite_pnl = new JPanel(new BorderLayout());
            JPanel btn_pnl2 = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[]10", "3[]3"));
            btn_pnl2.add(_btnAddSymbol);
            _btnAddSymbol.setDisabledIcon(new DisabledIcon(LazyIcon.PLUS_SIGN.getImage()));
//TODO: after add, only add new mki to map instead of doing all mkis
            _btnAddSymbol.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    NameDialog dlg = new NameDialog(MdiMainFrame.getInstance(), "");
                    if (dlg.isCancelled())
                        return;

                    //check duplicate
                    String symbol = dlg.getEntry().toUpperCase();
                    for (int i = 0; i < _lmSymbol.size(); i++) {
                        if (_lmSymbol.getElementAt(i).equals(symbol)) {
                            MessageBox.messageBox(MdiMainFrame.getInstance(),
                                    FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                                    ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_4"),
                                    MessageBox.OK_OPTION, MessageBox.IMAGE_WARNING);
                            return;
                        }
                    }
                    _lmSymbol.addElement(symbol);
                    sortSymbolList();
                    GroupStore.getInstance().addMember(_sCurrentGroup, symbol);//persist
                    MainModel.getInstance().setWatchListModel(new WatchListModel(_sCurrentGroup, false));
                    _lstSymbol.setSelectedIndex(0);//trigger selection event
                }
            });
            btn_pnl2.add(_btnDeleteSymbol);
            _btnDeleteSymbol.setEnabled(false);
            _btnDeleteSymbol.setDisabledIcon(new DisabledIcon(LazyIcon.MINUS_SIGN.getImage()));
//TODO: after delete, only remove mki's from map instead of doing all again
            _btnDeleteSymbol.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (_lmSymbol.size() == 0) {//after the last one deleted
                        _btnDeleteSymbol.setEnabled(false);
                        _btnViewEditSymbols.setEnabled(false);
                        return;
                    }
                    removeSymobls();
                }
            });
            btn_pnl2.add(_btnViewEditSymbols);
            _btnViewEditSymbols.setEnabled(false);
            _btnViewEditSymbols.setDisabledIcon(new DisabledIcon(FrameworkIcon.VIEW_EDIT.getImage()));
            _btnViewEditSymbols.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ArrayList<String> cur_symbols = GroupStore.getInstance().getMembers(_sCurrentGroup);
                    ViewSymbolsDialog dlg = new ViewSymbolsDialog(cur_symbols, false);
                    if (dlg.isCancelled())
                        return;

                    //if list remains the same, skip
                    ArrayList<String> tmp_symbols = dlg.getSymbols();
                    if (cur_symbols.size() == tmp_symbols.size()) {
                        boolean no_change = true;
                        for (String sym : tmp_symbols) {
                            if (!cur_symbols.contains(sym)) {
                                no_change = false;
                                break;
                            }
                        }
                        if (no_change) return;
                    }

                    //remove duplicate symbols by transferring tmp_symbols to symbols
                    ArrayList<String> symbols = new ArrayList<>();
                    for (String sym : tmp_symbols) {
                        if (!symbols.contains(sym))
                            symbols.add(sym);
                    }

                    //override the same watch list, refresh list
                    GroupStore.getInstance().removeGroup(_sCurrentGroup);
                    GroupStore.getInstance().addGroup(_sCurrentGroup, symbols);
                    _lmSymbol.clear();
                    for (String symbol : symbols)
                        _lmSymbol.addElement(symbol);
                    sortSymbolList();
                    MainModel.getInstance().setWatchListModel(new WatchListModel(_sCurrentGroup, false));
                    if (_lmSymbol.size() > 0)
                        _lstSymbol.setSelectedIndex(0);//trigger selection event
                }
            });

            //download button - if symbols don't exist in database
            btn_pnl2.add(_btnDldQuote); _btnDldQuote.setEnabled(false);
            _btnDldQuote.setDisabledIcon(new DisabledIcon(FrameworkIcon.DOWNLOAD.getImage()));
            _btnDldQuote.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ArrayList<String> dld_list = new ArrayList<>();
                    ArrayList<String> cur_list = GroupStore.getInstance().getGroup(_sCurrentGroup);
                    File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
                    String[] quote_list = folder.list();
                    for (String sym : cur_list) {
                        boolean found = false;
                        for (String qsym : quote_list) {
                            int dot_idx = qsym.indexOf(".");
                            if (dot_idx <= 0)
                                continue;

                            //remove file extension
                            String qsub = qsym.substring(0, dot_idx);
                            if (qsub.equals(sym)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            dld_list.add(sym);
                    }
                    if (dld_list.size() == 0) {
                        MessageBox.messageBox(MdiMainFrame.getInstance(),
                                Constants.COMPONENT_BUNDLE.getString("warning"),
                                ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_7"),
                                MessageBox.OK_OPTION, MessageBox.IMAGE_QUESTION);
                        return;
                    }

                    //show this to user in a window
                    StringBuilder buf = new StringBuilder();
                    for (String str : dld_list)
                        buf.append(str).append("  ");
                    String ms = WidgetUtil.getMultiLineString(buf);
                    String msg = ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_6") + ms;
                    if (WidgetUtil.confirmAction(MdiMainFrame.getInstance(), msg)) {
                        Props.ShowApp.setValue(null, DataMgrFrame.class);
                        Props.AddSymbols.setValue(null, dld_list);//informs listener to start download
                    }
                }
            });
            btn_pnl2.add(_btnDldDiv); _btnDldDiv.setEnabled(false);
            _btnDldDiv.setDisabledIcon(new DisabledIcon(FrameworkIcon.DOWNLOAD.getImage()));
            _btnDldDiv.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    DividendThread dth = new DividendThread(); dth.start();
                }
            });
            rite_pnl.add(btn_pnl2, BorderLayout.NORTH);
            _lstSymbol = new JList(_lmSymbol = new DefaultListModel());
            _lstSymbol.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            _lstSymbol.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    int[] sel = _lstSymbol.getSelectedIndices();
                    if (sel.length >= 1) {//multi-select
                        _btnDeleteSymbol.setEnabled(true);
                        _btnViewEditSymbols.setEnabled(false);
                        _btnDldQuote.setEnabled(false); _btnDldDiv.setEnabled(false);
                        if (sel.length == 1) {
                            _btnViewEditSymbols.setEnabled(true);
                            _btnViewEditSymbols.setEnabled(true);
                            _btnDldQuote.setEnabled(true); _btnDldDiv.setEnabled(true);
                            Props.SymbolSelection.setValue(_lstSymbol.getSelectedValue());
                        }
                    }
                }
            });
            _lstSymbol.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (KeyEvent.VK_DELETE == e.getKeyCode()) {
                        removeSymobls();
                    }
                }
            });
            rite_pnl.add(new JScrollPane(_lstSymbol), BorderLayout.CENTER);
            setRightComponent(rite_pnl);

            //populate group and device lists from storage file
            populateGroups();
        }

        private void sortSymbolList() { //sort JList via model
            Enumeration<String> symbols = _lmSymbol.elements();
            ArrayList<String> new_list = new ArrayList<>();
            while (symbols.hasMoreElements())
                new_list.add(symbols.nextElement());
            Collections.sort(new_list);
            _lmSymbol.clear();
            for (String s : new_list)
                _lmSymbol.addElement(s);
        }

        //fill and sort the group list with stored data
        private void populateGroups() {
            HashMap<String, ArrayList<String>> groups = GroupStore.getInstance().getGroups();
            boolean no_group = groups != null;
            if (no_group) {//populate view
                Set<String> grp_names = groups.keySet();
                ArrayList<String> grp_list = new ArrayList<>();
                for (String grp : grp_names)
                    grp_list.add(grp);
                Collections.sort(grp_list);
                _lmGroup.clear();
                for (String grp : grp_list) {
                    if (_sCurrentGroup == null) //pick up the first one
                        _sCurrentGroup = grp;
                    _lmGroup.addElement(grp);
                }
                _lstGroup.setSelectedIndex(0);//this will trigger list selection event
            }
            //special condition: no group defined
            _btnDeleteWatchList.setEnabled(no_group);
            _btnRename.setEnabled(no_group);
            _btnDuplicate.setEnabled(no_group);
            _btnExport.setEnabled(no_group);
            _btnLaunch.setEnabled(no_group);
            _btnMergeWatchList.setEnabled(no_group);
            _btnAddSymbol.setEnabled(no_group);
            _btnDeleteSymbol.setEnabled(no_group);
            _btnViewEditSymbols.setEnabled(no_group);
            _btnDldQuote.setEnabled(no_group); _btnDldDiv.setEnabled(no_group);
        }

        private void removeSymobls() {
            List<String> sel = _lstSymbol.getSelectedValuesList();//java 7 has getSelectedValueList()
            for (int i=0; i<sel.size(); i++)
                _lmSymbol.removeElement(sel.get(i));
            GroupStore.getInstance().removeMembers(_sCurrentGroup, sel);//persist
            sortSymbolList();
            MainModel.getInstance().setWatchListModel(new WatchListModel(_sCurrentGroup, false));
            if (_lmSymbol.size() > 0)
                _lstSymbol.setSelectedIndex(0);//trigger selection event
        }
    }

    //----- inner classes -----
    private class DividendThread extends Thread {
        public void run() {
            final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("imp_dld_div"));
            pb.setVisible(true);
            ArrayList<String> members = GroupStore.getInstance().getMembers(_sCurrentGroup);
            for (final String symbol : members) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() { pb.setLabel("Downloading Dividends...." + symbol); } });
                try {
                    DataUtil.downloadDividend(symbol, 0, 1, 1900, 11, 31, 2099);
                } catch (IOException e) { e.printStackTrace(); }
            }
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    pb.setVisible(false);
                }
            });
        }
    }

    //----- variables -----
    private JButton _btnAddGroup = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_3"), LazyIcon.PLUS_SIGN);
    private JButton _btnDeleteWatchList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_4"), LazyIcon.MINUS_SIGN);
    private JButton _btnRename = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_13"), FrameworkIcon.FILE_SAVE_AS);
    private JButton _btnImport = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_9"), FrameworkIcon.IMPORT);
    private JButton _btnExport = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_14"), FrameworkIcon.EXPORT);
    private JButton _btnLaunch = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_5"), ApolloIcon.LAUNCH);
    private JButton _btnDuplicate = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_6"), FrameworkIcon.DUPLICATE);
    private JButton _btnMergeWatchList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_17"), FrameworkIcon.MERGE);
    private JButton _btnDownloadAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlm_dbdld"), FrameworkIcon.DOWNLOAD);
    private JButton _btnAddSymbol = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlm_addsym"), LazyIcon.PLUS_SIGN);
    private JButton _btnDeleteSymbol = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlm_delsym"), LazyIcon.MINUS_SIGN);
    private JButton _btnViewEditSymbols = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlm_edtsym"), FrameworkIcon.VIEW_EDIT);
    private JButton _btnDldQuote = WidgetUtil.createIconButton("Update Quotes for this list", FrameworkIcon.DOWNLOAD);
    private JButton _btnDldDiv = WidgetUtil.createIconButton("Download Dividends for this list", FrameworkIcon.DOWNLOAD);
    private JButton _btnBulkDelete = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_18"), FrameworkIcon.FILE_DELETE);
    private JButton _btnExportList = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_21"), FrameworkIcon.EXPORT_LIST);
    private JButton _btnGap = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_gap"), FrameworkIcon.CDL_BULL_GAP);
    private JButton _btnDump = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_19"), FrameworkIcon.DUMPER);
    private JLabel _lblCount = new JLabel();
    private String _sCurrentGroup;
    private ListMgrPanel _pnlListMgr;
    private JList<String> _lstGroup;
    private JList<String> _lstSymbol;
    private DefaultListModel _lmGroup = new DefaultListModel(), _lmSymbol = new DefaultListModel();
    private boolean _bStartup = true;//true = first time application start
    private JTextField _fldSymbol = new JTextField(8);//TODO consider new type of field only allows upper case stock symbols
    private JList<String> _lstSearchGroups = new JList<>();
    private DefaultListModel<String> _lmGroups = new DefaultListModel<>();

    //----- literals -----
    private static final int DEFAULT_POSITION = 300;


    private JButton _btnTest = WidgetUtil.createIconButton("Ichimoku Debug", FrameworkIcon.ARROW_3D_RIGHT);

}