package org.marketsuite.marektview.ranking;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Select a subset of columns in JTable for top frequency calculation.
 */
public class RankingOptionDialog extends JDialog {
    public RankingOptionDialog(JTable _table, String[] columnNames, Image image, RankingPanel parent) {
        super(MdiMainFrame.getInstance(), ApolloConstants.APOLLO_BUNDLE.getString("mkt_44"), false);
        _Table = _table;
        _Parent = parent;
        setIconImage(image);

        //content pane
        JPanel content_pnl = (JPanel)getContentPane();
        content_pnl.setLayout(new MigLayout("flowy"));
        final ActionListener updateListener = new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               updateTable();
//TODO may produce too many calls back to self..../???????????
               //when checked, all lower time frame ones are also checked
           }
        };

        //center - vertical list of checkboxes, one for each column
        final int size = columnNames.length - 2;//exclude symbol and top freq
        _chkColumns = new JCheckBox[size];
        for (int i = 0; i < size; i++) {
            _chkColumns[i] = new JCheckBox(columnNames[i + 1], true);//all checked
            _chkColumns[i].addActionListener(updateListener);
            content_pnl.add(_chkColumns[i]);
        }
        Dimension dim = new Dimension(0, 0);//set dimension first to allow centering
        WidgetUtil.setDialogProperties(this, dim, false, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    //visually change table in the backgroud to new set of columns
    private void updateTable() {
        if (_Table == null)
            return;

        //gather column list,
        int[] tfc = new int[_chkColumns.length];
        for (int i=0; i<_chkColumns.length; i++) {
            int tf = AppUtil.COLUMN_3_MONTH_PCT;
            switch (i) {
                case 0://3 month
                    break;

                case 1://2 month
                    tf = AppUtil.COLUMN_2_MONTH_PCT;
                    break;

                case 2://1 month
                    tf = AppUtil.COLUMN_4_WEEK_PCT;
                    break;

                case 3://2 week
                    tf = AppUtil.COLUMN_2_WEEK_PCT;
                    break;

                case 4://1 week
                    tf = AppUtil.COLUMN_1_WEEK_PCT;
                    break;

                case 5://custom 1
                    tf = RankingPanel.CUSTOM1;
                    break;

                case 6://custom 2
                    tf = RankingPanel.CUSTOM2;
                    break;
            }
            if (_chkColumns[i].isSelected())
                tfc[i] = tf;
        }
        _Parent.populateRankingTable(tfc);
    }

    //instance variables
    private JTable _Table;
    private JCheckBox[] _chkColumns;
    private RankingPanel _Parent;
}