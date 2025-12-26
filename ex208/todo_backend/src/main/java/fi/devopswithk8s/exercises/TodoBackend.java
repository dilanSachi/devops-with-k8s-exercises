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

public class TodoBackend {

    private static Connection connection;

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        int port = 3000;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            System.out.println("PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/addtodo", new AddTodoHandler());
        server.createContext("/gettodos", new GetTodoHandler());
        server.setExecutor(null);
        server.start();
        initDBConnection();
        System.out.println("Todo Backend started in port " + port);
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

    static class AddTodoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            System.out.println("Adding a new todo");
            InputStream inputStream = exchange.getRequestBody();
            String newTodo = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println(newTodo);
            insertTodo(newTodo);
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.close();
        }

        private void insertTodo(String newTodo) {
            try {
                Statement st = connection.createStatement();
                st.execute("INSERT INTO TODO (TODO) VALUES ('" + newTodo + "')");
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static class GetTodoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            System.out.println("Responding todos");
            List<String> todos = getTodos();
            String todoList = "";
            for (int i = 0; i < todos.size(); i++) {
                todoList = todoList + todos.get(i);
                if (i != todos.size() - 1) {
                    todoList = todoList + ",";
                }
            }
            System.out.println(todoList);
            exchange.sendResponseHeaders(200, todoList.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(todoList.getBytes());
            outputStream.close();
        }

        private static List<String> getTodos() {
            try {
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT TODO FROM TODO");
                List<String> todos = new ArrayList<>();
                while (rs.next()) {
                    todos.add(rs.getString("TODO"));
                }
                rs.close();
                return todos;
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }
}
