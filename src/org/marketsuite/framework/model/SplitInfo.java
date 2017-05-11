package org.marketsuite.framework.model;

/**
 * A data class that stores historical split dates and split ratio.
 */
public class SplitInfo {
    public SplitInfo(String split_date, float split_ratio) {
        this._sSplitDate = split_date;
        this._fSplitRatio = split_ratio;
    }

    //----- accessor -----
    public String getSplitDate() { return _sSplitDate; }
    public float getSplitRatio() { return _fSplitRatio; }

    //----- variable -----
    private String _sSplitDate;
    private float _fSplitRatio; //2:1 --> 2; 4:3 --> 1.3333
}
