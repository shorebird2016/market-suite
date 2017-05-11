package org.marketsuite.gap;

import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloConstants;

//Factors affecting gap behavior
enum GapFactor {
    PREGAP_PHASE(ApolloConstants.APOLLO_BUNDLE.getString("gps_pre_phase")),
    PREGAP_CANDLE(ApolloConstants.APOLLO_BUNDLE.getString("gps_pre_cdl")),
    PREGAP_PATTERN(ApolloConstants.APOLLO_BUNDLE.getString("gps_pre_pattern")),
    PREGAP_RATING(ApolloConstants.APOLLO_BUNDLE.getString("gps_pre_rtg")),
    PREGAP_VSQ(ApolloConstants.APOLLO_BUNDLE.getString("gps_pre_vsq")),
    PREGAP_MKT_CONDITION(ApolloConstants.APOLLO_BUNDLE.getString("gps_pre_mkt")),
    PREGAP_SECTOR_CONDITION(ApolloConstants.APOLLO_BUNDLE.getString("gps_pre_sector")),
    PREGAP_IG_CONDITION(ApolloConstants.APOLLO_BUNDLE.getString("gps_pre_ig")),
    AT_GAP_ROI(ApolloConstants.APOLLO_BUNDLE.getString("gps_gap_roi")),
    AT_GAP_CANDLE(ApolloConstants.APOLLO_BUNDLE.getString("gps_gap_cdl")),
    AT_GAP_BB(ApolloConstants.APOLLO_BUNDLE.getString("gps_gap_bb")),
    AT_GAP_CLOUD(ApolloConstants.APOLLO_BUNDLE.getString("gps_cloud")),
    AT_GAP_ICHIMOKU(ApolloConstants.APOLLO_BUNDLE.getString("gps_ich")),
    AT_GAP_LAGGING(ApolloConstants.APOLLO_BUNDLE.getString("gps_lag")),
    POSTGAP_PHASE(ApolloConstants.APOLLO_BUNDLE.getString("gps_post_phase")),
    POSTGAP_PULLBACK(ApolloConstants.APOLLO_BUNDLE.getString("gps_post_pullback")),
    POSTGAP_TYPE(ApolloConstants.APOLLO_BUNDLE.getString("gps_post_type")),
    ;

    GapFactor(String description) { descr = description; }
    public String toString() { return descr; }
    public static GapFactor toEnumConstant(String descr) {
        for (GapFactor gf : values()) {
            if (gf.toString().equals(descr))
                return gf;
        }
        return null;
    }
    private String descr;

}
