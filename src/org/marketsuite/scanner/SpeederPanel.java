package org.marketsuite.scanner;

import org.marketsuite.component.field.NameField;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.model.FundData;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.pattern.PatternUtil;
import org.marketsuite.framework.resource.FrameworkIcon;
import org.marketsuite.framework.util.DataUtil;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.resource.ApolloConstants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class SpeederPanel extends JPanel {
    public SpeederPanel() {
        setLayout(new MigLayout("insets 0"));

        //north - title strip, calender widget, strategy type, parameter widgets
        JPanel ttl_pnl = new JPanel(new MigLayout("insets 0", "10[]10[]10[]20[]", "3[]3")); ttl_pnl.setOpaque(false);
        ttl_pnl.add(_fldSymbol); ttl_pnl.add(_cmbTimeFrame);
        ttl_pnl.add(_cmbBarType); ttl_pnl.add(_btnRun); _btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //translate duration to index
                int tf_sel = _cmbTimeFrame.getSelectedIndex();
                int num_bar = 250;
                switch (tf_sel) {
                    case 0:  num_bar = 60; break;
                    case 1:  num_bar = 120; break;
                    case 2:  num_bar = 180; break;
                    case 3:  num_bar = 250; break;
                }
                int bt_sel = _cmbBarType.getSelectedIndex();

                //look up symbol's quotes
                _txaInfo.setText("");
                String sym = _fldSymbol.getText(); if (sym.equals("")) return;//must have a symbol
                try {
                    ArrayList<FundQuote> quotes = DataUtil.readHistory(sym, num_bar).getQuote();

                    //find fractals, print in text area
                    ArrayList<FundQuote> fractals = PatternUtil.findFractals(quotes, quotes.size() - 1);
                    StringBuilder buf = new StringBuilder();
                    for (FundQuote fq : fractals) {
                        buf.append(fq.getDate()).append("\t").append(fq.getLow()).append("\n");
                    }
                    _txaInfo.setText(buf.toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        add(ttl_pnl, "dock north");

        //center - area for calculated result
        add(_txaInfo, "dock center");
    }

    private NameField _fldSymbol = new NameField(5);
    private JComboBox<String> _cmbTimeFrame = new JComboBox<>(TIME_FRAME);
    private JComboBox<String> _cmbBarType = new JComboBox<>(BAR_TYPE);
    private JTextArea _txaInfo = new JTextArea();
    private JButton _btnRun = WidgetUtil.createIconButton(ApolloConstants.APOLLO_BUNDLE.getString("qp_59"), FrameworkIcon.RUN);
    private final static String[] TIME_FRAME = { "3 Month", "6 Month", "9 Month", "1 Year" };
    private final static String[] BAR_TYPE = { "Daily", "Weekly" };
}
