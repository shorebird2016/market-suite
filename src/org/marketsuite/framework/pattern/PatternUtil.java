package org.marketsuite.framework.pattern;

import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.FundQuote;

import java.util.ArrayList;

//collection of utilities to calculate technical patterns
public class PatternUtil {
    //is 2nd element of array fractal point ?
    public static boolean isLowFractal(FundQuote[] quotes) {
        for (int i = 0; i < 5; i++) {
            if (quotes[0].getLow() > quotes[1].getLow() && quotes[1].getLow() > quotes[2].getLow()
                    && quotes[2].getLow() < quotes[3].getLow() && quotes[3].getLow() < quotes[4].getLow())
                return true;
        }
        return false;
    }
    public static ArrayList<FundQuote> findFractals(ArrayList<FundQuote> quotes, int start_index) {
        ArrayList<FundQuote> ret = new ArrayList<>();
        for (int idx = start_index; idx >= 4; idx--) {
            FundQuote[] fq = new FundQuote[5]; int tmp_idx = idx;
            for (int fqidx = 4; fqidx >= 0; fqidx--) {
                fq[fqidx] = quotes.get(tmp_idx--);
            }
            if (isLowFractal(fq)) ret.add(fq[2]);
        }
        return ret;
    }
}
