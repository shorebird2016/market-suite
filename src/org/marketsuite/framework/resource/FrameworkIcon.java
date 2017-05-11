package org.marketsuite.framework.resource;

import javax.swing.*;
import java.awt.*;

//collection of images, icons
public enum FrameworkIcon implements Icon {
    TRASH_EMPTY             ( "icon/trash_empty.png"),
    WINDOW_MAXIMIZE         ( "icon/window_maximize.png"),
    WINDOW_MINIMIZE         ( "icon/window_minimize.png"),
    WINDOW_MULTIPLE         ( "icon/window_multiple.png"),
    BAR_CHART               ( "icon/bar_chart.png" ),
    CANDLE_CHART            ( "icon/candle_chart.png" ),
    CANDLE_SAMPLE           ( "icon/candle_sample.png" ),
    RATING                  ( "icon/rating.png" ),
    CHART_MACD              ( "icon/candle-indicator.png" ),
    CHART_STO               ( "icon/candle-oscillator.png" ),
    CHART_BOLLINGER         ( "icon/candle-band.png" ),
    PRICE_CHART             ( "icon/price_chart.png" ),
    LINE_CHART              ( "icon/line_chart.png" ),
    LINE_CHART_32           ( "icon/line_chart_32.png" ),//32x32
    EQUITY_CURVE            ( "icon/equity_curve.png" ),
    GRAPH                   ( "icon/graph.png" ), //32x32
    LOG_SCALE               ( "icon/log_scale.png" ),
    FILE_OPEN               ( "icon/file_open.png" ),
    FILE_SAVE               ( "icon/file_save.gif" ),
    FILE_SAVE_AS            ( "icon/file_save_as.gif" ),
    FILE_CLOSE              ( "icon/file_close.png" ),
    FILE_DELETE             ( "icon/file_delete.png" ),
    FILE_ADD                ( "icon/file_add.png" ),
    FILES                   ( "icon/files.png" ),
    REPORT                  ( "icon/report.png" ),
    BACKGROUND_CONTENT      ( "image/bg_content.png" ),
    RIGHT_ARROW             ( "icon/arrow_right.png"),
    LEFT_ARROW              ( "icon/arrow_left.png"),
    DOWN_ARROW              ( "icon/down_arrow.png"),
    NEXT                    ( "icon/next.png"),
    RUN                     ( "icon/play.png"),
    CLEAR                   ( "icon/clear.png"),
    REFRESH                 ( "icon/refresh.png"),
    CURSOR                  ( "icon/cursor.png"),
    FONT_LARGER             ( "icon/font_larger.png"),
    FONT_SMALLER            ( "icon/font_smaller.png"),
    PLAY_MOVIE              ( "icon/movie.png"),
    PAUSE_MOVIE             ( "icon/pause.png"),
    RESUME_MOVIE            ( "icon/resume.png"),
    IMPORT                  ( "icon/import.png"),
    EXPORT                  ( "icon/export.png"),
    EXPORT_LIST             ( "icon/export_list.png"),
    IMAGE_TOOLBAR_SEPARATOR2( "image/separator.png" ),
    SETTING                 ( "icon/settings.png"),
    SEARCH                  ( "icon/search.png" ),
    VIEW_EDIT               ( "icon/view-edit.png" ),
    DOWNLOAD                ( "icon/download.png" ),
    ZOOM_IN                 ( "icon/zoom_in.png" ),
    ZOOM_OUT                ( "icon/zoom_out.png" ),
    ARROW_3D_RIGHT          ( "icon/arrow3d_right.png" ),
    ARROW_3D_LEFT           ( "icon/arrow3d_left.png" ),
    MAGNIFIER               ( "icon/magnifier.png" ),
    TRACKER                 ( "icon/tracking.png" ),
    RADAR                   ( "icon/radar.png" ),
    SELECT_ALL              ( "icon/select_all.png"),
    SELECT                  ( "icon/select.png"),
    WATCH                   ( "icon/watch.png"),
    DUPLICATE               ( "icon/duplicate.png"),
    MERGE                   ( "icon/merge.png"),
    STOP                    ( "icon/stop.png"),
    RANGE                   ( "icon/range.gif"),
    EXIT_TRADE              ( "icon/exit.png"),
    MARKET                  ( "icon/market.png"),
    EXPAND_TREE             ( "icon/expand_tree.png"),
    COLLAPSE_TREE           ( "icon/collapse_tree.png"),
    DUMPER                  ( "icon/dumper.png"),
    VALIDATE                ( "icon/validate.png"),
    BACKUP                  ( "icon/db_backup.png"),
    RESTORE                 ( "icon/db_restore.png"),
    THUMBNAIL               ( "icon/thumbnail.png"),
    CALCULATOR              ( "icon/calculator.png"),
    UNDO                    ( "icon/undo.png"),
    BACKGROUND_ATLANTIS     ( "image/atlantis.jpg"),
    BACKGROUND_PLAIN_1      ( "image/bg_plain1.jpg"),
    THUMB_TACK              ( "icon/thumb_tack.png"),
    QUESTION_MARK           ( "icon/question_mark.png"),
    FILTER                  ( "icon/filter.png"),
    BULL                    ( "icon/bull.png"),
    BEAR                    ( "icon/bear.png"),
    TREND_UP                ( "icon/trend_up.png"),
    TREND_DOWN              ( "icon/trend_down.png"),
    SQUARE_RED              ( "icon/red_square.gif"),
    SQUARE_GREEN            ( "icon/green_square.gif"),

//    CDL_BEAR_FALL3          ( "candle/bear-falling-3.png"),
//    CDL_BEAR_SIDE_BY_SIDE   ( "candle/bear-side-by-side.png"),
    CDL_DOJI_TOP            ( "candle/doji-top.png"),
    CDL_BULL_ENGULF         ( "candle/bull-engulf.png"),
    CDL_BEAR_ENGULF         ( "candle/bear-engulf.png"),
    CDL_BULL_HARAMI         ( "candle/bull-harami.png"),
    CDL_BEAR_HARAMI         ( "candle/bear-harami.png"),
    CDL_BULL_KICKER         ( "candle/bull-kicker.png"),
    CDL_BEAR_KICKER         ( "candle/bear-kicker.png"),
    CDL_BULL_PUSHER         ( "candle/bull-pusher.png"),
    CDL_BEAR_PUSHER         ( "candle/bear-pusher.png"),
    CDL_BULL_GAP            ( "candle/bull-gap.png"),
    CDL_BEAR_GAP            ( "candle/bear-gap.png"),
    CDL_BULL_PIERCING       ( "candle/bull-piercing.png"),
    CDL_BEAR_DARK_CLOUD     ( "candle/bear-dark-cloud.png"),
    CDL_BEAR_EVE_STAR       ( "candle/bear-evening-star.png"),
    CDL_BULL_MORNING_STAR   ( "candle/bull-morning-star.png"),
    CDL_BULL_HAMMER         ( "candle/bull-hammer.png"),
    CDL_BULL_INVERT_HAMMER  ( "candle/bull-inverted-hammer.png"),
    CDL_BEAR_HANGMAN        ( "candle/bear-hangman.png"),
    CDL_BEAR_SHOOTING_STAR  ( "candle/bear-shooting-star.png"),
    TREND_UP_1              ( "chart/trend_up_1.png"),
    TREND_UP_2              ( "chart/trend_up_2.png"),
    TREND_DOWN_1            ( "chart/trend_down_1.png"),
    TREND_DOWN_2            ( "chart/trend_down_2.png"),
    DOLLAR_16               ( "icon/dollar_16.png"),
    DOLLAR_24               ( "icon/dollar_24.png"),
    PIVOT                   ( "icon/pivot.png"),
    DATABASE                ( "icon/database.png"),
    REWIND                  ( "icon/rewind.png"),
    FORWARD                 ( "icon/forward.png"),
    TODAY                   ( "icon/today.png"),
    ;

    //CTOR
    FrameworkIcon(String _file) {
        file = _file;
    }

    // Icon implementation
    public final int getIconHeight() {
        return getIcon().getIconHeight();
    }

    public final int getIconWidth() {
        return getIcon().getIconWidth();
    }

    public final void paintIcon(Component c, Graphics g, int x, int y) {
        getIcon().paintIcon(c, g, x, y);
    }

    public final Image getImage() {
       return getIcon().getImage();
    }

    public final ImageIcon getIcon() {
        if (icon == null) {
            if (FrameworkIcon.class.getResource(file) == null)
                System.err.println("=============NO FILE ==========" + file);
            icon = new ImageIcon(FrameworkIcon.class.getResource(file));
        }
        return icon;
    }

    //instance variables
    private ImageIcon icon;
    private String file;
}

