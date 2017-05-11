package org.marketsuite.component.dialog;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.component.Constants;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Small dialog for entering export file name.
 */
public class NameDialog extends JDialog {
    public NameDialog(final JFrame parent, String title, String default_txt) {
        this(parent, default_txt);
        setTitle(title);
    }

    public NameDialog(final JFrame parent, String default_txt) {
        super(parent, true);
        setTitle(Constants.COMPONENT_BUNDLE.getString("msg_01"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //center - box layout with two rows
        JPanel cen_pnl = new JPanel();
        cen_pnl.setOpaque(false);
        cen_pnl.setBorder(new EmptyBorder(5, 10, 0, 10));
        cen_pnl.add(_txtName);
        _txtName.setText(default_txt);
//TODO: auto-clear initial message
//        _txtName.getDocument().addDocumentListener(new DocumentListener() {
//            public void insertUpdate(DocumentEvent e) {
//                removeQue();
//            }
//            public void removeUpdate(DocumentEvent e) {
//                removeQue();
//            }
//            public void changedUpdate(DocumentEvent e) {
//                removeQue();
//            }
//        });
        content_pnl.add(cen_pnl, BorderLayout.CENTER);
        JPanel btn_pnl = new JPanel();
        btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                //check empty file name
                String name = _txtName.getText();
                if (name.length() == 0) //empty field, click ok
                    WidgetUtil.showWarning(Constants.COMPONENT_BUNDLE.getString("msg_02"));
                else {//pass
                    _bCancelled = false;
                    dispose();
                }
            }
        });
        btn_pnl.add(ok_btn);
        JButton cancel_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("cancel"));
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

    //----- accessor -----
    public boolean isCancelled() {
        return _bCancelled;
    }

    public String getEntry() {
        return _txtName.getText();
    }

    //----- private method -----
    private void removeQue() {
        String txt = _txtName.getText();
        if (txt.startsWith(Constants.COMPONENT_BUNDLE.getString("fld_01")))
            _txtName.setText("");
    }

    //instance variables
    private NameField _txtName = new NameField(25, true);
    private boolean _bCancelled;
}