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
        System.out.println("[ConnectionManager] New user conn was added -> " + userId + " <---> " + conn.toString());
        System.out.println("[ConnectionManager] All user connections -> " + userConnections);
    }
    public WebSocket getUserConnection(int userId) {
        return userConnections.get(userId);
    }
    public void removeUserConnection(WebSocket userConn) {
        for (Map.Entry<Integer, WebSocket> entry : userConnections.entrySet()) {
            if (entry.getValue().equals(userConn)) {
                userConnections.remove(entry.getKey());
                System.out.println("[ConnectionManager] User conn removed -> " + entry.getKey());
                break;
            }
        }
    }


}
