package org.marketsuite.framework.pattern;

import org.marketsuite.framework.model.FundQuote;

//Bill Williams' naming of "Five Bar Pattern", objectified to be used by algorithms
public class Fractal {
    //construct a fractal object with supplied five quotes, caller must ensure quotes have 5 elements
    // the comparison is based on LOWs
    // index = from older quote to recent quotes (YAHOO convention)
    public Fractal(FundQuote[] quotes) {
        for (int i = 0; i < 5; i++) {
            float midq = quotes[2].getLow();
            if (quotes[0].getLow() > quotes[1].getLow() && quotes[1].getLow() > quotes[2].getLow()
                   && quotes[2].getLow() < quotes[3].getLow() && quotes[3].getLow() < quotes[4].getLow())
                fractalQuote = quotes[2];
        }
    }

    //accessor
    public FundQuote getFractal() { return fractalQuote; }
    //variables
    private FundQuote fractalQuote;
}
