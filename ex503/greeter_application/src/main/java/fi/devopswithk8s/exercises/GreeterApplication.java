package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GreeterApplication {

    private static final Logger logger = Logger.getLogger(GreeterApplication.class.getName());

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        int port = 3004;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new GreetHandler());
        server.createContext("/favicon.ico", new FaviconHandler());
        server.createContext("/healthz", new HealthHandler());
        server.setExecutor(null);
        server.start();
        logger.log(Level.INFO, "Greeter Application started in port " + port);
    }

    private static class FaviconHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            logger.log(Level.INFO, "Responding 404 for favicon request.");
            exchange.sendResponseHeaders(404, 0);
            OutputStream os = exchange.getResponseBody();
            os.close();
        }
    }

    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            logger.log(Level.INFO, "Responding 200 for health request.");
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.close();
        }
    }

    private static class GreetHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            logger.log(Level.INFO, "Got a greeting from client.");
            Headers headers = exchange.getRequestHeaders();
            if (headers.containsKey("User-agent") && headers.get("User-agent").getFirst().contains("GoogleHC")) {
                logger.log(Level.INFO, "Responding 200 to health check request.");
                exchange.sendResponseHeaders(200, 0);
                OutputStream os = exchange.getResponseBody();
                os.close();
                return;
            }
            String response = "Hello from version " + System.getenv("GREET_VERSION");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
