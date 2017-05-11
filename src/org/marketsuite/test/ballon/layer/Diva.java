package org.marketsuite.test.ballon.layer;

import org.marketsuite.component.layer.SpotlightLayerUI;

import javax.swing.*;
import javax.swing.plaf.LayerUI;

public class Diva {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createUI();
            }
        });
    }

    public static void createUI() {
        JFrame f = new JFrame("Diva");

        LayerUI<JPanel> layerUI = new SpotlightLayerUI();
        JPanel panel = createPanel();
        JLayer<JPanel> jlayer = new JLayer<JPanel>(panel, layerUI);

        f.add(jlayer);

        f.setSize(300, 200);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static JPanel createPanel() {
        JPanel p = new JPanel();

        ButtonGroup entreeGroup = new ButtonGroup();
        JRadioButton radioButton;
        p.add(radioButton = new JRadioButton("Beef", true));
        entreeGroup.add(radioButton);
        p.add(radioButton = new JRadioButton("Chicken"));
        entreeGroup.add(radioButton);
        p.add(radioButton = new JRadioButton("Vegetable"));
        entreeGroup.add(radioButton);

        p.add(new JCheckBox("Ketchup"));
        p.add(new JCheckBox("Mustard"));
        p.add(new JCheckBox("Pickles"));

        p.add(new JLabel("Special requests:"));
        p.add(new JTextField(20));

        JButton orderButton = new JButton("Place Order");
        p.add(orderButton);

        return p;
    }
}

//class SpotlightLayerUI extends LayerUI<JPanel> {
//    public void installUI(JComponent c) {
//        super.installUI(c);
//        JLayer jlayer = (JLayer) c;
//        jlayer.setLayerEventMask(
//                AWTEvent.MOUSE_EVENT_MASK |
//                        AWTEvent.MOUSE_MOTION_EVENT_MASK
//        );
//    }
//
//    public void uninstallUI(JComponent c) {
//        JLayer jlayer = (JLayer) c;
//        jlayer.setLayerEventMask(0);
//        super.uninstallUI(c);
//    }
//
//    public void paint(Graphics g, JComponent c) {
//        Graphics2D g2 = (Graphics2D) g.create();
//
//        // Paint the view.
//        super.paint(g2, c);
//
//        if (mActive) {
//            // Create a radial gradient, transparent in the middle.
//            java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(mX, mY);
//            float radius = 72;
//            float[] dist = {0.0f, 1.0f};
//            Color[] colors = {new Color(0.0f, 0.0f, 0.0f, 0.0f), Color.BLACK};
//            RadialGradientPaint p =
//                    new RadialGradientPaint(center, radius, dist, colors);
//            g2.setPaint(p);
//            g2.setComposite(AlphaComposite.getInstance(
//                    AlphaComposite.SRC_OVER, .6f));
//            g2.fillRect(0, 0, c.getWidth(), c.getHeight());
//        }
//
//        g2.dispose();
//    }
//
//    protected void processMouseEvent(MouseEvent e, JLayer l) {
//        if (e.getID() == MouseEvent.MOUSE_ENTERED) mActive = true;
//        if (e.getID() == MouseEvent.MOUSE_EXITED) mActive = false;
//        l.repaint();
//    }
//
//    protected void processMouseMotionEvent(MouseEvent e, JLayer l) {
//        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), l);
//        mX = p.x;
//        mY = p.y;
//        l.repaint();
//    }
//
//    private boolean mActive;
//    private int mX, mY;
//}
