package org.marketsuite.component.field;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalTextFieldUI;
import javax.swing.text.Element;
import javax.swing.text.View;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.resource.LazyIcon;

public class IPAddressFieldUI extends MetalTextFieldUI  {
    //CTOR
    public IPAddressFieldUI() {
        super();
    }

    //interface implementation
    public void installUI(JComponent c) {
        if (c instanceof IPAddressField) {
            editor = (IPAddressField) c;
        }
        super.installUI(c);
    }

    public void uninstallUI(JComponent c) {
        if (c instanceof IPAddressField) {
            editor = (IPAddressField) c;
        }
        super.uninstallUI(c);
    }

    protected void paintBackground(Graphics g) {
       super.paintBackground(g);
       if(editor.isEnabled()) return;
       Dimension size = editor.getSize();
       if(ic != null)
          g.drawImage(ic.getImage(),0,0,size.width,size.height,0,0,ic.getIconWidth(),ic.getIconHeight(),null);
    }
    
    // ViewFactory method - creates a view
    public View create(Element elem) {
        return new IPAddressFieldView(elem, editor);
    }

    public static ComponentUI createUI(JComponent c) {
        return new IPAddressFieldUI();
    }

    //instance variables
    protected IPAddressField editor;

    //literals
    private static ImageIcon ic = LazyIcon.BACKGROUND_IP_ADDRESS.getIcon();
    public static final long serialVersionUID = -1L;
}