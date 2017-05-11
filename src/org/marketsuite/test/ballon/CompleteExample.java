package org.marketsuite.test.ballon;

import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;
import org.marketsuite.component.UI.CapLookAndFeel;
import org.marketsuite.test.ballon.panels.MainPanel;

import javax.swing.*;

/**
 * Main class for the Balloontip example application
 * @author Tim Molderez
 */
public class CompleteExample {
	/**
	 * Main method
	 * @param args		command-line arguments (unused)
	 */
	public static void main(String[] args) {
		// Switch to the OS default look & feel
//		try {
//	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//	    } catch (Exception e) {}
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

        // Now create the GUI
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("BalloonTip example");
				frame.setIconImage(new ImageIcon(CompleteExample.class.getResource("/org/marketsuite/test/ballon/resource/frameIcon.png")).getImage());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setContentPane(new MainPanel());
				frame.pack();
				frame.setSize(480, 640);
				frame.setLocationRelativeTo(null); // Centers the frame on the screen
				frame.setVisible(true);
			}
		});
	}
}
