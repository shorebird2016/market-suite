package org.marketsuite.riskmgr.model;

/**
 * A model class representing a stop level with identifier.
 */
public class StopLevel {
    public StopLevel(String id, double level) {
        this.id = id;
        this.level = level;
    }
    public StopLevel(String id, double level, String method) {
        this.id = id;
        this.level = level;
        this.method = method;
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

    private String method;
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
