package org.bytebound;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class receiveDataHandler implements HttpHandler {

    static String login;
    static String password;
    static String hashedpassword;
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method;

        // Получаем данные из запроса
        InputStream requestBody = exchange.getRequestBody();
        Scanner scanner = new Scanner(requestBody, "UTF-8").useDelimiter("\\A");
        String requestData = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        System.out.println("Data from form: " + requestData);

        // Ищем значения для login и password
        String loginPattern = "Content-Disposition: form-data; name=\"login\"\\s*\\r?\\n\\r?\\n(.*?)\\r?\\n";
        String passwordPattern = "Content-Disposition: form-data; name=\"password\"\\s*\\r?\\n\\r?\\n(.*?)\\r?\\n";
        String methodPattern = "Content-Disposition: form-data; name=\"method\"\\s*\\r?\\n\\r?\\n(.*?)\\r?\\n";

        login = extractValue(requestData, loginPattern);
        password = extractValue(requestData, passwordPattern);
        method = extractValue(requestData, methodPattern);

        // Выводим полученные значения на консоль
        System.out.println("Login: " + login);
        System.out.println("Password: " + password);
        System.out.println("Method: " + method);

        try {
            // Создание экземпляра MessageDigest с алгоритмом SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Получение байтового представления пароля
            byte[] passwordBytes = password.getBytes();

            // Обновление MessageDigest с байтовым представлением пароля
            byte[] hashedBytes = digest.digest(passwordBytes);

            // Преобразование байтового массива в строку в шестнадцатеричном формате
            StringBuilder hexString = new StringBuilder();
            for (byte hashedByte : hashedBytes) {
                String hex = Integer.toHexString(0xff & hashedByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            hashedpassword = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Получаем данные из базы данных и обрабатываем их
        String responseFromDB = fetchUserDataAndProcess(login, hashedpassword);
        String response;
        String[] parts = responseFromDB.split("\\|\\|");
        String loginFromDatabase = parts[0];
        String passwordFromDatabase = parts[1];

        if (!loginFromDatabase.equals(" ") || !passwordFromDatabase.equals(" ")) {
            if (loginFromDatabase.equals(login) && passwordFromDatabase.equals(hashedpassword)) {
                response = "Success: Data received and matched!";
                System.out.println("Success: Data received and matched!");
                System.out.println("Database login and password \n" + "Database \n Login: " + loginFromDatabase + " \n Password: " + passwordFromDatabase);
                System.out.println("Users data \n Login: " + login + " \n Password: " + hashedpassword + "\n s------------------------------------------------ \n ");
            } else {
                response = "Check if your data in form are correct!";
                System.out.println("Error with database login and password \n" + "Database \n Login: " + loginFromDatabase + " \n Password: " + passwordFromDatabase);
                System.out.println("Users data \n Login: " + login + " \n Password: " + hashedpassword + "\n ------------------------------------------------------- \n ");
            }
        } else if (method.equals("registrate")) {
            insertUserDataInDataBase(login, hashedpassword);
            response = "Data was succesful inserted in database!";
            System.out.println("Data was succesful inserted in database! You now registrated! \n ------------------------------------------------- \n ");
        } else {
            response = "Data not exist in database!";
            System.out.println("Data not exist in database!");
            System.out.println("Do you want to registrate? \n ---------------------------------------------------- \n ");
        }

        // Отправляем ответ клиенту
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    private boolean insertUserDataInDataBase(String login, String password) {
        // Параметры подключения к базе данных
        String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
        String dbUsername = "root";
        String dbPassword = "";

        boolean answer = false;

        //SQL-запрос на вставку данных
        String insertData = "INSERT INTO users (Login, Password) VALUES ('" + login + "', '" + password + "');";

        Connection connection = null;

        try {
            // Установка соединения с базой данных
            connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

            boolean isValid = connection.isValid(1);

            if (isValid) {
                //Делаем запросы в db и проверяем количество находящихся там ответов на Login
                PreparedStatement dataINSEERT = connection.prepareStatement(insertData, Statement.RETURN_GENERATED_KEYS);
                dataINSEERT.executeUpdate();

                answer = true;
            } else System.out.println("Cannot connect to database!");
            connection.close();
        } catch (SQLException e) {
            // Если возникло исключение, выводим сообщение об ошибке
            e.printStackTrace();
        } finally {
            // Важно закрыть соединение после использования
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Issue with connection close: " + e.getMessage());
                }
            }
        }

        return answer;
    }

    private String fetchUserDataAndProcess(String login, String password) {
        System.out.println("Finding data in database for login! \n --------------------------------------------- \n");
        // Параметры подключения к базе данных
        String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
        String dbUsername = "root";
        String dbPassword = "";

        // Инициализация переменных для ответа
        String answerLogin = " ";
        String answerPassword = " ";

        // SQL-запрос для извлечения данных
        String selectLogin = "SELECT * FROM users WHERE users.Login='" + login + "';";

        Connection connection = null;

        try {
            // Установка соединения с базой данных
            connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

            boolean isValid = connection.isValid(1);

            if (isValid) {
                //Делаем запросы в db и проверяем количество находящихся там ответов на Login
                PreparedStatement loginStatement = connection.prepareStatement(selectLogin, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = loginStatement.executeQuery();

                int count = 0;
                boolean loginPassed = false;

                while (resultSet.next()) {
                    count++;
                }

                switch (count) {
                    default: {
                        System.out.println("More than 1 Login was matched!");
                        break;
                    }
                    case (1): {
                        System.out.println("Login was suceful detected, and sent to manipulate");
                        resultSet.beforeFirst(); // Возвращаем курсор перед первой строкой
                        if (resultSet.next()) {
                            answerLogin = resultSet.getString("Login");
                            loginPassed = true;
                        }
                        break;
                    }
                    case (0): {
                        System.out.println("Login was not detacted in database");
                        break;
                    }
                }

                //Делаем запросы в db для пароля

                if (loginPassed) {
                    resultSet.beforeFirst(); // Возвращаем курсор перед первой строкой
                    if (resultSet.next()) {
                        answerPassword = resultSet.getString("Password");
                    }
                }

                // Закрытие всех ресурсов
                loginStatement.close();
                resultSet.close();
            } else System.out.println("Cannot connect to database!");
            connection.close();
        } catch (SQLException e) {
            // Если возникло исключение, выводим сообщение об ошибке
            e.printStackTrace();
        } finally {
            // Важно закрыть соединение после использования
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
                }
            }
        }

        return answerLogin + "||" + answerPassword;
    }

    private String extractValue(String text, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
