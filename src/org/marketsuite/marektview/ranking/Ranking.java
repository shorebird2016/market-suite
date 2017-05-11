package org.marketsuite.marektview.ranking;

import java.util.ArrayList;

public class Ranking {
    public Ranking(String _symbol) {
        symbol = _symbol;
    }
    public String symbol;
    public ArrayList<Float> percents = new ArrayList<>();//for all bars
    public ArrayList<Integer> ranks = new ArrayList<>();//for all bars
    public ArrayList<Integer> freqs; //count of ranks during this run
}
