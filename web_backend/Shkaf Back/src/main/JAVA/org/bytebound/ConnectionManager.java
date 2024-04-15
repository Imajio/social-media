package org.bytebound;

import java.util.HashMap;
import java.util.Map;
import org.java_websocket.WebSocket;
public class ConnectionManager {
    private Map<Integer, WebSocket> userConnections;

    public ConnectionManager() {
        userConnections = new HashMap<>();
    }

    // Method for adding new connection
    public void addUserConnection(int userId, WebSocket conn) {
        userConnections.put(userId, conn);
    }

    // Method for taking userConnectiong depended on its ID
    public WebSocket getUserConnection(int userId) {
        return userConnections.get(userId);
    }
    // Method fo deleting connection
    public void removeUserConnection(int userId) {
        userConnections.remove(userId);
    }

}
