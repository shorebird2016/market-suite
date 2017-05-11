package org.marketsuite.framework.vlc;

import org.marketsuite.component.UI.CapLookAndFeel;
import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;
import org.marketsuite.component.UI.CapLookAndFeel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            SyntheticaLookAndFeel.setWindowsDecorated(false);
            //this is necessary to allow silver theme to show up if windows desktop already has silver theme
            UIManager.setLookAndFeel(new CapLookAndFeel());
            UIManager.put("TabbedPane.tabsOpaque", Boolean.TRUE);
            //NOTE: this will remove title bar and thus not moveable or resize
//            JFrame.setDefaultLookAndFeelDecorated(true);//use swing components to build this frame
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        ToolTipManager.sharedInstance().setInitialDelay(10);
        ToolTipManager.sharedInstance().setDismissDelay(30000);

    }
}
