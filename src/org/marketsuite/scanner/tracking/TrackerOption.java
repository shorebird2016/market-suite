package org.marketsuite.scanner.tracking;

import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.DivergenceOption;

public class TrackerOption {
    public TrackerOption() {}

    public TrackerOption(int dstoLow, int dstoHigh, float percent10x30, float percent50x120,
                         float priceThreshold, int averageVolumeThreshold, DivergenceOption dvgOption) {
        this.dstoLow = dstoLow;
        this.dstoHigh = dstoHigh;
        this.percent10x30 = percent10x30;
        this.percent50x120 = percent50x120;
        this.priceThreshold = priceThreshold;
        this.averageVolumeThreshold = averageVolumeThreshold;
        this.dvgOption = dvgOption;
    }

    //----- accessor -----
    public int getDstoLow() { return dstoLow; }
    public void setDstoLow(int dstoLow) { this.dstoLow = dstoLow; }
    public int getDstoHigh() { return dstoHigh; }
    public void setDstoHigh(int dstoHigh) { this.dstoHigh = dstoHigh; }
    public float getPercent10x30() { return percent10x30; }
    public void setPercent10x30(float percent10x30) { this.percent10x30 = percent10x30; }
    public float getPercent50x120() { return percent50x120; }
    public void setPercent50x120(float percent50x120) { this.percent50x120 = percent50x120; }
    public float getPriceThreshold() { return priceThreshold; }
    public int getAverageVolumeThreshold() { return averageVolumeThreshold; }
    public DivergenceOption getDvgOption() { return dvgOption; }
    public void setDvgOption(DivergenceOption dvgOption) { this.dvgOption = dvgOption; }

    //----- variables -----
    private int dstoLow = 20;
    private int dstoHigh = 50;
    private float percent10x30 = 5;//percent of 10MA below 30MA
    private float percent50x120 = 5;//percent of 50MA below 120MA
    private float priceThreshold = 20;//minimum $20
    private int averageVolumeThreshold = 250;//of last 20 trading days, in 000s
    private DivergenceOption dvgOption = new DivergenceOption(5, 90, 3);
}
