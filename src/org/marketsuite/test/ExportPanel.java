package org.marketsuite.test;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.StandardOption;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.framework.strategy.base.SimParam;
import org.marketsuite.framework.strategy.mac.MacEngine;
import org.marketsuite.framework.strategy.mac.MacOption;
import org.marketsuite.simulator.indicator.macd.MacdZeroCrossEngine;
import org.marketsuite.simulator.indicator.macd.MzcOption;
import org.marketsuite.simulator.indicator.rsi.RsiEngine;
import org.marketsuite.simulator.indicator.rsi.RsiOption;
import org.marketsuite.simulator.indicator.stochastic.StochasticEngine;
import org.marketsuite.simulator.indicator.stochastic.StochasticOption;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.SimUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.base.AbstractEngine;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.indicator.macd.MacdZeroCrossEngine;
import org.marketsuite.simulator.indicator.macd.MzcOption;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Container for stock quote related functions such as update and download, also viewing quote files.
 */
public class ExportPanel extends SkinPanel {
    public ExportPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());

        //south - some hints
        SkinPanel ttl_pnl = WidgetUtil.createTitleStrip(ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_1"));
        add(ttl_pnl, BorderLayout.NORTH);

        //center - splitter
        JSplitPane hor_spl = new JSplitPane();
        hor_spl.setDividerLocation(200);
        hor_spl.setContinuousLayout(true);

        //left - list of quote files
        _treNavigator = new JTree(createTreeRoot());
        _treNavigator.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                String file_name = (String)node.getUserObject();
                if (!file_name.endsWith(FrameworkConstants.EXTENSION_TRADES))
                    return;

                if (node.isLeaf()) {
                    String parent_name = (String)((DefaultMutableTreeNode)node.getParent()).getUserObject();
                    //open file, show in content
                    try {
                        showContent(FrameworkConstants.DATA_FOLDER_EXPORT + File.separator +
                            parent_name + File.separator + file_name);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        WidgetUtil.showWarning(MdiMainFrame.getInstance(), e1.getMessage());
                    }
                }
            }
        });
        hor_spl.setLeftComponent(new JScrollPane(_treNavigator));

        //right - content of file, update and download functions
        JSplitPane rite_pnl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rite_pnl.setDividerLocation(350);
        rite_pnl.setContinuousLayout(true);
        rite_pnl.setTopComponent(new JScrollPane(_txaContent));  _txaContent.setEditable(false);

        //update and download panels
        JPanel rs_pnl = new JPanel(new GridLayout(1, 2));//right, south
        rs_pnl.add(new ExportOptionPanel());
        rite_pnl.setBottomComponent(rs_pnl);
        add(hor_spl, BorderLayout.CENTER);
        hor_spl.setRightComponent(rite_pnl);

        //initialize - select and show first quote content, populate file list
    }

    //-----private methods-----
//todo consider merge with QuotePanel's same method
    //display file content in _txaContent window
    private void showContent(String file_path) throws IOException {
        _txaContent.setText("");
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        String line;
        //skip all comment lines
        while ( (line = br.readLine()) != null )
            _txaContent.append(line + "\n");
        _txaContent.setCaretPosition(0);//cursor back to beginning
    }

    // create the tree from the file system recursively.
    private DefaultMutableTreeNode createTreeRoot() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode();
        top.setUserObject(ApolloConstants.APOLLO_BUNDLE.getString("dme_lbl_2"));

        //add all strategy names as children to top level
        File export_folder = new File(FrameworkConstants.DATA_FOLDER_EXPORT);
        for (String str : STRATEGIES) {
            DefaultMutableTreeNode str_node = new DefaultMutableTreeNode(str);
            top.add(str_node);

            //find each strategy folder, get list of files, add then as children to each folder
            String folder_name = export_folder + File.separator + str;
            File file = new File(folder_name);

            //if folder doesn't exist, create one

            String[] file_list = file.list();

            for (String name : file_list) {
                DefaultMutableTreeNode file_node = new DefaultMutableTreeNode(name);
                str_node.add(file_node);
            }
        }
        return top;
    }

    /**
     * Use background thread to batch simulate and export .csv transaction files.
     */
    private void bulkExport() {
        //show progress bar
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(), "");
        pb.setVisible(true);

        //start a thread to simulate and export all files
        Thread thread = new Thread() {
            public void run() {
                final ArrayList<String> failed_symbols = new ArrayList<String>();

                //read list of symbols from files in folder
                File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
                String[] file_list = folder.list();
                for (String file : file_list) {
                    if (!file.endsWith(FrameworkConstants.EXTENSION_QUOTE))//skip non .txt files
                        continue;

                    final String symbol = file.substring(0, file.indexOf(FrameworkConstants.EXTENSION_QUOTE));
                    try {
                        FundData fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
                        AbstractEngine engine = _Engines[_nCurEngine];
                        engine.setFund(fund);

                        //setup simulation options, start with max range data
//                        int start_index = fund.getSize() -1 - MacEngine._nExitMA2;//adjust start index this much to allow look back
//                        if (start_index <= 0) //not enough data, skip
//                            continue;

                        String start_date = fund.getDate(fund.getSize() - 1);//full size
                        StandardOption std_opt = new StandardOption(symbol, true, false,
                            start_date, fund.getQuote().get(0).getDate(), true);

                        //custom options - all use default parameters
                        String export_path = "/MAC/";
                        switch (_nCurEngine) {
                            case INDEX_MAC:
                                MacOption mac_opt = new MacOption(MacEngine._nEntryMA1, MacEngine._nEntryMA2,
                                    MacEngine._nEntryMA2, MacEngine._nExitMA2);
                                engine.setSimParam(new SimParam(std_opt, mac_opt));
                                break;

                            case INDEX_MZC:
                                MzcOption mzc_opt = new MzcOption(MacdZeroCrossEngine.FAST_MA, MacdZeroCrossEngine.SLOW_MA);
                                engine.setSimParam(new SimParam(std_opt, mzc_opt));
                                export_path = "/MACD Zero Cross/";
                                break;

                            case INDEX_RSI:
                                RsiOption rsi_option = new RsiOption(RsiOption.DEFAULT_LENGTH,
                                    RsiOption.OVERSOLD_LEVEL, RsiOption.OVERBOUGHT_LEVEL);
                                engine.setSimParam(new SimParam(std_opt, rsi_option));
                                export_path = "/RSI/";
                                break;

                            case INDEX_STO:
                                StochasticOption sto_opt = new StochasticOption(StochasticOption.DEFAULT_LENGTH,
                                    StochasticOption.DEFAULT_MA_PERIOD, StochasticOption.OVERSOLD_LEVEL,
                                    StochasticOption.OVERBOUGHT_LEVEL);
                                engine.setSimParam(new SimParam(std_opt, sto_opt));
                                export_path = "/Stochastic/";
                                break;
                        }
                        EventQueue.invokeLater(new Runnable() {//change prog bar text
                            public void run() {
                                pb.setLabel(ApolloConstants.APOLLO_BUNDLE.getString("exp_msg_3") + STRATEGIES[_nCurEngine] + " " +
                                    ApolloConstants.APOLLO_BUNDLE.getString("exp_msg_4") + " " + symbol +
                                    ApolloConstants.APOLLO_BUNDLE.getString("dld_msg_3"));
                            }
                        });
                        engine.simulate();
                        ArrayList<Transaction> trans = engine.getTransactionLog();
                        if (trans == null || trans.size() == 0) {//nothing to show
                            failed_symbols.add(symbol);
                            continue;
                        }

                        //export files
                        SimUtil.exportTransaction(trans, engine, true, export_path);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        //save symbol to notify user later
                        failed_symbols.add(symbol);
                    }
                }

                EventQueue.invokeLater(new Runnable() {//hide progress bar
                    public void run() {
                        pb.setVisible(false);
                        StringBuilder sb = new StringBuilder(ApolloConstants.APOLLO_BUNDLE.getString("dme_msg_1"));
                        if (failed_symbols.size() > 0) {
                            for (String sym : failed_symbols)
                                sb.append(sym).append(", ");
                            WidgetUtil.showWarning(MdiMainFrame.getInstance(), sb.toString());
                        }

//todo refresh tree                        _treNavigator = new JTree(createTreeRoot());
//                        showContent();//refresh tree and content
                    }
                });
            }
        };
        thread.start();
    }

    //-----inner classes---
    /**
     * Container for selecting strategies and start bulk export.
     */
    private class ExportOptionPanel extends JPanel {
        private ExportOptionPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new BevelBorder(BevelBorder.LOWERED));
            add(WidgetUtil.createTitleStrip(ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_2")), BorderLayout.NORTH);

            //center - form to select strategy
            FormLayout layout = new FormLayout(
                    "2dlu, pref, 2dlu, pref, 50dlu",//columns
                    "2dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//rows
            );
            PanelBuilder builder = new PanelBuilder(layout);
            CellConstraints cc = new CellConstraints();
            int col = 2, row = 2;
//todo later use loop when more strategies are available
            _chkStrategy[0] = new JRadioButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_2"));
            _chkStrategy[0].setSelected(true);
            builder.add(_chkStrategy[0], cc.xy(col, row));
            row += 2;
            _chkStrategy[1] = new JRadioButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_3"));
            builder.add(_chkStrategy[1], cc.xy(col, row));
            row += 2;
            _chkStrategy[2] = new JRadioButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_4"));
            builder.add(_chkStrategy[2], cc.xy(col, row));
            row += 2;
            _chkStrategy[3] = new JRadioButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_5"));
            builder.add(_chkStrategy[3], cc.xy(col, row));
            add(builder.getPanel(), BorderLayout.CENTER);

            ButtonGroup grp = new ButtonGroup();
            for (JRadioButton btn : _chkStrategy)
                grp.add(btn);

            SkinPanel btn_pnl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new FlowLayout(FlowLayout.RIGHT));
            btn_pnl.setOpaque(false);
            _btnExport.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //loop thru all checkboxes, if checked, launch corresponding engine, bulk export
                    for (int index = 0; index < 4; index++) {
                        if (_chkStrategy[index].isSelected()) {
                            _nCurEngine = index;
                            bulkExport();
                        }
                    }
                }
            });
            btn_pnl.add(_btnExport);
            add(btn_pnl, BorderLayout.SOUTH);

            //initialize engine
            FundData fund = null;
            _Engines[INDEX_MAC] = new MacEngine(fund);
            _Engines[INDEX_MZC] = new MacdZeroCrossEngine(fund);
            _Engines[INDEX_RSI] = new RsiEngine(fund);
            _Engines[INDEX_STO] = new StochasticEngine(fund);
        }
    }

    //-----instance variables-----
    private JTextArea _txaContent = new JTextArea();
    private JRadioButton[] _chkStrategy = new JRadioButton[4];
    private AbstractEngine[] _Engines = new AbstractEngine[4];//checkbox and engine share same index
    private JTree _treNavigator;
    private JButton _btnExport = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("dme_lbl_1"));
    private int _nCurEngine = INDEX_MAC;

    //-----literals------
    private static final String[] STRATEGIES = {
        ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_2"), ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_3"),
        ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_4"), ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_5"),
    };
    private static final int INDEX_MAC = 0;
    private static final int INDEX_MZC = 1;
    private static final int INDEX_RSI = 2;
    private static final int INDEX_STO = 3;
}