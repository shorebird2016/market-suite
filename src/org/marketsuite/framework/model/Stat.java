package org.marketsuite.framework.model;

/**
 * Statistics data structure for a give data list
 */
public class Stat {
    //-----CTOR-----
    public Stat() {}
    public Stat(float avg_amt, float min_amt, float max_amt, float median_amt,
                float avg_pct, float min_pct, float max_pct, float median_pct) {
        avgAmount = avg_amt;
        minAmount = min_amt;
        maxAmount = max_amt;
        medianAmount = median_amt;
        avgPct = avg_pct;
        minPct = min_pct;
        maxPct = max_pct;
        medianPct = median_pct;
    }

    //stats in dollar amount
    private float avgAmount;
    public float getAvgAmount() {
        return avgAmount;
    }
    public void setAvgAmount(float avgAmount) {
        this.avgAmount = avgAmount;
    }

    private float minAmount;
    public float getMinAmount() {
        return minAmount;
    }
    public void setMinAmount(float minAmount) {
        this.minAmount = minAmount;
    }

    private float maxAmount;
    public float getMaxAmount() {
        return maxAmount;
    }
    public void setMaxAmount(float maxAmount) {
        this.maxAmount = maxAmount;
    }

    private float medianAmount;
    public float getMedianAmount() {
        return medianAmount;
    }
    public void setMedianAmount(float medianAmount) {
        this.medianAmount = medianAmount;
    }
//
//    private float drawDownAmount;
//    public float getDrawDownAmount() {
//        return drawDownAmount;
//    }
//    public void setDrawDownAmount(float amt) {
//        drawDownAmount = amt;
//    }

    //stats in percentage
    private float avgPct;
    public float getAvgPct() {
        return avgPct;
    }
    public void setAvgPct(float avgPct) {
        this.avgPct = avgPct;
    }

    private float minPct;
    public float getMinPct() {
        return minPct;
    }
    public void setMinPct(float minPct) {
        this.minPct = minPct;
    }

    private float maxPct;
    public float getMaxPct() {
        return maxPct;
    }
    public void setMaxPct(float maxPct) {
        this.maxPct = maxPct;
    }

    private float medianPct;
    public float getMedianPct() {
        return medianPct;
    }
    public void setMedianPct(float medianPct) {
        this.medianPct = medianPct;
    }
//
//    private float drawDownPct;
//    public float getDrawDownPct() {
//        return drawDownPct;
//    }
//    public void setDrawDownPct(float pct) {
//        drawDownPct = pct;
//    }
}
