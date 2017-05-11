package org.marketsuite.component.label;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import org.marketsuite.component.resource.LazyIcon;

/**
 * a label with skin capabilities, pluggable background like synth does.
 * The regular JLabel allows images but it's side by side to text, not the same
 */
public class SkinLabel extends JLabel {
	//CTOR: create this label with 3 default background images
    public SkinLabel(String text) {
        this(text, LazyIcon.IMAGE_LABEL_NORMAL, LazyIcon.IMAGE_LABEL_SELECTED, LazyIcon.IMAGE_LABEL_HOVER);
    }

    //CTOR:create this object with 3 types of background images
    public SkinLabel(String text, LazyIcon normal_bkgnd, LazyIcon seleted_bkgnd, LazyIcon hover_bkgnd) {
        super(text, JLabel.CENTER);
        _CurrentBackground =_NormalBackground = normal_bkgnd;
        _SelectedBackground = seleted_bkgnd;
        _HoverBackground = hover_bkgnd;
        addMouseListener(new MouseAdapter() {//only handle hovering image change
            public void mouseEntered(MouseEvent e) {
                setSkin(SkinLabel.HOVER);
            }

            public void mouseExited(MouseEvent e) {
                setSkin(_nState);
            }
        });
    }

    //interface implementation
    public void paint(Graphics g) {
        //add background first
        Dimension size = getSize();
        g.fillRect(0, 0, size.width, size.height);
        g.drawImage(_CurrentBackground.getImage(), 0, 0, getWidth(), getHeight(), null);
        super.paint(g);
    }

    //public methods
    //to change background image based on skin type, NORMAL, SELECTED, HOVER
    public void setSkin(int skin_type) {
        switch(skin_type) {
            case NORMAL:
            default:
                _CurrentBackground = _NormalBackground;
                _nState = NORMAL;
                break;

            case SELECTED:
                _CurrentBackground = _SelectedBackground;
                _nState = SELECTED;
                break;

            case HOVER://deos not change state
                _CurrentBackground = _HoverBackground;
                break;
        }
        repaint();
    }

    //instance variables
    private LazyIcon _NormalBackground;//when not selected
    private LazyIcon _SelectedBackground;//when selected
    private LazyIcon _HoverBackground;//when mouse over
    private LazyIcon _CurrentBackground;
    private int _nState = NORMAL;//can only be normal or selected

    //literals
    public static final int NORMAL = 1;
    public static final int SELECTED = 2;
    public static final int HOVER = 3;
}