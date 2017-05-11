package org.marketsuite.watchlist.mgr;

import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.util.WidgetUtil;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.main.MdiMainFrame;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * Small dialog for selecting type of file for import.
 */
public class ViewSymbolsDialog extends JDialog {
    public ViewSymbolsDialog(final ArrayList<String> symbols, boolean view_only) {
        super(MdiMainFrame.getInstance(), true);
        SkinPanel content_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        content_pnl.setBorder(new BevelBorder(BevelBorder.LOWERED));

        //center - box layout with two rows
        JPanel cen_pnl = new JPanel(new GridLayout(1,1)); cen_pnl.setOpaque(false);
        cen_pnl.setBorder(new EmptyBorder(5, 5, 5, 5));
        cen_pnl.add(new JScrollPane(_txaSymbols));  _txaSymbols.setLineWrap(true);  _txaSymbols.setBorder(new LineBorder(Color.blue));
        content_pnl.add(cen_pnl, BorderLayout.CENTER);
        JPanel btn_pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));  btn_pnl.setOpaque(false);
        if (!view_only) {
            setTitle("View / Edit Symbols");
            JButton update_btn = new JButton("Update Watchlist");
            update_btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent aev) {
                    _bCancelled = false;
                    dispose();
                }
            });
            btn_pnl.add(update_btn); btn_pnl.add(Box.createHorizontalStrut(50));
            getRootPane().setDefaultButton(update_btn);
        }
        else
            setTitle("Viewing Symbols");
        JButton reformat_btn = new JButton("Re-Format");
        reformat_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //re-layout symbols into one per line, no commas
                _txaSymbols.setText("");
                for (String sym : symbols) {
                    _txaSymbols.append(sym + "\n");
                }
            }
        });
        btn_pnl.add(reformat_btn);
        content_pnl.add(btn_pnl, BorderLayout.SOUTH);
        setContentPane(content_pnl);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent wev) {
                _bCancelled = true;
            }
        });

        //populate
        StringBuilder buf = new StringBuilder();
        for(String str : symbols)
            buf.append(str).append(",");
        _txaSymbols.setText(buf.toString());
        _txaSymbols.setFont(FrameworkConstants.SMALL_FONT_BOLD);
        WidgetUtil.setDialogProperties(this, new Dimension(400, 300), true, MdiMainFrame.getInstance(), WindowConstants.DISPOSE_ON_CLOSE);
    }

    public boolean isCancelled() { return _bCancelled; }

    public ArrayList<String> getSymbols() {
        ArrayList<String> ret = new ArrayList<String>();
        String text = _txaSymbols.getText();
        String[] tokens = text.split(Constants.REGEX_SYMBOL_SPLITTER);
        for (String str : tokens) {
            if (!str.isEmpty())
                ret.add(str.toUpperCase());
        }
        return ret;
    }

    //instance variables
    private JTextArea _txaSymbols = new JTextArea();
    private boolean _bCancelled;
}