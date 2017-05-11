package org.marketsuite.framework.strategy.analysis;

import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.HeadingRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.HeadingRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.Transaction;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Container to maintain custom transactions either read from file or entered by user.
 */
public class TransactionPanel extends JPanel {
    public TransactionPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        //title of this panel
        JPanel east_pnl = new JPanel(); east_pnl.setOpaque(false);
        east_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_lbl_4")));
        east_pnl.add(_txtEquity); _txtEquity.setEditable(false);
        _txtEquity.setBackground(FrameworkConstants.COLOR_LITE_GREEN);
        WidgetUtil.attachToolTip(_txtEquity, FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_2"),
                SwingConstants.LEFT, SwingConstants.BOTTOM);
        east_pnl.add(Box.createHorizontalGlue());
        east_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("bh_lbl_6")));
        east_pnl.add(_txtEndEquity); _txtEndEquity.setEditable(false); _txtEndEquity.setForeground(Color.blue);
        JLabel lbl = new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("trn_lbl_1"));
        lbl.setFont(Constants.LINK_FONT_BOLD);
        SkinPanel ttl_pnl = WidgetUtil.createTitleStrip(lbl, null, east_pnl);
        WidgetUtil.attachToolTip(ttl_pnl, FrameworkConstants.FRAMEWORK_BUNDLE.getString("trn_tip_4"),
            SwingConstants.CENTER, SwingConstants.BOTTOM);
        add(ttl_pnl, BorderLayout.NORTH);

        //center - table
        _TransactionTableModel = new TransactionTableModel();
        DynaTableCellRenderer renderer = new DynaTableCellRenderer(_TransactionTableModel);
        _tblTrans = WidgetUtil.createDynaTable(_TransactionTableModel, ListSelectionModel.SINGLE_SELECTION,
            new HeadingRenderer(), true, renderer);
        WidgetUtil.forceColumnWidth(_tblTrans.getColumnModel().getColumn(TransactionTableModel.COLUMN_SEQUENCE), 25);
        WidgetUtil.forceColumnWidth(_tblTrans.getColumnModel().getColumn(TransactionTableModel.COLUMN_DIRECTION), 25);
        _tblTrans.setOpaque(false);
        //special editing for entry/exit dates
        JScrollPane scr = new JScrollPane(_tblTrans); scr.getViewport().setOpaque(false);
        add(scr, BorderLayout.CENTER);
    }

    public void populate(ArrayList<Transaction> trans) {
        _TransLog = trans;
        if (trans == null)
            _TransactionTableModel.clear();
        else
            _TransactionTableModel.populate(trans);
    }

    public void setEndEquity(String str) {
        _txtEndEquity.setText(str);
    }

    public void clear() {
        _TransactionTableModel.clear();
    }

    //-----instance variables-----
    protected JTextField _txtEquity = new JTextField("$10,000");
    protected JTextField _txtEndEquity = new JTextField("$ 100,000.00 ");
    private JTable _tblTrans;
    private TransactionTableModel _TransactionTableModel;
    private ArrayList<Transaction> _TransLog;
}
