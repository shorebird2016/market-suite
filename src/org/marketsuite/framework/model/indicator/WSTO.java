package org.marketsuite.framework.model.indicator;

import java.util.ArrayList;

import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.FundQuote;

//Weekly fast stochastic, inherited from DSTO
public class WSTO extends DSTO {
    //CTOR: create/populate all stochastic values for given quote array
    public WSTO(int period, int smooth_period, ArrayList<FundQuote> quotes) {
    }

    //CTOR: create/populate a portion of stochastic values for given array, exception is thrown if out of bound
    //  caller must make sure indices are within bounds
    public WSTO(int period, int smooth_period, ArrayList<FundQuote> quotes, int start_index, int end_index) {
    }
}