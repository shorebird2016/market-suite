package org.marketsuite.component.label;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JLabel;

/**
 * This class implements a multiple-line text label.
 * It displays text on one or more lines, wrapping text
 * as needed to fit in the available horizontal space.
 */
public class WrappingLabel extends JLabel {
	/**
     * Constructs a default wrapping label. Default values are an empty text string
     * and left alignment.
     */
    public WrappingLabel() {
        super();
    }

    /**
     * Constructs a wrapping label that displays the specified string.
     * The label will default to left alignment.
     *
     * @param s string to be displayed in label
     */
    public WrappingLabel(String s) {
        super(s);
    }

    /**
     * Constructs wrapping label with the specified text and alignment.
     *
     * @param s the string to be displayed in label
     * @param a the alignment, one of LEFT, CENTER, or RIGHT
     */
    public WrappingLabel(String s, int a) {
        super(s, a);
    }

    /**
     * Moves and/or resizes this component.
     * This is a standard Java AWT method which gets called to move and/or
     * resize this component. Components that are in containers with layout
     * managers should not call this method, but rely on the layout manager
     * instead.
     * <p/>
     * It is overridden here to re-wrap the text as necessary.
     *
     * @param x      horizontal position in the parent's coordinate space
     * @param y      vertical position in the parent's coordinate space
     * @param width  the new width
     * @param height the new height
     */
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        invalidate();
        validate();
        repaint();
    }

    /**
     * Paints this component using the given graphics context.
     * This is a standard Java AWT method which typically gets called
     * by the AWT to handle painting this component. It paints this component
     * using the given graphics context. The graphics context clipping region
     * is set to the bounding rectangle of this component and its [0,0]
     * coordinate is this component's top-left corner.
     *
     * @param g the graphics context used for painting
     */
    public void paintComponent(Graphics g) {
        String text = getText();
        if (text != null) {
            int x, y;
            int boundx, boundy;
            Dimension d;
            int fromIndex = 0;
            int pos = 0;
            int bestpos;
            String largestString;
            String s;

            // Set up some class variables
            fm = getFontMetrics(getFont());
            baseline = fm.getMaxAscent();

            // Get the maximum height and width of the current control
            d = getSize();
            boundx = d.width;
            boundy = d.height;

            // X and Y represent the coordinates of the upper left portion
            // of the next text line.
            x = 0;
            y = 0;

            // While we haven't passed the bottom of the label and we
            // haven't run past the end of the string...
            while ((y + fm.getHeight()) <= boundy && fromIndex != -1) {
                // Automatically skip any spaces at the beginning of the line
                while (fromIndex < text.length() && text.charAt(fromIndex) == ' ') {
                    ++fromIndex;
                    // If we hit the end of line while skipping spaces, we're done.
                    if (fromIndex >= text.length()) break;
                }

                // fromIndex represents the beginning of the line
                pos = fromIndex;
                bestpos = -1;
                largestString = null;

                while (pos >= fromIndex) {
                    pos = text.indexOf(' ', pos);

                    // Couldn't find another space?
                    if (pos == -1) {
                        s = text.substring(fromIndex);
                    } else {
                        s = text.substring(fromIndex, pos);
                    }

                    // If the string fits, keep track of it.
                    if (fm.stringWidth(s) < boundx) {
                        largestString = s;
                        bestpos = pos;

                        // If we've hit the end of the string, use it.
                        if (pos == -1) break;
                    } else {
                        break;
                    }

                    ++pos;
                }

                if (largestString == null) {
                    // Couldn't wrap at a space, so find the largest line
                    // that fits and print that.  Note that this will be
                    // slightly off -- the width of a string will not necessarily
                    // be the sum of the width of its characters, due to kerning.
                    int totalWidth = 0;
                    int oneCharWidth = 0;

                    pos = fromIndex;

                    while (pos < text.length()) {
                        oneCharWidth = fm.charWidth(text.charAt(pos));
                        if ((totalWidth + oneCharWidth) >= boundx) break;
                        totalWidth += oneCharWidth;
                        ++pos;
                    }

                    drawAlignedString(g, text.substring(fromIndex, pos), x, y, boundx);
                    fromIndex = pos;
                } else {
                    drawAlignedString(g, largestString, x, y, boundx);

                    fromIndex = bestpos;
                }

                y += fm.getHeight();
            }

            // We're done with the font metrics...
            fm = null;
        }
    }

    /**
     * This helper method draws a string aligned the requested way.
     *
     * @param g     the graphics context used for painting
     * @param s     the string to draw
     * @param x     the point to start drawing from, x coordinate
     * @param y     the point to start drawing from, y coordinate
     * @param width the width of the area to draw in, in pixels
     */
    protected void drawAlignedString(Graphics g, String s, int x, int y, int width) {
        int drawx;
        int drawy;
        int align = getHorizontalAlignment();

        drawx = x;
        drawy = y + baseline;

        if (align != LEFT) {
            int sw;

            sw = fm.stringWidth(s);

            if (align == CENTER) {
                drawx += (width - sw) / 2;
            } else if (align == RIGHT) {
                drawx = drawx + width - sw;
            }
        }

        g.drawString(s, drawx, drawy);
    }

    /**
     * The maximum ascent of the font used to display text.
     */
    protected int baseline;

    /**
     * The metrics of the font used to display text.
     */
    transient protected FontMetrics fm;
}
