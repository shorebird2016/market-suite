package org.marketsuite.component.spinner;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * A component with a spin button for decimal numbers, title text, min and max ranges and trailing text.
 */
public class DecimalSpinner extends JPanel {
    /**
     * CTOR - create a spinner that displays an integer with specified min, max and step size, also allows setting
     * of leading text and trailing text and change listener.
     * @param leading_text leading text, null or empty ok
     * @param value        initial value
     * @param min          minimum
     * @param max          maximum
     * @param step         increment / decrement size
     * @param trail_text   trailing text, null or "" ok
     * @param listener     change listener, null = no listener supplied
     */
    public DecimalSpinner(String leading_text, double value, double min, double max, double step,
                          String trail_text, ChangeListener listener) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        //_model = new SpinnerNumberModel(value, min, max, step);
        _model = new SpinnerModel(value, min, max, step, true);
        if (leading_text != null && leading_text.length() > 0) {
            JLabel lbl = new JLabel(leading_text);
            lbl.setOpaque(false);
            add(lbl);
        }
        add(_spinner = new JSpinner(_model));
        _spinner.setPreferredSize(new Dimension(50, 20));
        if (trail_text != null && trail_text.length() > 0) {
            JLabel lbl = new JLabel(trail_text);
            lbl.setOpaque(false);
            add(lbl);
        }
        if (listener != null)
            _spinner.addChangeListener(listener);
    }

    public void setEnabled(boolean flag) {
        _spinner.setEnabled(flag);
    }
    public double getValue() { return (Double) _spinner.getValue(); }
    public void setValue(double v) { _spinner.setValue(v); }

    public void setMax(int max) {
        _model.setMaximum(max); //must use Comparable
    }

    public void setMin(int min) {
        _model.setMinimum(min); //must use Comparable
    }

    /**
     * Add extra change listener.
     */
    public void addChangeListener(ChangeListener listener) {
        _spinner.addChangeListener(listener);
    }
//TODO really need this model ????
    private static class SpinnerModel extends SpinnerNumberModel {
        public SpinnerModel(double _value, double _min, double _max, double _step, boolean _cycle) {
            super(_value, _min, _max, _step);
            value = _value;
            min = _min;
            max = _max;
            step = _step;
            cycle = _cycle;
        }
        public void setMaximum(Comparable _max) {
            super.setMaximum(_max);
            max = (Integer) _max;
        }
        public void setMinimum(Comparable _min) {
            super.setMinimum(_min);
            min = (Integer) _min;
        }
        public void setValue(Object _val) {
            super.setValue(_val);
            value = (Double) _val;
        }
        public Object getNextValue() {
            value += step;
            if (value > max)
                value = cycle ? min : max;
            return Double.valueOf(value);
        }
        public Object getPreviousValue() {
            value -= step;
            if (value < min)
                value = cycle ? max : min;
            return Double.valueOf(value);
        }

        //----- variables -----
        private double value, min, max, step;
        private boolean cycle;
    }

    //instance variables
    private JSpinner _spinner;
    private SpinnerNumberModel _model;
}