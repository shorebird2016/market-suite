package org.marketsuite.component.spinner;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

/**
 * A component with a spin button, title text, min and
 * max ranges and trailing text.
 */
public class IntegerSpinner extends JPanel {
    /**
     * CTOR - create a spinner that displays an integer with specified min, max and step size, also allows setting
     * of leading text and trailing text and change listener.
     * @param leading_text leading text
     * @param value        initial value
     * @param min          minimum
     * @param max          maximum
     * @param step         increment / decrement size
     * @param trail_text   trailing text
     * @param listener     change listener, null = no listener supplied
     */
    public IntegerSpinner(String leading_text, int value, int min, int max, int step, String trail_text, ChangeListener listener) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        //_model = new SpinnerNumberModel(value, min, max, step);
        _model = new WrappingSpinnerNumberModel(value, min, max, step, true);
        if (leading_text.length() > 0) {
            JLabel lbl = new JLabel(leading_text);
            lbl.setOpaque(false);
            add(lbl);
        }
        add(_spinner = new JSpinner(_model));
        if (trail_text.length() > 0) {
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

    public int getValue() {
        return (Integer) _spinner.getValue();
    }

    public void setValue(int v) {
        _spinner.setValue(v);
    }

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

    private static class WrappingSpinnerNumberModel extends SpinnerNumberModel {
        private static final long serialVersionUID = 2057392549604932746L;
        private int value, min, max, step;
        private boolean cycle;

        public WrappingSpinnerNumberModel(int _value, int _min, int _max, int _step, boolean _cycle) {
            super(_value, _min, _max, _step);
            value = _value;
            min = _min;
            max = _max;
            step = _step;
            cycle = _cycle;
        }

        @Override
        public void setMaximum(Comparable _max) {
            super.setMaximum(_max);
            max = (Integer) _max;
        }

        @Override
        public void setMinimum(Comparable _min) {
            super.setMinimum(_min);
            min = (Integer) _min;
        }

        @Override
        public void setValue(Object _val) {
            super.setValue(_val);
            value = (Integer) _val;
        }


        @Override
        public Object getNextValue() {
            value += step;
            if (value > max)
                value = cycle ? min : max;
            return Integer.valueOf(value);
        }

        @Override
        public Object getPreviousValue() {
            value -= step;
            if (value < min)
                value = cycle ? max : min;
            return Integer.valueOf(value);
        }
    }

    //instance variables
    private JSpinner _spinner;
    private SpinnerNumberModel _model;
}