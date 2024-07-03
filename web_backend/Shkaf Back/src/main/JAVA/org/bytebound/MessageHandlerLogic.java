package org.bytebound;

import org.java_websocket.WebSocket;

import java.sql.*;

public class MessageHandlerLogic implements MessageHandler {
    private DatabaseData databaseData = new DatabaseData();
    @Override
    public void handleMessage(String message, WebSocket conn) {
        System.out.println("[MessageHandlerLogic] handleMessage -> " + message + " -> " + conn);
        String[] splitedDataOfBody = message.split(",");

        String nickOfSender = splitedDataOfBody[0].substring(1);
        String nickOfReceiver = splitedDataOfBody[1];
        String messageText = splitedDataOfBody[2];
        messageText = messageText.substring(0, messageText.length() - 1);

        System.out.println("[MessageHandlerLogic] " + nickOfSender + " " + nickOfReceiver + " " + messageText + "\n");

        if (conn != null) {
            conn.send(messageText);
        } else {
            System.err.println("[MessageHandlerLogic] WebSocket to send data is null");
        }

        Timestamp timestamp = null;

        int messageIdAfterAddingToDataBase = insertMessageIntoDatabase(nickOfSender, nickOfReceiver, messageText);
        if (messageIdAfterAddingToDataBase != -1) {
            timestamp = dataOfSentMessage(messageIdAfterAddingToDataBase);
            System.out.println("[MessageHandlerLogic] Time of sent message -> " + timestamp);
        } else {
            System.err.println("[MessageHandlerLogic] Error with adding data to database!");
        }
    }

    @Override
    public Timestamp dataOfSentMessage(int messageId) {
        Timestamp answer = null;
        String sqlQuery = "SELECT Timestamp FROM messages WHERE message_id = ?;";

        try (
                Connection connection = DriverManager.getConnection(databaseData.getJdbcUrl(),databaseData.getDbUsername(), databaseData.getDbPassword());
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
        String jdbcUrl = "jdbc:mysql://localhost:3306/shkafdatabase";
        String dbUsername = "root";
        String dbPassword = "";
        String sqlQuery = "INSERT INTO messages(sender_id, receiver_id, message_text) VALUES (?, ? ,?);";
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
        ) {

            preparedStatement.setInt(1,findNickId(nickOfSender));
            preparedStatement.setInt(2,findNickId(nickOfReceiver));
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
        String jdbcUrl = "jdbc:mysql://localhost:3306/shkafdatabase";
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

        System.out.println("[MessageHandlerLogic] -> Id for User " + nick + " ->" + answer);
        return answer;
    }
}
