package org.marketsuite.test.ballon.layer;
import java.awt.*;
import java.text.*;

import javax.swing.*;
import javax.swing.plaf.LayerUI;

public class FieldValidator extends JPanel {
  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createUI();
      }
    });
  }

  public static void createUI() {
    JFrame f = new JFrame ("FieldValidator");

    JComponent content = createContent();

    f.add (content);

    f.pack();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setLocationRelativeTo (null);
    f.setVisible (true);
  }

  private static JComponent createContent() {
    Dimension labelSize = new Dimension(80, 20);

    Box box = Box.createVerticalBox();

    // A single LayerUI for all the fields.
    LayerUI<JFormattedTextField> layerUI = new ValidationLayerUI();

    // Number field.
    JLabel numberLabel = new JLabel("Number:");
    numberLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    numberLabel.setPreferredSize(labelSize);

    NumberFormat numberFormat = NumberFormat.getInstance();
    JFormattedTextField numberField = new JFormattedTextField(numberFormat);
    numberField.setColumns(16);
    numberField.setFocusLostBehavior(JFormattedTextField.PERSIST);
    numberField.setValue(42);

    JPanel numberPanel = new JPanel();
    numberPanel.add(numberLabel);
    numberPanel.add(new JLayer<JFormattedTextField>(numberField, layerUI));

    // Date field.
    JLabel dateLabel = new JLabel("Date:");
    dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    dateLabel.setPreferredSize(labelSize);

    DateFormat dateFormat = DateFormat.getDateInstance();
    JFormattedTextField dateField = new JFormattedTextField(dateFormat);
    dateField.setColumns(16);
    dateField.setFocusLostBehavior(JFormattedTextField.PERSIST);
    dateField.setValue(new java.util.Date());

    JPanel datePanel = new JPanel();
    datePanel.add(dateLabel);
    datePanel.add(new JLayer<JFormattedTextField>(dateField, layerUI));

    // Time field.
    JLabel timeLabel = new JLabel("Time:");
    timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    timeLabel.setPreferredSize(labelSize);

    DateFormat timeFormat = DateFormat.getTimeInstance();
    JFormattedTextField timeField = new JFormattedTextField(timeFormat);
    timeField.setColumns(16);
    timeField.setFocusLostBehavior(JFormattedTextField.PERSIST);
    timeField.setValue(new java.util.Date());

    JPanel timePanel = new JPanel();
    timePanel.add(timeLabel);
    timePanel.add(new JLayer<JFormattedTextField>(timeField, layerUI));

    // Put them all in the box.
    box.add(Box.createGlue());
    box.add(numberPanel);
    box.add(Box.createGlue());
    box.add(datePanel);
    box.add(Box.createGlue());
    box.add(timePanel);

    return box;
  }
}

class ValidationLayerUI
    extends LayerUI<JFormattedTextField> {
  @Override
  public void paint (Graphics g, JComponent c) {
    super.paint (g, c);

    JLayer jlayer = (JLayer)c;
    JFormattedTextField ftf = (JFormattedTextField)jlayer.getView();
    if (!ftf.isEditValid()) {
      Graphics2D g2 = (Graphics2D)g.create();

      // Paint the red X.
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      int w = c.getWidth();
      int h = c.getHeight();
      int s = 8;
      int pad = 4;
      int x = w - pad - s;
      int y = (h - s) / 2;
      g2.setPaint(Color.red);
      g2.fillRect(x, y, s + 1, s + 1);
      g2.setPaint(Color.white);
      g2.drawLine(x, y, x + s, y + s);
      g2.drawLine(x, y + s, x + s, y);

      g2.dispose();
    }
  }
}
