package org.marketsuite.simulator.advanced.scanreport;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.panel.WatchlistFilterPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.type.Strategy;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.SimGraphDialog;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.type.Strategy;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ScanReportPanel extends JPanel {
    public ScanReportPanel() {
        setOpaque(false);
        setLayout(new MigLayout("insets 0"));

        //north - title bar
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[][]push[]push[]15[]15[]15[]15[]10", "5[]5"));
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("cmn_strategy1")));
        ttl_pnl.add(_cmbStrategy);
        ttl_pnl.add(_lblstat); _lblstat.setEditable(false); _lblstat.setHorizontalAlignment(SwingConstants.CENTER);
        WidgetUtil.attachToolTip(_lblstat, ApolloConstants.APOLLO_BUNDLE.getString("l2_wrtip"), SwingConstants.RIGHT, SwingConstants.TOP);
        ttl_pnl.add(_btnScan);
        _btnScan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _lblstat.setText("");
                ArrayList<String> symbols = _pnlWatchlist.getSymbols();
                if (symbols == null) symbols = DataUtil.getAllSymbolsInDb();
                Strategy strategy = (Strategy)_cmbStrategy.getSelectedItem();
                _pnlResult.setStatArea(_lblstat);
                _pnlResult.runReport(strategy, symbols, (float)_fldMinCagr.getValue());
            }
        });
        ttl_pnl.add(_btnGraph);
        _btnGraph.setDisabledIcon(new DisabledIcon(FrameworkIcon.PRICE_CHART.getImage()));
        _btnGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SimGraphDialog dialog = SimGraphDialog.getInstance();
                dialog.setVisible(true);
//                try {
//                    int sel = _pnlResult.getTable().getSelectedRow();
//                    sel = _pnlResult.getTable().convertRowIndexToModel(sel);//convert for sorting
//                    dialog.refreshGraph(_pnlResult.findReportByIndex(sel));
//                } catch (ParseException | IOException e1) {
//                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
//                }
            }
        });
        ttl_pnl.add(_btnExport);
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
//                try {
//                    _pnlResult.exportReports();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        });
        JLabel showhide_btn = new JLabel(LazyIcon.TABLE_COLUMN_OP);
        ttl_pnl.add(showhide_btn);
        WidgetUtil.attachToolTip(showhide_btn, ApolloConstants.APOLLO_BUNDLE.getString("runrpt_lbl_1"), SwingConstants.RIGHT, SwingConstants.TOP);
//        showhide_btn.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent mev) {//to show / hide columns
//                Image image = LazyIcon.APP_ICON.getImage();
//                //gather column names from schema
//                String[] column_names = new String[ReportTableModel.TABLE_SCHEMA.length];
//                for (int row = 0; row < ReportTableModel.TABLE_SCHEMA.length; row++)
//                    column_names[row] = (String) ReportTableModel.TABLE_SCHEMA[row][0];
//                SchemaColumnDialog dlg = new SchemaColumnDialog(_pnlResult.getTable(), column_names,
//                        MdiMainFrame.getInstance(), image, LOCKED_COLUMNS);
//                dlg.setVisibleColumns(ApolloPreferenceStore.getPreferences().getAdvReportColumnVisible());
//                dlg.setVisible(true);
//                boolean[] visible_columns = dlg.getResult();
//                if (null != visible_columns) {
//                    ApolloPreferenceStore.getPreferences().setAdvReportColumnVisible(visible_columns);
//                    ApolloPreferenceStore.savePreferences();
//                    TableUtil.setColumnsVisible(_pnlResult.getTable(), visible_columns);
//                }
//            }
//        });
        add(ttl_pnl, "dock north");

        //west - watch list selector and option pane
        JPanel west_pnl = new JPanel(new MigLayout("insets 0, flowy", "[fill]"));
        west_pnl.add(_pnlOption = new OptionPanel());
        west_pnl.add(_pnlWatchlist = new WatchlistFilterPanel());
        add(west_pnl, "dock west");
        _pnlWatchlist.setEnabled(true);

        //center - result table
        add(_pnlResult = new ScanResultPanel(), "dock center");
    }

    //----- inner classes -----
    private class OptionPanel extends JPanel {
        private OptionPanel() {
            setBorder(new BevelBorder(BevelBorder.LOWERED));
            setLayout(new MigLayout("insets 5 10 5 0"));
            add(new JLabel((ApolloConstants.APOLLO_BUNDLE.getString("scnrpt_mincagr"))));
            add(_fldMinCagr); add(new JLabel("%"));
        }
    }

    //----- variables -----
    private JComboBox<Strategy> _cmbStrategy = new JComboBox<>(Strategy.values());
    private JTextField _lblstat = new JTextField(20);//statistics of this group
    private JButton _btnScan = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_6"), FrameworkIcon.RUN);
    private JButton _btnGraph = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_tip_2"), FrameworkIcon.PRICE_CHART);
    private JButton _btnExport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_5"), FrameworkIcon.EXPORT);
    private DecimalField _fldMinCagr = new DecimalField(10, 5, 0, 40, null);
    private ScanResultPanel _pnlResult;
    private WatchlistFilterPanel _pnlWatchlist;
    private OptionPanel _pnlOption;

    //----- literals -----
    private static final int LOCKED_COLUMNS = 1;//first column is locked, ie.they cannot be hidden
}
