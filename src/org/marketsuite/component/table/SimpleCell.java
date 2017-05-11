package org.marketsuite.component.table;

import java.io.Serializable;

/**
 * A data class for storing spreadsheet cell information. It stores data of Object type and implement
   several rendering different "state"s
    1. No content, not editable, dark gray ---- enabled: false,
    2. With content, not editable, dark gray
    3. With content, not editable, interlacing white and light gray
    4. With content, editable (light blue)
    5. With content that has underline clickable link, not editable, interlacing white/light gray

   4 booleans are provided to implement these states.
    enabled     false --> state 1
    showContent false --> state 2
    highlight   true --> state 4
    underline   true --> state 5

    state 3 - enabled = true, active = true, highlight = false, underline = false
 */
public class SimpleCell implements Serializable {
	public SimpleCell(Object o) {
        value = o;
    }

    public String toString() {
        if (value != null)
            return value.toString();
        return "";
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object v) {
        value = v;
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean hl) {
        highlight = hl;
    }

    public boolean isShowContent() {
        return showContent;
    }

    public void setShowContent(boolean f) {
        showContent = f;
    }

    public void setUnderline(boolean f) {
        underline = f;
    }

    public boolean isUnderline() {
        return underline;
    }

    //convinence method
    public void setEnableHighlight(boolean enable, boolean high_light) {
        enabled = enable;
        highlight = high_light;
    }

    //instance variables
    private Object value = "";
    private boolean dirty;
    private boolean enabled = true;//if false, content not shown with "disabled" dark gray
    private boolean showContent = true; //show content with dark gray
    private boolean highlight;//emphasize this cell, true usually indicates editability
    private boolean underline;//looks like link
    private static final long serialVersionUID = 569685638765819030L;
}