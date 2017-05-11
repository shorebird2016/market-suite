package org.marketsuite.component.UI;

import org.marketsuite.component.Constants;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * A consistent L&F tab UI with distinct "CherryPicker" look.  Tabs can be at top level, level 2 or level 3.
 * When creating a new JTabbedPane with this UI, specify name as "Main" for top level, "Level2" for 2nd level
 * and "Level3" as third level.  Each level has distinct font, size...etc.
 */
public class MainTabUI extends BasicTabbedPaneUI implements PropertyChangeListener, ContainerListener {
    public MainTabUI() {
        readImages();
    }

    public MainTabUI(int level) {
        readImages();
        isMainTab = false;
        switch (level) {
            case 1: //top
                isMainTab = true;
                tabWidth = 150;
                isWidthSet = true;
                break;

            case 2: //2nd level nav
                tabWidth = 130;
                isWidthSet = true;
                break;

            case 3: //3rd level nav
                tabWidth = 90;
                isWidthSet = true;
                break;

            default: //others
                break;
        }
    }

    public void componentAdded(ContainerEvent e) {
        if (!isWidthSet)
            tabWidth = calcMaxWidth();
    }
    
    public void componentRemoved(ContainerEvent e) {
        if (!isWidthSet)
            tabWidth = calcMaxWidth();
    }

    public void propertyChange(PropertyChangeEvent ev) {
        String name = ev.getPropertyName();
        if ("name".equals(name)) {
            String n = (String) ev.getNewValue();
            isMainTab = "Main".equals(n);
            isLevel3 = "Level3".equals(n);
        }
    }

    private void readImages() {
        if (selectedImage == null) {
            try {
                URL url = MainTabUI.class.getResource("/org/marketsuite/component/UI/images/tabbedPaneTab_selected.png");
                selectedImage = javax.imageio.ImageIO.read(url);

                url = MainTabUI.class.getResource("/org/marketsuite/component/UI/images/tabbedPaneTab.png");
                bgImage = javax.imageio.ImageIO.read(url);

                url = MainTabUI.class.getResource("/org/marketsuite/component/UI/images/tabbedPaneTab_hover4.png");
                hoverImage = javax.imageio.ImageIO.read(url);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void installUI(JComponent c) {
        super.installUI(c);
        if (!isWidthSet)
            tabWidth = calcMaxWidth();
        tabPane.addContainerListener(this);
        tabPane.addPropertyChangeListener(this);
    }

    public void uninstallUI(JComponent c) {
        tabPane.removeContainerListener(this);
        tabPane.removePropertyChangeListener(this);
        super.uninstallUI(c);
    }

    private int calcMaxWidth() {
        FontMetrics metrics = selectedFontMetrics;
        int tabCount = tabPane.getTabCount();
        int placement = tabPane.getTabPlacement();
        int result = 0;
        for (int i = 0; i < tabCount; i++) {
            result = Math.max(super.calculateTabWidth(placement, i, metrics), result);
        }
        return result + 20;
    }

    public static ComponentUI createUI(JComponent c) {
        return new MainTabUI();
    }

    protected void installDefaults() {
        super.installDefaults();
        tabAreaInsets.left = 4;
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabInsets = selectedTabPadInsets;

        //Color background = tabPane.getBackground();

        //tabPane.setFont(new Font("Verdana",Font.BOLD,12));
        //boldFont = tabPane.getFont();
        //System.out.println(boldFont);
        normalFontMetrics = tabPane.getFontMetrics(selectedFont);
        selectedFontMetrics = tabPane.getFontMetrics(selectedFont);
    }

    //  Custom sizing methods
    public int getTabRunCount(JTabbedPane pane) {
        return 1;
    }

    protected Insets getContentBorderInsets(int tabPlacement) {
        return NO_INSETS;
    }

    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        int vHeight = fontHeight;
        if (vHeight % 2 > 0) {
            vHeight += 1;
        }
        if (isMainTab)
            return vHeight + 7;
        return vHeight + 4;
    }

    public void setTabWidth(int w) {
        tabWidth = w;
    }

    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        return tabWidth;
        //return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + metrics.getHeight() + 20;
    }

    protected void setRolloverTab(int index) {
        int temp = getRolloverTab();
        super.setRolloverTab(index);
        // synthetica will automatically do a repaint of the tabbed pane, but other UI implementations may not
        if (index != temp)
            tabPane.repaint();
    }

    private GeneralPath getGP(int x, int y, int w, int h, boolean isSelected) {
        GeneralPath gp = new GeneralPath();
        //int ydiff = isSelected ? 0 : 4;
        int ydiff = 0;
        w -= 2;
        gp.moveTo(x + diff, y + ydiff);
        gp.lineTo(x + w - diff, y + ydiff);
        gp.quadTo(x + w, y + ydiff, x + w, y + diff + ydiff);
        gp.lineTo(x + w, y + h);
        gp.lineTo(x, y + h);
        gp.lineTo(x, y + diff + ydiff);
        gp.quadTo(x, y + ydiff, x + diff, y + ydiff);
        return gp;
    }

    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        h += 6;
        GeneralPath gp = getGP(x, y, w, h, isSelected);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        if (isSelected) {
            //g2.setClip(gp);
            //g2.setColor(Color.white);
            //g2.drawImage(selectedImage,x,y,w,h,null);
            g2.setPaint(new TexturePaint(selectedImage, new Rectangle2D.Float(x, y, w, h)));
        }
        else {
            if (isMainTab) {
                if (tabPane.isEnabledAt(tabIndex)) {
                    if (tabIndex == getRolloverTab())
                        g2.setColor(LIGHTER_COLOR);
                    else
                        g2.setColor(DARK_COLOR);
                }
                else
                    // disabled color
                    g2.setColor(MAIN_DISABLED_COLOR);
            }
            else if (tabIndex == getRolloverTab())
                g2.setPaint(new TexturePaint(hoverImage, new Rectangle2D.Float(x, y, w, h)));
            else
                g2.setPaint(new TexturePaint(bgImage, new Rectangle2D.Float(x, y, w, h)));
        }
        g2.fill(gp);
        g2.dispose();
    }

    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        if (true) return;
//      if(!isSelected) return;
//      GeneralPath gp = getGP(x,y,w,h,isSelected);
//      g.setColor(Color.white);
//      Graphics2D g2 = (Graphics2D)g;
//      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                          RenderingHints.VALUE_ANTIALIAS_ON);
//      g2.draw(gp);
    }

    protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        if (true) return;
//      Rectangle selectedRect = selectedIndex < 0 ? null : getTabBounds(selectedIndex, calcRect);
//
//      selectedRect.width = selectedRect.width + (selectedRect.height / 2) - 1;
//
//      g.setColor(Color.BLACK);
//
//      g.drawLine(x, y, selectedRect.x, y);
//      g.drawLine(selectedRect.x + selectedRect.width + 1, y, x + w, y);
//
//      g.setColor(Color.WHITE);
//
//      g.drawLine(x, y + 1, selectedRect.x, y + 1);
//      g.drawLine(selectedRect.x + 1, y + 1, selectedRect.x + 1, y);
//      g.drawLine(selectedRect.x + selectedRect.width + 2, y + 1, x + w, y + 1);
//
//      g.setColor(shadow);
//      g.drawLine(selectedRect.x + selectedRect.width, y, selectedRect.x + selectedRect.width + 1, y + 1);
    }

    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        // Do nothing
    }

    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        // Do nothing
    }

    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
        // Do nothing
    }

    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects1, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        // Do nothing
    }

    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        //int tw = tabPane.getBounds().width;

        //g.setColor(fillColor);
        //g.fillRect(0, 0, tw, rects[0].height + 3);

        super.paintTabArea(g, tabPlacement, selectedIndex);
    }

    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        /*
       Graphics2D g2 = (Graphics2D)g;
       g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        */
        if (isSelected) {
            tabPane.setForegroundAt(tabIndex, Color.black);
            int vDifference = (int) (selectedFontMetrics.getStringBounds(title, g).getWidth()) - textRect.width;
            textRect.x -= (vDifference / 2);
         Font font1 = isLevel3 ? smallFont : selectedFont;
         super.paintText(g, tabPlacement, font1, selectedFontMetrics, tabIndex, title, textRect, isSelected);
        }
        else {
            if (isMainTab) {
                tabPane.setForegroundAt(tabIndex, MAIN_TEXT_COLOR);
                int vDifference = (int) (selectedFontMetrics.getStringBounds(title, g).getWidth()) - textRect.width;
                textRect.x -= (vDifference / 2);
                super.paintText(g, tabPlacement, selectedFont, selectedFontMetrics, tabIndex, title, textRect, isSelected);
            }
         else if (!isLevel3) {
                tabPane.setForegroundAt(tabIndex, TEXT_COLOR);
                int vDifference = (int) (normalFontMetrics.getStringBounds(title, g).getWidth()) - textRect.width;
                textRect.x -= (vDifference / 2);
                super.paintText(g, tabPlacement, normalFont, normalFontMetrics, tabIndex, title, textRect, isSelected);
            }
         else {//level 3 tab
             tabPane.setForegroundAt(tabIndex,TEXT_COLOR);
             int vDifference = (int)(normalFontMetrics.getStringBounds(title,g).getWidth()) - textRect.width;
             textRect.x -= (vDifference / 2);
             super.paintText(g, tabPlacement, smallFont, normalFontMetrics, tabIndex, title, textRect, isSelected);
         }
        }
    }

    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    //literals
    private int tabWidth/* = 140*/;
    private boolean isMainTab;
    private boolean isLevel3;//third level tab with smaller font
    private boolean isWidthSet;

    //static final int diff = 6;
    private static final int diff = 4;
    private static BufferedImage selectedImage, bgImage, hoverImage;
    private static Font normalFont = Constants.VERDONA_PLAIN_12;
    private static Font smallFont = Constants.LINK_FONT_BOLD;
    private static Font selectedFont = Constants.VERDONA_BOLD_12;
    private FontMetrics selectedFontMetrics, normalFontMetrics;
    private final Insets NO_INSETS = new Insets(2, 0, 0, 0);
    private final Color TEXT_COLOR = Color.black;
    private final Color MAIN_TEXT_COLOR = new Color(207, 198, 184);
    private final Color DARK_COLOR = new Color(40, 30, 30);
    private final Color LIGHTER_COLOR = new Color(80, 80, 80);
    private final Color MAIN_DISABLED_COLOR = Color.lightGray;
}