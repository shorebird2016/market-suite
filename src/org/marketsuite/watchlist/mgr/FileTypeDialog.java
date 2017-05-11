package org.marketsuite.watchlist.mgr;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.type.ImportFileType;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Small dialog for selecting type of file for import.
 */
public class FileTypeDialog extends JDialog {
    public FileTypeDialog() {
        super(MdiMainFrame.getInstance(), true);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("wlg_msg_3"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //center - box layout with two rows
        JPanel cen_pnl = new JPanel(); cen_pnl.setOpaque(false);
        cen_pnl.setBorder(new EmptyBorder(5, 10, 0, 10));
        cen_pnl.add(_cmbSources);
        _cmbSources.setRenderer(new BasicComboBoxRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (comp instanceof JLabel) {
                    JLabel lbl = (JLabel)comp;
                    lbl.setText(((ImportFileType)value).getDisplayText());
                }
                return comp;
            }
        });
        content_pnl.add(cen_pnl, BorderLayout.CENTER);
        JPanel btn_pnl = new JPanel();  btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(ApolloConstants.APOLLO_BUNDLE.getString("wlg_lbl_12"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                _bCancelled = false;
                dispose();
            }
        });
        btn_pnl.add(ok_btn);
        content_pnl.add(btn_pnl, BorderLayout.SOUTH);
        setContentPane(content_pnl);
        getRootPane().setDefaultButton(ok_btn);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });
        WidgetUtil.setDialogProperties(this, new Dimension(200, 100), false, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    public boolean isCancelled() { return _bCancelled; }

    public ImportFileType getFileType() {
        return (ImportFileType)_cmbSources.getSelectedItem();
    }

    //instance variables
    private JComboBox _cmbSources = new JComboBox(ImportFileType.values());
    private boolean _bCancelled;
}