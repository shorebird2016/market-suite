package org.marketsuite.component.field;

import java.text.DecimalFormat;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Extension of TextField to implement editor specifically for editing a decimal number.
 * The value will be set to the last known good value when entry is out of range.
 */
public class DecimalField extends JTextField {
	/**
     * CTOR; create a text field with initial value, fix width and limit ranges.
     * @param value initial value
     * @param columns width in # of characters
     * @param lower_limit lower bound inclusive
     * @param upper_limit upper bound inclusive
     * @param listener field change
     */
    public DecimalField(double value, int columns,
           double lower_limit, double upper_limit, DocumentListener listener) {
        super(columns);
        setHorizontalAlignment(JTextField.CENTER);
        setValue(value);
        _dDefaultValue = value;
        _dLowerBound = lower_limit;
        _dUpperBound = upper_limit;
        setInputVerifier(new RangeVerifier());
        getDocument().addDocumentListener(listener);
    }

    public double getValue() throws NumberFormatException {
       try {
          double val = Double.parseDouble(getText());
          if (val < _dLowerBound) {//under-range
             setText(String.valueOf(_dLowerBound)); //make min
             return _dLowerBound;
          }
          else if (val > _dUpperBound) {//over-range
             setText(String.valueOf(_dUpperBound)); //make max
             return _dUpperBound;
          }
          else
              return val;
      }catch(NumberFormatException nfe) { //bad entry, set to low
         setText(String.valueOf(_dLowerBound));
          return _dLowerBound;
      }
    }

    //only two decimal places allowed
    public void setValue(double value) {
        DecimalFormat df = new DecimalFormat("##.##");
        setText(df.format(value));
    }

    protected Document createDefaultModel() {
        return new NumberDocument();
    }

    /**
     * A document that only allows digits, decimal and scientific notations.
     */
    private class NumberDocument extends PlainDocument {
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
//            int numDots = 0;
//            int numE = 0;
//            String txt = getText(0, getLength()).toLowerCase();
//            if (txt.indexOf('.') >= 0) numDots = 1;
//            if (txt.indexOf('e') >= 0) numE = 1;
//            char[] source = str.toCharArray();
//            char[] result = new char[source.length];
//            int j = 0;
//            for (int i = 0; i < result.length; i++) {
//                char ch = Character.toLowerCase(source[i]);
//                if (Character.isDigit(ch))
//                    result[j++] = source[i];
//                else if (ch == '.' && numDots == 0) {
//                    result[j++] = ch;
//                    numDots = 1;
//                }
//                else if (ch == 'e' && numE == 0) {
//                    result[j++] = ch;
//                    numE = 1;
//                }
//            }
            String orig = getText(0,getLength());
            super.insertString(offs, str, a);
//            super.insertString(offset, str, a);
            String s2 = getText(0, getLength());
            try {
                double val = Double.parseDouble(s2);
                if (val > _dUpperBound) {
                    super.replace(0, getLength(), orig, a);
                }
            } catch (NumberFormatException ex) {
                super.replace(0, getLength(), orig, a);
            }
        }
    }

    /**
     * An input verifier to limit data entry ranges. For bad entry
     * or out of range values, set to lower bound.
     */
    private class RangeVerifier extends InputVerifier {
        public boolean verify(JComponent comp) {
            DecimalField field = (DecimalField)comp;
            double val = field.getValue();
            if (val < _dLowerBound || val > _dUpperBound) {
                field.setValue(_dDefaultValue);
                return false;
            }
            return true;
        }
    }

    //instance variables
    private double _dLowerBound;
    private double _dUpperBound;
    private double _dDefaultValue;
}