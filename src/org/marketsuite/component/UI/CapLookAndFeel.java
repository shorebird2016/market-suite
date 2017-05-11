package org.marketsuite.component.UI;

import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

/**
 * Special class to set up CAP like look and feel upon application startup.  Use synthetica underneath
 */
public class CapLookAndFeel extends SyntheticaLookAndFeel {
   public CapLookAndFeel() throws ParseException {
      // load synth.xml from custom package
      super("/org/marketsuite/component/UI/synth.xml");
      String[] li = {"Licensee=Motorola Inc",
         "LicenseRegistrationNumber=186121025", "Product=Synthetica",
         "LicenseType=Source Code License", "ExpireDate=--.--.----",
      "MaxVersion=2.999.999"};
      UIManager.put("Synthetica.license.info", li);
      UIManager.put("Synthetica.license.key", "B12D30BA-88E09248-E4F35E49-19585B8A-F2BA7346");

      UIManager.addPropertyChangeListener(new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent ev) {
            // synthetica sets these in its PropertyChangeListener so we need to override them here
            if ("lookAndFeel".equals(ev.getPropertyName())) {
               UIManager.put("ScrollPaneUI",               "org.marketsuite.component.UI.CapScrollPaneUI");
               UIManager.put("MonthViewUI",                "org.marketsuite.component.UI.CapMonthViewUI");
               UIManager.put("HyperlinkUI",                "org.jdesktop.swingx.plaf.basic.BasicHyperlinkUI");
               UIManager.put("BusyLabelUI",                "org.jdesktop.swingx.plaf.basic.BasicBusyLabelUI");
               UIManager.put("DatePickerUI",               "org.jdesktop.swingx.plaf.basic.BasicDatePickerUI");
               UIManager.put("ErrorPaneUI",                "org.jdesktop.swingx.plaf.basic.BasicErrorPaneUI");
               UIManager.put("HeaderUI",                   "org.jdesktop.swingx.plaf.basic.BasicHeaderUI");
               UIManager.put("LoginPaneUI",                "org.jdesktop.swingx.plaf.basic.BasicLoginPaneUI");
               UIManager.put("MultiThumbSliderUI",         "org.jdesktop.swingx.plaf.basic.BasicMultiThumbSliderUI");
               UIManager.put("StatusBarUI",                "org.jdesktop.swingx.plaf.basic.BasicStatusBarUI");
               UIManager.put("swingx/TaskPaneContainerUI", "org.jdesktop.swingx.plaf.basic.BasicTaskPaneContainerUI");
               UIManager.put("swingx/TaskPaneUI",          "org.jdesktop.swingx.plaf.basic.BasicTaskPaneUI");
               UIManager.put("swingx/TipOfTheDayUI",       "org.jdesktop.swingx.plaf.basic.BasicTipOfTheDayUI");
               UIManager.put("TitledPanelUI",              "org.jdesktop.swingx.plaf.basic.BasicTitledPanelUI");
               UIManager.put("ToolTipUI",                  "org.marketsuite.component.UI.MultiLineToolTipUI");
               UIManager.put("TabbedPaneUI",               "org.marketsuite.component.UI.MainTabUI");
               UIManager.put("ToolTip.font", new FontUIResource(Font.SANS_SERIF, Font.BOLD, 12));
               // override synthetica PopupFactory to avoid tooltip flicker problems: Razor 2268
               PopupFactory.setSharedInstance(new PopupFactory());
            }
         }
      });
   }

   // return a unique LAF id
   public String getID() {
      return "CapLookAndFeel";
   }

   // return the LAF name - readable for humans
   public String getName() {
      return "Cap Custom Look and Feel";
   }

   public UIDefaults getDefaults() {
      UIDefaults table = super.getDefaults();
      updateDefaults(table);
      return table;
   }

   private void updateDefaults(UIDefaults table) {
      Object[] def = {
         "JXMonthView.monthDownFileName", LookAndFeel.makeIcon(CapLookAndFeel.class, "images/arrowLeft.png"),
         "JXMonthView.monthUpFileName", LookAndFeel.makeIcon(CapLookAndFeel.class, "images/arrowRight.png"),
         // "JXMonthView.monthDownFileName", LookAndFeel.makeIcon(MonthViewAddon.class, "basic/resources/month-down.png"),
         // "JXMonthView.monthUpFileName", LookAndFeel.makeIcon(MonthViewAddon.class, "basic/resources/month-up.png"),
         "JXMonthView.monthStringBackground", new Color(0, 0, 0, 0), // Transparent to allow CapMonthViewUI to paint a gradient background
         "JXMonthView.background", new Color(230, 230, 230),
         "TabbedPane.tabsOpaque", Boolean.TRUE,
      };
      table.putDefaults(def);
   }
}
