package org.marketsuite.scanner.query;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.component.field.LongIntegerField;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.framework.model.FundQuote;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.CandleUtil;
import org.marketsuite.resource.ApolloConstants;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.util.ArrayList;

//Container for candle related components
class CandlePanel extends JPanel {
    CandlePanel() {
        setLayout(new MigLayout("insets 0, wrap", "[grow]"));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        JPanel ttl = new SkinPanel(LazyIcon.BACKGROUND_TOOLBAR, new MigLayout("insets 0", "5[]5[]", "3[]3"));
        JLabel lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qic_ttl"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        ttl.add(_fldBars);
        lbl = new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qic_len"));
        ttl.add(lbl); lbl.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        add(ttl, "dock north");
        add(_chkDojiSpintop, "split 4"); add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qic_djtp_pct")), "gap 10");
        add(_fldDojiSpintopPct); add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qic_djtp_pb")));
        _fldDojiSpintopPct.setText("25.0");
        add(_chkEngulf, "split 4"); add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qic_egf_pct")), "gap 10");
        add(_fldEngulfPct); add(new JLabel("%"));
        _fldEngulfPct.setText("30.0");
        add(_chkHarimi, "split 3"); add(new JLabel(ApolloConstants.APOLLO_BUNDLE.getString("qic_hmi_ratio")), "gap 10");
        add(_fldHaramiRatio); _fldHaramiRatio.setText("3.0");
        add(_chkGap);
    }

    //----- protected methods ------
    boolean isDojiPresent(ArrayList<FundQuote> quotes) {
        if (!_chkDojiSpintop.isSelected()) return true;//feature inactive
        _lstDojiTop = CandleUtil.findDojiSpintop(quotes, (int)_fldBars.getValue(), (float)_fldDojiSpintopPct.getValue());
        return _lstDojiTop != null && _lstDojiTop.size() > 0;
    }
    boolean isEngulfPresent(ArrayList<FundQuote> quotes) {
        if (!_chkEngulf.isSelected()) return true;//feature inactive
        _lstEngulf = CandleUtil.findEngulf(quotes, (int) _fldBars.getValue(), (float) _fldEngulfPct.getValue(), true);
        return _lstEngulf != null && _lstEngulf.size() > 0;
    }
    boolean isHaramiPresent(ArrayList<FundQuote> quotes) {
        if (!_chkHarimi.isSelected()) return true;//feature inactive
        _lstHarami = CandleUtil.findHarami(quotes, (int) _fldBars.getValue(), (float) _fldHaramiRatio.getValue(), true);
        return _lstHarami != null && _lstHarami.size() > 0;
    }
    ArrayList<Integer> getDojiTops() { return _lstDojiTop; }
    ArrayList<Integer> getEngulfs() { return _lstEngulf; }
    ArrayList<Integer> getHaramis() { return _lstHarami; }

    //----- variables -----
    private LongIntegerField _fldBars = new LongIntegerField(10, 3, 5, 20);
    private JCheckBox _chkDojiSpintop = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qic_doji"));
    private DecimalField _fldDojiSpintopPct = new DecimalField(25, 4, 0.01, 50, null);
    private JCheckBox _chkEngulf = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qic_egf"));
    private DecimalField _fldEngulfPct = new DecimalField(30, 4, 0.01, 80, null);
    private JCheckBox _chkHarimi = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qic_harami"));
    private DecimalField _fldHaramiRatio = new DecimalField(3, 4, 2, 10, null);
    private JCheckBox _chkGap = new JCheckBox(ApolloConstants.APOLLO_BUNDLE.getString("qic_gap"));
    private ArrayList<Integer> _lstDojiTop, _lstEngulf, _lstHarami;
}
