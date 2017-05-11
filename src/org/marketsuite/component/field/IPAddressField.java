package org.marketsuite.component.field;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

/**
 * Extension of TextField to implement editor specifically for editing IPv4 addresses.
 * The value will be set to the last known good value when entry is out of range.
 */
public abstract class IPAddressField extends JTextField {
    //CTOR
    public IPAddressField(String text) {
        this(null, text, 0);
    }

    public IPAddressField(String text, int columns) {
        this(null, text, columns);
    }

    public IPAddressField(Document doc, String text, int columns) {
        super(doc, text, columns);
        /*
        if(font == null) {
           try {
              font = Font.createFont(Font.ROMAN_BASELINE,
                                     Object.class.getResourceAsStream("/com/clec/cap/resource/font/LucidaTypewriterRegular.ttf"));
              font = font.deriveFont(Font.PLAIN,10);
           }catch(Exception ex) {
              ex.printStackTrace();
           }
        }
        */
        _sLastValidEntry = text;
        //setFont(Constants.CELL_FONT);
        setFont(font);
        //setInputVerifier(new IPAddressVerifier());
        //InputMap map = dummy.getInputMap(JComponent.WHEN_FOCUSED);
        setInputMap(JComponent.WHEN_FOCUSED, map);
        setColumns(24);
    }

    //optionally turn on/off verifier
    public IPAddressField(Document doc, String text, int column, boolean verifier_on) {
        this(doc, text, column);
        IPAddressVerifier v = verifier_on ? new IPAddressVerifier() : null;
        setInputVerifier(v);
    }

    //interface
    public abstract int getFieldWidth();

    public abstract int getNumFields();

    public abstract char getSeparator();

    //public methods
    public void updateUI() {
        setUI(new IPAddressFieldUI());
    }

    private class IPAddressVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            JTextField field = (JTextField) input;
            String ip = field.getText();
            if (ip == null || ip.equals("")) {
                field.setText(_sLastValidEntry);
                return false;
            }
            return true;
        }
    }

    public static class V4 extends IPAddressField {
        /**
         *
         */
        private static final long serialVersionUID = -8460108908266104010L;

        //CTOR
        public V4(String text) {
            super(null, text, 0);
            setDocument(new IPAddressDocument.V4(this));
        }

        //CTOR: with or without verifier
        public V4(String text, boolean verifier_on) {
            super(null, text, 0, verifier_on);
            setDocument(new IPAddressDocument.V4(this));
        }

        public V4(String text, int columns) {
            super(null, text, columns);
            setDocument(new IPAddressDocument.V4(this));
        }

        public V4(Document doc, String text, int columns) {
            super(doc, text, columns);
            setDocument(new IPAddressDocument.V4(this));
        }

        public String getText() {
            String txt = super.getText();
            if (txt.length() == 3)
                return "";
            return txt;
        }

        public int getFieldWidth() {
            return 3;
        }

        public int getNumFields() { return 4; }

        public char getSeparator() {
            return '.';
        }

        //least significant octet, -1=empty
        public int getOctet4() {
            String txt = getText();
            if ("".equals(txt))
                return -1;

            int pos = txt.lastIndexOf(getSeparator());
            String ot = txt.substring(pos + 1, txt.length());
            return Integer.parseInt(ot);
        }

        //to get all octets in an array, null=empty field
        public int[] getOctets() {
            String[] octets = ((IPAddressDocument.V4) getDocument()).getOctets();
            int[] ret = new int[octets.length];
            for (int i = 0; i < octets.length; i++)
                if (octets[i].equals(""))
                    ret[i] = -1;
                else
                    ret[i] = Integer.parseInt(octets[i]);
            return ret;
        }

//        public static final long serialVersionUID = -1L;
    }

    public static class V6 extends IPAddressField {
        public static final long serialVersionUID = -1L;

        public V6() {
            super(null, null, 0);
            setDocument(new IPAddressDocument.V6(this));
        }

        public V6(String text) {
            super(null, text, 0);
            setDocument(new IPAddressDocument.V6(this));
        }

        public V6(int columns) {
            super(null, null, columns);
            setDocument(new IPAddressDocument.V6(this));
        }

        public V6(String text, int columns) {
            super(null, text, columns);
            setDocument(new IPAddressDocument.V6(this));
        }

        public V6(Document doc, String text, int columns) {
            super(doc, text, columns);
            setDocument(new IPAddressDocument.V6(this));
        }

        public String getText() {
            String txt = super.getText();
            if (txt.length() == 7)
                return "";
            return txt;
        }

        public int getFieldWidth() {
            return 4;
        }

        public int getNumFields() { return 8; }

        public char getSeparator() {
            return ':';
        }
    }

    public static class MAC extends IPAddressField {
        public static final long serialVersionUID = -1L;

        public MAC() {
            super(null, null, 0);
            setDocument(new IPAddressDocument.MAC(this));
        }

        public MAC(String text) {
            super(null, text, 0);
            setDocument(new IPAddressDocument.MAC(this));
        }

        public MAC(int columns) {
            super(null, null, columns);
            setDocument(new IPAddressDocument.MAC(this));
        }

        public MAC(String text, int columns) {
            super(null, text, columns);
            setDocument(new IPAddressDocument.MAC(this));
        }

        public MAC(Document doc, String text, int columns) {
            super(doc, text, columns);
            setDocument(new IPAddressDocument.MAC(this));
        }

        public String getText() {
            String txt = super.getText();
            if (txt.length() == 5)
                return "";
            return txt;
        }

        public int getFieldWidth() {
            return 2;
        }

        public int getNumFields() { return 6; }

        public char getSeparator() {
            return ':';
        }
    }

    //instance variables
    private String _sLastValidEntry;//last known good value
    static Font font;
    static JTextField dummy;
    static InputMap map;

    static {
        font = new Font("Lucida Sans Typewriter", Font.PLAIN, 10);
        if (!font.getFamily().equals("Lucida Sans Typewriter")) {
            font = new Font("Lucida Console", Font.PLAIN, 12);
            if (!font.getFamily().equals("Lucida Console"))
                font = new Font("Monospaced", Font.PLAIN, 12);
        }
        //System.out.println("font: " + font);
        dummy = new JTextField();
        map = dummy.getInputMap(JComponent.WHEN_FOCUSED);
    }
}
