package org.bytebound;

//import org.java_websocket.WebSocket;

import org.java_websocket.WebSocket;

import java.sql.Timestamp;

public interface MessageHandler {
    void handleMessage(String message);
    Timestamp dataOfSentMessage(int messageId);

//    int insertMessageIntoDatabase(String nickOfSender, String nickOfReceiver, String messageText);
//    int findNickId(String nick);
    void sendMessageToReceiver(int receiverId, String message, WebSocket conn);
}
