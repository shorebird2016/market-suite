package org.marketsuite.riskmanager;

import javax.swing.*;

public class RiskMgrModel {
    private static RiskMgrModel _Instance;
    public static RiskMgrModel getInstance() {
        if (_Instance == null)
            _Instance = new RiskMgrModel();
        return _Instance;
    }

    private JFrame parent;
    public void setParent(JFrame p) { parent = p; }
    public JFrame getParent() { return parent; }
}
