package org.marketsuite.framework.strategy.macoscillator;

/**
 * MAC + Oscillator strategy related simulation options.
 */
public class MacOscillatorOption {//Note: must have default CTOR, all get/set for XmlEncoder/XmlDecoder to work
    public MacOscillatorOption() { }
    public MacOscillatorOption(boolean entry_use_ema, int entry_fast_ma, int entry_slow_ma, 
                               boolean use_fast_dsto, int dsto_param1, int dsto_param2,
                               boolean use_wsto, boolean use_fast_wsto, int wsto1, int wsto2,
                               boolean use_ema_exit, int exit_ma) {
        entryUseEma = entry_use_ema;
        entryFastMa = entry_fast_ma;
        entrySlowMa = entry_slow_ma;
        entryUseFastDsto = use_fast_dsto;
        dstoParam1 = dsto_param1;
        dstoParam2 = dsto_param2;
        entryUseWsto = use_wsto;
        wstoUseFast = use_fast_wsto;
        wstoParam1 = wsto1;
        wstoParam2 = wsto2;
        exitUseEma = use_ema_exit;
        exitMa = exit_ma;
    }

    private boolean entryUseEma;
    public boolean isEntryUseEma() {
        return entryUseEma;
    }
    public void setEntryUseEma(boolean entryUseEma) {
        this.entryUseEma = entryUseEma;
    }
    
    private int entryFastMa;
    public int getEntryFastMa() {
        return entryFastMa;
    }
    public void setEntryFastMa(int entryFastMa) {
        this.entryFastMa = entryFastMa;
    }

    private int entrySlowMa;
    public int getEntrySlowMa() {
        return entrySlowMa;
    }
    public void setEntrySlowMa(int entrySlowMa) {
        this.entrySlowMa = entrySlowMa;
    }

    private boolean entryUseFastDsto;
    public boolean isEntryUseFastDsto() {
        return entryUseFastDsto;
    }
    public void setEntryUseFastDsto(boolean entryUseFastDsto) {
        this.entryUseFastDsto = entryUseFastDsto;
    }

    private int dstoParam1;
    public int getDstoParam1() {
        return dstoParam1;
    }
    public void setDstoParam1(int dstoParam1) {
        this.dstoParam1 = dstoParam1;
    }

    private int dstoParam2;
    public int getDstoParam2() {
        return dstoParam2;
    }
    public void setDstoParam2(int dstoParam2) {
        this.dstoParam2 = dstoParam2;
    }

    private boolean entryUseWsto;
    private boolean wstoUseFast;
    private int wstoParam1;
    private int wstoParam2;
    
    private boolean exitUseEma;
    private int exitMa;
    
    //-----literals-----
    private static final String MAC_SETTING = "mac-oscillator-setting";

//    private boolean useMacAsFilter;//true to use MAC as Pankin's filter during buy
//    public void setUseMacAsFilter(boolean use_mac) { useMacAsFilter = use_mac; }
//    public boolean isUseMacAsFilter() { return useMacAsFilter; }
}
