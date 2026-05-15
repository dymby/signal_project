package com.alerts.decorators;

import com.alerts.Alert;

public abstract class AlertDecorator implements Alert {

    // wraps another alert — could be a BaseAlert or another decorator
    protected final Alert decoratedAlert;

    public AlertDecorator(Alert decoratedAlert) {
        this.decoratedAlert = decoratedAlert;
    }

    // default: delegate to the wrapped alert
    @Override
    public String getPatientId() { return decoratedAlert.getPatientId(); }

    @Override
    public String getCondition() { return decoratedAlert.getCondition(); }

    @Override
    public long getTimestamp() { return decoratedAlert.getTimestamp(); }
}