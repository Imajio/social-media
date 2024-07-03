package org.bytebound;

import org.java_websocket.WebSocket;
import java.sql.Timestamp;

public interface MessageHandler {
    void handleMessage(String message, WebSocket conn);
    Timestamp dataOfSentMessage(int messageId);
}
