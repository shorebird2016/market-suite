package org.marketsuite.component.dialog;

import java.util.EventListener;

public interface MessageBoxListener extends EventListener {
   /** Message Box has been initialized */
   public void initialized(MessageBoxEvent ev );
   /** Message Box has been disposed */
   public void disposed( MessageBoxEvent ev );
}
