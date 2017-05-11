package org.marketsuite.component.table;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

public class ContextMenu extends JPopupMenu {
   public ContextMenu(JTable t) {
      table = t;
   }
   
   public void updateTableSelection(int row) {
      if (null != table && !table.isRowSelected(row))
         table.setRowSelectionInterval(row, row);
   }
   
   protected JTable table;
}
