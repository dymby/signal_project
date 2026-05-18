package data_management;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientReader;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketClientReaderTest {

    private DataStorage storage;
    private WebSocketClientReader reader;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        reader = new WebSocketClientReader("ws://localhost:8080");
    }

    @Test
    void testParse_validMessage_storedCorrectly() {
        reader.parseAndStore("1,1000,SystolicPressure,126.0", storage);
        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(1, records.size());
    }

    @Test
    void testParse_correctPatientId() {
        reader.parseAndStore("42,1000,SystolicPressure,126.0", storage);
        assertFalse(storage.getRecords(42, 0L, Long.MAX_VALUE).isEmpty());
    }

    @Test
    void testParse_correctTimestamp() {
        reader.parseAndStore("1,5000,SystolicPressure,126.0", storage);
        assertEquals(5000L,
                storage.getRecords(1, 0L, Long.MAX_VALUE).get(0).getTimestamp());
    }

    @Test
    void testParse_correctLabel() {
        reader.parseAndStore("1,1000,SystolicPressure,126.0", storage);
        assertEquals("SystolicPressure",
                storage.getRecords(1, 0L, Long.MAX_VALUE).get(0).getRecordType());
    }

    @Test
    void testParse_correctValue() {
        reader.parseAndStore("1,1000,SystolicPressure,126.0", storage);
        assertEquals(126.0,
                storage.getRecords(1, 0L, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testParse_saturationStripsPercent() {
        reader.parseAndStore("1,1000,Saturation,99.0%", storage);
        assertEquals(99.0,
                storage.getRecords(1, 0L, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testParse_alertTriggered_encodedAs1() {
        reader.parseAndStore("1,1000,Alert,triggered", storage);
        assertEquals(1.0,
                storage.getRecords(1, 0L, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testParse_alertResolved_encodedAs0() {
        reader.parseAndStore("1,1000,Alert,resolved", storage);
        assertEquals(0.0,
                storage.getRecords(1, 0L, Long.MAX_VALUE).get(0).getMeasurementValue());
    }

    @Test
    void testParse_multipleMessages_allStored() {
        reader.parseAndStore("1,1000,SystolicPressure,120.0", storage);
        reader.parseAndStore("1,2000,SystolicPressure,122.0", storage);
        reader.parseAndStore("1,3000,SystolicPressure,125.0", storage);
        assertEquals(3, storage.getRecords(1, 0L, Long.MAX_VALUE).size());
    }

    @Test
    void testParse_multiplePatients_storedSeparately() {
        reader.parseAndStore("1,1000,SystolicPressure,120.0", storage);
        reader.parseAndStore("2,1000,Saturation,97.0", storage);
        assertFalse(storage.getRecords(1, 0L, Long.MAX_VALUE).isEmpty());
        assertFalse(storage.getRecords(2, 0L, Long.MAX_VALUE).isEmpty());
    }

    @Test
    void testParse_ecgValue_parsedCorrectly() {
        reader.parseAndStore("1,1000,ECG,0.32641426767704684", storage);
        assertEquals(0.32641426767704684,
                storage.getRecords(1, 0L, Long.MAX_VALUE).get(0).getMeasurementValue(),
                1e-10);
    }

    @Test
    void testParse_tooFewFields_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                reader.parseAndStore("1,1000,SystolicPressure", storage));
    }

    @Test
    void testParse_tooManyFields_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                reader.parseAndStore("1,1000,SystolicPressure,120.0,extra", storage));
    }

    @Test
    void testParse_invalidPatientId_throwsException() {
        assertThrows(NumberFormatException.class, () ->
                reader.parseAndStore("abc,1000,SystolicPressure,120.0", storage));
    }

    @Test
    void testParse_invalidTimestamp_throwsException() {
        assertThrows(NumberFormatException.class, () ->
                reader.parseAndStore("1,notATimestamp,SystolicPressure,120.0", storage));
    }

    @Test
    void testParse_invalidValue_throwsException() {
        assertThrows(NumberFormatException.class, () ->
                reader.parseAndStore("1,1000,SystolicPressure,notANumber", storage));
    }

    @Test
    void testParse_emptyMessage_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                reader.parseAndStore("", storage));
    }

    @Test
    void testIsConnected_beforeConnect_returnsFalse() {
        assertFalse(reader.isConnected());
    }

    @Test
    void testReadData_invalidUri_neverConnects() {
        WebSocketClientReader badReader =
                new WebSocketClientReader("ws://localhost:9999"); // nothing listening here
        assertFalse(badReader.isConnected());
    }
}