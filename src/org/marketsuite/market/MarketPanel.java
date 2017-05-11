package org.marketsuite.market;

import org.marketsuite.component.field.NameField;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MarketPanel extends JPanel {
    MarketPanel() {
        setLayout(new MigLayout("inset 2 2 2 2"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));

        //north - title strip with buttons
        JPanel tool_pnl = new JPanel();  tool_pnl.setOpaque(false);
        tool_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_35") + ":"));
        tool_pnl.add(_txtSearch);
        _txtSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //user hit return
                String sym = _txtSearch.getText();
                _treMarket.showNode(sym);
                _txtSearch.selectAll();
            }
        });
        tool_pnl.add(Box.createGlue());
        tool_pnl.add(_btnExpand);
        _btnExpand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                for (int i = 0; i < _treMarket.getRowCount(); i++)
//                    _treMarket.expandRow(i);
                WidgetUtil.expandAll(_treMarket, true);
            }
        });
        tool_pnl.add(Box.createGlue());
        tool_pnl.add(_btnCollapse);
        _btnCollapse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                for (int i = 0; i < _treMarket.getRowCount(); i++)
//                    _treMarket.collapseRow(i);
                WidgetUtil.expandAll(_treMarket, false);
            }
        });
        tool_pnl.add(Box.createGlue());
//        tool_pnl.add(_btnRefresh);
        tool_pnl.add(Box.createHorizontalBox());
        add(WidgetUtil.createTitleStrip(ApolloConstants.APOLLO_BUNDLE.getString("mkt_02"), tool_pnl), "dock north");
        add(new JScrollPane(_treMarket = new MarketTree()), "dock center");
    }

    //----- variables -----
    private NameField _txtSearch = new NameField(5);
    private JButton _btnExpand = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("mkt_03"), FrameworkIcon.EXPAND_TREE);
    private JButton _btnCollapse = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("mkt_04"), FrameworkIcon.COLLAPSE_TREE);
//    private JButton _btnRefresh = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("mkt_01"), FrameworkIcon.REFRESH);
    private MarketTree _treMarket;
}
