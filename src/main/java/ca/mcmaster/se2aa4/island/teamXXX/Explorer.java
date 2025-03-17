package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.ace_design.island.bot.IExplorerRaid;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private int currentX = 1, currentY = 1;
    private String currentHeading = "E";
    private int batteryLevel = 0;
    private int decisionCount = 0;
    private int horizontalLimit = 5;
    private int horizontalMoves = 0;
    private boolean goingEast = true;
    private boolean awaitingScan = true;
    private boolean explorationCompleted = false;
    private String lastAction = "";
    private Map<String, Poi> pois = new HashMap<>();

    private static class Poi {
        String type;
        String id;
        int x, y;
        Poi(String type, String id, int x, int y) {
            this.type = type;
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    /**
     * initializes the exploration with drone configuration
     */
    @Override
    public void initialize(String s) {
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        currentHeading = info.getString("heading");
        batteryLevel = info.getInt("budget");
        logger.info("Initialized with heading {} and battery {}", currentHeading, batteryLevel);
    }

    /**
     * decides the next command based on the exploration state
     */
    @Override
    public String takeDecision() {
        JSONObject decision = new JSONObject();

        if (explorationCompleted) {
            decision.put("action", "stop");
            lastAction = "stop";
            logger.info("Decision: stop");
            return decision.toString();
        }

        if (decisionCount > 0 && decisionCount % 10 == 0 && !awaitingScan && !currentHeading.equals("W")) {
            decision.put("action", "radar");
            decision.put("direction", currentHeading);
            lastAction = "radar";
            logger.info("Decision: radar in direction {}", currentHeading);
            return decision.toString();
        }

        if (awaitingScan) {
            decision.put("action", "scan");
            lastAction = "scan";
            logger.info("Decision: scan at ({}, {})", currentX, currentY);
            return decision.toString();
        }

        if (horizontalMoves < horizontalLimit) {
            decision.put("action", "fly");
            lastAction = "fly";
            if (currentHeading.equals("E")) {
                currentX++;
            } else if (currentHeading.equals("W")) {
                currentX--;
            }
            horizontalMoves++;
            awaitingScan = true;
            logger.info("Decision: fly to ({}, {})", currentX, currentY);
            return decision.toString();
        } else {
            if (!currentHeading.equals("S")) {
                decision.put("action", "heading");
                decision.put("new_heading", "S");
                lastAction = "heading";
                currentHeading = "S";
                logger.info("Decision: change heading to South");
                return decision.toString();
            } else {
                decision.put("action", "fly");
                lastAction = "fly";
                currentY++;
                awaitingScan = true;
                logger.info("Decision: fly vertically to ({}, {})", currentX, currentY);
                horizontalMoves = 0;
                currentHeading = goingEast ? "E" : "W";
                goingEast = !goingEast;
                return decision.toString();
            }
        }
    }

    /**
     * Processes the reslt of the executed command
     */
    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        int cost = response.getInt("cost");
        String status = response.getString("status");
        JSONObject extras = response.getJSONObject("extras");
        batteryLevel -= cost;
        logger.info("Ack: cost {} status {} battery {}", cost, status, batteryLevel);

        if (lastAction.equals("scan")) {
            if (extras.has("biome")) {
                String biome = extras.getString("biome");
                logger.info("Scan biome: {}", biome);
            }
            if (extras.has("poi")) {
                JSONObject poiObj = extras.getJSONObject("poi");
                if (poiObj.has("type") && poiObj.has("id")) {
                    String type = poiObj.getString("type");
                    String id = poiObj.getString("id");
                    if (!pois.containsKey(id)) {
                        pois.put(id, new Poi(type, id, currentX, currentY));
                        logger.info("Discovered POI: {} of type {} at ({}, {})", id, type, currentX, currentY);
                    }
                }
            }
            awaitingScan = false;
        } else if (lastAction.equals("radar")) {
            logger.info("Radar data: {}", extras);
        }
        decisionCount++;

        boolean hasEmergency = false;
        boolean hasCreek = false;
        for (Poi poi : pois.values()) {
            if (poi.type.equalsIgnoreCase("emergency")) {
                hasEmergency = true;
            }
            if (poi.type.equalsIgnoreCase("creek")) {
                hasCreek = true;
            }
        }
        if (hasEmergency && hasCreek) {
            explorationCompleted = true;
        }
    }

    /**
     * Delivers the final report, including the identifier of the closest creek to the emergency site
     */
    @Override
    public String deliverFinalReport() {
        Poi emergency = null;
        for (Poi poi : pois.values()) {
            if (poi.type.equalsIgnoreCase("emergency")) {
                emergency = poi;
                break;
            }
        }
        if (emergency == null) {
            return "No emergency site found";
        }
        Poi closestCreek = null;
        int minDistance = Integer.MAX_VALUE;
        for (Poi poi : pois.values()) {
            if (poi.type.equalsIgnoreCase("creek")) {
                int distance = Math.abs(poi.x - emergency.x) + Math.abs(poi.y - emergency.y);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCreek = poi;
                }
            }
        }
        if (closestCreek == null) {
            return "No creek found";
        }
        return "Nearest creek: " + closestCreek.id;
    }
}
