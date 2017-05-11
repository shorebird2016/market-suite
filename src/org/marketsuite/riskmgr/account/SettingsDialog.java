package org.marketsuite.riskmgr.account;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.RiskMgrModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog to obtain several parameters from user while exiting a trade.
 */
class SettingsDialog extends JDialog {
    SettingsDialog(int atr_len, int look_back, double qtr_adj) {
        super(RiskMgrModel.getInstance().getParent(), true);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("active_lbl_4"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //form declaration
        FormLayout layout = new FormLayout(
            "10dlu, r:p, 5dlu, l:p, 10dlu, pref, 10dlu",//3 components each row
            "10dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu"//6 rows
        );
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        int row = 2;

        //ATR length
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("ss_lbl_1"), cc.xy(2, row));
        builder.add(_txtAtrLength, cc.xy(4, row));
        _txtAtrLength.setText(String.valueOf(atr_len));

        //look back for quotes
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("ss_lbl_2"), cc.xy(2, row));
        builder.add(_txtLookbackCount, cc.xy(4, row));
        _txtLookbackCount.setText(String.valueOf(look_back));

        //quarterly adjustment factor
        row += 2;
        builder.addLabel(ApolloConstants.APOLLO_BUNDLE.getString("ss_lbl_3"), cc.xy(2, row));
        builder.add(_txtCostAdjFactor, cc.xy(4, row));  _txtCostAdjFactor.setValue(1.5);
        _txtCostAdjFactor.setText(String.valueOf(qtr_adj));
        JPanel pnl = builder.getPanel();  pnl.setOpaque(false);
        content_pnl.add(pnl, BorderLayout.CENTER);

        //buttons
        JPanel btn_pnl = new JPanel();
        btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
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
                _bCancelled = true;
                dispose();
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
        getRootPane().setDefaultButton(ok_btn);
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false,
            RiskMgrModel.getInstance().getParent(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    boolean isCancelled() { return _bCancelled; }
    int getAtrLen() { return (int)_txtAtrLength.getValue(); }
    int getLookback() { return (int)_txtLookbackCount.getValue(); }
    double getAdjFactor() { return (double)_txtCostAdjFactor.getValue(); }

    //----- instance variables -----
    private LongIntegerField _txtAtrLength = new LongIntegerField(14, 5, 1, 100, true);
    private LongIntegerField _txtLookbackCount = new LongIntegerField(60, 5, 1, 500, true);
    private DecimalField _txtCostAdjFactor = new DecimalField(1.5, 6, 0, 4, null);

    private boolean _bCancelled;
}
