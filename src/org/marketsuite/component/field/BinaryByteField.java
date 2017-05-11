package org.marketsuite.component.field;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Extension of TextField to implement editor specifically for editing 8 bit binary number.
 * The value will be set to the last known good value when entry is out of range.
 */
public class BinaryByteField extends JTextField {
    /**
	 * 
	 */
	private static final long serialVersionUID = -518771607483210459L;

	/**
     * CTOR; create a text field with initial value.
     * @param initial_value initial value(also default)
     */
    public BinaryByteField(String initial_value) {
        _sInitialValue = initial_value;
        setInputVerifier(new BinaryByteVerifier());
        setText(initial_value);
    }

    public BinaryByteField(String initial_value, int columns) {
        super(columns);
        _sInitialValue = initial_value;
        setInputVerifier(new BinaryByteVerifier());
        setText(initial_value);
    }

    //interface implementation, overides
    protected Document createDefaultModel() {
        return new BinaryByteDocument();
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
    static private class BinaryByteDocument extends PlainDocument {
        /**
		 * 
		 */
		private static final long serialVersionUID = -8889432341251542118L;

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
            //disallow more than 8 characters
            if (getLength() == 8)
                return;

            str = str.trim();
            char[] src = str.toCharArray();
            char[] dest = new char[src.length];
            int dest_idx = 0;

            //remove illegal characters
            for (int src_idx = 0; src_idx < src.length; src_idx++) {
                char c = src[src_idx];
                if (c >= '0' && c <= '1')
                    dest[dest_idx++] = c;
            }
            super.insertString(offset, new String(dest, 0, dest_idx), a);
        }
    }

    /**
     * An input verifier that validate entry values of this field.
     * If any octet has bad value, last good known value is restored.
     */
    private class BinaryByteVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JTextField field = (JTextField)input;
            String text = field.getText();
            if (text.length() > 8) {
//                Commander.getTopLevelFrame().setStatusBarText(WARNING_1);
                setText(_sInitialValue);//restore if bad
                return false;
            }

//            Commander.getTopLevelFrame().setStatusBarText(" ");
            return true;
        }
    }

    //instance variables
    private String _sInitialValue;//last known good vlaue
}