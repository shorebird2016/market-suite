package org.marketsuite.riskmanager.portfolio;

/**
 * A model class representing a stop level with identifier.
 */
public class StopLevel {
    public StopLevel(String id, double level) {
        this.id = id;
        this.level = level;
    }

    private String id;
    public String getId() {
        return id;
    }

    private double level;
    public double getLevel() {
        return level;
    }
    public void setLevel(double level) {
        this.level = level;
    }
}
