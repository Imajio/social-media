package org.bytebound;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class App {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/receiveData", new ReceiveDataHandler());
        server.setExecutor(null); // создаем отдельный поток для каждого запроса
        server.start();
        System.out.println("Server started on port: 8000");
    }

    static class ReceiveDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Получаем данные из запроса
            InputStream requestBody = exchange.getRequestBody();
            Scanner scanner = new Scanner(requestBody, "UTF-8").useDelimiter("\\A");
            String requestData = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            System.out.println("Data wrom form: " + requestData);

            // Отправляем ответ клиенту
            String response = "Data was succesfully taken!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
}