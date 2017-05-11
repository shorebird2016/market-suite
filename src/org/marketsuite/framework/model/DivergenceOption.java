package org.marketsuite.framework.model;

public class DivergenceOption {
    public DivergenceOption(int barPerSegment, int lookbackDays, int recentFilter) {
        this.barPerSegment = barPerSegment;
        this.lookbackDays = lookbackDays;
        this.recentFilter = recentFilter;
    }

    public int getBarPerSegment() { return barPerSegment; }
    public void setBps(int bps) { this.barPerSegment = bps; }
    public int getLookback() { return lookbackDays; }
    public void setLookback(int lookback) { this.lookbackDays = lookback; }
    public int getRecentFilter() { return recentFilter; }
    public void setRecentFilter(int recentFilter) { this.recentFilter = recentFilter; }

    private int barPerSegment = 5;//DVG bar per segment
    private int lookbackDays = 90;//DVG look back period
    private int recentFilter = 10;//DVG only look at recent N days
}
