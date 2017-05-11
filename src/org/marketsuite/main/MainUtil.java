package org.marketsuite.main;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.GroupStore;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloPreferenceStore;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class MainUtil {
    /**
     * Common code shared by all internal frames to retrieve/update location/size preferences.
     * @param i_frame an InternalFrame instance
     * @param frame_index index into a list of Internal frames managed by MdiMainFrame
     * @param default_location if location does not exist in preference
     * @param default_size if size does not exist in preference
     */
    public static void handleLocationAndSize(final JInternalFrame i_frame, final int frame_index, Point default_location, Dimension default_size) {
        //retrieve preference
        Point loc = ApolloPreferenceStore.getPreferences().getAppFrameLocation(frame_index);
        if (loc != null && loc.x >= 0) {
            i_frame.setLocation(loc);
            Dimension dim = ApolloPreferenceStore.getPreferences().getAppFrameSize(frame_index);
            if (dim != null)
                i_frame.setSize(dim);
        }
        else {//default, also init pref
            i_frame.setSize(default_size);
            ApolloPreferenceStore.getPreferences().setAppFrameSize(frame_index, default_size);
            i_frame.setLocation(default_location);
            ApolloPreferenceStore.getPreferences().setAppFrameLocation(frame_index, default_location);
        }

        //save preferences
        i_frame.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                ApolloPreferenceStore.getPreferences().setAppFrameLocation(frame_index, i_frame.getLocation());
                ApolloPreferenceStore.savePreferences();
            }
            public void componentResized(ComponentEvent e) {
                ApolloPreferenceStore.getPreferences().setAppFrameSize(frame_index, i_frame.getSize());
                ApolloPreferenceStore.savePreferences();
            }
        });

    }
}
