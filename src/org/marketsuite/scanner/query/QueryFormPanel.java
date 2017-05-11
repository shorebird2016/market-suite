package org.marketsuite.scanner.query;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.WatchlistFilterPanel;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.Props;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.market.MarketUtil;
import org.marketsuite.framework.model.DivergenceOption;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

class QueryFormPanel extends JPanel {
    QueryFormPanel() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));

        //two tabs in center
        JTabbedPane tabs = new JTabbedPane();

        //common tab
        JPanel common_pnl = new JPanel(new MigLayout("insets 0, flowy, fill"));
        common_pnl.add(_pnlWatchlistFilter = new WatchlistFilterPanel(), "wrap, growx");
        _pnlWatchlistFilter.setEnabled(false);
        JPanel pnl = new JPanel(new MigLayout("insets 0, flowy"));
        pnl.add(_pnlPhaseFilter = new PhaseFilterPanel(), "grow");
        pnl.add(_pnlPriceVolume = new PriceVolumePanel(), "grow");
        pnl.add(_pnlMa = new MovingAveragePanel());
        common_pnl.add(new JScrollPane(pnl));
        tabs.add("Filters", common_pnl);

        //indicator tab
        JPanel indicator_pnl = new JPanel(new MigLayout("insets 0, wrap 2, flowy, fill"));
        indicator_pnl.add(_pnlCandle = new CandlePanel(), "grow");
        indicator_pnl.add(_pnlMacd = new MacdPanel(), "grow");
        indicator_pnl.add(_pnlVsq = new VsqPanel(), "grow");
        indicator_pnl.add(new AdxPanel(), "grow");
        indicator_pnl.add(_pnlDsto = new DstoWstoPanel(true), "grow");
        indicator_pnl.add(_pnlWsto = new DstoWstoPanel(false), "grow");
//        tabs.add(ApolloConstants.APOLLO_BUNDLE.getString("qp_tab2"), indicator_pnl);
        add(tabs, "dock center");
    }

    //----- protected methods -----
    boolean runQuery() {
        //those 2 filters require at least one selection
        if (_pnlWatchlistFilter.hasNoSelection() || _pnlPhaseFilter.isNoSelection()) {
            MessageBox.messageBox(MdiMainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                ApolloConstants.APOLLO_BUNDLE.getString("qp_55"),
                MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
            return false;
        }
        final ProgressBar pb = ProgressBar.getInstance(MdiMainFrame.getInstance(),
            FrameworkConstants.FRAMEWORK_BUNDLE.getString("scan_msg_1") + " " +
                ApolloConstants.APOLLO_BUNDLE.getString("qp_56"));
        pb.setVisible(true);
        Thread scan_thread = new Thread() {
            public void run() {
                results = new ArrayList<>();
                final ArrayList<String> symbols_wo_ibd = new ArrayList<>();
                ArrayList<String> symbols = _pnlWatchlistFilter.getSymbols();
                if (symbols == null) //use all symbols in DB
                    symbols = DataUtil.getAllSymbolsInDb();
                for (final String sym : symbols) {
                    MarketInfo mki;
                    try {
                        mki = MarketUtil.calcMarketInfo(sym,
                                FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
                    } catch (Exception e) {//unable to calculate mki
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("qp_rdmki") + " " + sym, LoggingSource.SCANNER_QUERY);
                            }
                        });
                        continue;
                    }
                    _pnlMacd.updateMacd(mki);
                    _pnlDsto.updateDsto(mki);
                    _pnlVsq.recalcBb(mki);
                    FundData fund = mki.getFund();
                    float cur_price = fund.getPrice(0);

                    //price volume ANDing
                    boolean no_etf = _pnlPriceVolume.isNoEtf();
                    Fundamental fdm = MainModel.getInstance().getFundamentals().get(sym);
                    if (no_etf && fdm != null && fdm.isETF()) continue;//skip here
                    boolean pir = _pnlPriceVolume.isPriceInRange(cur_price);
                    float[] va = mki.getVolumeAverage();
                    boolean vs = isNullOrEmpty(va) ? false : _pnlPriceVolume.isVolumeSufficient((int) va[0]);
                    String high_date = mki.getHighestDate();
                    boolean rh;
                    float recent_high = fund.findQuoteByDate(high_date).getClose();
                    rh = _pnlPriceVolume.isOffHigh(fund.getPrice(0), recent_high);
                    boolean pir_va = pir && vs && rh;

                    //phases ORing
                    String cur_phase = mki.getCurrentPhase();
                    boolean phase_filter = false;
                    if (cur_phase != null) {
                        boolean ph_bull = _pnlPhaseFilter.showBullish(cur_phase);
                        boolean ph_ww = _pnlPhaseFilter.isWeakWarning(cur_phase);
                        boolean ph_sw = _pnlPhaseFilter.isStrongWarning(cur_phase);
                        boolean ph_dist = _pnlPhaseFilter.isDistribution(cur_phase);
                        boolean ph_bear = _pnlPhaseFilter.isBearish(cur_phase);
                        boolean ph_rec = _pnlPhaseFilter.isRecovery(cur_phase);
                        boolean ph_accum = _pnlPhaseFilter.isAccumulation(cur_phase);
                        phase_filter = ph_bull || ph_ww || ph_sw || ph_dist || ph_bear || ph_rec || ph_accum;
                    }

                    //IBD rating ORing except ranges ANDing
                    ArrayList<IbdRating> ratings;
                    boolean ibd_hk = false;
                    try {
//                        DataUtil.adjustForSplits(fund, fund.getSize() - 1, 0);
                        WeeklyQuote wq = new WeeklyQuote(fund, 200);//TODO change to user defined range
                        ratings = IbdRating.readIbdWeeklyRating(sym, FrameworkConstants.DATA_FOLDER_IBD_RATING, wq);
                        boolean hu_sel = _pnlIbdFilter.isHookupSelected();
                        boolean hk_up = _pnlIbdFilter.isHookup(ratings) && IbdRating.doCompRsHookup(ratings)
                            && WeeklyQuote.doesPriceHookup(wq.getQuotes(), ratings.get(0).getDate());
                        boolean hd_sel = _pnlIbdFilter.isHookdownSelected();
                        boolean hk_dn = _pnlIbdFilter.isHookdown(ratings) && IbdRating.doCompRsHookdown(ratings)
                            && WeeklyQuote.doesPriceHookdown(wq.getQuotes(), ratings.get(0).getDate());
                        boolean comp_rng = _pnlIbdFilter.isCompositeInRange(ratings.get(0));
                        boolean rs_rng = _pnlIbdFilter.isRsInRange(ratings.get(0));
                        ibd_hk = (!hu_sel || hk_up) && (!hd_sel || hk_dn) && (comp_rng && rs_rng);
                    } catch (Exception e) {//can't read IBD rating for this symbol or rating is empty
//                        e.printStackTrace();
                        symbols_wo_ibd.add(sym);
                        ibd_hk = true; //ok not finding IBD rating
                    }

                    //moving average ANDing
                    float[] sma10 = mki.getSma10();
                    boolean m10 = true;
                    if (sma10 != null)
                        m10 = _pnlMa.is10MaInRange(Math.abs((sma10[0] - cur_price) / cur_price) * 100);
                    float[] sma20 = mki.getSma20();
                    boolean m20 = true;
                    if (sma20 != null)
                        m20 = _pnlMa.is20MaInRange(Math.abs((sma20[0] - cur_price) / cur_price) * 100);
                    float[] sma30 = mki.getSma30();
                    boolean m30 = true;
                    if (sma30 != null)
                        m30 = _pnlMa.is30MaInRange(Math.abs((sma30[0] - cur_price) / cur_price) * 100);
                    float[] sma50 = mki.getSma50();
                    boolean m50 = true;
                    if (sma50 != null)
                        m50 = _pnlMa.is50SmaPass(cur_price, sma50[0]);
                    float[] sma200 = mki.getSma200();//sometimes 200MA can't be calculated
                    boolean m200 = true;
                    if (sma200 != null)
                        m200 = isNullOrEmpty(sma200) ? false : _pnlMa.is200MaInRange(Math.abs((sma200[0] - cur_price) / cur_price) * 100);
                    float[] ema50 = mki.getEma50();
                    boolean em50 = isNullOrEmpty(ema50) ? false : _pnlMa.is50EmaInRange(Math.abs((ema50[0] - cur_price) / cur_price) * 100);
                    float[] ema120 = mki.getEma120();
                    boolean em120 = isNullOrEmpty(ema120) ? false : _pnlMa.is120EmaInRange(Math.abs((ema120[0] - cur_price) / cur_price) * 100);
                    float[] ema200 = mki.getEma200();
                    boolean em200 = isNullOrEmpty(ema200) ? false : _pnlMa.is200EmaInRange(Math.abs((ema200[0] - cur_price) / cur_price) * 100);
                    boolean ma = m10 && m20 && m30 && m50 && m200 && em50 && em120 && em200;

                    //candle ORing (not mutually exclusive)
                    boolean djt = _pnlCandle.isDojiPresent(fund.getQuote());
                    ArrayList<CandlePattern> cps = new ArrayList<>();
                    _mapCandlePattern.put(sym, cps);
                    if (djt) {
                        ArrayList<Integer> djts = _pnlCandle.getDojiTops();
                        if (djts != null)
                            for (Integer idx : djts) {
                                Calendar dt = fund.findDateByIndex(idx);
                                cps.add(new CandlePattern(dt, CandleSignal.DOJI_SPINTOP));
                            }
                    }
                    boolean egf = _pnlCandle.isEngulfPresent(fund.getQuote());
                    if (egf) {
                        ArrayList<Integer> engulfs = _pnlCandle.getEngulfs();
                        if (engulfs != null)
                            for (Integer idx : engulfs) {
                                Calendar dt = fund.findDateByIndex(idx);
                                cps.add(new CandlePattern(dt, CandleSignal.ENGULF));
                            }
                    }
                    boolean hmi = _pnlCandle.isHaramiPresent(fund.getQuote());
                    if (hmi) {
                        ArrayList<Integer> hmis = _pnlCandle.getHaramis();
                        if (hmis != null)
                            for (Integer idx : hmis) {
                                Calendar dt = fund.findDateByIndex(idx);
                                cps.add(new CandlePattern(dt, CandleSignal.HARAMI));
                            }
                    }
                    boolean candle = djt || egf || hmi;

                    //VSQ
                    boolean vsq = _pnlVsq.isSqueezed(mki);

                    //MACD ANDing
                    boolean macd_zcu = _pnlMacd.isZeroCrossUp(mki.getMacd()[1], mki.getMacd()[0]);//last -> now
                    boolean macd_zcd = _pnlMacd.isZeroCrossDown(mki.getMacd()[1], mki.getMacd()[0]);//last -> now
                    float[] macd_sig = mki.getMacdSig();
                    boolean macd_sigu = isNullOrEmpty(macd_sig) ? false : _pnlMacd.isSigCrossUp(mki.getMacd()[1], mki.getMacd()[0],
                            macd_sig[1], macd_sig[0]);
                    boolean macd_sigd = isNullOrEmpty(macd_sig) ? false : _pnlMacd.isSigCrossDn(mki.getMacd()[1], mki.getMacd()[0],
                            macd_sig[1], macd_sig[0]);
                    boolean macd = macd_zcu && macd_zcd && macd_sigu && macd_sigd;

                    //DSTO ANDing
                    boolean dsto_ob = _pnlDsto.isDstoOverbought(mki.getDsto()[0]);
                    boolean dsto_os = _pnlDsto.isDstoOversold(mki.getDsto()[0]);
                    boolean dsto_ca = _pnlDsto.isDstoCrossAbove(mki.getDsto()[1], mki.getDsto()[0]);
                    boolean dsto_cb = _pnlDsto.isDstoCrossBelow(mki.getDsto()[1], mki.getDsto()[0]);
                    boolean dsto_btwn = _pnlDsto.isDstoBetween(mki.getDsto()[0]);
                    boolean dsto = dsto_ob && dsto_os && dsto_ca && dsto_cb && dsto_btwn;

                    //WSTO

                //ADX
                    if (pir_va && phase_filter && ma && ibd_hk && vsq && macd && dsto && candle)
                        results.add(mki);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            pb.setLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("scan_msg_7") + " " + sym);
                        }
                    });
                }

                //after completion, show symbols_wo_ibd, update table, remove overlay
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        pb.setVisible(false);
                        Props.ScanComplete.setValue(null, results);
                        if (symbols_wo_ibd.size() > 0) {
                            StringBuilder buf = new StringBuilder(ApolloConstants.APOLLO_BUNDLE.getString("qp_noidb") + " " );
                            for (String s : symbols_wo_ibd)
                                buf.append(s).append(" ");
                            LogMessage.logSingleMessage(buf.toString(), LoggingSource.SCANNER_QUERY);
                        }
                    }
                });
            }
        };
        scan_thread.start();
        return true;
    }
    //retrieve candle pattern information from map
    ArrayList<CandlePattern> getCandlePatterns(final String symbol) {
        return _mapCandlePattern.get(symbol);
//        try {
//            MarketInfo mki = MarketUtil.calcMarketInfo(symbol,
//                FrameworkConstants.MARKET_QUOTE_LENGTH, new DivergenceOption(5, 90, 3));
//            ArrayList<CandlePattern> ret = new ArrayList<>();
//            ArrayList<Integer> occur_idx = _mapCandlePattern.get(symbol);
//            ArrayList<FundQuote> quotes = mki.getFund().getQuote();
//            for (Integer idx : occur_idx) {
//                Calendar cd_date = mki.getFund().findDateByIndex(idx);
//
//            }
//            return ret;
//        } catch (Exception e) {//unable to calculate mki
//            EventQueue.invokeLater(new Runnable() {
//                public void run() {
//                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("qp_rdmki") + " " + symbol, LoggingSource.SCANNER_QUERY);
//                }
//            });
//            return null;//can't get quote
//        }
    }

    private boolean isNullOrEmpty(float[] values) {
        if (values == null || values.length == 0) return true;
        return false;
    }

    //----- variables -----
    private WatchlistFilterPanel _pnlWatchlistFilter;
    private PriceVolumePanel _pnlPriceVolume;
    private PhaseFilterPanel _pnlPhaseFilter;
    private MovingAveragePanel _pnlMa;
    private IbdFilterPanel _pnlIbdFilter;
    private VsqPanel _pnlVsq;
    private CandlePanel _pnlCandle;
    private MacdPanel _pnlMacd;
    private DstoWstoPanel _pnlDsto, _pnlWsto;
    private ArrayList<MarketInfo> results = new ArrayList<>();
    private HashMap<String, ArrayList<CandlePattern>> _mapCandlePattern = new HashMap<>();
}
