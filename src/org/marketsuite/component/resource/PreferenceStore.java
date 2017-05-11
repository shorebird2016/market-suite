package org.marketsuite.component.resource;

import java.awt.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

/**
   Application level preferences in singleton. Must be derived to add application specific attributes.
*/
public abstract class PreferenceStore {
    /**
     * Read application preference information from serialized storage into hashmap
     * @return hashmap of string and array of strings, or null = no prefs or error
     */
    public void loadPreferences() {
        FileInputStream is = null;
        try {
            is = new FileInputStream(FILE_PATH);
            XMLDecoder dec = new XMLDecoder(new BufferedInputStream(is));
            _Prefs = (PreferenceStore)dec.readObject();
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
    public void savePrefences() {
        FileOutputStream is = null;
        try {
            is = new FileOutputStream(FILE_PATH);
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

    //----- accessors -----
    public Point getMainFrameLocation() { return mainFrameLocation; }
    public void setMainFrameLocation(Point mainFrameLocation) { this.mainFrameLocation = mainFrameLocation; }
    public Dimension getMainFrameSize() { return mainFrameSize; }
    public void setMainFrameSize(Dimension mainFrameSize) { this.mainFrameSize = mainFrameSize; }
    public Point[] getAppFrameLocation() { return appFrameLocation; }
    public Point getAppFrameLocation(int index) { return appFrameLocation[index]; }
    public void setAppFrameLocation(Point[] locs) { appFrameLocation = locs; }
    public void setAppFrameLocation(int index, Point loc) { appFrameLocation[index] = loc; }
    public Dimension[] getAppFrameSize() { return appFrameSize; }
    public Dimension getAppFrameSize(int index) { return appFrameSize[index]; }
    public void setAppFrameSize(Dimension[] sizes) { appFrameSize = sizes; }
    public void setAppFrameSize(int index, Dimension size) { appFrameSize[index] = size; }

    //----- variables -----
    protected static PreferenceStore _Prefs;
    //main frame
    private Point mainFrameLocation;//for main window location and size
    private Dimension mainFrameSize;
    //internal frames
    private int _nFrameCount = 5;//for internal frame
    private Point[] appFrameLocation = new Point[_nFrameCount];//for internal frames, indexed by MdiMainFrame constants INDEX_???
    private Dimension[] appFrameSize = new Dimension[_nFrameCount];
//TODO: move this to app specific constants
    //----- literals -----
    public static final String FILE_NAME = "MarketSuitePref.xml";
    public static final String FILE_PATH = System.getProperty("user.home") + File.separator + FILE_NAME;
}
