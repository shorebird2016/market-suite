package org.marketsuite.riskmanager;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.riskmanager.portfolio.PositionPanel;
import jxl.Sheet;
import jxl.Workbook;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//main container for all views,
public class RiskMgrMainPanel extends JPanel {
    public RiskMgrMainPanel(JFrame parent) {
        RiskMgrModel.getInstance().setParent(parent);
        setLayout(new BorderLayout());
        setOpaque(false);

        //Top level tabs
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        _MainTab = new JTabbedPane();
        _MainTab.setName("Main");//tell MainTabUI to use taller height
//        JXLayer layer = new JXLayer(_pnlAnalysis = new TradeAnalysisPanel());
//        MagnifierUI ui = new MagnifierUI();
//        layer.setUI(ui);
//        _MainTab.add(FrameworkConstants.APP_BUNDLE.getString("ana_lbl_1"), layer);
//        _MainTab.add(AppConstants.APP_BUNDLE.getString("tab_1"), _pnlPosition = new PositionPanel());
        content.add(_MainTab, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    //-----public methods-----
    public void updateSummary(double total_risk, double mkt_val, double risk_pct, double pl_amt, double pl_pct,
                              double adj_risk, double adj_pct, double total_cost, double cash_pct,
                              double mp_amt, double mp_pct) {
        PositionPanel pos_pnl = getActiveTab();
        pos_pnl.updateSummary(total_risk, mkt_val, risk_pct, pl_amt, pl_pct, adj_risk, adj_pct, total_cost,
            cash_pct, mp_amt, mp_pct);
    }

    public PositionPanel getActiveTab() {
        int active_idx = _MainTab.getSelectedIndex();
        return (PositionPanel)_MainTab.getComponentAt(active_idx);
    }
    public void closeActiveTab() {
        int active_idx = _MainTab.getSelectedIndex();
        String name = _MainTab.getTitleAt(active_idx);
        removeAccount(name);
        _MainTab.remove(active_idx);
    }

    //create and open an account file in a new tab in the end
    //account_files = File object found in usual "account" folder
    public void addAccount(File[] account_files) {
        for (File acct_file : account_files) {
            String acct_name = acct_file.getName();//remove extension
            String name = acct_name.substring(0, acct_name.indexOf(FrameworkConstants.EXTENSION_ACCOUNT));
            if (_sAccountNames.contains(name))
                continue;

            PositionPanel acct_pnl = new PositionPanel();//a new panel for it
            _sAccountNames.add(name);
            _MainTab.add(name, acct_pnl);
            _MainTab.setSelectedComponent(acct_pnl);//this will populate summary properly initially
            try {
//todo use progress bar to show each file being loaded
                acct_pnl.populate(acct_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //add single account tab from spreadsheet with TradeStation holding format
    public void addAccount(Sheet sheet) {
        //read starting row 5 until a blank encountered in column A
        String descr = sheet.getCell(0, 2).getContents();//file description
        int row = 5;
        while (true) {
            String sym = sheet.getCell(0, row - 1).getContents();
            if (sym.equals("")) {
                break;
            }
            String qty = sheet.getCell(1, row - 1).getContents();
            String cost = sheet.getCell(5, row - 1).getContents();
//TODO save into a structure, make an account out of it
            row++;
        }
    }
    void removeAccount(String account_name) {
        for (String name : _sAccountNames) {
            if (name.equals(account_name)) {
                _sAccountNames.remove(name);
                break;
            }
        }
    }

    //-----instance variables-----
    private JTabbedPane _MainTab;
    private ArrayList<String> _sAccountNames = new ArrayList();
}