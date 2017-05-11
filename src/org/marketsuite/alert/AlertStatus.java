package org.marketsuite.alert;

public enum AlertStatus {
    StopTriggered("Stop Level Triggered"),
    LimitTriggered("Limit Level Triggered"),
//    Cancelled("Level Monitoring Cancelled"),
    Inactive("Level Monitoring Suspended"),
    NotTriggered("Level Not Triggered"),
    ;
    AlertStatus(String disp_str) { displayString = disp_str; }
    private String displayString;
    public String toString() { return displayString; }
}
