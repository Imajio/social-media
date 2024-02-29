package org.bytebound;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
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
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 1);
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
            System.out.println("Data from form: " + requestData);

            // Ищем значения для login и password
            String loginPattern = "Content-Disposition: form-data; name=\"login\"\\s*\\r?\\n\\r?\\n(.*?)\\r?\\n";
            String passwordPattern = "Content-Disposition: form-data; name=\"password\"\\s*\\r?\\n\\r?\\n(.*?)\\r?\\n";

            login = extractValue(requestData, loginPattern);
            password = extractValue(requestData, passwordPattern);

            // Выводим полученные значения на консоль
            System.out.println("Login: " + login);
            System.out.println("Password: " + password);

            //Проверяем сходство данных
            String response = "";
            if (login.equals("test@gmail.com") && password.equals("Danik")) {
                response = "Success: Data received and matched!";
            } else {
                response = "Error: Invalid login or password!";
            }

            // Отправляем ответ клиенту
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
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