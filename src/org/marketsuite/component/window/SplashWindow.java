package org.marketsuite.component.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import org.marketsuite.component.util.WidgetUtil;


public class SplashWindow extends JWindow implements ActionListener {
	public SplashWindow(ImageIcon icon, int prog_steps) {
        _nProgressSteps = prog_steps;
        Image image = icon.getImage();
        _pnlSplash = new SplashPanel(image);
        getContentPane().add(_pnlSplash, BorderLayout.SOUTH);
        pack();//must do this, otherwise window size is still 0
        //centering
        WidgetUtil.centerComponent(this);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                close();
            }
        });
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        close();
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    public void progress(int step, String text) {
        int percent = (step * 100 + 1) / _nProgressSteps;
        _pnlSplash.setStatus("(" + percent + " %) " + text);
    }

    private static class SplashPanel extends JPanel {
		public SplashPanel(Image image) {
            setLayout(new BorderLayout());
            setOpaque(false);
            _Image = image;
            _lblStatus = new JLabel("                                     ");
            _lblStatus.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            _lblStatus.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
            _lblStatus.setHorizontalAlignment(JLabel.RIGHT);
            add(_lblStatus, BorderLayout.SOUTH);
            setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
        }

        public void paintComponent(Graphics g) {
            g.drawImage(_Image, 0, 0, this);
            super.paintComponent(g);
        }

        public void setStatus(String status) {
            _lblStatus.setText(status);
        }
        //instance variables
        private JLabel _lblStatus;
        private Image _Image;
    }

    //instance variables
    private SplashPanel _pnlSplash;
    private int _nProgressSteps;
}