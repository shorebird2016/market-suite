package org.marketsuite.framework.model.type;

public enum StrategyType {
    DCOM, EMAC;
    public static StrategyType findStrategy(String type_string) {
        if (type_string.equals(DCOM.toString()))
            return DCOM;
        else
            return EMAC;
    }
}
