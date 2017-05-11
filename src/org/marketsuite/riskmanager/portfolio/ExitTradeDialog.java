package org.marketsuite.riskmanager.portfolio;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmanager.RiskMgrModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;

/**
 * Dialog to obtain several parameters from user while exiting a trade.
 */
class ExitTradeDialog extends JDialog {
    ExitTradeDialog() {
        super(RiskMgrModel.getInstance().getParent(), true);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_6"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //form declaration
        FormLayout layout = new FormLayout(
            "10dlu, r:p, 5dlu, l:p, 10dlu, pref, 10dlu",//3 components each row
            "10dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//2 rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int row = 2;

        //exit date
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("closed_col_1") + ":", cc.xy(2, row));
        builder.add(_txtExitDate, cc.xy(4, row));
        _txtExitDate.setDate(new Date());
        _txtExitDate.setFormats(FrameworkConstants.YAHOO_DATE_FORMAT);

        //exit price
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("closed_col_2") + "($):", cc.xy(2, row));
        builder.add(_txtExitPrice, cc.xy(4, row));

        //cost
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_7"), cc.xy(2, row));
        builder.add(_txtTradeCost, cc.xy(4, row));

        //proceeds
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_8"), cc.xy(2, row));
        builder.add(_txtProceeds, cc.xy(4, row));

        //file name
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_9"), cc.xyw(2, row, 3));
        builder.add(_btnOpen, cc.xy(5, row));
        _btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _PortfolioFile = WidgetUtil.selectSingleFile(FrameworkConstants.EXTENSION_TRANSACTION,
                        RiskMgrModel.getInstance().getParent());
                if (_PortfolioFile == null)
                    return;

                //update text field
                _txtName.setText(_PortfolioFile.getName());
            }
        });
        row += 2;
        builder.add(_txtName, cc.xyw(3, row, 4));  _txtName.setEditable(false);//only for display
        JPanel pnl = builder.getPanel();  pnl.setOpaque(false);
        content_pnl.add(pnl, BorderLayout.CENTER);

        //buttons
        JPanel btn_pnl = new JPanel();
        btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                //disallow empty file name
                if (_PortfolioFile == null) {
                    MessageBox.messageBox(RiskMgrModel.getInstance().getParent(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        Constants.COMPONENT_BUNDLE.getString("empty_msg_1"),
                        MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
                    _PortfolioFile = new File("file_Name" + FrameworkConstants.EXTENSION_TRANSACTION);
                    return;
                }

                //write a line to selected file


                //if file doesn't exist, create first

//                _bCancelled = false;
                dispose();
            }
        });
        btn_pnl.add(ok_btn);
        JButton cancel_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_cancel"));
        cancel_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
//                _bCancelled = true;
//                dispose();
            }
        });
        btn_pnl.add(cancel_btn);
        content_pnl.add(btn_pnl, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        setContentPane(content_pnl);
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false,
            RiskMgrModel.getInstance().getParent(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    File getPortfolioFile() {
        return _PortfolioFile;
    }
    String getExitDate() { return FrameworkConstants.YAHOO_DATE_FORMAT.format(_txtExitDate.getDate()); }
    double getExitPrice() { return _txtExitPrice.getValue(); }
    double getTradeCost() { return _txtTradeCost.getValue(); }
    double getProceeds() { return _txtProceeds.getValue(); }

    //----- instance variables -----
    private JXDatePicker _txtExitDate = new JXDatePicker();
    private DecimalField _txtExitPrice = new DecimalField(0, 5, 0, 1000, null);
    private DecimalField _txtTradeCost = new DecimalField(0, 6, 0, 100000, null);
    private DecimalField _txtProceeds = new DecimalField(0, 6, 0, 100000, null);
    private JButton _btnOpen = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("active_tip_9"), FrameworkIcon.FILE_OPEN);
    private JTextField _txtName = new JTextField(10);
    private File _PortfolioFile;
    private boolean _bCancelled;
}
