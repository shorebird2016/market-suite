package org.marketsuite.framework.vlc;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.vlc.VideoUtil;
import net.miginfocom.swing.MigLayout;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

//simple container to hold VLC player
public class SimpleVlcPlayerPanel extends JPanel {
    public SimpleVlcPlayerPanel() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(_cmpPlayer, "dock center");
    }

    //----- play a file -----
    public void playFile(String path) {
        _cmpPlayer.getMediaPlayer().playMedia(path);
    }
    public void cleanup() { _cmpPlayer.release(); }

    //----- variables -----
    private EmbeddedMediaPlayerComponent _cmpPlayer = new EmbeddedMediaPlayerComponent();

    //----- test -----
    public static void main(String[] args) {
        String home_folder = System.getProperty("user.home");
        VideoUtil.findVlcLibrary();
        JFrame frame = new JFrame();
        SimpleVlcPlayerPanel player_pnl = new SimpleVlcPlayerPanel();
        frame.setContentPane(player_pnl);
        WidgetUtil.setFrameProperties(frame, new Dimension(550, 350), true, null, WindowConstants.DISPOSE_ON_CLOSE);
        player_pnl.playFile(home_folder + File.separator + "clip1.avi");
    }
}