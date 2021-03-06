package org.marketsuite.component.UI;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Shows a magnification glass on top of a component.
 */
public class MagnifierUI extends AbstractLayerUI {

    private int radius = 50;//150

    private double magnifyingFactor = 1.5;//from original size

    private Point2D point = new Point2D.Double();

    /**
     * Returns the magnifying scale factor.
     *
     * @return The magnifying scale factor.
     */
    public double getMagnifyingFactor() {
        return this.magnifyingFactor;
    }

    /**
     * Sets the magnifying scale factor.
     *
     * @param factor  the new scale factor.
     */
    public void setMagnifyingFactor(double factor) {
        this.magnifyingFactor = factor;
    }

    /**
     * Returns the radius of the magnifying glass, in Java2D units.
     *
     * @return The radius.
     */
    public int getRadius() {
        return this.radius;
    }

    /**
     * Sets the radius of the magnifying glass.
     *
     * @param radius  the new radius (in Java2D units).
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Returns the current location of the magnifying glass.
     *
     * @return The current location.
     */
    public Point2D getPoint() {
        return this.point;
    }

    /**
     * Sets the location of the magnifying glass.
     *
     * @param point  the new location.
     */
    public void setPoint(Point2D point) {
        this.point.setLocation(point);
    }

    /**
     * Paints the magnifying glass.
     *
     * @param g2  the graphics device.
     * @param layer  the layer.
     */
    protected void paintLayer(Graphics2D g2, JXLayer layer) {
        super.paintLayer(g2, layer);
        Point2D point = getPoint();
        double scale = getMagnifyingFactor();
        double baseRadius = getRadius();
        double scaledRadius = (baseRadius / scale);
        double strokeAdjust = 0.5;
        double drawSize = 2 * (baseRadius + strokeAdjust);
        double clipSize = 2 * scaledRadius;
        Ellipse2D drawGlass = new Ellipse2D.Double(-strokeAdjust,
                -strokeAdjust, drawSize, drawSize);
        Ellipse2D clipGlass = new Ellipse2D.Double(0, 0, clipSize, clipSize);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.translate(point.getX() - baseRadius, point.getY() - baseRadius);
                Color oldColor = g2.getColor();
        g2.setPaint(createPaint(drawGlass, false));
        g2.fill(drawGlass);
        g2.setColor(oldColor);
        g2.draw(drawGlass);
        AffineTransform oldTransform = g2.getTransform();
        Shape oldClip = g2.getClip();
        g2.scale(scale, scale);
        g2.clip(clipGlass);
        g2.translate(scaledRadius - point.getX(), scaledRadius - point.getY());
        layer.paint(g2);
        g2.setTransform(oldTransform);
        g2.setClip(oldClip);
        g2.setPaint(createPaint(drawGlass, true));
        g2.fill(drawGlass);
    }

    /**
     * Updates the glass location in response to mouse events.
     *
     * @param e  the event.
     * @param layer  the layer.
     */
    protected void processMouseEvent(MouseEvent e, JXLayer layer) {
        super.processMouseEvent(e, layer);
        setPoint(SwingUtilities.convertPoint(e.getComponent(),
                e.getPoint(), layer));
        setDirty(true);
    }

    /**
     * Updates the glass location in response to mouse events.
     *
     * @param e  the event.
     * @param layer  the layer.
     */
    protected void processMouseMotionEvent(MouseEvent e,
        JXLayer layer) {
        super.processMouseMotionEvent(e, layer);
        setPoint(SwingUtilities.convertPoint(e.getComponent(),
                e.getPoint(), layer));
        setDirty(true);
    }

    /**
     * A utility method to create the paint for the glass.
     *
     * @param glass  the glass shape.
     * @param transparent  transparent?
     *
     * @return The paint.
     */
    private Paint createPaint(Ellipse2D glass, boolean transparent) {
        Point2D center = new Point2D.Double(glass.getCenterX(),
                glass.getCenterY());
        float radius = (float) (glass.getCenterX() - glass.getX());
        Point2D focus = new Point2D.Double(center.getX() - 0.5 * radius,
                center.getY() - 0.5 * radius);
        Color[] colors = new Color[] {
                transparent ? new Color(255, 255, 255, 128) : Color.WHITE,
                transparent ? new Color(0, 255, 255, 32) : Color.CYAN };
        float[] fractions = new float[] { 0f, 1f };
        RadialGradientPaint paint = new RadialGradientPaint(center, radius,
                focus, fractions, colors,
                MultipleGradientPaint.CycleMethod.NO_CYCLE);
        return paint;
    }

}

