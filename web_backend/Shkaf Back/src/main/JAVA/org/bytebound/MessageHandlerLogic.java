package org.bytebound;

import org.java_websocket.WebSocket;

import java.sql.*;

public class MessageHandlerLogic implements MessageHandler {
    @Override
    public void handleMessage(String message) {
        System.out.println("[MessageHandlerLogic] handleMessage -> " + message);
        String[] splitedDataOfBody = message.split(",");

        String nickOfSender = splitedDataOfBody[0].substring(1);
        String nickOfReceiver = splitedDataOfBody[1];
        String messageText = splitedDataOfBody[2];
        messageText = messageText.substring(0, messageText.length() - 1);

        System.out.println("[MessageHandlerLogic]\n" + nickOfSender + "\n" + nickOfReceiver + "\n" + messageText + "\n");

        Timestamp timestamp = null;
        ConnectionManager connectionManager = new ConnectionManager();
        WebSocket webSocket = null;

        int messageIdAfterAddingToDataBase = insertMessageIntoDatabase(nickOfSender, nickOfReceiver, messageText);
        if (messageIdAfterAddingToDataBase != -1) {
            timestamp = dataOfSentMessage(messageIdAfterAddingToDataBase);
            System.out.println("[MessageHandlerLogic] Time of sent message -> " + timestamp);
            int idOfReceiver = findNickId(nickOfReceiver);
            webSocket = connectionManager.getUserConnection(idOfReceiver);
            webSocket.send(messageText);
        } else {
            System.err.println("[MessageHandlerLogic] Error with adding data to database!");
        }
    }

    private void sendMessageToReceiver(String message, WebSocket conn) {

    }

    @Override
    public Timestamp dataOfSentMessage(int messageId) {
        Timestamp answer = null;
        String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
        String dbUsername = "root";
        String dbPassword = "";
        String sqlQuery = "SELECT Timestamp FROM messages WHERE message_id = ?;";

        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        ) {

            preparedStatement.setInt(1, messageId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                answer = resultSet.getTimestamp("Timestamp");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return answer;
    }

    private int insertMessageIntoDatabase(String nickOfSender, String nickOfReceiver, String messageText) {
        int answer = -1;
        String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
        String dbUsername = "root";
        String dbPassword = "";
        String sqlQuery = "INSERT INTO messages(sender_id, receiver_id, message_text) VALUES (?, ? ,?);";
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
        ) {

            int idOfReceiver = findNickId(nickOfReceiver);

            preparedStatement.setInt(1,findNickId(nickOfSender));
            preparedStatement.setInt(2,idOfReceiver);
            preparedStatement.setString(3,messageText);

            int rowsAffected = preparedStatement.executeUpdate();

            // Проверка количества измененных строк
            if (rowsAffected > 0) {
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    answer = resultSet.getInt(1);
                } else {
                    System.err.println("[MessageHandlerLogic] Error with take generated Id");
                }
            } else {
                System.out.println("[MessageHandlerLogic] Issue with putting message to database!!");
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return answer;
    }

    private int findNickId(String nick) {
        int answer = 0;
        String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
        String dbUsername = "root";
        String dbPassword = "";
        String sqlQuery = "SELECT Id FROM users WHERE Login = ?;";

        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        ) {

            preparedStatement.setString(1, nick);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                answer = resultSet.getInt("Id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println(answer);
        return answer;
    }
}
