package org.marketsuite.scanner.common;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class WatchlistSelectorWindow extends JWindow {
    public WatchlistSelectorWindow() {
        JPanel content = new JPanel(new MigLayout());  content.setOpaque(false);
        content.setBorder(new BevelBorder(BevelBorder.RAISED));
        setContentPane(content);

        //north - title
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[]push[]10[]10[]5", "3[]3"));//  ttl_pnl.setOpaque(false);
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("scn_lbl_2"));
        lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl_pnl.add(lbl);
        ttl_pnl.add(_btnSelectAll);
        _btnSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkWatchlists)
                    chk.setSelected(true);
            }
        });
        ttl_pnl.add(_btnSelectNone);
        _btnSelectNone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkWatchlists)
                    chk.setSelected(false);
            }
        });
        content.add(ttl_pnl, "dock north");

        //center - checkboxes
        ArrayList<String> names = GroupStore.getInstance().getGroupNames();
        int size = names.size();
        JPanel box = new JPanel();
        if (size < 50)
            box.setLayout(new MigLayout("insets 2 5 2 5,flowy, wrap 10, gapy 0"));
        else
            box.setLayout(new MigLayout("insets 2 5 2 5,flowy, wrap 25, gapy 0"));
        _chkWatchlists = new JCheckBox[size];
        for (int i = 0; i < size; i++) {
            _chkWatchlists[i] = new JCheckBox(names.get(i));
            _chkWatchlists[i].setFocusable(false);
            box.add(_chkWatchlists[i]);
        }
        content.add(new JScrollPane(box), "dock center");
        WidgetUtil.setWindowProperties(this, new Dimension(500, 600));
    }

    public ArrayList<String> getSelectedList() {
        ArrayList<String> ret = new ArrayList<>();
        for (JCheckBox cb : _chkWatchlists) {
            if (cb.isSelected())
                ret.add(cb.getText());
        }
        return ret;
    }

    //----- variables -----
    private JButton _btnSelectAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_09"), FrameworkIcon.SELECT_ALL);
    private JButton _btnSelectNone = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_10"), FrameworkIcon.CLEAR);
    private JCheckBox[] _chkWatchlists;
}
