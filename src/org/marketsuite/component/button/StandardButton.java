package org.marketsuite.component.button;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * A typically reused button, such as an OK or Cancel button.
 * This is a useful class for localization purposes.
 */
public class StandardButton extends JButton {
	/**
     * Button styles
     */
    public final static String ABORT = "Abort";
    public final static String ABOUT = "About";
    public final static String ABOUT_ = "About...";
    public final static String ACCEPT = "Accept";
    public final static String ADD = "Add";
    public final static String ADD_ = "Add...";
    public final static String ADDPROG = "Add Prog";//David Borovsky
    public final static String ADDPID = "Add PID";//David Borovsky
    public final static String APPLY = "Apply";
    public final static String BACK = "Back";
    public final static String BACKGROUND = "Background";
    public final static String BACKGROUND_ = "Background...";
    public final static String CANCEL = "Cancel";
    public final static String CENTER = "Center";
    public final static String CLEAR = "Clear";
    public final static String CLOSE = "Close";
    public final static String CONNECT = "Connect";
    public final static String COPY = "Copy";
    public final static String CUT = "Cut";
    public final static String DEFAULT = "Default";
    public final static String DELETE = "Delete";
    public final static String DELPROG = "Delete Prog";//David borovsky
    public final static String DELPID = "Delete PID";//David Borovsky
    public final static String DONE = "Done";
    public final static String DOWN = "Down";
    public final static String EDIT = "Edit";
    public final static String EDIT_ = "Edit...";
    public final static String EXIT = "Exit";
    public final static String FILE = "File";
    public final static String FIND = "Find";
    public final static String FIND_ = "Find...";
    public final static String FINISH = "Finish";
    public final static String FOREGROUND = "Foreground";
    public final static String FOREGROUND_ = "Foreground...";
    public final static String FORWARD = "Forward";
    public final static String GO = "Go";
    public final static String GO_ = "Go...";
    public final static String HELP = "Help";
    public final static String HELP_ = "Help...";
    public final static String IGNORE = "Ignore";
    public final static String INSERT = "Insert";
    public final static String KILL = "Kill";
    public final static String LEFT = "Left";
    public final static String LESS = "Less";
    public final static String LOGIN = "Login";
    public final static String LOGIN_ = "Login...";
    public final static String LOG_IN = "Log In";
    public final static String LOG_IN_ = "Log In...";
    public final static String LOGOUT = "Logout";
    public final static String LOG_OUT = "Log Out";
    public final static String MORE = "More";
    public final static String MORE_ = "More...";
    public final static String MORE__ = "...";
    public final static String MOVE = "Move";
    public final static String NEW = "New";
    public final static String NEW_ = "New...";
    public final static String NEXT = "Next";
    public final static String NO = "No";
    public final static String NOALL = "No to All";
    public final static String OK = "OK";
    public final static String OPEN = "Open";
    public final static String OPEN_ = "Open...";
    public final static String PASTE = "Paste";
    public final static String PREVIOUS = "Previous";
    public final static String PRINT = "Print";
    public final static String PRINT_ = "Print...";
    public final static String QUIT = "Quit";
    public final static String REBOOT = "Reboot";
    public final static String REDO = "Redo";
    public final static String REJECT = "Reject";
    public final static String REMOVE = "Remove";
    public final static String RESTART = "Restart";
    public final static String RESTORE = "Restore";
    public final static String RETRY = "Retry";
    public final static String RIGHT = "Right";
    public final static String RUN = "Run";
    public final static String RUN_ = "Run...";
    public final static String SAVE = "Save";
    public final static String SAVE_AS = "Save As";
    public final static String SAVE_AS_ = "Save As...";
    public final static String SEARCH = "Search";
    public final static String SEARCH_ = "Search...";
    public final static String START = "Start";
    public final static String START_ = "Start...";
    public final static String STOP = "Stop";
    public final static String UNDO = "Undo";
    public final static String UP = "Up";
    public final static String VIEW = "View";
    public final static String WINDOW = "Window";
    public final static String YES = "Yes";
    public final static String CHANGE_HOST_ = "Change Host...";
    public final static String RECONNECT = "Reconnect";
    public final static String BROWSE = "Browse";

    //to ba part of the second release
    public final static String YESALL = "Yes to All";

    /**
     * Create a standard button (default is an OK button)
     */
    public StandardButton() {
        this(OK);
    }

    /**
     * Create a standard button with the specified style
     *
     * @param style The style for the button.  This is the name of the
     *              localized label in the resource bundle for StandardButton.  <B>This is
     *              not the label itself</B>
     */
    public StandardButton(String style) {
        resources = ResourceBundle.getBundle("org.marketsuite.component.button.StandardButtonResources");
        setStyle(style);
    }

    private ResourceBundle resources;

    /**
     * Set the button style
     *
     * @param style The style for the button.  This is the name of the
     *              localized label in the resource bundle for StandardButton.  <B>This is
     *              not the label itself</B>
     */
    public void setStyle(String style) {
        this.style = style;

        String str = resources.getString(style);
        super.setText(str);
    }

    /**
     * Get the button style
     *
     * @return The style for the button.  This is the name of the
     *         localized label in the resource bundle for StandardButton.  <B>This is
     *         not the label itself</B>
     */
    public String getStyle() {
        return style;
    }

    private String style;

    /**
     * Set the label
     * Does nothing--overridden here to throw an exception
     */
    public void setText(String label) {
        throw new RuntimeException("Do not call setText().  Use setStyle() instead");
    }
}
