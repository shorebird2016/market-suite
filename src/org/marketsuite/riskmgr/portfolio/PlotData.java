package org.marketsuite.riskmgr.portfolio;

import java.util.Calendar;

//a simple object contains fields for plotting graph
class PlotData {
    PlotData(Calendar[] _dates, Double[] _data1, Double[] _data2) {
        dates = _dates;
        data1 = _data1;
        data2 = _data2;
    }

    public Calendar[] getDates() { return dates; }
    public double[] getData1() {
        double[] ret = new double[data1.length];
        for (int i=0; i<ret.length; i++)
            ret[i] = data1[i];
        return ret;
    }

    private Calendar[] dates;
    private Double[] data1;
    private Double[] data2;
}
