package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class LogReader {

    final static String randomString = UUID.randomUUID().toString();

    public static void main(String[] args) throws IOException {
        int port = 3001;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            System.out.println("PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started in port " + port);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String counter = "0";
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream("/usr/src/app/files/counter.txt");
                String fileContent = new String(fileInputStream.readAllBytes());
                counter = fileContent;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                fileInputStream.close();
            }
            String response = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date()) + ": " + randomString + "\n" + "Ping / Pongs: " + counter;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}