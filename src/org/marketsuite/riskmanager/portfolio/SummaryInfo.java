package org.marketsuite.riskmanager.portfolio;

public class SummaryInfo {
    public SummaryInfo(double totalRisk, double marketValue, double riskPercent,
                       double profitLossAmount, double profitLossPercent, double adjustedRisk,
                       double adjustedRiskPercent, double totalCost, double cashPercent,
                       double mpAmount, double mpPercent) {
        this.totalRisk = totalRisk;
        this.marketValue = marketValue;
        this.riskPercent = riskPercent;
        this.profitLossAmount = profitLossAmount;
        this.profitLossPercent = profitLossPercent;
        this.adjustedRisk = adjustedRisk;
        this.adjustedRiskPercent = adjustedRiskPercent;
        this.totalCost = totalCost;
        this.cashPercent = cashPercent;
        this.mpAmount = mpAmount; //max pullback
        this.mpPercent = mpPercent;
    }

    public double getTotalRisk() {
        return totalRisk;
    }
    public double getMarketValue() {
        return marketValue;
    }
    public double getRiskPercent() {
        return riskPercent;
    }
    public double getProfitLossAmount() {
        return profitLossAmount;
    }
    public double getProfitLossPercent() {
        return profitLossPercent;
    }
    public double getAdjustedRisk() {
        return adjustedRisk;
    }
    public double getAdjustedRiskPercent() {
        return adjustedRiskPercent;
    }
    public double getTotalCost() {
        return totalCost;
    }
    public double getCashPercent() {
        return cashPercent;
    }
    public double getMpAmount() {
        return mpAmount;
    }
    public double getMpPercent() {
        return mpPercent;
    }
    private double totalRisk;
    private double marketValue;
    private double riskPercent;
    private double profitLossAmount;
    private double profitLossPercent;
    private double adjustedRisk;
    private double adjustedRiskPercent;
    private double totalCost;
    private double cashPercent;
    private double mpAmount;
    private double mpPercent;
}
