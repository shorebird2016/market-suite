package org.marketsuite.component.text;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

/**
 * An alternative to JTextField.  Whenever a text component becomes
 * uneditable, this grays the text component and removes it from the focus
 * traversal.
 */
public class TextFieldBase extends JTextField {
    public TextFieldBase() {
        super();
        initReturn();
    }

    public TextFieldBase(String text) {
        super(text);
        initReturn();
    }

    public TextFieldBase(int columns) {
        super(columns);
        initReturn();
    }

    public TextFieldBase(String text, int columns) {
        super(text, columns);
        initReturn();
    }

    public TextFieldBase(Document doc, String text, int columns) {
        super(doc, text, columns);
        initReturn();
    }

    public void setEditable(boolean b) {
        super.setEditable(b);
        TextComponentBase.setEnabledStateUI(b, this);
    }

    /**
     * Returns true if the focus can be traversed.
     * @return true if the focus is traversable
     */
    public boolean isFocusable() {
        return super.isFocusable() && super.isEditable();
    }

    public void initReturn() {
        KeyBinding[] bindings =
            {
                new KeyBinding(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, 0), RETURN_ACTION),
            };
        Keymap k = getKeymap();
        JTextComponent.loadKeymap(k, bindings,
            new Action[]{new TextFieldBase.ReturnAction(),});
    }

    public final static String RETURN_ACTION = "returnAction";

    class ReturnAction extends AbstractAction {
        public ReturnAction() { super(RETURN_ACTION); }

        public void actionPerformed(ActionEvent ev) {
            JRootPane root = getRootPane();
            if (root != null) {
                JButton defaultButton = root.getDefaultButton();
                if (defaultButton != null)
                    defaultButton.doClick();
            }
        }
    }
}
