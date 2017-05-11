package org.marketsuite.component.window;

import org.marketsuite.component.resource.PreferenceStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * An abstract InternalFrame that supports location and size preferences.
 */
public abstract class DynaInternalFrame extends JInternalFrame {
//    public DynaInternalFrame() {}
    public DynaInternalFrame(PreferenceStore prefs, int frame_index, Point default_loc, Dimension default_size) {
        _nFrameIndex = frame_index;
        _Prefs = prefs;
        Point loc = prefs.getAppFrameLocation(frame_index);
        if (loc != null && loc.x > 0) {
            setLocation(loc);
            Dimension dim = prefs.getAppFrameSize(frame_index);
            if (dim != null)
                setSize(dim);
        }
        else {//default, also init/persist pref
            setSize(default_size);
            prefs.setAppFrameSize(frame_index, default_size);
            setLocation(default_loc);
            prefs.setAppFrameLocation(frame_index, default_loc);
            prefs.savePrefences();
        }

        //handle movements, sizing preferences
        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                _Prefs.setAppFrameLocation(_nFrameIndex, getLocation());
                _Prefs.savePrefences();
            }
            public void componentResized(ComponentEvent e) {
                _Prefs.setAppFrameSize(_nFrameIndex, getSize());
                _Prefs.savePrefences();
            }
        });
    }

    //----- variables -----
    protected int _nFrameIndex;
    protected PreferenceStore _Prefs;

}
