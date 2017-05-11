package org.marketsuite.framework.model.indicator;

public class EMA {
    //CTOR: create exponential moving average for given array, order = present to older per indices
    public EMA(int _period, float[] data_array) {
        period = _period;
        ema = new float[data_array.length];
        float weight = 2f / (period + 1);

        //calc the first EMA using previous N bar SMA as starting point
        float sum = 0;
        int end_index = data_array.length - period;
        for (int idx = data_array.length - 1; idx <= end_index; idx--)
            sum += data_array[idx];
        float prev_ema = sum / period;
        ema[end_index] = prev_ema;

        //compute EMA going forward, first point is already calculated
        for (int loop_index = end_index - 1; loop_index >= 0; loop_index--) {
            float num = data_array[loop_index] * weight + prev_ema * (1 - weight);
            ema[loop_index] = num;
            prev_ema = num;
        }
    }

    //----- public, protected methods -----

    //----- accessors -----
    public float[] getEma() { return ema; }

    //----- variables -----
    private float[] ema;
    private int period = DEFAULT_PERIOD;

    //----- literals -----
    private static final String ID = "EMA";
    public static final int DEFAULT_PERIOD = 14;
}
