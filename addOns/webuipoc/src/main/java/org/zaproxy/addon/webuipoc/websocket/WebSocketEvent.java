package org.zaproxy.addon.webuipoc.websocket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketEvent {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LogManager.getLogger(WebSocketEvent.class);
    private final String type;
    private final JsonNode message;

    public WebSocketEvent(String type, JsonNode message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        try {
            return "{\"type\":\"" + type + "\",\"message\":" + MAPPER.writeValueAsString(message) + "}";
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not serialize WebSocketEvent to JSON", e);
            return null;
        }
    }
}
