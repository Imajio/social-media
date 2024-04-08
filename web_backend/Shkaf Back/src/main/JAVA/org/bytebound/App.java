package org.bytebound;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;
import com.sun.net.httpserver.HttpServer;


public class App {

    public static void main(String[] args) throws IOException {
        if (pingDatabase()) {
            System.out.println("Database connection succesful!");
            // Создание HTTP-сервера
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 1);
            // Обработчик запросов на путь "/receiveData"
            server.createContext("/receiveData", new receiveDataHandler());
            server.createContext("/getDataForChat", new newChatCreation());
            server.createContext("/sendMessage", new receiveMessageFromClient());
            server.createContext("/takeAllChatsOfUserFromDataBase", new takeAllChatsOfUserFromDataBase());




            server.setExecutor(null); // создаем отдельный поток для каждого запроса
            server.start();

            //Debug data
            System.out.println("Server started on port: 8000");
        } else {
            System.err.println("Error with database. Try to: \n - Check if it turned on. \n - If Data of database set right.");
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

    static {
        System.out.println("\n \n \n Code was sucessfuly compiled! \n");
    }

}