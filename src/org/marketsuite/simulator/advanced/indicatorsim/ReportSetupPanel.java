package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import org.marketsuite.simulator.advanced.report.model.ReportTemplate;
import org.marketsuite.simulator.advanced.report.model.StrategySetting;
import org.marketsuite.simulator.util.FileNameDialog;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.simulator.advanced.report.model.StrategySetting;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Container to setup and run advanced simulation.
 */
class ReportSetupPanel extends SkinPanel {
    ReportSetupPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.LOWERED));

        //north - title strip with text and tool buttons
        JPanel tool_pnl = new JPanel(new FlowLayout(FlowLayout.LEFT)); tool_pnl.setOpaque(false);
        tool_pnl.add(_btnAddTemplate);
        _btnAddTemplate.setDisabledIcon(new DisabledIcon(LazyIcon.PLUS_SIGN.getImage()));
        _btnAddTemplate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileNameDialog dlg = new FileNameDialog(MdiMainFrame.getInstance(), "");
                if (dlg.isCancelled())
                    return;

                //check duplicate
                String name = dlg.getName();
                Enumeration names = _ReportListModel.elements();
                while (names.hasMoreElements()) {
                    if (name.equals(names.nextElement())) {
                        MessageBox.messageBox(MdiMainFrame.getInstance(),
                            Constants.COMPONENT_BUNDLE.getString("warning"),//title
                            ApolloConstants.APOLLO_BUNDLE.getString("file_msg_2"),//caption
                            MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
                        return;
                    }
                }

                //add to list on left, blank out table, set defaults to forms
                _ReportListModel.addElement(name);
                _pnlSymbol.clearTable();
                _pnlSetting.clear();

                //add blank report template
                ApolloPreferenceStore.getPreferences().getAdvReportTemplates().add(
                    new ReportTemplate(name, new HashMap<String, ArrayList<Boolean>>()));

                //select this new report
                _lstReports.setSelectedIndex(_ReportListModel.size() - 1);
            }
        });
        tool_pnl.add(_btnDeleteTemplate);
        _btnDeleteTemplate.setDisabledIcon(new DisabledIcon(LazyIcon.MINUS_SIGN.getImage()));
        _btnDeleteTemplate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String rpt_name = (String)_lstReports.getSelectedValue();

                //find template from ReportTemplates in pref and view
                ArrayList<ReportTemplate> templates = ApolloPreferenceStore.getPreferences().getAdvReportTemplates();
                for (ReportTemplate template : templates) {
                    if (template.getReportName().equals(rpt_name)) {
                        templates.remove(template);
                        _ReportListModel.remove(_lstReports.getSelectedIndex());
                        break;
                    }
                }
            }
        });

        //save templates
        tool_pnl.add(_btnSaveTemplate);
        _btnSaveTemplate.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILE_SAVE.getImage()));
        _btnSaveTemplate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                settingToModel();
                ApolloPreferenceStore.savePreferences();
            }
        });

        SkinPanel ttl_pnl = WidgetUtil.createTitleStrip(tool_pnl, null, null);
        add(ttl_pnl, BorderLayout.NORTH);

        //vertical splitter - report list on left, right side has report detail
        JSplitPane hor_spl = new JSplitPane();

        //left - list of reports
        hor_spl.setDividerLocation(200);
        hor_spl.setContinuousLayout(true);
        hor_spl.setLeftComponent(new JScrollPane(_lstReports));
        _lstReports.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _lstReports.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                handleReportSelection();
            }
        });

        //right side - table and form
        JPanel rite_pnl = new JPanel(new BorderLayout());
        hor_spl.setRightComponent(rite_pnl);
        rite_pnl.add(_pnlSymbol = new SymbolStrategyPanel(), BorderLayout.CENTER);
        rite_pnl.add(_pnlSetting = new SettingPanel(), BorderLayout.SOUTH);
        add(hor_spl, BorderLayout.CENTER);

        //south - some hints
        ttl_pnl = WidgetUtil.createTitleStrip(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_2"));
        add(ttl_pnl, BorderLayout.SOUTH);

        //initialize report template list from preference storage
        ArrayList<ReportTemplate> templates = ApolloPreferenceStore.getPreferences().getAdvReportTemplates();
        if (templates.size() > 0) {
            for (ReportTemplate template : templates)
                _ReportListModel.addElement(template.getReportName());
            _lstReports.setSelectedIndex(0);
        }
        else {
            _btnDeleteTemplate.setEnabled(false);
//            _btnSaveTemplate.setEnabled(false);
        }
    }

    //-----public methods-----
    public ArrayList<String> getReportList() {
        ArrayList<String> ret = new ArrayList<String>();
        Enumeration<?> rpt_enum = _ReportListModel.elements();
        while (rpt_enum.hasMoreElements()) {
            ret.add((String)rpt_enum.nextElement());
        }
        return ret;
    }

    //transfer all settings from currently selected report into pref
    public void settingToModel() {
        ArrayList<ReportTemplate> templates = ApolloPreferenceStore.getPreferences().getAdvReportTemplates();
        int cur_index = _lstReports.getSelectedIndex();
        if (cur_index == -1)
            return;

        ReportTemplate cur_template = templates.get(cur_index);
        ReportSetting rpt_setting = _pnlSetting.getReportSetting();
        cur_template.setTimeSetting(rpt_setting.getTimeSetting());
        cur_template.setStrategySetting(new StrategySetting(rpt_setting.getMacOption(),
            rpt_setting.getMzcOption(), rpt_setting.getRsiOption(), rpt_setting.getStochasticOption()));
    }

    //-----private methods-----
    //report list drop down just got changed
    private boolean handleReportSelection() {
        boolean empty_selection = _lstReports.isSelectionEmpty();
        _btnDeleteTemplate.setEnabled(!empty_selection);
        if (empty_selection) {
            _pnlSymbol.clearTable();
            _pnlSetting.clear();
            _nPrevIndex = -1;
        }
        //populate list, table, forms
        else {
            //check if symbol-strategy table is dirty, update report prev_template when necessary
            ArrayList<ReportTemplate> templates = ApolloPreferenceStore.getPreferences().getAdvReportTemplates();
            if (_nPrevIndex != -1) {
                ReportTemplate prev_template = templates.get(_nPrevIndex);
                if (_pnlSymbol.isDirty()) {
                    HashMap<String, ArrayList<Boolean>> sym_map = _pnlSymbol.getSymbolMap();
                    prev_template.setSymbolMap(sym_map);
                    _pnlSymbol.setDirty(false);
                }

                //update time and strategy settings regardless of dirty
                ReportSetting rpt_setting = _pnlSetting.getReportSetting();
                prev_template.setTimeSetting(rpt_setting.getTimeSetting());
                prev_template.setStrategySetting(new StrategySetting(rpt_setting.getMacOption(), rpt_setting.getMzcOption(),
                    rpt_setting.getRsiOption(), rpt_setting.getStochasticOption()));
            }

            //populate symbol map, time setting, strategy setting with values from new selection
            int cur_index = _lstReports.getSelectedIndex();
            ReportTemplate cur_template = templates.get(cur_index);
            _pnlSymbol.populate(cur_template.getSymbolMap());
            _pnlSetting.populate(cur_template);
            _nPrevIndex = cur_index;

            //notify run panel to select the same template
            Props.TemplateChange.setValue(cur_template.getReportName());
        }
        return empty_selection;
    }

    //-----instance variables-----
    private DefaultListModel _ReportListModel = new DefaultListModel();//must use this class
    private JList<String> _lstReports = new JList(_ReportListModel);//todo when jdk1.7 works on MAC
    private JButton _btnAddTemplate = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_tip_1"), LazyIcon.PLUS_SIGN);
    private JButton _btnDeleteTemplate = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_tip_2"), LazyIcon.MINUS_SIGN);
    private JButton _btnSaveTemplate = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("advsim_tip_3"), FrameworkIcon.FILE_SAVE);
    private SymbolStrategyPanel _pnlSymbol;
    private SettingPanel _pnlSetting;
    private int _nPrevIndex = -1;//no selection = -1
}
