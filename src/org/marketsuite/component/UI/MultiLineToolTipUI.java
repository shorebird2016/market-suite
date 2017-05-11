package org.marketsuite.component.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolTipUI;

public class MultiLineToolTipUI extends MetalToolTipUI {
   public static final Color background = new Color(0xffffe1);
   static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();

   private String[] strs;

   private int maxWidth = 0;

   public static ComponentUI createUI(JComponent c) {
      //c.setFont(Constants.LINK_FONT_BOLD);
      return sharedInstance;
   }

   public MultiLineToolTipUI() {
      super();
   }

   public void update(Graphics g, JComponent c) {
      if (c.isOpaque()) {
         Dimension size = c.getSize();
         g.setColor(background);
         g.fillRect(0, 0, size.width,size.height);
         g.setColor(Color.gray);
         g.drawRect(0, 0, size.width-1, size.height-1);
      }
      paint(g, c);
   }

   public void paint(Graphics _g, JComponent c) {
      String tipText = ((JToolTip) c).getTipText();
      if(tipText.trim().toLowerCase().startsWith("<html>")) {
         super.paint(_g,c);
         return;
      }
      FontMetrics metrics = _g.getFontMetrics( _g.getFont());
      Graphics2D g = (Graphics2D) _g;

      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setColor(c.getForeground());
      if (strs != null) {
         int y = metrics.getHeight();
         for(String s : strs) {
            g.drawString(s, 3, y);
            y += metrics.getHeight();
         }
      }
   }

   public Dimension getPreferredSize(JComponent c) {
      FontMetrics metrics = c.getFontMetrics(c.getFont());
      String tipText = ((JToolTip) c).getTipText();
      if (tipText == null) {
         tipText = "";
      }
      if(tipText.trim().toLowerCase().startsWith("<html>"))
         return super.getPreferredSize(c);

      strs = getMultiLineString(tipText);

      if(strs == null) return null;

      maxWidth = 0;
      for(String s : strs) {
         int width = SwingUtilities.computeStringWidth(metrics, s);
         maxWidth = (maxWidth < width) ? width : maxWidth;
      }

      int height = metrics.getHeight() * strs.length;
      return new Dimension(maxWidth + 6, height + 4);
   }

   static String[] getMultiLineString(Object value) {
      return getMultiLineString(value,90);
   }

   static String[] getMultiLineString(Object value,int maxlen)
   {
      ArrayList<String> al = new ArrayList<String>();

      String s = value.toString();
      int len = s.length();
      if(len == 0) {
         return null;
      }
      // split tooltip into multiple lines to fit the screen better
      if(len > maxlen) {
         int start = 0;
         int count = 0;
         int i = 0;
         while(i < len) {
            if(s.charAt(i) == ' ' && count >= maxlen) {
               al.add(s.substring(start,start + count + 1).trim());
               start += count + 1;
               count = -1;
            }
            i++;
            count++;
         }
         if(count > 0) {
            al.add(s.substring(start,start + count));
         }
      }
      else {
         al.add(s);
      }
      String[] list = new String[al.size()];
      return al.toArray(list);
   }
}
