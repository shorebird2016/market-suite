package org.marketsuite.component.UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.util.Calendar;

import javax.swing.JComponent;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.plaf.basic.BasicMonthViewUI;
import org.jdesktop.swingx.plaf.basic.CalendarState;

public class CapMonthViewUI extends BasicMonthViewUI {

   // Border border = new EtchedBorder(EtchedBorder.LOWERED);
   Border border = new SoftBevelBorder(BevelBorder.LOWERED);

   public static ComponentUI createUI(JComponent c) {
      return new CapMonthViewUI();
   }

   protected void paintDayOfMonth(Graphics _g,
                                  Rectangle bounds,
                                  Calendar calendar,
                                  CalendarState state) {
      Graphics2D g = (Graphics2D) _g;
      if (state == CalendarState.TITLE) {
         // Painting this gradient background relies on the monthStringBackground being set to a completely transparent color
         LinearGradientPaint gp = new LinearGradientPaint(bounds.x,
                                                          bounds.y,
                                                          bounds.x,
                                                          bounds.y + bounds.height,
                                                          new float[] { 0.0f, 1.0f },
                                                          new Color[] { Color.lightGray, Color.white });
         g.setPaint(gp);
         g.fill(bounds);

         border.paintBorder(monthView,
                            g,
                            bounds.x,
                            bounds.y,
                            bounds.width,
                            bounds.height);
      }
      super.paintDayOfMonth(g,
                            bounds,
                            calendar,
                            state);
   }
}
