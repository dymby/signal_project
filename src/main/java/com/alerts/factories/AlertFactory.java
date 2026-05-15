package com.alerts.factories;

import com.alerts.BaseAlert;

public abstract class AlertFactory {
    public abstract BaseAlert createAlert(String patientId, String condition, long timestamp);
}
