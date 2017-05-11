package org.marketsuite.component.dialog;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import org.marketsuite.component.Constants;
import org.marketsuite.component.util.XmtLayout;
import org.marketsuite.component.Constants;
import org.marketsuite.component.util.XmtLayout;

public class ProgressBar extends JPanel {
    public static ProgressBar getInstance(JFrame _frame, String title) {
        if (frameInstance == null)
            frameInstance = new ProgressBar(_frame, title);
        else
            frameInstance.setLabel(title);
        frameInstance.frame = _frame;
        frameInstance.setShape();
        return frameInstance;
    }

    public static ProgressBar getProgressBar() {
        return frameInstance;
    }

    public static ProgressBar getInstance(JDialog _dialog, String title) {
        if (dialogInstance == null)
            dialogInstance = new ProgressBar(_dialog, title);
        else
            dialogInstance.setLabel(title);
        dialogInstance.dlg = _dialog;
        dialogInstance.setShape();
        return dialogInstance;
    }

    // Paint background and rectangle around meter and label
    public void paintComponent(Graphics gg) {
        if (!isValid())
            return;

        Dimension size = getSize();
        Graphics2D g = (Graphics2D) gg.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // semi transparent panel background
        g.setColor(bgColor);
        g.fillRect(0, 0, size.width, size.height);
        // rectangle around progress meter
        g.translate((size.width - meterWidth) / 2,
            (size.height - meterHeight) / 2);
        g.setPaint(bgPaint);
        g.fill(shape);
        g.setStroke(stroke);
        g.setPaint(Color.black);
        g.draw(shape);
        g.dispose();
    }

    public void setVisible(boolean b) {
        if (b)
            showWindow();
        else
            hideWindow();
    }

    public void show() {
        if (frame != null) {
            oldGlassPane = frame.getGlassPane();
            frame.setGlassPane(glassPane);
            frame.setEnabled(false);
        }
        else if (dlg != null) {
            oldGlassPane = dlg.getGlassPane();
            dlg.setGlassPane(glassPane);
        }
        glassPane.setVisible(true);
        glassPane.revalidate();
        pmi.startAnim();
    }

    public void hide() {
        pmi.stopAnim();
        if (frame != null && oldGlassPane != null) {
            frame.setGlassPane(oldGlassPane);
            frame.setEnabled(true);
        }
        else if (dlg != null && oldGlassPane != null)
            dlg.setGlassPane(oldGlassPane);
        glassPane.setVisible(false);
        if (oldGlassPane != null)
            oldGlassPane.setVisible(false);
    }

    public void dispose() {
        hide();
    }

    public void showWindow() {
        show();
        requestFocus();
        setFocusCycleRoot(true);
        if (frame != null)
            frame.getContentPane().setFocusCycleRoot(false);
        else if (dlg != null)
            dlg.getContentPane().setFocusCycleRoot(false);
    }

    public void hideWindow() {
        hide();
    }

    private ProgressBar(JFrame _frame, String title) {
        this(60, 50);
        frame = _frame;
        if (frame != null)
            oldGlassPane = frame.getGlassPane();
        setLabel(title);
        glassPane = new JPanel();
        glassPane.setOpaque(false);
        glassPane.setLayout(new BorderLayout());
        glassPane.add(this, BorderLayout.CENTER);
        MouseInputAdapter mia = new MouseInputAdapter() {
            //
        };
        glassPane.addMouseListener(mia);
        glassPane.addMouseMotionListener(mia);
    }

    public ProgressBar(JDialog _dlg, String title) {
        this(60, 50);
        dlg = _dlg;
        if (dlg != null)
            oldGlassPane = dlg.getGlassPane();
        setLabel(title);
        glassPane = new JPanel();
        glassPane.setOpaque(false);
        glassPane.setLayout(new BorderLayout());
        glassPane.add(this, BorderLayout.CENTER);
        MouseInputAdapter mia = new MouseInputAdapter() { //
        };
        glassPane.addMouseListener(mia);
        glassPane.addMouseMotionListener(mia);
    }

    public ProgressBar(int s, int _delay) {
        pmi = new ProgBarImpl(s, _delay);
        lab = new JLabel();
        lab.setFont(Constants.VERDANA_PLAIN_15);
        lab.setForeground(Color.white);
        setLayout(new XmtLayout("col lreven margin 0 0 padx 0 pady 0 space 0 +0"));
        add("", new JLabel());
        add("centered fixed", pmi);
        add("centered fixed", lab);
        add("", new JLabel());
        setOpaque(false);
    }

    private void setShape() {
        final int radius = 25;
        final int off = 0;
        final int off2 = 0;

        // rectangle around progress meter
        Dimension size = lab.getPreferredSize();
        int tempWidth = size.width + 75;
        if (Math.abs(tempWidth - meterWidth) > 10)
            meterWidth = tempWidth;
        GeneralPath gp = new GeneralPath();
        int x = 0;
        int y = 0;
        gp.moveTo(x + radius, y);
        x = meterWidth;
        gp.quadTo(meterWidth / 2.0, off2, x - radius, y);
        gp.quadTo(x + off, y - off, x, y + radius);
        y += meterHeight;
        gp.quadTo(x, meterHeight / 2.0, x, y - radius);
        gp.quadTo(x + off, y + off, x - radius, y);
        x = 0;
        gp.quadTo(meterWidth / 2.0, y - off2, x + radius, y);
        gp.quadTo(x - off, y + off, x, y - radius);
        y = 0;
        gp.quadTo(x, meterHeight / 2.0, x, y + radius);
        gp.quadTo(x - off, y - off, x + radius, y);
        gp.closePath();
        shape = gp;
        revalidate();
        repaint();
    }

    public void setFrame(JFrame _frame) {
        frame = _frame;
    }

    public void setLabel(String t) {
        lab.setText(t);
        setShape();
    }

    public void startAnim() {
        pmi.startAnim();
    }

    public void stopAnim() {
        pmi.stopAnim();
    }

    private static class ProgBarImpl extends JPanel {
        public void paintComponent(Graphics gg) {
            if (!isValid()) return;
            //super.paintComponent(gg);
            Graphics2D g = (Graphics2D) gg;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(stroke);
            AffineTransform old = g.getTransform();
            synchronized (colorBuffer) {
                for (int i = 0; i < points; i++) {
                    g.setColor(colorBuffer[i]);
                    g.rotate(i * inc, cx, cy);
                    g.translate(offset + cx, cy);
                    g.drawLine(0, 0, lineLen, 0);
                    g.setTransform(old);
                }
            }
        }

        private ProgBarImpl(int s, int _delay) {
            delay = _delay;
            psize.width = s + 8;
            psize.height = s + 8;
            setPreferredSize(psize);
            setMaximumSize(psize);
            lineLen = s / 4 - 3;
            offset = s / 4;
            setBackground(new Color(100, 170, 180));
            setOpaque(false);
            cx = psize.width / 2;
            cy = psize.height / 2;
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent ev) {
                    inc = -inc;
                }
            });
            for (int i = 0; i < points; i++)
                colorBuffer[i] = darkColor;

            colorBuffer[0] = color4;
            colorBuffer[1] = color3;
            colorBuffer[2] = color2;
            colorBuffer[3] = color1;
            colorBuffer[4] = color0;
        }

        private void stopAnim() {
            running = false;
            try {
                animThread.join();
            } catch (Exception ex) {
                //
            }
        }

        private void startAnim() {
            if (running) return;
            running = true;
            animThread = new Thread("Progress Meter") {
                public void run() {
                    try {
                        while (running) {
                            synchronized (colorBuffer) {
                                Color temp = colorBuffer[points - 1];
                                System.arraycopy(colorBuffer, 0, colorBuffer, 1, points - 1);
                                colorBuffer[0] = temp;
                            }
                            ProgBarImpl.this.repaint();
                            Thread.sleep(delay);
                        }
                    } catch (Exception ex) {
                        System.out.println("ex= " + ex);
                    }
                }
            };
            animThread.start();
        }

        private transient Stroke stroke = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        private static final int points = 16;
        private Color color0 = Color.red;
        private Color color1 = new Color(180, 50, 40);
        private Color color2 = new Color(150, 50, 40);
        private Color color3 = new Color(100, 50, 40);
        private Color color4 = new Color(80, 50, 40);
        private Color darkColor = new Color(50, 40, 40);
        private Color[] colorBuffer = new Color[points];
        private Dimension psize = new Dimension();
        private int lineLen = 0;
        private int offset = 0;
        private int cx, cy;
        private double inc = 2 * Math.PI / points;
        private transient Thread animThread;
        private boolean running = false;
        private int delay;
    }

    //instance variables
    private ProgBarImpl pmi;
    private JLabel lab;
    private JFrame frame;
    private JDialog dlg;
    private JPanel glassPane;
    private Component oldGlassPane;
    private Shape shape;
    private Color bgColor = new Color(0, 0, 0, 128);
    private static final float[] gradientFractions = new float[]{0f, .25f, .75f, 1f};
    private static final int trans = 208;
    private static final Color[] gradientColors = new Color[]{new Color(190, 210, 190, trans),
        new Color(215, 240, 215, trans),
        new Color(80, 90, 80, trans),
        new Color(30, 40, 30, trans),
    };
    private int meterHeight = 100;
    private int meterWidth;
    private transient LinearGradientPaint bgPaint = new LinearGradientPaint(0, 0, 0, meterHeight, gradientFractions, gradientColors);
    private static final Stroke stroke = new BasicStroke(2);
    private static ProgressBar frameInstance = null;
    private static ProgressBar dialogInstance = null;
}