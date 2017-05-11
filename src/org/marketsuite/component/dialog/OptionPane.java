package org.marketsuite.component.dialog;

import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import org.marketsuite.component.resource.LazyIcon;

//derivative of JOptionPane, the only purpose is to intercept JDK version to include icon
public class OptionPane extends JOptionPane {
	public Object showDialog(
            Component parentComponent,
            Object message1,
            String title,
            int messageType1,
            Icon icon1,
            Object[] selectionValues1,
            Object initialSelectionValue1) throws HeadlessException {
        JOptionPane pane = new JOptionPane(message1, messageType1, OK_CANCEL_OPTION, icon1, null, null);
        pane.setWantsInput(true);
        pane.setSelectionValues(selectionValues1);
        pane.setInitialSelectionValue(initialSelectionValue1);
        pane.setComponentOrientation(((parentComponent == null) ?
                getRootFrame() : parentComponent).getComponentOrientation());
        int style = styleFromMessageType(messageType1);
        setMessageType(style);
        JDialog dialog = pane.createDialog(parentComponent, title);
        dialog.setIconImage(LazyIcon.DIALOG_ICON.getImage());
        pane.selectInitialValue();
        dialog.setVisible(true);
        dialog.dispose();
        Object value1 = pane.getInputValue();
        if (value1 == UNINITIALIZED_VALUE)
            return null;
        return value1;
    }

    private int styleFromMessageType(int messageType1) {
        switch (messageType1) {
            case ERROR_MESSAGE:
                return JRootPane.ERROR_DIALOG;
            case QUESTION_MESSAGE:
                return JRootPane.QUESTION_DIALOG;
            case WARNING_MESSAGE:
                return JRootPane.WARNING_DIALOG;
            case INFORMATION_MESSAGE:
                return JRootPane.INFORMATION_DIALOG;
            case PLAIN_MESSAGE:
            default:
                return JRootPane.PLAIN_DIALOG;
        }
    }
}
