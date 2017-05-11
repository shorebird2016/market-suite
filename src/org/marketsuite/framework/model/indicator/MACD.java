package org.marketsuite.framework.model.indicator;

import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.framework.util.IndicatorUtil;
import org.jdom.Element;
import org.marketsuite.framework.model.FundData;

import java.io.IOException;

/**
 * Encapsulates various information about MACD indicator.  Including XML serialization support.
 */
public class MACD implements Indicator {
    public MACD(String _symbol) throws IOException {
        symbol = _symbol;
        fund = DataUtil.readFundHistory(FrameworkConstants.DATA_FOLDER_DAILY_QUOTE, symbol);
        values = IndicatorUtil.calcMACD(fastMA, slowMA, fund.getSize() - 1, fund);//full range
    }
    public MACD(int fastMA, int slowMA) {
        this.fastMA = fastMA;
        this.slowMA = slowMA;
    }
    public MACD(Element element) {

    }

    //----- interface, overrides -----
    public String getId() { return ID; }
    public Element objToXml() {
        Element ret = new Element(ID);
        return ret;
    }

    //----- variables, accessors -----
    private String symbol;

    private int fastMA = DEFAULT_FAST_MA;
    public int getFastMA() {
        return fastMA;
    }
    public void setFastMA(int fastMA) {
        this.fastMA = fastMA;
    }

    private int slowMA = DEFAULT_SLOW_MA;
    public int getSlowMA() {
        return slowMA;
    }
    public void setSlowMA(int slowMA) {
        this.slowMA = slowMA;
    }

    private float[] values;
    public float[] getValues() { return values; }

    private FundData fund;
    public FundData getQuotes() { return fund; }

    //-----literals-----
    private final String ID = "MACD";
    private final static int DEFAULT_FAST_MA = 12;
    private final static int DEFAULT_SLOW_MA = 26;
}
