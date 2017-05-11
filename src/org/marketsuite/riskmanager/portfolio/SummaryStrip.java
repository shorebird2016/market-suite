package org.marketsuite.riskmanager.portfolio;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.main.RiskMgrFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A container for summary information, located on the bottom of window.
 */
public class SummaryStrip extends JPanel {
    public SummaryStrip() {
        setLayout(new BorderLayout());
        JPanel pnl_west = new JPanel(new FlowLayout(FlowLayout.LEFT));

        //risks
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_2"));
        lbl.setFont(FONT);
        lbl.setForeground(LABEL_COLOR);
        pnl_west.add(lbl);
        pnl_west.add(_lblTotalRisk); _lblTotalRisk.setEditable(false); _lblTotalRisk.setFont(FONT);
        _lblTotalRisk.setBackground(FrameworkConstants.LIGHT_PINK);
        pnl_west.add(_lblRiskPercent); _lblRiskPercent.setEditable(false); _lblRiskPercent.setFont(FONT);
        _lblRiskPercent.setBackground(FrameworkConstants.LIGHT_PINK);
        pnl_west.add(Box.createHorizontalStrut(20));

        //P/L
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_79"));
        lbl.setFont(FONT);
        lbl.setForeground(LABEL_COLOR);
        pnl_west.add(lbl);
        pnl_west.add(_lblPLAmount); _lblPLAmount.setEditable(false); _lblPLAmount.setFont(FONT);
        _lblPLAmount.setBackground(FrameworkConstants.LIGHT_GREEN);
        pnl_west.add(_lblPLPercent); _lblPLPercent.setEditable(false); _lblPLPercent.setFont(FONT);
        _lblPLPercent.setBackground(FrameworkConstants.LIGHT_GREEN);
        pnl_west.add(Box.createHorizontalStrut(20));

        //cost and market value
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_75"));
        lbl.setFont(FONT);
        lbl.setForeground(LABEL_COLOR);
        pnl_west.add(lbl);
        pnl_west.add(_lblTotalCost); _lblTotalCost.setEditable(false); _lblTotalCost.setFont(FONT);
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_76"));
        lbl.setFont(FONT);
        lbl.setForeground(LABEL_COLOR);
        pnl_west.add(lbl);
        pnl_west.add(_lblMarketValue); _lblMarketValue.setEditable(false);
        _lblMarketValue.setFont(FONT);
        _lblMarketValue.setBackground(FrameworkConstants.LIGHT_GREEN);//light green
        pnl_west.add(Box.createHorizontalStrut(5));
        add(pnl_west, BorderLayout.WEST);

        //east - cash amount/percent
        JPanel east_pnl = new JPanel();
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_77"));
        east_pnl.add(lbl);
        east_pnl.add(_lblCashPercent);
        _lblCashPercent.setEditable(false);
        _lblCashPercent.setFont(FONT);
        _lblPLPercent.setBackground(FrameworkConstants.LIGHT_GREEN);
        lbl.setFont(FONT);
        lbl.setForeground(LABEL_COLOR);
        east_pnl.add(_txtCashAmount);
        _txtCashAmount.setFont(FONT);
        _txtCashAmount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double cash = _txtCashAmount.getValue();
                double cash_pct = cash / _dMarketValue;
                _lblCashPercent.setText(FrameworkConstants.ROI_FORMAT.format(cash_pct));

                //notify table model so that it can be saved
                RiskMgrFrame rmf = ((MdiMainFrame) RiskMgrModel.getInstance().getParent()).findRiskMgrFrame();
                if (rmf != null) {
//                    PositionPanel tab = rmf.getMainPanel().getActiveTab();
//                    tab.setCashAmount(cash);
//                    tab.markDirty(true);
                }
            }
        });
        add(east_pnl, BorderLayout.EAST);
    }

    //caller must calculate these strings
    public void updateSummary(double total_risk, double mkt_val, double risk_pct, double pl_amt, double pl_pct,
                              double adj_risk, double adj_pct, double total_cost, double cash_pct,
                              double mp_amt, double mp_pct) {
        //values
        _lblTotalCost.setText(FrameworkConstants.DOLLAR_FORMAT.format(total_cost));
        _lblMarketValue.setText(FrameworkConstants.DOLLAR_FORMAT.format(mkt_val));
        _lblPLAmount.setText(FrameworkConstants.DOLLAR_FORMAT.format(pl_amt < 0 ? -pl_amt : pl_amt));
        _lblPLPercent.setText(FrameworkConstants.ROI_FORMAT.format(pl_pct < 0 ? -pl_pct : pl_pct));
        _lblTotalRisk.setText(FrameworkConstants.DOLLAR_FORMAT.format(/*total_risk < 0 ? -total_risk : */total_risk));
        _lblRiskPercent.setText(FrameworkConstants.ROI_FORMAT.format(/*risk_pct < 0 ? -risk_pct : */risk_pct));
        _lblAdjustedRisk.setText(FrameworkConstants.DOLLAR_FORMAT.format(adj_risk < 0 ? -adj_risk : adj_risk));
        _lblAdjPercent.setText(FrameworkConstants.ROI_FORMAT.format(adj_pct < 0 ? -adj_pct : adj_pct));
        _lblCashPercent.setText(FrameworkConstants.ROI_FORMAT.format(cash_pct));
        _lblPullbackAmount.setText(FrameworkConstants.DOLLAR_FORMAT.format(mp_amt));
        _lblPullbackPercent.setText(FrameworkConstants.ROI_FORMAT.format(mp_pct));

        //background coloring
        _lblPLAmount.setBackground(pl_amt < 0 ? FrameworkConstants.LIGHT_PINK : FrameworkConstants.LIGHT_GREEN);
        _lblPLPercent.setBackground(pl_pct < 0 ? FrameworkConstants.LIGHT_PINK : FrameworkConstants.LIGHT_GREEN);
        _lblTotalRisk.setBackground(total_risk > 0 ? FrameworkConstants.LIGHT_GREEN : FrameworkConstants.LIGHT_PINK);
        _lblRiskPercent.setBackground(risk_pct > 0 ? FrameworkConstants.LIGHT_GREEN : FrameworkConstants.LIGHT_PINK);
        _lblAdjustedRisk.setBackground(total_risk <= 0 ? FrameworkConstants.LIGHT_GREEN : FrameworkConstants.LIGHT_PINK);
        _lblAdjPercent.setBackground(risk_pct <= 0 ? FrameworkConstants.LIGHT_GREEN : FrameworkConstants.LIGHT_PINK);
    }

    public void updateMarketValue(double mkt_val) { _dMarketValue = mkt_val; }
    public double getMarketValue() { return _dMarketValue; }
    public double getCashAmount() {return _txtCashAmount.getValue(); }

    //----- private methods -----
    //create 2 strips for holding summary information
    private Box createSummaryBar() {
        //TODO move all below to a popout in center
        //toolbar - summary information for entire portfolio
        Box summary_pnl = Box.createVerticalBox();
        JPanel pnl_row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        //risks
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_2"));
//        lbl.setFont(FONT);
//        lbl.setForeground(LABEL_COLOR);
//        pnl_row1.add(lbl);
//        pnl_row1.add(Box.createGlue());
//        pnl_row1.add(
//                _lblTotalRisk); _lblTotalRisk.setEditable(false); _lblTotalRisk.setFont(FONT);
//        _lblTotalRisk.setBackground(AppConstants.LIGHT_PINK);
//        pnl_row1.add(_lblRiskPercent); _lblRiskPercent.setEditable(false); _lblRiskPercent.setFont(FONT);
//        _lblRiskPercent.setBackground(AppConstants.LIGHT_PINK);

        //adjusted risks
//        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_13"));
//        lbl.setFont(FONT);
//        lbl.setForeground(LABEL_COLOR);
//        pnl_row1.add(lbl);
//        pnl_row1.add(Box.createGlue());
//        pnl_row1.add(_lblAdjustedRisk); _lblAdjustedRisk.setEditable(false); _lblAdjustedRisk.setFont(FONT);
//        _lblAdjustedRisk.setBackground(AppConstants.LIGHT_PINK);
//        pnl_row1.add(_lblAdjPercent); _lblAdjPercent.setEditable(false); _lblAdjPercent.setFont(FONT);
//        _lblAdjPercent.setBackground(AppConstants.LIGHT_PINK);
//        pnl_row1.add(Box.createHorizontalStrut(10));
//
//        //cost and market value
//        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_14"));
//        lbl.setFont(FONT);
//        lbl.setForeground(LABEL_COLOR);
//        pnl_row1.add(lbl);
//        pnl_row1.add(_lblTotalCost); _lblTotalCost.setEditable(false); _lblTotalCost.setFont(FONT);
//        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_3"));
//        lbl.setFont(FONT);
//        lbl.setForeground(LABEL_COLOR);
//        pnl_row1.add(lbl);
//        pnl_row1.add(_lblMarketValue); _lblMarketValue.setEditable(false);
//        _lblMarketValue.setFont(FONT);
//        _lblMarketValue.setBackground(AppConstants.LIGHT_GREEN);//light green
//        pnl_row1.add(Box.createHorizontalStrut(5));

        //P/L
//        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_5"));
//        lbl.setFont(FONT);
//        lbl.setForeground(LABEL_COLOR);
//        pnl_row1.add(lbl);
//        pnl_row1.add(_lblPLAmount); _lblPLAmount.setEditable(false); _lblPLAmount.setFont(FONT);
//        _lblPLAmount.setBackground(AppConstants.LIGHT_GREEN);
//        pnl_row1.add(_lblPLPercent); _lblPLPercent.setEditable(false); _lblPLPercent.setFont(FONT);
//        _lblPLPercent.setBackground(AppConstants.LIGHT_GREEN);
//        summary_pnl.add(pnl_row1);

        //row 2 - cash amount/percent
//        JPanel pnl_row2 = new JPanel();
//        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_15"));
//        lbl.setFont(FONT);
//        lbl.setForeground(LABEL_COLOR);
//        pnl_row2.add(lbl);
//        pnl_row2.add(_txtCashAmount);
//        _txtCashAmount.setFont(FONT);
//        _txtCashAmount.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                _TableModel.setCashAmount(_txtCashAmount.getValue());
//                _lblCashPercent.setText(FrameworkConstants.ROI_FORMAT.format(_TableModel.getCashPercent()));
//            }
//        });
//        _txtCashAmount.getDocument().addDocumentListener(new DocumentListener() {
//            public void insertUpdate(DocumentEvent documentEvent) {
//                _TableModel.setDirty(true);
//            }
//
//            public void removeUpdate(DocumentEvent documentEvent) {
//                _TableModel.setDirty(true);
//            }
//
//            public void changedUpdate(DocumentEvent documentEvent) {
//                _TableModel.setDirty(true);
//            }
//        });
//        pnl_row2.add(_lblCashPercent);
//        _lblCashPercent.setEditable(false);
//        _lblCashPercent.setFont(FONT);
//        _lblPLPercent.setBackground(AppConstants.LIGHT_GREEN);
//        pnl_row2.add(Box.createHorizontalStrut(20));

        //max pullback amount/percent
//        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_16"));
//        lbl.setFont(FONT);
//        lbl.setForeground(LABEL_COLOR);
//        pnl_row2.add(lbl);
//        pnl_row2.add(_lblPullbackAmount); _lblPullbackAmount.setEditable(false);
//        _lblPullbackAmount.setFont(FONT);
//        pnl_row2.add(_lblPullbackPercent); _lblPullbackPercent.setEditable(false);
//        _lblPullbackPercent.setFont(FONT);
//        _lblPLPercent.setBackground(AppConstants.LIGHT_GREEN);
//        summary_pnl.add(pnl_row2);
        return summary_pnl;
    }

    //----- instance variables -----
    private JTextField _lblTotalRisk = new JTextField(6);
    private JTextField _lblRiskPercent = new JTextField(5);
    private JTextField _lblAdjustedRisk = new JTextField(6);
    private JTextField _lblAdjPercent = new JTextField(5);
    private JTextField _lblTotalCost = new JTextField(8);
    private JTextField _lblMarketValue = new JTextField(8);
    private JTextField _lblPLAmount = new JTextField(6);
    private JTextField _lblPLPercent = new JTextField(5);
    private DecimalField _txtCashAmount = new DecimalField(0, 6, 0, 100000, null);
    private JTextField _lblCashPercent = new JTextField(5);
    private JTextField _lblPullbackAmount = new JTextField(6);
    private JTextField _lblPullbackPercent = new JTextField(5);
    private double _dMarketValue;

    //----- literals -----
    private static final Font FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color LABEL_COLOR = Color.blue;
}
