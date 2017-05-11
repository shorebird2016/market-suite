package org.marketsuite.riskmgr.matrix;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;

//a building block of a heat map matrix
public class HeatmapBlock extends JPanel {
    public HeatmapBlock(String title) {
        setLayout(new MigLayout("flowy"));

        //title
        add(new JLabel(title), "dock north");

        //body, n rows of BlockElement
        for (BlockElement element : elements) {
            add(element);
        }

        //south - summary
        JPanel sum_pnl = new JPanel(new MigLayout());
        add(sum_pnl, "dock south");
    }

    //----- variables -----
    private ArrayList<BlockElement> elements;
}
