package org.marketsuite.framework.model;

import java.util.ArrayList;
import java.util.Calendar;

//A collection of symbols' entry/exit dates on/off a list
// multiple pairs are possible per symbol
// NOTE: to be used by XMLEncoder, must obey java bean convention with deafult CTOR, setter and getter
public class EntryExitDates {
    public EntryExitDates() {} //Note: need this for XMLEncoder
    //CTOR: create object with symbol and first entry date (open pair)
    public EntryExitDates(String sym, Calendar friday) {
        symbol = sym;
        addEntryDate(friday);
    }

    private String symbol;
    public String getSymbol() { return symbol; }
    public void setSymbol(String sym) { symbol = sym; }

    private EntryExitPair currentPair;//null = no open pair
    public EntryExitPair getCurrentPair() { return currentPair; }
    public void setCurrentPair(EntryExitPair currentPair) { this.currentPair = currentPair; }

    //an array of entry / exit date pairs
    private ArrayList<EntryExitPair> pairs = new ArrayList<>();
    public ArrayList<EntryExitPair> getPairs() { return pairs; }
    public void setPairs(ArrayList<EntryExitPair> pairs) { this.pairs = pairs; }

    //add a new entry date
    public void addEntryDate(Calendar entry_date) {
        EntryExitPair pair = new EntryExitPair(entry_date);
        pairs.add(pair);
        currentPair = pair;//open a new pair
    }

    //set exit date for current open pair
    public void setExitDate(Calendar exit_date) {
        if (currentPair == null)//should never be here
            throw new IllegalArgumentException("No Entry Recorded yet !");
        currentPair.setExit(exit_date);
        currentPair = null;
    }

}
