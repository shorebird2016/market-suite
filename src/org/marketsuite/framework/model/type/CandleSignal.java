package org.marketsuite.framework.model.type;

import org.marketsuite.framework.resource.FrameworkIcon;

//collection of candle stick signals
public enum CandleSignal {
    DojiTop("DJ", "Doji Spinning Top", FrameworkIcon.CDL_DOJI_TOP),
    BullishEngulfing("E+", "Bullish Engulfing", FrameworkIcon.CDL_BULL_ENGULF),
    BearishEngulfing("E-", "Bearish Engulfing", FrameworkIcon.CDL_BEAR_ENGULF),
    BullishHarami("H+", "Bullish Harami", FrameworkIcon.CDL_BULL_HARAMI),
    BearishHarami("H-", "Bearish Harami", FrameworkIcon.CDL_BEAR_HARAMI),
    Hammer("HM", "Hammer", FrameworkIcon.CDL_BULL_HAMMER),
    HangingMan("HN", "Hanging Man", FrameworkIcon.CDL_BEAR_HANGMAN),
    InvertedHammer("IH", "Inverted Hammer", FrameworkIcon.CDL_BULL_INVERT_HAMMER),
    ShootingStar("SS", "Shooting Star", FrameworkIcon.CDL_BEAR_SHOOTING_STAR),
    DarkCloud("DC", "Dark Cloud Cover", FrameworkIcon.CDL_BEAR_DARK_CLOUD),
    Piercing("PC", "Piercing", FrameworkIcon.CDL_BULL_PIERCING),
    BullishPusher("P+", "Bullish Pusher", FrameworkIcon.CDL_BULL_PUSHER),
    BearishPusher("P-", "Bearish Pusher", FrameworkIcon.CDL_BEAR_PUSHER),
    BullishKicker("K+", "Bullish Kicker", FrameworkIcon.CDL_BULL_KICKER),
    BearishKicker("K-", "Bearish Kicker", FrameworkIcon.CDL_BEAR_KICKER),
    MorningStar("MS", "Morning Star", FrameworkIcon.CDL_BULL_MORNING_STAR),
    EveningStar("ES", "Evening Star", FrameworkIcon.CDL_BEAR_EVE_STAR),
    BullishWindows("G+", "Bullish Gap", FrameworkIcon.CDL_BULL_GAP),
    BearishWindows("G-", "Bearish Gap", FrameworkIcon.CDL_BEAR_GAP),
    ;
    CandleSignal(String code, String display_string, FrameworkIcon icon) {
        codeString = code; displayString = display_string; candleIcon = icon; }
    private String displayString, codeString;
    private FrameworkIcon candleIcon;
    public String toString() { return displayString; }
    public String getCode() { return codeString; }
    public FrameworkIcon getCandleIcon() { return candleIcon; }
    public static CandleSignal toEnumConstant(String disp_str) {//from display string --> constnat
        CandleSignal[] constants = CandleSignal.values();
        for (CandleSignal cs : constants) {
            if (cs.toString().equals(disp_str))
                return cs;
        }
        return null;//not found
    }
    public static boolean isBullish(CandleSignal cs) {
        return  (cs.equals(BullishEngulfing) || cs.equals(BullishHarami) || cs.equals(BullishKicker) ||
            cs.equals(BullishPusher) || cs.equals(BullishWindows) || cs.equals(Hammer) ||
            cs.equals(InvertedHammer) || cs.equals(Piercing) || cs.equals(MorningStar));
    }
    public static boolean isBearish(CandleSignal cs) {
        return (cs.equals(BearishEngulfing) || cs.equals(BearishHarami) || cs.equals(BearishKicker) ||
                cs.equals(BearishPusher) || cs.equals(BearishWindows) || cs.equals(DarkCloud) ||
                cs.equals(HangingMan) || cs.equals(ShootingStar) || cs.equals(EveningStar));
    }
}
