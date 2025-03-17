package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private int decisionCount = 0;
    private String currentHeading = "E";
    private int batteryLevel = 0;

    // initializes the drones state with heading and battery information
    @Override
    public void initialize(String s) {
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        currentHeading = info.getString("heading");
        batteryLevel = info.getInt("budget");
        logger.info("The drone is facing {}", currentHeading);
        logger.info("Battery level is {}", batteryLevel);
    }

    // issues a command based on the current decision count:
    // 0: fly, 1: heading (rotate 90 clockwise), 2: fly, then stop
    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();
        if (decisionCount == 0) {
            decision.put("action", "fly");
        } else if (decisionCount == 1) {
            currentHeading = getNextHeading(currentHeading);
            decision.put("action", "heading");
            decision.put("new_heading", currentHeading);
        } else if (decisionCount == 2) {
            decision.put("action", "fly");
        } else {
            decision.put("action", "stop");
        }
        logger.info("Decision {}: {}", decisionCount, decision.toString());
        return decision.toString();
    }

    // processes the drone's response and increments the decision counter
    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        int cost = response.getInt("cost");
        String status = response.getString("status");
        JSONObject extraInfo = response.getJSONObject("extras");
        logger.info("Cost: {}", cost);
        logger.info("Status: {}", status);
        logger.info("Extra: {}", extraInfo);
        decisionCount++;
    }

    // returns the final report
    @Override
    public String deliverFinalReport() {
        return "no creek found";
    }

    // returns the next heading in clockwise order
    private String getNextHeading(String current) {
        switch (current) {
            case "N": return "E";
            case "E": return "S";
            case "S": return "W";
            case "W": return "N";
            default: return "E";
        }
    }
}
