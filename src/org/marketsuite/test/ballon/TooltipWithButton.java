package org.marketsuite.test.ballon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TooltipWithButton {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new TooltipWithButton().makeUI();
            }
        });
    }

    public void makeUI() {
        JLabel label = new JLabel("Mouse here") {
            private JToolTip toolTip;
            public JToolTip createToolTip() {
                if (toolTip == null) {
                    JPanel panel = new JPanel(new GridLayout(0, 1));
                    for (int i = 0; i < 6; i++) {
                        final int j = i;
                        JButton button = new JButton("Click " + i);
                        button.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                System.out.println("You clicked button number " + j);
                            }
                        });
                        panel.add(button);
                    }

                    toolTip = super.createToolTip();
                    toolTip.setLayout(new BorderLayout());
                    Insets insets = toolTip.getInsets();
                    Dimension panelSize = panel.getPreferredSize();
                    panelSize.width += insets.left + insets.right;
                    panelSize.height += insets.top + insets.bottom;
                    toolTip.setPreferredSize(panelSize);
                    toolTip.add(panel);
                }
                return toolTip;
            }
        };
        label.setToolTipText("");
        JFrame frame = new JFrame();
        frame.add(label);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
