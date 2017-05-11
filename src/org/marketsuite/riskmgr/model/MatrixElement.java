package org.marketsuite.riskmgr.model;

import org.marketsuite.riskmgr.model.Position;

//represent single element (among many) inside each cell of the matrix
public class MatrixElement {
    public MatrixElement(String symbol, Position pos, float pl, float volatility) {
        this.symbol = symbol;
        this.pl = pl;
        this.position = pos;
        this.volatility = volatility;
    }

    private String symbol;
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    private Position position;
    public Position getPosition() { return position; }

    private float pl;
    public float getPl() {
        return pl;
    }
    public void setPl(float pl) {
        this.pl = pl;
    }

//    private float risk;
//    public float getRisk() {
//        return risk;
//    }
//    public void setRisk(float risk) {
//        this.risk = risk;
//    }

    private float volatility;
    public float getVolatility() {
        return volatility;
    }
    public void setVolatility(float volatility) {
        this.volatility = volatility;
    }
}
