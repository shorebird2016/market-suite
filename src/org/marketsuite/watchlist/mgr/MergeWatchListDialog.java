package org.marketsuite.watchlist.mgr;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.WatchListSelectorPanel;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.market.WatchListSelectorPanel;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * To merge several watch lists into one.
 */
public class MergeWatchListDialog extends JDialog {
    public MergeWatchListDialog(final JFrame parent) {
        super(parent, true);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_72"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //center - box layout with two rows
        JPanel cen_pnl = new JPanel(); cen_pnl.setOpaque(false);
        cen_pnl.add(_pnlWatchList = new WatchListSelectorPanel());
        content_pnl.add(cen_pnl, BorderLayout.CENTER);

        //south - combo for name and buttons
        JPanel south_pnl = new JPanel(new BorderLayout());
        JPanel cmb_pnl = new JPanel();
        cmb_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_74")));
        WidgetUtil.attachToolTip(_cmbWatchListName, ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_75"),
                SwingConstants.CENTER, SwingConstants.BOTTOM);
        cmb_pnl.add(_cmbWatchListName);
        ArrayList<String> full_list = _pnlWatchList.getWatchListNames();
        String[] fl = new String[full_list.size()];
        for (int i=0; i<fl.length; i++)
            fl[i] = full_list.get(i);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(fl);
        _cmbWatchListName.setModel(model);
        _cmbWatchListName.setEditable(true); _cmbWatchListName.setSelectedItem(null);//empty safer
        cmb_pnl.add(_cmbWatchListName);
        south_pnl.add(cmb_pnl, BorderLayout.WEST);

        //buttons
        JPanel btn_pnl = new JPanel();  btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                //check empty destination name
                Object item = _cmbWatchListName.getSelectedItem();
                if (item == null || item.equals("")) {
                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("wl_empty_name")); return; }

                //check over-write existing watchlist
                if (GroupStore.getInstance().doesGroupExist((String)item))
                    if (!WidgetUtil.confirmOkCancel(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("wl_ovr_grp")))
                        return;
//                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("wl_ovr_grp")); return; }

                //check number of selection
                _WatchListNames = _pnlWatchList.getSelection();
                if (_WatchListNames.size() <= 1) {//empty or 1 selection
                    WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("wl_lbl_73")); return; }
                _bCancelled = false;
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
        south_pnl.add(btn_pnl, BorderLayout.CENTER);
        content_pnl.add(south_pnl, BorderLayout.SOUTH);
        setContentPane(content_pnl);
        getRootPane().setDefaultButton(ok_btn);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false, parent, WindowConstants.DISPOSE_ON_CLOSE);
    }

    //----- accessor -----
    public boolean isCancelled() { return _bCancelled; }
    public ArrayList<String> getWatchListNames() { return _WatchListNames; }
    public String getMergedName() { return (String)_cmbWatchListName.getEditor().getItem(); }

    //instance variables
    private boolean _bCancelled;
    private ArrayList<String> _WatchListNames = new ArrayList<>();
    private WatchListSelectorPanel _pnlWatchList;
    private JComboBox<String> _cmbWatchListName = new JComboBox<>();
}