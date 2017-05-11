package org.marketsuite.component.panel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.marketsuite.component.resource.LazyIcon;

/**
 * a label with skin capabilities, pluggable background like synth does.
 */
public class SkinPanel extends JPanel {
	//CTOR:create this object with 3 types of background images
   public SkinPanel(ImageIcon bkgnd) {
      setOpaque(false);
      _BackgroundImage = bkgnd.getImage();
  }
   public SkinPanel(LazyIcon bkgnd) {
      setOpaque(false);
      _BackgroundImage = bkgnd.getImage();
  }

   public SkinPanel(ImageIcon bkgnd, LayoutManager layout) {
      this(bkgnd);
      setLayout(layout);
  }
   
   public SkinPanel(LazyIcon bkgnd, LayoutManager layout) {
      this(bkgnd);
      setLayout(layout);
  }

    //interface implementation
    public void paint(Graphics g) {
        //add background first
        Dimension size = getSize();
        g.fillRect(0, 0, size.width, size.height);
        g.drawImage(_BackgroundImage, 0, 0, getWidth(), getHeight(), null);
        super.paint(g);
    }

    //public methods
    //to change background image based on skin type, NORMAL, SELECTED, HOVER
    public void setSkin(ImageIcon bkgnd) {
       _BackgroundImage = bkgnd.getImage();
       repaint();
   }
    public void setSkin(LazyIcon bkgnd) {
       _BackgroundImage = bkgnd.getImage();
       repaint();
   }

    //instance variables
    private Image _BackgroundImage;

    //literals
    public static final int NORMAL = 1;
    public static final int SELECTED = 2;
    public static final int HOVER = 3;
}