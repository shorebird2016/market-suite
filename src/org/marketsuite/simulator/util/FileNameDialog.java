package org.marketsuite.simulator.util;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Small dialog for entering export file name.
 */
public class FileNameDialog extends JDialog {
    public FileNameDialog(final JFrame parent, String default_txt) {
        super(parent, true);
        _txtName.setText(default_txt);
        setTitle(ApolloConstants.APOLLO_BUNDLE.getString("file_lbl_1"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //center - box layout with two rows
        JPanel cen_pnl = new JPanel(); cen_pnl.setOpaque(false);
        cen_pnl.setBorder(new EmptyBorder(5, 10, 0, 10));
//        cen_pnl.add(new JLabel(Constants.SETUP_BUNDLE.getString("file_lbl_2")));
//        cen_pnl.add(Box.createHorizontalGlue());
        cen_pnl.add(_txtName);
        content_pnl.add(cen_pnl, BorderLayout.CENTER);
        JPanel btn_pnl = new JPanel();  btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                //check empty file name
                String name = _txtName.getText();
                if (name.length() == 0) {//empty field, click ok
                    MessageBox.messageBox(parent,
                        Constants.COMPONENT_BUNDLE.getString("warning"),//title
                        ApolloConstants.APOLLO_BUNDLE.getString("file_msg_3"),//caption
                        MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
                }
                else {//pass
                    _bCancelled = false;
                    dispose();
                }
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
        WidgetUtil.setDialogProperties(this, new Dimension(0, 0), false, parent, WindowConstants.DISPOSE_ON_CLOSE);
    }

    public boolean isCancelled() { return _bCancelled; }

    public String getName() { return _txtName.getText(); }

    //instance variables
    private NameField _txtName = new NameField(25, true);
    private boolean _bCancelled;
    private static final long serialVersionUID = 691875913484920194L;
}