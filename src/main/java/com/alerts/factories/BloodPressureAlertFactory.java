package com.alerts.factories;

import com.alerts.BaseAlert;

public class BloodPressureAlertFactory extends AlertFactory{

    @Override
    public BaseAlert createAlert(String patientId, String condition, long timestamp) {
        return new BaseAlert(patientId, condition, timestamp);
    }
}
