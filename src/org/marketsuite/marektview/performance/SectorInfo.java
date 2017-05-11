package org.marketsuite.marektview.performance;

import org.marketsuite.framework.model.type.PerfTimeframe;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.marektview.ranking.RankElement;
import org.marketsuite.watchlist.model.WatchListModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//US sector related constants and methods
public class SectorInfo {
    //----- public methods -----
    public static void createSectorToIGroup() {//call this at startup
        SECTOR_TO_IGROUP = new HashMap<>();
        SECTOR_TO_IGROUP.put("XLB", XLB);
        SECTOR_TO_IGROUP.put("XLE", XLE);
        SECTOR_TO_IGROUP.put("XLF", XLF);
        SECTOR_TO_IGROUP.put("XLI", XLI);
        SECTOR_TO_IGROUP.put("XLK", XLK);
        SECTOR_TO_IGROUP.put("XLP", XLP);
        SECTOR_TO_IGROUP.put("XLU", XLU);
        SECTOR_TO_IGROUP.put("XLV", XLV);
        SECTOR_TO_IGROUP.put("XLY", XLY);
        SECTOR_TO_IGROUP.put("IYR", IYR);
        SECTOR_TO_IGROUP.put("IYT", IYT);
        SECTOR_TO_IGROUP.put("IYZ", IYZ);
    }
    //given a industrial group symbol, look up its sector symbol
    public static String findSectorByIGroup(String indu_grp) {
        Iterator<String> itor = SECTOR_TO_IGROUP.keySet().iterator();
        while (itor.hasNext()) {
            String sector = itor.next();
            String[] ig_lst = SECTOR_TO_IGROUP.get(sector);
            for (String ig : ig_lst)
                if (indu_grp.equals(ig)) return sector;
        }
        return null;
    }
    //sort up to date sector performance based on a particular time frame in decending order
    public static ArrayList<RankElement> computeSectorRanking(PerfTimeframe time_frame) {
        WatchListModel wlm = new WatchListModel("ETF - 3 US Sector", false);
        ArrayList<RankElement> rank_elements = wlm.computePerfRanking(time_frame, 0);
        return rank_elements;
    }

    //----- variables -----
    // a map that helps finding relationships
    public static HashMap<String, String[]> SECTOR_TO_IGROUP;

    //----- literals -----
    //----- Relationships between sector and industrial groups
    public static final String[] XLB = { "CGW", "GNR", "KOL", "MOO", "OIH", "PHO", "PIO" };
    public static final String[] XLE = { "AMJ", "AMLP", "FCG", "GEX", "IEO", "IGE", "PBW", "PXJ", "QCLN", "TAN", "XES", "XOP" };
    public static final String[] XLF = { "IAI", "IAK", "KBE", "KCE", "KIE", "KRE", "PSP" };
    public static final String[] XLI = { "ITA", "PPA", "XAR" };
    public static final String[] XLK = { "FDN", "IGN", "IGV", "PXQ", "ROBO", "SKYY", "SMH", "SOCL", "SOXX", "HACK", "VGT", "XSD" };
    public static final String[] XLP = { "PBJ" };
    public static final String[] XLU = { "" };
    public static final String[] XLV = { "BBH", "IBB", "IHF", "IHI", "IXJ", "PJP", "PPH", "VHT", "XBI", "XPH" };
    public static final String[] XLY = { "BJK", "ITB", "PBS", "PEJ", "RTH", "XHB", "XRT" };
    public static final String[] IYR = { "RWO", "RWX", "VNQI" };
    public static final String[] IYT = { "" };
    public static final String[] IYZ = { "FONE" };
}
