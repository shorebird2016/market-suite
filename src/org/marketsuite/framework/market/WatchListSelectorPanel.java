package org.marketsuite.framework.market;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class WatchListSelectorPanel extends JPanel {
    public WatchListSelectorPanel() {
        setLayout(new BorderLayout());
        JPanel btn_pnl = new JPanel();  btn_pnl.setOpaque(false);
        btn_pnl.add(_btnSelectAll);  btn_pnl.add(Box.createGlue());
        _btnSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkWatchListNames)
                    chk.setSelected(true);
            }
        });
        btn_pnl.add(_btnSelectNone);
        _btnSelectNone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox chk : _chkWatchListNames)
                    chk.setSelected(false);
            }
        });
        add(WidgetUtil.createTitleStrip(ApolloConstants.APOLLO_BUNDLE.getString("trk_34"), btn_pnl), BorderLayout.NORTH);

        //use MigLayout to put down many checkboxes
        MigLayout layout = new MigLayout("insets 2 5 2 5,flowy, gapy 0, wrap 10");
        JPanel box = new JPanel(layout);
        _sWatchListNames = GroupStore.getInstance().getGroupNames();
        final int size = _sWatchListNames.size();
        _chkWatchListNames = new JCheckBox[size];
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox)e.getSource();
                if (src.isSelected())
                    _sSelection.add(src.getText());
                else
                    _sSelection.remove(src.getText());
            }
        };
        for (int i = 0; i < size; i++) {
            _chkWatchListNames[i] = new JCheckBox(_sWatchListNames.get(i));
            _chkWatchListNames[i].setFocusable(false);
            box.add(_chkWatchListNames[i]);
            _chkWatchListNames[i].addActionListener(listener);
        }
        JScrollPane jsp = new JScrollPane(box);
        jsp.getVerticalScrollBar().setUnitIncrement(_chkWatchListNames[0].getPreferredSize().height);
        add(jsp, BorderLayout.CENTER);
    }

    public ArrayList<String> getSelection() { return _sSelection; }
    public ArrayList<String> getWatchListNames() { return _sWatchListNames; }

    private ArrayList<String> _sWatchListNames;//same index as check boxes
    private ArrayList<String> _sSelection = new ArrayList<>();
    private JCheckBox[] _chkWatchListNames;
    private JButton _btnSelectAll = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_09"), FrameworkIcon.SELECT_ALL);
    private JButton _btnSelectNone = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("trk_10"), FrameworkIcon.CLEAR);
}
