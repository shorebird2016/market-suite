package org.marketsuite.framework.vlc;

import org.marketsuite.component.util.WidgetUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class MainFrame extends JFrame {
    MainFrame() throws HeadlessException {
        super("VLCJ Test");
        setContentPane(new VideoClipFrame("c:\\Users\\Alex\\Downloads\\MamboRock-05162015.mp4"));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        WidgetUtil.setFrameProperties(this, new Dimension(500, 400), true, null, WindowConstants.DISPOSE_ON_CLOSE);
    }
}
