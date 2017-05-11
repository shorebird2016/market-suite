package org.marketsuite.framework.model.data;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.model.indicator.CCI;
import org.marketsuite.framework.model.indicator.Ichimoku;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.MarketPhase;
import org.marketsuite.framework.model.type.MarketTrend;
import org.marketsuite.framework.model.type.Timeframe;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.CandleUtil;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.model.indicator.SMA;
import org.marketsuite.framework.model.type.MarketPhase;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.util.ArrayList;

/**
 * A data structure that contains various technical indicators.
 */
public class IndicatorRepository {
//    public IndicatorRepository() {}
    //CTOR: fields maybe null and/or exception, caller should stop and post error dialog, in batch operation, use logging window
    public IndicatorRepository(String _symbol, Timeframe tf, int num_bars) throws Exception {
        symbol = _symbol;
        barCount = num_bars;

        //obtain daily quotes first, adjust split, may need to convert to weekly, monthly
        fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol, num_bars);
        int fund_size = fund.getSize();
        int start_index = fund_size - 1;
        ArrayList<FundQuote> quotes = fund.getQuote();
        for (FundQuote quote : quotes)
            quote.setClose(quote.getUnAdjclose());//TODO overhaul unadjusted close
        DataUtil.adjustForSplits(fund, start_index, 0);

        //phase init
        phase = new MarketPhase[fund_size];
        for (int idx = start_index; idx >= 0; idx--) phase[idx] = MarketPhase.Unknown;

        //split between weekly and daily
        //compute highest point in this data series
        float highest_close = 0;
        int highest_index = 0;
        for (int idx = 0; idx < fund_size - 1; idx++) {
            float close = fund.getQuote().get(idx).getClose();
            if (close > highest_close) {
                highest_close = close;
                highest_index = idx;
            }
        }
        highestDate = fund.getDate(highest_index);

        //volume average, BB
        if (fund_size >= 20) {
            volumeAverage = IndicatorUtil.calcVolumeAverage(fund, 20, start_index - 20);
            bollingerBand = new BollingerBand(20, 2, 2, fund.getQuote());
            cci = new CCI(20, fund.getQuote());
        }

        //moving averages
        //pre-create result arrays, if SMA can't be calculated, return empty array, and longer SMA won't be calculated
        if (fund_size > 50)
            sma50d = new SMA(50, fund);
        if (fund_size > 200)
            sma200d = new SMA(200, fund); //if (!sma200d.getTrend(fund.getSize() - 1, 0).equals(MarketTrend.Up)) return;

        //ichimoku
        if (fund_size >= 52)
            ichimoku = new Ichimoku(fund.getQuote(), start_index, 0);

        //phase
        if (sma50d != null && sma200d != null) {
            for (int idx = start_index; idx >= 0; idx--)
                phase[idx] = IndicatorUtil.calcMarketPhase(quotes.get(idx).getClose(),
                    sma50d.getSma()[idx], sma200d.getSma()[idx]);
        }
}

    //----- public/protected methods -----


    //----- accessors -----
    public String getSymbol() { return symbol; }
    public FundData getFund() { return fund; }
    public void setBarCount(int barCount) { this.barCount = barCount; }
    public int getBarCount() { return barCount; }
    public float[] getVolumeAverage() { return volumeAverage; }
    public void setVolumeAverage(float[] volumeAverage) { this.volumeAverage = volumeAverage; }
    public BollingerBand getBollingerBand() { return bollingerBand; }
    public void setBollingerBand(BollingerBand bollingerBand) { this.bollingerBand = bollingerBand; }
    public String getHighestDate() { return highestDate; }
    public void setHighestDate(String cal) { highestDate = cal; }
    public CCI getCci() { return cci; }
    public MarketPhase[] getPhase() { return phase; }
    public Ichimoku getIchimoku() { return ichimoku; }
    public SMA getSma50d() { return sma50d; }
    public SMA getSma200d() { return sma200d; }

    //----- variables -----
    private String symbol;
    private FundData fund;
    private int barCount;//first data point for calculation
    private String highestDate;//highest point of this entire quote
    private float[] volumeAverage;
    private BollingerBand bollingerBand;
    private CCI cci;
    private SMA sma50d, sma200d;
    private MarketPhase[] phase;
    private Ichimoku ichimoku;

    //----- literals -----
}
