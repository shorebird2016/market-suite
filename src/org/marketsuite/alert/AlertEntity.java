package org.marketsuite.alert;

import java.util.Calendar;

//data object storing attributes of alert related information
public class AlertEntity {
    public Calendar getStopTriggerTime() { return stopTriggerTime; }
    public AlertStatus getStatus() { return status; }
    public Calendar getLimitTriggerTime() { return limitTriggerTime; }

    public void setStopTriggerTime(Calendar stopTriggerTime) { this.stopTriggerTime = stopTriggerTime; }
    public void setLimitTriggerTime(Calendar limitTriggerTime) { this.limitTriggerTime = limitTriggerTime; }
    public void setStatus(AlertStatus status) { this.status = status; }

    private String symbol;
    private float stopLevel;//protective stop
    private float limitLevel;//buy limit
    private Calendar stopTriggerTime;
    private Calendar limitTriggerTime;
    private AlertStatus status = AlertStatus.Inactive;
}