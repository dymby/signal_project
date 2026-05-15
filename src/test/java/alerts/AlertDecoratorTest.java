// src/test/java/alerts/AlertDecoratorTest.java
package alerts;

import com.alerts.Alert;
import com.alerts.BaseAlert;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertDecoratorTest {

    private final Alert base = new BaseAlert("1", "Critical BloodPressure", 1000L);

    @Test
    void testBaseAlert_getPatientId() {
        assertEquals("1", base.getPatientId());
    }

    @Test
    void testBaseAlert_getCondition() {
        assertEquals("Critical BloodPressure", base.getCondition());
    }

    @Test
    void testBaseAlert_getTimestamp() {
        assertEquals(1000L, base.getTimestamp());
    }

    @Test
    void testPriorityDecorator_prependsPriorityToCondition() {
        Alert alert = new PriorityAlertDecorator(base, PriorityAlertDecorator.Priority.HIGH);
        assertEquals("[HIGH] Critical BloodPressure", alert.getCondition());
    }

    @Test
    void testPriorityDecorator_preservesPatientId() {
        Alert alert = new PriorityAlertDecorator(base, PriorityAlertDecorator.Priority.HIGH);
        assertEquals("1", alert.getPatientId());
    }

    @Test
    void testPriorityDecorator_preservesTimestamp() {
        Alert alert = new PriorityAlertDecorator(base, PriorityAlertDecorator.Priority.HIGH);
        assertEquals(1000L, alert.getTimestamp());
    }

    @Test
    void testPriorityDecorator_critical() {
        Alert alert = new PriorityAlertDecorator(base, PriorityAlertDecorator.Priority.CRITICAL);
        assertTrue(alert.getCondition().startsWith("[CRITICAL]"));
    }

    @Test
    void testPriorityDecorator_low() {
        Alert alert = new PriorityAlertDecorator(base, PriorityAlertDecorator.Priority.LOW);
        assertTrue(alert.getCondition().startsWith("[LOW]"));
    }

    @Test
    void testRepeatedDecorator_appendsRepeatInfo() {
        Alert alert = new RepeatedAlertDecorator(base, 3, 5000L);
        assertTrue(alert.getCondition().contains("[REPEATED x3 every 5000ms]"));
    }

    @Test
    void testRepeatedDecorator_preservesOriginalCondition() {
        Alert alert = new RepeatedAlertDecorator(base, 3, 5000L);
        assertTrue(alert.getCondition().startsWith("Critical BloodPressure"));
    }

    @Test
    void testRepeatedDecorator_preservesPatientId() {
        Alert alert = new RepeatedAlertDecorator(base, 3, 5000L);
        assertEquals("1", alert.getPatientId());
    }

    @Test
    void testRepeatedDecorator_preservesTimestamp() {
        Alert alert = new RepeatedAlertDecorator(base, 3, 5000L);
        assertEquals(1000L, alert.getTimestamp());
    }

    @Test
    void testRepeatedDecorator_getRepeatCount() {
        RepeatedAlertDecorator alert = new RepeatedAlertDecorator(base, 5, 1000L);
        assertEquals(5, alert.getRepeatCount());
    }

    @Test
    void testRepeatedDecorator_getIntervalMs() {
        RepeatedAlertDecorator alert = new RepeatedAlertDecorator(base, 3, 2000L);
        assertEquals(2000L, alert.getIntervalMs());
    }

    @Test
    void testStackedDecorators_priorityThenRepeated() {
        Alert alert = new PriorityAlertDecorator(base, PriorityAlertDecorator.Priority.HIGH);
        alert = new RepeatedAlertDecorator(alert, 3, 5000L);
        String condition = alert.getCondition();
        assertTrue(condition.contains("[HIGH]"));
        assertTrue(condition.contains("[REPEATED x3 every 5000ms]"));
    }

    @Test
    void testStackedDecorators_preservesPatientId() {
        Alert alert = new PriorityAlertDecorator(base, PriorityAlertDecorator.Priority.HIGH);
        alert = new RepeatedAlertDecorator(alert, 3, 5000L);
        assertEquals("1", alert.getPatientId());
    }
}