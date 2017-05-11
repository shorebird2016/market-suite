package org.marketsuite.component.text;

import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

/**
 * An alternative to JTextComponent.  Whenever a text component becomes
 * uneditable, this grays the text component and removes it from the focus
 * traversal.
 */
public class TextComponentBase extends JTextComponent {
    public TextComponentBase() { super(); }

    public void setEditable(boolean b) {
        super.setEditable(b);
        setEnabledStateUI(b, this);
    }

    /**
     * Returns true if the focus can be traversed.
     * @return true if the focus is traversable
     */
    public boolean isFocusable() {
        return super.isFocusable() && super.isEditable();
    }

    public static void setEnabledStateUI(boolean b, JTextComponent c) {
        if (b) {
            c.setBorder(UIManager.getBorder("TextField.border"));
            c.setForeground(UIManager.getColor("TextField.foreground"));
            c.setBackground(UIManager.getColor("TextField.background"));
        }
        else {
            String plafid = UIManager.getLookAndFeel().getID();
            if (plafid.equals("Windows")) {
                c.setBorder(UIManager.getBorder("TextField.border"));
                c.setForeground(UIManager.getColor("TextField.foreground"));
                c.setBackground(UIManager.getColor("Panel.background"));
            }
            else if (plafid.equals("Motif")) {
                c.setBorder(UIManager.getBorder("TextField.border"));
                c.setForeground(UIManager.getColor("TextField.foreground"));
                c.setBackground(UIManager.getColor("Panel.background"));
            }
            else if (plafid.equals("Metal")) {
                c.setBorder(UIManager.getBorder("TitledBorder.border"));
                c.setForeground(UIManager.getColor("TextField.foreground"));
                c.setBackground(UIManager.getColor("Panel.background"));
            }
            else {
                c.setBorder(UIManager.getBorder("TextField.border"));
                c.setForeground(UIManager.getColor("TextField.inactiveForeground"));
                c.setBackground(UIManager.getColor("TextField.background"));
            }
        }
    }
}
