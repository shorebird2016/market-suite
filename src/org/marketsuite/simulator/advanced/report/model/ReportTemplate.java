package org.marketsuite.simulator.advanced.report.model;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Data structure holding report template: time option, symbol-strategy list, strategy options.  This structure
 * will be persisted to xml .rpt file.
 */
public class ReportTemplate {
    //even though this CTOR is not used, must have this for preference to work
    public ReportTemplate() {}

    public ReportTemplate(String name, HashMap<String, ArrayList<Boolean>> symbol_map) {
        reportName = name;
        symbolMap = symbol_map;
//todo time setting and report setting.....
    }
    
    public ReportTemplate(Element element) {
//todo symbol map

        timeSetting = new TimeSetting(element.getChild(TimeSetting.TIME_SETTING));
        strategySetting = new StrategySetting(element.getChild(StrategySetting.STRATEGY_SETTING));
    }
    
    private Element objToXml() {
        Element ret = new Element(REPORT_TEMPLATE);
//todo for hash map

        ret.addContent(timeSetting.objToXml());
        ret.addContent(strategySetting.objToXml());
        return ret;
    }
    
    //-----instance variables / accessors-----
    private String reportName;
    public String getReportName() {
        return reportName;
    }
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    private HashMap<String, ArrayList<Boolean>> symbolMap; //element: symbol as key, point to value (array of strings)
    public HashMap<String, ArrayList<Boolean>> getSymbolMap() {
        return symbolMap;
    }
    public void setSymbolMap(HashMap<String, ArrayList<Boolean>> symbolMap) {
        this.symbolMap = symbolMap;
    }

    private TimeSetting timeSetting;
    public TimeSetting getTimeSetting() {
        return timeSetting;
    }
    public void setTimeSetting(TimeSetting timeSetting) {
        this.timeSetting = timeSetting;
    }

    private StrategySetting strategySetting;
    public StrategySetting getStrategySetting() {
        return strategySetting;
    }
    public void setStrategySetting(StrategySetting strategySetting) {
        this.strategySetting = strategySetting;
    }
    
    //-----literals-----
    private static final String REPORT_TEMPLATE = "report-template";
}
