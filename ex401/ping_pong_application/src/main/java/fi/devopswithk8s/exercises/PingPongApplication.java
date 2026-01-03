package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PingPongApplication {

    private static Connection connection;
    private static final Logger logger = Logger.getLogger(PingPongApplication.class.getName());

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        int port = 3000;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new PingPongHandler());
        server.createContext("/pings", new PingsHandler());
        server.createContext("/favicon.ico", new FaviconHandler());
        server.setExecutor(null);
        server.start();
        initDBConnection();
        logger.log(Level.INFO, "Ping Pong Application started in port " + port);
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
                    logger.log(Level.SEVERE, "Could not connect to db. Shutting down.");
                    System.exit(1);
                }
                logger.log(Level.INFO, "Could not connect to db. Retrying after a while.");
                Thread.sleep(5000);
            }
        }
        logger.log(Level.INFO, "Connected to db: " + connection.getMetaData().getDatabaseProductVersion());
    }

    private static Connection getDbConnection() throws SQLException {
        try {
            connection.getClientInfo();
        } catch (SQLException e) {
            try {
                initDBConnection();
            } catch (InterruptedException exception) {
                e.printStackTrace();
            }
        }
        return connection;
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

    private static class PingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            logger.log(Level.INFO, "Responding num of pings to log reader." + readCounter());
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
            logger.log(Level.INFO, "Got a ping from client.");
            Headers headers = exchange.getRequestHeaders();
            if (headers.containsKey("User-agent") && headers.get("User-agent").getFirst().contains("GoogleHC")) {
                logger.log(Level.INFO, "Responding 200 to health check request.");
                exchange.sendResponseHeaders(200, 0);
                OutputStream os = exchange.getResponseBody();
                os.close();
                return;
            }
            String response = "pong " + readCounter();
            updateCounter();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void updateCounter() {
            try {
                Statement st = getDbConnection().createStatement();
                logger.log(Level.INFO, "Updating counter.");
                st.execute("UPDATE PINGPONGCOUNTER SET COUNTER=((SELECT COUNTER FROM PINGPONGCOUNTER WHERE ID=1) + 1) WHERE ID=1");
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static int readCounter() {
        try {
            Statement st = getDbConnection().createStatement();
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
