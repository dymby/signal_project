package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorates an alert with a priority level.
 * Higher priority alerts require more urgent attention.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    private final Priority priority;

    /**
     * @param decoratedAlert the alert to decorate
     * @param priority       the urgency level to assign
     */
    public PriorityAlertDecorator(Alert decoratedAlert, Priority priority) {
        super(decoratedAlert);
        this.priority = priority;
    }

    /**
     * Prepends the priority tag to the condition string.
     * e.g. "[CRITICAL] Critical SystolicPressure"
     */
    @Override
    public String getCondition() {
        return "[" + priority.name() + "] " + decoratedAlert.getCondition();
    }

    public Priority getPriority() { return priority; }
}