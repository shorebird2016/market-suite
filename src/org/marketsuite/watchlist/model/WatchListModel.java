package org.marketsuite.watchlist.model;

import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.model.type.PerfTimeframe;
import org.marketsuite.framework.model.type.Timeframe;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.framework.model.DivergenceOption;

import java.io.IOException;
import java.util.*;

/**
 * Top level container for data models used in watchlist package.
 * Created when watch list window is launched, destroyed when window closed.
 */
public class WatchListModel {
    /**
     * Create model using list stored in GroupStore specified by name.
     * @param watchlist_name name of list
     * @param no_wait true = don't wait it's finished
     */
//TODO Fix problem with no_wait being true, it doesn't work
    public WatchListModel(String watchlist_name, boolean no_wait) {
        _sWatchlistName = watchlist_name;
        ArrayList<String> members = getMembers();
        if (members.size() > 0)
            _sCurSymbol = members.get(0);
        if (no_wait)
            _mapMarketInfo = MarketUtil.createMarketInfoMapNoWait(GroupStore.getInstance().getMembers(watchlist_name), _arySkippedSymbols);
        else
            _mapMarketInfo = MarketUtil.createMarketInfoMap(GroupStore.getInstance().getMembers(watchlist_name), _arySkippedSymbols);
        _nModelType = MODELTYPE_GROUPSTORE;
        createIbdRating();
    }
    /**
     * Create model using symbols specified in argument.
     * @param symbol_list list of symbols
     * @param watchlist_name name of this watch list
     */
    public WatchListModel(ArrayList<String> symbol_list, String watchlist_name) {
        _sSymbolList = symbol_list;
        _sWatchlistName = watchlist_name;
        if (symbol_list.size() > 0)
            _sCurSymbol = symbol_list.get(0);
        _mapMarketInfo = MarketUtil.createMarketInfoMap(symbol_list, _arySkippedSymbols);
        _nModelType = MODELTYPE_SYMBOLLIST;
        createIbdRating();
    }

    //----- public methods -----
    //find next symbol, null = can't find next one or empty list
    public String getNextPrevSymbol(boolean forward) {
        if (getMembers().size() == 0) return null;

        //find out position of current symbol
        ArrayList<String> members = GroupStore.getInstance().getGroup(getWatchlistName());
        int pos = members.indexOf(_sCurSymbol);
        if (pos < 0)  return null;//can't find current symbol
        if (forward) {
            pos++;  if (pos == members.size()) pos = 0;
        }
        else {
            pos--;  if (pos < 0) pos = members.size() - 1;
        }
        _sCurSymbol = members.get(pos);
        return _sCurSymbol;
    }
    public void calcQuoteRanges(WatchListModel wlm) {
        ArrayList<String> members = wlm.getMembers();
        _fMaxRange = 0;
        for (String symbol : members) {
            float range = calcRange(wlm.getMarketInfo(symbol).getFund().getQuote());
            if (range > _fMaxRange)
                _fMaxRange = range;
            _fRanges.put(symbol, range);
        }
    }
    //add new symbol to this model
    public void addSymbol(String symbol) throws Exception {
        if (_mapMarketInfo.get(symbol) != null)
            return;//already exist
        MarketInfo mki = MarketUtil.calcMarketInfo(symbol, FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
        _mapMarketInfo.put(symbol, mki);

        //collect symbol list
        Iterator<String> itor = _mapMarketInfo.keySet().iterator();
        _sSymbolList.clear();
        while (itor.hasNext())
            _sSymbolList.add(itor.next());
        _nModelType = MODELTYPE_SYMBOLLIST;//change mode
    }
    public static ArrayList<String> mergeLists(List<String> watch_lists) {
        ArrayList<String> merge_lst = new ArrayList<>();
        for (String name : watch_lists) {
            ArrayList<String> members = GroupStore.getInstance().getMembers(name);
            for (String member : members) {
                if (!merge_lst.contains(member))
                    merge_lst.add(member);
            }
        }
        return merge_lst;
    }
    //calculate performance ranking of all symbols for a given time frame between 2 indices
    public ArrayList<RankElement> computePerfRanking(PerfTimeframe tf, int end_index) {
        ArrayList<FundQuote> quotes1 = getMarketInfo(getMembers().get(0)).getFund().getQuote();
        int start_index = PerfTimeframe.calcStartIndex(quotes1, end_index, tf);
        return AppUtil.calcRank(start_index, end_index, this);
    }

    //----- private methods -----
    //calculate ranges for all symbols in watch list
    //find range of a data series for closing prices
    private float calcRange(ArrayList<FundQuote> quotes) {
        float first_close = quotes.get(0).getClose();
        float max = first_close, min = first_close;
        for (FundQuote q : quotes) {
            float close = q.getClose();
            if (close > max)
                max = close;
            else if (close < min)
                min = close;
        }
        return max - min;
    }
    //create IBD rating map for the current watch list _sWatchListName / _sSymbolList
    private void createIbdRating() {
        _mapIbdRating = new TreeMap<>();
        for (String sym : getMembers()) {
            ArrayList<IbdRating> rating = null;
            try {
                rating = IbdRating.readIbdRating(sym, FrameworkConstants.DATA_FOLDER_IBD_RATING, 100);
            } catch (IOException e) {//ok not having the rating
//                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_02") + " " + sym, LoggingSource.L_SQUARE_IBD_RATING);
            }
            _mapIbdRating.put(sym, rating);
        }
    }

    //----- accessor -----
    public String getCurSymbol() { return _sCurSymbol; }
    public void setCurSymbol(String symbol) { _sCurSymbol = symbol; }
    public String getBaselineSymbol() { return _sBaselineSymbol; }
    public void setBaselineSymbol(String baseline_symbol) { _sBaselineSymbol = baseline_symbol; }
    public String getWatchlistName() { return _sWatchlistName; }
    public ArrayList<String> getMembers() {
        if (_nModelType == MODELTYPE_GROUPSTORE)
            return GroupStore.getInstance().getMembers(_sWatchlistName);
        else
            return _sSymbolList;
    }
    public MarketInfo getMarketInfo(String symbol) { return _mapMarketInfo.get(symbol); }
    public HashMap<String, MarketInfo> getMarketInfoMap() { return _mapMarketInfo; }
    public int getQuoteSize() {
        ArrayList<String> members = getMembers();
        if (members.size() > 0) {
            MarketInfo mki = getMarketInfo(members.get(0));
            return mki.getFund().getSize();
        }
        return 0;
    }
    public TreeMap<String, ArrayList<IbdRating>> getIbdRatingMap() { return _mapIbdRating; }

    //----- variables -----
    private String _sCurSymbol;
    private String _sBaselineSymbol = DEFAULT_BASELINE_SYMBOL;
    private String _sWatchlistName;//of this watch list
    private HashMap<String, MarketInfo> _mapMarketInfo;
    private ArrayList<String> _sSymbolList = new ArrayList<>();
    private int _nModelType = MODELTYPE_GROUPSTORE;
    private HashMap<String, Float> _fRanges = new HashMap<>();
    private float _fMaxRange;
    private ArrayList<String> _arySkippedSymbols = new ArrayList<>();
    private TreeMap<String, ArrayList<IbdRating>> _mapIbdRating;

    //----- literals -----
    public static final int MODELTYPE_GROUPSTORE = 0;
    public static final int MODELTYPE_SYMBOLLIST = 1;
    public final static String DEFAULT_BASELINE_SYMBOL = "SPY";
}
