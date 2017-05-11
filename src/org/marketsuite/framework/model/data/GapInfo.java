package org.marketsuite.framework.model.data;

import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.BollingerBandMagnitude;
import org.marketsuite.framework.model.type.GapType;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.BollingerBandMagnitude;
import org.marketsuite.framework.model.type.GapType;

//a data class holding gap related information for easy reporting / analyzing
public class GapInfo {
    //CTOR
    public GapInfo(FundQuote quote, float pct) {
        this.quote = quote;
        atGapRoi = pct;
    }
    //CTOR: special one only for no gap, only symbol in quote is meaningful
    public GapInfo(FundQuote quote, GapType gap_type) {
        this.quote = quote; type = gap_type; }

    //----- accessors -----
    public String getPrePhase() { return prePhase; }
    public void setPrePhase(String prePhase) { this.prePhase = prePhase; }
    public String getAtPhase() { return atPhase; }
    public void setAtPhase(String atPhase) { this.atPhase = atPhase; }
    public float getAtGapRoi() { return atGapRoi; }
    public void setAtGapRoi(float atGapRoi) { this.atGapRoi = atGapRoi; }
    public FundQuote getQuote() { return quote; }
    public void setQuote(FundQuote quote) { this.quote = quote; }
    public GapType getType() { return type; }
    public void setType(GapType type) { this.type = type; }
    public FundQuote getQuoteLowestLow() { return quoteLowestLow; }
    public void setQuoteLowestLow(FundQuote quoteLowestLow) { this.quoteLowestLow = quoteLowestLow; }
    public float getRoiLowestLow() { return roiLowestLow; }
    public void setRoiLowestLow(float roiLowestLow) { this.roiLowestLow = roiLowestLow; }
    public float getRoi1wk() { return roi1wk; }
    public void setRoi1wk(float roi1wk) { this.roi1wk = roi1wk; }
    public float getRoi2wk() { return roi2wk; }
    public void setRoi2wk(float pct2wk) { this.roi2wk = pct2wk; }
    public float getRoi4wk() { return roi4wk; }
    public void setRoi4wk(float pct4wk) { this.roi4wk = pct4wk; }
    public float getRoi6wk() { return roi6wk; }
    public void setRoi6wk(float roi6wk) { this.roi6wk = roi6wk; }
    public float getRoi8wk() { return roi8wk; }
    public void setRoi8wk(float pct8wk) { this.roi8wk = pct8wk; }
    public float getRoi10wk() { return roi10wk; }
    public void setRoi10wk(float roi10wk) { this.roi10wk = roi10wk; }
    public float getRoi12wk() { return roi12wk; }
    public void setRoi12wk(float pct12wk) { this.roi12wk = pct12wk; }
    public BollingerBandMagnitude getBbMag() { return bbMag; }
    public void setBbMag(BollingerBandMagnitude bbMag) { this.bbMag = bbMag; }

    //----- variables -----
    private FundQuote quote;
    private String prePhase, atPhase;
    private float atGapRoi;
    private GapType type = GapType.TYPE_5;
    private FundQuote quoteLowestLow;
    private float roiLowestLow;
    private float roi1wk, roi2wk, roi4wk, roi6wk, roi8wk, roi10wk, roi12wk;
    private BollingerBandMagnitude bbMag = BollingerBandMagnitude.Normal;
}
