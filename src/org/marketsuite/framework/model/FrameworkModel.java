package org.marketsuite.framework.model;

import javax.swing.*;

/**
 * A catch-all model class for misc variables used in framework level
 */
public class FrameworkModel {
    //-----class variables-----
    public static boolean logging;

    //a convenient pointer to main container of App layer
    private static JFrame mainFrame;
    public static void setMainFrame(JFrame frame) { mainFrame = frame; }
    public static JFrame getMainFrame() { return mainFrame; }
}
