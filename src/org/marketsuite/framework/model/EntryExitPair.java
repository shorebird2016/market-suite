package org.marketsuite.framework.model;

import java.util.Calendar;

public class EntryExitPair {
    public EntryExitPair() {} //for XMLEncoder
    public EntryExitPair(Calendar entry_date) {
        entry = entry_date;
    }

    private Calendar entry;
    public Calendar getEntry() { return entry; }
    public void setEntry(Calendar entry) { this.entry = entry; }

    private Calendar exit;
    public Calendar getExit() { return exit; }
    public void setExit(Calendar exit) { this.exit = exit; }

    private Calendar priorExit;//previous Friday's date from exit, to get other IbdInfo
    public Calendar getPriorExit() { return priorExit; }
    public void setPriorExit(Calendar pe) { priorExit = pe; }
}
