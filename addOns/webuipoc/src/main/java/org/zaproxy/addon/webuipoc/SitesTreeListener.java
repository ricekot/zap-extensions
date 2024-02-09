package org.zaproxy.addon.webuipoc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.parosproxy.paros.model.SiteMapEventPublisher;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.addon.webuipoc.websocket.WebSocketEvent;
import org.zaproxy.addon.webuipoc.websocket.WebSocketServer;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;

public class SitesTreeListener implements EventConsumer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private TestProxyServer server;

    public SitesTreeListener(TestProxyServer server) {
        this.server = server;
    }

    @Override
    public void eventReceived(Event event) {
        if (SiteMapEventPublisher.SITE_NODE_ADDED_EVENT.equals(event.getEventType())) {
            server.getWebSocketServer().sendMessage(siteNodeToWebSocketEvent(event.getTarget().getStartNode()));
        }
        // TODO handle remove events
    }

    private WebSocketEvent siteNodeToWebSocketEvent(SiteNode node) {
        ObjectNode message = null;
        while(node != node.getRoot()) {
            ObjectNode parentNode = MAPPER.createObjectNode();
            parentNode.put("id", node.getHierarchicNodeName(false));
            parentNode.put("name", node.getNodeName());
            if (message != null) {
                parentNode.putArray("children").add(message);
            }
            message = parentNode;
            node = node.getParent();
        }
        return new WebSocketEvent(SiteMapEventPublisher.SITE_NODE_ADDED_EVENT, message);
    }
}
