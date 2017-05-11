package org.marketsuite.framework.strategy.mac;

import org.jdom.Element;

/**
 * MAC strategy related simulation options.
 */
public class MacOption {//Note: must have default CTOR, all get/set for XmlEncoder/XmlDecoder to work
    public MacOption() { }
    public MacOption(int entryMA1, int entryMA2, int exitMA1, int exitMA2) {
        this.entryMA1 = entryMA1;
        this.entryMA2 = entryMA2;
        this.exitMA1 = exitMA1;
        this.exitMA2 = exitMA2;
    }

    public MacOption(Element element) {

    }

    public Element objToXml() {
        Element ret = new Element(MAC_SETTING);
        return ret;
    }

    private int entryMA1 = 50;
    public int getEntryMA1() {
        return entryMA1;
    }
    public void setEntryMA1(int entryMA1) {
        this.entryMA1 = entryMA1;
    }

    private int entryMA2 = 120;
    public int getEntryMA2() {
        return entryMA2;
    }
    public void setEntryMA2(int entryMA2) {
        this.entryMA2 = entryMA2;
    }

    private int exitMA1 = 120;
    public int getExitMA1() {
        return exitMA1;
    }
    public void setExitMA1(int exitMA1) {
        this.exitMA1 = exitMA1;
    }

    private int exitMA2 = 200;
    public int getExitMA2() {
        return exitMA2;
    }
    public void setExitMA2(int exitMA2) {
        this.exitMA2 = exitMA2;
    }

    //-----literals-----
    private static final String MAC_SETTING = "mac-setting";

//    private boolean useMacAsFilter;//true to use MAC as Pankin's filter during buy
//    public void setUseMacAsFilter(boolean use_mac) { useMacAsFilter = use_mac; }
//    public boolean isUseMacAsFilter() { return useMacAsFilter; }
}
