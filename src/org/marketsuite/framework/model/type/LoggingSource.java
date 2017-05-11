package org.marketsuite.framework.model.type;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloConstants;

//a list of sources of classes that generates logging messages
public enum LoggingSource {
    SCANNER_STRATEGY,
    SCANNER_QUERY,
    SCANNER_RANKING,
    SCANNER_EARNING,
    RISKMGR_ACCOUNT,
    RISKMGR_MATRIX,//5
    THUMBNAIL,
    WATCHLIST_MGR,
    MARKETVIEW_IBD50,
    DATAMGR_IMPORT,
    DATAMGR_QUOTE,//10
    DAILY_CHART,
    WEEKLY_CHART,
    MONTHLY_CHART,
    L_SQUARE_IBD_RATING,
    L_SQUARE_PERFORMANCE,//15
    L_SQUARE_TECHNICAL,
    L_SQUARE_FUNDAMENTAL,
    L_SQUARE_SPEED,
    RISKMGR_PORTFOLIO,
    SIMULATOR_LSQUARE,//20
    SIMULATOR_CCI,
    SIMULATOR_REPORT,
    DAILY_CANDLE_CHART,
    WEEKLY_CANDLE_CHART,
    PLANNING_SHEET,//25
    GAP_STUDY,
    ;

    public String toString() { return DISPLAY_STRING[ordinal()]; }

    private final static String[] DISPLAY_STRING = {
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_00"),//0
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_01"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_02"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_03"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_04"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_05"),//5
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_06"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_07"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_08"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_09"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_10"),//10
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_18"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_11"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_12"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_13"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_15"),//15
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_16"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_17"),
            ApolloConstants.APOLLO_BUNDLE.getString("pw_plansheet"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_spd"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_siml2"),//20
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_sim_rpt"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_sim_cci"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_daily_candle"),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("ls_daily_candle"),
            ApolloConstants.APOLLO_BUNDLE.getString("pw_plansheet"),
            ApolloConstants.APOLLO_BUNDLE.getString("gps_mnu"),
    };
}
