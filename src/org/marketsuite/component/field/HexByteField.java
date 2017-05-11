package org.marketsuite.component.field;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.marketsuite.component.util.CoreUtil;

/**
 * Extension of TextField to implement editor specifically for hex entry 00-FF.
 * The value will be set to the last known good value when entry is out of range.
 */
public class HexByteField extends JTextField {
	/**
     * CTOR; create a text field with initial value.
     * @param initial_value initial value(also default)
     * @param even_only true = only allow even bytes
     */
    public HexByteField(String initial_value, boolean even_only) {
        _sInitialValue = initial_value;
        _bEvenOnly = even_only;
        setInputVerifier(new HexByteVerifier());
        setText(initial_value);
    }

    public HexByteField(String initial_value, int columns, boolean even_only) {
        super(columns);
        _sInitialValue = initial_value;
        _bEvenOnly = even_only;
        setInputVerifier(new HexByteVerifier());
        setText(initial_value);
    }

    //interface implementation, overides
    protected Document createDefaultModel() {
        return new HexByteDocument();
    }

    //public methods
    public void setText(String value) {
        super.setText(value);
        _sInitialValue = value;
    }

    //inner classes
    /**
     * Special model for IP address entry. It filters out illegal characters and format
     * into standard XX.XX.XX.XX format.
     */
    static private class HexByteDocument extends PlainDocument {
        /**
		 * 
		 */
		private static final long serialVersionUID = -4604879168023099117L;

		/**
         * When new string is entered by user, only allow digits to get thru. Other characters
         * are ignored. This method is called on every character insert.
         * @param offset the starting offset >= 0
         * @param str the string to insert; does nothing with null/empty strings
         * @param a the attributes for the inserted content
         * @exception javax.swing.text.BadLocationException  the given insert position is not a valid
         *   position within the document
         */
        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            //disallow more than two characters
            if (getLength() == 2)
                return;

            str = str.trim();
            char[] src = str.toCharArray();
            char[] dest = new char[src.length];
            int dest_idx = 0;

            //remove illegal characters
            for (int src_idx = 0; src_idx < src.length; src_idx++) {
                char c = src[src_idx];
                if (Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
                    if (c >= 'a' && c <= 'f')
                        c = Character.toUpperCase(c);
                    dest[dest_idx++] = c;
                }
            }
            super.insertString(offset, new String(dest, 0, dest_idx), a);
        }
    }

    /**
     * An input verifier that validate entry values of this field.
     * If any octet has bad value, last good known value is restored.
     */
    private class HexByteVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JTextField field = (JTextField)input;
            String text = field.getText();
            if (text.length() > 2) {
                setText(_sInitialValue);//restore if bad
                return false;
            }

            //if value is odd, warn user
            int val = CoreUtil.hexToNumber(text.getBytes());
            if (_bEvenOnly && val % 2 != 0) {
                setText(_sInitialValue);//restore if bad
                return false;
            }
            return true;
        }
    }

    //instance variables
    private String _sInitialValue;//last known good vlaue
    private boolean _bEvenOnly;
}