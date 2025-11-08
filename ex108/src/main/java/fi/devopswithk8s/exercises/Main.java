package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;

public class Main {

    static String applicationId = UUID.randomUUID().toString();

    public static void main(String[] args) throws IOException {
        int port = 3000;
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
        public void handle(HttpExchange exchange) throws IOException
        {
            String response = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<title>DevOps</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "<h1>Application " + applicationId + "!</h1>\n" +
                    "<p>This is a simple response! - " + UUID.randomUUID() + "</p>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}