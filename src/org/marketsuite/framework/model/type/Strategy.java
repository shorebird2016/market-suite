package org.marketsuite.framework.model.type;

public enum Strategy {
    BUY_HOLD ("Buy and Hold"), //TODO add time frame
    DCA("Dollar Cost Averaging"),
    MACD_ZC("MACD Zero Cross"),
    RSI_OB_OS("RSI Overbought/Oversold"),
    DSTO_OB_OS("Stochastic Overbought/Oversold"),//Daily
    CCI_ZC("CCI Zero Cross"),
    ICHMOKU_KUMO_BREAK("Kumo Breakout"),//Ichimoku
    ICHIMOKU_TK_CROSS("Tekan/Kijun Cross"),//Ichimoku
    MAC("Moving Average Cross"),
    IBD_RATING("L-Square"),
    ;

    Strategy(String descr) { description = descr; }

    public static Strategy[] singleIndicatorList() {
        return new Strategy[] { MACD_ZC, RSI_OB_OS, DSTO_OB_OS, CCI_ZC, /*ICHIMOKU_TK_CROSS, */ICHMOKU_KUMO_BREAK };
    }
    public static Strategy[] compoundIndicatorList() {
        return new Strategy[] { MAC, IBD_RATING };
    }
    public static Strategy[] basicStrategies() { return new Strategy[] {BUY_HOLD, DCA}; }

    public String toString() { return description; }
    public String getDescription() { return description; }
    private String description;

}