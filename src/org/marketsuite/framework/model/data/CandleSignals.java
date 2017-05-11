package org.marketsuite.framework.model.data;

import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.util.CandleUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.type.CandleSignal;

import java.util.ArrayList;

//collection of various candlestick signals
public class CandleSignals {
    public CandleSignals(ArrayList<FundQuote> _quotes, int start_index) {
        quotes = _quotes;
        _nDojiTops = CandleUtil.findDojiSpintop(_quotes, start_index, 20);
        _nBullishEngulfs = CandleUtil.findEngulf(_quotes, start_index, 90, true);
        _nBearishEngulfs = CandleUtil.findEngulf(_quotes, start_index, 90, false);
        _nBullishHaramis = CandleUtil.findHarami(_quotes, start_index, 1.1f, true);
        _nBearishHaramis = CandleUtil.findHarami(_quotes, start_index, 1.1f, false);
        _nDarkClouds = CandleUtil.findDarkCloud(_quotes, start_index);
        _nPiercing = CandleUtil.findPiercing(_quotes, start_index);
        _nHammer = CandleUtil.findHammerHangman(_quotes, start_index, 10, true);
        _nHangman = CandleUtil.findHammerHangman(_quotes, start_index, 10, false);
        _nInvHammer = CandleUtil.findInvHammerShootingStar(_quotes, start_index, 10, true);
        _nShootingStar = CandleUtil.findInvHammerShootingStar(_quotes, start_index, 10, false);
        _nKickerBull = CandleUtil.findKicker(_quotes, start_index, true);
        _nKickerBear = CandleUtil.findKicker(_quotes, start_index, false);
        _nPusherBull = CandleUtil.findPusher(_quotes, start_index, true);
        _nPusherBear = CandleUtil.findPusher(_quotes, start_index, false);
        _nGapBull = CandleUtil.findGap(_quotes, start_index, true);
        _nGapBear = CandleUtil.findGap(_quotes, start_index, false);
    }

    //----- accessors -----
    //get back a string of various 2 letter codes during most recent n days from start_index
    public ArrayList<CandleSignal> getSignals(int start_index) {
        ArrayList<CandleSignal> ret = new ArrayList<>();
        for (Integer idx : _nDojiTops) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.DojiTop); break;//found 1 is enough
        }
        for (Integer idx : _nBullishEngulfs) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BullishEngulfing); break;//found 1 is enough
        }
        for (Integer idx : _nBearishEngulfs) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BearishEngulfing); break;//found 1 is enough
        }
        for (Integer idx : _nBullishHaramis) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BullishHarami); break;//found 1 is enough
        }
        for (Integer idx : _nBearishHaramis) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BearishHarami); break;//found 1 is enough
        }
        for (Integer idx : _nDarkClouds) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.DarkCloud); break;//found 1 is enough
        }
        for (Integer idx : _nPiercing) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.Piercing); break;//found 1 is enough
        }
        for (Integer idx : _nHammer) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.Hammer); break;//found 1 is enough
        }
        for (Integer idx : _nHangman) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.HangingMan); break;//found 1 is enough
        }
        for (Integer idx : _nInvHammer) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.InvertedHammer); break;//found 1 is enough
        }
        for (Integer idx : _nShootingStar) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.ShootingStar); break;//found 1 is enough
        }
        for (Integer idx : _nKickerBull) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BullishKicker); break;//found 1 is enough
        }
        for (Integer idx : _nKickerBear) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BearishKicker); break;//found 1 is enough
        }
        for (Integer idx : _nPusherBull) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BullishPusher); break;//found 1 is enough
        }
        for (Integer idx : _nPusherBear) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BearishPusher); break;//found 1 is enough
        }
        for (Integer idx : _nGapBull) {
            if (idx > start_index) continue;//older
            ret.add(CandleSignal.BullishWindows); break;//found 1 is enough
        }
        return ret;
    }

    //----- variables -----
    private ArrayList<FundQuote> quotes;
    private ArrayList<Integer> _nDojiTops, _nBullishHaramis, _nBearishHaramis, _nBullishEngulfs, _nBearishEngulfs,
            _nDarkClouds, _nPiercing, _nHammer, _nHangman, _nInvHammer, _nShootingStar, _nPusherBull, _nPusherBear,
            _nKickerBull, _nKickerBear, _nGapBull, _nGapBear;
}
