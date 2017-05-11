package org.marketsuite.framework.model.data;

import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.Timeframe;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.watchlist.model.WatchListModel;

import javax.swing.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

//data object describing relative performance of a group of symbols optionally from a watch list
public class RelativePerformance {
    //----- CTOR -----
    public RelativePerformance(WatchListModel wlm, Timeframe tf) {
        model = wlm; timeframe = tf;
        //generate perf
        ArrayList<String> members = model.getMembers();
        HashMap<String, MarketInfo> mki_map = model.getMarketInfoMap();
        for (String member : members) {
            MarketInfo mki = mki_map.get(member);
            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
            //TODO retrieve quote based on time frame, calc ROI, store in map
            //sort by perf

        }
    }

    //----- public methods -----
    public void sort(SortOrder order) {

    }

    //----- accessor -----
    public HashMap<String, Float[]> getPerfMap() { return perfMap; }

    //----- variables -----
    private HashMap<String, Float[]> perfMap = new HashMap<>();//holds performance of each symbol
    private Timeframe timeframe;
    private String id;//of this group, can be watch list name
    private WatchListModel model;
}
