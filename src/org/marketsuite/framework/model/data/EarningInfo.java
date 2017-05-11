package org.marketsuite.framework.model.data;

//Information with regard to earning behavior
public class EarningInfo {
    public EarningInfo(String symbol, String date, boolean gap, String reaction) {
        this.symbol = symbol;
        this.date = date;
        this.gap = gap;
        this.reaction = reaction;
    }

    public String getSymbol() { return symbol; }
    public String getDate() { return date; }
    public boolean isGap() { return gap; }
    public String getReaction() { return reaction; }

    private String symbol;
    private String date;//of earning
    private boolean gap;//after earning
    private String reaction;//text description
}
