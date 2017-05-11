package org.marketsuite.component.spinner;

import java.io.Serializable;

/**
 * Creates a text box, containing a list of numbers, with up and down arrows.
 * Use this component to allow your users to move through a set of fixed
 * values or type a valid value in the box.
 * <p/>
 * At run time only the selected value is displayed in the text box.
 * <p/>
 * This is a lightweight component version of Symantec's.
 *
 * @author Michael Martak
 * @version 1.0 02/10/98
 */
public class NumericSpinner extends SpinnerBase implements Serializable {
    /**
     * Constructs an empty NumericSpinner.
     */
    public NumericSpinner() {
        setIncrement(1);
        min = 0;
        max = 10;
        zeroPadding = 0;
    }

    /**
     * Sets the minimum value the spinner may have.
     * Overriden here to set the size of the text area.
     *
     * @param i the new minimum value
     */
    public void setMin(int i) {
        super.setMin(i);

        if (added) {
            textWidth = Math.max(Integer.toString(min).length(), Integer.toString(max).length());
        }
    }

    /**
     * Sets the maximum value the spinner may have.
     * Overriden here to set the size of the text area.
     *
     * @param i the new maximum value
     */
    public void setMax(int i) {
        super.setMax(i);

        if (added) {
            textWidth = Math.max(Integer.toString(min).length(), Integer.toString(max).length());
        }
    }

    /**
     * Gets the current text from the Spinner.
     *
     * @return the text of the currently selected Spinner value
     */
    public String getCurrentText() {
        String s = Integer.toString(current);
        int zeroes = zeroPadding - s.length();
        StringBuffer sb = new StringBuffer();
        while (zeroes > 0) {
            sb.append('0');
            --zeroes;
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Tells this component that it has been added to a container.
     * This is a standard Java AWT method which gets called by the AWT when
     * this component is added to a container. Typically, it is used to
     * create this component's peer.
     * Here it's used to set maximum text width and note the text of the
     * current selection.
     *
     * @see java.awt.Container#removeNotify
     */
    public void addNotify() {
        textWidth = Math.max(Integer.toString(min).length(), Integer.toString(max).length());
        text = Integer.toString(current);
        super.addNotify();
        updateText(false);
    }

    /**
     * Sets the value to increment/decrement the Spinner by.
     *
     * @param i the increment/decrement value
     * @see #getIncrement
     */
    public void setIncrement(int i) {
        Integer oldValue = Integer.valueOf(increment);
        Integer newValue = Integer.valueOf(i);

        increment = i;
        firePropertyChange("increment", oldValue, newValue);
    }

    /**
     * Gets the increment/decrement value.
     *
     * @return the increment/decrement value
     * @see #setIncrement
     */
    public int getIncrement() {
        return increment;
    }

    public void setZeroPadding(int zeroes) {
        zeroPadding = zeroes;
        repaint();
    }

    public int getZeroPadding() {
        return zeroPadding;
    }

    private int zeroPadding;
}
