package org.marketsuite.framework.model.type;

import org.marketsuite.thumbnail.ThumbnailPanel;

//rough estimate of prevailing trend
public enum MarketTrend {
    Up,
    Down,
    SideWay,
    ;

    //----- accessors -----
    public void setRawSlope(float rawSlope) { this.rawSlope = rawSlope; }
    public void setNormalizedSlope(float normalizedSlope) { this.normalizedSlope = normalizedSlope; }

    //----- variables
    private Timeframe timeFrame;
    private float rawSlope, normalizedSlope;
}
