package org.marketsuite.component.resource;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.Icon;

/**
 * CTOR: creates a blurred icon from the original image
 */
public class DisabledIcon implements Icon {
    public DisabledIcon(Image orig_image) {
        originalImage = orig_image;
        width = originalImage.getWidth(null);
        height = originalImage.getHeight(null);
    }

    private void createImage() {
        disabledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) disabledImage.getGraphics();
        g2d.setComposite(composite);
        g2d.drawImage(originalImage, 0, 0, null);
        disabledImage = convolveOp.filter(disabledImage, null);
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }

    public void paintIcon(Component comp, Graphics g, int x, int y) {
        if (disabledImage == null) createImage();  // lazy image creation
        g.drawImage(disabledImage, x, y, null);
    }

    //instance variables
    private int width, height;
    private BufferedImage disabledImage;
    private Image originalImage;
    //literals
    private static final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .30f);
    private static final float[] BLUR = {0.25f, 0.25f, 0.25f, 0.25f};
    private static final Kernel kernel = new Kernel(2, 2, BLUR);
    private static final ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
}
