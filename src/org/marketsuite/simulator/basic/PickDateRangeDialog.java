package org.marketsuite.simulator.basic;

import org.marketsuite.component.Constants;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.List;

//Simple dialog for user to select one or more watch lists
public class PickDateRangeDialog extends JDialog {
    public PickDateRangeDialog() {
        super(MdiMainFrame.getInstance(), "Select Date Range", true);

        //read from csv file for stored date range_info
        _names = new ArrayList<>(); _beginDates = new ArrayList<>(); _endDates = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER_QUOTE + File.separator + "sim-date-ranges.txt"));
            int line_num = 1; String line = "";
            //read comma separated file line by line
            while ( (line = br.readLine()) != null ) {
                if (line_num == 1) {
                    line_num++;
                    continue;//skip first header row
                }
                //parse line
                String[] tokens = line.split(",");
                _names.add(tokens[0]); _beginDates.add(tokens[1]); _endDates.add(tokens[2]);
            }
        } catch (Exception e1) { e1.printStackTrace(); WidgetUtil.showMessageInEdt(e1.getMessage()); }
        String[] range_info = new String[_names.size()];
        for (int i=0; i<range_info.length; i++)
            range_info[i] = _names.get(i) + " (" + _beginDates.get(i) + " to " + _endDates.get(i) + ")";
        _lstRanges = new JList<>(range_info);
        JPanel content = new JPanel(new MigLayout());
        content.add(new JScrollPane(_lstRanges), "dock center");

        //south - buttons
        JPanel btn_pnl = new JPanel(new MigLayout("", "push[][]push"));
        JButton ok = new JButton(Constants.COMPONENT_BUNDLE.getString("ok"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); _bCancelled = false; } });
        btn_pnl.add(ok);
        JButton cancel = new JButton(Constants.COMPONENT_BUNDLE.getString("cancel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); } });
        btn_pnl.add(cancel);
        content.add(btn_pnl, "dock south");
        setContentPane(content);
        WidgetUtil.setDialogProperties(this, new Dimension(350, 250), false, MdiMainFrame.getInstance(),
            WindowConstants.DISPOSE_ON_CLOSE);
    }

    public List<String> getWatchlists() {
        return _lstRanges.getSelectedValuesList();
    }
    public String getBeginDate() {
        int sel = _lstRanges.getSelectedIndex(); if (sel < 0) return null;//no selection
        return _beginDates.get(sel);
    }
    public String getEndDate() {
        int sel = _lstRanges.getSelectedIndex(); if (sel < 0) return null;//no selection
        return _endDates.get(sel);
    }
    public boolean isCancelled() { return _bCancelled; }

    //----- variables -----
    private ArrayList<String> _names, _beginDates, _endDates;
    private JList<String> _lstRanges;
    private boolean _bCancelled = true;//by default
}