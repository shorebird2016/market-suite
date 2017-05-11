package org.marketsuite.simulator.advanced.indicatorsim;

import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * A dialog to add multiple symbols and selecting strategies for them.
 */
class AddSymbolDialog extends JDialog {
    AddSymbolDialog(HashMap<String, ArrayList<Boolean>> symbol_map) {
        super(MdiMainFrame.getInstance(), true);//must have 2nd arg true to wait for user
//todo resize doesn't work
        setResizable(true);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("advsim_ttl_4"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new EmptyBorder(0, 10, 0, 5));

        //center - symbols and check boxes
        JPanel cen_pnl = new JPanel(new GridLayout(1,2));
        cen_pnl.add(new JScrollPane(_lstSymbol = createSymbolList(symbol_map)));

        //center - strategy list
        JScrollPane jsp = new JScrollPane(createStrategySelector());
        jsp.getVerticalScrollBar().setUnitIncrement(_chkStrategy[0].getPreferredSize().height);
        cen_pnl.add(jsp/*new JScrollPane(createStrategySelector())*/);
        content_pnl.add(cen_pnl, BorderLayout.CENTER);

        //south - buttons
        JPanel btn_pnl = new JPanel();  btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
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
        content_pnl.add(btn_pnl, BorderLayout.SOUTH);
        setContentPane(content_pnl);
        getRootPane().setDefaultButton(ok_btn);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        WidgetUtil.setDialogProperties(this, new Dimension(250, 400), false, MdiMainFrame.getInstance(),
            WindowConstants.DISPOSE_ON_CLOSE);
    }

    //-----public methods-----
    public boolean isCancelled() { return _bCancelled; }

    //obtain user selections
    public HashMap<String, ArrayList<Boolean>> getSelection() {
        HashMap<String, ArrayList<Boolean>> ret = new HashMap<String, ArrayList<Boolean>>();
        java.util.List symbols = _lstSymbol.getSelectedValuesList();
        for (Object sym : symbols) {
            //find all checked strategies
            ArrayList<Boolean> strategies = new ArrayList<>();
            for (int index = 0; index < _chkStrategy.length; index++)
                strategies.add(_chkStrategy[index].isSelected());
            ret.put((String)sym, strategies);
        }
        return ret;
    }

    //-----private methods-----
    //a list with all symbols on disk
    private JList createSymbolList(HashMap<String, ArrayList<Boolean>> symbol_map) {
        JList ret = new JList();
        File folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        String[] file_list = folder.list();//read file list
        DefaultListModel/*<String>*/ model = new DefaultListModel/*<String>*/();
        Set<String> used_symbols = symbol_map.keySet();
        for (String f : file_list) {
            int index = f.indexOf(FrameworkConstants.EXTENSION_QUOTE);
            if (index == -1 || f.startsWith(".")) //skip non-quote files or hidden files
                continue;

            String name = f.substring(0, index);

            //skip symbols that are already used
            Iterator<String> itor = used_symbols.iterator();
            boolean used = false;
            while (itor.hasNext())
                if (itor.next().equals(name)) {
                    used = true;
                    break;
                }
            if (used)
                continue;
            model.addElement(name);
        }
        ret.setModel(model);
        ret.setSelectedIndex(0);
        ret.setBorder(new BevelBorder(BevelBorder.LOWERED));
        return ret;
    }

    //vertical list of checkboxes, one for each strategy
    private Box createStrategySelector() {
        Box ret = Box.createVerticalBox();
        int size = FrameworkConstants.LIST_SIM_STRATEGY.length;
        _chkStrategy = new JCheckBox[size];
        for (int i = 0; i < size; i++) {
            _chkStrategy[i] = new JCheckBox(FrameworkConstants.LIST_SIM_STRATEGY[i]);
            _chkStrategy[i].setSelected(true);
            ret.add(_chkStrategy[i]);
        }
        return ret;

    }

    //-----inner classes-----

    //instance variables
    private JList _lstSymbol;
    private JCheckBox[] _chkStrategy;//list of strategies
    private boolean _bCancelled;
}