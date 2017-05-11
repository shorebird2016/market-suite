package org.marketsuite.framework.strategy.lsquare;

public class LSquareParam {
    public LSquareParam(int ratingThreshold, int highRating) {
        this.ratingThreshold = ratingThreshold;
        this.highRating = highRating;
    }

    public LSquareParam(int ratingThreshold, int highRating, boolean allowBuyEqualComp,
                        boolean allowBuyEqualRs, boolean allowSellEqualCr,
                        boolean costStop, float priceDropPct, boolean weekDropStop, float weekDropPct,
                        boolean highStop, float highDropPct) {
        this.ratingThreshold = ratingThreshold;
        this.highRating = highRating;
        this.allowBuyEqualComp = allowBuyEqualComp;
        this.allowSellEqualCr = allowSellEqualCr;
        this.costStop = costStop;
        this.priceDropPct = priceDropPct;
        this.weekDropStop = weekDropStop;
        this.weekDropPct = weekDropPct;
        this.highStop = highStop;
        this.highDropPct = highDropPct;
    }

    public int getRatingThreshold() { return ratingThreshold; }
    public void setRatingThreshold(int ratingThreshold) { this.ratingThreshold = ratingThreshold; }
    public int getHighRating() { return highRating; }
    public void setHighRating(int highRating) { this.highRating = highRating; }
    public boolean isAllowBuyEqualComp() { return allowBuyEqualComp; }
    public void setAllowBuyEqualComp(boolean allowBuyEqualComp) { this.allowBuyEqualComp = allowBuyEqualComp; }
    public boolean isAllowBuyEqualRs() { return allowBuyEqualRs; }
    public boolean isAllowSellEqualCr() { return allowSellEqualCr; }
    public void setAllowSellEqualCr(boolean allowSellEqualCr) { this.allowSellEqualCr = allowSellEqualCr; }
    public boolean isCostStop() { return costStop; }
    public void setCostStop(boolean costStop) { this.costStop = costStop; }
    public float getPriceDropPct() { return priceDropPct; }
    public void setPriceDropPct(float priceDropPct) { this.priceDropPct = priceDropPct; }
    public boolean isWeekDropStop() { return weekDropStop; }
    public void setWeekDropStop(boolean weekDropStop) { this.weekDropStop = weekDropStop; }
    public float getWeekDropPct() { return weekDropPct; }
    public void setWeekDropPct(float weekDropPct) { this.weekDropPct = weekDropPct; }
    public boolean isHighStop() { return highStop; }
    public void setHighStop(boolean highStop) { this.highStop = highStop; }
    public float getHighDropPct() { return highDropPct; }
    public void setHighDropPct(float highDropPct) { this.highDropPct = highDropPct; }

    //----- variables -----
    private int ratingThreshold;
    private int highRating;
    private boolean allowBuyEqualComp, allowBuyEqualRs, allowSellEqualCr;
    private boolean costStop;
    private float priceDropPct;
    private boolean weekDropStop;
    private float weekDropPct;
    private boolean highStop;
    private float highDropPct;
}
