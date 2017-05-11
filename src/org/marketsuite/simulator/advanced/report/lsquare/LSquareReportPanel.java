package org.marketsuite.simulator.advanced.report.lsquare;

import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.TableUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.strategy.lsquare.SimOptionDialog;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.component.panel.WatchlistFilterPanel;
import org.marketsuite.simulator.advanced.SimGraphDialog;
import org.marketsuite.simulator.advanced.report.model.ReportTableModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.dialog.SchemaColumnDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.strategy.lsquare.SimOptionDialog;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.report.model.ReportTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.ParseException;

//container for running L2 method related reports
public class LSquareReportPanel extends JPanel {
    public LSquareReportPanel() {
        setOpaque(false);
        setLayout(new MigLayout("insets 0"));

        //north - title bar
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[]push[][]push[]15[]15[]15[]15[]10", "5[]5"));
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("l2_info")));
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("l2_stat")));
        ttl_pnl.add(_lblWinRatio); _lblWinRatio.setEditable(false); _lblWinRatio.setHorizontalAlignment(SwingConstants.CENTER);
        WidgetUtil.attachToolTip(_lblWinRatio, ApolloConstants.APOLLO_BUNDLE.getString("l2_wrtip"), SwingConstants.RIGHT, SwingConstants.TOP);
        ttl_pnl.add(_btnSimOption);
        _btnSimOption.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                _dlgOption.setVisible(true);
            }
        });
        ttl_pnl.add(_btnRunReport);
        _btnRunReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _lblWinRatio.setText("");
                _pnlReport.runReport(_pnlWatchlist.getSymbols(), _lblWinRatio, _dlgOption.getOptions());
            }
        });
        ttl_pnl.add(_btnGraph);
        _btnGraph.setDisabledIcon(new DisabledIcon(FrameworkIcon.PRICE_CHART.getImage()));
        _btnGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SimGraphDialog dialog = SimGraphDialog.getInstance();
                dialog.setVisible(true);
                try {
                    int sel = _pnlReport.getTable().getSelectedRow();
                    sel = _pnlReport.getTable().convertRowIndexToModel(sel);//convert for sorting
                    dialog.refreshGraph(_pnlReport.findReportByIndex(sel));
                } catch (ParseException | IOException e1) {
                    WidgetUtil.showWarning(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dme_txt_4") + e1.getMessage());
                }
            }
        });
        ttl_pnl.add(_btnExport);
        _btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    _pnlReport.exportReports();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        JLabel showhide_btn = new JLabel(LazyIcon.TABLE_COLUMN_OP);
        ttl_pnl.add(showhide_btn);
        WidgetUtil.attachToolTip(showhide_btn, ApolloConstants.APOLLO_BUNDLE.getString("runrpt_lbl_1"), SwingConstants.RIGHT, SwingConstants.TOP);
        showhide_btn.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mev) {//to show / hide columns
                Image image = LazyIcon.APP_ICON.getImage();
                //gather column names from schema
                String[] column_names = new String[ReportTableModel.TABLE_SCHEMA.length];
                for (int row = 0; row < ReportTableModel.TABLE_SCHEMA.length; row++)
                    column_names[row] = (String) ReportTableModel.TABLE_SCHEMA[row][0];
                SchemaColumnDialog dlg = new SchemaColumnDialog(_pnlReport.getTable(), column_names,
                        MdiMainFrame.getInstance(), image, LOCKED_COLUMNS);
                dlg.setVisibleColumns(ApolloPreferenceStore.getPreferences().getAdvReportColumnVisible());
                dlg.setVisible(true);
                boolean[] visible_columns = dlg.getResult();
                if (null != visible_columns) {
                    ApolloPreferenceStore.getPreferences().setAdvReportColumnVisible(visible_columns);
                    ApolloPreferenceStore.savePreferences();
                    TableUtil.setColumnsVisible(_pnlReport.getTable(), visible_columns);
                }
            }
        });
        add(ttl_pnl, "dock north");

        //west - watch list selector
        add(_pnlWatchlist = new WatchlistFilterPanel(), "dock west");
        _pnlWatchlist.setEnabled(true);

        //center - result table
        add(_pnlReport = new ReportPanel(), "dock center");
    }

    //----- variables -----
    private JTextField _lblWinRatio = new JTextField(20);//statistics of this group
    protected JButton _btnSimOption = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("macosc_lbl_9"), FrameworkIcon.SETTING);
    private JButton _btnRunReport = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_lbl_6"), FrameworkIcon.RUN);
    private JButton _btnGraph = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("runrpt_tip_2"), FrameworkIcon.PRICE_CHART);
    private JButton _btnExport = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("mac_tip_5"), FrameworkIcon.EXPORT);
    private ReportPanel _pnlReport;
    private WatchlistFilterPanel _pnlWatchlist;
    private SimOptionDialog _dlgOption = new SimOptionDialog();

    //----- literals -----
    private static final int LOCKED_COLUMNS = 1;//first column is locked, ie.they cannot be hidden
}
