package org.marketsuite.datamgr.quote;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.marektview.ranking.RankingPanel;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

/**
 * A dialog to show obsolete symbols in database
 */
class ObsoleteSymbolsDialog extends JDialog {
    ObsoleteSymbolsDialog(ArrayList<String> symbol_dates) {
        super(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_17"), true);
        setIconImage(LazyIcon.APP_ICON.getImage());
        setResizable(true);

        //content pane
        JPanel content_pnl = new JPanel(new MigLayout("insets 0"));
        setContentPane(content_pnl);
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
//        final ActionListener updateListener = new ActionListener() {
//           public void actionPerformed(ActionEvent e) {
//               updateTable();
//               //when checked, all lower time frame ones are also checked
//           }
//        };

        //center - a list of symbols with most recent dates
        _lstSymbols.setModel(_lmSymbols);
        _lstSymbols.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        for (String sd : symbol_dates)
            _lmSymbols.addElement(sd);
        content_pnl.add(new JScrollPane(_lstSymbols), "dock center");
        JPanel btn_pnl = new JPanel();
        btn_pnl.setOpaque(false);
        _btnDelete = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("dmq_lbl_18"));
        _btnDelete.setEnabled(false);
        _btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                _bCancelled = false;
                dispose();
            }
        });
        btn_pnl.add(_btnDelete);
        JButton cancel_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("cancel"));
        cancel_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                _bCancelled = true;
                dispose();
            }
        });
        btn_pnl.add(cancel_btn);
        content_pnl.add(btn_pnl, "dock south");

        //TODO maybe row click activates quote windows beneath
        _lstSymbols.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                _btnDelete.setEnabled(_lstSymbols.getSelectedValuesList().size() > 0);
//                _lstSymbols.getSelectedValue()
                //TODO drive the quote window beneath
            }
        });
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                _bCancelled = true;
                dispose();
            }
        });

        WidgetUtil.setDialogProperties(this, new Dimension(200, 300), false, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    boolean isCancelled() { return _bCancelled; }
    ArrayList<String> getSelection() {
        List<String> sel = _lstSymbols.getSelectedValuesList();
        ArrayList<String> ret = new ArrayList<>();
        for (String s : sel)
            ret.add(s.substring(0, s.indexOf(" ")));
        return ret;
    }

    //visually change table in the backgroud to new set of columns
//    private void updateTable() {
//        if (_Table == null)
//            return;
//
//        //gather column list,
//        int[] tfc = new int[_chkColumns.length];
//        for (int i=0; i<_chkColumns.length; i++) {
//            int tf = AppUtil.COLUMN_3_MONTH_PCT;
//            switch (i) {
//                case 0://3 month
//                    break;
//
//                case 1://2 month
//                    tf = AppUtil.COLUMN_2_MONTH_PCT;
//                    break;
//
//                case 2://1 month
//                    tf = AppUtil.COLUMN_4_WEEK_PCT;
//                    break;
//
//                case 3://2 week
//                    tf = AppUtil.COLUMN_2_WEEK_PCT;
//                    break;
//
//                case 4://1 week
//                    tf = AppUtil.COLUMN_1_WEEK_PCT;
//                    break;
//
//                case 5://custom 1
//                    tf = RankingPanel.CUSTOM1;
//                    break;
//
//                case 6://custom 2
//                    tf = RankingPanel.CUSTOM2;
//                    break;
//            }
//            if (_chkColumns[i].isSelected())
//                tfc[i] = tf;
//        }
//        _Parent.populateRankingTable(tfc);
//    }

    //instance variables
    private boolean _bCancelled;
    private JButton _btnDelete;
    private JList<String> _lstSymbols = new JList<>();
    private DefaultListModel<String> _lmSymbols = new DefaultListModel<>();
}