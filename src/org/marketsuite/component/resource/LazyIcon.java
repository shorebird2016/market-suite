package org.marketsuite.component.resource;

import javax.swing.*;
import java.awt.*;

/**
 * Enumeration of icons and images used in component package.
 */
// please make sure that all .gif files have no spaces and contain all lowercase in the filename
public enum LazyIcon implements Icon {
    //main frame
    APP_ICON("icon/icon_app.png"),
    DIALOG_ICON("icon/icon_app.png"),
//    SPLASH_SCREEN("image/moto_splash_screen.png"),
//    BRAND_BAR("image/MotoCPbrandBar.png"),
//    HOUR_GLASS("icon/hourglass.gif"),

    //backgrounds
    BACKGROUND_TABLE_HEADER("image/header_bgnd.png"),
    BACKGROUND_IP_ADDRESS("image/border_locked.png"),
    BACKGROUND_CONTENT("image/bg_content.png"),
//    BACKGROUND_STATUS_BAR_ALARM("image/bg_footer_left.png"),
//    BACKGROUND_STATUS_BAR("image/bg_footer_right.png"),
//    BACKGROUND_MENUBAR("image/bg_menubar.png"),
    BACKGROUND_PROPERTIES("image/bg_properties.png"),
    BACKGROUND_TOOLBAR      ( "/org/marketsuite/component/UI/images/bg_toolbar.png" ),
    BACKGROUND_TREE("image/bg_tree.png"),
    IMAGE_TOOLBAR_SEPARATOR("image/toolBarSeparatorY.png"),
    IMAGE_TOOLBAR_SEPARATOR2("image/toolBarSeparatorY2.png"),

    //setup tab, system panel
//    FRONT_VIEW("image/front_view.png"),
//    FRONT_VIEW_CRDSP("image/front_view_crdsp.png"),
//    REAR_VIEW("image/rear_view.png"),
//    LICENSE_OUT("image/license_out.gif"),
//    TABLE_COLUMN_OP("icon/table_column_op.gif"),
//    REBOOT("icon/reboot.gif"),
//    RESTART_CONTROLLER("icon/restart.gif"),
//    RESTART_CORE("icon/downarr.gif"),
//    CONNECT("icon/connect.gif"),
//    DISCONNECT("icon/disconnect.gif"),
//    REFRESH("icon/refresh.gif"),
//    OUTLET_FILL("image/cap_outlet_fill.png"),
//    DC_PS("image/48V.png"),
//    AC_PS("image/AC.png"),
    //setup tab, redundancy tab
    PLUS_SIGN("icon/plus_sign.jpg"),
    PLUS_SIGN_2("icon/plus_sign_2.png"),
    MINUS_SIGN("icon/minus_sign.jpg"),
    MINUS_SIGN_DISABLED("icon/minus_sign.jpg"),

    //alarm tab
//    ICON_ALARM_CRITICAL("icon/icon_alarm_critical.png"),
//    ICON_ALARM_MAJOR("icon/icon_alarm_major.png"),
//    ICON_ALARM_MINOR("icon/icon_alarm_minor.png"),
//    ICON_ALARM_WARNING("icon/alarm_warning.gif"),
//    ICON_ALARM_NOTE("icon/alarm_note.gif"),
//    ICON_ALARM_SEARCH("icon/alarm_search.gif"),
    //status bar primary/backup mode
//    MODE_PRIMARY("icon/icon_mode_primary.png"),
//    MODE_BACKUP("icon/icon_mode_backup.png"),

    //navigation trees
    //input Output line icons
//    ICON_INPUT_ASI("icon/input_ASI.gif"),
//    ICON_OUTPUT_ASI("icon/output_ASI.gif"),

    //GIGE Input Line
//    ICON_GIGE_IN_LINE_C("icon/icon_gigE_input_collapsed.png"),
//    ICON_GIGE_IN_LINE_E("icon/icon_gigE_input_expanded.png"),
//    ICON_GIGE_IN_DISABLE("icon/icon_gige_input_disabled.png"),

    //ASI input Line
//    ICON_IN_LINE_C("icon/icon_dvb_asi_input_collapsed.png"),
//    ICON_IN_LINE_E("icon/icon_dvb_asi_input_expanded.png"),

    //GIGE Output Line
//    ICON_GIGE_OUT_LINE_E("icon/IconGigeOutputLineEnabled.gif"),
//    ICON_GIGE_OUT_LINE_D("icon/IconGigeOutputLineDisabled.gif"),

    //ASI output Line
//    ICON_OUT_LINE_C("icon/IconOutLineC.gif"),
//    ICON_OUT_LINE_E("icon/IconOutLineE.gif"),

    //Transport Stream - mux
//    ICON_MUX("icon/IconMux.png"),
//    ICON_MIRROR_MUX("icon/mux_mirror.png"),

    //Elementary Stream
//    ICON_VIDEO_E("icon/icon_video_node.png"),
//    ICON_AUDIO_E("icon/icon_audio_node.png"),
//    ICON_DATA_E("icon/icon_data_node.png"),
//    ICON_AUDIO_C("icon/IconAudioC.gif"),
//    ICON_VIDEO_C("icon/IconVideoC.gif"),
//    ICON_DATA_C("icon/IconElemC.gif"),

    //Program
//    ICON_PROG("icon/icon_program_node.png"),
//    ICON_PROG_GROOM("icon/icon_program_node_groomed.png"),
//    ICON_PROG_GROOM_COMMITED("icon/IconProgENotCommitted.gif"),
//    ICON_PROG_EXPANDED("icon/IconProgE.gif"),
//    ICON_PROG_ENC("icon/IconEncryp.png"),
//    ICON_PROG_GROOM_ENC("icon/IconColorEncryptedProgram.png"),
//    ICON_PROG_GROOM_ENC_COMMITED("icon/IconEncrypBlackWhite.gif"),
//    ICON_PROG_HASDEF("icon/IconBlackWhiteProgram.png"),
//    ICON_PROG_HASDEF_ENC("icon/IconBlackWhiteEncryptedProgram.png"),

    //Backup
//    ICON_BACK_PROG_GROOMED("icon/IconBackProgramGroomed.png"),
//    ICON_BACK_PROG_EMPTY("icon/IconBackProgramEmpty.png"),

    //Recoder
//    ICON_RECODER("icon/IconRecoder.png"),
//    ICON_AD_SERVER("icon/IconAdServer.png"),

    //PassedPIDS
//    ICON_PASSED_PIDS_E("icon/IconPassedPIDsE.png"),
//    ICON_PASSED_PIDS_C("icon/IconPassedPIDsC.png"),

    //Farmer
//    ICON_FARMER_C("icon/IconFarmerC.gif"),
//    ICON_FARMER_E("icon/IconFarmerE.gif"),
//    ICON_FARMER_DOWN("icon/IconFarmerDown.gif"),
//    ICON_FARMER_CAP("icon/IconFarmerCap.png"),
    //system icons
//    ICON_SYSTEM_C("icon/IconSystemC.gif"),
//    ICON_SYSTEM_E("icon/IconSystemE.gif"),

    //Table icons
//    ICON_TABLE_GROUP_C("icon/icon_table_group_collapsed.png"),
//    ICON_TABLE_GROUP_E("icon/icon_table_group_expanded.png"),
//    ICON_TABLE_C("icon/IconTableC.gif"),
//    ICON_TABLE_E("icon/icon_table_node.png"),

    //config file editor
//    ICON_OPENFILE("icon/IconOpen.gif"),
//    ICON_OPENFILEEX("icon/IconOpenEx.gif"),
//    ICON_SAVE("icon/IconSave.gif"),
//    ICON_SAVEAS("icon/IconSaveAs.gif"),
//    ICON_REFCOL("icon/IconRefCol.gif"),
//    ICON_RESTORE("icon/IconRest.gif"),
//    ICON_CLOSE("icon/IconClose.gif"),

//    ICON_ARROW("image/arrow.gif"),
//    ICON_UP_ARROW_S("image/upArrowSmall.gif"),
//    ICON_DOWN_ARROW_S("image/downarrowSmall.gif"),

    //ethernet ports status icon
    ICON_PORT_DOWN("icon/LED_red.gif"),
    ICON_PORT_10M("icon/LED_yellow.gif"),
    ICON_PORT_100M("icon/LED_cyan.gif"),
    ICON_PORT_1000M("icon/LED_green.gif"),
    ICON_PORT_UP("icon/LED_green.gif"),
    ICON_GROUP_DISCONNECTED("icon/LED_white.gif"),

    //table related
    TABLE_COLUMN_OP               ( "icon/table_column_op.gif" ),

    //toolbar in grooming
//    ICON_DIAGRAMVIEW("icon/button_toggle_left.png"),
//    ICON_DIAGRAMVIEW_PRESSED("icon/button_toggle_left_pressed.png"),
//    ICON_TABULARVIEW("icon/button_toggle_middle.png"),
//    ICON_TABULARVIEW_PRESSED("icon/button_toggle_middle_pressed.png"),
//    ICON_TIMELINEVIEW("icon/button_toggle_right.png"),
//    ICON_TIMELINEVIEW_PRESSED("icon/button_toggle_right_pressed.png"),
//    ICON_ALL_INPUTS("icon/button_toggle_left.png"),
//    ICON_ALL_INPUTS_PRESSED("icon/button_toggle_left_pressed.png"),
//    ICON_PICKED_INPUTS("icon/button_toggle_right.png"),
//    ICON_PICKED_INPUTS_PRESSED("icon/button_toggle_right_pressed.png"),
    IMAGE_LABEL_SELECTED("image/menuBar_normal.png"),
    IMAGE_LABEL_HOVER("image/menuBar_hover.png"),
    IMAGE_LABEL_NORMAL("image/menuBar_selected.png"),

    //analysis
//    ICON_ZOOM_IN("icon/zoomIn.png"),
//    ICON_ZOOM_OUT("icon/zoomOut.png"),
//    ICON_FIT("icon/fitscale.png"),

    //table editing icons, tools
    ICON_UNDER_CONSTRUCTION("icon/underConstruction.gif"),
    ICON_DELETED("icon/deleted.gif"),
//    ICON_DELETE("icon/icon_delete.png"),
//    ICON_ADD("icon/icon_add.png"),
//    ICON_IMPORT("icon/icon_import.png"),
//    ICON_EXPORT("icon/icon_export.png"),
//    ICON_CONNECT("icon/icon_connect.png"),
//    ICON_RIGHT_ARROW("icon/right_arrow.png"),
//   ICON_LEFT_ARROW               ( "icon/left_arrow.jpg"),
//   ICON_RIGHT_ARROW_BLUE         ( "icon/right_arrow_blue.jpg"),
//   ICON_LEFT_ARROW_BLUE          ( "icon/left_arrow_blue.jpg"),
//    ICON_CONN_HTTP("icon/icon_conn_http.png"),
//    ICON_CONN_HTTPS("icon/icon_conn_https.png"),

//    ICON_SMP_C("icon/IconSMPC.gif"),
//    ICON_SMP_E("icon/IconSMPG.gif"),

    BACK_ICON("icon/backIcon.gif"),
    CANCEL_ICON("icon/cancelIcon.gif"),
    FINISH_ICON("icon/finishIcon.gif"),
    NEXT_ICON("icon/nextIcon.gif"),
    WIZARD_IMAGE("image/clouds.jpg"),
//    GIGA_E_PORTS("image/gige_ports.png"),
//    PLAY("icon/play.png"),

    //toolbar
    ;

    //===============================================================
    // LazyIcon implementation
    //===============================================================
    private String file;
    private ImageIcon icon;

    LazyIcon(String _file) {
        file = _file;
    }

    public final Image getImage() {
        return getIcon().getImage();
    }

    public final ImageIcon getIcon() {
        if (icon == null)
            icon = new ImageIcon(LazyIcon.class.getResource(file));
        return icon;
    }

    // Icon implementation
    public final int getIconHeight() {
        return getIcon().getIconHeight();
    }

    public final int getIconWidth() {
        return getIcon().getIconWidth();
    }

    public final void paintIcon(Component c,
                                Graphics g,
                                int x,
                                int y) {
        getIcon().paintIcon(c, g, x, y);
    }

    public static final Icon disabledIcon = new Icon() {
        public int getIconWidth() { return 11; }

        public int getIconHeight() { return 11; }

        public void paintIcon(Component comp, Graphics g, int x, int y) {
            g.setColor(Color.black);
            g.fillRect(x, y, 10, 10);
        }
    };


}