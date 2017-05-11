package org.marketsuite.riskmanager.portfolio;

import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.table.*;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.resource.ApolloConstants;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * A container for holding various textual stop level information to help deciding stop.
 */
public class InfoPanel extends SkinPanel {
    public InfoPanel() {
        super(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        setOpaque(false);

        //title strip with buttons for choosing views
        JPanel btn_pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));  btn_pnl.setOpaque(false);
        btn_pnl.add(_btnStopLevel);
        _btnStopLevel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) _pnlCard.getLayout()).show(_pnlCard, CARD_STOP_LEVEL);
            }
        });
        btn_pnl.add(Box.createGlue());
        btn_pnl.add(_btnSwp);
        _btnSwp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) _pnlCard.getLayout()).show(_pnlCard, CARD_SWP);
            }
        });
        btn_pnl.add(Box.createGlue());
        btn_pnl.add(_btnAtr);
        _btnAtr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((CardLayout) _pnlCard.getLayout()).show(_pnlCard, CARD_ATR);
            }
        });
        SkinPanel ttl_pnl = WidgetUtil.createTitleStrip(null, null, btn_pnl);
        add(ttl_pnl, BorderLayout.NORTH);

        //center - card panel with 3 views: levels, SWP, ATR fail rate
        _pnlCard = new JPanel(new CardLayout());
        _LevelTableModel = new LevelTableModel();
        JTable lvl_tbl = WidgetUtil.createDynaTable(_LevelTableModel, ListSelectionModel.SINGLE_SELECTION,
            new HeadingRenderer(), true, new DynaTableCellRenderer(_LevelTableModel));
        JScrollPane scr1 = new JScrollPane(lvl_tbl);  scr1.getViewport().setOpaque(false);
        _pnlCard.add(CARD_STOP_LEVEL, scr1);

        //second tab: SWP levels
        _SwpTableModel = new SwpTableModel();
        JTable swp_tbl = WidgetUtil.createDynaTable(_SwpTableModel, ListSelectionModel.SINGLE_SELECTION,
            new HeadingRenderer(), true, new DynaTableCellRenderer(_SwpTableModel));
        JScrollPane scr2 = new JScrollPane(swp_tbl);  scr2.getViewport().setOpaque(false);
        _pnlCard.add(CARD_SWP, scr2);

        //third tab: ATR failed rates
        _AtrTableModel = new AtrTableModel();
        JTable atr_tbl = WidgetUtil.createDynaTable(_AtrTableModel, ListSelectionModel.SINGLE_SELECTION,
                new HeadingRenderer(), true, new DynaTableCellRenderer(_AtrTableModel));
        JScrollPane scr3 = new JScrollPane(atr_tbl);  scr3.getViewport().setOpaque(false);
        _pnlCard.add(CARD_ATR, scr3);
        add(_pnlCard, BorderLayout.CENTER);
    }

    //-----public methods-----
    public void populate(StopLevelInfo level_info, ArrayList<FundQuote> swps, int bars_lookback) {
        _LevelTableModel.populate(level_info);
        _SwpTableModel.populate(swps);
        _AtrTableModel.populate(level_info, bars_lookback);
    }

    public void clear() {
        _LevelTableModel.clear();
        _SwpTableModel.clear();
        _AtrTableModel.clear();
    }

    //-----inner classes-----
    private class LevelTableModel extends DynaTableModel {
        private LevelTableModel() {
            remodel(DynaTableModel.generateSchema(TABLE_SCHEMA_LEVEL));
        }
        public void populate() {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA_LEVEL.length];
            cells[COLUMN_LEVEL] = new SimpleCell("");
            cells[COLUMN_DESCRIPTION] = new SimpleCell("");
            _lstRows.add(cells);
        }
        public boolean isCellEditable(int row, int col) { return false; }
        public void populate(StopLevelInfo sli) {
            ArrayList<StopLevel> stops = sli.gatherNominalLevels();
            _lstRows.clear();

            //show all stop values from low to high
            for (StopLevel sl : stops) {
                String id = sl.getId();
                boolean today_close = id.equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.STOP_CUSTOM_INDEX]);//steal this to represent today close
                boolean break_even_id = id.equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.BREAK_EVEN_INDEX]);
                String lvl = FrameworkConstants.DOLLAR_FORMAT.format(sl.getLevel());
//                boolean cur_stop_id = id.equals(StopLevelInfo.LIST_STOP_METHOD[StopLevelInfo.CURRENT_STOP_INDEX]);
//                if (today_close)
//                    buf.append("<u>").append(lvl).append("</u>");
//                else if (break_even_id)
//                    buf.append("<u>").append(FrameworkConstants.DOLLAR_FORMAT.format(sli.calcBreakEvenPrice())).append("</u>");
//                else
//                    buf.append(lvl);
//                buf.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
//                if (today_close)
//                    buf.append("<u>CLOSE</u>");
//                else if (break_even_id)
//                    buf.append("<u>").append(sl.getId()).append("</u>");
//                else if (cur_stop_id)
//                    buf.append("<u>").append(sl.getId()).append("</u>");
//                else
//                    buf.append(sl.getId());
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA_LEVEL.length];
                if (break_even_id)
                    lvl = FrameworkConstants.DOLLAR_FORMAT.format(sli.calcBreakEvenPrice());
                cells[COLUMN_LEVEL] = new SimpleCell(lvl);
                if (today_close)
                    id = ApolloConstants.APOLLO_BUNDLE.getString("slt_lbl_1");
                cells[COLUMN_DESCRIPTION] = new SimpleCell(id);
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
    }

    private class SwpTableModel extends DynaTableModel {
        private SwpTableModel() {
            remodel(DynaTableModel.generateSchema(TABLE_SCHEMA_SWP));
        }
        public void populate() {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA_LEVEL.length];
            cells[COLUMN_LEVEL] = new SimpleCell("");
            cells[COLUMN_DESCRIPTION] = new SimpleCell("");
            _lstRows.add(cells);
        }
        public boolean isCellEditable(int row, int col) { return false; }
        public void populate(ArrayList<FundQuote> swps) {
            _lstRows.clear();

            //show all stop values from low to high
            for (FundQuote swp : swps) {
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA_LEVEL.length];
                cells[COLUMN_LEVEL] = new SimpleCell(FrameworkConstants.DOLLAR_FORMAT.format(swp.getLow()));
                cells[COLUMN_DESCRIPTION] = new SimpleCell(swp.getDate());
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
    }

    private class AtrTableModel extends DynaTableModel {
        private AtrTableModel() {
            remodel(DynaTableModel.generateSchema(TABLE_SCHEMA_ATR));
        }
        public void populate() {
            SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA_LEVEL.length];
            cells[COLUMN_LEVEL] = new SimpleCell("");
            cells[COLUMN_DESCRIPTION] = new SimpleCell("");
            _lstRows.add(cells);
        }
        public boolean isCellEditable(int row, int col) { return false; }
        public void populate(StopLevelInfo sli, int bars_lookback) {
            _lstRows.clear();

            //show ATR fail rate from 1 ATR to 4 ATR with 0.25 increment
            for (float mul = 1.0f; mul <= 4.0f; mul += 0.25f) {
                float pct = sli.getATRMultipleFailRate(mul) / bars_lookback;
                SimpleCell[] cells = new SimpleCell[TABLE_SCHEMA_LEVEL.length];
                cells[COLUMN_LEVEL] = new SimpleCell(FrameworkConstants.PRICE_FORMAT.format(mul));
                cells[COLUMN_DESCRIPTION] = new SimpleCell(FrameworkConstants.ROI_FORMAT.format(pct));
                _lstRows.add(cells);
            }
            fireTableDataChanged();
        }
    }

    //-----instance variables------
    private LevelTableModel _LevelTableModel;
    private SwpTableModel _SwpTableModel;
    private AtrTableModel _AtrTableModel;
    private JButton _btnStopLevel = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_85"), FrameworkIcon.STOP);
    private JButton _btnSwp = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_86"), FrameworkIcon.LINE_CHART);
    private JButton _btnAtr = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("rm_87"), FrameworkIcon.RANGE);
    private JPanel _pnlCard;

    //-----literals-----
    private static final String CARD_STOP_LEVEL = "CARD_STOP_LEVEL";
    private static final String CARD_SWP = "CARD_SWP";
    private static final String CARD_ATR = "CARD_ATR";
    private static final int COLUMN_LEVEL = 0;
    private static final int COLUMN_DESCRIPTION = 1;
    private static final Object[][] TABLE_SCHEMA_LEVEL = {
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_80"),  ColumnTypeEnum.TYPE_STRING, -1,  20, null, null, null },//0, price level
        { ApolloConstants.APOLLO_BUNDLE.getString("rm_81"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null },//1, description
    };
    private static final Object[][] TABLE_SCHEMA_SWP = {
            { ApolloConstants.APOLLO_BUNDLE.getString("rm_80"),  ColumnTypeEnum.TYPE_STRING, -1,  20, null, null, null },//0, price level
            { ApolloConstants.APOLLO_BUNDLE.getString("rm_82"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null },//1, date
    };
    private static final Object[][] TABLE_SCHEMA_ATR = {
            { ApolloConstants.APOLLO_BUNDLE.getString("rm_84"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null },//0, multiplier
            { ApolloConstants.APOLLO_BUNDLE.getString("rm_83"),  ColumnTypeEnum.TYPE_STRING, -1,  40, null, null, null },//1, percent
    };
}