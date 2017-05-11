package org.marketsuite.framework.model.indicator;

import org.marketsuite.framework.model.FundData;
import org.jdom.Element;
import org.marketsuite.framework.model.FundData;

/**
 * Abstraction of indicators.
 */
public interface Indicator {
    public String getId();
    public Element objToXml();
    public float[] getValues();
    public FundData getQuotes();
}
