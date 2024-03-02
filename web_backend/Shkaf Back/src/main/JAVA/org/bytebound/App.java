package org.bytebound;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class App {

    static String login;
    static String password;

    public static void main(String[] args) throws IOException {
        // Создание HTTP-сервера
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 1);
        // Обработчик запросов на путь "/receiveData"
        server.createContext("/receiveData", new ReceiveDataHandler());
        server.setExecutor(null); // создаем отдельный поток для каждого запроса
        server.start();
        System.out.println("Server started on port: 8000");
    }

    static class ReceiveDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Получаем данные из запроса
            InputStream requestBody = exchange.getRequestBody();
            Scanner scanner = new Scanner(requestBody, "UTF-8").useDelimiter("\\A");
            String requestData = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            System.out.println("Данные из формы: " + requestData);

            // Ищем значения для login и password
            String loginPattern = "Content-Disposition: form-data; name=\"login\"\\s*\\r?\\n\\r?\\n(.*?)\\r?\\n";
            String passwordPattern = "Content-Disposition: form-data; name=\"password\"\\s*\\r?\\n\\r?\\n(.*?)\\r?\\n";

            login = extractValue(requestData, loginPattern);
            password = extractValue(requestData, passwordPattern);

            // Выводим полученные значения на консоль
            System.out.println("Логин: " + login);
            System.out.println("Пароль: " + password);

            //Добавляем кавычки для db
            String newLogin = "'" + login + "'";
            String newPassword = "'" + password + "'";

            // Получаем данные из базы данных и обрабатываем их
            String responseFromDB = fetchDataAndProcess(newLogin, newPassword);
            String response;
            String[] parts = responseFromDB.split("\\|\\|");
            String loginFromDatabase = parts[0];
            String passwordFromDatabase = parts[1];
            if (loginFromDatabase.equals(login) && passwordFromDatabase.equals(password)) {
                response = "Success: Data received and matched!";
                System.out.println("Success: Data received and matched!");
            } else {response = "Check if your data in form are correct!";
                System.out.println("Error with database login and password \n" + "Database \n Login: " + loginFromDatabase + " \n Password: " + passwordFromDatabase);
                System.out.println("Users data \n Login: " + login + " \n Password: " + password);
            }

            // Отправляем ответ клиенту
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }

        private String fetchDataAndProcess(String login, String password) {
            // Параметры подключения к базе данных
            String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
            String dbUsername = "root";
            String dbPassword = "";

            // Инициализация переменных для ответа
            String answerLogin = "";
            String answerPassword = "";

            // SQL-запрос для извлечения данных
            String selectLogin = "SELECT * FROM users WHERE users.Login=" + login + ";";
            String selectPassword = "SELECT * FROM users WHERE users.Password=" + password + ";";


            try {
                // Установка соединения с базой данных
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

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
                    case(1): {
                        System.out.println("Login was suceful detected, and sended to manipulate");
                        resultSet.beforeFirst(); // Возвращаем курсор перед первой строкой
                        if (resultSet.next()) {
                            answerLogin = resultSet.getString("Login");
                            loginPassed = true;
                        }
                        break;
                    }
                    case(0): {
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
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
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


    static {
        System.out.println("\n \n \n Code was sucessfuly compiled! \n");
    }

}