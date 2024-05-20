package org.bytebound;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class newChatCreation implements HttpHandler {
    private DatabaseData databaseData = new DatabaseData();
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String response = "null";

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        InputStream requestBody = exchange.getRequestBody();
        Scanner scanner = new Scanner(requestBody, "UTF-8").useDelimiter("\\A");
        String requestData = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        System.out.println("[newChatCreation] Request to create new chat between 2 users : " + requestData + " \n ------------------------------------------- \n ");
        String ifChat = insertDataInChatsTable(extractNamesFromAJAXRecuest(requestData));
        if (ifChat != null && ifChat.equals("Data was succesful puted to database!")) {
            response = "{ \"message\": \"Chat could be created!\" }";
        } else if (ifChat != null && ifChat.equals("One or Both names aren\'t in database!")) {
            response = "{ \"message\": \"One or Both names arent in database!\" }";
        } else {
            response = "{ \"message\": \"Some error with inserting  data to db!\" }";
        }

        // Отправляем ответ клиенту
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(response.getBytes());
        responseBody.close();
        requestBody.close();
        responseBody.close();
    }

    private String insertDataInChatsTable(String answerFromOtherTable) {
        if (answerFromOtherTable != null) {
            String answer = null;
            System.out.println("[newChatCreation] Both names exist, chat is going to be created!");
            String[] parts = answerFromOtherTable.split(",");
            Arrays.sort(parts);
            String firstNick = parts[0];
            String secondNick = parts[1];

            //Database данные
            String jdbcUrl = "jdbc:mysql://localhost:3306/shkafdatabase";
            String dbUsername = "root";
            String dbPassword = "";

            //SQL-запрос на вставку данных
            String ifDataNOTExist = "INSERT INTO chats (NickFirst, NickSecond) VALUES (?, ?);";

            if (!ifDataOfChatInDatabaseExist(firstNick, secondNick)) {
                try (
                        Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
                        PreparedStatement preparedStatement = connection.prepareStatement(ifDataNOTExist, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ) {
                    preparedStatement.setString(1, firstNick);
                    preparedStatement.setString(2, secondNick);

                    int rowsAffected = preparedStatement.executeUpdate();

                    // Проверка количества измененных строк
                    if (rowsAffected > 0) {
                        answer = "Data was succesful puted to database!";
                    } else {
                        System.out.println("[newChatCreation] Issue with putting data in database!!");
                        answer = "Issue with putting data in database!!";
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    answer = "Error with database";
                }
            }
            return answer;
        } else {
            System.out.println("[newChatCreation] One or Both names aren\'t in database!");
            return "One or Both names aren\'t in database!";
        }
    }

    private boolean ifDataOfChatInDatabaseExist(String firstNick, String secondNick) {
        boolean answer = false;

        //SQL-запрос на вставку данных
        String ifDataExist = "SELECT * FROM chats WHERE NickFirst = ? AND NickSecond = ?;";

        int i = 0;

        try (
                Connection connection = DriverManager.getConnection(databaseData.getJdbcUrl(),databaseData.getDbUsername(), databaseData.getDbPassword());
                PreparedStatement preparedStatement = connection.prepareStatement(ifDataExist, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ) {
            preparedStatement.setString(1, firstNick);
            preparedStatement.setString(2, secondNick);

            // Выполнение запроса на вставку данных
            ResultSet resultSet = preparedStatement.executeQuery();

            // Проверка наличия результатов
            if (resultSet.next()) {
                answer = true;
            } else {
                System.out.println("[newChatCreation] Data needed to be putted in db!!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answer;
    }

    private String extractNamesFromAJAXRecuest(String fullStringData) {
        String d = fullStringData.substring(1, fullStringData.length() - 1);

        String[] parts = d.split(",");

        String nickOfFirstUserValue = parts[0].split(":")[1];
        String nickOfSecondUserValue = parts[1].split(":")[1];

        String clearNicknameFirst = nickOfFirstUserValue.replace("\"", "").trim();
        String clearNicknameSecond = nickOfSecondUserValue.replace("\"", "").trim();

        String ifNicksExistInDatabase = fetchUserDataAndProcess(clearNicknameFirst, clearNicknameSecond);

        if (ifNicksExistInDatabase != null) {
            return clearNicknameFirst + "," + clearNicknameSecond;
        } else {
            return null;
        }
    }

    private String fetchUserDataAndProcess(String loginFirst, String loginSecond) {
        System.out.println("[newChatCreation] Finding data in database for new chat! \n --------------------------------------------- \n");

        // Инициализация переменных для ответа
        String answerNickname = null;

        // SQL-запрос для извлечения данных
        String selectLoginFirst = "SELECT * FROM users WHERE users.Login=\'" + loginFirst + "\';";
        String selectLoginSecond = "SELECT * FROM users WHERE users.Login=\'" + loginSecond + "\';";

        try {
            Connection connection = DriverManager.getConnection(databaseData.getJdbcUrl(),databaseData.getDbUsername(), databaseData.getDbPassword());

            // Если мы дошли до этой точки без исключения, подключение прошло успешно

            //Инициация временых переменых
            int count = 0;
            boolean isFirstExist = false;
            boolean isSecondExist = false;

            //Проверяем есть ли первый пользователь в базе данных
            PreparedStatement loginStatementFirst = connection.prepareStatement(selectLoginFirst, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSetFirst = loginStatementFirst.executeQuery();

            while (resultSetFirst.next()) {
                count++;
            }

            switch (count) {
                default: {
                    System.out.println("[newChatCreation] Some error with login 1");
                    break;
                }
                case (1): {
                    System.out.println("[newChatCreation] Login 1 was detected in database");
                    resultSetFirst.beforeFirst(); // Возвращаем курсор перед первой строкой
                    if (resultSetFirst.next()) {
                        isFirstExist = true;
                    }
                    break;
                }
                case (0): {
                    System.out.println("[newChatCreation] Login 1 was not detected in database");
                    break;
                }
            }

            count = 0;

            //Проверяем есть ли второй пользователь в базе данных
            PreparedStatement loginStatementSecond = connection.prepareStatement(selectLoginSecond, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSetSecond = loginStatementSecond.executeQuery();

            while (resultSetSecond.next()) {
                count++;
            }

            switch (count) {
                default: {
                    System.out.println("[newChatCreation] Some error with login 2");
                    break;
                }
                case (1): {
                    System.out.println("[newChatCreation] Login 2 was detected");
                    resultSetSecond.beforeFirst(); // Возвращаем курсор перед первой строкой
                    if (resultSetSecond.next()) {
                        isSecondExist = true;
                    }
                    break;
                }
                case (0): {
                    System.out.println("[newChatCreation] Login 2 was not detected in database");
                    break;
                }
            }

            if (isFirstExist && isSecondExist) {
                answerNickname = "All right, lets continue!";
            }

            // Закрытие всех ресурсов
            loginStatementFirst.close();
            resultSetFirst.close();
            loginStatementSecond.close();
            resultSetSecond.close();
        } catch (SQLException e) {
            // Если возникло исключение, выводим сообщение об ошибке
            e.printStackTrace();
            answerNickname = "Failed to connect to database";
        }
        return answerNickname;
    }
}
