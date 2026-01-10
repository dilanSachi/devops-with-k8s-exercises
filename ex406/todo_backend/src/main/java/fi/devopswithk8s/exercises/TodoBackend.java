package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import io.nats.service.*;
import io.nats.client.*;

public class TodoBackend {

    private static java.sql.Connection connection;
    private static io.nats.client.Connection natsConnection;

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        int port = 3000;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            log("INFO", "PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HealthHandler());
        server.createContext("/addtodo", new AddTodoHandler());
        server.createContext("/gettodos", new GetTodoHandler());
        server.createContext("/todos", new UpdateTodoHandler());
        server.setExecutor(null);
        server.start();
        initDBConnection();
        initNATSConnection();
        log("INFO", "Todo Backend started in port " + port);
    }

    private static void initNATSConnection() throws InterruptedException, IOException {
        natsConnection = Nats.connect(System.getenv("NATS_URL"));
        System.out.println("Connected to nats");
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
                log("INFO", "Could not connect to db. Retrying after a while.");
                Thread.sleep(5000);
            }
        }
        log("INFO", "Connected to db: " + connection.getMetaData().getDatabaseProductVersion());
    }

    private static java.sql.Connection getDbConnection() throws SQLException {
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

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.close();
        }
    }

    static class AddTodoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            InputStream inputStream = exchange.getRequestBody();
            String newTodo = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log("INFO", "Adding a new todo: " + newTodo);
            if (newTodo.length() > 140) {
                exchange.sendResponseHeaders(413, 0);
                OutputStream os = exchange.getResponseBody();
                os.close();
                log("SEVERE", "Todo exceeds the allowed length limit (140): " + newTodo);
                return;
            }
            String id = UUID.randomUUID().toString();
            try {
                insertTodo(newTodo, id);
            } catch (SQLException e) {
                exchange.sendResponseHeaders(500, 0);
                OutputStream os = exchange.getResponseBody();
                os.close();
                throw new RuntimeException(e);
            }
            try {
                sendStatusToNATS("create", id, newTodo);
            } catch (Exception e) {
                log("SEVERE", "Error occurred while sending msg to nats: " + e.getMessage());
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
                OutputStream os = exchange.getResponseBody();
                os.close();
                throw new RuntimeException(e);
            }
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.close();
        }

        private void insertTodo(String newTodo, String id) throws SQLException {
            try {
                Statement st = getDbConnection().createStatement();
                st.execute("INSERT INTO TODO (ID, TODO, DONE) VALUES ('" + id + "','" + newTodo + "', FALSE)");
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
                log("SEVERE", e.getMessage());
                throw e;
            }
        }
    }

    static class GetTodoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<TODO> todos = getTodos();
            String todoList = "";
            for (int i = 0; i < todos.size(); i++) {
                todoList = todoList + todos.get(i).getId() + "::" + todos.get(i).getTodo() + "::" + todos.get(i).isDone();
                if (i != todos.size() - 1) {
                    todoList = todoList + ",";
                }
            }
            log("INFO", "Responding todos: " + todoList);
            exchange.sendResponseHeaders(200, todoList.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write((todoList).getBytes());
            outputStream.close();
        }
    }

    static class UpdateTodoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String[] paths = exchange.getRequestURI().toString().split("/");
            String todoId = paths[paths.length - 1];
            log("INFO", "Marking todo " + todoId + " as done");
            String newTodo = "";
            try {
                Statement st = getDbConnection().createStatement();
                st.execute("UPDATE TODO SET DONE=TRUE where ID='" + todoId + "'");
                ResultSet rs = st.executeQuery("SELECT TODO FROM TODO WHERE ID='" + todoId + "'");
                rs.next();
                newTodo = rs.getString("TODO");
            } catch (SQLException e) {
                log("SEVERE", "Error occurred while marking todo " + todoId + " as done");
                e.printStackTrace();
                exchange.sendResponseHeaders(500, 0);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.close();
                return;
            }
            try {
                sendStatusToNATS("update", todoId, newTodo);
            } catch (Exception e) {
                log("SEVERE", "Error occurred while sending msg to nats: " + e.getMessage());
                e.printStackTrace();
            }
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.close();
        }
    }

    private static List<TODO> getTodos() {
        try {
            Statement st = getDbConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT ID, TODO, DONE FROM TODO");
            List<TODO> todos = new ArrayList<>();
            while (rs.next()) {
                todos.add(new TODO(rs.getString("ID"), rs.getString("TODO"),
                        rs.getBoolean("DONE")));
            }
            rs.close();
            return todos;
        } catch (SQLException e) {
            log("SEVERE", e.getMessage());
            throw new RuntimeException();
        }
    }

    private static class TODO {
        String id;
        String todo;
        boolean done;

        public TODO(String id, String todo, boolean done) {
            this.id = id;
            this.todo = todo;
            this.done = done;
        }

        public String getId() {
            return id;
        }

        public String getTodo() {
            return todo;
        }

        public boolean isDone() {
            return done;
        }
    }

    private static void log(String level, String message) {
        if (level.equals("INFO")) {
            System.out.println(message);
        } else {
            System.err.println(message);
        }
    }

    private static void sendStatusToNATS(String event, String id, String newTodo) throws Exception {
        natsConnection.publish(event.equals("create") ? System.getenv("NATS_TODO_TOPIC"): System.getenv("NATS_DONE_TOPIC"),
                ("{\"task\": \"" + newTodo + "\", \"done\":\"" +
                (event.equals("create") ? "false": "true") + "\", \"id\": \"" + id + "\"}").getBytes());
    }
}
