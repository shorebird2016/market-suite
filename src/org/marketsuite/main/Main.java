package org.marketsuite.main;

import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;
import org.marketsuite.component.UI.CapLookAndFeel;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.type.UserLevel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.ManageDebugFiles;
import org.marketsuite.marektview.performance.SectorInfo;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;

import javax.swing.*;
import java.io.File;
import java.util.Calendar;

//main entry point
public class Main {
    public static void main(String[] args) {
CoreUtil.setAppStartTime();
        //check hard ending date use this format: 05-15-2013
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int yr = now.get(Calendar.YEAR);
        boolean expire = false;
//TODO....change expiration here....
        if (yr != 2017)
            expire = true;
        else if (month != 4 && month != 5)
            expire = true;
        if (expire) {
            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                ApolloConstants.APOLLO_BUNDLE.getString("lic_msg"));
            System.exit(0);
        }

        FrameworkConstants.DATA_FOLDER = System.getProperty("user.home") + File.separator
            + FrameworkConstants.DATABASE;//default MAC
        UserLevel usr_lvl = UserLevel.Medium;//default if no such argument
        for (String arg : args) {
            //process all the arguments
            if (arg.equals("-pc")) { //run on PC, MAC is default
                FrameworkConstants.DATA_FOLDER = FrameworkConstants.DATA_FOLDER_PC;//default MAC
            }
            else if (arg.equals("-log")) {
                logging = true;
            }
            else if (arg.equals(UserLevel.Basic.getCmdArg()))
                usr_lvl = UserLevel.Basic;
            else if (arg.equals(UserLevel.Medium.getCmdArg()))
                usr_lvl = UserLevel.Medium;
            else if (arg.equals(UserLevel.Expert.getCmdArg()))
                usr_lvl = UserLevel.Expert;
            else if (arg.equals("-redirect"))//when not in dev mode, turn on this
                try {
                    ManageDebugFiles.init();//stored in user profile sim.out... or sim.err....
                } catch (Exception e) {
                    e.printStackTrace();
                }
            else {
                System.out.println("Unknown Argument.  Exiting....");
                System.exit(0);
            }
        }

        //validate MAC Address from license file
//        try {
//            if (!SecurityUtil.verifyMacAddr()) {
//                MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                    ApolloConstants.APOLLO_BUNDLE.getString("lic_msg"));
//                System.exit(0);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//TODO: THIS call costs about 900ms...
        FrameworkConstants.adjustDataFolder();
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

        //check database
        File quote_folder = new File(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        if (quote_folder.list() == null || quote_folder.list().length == 0) {
            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("db_msg") +
                    ApolloConstants.APOLLO_BUNDLE.getString("db_msg_2") + quote_folder);
//            MessageBox.messageBox(
//                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                ApolloConstants.APOLLO_BUNDLE.getString("db_msg") + ApolloConstants.APOLLO_BUNDLE.getString("db_msg_2") + quote_folder,
//                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING
//            );
            System.exit(0);
        }

        //initialize benchmark, check SP500 data ok
        boolean sp500data_ok = FrameworkConstants.populateSp500Data(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE);
        if (!sp500data_ok) {
            WidgetUtil.showWarning(ApolloConstants.APOLLO_BUNDLE.getString("sp500_msg"));
//            MessageBox.messageBox(
//                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                ApolloConstants.APOLLO_BUNDLE.getString("sp500_msg"),
//                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING
//            );
            System.exit(0);
        }
        FrameworkConstants.calcSp500AnnualReturn();
        SectorInfo.createSectorToIGroup();
        FrameworkConstants.calcFridays(200);//TODO: make it variable, enough for now..

        //validate license
//        if (!SecurityUtil.validateLicense()) {
//            MessageBox.messageBox(FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
//                    ApolloConstants.APOLLO_BUNDLE.getString("lic_msg"));
//            System.exit(0);
//        }
        ApolloPreferenceStore.loadPreferences();
        ToolTipManager.sharedInstance().setInitialDelay(2);
        ToolTipManager.sharedInstance().setDismissDelay(30000);
        MainModel.getInstance().setUserLevel(usr_lvl);//do this after init complete, but before main window
        MdiMainFrame.getInstance().setVisible(true);
        CoreUtil.showTimeFromAppStart("<Main.CTOR()>...DONE...NOW VISIBLE...");
    }

    //----- instance variable -----
    public static boolean logging;
}