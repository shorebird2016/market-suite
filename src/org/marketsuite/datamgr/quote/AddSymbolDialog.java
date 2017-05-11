package org.marketsuite.datamgr.quote;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Container for downloading new historical price quotes.
 */
public class AddSymbolDialog extends JDialog {
    public AddSymbolDialog() {
        super(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_4"), false);//non-modal to allow progress bar to show
        setContentPane(new SymbolsPanel());
//TODO disable close button
//        addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                //don't allow quitting if updating
//                if (_bDownloading) {
//                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dmq_txt_4"));
//                    return;
//                }
//                dispose();
//            }
//        });
        WidgetUtil.setDialogProperties(this, new Dimension(300, 400), true, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE, false);
    }

    //populate add symbol dialog with list of symbols
    void setSymbols(ArrayList<String> symbols) {
        _txaSymbols.setText("");
        for (String str : symbols)
            _txaSymbols.append(str + ",");
    }

    //download new quotes using symbols listed in _txaSymbols, duplicate quote will be overwritten
    private void getQuotes() {
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
        pb.setVisible(true);
        Thread thread = new Thread() {
            public void run() {
                final ArrayList<LogMessage> err = new ArrayList<>();

                //read symbol list to download
                String names = _txaSymbols.getText();
                String[] name_list = names.split(Constants.REGEX_SYMBOL_SPLITTER);
                for (final String symbol : name_list) {
                    if (symbol == null || symbol.isEmpty())
                        continue;

                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                        pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("dld_msg_1")
                        + " " + symbol.toUpperCase() + ApolloConstants.APOLLO_BUNDLE.getString("dld_msg_3"));
                        }
                    });
                    try {
                        DataUtil.downloadDailyQuote(symbol.toUpperCase());
                    } catch (IOException e) {//continue after error, but log error
//                        e.printStackTrace();
                        LogMessage lm = new LogMessage(LoggingSource.DATAMGR_QUOTE,
                            symbol + ApolloConstants.APOLLO_BUNDLE.getString("exp_msg_3") + "["
                                    + symbol + "] " + ApolloConstants.APOLLO_BUNDLE.getString("exp_msg_4") + "   ", e);
                        err.add(lm);
//                        err.add(ApolloConstants.APOLLO_BUNDLE.getString("exp_msg_3") + "["
//                            + symbol + "] " + ApolloConstants.APOLLO_BUNDLE.getString("exp_msg_4") + "   "
//                            + e.getMessage());
                    }
                }

                //take down progress wheel
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setVisible(false);
                        FrameworkConstants.populateSp500Data(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
                        MdiMainFrame.getInstance().findDataMgrFrame().getMainPanel().getQuotePanel().refreshQuoteList();

                        //when there is error, show in dialog
                        if (err.size() > 0) {
                            Props.Log.setValue(null, err);
//                            new MultiMessageDialog(MdiMainFrame.getInstance(),
//                            Constants.COMPONENT_BUNDLE.getString("warning"), err);
                        }
                    }
                });
            }
        };
        thread.start();//must NOT join thread here, otherwise this dialog will block progress bar

        //exit dialog
        dispose();
    }

    /**
     * Container for downloading new quotes.
     */
    private class SymbolsPanel extends JPanel {
        private SymbolsPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new BevelBorder(BevelBorder.LOWERED));
            add(WidgetUtil.createTitleStrip(ApolloConstants.APOLLO_BUNDLE.getString("dmq_txt_6")), BorderLayout.NORTH);
            add(new JScrollPane(_txaSymbols), BorderLayout.CENTER);
            SkinPanel btn_pnl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new FlowLayout(FlowLayout.RIGHT));
            btn_pnl.setOpaque(false);
            JButton btn = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_2"));
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getQuotes();
                }
            });
            btn_pnl.add(btn);
            add(btn_pnl, BorderLayout.SOUTH);
//            WidgetUtil.setMaxMinPrefSize(this, 100, 50);
        }
    }

    private JTextArea _txaSymbols = new JTextArea();
//    private boolean _bDownloading;
}
