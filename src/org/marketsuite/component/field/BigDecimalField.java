package org.marketsuite.component.field;

import java.math.BigDecimal;
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
 * A widget used to preserve data accuracy as opposed to DecimalField inherently uses double type and subject
 * to truncation problems.  Like all XXXField, it uses text field with storage of BigDecimal type.
 */
public class BigDecimalField extends JTextField {
    /**
     * CTOR; create a text field with initial value, fix width and limit ranges.
     *
     * @param value       initial value
     * @param columns     width in # of characters
     * @param lower_limit lower bound inclusive
     * @param upper_limit upper bound inclusive
     * @param listener    field change, ignored if null
     */
    public BigDecimalField(double value, int columns,
                           double lower_limit, double upper_limit, DocumentListener listener) {
        super(columns);
        setValue(new BigDecimal(value));
        lowerBound = new BigDecimal(lower_limit);
        upperBound = new BigDecimal(upper_limit);
        setInputVerifier(new RangeVerifier());
        if (listener != null)
            getDocument().addDocumentListener(listener);
    }

    public BigDecimal getValue() throws NumberFormatException {
        try {
            BigDecimal val = new BigDecimal(getText());
            if (val.compareTo(lowerBound) < 0) {//under-range
                setText(FORMAT.format(lowerBound)); //make min
                return lowerBound;
            }
            else if (val.compareTo(upperBound) > 0) {//over-range
                setText(FORMAT.format(upperBound)); //make max
                return upperBound;
            }
            else
                return val;
        } catch (NumberFormatException nfe) { //bad entry, set to low
            setText(FORMAT.format(lowerBound));
            return lowerBound;
        }
    }

    //only two decimal places allowed
    public void setValue(BigDecimal value) {
        setText(FORMAT.format(value));
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
            String orig = getText(0, getLength());
            super.insertString(offs, str, a);
//            super.insertString(offset, str, a);
            String s2 = getText(0, getLength());
            try {
                BigDecimal val = new BigDecimal(s2);
                if (val.compareTo(upperBound) > 0) //over range, set to 0 todo????????????????????????
                    super.replace(0, getLength(), orig, a);
            } catch (NumberFormatException ex) {
                super.replace(0, getLength(), orig, a);
            }
        }
        private static final long serialVersionUID = 6017142843365509577L;
    }

    /**
     * An input verifier to limit data entry ranges. For bad entry
     * or out of range values, set to lower bound.
     */
    private class RangeVerifier extends InputVerifier {
        public boolean verify(JComponent comp) {
            BigDecimalField field = (BigDecimalField)comp;
            BigDecimal val = field.getValue();
            if (val.compareTo(lowerBound) < 0 || val.compareTo(upperBound) > 0) {
                field.setValue(lowerBound);
                return false;
            }
            return true;
        }
    }

    //instance variables
    private BigDecimal lowerBound = new BigDecimal(0);
    private BigDecimal upperBound = new BigDecimal(100);
    private static final DecimalFormat FORMAT = new DecimalFormat("##.##");
    private static final long serialVersionUID = -7410624032578321468L;
}