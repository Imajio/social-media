package org.bytebound;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class App {

    public static void main(String[] args) throws IOException {
        if (pingDatabase()) {
            System.out.println("Database connection succesful!");
            // Создание HTTP-сервера
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 1);
            // Обработчик запросов на путь "/receiveData"
            server.createContext("/receiveData", new ReceiveDataHandler());
            server.createContext("/getDataForChat", new newChatCreation());
            server.createContext("/takeAllChatsOfUserFromDataBase", new takeAllChatsOfUserFromDataBase());
            server.setExecutor(null); // создаем отдельный поток для каждого запроса
            server.start();
            System.out.println("Server started on port: 8000");
        } else {
            System.err.println("Error with database. Try to: \n - Check if it turned on. \n - If Data of database set right.");
        }
    }

    static class takeAllChatsOfUserFromDataBase implements HttpHandler {
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
            System.out.println("Nick >>");
            System.out.println(nick);

            List<String> chats = loadAllChatsOfUser(nick);

            for (int i = 0;i < chats.size();i++) {
                response.append("\"").append(i+1).append("\": \"").append(chats.get(i)).append("\",");
            }

            StringBuilder finalResponse = new StringBuilder();
            finalResponse.append("{").append(response.deleteCharAt(response.length()-1)).append("}");

            System.out.println("Final Response >>");
            System.out.println(finalResponse);

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

    private static boolean pingDatabase() {
        // Параметры подключения к базе данных
        String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
        String dbUsername = "root";
        String dbPassword = "";

        // SQL-запрос для проверки доступности базы данных
        String pingQuery = "SELECT 1;";

        Connection connection = null;

        try {
            // Установка соединения с базой данных
            connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

            // Создаем PreparedStatement для выполнения запроса
            PreparedStatement pingStatement = connection.prepareStatement(pingQuery);

            // Выполняем запрос
            pingStatement.executeQuery();

            // Если запрос выполнен успешно, возвращаем true
            return true;
        } catch (SQLException e) {
            // Если возникло исключение, возвращаем false
            return false;
        } finally {
            // Важно закрыть соединение после использования
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error while connecting close: " + e.getMessage());
                }
            }
        }
    }

    static class newChatCreation implements HttpHandler {
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

            System.out.println(" Request to create new chat between 2 users : " + requestData + " \n ------------------------------------------- \n ");
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
                System.out.println(" Both names exist, chat is going to be created!");
                String[] parts = answerFromOtherTable.split(",");
                Arrays.sort(parts);
                String firstNick = parts[0];
                String secondNick = parts[1];

                //Database данные
                String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
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
                            System.out.println("Issue with putting data in database!!");
                            answer = "Issue with putting data in database!!";
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        answer = "Error with database";
                    }
                }
                return answer;
            } else {
                System.out.println("One or Both names aren\'t in database!");
                return "One or Both names aren\'t in database!";
            }
        }

        private boolean ifDataOfChatInDatabaseExist(String firstNick, String secondNick) {
            boolean answer = false;
            //Database данные
            String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
            String dbUsername = "root";
            String dbPassword = "";

            //SQL-запрос на вставку данных
            String ifDataExist = "SELECT * FROM chats WHERE NickFirst = ? AND NickSecond = ?;";

            int i = 0;

            try (
                Connection connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
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
                    System.out.println("Data needed to be putted in db!!");
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
            System.out.println("Finding data in database for new chat! \n --------------------------------------------- \n");

            String jdbcUrl = "jdbc:mysql://localhost:3306/shkaf database";
            String dbUsername = "root";
            String dbPassword = "";

            // Инициализация переменных для ответа
            String answerNickname = null;

            // SQL-запрос для извлечения данных
            String selectLoginFirst = "SELECT * FROM users WHERE users.Login=\'" + loginFirst + "\';";
            String selectLoginSecond = "SELECT * FROM users WHERE users.Login=\'" + loginSecond + "\';";

            Connection connection = null;

            try {
                connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);

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
                        System.out.println("Some error with login 1");
                        break;
                    }
                    case (1): {
                        System.out.println("Login 1 was detected in database");
                        resultSetFirst.beforeFirst(); // Возвращаем курсор перед первой строкой
                        if (resultSetFirst.next()) {
                            isFirstExist = true;
                        }
                        break;
                    }
                    case (0): {
                        System.out.println("Login 1 was not detected in database");
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
                        System.out.println("Some error with login 2");
                        break;
                    }
                    case (1): {
                        System.out.println("Login 2 was detected");
                        resultSetSecond.beforeFirst(); // Возвращаем курсор перед первой строкой
                        if (resultSetSecond.next()) {
                            isSecondExist = true;
                        }
                        break;
                    }
                    case (0): {
                        System.out.println("Login 2 was not detected in database");
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
            return answerNickname;
        }
    }

    static class ReceiveDataHandler implements HttpHandler {

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

    static {
        System.out.println("\n \n \n Code was sucessfuly compiled! \n");
    }

}