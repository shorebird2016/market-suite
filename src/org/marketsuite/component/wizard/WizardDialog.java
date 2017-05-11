package org.marketsuite.component.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import org.marketsuite.component.Constants;
import org.marketsuite.component.panel.SkinPanel;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.panel.SkinPanel;

public abstract class WizardDialog extends JDialog  implements PropertyChangeListener, ActionListener {
    //CTOR:
    public WizardDialog(JFrame owner, String title) {
        super(owner, title, true);
        _Model.addPropertyChangeListener(this);

        //content pane
        SkinPanel content = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());

        //center - card panel
        _pnlCard = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new CardLayout());
        _pnlCard.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        content.add(_pnlCard, BorderLayout.CENTER);

        //south - row of buttons
        SkinPanel btn_pnl = new SkinPanel(LazyIcon.BACKGROUND_CONTENT, new BorderLayout());
        btn_pnl.add(new JSeparator(), BorderLayout.NORTH);//a line to the north
        Box btn_box = new Box(BoxLayout.X_AXIS);//holds buttons
        btn_box.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        //  Create the buttons with a separator above them, then place them
        //  on the east side of the panel with a small amount of space between
        //  the back and the next button, and a larger amount of space between
        //  the next button and the cancel button.
        btn_box.add(_btnBack);
        _btnBack.setIcon(LazyIcon.BACK_ICON);
        _btnBack.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
        _btnBack.addActionListener(this);
        btn_box.add(Box.createHorizontalStrut(10));
        btn_box.add(_btnNext);
        _btnNext.setIcon(LazyIcon.NEXT_ICON);
        _btnNext.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        _btnNext.addActionListener(this);
        btn_box.add(Box.createHorizontalStrut(30));
        btn_box.add(_btnCancel);
        _btnCancel.setIcon(LazyIcon.CANCEL_ICON);
        _btnCancel.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);
        _btnCancel.addActionListener(this);
        btn_pnl.add(btn_box, BorderLayout.EAST);
        content.add(btn_pnl, BorderLayout.SOUTH);
        setContentPane(content);
//        WidgetUtil.setDialogProperties(this, new Dimension(450,250), true, owner, WindowConstants.DISPOSE_ON_CLOSE);
        //todo: replace this with widgetutil....
        setSize(new Dimension(650, 450));
        setResizable(true);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * Method used to listen for property change events from the model and update the
     * dialog's graphical components as necessary.
     * @param evt PropertyChangeEvent passed from the model to signal that one of its properties has changed value.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WizardModel.CURRENT_PANEL_DESCRIPTOR_PROPERTY))
            resetButtonsToPanelRules();

        else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_TEXT_PROPERTY))
            _btnNext.setText(evt.getNewValue().toString());

        else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_TEXT_PROPERTY))
            _btnBack.setText(evt.getNewValue().toString());

        else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_TEXT_PROPERTY))
            _btnCancel.setText(evt.getNewValue().toString());

        else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_ENABLED_PROPERTY))
            _btnNext.setEnabled(((Boolean) evt.getNewValue()).booleanValue());

        else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ENABLED_PROPERTY))
            _btnBack.setEnabled(((Boolean) evt.getNewValue()).booleanValue());

        else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ENABLED_PROPERTY))
            _btnCancel.setEnabled(((Boolean) evt.getNewValue()).booleanValue());

        else if (evt.getPropertyName().equals(WizardModel.NEXT_FINISH_BUTTON_ICON_PROPERTY))
            _btnNext.setIcon((Icon) evt.getNewValue());

        else if (evt.getPropertyName().equals(WizardModel.BACK_BUTTON_ICON_PROPERTY))
            _btnBack.setIcon((Icon) evt.getNewValue());

        else if (evt.getPropertyName().equals(WizardModel.CANCEL_BUTTON_ICON_PROPERTY))
            _btnCancel.setIcon((Icon) evt.getNewValue());

    }

    /**
     * Calling method for the action listener interface. This class listens for actions
     * performed by the buttons in the Wizard class, and calls methods below to determine
     * the correct course of action.
     *
     * @param evt The ActionEvent that occurred.
     */
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand().equals(CANCEL_BUTTON_ACTION_COMMAND))
            dispose();
        else if (evt.getActionCommand().equals(BACK_BUTTON_ACTION_COMMAND))
            clickBackButton();
        else if (evt.getActionCommand().equals(NEXT_BUTTON_ACTION_COMMAND))
            clickNextButton();
    }

    //public methods
    /**
     * Add a Component as a panel for the wizard dialog by registering its
     * WizardPanelDescriptor object. Each panel is identified by a unique Object-based
     * identifier (often a String), which can be used by the setCurrentPanel()
     * method to display the panel at runtime.
     *
     * @param id    An Object-based identifier used to identify the WizardPanelDescriptor object.
     * @param descriptor The WizardPanelDescriptor object which contains helpful information about the panel.
     */
    public void registerWizardPanel(Object id, WizardPanelDescriptor descriptor) {
        //  Add the incoming descriptor to our JPanel display that is managed by
        //  the CardLayout layout manager.
        _pnlCard.add(descriptor.getPanelComponent(), id);

        //  Set a callback to the current wizard.
        descriptor.setWizard(this);

        //  Place a reference to it in the model.
        _Model.registerPanel(id, descriptor);
    }

    /**
     * Displays the panel identified by the object passed in. This is the same Object-based
     * identified used when registering the panel.
     * @param id The Object-based identifier of the panel to be displayed.
     */
    public void setCurrentPanel(Object id) {
        //  Get the hashtable reference to the panel that should
        //  be displayed. If the identifier passed in is null, then close the dialog.
        if (id == null) {
//            close(ERROR_RETURN_CODE);
            throw new IllegalArgumentException("WizardDialog.setCurrentPanel():Current Panel Should NOT Be NULL....");
        }
        WizardPanelDescriptor oldPanelDescriptor = _Model.getCurrentPanelDescriptor();
        if (oldPanelDescriptor != null)
            oldPanelDescriptor.aboutToHidePanel();
        _Model.setCurrentPanel(id);
        _Model.getCurrentPanelDescriptor().aboutToDisplayPanel();
        //Show the panel in the dialog.
        ((CardLayout)_pnlCard.getLayout()).show(_pnlCard, id.toString());
        _Model.getCurrentPanelDescriptor().displayingPanel();
    }

    public void setNextFinishButtonEnabled(boolean newValue) {
        _Model.setNextFinishButtonEnabled(newValue);
    }

    WizardModel getModel() {
        return _Model;
    }

    //private methods
    private void resetButtonsToPanelRules() {
        //  Reset the buttons to support the original panel rules,
        //  including whether the next or back buttons are enabled or
        //  disabled, or if the panel is finishable.
        WizardPanelDescriptor descriptor = _Model.getCurrentPanelDescriptor();
        _Model.setCancelButtonText(Constants.COMPONENT_BUNDLE.getString("btn_cancel"));
        _Model.setCancelButtonIcon(LazyIcon.CANCEL_ICON);

        //  If the panel in question has another panel behind it, enable
        //  the back button. Otherwise, disable it.
        _Model.setBackButtonText(Constants.COMPONENT_BUNDLE.getString("btn_back"));
        _Model.setBackButtonIcon(LazyIcon.BACK_ICON);
        if (descriptor.getBackPanelDescriptor() != null)
            _Model.setBackButtonEnabled(Boolean.TRUE);
        else
            _Model.setBackButtonEnabled(Boolean.FALSE);
        //  If the panel in question has one or more panels in front of it,
        //  enable the next button. Otherwise, disable it.

        if (descriptor.getNextPanelDescriptor() != null)
            _Model.setNextFinishButtonEnabled(Boolean.TRUE);
        else
            _Model.setNextFinishButtonEnabled(Boolean.FALSE);
        //  If the panel in question is the last panel in the series, change
        //  the Next button to Finish. Otherwise, set the text back to Next.

        if (descriptor.getNextPanelDescriptor().equals(WizardPanelDescriptor.FINISH_ID)) {
            _Model.setNextFinishButtonText(Constants.COMPONENT_BUNDLE.getString("btn_finish"));
            _Model.setNextFinishButtonIcon(LazyIcon.FINISH_ICON);
        } else {
            _Model.setNextFinishButtonText(Constants.COMPONENT_BUNDLE.getString("btn_next"));
            _Model.setNextFinishButtonIcon(LazyIcon.NEXT_ICON);
        }
    }

    private void clickBackButton() {
        WizardPanelDescriptor descriptor = _Model.getCurrentPanelDescriptor();
        //  Get the descriptor that the current panel identifies as the previous
        //  panel, and display it.
        Object backPanelDescriptor = descriptor.getBackPanelDescriptor();
        setCurrentPanel(backPanelDescriptor);
    }

    private void clickNextButton() {
        WizardPanelDescriptor descriptor = _Model.getCurrentPanelDescriptor();
        //  If it is a finishable panel, close down the dialog. Otherwise,
        //  get the ID that the current panel identifies as the next panel,
        //  and display it.
        Object nextPanelDescriptor = descriptor.getNextPanelDescriptor();
        if (nextPanelDescriptor.equals(WizardPanelDescriptor.FINISH_ID))
            dispose();
        else
            setCurrentPanel(nextPanelDescriptor);
    }

    //instance variables
    private WizardModel _Model = new WizardModel();
    private JPanel _pnlCard;
    private JButton _btnBack = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_back"));
    private JButton _btnNext = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_next"));
    private JButton _btnCancel = new JButton(Constants.COMPONENT_BUNDLE.getString("btn_cancel"));

    //literals
    private static final String NEXT_BUTTON_ACTION_COMMAND = "NextButtonActionCommand";
    private static final String BACK_BUTTON_ACTION_COMMAND = "BackButtonActionCommand";
    public static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelButtonActionCommand";
    private static final long serialVersionUID = -1L;
}
