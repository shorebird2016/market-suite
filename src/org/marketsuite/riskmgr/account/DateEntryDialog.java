package org.marketsuite.riskmgr.account;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;
import org.jdesktop.swingx.JXMonthView;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

class DateEntryDialog extends JDialog {
    /**
     * Request user to select a date from calendar
     * @param initial_time in the form of YYYY-MM-DD (Yahoo)
     */
    DateEntryDialog(String initial_time) {
        super(RiskMgrModel.getInstance().getParent(), ApolloConstants.APOLLO_BUNDLE.getString("ded_lbl_1"), true);
        Calendar cal;
        try {
            cal = AppUtil.stringToCalendar(initial_time);
        } catch (ParseException e) {
            MessageBox.messageBox(RiskMgrModel.getInstance().getParent(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                ApolloConstants.APOLLO_BUNDLE.getString("ded_msg_1"), MessageBox.OK_OPTION,
                    MessageBox.WARNING_MESSAGE);
            e.printStackTrace();
            return;
        }

        JPanel content_pane = new JPanel(new BorderLayout());
        setContentPane(content_pane);

        //center - date time panel
        content_pane.add(_MonthView = WidgetUtil.createCalendar(cal), BorderLayout.CENTER);

        //south - buttons
        JPanel btn_pnl = new JPanel();
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                _bCancelled = false;
                dispose();
            }
        });
        btn_pnl.add(ok_btn);
        JButton cancel_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_cancel"));
        cancel_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                _bCancelled = true;
                dispose();
            }
        });
        btn_pnl.add(cancel_btn);
        add(btn_pnl, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false,
            RiskMgrModel.getInstance().getParent(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    //returns setting in this dialog
    public Calendar getDate() {
        Calendar ret = Calendar.getInstance();
        Date start_date = _MonthView.getSelectionDate();
        long start_mil = start_date.getTime();
        ret.setTimeInMillis(start_mil);
        return ret;
    }

    public boolean isCancelled() { return _bCancelled; }

    private JXMonthView _MonthView = new JXMonthView();
    private boolean _bCancelled;
}
