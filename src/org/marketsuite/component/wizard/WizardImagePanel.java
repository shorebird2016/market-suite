package org.marketsuite.component.wizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import javax.swing.JLabel;
import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;

/**
 * The left margin panel of wizard window, there's a default image, but can be replaced
 */
public class WizardImagePanel extends SkinPanel {
    //start_y = y coordinate in pixels of the first label
    //step_number = 1 based to indicate which step of the process
    public WizardImagePanel(String[] step_labels, int start_y, int cur_step) {
        super(LazyIcon.WIZARD_IMAGE, null);
        _nCurrentStep = cur_step;
        Image image = LazyIcon.WIZARD_IMAGE.getImage();
        int img_width = image.getWidth(null);
        setPreferredSize(new Dimension(img_width, image.getHeight(null)));
        int y = start_y;
        _lblSteps = new JLabel[step_labels.length];
        for (int step=0; step<step_labels.length; step++) {
            add(createStepLabel(step, step_labels[step], 5, y, img_width));
            y += 40;
        }
        setCurrentStep(_nCurrentStep);
    }

    //highlight text for current step
    public void setCurrentStep(int step) {
        _lblSteps[_nCurrentStep -1].setForeground(Color.black);//turn last step off
        _nCurrentStep = step;
        _lblSteps[step -1].setForeground(Color.white);//turn on current step
    }

    private JLabel createStepLabel(int step, String text, int x, int y, int width) {
        _lblSteps[step] = new JLabel(text);
        _lblSteps[step].setFont(Constants.FONT_BOLD_15);
        _lblSteps[step].setForeground(Color.black);
        _lblSteps[step].setLocation(x, y);
        _lblSteps[step].setSize(width, 25);
        return _lblSteps[step];
    }

    private int _nCurrentStep = 1;
    private JLabel[] _lblSteps;
    private static final long serialVersionUID = -1L;
}
