package org.marketsuite.resource;

import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.scanner.tracking.TrackerOption;
import org.marketsuite.simulator.advanced.report.model.ReportTemplate;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.scanner.tracking.TrackerOption;
import org.marketsuite.simulator.advanced.report.model.ReportTemplate;

import java.awt.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;

//Apollo application level preferences singleton
public class ApolloPreferenceStore {
    //----- CTOR -----
    public ApolloPreferenceStore() {
//        loadPreferences();
    }

    //----- public methods -----
    public static ApolloPreferenceStore getPreferences() {
        if (_Prefs == null)
            loadPreferences();
        return _Prefs;
    }

    /**
     * Read apollo application preference information from serialized storage into hashmap
     * @return hashmap of string and array of strings, or null = no prefs or error
     */
    public static void loadPreferences() {
        if (_Prefs == null)
            _Prefs = new ApolloPreferenceStore();
        FileInputStream is = null;
        try {
            is = new FileInputStream(APP_PREF_PATH);
            XMLDecoder dec = new XMLDecoder(new BufferedInputStream(is));
            _Prefs = (ApolloPreferenceStore)dec.readObject();
            dec.close();
        } catch (FileNotFoundException ex1) {
            //ok not having this file
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally{
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void savePreferences() {
        if (_Prefs == null) return;
        FileOutputStream is = null;
        try {
            is = new FileOutputStream(APP_PREF_PATH);
            XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(is));
            enc.writeObject(_Prefs);
            enc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //----- variables / accessors -----
    private static ApolloPreferenceStore _Prefs;

    //--Main Window--
    private Point mainFrameLocation;//for main window location and size
    public Point getMainFrameLocation() { return mainFrameLocation; }
    public void setMainFrameLocation(Point mainFrameLocation) { this.mainFrameLocation = mainFrameLocation; }
    private Dimension mainFrameSize;
    public Dimension getMainFrameSize() { return mainFrameSize; }
    public void setMainFrameSize(Dimension mainFrameSize) { this.mainFrameSize = mainFrameSize; }
    private Point[] appFrameLocation = new Point[MdiMainFrame.NUM_FRAMES];//for internal frames, indexed by MdiMainFrame constants INDEX_???
    public Point[] getAppFrameLocation() { return appFrameLocation; }
    public Point getAppFrameLocation(int index) { return appFrameLocation[index]; }
    public void setAppFrameLocation(Point[] locs) { appFrameLocation = locs; }
    public void setAppFrameLocation(int index, Point loc) { appFrameLocation[index] = loc; }
    private Dimension[] appFrameSize = new Dimension[MdiMainFrame.NUM_FRAMES];
    public Dimension[] getAppFrameSize() { return appFrameSize; }
    public Dimension getAppFrameSize(int index) { return appFrameSize[index]; }
    public void setAppFrameSize(Dimension[] sizes) { appFrameSize = sizes; }
    public void setAppFrameSize(int index, Dimension size) { appFrameSize[index] = size; }
    //icon locations
//    private Point[] appIconLocation = new Point[MdiMainFrame.LIST_APP_ICON.length];
//    public Point[] getAppIconLocation() { return appIconLocation; }
//    public void setAppIconLocation(Point[] locs) { appIconLocation = locs; }

    //--Scanner--
    private boolean[] QueryColumnVisible;
    public boolean[] getQueryColumnVisible() { return QueryColumnVisible; }
    public void setQueryColumnVisible(boolean[] queryColumnVisible) { QueryColumnVisible = queryColumnVisible; }

    //---Data Manager---
    public String lastSnapshotPath;
    public String getLastSnapshotPath() { return lastSnapshotPath; }
    public void setLastSnapshotPath(String lastSnapshotPath) { this.lastSnapshotPath = lastSnapshotPath; }
    //--Finviz--
    public String lastFinvizPath;
    public String getLastFinvizPath() { return lastFinvizPath; }
    public void setLastFinvizPath(String lastFinvizPath) { this.lastFinvizPath = lastFinvizPath; }
    //--Apollo watch list--
    public String lastWatchListPath;
    public String getLastWatchListPath() { return lastWatchListPath; }
    public void setLastWatchListPath(String lastWatchListPath) { this.lastWatchListPath = lastWatchListPath; }

    //--IBD Historical Portfolio--
    public String lastIbdPortfolioPath;
    public String getLastIbdPortfolioPath() { return lastIbdPortfolioPath; }
    public void setLastIbdPortfolioPath(String path) { this.lastIbdPortfolioPath = path; }


    //--L2--
    private int[] fundamentalColumnOrder;//fundamental tab column ordering
    public int[] getFundamentalColumnOrder() { return fundamentalColumnOrder; }
    public void setFundamentalColumnOrder(int[] order) { fundamentalColumnOrder = order; }
    private boolean[] fundamentalColumnVisible;//fundamental tab column visibility
    public boolean[] getFundamentalColumnVisible() { return fundamentalColumnVisible; }
    public void setFundamentalColumnVisible(boolean[] hs) { fundamentalColumnVisible = hs; }

    //--Tracker--
    private int trackerSplitterPosition = -1;//Splitter position in Tracker frame
    public int getTrackerSplitterPosition() { return trackerSplitterPosition; }
    public void setTrackerSplitterPosition(int pos) { trackerSplitterPosition = pos; }
    private TrackerOption trackerOption;
    public TrackerOption getTrackerOption() { return trackerOption; }
    public void setTrackerOption(TrackerOption trackerOption) { this.trackerOption = trackerOption; }

    //--Watch List Manager--
    private int watchListMgrSplitterPosition = -1;//watch list manager window splitter position
    public int getWatchListMgrSplitterPosition() { return watchListMgrSplitterPosition; }
    public void setWatchListMgrSplitterPosition(int watchListMgrSplitterPosition) { this.watchListMgrSplitterPosition = watchListMgrSplitterPosition; }

    //--Risk Manager--
    private boolean[] accountColumnVisible;//account table column ordering, visibility
    public boolean[] getAccountColumnVisible() { return accountColumnVisible; }
    public void setAccountColumnVisible(boolean[] hs) { accountColumnVisible = hs; }
    private int[] accountColumnOrder;
    public int[] getAccountColumnOrder() { return accountColumnOrder; }
    public void setAccountColumnOrder(int[] order) { accountColumnOrder = order; }
    //--TradeStation--
    public String tradeStationPath;
    public String getTradeStationPath() { return tradeStationPath; }
    public void setTradeStationPath(String lastImportPath) { tradeStationPath = lastImportPath; }

    //--Simulator--
    private int[] customReportColumnOrder;//custom analysis column ordering, visibility
    public int[] getCustomReportColumnOrder() { return customReportColumnOrder; }
    public void setCustomReportColumnOrder(int[] order) { customReportColumnOrder = order; }
    private boolean[] customReportColumnVisible;
    public boolean[] getCustomReportColumnVisible() { return customReportColumnVisible; }
    public void setCustomReportColumnVisible(boolean[] hs) { customReportColumnVisible = hs; }
    private int[] AdvReportColumnOrder;//advanced analysis column ordering, visibility
    public int[] getAdvReportColumnOrder() { return AdvReportColumnOrder; }
    public void setAdvReportColumnOrder(int[] order) { AdvReportColumnOrder = order; }
    private boolean[] AdvReportColumnVisible;
    public boolean[] getAdvReportColumnVisible() { return AdvReportColumnVisible; }
    public void setAdvReportColumnVisible(boolean[] hs) { AdvReportColumnVisible = hs; }
    private ArrayList<ReportTemplate> AdvReportTemplates = new ArrayList<>();//advanced report templates
    public ArrayList<ReportTemplate> getAdvReportTemplates() {
        return AdvReportTemplates;
    }
    public void setAdvReportTemplates(ArrayList<ReportTemplate> advReportTemplates) {
        this.AdvReportTemplates = advReportTemplates;
    }

    //--IBD50--
    public String lastIbd50Path;
    public String getLastIbd50Path() { return lastIbd50Path; }
    public void setLastIbd50Path(String path) { this.lastIbd50Path = path; }



    //----- literals -----
    public static final String FILE_NAME = "ms_pref.xml";
    public static final String APP_PREF_PATH = System.getProperty("user.home") + File.separator + FILE_NAME;
}
//TODO: XMLEncoder is touchy that slight change in this code causes not saving properly.  Be careful....