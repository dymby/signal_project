package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorates an alert to indicate it has been triggered repeatedly.
 * Adds a repeat count and interval to the alert condition string.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private final int repeatCount;
    private final long intervalMs;

    /**
     * @param decoratedAlert the alert to decorate
     * @param repeatCount    how many times the condition has triggered
     * @param intervalMs     the interval between checks in milliseconds
     */
    public RepeatedAlertDecorator(Alert decoratedAlert, int repeatCount, long intervalMs) {
        super(decoratedAlert);
        this.repeatCount = repeatCount;
        this.intervalMs  = intervalMs;
    }

    /**
     * Appends repeat information to the condition string.
     * e.g. "Critical SystolicPressure [REPEATED x3 every 5000ms]"
     */
    @Override
    public String getCondition() {
        return decoratedAlert.getCondition()
                + " [REPEATED x" + repeatCount
                + " every " + intervalMs + "ms]";
    }

    public int getRepeatCount()  { return repeatCount; }
    public long getIntervalMs()  { return intervalMs; }
}