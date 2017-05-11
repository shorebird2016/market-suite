package org.marketsuite.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;

public class CompoundIcon implements Icon {
    public void setIcon(Icon ic, boolean f, boolean p) {
        setIcon(ic, f, p, DEFAULT_BACKUP_COLOR);
    }

    public void setIcon(Icon ic, boolean f, boolean p, Color c) {
        setIcon(ic, 6, 2, f, p, c);
    }

    public void setIcon(Icon ic, int w, int h, boolean f, boolean p) {
        setIcon(ic, w, h, f, p, DEFAULT_BACKUP_COLOR);
    }

    public void setIcon(Icon ic, int w, int h, boolean f, boolean p, Color bColor) {
        orig = ic;
        forceToBackup = f;
        playing = p;
        dw = w;
        dh = h;
        backupColor = bColor;
    }

    public int getIconHeight() {
        return orig.getIconHeight() + dh;

    }

    public int getIconWidth() {

        return orig.getIconWidth() + dw;

    }

    public void paintIcon(Component comp, Graphics _g, int x, int y) {

        Graphics2D g = (Graphics2D) _g;

        if (forceToBackup) {
            g.setColor(backupColor);
            int dx = dw / 2;
            int dy = dh / 2;
            g.fillRect(x - dx, y - dy, getIconWidth(), getIconHeight());
            g.setColor(Color.darkGray);
            g.drawRect(x - dx, y - dy, getIconWidth(), getIconHeight());
        }

        orig.paintIcon(comp, g, x, y);
        if (playing) {
            g.setColor(playingColor);
            g.fillRect(x + 1, y + 1, 14, 8);
            g.setColor(Color.blue);
            g.drawRect(x + 1, y + 1, 14, 8);
        }

    }

    private Icon orig;
    private boolean forceToBackup;
    private boolean playing;
    private static final Color DEFAULT_BACKUP_COLOR = new Color(180, 180, 255, 255);
    private Color backupColor = DEFAULT_BACKUP_COLOR;
    private Color playingColor = new Color(100, 255, 255, 96);
    private int dw;
    private int dh;
}

