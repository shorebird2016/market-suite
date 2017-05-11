package org.marketsuite.riskmgr.account;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;

import javax.swing.*;
import java.util.ArrayList;

/**
 * A container for summary information, located on the bottom of window.
 */
class SummaryStrip extends JPanel {
    SummaryStrip() {
        setLayout(new MigLayout("insets 0", "5[][][][][]push[][][]push[][]push[][]5", "3[]3"));
        add(WidgetUtil.createFieldLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_79"), false));//cost based P/L
        add(_txtCostPlAmt);
        add(_txtCostPlPct);
        add(_txtStopPlAmt);
        add(_txtStopPlPct);
        add(WidgetUtil.createFieldLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_75"), false));//equity
        add(_txtEquity); add(_txtStopEquity);
        add(WidgetUtil.createFieldLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_76"), false));//cash
        add(_txtCash);
        add(WidgetUtil.createFieldLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_77"), false));//value
        add(_txtValue);
    }

    //----- protected methods -----
    void populate() {//using RiskMgrModel's position array
        ArrayList<Position> positions = RiskMgrModel.getInstance().getPositions();
        float total_cost = 0, equity = 0, total_stop_eqty = 0, total_cost_pl = 0, total_stop_pl = 0;
        for (Position pos : positions) {
            int shares = pos.getShares();
            float cost = pos.getCost() * shares;
            float quote = pos.getCurClose();
            total_cost_pl += quote * shares - cost;
            total_stop_pl += shares * (pos.getStop() - pos.getCost());
            total_cost += cost;
            equity += quote * shares;
            total_stop_eqty += shares * pos.getStop();
        }
        float cost_pl_pct = total_cost_pl / total_cost;
        _txtCostPlAmt.setText(FrameworkConstants.DOLLAR_FORMAT.format(total_cost_pl));
        _txtCostPlPct.setText(FrameworkConstants.ROI_FORMAT.format(total_cost_pl / total_cost));
        float stop_pl_pct = total_stop_pl / total_cost;
        _txtStopPlAmt.setText(FrameworkConstants.DOLLAR_FORMAT.format(total_stop_pl));
        _txtStopPlPct.setText(FrameworkConstants.ROI_FORMAT.format(stop_pl_pct));
        _txtEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(equity));
        _txtStopEquity.setText(FrameworkConstants.DOLLAR_FORMAT.format(total_stop_eqty));
        float cash = RiskMgrModel.getInstance().getCashBalance();
        _txtCash.setText(FrameworkConstants.DOLLAR_FORMAT.format(cash));
        _txtValue.setText(FrameworkConstants.DOLLAR_FORMAT.format(cash + equity));

        //background coloring
        _txtCostPlAmt.setBackground(total_cost_pl < 0 ? FrameworkConstants.LIGHT_PINK : FrameworkConstants.LIGHT_GREEN);
        _txtCostPlPct.setBackground(cost_pl_pct < 0 ? FrameworkConstants.LIGHT_PINK : FrameworkConstants.LIGHT_GREEN);
        _txtStopPlAmt.setBackground(total_stop_pl < 0 ? FrameworkConstants.LIGHT_PINK : FrameworkConstants.LIGHT_GREEN);
        _txtStopPlPct.setBackground(stop_pl_pct < 0 ? FrameworkConstants.LIGHT_PINK : FrameworkConstants.LIGHT_GREEN);
    }

    //----- instance variables -----
    private JTextField _txtCostPlAmt = WidgetUtil.createBasicField(6, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_71"));//cost based P/L
    private JTextField _txtCostPlPct = WidgetUtil.createBasicField(4, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_72"));
    private JTextField _txtStopPlAmt = WidgetUtil.createBasicField(6, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_73"));//stop based P/L
    private JTextField _txtStopPlPct = WidgetUtil.createBasicField(4, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_74"));
    private JTextField _txtEquity = WidgetUtil.createBasicField(8, false, true, null);
    private JTextField _txtStopEquity = WidgetUtil.createBasicField(8, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_78"));//value for all stopped out
    private JTextField _txtCash = WidgetUtil.createBasicField(8, false, true, null);
    private JTextField _txtValue = WidgetUtil.createBasicField(8, false, true, null);
}
