package org.marketsuite.framework.market;

import java.util.Calendar;

public class ChangeInfo {
    public ChangeInfo(int _index, int _from, int _to, Calendar _calendar) {
        index = _index;
        from = _from;
        to = _to;
        calendar = _calendar;
    }

    public int getIndex() { return index; }
    public int getFrom() { return from; }
    public int getTo() { return to; }
    public Calendar getCalendar() { return calendar; }

    private int index; //of quote array
    private int from;
    private int to;
    private Calendar calendar;
}
