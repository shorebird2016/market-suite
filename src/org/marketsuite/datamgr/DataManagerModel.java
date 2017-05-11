package org.marketsuite.datamgr;

import javax.swing.*;

public class DataManagerModel {
    private static DataManagerModel _Instance;
    public static DataManagerModel getInstance() {
        if (_Instance == null)
            _Instance = new DataManagerModel();
        return _Instance;
    }

//    private JFrame parent;
//    public void setParent(JFrame p) { parent = p; }
//    public JFrame getParent() { return parent; }
}
