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
 * Extension of TextField to implement editor specifically for MAC addresses.
 * The value will be set to the last known good value when user entry is illegal.
 */
public class MacAddressField extends JTextField {
	/**
     * CTOR; create a text field with initial value.
     * @param default_value initial value(also default)
     */
    public MacAddressField(String default_value) {
        super(default_value, 18);
        _sDefaultValue = default_value;
        setInputVerifier(new MacAddressVerifier());
    }

    //interface implementation, overides
    protected Document createDefaultModel() {
        return new MacAddressDocument();
    }

    public void setText(String value) {
        super.setText(value);
        _sDefaultValue = value;
    }

    //inner classes
    /**
     * Special model for IP address entry. It filters out illegal characters and format
     * into standard XX:XX:XX:XX:XX:XX format.
     */
    static private class MacAddressDocument extends PlainDocument {
		/**
         * When new string is entered by user, only allow digits and colon char to get thru.
         * Other characters are ignored. This method is called on every character insert.
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
                    c -= 0x20;
                if ((c >= 0x30 && c <= 0x39) || (c >= 0x41 && c <= 0x46) || c == ':')
                    dest[dest_idx++] = c;
            }
            super.insertString(offset, new String(dest, 0, dest_idx), a);
        }
        private static final long serialVersionUID = 2080651523813795955L;
    }

    /**
     * An input verifier that validate entry values of this field.
     * If any octet has bad value, last good known value is restored.
     */
    private class MacAddressVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JTextField field = (JTextField)input;
            String mac = field.getText();
            if (mac == null || mac.equals("")) {
                field.setText(_sDefaultValue);
                return false;
            }
            else if (CoreUtil.isValidMacAddress(mac) && !CoreUtil.isSpecialMacAddress(mac)) {
                return true;
            }
            else {
                //restore to last known value
                MacAddressField.this.setText(_sDefaultValue);
                return false;
            }
        }
    }

    //instance variables
    private String _sDefaultValue;//last known good vlaue
}