package org.marketsuite.scanner.query;

import java.util.Calendar;

class CandlePattern {
    CandlePattern(Calendar startDate, CandleSignal signal) {
        this.startDate = startDate;
        this.signal = signal;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public CandleSignal getSignal() {
        return signal;
    }

    private Calendar startDate;//of pattern
    private CandleSignal signal;
}
