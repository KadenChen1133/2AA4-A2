package ca.mcmaster.se2aa4.island.teamXXX;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import java.lang.reflect.Field;

public class ExplorerTest {

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    // Test: Verify that initialize correctly sets the heading and battery level.
    @Test
    public void testInitializeSetsHeadingAndBattery() throws Exception {
        Explorer explorer = new Explorer();
        String initJson = "{\"heading\":\"N\",\"budget\":100}";
        explorer.initialize(initJson);
        String currentHeading = (String) getPrivateField(explorer, "currentHeading");
        int batteryLevel = (Integer) getPrivateField(explorer, "batteryLevel");
        assertEquals("N", currentHeading);
        assertEquals(100, batteryLevel);
    }
    
    // Test: Verify that takeDecision returns a "scan" action when awaitingScan is true.
    @Test
    public void testTakeDecisionReturnsScanWhenAwaitingScan() {
        Explorer explorer = new Explorer();
        String decision = explorer.takeDecision();
        JSONObject jsonDecision = new JSONObject(decision);
        assertEquals("scan", jsonDecision.getString("action"));
    }
    
    // Test: After acknowledging a scan response, takeDecision should return a "fly" action.
    @Test
    public void testTakeDecisionReturnsFlyAfterScanAcknowledgement() {
        Explorer explorer = new Explorer();
        explorer.takeDecision();
        String response = "{\"cost\":2,\"status\":\"OK\",\"extras\":{\"biome\":\"forest\"}}";
        explorer.acknowledgeResults(response);
        String decision = explorer.takeDecision();
        JSONObject jsonDecision = new JSONObject(decision);
        assertEquals("fly", jsonDecision.getString("action"));
    }
    
    // Test: Verify that acknowledgeResults decreases the battery level by the cost.
    @Test
    public void testAcknowledgeResultsDecreasesBattery() throws Exception {
        Explorer explorer = new Explorer();
        String initJson = "{\"heading\":\"E\",\"budget\":50}";
        explorer.initialize(initJson);
        int initialBattery = (Integer) getPrivateField(explorer, "batteryLevel");
        String response = "{\"cost\":5,\"status\":\"OK\",\"extras\":{}}";
        explorer.acknowledgeResults(response);
        int newBattery = (Integer) getPrivateField(explorer, "batteryLevel");
        assertEquals(initialBattery - 5, newBattery);
    }
    
    // Test: When no emergency POI is discovered, deliverFinalReport should return "No emergency site found".
    @Test
    public void testFinalReportNoEmergency() {
        Explorer explorer = new Explorer();
        String report = explorer.deliverFinalReport();
        assertEquals("No emergency site found", report);
    }
    
    // Test: When an emergency POI is discovered without a creek, deliverFinalReport should return "No creek found".
    @Test
    public void testFinalReportWithEmergencyNoCreek() throws Exception {
        Explorer explorer = new Explorer();
        String response = "{\"cost\":1,\"status\":\"OK\",\"extras\":{\"poi\":{\"type\":\"emergency\",\"id\":\"E1\"}}}";
        setPrivateField(explorer, "lastAction", "scan");
        explorer.acknowledgeResults(response);
        String report = explorer.deliverFinalReport();
        assertEquals("No creek found", report);
    }
    
    // Test: When both emergency and creek POIs are discovered, deliverFinalReport should return the nearest creek.
    @Test
    public void testFinalReportWithEmergencyAndCreek() throws Exception {
        Explorer explorer = new Explorer();
        String responseEmergency = "{\"cost\":1,\"status\":\"OK\",\"extras\":{\"poi\":{\"type\":\"emergency\",\"id\":\"E1\"}}}";
        setPrivateField(explorer, "lastAction", "scan");
        explorer.acknowledgeResults(responseEmergency);
        String responseCreek = "{\"cost\":1,\"status\":\"OK\",\"extras\":{\"poi\":{\"type\":\"creek\",\"id\":\"C1\"}}}";
        setPrivateField(explorer, "lastAction", "scan");
        explorer.acknowledgeResults(responseCreek);
        String report = explorer.deliverFinalReport();
        assertEquals("Nearest creek: C1", report);
    }
    
    // Test: Verify that decisionCount increments after acknowledging a response.
    @Test
    public void testDecisionCountIncrementsAfterAcknowledge() throws Exception {
        Explorer explorer = new Explorer();
        int initialDecisionCount = (Integer) getPrivateField(explorer, "decisionCount");
        String response = "{\"cost\":1,\"status\":\"OK\",\"extras\":{}}";
        explorer.acknowledgeResults(response);
        int newDecisionCount = (Integer) getPrivateField(explorer, "decisionCount");
        assertEquals(initialDecisionCount + 1, newDecisionCount);
    }
    
    // Test: Simulate that decisionCount is a multiple of 10 and conditions are met to trigger a "radar" action.
    @Test
    public void testRadarDecisionAtMultipleOfTen() throws Exception {
        Explorer explorer = new Explorer();
        setPrivateField(explorer, "decisionCount", 10);
        setPrivateField(explorer, "awaitingScan", false);
        setPrivateField(explorer, "currentHeading", "E");
        String decision = explorer.takeDecision();
        JSONObject jsonDecision = new JSONObject(decision);
        assertEquals("radar", jsonDecision.getString("action"));
    }
    
    // Test: When horizontalMoves reaches horizontalLimit, the next decision should trigger a heading change.
    @Test
    public void testHorizontalMovementResetsAfterLimit() throws Exception {
        Explorer explorer = new Explorer();
        setPrivateField(explorer, "horizontalMoves", 5);
        setPrivateField(explorer, "currentHeading", "E");
        setPrivateField(explorer, "awaitingScan", false);
        String decision = explorer.takeDecision();
        JSONObject jsonDecision = new JSONObject(decision);
        assertEquals("heading", jsonDecision.getString("action"));
        assertEquals("S", jsonDecision.getString("new_heading"));
    }
}
