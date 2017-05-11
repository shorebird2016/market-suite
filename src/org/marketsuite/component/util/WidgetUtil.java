package org.marketsuite.component.util;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.dialog.ProgressBar;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.DynaTable;
import org.marketsuite.component.table.DynaTableCellRenderer;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.*;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.RoundedBalloonStyle;
import net.java.balloontip.utils.ToolTipUtils;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXMonthView;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.table.DynaTable;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

// Helper class for various widgets.
public class WidgetUtil {
    //components related
    //Helper to set max, min and preferred size.
    public static void setMaxMinPrefSize(JComponent comp, int w, int h) {
        comp.setPreferredSize(new Dimension(w, h));
        comp.setMaximumSize(new Dimension(w, h));
        comp.setMinimumSize(new Dimension(w, h));
    }

    /**
     * Create an image button with no text gap, not focusable, no border.
     *
     * @param icon    for the button (user supplied icon)
     * @param tooltip of the button
     * @return the created button
     */
    public static JButton createIconButton(String tooltip, Icon icon) {
        JButton ret = new JButton(icon);
        ret.setMargin(new Insets(0, 0, 0, 0));
        ret.setBorderPainted(false);
        ret.setBorder(null);
        ret.setFocusable(false);
        ret.setIconTextGap(0);
        if (tooltip != null)
            attachToolTip(ret, tooltip, SwingConstants.RIGHT, SwingConstants.TOP);
//TODO combine FrameworkIcon and LazyIcon into one class
//        if (icon instanceof FrameworkIcon) {
//            FrameworkIcon ficon = (FrameworkIcon)icon;
//            ret.setDisabledIcon(ficon);
//        }
//        else if (icon instanceof LazyIcon) {
//            LazyIcon licon = (LazyIcon)icon;
//            ret.setDisabledIcon(licon);
//        }
        return ret;
    }

    /**
     * When table was left in editing mode and application need to retrieve
     * information from cells, it is necessary to stop editing first.
     *
     * @param table
     */
    public static void stopTableEditing(JTable table) {
        if (!table.isEditing())
            return;

        int editing_column = table.getEditingColumn();
//        if (editing_column == -1)
//            return; //not active

        TableColumnModel column_model = table.getColumnModel();
        TableColumn column = column_model.getColumn(editing_column);
        TableCellEditor editor = column.getCellEditor();
        if (editor != null) //has custom editor
            editor.stopCellEditing();
        else {//without custom cell editor, use default editor 1-189AJ9
            editor = table.getDefaultEditor(Boolean.class);
            editor.stopCellEditing();
        }
    }

    /**
     * To register Cancel key for dialog closing
     *
     * @param dialog to be registered
     */
    public static void registerCancelKey(final JDialog dialog) {
        InputMap inpMap = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inpMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Dispose");
        dialog.getRootPane().getActionMap().put("Dispose", new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent ev) {
                dialog.dispose();
            }
        });
    }

    //comp, frame, container related
    public static void centerComponent(Component comp) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = comp.getSize();
        comp.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
    }

    /**
     * Generic ask yes no options to save code.
     *
     * @param parent of dialog
     * @param msg    of action
     * @return true = yes
     */
    public static boolean confirmAction(Container parent, String msg) {
        int rsp = JOptionPane.showConfirmDialog(
                parent,
                msg,
                "Question",
                JOptionPane.YES_NO_OPTION
        );
        if (rsp == JOptionPane.NO_OPTION || rsp == JOptionPane.CLOSED_OPTION) {
            return false; //don't replace
        }
        return true;
    }

    /**
     * Option dialog for user to choose yes or no.
     *
     * @param parent of dialog
     * @param msg    at center of dialog
     * @return true = ok, false = cancel
     */
    public static boolean confirmOkCancel(Container parent, String msg) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                msg,
                "Question",
                JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.CANCEL_OPTION == result
                || JOptionPane.CLOSED_OPTION == result) { //re-select tree node
            return false;
        }
        return true;
    }

    //to set common frame, dialog properties during creation
    //  the sequence of call is very important, otherwise centering won't work
    public static void setFrameProperties(JFrame frame, Dimension dim, boolean resizable, Container parent, int close_action) {
        if (dim.width == 0)
            frame.pack();
        else
            frame.setSize(dim.width, dim.height);
        frame.setResizable(resizable);
        frame.setLocationRelativeTo(parent);
        frame.setDefaultCloseOperation(close_action);
        frame.setVisible(true);
    }

    public static void setDialogProperties(JDialog dialog, Dimension dim, boolean resizable,
                                           Container parent, int close_action) {
        setDialogProperties(dialog, dim, resizable, parent, close_action, true);
    }

    public static void setDialogProperties(JDialog dialog, Dimension dim, boolean resizable,
                                           Container parent, int close_action, boolean visible) {
        if (dim.width == 0)
            dialog.pack();
        else
            dialog.setSize(dim.width, dim.height);
        dialog.setResizable(resizable);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(close_action);
        dialog.setVisible(visible);
    }

    public static void setWindowProperties(JWindow window, Dimension dim) {
        if (dim == null || dim.width == 0)
            window.pack();
        else
            window.setSize(dim.width, dim.height);
        window.setAlwaysOnTop(true);
        window.setVisible(true);
    }

    //find an item in combobox, -1 = not found
    public static int findComboItem(JComboBox combo_box, Object item) {
        ComboBoxModel model = combo_box.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equals(item))
                return i;
        }
        return -1;
    }

    //To posistion component in a NULL layout container (absolute coordinate)
    //offset - measured from (0, 0) of container
    public static JComponent positionComponent(int offsetx, int offsety, JComponent comp) {
        Dimension size = comp.getPreferredSize();
        comp.setBounds(offsetx, offsety, size.width, size.height);
        return comp;
    }

    //Create a JLabel object with gray colored belt and white text label
    public static JLabel createLabelStripe(String cap) {
        JLabel label = new JLabel(cap, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setForeground(Color.white);
        label.setBackground(Color.gray);
        return label;
    }

    public static JLabel createFieldLabel(String text, boolean emphasized) {
        JLabel ret = new JLabel(text);
        ret.setFont(emphasized ? FrameworkConstants.SMALL_FONT_BOLD : FrameworkConstants.SMALL_FONT);
        ret.setForeground(Color.blue);
        return ret;
    }
    public static JTextField createBasicField(int width, boolean emphasized, boolean read_only, String tooltip) {
        JTextField ret = new JTextField(width);
        ret.setHorizontalAlignment(JTextField.RIGHT);
        ret.setEditable(!read_only);
        ret.setFont(emphasized ? FrameworkConstants.SMALL_FONT_BOLD : FrameworkConstants.SMALL_FONT);
        if (tooltip != null)
            WidgetUtil.attachToolTip(ret, tooltip, SwingConstants.LEFT, SwingConstants.BOTTOM);
        return ret;
    }
    //Shows the splash screen at the center of the screen
    // Parameter examples: MainApplet.class, DEF_APPRES_PROPERTY_FILE
    // Assuming the propFile is in "resources" subdirectory of the cls dir and
    // image files in "resources.images" subdirectory.
    public static void showSplashScreen(Class cls, String propFile) {//todo: is this useful ?????.....
        final Class appClass = cls;
        final String resPropFile = propFile;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (splashScreen == null) {
                    String filename = "";
                    String resFile = CoreUtil.getPathOnly(appClass.getName(), true) + "resources." + resPropFile;
                    try {
                        filename = ResourceBundle.getBundle(resFile)
                                .getString("image.SplashScreenImage");
                    } catch (MissingResourceException e) {
                        System.out.println("java.util.MissingResourceException: Couldn't find value for: image.SplashScreenImage");
                    }
                    String path = "resources/images/" + filename;
                    ImageIcon img = new ImageIcon(appClass.getResource(path));
                    splashScreen = new JWindow();
                    splashLabel = new JLabel(img);
                    splashScreen.getContentPane().add(splashLabel);
                    splashScreen.pack();
                }
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                splashScreen.setLocation(screenSize.width / 2 - splashScreen.getSize().width / 2,
                        screenSize.height / 2 - splashScreen.getSize().height / 2);
                splashScreen.setVisible(true);
            }
        });
    }

    /**
     * Hides the splash screen.
     */
    public static void hideSplashScreen() {//todo: is this useful ?????.....
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (splashScreen != null) {
                    splashScreen.setVisible(false);
                    splashScreen.dispose();
                }
            }
        });
    }

    //helper to create toggle button
    public static JToggleButton createToggleButton(Icon default_icon, String tooltip, ActionListener listener) {
        JToggleButton ret = new JToggleButton(default_icon);
        WidgetUtil.attachToolTip(ret, tooltip, SwingConstants.LEFT, SwingConstants.BOTTOM);
        ret.setBorderPainted(false);
        if (listener != null)
            ret.addActionListener(listener);
        return ret;
    }

    // create multiline html string from regular string
    // useful for long tooltips
    public static String getMultiLineString(Object value) {
        return getMultiLineString(value, 90);
    }

    public static String getMultiLineString(Object value, int maxlen) {
        String s = value.toString();
        int len = s.length();
        if (len == 0) {
            return null;
        }
        // split tooltip into multiple lines to fit the screen better
        StringBuilder sb = new StringBuilder("<html>");
        if (len > maxlen) {
            int start = 0;
            int count = 0;
            int i = 0;
            while (i < len) {
                if (s.charAt(i) == ' ' && count >= maxlen) {
                    sb.append(s.substring(start, start + count + 1));
                    start += count + 1;
                    count = -1;
                    if (start < len)
                        sb.append("<br>");
                }
                i++;
                count++;
            }
            if (count > 0) {
                sb.append(s.substring(start, start + count));
            }
        } else {
            sb.append(s);
        }
        sb.append("</html>");
        return sb.toString();
    }

    //to create a label within a flow layout panel
    public static JPanel createLabelPanel(String text) {
        JPanel lbl_pnl = new JPanel();
        lbl_pnl.setOpaque(false);
        JLabel id_lbl = new JLabel(text);
        id_lbl.setFont(Constants.LINK_FONT_BOLD);
        lbl_pnl.add(id_lbl);
        return lbl_pnl;
    }

    //to create a title strip typically used for panel title area
    public static SkinPanel createTitleStrip(String title_text) {
        SkinPanel ret = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new BorderLayout());
        ret.add(createLabelPanel(title_text), BorderLayout.WEST);
        return ret;
    }

    //to create a title strip with optional component on the east side, east_comp = null, empty
    //   note: do watch for the size of east_comp, for it may make strip taller than usual
    public static SkinPanel createTitleStrip(String title_text, JComponent east_comp) {
        SkinPanel ret = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new BorderLayout());
        ret.add(createLabelPanel(title_text), BorderLayout.WEST);
        if (east_comp != null)
            ret.add(east_comp, BorderLayout.EAST);
        return ret;
    }

    public static SkinPanel genTitleStrip(String title, JComponent east_comp) {
        SkinPanel ret = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout());
        JLabel lbl = new JLabel(title);
        lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ret.add(lbl);
        if (east_comp != null)
            ret.add(east_comp);
        return ret;
    }

    //to create a title strip with variable content
    public static SkinPanel createTitleStrip(JLabel label) {
        SkinPanel ret = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new BorderLayout());
        JPanel lbl_pnl = new JPanel();
        lbl_pnl.setOpaque(false);
        label.setFont(Constants.LINK_FONT_BOLD);
        lbl_pnl.add(label);
        ret.add(lbl_pnl, BorderLayout.WEST);
        return ret;
    }

    /**
     * To create a title strip with border layout and specified west, center and east components
     *
     * @param west_comp   west side component, null = not exist
     * @param center_comp west side component, null = not exist
     * @param east_comp   west side component, null = not exist
     * @return container instance
     */
    public static SkinPanel createTitleStrip(JComponent west_comp, JComponent center_comp, JComponent east_comp) {
        SkinPanel ret = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new BorderLayout());
        if (west_comp != null)
            ret.add(west_comp, BorderLayout.WEST);
        if (center_comp != null)
            ret.add(center_comp, BorderLayout.CENTER);
        if (east_comp != null)
            ret.add(east_comp, BorderLayout.EAST);
        return ret;
    }

    //to create a dyna table, selection_mode = -1, no selection, cell_renderer = null = use DynaTableCellRenderer2
    public static DynaTable createDynaTable(
            DynaTableModel model, int selection_mode,
            TableCellRenderer heading_renderer, boolean can_reorder, TableCellRenderer cell_renderer) {
        DynaTable table = new DynaTable(model);
        initDynaTable(table, model, selection_mode, heading_renderer, can_reorder, cell_renderer);
        return table;
    }

    public static void initDynaTable(
            JTable table, DynaTableModel model, int selection_mode,
            TableCellRenderer heading_renderer, boolean can_reorder, TableCellRenderer cell_renderer) {
        if (-1 != selection_mode)
            table.setSelectionMode(selection_mode);
        else
            table.setRowSelectionAllowed(false);
        table.getTableHeader().setDefaultRenderer(heading_renderer);
        table.getTableHeader().setReorderingAllowed(can_reorder);
        if (null == cell_renderer)
            cell_renderer = new DynaTableCellRenderer(model);
        table.setDefaultRenderer(String.class, cell_renderer);
        table.setDefaultRenderer(Long.class, cell_renderer);
        table.setDefaultRenderer(Double.class, cell_renderer);
        table.setDefaultRenderer(Boolean.class, cell_renderer);
        model.initTable(table.getColumnModel());
    }

    //hyperlink support
    //level = LEVEL_1 for login/main frame, level = LEVEL_2
    public static JXHyperlink createHyperLink(int level, String text, ActionListener listener) {
        JXHyperlink ret = new JXHyperlink();
        ret.setText(text);
        if (level == LEVEL_1) {
            ret.setForeground(Constants.LINK_COLOR_BOLD);
            ret.setClickedColor(Color.blue);//Constants.LINK_COLOR_BOLD);
        }
        else {
            ret.setFont(new Font("Verdana", Font.PLAIN, 10));//FrameworkConstants.SMALL_FONT);
            ret.setForeground(Color.blue);//Constants.LINK_COLOR_BOLD);
            ret.setClickedColor(Color.blue);//Constants.LINK_COLOR_BOLD);
        }
        ret.addActionListener(listener);
        ret.setFocusable(false);
        return ret;
    }

    //to control dirty cell width in a table
    public static void setColumnWidth(TableColumn column, int min, int max) {
        column.setMinWidth(min);
        column.setMaxWidth(max);
    }
    public static void forceColumnWidth(TableColumn column, int width) {
        column.setWidth(width);
        column.setPreferredWidth(width);
        column.setMaxWidth(width);
        column.setMinWidth(width);
    }

    // allow horizontal scrolling with the mouse wheel by holding shift down
    public static JScrollPane addHorizontalWheelScroll(final JScrollPane jsp) {
       // to make sure this is the first listener
       // 1. save and remove existing listeners
       // 2. add new listener for horizontal scrolling
       // 3. replace saved listeners
       MouseWheelListener[] savedListeners = jsp.getMouseWheelListeners();
       for(MouseWheelListener l : savedListeners)
          jsp.removeMouseWheelListener(l);
       jsp.addMouseWheelListener(new MouseWheelListener() {
          public void mouseWheelMoved(MouseWheelEvent ev) {
             if(ev.isShiftDown() ) {
                // if the vertical scrollbar is not visible the pane will scroll horizontally
                jsp.getVerticalScrollBar().setVisible(false);
                // visibility is returned by the jscrollpane
             }
          }
       });
       // replace saved listeners
       for(MouseWheelListener l : savedListeners)
          jsp.addMouseWheelListener(l);

       jsp.getHorizontalScrollBar().setUnitIncrement(15);
       return jsp;
    }

    //to create consistent balloon tool tip todo: add a system wide preference to turn on/off....
    // left_side = true for horizontal_position tip near left edge, false for right edge
    // horizontal_position = SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.CENTER
    // vertical_position = SwingContants.TOP, SwingConstants.BOTTOM
    public static BalloonTip attachToolTip(JComponent attached_part, String tip_text,
                                           int horizontal_position, int vertical_position) {
        BalloonTipStyle style = new RoundedBalloonStyle(5, 5, new Color(250, 250, 150), Color.black);
        BalloonTip ret = new BalloonTip(attached_part, tip_text, style, false);
        ToolTipUtils.balloonToToolTip(ret, 10, 60000);
        BalloonTipPositioner posr = new CenteredPositioner(5);
        switch (horizontal_position) {
            case SwingConstants.LEFT:
                posr = (vertical_position == SwingConstants.TOP) ? new LeftAbovePositioner(5, 5)
                        : new LeftBelowPositioner(5, 5);
                break;

            case SwingConstants.RIGHT:
                posr = (vertical_position == SwingConstants.TOP) ? new RightAbovePositioner(5, 5)
                        : new RightBelowPositioner(5, 5);
                break;

            case SwingConstants.CENTER:
            default:
                break;
        }
        ret.setPositioner(posr);
        return ret;
    }

    public static BalloonTip attachRightBalloonTip(JComponent attached, String tip_text) {
        BalloonTipStyle style = new RoundedBalloonStyle(5, 5, new Color(250, 250, 150), Color.black);
        BalloonTip ret = new BalloonTip(attached, tip_text, style, false);
        ToolTipUtils.balloonToToolTip(ret, 10, 60000);
        ret.setPositioner(new RightBelowPositioner(5, 5));
        return ret;
    }
    public static BalloonTip attachLeftBalloonTip(JComponent attached, String tip_text) {
        BalloonTipStyle style = new RoundedBalloonStyle(5, 5, new Color(250, 250, 150), Color.black);
        BalloonTip ret = new BalloonTip(attached, tip_text, style, false);
        ToolTipUtils.balloonToToolTip(ret, 10, 60000);
        ret.setPositioner(new LeftBelowPositioner(5, 5));
        return ret;
    }

    public static BalloonTip attachBalloonTip(JComponent attached, JComponent comp_inside) {
        BalloonTip ret = new BalloonTip(attached, comp_inside,
            new RoundedBalloonStyle(5, 5, new Color(250, 250, 150), Color.black), false);
        return ret;
    }

    /**
     * Ask user to select or enter a transaction file from list with specified extension pattern (eg. .trn).
     * Empty string return not allowed, always a default "file_name.xxx" returned.
     *
     * @param extension_pattern defined in FrameworkConstants.EXTENSION_XXX
     * @param parent            container window
     * @return File object(default "file_name.xxx") or null if cancelled or wrong extenstion
     */
    public static File selectSingleFile(final String extension_pattern, JFrame parent) {
        File ret = null;
        JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_TRADE_LOG));
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;

                int ext_pos = file.getName().lastIndexOf(extension_pattern);
                if (ext_pos > 0)
                    return true;
                return false;
            }

            public String getDescription() {//this shows up in description field of dialog
                return "*" + extension_pattern;
            }
        });
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int reply = fc.showOpenDialog(parent);
        if (reply == JFileChooser.APPROVE_OPTION) {
            ret = fc.getSelectedFile();
            String file_name = ret.getName();

            //warn about wrong extension
            if (!file_name.endsWith(extension_pattern)) {
                MessageBox.messageBox(parent, Constants.COMPONENT_BUNDLE.getString("warning"),
                        Constants.COMPONENT_BUNDLE.getString("dup_msg_1") + extension_pattern +
                                Constants.COMPONENT_BUNDLE.getString("dup_msg_2"),
                        MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
                return null;
            }

            //warn empty file name
            else if (file_name.equals("")) {
                MessageBox.messageBox(parent,
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        Constants.COMPONENT_BUNDLE.getString("empty_msg_1"),
                        MessageBox.OK_OPTION, MessageBox.WARNING_MESSAGE);
                return new File("file_Name" + extension_pattern);
            }

//todo check duplicate
        }
        return ret;
    }

    /**
     * Select N files from a folder with same extension.
     *
     * @param extension_pattern defined in FrameworkConstants.EXTENSION_XXX
     * @param parent            container frame
     * @return null if canceled or list of File object
     */
    public static File[] selectMultipleFiles(final String extension_pattern, JFrame parent) {
        JFileChooser fc = new JFileChooser(new File(FrameworkConstants.DATA_FOLDER_TRADE_LOG));
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;

                int ext_pos = file.getName().lastIndexOf(extension_pattern);
                if (ext_pos > 0)
                    return true;
                return false;
            }

            public String getDescription() {//this shows up in description field of dialog
                return "*" + extension_pattern;
            }
        });
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setMultiSelectionEnabled(true);
        int reply = fc.showOpenDialog(parent);
        if (reply == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFiles();
        }
        return null;//user cancel
    }

    /**
     * Display an ok/cancel warning dialog with specified caption.
     */
    public static void showWarning(JFrame parent, String caption) {
        MessageBox.messageBox(parent,
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"), caption,
                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
    }
    public static void showWarning(String caption) { showWarning(MdiMainFrame.getInstance(), caption); }
    public static void showMessageNoEdt(String caption) {
        MessageBox.messageBox(MdiMainFrame.getInstance(),
                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"), caption,
                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
    }
    public static void showMessageInEdt(final String caption) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                MessageBox.messageBox(MdiMainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"), caption,
                        MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
            }
        });
    }
    /**
     * Display simple warning dialog with message and optional turning progress bar off.
     *
     * @param parent       frame to center
     * @param message      to display (can have HTML)
     * @param progress_bar if not null, turn it off
     */
    public static void showWarningInEdt(final JFrame parent, final String message, final ProgressBar progress_bar) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                MessageBox.messageBox(parent,
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),
                        message,
                        MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE);
                if (progress_bar != null)
                    progress_bar.setVisible(false);
            }
        });

    }
    public static void setUIFont(FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    //----- Calendar Related -----
    public static JXMonthView createCalendar(Calendar initial_time) {
        JXMonthView ret = new JXMonthView();
        ret.setTraversable(true);
        ret.setTodayBackground(Color.pink);
        ret.setSelectionDate(initial_time.getTime());
        return ret;
    }

    //----- Table Related -----

    /**
     * Assume table is contained within a JScrollPane, scrolls the cell such that it is visible in viewport.
     * @param table JTable object
     * @param row row index
     * @param col column index
     */
    public static void scrollCellVisible(JTable table, int row, int col) {
        if (!(table.getParent() instanceof JViewport))
            return;

        JViewport viewport = (JViewport)table.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = table.getCellRect(row, col, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x-pt.x, rect.y-pt.y);

        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }

    //----- Tree Related -----
    /**
     * To create a file tree from a given file folder recursively.
     * @param folder root of tree
     * @param extension filter of files to include .csv for example TODO use FileFilter object
     * @return root node
     */
    public static DefaultMutableTreeNode createFileTree(String folder, String extension) {
        File f = new File(folder);
        DefaultMutableTreeNode top = new DefaultMutableTreeNode();
        top.setUserObject(f.getName());
        if (f.isDirectory()) {
            File fls[] = f.listFiles();
            for (int i = 0; i < fls.length; i++) {
                String name = fls[i].getName();
                if (name.endsWith(extension)
                        || fls[i].isDirectory()) //skip non-csv files
                    top.add(createFileTree(fls[i].getPath(), extension) );
                else
                    System.out.println("Ignoring...." + name);
            }
        }
        return (top);
    }
    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    public static void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }

    /**
     * @return Whether an expandPath was called for the last node in the parent path
     */
    private static boolean expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() > 0) {
            boolean childExpandCalled = false;
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                childExpandCalled = expandAll(tree, path, expand) || childExpandCalled; // the OR order is important here, don't let childExpand first. func calls will be optimized out !
            }

            if (!childExpandCalled) { // only if one of the children hasn't called already expand
                // Expansion or collapse must be done bottom-up, BUT only for non-leaf nodes
                if (expand) {
                    tree.expandPath(parent);
                } else {
                    tree.collapsePath(parent);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    //instance variables
    private static JWindow splashScreen;
    private static JLabel splashLabel;

    //literals
    public static final int EAST = GridBagConstraints.EAST;
    public static final int SOUTHEAST = GridBagConstraints.SOUTHEAST;
    public static final int NORTH = GridBagConstraints.NORTH;
    public static final int NORTHEAST = GridBagConstraints.NORTHEAST;
    public static final int WEST = GridBagConstraints.WEST;
    public static final int CENTER = GridBagConstraints.CENTER;
    public static final int BOTH = GridBagConstraints.BOTH;
    public static final int NONE = GridBagConstraints.NONE;
    public static final int HOR = GridBagConstraints.HORIZONTAL;
    public static final int VERT = GridBagConstraints.VERTICAL;
    public static final int LEVEL_1 = 1;//for login, main frame
    public static final int LEVEL_2 = 2;//for lower tabs
    //----- JFreeChart Related -----
    public static final Paint PAINT_BACKGROUND_LIGHT_GREEN = new GradientPaint(0, 0, new Color(220, 240, 240), 500, 500, new Color(240, 240, 240));
    public static final Paint PAINT_BACKGROUND_LIGHT_YELLOW = new GradientPaint(0, 0, new Color(240, 240, 150), 500, 500, new Color(240, 240, 230));
}