package org.marketsuite.framework.model;

//util of a point in equity curve
public class Equity {
    public Equity(String _date, float _shares, float _price) {
        date = _date;
        price = _price;
        shares = _shares;
        equity = _shares * price;
    }

    public String getDate() {
        return date;
    }

    public float getPrice() {
        return price;
    }

    public float getShares() {
        return shares;
    }

    public float getEquity() {
        return equity;
    }

    private String date;
    private float price;
    private float shares;
    private float equity;
}
