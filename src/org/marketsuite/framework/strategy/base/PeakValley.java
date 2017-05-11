package org.marketsuite.framework.strategy.base;

/**
 * Data object tracks peak, valley and their differences for draw down calculations
 */
public class PeakValley {
    //-----CTOR-----
    public PeakValley() {}
    //use an existing object
    public PeakValley(PeakValley pv) {
        peakDate = pv.getPeakDate();
        peak = pv.getPeak();
        valleyDate = pv.getValleyDate();
        valley = pv.getValley();
    }
    public PeakValley(String peak_date, float peak_value) {
        peakDate = peak_date;
        peak = peak_value;
    }

    //-----instance variables / assessors-----
    //peak related
    private float peak = Float.MIN_VALUE;
    public float getPeak() {
        return peak;
    }
    public void setPeak(float peak) {
        this.peak = peak;
    }
    private String peakDate;
    public String getPeakDate() {
        return peakDate;
    }
    public void setPeakDate(String peakDate) {
        this.peakDate = peakDate;
    }
    //valley related
    private float valley = Float.MAX_VALUE;
    public float getValley() {
        return valley;
    }
    public void setValley(float valley) {
        this.valley = valley;
    }
    private String valleyDate;
    public String getValleyDate() {
        return valleyDate;
    }
    public void setValleyDate(String valleyDate) {
        this.valleyDate = valleyDate;
    }
}
