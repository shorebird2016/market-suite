package org.marketsuite.framework.model.indicator;

/**
 * A general purpose data structure to store indicator related information
 */
public class IndicatorData {
    public IndicatorData(String name, float param1, float param2, float param3) {
        this.name = name;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }

    private String name = "";
    public String getName() {
        return name;
    }

    private float param1;
    public float getParam1() {
        return param1;
    }

    private float param2;//for RSI, it is average gain
    public float getParam2() {
        return param2;
    }

    private float param3;//for RSI, it is average loss
    public float getParam3() {
        return param3;
    }
}
