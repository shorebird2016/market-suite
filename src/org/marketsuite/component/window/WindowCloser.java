package org.marketsuite.component.window;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Very useful utility window adapter for closing windows.
 * This is a preferable alternative to using anonymous classes.
 */
public class WindowCloser extends WindowAdapter
    implements ComponentListener {
    public void componentHidden(ComponentEvent e) {
        System.exit(0);
    }

    public void componentMoved(ComponentEvent e) {
        //
    }

    public void componentResized(ComponentEvent e) {
        //
    }

    public void componentShown(ComponentEvent e) {
        //
    }

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }
}
