package org.marketsuite.component.dialog;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.marketsuite.component.Constants;
import org.marketsuite.component.Constants;

public class ProgressMeter extends JPanel {

	private static class ProgMeterImpl extends JPanel {

	transient Stroke stroke = new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      static final int points = 16;
      Color color0 = Color.red;
      Color color1 = new Color(180, 50, 40);
      Color color2 = new Color(150, 50, 40);
      Color color3 = new Color(100, 50, 40);
      Color color4 = new Color(80, 50, 40);
      Color darkColor = new Color(50, 40, 40);
      Color[] colorBuffer = new Color[points];
      Dimension psize = new Dimension();
      int lineLen = 0;
      int offset = 0;
      int cx, cy;
      double inc = 2 * Math.PI / points;
      transient Thread t;
      boolean running = false;
      int delay;

      public ProgMeterImpl(int s, int _delay) {
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
      }

      public void stopAnim() {
         running = false;
      }

      public void startAnim() {
         if (running) return;
         running = true;
         for (int i = 0; i < points; i++)
            colorBuffer[i] = darkColor;
         colorBuffer[0] = color4;
         colorBuffer[1] = color3;
         colorBuffer[2] = color2;
         colorBuffer[3] = color1;
         colorBuffer[4] = color0;
         t = new Thread("Progress Meter") {
            public void run() {
               try {
                  while (running) {
                     ProgMeterImpl.this.repaint();
                     Thread.sleep(delay);
                  }
               } catch (Exception ex) {
                  System.out.println("ex= " + ex);
               }
            }
         };
         t.start();
      }

      public void paintComponent(Graphics gg) {
         // super.paintComponent(gg);
         Graphics2D g = (Graphics2D) gg;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

         g.setStroke(stroke);
         AffineTransform old = g.getTransform();

         for (int i = 0; i < points; i++) {
            g.setColor(colorBuffer[i]);
            g.rotate(i * inc, cx, cy);
            g.translate(offset + cx, cy);
            g.drawLine(0, 0, lineLen, 0);
            g.setTransform(old);
         }

         Color temp = colorBuffer[points - 1];
         System.arraycopy(colorBuffer, 0, colorBuffer, 1, points - 1);
         colorBuffer[0] = temp;
      }
   }

   ProgMeterImpl pmi;
   JLabel lab;
   JFrame frame;
   JPanel glassPane;

   private ProgressMeter(JFrame _frame, String title) {
      this(60, 50);
      frame = _frame;
      setLabel(title);
      glassPane = (JPanel)frame.getGlassPane();
      glassPane.setLayout(new BorderLayout());
      glassPane.add(this,BorderLayout.CENTER);
      glassPane.addMouseListener(new MouseAdapter() {
    	  //
      });
      glassPane.addMouseMotionListener(new MouseMotionAdapter() {
    	  //
      });
   }

   public ProgressMeter(int s, int _delay) {
      pmi = new ProgMeterImpl(s, _delay);
      lab = new JLabel();
      lab.setFont(Constants.VERDANA_PLAIN_15);
      lab.setForeground(Color.white);

      pmi.setAlignmentX(0.5f);
      pmi.setAlignmentY(0.5f);
      lab.setAlignmentX(0.5f);
      lab.setAlignmentY(0.5f);

      setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
      add(Box.createGlue());
      add(pmi);
      add(lab);
      add(Box.createGlue());
      setOpaque(false);
   }

   public void setLabel(String t) {
      lab.setText(t);
   }
/*

   public void show() {
      glassPane.setVisible(true);
      pmi.startAnim();
   }

   public void hide() {
      pmi.stopAnim();
      glassPane.setVisible(false);
   }
*/

   public void startAnim() {
      pmi.startAnim();
   }

   public void stopAnim() {
      pmi.stopAnim();
   }

   static private ProgressMeter _instance = null;

   static public ProgressMeter getInstance(JFrame _frame, String title) {
      _instance = new ProgressMeter(_frame, title);
      return _instance;
   }
}
