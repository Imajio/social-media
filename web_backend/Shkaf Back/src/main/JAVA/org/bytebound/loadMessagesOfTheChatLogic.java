package org.bytebound;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class loadMessagesOfTheChatLogic {
    private DatabaseData databaseData = new DatabaseData();
    public List<String> lastMessages(int count,int firstUserId,int secondUserId) {
        List<String> answer = new ArrayList<>();

        String searchMessages = "SELECT * FROM messages WHERE (sender_id = ? OR sender_id = ?) AND " +
                "(receiver_id = ? OR receiver_id = ?) ORDER BY Timestamp DESC LIMIT ?;";

        try (
                Connection connection = DriverManager.getConnection(databaseData.getJdbcUrl(),databaseData.getDbUsername(), databaseData.getDbPassword());
                PreparedStatement preparedStatement = connection.prepareStatement(searchMessages, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                )
        {
            preparedStatement.setInt(1, firstUserId);
            preparedStatement.setInt(2, secondUserId);
            preparedStatement.setInt(3, secondUserId);
            preparedStatement.setInt(4, firstUserId);
            preparedStatement.setInt(5, count);

            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("[loadMessagesOfTheChatLogic]" + resultSet.getArray("message_text"));
            resultSet.beforeFirst();
            while (resultSet.next()) {
                answer.add(resultSet.getString("message_text"));
            }
        } catch(SQLException e) {
            e.printStackTrace();
            answer.add("Error");
        }

        System.out.println("[loadMessagesOfTheChatLogic] " + answer);
        return answer;
    }
}