package org.marketsuite.marektview.ranking;

public class RankElement implements Comparable<RankElement> {
    public int compareTo(RankElement re) {
        if (pct > re.pct)
            return 1;
        else if (pct < re.pct)
            return -1;
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RankElement(String _symbol, float _pct) {
        symbol = _symbol;  pct = _pct;
    }
    public RankElement(String _symbol, float _pct, int _rank) {
        symbol = _symbol;  pct = _pct;  rank = _rank;
    }
    public String symbol;
    public float pct;
    public int rank;//positive integer 1..N
}
