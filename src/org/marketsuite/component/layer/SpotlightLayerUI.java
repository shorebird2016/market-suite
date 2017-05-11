package org.marketsuite.component.layer;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;

//A layer UI that shows a white spot light over component below and reacts to mouse events
public class SpotlightLayerUI  extends LayerUI<JPanel> {
    //----- interface/overrides -----
    public void installUI(JComponent c) {
        super.installUI(c);
        JLayer jlayer = (JLayer) c;
        jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
    public void uninstallUI(JComponent c) {
        JLayer jlayer = (JLayer) c;
        jlayer.setLayerEventMask(0);
        super.uninstallUI(c);
    }
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Paint the view.
        super.paint(g2, c);

        if (mActive) {
            // Create a radial gradient, transparent in the middle.
            java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(mX, mY);
            float radius = 72;
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {new Color(0.0f, 0.0f, 0.0f, 0.0f), Color.BLACK};
            RadialGradientPaint p =
                    new RadialGradientPaint(center, radius, dist, colors);
            g2.setPaint(p);
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, .6f));
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        }

        g2.dispose();
    }
    protected void processMouseEvent(MouseEvent e, JLayer l) {
        if (e.getID() == MouseEvent.MOUSE_ENTERED) mActive = true;
        if (e.getID() == MouseEvent.MOUSE_EXITED) mActive = false;
        l.repaint();
    }
    protected void processMouseMotionEvent(MouseEvent e, JLayer l) {
        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
        mX = p.x;
        mY = p.y;
        l.repaint();
    }

    //----- variables -----
    private boolean mActive;
    private int mX, mY;
}