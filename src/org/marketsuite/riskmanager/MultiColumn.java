package org.marketsuite.riskmanager;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MultiColumn {
    public MultiColumn() { //create the frame and JList JPanel
        jf = new aFrame();
//create element List array
        addElements();
//set list for JList
        list.setListData(columnData);
//create Renderer and dislpay
        list.setCellRenderer(new MyCellRenderer());
    }

    public void addElements() { //first number is number of rows, second is number of columns
        columnData = new String[4][3];
//static setting of String[][]
        columnData[0][0] = "William";
        columnData[0][1] = "A";
        columnData[0][2] = "Wilson";
//dynamic setting of String[][]
        for (int i = 1; i < 4; i++) {
            columnData[i][0] = "William";
            columnData[i][1] = String.valueOf(i * 13);
            Calendar c = new GregorianCalendar();
            columnData[i][2] = ((Date) c.getTime()).toString();
        }
    }

    public class aFrame extends JFrame {
        public aFrame() {
            super("Multi-Column JList Example");
            getContentPane().add(new PanelBuilder());

// display rules setResizable(true);
            setLocation(250, 50);
            setBackground(Color.lightGray);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(650, 400);
            setVisible(true);
        }

        private class PanelBuilder extends JPanel {
            public PanelBuilder() {
                GridBagLayout layout = new GridBagLayout();
                GridBagConstraints layoutConstraints = new GridBagConstraints();
                setLayout(layout);

                scroll = new JScrollPane();
                list = new JList();
                layoutConstraints.gridx = 0;
                layoutConstraints.gridy = 0;
                layoutConstraints.gridwidth = 1;
                layoutConstraints.gridheight = 1;
                layoutConstraints.fill = GridBagConstraints.BOTH;
                layoutConstraints.insets = new Insets(1, 1, 1, 1);
                layoutConstraints.anchor = GridBagConstraints.CENTER;
                layoutConstraints.weightx = 1.0;
                layoutConstraints.weighty = 1.0;
                scroll = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                layout.setConstraints(scroll, layoutConstraints);
                add(scroll);
            }
        }

    }

    private static class MyCellRenderer extends JPanel implements ListCellRenderer {
        JLabel left, middle, right;

        MyCellRenderer() {
            setLayout(new GridLayout(1, 3));
            left = new JLabel();
            middle = new JLabel();
            right = new JLabel();
            left.setOpaque(true);
            middle.setOpaque(true);
            right.setOpaque(true);
            add(left);
            add(middle);
            add(right);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String leftData = ((String[]) value)[0];
            String middleData = ((String[]) value)[1];
            String rightData = ((String[]) value)[2];
            left.setText(leftData);
            middle.setText(middleData);
            right.setText(rightData);
            if (isSelected) {
                left.setBackground(list.getSelectionBackground());
                left.setForeground(list.getSelectionForeground());
                middle.setBackground(list.getSelectionBackground());
                middle.setForeground(list.getSelectionForeground());
                right.setBackground(list.getSelectionBackground());
                right.setForeground(list.getSelectionForeground());
            } else {
                left.setBackground(list.getBackground());
                left.setForeground(list.getForeground());
                middle.setBackground(list.getBackground());
                middle.setForeground(list.getForeground());
                right.setBackground(list.getBackground());
                right.setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            return this;
        }
    }

    private JFrame jf;
    private JScrollPane scroll;
    private JList list;
    private String[][] columnData;

    public static void main(String args[]){ MultiColumn jf = new MultiColumn();
    }
}