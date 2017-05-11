package org.marketsuite.scanner;

import javax.swing.*;

public class ScannerModel {
    private static ScannerModel _Instance;
    public static ScannerModel getInstance() {
        if (_Instance == null)
            _Instance = new ScannerModel();
        return _Instance;
    }

    private JFrame parent;
    public void setParent(JFrame p) { parent = p; }
    public JFrame getParent() { return parent; }
}
