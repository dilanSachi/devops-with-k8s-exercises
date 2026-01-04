package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogReader {

    final static String randomString = UUID.randomUUID().toString();
    private static final Logger logger = Logger.getLogger(LogReader.class.getName());

    public static void main(String[] args) throws IOException {
        int port = 3001;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new LogHandler());
        server.createContext("/favicon.ico", new FaviconHandler());
        server.setExecutor(null);
        server.start();
        logger.log(Level.INFO, "Server started in port " + port);
    }

    static class LogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getRequestHeaders();
            if (headers.containsKey("User-agent") && headers.get("User-agent").getFirst().contains("GoogleHC")) {
                logger.log(Level.INFO, "Responding 200 to health check request.");
                exchange.sendResponseHeaders(200, 0);
                OutputStream os = exchange.getResponseBody();
                os.close();
                return;
            }
            String counter = "0";
            try {
                HttpRequest request = HttpRequest.newBuilder().uri(new URI(System.getenv("PING_PONG_APP_URL") + "/pings")).GET().build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                logger.log(Level.INFO, "Received num pings from ping pong app: " + response.body());
                counter = response.body();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String fileContent = "";
            try {
                InputStream fileInputStream = new FileInputStream("/config-vol/information.txt");
                fileContent = new String(fileInputStream.readAllBytes());
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            FileInputStream fileInputStream = new FileInputStream("/usr/src/app/files/log.txt");
            String logContent = new String(fileInputStream.readAllBytes());
            fileInputStream.close();
            String response = "file content: " + fileContent + "\n" + "env variable: MESSAGE=" + System.getenv("MESSAGE") + "\n" + logContent + "Ping / Pongs: " + counter;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
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
}