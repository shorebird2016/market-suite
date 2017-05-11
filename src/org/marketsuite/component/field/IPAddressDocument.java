package org.marketsuite.component.field;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public abstract class IPAddressDocument extends PlainDocument {
   private static final long serialVersionUID = -2158297266989863466L;
   
   //instance variables
   protected int[] start;
   protected String[] res;
   protected byte separator;
   protected int numFields;
   protected IPAddressField field;
   protected boolean multi = false;
   
   //interface implementation
   public void remove(int offset, int len) {
      try {
         String txt = getText(0, getLength());
         byte[] txtBytes = txt.getBytes();
         super.remove(0, txtBytes.length);
         byte[] newBytes = new byte[txtBytes.length];

         int i = 0;
         int j = 0;
         while (i < offset)
            newBytes[j++] = txtBytes[i++];
         for (int k = 0; k < len; k++) {
            if (txtBytes[i++] == separator)
               newBytes[j++] = separator;
         }
         while (i < txtBytes.length)
            newBytes[j++] = txtBytes[i++];

         String newString = new String(newBytes, 0, j);
         super.insertString(0, newString, null);
         field.getCaret().setDot(offset);

         getRes(newBytes, j);
      }
      catch (Exception ex) {
         System.out.println("remove: " + ex);
         ex.printStackTrace();
      }
   }

   protected void getRes(byte[] s, int length) {
      int p = 0;
      int len = 0;
      int offset = 0;
      for (int i = 0; i < length; i++) {
         if (s[i] == separator) {
            res[p++] = new String(s, offset, len);
            if (p > res.length)
               return;
            len = 0;
            offset = i + 1;
         } else
            len++;
      }
      res[p] = new String(s, offset, len);
   }

   protected int getPos(int offset, byte[] s) {
      int returnPos = 0;
      int pos = 0;
      for (int i = 0; i < s.length; i++) {
         if (s[i] == separator) {
            start[++pos] = i + 1;
            if (i < offset)
               returnPos = pos;
         }
      }
      return returnPos;
   }

   protected String insertDigit(byte[] orig, byte digit, int insertPos) {
      byte[] result = new byte[orig.length + 1];
      int i = 0;
      int j = 0;
      for (; i < insertPos; i++)
         result[i] = orig[j++];

      result[i++] = digit;

      for (; j < orig.length; j++)
         result[i++] = orig[j];
      return new String(result);
   }

   protected boolean isDecimalDigit(byte b) {
      if (b >= '0' && b <= '9')
         return true;
      return false;
   }
   
   protected boolean isHexDigit(byte b) {
      if (b >= '0' && b <= '9')
         return true;
      if (b >= 'a' && b <= 'f')
         return true;
      if (b >= 'A' && b <= 'F')
         return true;
      return false;
   }

   public static class V4 extends IPAddressDocument {
      private static int LAST_FIELD = 3;
      private static int NUM_FIELDS = 4;
      private static int MAX_DIGITS = 3;  // max in each field
      //CTOR
      public V4(IPAddressField f) {
         start = new int[NUM_FIELDS];
         res = new String[NUM_FIELDS];
         separator = '.';
         field = f;
         try {
            super.insertString(0, "...", null);
         }
         catch (Exception ex) {
            System.out.println(ex);
         }
         for (int i = 0; i < NUM_FIELDS; i++)
            res[i] = "";
      }


      public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
         if (str.length() > 1) { // Probably from a paste operation
            multi = true;
            str = str.trim();
            // call insertString repeatedly with one character at a time
            for (int i = 0; i < str.length(); i++) {
               int dot = field.getCaret().getDot();
               insertString(dot, str.substring(i, i + 1), a);
            }
            multi = false;
            return;
         }
         String txt = getText(0, getLength());
         int pos = getPos(offset, txt.getBytes());
         
         byte b3 = str.getBytes()[0];
         if ( b3 == separator) {
            if(multi || (res[pos].length() > 0)) { // don't advance if the field is blank
               if (pos < LAST_FIELD)
                  field.getCaret().setDot(start[pos + 1]);
            }
            return;
         }
         
         if (b3 == ' ') {
            if (pos < LAST_FIELD)
               field.getCaret().setDot(start[pos + 1]);
            return;
         }
         if (!isDecimalDigit(b3))
            return;

         int insertPos = offset - start[pos];
         byte[] b1 = res[pos].getBytes();

         String str2 = insertDigit(b1, b3, insertPos);
         int val = Integer.parseInt(str2);
         if ( b1.length < MAX_DIGITS && val <= 255) {
            res[pos] = str2;
            super.insertString(offset, str, a);
            if (((res[pos].length() == MAX_DIGITS) || (val > 25)) && pos < LAST_FIELD && !multi) {
               field.getCaret().setDot(start[pos + 1] + 1);
            } else
               field.getCaret().setDot(offset + 1);
         } 
         else if (pos < LAST_FIELD)
            // insert in the next field
            insertString(offset + 1, str, a);
         else
            field.getCaret().setDot(offset);
      }

      public String[] getOctets() {
         return res;
      }

      //literals
      public static final long serialVersionUID = -1L;
   }

   public static class V6 extends IPAddressDocument {
      public static final long serialVersionUID = -1L;
      private static int LAST_FIELD = 7;
      private static int NUM_FIELDS = 8;
      private static int MAX_DIGITS = 4;  // max in each field

      public V6(IPAddressField f) {
         res = new String[NUM_FIELDS];
         start = new int[NUM_FIELDS];
         separator = ':';
         field = f;
         try {
            super.insertString(0, ":::::::", null);
         }
         catch (Exception ex) {
            //
         }
         for (int i = 0; i < NUM_FIELDS; i++)
            res[i] = "";
      }


      public void insertString(int offset, String str, AttributeSet a) {
         if (str.length() > 1) {
            multi = true;
            str = str.trim();
            for (int i = 0; i < str.length(); i++) {
               int dot = field.getCaret().getDot();
               insertString(dot, str.substring(i, i + 1), a);
            }
            multi = false;
            return;
         }

         try {
            String txt = getText(0, getLength());
            int pos = getPos(offset, txt.getBytes());

            byte b3 = str.getBytes()[0];
            if ( b3 == separator) {
               if(multi || (res[pos].length() > 0)) { // don't advance if the field is blank
                  if (pos < LAST_FIELD)
                     field.getCaret().setDot(start[pos + 1]);
               }
               return;
            }

            if (b3 == ' ') {
               if (pos < LAST_FIELD)
                  field.getCaret().setDot(start[pos + 1]);
               return;
            }
            if (!isHexDigit(b3))
               return;

            int insertPos = offset - start[pos];
            byte[] b1 = res[pos].getBytes();
            if (b1.length < MAX_DIGITS) {
               res[pos] = insertDigit(b1, b3, insertPos);
               super.insertString(offset, str, a);
               if (res[pos].length() == MAX_DIGITS && pos < LAST_FIELD && !multi) {
                  field.getCaret().setDot(start[pos + 1] + 1);
               } else {
                  field.getCaret().setDot(offset + 1);
               }
            } else if (pos < LAST_FIELD) {
               insertString(offset + 1, str, a);
            } else
               field.getCaret().setDot(offset);
         }
         catch (Exception ex) {
            System.out.println("ex1: " + ex);
            ex.printStackTrace();
         }
      }
   }

   public static class MAC extends IPAddressDocument {
      public static final long serialVersionUID = -1L;
      private static int LAST_FIELD = 5;
      private static int NUM_FIELDS = 6;
      private static int MAX_DIGITS = 2;  // max in each field

      public MAC(IPAddressField f) {
         res = new String[NUM_FIELDS];
         start = new int[NUM_FIELDS];
         separator = ':';
         field = f;
         try {
            super.insertString(0, ":::::", null);
         }
         catch (Exception ex) {
            //
         }
         for (int i = 0; i < NUM_FIELDS; i++)
            res[i] = "";
      }

      public void insertString(int offset, String str, AttributeSet a) {
         if (str.length() > 1) {
            multi = true;
            str = str.trim();
            for (int i = 0; i < str.length(); i++) {
               int dot = field.getCaret().getDot();
               insertString(dot, str.substring(i, i + 1), a);
            }
            multi = false;
            return;
         }

         try {
            String txt = getText(0, getLength());
            int pos = getPos(offset, txt.getBytes());

            byte b3 = str.getBytes()[0];
            if ( b3 == separator) {
               if(multi || (res[pos].length() > 0)) { // don't advance if the field is blank
                  if (pos < LAST_FIELD)
                     field.getCaret().setDot(start[pos + 1]);
               }
               return;
            }

            if (b3 == ' ') {
               if (pos < LAST_FIELD)
                  field.getCaret().setDot(start[pos + 1]);
               return;
            }

            if (!isHexDigit(b3))
               return;

            int insertPos = offset - start[pos];
            byte[] b1 = res[pos].getBytes();
            if (b1.length < MAX_DIGITS) {
               res[pos] = insertDigit(b1, b3, insertPos);
               super.insertString(offset, str, a);
               if (res[pos].length() == MAX_DIGITS && pos < LAST_FIELD && !multi) {
                  field.getCaret().setDot(start[pos + 1] + 1);
               } else {
                  field.getCaret().setDot(offset + 1);
               }
            } else if (pos < LAST_FIELD) {
               insertString(offset + 1, str, a);
            } else
               field.getCaret().setDot(offset);
         }
         catch (Exception ex) {
            System.out.println("ex1: " + ex);
            ex.printStackTrace();
         }
      }
   }
}