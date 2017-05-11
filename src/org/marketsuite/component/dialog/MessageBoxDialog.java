package org.marketsuite.component.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

/**
 * Dialog used witing MessageBox class.
 */
public class MessageBoxDialog extends JDialog {
	public MessageBoxDialog(MessageBox mb, Component parentComponent, String title) {
        super(MessageBox.getFrameForComponent(parentComponent), title, true);
        this.mb = mb;
        getRootPane().registerKeyboardAction(new MessageBoxDialog.CancelAction(),
            KeyStroke.getKeyStrokeForEvent(
                new KeyEvent(this, KeyEvent.KEY_PRESSED, 0, 0,
                    KeyEvent.VK_ESCAPE, (char) 0x5C)),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mb, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(parentComponent);
        addWindowListener(new MessageBoxDialog.SymWindow());
    }

    protected class CancelAction extends AbstractAction {
		public CancelAction() {
            super("btn_cancel");
        }

        public void actionPerformed(ActionEvent event) {
            cancel();
        }
    }

    private class SymWindow extends WindowAdapter {
        private boolean gotFocus = false;

        public void windowClosing(WindowEvent we) {
            cancel();
        }

        public void windowActivated(WindowEvent we) {
            // Once window gets focus, set initial focus
            if (!gotFocus) {
                mb.selectInitialValue();
                gotFocus = true;
            }
        }
    }

    public void cancel() {
        mb.setValue(null);
    }

    public void dispose() {
        super.dispose();
        MessageBox.fireDisposed(new MessageBoxEvent(mb));
    }

    private MessageBox mb;
}
