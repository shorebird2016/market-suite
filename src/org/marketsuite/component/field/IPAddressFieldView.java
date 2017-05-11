package org.marketsuite.component.field;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.FieldView;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

class IPAddressFieldView extends FieldView {
   //CTOR
   public IPAddressFieldView(Element elem, IPAddressField ed) {
      super(elem);
      
      editor = ed;
      fieldWidth = ed.getFieldWidth();
      separator = ed.getSeparator();
      numFields = ed.getNumFields();

      formatSize = numFields * (fieldWidth + pad + pad) + numFields - 1;

      this.contentBuff = new Segment();
      //this.measureBuff = new Segment();
      this.workBuff = new Segment();
      this.element = elem;

      formattedContent = new char[formatSize];
      contentBuff.offset = 0;
      contentBuff.count = formatSize;
      contentBuff.array = formattedContent;

      offsets = new int[16];
      offsets[0] = pad;
      for (int i = 1; i < 16; i++) {
         offsets[i] = offsets[i - 1] + fieldWidth + pad * 2 + 1;
         // System.out.println("offsets[" + i + "] " + offsets[i]);
      }

      if (editor.isEditable())
         createContent(); // Update content string
   }

   // View methods start here
   public float getPreferredSpan(int axis) {
      //int widthFormat;
      int widthContent;

      if (formatSize == 0 || axis == View.Y_AXIS) {
         return super.getPreferredSpan(axis);
      }

      //widthFormat = Utilities.getTabbedTextWidth(measureBuff, getFontMetrics(), 0, this, 0);
      widthContent = Utilities.getTabbedTextWidth(contentBuff, getFontMetrics(), 0, this, 0);

      //return Math.max(widthFormat, widthContent);
      return widthContent;
   }

   public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
      if (!editor.isEditable()) {
         return super.modelToView(pos, a, b);
      }

      int offset = 0;
      a = adjustAllocation(a);
      Rectangle r = new Rectangle(a.getBounds());
      FontMetrics fm = getFontMetrics();
      r.height = fm.getHeight();

      Document doc = getDocument();
      int startOffset = element.getStartOffset();
      int endOffset = element.getEndOffset();
      int length = endOffset - startOffset - 1;

      // Get the document content
      doc.getText(startOffset, length, workBuff);

      int oldCount = contentBuff.count;

      int k = 0;
      int count = offsets[k++];
      for (int i = 0; i < pos; i++) {
         if (workBuff.array[i] == separator)
            count = offsets[k++];
         else
            count++;
      }

      contentBuff.count = count;
      // System.out.println("modelToView: " + pos + " " + count);

      offset = Utilities.getTabbedTextWidth(contentBuff, metrics, 0, this, element.getStartOffset());
      contentBuff.count = oldCount;

      r.x += offset;
      r.width = 1;

      return r;
   }

   public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
      if (!editor.isEditable()) {
         return super.viewToModel(fx, fy, a, bias);
      }

      int offset = 0;
      a = adjustAllocation(a);
      bias[0] = Position.Bias.Forward;

      int x = (int) fx;
      int y = (int) fy;
      Rectangle r = a.getBounds();
      int startOffset = element.getStartOffset();
      int endOffset = element.getEndOffset();

      if (y < r.y || x < r.x) {
         return startOffset;
      } else if (y > r.y + r.height || x > r.x + r.width) {
         return endOffset - 1;
      }

      // The given position is within the bounds of the view.
      offset = Utilities.getTabbedTextOffset(contentBuff, getFontMetrics(), r.x, x, this, startOffset);
      // The offset includes characters not in the model,
      // so get rid of them to return a true model offset.
      int count = offset;
      for (int i = 0; i < count; i++) {
         if (formattedContent[i] == blank)
            offset--;
      }

      // Don't return an offset beyond the data
      // actually in the model.
      if (offset > endOffset - 1) {
         offset = endOffset - 1;
      }
      // System.out.println("viewToModel: " + offset);

      return offset;
   }

   public void insertUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
      if (editor.isEditable()) {
         super.insertUpdate(changes, adjustAllocation(a), f);
         createContent(); // Update content string
      } else
         super.insertUpdate(changes, a, f);
   }

   public void removeUpdate(DocumentEvent changes, Shape a, ViewFactory f) {
      if (editor.isEditable()) {
         super.removeUpdate(changes, adjustAllocation(a), f);
         createContent(); // Update content string
      } else
         super.removeUpdate(changes, a, f);
   }
   // End of View methods

   // View drawing methods: overridden from PlainView
   protected void drawLine(int line, Graphics g, int x, int y) {
      if (!editor.isEditable()) {
         // draw the field only if some data is there ( not just the separators )
         // numFields is the number of separators + 1, so if data is present the length will be at least numFields
         if( getDocument().getLength() >= numFields)
            super.drawLine(line, g, x, y);
         return;
      }

      // Set the colors
      JTextComponent host = (JTextComponent) getContainer();
      unselected = (host.isEnabled()) ? host.getForeground() : host.getDisabledTextColor();
      Caret c = host.getCaret();
      selected = c.isSelectionVisible() ? host.getSelectedTextColor() : unselected;

      int p0 = element.getStartOffset();
      int p1 = element.getEndOffset() - 1;
      int sel0 = ((JTextComponent) getContainer()).getSelectionStart();
      int sel1 = ((JTextComponent) getContainer()).getSelectionEnd();

      try {
         // If the element is empty or there is no selection
         // in this view, just draw the whole thing in one go.
         if (p0 == p1 || sel0 == sel1 || inView(p0, p1, sel0, sel1) == false) {
            drawUnselectedText(g, x, y, 0, contentBuff.count);
            return;
         }

         // There is a selection in this view. Draw up to three regions:
         // (a) The unselected region before the selection.
         // (b) The selected region.
         // (c) The unselected region after the selection.
         // First, map the selected region offsets to be relative
         // to the start of the region and then map them to view
         // offsets so that they take into account characters not
         // present in the model.
         int mappedSel0 = mapOffset(Math.max(sel0 - p0, 0));
         int mappedSel1 = mapOffset(Math.min(sel1 - p0, p1 - p0));

         if (mappedSel0 > 0) {
            // Draw an initial unselected region
            x = drawUnselectedText(g, x, y, 0, mappedSel0);
         }
         x = drawSelectedText(g, x, y, mappedSel0, mappedSel1);

         if (mappedSel1 < contentBuff.count) {
            drawUnselectedText(g, x, y, mappedSel1, contentBuff.count);
         }
      }
      catch (BadLocationException e) {
         // Should not happen!
      }
   }

   protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
      if (!editor.isEditable()) {
         return super.drawUnselectedText(g, x, y, p0, p1);
      }
      g.setColor(unselected);
      workBuff.array = contentBuff.array;
      workBuff.offset = p0;
      workBuff.count = p1 - p0;
      return Utilities.drawTabbedText(workBuff, x, y, g, this, p0);
   }

   protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
      if (!editor.isEditable()) {
         return super.drawSelectedText(g, x, y, p0, p1);
      }
      workBuff.array = contentBuff.array;
      workBuff.offset = p0;
      workBuff.count = p1 - p0;
      g.setColor(selected);
      return Utilities.drawTabbedText(workBuff, x, y, g, this, p0);
   }
   // End of View drawing methods

   protected void buildMapping() {

      // Allocate a buffer to store the formatted string
      formattedContent = new char[formatSize];
      contentBuff.offset = 0;
      contentBuff.count = formatSize;
      contentBuff.array = formattedContent;

   }

   // Use the document content and the format
   // string to build the display content
   protected void createContent() {
      try {
         Document doc = getDocument();
         int startOffset = element.getStartOffset();
         int endOffset = element.getEndOffset();
         int length = endOffset - startOffset - 1;

         // Get the document content
         doc.getText(startOffset, length, workBuff);
         // System.out.println("work: " + new String(workBuff.array));

         // Initialize the output buffer with the
         // format string.
         for (int i = 0; i < formatSize; i++)
            formattedContent[i] = blank;
         for (int i = 1; i < numFields; i++)
            formattedContent[offsets[i] - (pad + 1)] = separator;

         // Insert the model content into
         // the target string.
         int count = length;
         int firstOffset = workBuff.offset;
         // System.out.println("count: " + count + " firstOffset: " +
         // firstOffset);

         // Place the model data into the output array
         int k = 0;
         int j = offsets[k++];
         for (int i = 0; i < count; i++) {
            if (workBuff.array[i + firstOffset] == separator)
               j = offsets[k++];
            else {
               formattedContent[j++] = workBuff.array[i + firstOffset];
            }
         }
         // System.out.println("buf: " + new String(formattedContent));
      }
      catch (BadLocationException bl) {
         contentBuff.count = 0;
      }
   }

   // Map a document offset to a view offset.
   protected int mapOffset(int pos) {
      int j = 0;
      int count = 0;
      for (int i = 0; i < pos; i++) {
         while (formattedContent[j++] == blank)
            count++;
         count++;
      }
      return count;
   }

   // Determines whether the selection intersects
   // a given range of model offsets.
   protected boolean inView(int p0, int p1, int sel0, int sel1) {
      if (sel0 >= p0 && sel0 < p1) {
         return true;
      }

      if (sel0 < p0 && sel1 >= p0) {
         return true;
      }

      return false;
   }

   //instance variables
   protected char[] formattedContent; // The formatted content for display
   protected char[] formatChars; // The format string as characters
   protected Segment contentBuff; // Segment pointing to formatted content
   //protected Segment measureBuff; // Segment pointing to mask string
   protected Segment workBuff; // Segment used for scratch purposes
   protected Element element; // The mapped element
   protected int[] offsets; // Model-to-view offsets
   protected Color selected; // Selected text color
   protected Color unselected; // Unselected text color
   protected int formatSize; // Length of the formatting string
   protected int fieldWidth;
   protected char separator;
   static private final int pad = 1;
   static private final char blank = ' ';
   private int numFields;
   private IPAddressField editor;

}