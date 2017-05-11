package org.marketsuite.datamgr.dataimport;

import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.framework.model.LogMessage;
import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

//Persistent storage for historical earning dates extracted from Finviz export files
public class EarningStore {
//    private static EarningStore _Instance;
//    public static EarningStore getInstance() {
//        if (_Instance == null) _Instance = new EarningStore();
//        return _Instance;
//    }
//    private EarningStore() { readEarningDb(); }

    //return null = fail to read somehow
    public static HashMap<String, ArrayList<Calendar>> readEarningDb() {//read file into structure of MainModel
        FileInputStream is = null;
        try {
            is = new FileInputStream(STORE_PATH);
            XMLDecoder dec = new XMLDecoder(new BufferedInputStream(is));
            HashMap<String, ArrayList<Calendar>> map = (HashMap<String, ArrayList<Calendar>>) dec.readObject();
            dec.close();
            return map;
        } catch (FileNotFoundException ex1) {
            //ok not having this file
            LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_edb1"), LoggingSource.DATAMGR_IMPORT);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_edb2"), LoggingSource.DATAMGR_IMPORT);
        }
        finally{
                try {
                    if (is != null) is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    //empty db won't be saved, must be run inside a thread
    public static void saveEarningDb(HashMap<String, ArrayList<Calendar>> earning_db) {
        if (earning_db == null || earning_db.size() == 0) return;
        FileOutputStream is = null;
        try {
            is = new FileOutputStream(STORE_PATH);
            XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(is));
            enc.writeObject(earning_db);
            enc.close();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            LogMessage.logSingleMessageInEdt(ApolloConstants.APOLLO_BUNDLE.getString("imp_fail_edb3"), LoggingSource.DATAMGR_IMPORT);
        }
        finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //----- literals ------
    public static final String STORE_NAME = "Earning.xml";
//    public static final String WATCHLIST_PREF_PATH = System.getProperty("user.home") + File.separator + STORE_NAME;
    public static final String STORE_PATH = FrameworkConstants.DATA_FOLDER + File.separator + STORE_NAME;
}
