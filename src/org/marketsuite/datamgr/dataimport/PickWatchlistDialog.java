package org.marketsuite.datamgr.dataimport;

import org.marketsuite.component.Constants;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//Simple dialog for user to select one or more watch lists
public class PickWatchlistDialog extends JDialog {
    public PickWatchlistDialog(Iterator<String> item_list) {
        super(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("mkt_54"), true);
        JPanel content = new JPanel(new MigLayout());
        Vector<String> items = new Vector<>();
        while (item_list.hasNext())
            items.add(item_list.next());
        Object[] list = items.toArray();
        Arrays.sort(list);
        String[] wls = new String[list.length];
        for (int i=0; i<wls.length; i++)
            wls[i] = (String)list[i];
        _lstWatchlist = new JList<>(wls);
        content.add(new JScrollPane(_lstWatchlist), "dock center");

        //south - buttons
        JPanel btn_pnl = new JPanel(new MigLayout("", "push[][]push"));
        JButton ok = new JButton(Constants.COMPONENT_BUNDLE.getString("ok"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); _bCancelled = false;
            }
        });
        btn_pnl.add(ok);
        JButton cancel = new JButton(Constants.COMPONENT_BUNDLE.getString("cancel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); }
        });
        btn_pnl.add(cancel);
        content.add(btn_pnl, "dock south");
        setContentPane(content);
        WidgetUtil.setDialogProperties(this, new Dimension(250, 450), false, MdiMainFrame.getInstance(),
            WindowConstants.DISPOSE_ON_CLOSE);
    }

    public List<String> getWatchlists() {
        return _lstWatchlist.getSelectedValuesList();
    }
    public boolean isCancelled() { return _bCancelled; }

    private JList<String> _lstWatchlist;
    private boolean _bCancelled = true;//by default
}