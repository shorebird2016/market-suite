package org.marketsuite.riskmgr.volatility;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.MatrixElement;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevelInfo;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import org.marketsuite.riskmgr.model.StopLevelInfo;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class VolatilityPanel extends JPanel {
    public VolatilityPanel() {
        setLayout(new MigLayout("insets 0"));
        JPanel ttl_pnl = new SkinPanel(LazyIcon.BACKGROUND_TABLE_HEADER, new MigLayout("insets 0", "5[][]10[][]push[][]push[]5", "5[]5"));
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_42")));
        ttl_pnl.add(_fldPlThreshold); _fldPlThreshold.setText("3.0");
        ttl_pnl.add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("rm_43")));
        ttl_pnl.add(_fldVolatilityThreshold); _fldVolatilityThreshold.setText("5.0");
        ttl_pnl.add(_btnRun);
        _btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //get threshold from fields, run thru all rows in table
                createMatrix();

                //get 4 lists from model, place them inside matrix
                ArrayList<MatrixElement> mes = getMatrixCell(true, true);
                _pnlMatrixLL.populate(mes);
                mes = getMatrixCell(true, false);
                _pnlMatrixLH.populate(mes);
                mes = getMatrixCell(false, true);
                _pnlMatrixHL.populate(mes);
                mes = getMatrixCell(false, false);
                _pnlMatrixHH.populate(mes);
                _pnlMatrix.doLayout();
            }
        });
        add(ttl_pnl, "dock north");
        _pnlMatrix.add(new JScrollPane(_pnlMatrixLL));
        _pnlMatrix.add(new JScrollPane(_pnlMatrixLH));
        _pnlMatrix.add(new JScrollPane(_pnlMatrixHL));
        _pnlMatrix.add(new JScrollPane(_pnlMatrixHH));
        add(_pnlMatrix, "dock center");
    }

    //----- public/protected methods -----

    //----- private methods -----
    private void createMatrix() {
        ArrayList<Position> positions = RiskMgrModel.getInstance().getPositions();
        cellsLcLv = new ArrayList<>();
        cellsLcHv = new ArrayList<>();
        cellsHcLv = new ArrayList<>();
        cellsHcHv = new ArrayList<>();
        for (Position pos : positions) {
            float cost = pos.getCost();
            StopLevelInfo sli = pos.getStopLevelInfo();
            float quote = sli.getFund().getQuote().get(0).getClose();
            double pl_pct = 100 * (quote - cost) / cost;
            float vol_atr = 100 * sli.getATR() / quote;
            boolean low_cost = pl_pct > (float)_fldPlThreshold.getValue();
            boolean low_vol = vol_atr < (float)_fldVolatilityThreshold.getValue();
            String sym = pos.getSymbol();
            MatrixElement me = new MatrixElement(sym, RiskMgrModel.getInstance().findPosition(sym), (float) pl_pct, (float) vol_atr);
            if (low_cost) {
                if (low_vol) cellsLcLv.add(me);
                else cellsLcHv.add(me);
            }
            else {
                if (low_vol) cellsHcLv.add(me);
                else cellsHcHv.add(me);
            }
        }
    }
    private ArrayList<MatrixElement> getMatrixCell(boolean low_cost, boolean low_volatility) {
        if (low_cost && low_volatility) return cellsLcLv;
        else if (low_cost && !low_volatility) return cellsLcHv;
        else if (low_volatility) return cellsHcLv;
        else return cellsHcHv;
    }

    //----- inner classes -----
    private class MatrixCellPanel extends JPanel {
        private MatrixCellPanel(int type) {
            cellType = type;
            setLayout(new MigLayout("insets 2 5 2 0, flowy, wrap 10"));
            setBorder(new BevelBorder(BevelBorder.LOWERED));
        }

        private void populate(ArrayList<MatrixElement> mes) {
            removeAll();
            switch (cellType) {
                case 0:
                    add(new JLabel("Low Cost, Low Volatility"), "dock north");
                    break;
                case 1:
                    add(new JLabel("Low Cost, High Volatility"), "dock north");
                    break;
                case 2:
                    add(new JLabel("High Cost, Low Volatility"), "dock north");
                    break;
                case 3:
                    add(new JLabel("High Cost, High Volatility"), "dock north");
                    break;
            }
            for (MatrixElement me : mes) {
                StringBuilder buf = new StringBuilder("<html><b>");
                buf.append(me.getSymbol()).append("</b>&nbsp;&nbsp;(");
                String pl = FrameworkConstants.PCT2_FORMAT.format(me.getPl() / 100);
                buf.append(pl).append(")<br>");//.append(me.getVolatility());
                add(new JLabel(buf.toString()));
            }
        }

        private int cellType;
    }

    //----- variables -----
    private JPanel _pnlMatrix = new JPanel(new GridLayout(2,2));
    private MatrixCellPanel _pnlMatrixLL = new MatrixCellPanel(0), _pnlMatrixLH = new MatrixCellPanel(1),
            _pnlMatrixHL = new MatrixCellPanel(2), _pnlMatrixHH = new MatrixCellPanel(3);
    private DecimalField _fldPlThreshold = new DecimalField(2.5, 5, 0, 100, null);//in percent P/L
    private DecimalField _fldVolatilityThreshold = new DecimalField(5, 5, 0, 100, null); //in percent
    private JButton _btnRun = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_46"), FrameworkIcon.RUN);

    //4 arrays of MatrixElement, one for each quadrant
    private ArrayList<MatrixElement> cellsLcLv = new ArrayList<>();
    private ArrayList<MatrixElement> cellsLcHv = new ArrayList<>();
    private ArrayList<MatrixElement> cellsHcLv = new ArrayList<>();
    private ArrayList<MatrixElement> cellsHcHv = new ArrayList<>();
}