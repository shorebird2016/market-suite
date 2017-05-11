package org.marketsuite.riskmgr.matrix;

import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.MatrixElement;
import org.marketsuite.riskmgr.model.Position;
import org.marketsuite.riskmgr.model.RiskMgrModel;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.riskmgr.model.RiskMgrModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//container to render portfolio matrix from RiskMgrModel
public class MatrixPanel extends JPanel {
    public MatrixPanel() {
        setLayout(new MigLayout("insets 0, wrap 6"));//6 provides taller cell vertically
    }

    //render matrix in view
    public void populate(HashMap<String, ArrayList<MatrixElement>> portfolio_matrix) {
        removeAll();

        //create new set of MatrixCells, one for each map element's array
        Iterator<String> itor = portfolio_matrix.keySet().iterator();
        while (itor.hasNext()) {
            String group = itor.next();
            ArrayList<MatrixElement> matrix_elements = portfolio_matrix.get(group);
            MatrixCell cell = new MatrixCell(group, matrix_elements);
            add(cell);
        }
    }

    //----- inner classes -----
    private class MatrixCell extends JPanel {
        private MatrixCell(String title, ArrayList<MatrixElement> matrix_elements) {
            setLayout(new MigLayout("insets 0"));
            setBorder(new BevelBorder(BevelBorder.RAISED));
            JPanel ttl = new JPanel(new MigLayout("insets 0 0 0 0", "5[]push[][][]", "2[]2"));
            JLabel lbl = new JLabel(title); lbl.setFont(FrameworkConstants.FONT_TINY_BOLD);
            ttl.add(lbl);
            ttl.add(_txtRiskPct); _txtRiskPct.setFont(FrameworkConstants.FONT_TINY); _txtRiskPct.setEditable(false);
            ttl.add(_txtWeightPct);  _txtWeightPct.setFont(FrameworkConstants.FONT_TINY); _txtWeightPct.setEditable(false);
            add(ttl, "dock north");

            //center - table
            _tmMatrix = new MatrixTableModel();
            JTable tbl = WidgetUtil.createDynaTable(_tmMatrix, ListSelectionModel.SINGLE_SELECTION,
                new SortHeaderRenderer(FrameworkConstants.FONT_TINY), true, new MatrixCellRenderer(_tmMatrix));
            tbl.setAutoCreateRowSorter(true);
            add(new JScrollPane(tbl), "dock center");

            //immediate populate, update tooltip
            _tmMatrix.populate(matrix_elements);
            float total_cost = RiskMgrModel.getInstance().calcTotalCost();
            float total_risk = RiskMgrModel.getInstance().calcTotalRisk();
            float cell_cost = 0, cell_risk = 0;
            for (MatrixElement me : matrix_elements) {
                Position pos = me.getPosition();
                cell_cost += pos.getCost() * pos.getShares();
                float risk = me.getPosition().getRisk();
                if (risk >= 0) continue;//skip positive risk
                cell_risk += risk;
            }
            _txtWeightPct.setText(FrameworkConstants.PCT2_FORMAT.format(cell_cost / total_cost));
            float rsk_pct = cell_risk / total_risk;
            _txtRiskPct.setText(FrameworkConstants.PCT2_FORMAT.format(rsk_pct > 0 ? rsk_pct : -rsk_pct));
            WidgetUtil.attachToolTip(_txtRiskPct, ApolloConstants.APOLLO_BUNDLE.getString("rm_93") + " " +
                FrameworkConstants.DOLLAR_FORMAT.format(cell_risk), SwingConstants.CENTER, SwingConstants.TOP);
        }

        //variables
        private MatrixTableModel _tmMatrix;
        private JTextField _txtRiskPct = WidgetUtil.createBasicField(4, false, true, null);
        private JTextField _txtWeightPct = WidgetUtil.createBasicField(4, false, true, ApolloConstants.APOLLO_BUNDLE.getString("rm_92"));
    }
    private class MatrixTableModel extends DynaTableModel {
        private MatrixTableModel() { remodel(DynaTableModel.generateSchema(TABLE_SCHEMA)); }
        public void populate() { }
        public boolean isCellEditable(int row, int col) { return false; }
        private void populate(ArrayList<MatrixElement> cell_data) {
            _lstRows.clear();
            for (MatrixElement me : cell_data) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA.length];
                cells[COLUMN_SYMBOL] = new SimpleCell(me.getSymbol());
                cells[COLUMN_PROFIT_LOSS_PERCENT] = new SimpleCell(new Double(me.getPl()));
                cells[COLUMN_VOLATILITY] = new SimpleCell(new Double(me.getVolatility()));
                float risk = me.getPosition().getRisk();
                cells[COLUMN_RISK] = new SimpleCell(new Double(risk > 0 ? 0 : risk));
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
    }
    private class MatrixCellRenderer extends DynaTableCellRenderer {
        private MatrixCellRenderer(MatrixTableModel model) { super(model); _TableModel = model; }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            JLabel ret = (JLabel)comp;
            ret.setFont(FrameworkConstants.FONT_TINY);
            ret.setToolTipText("");
            int model_col = table.convertColumnIndexToModel(column);
            switch(model_col) {
                case COLUMN_RISK:
                    ret.setText(FrameworkConstants.DOLLAR2_FORMAT.format(value));
                    if ((Double)value < 0)
                        ret.setBackground(FrameworkConstants.LIGHT_PINK);
                    return ret;

                //show bold red letter for P/L percent
                case COLUMN_VOLATILITY:
                    ret.setText(FrameworkConstants.PCT2_FORMAT.format(value));
                    return ret;

                case COLUMN_PROFIT_LOSS_PERCENT:
                    ret.setText(FrameworkConstants.PCT2_FORMAT.format(value));
                    if ((Double)value > 0)
                        ret.setForeground(new Color(10, 79, 45));
                    else
                        ret.setForeground(new Color(217, 4, 4));
                    return ret;
            }
            return comp;
        }

        private MatrixTableModel _TableModel;
    }

    //----- literals -----
    private static final int COLUMN_SYMBOL = 0;
    private static final int COLUMN_PROFIT_LOSS_PERCENT = 1;
    private static final int COLUMN_VOLATILITY = 2;
    private static final int COLUMN_RISK = 3;
    private static final Object[][] TABLE_SCHEMA = {
        //heading, type, visible, hide-able, alignment, width, editor, renderer, comparator
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_51"),  ColumnTypeEnum.TYPE_STRING, -1,  10, null, null, null },//0, symbol
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_52"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  10, null, null, null },//1, P/L
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_53"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  10, null, null, null },//2, volatility
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_54"),  ColumnTypeEnum.TYPE_DOUBLE, -1,  10, null, null, null },//3, risk
    };
}
