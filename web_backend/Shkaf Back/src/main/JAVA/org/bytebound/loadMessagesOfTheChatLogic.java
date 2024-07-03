package org.bytebound;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class loadMessagesOfTheChatLogic {
    private DatabaseData databaseData = new DatabaseData();

    public List<String> lastMessages(int count, int firstUserId, int secondUserId) {
        System.out.println("[loadMessagesOfTheChatLogic] First id -> " + firstUserId + " Second id ->" + secondUserId);
        List<String> answer = new ArrayList<>();

        String searchMessages = "SELECT message_text FROM messages WHERE sender_id = ? AND receiver_id = ? ORDER BY Timestamp DESC LIMIT ?;";

        try (
                Connection connection = DriverManager.getConnection(databaseData.getJdbcUrl(), databaseData.getDbUsername(), databaseData.getDbPassword());
                PreparedStatement preparedStatement = connection.prepareStatement(searchMessages, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ) {
            preparedStatement.setInt(1, firstUserId);
            preparedStatement.setInt(2, secondUserId);
            preparedStatement.setInt(3, count);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                answer.add(resultSet.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            answer.add("[loadMessagesOfTheChatLogic] Error");
        }

        if (answer.isEmpty()) {
            answer.add("[loadMessagesOfTheChatLogic] No messages yet");
        }

        System.out.println("[loadMessagesOfTheChatLogic] " + answer);
        return answer;
    }
}