package org.marketsuite.riskmgr.model;

//Data class that models a position in portfolio
public class Position {
    public Position(String symbol, int shares, float cost, float stop, float risk, float reward, float mkt_value, String account, StopLevelInfo sli) {
        this.symbol = symbol;
        this.shares = shares;
        this.cost = cost;
        this.stop = stop;
        this.risk = risk;
        this.reward = reward;
        this.account = account;
        this.stopLevelInfo = sli;
        marketValue = mkt_value;
    }

    private String symbol;
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    private int shares;
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }

    private float cost;
    public float getCost() { return cost; }
    public void setCost(float cost) { this.cost = cost; }

    private float stop;
    public float getStop() { return stop; }
    public void setStop(float stop) { this.stop = stop; }

    private float risk;
    public float getRisk() { return risk; }
    public void setRisk(float risk) { this.risk = risk; }

    private float reward;
    public float getReward() { return reward; }
    public void setReward(float reward) { this.reward = reward; }

    private String account;
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    private StopLevelInfo stopLevelInfo;
    public StopLevelInfo getStopLevelInfo() { return stopLevelInfo; }
    public float getCurClose() { return stopLevelInfo.getQuotes().get(0).getClose(); }

    public double getBreakEvenPrice() {
        return (cost * shares + 0.01 * 2 * shares + 3) / shares;
    }

    private float marketValue;
    public float getMarketValue() { return marketValue; }
}
