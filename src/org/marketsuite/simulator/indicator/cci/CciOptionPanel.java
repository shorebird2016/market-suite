package org.marketsuite.simulator.indicator.cci;

import org.marketsuite.component.field.LongIntegerField;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;

import javax.swing.*;

//optional parameters for CCI
public class CciOptionPanel extends JPanel {
    public CciOptionPanel() {
        setLayout(new MigLayout("insets 0", "20[][]"));
        add(new JLabel("Period:"));
        add(_fldPeriod);
    }
    public int getPeriod() { return (int)_fldPeriod.getValue(); }
    private LongIntegerField _fldPeriod = new LongIntegerField(20, 5, 5, 100, true);
}
