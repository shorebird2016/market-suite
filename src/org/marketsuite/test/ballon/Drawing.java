package org.marketsuite.test.ballon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//draw a simple line and rectangle
public class Drawing {

    public static void main(String[] args) {
        JFrame frm = new JFrame();

        final JPanel pnl = new JPanel();
        pnl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Graphics2D g2d = (Graphics2D)pnl.getGraphics();
                g2d.setPaint(Color.green);
                g2d.drawLine(10, 10, 200, 200);
                g2d.setPaint(Color.red);
                g2d.draw3DRect(220, 10, 25, 100, false);
            }
        });

        frm.setContentPane(pnl);
        frm.setSize(500, 500);
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);
        frm.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}
