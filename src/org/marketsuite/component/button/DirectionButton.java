package org.marketsuite.component.button;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;

/**
 * The DirectionButton is a button component that has an arrow drawn in it that
 * points one of four ways (left, up, right, or down). At runtime, the button has
 * a raised look and a pressed look.
 * <p/>
 * This component is usually used in conjunction with a combo or list box to
 * indicate a list that the user can view by clicking the arrow, or with spinners
 * to let the user scroll through available values.
 * <p/>
 * This is a lightweight component version of Symantec's.
 *
 * @author Michael Martak
 * @version 1.0 04/14/98
 */
public class DirectionButton extends JButton implements Serializable {
	/**
     * The point LEFT style constant.
     */
    public static final int LEFT = 0;

    /**
     * The point RIGHT style constant.
     */
    public static final int RIGHT = 1;

    /**
     * The point UP style constant.
     */
    public static final int UP = 2;

    /**
     * The point DOWN style constant.
     */
    public static final int DOWN = 3;

    /**
     * Constructs a default DirectionButton, which will point left.
     */
    public DirectionButton() {
        this(LEFT);
    }

    /**
     * Constructs a DirectionButton pointing the specified direction.
     *
     * @param d a style constant indicating which direction to point the button
     * @see #LEFT
     * @see #UP
     * @see #RIGHT
     * @see #DOWN
     */
    public DirectionButton(int d) {
        direction = d;
        left = 0;
        right = 0;
        bottom = 0;
        indent = 3;
        setArrowColor(UIManager.getColor("Button.foreground"));
        setIcon(new DirectionButton.ButtonIcon());
        setPressedIcon(new DirectionButton.ButtonIcon(true));
    }

    /**
     * Sets the direction of the arrow after construction.
     *
     * @param d constant indicating direction to point button
     * @see #getDirection
     * @see #LEFT
     * @see #UP
     * @see #RIGHT
     * @see #DOWN
     */
    public void setDirection(int d) {
        if (direction != d) {
            Integer oldValue = Integer.valueOf(direction);
            Integer newValue = Integer.valueOf(d);

            direction = d;
            repaint();

            firePropertyChange("direction", oldValue, newValue);
        }
    }

    /**
     * Returns the direction the button is currently pointing.
     *
     * @see #setDirection
     * @see #LEFT
     * @see #UP
     * @see #RIGHT
     * @see #DOWN
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Sets the amount of blank space between the arrow and the button
     * border in pixels.
     *
     * @param ai the margin around the arrow in pixels. 0=arrow takes up entire button
     * @see #getArrowIndent
     */
    public void setArrowIndent(int ai) {
        if (indent != ai) {
            Integer oldValue = Integer.valueOf(indent);
            Integer newValue = Integer.valueOf(ai);

            indent = ai;
            //Make sure that changes to indent don't make changes to shrinkTriangle
            //give us a bad triangle.
            shrinkTriangle(left, right, top, bottom);
            repaint();

            firePropertyChange("arrowIndent", oldValue, newValue);
        }
    }

    /**
     * Sets the color of the direction arrow.
     *
     * @param newValue the new arrow color.
     * @see #getArrowColor
     */
    public void setArrowColor(Color newValue) {
        if (arrowColor == null || !arrowColor.equals(newValue)) {
            Color oldValue = arrowColor;

            arrowColor = newValue;
            try {
                disabledArrowColor =
                    UIManager.getColor("TextField.inactiveForeground");
            }
            catch (IllegalArgumentException exc) {
            	exc.printStackTrace();
            }

            repaint();

            firePropertyChange("arrowColor", oldValue, newValue);
        }
    }

    /**
     * Gets the current color of the direction arrow.
     *
     * @return the current arrow color
     * @see #setArrowColor
     */
    public Color getArrowColor() {
        return arrowColor;
    }

    /**
     * Returns the amount of blank space between the arrow and the button
     * border in pixels.
     *
     * @see #setArrowIndent
     */
    public int getArrowIndent() {
        System.out.println("indent: " + indent);
        return indent;
    }

    /**
     * Sets the extra amount, in pixels, to shrink the arrow triangle.
     * Constrains the values such that the arrow will never be less than
     * three pixels.  If a value is entered that would exceed this limit,
     * the limit will be used instead.
     */
    public void shrinkTriangle(int l, int r, int t, int b) {
        System.out.println("shrink called...");
        Dimension s = getSize();
        int maxWidth = s.width - 2;
        int maxHeight = s.height - 2;

        if (maxWidth - (l + r + indent + indent) >= 3) {
            left = l;
            right = r;
        } else {
            left = (maxWidth - indent - indent - 3) / 2;
            right = left;
        }

        if (maxHeight - (t + b + indent + indent) >= 3) {
            top = t;
            bottom = b;
        } else {
            top = (maxHeight - indent - indent - 3) / 2;
            bottom = top;
        }
    }

    /**
     * Returns the recommended dimensions to properly display this component.
     * This is a standard Java AWT method which gets called to determine
     * the recommended size of this component.
     *
     * @return a button that has a content area of 7 by 7 pixels.
     * @see java.awt.Component#getMinimumSize
     */

    public Dimension getPreferredSize() {

        Dimension defaultSize = super.getPreferredSize();
        return new Dimension(defaultSize.width + 7, defaultSize.height + 7);
    }

    /**
     * Returns the minimum dimensions to properly display this component.
     * This is a standard Java AWT method which gets called to determine
     * the minimum size of this component.
     *
     * @return a button that has a content area of 3 by 3 pixels.
     * @see java.awt.Component#getMinimumSize
     */
    public Dimension getMinimumSize() {
        Dimension defaultSize = super.getPreferredSize();
        return new Dimension(defaultSize.width + 3, defaultSize.height + 3);
    }

    class ButtonIcon implements Icon {
        public ButtonIcon() {
            this(false);
        }

        public ButtonIcon(boolean pressed) {
            this.pressed = pressed;
        }

        public int getIconHeight() {
            return getSize().height;
        }

        public int getIconWidth() {
            return getSize().width;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            updateIcon(c, g, x, y, pressed);
        }

        private boolean pressed;
    }

    protected void updateIcon(Component c, Graphics g, int x, int y,
                              boolean pressed) {
        Dimension s = c.getSize();
        int centerHorizontal;
        int centerVertical;
        int topSide;
        int bottomSide;
        int leftSide;
        int rightSide;
        int pressedAdjustment = pressed ? indent : 0;

        if (isEnabled()) {
            g.setColor(arrowColor);
        } else {
            g.setColor(disabledArrowColor);
        }

        centerHorizontal = ((s.width - 1) / 2) + pressedAdjustment;
        centerVertical = ((s.height - 1) / 2) + pressedAdjustment;
        topSide = (top - 1) + indent + pressedAdjustment;
        bottomSide = (s.height - 1 - bottom) - indent + pressedAdjustment;
        leftSide = (left - 1) + indent + pressedAdjustment;
        rightSide = (s.width - 1 - right) - indent + pressedAdjustment;

        switch (direction) {
            case UP: {
                fillTriangle(g, centerHorizontal, topSide, leftSide, bottomSide, rightSide, bottomSide, direction);
                break;
            }
            case DOWN: {
                fillTriangle(g, centerHorizontal, bottomSide, leftSide, topSide, rightSide, topSide, direction);
                break;
            }
            case LEFT: {
                fillTriangle(g, leftSide, centerVertical, rightSide, bottomSide, rightSide, topSide, direction);
                break;
            }
            case RIGHT: {
                fillTriangle(g, rightSide, centerVertical, leftSide, bottomSide, leftSide, topSide, direction);
                break;
            }
        }
    }

    /**
     * Fills a triangle which has at least one side that is straight up and down or left and right.
     *
     * @param g         the Graphics to use to draw with.
     * @param tipX      the horizontal coordinate of the point opposite a straight side.
     * @param tipY      the vertical coordinate of the point opposite a straight side.
     * @param aX        the horizontal coordinate of one of the two points defining the straight side.
     * @param aY        the vertical coordinate of one of the two points defining the straight side.
     * @param bX        the horizontal coordinate of one of the two points defining the straight side.
     * @param bY        the vertical coordinate of one of the two points defining the straight side.
     * @param direction1 the direction of the straight line UP, DOWN, or LEFT, RIGHT.
     *                  <p/>
     *                  aX and bX should be the same for UP or Down.  aY and bY should be the same for LEFT or RIGHT.
     *                  If not, then the a coordinates are used.
     * @see #UP
     * @see #DOWN
     * @see #LEFT
     * @see #RIGHT
     */
    protected void fillTriangle(Graphics g, int tipX, int tipY, int aX, int aY, int bX, int bY, int direction1) {
        int max, min;

        switch (direction1) {
            case UP:
            case DOWN:
                max = Math.max(aX, bX);
                min = Math.min(aX, bX);
                for (int i = min; i <= max; ++i) {
                    g.drawLine(tipX, tipY, i, aY);
                }
                break;
            case RIGHT:
            case LEFT:
                max = Math.max(aY, bY);
                min = Math.min(aY, bY);
                for (int i = min; i <= max; ++i) {
                    g.drawLine(tipX, tipY, aX, i);
                }
                break;
        }
    }

    /**
     * Is the given bevel size valid for this button.
     *
     * @param i the given bevel size
     * @return true if the given bevel size is acceptable, false if not.
     */
    protected boolean isValidBevelSize(int i) {
        Dimension s = getSize();

        int temp = i * 2 + 4;

        if (i < 0 || s.width < temp || s.height < temp)
            return false;
		return true;
    }

    /**
     * Is the given direction valid for this button.
     *
     * @param i the given bevel size
     * @return true if the given direction is acceptable, false if not.
     */
    protected boolean isValidDirection(int i) {
        switch (i) {
            case LEFT:
            case RIGHT:
            case UP:
            case DOWN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Is the given arrow indent is valid for this button.
     *
     * @param i the given bevel size
     * @return true if the given indent size is acceptable, false if not.
     */
    protected boolean isValidArrowIndent(int i) {
        Dimension s = getSize();

        int temp = (i * 2) + 6;

        if (i < 0 || s.width < temp || s.height < temp)
            return false;
		return true;
    }

    /**
     * The color of the arrow in the button.
     */
    protected Color arrowColor = null;
    /**
     * The color of the arrow when the button is disabled.
     */
    protected Color disabledArrowColor = null;
    /**
     * The direction the arrow points.
     * One of: LEFT, UP, RIGHT, or DOWN.
     *
     * @see #LEFT
     * @see #UP
     * @see #RIGHT
     * @see #DOWN
     */
    protected int direction;
    /**
     * The number of pixels to shrink the arrow from the left side of the button.
     */
    protected int left;
    /**
     * The number of pixels to shrink the arrow from the right side of the button.
     */
    protected int right;
    /**
     * The number of pixels to shrink the arrow from the top side of the button.
     */
    protected int top;
    /**
     * The number of pixels to shrink the arrow from the bottom side of the button.
     */
    protected int bottom;
    /**
     * The margin around the arrow in pixels. 0 = arrow takes up entire button.
     */
    protected int indent;
}
