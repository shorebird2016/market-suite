package org.marketsuite.chart.candle;

import org.marketsuite.component.layer.CrosshairLayerUI;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.chart.Line;
import org.marketsuite.framework.model.data.IbdRating;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.model.indicator.DSTO;
import org.marketsuite.framework.model.indicator.PersonPivot;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.AppUtil;
import org.marketsuite.framework.util.CandleUtil;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.layer.CrosshairLayerUI;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.indicator.BollingerBand;
import org.marketsuite.framework.model.indicator.DSTO;
import org.marketsuite.framework.model.indicator.PersonPivot;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MainModel;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

//a self-drawn candlestick chart panel with a cross hair cursor and data pad
public class CandleGraphPanel extends JPanel {
    public CandleGraphPanel() {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new MigLayout("insets 0"));
        _pnlCanvas = new JPanel() {//main body of painting entire view
            protected void paintComponent(Graphics g) {
                if (_PlotQuotes == null) return;
                if (!_bWeekly && _MarketInfo == null) return;
                Graphics2D g2d = (Graphics2D)g;

                //if only cursor movement, don't redraw candles all the time
                g2d.clearRect(0, 0, getWidth(), getHeight());//must have this to clear area, or it will pickup whatever was left in buffer

                //draw candles
                x_prev_comp = x_prev_rs = x_prev_eps = 0; //yprev_volavg = 0;
                x_prev_ubb = y_prev_ubb = x_prev_lbb = y_prev_lbb = 0; x_prev_pp = 0;
                x_prev_sto = y_prev_sto = x_prev_macd = y_prev_macd = x_prev_ovly = y_prev_ovly = 0;
                int plot_height = getHeight() - topMargin - bottomMargin;

                //translate data coordinates to screen coordinates: x, y ranges, data points
                //date axis
                _fXWidth = getWidth() - leftMargin - riteMargin;
                _fXGridWidth = _fXWidth / _PlotQuotes.size();//each day's width

                //price axis
                float price_range = _fMaxPrice - _fMinPrice;
                y_price_height = (int)(plot_height * DEFAULT_PRICE_HEIGHT_PERCENT);
                _fPriceHeight = plot_height * DEFAULT_PRICE_HEIGHT_PERCENT;//use float more accurate

                //draw border
//                g2d.setColor(Color.gray); g2d.setStroke(new BasicStroke(1));//TODO may use dotted line later
//                g2d.drawRect(leftMargin, topMargin, _fXWidth, y_price_height);

                //volume axis
                float vol_range = _fMaxVol - _fMinVol;
                int y_vol_height = (int)(plot_height * DEFAULT_VOL_HEIGHT_PERCENT);
                _fVolHeight = plot_height * DEFAULT_VOL_HEIGHT_PERCENT;
                int seg = 0;//each day a segment
                if (!_bWeekly) {
                    _ema8 = _MarketInfo.getEma8(); _sma20 = _MarketInfo.getSma20(); _sma50 = _MarketInfo.getSma50();
                    _volAvg = _MarketInfo.getVolumeAverage();
                }
                float prev_close = 0; int yprev_ema8 = 0, yprev_sma50 = 0, yprev_sma20 = 0, yprev_sma200 = 0,
                    yprev_volavg = (int)_volAvg[_PlotQuotes.size() - 1];

                //pre-load earning dates
                ArrayList<Calendar> earning_dates = MainModel.getInstance().getEarningDates(_PlotQuotes.get(0).getSymbol());

                //traverse all quotes from earliest to recent (left to right)
                for (int idx = _PlotQuotes.size() - 1; idx >= 0; idx--) {
                    FundQuote quote = _PlotQuotes.get(idx);
                    float body_top = Math.max(quote.getOpen(), quote.getClose());
                    float body_bottom = Math.min(quote.getOpen(), quote.getClose());
                    int x = (int)((seg + 0.5) * _fXGridWidth) + leftMargin; seg++;

                    //covert y into screen coordinates, distance from top
                    int yshadow_top = y_price_height - (int)(y_price_height * (quote.getHigh() - _fMinPrice) / price_range) + topMargin;
                    int ybody_top = y_price_height - (int)(y_price_height * (body_top - _fMinPrice) / price_range) + topMargin;
                    int ybody_bottom = y_price_height - (int)(y_price_height * (body_bottom - _fMinPrice) / price_range) + topMargin;
                    int yshadow_bottom = y_price_height - (int)(y_price_height * (quote.getLow() - _fMinPrice) / price_range) + topMargin;

                    //close higher than previous day, green; lower use red paint
                    Color color = new Color(0, 0, 0, _intensityPrice); boolean down_day = false;
                    if (quote.getClose() > prev_close) color = new Color(14, 130, 95, _intensityPrice);
                    else if (quote.getClose() < prev_close) {
                        color = new Color(255, 0, 0, _intensityPrice); down_day = true; }
                    prev_close = quote.getClose();
                    g2d.setPaint(color);
                    g2d.setStroke(new BasicStroke(2));

                    //draw top shadow : high to max(open, close)
                    g2d.drawLine(x, yshadow_top, x, ybody_top);
                    int body_height = ybody_bottom - ybody_top; if (body_height == 0) body_height = 1;//at least 1 doji
                    int x_half_width = Math.round(_fXGridWidth / 4);
                    if (down_day) {//already red color
                        if (quote.getClose() >= quote.getOpen())
                            g2d.drawRect(x - x_half_width, ybody_top, 2 * x_half_width, body_height);
                        else
                            g2d.fillRect(x - x_half_width, ybody_top, 2 * x_half_width, body_height);
                    }
                    else {//green color
                        if (quote.getClose() > quote.getOpen())
                            g2d.drawRect(x - x_half_width, ybody_top, 2 * x_half_width, body_height);
                        else
                            g2d.fillRect(x - x_half_width, ybody_top, 2 * x_half_width, body_height);
                    }
                    g2d.drawLine(x, ybody_bottom, x, yshadow_bottom);

                    //connect T line
                    int yema8 = y_price_height - (int)(y_price_height * (_ema8[idx] - _fMinPrice) / price_range) + topMargin;
//                    g2d.setPaint(new Color(175, 23, 217, _intensityIndicator/*120*/)); g2d.setStroke(new BasicStroke(1));
//                    if (idx < _PlotQuotes.size() - 1)
//                        g2d.drawLine(x, yprev_ema8, (int)(x + _fXGridWidth), yema8);
                    yprev_ema8 = yema8;

                    //connect 20MA, only for daily
                    if (!_bWeekly) {
                        int ysma20 = y_price_height - (int)(y_price_height * (_sma20[idx] - _fMinPrice) / price_range) + topMargin;
                        g2d.setPaint(new Color(40, 255, 100, _intensityIndicator)); g2d.setStroke(new BasicStroke(2));
                        if (idx < _PlotQuotes.size() - 1)
                            g2d.drawLine(x, yprev_sma20, (int)(x + _fXGridWidth), ysma20);
                        yprev_sma20 = ysma20;
                    }

                    //connect 50MA
                    int ysma50 = y_price_height - (int)(y_price_height * (_sma50[idx] - _fMinPrice) / price_range) + topMargin;
                    g2d.setPaint(new Color(37, 34, 255, _intensityIndicator)); g2d.setStroke(new BasicStroke(2));
                    if (idx < _PlotQuotes.size() - 1)
                        g2d.drawLine(x, yprev_sma50, (int)(x + _fXGridWidth), ysma50);
                    yprev_sma50 = ysma50;

                    //connect 200MA for weekly only
                    if (_bWeekly) {
                        int ysma200 = y_price_height - (int)(y_price_height * (_sma200[idx] - _fMinPrice) / price_range) + topMargin;
                        g2d.setPaint(new Color(240, 215, 36, _intensityIndicator/*215*/)); g2d.setStroke(new BasicStroke(2));
                        if (idx < _PlotQuotes.size() - 1)
                            g2d.drawLine(x, yprev_sma200, (int)(x + _fXGridWidth), ysma200);
                        yprev_sma200 = ysma200;
                    }

                    //draw volume bars, use some transparency
                    g2d.setPaint(new Color(14, 130, 95, _intensityVol)); g2d.setStroke(new BasicStroke(1));
                    if (down_day)
                        g2d.setPaint(new Color(217, 45, 69, _intensityVol));
                    int yvol = getHeight() - bottomMargin - (int)(y_vol_height * (quote.getVolume() - _fMinVol) / vol_range);
                    g2d.fillRect((int)(x - _fXGridWidth / 4), yvol, (int)(_fXGridWidth / 2), getHeight() - bottomMargin);

                    //average volume
                    g2d.setPaint(new Color(77, 7, 184, _intensityVol)); g2d.setStroke(new BasicStroke(1));
                    int yvol_avg = getHeight() - bottomMargin - (int)(y_vol_height * (_volAvg[idx] - _fMinVol) / vol_range);
                    if (_volAvg[idx] > 0)//skip cells w no avg volume
                        g2d.drawLine(x, yprev_volavg, (int) (x + _fXGridWidth), yvol_avg);
                    yprev_volavg = yvol_avg;

                    //candle signals
//                    if (signalSettings == null || signalSettings.size() == 0) continue;//don't show candle signal
                    if (signalSettings != null && signalSettings.size() > 0) {
                        //turn on signals that user selected (default all on)
                        //candle signals, H = harami, DT = doji/top, E = engulf, below shadow bottom by 5 pixels
                        g2d.setPaint(new Color(15, 15, 31, _intensityPrice)); int vg = 10;
                        int cx = (int)(x - _fXGridWidth / 4); int cy = yshadow_bottom + y_price_height / 30; boolean cyp = false;
                        if (signalSettings.contains(CandleSignal.DojiTop)) {
                            for (Integer dt_idx : _nDojiTops)
                                if (idx == dt_idx) { g2d.drawString(CandleSignal.DojiTop.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BullishEngulfing)) {
                            for (Integer eg_idx : _nBullishEngulfs)
                                if (idx == eg_idx) { g2d.drawString(CandleSignal.BullishEngulfing.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BearishEngulfing)) {
                            for (Integer eg_idx : _nBearishEngulfs)
                                if (idx == eg_idx) { g2d.drawString(CandleSignal.BearishEngulfing.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BullishHarami)) {
                            for (Integer h_idx : _nBullishHaramis)
                                if (idx == h_idx) { g2d.drawString(CandleSignal.BullishHarami.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BearishHarami)) {
                            for (Integer h_idx : _nBearishHaramis)
                                if (idx == h_idx) { g2d.drawString(CandleSignal.BearishHarami.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.DarkCloud)) {
                            for (Integer dc_idx : _nDarkClouds)
                                if (idx == dc_idx) { g2d.drawString(CandleSignal.DarkCloud.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.Piercing)) {
                            for (Integer pc_idx : _nPiercing)
                                if (idx == pc_idx) { g2d.drawString(CandleSignal.Piercing.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.Hammer)) {
                            for (Integer hm_idx : _nHammer)
                                if (idx == hm_idx) { g2d.drawString(CandleSignal.Hammer.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.HangingMan)) {
                            for (Integer hm_idx : _nHangman)
                                if (idx == hm_idx) { g2d.drawString(CandleSignal.HangingMan.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.InvertedHammer)) {
                            for (Integer ss_idx : _nInvHammer)
                                if (idx == ss_idx) { g2d.drawString(CandleSignal.InvertedHammer.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.ShootingStar)) {
                            for (Integer ss_idx : _nShootingStar)
                                if (idx == ss_idx) { g2d.drawString(CandleSignal.ShootingStar.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BullishPusher)) {
                            for (Integer bp_idx : _nPusherBull)
                                if (idx == bp_idx) { g2d.drawString(CandleSignal.BullishPusher.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BearishPusher)) {
                            for (Integer bp_idx : _nPusherBear)
                                if (idx == bp_idx) { g2d.drawString(CandleSignal.BearishPusher.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BullishKicker)) {
                            for (Integer bk_idx : _nKickerBull)
                                if (idx == bk_idx) { g2d.drawString(CandleSignal.BullishKicker.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BearishKicker)) {
                            for (Integer bk_idx : _nKickerBear)
                                if (idx == bk_idx) { g2d.drawString(CandleSignal.BearishKicker.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BullishWindows)) {
                            for (Integer gap_idx : _nGapBull)
                                if (idx == gap_idx) { g2d.drawString(CandleSignal.BullishWindows.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                        if (signalSettings.contains(CandleSignal.BearishWindows)) {
                            for (Integer gap_idx : _nGapBear)
                                if (idx == gap_idx) { g2d.drawString(CandleSignal.BearishWindows.getCode(), cx, cy); cyp = true; }
                            if (cyp) cy += vg; cyp = false;
                        }
                    }

                    //check if earning date matches this quote
                    if (_bShowEarning) {
                        String date = quote.getDate();
                        if (earning_dates != null) {
                            for (Calendar cal : earning_dates) {
                                String dt = AppUtil.calendarToString(cal);
                                if (dt.equals(date)) {//found match
                                    drawEarning(g, x, yshadow_top);
                                    break;
                                }
                            }
                        }
                    }
                    if (_bShowRating) drawIbdRating(g2d, idx, x);
                    if (_bShowBollingerBand) drawBollingerBand(g2d, idx, x);
                    if (_bShowStochastic) drawStochastic(g2d, idx, x);
                    if (_bShowMacd) drawMacd(g2d, idx, x);
                    if (_OverlayQuotes != null) drawOverlaySymbol(g2d, idx, x);

                    //draw pivot lines
                    if (_bShowPivot) {
                        drawPivot(g2d, idx, x);
                    }

                }

                //draw data pad area, do this last to be top of Z-axis
                drawDataPad(g2d, _ActiveQuote != null ? _ActiveQuote : _PlotQuotes.get(0));
                if (_bShowStochastic) drawStoBounds(g2d);
                if (_bShowMacd) drawMacdLine(g2d);

                //draw lines
                g2d.setPaint(new Color(213, 140, 82, 155)); g2d.setStroke(new BasicStroke(5));
                if (_Lines.size() > 0) {
                    for (Line line : _Lines) {
                        if (!line.isEmpty())
                            line.draw(g2d);
                    }
                }

                //future pivot
                if (_bShowPivot)  drawFuturePivot(g2d);
            }
        };
        JLayer<JPanel> layer = new JLayer<>(_pnlCanvas, new CrosshairLayerUI());
        add(layer, "dock center");

        //listen to mouse movement, update datapad, watch to draw new lines
        _pnlCanvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mev) {
                //clear lines
                if (SwingUtilities.isRightMouseButton(mev)) {
                    clearLines();
                    return;
                }

                //draw line
                if (!_bLineClick1) {//new line, first click
                    Line new_line = new Line(mev.getPoint()); _Lines.add(new_line);
                    _bLineClick1 = true;
                }
                else {
                    _Lines.get(_Lines.size() - 1).setEndPoint(mev.getPoint());
                    _bLineClick1 = false;
                }
            }
        });
        _pnlCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                //update datapad
                if (_PlotQuotes == null) return;
                if (!_bWeekly && _MarketInfo == null) return;
                _nActiveIndex = !_bWeekly ? _PlotQuotes.size() - (int) (e.getX() / _fXGridWidth) - 1 :
                        _PlotQuotes.size() - (int) (e.getX() / _fXGridWidth) - 1; //from latest date
                if (_nActiveIndex < 0 || _nActiveIndex >= _PlotQuotes.size()) return;//outside chart,
                _ActiveQuote = _PlotQuotes.get(_nActiveIndex);
                _nCursorY = e.getY();
                if (_bShowBollingerBand) {
                    _fCurUpperBB = _BollingerBand.getUpperBand()[_nActiveIndex];
                    _fCurLowerBB = _BollingerBand.getLowerBand()[_nActiveIndex];
                }

                // prevent outside rating data range
                if (_bShowRating && _nActiveIndex < _IbdRatings.size() ) {
                    _nCurComp = _IbdRatings.get(_nActiveIndex).getComposite();
                    _nCurRs = _IbdRatings.get(_nActiveIndex).getRsRating();
                    _nCurEps = _IbdRatings.get(_nActiveIndex).getEpsRating();
                }
                if (_bShowStochastic) {
//TODO
                }
                if (_bShowMacd) {
//TODO
                }
            }
        });
    }

    //----- public methods -----
    //plot w new set of information, mainly from daily chart
    public void plot(MarketInfo mki, ArrayList<FundQuote> plot_quotes, ArrayList<CandleSignal> settings) {
//        if (settings == null) setDefaultSelection(); else signalSettings = settings;
        _MarketInfo = mki; _PlotQuotes = plot_quotes;
        preparePlotData(); clearLines();
        repaint();
    }
    //special plot for weekly quotes with supplied moving averages
    public void plot(ArrayList<FundQuote> plot_quotes, float[] sma50, float[] sma200, float[] ema8, float[] vol_avg ,
                     ArrayList<CandleSignal> settings) {
//        if (settings == null) setDefaultSelection(); else signalSettings = settings;
        _bWeekly = true;  _PlotQuotes = plot_quotes;
        _sma50 = sma50; _sma200 = sma200; _ema8 = ema8; _volAvg = vol_avg;
        preparePlotData(); clearLines();
        repaint();
    }
    public void plotOverlay(ArrayList<FundQuote> overlay_quotes) {
        //calc overlay bounds
        _OverlayQuotes = overlay_quotes;
        _fMaxOverlay = Float.MIN_VALUE; _fMinOverlay = Float.MAX_VALUE;
        for (FundQuote quote : _OverlayQuotes) {
            if (quote.getHigh() > _fMaxOverlay) _fMaxOverlay = quote.getHigh();
            if (quote.getLow() < _fMinOverlay) _fMinOverlay = quote.getLow();
        }
        repaint();
    }
    public void toggleRating() {//toggle
        if (!_bWeekly) return;
        _bShowRating = !_bShowRating;
        if (_bShowRating) {
            /*_bShowRating = false; */pad_height = 180;
        }
        else {
            /*_bShowRating = true; */pad_height = 220;
        }
        adjustIntensity();
        repaint();
    }
    public void toggleBB() {//toggle
//        if (_bShowBollingerBand) _bShowBollingerBand = false;
//        else _bShowBollingerBand = true;
        _bShowBollingerBand = !_bShowBollingerBand;
        adjustIntensity();
        repaint();
    }
    public void toggleStochastic() {//toggle
//        if (_bShowStochastic) _bShowStochastic = false;
//        else _bShowStochastic = true;
        _bShowStochastic = !_bShowStochastic;
        adjustIntensity();
        repaint();
    }
    public void toggleMacd() {
//        if (_bShowMacd) _bShowMacd = false;
//        else _bShowMacd = true;
        _bShowMacd = !_bShowMacd;
        adjustIntensity();
        repaint();
    }
    public void toggleEarning() { _bShowEarning = !_bShowEarning; repaint(); }
    public void togglePivot() {
        _bShowPivot = !_bShowPivot; repaint();
    }
    //cycle position of data pad around 4 corners of chart window
    void moveDatapad() {
        if (_nPadY == 2 && _nPadX == _pnlCanvas.getWidth() - pad_width/*112*/) //right, upper --> right, lower
            _nPadY = _pnlCanvas.getHeight() - pad_height;//202;
        else if (_nPadY == 2 && _nPadX == 2) //left/upper --> right/upper
            _nPadX = _pnlCanvas.getWidth() - pad_width/*112*/;
        else if (_nPadY == _pnlCanvas.getHeight() - pad_height/*202*/ && _nPadX == _pnlCanvas.getWidth() - pad_width/*112*/) //right/lower --> left/lower
            _nPadX = 2;
        else //left lower --> left upper
            _nPadY = 2;
        repaint();
    }
    void clearLines() { _Lines = new ArrayList<>(); _bLineClick1 = false; }

    //----- private methods -----
    private void preparePlotData() {//calculate once per plot call
        //price and volume bounds
        _fMaxPrice = Float.MIN_VALUE;  _fMinPrice = Float.MAX_VALUE;
        _fMaxVol = Float.MIN_VALUE;  _fMinVol = Float.MAX_VALUE;
        for (FundQuote quote : _PlotQuotes) {
            if (quote.getHigh() > _fMaxPrice) _fMaxPrice = quote.getHigh();
            if (quote.getLow() < _fMinPrice) _fMinPrice = quote.getLow();
            if (quote.getVolume() > _fMaxVol) _fMaxVol = quote.getVolume();
            if (quote.getVolume() < _fMinVol) _fMinVol = quote.getVolume();
        }
        _nDojiTops = CandleUtil.findDojiSpintop(_PlotQuotes, -1, 20);
        _nBullishEngulfs = CandleUtil.findEngulf(_PlotQuotes, -1, 90, true);
        _nBearishEngulfs = CandleUtil.findEngulf(_PlotQuotes, -1, 90, false);
        _nBullishHaramis = CandleUtil.findHarami(_PlotQuotes, -1, 1.1f, true);
        _nBearishHaramis = CandleUtil.findHarami(_PlotQuotes, -1, 1.1f, false);
        _nDarkClouds = CandleUtil.findDarkCloud(_PlotQuotes, -1);
        _nPiercing = CandleUtil.findPiercing(_PlotQuotes, -1);
        _nHammer = CandleUtil.findHammerHangman(_PlotQuotes, -1, 10, true);
        _nHangman = CandleUtil.findHammerHangman(_PlotQuotes, -1, 10, false);
        _nInvHammer = CandleUtil.findInvHammerShootingStar(_PlotQuotes, -1, 10, true);
        _nShootingStar = CandleUtil.findInvHammerShootingStar(_PlotQuotes, -1, 10, false);
        _nKickerBull = CandleUtil.findKicker(_PlotQuotes, -1, true);
        _nKickerBear = CandleUtil.findKicker(_PlotQuotes, -1, false);
        _nPusherBull = CandleUtil.findPusher(_PlotQuotes, -1, true);
        _nPusherBear = CandleUtil.findPusher(_PlotQuotes, -1, false);
        _nGapBull = CandleUtil.findGap(_PlotQuotes, -1, true);
        _nGapBear = CandleUtil.findGap(_PlotQuotes, -1, true);

        //indicators
        if (_bWeekly)
            _BollingerBand = new BollingerBand(20, 2, 2, _WeeklyQuote.getQuotes());
        else
            _BollingerBand = _MarketInfo.getBollingerBand();
        x_prev_ubb = y_prev_ubb = x_prev_lbb = y_prev_lbb = 0;
        if (_bWeekly) { //IBD Rating only under weekly
            try {//only for weekly chart
                _IbdRatings = IbdRating.readIbdWeeklyRating(_PlotQuotes.get(0).getSymbol(),
                    FrameworkConstants.DATA_FOLDER_IBD_RATING, _WeeklyQuote);
            } catch (IOException e) {
                LogMessage.logSingleMessage(ApolloConstants.APOLLO_BUNDLE.getString("wc_01") + " "
                        + _PlotQuotes.get(0).getSymbol(), LoggingSource.DAILY_CANDLE_CHART);
            }
            _Stochastic = new DSTO(14, 3, _PlotQuotes).getPctK();
            _Macd = null;//IndicatorUtil.calcMACD(12, 26, start_index - 26, fund);
        }
        else {
            _Stochastic = _MarketInfo.getDstoStd();//start w standard DSTO
            _Macd = _MarketInfo.getMacd();
        }
        x_prev_sto = y_prev_sto = 0;//reset each time
        x_prev_macd = y_prev_macd = 0;

        //find max and min of MACD
        _fMaxMacd = Float.MIN_VALUE; _fMinMacd = Float.MAX_VALUE;
        if (_Macd == null) return;
        for (int idx = 0; idx < _Macd.length; idx++) {
            if (_Macd[idx] > _fMaxMacd) _fMaxMacd = _Macd[idx];
            else if (_Macd[idx] < _fMinMacd) _fMinMacd = _Macd[idx];
        }

        //person's pivot calculation
        _Pivot = new PersonPivot(_PlotQuotes, _nPivotInterval);

//        _fMaxBB = Float.MIN_VALUE; _fMinBB = Float.MAX_VALUE;
//        float[] ub = _BollingerBand.getUpperBand();
//        for (int idx = 0; idx < ub.length; idx++) {
//            if (ub[idx] > _fMaxBB) _fMaxBB = ub[idx];
//            if (ub[idx] < _fMinBB) _fMinBB = ub[idx];
//        }
//        float[] lb = _BollingerBand.getLowerBand();
//        for (int idx = 0; idx < ub.length; idx++) {
//            if (lb[idx] > _fMaxBB) _fMaxBB = ub[idx];
//            if (lb[idx] < _fMinBB) _fMinBB = ub[idx];
//        }
    }
    private void drawDataPad(Graphics2D g2d, FundQuote quote) {//TODO Objectify this window
        //save previous font, calc various font info
        final Font cur_font = g2d.getFont();
        final FontMetrics param_font_metrics = g2d.getFontMetrics(cur_font);
        final Font value_font = cur_font.deriveFont(cur_font.getStyle() | Font.BOLD, (float)cur_font.getSize());
        final FontMetrics value_font_metrics = g2d.getFontMetrics(value_font);
        final Font date_font = cur_font.deriveFont((float) cur_font.getSize());
        g2d.setPaint(new Color(255, 255, 153, 175)); //g2d.setComposite(ChartLayerUI.makeComposite(0.75f));
        g2d.setStroke(new BasicStroke(1));
        g2d.fillRoundRect(_nPadX, _nPadY, pad_width, pad_height, 15, 15);
        g2d.setPaint(new Color(255, 204, 0));
        g2d.drawRoundRect(_nPadX, _nPadY, pad_width, pad_height, 15, 15);
        g2d.setFont(date_font); g2d.setPaint(new Color(81, 81, 242));//blueish
        g2d.drawString(quote.getDate(), _nPadX + 20, _nPadY + 10);

        //for open, high, low, close, volume labels
        int x = _nPadX + 3;
        int y = _nPadY + 10; g2d.setPaint(Color.black); g2d.setFont(cur_font);
        float hinc = param_font_metrics.getHeight();
        g2d.drawString("Open:", x, y += hinc);
        g2d.drawString("High:", x, y += hinc);
        g2d.drawString("Low:", x, y += hinc);
        g2d.drawString("Close:", x, y += hinc);
        g2d.setPaint(new Color(184, 103, 10, 184));
        g2d.drawLine(x, (int)(y + 0.25 * hinc), x + pad_width - 2, (int)(y + 0.25 * hinc));//-2 to avoid protruding
        g2d.setPaint(Color.black);
        g2d.drawString("Volume:", x, y += hinc);
        g2d.drawString("Vol Avg:", x, y += hinc);
        g2d.drawString("Cursor:", x, y += hinc);

        //show indicator if on
//        g2d.setStroke(new BasicStroke(1));
        g2d.setPaint(new Color(184, 103, 10, 184));
        g2d.drawLine(x, (int)(y + 0.25 * hinc), x + pad_width - 2, (int)(y + 0.25 * hinc));
        g2d.setPaint(Color.black);
        g2d.drawString("Upper BBand:", x, y += hinc);
        g2d.drawString("Lower BBand:", x, y += hinc);
        g2d.drawString("Stochastic %K:", x, y += hinc);
        g2d.drawString("MACD:", x, y += hinc);

        //display rating if on
        if (_bShowRating) {
            g2d.setPaint(new Color(184, 103, 10, 184));
            g2d.drawLine(x, (int)(y + 0.25 * hinc), x + pad_width - 2, (int)(y + 0.25 * hinc));
            g2d.setPaint(Color.black);
            g2d.drawString("Composite:", x, y += hinc);
            g2d.drawString("RS:", x, y += hinc);
            g2d.drawString("EPS:", x, y += hinc);
        }

        //values - price, MAs
        int padding = 3;
        y = _nPadY + 10; g2d.setFont(value_font);
        String val = FrameworkConstants.PRICE_FORMAT.format(quote.getOpen());//open
        g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        val = FrameworkConstants.PRICE_FORMAT.format(quote.getHigh());//high
        g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        val = FrameworkConstants.PRICE_FORMAT.format(quote.getLow());//low
        g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        val = FrameworkConstants.PRICE_FORMAT.format(quote.getClose());//close
        g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        val = FrameworkConstants.VOLUME_FORMAT2.format(quote.getVolume());//volume
        g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        if (_nActiveIndex >= 0 && _nActiveIndex < _volAvg.length) {
            val = FrameworkConstants.VOLUME_FORMAT2.format(_volAvg[_nActiveIndex]);//volume
            g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        }

        //current y value
        float cur_price = _fMinPrice + (_fMaxPrice - _fMinPrice) * ((y_price_height - _nCursorY + topMargin) / y_price_height);
        val = FrameworkConstants.PRICE_FORMAT.format(cur_price);
        g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);

        //indicators
        if (_bShowBollingerBand) {
            val = FrameworkConstants.PRICE_FORMAT.format(_fCurUpperBB);//upper Bollinger
            g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
            val = FrameworkConstants.PRICE_FORMAT.format(_fCurLowerBB);//lower Bollinger
            g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        }
        else y += 2 * hinc;

        //Stochastics, MACD...
        if (_bShowStochastic && _nActiveIndex >= 0 && _nActiveIndex < _Stochastic.length) {
            val = FrameworkConstants.PRICE_FORMAT.format(_Stochastic[_nActiveIndex]);//%K
            g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        } else y += hinc;
        if (_bShowMacd && _nActiveIndex >= 0 && _nActiveIndex < _Macd.length) {
            val = FrameworkConstants.PRICE_FORMAT.format(_Macd[_nActiveIndex]);
            g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        } else y += hinc;

        //IBD Rating
        if (_bShowRating) {
            val = String.valueOf(_nCurComp);
            g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
            val = String.valueOf(_nCurRs);
            g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
            val = String.valueOf(_nCurEps);
            g2d.drawString(val, x + pad_width - value_font_metrics.stringWidth(val) - padding, y += hinc);
        }
    }
    private void setDefaultSelection() { //default set all signals on
        signalSettings = new ArrayList<>();
        CandleSignal[] consts = CandleSignal.values();
        for (CandleSignal cs : consts)
            signalSettings.add(cs);
    }
    private void drawIbdRating(Graphics2D g2d, int quote_index, int x_coord) {
        if (_IbdRatings == null) return;//don't plot

        //for each quote, find matching entry in rating array via quote_date
        String quote_date = _PlotQuotes.get(quote_index).getDate();
        for (int idx = _IbdRatings.size() - 1; idx >= 0; idx--) {
            IbdRating rating = _IbdRatings.get(idx);
            String date = AppUtil.calendarToString(rating.getDate());
            if (date.equals(quote_date)) { //draw 3 symbols: composite, EPS, RS
                //composite
                float y_comp = (100 * (_fPriceHeight + topMargin) - rating.getComposite() * _fPriceHeight) / 100;
                int y_cur_comp = (int) y_comp;
                g2d.setPaint(new Color(221, 89, 5, _intensityOverlay));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x_coord - 3, y_cur_comp - 3, 6, 6);//small rectangle
                g2d.setStroke(new BasicStroke(2));
                if (x_prev_comp > 0) //skip first point
                    g2d.drawLine(x_coord, y_cur_comp, x_prev_comp, y_prev_comp);
                x_prev_comp = x_coord; y_prev_comp = y_cur_comp;

                //rs
                float y_rs = (100 * (_fPriceHeight + topMargin) - rating.getRsRating() * _fPriceHeight) / 100;
                int y_cur_rs = (int) y_rs;
                g2d.setPaint(new Color(21, 144, 175, _intensityOverlay));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x_coord - 3, y_cur_rs - 3, 6, 6);
                g2d.setStroke(new BasicStroke(2));
                if (x_prev_rs > 0) //skip first point
                    g2d.drawLine(x_coord, y_cur_rs, x_prev_rs, y_prev_rs);
                x_prev_rs = x_coord; y_prev_rs = y_cur_rs;

                //eps
                float y_eps = (100 * (_fPriceHeight + topMargin) - rating.getEpsRating() * _fPriceHeight) / 100;
                int y_cur_eps = (int) y_eps;
                g2d.setPaint(new Color(38, 222, 49, _intensityOverlay));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x_coord - 3, y_cur_eps - 3, 6, 6);
                g2d.setStroke(new BasicStroke(2));
                if (x_prev_eps > 0) //skip first point
                    g2d.drawLine(x_coord, y_cur_eps, x_prev_eps, y_prev_eps);
                x_prev_eps = x_coord; y_prev_eps = y_cur_eps;
            }
        }
    }
    private void drawBollingerBand(Graphics2D g2d, int quote_index, int x_coord) {
        if (_BollingerBand == null) return;
        float price_range = _fMaxPrice - _fMinPrice;
        float y_upper = _fPriceHeight + topMargin - _fPriceHeight * (_BollingerBand.getUpperBand()[quote_index] - _fMinPrice) / price_range;
        g2d.setPaint(new Color(16, 218, 211, _intensityOverlay));
        g2d.setStroke(new BasicStroke(2));
        if (x_prev_ubb > 0) //skip first point
            g2d.drawLine(x_coord, (int)y_upper, x_prev_ubb, y_prev_ubb);
        x_prev_ubb = x_coord; y_prev_ubb = (int)y_upper;
        float y_lower = _fPriceHeight + topMargin - _fPriceHeight * (_BollingerBand.getLowerBand()[quote_index] - _fMinPrice) / price_range;
        if (x_prev_lbb > 0) //skip first point
            g2d.drawLine(x_coord, (int)y_lower, x_prev_lbb, y_prev_lbb);
        x_prev_lbb = x_coord; y_prev_lbb = (int)y_lower;
    }
    private void drawStochastic(Graphics2D g2d, int quote_index, int x_coord) {//use volume area to draw
        if (_Stochastic == null) return;
        float y_sto = 100 * _Stochastic[quote_index] / _fVolHeight;
        float y_coord = _pnlCanvas.getHeight() - y_sto - bottomMargin;
        g2d.setPaint(new Color(45, 65, 255, _intensityOverlay));
        g2d.setStroke(new BasicStroke(2));
        if (x_prev_sto > 0) //skip first point
            g2d.drawLine(x_coord, (int)y_coord, x_prev_sto, y_prev_sto);
//TODO draw %D
        x_prev_sto = x_coord; y_prev_sto = (int)y_coord;
    }
    private void drawStoBounds(Graphics2D g2d) {
        g2d.setPaint(new Color(221, 12, 26, _intensityOverlay));
        g2d.setStroke(new BasicStroke(1));
        float y_sto = 100 * 80 / _fVolHeight;
        int y_coor = (int) (_pnlCanvas.getHeight() - y_sto - bottomMargin);
        g2d.drawLine(leftMargin, y_coor, (int) (_fXWidth - riteMargin), y_coor);
        g2d.setPaint(new Color(15, 130, 40, _intensityOverlay));
        y_sto = 100 * 20 / _fVolHeight;
        y_coor = (int) (_pnlCanvas.getHeight() - y_sto - bottomMargin);
        g2d.drawLine(leftMargin, y_coor, (int)(_fXWidth - riteMargin), y_coor);
    }
    private void drawMacd(Graphics2D g2d, int quote_index, int x_coord) {
        if (_Macd == null) return;
        float macd_range = _fMaxMacd - _fMinMacd;
        float y_macd = _fVolHeight * (_Macd[quote_index] - _fMinMacd) / macd_range;
        float y_coord = _pnlCanvas.getHeight() - y_macd - bottomMargin;
        g2d.setPaint(new Color(45, 65, 255, _intensityOverlay));//same color as STO
        g2d.setStroke(new BasicStroke(2));
        if (x_prev_macd > 0)
            g2d.drawLine(x_coord, (int)y_coord, x_prev_macd, y_prev_macd);
//TODO signal line
        x_prev_macd = x_coord; y_prev_macd = (int)y_coord;
    }
    private void drawMacdLine(Graphics2D g2d) {
        g2d.setPaint(new Color(217, 118, 136, _intensityOverlay));
        g2d.setStroke(new BasicStroke(1));
        float y_macd = _fVolHeight * (0 - _fMinMacd) / (_fMaxMacd - _fMinMacd);
        int y_coor = (int)(_pnlCanvas.getHeight() - y_macd - bottomMargin);
        g2d.drawLine(leftMargin, y_coor, (int)(_fXWidth - riteMargin), y_coor);
    }
    private void drawOverlaySymbol(Graphics2D g2d, int quote_index, int x_coord) {
        g2d.setPaint(new Color(53, 53, 53));//, _intensityOverlay));
        g2d.setStroke(new BasicStroke(1));
        FundQuote quote = _OverlayQuotes.get(quote_index);
        float yshadow_top = _fPriceHeight - (_fPriceHeight * (quote.getClose() - _fMinOverlay) / (_fMaxOverlay - _fMinOverlay)) + topMargin;
        int y_coor = (int)yshadow_top;
        if (x_prev_ovly > 0) //skip first point
            g2d.drawLine(x_coord, y_coor, x_prev_ovly, y_prev_ovly);
        x_prev_ovly = x_coord; y_prev_ovly = y_coor;
    }
    private void drawEarning(Graphics g, int x, int y) {
        //draw a little symbol representing earning date
        FrameworkIcon icon = FrameworkIcon.DOLLAR_24;
        int y_icon = y - icon.getIconHeight();
        if (y_icon <= topMargin)
            y_icon = y;//don't put outside of chart
        g.drawImage(icon.getImage(), x - icon.getIconWidth()/2, y_icon, null);
    }
    private void drawPivot(Graphics2D g2d, int quote_index, int x_coord) {
        if (_Pivot == null) return;
        int intv = _Pivot.getInterval();
        if (quote_index % intv != 0) return;//don't draw inside interval
        g2d.setStroke(new BasicStroke(5));

        //calculate bullish/bearish, selectively show R and S
        int pivot_idx = quote_index / intv + 1;//use earlier interval to calc current pivots
        int pivot_size = _Pivot.getPivot().size();

        //calc bull/bear, skip earliest and latest pivot
        boolean bullish = false;
        if (pivot_idx < pivot_size - 1 && pivot_idx > 0) {
            float prev_pp = _Pivot.getPivot().get(pivot_idx + 1);
            float cur_pp = _Pivot.getPivot().get(pivot_idx);
            float fut_pp = _Pivot.getPivot().get(pivot_idx - 1);
            float avg = (prev_pp + cur_pp + fut_pp) / 3;
            if (fut_pp > avg) bullish = true;
        }

        //convert value to y coordinate using price min/max
        if (pivot_idx >= pivot_size) return; //no data pass earliest date
        float y_pp = calcPriceCoord(_Pivot.getPivot().get(pivot_idx));
        float y_r2 = calcPriceCoord(_Pivot.getR2().get(pivot_idx));
        float y_r1 = calcPriceCoord(_Pivot.getR1().get(pivot_idx));
        float y_s1 = calcPriceCoord(_Pivot.getS1().get(pivot_idx));
        float y_s2 = calcPriceCoord(_Pivot.getS2().get(pivot_idx));
        if (x_prev_pp > 0) {
            g2d.setPaint(new Color(255, 61, 114, 71));//, _intensityOverlay));
            if (bullish) {
                g2d.drawLine(x_coord, (int) y_r2, x_prev_pp + (int) _fXGridWidth, (int) y_r2);
            }
            if (!bullish) {
                g2d.drawLine(x_coord, (int) y_r1, x_prev_pp + (int) _fXGridWidth, (int) y_r1);
            }
            g2d.setPaint(new Color(54, 174, 255, 71));//, _intensityOverlay));
            g2d.drawLine(x_coord, (int) y_pp, x_prev_pp + (int) _fXGridWidth, (int) y_pp);
            g2d.setPaint(new Color(49, 255, 153, 71));//, _intensityOverlay));
            if (bullish) {
                g2d.drawLine(x_coord, (int) y_s1, x_prev_pp + (int) _fXGridWidth, (int) y_s1);
            }
            if (!bullish) {
                g2d.drawLine(x_coord, (int)y_s2, x_prev_pp + (int)_fXGridWidth, (int)y_s2);
            }
        }
        x_prev_pp = x_coord;
    }
    //draw 3 lines for future pivots
    private void drawFuturePivot(Graphics2D g2d) {
        if (_Pivot == null || _Pivot.getPivot().size() < 3) return;
        g2d.setStroke(new BasicStroke(5));

        //calc bull/bear, skip earliest and latest pivot
        boolean bullish = false; int pivot_idx = 1;//only draw index 0
        float prev_pp = _Pivot.getPivot().get(pivot_idx + 1);
        float cur_pp = _Pivot.getPivot().get(pivot_idx);
        float fut_pp = _Pivot.getPivot().get(pivot_idx - 1);
        float avg = (prev_pp + cur_pp + fut_pp) / 3;
        if (fut_pp > avg) bullish = true;

        //convert value to y coordinate using price min/max
        float y_pp = calcPriceCoord(_Pivot.getPivot().get(pivot_idx + 1));
        float y_r2 = calcPriceCoord(_Pivot.getR2().get(pivot_idx + 1));
        float y_r1 = calcPriceCoord(_Pivot.getR1().get(pivot_idx + 1));
        float y_s1 = calcPriceCoord(_Pivot.getS1().get(pivot_idx + 1));
        float y_s2 = calcPriceCoord(_Pivot.getS2().get(pivot_idx + 1));
        g2d.setPaint(new Color(255, 61, 114, 71));//, _intensityOverlay));
        int width = getWidth();
        if (bullish) {
            g2d.drawLine(width - riteMargin, (int) y_r2, width, (int) y_r2);
        }
        if (!bullish) {
            g2d.drawLine(width - riteMargin, (int) y_r1, width, (int) y_r1);
        }
        g2d.setPaint(new Color(54, 174, 255, 71));//, _intensityOverlay));
        g2d.drawLine(width - riteMargin, (int) y_pp, width, (int) y_pp);
        g2d.setPaint(new Color(49, 255, 153, 71));//, _intensityOverlay));
        if (bullish) {
            g2d.drawLine(width - riteMargin, (int) y_s1, width, (int) y_s1);
        }
        if (!bullish) {
            g2d.drawLine(width - riteMargin, (int)y_s2, width, (int)y_s2);
        }
    }
    //calculate price relative y coordinates such as BB, pivot
    private float calcPriceCoord(float value) {
        return  _fPriceHeight + topMargin - _fPriceHeight * (value - _fMinPrice) / (_fMaxPrice - _fMinPrice);
    }
    //draw overbought/oversold lines only once per repaint()
    private void adjustIntensity() {
        if (_bShowRating || _bShowBollingerBand || _bShowStochastic || _bShowMacd) {
            _intensityPrice = INTENSITY_DIM;
            _intensityIndicator = INTENSITY_DIM;
            _intensityVol = INTENSITY_DIM;
        }
        else {
            _intensityPrice = INTENSITY_PRICE_NORMAL;
            _intensityIndicator = INTENSITY_PRICE_NORMAL;
            _intensityVol = INTENSITY_VOLUME_NORMAL;
        }
    }

//TODO a routine to find max and min of an array of floats as utility

    //----- accessor -----
    //only for weekly chart
    public void setWeeklyQuote(WeeklyQuote wq) { _WeeklyQuote = wq; }
    public void clearOverlayQuotes() { _OverlayQuotes = null; repaint(); }
    public void setPivotInterval(int interval) {
        _nPivotInterval = interval;  _Pivot = new PersonPivot(_PlotQuotes, _nPivotInterval);
        repaint();
    }
    public void setRightMargin(int width) { riteMargin = width; repaint(); }

    //----- variables -----
    //price/volume related
    private float _fMaxVol, _fMinVol, _fMaxPrice, _fMinPrice, _fMaxOverlay, _fMinOverlay;
    //user draw line related
    private ArrayList<Line> _Lines = new ArrayList<>();
    private boolean _bLineClick1;//first click to start a line
    //overlay symbol related
    private ArrayList<FundQuote> _OverlayQuotes;
    private int x_prev_ovly, y_prev_ovly;
    //earning related
    private boolean _bShowEarning;
    //pivot related
    private boolean _bShowPivot; private PersonPivot _Pivot; private int x_prev_pp; private int _nPivotInterval = 5;
    //candle related
    private ArrayList<FundQuote> _PlotQuotes;
    private BollingerBand _BollingerBand;
    private float[] _Stochastic, _Macd;
    private WeeklyQuote _WeeklyQuote;
    private MarketInfo _MarketInfo;
    private boolean _bShowRating, _bShowBollingerBand, _bWeekly, _bShowStochastic, _bShowMacd;
    private JPanel _pnlCanvas;
    private float[] _sma20, _sma50, _sma200, _ema8, _volAvg;
    private ArrayList<Integer> _nDojiTops, _nBullishHaramis, _nBearishHaramis, _nBullishEngulfs, _nBearishEngulfs,
            _nDarkClouds, _nPiercing, _nHammer, _nHangman, _nInvHammer, _nShootingStar, _nPusherBull, _nPusherBear,
            _nKickerBull, _nKickerBear, _nGapBull, _nGapBear;
    private FundQuote _ActiveQuote;
    private float _fXWidth, _fXGridWidth, _nCursorY;//for the currently plotted symbol
    private float _fPriceHeight, _fVolHeight;
    private int x_prev_comp, y_prev_comp, x_prev_rs, y_prev_rs, x_prev_eps, y_prev_eps;
    private int y_price_height, x_prev_ubb, y_prev_ubb, x_prev_lbb, y_prev_lbb, yprev_volavg;
    private ArrayList<CandleSignal> signalSettings;
    private ArrayList<IbdRating> _IbdRatings;
    private float /*_fMaxBB, _fMinBB,*/ _fCurUpperBB, _fCurLowerBB, _fMaxMacd, _fMinMacd;//, _fMaxSTO, _fMinSTO;
    private int x_prev_sto, y_prev_sto, x_prev_macd, y_prev_macd, pad_height = 180, pad_width = 112;
    private int _nCurComp, _nCurRs, _nCurEps;
    private int _nActiveIndex, _nPadX = 2, _nPadY = 2;
    private int _intensityPrice = INTENSITY_PRICE_NORMAL, _intensityOverlay = INTENSITY_OVERLAY_ON,
        _intensityVol = INTENSITY_VOLUME_NORMAL, _intensityIndicator = INTENSITY_PRICE_NORMAL;
    private int topMargin = DEFAULT_MARGIN, bottomMargin = DEFAULT_MARGIN, leftMargin = DEFAULT_MARGIN, riteMargin = 0;

    //----- literals -----
    private static final int DEFAULT_MARGIN = 5;//in pixels
    private static final float DEFAULT_PRICE_HEIGHT_PERCENT = 0.80f;
    private static final float DEFAULT_VOL_HEIGHT_PERCENT = 0.3f;
    private static final int INTENSITY_PRICE_NORMAL = 200;
    private static final int INTENSITY_DIM = 100;//80
    private static final int INTENSITY_OVERLAY_ON = 250;
    private static final int INTENSITY_VOLUME_NORMAL = 120;
    private static final SimpleDateFormat FINVIZ_EARNING_DATE_FORMAT1 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
    private static final SimpleDateFormat FINVIZ_EARNING_DATE_FORMAT2 = new SimpleDateFormat("MM/dd/yy");
}
