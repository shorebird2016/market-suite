package org.marketsuite.tool;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.Date;

/**
 * Dialog to calculate CAGR. For stand-alone, minimum imports, no literals, no external references. All self-contained.
 */
class CagrDialog extends JDialog {
    CagrDialog() {
        setTitle("CAGR Calculator");
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT,
            new MigLayout("insets 10 20 10 30, wrap 4", "[right][left][right][left]"));
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
        content_pnl.add(_chkTimeRange); _chkTimeRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean use_dates = _chkTimeRange.isSelected();
                _fldBeginDate.setEnabled(use_dates);
                _fldEndDate.setEnabled(use_dates);
                _fldNumYear.setEditable(!use_dates);
                if (use_dates)
                    _fldNumYear.setValue(0);
            }
        });
        content_pnl.add(new JLabel()); content_pnl.add(new JLabel());
        content_pnl.add(_fldCagr, "wrap"); _fldCagr.setBackground(new Color(49, 255, 153, 47));
        _fldCagr.setFont(new Font("Verdana", Font.PLAIN, 18)); _fldCagr.setHorizontalAlignment(JTextField.CENTER);
        content_pnl.add(new Label("Number of Years:")); content_pnl.add(_fldNumYear, "wrap"); _fldNumYear.setValue(6);
        content_pnl.add(new JLabel("Begin Date:")); content_pnl.add(_fldBeginDate); _fldBeginDate.setEnabled(false);
        _fldBeginDate.setDate(Calendar.getInstance().getTime()); _fldBeginDate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { updateYears();
//                //if begin < end date, disallow
//                Date begin_date = _fldBeginDate.getDate();
//                Date end_date = _fldEndDate.getDate();
//                if (begin_date.compareTo(end_date) >= 0) {
//                    MessageBox.messageBox(CagrDialog.this,
//                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"), "Begin Date MUST be earlier than End Date!",
//                            MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
//                    return;
//                }
//                double dur = end_date.getTime() - begin_date.getTime();//in miliseconds
//                double sec_yr = 365.0 * 24.0 * 60 * 60 * 1000;
//                double years = dur / sec_yr;
//                _fldNumYear.setValue(years);
            }
        });
        content_pnl.add(new Label("Begin Price:")); content_pnl.add(_fldBeginPrice); _fldBeginPrice.setValue(10);
        content_pnl.add(new JLabel("End Date:")); content_pnl.add(_fldEndDate); _fldEndDate.setEnabled(false);
        _fldEndDate.setDate(Calendar.getInstance().getTime()); _fldEndDate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { updateYears(); }
        });
        content_pnl.add(new Label("End Price:")); content_pnl.add(_fldEndPrice); _fldEndPrice.setValue(20);
        content_pnl.add(_btnCalc, "gaptop 15, spanx, right"); _btnCalc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double cagr = calcCagr((float) _fldNumYear.getValue(),
                    (float) _fldBeginPrice.getValue(), (float) _fldEndPrice.getValue());
                _fldCagr.setText(FrameworkConstants.PCT_FORMAT.format(cagr));
            }
        });
        setContentPane(content_pnl);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) { System.exit(0); } });
        getRootPane().setDefaultButton(_btnCalc);
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false, null, WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private double calcCagr(float num_years, float begin_value, float end_value) {
        float exponent = 1 / num_years;
        float base = end_value / begin_value;
        return Math.pow(base, exponent ) - 1;
    }
    private void updateYears() {
        //if begin < end date, disallow
        Date begin_date = _fldBeginDate.getDate();
        Date end_date = _fldEndDate.getDate();
        if (begin_date.compareTo(end_date) >= 0) {
            MessageBox.messageBox(CagrDialog.this,
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"), "Begin Date MUST be earlier than End Date!",
                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
            return;
        }
        double dur = end_date.getTime() - begin_date.getTime();//in miliseconds
        double sec_yr = 365.0 * 24.0 * 60 * 60 * 1000;
        double years = dur / sec_yr;
        _fldNumYear.setValue(years);
    }

    //----- instance variables -----
    private JCheckBox _chkTimeRange = new JCheckBox("Use Date Range");
    private JTextField _fldCagr = new JTextField(5);
    private JXDatePicker _fldBeginDate = new JXDatePicker();
    private JXDatePicker _fldEndDate = new JXDatePicker();
    private DecimalField _fldBeginPrice = new DecimalField(0, 5, 10, 1000, null);
    private DecimalField _fldEndPrice = new DecimalField(0, 5, 20, 1000, null);
    private DecimalField _fldNumYear = new DecimalField(2, 5, 1, 70, null);
    private JButton _btnCalc = new JButton("Calculate");

    //----- main -----
    public static void main(String[] args) {
        new CagrDialog();
    }
}