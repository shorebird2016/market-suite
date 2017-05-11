package org.marketsuite.component.layer;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;

//A layer UI that draws cross hair cursor at mouse location over component below and reacts to mouse events
public class CrosshairLayerUI extends LayerUI<JPanel> {
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
        super.paint(g2, c);

        //draw layer when mouse is inside component
        if (_bActive) {
            //draw horizontal and vertical lines
            g2.setPaint(new Color(224, 175, 24, 159));
            g2.drawLine(0, _nY, c.getWidth(), _nY);
            g2.drawLine(_nX, 0, _nX, c.getHeight());

            // Create a radial gradient, transparent in the middle.
//            java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(_nX, _nY);
//            float radius = 72;
//            float[] dist = {0.0f, 1.0f};
//            Color[] colors = {new Color(0.0f, 0.0f, 0.0f, 0.0f), Color.BLACK};
//            RadialGradientPaint p =
//                    new RadialGradientPaint(center, radius, dist, colors);
//            g2.setPaint(p);
//            g2.setComposite(AlphaComposite.getInstance(
//                    AlphaComposite.SRC_OVER, .6f));
//            g2.fillRect(0, 0, c.getWidth(), c.getHeight());

        }

        g2.dispose();//must remove copied context
    }
    protected void processMouseEvent(MouseEvent e, JLayer l) {
        if (e.getID() == MouseEvent.MOUSE_ENTERED) _bActive = true;
        if (e.getID() == MouseEvent.MOUSE_EXITED) _bActive = false;
        l.repaint();
    }
    protected void processMouseMotionEvent(MouseEvent e, JLayer l) {
        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
        _nX = p.x;
        _nY = p.y;
        l.repaint();
    }

    //----- variables -----
    private boolean _bActive;
    private int _nX, _nY;
}