package org.marketsuite.component.UI;


import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

/**
 * Draws a crosshair over a JComponent. Use as:
 * {@code
 * JXLayer layer = new JXLayer(component);
 * layer.setUI(new CrossHairLayerUI());
 * }
 */
public class CrossHairLayerUI extends AbstractLayerUI {
    private GeneralPath crosshair = new GeneralPath();

    protected void paintLayer(Graphics2D gd, JXLayer jxl) {
        super.paintLayer(gd, jxl);
        gd.setXORMode(Color.WHITE);
        gd.draw(crosshair);
        gd.setPaintMode();
    }

    protected void processMouseMotionEvent(MouseEvent me, JXLayer jxl) {
        super.processMouseMotionEvent(me, jxl);

        if (me.getID() == MouseEvent.MOUSE_MOVED || me.getID() == MouseEvent.MOUSE_DRAGGED) {
            Point point = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), jxl);
            int w = jxl.getWidth();
            int h = jxl.getHeight();

            /*
             * Create crosshair
             */
            crosshair.reset();

            if (point.y <= h) {
                crosshair.moveTo(0, point.y);
                crosshair.lineTo(w, point.y);
            }

            if (point.x <= w) {
                crosshair.moveTo(point.x, 0);
                crosshair.lineTo(point.x, h);
            }

            // mark the ui as dirty and needed to be repainted
            setDirty(true);
        }
    }
}