package org.bytebound;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class takeAllChatsOfUserFromDataBase implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        StringBuilder response = new StringBuilder();

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        InputStream requestBody = exchange.getRequestBody();
        Scanner scanner = new Scanner(requestBody, "UTF-8").useDelimiter("\\A");
        String nick = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        System.out.println("[takeAllChatsOfUserFromDataBase] Nick >>");
        System.out.println(nick);

        List<String> chats = loadAllChatsOfUser(nick);

        for (int i = 0;i < chats.size();i++) {
            response.append("\"").append(i+1).append("\": \"").append(chats.get(i)).append("\",");
        }

        StringBuilder finalResponse = new StringBuilder();
        finalResponse.append("{").append(response.deleteCharAt(response.length()-1)).append("}");

        System.out.println("[takeAllChatsOfUserFromDataBase] Final Response >>");
        System.out.println("[takeAllChatsOfUserFromDataBase] " + finalResponse);

        // Отправка ответа клиенту
        exchange.sendResponseHeaders(200, finalResponse.toString().getBytes().length);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(finalResponse.toString().getBytes());
        responseBody.close();
    }

    private List<String> loadAllChatsOfUser(String nick) {
        List<String> chats = new ArrayList<>();

        String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
        String dbUsername = "root";
        String dbPassword = "";

        String whichChatsExist = "SELECT NickFirst, NickSecond FROM chats WHERE NickFirst = '" + nick + "' OR NickSecond = '" + nick + "';";

        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
                PreparedStatement preparedStatement = connection.prepareStatement(whichChatsExist, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String nameFirst = resultSet.getString("NickFirst");
                String nameSecond = resultSet.getString("NickSecond");

                // Проверяем, какое из имен отличается от nick и добавляем его в список чатов
                if (!nameFirst.equals(nick)) {
                    chats.add(nameFirst);
                }
                if (!nameSecond.equals(nick)) {
                    chats.add(nameSecond);
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();

        }
        return chats;
    }
}
