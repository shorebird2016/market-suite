package org.marketsuite.riskmgr.portfolio;

import org.marketsuite.component.Constants;
import org.marketsuite.component.graph.SimpleTimeSeriesGraph;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.SortHeaderRenderer;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

public class PortfolioPanel extends JPanel {
    public PortfolioPanel() {
        setLayout(new MigLayout("insets 0"));

        //title strip - buttons and labels
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "push[]5", "3[]3"));
        ttl_pnl.add(_btnRefresh);
        _btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _tmPort.populate();
            }
        });
        add(ttl_pnl, "dock north");

        //center split pane of table and equity curve
        JSplitPane cen_pnl = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        cen_pnl.setDividerLocation(450);
        cen_pnl.setContinuousLayout(true);
        cen_pnl.setDividerSize(Constants.DEFAULT_SPLITTER_WIDTH);

        //top side - position table and stop worksheet
        _tmPort = new PortfolioModel();
        _tblPort = WidgetUtil.createDynaTable(_tmPort, ListSelectionModel.SINGLE_SELECTION,
                new SortHeaderRenderer(), true, new PortRenderer());
        JScrollPane scr = new JScrollPane(_tblPort);  scr.getViewport().setOpaque(false);
        cen_pnl.setTopComponent(scr);

        //bottom side, equity curve
        cen_pnl.setBottomComponent(_Plot = new SimpleTimeSeriesGraph("", "Account Value", "SP500"));
        _tmPort.setPlot(_Plot);//let model know to plot later
        add(cen_pnl, "dock center");
    }

    //----- inner classes -----
    private class PortRenderer extends DynaTableCellRenderer {
        private PortRenderer() { super(_tmPort);}
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Object acct = _tmPort.getCell(row, PortfolioModel.COLUMN_ACCOUNT).getValue();
            boolean is_total_row = acct.equals("");
            switch (column) {
                case PortfolioModel.COLUMN_CASH://all use $xxxx.xx
                case PortfolioModel.COLUMN_EQUITY:
                case PortfolioModel.COLUMN_VALUE:
                case PortfolioModel.COLUMN_DEPOSIT_WITHDRAW:
                    double amt = 0;
                    try {
                        amt = (Double)value;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lbl.setText(FrameworkConstants.DOLLAR_FORMAT.format(amt));
                    if (amt == 0) lbl.setText("");
                    break;

                case PortfolioModel.COLUMN_WEEKLY_PCT:
                    double pct = (Double)value;

                    //if weekly % less than both SPY/IWM, show pink background
                    double spy_pct = (Double)_tmPort.getCell(row, PortfolioModel.COLUMN_SPY_WEEKLY_PCT).getValue();
                    if (pct < spy_pct)
                        lbl.setBackground(FrameworkConstants.LIGHT_PINK);
                case PortfolioModel.COLUMN_CASH_PCT:
                case PortfolioModel.COLUMN_SPY_WEEKLY_PCT:
                    amt = (Double)value;
                    lbl.setText(FrameworkConstants.ROI_FORMAT.format(amt));
                    if (amt == 0) lbl.setText("");
                    lbl.setHorizontalAlignment(SwingConstants.TRAILING);
                    break;
            }
            if (is_total_row) {
                lbl.setFont(FrameworkConstants.SMALL_FONT);
                lbl.setForeground(Color.blue);
            }
            return lbl;
        }
    }

    //----- variables -----
    private JButton _btnRefresh = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_39"), FrameworkIcon.REFRESH);
    private JTable _tblPort;
    private PortfolioModel _tmPort;
    SimpleTimeSeriesGraph _Plot;
}
