package org.marketsuite.test.scannerold;

/**
 * A data class used by scannerold to pass information.
 */
public class ScanResult {
    public ScanResult(String symbol, boolean entrySignal, String entry_date, boolean exitSignal, String exit_date,
                      double cagr, double avgDrawDown, double avgGain, double avgLoss, double profitFactor,
                      double inMarketPercent, double winRatio) {
        this.symbol = symbol;
        this.entrySignal = entrySignal;
        this.entryDate = entry_date;
        this.exitSignal = exitSignal;
        this.exitDate = exit_date;
        this.cagr = cagr;
        this.avgDrawDown = avgDrawDown;
        this.avgGain = avgGain;
        this.avgLoss = avgLoss;
        this.profitFactor = profitFactor;
        this.inMarketPercent = inMarketPercent;
        this.winRatio = winRatio;
    }

    private String symbol;
    public String getSymbol() {
        return symbol;
    }

    private boolean entrySignal;
    public boolean isEntrySignal() {
        return entrySignal;
    }

    private String entryDate;
    public String getEntryDate() {
        return entryDate;
    }

    private boolean exitSignal;
    public boolean isExitSignal() {
        return exitSignal;
    }

    private String exitDate;
    public String getExitDate() {
        return exitDate;
    }

    private double cagr;
    public double getCagr() {
        return cagr;
    }

    private double avgDrawDown;
    public double getAvgDrawDown() {
        return avgDrawDown;
    }

    private double avgGain;
    public double getAvgGain() {
        return avgGain;
    }

    private double avgLoss;
    public double getAvgLoss() {
        return avgLoss;
    }

    private double profitFactor;
    public double getProfitFactor() {
        return profitFactor;
    }

    private double inMarketPercent;
    public double getInMarketPercent() {
        return inMarketPercent;
    }

    private double winRatio;
    public double getWinRatio() {
        return winRatio;
    }

}
