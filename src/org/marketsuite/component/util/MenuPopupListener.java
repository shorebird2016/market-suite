package org.marketsuite.component.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.marketsuite.component.table.TableUtil;

/**
 * Listener for cross platform popup menu handling
 * Different platforms may popup based on mousePressed,mouseReleased or mouseClicked events
 * They all need to be checked with isPopupTrigger before calling the user supplied 'showMenu' method
 * FixedTableAdapter ensures this handler will be copied to a table with some fixed columns
 */
public abstract class MenuPopupListener extends MouseAdapter implements TableUtil.FixedTableAdapter {
   public abstract void showMenu(MouseEvent ev);
   
   public void mousePressed(MouseEvent ev) {
      if(ev.isPopupTrigger())
         showMenu(ev);
   }
   public void mouseReleased(MouseEvent ev) {
      if(ev.isPopupTrigger())
         showMenu(ev);
   }
   public void mouseClicked(MouseEvent ev) {
      if(ev.isPopupTrigger())
         showMenu(ev);
   }
}
