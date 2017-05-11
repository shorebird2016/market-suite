package org.marketsuite.tool;

import org.marketsuite.component.field.DecimalField;
import org.marketsuite.framework.resource.FrameworkIcon;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.*;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.RoundedBalloonStyle;
import net.java.balloontip.utils.ToolTipUtils;
import net.miginfocom.swing.MigLayout;
import org.marketsuite.component.field.DecimalField;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class RiskCalcPanel extends JPanel {
    public RiskCalcPanel() {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setLayout(new MigLayout("insets 0, wrap 2", "5[right][left]5"));
        add(new JLabel("Ask: $:"));
        add(_fldAsk);
        add(new JLabel("Stop: $"));
        add(_fldStop);
        add(new JLabel("Target: $"));
        add(_fldTarget);
        add(new JLabel("Position Size: $" ));
        add(_fldCapital); _fldCapital.setText("2500");
        add(new JLabel("Max Risk: $"));
        add(_fldMaxRisk); _fldMaxRisk.setText("100");
//        add(new JSeparator(), "spanx, split 2, wrap");

        add(new JLabel("Risk / Share: $"));
        add(_txtRisk); _txtRisk.setEditable(false); _txtRisk.setForeground(Color.red);
        add(new JLabel("Reward / Risk:"));
        add(_txtRR); _txtRR.setEditable(false); _txtRR.setForeground(Color.green.darker());
        add(new JLabel("# Shares:" ));
        add(_txtShare); _txtShare.setEditable(false); _txtShare.setForeground(Color.blue);
        _txtShare.setBackground(Color.yellow);
        _txtShare.setFont(new Font("Verdana", Font.BOLD, 12));
        add(_btnCalc, "left");
        _btnCalc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //compute risk = ask - stop
                double risk = _fldAsk.getValue() - _fldStop.getValue();
                _txtRisk.setText(new DecimalFormat("#0.00").format(risk));
                double reward = _fldTarget.getValue() - _fldAsk.getValue();
                _txtRR.setText(new DecimalFormat("#0.00").format(reward / risk));
                double share1 = _fldCapital.getValue() / _fldAsk.getValue();
                double share2 = _fldMaxRisk.getValue() / risk;
                double min = Math.min(share1, share2);
                _txtShare.setText(String.valueOf((int)min));
            }
        });
        add(_btnClear, "right");
        _btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _fldAsk.setText("");
                _fldStop.setText("");
                _fldTarget.setText("");
                _txtRisk.setText("");
                _txtRR.setText("");
                _txtShare.setText("");
                _fldAsk.requestFocus();
            }
        });
    }

    //----- accessor -----
    private JButton getCalcButton() { return _btnCalc; }

    //----- variables -----
    private DecimalField _fldAsk = new DecimalField(1, 4, 1, 10000, null);
    private DecimalField _fldStop = new DecimalField(1, 4, 1, 10000, null);
    private DecimalField _fldCapital = new DecimalField(1500, 4, 1, 10000, null);
    private DecimalField _fldMaxRisk = new DecimalField(1, 4, 1, 500, null);
    private DecimalField _fldTarget = new DecimalField(1, 4, 1, 10000, null);
    private JTextField _txtRisk = new JTextField(4);
    private JTextField _txtRR = new JTextField(4);
    private JTextField _txtShare = new JTextField(4);
    private JButton _btnCalc = createIconButton("Perform Calculation", FrameworkIcon.CALCULATOR);
    private JButton _btnClear = createIconButton("Clear", FrameworkIcon.TRASH_EMPTY);

    private JButton createIconButton(String tooltip, Icon icon) {
        JButton ret = new JButton(icon);
        ret.setMargin(new Insets(0, 0, 0, 0));
        ret.setBorderPainted(false);
        ret.setBorder(null);
        ret.setFocusable(false);
        ret.setIconTextGap(0);
        attachToolTip(ret, tooltip, SwingConstants.RIGHT, SwingConstants.BOTTOM);
        return ret;
    }
    public static BalloonTip attachToolTip(JComponent attached_part, String tip_text,
                                           int horizontal_position, int vertical_position) {
        BalloonTipStyle style = new RoundedBalloonStyle(5, 5, new Color(250, 250, 150), Color.black);
        BalloonTip ret = new BalloonTip(attached_part, tip_text, style, false);
        ToolTipUtils.balloonToToolTip(ret, 100, 2000);
        BalloonTipPositioner posr = new CenteredPositioner(5);
        switch (horizontal_position) {
            case SwingConstants.LEFT:
                posr = (vertical_position == SwingConstants.TOP) ? new LeftAbovePositioner(5, 5)
                        : new LeftBelowPositioner(5, 5);
                break;

            case SwingConstants.RIGHT:
                posr = (vertical_position == SwingConstants.TOP) ? new RightAbovePositioner(5, 5)
                        : new RightBelowPositioner(5, 5);
                break;

            case SwingConstants.CENTER:
            default:
                break;
        }
        ret.setPositioner(posr);
        return ret;
    }
    public static void setFrameProperties(JFrame frame, Dimension dim, boolean resizable, Container parent, int close_action) {
        if (dim.width == 0)
            frame.pack();
        else
            frame.setSize(dim.width, dim.height);
        frame.setResizable(resizable);
        frame.setLocationRelativeTo(parent);
        frame.setDefaultCloseOperation(close_action);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        JFrame frm = new JFrame("Risk Calculator");
        RiskCalcPanel rcp = new RiskCalcPanel();
        frm.setContentPane(rcp);
        frm.getRootPane().setDefaultButton(rcp.getCalcButton());
        setFrameProperties(frm, new Dimension(0, 0), false, null, WindowConstants.EXIT_ON_CLOSE);
    }
}
