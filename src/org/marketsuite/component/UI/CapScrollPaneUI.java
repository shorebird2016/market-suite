package org.marketsuite.component.UI;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollPaneUI;

// modified mouseWheelListener to scroll horizontally if shift is down
// scrolls horizontally by only 1 column in a jtable

public class CapScrollPaneUI extends BasicScrollPaneUI implements MouseWheelListener {

   public static ComponentUI createUI(JComponent x) {
      return new CapScrollPaneUI();
   }

   protected MouseWheelListener createMouseWheelListener() {
      return this;
   }

   public void mouseWheelMoved(MouseWheelEvent e) {
      if (scrollpane.isWheelScrollingEnabled() &&
            e.getWheelRotation() != 0) {
         JScrollBar toScroll = scrollpane.getVerticalScrollBar();
         int direction = e.getWheelRotation() < 0 ? -1 : 1;
         int orientation = SwingConstants.VERTICAL;

         // find which scrollbar to scroll, or return if none
         if (toScroll == null || !toScroll.isVisible() || e.isShiftDown()) { 
            toScroll = scrollpane.getHorizontalScrollBar();
            if (toScroll == null || !toScroll.isVisible()) { 
               return;
            }
            orientation = SwingConstants.HORIZONTAL;
         }

         e.consume();

         if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            JViewport vp = scrollpane.getViewport();
            if (vp == null) { return; }
            Component comp = vp.getView();
            int units = Math.abs(e.getUnitsToScroll());

            // When the scrolling speed is set to maximum, it's possible
            // for a single wheel click to scroll by more units than
            // will fit in the visible area.  This makes it
            // hard/impossible to get to certain parts of the scrolling
            // Component with the wheel.  To make for more accurate
            // low-speed scrolling, we limit scrolling to the block
            // increment if the wheel was only rotated one click.
            boolean limitScroll = Math.abs(e.getWheelRotation()) == 1;

            // Check if we should use the visibleRect trick
            Object fastWheelScroll = toScroll.getClientProperty(
            "JScrollBar.fastWheelScrolling");
            if (Boolean.TRUE == fastWheelScroll &&
                  comp instanceof Scrollable) {
               // 5078454: Under maximum acceleration, we may scroll
               // by many 100s of units in ~1 second.
               //
               // BasicScrollBarUI.scrollByUnits() can bog down the EDT
               // with repaints in this situation.  However, the
               // Scrollable interface allows us to pass in an
               // arbitrary visibleRect.  This allows us to accurately
               // calculate the total scroll amount, and then update
               // the GUI once.  This technique provides much faster
               // accelerated wheel scrolling.
               Scrollable scrollComp = (Scrollable) comp;
               Rectangle viewRect = vp.getViewRect();
               int startingX = viewRect.x;
               boolean leftToRight =
                  comp.getComponentOrientation().isLeftToRight();
               int scrollMin = toScroll.getMinimum();
               int scrollMax = toScroll.getMaximum() -
               toScroll.getModel().getExtent();

               if (limitScroll) {
                  int blockIncr =
                     scrollComp.getScrollableBlockIncrement(viewRect,
                                                            orientation,
                                                            direction);
                  if (direction < 0) {
                     scrollMin = Math.max(scrollMin,
                                          toScroll.getValue() - blockIncr);
                  }
                  else {
                     scrollMax = Math.min(scrollMax,
                                          toScroll.getValue() + blockIncr);
                  }
               }

               for (int i = 0; i < units; i++) {
                  int unitIncr =
                     scrollComp.getScrollableUnitIncrement(viewRect,
                                                           orientation, direction);
                  // Modify the visible rect for the next unit, and
                  // check to see if we're at the end already.
                  if (orientation == SwingConstants.VERTICAL) {
                     if (direction < 0) {
                        viewRect.y -= unitIncr;
                        if (viewRect.y <= scrollMin) {
                           viewRect.y = scrollMin;
                           break;
                        }
                     }
                     else { // (direction > 0
                        viewRect.y += unitIncr;
                        if (viewRect.y >= scrollMax) {
                           viewRect.y = scrollMax;
                           break;
                        }
                     }
                  }
                  else {
                     // Scroll left
                     if ((leftToRight && direction < 0) ||
                           (!leftToRight && direction > 0)) {
                        viewRect.x -= unitIncr;
                        if (leftToRight) {
                           if (viewRect.x < scrollMin) {
                              viewRect.x = scrollMin;
                              break;
                           }
                        }
                     }
                     // Scroll right
                     else if ((leftToRight && direction > 0) ||
                           (!leftToRight && direction < 0)) {
                        viewRect.x += unitIncr;
                        if (leftToRight) {
                           if (viewRect.x > scrollMax) {
                              viewRect.x = scrollMax;
                              break;
                           }
                        }
                     }
                     else {
                        assert false : "Non-sensical ComponentOrientation / scroll direction";
                     }
                     // scroll only 1 column of a table
                     if(comp instanceof JTable) break;
                  }
               }
               // Set the final view position on the ScrollBar
               if (orientation == SwingConstants.VERTICAL) {
                  toScroll.setValue(viewRect.y);
               }
               else {
                  if (leftToRight) {
                     toScroll.setValue(viewRect.x);
                  }
                  else {
                     // rightToLeft scrollbars are oriented with 
                     // minValue on the right and maxValue on the
                     // left.
                     int newPos = toScroll.getValue() -
                     (viewRect.x - startingX);
                     if (newPos < scrollMin) {
                        newPos = scrollMin;
                     }
                     else if (newPos > scrollMax) {
                        newPos = scrollMax;
                     }
                     toScroll.setValue(newPos);
                  }
               }
            }
            else {
               // Viewport's view is not a Scrollable, or fast wheel
               // scrolling is not enabled.
               scrollByUnits(toScroll, direction,
                             units, limitScroll);
            }
         }
         else if (e.getScrollType() ==
            MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            scrollByBlock(toScroll, direction);
         }
      }
   }

   // these 2 methods are package protected static in BasicScrollbarUI, copied here
   private void scrollByBlock(JScrollBar scrollbar, int direction) {
      // This method is called from BasicScrollPaneUI to implement wheel
      // scrolling, and also from scrollByBlock().
      int oldValue = scrollbar.getValue();
      int blockIncrement = scrollbar.getBlockIncrement(direction);
      int delta = blockIncrement * ((direction > 0) ? +1 : -1);
      int newValue = oldValue + delta;

      // Check for overflow.
      if (delta > 0 && newValue < oldValue) {
         newValue = scrollbar.getMaximum();
      }
      else if (delta < 0 && newValue > oldValue) {
         newValue = scrollbar.getMinimum();
      }

      scrollbar.setValue(newValue);         
   }
   private void scrollByUnits(JScrollBar scrollbar, int direction,
                              int units, boolean limitToBlock) {
      // This method is called from BasicScrollPaneUI to implement wheel
      // scrolling, as well as from scrollByUnit().
      int delta;
      int limit = -1;

      if (limitToBlock) {
         if (direction < 0) {
            limit = scrollbar.getValue() -
            scrollbar.getBlockIncrement(direction);
         }
         else {
            limit = scrollbar.getValue() +
            scrollbar.getBlockIncrement(direction);
         }
      }

      for (int i=0; i<units; i++) {
         if (direction > 0) {
            delta = scrollbar.getUnitIncrement(direction);
         }
         else {
            delta = -scrollbar.getUnitIncrement(direction);
         }

         int oldValue = scrollbar.getValue();
         int newValue = oldValue + delta;

         // Check for overflow.
         if (delta > 0 && newValue < oldValue) {
            newValue = scrollbar.getMaximum();
         }
         else if (delta < 0 && newValue > oldValue) {
            newValue = scrollbar.getMinimum();
         }
         if (oldValue == newValue) {
            break;
         }

         if (limitToBlock && i > 0) {
            assert limit != -1;
            if ((direction < 0 && newValue < limit) ||
                  (direction > 0 && newValue > limit)) {
               break;
            }
         }
         scrollbar.setValue(newValue);
      }
   }
}
