package org.marketsuite.component.border;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Factory for new widget borders for look and feel cosmetic issues
 */
public abstract class StandardBorderFactory {
    /* Private constructor - cannot be instantiated */
    private StandardBorderFactory() {
    	//
    }

    /**
     * Create a "standard" panel border
     */
    public static Border createStandardBorder() {
        return UIManager.getBorder("TitledBorder.border");
    }

    /**
     * Create a "standard" titled panel border
     */
    public static Border createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            createStandardBorder(), title);
    }

    /**
     * Create a border for a cell item
     */
    public static Border createCellItemBorder() {
        String plafid = UIManager.getLookAndFeel().getID();
        if (plafid.equals("Windows")) {
            return UIManager.getBorder("TitledBorder.border");
        } else if (plafid.equals("Motif")) {
            return UIManager.getBorder("TitledBorder.border");
        } else if (plafid.equals("Metal")) {
            return UIManager.getBorder("TitledBorder.border");
        } else {
            return BorderFactory.createEtchedBorder();
        }
    }

    /**
     * Create a border for a cell header
     */
    public static Border createCellHeaderBorder() {
        String plafid = UIManager.getLookAndFeel().getID();
        if (plafid.equals("Windows")) {
            return UIManager.getBorder("Button.border");
        } else if (plafid.equals("Motif")) {
            return UIManager.getBorder("TitledBorder.border");
        } else if (plafid.equals("Metal")) {
            return UIManager.getBorder("TextField.border");
        } else {
            return BorderFactory.createEtchedBorder();
        }
    }
}
