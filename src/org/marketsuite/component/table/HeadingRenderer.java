package org.marketsuite.component.table;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import org.marketsuite.component.Constants;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.framework.resource.FrameworkConstants;

public class HeadingRenderer extends JLabel implements TableCellRenderer  {
    //CTOR: default
    public HeadingRenderer() {
       if (image == null)
           getImage();
    }
    public HeadingRenderer(Font custom_font) { this(); _FontCustom = custom_font; }

    //CTOR: specify clickable columns, true = clickable header
    public HeadingRenderer(boolean[] clickable_columns) {
        _bClickableHeaders = clickable_columns;
        if(image == null) getImage();
    }

    private void getImage() {
        image = LazyIcon.BACKGROUND_TABLE_HEADER.getImage();
    }

    public void paintComponent(Graphics g) {
       if(image != null) {
          Dimension size = getSize();
          g.drawImage(image,0,0,size.width,size.height,null);
       }
       ui.paint(g,this);
    }

    //interface implementation
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setHorizontalAlignment(SwingConstants.CENTER);
        if (_FontCustom != null)
            setFont(_FontCustom);
        else
            setFont(Constants.HEADER_FONT);//default
        setForeground(Constants.HEADER_FOREGROUND);
        setBorder(Constants.HEADER_BORDER);
        if (_bClickableHeaders!= null && _bClickableHeaders[column]) //change into html
            setText("<html><b><u><i><font color=blue>" + value + "</b></i></u></html>");
        else
            setText((String)value);
        return this;
    }

    public boolean isHeaderClicable(int col) {
        return _bClickableHeaders[col];
    }

    private static Image image;
    private boolean[] _bClickableHeaders;
    private Font _FontCustom;
}