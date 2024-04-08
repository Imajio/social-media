package org.bytebound;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Scanner;

public class receiveMessageFromClient implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //Header settings
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        //Main logic
        InputStream requestBody = exchange.getRequestBody();
        Scanner scanner = new Scanner(requestBody, "UTF-8").useDelimiter("\\A");
        String body = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        System.out.println(body);

        String[] splitedDataOfBody = body.split(",");

        String nickOfSender = splitedDataOfBody[0];
        String nickOfReceiver = splitedDataOfBody[1];
        String messageText = splitedDataOfBody[2];

        System.out.println(nickOfSender + "\n" + nickOfReceiver + "\n" + messageText + "\n");

        Timestamp response = null;

        int  messageIdAfterAddingToDataBase = insertMessageIntoDatabase(nickOfSender, nickOfReceiver, messageText);
        if (messageIdAfterAddingToDataBase != -1) {
            response = dataOfSentMessage(messageIdAfterAddingToDataBase);
            System.out.println("Data of sended message -> " + response);
        } else {
            System.err.println("Error with fetching data of message!");
        }

    }

    private Timestamp dataOfSentMessage(int messageId) {
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
                    System.err.println("Error with take generated Id");
                }
            } else {
                System.out.println("Issue with putting message to database!!");
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
