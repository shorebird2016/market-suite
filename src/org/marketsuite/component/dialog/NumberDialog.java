package org.marketsuite.component.dialog;

import org.marketsuite.component.Constants;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.field.NameField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Small dialog for entering an integer number.
 */
public class NumberDialog extends JDialog {
    public NumberDialog(final JFrame parent, String prompt_text, int lower_bound, int upper_bound, int default_value) {
        super(parent, true);
        setTitle(Constants.COMPONENT_BUNDLE.getString("msg_03"));
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new MigLayout("", "", "5[]5px[][]"));
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));
        content_pnl.add(new JLabel(prompt_text), "dock north, gaptop 10, gapleft 5, gapright 5, gapright 10");
        content_pnl.add(_txtNumber = new LongIntegerField(default_value, 3, lower_bound, upper_bound), "dock center, gapleft 50, gapright 50");
        JPanel btn_pnl = new JPanel();  btn_pnl.setOpaque(false);
        JButton ok_btn = new JButton(Constants.COMPONENT_BUNDLE.getString("ok"));
        ok_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aev) {
                //check empty file name
                String name = _txtNumber.getText();
                if (name.length() == 0) {//empty field, click ok
                    MessageBox.messageBox(parent,
                        Constants.COMPONENT_BUNDLE.getString("warning"),//title
                        Constants.COMPONENT_BUNDLE.getString("msg_02"),//caption
                        MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
                }
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
    public boolean isCancelled() { return _bCancelled; }
    public int getEntry() { return (int)_txtNumber.getValue(); }

    //instance variables
    private LongIntegerField _txtNumber;
    private boolean _bCancelled;
}