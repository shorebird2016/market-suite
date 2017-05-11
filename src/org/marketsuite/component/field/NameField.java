package org.marketsuite.component.field;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Extension of TextField to restrict entry for names, only allowing alphanumeric, forward slash, dash and underscore characters
 */
public class NameField extends JTextField {
    /**
	 *
	 */
	private static final long serialVersionUID = -3271939162791365494L;

	public NameField() {
		//
	}

    //allow_blank = true for blanks in name
    public NameField(boolean allow_blank) {
        _bAllowBlanks = allow_blank;
    }

    public NameField(int column_width) {
        super(column_width);
    }

    public NameField(int column_width, boolean allow_blank) {
        super(column_width);
        _bAllowBlanks = allow_blank;
    }

    //public methods
    protected Document createDefaultModel() {
        return new NameDocument();
    }

    //inner classes
    private class NameDocument extends PlainDocument {
        /**
		 *
		 */
		private static final long serialVersionUID = -4089450897690476229L;

		/**
         * When new string is entered by user, only allow alphanumeric, '-', '_', '/ to pass
         * Other characters are ignored.
         * @param offset into the string
         * @param str input
         * @param a attribute
         */
        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            char[] source = str.toCharArray();
            char[] result = new char[source.length];
            int result_index = 0;

            //remove illegal characters
            for (int src_index = 0; src_index < result.length; src_index++) {
                boolean legal_keys = Character.isLetterOrDigit(source[src_index]) || source[src_index] == '-'
                        || source[src_index] == '_' || source[src_index] == '/' || source[src_index] == '+'
                        || source[src_index] == '@' || source[src_index] == '[' || source[src_index] == ']'
                        || source[src_index] == '#' || source[src_index] == '*';
                legal_keys |= _bAllowBlanks && source[src_index] == ' ';
                if (legal_keys)
                    result[result_index++] = source[src_index];
            }
            super.insertString(offset, new String(result, 0, result_index), a);
        }
    }

    //instance variables
    private boolean _bAllowBlanks;
}