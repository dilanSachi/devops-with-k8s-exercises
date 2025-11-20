package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TodoBackend {

    static List<String> todos = new ArrayList<>();

    public static void main(String[] args) throws IOException {
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
        System.out.println("Todo Backend started in port " + port);
    }

    static class AddTodoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            System.out.println("Adding a new todo");
            InputStream inputStream = exchange.getRequestBody();
            String newTodo = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            System.out.println(newTodo);
            todos.add(newTodo);
            exchange.sendResponseHeaders(200, 0);
            OutputStream os = exchange.getResponseBody();
            os.close();
        }
    }

    static class GetTodoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            System.out.println("Responding todos");
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
    }
}
