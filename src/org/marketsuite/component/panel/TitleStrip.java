package org.marketsuite.component.panel;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.marketsuite.component.Constants;
import org.marketsuite.component.resource.LazyIcon;

/**
 * A general purpose title bar that supports variable content such as title text and east side component
 * and follows the general look and feel
 */
public class TitleStrip extends SkinPanel {
   public TitleStrip(String title_text, JComponent east_component) {
        super(LazyIcon.BACKGROUND_TABLE_HEADER, new BorderLayout());
        JPanel lbl_pnl = new JPanel();//holds title label
        lbl_pnl.setOpaque(false);
        _lblTitle = new JLabel(title_text);
        _lblTitle.setFont(Constants.LINK_FONT_BOLD);
        lbl_pnl.add(_lblTitle);
        add(lbl_pnl, BorderLayout.WEST);
        add(_EastComponent = east_component, BorderLayout.EAST);
    }

    public TitleStrip(JComponent west_component, JComponent center_component, JComponent east_component) {
        super(LazyIcon.BACKGROUND_TABLE_HEADER, new BorderLayout());
        if (west_component instanceof JLabel) {
            _lblTitle = (JLabel)west_component;
        }
        add(west_component, BorderLayout.WEST);
        add(center_component, BorderLayout.CENTER);
        add(east_component, BorderLayout.EAST);
    }

    public void setTitle(String title_text) {
        _lblTitle.setText(title_text);
    }

    public JComponent getEastComponent() {
        return _EastComponent;
    }

    //instance variables
    private JComponent _EastComponent;
    private JLabel _lblTitle;
    private static final long serialVersionUID = -1L;
}