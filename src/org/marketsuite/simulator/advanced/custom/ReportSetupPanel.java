package org.marketsuite.simulator.advanced.custom;

import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.DisabledIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Sub-tab for setting up summary reports
 */
class ReportSetupPanel extends JPanel {
    ReportSetupPanel() {
        setLayout(new BorderLayout());

        //west - refresh button and info
        JPanel west_pnl = new JPanel();  west_pnl.setOpaque(false);
        west_pnl.add(_btnRefresh);
        _btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //re-build tree root, then update tree model, tree will change automatically
                DefaultMutableTreeNode root = WidgetUtil.createFileTree(FrameworkConstants.DATA_FOLDER_EXPORT,
                        FrameworkConstants.EXTENSION_CSV);
                _modelNavigator = new DefaultTreeModel(root);
                _treNavigator.setModel(_modelNavigator);
            }
        });
        west_pnl.add(new JLabel(FrameworkConstants.FRAMEWORK_BUNDLE.getString("lbl_rpt_setup")));

        //title strip - C R U D buttons
        JPanel btn_pnl = new JPanel();
        btn_pnl.setOpaque(false);
        btn_pnl.add(_btnOpen);
        _btnOpen.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILE_OPEN.getImage()));
        _btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //open dialog, read template into list pane
                String template_path = FrameworkConstants.DATA_FOLDER_REPORT;
                JFileChooser fc = new JFileChooser(new File(template_path));
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory())
                            return true;
                        //only allow .rpt extension
                        int ext_pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_REPORTS);
                        if (ext_pos > 0)
                            return true;
                        return false;
                    }

                    public String getDescription() {//this shows up in description field of dialog
                        return FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_report_descr");
                    }
                });
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
                int ret = fc.showOpenDialog(ReportSetupPanel.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File template_file = fc.getSelectedFile();
                    try {
                        ArrayList<String> file_list = ReportUtil.openTemplate(template_file.getPath());
                        _FileListModel.clear();
                        for (String file : file_list)
                            _FileListModel.addElement(file);
                        _CurrentFile = template_file;
                        _btnDelete.setEnabled(true);
                        _lblReportName.setText(_CurrentFile.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.messageBox(MdiMainFrame.getInstance(),
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),//title
                                e.getMessage(),//caption
                                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
                    }
                }
            }
        });
        btn_pnl.add(Box.createHorizontalGlue());
        btn_pnl.add(_btnSave);
        _btnSave.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILE_SAVE.getImage()));
        _btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //overwrite current report template
                if (_CurrentFile == null) {
                    return;
                }

                //get list of files
                Object[] list = _FileListModel.toArray();
                ArrayList<String> files = new ArrayList<String>();
                for (Object f : list) {
                    if (f instanceof String)
                        files.add((String) f);
                }
                try {
                    String path = _CurrentFile.getPath();
                    //if no extension, add on
                    if (!path.endsWith(FrameworkConstants.EXTENSION_REPORTS))
                        path += FrameworkConstants.EXTENSION_REPORTS;
                    saveTemplate(files, path);
                    _btnSave.setEnabled(false);
                    _btnSaveAs.setEnabled(false);
                    _btnDelete.setEnabled(true);
                    _lblReportName.setText(_CurrentFile.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                    MessageBox.messageBox(MdiMainFrame.getInstance(),
                            FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),//title
                            e.getMessage(),//caption
                            MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
                }
            }
        });
        _btnSave.setEnabled(false);
        btn_pnl.add(Box.createHorizontalGlue());
        btn_pnl.add(_btnSaveAs);
        _btnSaveAs.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILE_SAVE_AS.getImage()));
        _btnSaveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String template_path = FrameworkConstants.DATA_FOLDER_REPORT;
                //ask template name, write to new file todo consider combine file chooser.........
                JFileChooser fc = new JFileChooser(new File(template_path));//must use this folder
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isDirectory())
                            return true;
                        //only allow .rpt extension
                        int ext_pos = file.getName().lastIndexOf(FrameworkConstants.EXTENSION_REPORTS);
                        if (ext_pos > 0)
                            return true;
                        return false;
                    }

                    public String getDescription() {//this shows up in description field of dialog
                        return FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_report_descr");
                    }
                });
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setAcceptAllFileFilterUsed(false);
                int ret = fc.showSaveDialog(ReportSetupPanel.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    //check duplicate
                    File template_file = fc.getSelectedFile();
                    if (template_file.exists()) {
                        int rsp = MessageBox.messageBox(MdiMainFrame.getInstance(),
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),//title
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_1"),//caption
                                MessageBox.STYLE_OK_CANCEL, MessageBox.IMAGE_WARNING);
                        if (rsp != MessageBox.RESULT_OK)
                            return;
                    }
                    //get list of files
                    Object[] list = _FileListModel.toArray();
                    ArrayList<String> files = new ArrayList<String>();
                    for (Object f : list) {
                        if (f instanceof String)
                            files.add((String) f);
                    }
                    try {
                        String path = template_file.getPath();
                        //if no extension, add on
                        if (!path.endsWith(FrameworkConstants.EXTENSION_REPORTS))
                            path += FrameworkConstants.EXTENSION_REPORTS;
                        saveTemplate(files, path);
                        _CurrentFile = new File(path);
                        _btnSave.setEnabled(false);
                        _btnSaveAs.setEnabled(false);
                        _btnDelete.setEnabled(true);
                        _lblReportName.setText(_CurrentFile.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                        MessageBox.messageBox(MdiMainFrame.getInstance(),
                                FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),//title
                                e.getMessage(),//caption
                                MessageBox.STYLE_OK, MessageBox.IMAGE_WARNING);
                    }
                }
            }
        });
        _btnSaveAs.setEnabled(false);
        btn_pnl.add(Box.createHorizontalGlue());
        btn_pnl.add(_btnDelete);
        _btnDelete.setDisabledIcon(new DisabledIcon(FrameworkIcon.FILE_DELETE.getImage()));
        _btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int rsp = MessageBox.messageBox(MdiMainFrame.getInstance(),
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("warning"),//title
                        FrameworkConstants.FRAMEWORK_BUNDLE.getString("msg_2"),//caption
                        MessageBox.STYLE_OK_CANCEL, MessageBox.IMAGE_WARNING);
                if (rsp != MessageBox.RESULT_OK)
                    return;
                //remove file template from folder
                _CurrentFile.delete();//todo, it doesn't work?????
                _CurrentFile = null;
                _btnSave.setEnabled(false);
                _btnSaveAs.setEnabled(false);
                _btnDelete.setEnabled(true);
                _FileListModel.clear();//empty file list
                _lblReportName.setText("");//no more file
            }
        });
        _btnDelete.setEnabled(false);
        btn_pnl.add(Box.createHorizontalGlue());
        btn_pnl.add(_btnClear);
        _btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _FileListModel.clear();
                _lblReportName.setText("");
            }
        });

        JPanel name_pnl = new JPanel();
        name_pnl.setOpaque(false);
        name_pnl.add(_lblReportName);
        name_pnl.add(Box.createHorizontalGlue());
        SkinPanel north_pnl = WidgetUtil.createTitleStrip(west_pnl, name_pnl, btn_pnl);
        add(north_pnl, BorderLayout.NORTH);

        //center - split pane with tree and list
        JSplitPane cen_pnl = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        cen_pnl.setContinuousLayout(true);
        cen_pnl.setDividerLocation(350);

        //left pane - file tree from /export
        JPanel left_pnl = new JPanel(new BorderLayout());
        DefaultMutableTreeNode root = WidgetUtil.createFileTree(FrameworkConstants.DATA_FOLDER_EXPORT,
                FrameworkConstants.EXTENSION_CSV);
        _modelNavigator = new DefaultTreeModel(root);
        _treNavigator = new JTree(_modelNavigator);
        _treNavigator.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse) {
                //turn on/off arrow button
                _bthPickFiles.setEnabled(_treNavigator.getSelectionCount() > 0);
            }
        });
        left_pnl.add(new JScrollPane(_treNavigator), BorderLayout.CENTER);
        left_pnl.add(_bthPickFiles, BorderLayout.SOUTH);
        _bthPickFiles.setDisabledIcon(new DisabledIcon(FrameworkIcon.RIGHT_ARROW.getImage()));
        _bthPickFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //add selected file paths to the list on the right
                TreePath[] sel = _treNavigator.getSelectionModel().getSelectionPaths();
                for (TreePath tp : sel) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
                    Object[] objp = node.getUserObjectPath();
                    StringBuilder str = new StringBuilder();
                    for (Object o : objp)
                        str.append(o).append("/");
                    str.setLength(str.length() - 1);//remove trailing /
                    if (_FileListModel.indexOf(str.toString()) >= 0)
                        continue;//skip duplicate
                    _FileListModel.addElement(str.toString());
                }
                _btnSave.setEnabled(_CurrentFile != null);
                _btnSaveAs.setEnabled(true);
            }
        });
        _bthPickFiles.setEnabled(false);
        cen_pnl.setLeftComponent(left_pnl);

        //right pane - files from user selection
        JPanel rite_pnl = new JPanel(new BorderLayout());
        rite_pnl.add(new JScrollPane(_lstFiles = new JList(_FileListModel)), BorderLayout.CENTER);
        _lstFiles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                _bthRemoveFiles.setEnabled(_lstFiles.getSelectedIndices().length > 0);
            }
        });
        rite_pnl.add(_bthRemoveFiles, BorderLayout.SOUTH);
        _bthRemoveFiles.setDisabledIcon(new DisabledIcon(FrameworkIcon.LEFT_ARROW.getImage()));
        _bthRemoveFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Object[] sel = _lstFiles.getSelectedValues();
                for (Object o : sel)
                    _FileListModel.removeElement(o);
                _btnSave.setEnabled(_CurrentFile != null);
                _btnSaveAs.setEnabled(true);
            }
        });
        _bthRemoveFiles.setEnabled(false);
        cen_pnl.setRightComponent(rite_pnl);

        add(cen_pnl, BorderLayout.CENTER);
    }

    // create the tree from the file system recursively.
    private DefaultMutableTreeNode createTree(String folder) {
        File f = new File(folder);
        DefaultMutableTreeNode top = new DefaultMutableTreeNode();
        top.setUserObject(f.getName());
        if (f.isDirectory()) {
            File fls[] = f.listFiles();
            for (int i = 0; i < fls.length; i++) {
                String name = fls[i].getName();
                if (name.endsWith(FrameworkConstants.EXTENSION_CSV)
                        || name.endsWith(FrameworkConstants.EXTENSION_XLS)
                        || fls[i].isDirectory()) //skip non-csv files
                    top.add(createTree(fls[i].getPath()));
                else
                    System.out.println("Ignoring...." + name);
            }
        }
        return (top);
    }

    //save a list to a file
    private void saveTemplate(ArrayList<String> list, String file_path) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(file_path));
        for (String file : list)
            pw.println(file);
        pw.close();
    }

    //-----accessor-----
    File getCurrentFile() { return _CurrentFile; }

    //-----instance variables-----
    private JButton _btnRefresh = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_refresh"), FrameworkIcon.REFRESH);
    private JButton _btnOpen = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_open_list"), FrameworkIcon.FILE_OPEN);
    private JButton _btnSave = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_update_list"), FrameworkIcon.FILE_SAVE);
    private JButton _btnSaveAs = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_new_list"), FrameworkIcon.FILE_SAVE_AS);
    private JButton _btnDelete = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_delete_list"), FrameworkIcon.FILE_DELETE);
    private JButton _bthPickFiles = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_pick_files"), FrameworkIcon.RIGHT_ARROW);
    private JButton _bthRemoveFiles = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_remove_files"), FrameworkIcon.LEFT_ARROW);
    private JButton _btnClear = WidgetUtil.createIconButton(FrameworkConstants.FRAMEWORK_BUNDLE.getString("tip_delete_list"), FrameworkIcon.CLEAR);
    private JLabel _lblReportName = new JLabel();
    private JTree _treNavigator;
    private DefaultTreeModel _modelNavigator;
    private JList _lstFiles;
    private DefaultListModel _FileListModel = new DefaultListModel();
    private File _CurrentFile;
}