package org.marketsuite.test.ballon.panels;

import net.java.balloontip.BalloonTip;
import org.marketsuite.test.ballon.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * Class adapted from the Sun tutorial on "How to Use Layered Panes".
 * Source: http://java.sun.com/docs/books/tutorial/uiswing/components/layeredpane.html
 */
public class LayersTab extends JPanel {
	private String[] layerStrings = { "Yellow (0)", "Magenta (1)",	"Cyan (2)", "Red (3)", "Green (4)" };
	private Color[] layerColors = { Color.yellow, Color.magenta, Color.cyan, Color.red, Color.green };

	private JLayeredPane layeredPane;
	private JLabel dukeLabel;
	private JComboBox<?> layerList;
	private BalloonTip balloonTip;

	// Adjustments to the balloon's relative position to Duke
	private int xFudge = 40;
	private int yFudge = 57;

	public LayersTab()    {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setOpaque(true); // Content panes must be opaque

		// Create and load the duke icon.
		final ImageIcon icon = new ImageIcon(LayersTab.class.getResource("/org/marketsuite/test/ballon/resource/dukeWaveRed.gif"));

		// Create and set up the layered pane.
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(300, 400));
		layeredPane.setBorder(BorderFactory.createTitledBorder("Move the mouse to make Duke follow:"));

		// Make Duke follow the cursor

		layeredPane.addMouseMotionListener(new MouseMotionAdapter() {
			final int paddingTop = 50;
			public void mouseMoved(MouseEvent e) {
				if (e.getY() > paddingTop) {
					dukeLabel.setLocation(e.getX()-xFudge/2, e.getY()-yFudge/2);
				}
			}
		});

		// This is the origin of the first label added.
		Point origin = new Point(100, 80);

		// This is the offset for computing the origin for the next label.
		int offset = 35;

		// Add several overlapping, colored labels to the layered pane using absolute positioning/sizing.
		for (int i = 0; i < layerStrings.length; i++) {
			JLabel label = createColoredLabel(layerStrings[i],
					layerColors[i], origin);
			layeredPane.add(label, new Integer(i));
			origin.x += offset;
			origin.y += offset;
		}

		// Create and add the Duke label to the layered pane.
		dukeLabel = new JLabel(icon);
		dukeLabel.setBounds(40, 225,
				icon.getIconWidth(),
				icon.getIconHeight());

		layeredPane.add(dukeLabel, new Integer(2), 0);
		balloonTip = new BalloonTip(dukeLabel, new JLabel("Ready for action!"),
				Utils.createBalloonTipStyle(),
				Utils.createBalloonTipPositioner(),
				null);
		balloonTip.setTopLevelContainer(layeredPane);
		balloonTip.setPadding(4);
		layeredPane.setLayer(balloonTip, 2, 0);

		// Add control pane and layered pane to this JPanel.
		add(createControlPanel());
		add(layeredPane);
	}

	// Create and set up a colored label.
	private JLabel createColoredLabel(String text,
			Color color,
			Point origin) {
		JLabel label = new JLabel(text);
		label.setVerticalAlignment(JLabel.TOP);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setOpaque(true);
		label.setBackground(color);
		label.setForeground(Color.black);
		label.setBorder(BorderFactory.createLineBorder(Color.black));
		label.setBounds(origin.x, origin.y, 140, 140);
		return label;
	}

	// Create the control pane for the top of the frame.
	private JPanel createControlPanel() {

		layerList = new JComboBox<Object>(layerStrings);
		layerList.setSelectedIndex(2); //cyan layer
		// Behaviour of the layer selection list
		layerList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layeredPane.setLayer(dukeLabel,
						layerList.getSelectedIndex(),
						0);
				layeredPane.setLayer(balloonTip,
						layerList.getSelectedIndex(),
						0);
			}});

		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());
		controls.add(new JLabel("<html>The pane on which a balloon tip is drawn can be set manually to, for instance, a "
				+ Utils.monospace("JLayeredPane") + ". In this example, you can make a balloon tip switch between different layers of such a "
				+ Utils.monospace("JLayeredPane") + ".</html>"), new GridBagConstraints(0,0,3,1,1.0,0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10,10,10,0), 0, 0));

		controls.add(new JLabel("Choose Duke's layer:"), new GridBagConstraints(0,1,1,1,0.0,1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,10,10,0), 0, 0));
		layerList.setPreferredSize(new Dimension(100,25));
		controls.add(layerList, new GridBagConstraints(1,1,1,1,0.0,1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,10,10,0), 0, 0));

		controls.setBorder(BorderFactory.createTitledBorder(
		"Setting a layer:"));
		return controls;
	}
}
