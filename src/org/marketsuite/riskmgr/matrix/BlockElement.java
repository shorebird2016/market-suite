package org.marketsuite.riskmgr.matrix;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.riskmgr.model.MatrixElement;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.framework.resource.FrameworkConstants;

import javax.swing.*;

//container for one symbol with associated information such as P/L, risk, volatility
public class BlockElement extends JPanel {
    public BlockElement() {
        setLayout(new MigLayout("", "5[]5[]5[]5[]5"));
        add(_lblSymbol); _lblSymbol.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        add(_lblProfitLoss);
        add(_lblRisk);
        add(_lblVolatility);
    }

    //-----variables-----
    private MatrixElement data;
    private JLabel _lblSymbol = new JLabel("01234");
    private JLabel _lblProfitLoss = new JLabel();
    private JLabel _lblRisk = new JLabel();
    private JLabel _lblVolatility = new JLabel();
}
