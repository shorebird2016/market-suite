package org.marketsuite.component;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Collection of constants used across all features.
 */
public class Constants {
    //borders
    public static final Border HEADER_BORDER = new BevelBorder(BevelBorder.RAISED);

    //colors - table cells
    public static final Color HEADER_BACKGROUND = Color.blue;
    public static final Color TITLE_BACKGROUND = Color.white;
    public static final Color HEADER_FOREGROUND = Color.black;

    public static final Color CELL_EVEN_UNSELECTED_BACKGROUND = new Color(251, 251, 251);
    public static final Color CELL_ODD_UNSELECTED_BACKGROUND = new Color(233, 238, 235);
    public static final Color CELL_EDITING_BACKGROUND = new Color(255, 200, 200);//cell editor background
    public static final Color SINGLE_SELECTION_BACKGROUND = new Color(151, 223, 230);//new Color(61, 180, 190)
    public static final Color MULTI_SELECTION_BACKGROUND = SINGLE_SELECTION_BACKGROUND;
    public static final Color CELL_SELECTED_BACKGROUND = new Color(200, 200, 255);//dark cyan
    public static final Color CELL_DISABLED_BACKGROUND = new Color(230, 230, 230);
    public static final Color CELL_READONLY_BACKGROUND = new Color(230, 230, 230);//near white
    public static final Color CELL_HIGHLIGHT_BACKGROUND =  new Color(200, 200, 255);//purple as editable//new Color(255, 248, 237);
    //new Color(174, 223, 228);//new Color(255, 230, 255);//indicate editable light cyan new Color(235, 253, 255)
    public static final Color CELL_DIRTY_BACKGROUND = new Color(140, 180, 190);
    //alarm colors
    public static final Color ALARM_COLOR_RAISED = Color.red;
    public static final Color ALARM_COLOR_CLEARED = new Color(0, 150, 0);
    public static final Color ALARM_COLOR_ERROR = new Color(255, 70, 150);
    public static final Color ALARM_COLOR_NORMAL = Color.black;

    //hyperlink colors - help, about, logout
    public static final Color LINK_COLOR_NORMAL = new Color(198, 197, 186);
    public static final Color LINK_COLOR_BOLD = new Color(255, 255, 255);
    public static final Color LINK_COLOR_BACKGROUND = new Color(147, 82, 88);

    // stream analysis
    public static final Color ANALYSIS_BACKGROUND = new Color(0, 0, 0);
    //public static final Color ANALYSIS_GRID = Color.darkGray;
    public static final Color ANALYSIS_GRID = new Color(32, 32, 32);

    //IPv4 address
    public static final String BAD_NTP_ADDRESS = "0.0.0.0";
    public static final String DEFAULT_IPV4_ADDRESS = "0.0.0.0";
    public static final String DEFAULT_NTP_ADDRESS = "";
    public static final String DEFAULT_MAC_ADDRESS = "00:00:00:00:00:00";
    public static final String DISALLOWED_IP_ADDRESS_1 = "0.0.0.0";
    public static final String DISALLOWED_IP_ADDRESS_2 = "255.255.255.255";

    //max and mins
    public static final long MIN_NUMBER = 1;
    public static final long MAX_NUMBER = 65536;
    public static final long MIN_VLANID = 1;
    public static final long MAX_VLANID = 4094;
    public static final long MIN_STRING = 1;
    public static final long MAX_STRING = 65536;
    public static final long MIN_MAC_BYTE = 0;
    public static final long MAX_MAC_BYTE_1 = 254;
    public static final long MAX_MAC_BYTE = 255;
    public static final long MIN_BYTE = 1;
    public static final long MAX_BYTE = 0xFE;
    public static final long MIN_OCTET = 1;
    public static final long MAX_OCTET = 254;
    public static final long MAX_NAME_SUFFIX = 9999;
    public static final int DEFAULT_TABLE_ROW_HEIGHT = 18;
    public static final Dimension DEFAULT_FRAMESIZE = new Dimension(1024, 768);

    //MetalLookAndFeel f;
    //font
    public static final Font CELL_FONT = new Font(Font.DIALOG, Font.PLAIN, 11);
    public static final Font FONT_BOLD = new Font(Font.DIALOG, Font.BOLD, 11);
    public static final Font FONT_BOLD_BIG = new Font(Font.DIALOG, Font.BOLD, 12);
    public static final Font HEADER_FONT = new Font(Font.DIALOG, Font.PLAIN, 11);
    public static final Font FONT_ITALIC_11 = new Font("Arial", Font.ITALIC, 11);
    public static final Font FONT_ITALIC_12 = new Font("Arial", Font.ITALIC, 12);
    public static final Font FONT_BOLD_15 = new Font(Font.DIALOG, Font.BOLD, 15);
    public static final Font VERDONA_PLAIN_12 = new Font("Verdana", Font.PLAIN, 12);
    public static final Font VERDONA_BOLD_12 = new Font("Verdana", Font.BOLD, 12);
    public static final Font VERDANA_PLAIN_15 = new Font("Verdana", Font.PLAIN, 15);
    public static final Font FONT_NORMAL_STATE = new Font("Verdana", Font.PLAIN, 11);
    public static final Font HIGHLIGHT_FONT = new Font("Verdana", Font.BOLD, 11);
    public static final Font LINK_FONT_NORMAL = new Font("Verdana", Font.PLAIN, 10);
    public static final Font LINK_FONT_BOLD = new Font("Verdana", Font.BOLD, 10);
    //for showing simplified chinese characters
    public static final Font FONT_ARIAL_UNICODE_MS_PLAIN_11 = new Font("Arial Unicode MS", Font.PLAIN, 11);

    //regular expressions
    public static final String REGEX_SYMBOL_SPLITTER = "[,\n\t\n\\ ]";

    //date time formats
    public static final SimpleDateFormat BASCI_DATE_FORMAT = new SimpleDateFormat(" MM/dd/yyyy ");//todo: remove this variable.....
    public static final DecimalFormat BASIC_DECIMAL_FORMAT = new DecimalFormat("###,000");
    public static final String[] FORMAT_DATETIME_LIST = {"MM/dd/yyyy HH:mm:ss z", "MM/dd/yyyy hh:mm:ss a z"};//24 hours, 12 hour
    public static final String[] FORMAT_DATETIME_SCHEDULE = {"HH:mm", "hh:mm a"};//24 hour, 12 hours

    //timeline view related
    public static final long SEC_TO_MILLI = 1000;                // conversion: second to milliseconds
    public static final long MIN_TO_MILLI = 60 * SEC_TO_MILLI;    // conversion: minute to milliseconds
    public static final long HOUR_TO_MILLI = MIN_TO_MILLI * 60;    // conversion: hour to milliseconds
    public static final long QUARTERDAY_TO_MILLI = HOUR_TO_MILLI * 6;    // conversion: hour to milliseconds
    public static final long HALFDAY_TO_MILLI = HOUR_TO_MILLI * 12;    // conversion: hour to milliseconds
    public static final long DAY_TO_MILLI = HOUR_TO_MILLI * 24;    // conversion: day to milliseconds
    //for time sacle combo

    public static final String[] TIMESCALE = {"One Hour", "Quarter Day", "Half Day", "One Day", "One Week"};
    public static final int TIMESCALE_ONE_HOUR = 0;
    public static final int TIMESCALE_QUARTER_DAY = 1;
    public static final int TIMESCALE_HALF_DAY = 2;
    public static final int TIMESCALE_ONE_DAY = 3;
    public static final int TIMESCALE_ONE_WEEK = 4;
    //for controlling distance between header cells
    public static final int PIXEL_PER_GRID = 75;

    public static final Dimension SCREEN_SIZE;
    public static final Rectangle SCREEN_VIRTUAL_BOUNDS;

    //resource bundles
    public static final ResourceBundle COMPONENT_BUNDLE = BundleWrapper.getBundle("org.marketsuite.component.resource.ComponentBundle");

    static {
        SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (int j = 0; j < gs.length; j++) {
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for (int i=0; i < gc.length; i++) {
                virtualBounds = virtualBounds.union(gc[i].getBounds());
            }
        }
        SCREEN_VIRTUAL_BOUNDS = virtualBounds;
    }

    //defaults
    public static final int DEFAULT_SPLITTER_LOCATION = 150;
    public static final int DEFAULT_SPLITTER_WIDTH = 5;
    public static final double DEFAULT_SPLITTER_POSITION_LEFT = SCREEN_SIZE.width * 0.22;
    public static final double DEFAULT_SPLITTER_POSITION_RIGHT = SCREEN_SIZE.width * 0.45;
    public static final double DEFAULT_SPLITTER_POSITION_TOP = SCREEN_SIZE.height * 0.33;
    public static final double DEFAULT_SPLITTER_POSITION_BOTTOM = SCREEN_SIZE.height * 0.66;
    public static final double DEFAULT_SPLITTER_POSITION_VERTICAL_MIDDLE = SCREEN_SIZE.height * 0.5;
    public final static Dimension NAVLABEL_DIMENSION = new Dimension(90, 23);

    //plug-in XML related
    public static final String PLUGIN_XML_NAME = "plugin.xml";
    public static final String PLUGIN_ROOT = "plugins";
    public static final String PLUGIN_NODE = "plugin";
    public static final String PLATFORM_NAME = "platform-name";
    public static final String CLASS_NAME = "class-name";//attribute under plugin node
    public static final String UTIL_CLASS_NAME = "util-class-name";
    public static final String DISPLAY_KEY = "display-name-key";


    // Wrapper to provide notification of a missing key, otherwise the whole gui crashes
    // The requested key itself will be returned if it is missing in the resource file.
    public static class BundleWrapper extends ResourceBundle {
        public BundleWrapper(String name, Locale locale) {
            bundle = (PropertyResourceBundle) ResourceBundle.getBundle(name, locale);
            this.name = name;
        }

        public String handleGetObject(String key) {
            String val = key;
            try {
                val = bundle.getString(key);
            } catch (MissingResourceException ex) {
                System.err.println(ex + "  " + name);
            }
            return val;
        }

        public Enumeration<String> getKeys() {
            return bundle.getKeys();
        }

        private PropertyResourceBundle bundle;
        private String name;
    }
}