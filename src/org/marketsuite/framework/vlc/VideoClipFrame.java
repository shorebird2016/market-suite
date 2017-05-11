package org.marketsuite.framework.vlc;

import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.vlc.SimpleVlcPlayerPanel;
import org.marketsuite.framework.vlc.VideoUtil;

import java.awt.*;
import java.io.File;
import javax.swing.*;

//a simple frame for playing video clips
public class VideoClipFrame extends JFrame {
    public VideoClipFrame(String clip_name) throws HeadlessException {
        setIconImage(LazyIcon.APP_ICON.getImage());
        String home_folder = System.getProperty("user.home");
        VideoUtil.findVlcLibrary();
        SimpleVlcPlayerPanel player_pnl = new SimpleVlcPlayerPanel();
        setContentPane(player_pnl);
        WidgetUtil.setFrameProperties(this, new Dimension(800, 600), true, null, WindowConstants.DISPOSE_ON_CLOSE);
        player_pnl.playFile(home_folder + File.separator + clip_name);
    }
}
