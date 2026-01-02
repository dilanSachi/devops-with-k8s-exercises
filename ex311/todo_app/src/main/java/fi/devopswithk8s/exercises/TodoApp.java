package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class TodoApp {

    static long lastDownloadedTime = 0;
    static String IMAGE_FILE_PATH = "";
    private static final Logger logger = Logger.getLogger(TodoApp.class.getName());

    public static void main(String[] args) throws IOException {
        int port = 3000;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            log("INFO", "PORT variable not found. Starting on default port " + port);
        }
        IMAGE_FILE_PATH = System.getenv("IMAGE_FILE_PATH");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new BodyHandler());
        server.createContext("/image", new ImageHandler());
        server.setExecutor(null);
        server.start();
        log("INFO", "Todo App started in port " + port);
    }

    static class BodyHandler implements HttpHandler {
        boolean firstRequest = true;
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            if (!new File(IMAGE_FILE_PATH).isFile()) {
                downloadImage();
                lastDownloadedTime = System.currentTimeMillis();
            } else if (lastDownloadedTime == 0) {
                lastDownloadedTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastDownloadedTime > 10 * 60 * 1000) {
                if (!firstRequest) {
                    log("INFO", "Time exceeded. Downloading file...");
                    downloadImage();
                    firstRequest = true;
                } else {
                    firstRequest = false;
                }
            }
            String response = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<title>DevOps</title>" +
                    "</head>" +
                    "<body>" +
                    "" +
                    "<h1>The DevOps App</h1>" +
                    "<img src=\"/image\" alt=\"Lorem Picsum\" style=\"width:500px;\">" +
                    "<form id=\"todoForm\">" +
                    "  <input  name=\"todo\" id=\"todoinput\" type=\"text\"/>" +
                    "  <button type=\"submit\">Create ToDo</button>" +
                    "</form>" +
                    "<ul id=\"todoList\"></ul>" +
                    "<p>DevOps with Kubernetes 2025</p>" +
                    "</body>" +
                    "</html>" +
                    "<script>" +
                    "const todo = document.getElementById(\"todoinput\");" +
                    "const form = document.getElementById(\"todoForm\");" +
                    "todo.addEventListener(\"input\", (event) => {" +
                    "  todo.setCustomValidity(\"\");" +
                    "  if (!todo.validity.valid) {" +
                    "    return;" +
                    "  }" +
                    "  if (todo.value.length > 140) {" +
                    "    todo.setCustomValidity(\"Characters more than 140 are not accepted\");" +
                    "  }" +
                    "});"+
                    "form.addEventListener(\"submit\", async (ev) => {" +
                    "      console.log(\"Adding new todo\");" +
                    "      ev.preventDefault();" +
                    "      try {" +
                    "        const resp = await fetch(\"/addtodo\", {" +
                    "          method: \"POST\"," +
                    "          body: todo.value," +
                    "        });" +
                    "        if (!resp.ok) throw new Error(\"HTTP \" + resp.status + \" \" + resp.statusText);" +
                    "        form.reset();" +
                    "        loadTodos();" +
                    "      } catch (e) {" +
                    "        console.log(e);" +
                    "      }" +
                    "    });" +
                    "function renderList(items, ul) {" +
                    "      ul.innerHTML = \"\";" +
                    "      for (const item of items) {" +
                    "        const trimmed = item.trim();" +
                    "        if (!trimmed) continue;\n" +
                    "        const li = document.createElement(\"li\");" +
                    "        li.textContent = trimmed;" +
                    "        ul.appendChild(li);" +
                    "      }" +
                    "    }" +
                    "    async function loadTodos() {" +
                    "      const ul = document.getElementById(\"todoList\");" +
                    "      ul.innerHTML = \"\";" +
                    "      try {" +
                    "        const resp = await fetch(\"/gettodos\", { method: \"GET\" });" +
                    "        if (!resp.ok) throw new Error(\"HTTP \" + resp.status + \" \" + resp.statusText);" +
                    "        const text = await resp.text();" +
                    "        const items = text.split(\",\");" +
                    "        renderList(items, ul);" +
                    "      } catch (e) {" +
                    "        console.log(e);" +
                    "      }" +
                    "    }" +
                    "document.addEventListener(\"DOMContentLoaded\", loadTodos);" +
                    "</script>";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void downloadImage() {
            try (BufferedInputStream in = new BufferedInputStream(new URL(System.getenv("IMAGE_RESOURCE_URL")).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(IMAGE_FILE_PATH)) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                lastDownloadedTime = System.currentTimeMillis();
            } catch (IOException e) {
                log("ERROR", "Error occurred during image download.");
                e.printStackTrace();
            }
        }
    }

    static class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            File file = new File(IMAGE_FILE_PATH);
            exchange.sendResponseHeaders(200, file.length());
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "image/png");
            OutputStream outputStream = exchange.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
        }
    }

    private static void log(String level, String message) {
        if (level.equals("INFO")) {
            System.out.println(message);
        } else {
            System.err.println(message);
        }
    }
}
