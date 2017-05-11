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
 * Extension of TextField to implement editor specifically for IP addresses range separated by dash character.
 * The value will be set to the last known good value when entry is out of range.
 */
public class MacAddressRangeField extends JTextField {
	/**
     * CTOR; create a text field with initial value.
     * @param initial_value initial value(also default)
     */
    public MacAddressRangeField(String initial_value) {
        _sInitialValue = initial_value;
        setInputVerifier(new MacAddressRangeVerifier());
    }

    //interface implementation, overides
    protected Document createDefaultModel() {
        return new MacAddressRangeDocument();
    }

    public void setText(String value) {
        super.setText(value);
        _sInitialValue = value;
    }

    //inner classes
    /**
     * Special model for IP address entry. It filters out illegal characters and format
     * into standard XX.XX.XX.XX format.
     */
    static private class MacAddressRangeDocument extends PlainDocument {
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
        	str = str.trim();
            char[] src = str.toCharArray();
            char[] dest = new char[src.length];
            int dest_idx = 0;

            //remove illegal characters (only hex and colon allowed)
            for (int src_idx = 0; src_idx < src.length; src_idx++) {
                char c = src[src_idx];
                //convert lower case to upper case
                if (c >= 0x61 && c <= 0x66)
                    c -= 0x20;//convert to upper case
                if ((c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x46) || c == ':' || c == '-')
                    dest[dest_idx++] = c;
            }
            super.insertString(offset, new String(dest, 0, dest_idx), a);
        }
    }

    /**
     * An input verifier that validate entry values of this field.
     * If any octet has bad value, last good known value is restored.
     */
    private class MacAddressRangeVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JTextField field = (JTextField)input;
            String range = field.getText();
            //split range
            int dash_pos = range.indexOf("-");
            if (dash_pos == -1) {//test single address
                if (!CoreUtil.isValidMacAddress(range)) {
                    MacAddressRangeField.this.setText(_sInitialValue);
                    return false;
                }

                if (CoreUtil.isSpecialMacAddress(range)) {
                    MacAddressRangeField.this.setText(_sInitialValue);
                    return false;
                }
            }
            else {//test address range
                String start = range.substring(0, dash_pos);
                if (dash_pos + 1 >= range.length()) {//prevent '-' at end of string
                    MacAddressRangeField.this.setText(_sInitialValue);
                    return false;
                }

                String end = range.substring(dash_pos + 1, range.length());
                if (!CoreUtil.isValidMacAddress(start) && !CoreUtil.isValidMacAddress(end)) {
                    MacAddressRangeField.this.setText(_sInitialValue);
                    return false;
                }

                if (CoreUtil.isSpecialMacAddress(start) || CoreUtil.isSpecialMacAddress(end)) {
                    MacAddressRangeField.this.setText(_sInitialValue);
                    return false;
                }

                //check end smaller than start
                long sa = CoreUtil.macBytesToNumber(CoreUtil.macAddressToBytes(start));
                long ea = CoreUtil.macBytesToNumber(CoreUtil.macAddressToBytes(end));
                if (ea < sa) {
                    MacAddressRangeField.this.setText(_sInitialValue);
                    return false;
                }
            }
            return true;
        }
    }

    //instance variables
    private String _sInitialValue;//last known good vlaue
}