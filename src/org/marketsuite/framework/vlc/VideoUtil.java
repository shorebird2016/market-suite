package org.marketsuite.framework.vlc;

import org.marketsuite.component.util.WidgetUtil;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;

//Video related utilities
public class VideoUtil {
    //find VLC library on the computer, othwerwise warn user
    public static void findVlcLibrary() {
        try {
            new NativeDiscovery().discover();
        } catch (Exception e) {
            WidgetUtil.showWarning((JFrame) null, "Sorry! Can't find VLC library.");
            e.printStackTrace();
        }
    }
}
