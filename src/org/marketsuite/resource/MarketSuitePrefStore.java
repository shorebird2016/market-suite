package org.marketsuite.resource;

import org.marketsuite.component.resource.PreferenceStore;

import java.io.Serializable;

/**
 * Collection of attributes used throughout application as user preferences.  A singleton.
 */
public class MarketSuitePrefStore extends PreferenceStore {
    //singleton access
    public static PreferenceStore getInstance() {
        if (_Prefs == null)
            _Prefs = new MarketSuitePrefStore();
        return _Prefs;
    }
    //private CTOR
    private MarketSuitePrefStore() {
        loadPreferences();
    }

    //----- accessors -----
    public int[] getFundamentalColumnOrder() { return fundamentalColumnOrder; }
    public void setFundamentalColumnOrder(int[] order) { fundamentalColumnOrder = order; }
    public boolean[] getFundamentalColumnVisible() { return fundamentalColumnVisible; }
    public void setFundamentalColumnVisible(boolean[] hs) { fundamentalColumnVisible = hs; }
    public int getTrackerSplitterPosition() { return trackerSplitterPosition; }
    public void setTrackerSplitterPosition(int pos) { trackerSplitterPosition = pos; }

    //----- variables -----
    public int[] fundamentalColumnOrder;//fundamental tab column ordering
    public boolean[] fundamentalColumnVisible;//fundamental tab column visibility
    public int trackerSplitterPosition = -1;//Splitter position in Tracker frame
}
