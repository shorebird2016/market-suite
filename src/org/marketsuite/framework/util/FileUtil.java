package org.marketsuite.framework.util;

import org.marketsuite.component.Constants;
import org.marketsuite.component.dialog.MessageBox;
import org.marketsuite.component.table.ColumnSchema;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;
import org.marketsuite.framework.model.EntryExitDates;
import org.marketsuite.framework.resource.FrameworkConstants;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import org.marketsuite.component.Constants;
import org.marketsuite.component.table.ColumnSchema;
import org.marketsuite.component.table.ColumnTypeEnum;
import org.marketsuite.component.table.DynaTableModel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Boolean;

/**
 * A collection of utilities to aid file operations
 */
public class FileUtil {
    /**
     * Export given table model contents into an Excel sheet with .xls or .csv extension.
     * @param table_model a DynaTableModel instance
     * @param root_folder for file chooser to show initial file location
     */
    public static void exportSheet(DynaTableModel table_model, File root_folder) {
        //ask user for file name
        JFileChooser fc = new JFileChooser(root_folder);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;

                //only showing .csv or .xls extension
                int csv_pos = file.getName().lastIndexOf(".csv");
                int xls_pos = file.getName().lastIndexOf(".xls");
                if (csv_pos > 0 || xls_pos > 0)
                    return true;
                return false;
            }

            public String getDescription() {//this shows up in description field of dialog
                return Constants.COMPONENT_BUNDLE.getString("exp_03");
            }
        });
        int rsp = fc.showSaveDialog(null);
        if (rsp == JFileChooser.APPROVE_OPTION) {
            File output_path = fc.getSelectedFile();
            String name = output_path.getName();
            if (!name.endsWith(".csv") && !name.endsWith("xls")) {
                MessageBox.messageBox(null, //TODO use passed in parent
                        Constants.COMPONENT_BUNDLE.getString("warning"),
                        Constants.COMPONENT_BUNDLE.getString("exp_04"),
                        MessageBox.STYLE_OK, MessageBox.WARNING_MESSAGE
                );
                return;
            }
            if (output_path.exists()) { //warn user if file exist
                if (MessageBox.messageBox(null, //TODO use passed in parent
                        Constants.COMPONENT_BUNDLE.getString("warning"),
                        Constants.COMPONENT_BUNDLE.getString("exp_01"),
                        MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE) != MessageBox.RESULT_OK)
                    return;
            }

            //write lines into this file from table model

            //separate handling for .csv and .xls files
            if (output_path.getName().endsWith(".csv")) {
                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(new FileWriter(output_path));
                } catch (IOException e) {
                    e.printStackTrace();
                    MessageBox.messageBox(null, //TODO use passed in parent
                            Constants.COMPONENT_BUNDLE.getString("warning"),
                            Constants.COMPONENT_BUNDLE.getString("exp_02"),
                            MessageBox.STYLE_OK_CANCEL, MessageBox.WARNING_MESSAGE
                    );
                }

                //header
                List<ColumnSchema> schema = table_model.getTableSchema();
                for (int col = 0; col < schema.size(); col++)
                    pw.print(schema.get(col).getName() + ",");
                pw.println();

                //write rows
                int row_cnt = table_model.getRowCount();
                for (int row = 0; row < row_cnt; row++) {
                    StringBuilder sb = new StringBuilder();
                    for (int col = 0; col < schema.size(); col++) {
                        Object cell_value = table_model.getCell(row, col).getValue();
                        int type = schema.get(col).getType();
                        String out_val = "";
                        switch (type) {
                            case ColumnTypeEnum.TYPE_BOOLEAN:
                                out_val = (Boolean) cell_value ? "Y" : "N";
                                break;

                            case ColumnTypeEnum.TYPE_DOUBLE:
                                if (cell_value.equals(""))
                                    continue;
                                out_val = NumberFormat.getInstance().format((Double) cell_value);
                                break;

                            case ColumnTypeEnum.TYPE_LONG:
                                if (cell_value.equals(""))
                                    continue;
                                out_val = String.valueOf((Long) cell_value);
                                break;

                            case ColumnTypeEnum.TYPE_STRING:
                            default:
                                if (cell_value != null) {
                                    if (cell_value instanceof String)
                                        out_val = (String) cell_value;
                                    else
                                        out_val = cell_value.toString();
                                }
                                break;
                        }
                        sb.append(out_val).append(",");
                    }
                    pw.println(sb.toString());
                }
                pw.flush();
                pw.close();
            }
            else {//.xls handling
                try {
                    WritableWorkbook wb = Workbook.createWorkbook(output_path);
                    WritableSheet ws = wb.createSheet(output_path.getName(), 0);

                    //header
                    List<ColumnSchema> schema = table_model.getTableSchema();
                    for (int col = 0; col < schema.size(); col++) {
                        Label l = new Label(col, 0, schema.get(col).getName());
                        ws.addCell(l);
                    }

                    //write rows
                    int row_cnt = table_model.getRowCount();
                    for (int row = 0; row < row_cnt; row++) {
                        for (int col = 0; col < schema.size(); col++) {
                            Object cell_value = table_model.getCell(row, col).getValue();
                            int type = schema.get(col).getType();
                            WritableCell cell = null;
                            switch (type) {
                                case ColumnTypeEnum.TYPE_BOOLEAN:
                                    cell = new Label(col, row + 1, (Boolean) cell_value ? "Y" : "N");
                                    break;

                                case ColumnTypeEnum.TYPE_DOUBLE:
                                    if (cell_value.equals(""))
                                        continue;
                                    cell = new Number(col, row + 1, (Double) cell_value);
                                    break;

                                case ColumnTypeEnum.TYPE_LONG:
                                    if (cell_value.equals(""))
                                        continue;
                                    cell = new Number(col, row + 1, (Long) cell_value);
                                    break;

                                case ColumnTypeEnum.TYPE_STRING:
                                default:
                                    if (cell_value != null) {
                                        if (cell_value instanceof String)
                                            cell = new Label(col, row + 1, (String) cell_value);
                                        else
                                            cell = new Label(col, row + 1, cell_value.toString());
                                    }
                                    break;
                            }
                            ws.addCell(cell);
                        }
                    }
                    wb.write();
                    wb.close();
                } catch (Exception  ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public static void copyFolder(File src, File dest, JTextArea output) throws IOException {
        if (src.isDirectory()) { //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
                if (output != null)
                    output.append("Create folder " + dest.getName() + "\n");
            }

            //list all the directory contents
            String files[] = src.list();
            for (String file : files) {
                //construct the src and dest file structure
                File src_file = new File(src, file);
                File dest_file = new File(dest, file);

                //recursive copy
                copyFolder(src_file, dest_file, output);
            }
        }
        else { //if file, then copy it, Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;

            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0)
                out.write(buffer, 0, length);
            in.close();
            out.close();
            if (output != null)
                output.append("File copied from " + src + " to " + dest + "\n");
        }
    }

    public static void delete(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            System.err.println("Can't delete " + f.getName());
    }

    //remove extension
    public static String removeExtension(String name, String extension) {
        int index = name.indexOf(extension);
        if (index > 0)
            return name.substring(0, index);
        else
            return name;//no such extension
    }

    //Is this file start with YYYY-MM-DD format a Friday ?
    public static boolean isFridayFile(File file1, String extension) {
        String name = file1.getName();
        String dt = FileUtil.removeExtension(name, extension);
        return  AppUtil.isDateFriday(AppUtil.stringToCalendarNoEx(dt));
    }

    //does this symbol exist in a given folder regardless of extension?
    public static boolean isSymbolExist(File folder, String symbol) {
        if (!folder.isDirectory()) return false;
        String[] names = folder.list();
        for (String name : names)
            if (name.startsWith(symbol))
                return true;
        return false;
    }

    //persist an object to a file via simple XML Encoding
    //NOTE: very important, inner classes don't work well with XMLEncoder
    //      all classes must observe java beans convention
    public static void persistObject(Object ee_dates, File store) throws IOException {
        FileOutputStream fos = new FileOutputStream(store);
        XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(fos));
        xe.writeObject(ee_dates);
        xe.close();
        fos.close();
    }
    public static Object retreiveObject(File store) throws IOException {
        FileInputStream fis = new FileInputStream(store);
        XMLDecoder xd = new XMLDecoder(new BufferedInputStream(fis));
        Object obj = xd.readObject();
        xd.close();
        fis.close();
        return obj;
    }

    //read watch lists preference file from other people or other computers, null = failure
    public static HashMap<String, ArrayList<String>> readWatchlists(File wl_file) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(wl_file);
            XMLDecoder dec = new XMLDecoder(new BufferedInputStream(is));
            HashMap<String, ArrayList<String>> ret = (HashMap<String, ArrayList<String>>) dec.readObject();
            dec.close();
            return ret;
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
        return null;
    }
}
