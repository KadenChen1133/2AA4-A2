package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private boolean hasFlown = false;

    // initializes the drone's state with heading and battery information
    @Override
    public void initialize(String s) {
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        String direction = info.getString("heading");
        Integer batteryLevel = info.getInt("budget");
        logger.info("The drone is facing {}", direction);
        logger.info("Battery level is {}", batteryLevel);
    }

    // issues a "fly" command on the first call, then "stop" on subsequent calls
    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();
        if (!hasFlown) {
            decision.put("action", "fly");
            hasFlown = true;
        } else {
            decision.put("action", "stop");
        }
        return decision.toString();
    }

    // processes and logs the response from the drone
    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        Integer cost = response.getInt("cost");
        String status = response.getString("status");
        JSONObject extraInfo = response.getJSONObject("extras");
        logger.info("Cost: {}", cost);
        logger.info("Status: {}", status);
        logger.info("Extra: {}", extraInfo);
    }

    // returns final report
    @Override
    public String deliverFinalReport() {
        return "no creek found";
    }
}
