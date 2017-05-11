package org.marketsuite.component.spinner;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.EventListenerList;
import org.marketsuite.component.button.DirectionButton;

/**
 * This component provides the up/down or right/left buttons used in
 * spinners. It is used by abstract class Spinner and in the HorizontalSpinButtonPanel
 * and VerticalSpinButtonPanel components.  This is a lightweight version of
 * Symantec's class.
 */
public class SpinButtonPanel extends JPanel
    implements SwingConstants {

    public SpinButtonPanel() {
        //{{INIT_CONTROLS
        super.setLayout(new GridLayout(2, 1, 0, 0));
        //}}
        incBtn = new SpinButtonPanel.SpinDirectionButton();
        incBtn.setActionCommand("Increment");
        incBtn.setDirection(DirectionButton.UP);
        add(incBtn);
        decBtn = new SpinButtonPanel.SpinDirectionButton();
        decBtn.setActionCommand("Decrement");
        decBtn.setDirection(DirectionButton.DOWN);
        add(decBtn);
        buttonListener = new SpinButtonPanel.ArrowButtonListener();
        incBtn.addMouseListener(buttonListener);
        incBtn.addActionListener(new SpinButtonPanel.ScrollListener(incBtn));
        decBtn.addMouseListener(buttonListener);
        decBtn.addActionListener(new SpinButtonPanel.ScrollListener(decBtn));
        scrollListener = new SpinButtonPanel.ScrollListener();
        scrollTimer = new Timer(100, scrollListener);
        scrollTimer.setInitialDelay(300);
    }

    SpinButtonPanel.SpinDirectionButton incBtn;
    SpinButtonPanel.SpinDirectionButton decBtn;
    protected int orientation;
    /**
     * List of listeners
     */
    protected EventListenerList listenerListL = new EventListenerList();
    protected SpinButtonPanel.ArrowButtonListener buttonListener;
    protected SpinButtonPanel.ScrollListener scrollListener;
    protected Timer scrollTimer;

    public void setOrientation(int o) {
        if (o != orientation) {
            orientation = o;

            switch (orientation) {
                case VERTICAL: {
                    super.setLayout(new GridLayout(2, 1, 0, 0));
                    break;
                }
                case HORIZONTAL: {
                    super.setLayout(new GridLayout(1, 2, 0, 0));
                    break;
                }
            }

            invalidate();
            validate();
        }
    }

    public int getOrientation() {
        return (orientation);
    }

    /**
     * Enables or disables this component so that it will respond to user input
     * or not.
     * This is a standard Java AWT method which gets called to enable or disable
     * this component. Once enabled this component will respond to user input.
     *
     * @param flag true if the component is to be enabled,
     *             false if it is to be disabled.
     * @see java.awt.Component#isEnabled
     */
    public void setEnabled(boolean flag) {
        if (isEnabled() != flag) {
            if (flag) {
                incBtn.setEnabled(true);
                decBtn.setEnabled(true);
            }
            else {
                incBtn.setEnabled(false);
                decBtn.setEnabled(false);
            }
            super.setEnabled(flag);
        }
    }

    /**
     * This enables or disables the incrementing button only.
     *
     * @param flag true if the incrementing button is to be enabled,
     *             false if it is to be disabled.
     * @see #isUpButtonEnabled
     */
    public void setUpButtonEnabled(boolean flag) {
        if (isUpButtonEnabled() != flag) {
            if (flag) {
                incBtn.setEnabled(true);
            }
            else {
                incBtn.setEnabled(false);
            }
        }
    }

    /**
     * The enabled state of the incrementing button.
     *
     * @return true if the incrementing button is enabled,
     *         false if it is disabled.
     * @see #setUpButtonEnabled
     */
    public boolean isUpButtonEnabled() {
        return incBtn.isEnabled();
    }

    /**
     * This enables or disables the decrementing button only.
     *
     * @param flag true if the decrementing button is to be enabled,
     *             false if it is to be disabled.
     * @see #isDownButtonEnabled
     */
    public void setDownButtonEnabled(boolean flag) {
        if (isDownButtonEnabled() != flag) {
            if (flag) {
                decBtn.setEnabled(true);
            }
            else {
                decBtn.setEnabled(false);
            }
        }
    }

    /**
     * The enabled state of the decrementing button.
     *
     * @return true if the decrementing button is enabled,
     *         false if it is disabled.
     * @see #setDownButtonEnabled
     */
    public boolean isDownButtonEnabled() {
        return decBtn.isEnabled();
    }

    /**
     * Adds the specified action listener to receive action events.
     * The ActionCommand will be either "Increment" or "Decrement"
     * depending on which spinner button was pressed.
     *
     * @param l the action listener
     */
    public void addActionListener(ActionListener l) {
        listenerListL.add(ActionListener.class, l);
    }

    /**
     * Removes the specified action listener so it no longer receives
     * action events from this component.
     *
     * @param l the action listener
     */
    public void removeActionListener(ActionListener l) {
        listenerListL.remove(ActionListener.class, l);
    }

    /**
     * Fire an action event to the listeners
     */
    protected void fireActionEvent(ActionEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerListL.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ((ActionListener) listeners[i + 1]).actionPerformed(e);
            }
        }
    }

    static private class SpinDirectionButton extends DirectionButton {
        //public boolean isFocusTraversable() { return false; }
        public boolean isFocusable() { return false; }
    }

    /**
     * Listener for cursor keys.
     */
    class ArrowButtonListener extends MouseAdapter
        implements Serializable {

        public void mousePressed(MouseEvent e) {
            //previusly even when it was disabled, using mouse
            //one could change the value.
            //added to ensure that mouse does not
            //chnage any values when it is disabled.
            if (isEnabled()) //punitma
            {
                scrollTimer.stop();
                scrollListener.setActionButton((JButton) e.getSource());
                scrollTimer.start();
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (isEnabled()) //punitma
                scrollTimer.stop();
        }
    }

    class ScrollListener implements ActionListener, Serializable {
        public ScrollListener() {
            //
        }

        public ScrollListener(JButton btn) { setActionButton(btn); }

        private JButton btn;

        public void setActionButton(JButton btn) {
            this.btn = btn;
        }

        public void actionPerformed(ActionEvent e) {
            fireActionEvent(new ActionEvent(btn,
                ActionEvent.ACTION_PERFORMED, btn.getActionCommand()));
        }
    }
}
