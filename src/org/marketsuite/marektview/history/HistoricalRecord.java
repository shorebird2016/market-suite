package org.marketsuite.marektview.history;

import java.util.Calendar;

//data class to keep financial data on a per year basis
public class HistoricalRecord {
    public HistoricalRecord(Calendar cal, float close, float roi) {
        this.cal = cal;
        this.close = close;
        this.roi = roi;
    }

    //----- public methods -----
    public boolean isElectionYear() { return (cal.get(Calendar.YEAR) % 4 == 0); }
    public boolean isPreElectionYear() { return (cal.get(Calendar.YEAR) % 4 == 3); }
    public boolean isPostElectionYear() { return (cal.get(Calendar.YEAR) % 4 == 1); }
    public boolean isYearEnding5() {
        String str = String.valueOf(cal.get(Calendar.YEAR));
        String last_dig = str.substring(str.length() - 1, str.length());
        return last_dig.equals("5");
    }
    public boolean isYearEnding0() {
        String str = String.valueOf(cal.get(Calendar.YEAR));
        String last_dig = str.substring(str.length() - 1, str.length());
        return last_dig.equals("0");
    }
    public boolean isLambYear() {
        int yr = cal.get(Calendar.YEAR);
        for (int y : LAMB_YEARS)
            if (y == yr) return true;
        return false;//not found
    }
    public boolean isDemocraticPresident() {
        if (presidentParty == null) {//TODO remove 1900 and don't need this block
            System.err.println(" NO PART " + cal.get(Calendar.YEAR)); return false;
        }
        return presidentParty.equals(PoliticalParty.Democratic); }
    public boolean isRepublicanPresident() {
        if (presidentParty == null) {
            System.err.println(" NO PART " + cal.get(Calendar.YEAR)); return false;
        }
        return presidentParty.equals(PoliticalParty.Republican); }
    public boolean isDemocraticSenate() {
        if (senateMajority == null) {
            System.err.println(" NO PART " + cal.get(Calendar.YEAR)); return false;
        }
        return senateMajority.equals(PoliticalParty.Democratic); }
    public boolean isRepublicanSenate() {
        if (senateMajority == null) {
            System.err.println(" NO PART " + cal.get(Calendar.YEAR)); return false;
        }
        return senateMajority.equals(PoliticalParty.Republican); }

    //----- accessor -----
    public Calendar getCalendar() { return cal; }
    public int getYear() { return cal.get(Calendar.YEAR); }
    public float getPrice() { return close; }
    public float getRoi() { return roi; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getPresident() { return president; }
    public void setPresident(String president) { this.president = president; }
    public Calendar getSwornInDate() { return swornInDate; }
    public void setSwornInDate(Calendar swornInDate) { this.swornInDate = swornInDate; }
    public PoliticalParty getPresidentParty() { return presidentParty; }
    public void setPresidentParty(PoliticalParty presidentParty) { this.presidentParty = presidentParty; }
    public PoliticalParty getSenateMajority() { return senateMajority; }
    public void setSenateMajority(PoliticalParty senateMajority) { this.senateMajority = senateMajority; }
    public boolean isSeventhYear() { return seventhYear; }
    public void setSeventhYear(boolean seventhYear) { this.seventhYear = seventhYear; }

    //----- variables -----
    private Calendar cal;//last day of year
    private String symbol;
    private float close; //of symbol
    private float roi;//from previous year's close
    private String president;
    private Calendar swornInDate;
    private boolean seventhYear;//2 term +
    private PoliticalParty presidentParty;
    private PoliticalParty senateMajority;

    //----- literals -----
    private final static int[] LAMB_YEARS = {
        2015, 2003, 1991, 1979, 1967, 1955, 1943, 1931, 1919, 1907
    };
}
