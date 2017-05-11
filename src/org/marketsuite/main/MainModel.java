package org.marketsuite.main;

import org.marketsuite.component.util.CoreUtil;
import org.marketsuite.datamgr.dataimport.EarningStore;
import org.marketsuite.framework.market.IbdInfo;
import org.marketsuite.framework.model.Fundamental;
import org.marketsuite.framework.model.data.EquitySpeed;
import org.marketsuite.framework.model.type.UserLevel;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.watchlist.model.WatchListModel;
import org.marketsuite.watchlist.model.WatchListModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class MainModel {
    private static MainModel _Instance;
    public static MainModel getInstance() {
        if (_Instance == null)
            _Instance = new MainModel();
        return _Instance;
    }

    //singleton CTOR
    private MainModel() {
        CoreUtil.setDeltaTimeStart("");
        reloadFundamentalDb();
        reloadIbdDb();
        reloadEarningDb();
        reloadSpeedMap();
        CoreUtil.showDeltaTime("<MainModel.CTOR()>");
    }

    //----- public methods -----
    public void reloadFundamentalDb() { _mapFundamental = DataUtil.readFundamentalDb(); }
    public void reloadIbdDb() { _mapIbd = DataUtil.readIbdDb(); }
    public void reloadEarningDb() { _mapEarningDates = EarningStore.readEarningDb(); }
    public void reloadSpeedMap() { _mapSpeed = EquitySpeed.readSpeedSheet(); }

    //----- accessor -----
    public UserLevel getUserLevel() { return userLevel; }
    public boolean isBasicUser() { return userLevel.equals(UserLevel.Basic); }
    public boolean isMediumUser() { return userLevel.equals(UserLevel.Medium); }
    public boolean isExpertUer() { return userLevel.equals(UserLevel.Expert); }
    public void setUserLevel(UserLevel userLevel) { this.userLevel = userLevel; }
    public void setWatchListModel(WatchListModel model) { _WatchListModel = model; }
    public WatchListModel getWatchListModel() { return _WatchListModel; }
    public HashMap<String, Fundamental> getFundamentals() { return _mapFundamental; }
    public HashMap<String, ArrayList<IbdInfo>> getIbdInfoMap() { return _mapIbd; }
    public HashMap<String, ArrayList<Calendar>> getEarningDates() { return _mapEarningDates; }
    public void setEarningDates(HashMap<String, ArrayList<Calendar>> _mapEarningDates) { this._mapEarningDates = _mapEarningDates; }
    public ArrayList<Calendar> getEarningDates(String symbol) { return _mapEarningDates.get(symbol); }
    public void setSpeedMap(HashMap<String, EquitySpeed> _mapSpeed) { this._mapSpeed = _mapSpeed; }
    public HashMap<String, EquitySpeed> getSpeedMap() { return _mapSpeed; }

    //----- variables -----
    private UserLevel userLevel = UserLevel.Medium;
    private WatchListModel _WatchListModel;
    private HashMap<String, Fundamental> _mapFundamental = new HashMap();
    private HashMap<String, ArrayList<IbdInfo>> _mapIbd = new HashMap<>();
    private HashMap<String, ArrayList<Calendar>> _mapEarningDates = new HashMap<>();
    private HashMap<String, EquitySpeed> _mapSpeed = new HashMap<>();
    //IBD50 entry / exit dates
//    private ArrayList<EntryExitDates> _aryIbd50Dates = new ArrayList<>();
}
