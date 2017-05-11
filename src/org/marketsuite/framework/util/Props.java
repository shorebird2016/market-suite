package org.marketsuite.framework.util;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

/**
 * Example trigger calls:
 * Props.AlarmDeviceFilterChanged.setValue(oldValue, newValue);
 * Props.AlarmDeviceFilterChanged.setValue(newValue);
 * Props.AlarmDeviceFilterChanged.setChanged();
 * <p/>
 * Example PropertyChangeEvent handler:
 * Props.addPropertyChangeListener(Props.AlarmDeviceFilterChanged,new PropertyChangeListener() {
 * public void propertyChange(PropertyChangeEvent ev) {
 * Props prop = (Props)ev.getSource();
 * Object oldValue = ev.getOldValue();
 * Object newValue = ev.getNewValue();
 * }
 * }
 */
public enum Props {
    ShowApp,            //make app visible, move to front
    SymbolSelection,//symbol selection changed
    GroupChange,//group changed from watch list manager(added/deleted)
    WatchListsChange,//one or more watch list changed
    WatchListSelected,//new watch list selected in watch list manager
    AddSymbols,//starts data manager to add more symbols
    TemplateChange,
    MarketInfoChange,//for tracker to notify chart frame
    IndustryChange,//market view --> watch list frame
    SymbolRemoved,//big picture view table --> percent chart
    PlotThumbnails,//scanner --> thumbnail frame
    StopChanged,//risk manager internal, indicating stop level changed
    CashChanged,//risk manager internal, cash amount changed
    ScanComplete,//scanner | query --> table
    Log,//logging frame
    CandleSignal,//from selector to candle windows

    //market view related
    RestoreAllSymbols,//PerformancePanel table --> RelativePerformancePanel: plot
    TimeFrameChanged, //PerformancePanel table --> RelativePerformancePanel: plot / highlight hyperlink
    PlotWatchlist,//PerformancePanel table --> RelativePerformancePanel: plot new watch list
    ;

    //CTOR
    Props() {
        //
    }

    Props(Object initialValue) {
        value = initialValue;
    }

    //----- public methods -----
    // delegates for PropertyChangeSupport
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public static void addPropertyChangeListener(Props prop, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(prop.name(), listener);
    }

    public static void addWeakPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(new WeakListener(listener));
    }

    public static void addWeakPropertyChangeListener(Props prop, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(prop.name(), new WeakListener(listener, prop));
    }

    public static void firePropertyChange(Props prop, Object oldValue, Object newValue) {
        pcs.firePropertyChange(new PropertyChangeEvent(prop, prop.name(), oldValue, newValue));
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(WeakListener listener) {
        if (listener.prop == null)
            pcs.removePropertyChangeListener(listener);
        else
            pcs.removePropertyChangeListener(listener.prop.name(), listener);
    }

    public Object getValue() {
        return value;
    }

    // call listeners with previous and new values
    public void setValue(Object val) {
        Props.firePropertyChange(this, value, value = val);
    }

    // call listeners with old and new values
    public void setValue(Object oldValue, Object newValue) {
        Props.firePropertyChange(this, oldValue, value = newValue);
    }

    // call listeners without setting a value
    public void setChanged() {
        Props.firePropertyChange(this, null, null);
    }

    // reset to initial state
    public static void init() {
        pcs = new SwingPropertyChangeSupport(new Object(), true);
        // remove saved references
        for (Props p : Props.values()) {
            p.value = null;
        }
    }

    //----- inner classes -----
    // weak reference wrapper to avoid memory leaks
    private static class WeakListener implements PropertyChangeListener {
        WeakReference ref;
        Props prop;

        public WeakListener(PropertyChangeListener listener, Props _prop) {
            ref = new WeakReference(listener);
            prop = _prop;
        }

        public WeakListener(PropertyChangeListener listener) {
            ref = new WeakReference(listener);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener listener = (PropertyChangeListener) ref.get();
            if (listener == null)
                Props.removePropertyChangeListener(this);
            else
                listener.propertyChange(evt);
        }
    }

    //----- variables -----
    // fire property change events only on EDT
    private static SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(new Object(), true);
    private Object value;
}
