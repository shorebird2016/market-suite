package org.marketsuite.chart.candle;

import org.marketsuite.component.layer.CrosshairLayerUI;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.chart.Line;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.IndicatorUtil;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.layer.CrosshairLayerUI;
import org.marketsuite.framework.market.MarketInfo;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.model.chart.Line;
import org.marketsuite.framework.model.quote.WeeklyQuote;
import org.marketsuite.framework.model.type.CandleSignal;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.IndicatorUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;

//a self-drawn low level candlestick chart panel with a cross hair cursor and data pad
// other add-ons like candle signals, indicators are implemented in higher level panels
public class BaseCandlePanel extends JPanel {
    public BaseCandlePanel() {
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
                x_prev_ovly = y_prev_ovly = 0;
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
                    if (_bShowMa) {
                        int yema8 = y_price_height - (int)(y_price_height * (_ema8[idx] - _fMinPrice) / price_range) + topMargin;
                        g2d.setPaint(new Color(175, 23, 217, _intensityIndicator)); g2d.setStroke(new BasicStroke(1));
                        if (idx < _PlotQuotes.size() - 1)
                            g2d.drawLine(x, yprev_ema8, (int)(x + _fXGridWidth), yema8);
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

                    //fractals - pink line segment on high, light green line segment on low
                    if (_bShowFractal) {
                        if (isFractal(quote, true)) {
                            g2d.setPaint(new Color(233, 124, 186, 144));//, _intensityPrice));
                            g2d.setStroke(new BasicStroke(2));
                            int cx1 = (int)(x - _fXGridWidth * 2);
                            int cx2 = (int)(x + _fXGridWidth * 2);
                            g2d.drawLine(cx1, yshadow_top, cx2, yshadow_top);
                        }
                        else if (isFractal(quote, false)) {
                            g2d.setPaint(new Color(30, 245, 24, 200));//, _intensityPrice));
                            g2d.setStroke(new BasicStroke(2));
                            int cx1 = (int)(x - _fXGridWidth * 2);
                            int cx2 = (int)(x + _fXGridWidth * 2);
                            g2d.drawLine(cx1, yshadow_bottom, cx2, yshadow_bottom);
                        }
                    }
                    if (_OverlayQuotes != null) drawOverlaySymbol(g2d, idx, x);
                }

                //draw data pad area, do this last to be top of Z-axis
                drawDataPad(g2d, _ActiveQuote != null ? _ActiveQuote : _PlotQuotes.get(0));

                //draw lines
                g2d.setPaint(new Color(213, 140, 82, 155)); g2d.setStroke(new BasicStroke(5));
                if (_Lines.size() > 0) {
                    for (Line line : _Lines) {
                        if (!line.isEmpty())
                            line.draw(g2d);
                    }
                }
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
            }
        });
    }

    //----- public methods -----
    //plot w new set of information, mainly from daily chart
    public void plot(MarketInfo mki, ArrayList<FundQuote> plot_quotes, ArrayList<CandleSignal> settings) {
        _MarketInfo = mki; _PlotQuotes = plot_quotes;
        preparePlotData(); clearLines();
        repaint();
    }
    //special plot for weekly quotes with supplied moving averages
    public void plot(ArrayList<FundQuote> plot_quotes, float[] sma50, float[] sma200, float[] ema8, float[] vol_avg ,
                     ArrayList<CandleSignal> settings) {
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
        //find fractals from quotes arrray
        _HighFractals = IndicatorUtil.findFractals(_PlotQuotes, true, _PlotQuotes.size() - 1, 0);
        _LowFractals = IndicatorUtil.findFractals(_PlotQuotes, false, _PlotQuotes.size() - 1, 0);
    }
    //is this quote a fractal?  just look into _Fractals array
    private boolean isFractal(FundQuote quote, boolean high_fractal) {
        if (high_fractal) {
            for (FundQuote fq : _HighFractals)
                if (fq.getDate().equals(quote.getDate())) return true;
        }
        else {
            for (FundQuote fq : _LowFractals)
                if (fq.getDate().equals(quote.getDate())) return true;
        }
        return false;
    }
    //figure out support/resistance levels from fractals, combine fractals if level is close enough
    private HashMap<FundQuote, ArrayList<FundQuote>> findRLandSLs() {
        HashMap<FundQuote, ArrayList<FundQuote>> ret = new HashMap<>();
        ArrayList<FundQuote> fractals = new ArrayList<>();
        fractals.addAll(_HighFractals); fractals.addAll(_LowFractals);
        for (int cur_idx = 0; cur_idx < fractals.size(); cur_idx++) {
            FundQuote cur_fractal = fractals.get(cur_idx);
            float cur_lvl = fractals.get(cur_idx).isFractHigh() ? fractals.get(cur_idx).getHigh()
                : fractals.get(cur_idx).getLow();
            //for each subsequent quote, if within 5% of current quote, add to a new list
            for (int tst_idx = cur_idx + 1; tst_idx < fractals.size(); tst_idx++) {
                FundQuote tst_fractal = fractals.get(tst_idx);
                float tst_lvl = fractals.get(tst_idx).isFractHigh() ? fractals.get(tst_idx).getHigh()
                    : fractals.get(tst_idx).getLow();
                if (Math.abs((tst_lvl - cur_lvl) / cur_lvl) <= 0.05) {
                    ArrayList<FundQuote> quotes = ret.get(cur_fractal);
                    if (quotes == null) {//create new array since cur_fractal doesn't have one yet
                        quotes = new ArrayList<>();
                        ret.put(cur_fractal, quotes);
                    }
                    //already have some, add tst_fractal to the end, another one with similar ranges
                    if (!quotes.contains(tst_fractal)) quotes.add(tst_fractal);
                }
            }
        }
        return ret;
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
        y += 2 * hinc;
    }
    //another symbol overlay on top of current symbol
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
    //calculate price relative y coordinates such as BB, pivot
    private float calcPriceCoord(float value) {
        return  _fPriceHeight + topMargin - _fPriceHeight * (value - _fMinPrice) / (_fMaxPrice - _fMinPrice);
    }

    //----- accessor -----
    //only for weekly chart
    public void setWeeklyQuote(WeeklyQuote wq) { _WeeklyQuote = wq; }
    public void clearOverlayQuotes() { _OverlayQuotes = null; repaint(); }
    public void setRightMargin(int width) { riteMargin = width; repaint(); }
    public void setShowMa(boolean show) { _bShowMa = show; repaint(); }
    public void setShowFractal(boolean show) { _bShowFractal = show; repaint(); }

    //----- variables -----
    //price/volume related
    private float _fMaxVol, _fMinVol, _fMaxPrice, _fMinPrice, _fMaxOverlay, _fMinOverlay;
    //user draw line related
    private ArrayList<Line> _Lines = new ArrayList<>();
    private boolean _bLineClick1, _bWeekly;//first click to start a line
    //overlay symbol related
    private ArrayList<FundQuote> _OverlayQuotes;
    private int x_prev_ovly, y_prev_ovly;
    //candle related
    private ArrayList<FundQuote> _PlotQuotes;
    private WeeklyQuote _WeeklyQuote;
    private MarketInfo _MarketInfo;
    private JPanel _pnlCanvas;
    private float[] _sma20, _sma50, _sma200, _ema8, _volAvg;
    private FundQuote _ActiveQuote;
    private float _fXWidth, _fXGridWidth, _nCursorY;//for the currently plotted symbol
    private float _fPriceHeight, _fVolHeight;
    private int y_price_height, pad_height = 180, pad_width = 112;
    private int _nActiveIndex, _nPadX = 2, _nPadY = 2;
    private int _intensityPrice = INTENSITY_PRICE_NORMAL, _intensityOverlay = INTENSITY_OVERLAY_ON,
        _intensityVol = INTENSITY_VOLUME_NORMAL, _intensityIndicator = INTENSITY_PRICE_NORMAL;
    private int topMargin = DEFAULT_MARGIN, bottomMargin = DEFAULT_MARGIN, leftMargin = DEFAULT_MARGIN, riteMargin = 0;
    private boolean _bShowMa, _bShowFractal;
    private ArrayList<FundQuote> _HighFractals, _LowFractals;
    private HashMap<FundQuote, ArrayList<FundQuote>> _SLorRLs = new HashMap<>();

    //----- literals -----
    private static final int DEFAULT_MARGIN = 5;//in pixels
    private static final float DEFAULT_PRICE_HEIGHT_PERCENT = 0.80f;
    private static final float DEFAULT_VOL_HEIGHT_PERCENT = 0.3f;
    private static final int INTENSITY_PRICE_NORMAL = 200;
    private static final int INTENSITY_DIM = 100;//80
    private static final int INTENSITY_OVERLAY_ON = 250;
    private static final int INTENSITY_VOLUME_NORMAL = 120;
}
