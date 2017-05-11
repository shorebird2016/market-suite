package org.marketsuite.component.field;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Extension of TextField to restrict entry characters to 0..9.
 * The value will be set to the last known good value when entry
 * is out of range.
 */
public class LongIntegerField extends JTextField {//todo: make it work for negative integers

	/**
     * CTOR; create a text field with initial value, fix width and limit ranges. Default with built-in formatter.
     * @param default_value initial value(also default)
     * @param columns width in # of characters
     * @param lower_limit lower bound inclusive
     * @param upper_limit upper bound inclusive
     */
    public LongIntegerField(long default_value, int columns, long lower_limit, long upper_limit) {
        this(default_value, columns, lower_limit, upper_limit, false);
    }

    /**
     * Construct this object without default formatting
     * @param default_value initial value(also default)
     * @param columns width in # of characters
     * @param lower_limit lower bound inclusive
     * @param upper_limit upper bound inclusive
     * @param use_format true to use built-in formatter
     */
    public LongIntegerField(long default_value, int columns, long lower_limit, long upper_limit, boolean use_format) {
    	super(columns);
        setHorizontalAlignment(JTextField.CENTER);
        if (use_format){
            _formatter  = NumberFormat.getNumberInstance();
            _formatter.setParseIntegerOnly(true);
        }
        _nDefaultValue = default_value;
        _nLowerBound = lower_limit;
        _nUpperBound = upper_limit;
        setValue(default_value);
        setInputVerifier(new RangeVerifier());
    }

    //public methods
    public long getValue() {
        long retVal = _nDefaultValue;
        try {
            if (_formatter != null)
                retVal = _formatter.parse(getText()).longValue();
            else
                retVal = Long.parseLong(getText());
        } catch (ParseException pe) {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.
            //toolkit.beep();
        }catch (NumberFormatException pe) {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.
            //toolkit.beep();
        }
        return retVal;
    }

    public void setValue(long value) {
        if (_formatter != null)
            setText(_formatter.format(value));
        else
            setText(String.valueOf(value));
        _nDefaultValue = value;
    }

    public long getFieldValue() {
        return _value;
    }

    /**
     * Set new formatter. Default is NumberFormat.getNumberInstance()
     * @param formatter object, null means no formatter
     */
    public void setFormatter(NumberFormat formatter) {
        _formatter = formatter;
    }

    protected Document createDefaultModel() {
        return new IntegerDocument();
    }

    //inner classes
    private class IntegerDocument extends PlainDocument {
		/**
         * When new string is entered by user, only allow digits to get thru. Other characters
         * are ignored.
         * @param offset into the string
         * @param str input
         * @param a
         */
        public void insertString(int offset, String str, AttributeSet a)
                throws BadLocationException {
           String orig = getText(0,getLength());
           /*
            char[] source = str.toCharArray();
            char[] result = new char[source.length];
            int j = 0;

            //remove illegal characters
            for (int i = 0; i < result.length; i++) {//todo: can I use regex??.............
                if (Character.isDigit(source[i]))
                    result[j++] = source[i];
            }
            super.insertString(offset, new String(result, 0, j), a);
            */
           super.insertString(offset, str, a);
			String s2 = getText(0, getLength());
			try {
				long val = 0;
				if (_formatter != null)
					val = _formatter.parse(s2).longValue();
				else
					val = Long.parseLong(s2);

				if (val > _nUpperBound) {
					super.replace(0, getLength(), orig, a);
				}
			} catch (NumberFormatException ex) {
				super.replace(0, getLength(), orig, a);
			} catch (ParseException e) {
				super.replace(0, getLength(), orig, a);
			}
		}
    }

    /**
     * An input verifier to limit data entry ranges.
     * When out of range is entered, last known good value is set.
     */
    private class RangeVerifier extends InputVerifier {
       public boolean verify(JComponent comp) {
           LongIntegerField field = (LongIntegerField)comp;
           long val = field.getValue();
           _value = val;
           if (val < _nLowerBound || val > _nUpperBound) {
              field.setValue(_nDefaultValue);
              return false;
           }
           field.setValue(val);
           return true;
       }
    }

    //accessor / mutators
    public void setMin(long min) {
        _nLowerBound = min;
        if (getValue() < min)
           setValue(min);
    }

    public void setMax(long max) {
        _nUpperBound = max;
        if (getValue() > max)
           setValue(max);
    }

    //instance variables
    private NumberFormat _formatter = null;
    private long _nLowerBound;
    private long _nUpperBound;
    private long _nDefaultValue;
    private long _value;
}

