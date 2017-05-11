package org.marketsuite.component.panel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	//CTOR:
    public ImagePanel(ImageIcon icon, int w, int h) {
        this(icon.getImage());
        _nWidth = w;
        _nHeight = h;
    }

    public ImagePanel(Image img) {
        this.img = img;
//            Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        Dimension size = new Dimension(_nWidth, _nHeight);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }

    //instance variables
    private int _nWidth = 0;
    private int _nHeight = 0;
    private Image img;
}
