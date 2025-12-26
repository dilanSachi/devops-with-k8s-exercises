package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.Properties;

public class PingPongApplication {

    private static Connection connection;

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        int port = 3000;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            System.out.println("PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new PingPongHandler());
        server.createContext("/pings", new PingsHandler());
        server.setExecutor(null);
        server.start();
        initDBConnection();
        System.out.println("Ping Pong Application started in port " + port);
    }

    private static void initDBConnection() throws SQLException, InterruptedException {
        final String url = "jdbc:postgresql://" + System.getenv("DB_URL");
        final Properties props = new Properties();
        props.setProperty("user", System.getenv("DB_USER"));
        props.setProperty("password", System.getenv("DB_PASSWORD"));
        for (int i = 0; i < 3; i++) {
            try {
                connection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                if (i == 2) {
                    e.printStackTrace();
                    System.exit(1);
                }
                System.out.println("Could not connect to db. Retrying after a while.");
                Thread.sleep(5000);
            }
        }
        System.out.println("Connected to db: " + connection.getMetaData().getDatabaseProductVersion());
    }

    private static class PingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            System.out.println("Responding num of pings to log reader." + readCounter());
            String response = String.valueOf(readCounter());
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static class PingPongHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            String response = "pong " + readCounter();
            updateCounter();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void updateCounter() {
            try {
                Statement st = connection.createStatement();
                st.execute("UPDATE PINGPONGCOUNTER SET COUNTER=((SELECT COUNTER FROM PINGPONGCOUNTER WHERE ID=1) + 1) WHERE ID=1");
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static int readCounter() {
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNTER FROM PINGPONGCOUNTER WHERE ID=1");
            rs.next();
            int counter = rs.getInt("COUNTER");
            rs.close();
            return counter;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}