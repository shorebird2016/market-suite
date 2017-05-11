package org.marketsuite.datamgr.dataimport;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.TechnicalInfo;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.TechnicalInfo;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Various importing tools from IBD, Finviz, Barchart
 * IBD: several proprietary rankings such as SMR, RS, EPS..etc, placed in technical.db
 */
public class ImportPanel extends SkinPanel {
    public ImportPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new MigLayout());
        setOpaque(false);

        //north - title strip, show file, open button, file type,
        JPanel north_pnl = new JPanel(new MigLayout("insets 0", "5[][]push[]10[]5[]5", "3[]3")); north_pnl.setOpaque(false);
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_lbl_2"));
        lbl.setFont(Constants.FONT_BOLD);
        north_pnl.add(lbl); north_pnl.add(_cmbType);
        _cmbType.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) return;
                final String descr, extension;
                String prev_path = null;
                String import_type = (String)_cmbType.getSelectedItem();
                if (import_type.equals(LIST_IMPORT_TYPE[TYPE_SNAPSHOT_IBD_ETF])) {
                    return;
//TODO
                }
                else if (import_type.equals(LIST_IMPORT_TYPE[TYPE_FINVIZ])) {
                    descr = ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_71");
                    extension = FrameworkConstants.EXTENSION_CSV;
                    prev_path = ApolloPreferenceStore.getPreferences().getLastFinvizPath();
                }
                else if (import_type.equals(LIST_IMPORT_TYPE[TYPE_MS_WATCH_LISTS])) {
                    descr = ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_76");
                    extension = FrameworkConstants.EXTENSION_XML;
                    prev_path = ApolloPreferenceStore.getPreferences().getLastWatchListPath();
                }
                else if (import_type.equals(LIST_IMPORT_TYPE[TYPE_TS_POSITION])) {
                    return;
//TODO
                }
                else return;

                //open file dialog with .xml, .csv or .xls extension, use pref to get last stored import path
                if (prev_path == null)
                    prev_path = FrameworkConstants.DATA_FOLDER_IMPORT;
                JFileChooser fc = new JFileChooser(new File(prev_path));
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory()) return true;
                        int pos = file.getName().lastIndexOf(extension);
                        return (pos > 0);
                    }
                    public String getDescription() {//this shows up in description field of dialog
                        return descr;
                    }
                });
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(true);
                fc.setAcceptAllFileFilterUsed(false);
                int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
                if (ret == JFileChooser.APPROVE_OPTION) {
                    if (import_type.equals(LIST_IMPORT_TYPE[TYPE_FINVIZ]))
                        ApolloPreferenceStore.getPreferences().setLastFinvizPath(fc.getCurrentDirectory().getPath());
                    else if (import_type.equals(LIST_IMPORT_TYPE[TYPE_MS_WATCH_LISTS]))
                        ApolloPreferenceStore.getPreferences().setLastWatchListPath(fc.getCurrentDirectory().getPath());
                    ApolloPreferenceStore.savePreferences();//flush
                    File[] sels = fc.getSelectedFiles();
                    mergeFilesToDb(import_type, sels);
                }
            }
        });
        north_pnl.add(_btnEarning); _btnEarning.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                extractEarningDates();
            }
        });

//        _btnEarning.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent actionEvent) {
//                //setup description and file filter based on selection
//                String file_type = (String) _cmbType.getSelectedItem();
//                String ds, ext;
//                String prev_path = null;
//                if (file_type.equals(LIST_IMPORT_TYPE[TYPE_FINVIZ])) {//these 3 use .csv
//                    ds = ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_71");
//                    ext = FrameworkConstants.EXTENSION_CSV;
//                    if (file_type.equals(LIST_IMPORT_TYPE[TYPE_FINVIZ]))
//                        prev_path = ApolloPreferenceStore.getPreferences().getLastFinvizPath();
//                } else if (file_type.equals(LIST_IMPORT_TYPE[TYPE_MS_WATCH_LISTS])) {
//                    ds = ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_76");
//                    ext = FrameworkConstants.EXTENSION_XML;
//                    prev_path = ApolloPreferenceStore.getPreferences().getLastWatchListPath();
//                } else {//special import from many portfolio files into rating DB
//                    //ask user to select top level folder (IBD Portfolio usually)
//                    prev_path = ApolloPreferenceStore.getPreferences().getLastIbdPortfolioPath();
//                    if (prev_path == null)
//                        prev_path = FrameworkConstants.DATA_FOLDER_EXPORT;
//                    JFileChooser fc = new JFileChooser(new File(prev_path));
//                    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//                    fc.setMultiSelectionEnabled(false);
//                    fc.setAcceptAllFileFilterUsed(false);
//                    int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
//                    if (ret == JFileChooser.APPROVE_OPTION) {
//                        ApolloPreferenceStore.getPreferences().setLastIbdPortfolioPath(fc.getCurrentDirectory().getPath());//save location
//                        ApolloPreferenceStore.savePreferences();//flush
//                        File sel = fc.getSelectedFile();
//
//                        //create destination folder if not there
//                        File rating_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_RATING);
//                        if (!rating_folder.exists())
//                            rating_folder.mkdir();
//                        new IbdThread(sel).start();
//                    }
//                    return;
//                }
//                final String descr = ds;
//                final String extension = ext;
//
//                //open file dialog with .xml, .csv or .xls extension, use pref to get last stored import path
//                if (prev_path == null)
//                    prev_path = FrameworkConstants.DATA_FOLDER_IMPORT;
//                JFileChooser fc = new JFileChooser(new File(prev_path));
//                fc.setFileFilter(new FileFilter() {
//                    public boolean accept(File file) {
//                        if (file.isDirectory())
//                            return true;
//
//                        int pos = file.getName().lastIndexOf(extension);
//                        return (pos > 0);
//                    }
//
//                    public String getDescription() {//this shows up in description field of dialog
//                        return descr;
//                    }
//                });
//                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//                fc.setMultiSelectionEnabled(true);
//                fc.setAcceptAllFileFilterUsed(false);
//                int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
//                if (ret == JFileChooser.APPROVE_OPTION) {
//                    if (file_type.equals(LIST_IMPORT_TYPE[TYPE_FINVIZ]))
//                        ApolloPreferenceStore.getPreferences().setLastFinvizPath(fc.getCurrentDirectory().getPath());
//                    else if (file_type.equals(LIST_IMPORT_TYPE[TYPE_MS_WATCH_LISTS]))
//                        ApolloPreferenceStore.getPreferences().setLastWatchListPath(fc.getCurrentDirectory().getPath());
//                    ApolloPreferenceStore.savePreferences();//flush
//                    File[] sels = fc.getSelectedFiles();
//                    mergeFilesToDb(file_type, sels);
//                }
//            }
//        });
        north_pnl.add(_btnImportBulkIbdPortfolio);
        _btnImportBulkIbdPortfolio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!WidgetUtil.confirmOkCancel(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("ibd_15")))
                    return;

                //ask user to select top level folder (IBD Portfolio usually)
                String prev_path = ApolloPreferenceStore.getPreferences().getLastIbdPortfolioPath();
                if (prev_path == null)//don't exist in preference
                    prev_path = FrameworkConstants.DATA_FOLDER_EXPORT;
                JFileChooser fc = new JFileChooser(new File(prev_path));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setMultiSelectionEnabled(false);
                fc.setAcceptAllFileFilterUsed(false);
                int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
                if (ret == JFileChooser.APPROVE_OPTION) {
                    ApolloPreferenceStore.getPreferences().setLastIbdPortfolioPath(fc.getCurrentDirectory().getPath());//save location
                    ApolloPreferenceStore.savePreferences();//flush
                    File sel = fc.getSelectedFile();

                    //create destination folder if not there
                    File rating_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_RATING);
                    if (!rating_folder.exists())
                        rating_folder.mkdir();
                    new IbdThread(sel).start();
                }
            }
        });
        north_pnl.add(_btnImportSingleIbdPortfolio);
        _btnImportSingleIbdPortfolio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String prev_path = ApolloPreferenceStore.getPreferences().getLastIbdPortfolioPath();
                if (prev_path == null)//don't exist in preference
                    prev_path = FrameworkConstants.DATA_FOLDER_EXPORT;
                JFileChooser fc = new JFileChooser(new File(prev_path));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setMultiSelectionEnabled(false);
                int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
                if (ret == JFileChooser.APPROVE_OPTION) {
                    ApolloPreferenceStore.getPreferences().setLastIbdPortfolioPath(fc.getCurrentDirectory().getPath());//save location
                    ApolloPreferenceStore.savePreferences();//flush
                    File sel = fc.getSelectedFile();
                    String folder_name = sel.getName();
                    if (!folder_name.contains("Por")) {
                        MessageBox.messageBox(
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("dmi_tip_7"),
                            MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING
                        );
                        return;
                    }
                    int idx = folder_name.indexOf(" ");//get tail part of folder_name name = date
                    String date_str = folder_name.substring(idx + 1, folder_name.length());//tail is date YYYY-MM-DD
                    try {
                        Date dt = FrameworkConstants.YAHOO_DATE_FORMAT.parse(date_str);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dt);
                        DataUtil.importSingleIbdPortfolio(sel.listFiles(), cal);
                        WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_1"));
                    } catch (ParseException pse) {
                        MessageBox.messageBox(
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                            ApolloConstants.APOLLO_BUNDLE.getString("dmi_tip_7"),
                            MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING
                        );
                    }
                }
            }
        });
        add(north_pnl, "dock north");

        //center - view imported file
        _txaViewer.setEditable(false); _txaViewer.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(new JScrollPane(_txaViewer), "dock center");
    }

    //use thread to process importing tasks, capture errors
    private void mergeFilesToDb(final String file_type, final File[] file_list) {
        //two variables for thread information
        if (file_type.equals(LIST_IMPORT_TYPE[TYPE_FINVIZ])) {//only 1 file for finviz, also for fundamental DB
            mergeFinvizFile(file_list[0]);

            //create / merge earning data

            WidgetUtil.showMessageNoEdt(ApolloConstants.APOLLO_BUNDLE.getString("dm_01"));
            System.exit(0);
        }
        else if (file_type.equals(LIST_IMPORT_TYPE[TYPE_MS_WATCH_LISTS])) {//MS watch list, show in a list for user to pick from
            HashMap<String, ArrayList<String>> wl_map = FileUtil.readWatchlists(file_list[0]);
            Iterator<String> itor = wl_map.keySet().iterator();
            PickWatchlistDialog dlg = new PickWatchlistDialog(itor);
            if (!dlg.isCancelled()) {
                List<String> watch_lists = dlg.getWatchlists();
                for (String wl : watch_lists) {//add these groups to this computer's watch list
                    ArrayList<String> symbols = wl_map.get(wl);
                    ArrayList<String> existing_groups = GroupStore.getInstance().getGroupNames();
                    if (existing_groups.contains(wl)) {//duplicate watch list, append date
                        Calendar cal = Calendar.getInstance();
                        wl += new SimpleDateFormat("(MM-dd-yy)").format(cal.getTime());
                    }
                    GroupStore.getInstance().addGroup(wl, symbols);
                    Props.WatchListsChange.setChanged();
                }
            }
            return;
        }
        //create IBD ratings database
//        else if (file_type.equals(LIST_IMPORT_TYPE[TYPE_HISTORICAL_IBD_PORTFOLIO])) {
//            pb.setVisible(true);
//            //ask user to select top level folder (IBD Portfolio usually)
//            String prev_path = ApolloPreferenceStore.getPreferences().getLastIbdPortfolioPath();
//            if (prev_path == null)
//                prev_path = FrameworkConstants.DATA_FOLDER_IBD_DB;
//            JFileChooser fc = new JFileChooser(new File(prev_path));
//            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            fc.setMultiSelectionEnabled(false);
//            fc.setAcceptAllFileFilterUsed(false);
//            int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
//            if (ret == JFileChooser.APPROVE_OPTION) {
//                ApolloPreferenceStore.getPreferences().setLastIbdPortfolioPath(fc.getCurrentDirectory().getPath());//save location
//                ApolloPreferenceStore.savePreferences();//flush
//                File sel = fc.getSelectedFile();
//                new IbdThread(sel).start();
//            }
//            return;
//        }

        //other types may take longer time, use progress meter and thread, for technical DB
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
        final ArrayList<LogMessage> failed_files = new ArrayList<>();
        pb.setVisible(true);

        //start thread to import
        Thread import_thread = new Thread() {
            public void run() {
                File file = file_list[0];//use this to log failed files
                try {
                    HashMap<String, TechnicalInfo> tech_map = DataUtil.readTechnicalDb();
                    for (final File f : file_list) {
                        file = f;
                        if (file_type.equals(LIST_IMPORT_TYPE[TYPE_SNAPSHOT_IBD_ETF])) {
                            DataUtil.mergeIbdEtfXlsFile(f, tech_map);
                        }
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_3") + f.getAbsolutePath());
                            }
                        });
                    }
                } catch (Exception e) {
                    //record bad files
                    LogMessage lm = new LogMessage(LoggingSource.DATAMGR_IMPORT, file.getAbsolutePath() + ApolloConstants.APOLLO_BUNDLE.getString("rm_91"), e);
                    failed_files.add(lm);
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setVisible(false);
                        WidgetUtil.showMessageNoEdt(ApolloConstants.APOLLO_BUNDLE.getString("dm_01"));
                        System.exit(0);
                    }
                });
            }
        };
        import_thread.start();
    }
    private void mergeFinvizFile(File fin_file) {
        HashMap<String,Fundamental> fund_map = DataUtil.readFundamentalDb();
        if (fund_map == null)
            fund_map = new HashMap<>();//start empty
        try {
            fund_map = DataUtil.mergeFinvizFile(fin_file, fund_map);
            //write fund_map back to file, reload fundamental DB into memory
            DataUtil.writeFundamentlDb(fund_map);
        } catch (IOException e) {
            MessageBox.messageBox(
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("ime_msg_3"),
                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
            e.printStackTrace();
        }
//        _lnkFundamental.doClick();
    }

    //extract historical earning dates from all Finviz files
    private void extractEarningDates() {
        String prev_path = ApolloPreferenceStore.getPreferences().getLastFinvizPath();
        if (prev_path == null) prev_path = FrameworkConstants.DATA_FOLDER;
        JFileChooser fc = new JFileChooser(new File(prev_path));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        int rsp = fc.showOpenDialog(MdiMainFrame.getInstance());
        if (rsp == JFileChooser.APPROVE_OPTION) {
            ApolloPreferenceStore.getPreferences().setLastFinvizPath(fc.getCurrentDirectory().getPath());//save location
            ApolloPreferenceStore.savePreferences();//flush
            new EarningThread(fc.getSelectedFile()).start();
        }
    }

    //import 1 IBD50 sheet + 1 IBD portfolio folder with multiple sheets
    private void importSingleIbdRatings() {
        //open file dialog with .xls extension, use pref to get last stored import path
        String lip = ApolloPreferenceStore.getPreferences().getLastIbd50Path();
        if (lip == null)
            lip = FrameworkConstants.DATA_FOLDER_IMPORT;

        //ask user for IBD50 location
        JFileChooser fc = new JFileChooser(new File(lip));
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;
                int pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_XLS);
                return (pos > 0);
            }
            public String getDescription() {//this shows up in description field of dialog
                return ApolloConstants.APOLLO_BUNDLE.getString("act_lbl_2");
            }
        });
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle(ApolloConstants.APOLLO_BUNDLE.getString("imp_ttl_ibd50"));
        int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
        if (ret != JFileChooser.APPROVE_OPTION) return;
        ApolloPreferenceStore.getPreferences().setLastIbd50Path(fc.getCurrentDirectory().getPath());//save location
        ApolloPreferenceStore.savePreferences();//flush
        final File ibd50_sheet = fc.getSelectedFile();//single file

        //ask user for IBD Portfolio location
        String prev_path = ApolloPreferenceStore.getPreferences().getLastIbdPortfolioPath();
        if (prev_path == null)//don't exist in preference
            prev_path = FrameworkConstants.DATA_FOLDER_EXPORT;
        fc = new JFileChooser(new File(prev_path));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle(ApolloConstants.APOLLO_BUNDLE.getString("imp_ttl_port"));
        ret = fc.showOpenDialog(MdiMainFrame.getInstance());
        if (ret != JFileChooser.APPROVE_OPTION) return;
        ApolloPreferenceStore.getPreferences().setLastIbdPortfolioPath(fc.getCurrentDirectory().getPath());//save location
        ApolloPreferenceStore.savePreferences();//flush
        _RootFolder = fc.getSelectedFile();

        //extract date from root folder for IBD portfolio
        String folder_name = _RootFolder.getName();
        if (!folder_name.startsWith("Portfolio")) {
            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("imp_bad_folder"));
            return;
        }
        final Calendar folder_cal = IbdRating.extractPortfolioTime(folder_name);
        if (folder_cal == null) {
            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("imp_bad_folder"));
            return;
        }

        //run the rest in thread, dump results to text area
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("imp_ibd_start"));
        pb.setVisible(true); _txaViewer.setText("");
        Thread import_thread = new Thread() {
            public void run() {
                //IBD50 import
                HashMap<String, IbdRating> cur_rating = IbdRating.ibd50SheetToMap(ibd50_sheet);
                String date = FileUtil.removeExtension(ibd50_sheet.getName(), FrameworkConstants.EXTENSION_XLS);//file name is date
                IbdRating.ratingMapToDb(cur_rating, AppUtil.stringToCalendarNoEx(date));
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        String msg = ApolloConstants.APOLLO_BUNDLE.getString("imp_ibd50_end") + " "
                            + ibd50_sheet.getAbsolutePath();
                        pb.setLabel(msg); _txaViewer.append(msg + "\n");
                    }
                });

                //IBD portfolio import
                for (final File file_obj : _RootFolder.listFiles()) {
                    if (!file_obj.getName().startsWith("Portfolio")) continue;//only files start with Por, filter out junk
                    try {
                        cur_rating = IbdRating.ibdPortfolioToMap(file_obj, folder_cal);
                        IbdRating.ratingMapToDb(cur_rating, folder_cal);//handle exception within call
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                String msg = ApolloConstants.APOLLO_BUNDLE.getString("imp_port_end") + " " + file_obj.getAbsolutePath();
                                pb.setLabel(msg);
                                _txaViewer.append(msg + "\n");
                            }
                        });
                    } catch (IOException | BiffException e) {
                        e.printStackTrace();
                        LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_read") + " " +
                            file_obj.getAbsolutePath(), LoggingSource.DATAMGR_IMPORT);
                    }
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        _txaViewer.append(ApolloConstants.APOLLO_BUNDLE.getString("imp_port_end_folder") + " "
                            + _RootFolder.getAbsolutePath() + "\n");
                    }
                });
                EventQueue.invokeLater(new Runnable() {
                    public void run() { pb.setVisible(false); }
                });
            }
        };
        import_thread.start();
    }
    //from many IBD50 and IBD Portfolio spreadsheets from many folders
    private void bulkImportRatings() {
        //open file dialog with .xls extension, use pref to get last stored import path
        String lip = ApolloPreferenceStore.getPreferences().getLastIbd50Path();
        if (lip == null)
            lip = FrameworkConstants.DATA_FOLDER_IMPORT;

        //ask user for IBD50 location
        JFileChooser fc = new JFileChooser(new File(lip));
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;
                int pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_XLS);
                return (pos > 0);
            }
            public String getDescription() {//this shows up in description field of dialog
                return ApolloConstants.APOLLO_BUNDLE.getString("act_lbl_2");
            }
        });
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle(ApolloConstants.APOLLO_BUNDLE.getString("imp_ttl_ibd50"));
        int ret = fc.showOpenDialog(MdiMainFrame.getInstance());
        if (ret != JFileChooser.APPROVE_OPTION) return;
        ApolloPreferenceStore.getPreferences().setLastIbd50Path(fc.getCurrentDirectory().getPath());//save location
        ApolloPreferenceStore.savePreferences();//flush
        final File[] ibd50_sheets = fc.getSelectedFiles();

        //ask user for IBD Portfolio location
        //ask user to select top level folder (IBD Portfolio usually)
        String prev_path = ApolloPreferenceStore.getPreferences().getLastIbdPortfolioPath();
        if (prev_path == null)//don't exist in preference
            prev_path = FrameworkConstants.DATA_FOLDER_EXPORT;
        fc = new JFileChooser(new File(prev_path));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setDialogTitle(ApolloConstants.APOLLO_BUNDLE.getString("imp_ttl_port"));
        ret = fc.showOpenDialog(MdiMainFrame.getInstance());
        if (ret != JFileChooser.APPROVE_OPTION) return;
        ApolloPreferenceStore.getPreferences().setLastIbdPortfolioPath(fc.getCurrentDirectory().getPath());//save location
        ApolloPreferenceStore.savePreferences();//flush
        _RootFolder = fc.getSelectedFile();

        //run the rest in thread, dump results to text area
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("imp_ibd_start"));
        pb.setVisible(true); _txaViewer.setText("");
        Thread import_thread = new Thread() {
            public void run() {
                _mapIbdRating = new HashMap<>();//start with empty map

                //first IBD50 sheets
                for (final File sheet : ibd50_sheets) {
                    CoreUtil.setDeltaTimeStart("");
                    IbdRating.importIbd50(sheet, _mapIbdRating);
                    EventQueue.invokeLater(new Runnable() { public void run() {
                        pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("imp_msg_fi") + " " + sheet.getName());
                        long delta = CoreUtil.setDeltaTimeFinish();
                        _txaViewer.append(ApolloConstants.APOLLO_BUNDLE.getString("imp_msg_fi") + " " +
                            sheet.getAbsolutePath() + " [" + delta + " ms]\n"); } });
                }

                //then IBD Portfolios
                //look up all folders with "Portfolio YYYY-MM-DD" pattern, order by descending time
                String[] files = _RootFolder.list();
                ArrayList<Calendar> dates = new ArrayList<>();
                for (String f : files) {
                    if (!f.startsWith("Port")) continue;
                    Calendar folder_cal = IbdRating.extractPortfolioTime(f);
                    dates.add(folder_cal);
                }
                Collections.sort(dates);//default ascending in time
                Collections.reverse(dates);
                EventQueue.invokeLater(new Runnable() { public void run() { pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_4")); } });
                for (Calendar cal : dates) {
                    CoreUtil.setDeltaTimeStart("");
                    final String folder_name = FrameworkConstants.IBD_RATING_SOURCE_PREFIX + FrameworkConstants.YAHOO_DATE_FORMAT.format(cal.getTime());
                    final File folder = new File(_RootFolder.getPath() + File.separator + folder_name);
                    File[] portfolio_files = folder.listFiles();
                    IbdRating.importIbdPortfolio(portfolio_files, _mapIbdRating, cal);//merge into map
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            long delta = CoreUtil.setDeltaTimeFinish();
                            _txaViewer.append(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_7") + ": " + folder_name + " [" + delta + " ms]\n");
                            pb.setLabel(folder.getAbsolutePath());
                        }
                    });
//                    try { sleep(10); } catch (InterruptedException e) { }
                }

                //write big map into database files
                try {
                    IbdRating.createRatingDb(_mapIbdRating);
                } catch (IOException e) {
                    e.printStackTrace();//TODO log
                }

                //notify user about completion
                EventQueue.invokeLater(new Runnable() {
                    public void run() {//take down progress bar
                        pb.setVisible(false);
                    }
                });
            }
        };
        import_thread.start();
    }

    //----- inner classes -----
    private class EarningThread extends Thread {
        private EarningThread(File root_folder) {//of Finviz folders
            pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
            _RootFolder = root_folder; pb.setVisible(true);
        }
        public void run() {
            String[] files = _RootFolder.list();
            ArrayList<Calendar> dates = new ArrayList<>();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_4"));
                }
            });
            for (String f : files) {
                if (!f.endsWith("csv")) continue;
                String date_str = f.substring(0, f.indexOf(FrameworkConstants.EXTENSION_CSV));//date = name w/o extension
                try {
                    Date dt = FrameworkConstants.YAHOO_DATE_FORMAT.parse(date_str);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dt);
                    dates.add(cal);
                } catch (ParseException e) {
                    System.err.println(e.getMessage());   //e.printStackTrace();
                    LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_earning1") + " " +
                        date_str, LoggingSource.DATAMGR_IMPORT);
                }
            }
            Collections.sort(dates);//default ascending in time
            _mapEarningDates = new HashMap<>();
            for (final Calendar cal : dates) {
                File finviz_file = new File(_RootFolder.getAbsolutePath() + File.separator +
                    AppUtil.calendarToString(cal) + FrameworkConstants.EXTENSION_CSV);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_pb_earning") + " " + AppUtil.calendarToString(cal));
                    }
                });
                String name = finviz_file.getName();
                System.err.println("Reading Finviz Export: " + name);
                try {
                    BufferedReader br = new BufferedReader(new FileReader(finviz_file));
                    br.readLine();//skip first line header
                    String csv_line;
                    while ( (csv_line = br.readLine()) != null ) {
                        csv_line = DataUtil.preprocessFinvizLine(csv_line, true);
                        String[] tokens = csv_line.split(",");
                        String symbol = tokens[Fundamental.TOKEN_SYMBOL];
                        String earning_str = null;
                        if (tokens.length > Fundamental.TOKEN_EARNING_DATE)
                            earning_str = tokens[Fundamental.TOKEN_EARNING_DATE];
                        if (earning_str == null || earning_str.equals("")) continue;//skip empty
                        ArrayList<Calendar> earning_dates = _mapEarningDates.get(symbol);
                        if (earning_dates == null) {
                            earning_dates = new ArrayList<>();//start fresh
                            _mapEarningDates.put(symbol, earning_dates);
                        }

                        //determine earning reported AM or PM based on string pattern [4/27/14 16:05] or w/o time
                        Calendar e_cal = null;
                        try {//earning string must have a blank, parse first part date normally
                            if (earning_str.contains(" ")) {
                                int idx = earning_str.indexOf(" ");
                                Date e_date = FINVIZ_EARNING_DATE_FORMAT2.parse(earning_str.substring(0, idx));//AM format
                                e_cal = Calendar.getInstance(); e_cal.setTime(e_date);

                                //try to get time string from AM/PM to decide gap is earning related
                                // sometimes no AM/PM, instead 24 hr clock, then parse hour
                                String time_str = earning_str.substring(idx + 1);
                                if (time_str.contains("AM"))
                                    e_cal.set(Calendar.HOUR_OF_DAY, 8);
                                else if (time_str.contains("PM"))
                                    e_cal.set(Calendar.HOUR_OF_DAY, 16);
                                else {
                                    String[] time_tokens = time_str.split(":");
                                    int hr = Integer.parseInt(time_tokens[0]);
                                    //observed 7:30, 16:30, 2:00, 8:30, 7:00, 1:30, 3:15, 5:00, 6:00, 8:00,
                                    //   18:30, 17:00, 16:00, 20:00, 22:00, 0:45, 1:00
                                    if (hr < 16)
                                        e_cal.set(Calendar.HOUR_OF_DAY, 8);
                                    else
                                        e_cal.set(Calendar.HOUR_OF_DAY, 16);
//            System.err.println(name + " | " + symbol + "----[" + time_str + "]----<" + earning_str + ">");
                                }
                            }
                            else {//standard parsing like 5/30/14
                                Date e_date = FINVIZ_EARNING_DATE_FORMAT2.parse(earning_str);//AM format
                                e_cal = Calendar.getInstance(); e_cal.setTime(e_date);
                                e_cal.set(Calendar.HOUR_OF_DAY, 8);
                            }
                        } catch (ParseException e) {
                            System.err.println(name + ": " + e.getMessage());//can't parse at all
                            LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_earning2") + " " +
                                name, LoggingSource.DATAMGR_IMPORT);
                            continue;
                        }
//TODO Finsished reading one line, attempt to insert into map at right posistion
                        long e_cal_ms = e_cal.getTimeInMillis(); boolean match = false;
                        for (Calendar found_cal : earning_dates) {//is this already in map?
                            long found_ms = found_cal.getTimeInMillis();
                            if (found_ms == e_cal_ms) {
                                match = true;
                                break;
                            }
                        } if (match) continue;//ignore this e_cal, since already on the list
                        if (earning_dates.size() == 0) {
                            earning_dates.add(e_cal);
                            continue;
                        }

                        //qualify this new e_cal if it's more than 30 days away from last one
                        //  otherwise replace last one in array with this new e_cal
                        int last_idx = earning_dates.size() - 1;
                        Calendar last_cal = earning_dates.get(last_idx);
                        long last_cal_ms = last_cal.getTimeInMillis();
                        long diff = e_cal_ms - last_cal_ms;
                        if (diff > MILLISEC_PER_MONTH)
                            earning_dates.add(e_cal);//far apart, add to end
                        else {
                            earning_dates.remove(last_idx);
                            earning_dates.add(e_cal);
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    System.err.println(name + ": " + e.getMessage());
                }
            }
            MainModel.getInstance().setEarningDates(_mapEarningDates);
            CoreUtil.setDeltaTimeStart("");
            EarningStore.saveEarningDb(_mapEarningDates);//this one takes a long time
            CoreUtil.showDeltaTime("<ImportPanel.EarningStore.saveEarningDb()> " + _mapEarningDates.size());
            EventQueue.invokeLater(new Runnable() {
                public void run() { pb.setVisible(false); }
            });
        }
        private ProgressBar pb;
    }
    private class IbdThread extends Thread {
        private IbdThread(File root_folder) {//of portfolio folders
            pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
            _RootFolder = root_folder;
            pb.setVisible(true);
        }
        public void run() {
            //obtain folder list first, then order them based on dates with latest dates first
            final ArrayList<LogMessage> failed_files = new ArrayList<>();
            String[] files = _RootFolder.list();
            ArrayList<Calendar> dates = new ArrayList<>();
            for (String f : files) {
                if (!f.startsWith("Port")) continue;
                int idx = f.indexOf(" ");//get tail part of folder name = date
                String date_str = f.substring(idx + 1, f.length());//tail is date YYYY-MM-DD
                try {
                    Date dt = FrameworkConstants.YAHOO_DATE_FORMAT.parse(date_str);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dt);
                    dates.add(cal);
                } catch (ParseException e) {    e.printStackTrace();
                    //TODO log error
                }
            }
            Collections.sort(dates);//default ascending in time
            Collections.reverse(dates);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_4"));
                }
            });
//TODO replace this block with DataUtil.importSingleIbd.....
            //for each date (from most recent to earliest), open corresponding folder
            for (Calendar cal : dates) {
                String folder_name = FrameworkConstants.IBD_RATING_SOURCE_PREFIX + FrameworkConstants.YAHOO_DATE_FORMAT.format(cal.getTime());
                final File folder = new File(_RootFolder.getPath() + File.separator + folder_name);
                File[] portfolio_files = folder.listFiles();

                //for each .xls file in this folder, read its 50 symbols, form a line for each, write/append to matching files
                for (final File file_obj : portfolio_files) {
                    if (!file_obj.getName().startsWith("Por")) continue;//only files start with Por
                    CoreUtil.setDeltaTimeStart("");
                    Workbook wb = null;
                    try { //open this sheet, read between row 5-59 to retrieve 6 ratings and symbol
                        wb = Workbook.getWorkbook(file_obj);
                        Sheet sheet = wb.getSheet(0);
                        for (int row = IbdInfo.ROW_IBDPORT_SYMBOL_BEGIN; row <= IbdInfo.ROW_IBDPORT_SYMBOL_END; row++) {//IBD should fill between 6 and 60
                            String composite = sheet.getCell(IbdInfo.COLUMN_IBDPORT_COMPOSITE, row).getContents();
                            if (composite.equals(""))//done here on empty row
                                break;

                            //read fields into a buffer
                            StringBuilder buf = new StringBuilder();
                            String symbol = sheet.getCell(IbdInfo.COLUMN_IBDPORT_SYMBOL, row).getContents();
                            if (symbol.equals("BRKB"))
                                symbol = "BRK-B";
                            buf.append(FrameworkConstants.YAHOO_DATE_FORMAT.format(cal.getTime())).append(",").append(composite).append(",")
                                .append(sheet.getCell(IbdInfo.COLUMN_IBDPORT_EPS_RATING, row).getContents()).append(",")
                                .append(sheet.getCell(IbdInfo.COLUMN_IBDPORT_RS_RATING, row).getContents()).append(",")
                                .append(sheet.getCell(IbdInfo.COLUMN_IBDPORT_SMR_RATING, row).getContents()).append(",")
                                .append(sheet.getCell(IbdInfo.COLUMN_IBDPORT_ACC_DIS_RATING, row).getContents()).append(",")
                                .append(sheet.getCell(IbdInfo.COLUMN_IBDPORT_GROUP_RATING, row).getContents());

                            //rating folder should have already been created
                            File db_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_DB);
                            if (!db_folder.exists()) //remove all files in folder, keep folder
                                db_folder.mkdir();
                            File rating_folder = new File(FrameworkConstants.DATA_FOLDER_IBD_RATING);
                            if (!rating_folder.exists())
                                rating_folder.mkdir();
                            String name = FrameworkConstants.DATA_FOLDER_IBD_RATING + File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE;
                            boolean exist = new File(name).exists();
                            if (!exist) {
                                PrintWriter pw = new PrintWriter(new FileWriter(name));//create
                                pw.println("Date(YYYY-MM-DD), Composite, EPS, RS, SMR, Acc-Dis, Group Strength");
                                pw.println(buf.toString());
                                pw.close();
                            }
                            else {
                                PrintWriter pw = new PrintWriter(new FileWriter(name, true));//append
                                pw.println(buf.toString());
                                pw.close();
                            }
                            final String sym = symbol;
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_5") + file_obj.getName() +
                                        ApolloConstants.APOLLO_BUNDLE.getString("dmi_msg_6") + folder.getName() +
                                        "] " + sym + "...");
                                }
                            });
                            sleep(1);//allow EDT to run and show message
                        }
                        wb.close();
                        CoreUtil.showDeltaTime("<ImportPanel.IbdThread().readSheet()> " + folder.getName() + "/" + file_obj.getName());
                    } catch (Exception e) {
                        //TODO log error , can't read this file.....
                        System.err.println("File -----> " + file_obj.getName());
                        e.printStackTrace();
                        if (wb != null)
                            wb.close();
                    }
                }
            }
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    pb.setVisible(false);
                    if (failed_files.size() > 0) {
                        //TODO to logging window
                    }
                }
            });

        }
        private ProgressBar pb;
    }

    //----- instance variables -----
    private JComboBox<String> _cmbType = new JComboBox<>(LIST_IMPORT_TYPE);
    private JButton _btnEarning = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmi_earning_date"), FrameworkIcon.DOLLAR_16);
    private JButton _btnImportBulkIbdPortfolio = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmi_tip_8"), FrameworkIcon.FILES);
    private JButton _btnImportSingleIbdPortfolio = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmi_tip_6"), FrameworkIcon.FILE_ADD);
    private JTextArea _txaViewer = new JTextArea();
    private HashMap<String, ArrayList<IbdRating>> _mapIbdRating;
    private File _RootFolder;
    private HashMap<String, ArrayList<Calendar>> _mapEarningDates;

    //----- literals -----
    private final static long MILLISEC_PER_MONTH = 30 * 24 * 60 * 60 * 1000L;
    private final static int TYPE_SNAPSHOT_IBD_ETF = 0;
    private final static int TYPE_FINVIZ = 1;
    private final static int TYPE_MS_WATCH_LISTS = 2;
    private final static int TYPE_TS_POSITION = 4;
    private final static String[] LIST_IMPORT_TYPE = {
        "IBD ETF (.xls) Snapshot",
        "Finviz (.csv) Fundamental",
        "Merge External Market Suite Watch List (.xml)",
        "TradeStation Positions to Watch List (.xls)",
    };
    private static final SimpleDateFormat FINVIZ_EARNING_DATE_FORMAT1 = new SimpleDateFormat("MM/dd/yy HH:mm");
    private static final SimpleDateFormat FINVIZ_EARNING_DATE_FORMAT2 = new SimpleDateFormat("MM/dd/yy");
}
