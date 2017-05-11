package org.marketsuite.framework.model;

/**
 * Special class dedicated to annualized performance calculations.  Sometimes it can be partial year.
 */
public class AnnualReturn {
    public AnnualReturn(String sym, int yr, float perf) {
        symbol = sym;
        year = yr;
        performance = perf;
    }

    private String symbol;
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    //the year in question
    private int year;
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }

    //performance of this year
    private float performance;
    public float getPerformance() {
        return performance;
    }
    public void setPerformance(float performance) {
        this.performance = performance;
    }

    //begin point of time
//    private String beginDate;
//    public String getBeginDate() {
//        return beginDate;
//    }
//    public void setBeginDate(String beginDate) {
//        this.beginDate = beginDate;
//    }

    //ending point
//    private String endDate;
//    public String getEndDate() {
//        return endDate;
//    }
//    public void setEndDate(String endDate) {
//        this.endDate = endDate;
//    }
}
