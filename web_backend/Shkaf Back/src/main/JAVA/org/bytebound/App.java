package org.bytebound;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;

import com.sun.net.httpserver.HttpServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


public class App extends WebSocketServer {
    private MessageHandler messageHandler;
    private ConnectionManager connectionManager;
    public static void main(String[] args) throws IOException {
        if (pingDatabase()) {
            System.out.println("Database connection succesful!");

            int httpPort = 8000;
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(httpPort), 1);
            System.out.println("Http server started on " + httpPort + " port");
            httpServer.createContext("/receiveData", new receiveDataHandler());
            httpServer.createContext("/getDataForChat", new newChatCreation());
            httpServer.createContext("/takeAllChatsOfUserFromDataBase", new takeAllChatsOfUserFromDataBase());
            httpServer.setExecutor(null); // создаем отдельный поток для каждого запроса
            httpServer.start();

            int socketPort = 8080;
            App webSocketServer = new App(socketPort);
            webSocketServer.start();
            System.out.println("WebSocket server started on port " + socketPort);
        } else {
            System.err.println("Error with database. Try to: \n - Check if it turned on. \n - If Data of database set right.");
        }
    }

    public App(int port) {
        super(new InetSocketAddress(port));
        this.messageHandler = new MessageHandlerLogic();
        this.connectionManager = new ConnectionManager();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String resourceDescriptor = conn.getResourceDescriptor();

        switch (resourceDescriptor) {
            case "/sendMessage":
                messageHandler.handleMessage(message);
                break;
            case "/login":
                connectionManager.addUserConnection(findNickId(message), conn);
                break;
            default:
                System.err.println("Error with web socket (void onMessage)");
                break;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error occurred on connection " + conn.getRemoteSocketAddress() + ": " + ex);
    }

    @Override
    public void onStart() {}


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