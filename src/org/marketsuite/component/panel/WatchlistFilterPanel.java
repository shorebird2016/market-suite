package org.marketsuite.component.panel;

import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class WatchlistFilterPanel extends JPanel {
    public WatchlistFilterPanel() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel btn_pnl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]push[]5[]5", "2[]2"));  btn_pnl.setOpaque(false);
        btn_pnl.add(_chkEnable); _chkEnable.setFocusable(false); _chkEnable.setOpaque(false);
        _chkEnable.setFont(FrameworkConstants.SMALL_FONT_BOLD); _chkEnable.setSelected(true);
        _chkEnable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableForm();
                if (_chkEnable.isSelected()) _chkGroups[0].setSelected(true);//force first selection
            }
        });
        btn_pnl.add(_btnSelectAll);
        _btnSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkGroups)
                    chk.setSelected(true);
            }
        });
        btn_pnl.add(_btnSelectNone);
        _btnSelectNone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkGroups)
                    chk.setSelected(false);
            }
        });
        add(btn_pnl, "dock north");

        //use MigLayout to put down many checkboxes
        MigLayout layout = new MigLayout("insets 2 5 2 5,flowy, gapy 0");
        JPanel box = new JPanel(layout);
        ArrayList<String> names = GroupStore.getInstance().getGroupNames();
        final int size = names.size();
        _chkGroups = new JCheckBox[size];
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //are all checkboxes unchecked?
                boolean all_clear = true;
                for (JCheckBox chk : _chkGroups) {
                    if (chk.isSelected()) {
                        all_clear = false;
                        break;
                    }
                }
            }
        };
        for (int i = 0; i < size; i++) {
            _chkGroups[i] = new JCheckBox(names.get(i));
            _chkGroups[i].setFocusable(false);
            box.add(_chkGroups[i]);
            _chkGroups[i].addActionListener(listener);
        }
        JScrollPane jsp = new JScrollPane(box);
        jsp.getVerticalScrollBar().setUnitIncrement(_chkGroups[0].getPreferredSize().height);
        add(jsp, "dock center");
//        setPreferredSize(new Dimension(250, 1000));
        enableForm();
    }

    //public methods
    // return null = all symbols in DB
    public ArrayList<String> getSymbols() {
        ArrayList<String> ret = new ArrayList<>();
        if (!_chkEnable.isSelected())
            return null;//disabled, use all symbols in DB
        for (JCheckBox chk : _chkGroups) {
            if (!chk.isSelected()) continue;
            String grp_name = chk.getText();
            ArrayList<String> members = GroupStore.getInstance().getMembers(grp_name);
            for (String member : members)
                if (!ret.contains(member)) //remove duplicate
                    ret.add(member);
        }
        return ret;
    }
    public boolean hasNoSelection() {
        if (!_chkEnable.isSelected()) return false;//disable => all pass
        for (JCheckBox chk : _chkGroups) {
            if (chk.isSelected())
                return false;
        }
        return true;
    }
    public void setEnabled(boolean enable) {
        _chkEnable.setSelected(enable);
        enableForm();
    }

    //private methods
    private void enableForm() {
        boolean enable = _chkEnable.isSelected();
        for (JCheckBox chk : _chkGroups)
            chk.setEnabled(enable);
    }

    //----- variables -----
    private JCheckBox _chkEnable = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qp_09"));
    private JCheckBox[] _chkGroups;
    private JButton _btnSelectAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_09"), FrameworkIcon.SELECT_ALL);
    private JButton _btnSelectNone = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_10"), FrameworkIcon.CLEAR);
}
