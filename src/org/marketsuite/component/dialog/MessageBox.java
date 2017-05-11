package org.marketsuite.component.dialog;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import org.marketsuite.component.button.StandardButton;
import org.marketsuite.component.button.StandardButtonResources;
import org.marketsuite.component.label.WrappingLabel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.button.StandardButton;
import org.marketsuite.component.button.StandardButtonResources;
import org.marketsuite.component.label.WrappingLabel;
import org.marketsuite.component.resource.LazyIcon;

/**
 * A customizable extension of JOptionPane.
 */
public class MessageBox extends JOptionPane {
    protected MessageBox(Object caption, int messageType, int style) {
        super(caption, messageType, DEFAULT_OPTION, null, OPTIONS[style], OPTIONS[style][0]);
    }

    /**
     * Create a modal message box with an OK button
     * @param title   The title displayed at the top of the message box
     * @param caption The text to be displayed in the dialog itself
     */
    public static void messageBox(String title, String caption) {
        messageBox(title, caption, STYLE_OK);
    }

    /**
     * Create a modal message box
     * @param title   The title displayed at the top of the message box
     * @param caption The text to be displayed in the dialog itself
     * @param style   The message box style (see styles)
     * @return The result of which button the user selected (see result codes)
     */
    public static int messageBox(String title, String caption, int style) {
        return messageBox(title, caption, style, IMAGE_WARNING);
    }

    /**
     * Create a modal message box
     * @param title   The title displayed at the top of the message box
     * @param caption The text to be displayed in the dialog itself
     * @param style   The message box style (see styles)
     * @param image   The image to be displayed beside the dialog text.
     * @return The result of which button the user selected (see result codes)
     */
    public static int messageBox(String title, String caption, int style, int image) {
        return getResult(showOptionDialog(caption, title, image, style), style);
    }

    /**
     * Create a modal message box with the given parent Component.
     * @param parentComponent To show message box
     * @param title           The title displayed at the top of the message box
     * @param caption         The text to be displayed in the dialog itself
     * @param style           The message box style (see styles)
     * @param image           The image to be displayed beside the dialog text.
     * @return The result of which button the user selected (see result codes)
     */
    public static int messageBox(Component parentComponent, String title, String caption, int style, int image) {
        parent = parentComponent;
        return getResult(showOptionDialog(caption, title, image, style), style);
    }

    public static int showOptionDialog(String caption, String title, int messageType, int style) {
        // Create a new wrapping label
        JLabel label;
        boolean isHTML = false;
        caption = caption.trim();
        if (caption.startsWith("<HTML>") || caption.startsWith("<html>")) {
            isHTML = true;
            label = new JLabel(caption, JLabel.CENTER);
        }
        else
            label = new WrappingLabel(caption, WrappingLabel.CENTER);
        // If the text probably won't fit on the screen, set the preferred
        // size of the wrapping label.
        int font_size = label.getFont().getSize();
        int captionLength = caption.length();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (!isHTML && captionLength * font_size > screenSize.width) {
            int preferredWidth = screenSize.width - 200;
            int preferredHeight = (captionLength * font_size / preferredWidth + 1) * font_size;
            label.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        }
        else
            label = new JLabel(caption, JLabel.LEFT);
        label.setForeground(UIManager.getColor("OptionPane.foreground"));
        // Create the message box pane
        MessageBox pane;
        if (captionLength > 1200) {
            JScrollPane scrollPane = new JScrollPane(label);
            scrollPane.setPreferredSize(new Dimension(screenSize.width - 500, screenSize.height - 400));
            pane = new MessageBox(scrollPane, messageType, style);
        }
        else
            pane = new MessageBox(label, messageType, style);
        pane.setInitialValue(OPTIONS[style][0]);
        // Create the dialog
        JDialog dialog = pane.createDialog(getParentFrame(), title);
        dialog.setLocationRelativeTo(getParentFrame());
        pane.selectInitialValue();
        // Fire an event that the dialog has been initialized
        fireInitialized(new MessageBoxEvent(pane));
        dialog.setVisible(true);
        // Get the selected value from the dialog
        Object selectedValue = pane.getValue();
        if (selectedValue == null)
            return CLOSED_OPTION;
        for (int counter = 0, maxCounter = OPTIONS[style].length;
             counter < maxCounter; counter++) {
            if (OPTIONS[style][counter].equals(selectedValue))
                return counter;
        }
        return CLOSED_OPTION;
    }

    public static int showOptionDialog(JFrame parentFrame, String caption, String title, int messageType, int style) {
        parent = parentFrame;
        return showOptionDialog(caption, title, messageType, style);
    }

    public JDialog createDialog(Component parentComponent, String title) {
        MessageBoxDialog dialog = new MessageBoxDialog(this, parentComponent, title);
        dialog.setIconImage(_IconImage);
        dialog.setModalityType(modalityType);
        addPropertyChangeListener(new MessageBox.SymProperty(dialog));
        return dialog;
    }

    /**
     * Change default application icon, this call sets up a static variable and used by later static calls to set
     * icon of this dialog
     * @param image custom icon for this message dialog
     */
    public static void setIconImage(Image image) {
        _IconImage = image;
    }

    public static void setModalityType(Dialog.ModalityType type) {
        modalityType = type;
    }

    private class SymProperty implements PropertyChangeListener {
        private SymProperty(MessageBoxDialog dialog) {
            this.dialog = dialog;
        }

        public void propertyChange(PropertyChangeEvent event) {
            if (dialog.isVisible() &&
                event.getSource() == MessageBox.this &&
                (event.getPropertyName().equals(VALUE_PROPERTY) ||
                    event.getPropertyName().equals(INPUT_VALUE_PROPERTY))) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }

        private MessageBoxDialog dialog;
    }

    /**
     * Get the parent for all modal dialog boxes.  Returns a new Frame
     * object if there was previously none.
     *
     * @return the parent instance
     */
    public static Component getParentFrame() {
        if (parent == null) {
            parent = new JFrame();
            createdParentFrame = true;
        }
        return parent;
    }

    /**
     * Set parent for all modal dialog boxes. Disposes previously assigned parent.
     * @param parentComponent To show modal message box.
     */
    public static void setParentFrame(Component parentComponent) {
        // Dispose old parent frame
        disposeParentFrame();
        parent = parentComponent;
    }

    // Dispose any existing parent frame
    private static void disposeParentFrame() {
        if (createdParentFrame) {
            ((JFrame)parent).dispose();
            parent = null;
            createdParentFrame = false;
        }
    }

    private static Component parent;
    private static boolean createdParentFrame = false;
    private final static ResourceBundle resources = ResourceBundle.getBundle(StandardButtonResources.class.getName());

    public static int getResult(int res, int style) {
        if (res == -1) {
            return DEFAULT_RESULTS[style];
        }
        return RESULTS[style][res];
    }

    public static void addMessageBoxListener(MessageBoxListener l) {
        listenerList.add(MessageBoxListener.class, l);
    }

    public static void removeMessageBoxListener(MessageBoxListener l) {
        listenerList.remove(MessageBoxListener.class, l);
    }

    protected static void fireInitialized(MessageBoxEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == MessageBoxListener.class) {
                ((MessageBoxListener) listeners[i + 1]).initialized(e);
            }
        }
    }

    protected static void fireDisposed(MessageBoxEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == MessageBoxListener.class) {
                ((MessageBoxListener) listeners[i + 1]).disposed(e);
            }
        }
    }

    //variables
    private static Image _IconImage = LazyIcon.APP_ICON.getImage();

    public final static int STYLE_OK = 0;
    public final static int STYLE_OK_CANCEL = 1;
    public final static int STYLE_ABORT_RETRY_IGNORE = 2;
    public final static int STYLE_RETRY_CANCEL = 3;
    public final static int STYLE_YES_NO = 4;
    public final static int STYLE_YES_NO_CANCEL = 5;
    /**
     * Options for the above styles
     */
    private final static Object[][] OPTIONS = {
        // STYLE_OK
        {resources.getString(StandardButton.OK),},
        // STYLE_OK_CANCEL
        {resources.getString(StandardButton.OK), resources.getString(StandardButton.CANCEL),},
        // STYLE_ABORT_RETRY_IGNORE
        {resources.getString(StandardButton.ABORT), resources.getString(StandardButton.RETRY), resources.getString(StandardButton.IGNORE),},
        // STYLE_ABORT_RETRY_CANCEL
        {resources.getString(StandardButton.ABORT), resources.getString(StandardButton.RETRY), resources.getString(StandardButton.CANCEL),},
        // STYLE_YES_NO
        {resources.getString(StandardButton.YES), resources.getString(StandardButton.NO),},
        // STYLE_YES_NO_CANCEL
        {resources.getString(StandardButton.YES), resources.getString(StandardButton.NO), resources.getString(StandardButton.CANCEL),},
    };

    public final static int RESULT_OK = 0;
    public final static int RESULT_CANCEL = 1;
    public final static int RESULT_ABORT = 2;
    public final static int RESULT_RETRY = 3;
    public final static int RESULT_IGNORE = 4;
    public final static int RESULT_YES = 5;
    public final static int RESULT_NO = 6;

    private final static int[][] RESULTS = {
        // STYLE_OK
            {RESULT_OK,},
            // STYLE_OK_CANCEL
            {RESULT_OK, RESULT_CANCEL},
            // STYLE_ABORT_RETRY_IGNORE
            {RESULT_ABORT, RESULT_RETRY, RESULT_IGNORE},
            // STYLE_ABORT_RETRY_CANCEL
            {RESULT_ABORT, RESULT_RETRY, RESULT_CANCEL},
            // STYLE_YES_NO
            {RESULT_YES, RESULT_NO},
            // STYLE_YES_NO_CANCEL
            {RESULT_YES, RESULT_NO, RESULT_CANCEL},
        };

    private final static int[] DEFAULT_RESULTS = {
        // STYLE_OK
            RESULT_OK,
            // STYLE_OK_CANCEL
            RESULT_CANCEL,
            // STYLE_ABORT_RETRY_IGNORE
            RESULT_ABORT,
            // STYLE_ABORT_RETRY_CANCEL
            RESULT_CANCEL,
            // STYLE_YES_NO
            RESULT_NO,
            // STYLE_YES_NO_CANCEL
            RESULT_CANCEL,
        };

    public final static int IMAGE_ERROR = JOptionPane.ERROR_MESSAGE;
    public final static int IMAGE_WARNING = JOptionPane.WARNING_MESSAGE;
    public final static int IMAGE_QUESTION = JOptionPane.QUESTION_MESSAGE;
    public final static int IMAGE_INFORMATION = JOptionPane.INFORMATION_MESSAGE;
    private final static EventListenerList listenerList = new EventListenerList();
    private static Dialog.ModalityType modalityType = Dialog.ModalityType.APPLICATION_MODAL;

}