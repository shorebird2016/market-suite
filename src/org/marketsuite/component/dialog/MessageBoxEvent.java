package org.marketsuite.component.dialog;

import java.io.Serializable;
import java.util.EventObject;

public class MessageBoxEvent extends EventObject
    implements Serializable {

	public MessageBoxEvent(MessageBox source) {
        super(source);
    }
}
