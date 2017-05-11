package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.report.model.ReportTemplate;
import org.marketsuite.simulator.advanced.report.model.TimeSetting;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Container for simulation related settings organized into tabs.
 */
public class TimeSettingPanel extends JPanel {
    TimeSettingPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED));

        //north - title strip with text and tool buttons
        //center - options form
        ButtonGroup grp = new ButtonGroup();
        grp.add(_rdoFullRange);
        grp.add(_rdoPartialRange);
        grp.add(_rdoSkipPart);
        FormLayout layout = new FormLayout(
            "2dlu, pref, 2dlu, pref, 2dlu,  pref, 2dlu, pref, 2dlu, pref, 50dlu",//columns
            "2dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int col = 2, row = 2;
        builder.add(_rdoFullRange, cc.xyw(col, row, 6));  _rdoFullRange.setSelected(true);
        _rdoFullRange.setFocusable(false);
        _rdoFullRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _txtBeginDate.setEnabled(false);
                _txtEndDate.setEnabled(false);
                _txtSkipMonth.setEnabled(false);
            }
        });

        row += 2;
        builder.add(_rdoPartialRange, cc.xy(col, row));
        _rdoPartialRange.setFocusable(false);
        _rdoPartialRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _txtBeginDate.setEnabled(true);
                if (_txtBeginDate.getDate() == null)
                    _txtBeginDate.setDate(Calendar.getInstance().getTime());
                _txtEndDate.setEnabled(true);
                if (_txtEndDate.getDate() == null)
                    _txtEndDate.setDate(Calendar.getInstance().getTime());
                _txtSkipMonth.setEnabled(false);
            }
        });
        builder.add(_txtBeginDate, cc.xy(col+2, row)); _txtBeginDate.setEnabled(false);
        _txtBeginDate.setFocusable(false);
        _txtBeginDate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!checkRange()) {
                    WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
                        ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_2"), null);
                    _txtBeginDate.setDate(_lastBeginDate);//restore
                }
                _lastBeginDate = _txtBeginDate.getDate();
            }
        });
        builder.addLabel(" " + ApolloConstants.APOLLO_BUNDLE.getString("advsim_rdo_2_1") + " ", cc.xy(col + 4, row));
        builder.add(_txtEndDate, cc.xy(col+8, row)); _txtEndDate.setEnabled(false);
        _txtEndDate.setFocusable(false);
        _txtEndDate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    adjustEndDate();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                if (!checkRange()) {
                    WidgetUtil.showWarningInEdt(MdiMainFrame.getInstance(),
                        ApolloConstants.APOLLO_BUNDLE.getString("advsim_msg_2"), null);
                    _txtEndDate.setDate(_lastEndDate);//restore
                }
                _lastEndDate = _txtEndDate.getDate();
            }
        });

        row += 2;
        builder.add(_rdoSkipPart, cc.xy(col, row));
        _rdoSkipPart.setFocusable(false);
        _rdoSkipPart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _txtBeginDate.setEnabled(false);
                _txtEndDate.setEnabled(false);
                _txtSkipMonth.setEnabled(true);
            }
        });
        builder.add(_txtSkipMonth, cc.xy(col + 2, row)); _txtSkipMonth.setEnabled(false);

        //use 6 column wide to stretch this label
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("advsim_rdo_3_1"), cc.xyw(col + 4, row, 6));
        add(builder.getPanel(), BorderLayout.CENTER);

        //initialize dates, when there is nothing use today
        _txtBeginDate.setDate(_lastBeginDate);
        _txtEndDate.setDate(_lastEndDate);
    }

    //set all fields to default values
    void clearForm() {
//todo maybe others too....
        _rdoFullRange.setSelected(true);
    }

    void populate(ReportTemplate report_template) {
        _txtBeginDate.setEnabled(false);
        _txtEndDate.setEnabled(false);
        _txtSkipMonth.setEnabled(false);

        //update field values using TimeSetting
        TimeSetting time_setting = report_template.getTimeSetting();
        if (time_setting == null)
            return;

        String type = time_setting.getType();
        if (type == null)
            return;

        if (type.equals(TimeSetting.TIME_FULL_RANGE))
            _rdoFullRange.setSelected(true);
        else if (type.equals(TimeSetting.TIME_PARTIAL_RANGE)) {
            _rdoPartialRange.setSelected(true);
            _txtBeginDate.setEnabled(true);
            _txtEndDate.setEnabled(true);
            try {
                String bd = time_setting.getBeginDate();
                Calendar now = Calendar.getInstance();
                if (bd == null || bd.equals(""))//without date info, set to now
                    _txtBeginDate.setDate(now.getTime());
                else
                    _txtBeginDate.setDate(AppUtil.stringToCalendar(bd).getTime());
                String ed = time_setting.getEndDate();
                if (ed == null || ed.equals(""))
                    _txtEndDate.setDate(now.getTime());
                else
                    _txtEndDate.setDate(AppUtil.stringToCalendar(ed).getTime());
                _lastBeginDate = _txtBeginDate.getDate();
                _lastEndDate = _txtEndDate.getDate();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else if (type.equals(TimeSetting.TIME_SKIP_INITIAL)) {
            _rdoSkipPart.setSelected(true);//this won't trigger action listener
            _txtSkipMonth.setText(String.valueOf(time_setting.getSkipMonth()));
            _txtSkipMonth.setEnabled(true);
        }
    }

    TimeSetting getTimeSetting() {
        String type = TimeSetting.TIME_FULL_RANGE;
        if (_rdoPartialRange.isSelected())
            type = TimeSetting.TIME_PARTIAL_RANGE;
        else if (_rdoSkipPart.isSelected())
            type = TimeSetting.TIME_SKIP_INITIAL;

        //begin, end dates
        String begin_date = "", end_date = "";
        if (_rdoPartialRange.isSelected()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(_txtBeginDate.getDate());
            begin_date = AppUtil.calendarToString(cal);
            cal.setTime(_txtEndDate.getDate());
            end_date = AppUtil.calendarToString(cal);
        }
        return new TimeSetting(type, begin_date, end_date,
            _rdoSkipPart.isSelected() ? (int)_txtSkipMonth.getValue() : -1);
    }

    //-----private methods-----
    //return true = if begin is earlier than end date
    private boolean checkRange() {
        Calendar bcal = Calendar.getInstance();
        bcal.setTime(_txtBeginDate.getDate());
        Calendar ecal = Calendar.getInstance();
        ecal.setTime(_txtEndDate.getDate());
        return bcal.compareTo(ecal) < 0;
    }

    //return true = if end date not in SP500 quote file, force to last available date
    private void adjustEndDate() throws ParseException {
        String last_date = FrameworkConstants.SP500_DATA.getQuote().get(0).getDate();
        Calendar last_cal = AppUtil.stringToCalendar(last_date);
        Calendar end = Calendar.getInstance();
        end.setTime(_txtEndDate.getDate());
        if (last_cal.compareTo(end) < 0)
            _txtEndDate.setDate(last_cal.getTime());
    }

    //-----instance variables-----
    private JRadioButton _rdoFullRange = new JRadioButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_rdo_1"));
    private JRadioButton _rdoPartialRange = new JRadioButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_rdo_2"));
    private JRadioButton _rdoSkipPart = new JRadioButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_rdo_3"));
    private JXDatePicker _txtBeginDate = new JXDatePicker();
    private JXDatePicker _txtEndDate = new JXDatePicker();
    private LongIntegerField _txtSkipMonth = new LongIntegerField(3, 5, 0, 500);
    private Date _lastBeginDate = Calendar.getInstance().getTime();
    private Date _lastEndDate = Calendar.getInstance().getTime();
}
