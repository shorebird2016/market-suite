package org.marketsuite.component.spinner;

import java.awt.AWTEventMulticaster;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.marketsuite.component.text.TextFieldBase;

/**
 * This abstract class is used to create spinners. A spinner is a component
 * with two small direction buttons that lets the user scroll a list of
 * predetermined values to select one, or possibly enter a new legal value.
 * This is a lightweight component version of Symantec's Spinner class.
 */
public abstract class SpinnerBase extends JPanel implements SwingConstants, Serializable {

    class Action implements ActionListener, Serializable {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof TextFieldBase) {
                if (((TextFieldBase) e.getSource()) == textFld) {
                    updateText(false);
                    //Take the focus away from the edit box
                    requestFocus();
                    return;
                }
            }

            String cmdStr = "";
            String actionCommand = e.getActionCommand();

            if (actionCommand.equals("Increment")) {
                scrollUp();
                cmdStr = "ScrollUp";
                sourceActionEvent(cmdStr);
            }
            else if (actionCommand.equals("Decrement")) {
                scrollDown();
                cmdStr = "ScrollDown";
                sourceActionEvent(cmdStr);
            }
        }
    }

    protected final static int ORIENTATION_DEFAULT = VERTICAL;

    protected String text;
    protected int textWidth;
    protected int orientation;
    protected boolean wrappable;
    protected boolean editable;
    protected int min;
    protected int max;
    protected int current;
    protected int increment;
    protected ActionListener actionListener;
    protected SpinnerBase.Action action;
    protected boolean added;

    {
        min = 0;
        max = 0;
        increment = 1;
        current = 0;
        textWidth = 0;
        added = false;
    }

    protected SpinnerBase() {
        //{{INIT_CONTROLS
        //}}
        setLayout(null);
        textFld = new TextFieldBase();
        add(textFld);
        buttons = new SpinButtonPanel();
        add(buttons);
        setPreferredSize(new Dimension(72, 24));

        setWrappable(false);
        setOrientation(ORIENTATION_DEFAULT);
    }

    public void setPreferredSize(Dimension d) {
        super.setPreferredSize(d);
        textFld.setPreferredSize(new Dimension(d.width * 4 / 5, d.height));
        buttons.setPreferredSize(new Dimension(d.width / 5, d.height));
    }

    public void setMinimumSize(Dimension d) {
        super.setMinimumSize(d);
        textFld.setMinimumSize(new Dimension(d.width * 4 / 5, d.height));
        buttons.setMinimumSize(new Dimension(d.width / 5, d.height));
    }

    public void setMaximumSize(Dimension d) {
        super.setMaximumSize(d);
        textFld.setMaximumSize(new Dimension(d.width * 4 / 5, d.height));
        buttons.setMaximumSize(new Dimension(d.width / 5, d.height));
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        textFld.setBounds(0, 0, width * 4 / 5, height);
        buttons.setBounds(width * 4 / 5, 0, width / 5, height);
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);
        textFld.setEnabled(b);
        buttons.setEnabled(b);
        repaint();
    }

    protected TextFieldBase textFld;
    protected SpinButtonPanel buttons;

    /**
     * Conditionally enables editing of the Spinner's TextField.
     * @param f true = allow editing;
     *          false = disallow editing
     */
    public void setEditable(boolean f) {
        if (editable != f) {
            Boolean oldValue;
            Boolean newValue;

            oldValue = Boolean.valueOf(editable);
            newValue = Boolean.valueOf(f);
            editable = f;
            textFld.setEditable(editable);
            firePropertyChange("editable", oldValue, newValue);
        }
    }

    /**
     * Returns whether the Spinner's TextField is editable.
     * @return true if the TextField can be edited, false otherwise
     */
    public boolean isEditable() {
        return editable;
    }

    public void setOrientation(int o) {
        if (orientation != o) {
            Integer oldValue;
            Integer newValue;

            oldValue = Integer.valueOf(orientation);
            newValue = Integer.valueOf(o);
            orientation = o;
            buttons.setOrientation(orientation);
            firePropertyChange("orientation", oldValue, newValue);
        }
    }

    public int getOrientation() {
        return (orientation);
    }

    public void setWrappable(boolean f) {
        if (wrappable != f) {
            Boolean oldValue;
            Boolean newValue;

            oldValue = Boolean.valueOf(wrappable);
            newValue = Boolean.valueOf(f);
            wrappable = f;
            updateButtonStatus();
            firePropertyChange("wrappable", oldValue, newValue);
        }
    }

    public boolean isWrappable() {
        return (wrappable);
    }

    /**
     * Sets the minimum value the spinner may have.
     * @param i the new minimum value
     * @see #getMin
     */
    public void setMin(int i) {
        if (min != i) {
            Integer oldValue;
            Integer newValue;

            oldValue = Integer.valueOf(min);
            newValue = Integer.valueOf(i);
            min = i;

            if (getCurrent() < min)
                setCurrent(min);
            else
                updateButtonStatus();
            firePropertyChange("min", oldValue, newValue);
        }
    }

    /**
     * Gets the current minimum value the spinner may have.
     * @return the current minimum value
     */
    public int getMin() {
        return (min);
    }

    /**
     * Sets the maximum value the spinner may have.
     * @param i the new maximum value
     */
    public void setMax(int i) {
        if (max != i) {
            Integer oldValue;
            Integer newValue;
            oldValue = Integer.valueOf(max);
            newValue = Integer.valueOf(i);
            max = i;
            if (getCurrent() > max)
                setCurrent(max);
            else
                updateButtonStatus();
            firePropertyChange("max", oldValue, newValue);
        }
    }

    /**
     * Gets the current maximum value the spinner may have.
     * @return the current maximum value
     */
    public int getMax() {
        return (max);
    }

    /**
     * Sets the value of the spinner.
     * @param i the new value
     */
    public void setCurrent(int i) {
        if (current != i) {
            Integer oldValue;
            Integer newValue;
            oldValue = Integer.valueOf(current);
            newValue = Integer.valueOf(i);
            current = i;
            updateText(false);
            updateButtonStatus();
            firePropertyChange("current", oldValue, newValue);
        }
    }

    /**
     * Gets the current value of the spinner.
     * @return the current spinner value
     */
    public int getCurrent() {
        return (current);
    }

    /**
     * Returns the text that is in the entry TextField.
     */
    public String getEntryFieldText() {
        return (textFld.getText());
    }

    /**
     * Tells this component that it has been added to a container.
     * This is a standard Java AWT method which gets called by the AWT when
     * this component is added to a container. Typically, it is used to
     * create this component's peer.
     * <p/>
     * It has been overridden here to hook-up event listeners.
     * It is also used to setup the component, creating the TextField as needed.
     */
    public void addNotify() {
        super.addNotify();
        added = true;
        //Hook up listeners
        if (action == null) {
            action = new SpinnerBase.Action();
            buttons.addActionListener(action);
            textFld.addActionListener(action);
        }
        updateText(true);
    }

    /**
     * Tells this component that it is being removed from a container.
     * This is a standard Java AWT method which gets called by the AWT when
     * this component is removed from a container. Typically, it is used to
     * destroy the peers of this component and all its subcomponents.
     * <p/>
     * It has been overridden here to unhook event listeners.
     */
    public void removeNotify() {
        //Unhook listeners
        if (action != null) {
            textFld.removeActionListener(action);
            buttons.removeActionListener(action);
            action = null;
        }
        super.removeNotify();
    }

    /**
     * Adds the specified action listener to receive action events
     * from this button.
     * @param l the action listener
     */
    public void addActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.add(actionListener, l);
    }

    /**
     * Removes the specified action listener so it no longer receives
     * action events from this component.
     * @param l the action listener
     */
    public void removeActionListener(ActionListener l) {
        actionListener = AWTEventMulticaster.remove(actionListener, l);
    }

    /**
     * Is the given value valid for the Current property .
     * @param i the given value
     * @return true if the given value is acceptable, false if not.
     */
    protected boolean isValidCurrentValue(int i) {
        if (i > max || i < min)
            return false;
        return true;
    }

    /**
     * Is the given value valid for the Max property .
     * @param i the given value
     * @return true if the given value is acceptable, false if not.
     */
    protected boolean isValidMaxValue(int i) {
        return (i >= min);
    }

    /**
     * Is the given value valid for the Min property .
     * @param i the given value
     * @return true if the given value is acceptable, false if not.
     */
    protected boolean isValidMinValue(int i) {
        return (i <= max);
    }

    /**
     * Fire an action event to the listeners
     */
    protected void sourceActionEvent(String s) {
        if (actionListener != null)
            actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, s));
    }

    /**
     * Increments the spinner's value and handles wrapping as needed.
     */
    protected void scrollUp() {
        setCurrent(current + increment);

        updateText(false);
    }

    /**
     * Decrements the spinner's value and handles wrapping as needed.
     */
    protected void scrollDown() {
        setCurrent(current - increment);

        updateText(false);
    }

    /**
     * Updates the text field with the current text, as needed or depending on the force flag.
     * @param force If true, causes the text field to update even if the value has not changed.
     */
    protected void updateText(boolean force) {
        String currentText;
        currentText = getCurrentText();
        //If the text has changed, put the new text into the text field
        if (force || !textFld.getText().equals(currentText)) {
            textFld.setText(currentText);
        }
    }

    /**
     * Handles enabling or disabling the spinner buttons as needed.
     */
    protected void updateButtonStatus() {
        if (buttons != null) {
            if (wrappable) {
                buttons.setUpButtonEnabled(true);
                buttons.setDownButtonEnabled(true);
            }
            else {
                if (current == max && current == min) {
                    buttons.setUpButtonEnabled(false);
                    buttons.setDownButtonEnabled(false);
                }
                else if (current == max) {
                    buttons.setUpButtonEnabled(false);
                    buttons.setDownButtonEnabled(true);
                }
                else if (current == min) {
                    buttons.setUpButtonEnabled(true);
                    buttons.setDownButtonEnabled(false);
                }
                else {
                    buttons.setUpButtonEnabled(true);
                    buttons.setDownButtonEnabled(true);
                }
            }
        }
    }

    /**
     * Gets the currently selected string from the list.
     * @return the string currently visible in the Spinner
     */
    protected abstract String getCurrentText();
}
