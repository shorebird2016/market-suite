package org.marketsuite.riskmgr.matrix;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

//A container with N blocks of tiles laid out in a matrix of X rows, Y columns
// each tile represents P/L, risk, volatility of an industry group of symbols + ETFs
// purpose: to have a view of portfolio allocation to gain insight of
//  (1) Risk distribution across industry groups
//  (2) Total risk taken
//  (3) Capital allocation among industry groups
//  (4)
//
//Mainly to answer those questions:
// (1) Am I taking enough risk ?  Too little or too much ?
// (2) Is my capital effectively/efficiently distributed across "strong" industry groups ?
// (3)

public class HeatmapPanel extends JPanel {
    public HeatmapPanel() {
        setLayout(new MigLayout("insets 0, wrap 5"));

    }
}
