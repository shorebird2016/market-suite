package org.marketsuite.datamgr.quote;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.FileUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXHyperlink;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Container for stock quote related functions such as update and download, also viewing quote files.
 */
public class QuotePanel extends SkinPanel implements PropertyChangeListener {
    public QuotePanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new MigLayout());

        //west - add/update status area
        JPanel stat_pnl = new JPanel(new MigLayout("insets 0 2 2 5, flowy, fill")); stat_pnl.setOpaque(false);
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 8 0 8 0", "5[]5[]5[]5[]5"));
        ttl_pnl.setOpaque(false);
        ttl_pnl.add(_lnkUpdate = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_1,
                ApolloConstants.APOLLO_BUNDLE.getString("dmq_lnk_1"),
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _txaStatus.setText("");
                        _txaError.setText("");
                        _lnkUpdate.setEnabled(false);//disallow click once more
                        _lnkAddSymbol.setEnabled(false);
                        UpdateThread th = new UpdateThread();
                        th.start();
                    }
                }));
        _lnkUpdate.setForeground(Color.blue);
        _lnkUpdate.setClickedColor(Color.blue);
        WidgetUtil.attachToolTip(_lnkUpdate, ApolloConstants.APOLLO_BUNDLE.getString("dmq_txt_2"), SwingConstants.LEFT, SwingConstants.TOP);
        ttl_pnl.add(_lnkAddSymbol = WidgetUtil.createHyperLink(WidgetUtil.LEVEL_1, ApolloConstants.APOLLO_BUNDLE.getString("dmq_lnk_2"),
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    AddSymbolDialog adlg = new AddSymbolDialog();
                    adlg.setVisible(true);
                }
            }));
        _lnkAddSymbol.setForeground(Color.blue);
        _lnkAddSymbol.setClickedColor(Color.blue);
        ttl_pnl.add(_btnVerifyAll); _btnVerifyAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _txaStatus.setText("");
                _txaError.setText("");
                ValidateThread th = new ValidateThread();
                th.start();
            }
        });
        ttl_pnl.add(_btnDivDld); _btnDivDld.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //look for all symbols in quote database and download their dividends into files
                DividendThread dth = new DividendThread(); dth.start();
            }
        });
        WidgetUtil.attachToolTip(_lnkAddSymbol, ApolloConstants.APOLLO_BUNDLE.getString("dmq_txt_3"), SwingConstants.LEFT, SwingConstants.TOP);
        stat_pnl.add(ttl_pnl, "dock north");
        stat_pnl.add(new JScrollPane(_txaStatus), ""); _txaStatus.setEditable(false);
        _txaStatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
        stat_pnl.add(WidgetUtil.createTitleStrip(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_10")), "growx");
        stat_pnl.add(new JScrollPane(_txaError), "growx"); _txaError.setEditable(false);
        _txaError.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(stat_pnl, "dock west");

        //center - title bar, splitter
        JPanel cen_pnl = new JPanel(new MigLayout());  cen_pnl.setOpaque(false);
        cen_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
        ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER,
            new MigLayout("insets 6 0 6 0", "5px[]push[]10px[]5px"));  ttl_pnl.setOpaque(false);
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmq_txt_1"));
        lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl_pnl.add(lbl);
        ttl_pnl.add(_btnVerify);
        _btnVerify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String symbol = _lstQuote.getSelectedValue();
                String path = FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + symbol;
                try {
                    ArrayList<String> failed_lines = DataUtil.validateQuotes(path);
                    if (failed_lines.size() > 0) {
                        ArrayList<LogMessage> msgs = new ArrayList<>();
                        for (String fl : failed_lines) {
                            LogMessage lm = new LogMessage(LoggingSource.DATAMGR_QUOTE,
                                    ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_11") + " " + fl, null);
                            msgs.add(lm);
//TODO                            LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("rm_17") + pf.getName() + "<br><br>", LoggingSource.RISKMGR_ACCOUNT, e);
                        }
                        Props.Log.setValue(null, msgs);
                    }
                } catch (IOException e1) {
                    MessageBox.messageBox(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_11"),
                            ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_9") + path);
                }
            }
        });
        ttl_pnl.add(_btnRefresh);
        _btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshQuoteList();
            }
        });
        ttl_pnl.add(_btnBackup);
        _btnBackup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//TODO move code into thread such that status will scroll as copy is going on, otherwise it will wait till end
                //make copy of entire database under backup root in a folder using today's date
                File backup_root = new File(FrameworkConstants.DATA_FOLDER_BACKUP);
                if (!backup_root.exists())
                    backup_root.mkdir();
                Calendar today = Calendar.getInstance();
                String today_str = FrameworkConstants.YAHOO_DATE_FORMAT.format(today.getTime());
                File backup_folder = new File(FrameworkConstants.DATA_FOLDER_BACKUP + File.separator + today_str);
                if (backup_folder.exists())
                    FileUtil.delete(backup_folder);
                backup_folder.mkdir();
                try {
                    _txaStatus.setText("");
                    _txaError.setText("");
                    Path wl_file_path = Paths.get(GroupStore.WATCHLIST_PREF_PATH);
                    Path dest_path = Paths.get(backup_folder.getAbsolutePath() + File.separator + GroupStore.FILE_NAME);
                    Files.copy(wl_file_path, dest_path);
                    Path app_pref_path = Paths.get(ApolloPreferenceStore.APP_PREF_PATH);
                    dest_path = Paths.get(backup_folder.getAbsolutePath() + File.separator + ApolloPreferenceStore.FILE_NAME);
                    Files.copy(app_pref_path, dest_path);
                    FileUtil.copyFolder(new File(FrameworkConstants.DATA_FOLDER), backup_folder, _txaStatus);
                    _txaStatus.append(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_15") + " " + dest_path.getFileName());
                } catch (IOException ioe) {
                    _txaError.append(ioe.getMessage());
                    ioe.printStackTrace();
                }
            }
        });
        ttl_pnl.add(_btnDelete);
        _btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //nothing selected, no nothing
                List<String> symbols = _lstQuote.getSelectedValuesList();
                if (symbols.size() <= 0) return;
                if (MessageBox.messageBox(null,
                        Constants.COMPONENT_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_16"),
                        MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                    return;
                ArrayList<String> delete_symbols = new ArrayList<>();
                for (String symbol : symbols) {
                    new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + symbol).delete();
                    delete_symbols.add(symbol.substring(0, symbol.indexOf(FrameworkConstants.EXTENSION_QUOTE)));
                }
                GroupStore.getInstance().removeSymbols(delete_symbols);
                refreshQuoteList();
            }
        });
        ttl_pnl.add(_btnFindObsolete);
        _btnFindObsolete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent act) {
                //use SP500 in memory quotes as a guide, search all quotes to find any ones don't have matching date
                ArrayList<String> maybe_obs = new ArrayList<>();
                String cur_date = FrameworkConstants.SP500_DATA.getQuote().get(0).getDate();
                for (int i = 0; i < _lmQuote.getSize(); i++) {
                    String file_name = _lmQuote.get(i);
                    String symbol = file_name.substring(0, file_name.indexOf(FrameworkConstants.EXTENSION_QUOTE));
                    try {
                        FundData fund = DataUtil.readQuotes(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, 1);
                        String qd = fund.getDate(0);
                        if (qd.equals(cur_date)) continue;
                        maybe_obs.add(symbol + " - " + qd);
                    } catch (IOException e) {
                        LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_01") + " " + symbol, LoggingSource.RISKMGR_ACCOUNT, e);
                    }
                }
                if (maybe_obs.size() == 0) {
                    MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_13"));
                    return;
                }

                //put them in a non-modal window, give user a choice to delete some of them
                ObsoleteSymbolsDialog dlg = new ObsoleteSymbolsDialog(maybe_obs);
                if (dlg.isCancelled()) return;
                ArrayList<String> delete_list = dlg.getSelection();
                for (String symbol : delete_list)
                    new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + symbol + FrameworkConstants.EXTENSION_QUOTE).delete();

                //delete symbols from all watch lists, ask confirmation
                if (MessageBox.messageBox(null,
                        Constants.COMPONENT_BUNDLE.getString("warning"),
                        ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_16"),
                        MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                    return;
                GroupStore.getInstance().removeSymbols(delete_list);
                refreshQuoteList();
            }
        });
        cen_pnl.add(ttl_pnl, "dock north");

        //splitter
        JSplitPane hor_spl = new JSplitPane();
        hor_spl.setDividerLocation(150);
        hor_spl.setContinuousLayout(true);

        //left of splitter - list of quote files
        JScrollPane sp = new JScrollPane(_lstQuote);
        sp.setBorder(new BevelBorder(BevelBorder.RAISED));
        _lstQuote.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (_lstQuote.getSelectedIndex() == -1)//no selection
                    return;

                try {//open first file, show in content pane
                    String file_name = _lstQuote.getSelectedValue();
                    DataUtil.displayFile(new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + file_name), _txaFileContent);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(),//don't have parents under ITE
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("dme_txt_4") + ioe.getMessage());
                }
            }
        });
        hor_spl.setLeftComponent(sp);
        hor_spl.setRightComponent(new JScrollPane(_txaFileContent)); _txaFileContent.setEditable(false);
        cen_pnl.add(hor_spl, "dock center");

        //south - number of files
        JPanel south_pnl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new FlowLayout(FlowLayout.LEFT));
        south_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("dmq_txt_5") + " "));
        south_pnl.add(_lblCount);
        cen_pnl.add(south_pnl, "dock south");
        add(cen_pnl, "dock center");
        refreshQuoteList();
        Props.addWeakPropertyChangeListener(Props.AddSymbols, this);//handle symbol change
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Props prop = (Props) evt.getSource();
        switch (prop) {
            case AddSymbols://update graph
                //place symbols into Add symbol dialog
                AddSymbolDialog dlg = new AddSymbolDialog();
                dlg.setSymbols((ArrayList<String>) prop.getValue());
                dlg.setVisible(true);
                break;
        }
    }

    //----- public methods -----
//TODO this method costs over 1 second, put into background
    //initialize file list in _lstQuote
    public void refreshQuoteList() {
CoreUtil.setDeltaTimeStart("");
        File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        String[] file_list = folder.list();//read file list
        _lmQuote = new DefaultListModel<>();
        for (String f : file_list) {
            //skip hidden files or non .txt files
            if (f.endsWith(FrameworkConstants.EXTENSION_QUOTE) && !f.startsWith("."))
                _lmQuote.addElement(f);
        }
        _lblCount.setText(String.valueOf(_lmQuote.size()));
        _lstQuote.setModel(_lmQuote);
        _lstQuote.setSelectedIndex(0);
        try {//open first file, show in content pane
            String file_name = _lstQuote.getSelectedValue();
            DataUtil.displayFile(new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + file_name), _txaFileContent);
        } catch (IOException e) {
            e.printStackTrace();
            WidgetUtil.showWarning(MdiMainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("dme_txt_4") + e.getMessage());
        }
CoreUtil.showDeltaTime("<QuotePanel.refreshQuoteList()>");
    }

    //----- inner classes -----
    private class DividendThread extends Thread {
        public void run() {
            final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("imp_dld_div"));
            pb.setVisible(true); File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
            String[] file_list = folder.list();
            for (String file : file_list) {
                if (!file.endsWith(FrameworkConstants.EXTENSION_QUOTE)) //only .txt files
                    continue;

                //extract symbol from file name
                final String symbol = file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE));
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                    pb.setLabel("Downloading Dividends...." + symbol);
                    }
                });
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
    //update all quote files in database using Yahoo service
    private class UpdateThread extends Thread {
        public void run() {
            File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
            _SymbolsInError = new ArrayList<>();
            String[] file_list = folder.list();
            for (String file : file_list) {
                if (!file.endsWith(FrameworkConstants.EXTENSION_QUOTE)) //only .txt files
                    continue;

                //extract symbol from file name
                final String symbol = file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE));
                try {
                    if (DataUtil.updateQuoteFromYahoo(symbol, _txaStatus)) {
                        final String msg = symbol + ": " + ApolloConstants.APOLLO_BUNDLE.getString("dmq_txt_7") + "\n";
                        DataUtil.showMsgViaEdt(_txaStatus, msg);
                        System.err.print(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final String msg =  symbol + " " + ApolloConstants.APOLLO_BUNDLE.getString("dmq_txt_8") + "\n";
                    DataUtil.showMsgViaEdt(_txaError, msg);
                    System.err.print(msg);
                    _SymbolsInError.add(symbol);
                }
            }

            //refresh SP500 in memory data
            FrameworkConstants.populateSp500Data(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    //force restart
                    WidgetUtil.showMessageNoEdt(ApolloConstants.APOLLO_BUNDLE.getString("dm_01"));
                    System.exit(0);
//                    _lnkUpdate.setEnabled(true);
//                    _lnkAddSymbol.setEnabled(true);
//                    refreshQuoteList();
                }
            });
        }
    }
    private class ValidateThread extends Thread {
        public void run() {
            _SymbolsInError = new ArrayList<>();
            File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
            String[] file_list = folder.list();
            for (String file : file_list) {
                if (!file.endsWith(FrameworkConstants.EXTENSION_QUOTE)) //only .txt files
                    continue;

                //extract symbol from file name
                final String symbol = file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE));
                String path = FrameworkConstants.DATA_FOLDER_DAILY_QUOTE + File.separator + file;
                try {
                    ArrayList<String> failed_lines = DataUtil.validateQuotes(path);
                    if (failed_lines.size() > 0) {
                        DataUtil.showMsgViaEdt(_txaError, symbol + ": " + ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_3") + "\n");
                        _SymbolsInError.add(symbol);
                    }
                    else
                        DataUtil.showMsgViaEdt(_txaStatus, symbol + ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_1") + "\n");
                } catch (IOException e1) {
                    DataUtil.showMsgViaEdt(_txaError, symbol + ": " + ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_9") + "\n");
                    _SymbolsInError.add(symbol);
                }
            }

            //when there is some error, pop up add symbol dialog
            if (_SymbolsInError.size() > 0)
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        Props.AddSymbols.setValue(null, _SymbolsInError);//informs listener to start download
                    }
                });
        }
    }

    //-----instance variables-----
    private JList<String> _lstQuote = new JList<>();
    private DefaultListModel<String> _lmQuote;
    private JTextArea _txaFileContent = new JTextArea();
    private JTextArea _txaStatus = new JTextArea(40, 30);
    private JTextArea _txaError = new JTextArea(20, 30);
    private JXHyperlink _lnkUpdate, _lnkAddSymbol;
    private JButton _btnDivDld = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("wlm_divdld"), FrameworkIcon.DOWNLOAD);
    private JButton _btnVerifyAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_5"), FrameworkIcon.VALIDATE);
    private JButton _btnVerify = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_6"), FrameworkIcon.VALIDATE);
    private JButton _btnRefresh = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_14"), FrameworkIcon.REFRESH);
    private JButton _btnBackup = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_7"), FrameworkIcon.BACKUP);
    private JButton _btnDelete = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_8"), LazyIcon.ICON_DELETED);
    private JButton _btnFindObsolete = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_12"), FrameworkIcon.QUESTION_MARK);
    private ArrayList<String> _SymbolsInError = new ArrayList<>();
    private JLabel _lblCount = new JLabel();
}
//TODO pop up add symbol dialog if there is error symbol