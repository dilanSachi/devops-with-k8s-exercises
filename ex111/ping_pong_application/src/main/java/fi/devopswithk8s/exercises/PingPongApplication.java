package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class PingPongApplication {

    public static void main(String[] args) throws IOException {
        int port = 3000;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            System.out.println("PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new PingHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Ping Pong Application started in port " + port);
    }

    static class PingHandler implements HttpHandler {
        int counter = 0;
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            String response = "pong " + counter;
            counter++;
            FileOutputStream fileOutputStream = new FileOutputStream("/usr/src/app/files/counter.txt", false);
            byte[] strToBytes = String.valueOf(counter).getBytes();
            fileOutputStream.write(strToBytes);
            fileOutputStream.close();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}