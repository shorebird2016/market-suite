package org.marketsuite.component.spinner;

import java.io.Serializable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A Numeric Spinner that checks for user-typed values.
 * Symantec's NumericSpinner doesn't check user-typed values.
 * The default is editable; Symantec's spinner is by default uneditable.
 */
public class SmartNumericSpinner extends NumericSpinner implements Serializable {
    public SmartNumericSpinner() {
        setEditable(true);
    }

    public void addNotify() {
        super.addNotify();
        lSymDocument = new SmartNumericSpinner.SymDocument();
        textFld.getDocument().addDocumentListener(lSymDocument);
    }

    public void removeNotify() {
        if (lSymDocument != null) {
            textFld.getDocument().removeDocumentListener(lSymDocument);
            lSymDocument = null;
        }
        super.removeNotify();
    }

    class SymDocument implements DocumentListener {
        public void changedUpdate(DocumentEvent event) {
            new SmartNumericSpinner.DocumentUpdater().start();
        }

        public void insertUpdate(DocumentEvent event) {
            new SmartNumericSpinner.DocumentUpdater().start();
        }

        public void removeUpdate(DocumentEvent event) {
            new SmartNumericSpinner.DocumentUpdater().start();
        }
    }

    class DocumentUpdater extends Thread {
        /**
         * This operation must be done in a separate thread because
         * the current implementation of Swing locks the document for writing
         * and does not release the lock until *after* notifying the listeners.
         * This kinda makes having the listeners almost pointless.
         */
        public void run() {
            SwingUtilities.invokeLater(new SmartNumericSpinner.DocumentUpdateCallback());
        }
    }

    class DocumentUpdateCallback implements Runnable {
        public void run() {
            try {
                int value = Integer.valueOf(textFld.getText()).intValue();
                int max1 = getMax();
                int min1 = getMin();
                if (value > max1)
                    value = max1;
                if (value < min1)
                    value = min1;
                setCurrent(value);
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private transient SmartNumericSpinner.SymDocument lSymDocument;
}
