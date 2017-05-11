package org.marketsuite.simulator.indicator.ichimoku;

import org.marketsuite.component.field.LongIntegerField;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;

import javax.swing.*;

//optional parameters for Ichimoku indicator
public class IchimokuOptionPanel extends JPanel {
    public IchimokuOptionPanel() {
        setLayout(new MigLayout("insets 0", "10[][]"));
        add(new JLabel("Fast Period:")); add(_fldPeriodFast);
        add(new JLabel("Medium Period:")); add(_fldPeriodMid);
        add(new JLabel("Slow Period:")); add(_fldPeriodSlow);
    }
    public int getFastPeriod() { return (int) _fldPeriodFast.getValue(); }
    public int getMidPeriod() { return (int) _fldPeriodMid.getValue(); }
    public int getSlowPeriod() { return (int) _fldPeriodSlow.getValue(); }
    private LongIntegerField _fldPeriodFast = new LongIntegerField(9, 3, 5, 100, true);
    private LongIntegerField _fldPeriodMid = new LongIntegerField(26, 3, 5, 200, true);
    private LongIntegerField _fldPeriodSlow = new LongIntegerField(52, 3, 5, 500, true);
}
