package fi.devopswithk8s.exercises;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;

public class ImageApplication {

    static long lastDownloadedTime = 0;

    public static void main(String[] args) throws IOException {
        int port = 3000;
        try {
            port = Integer.parseInt(System.getenv("PORT"));
        } catch (NumberFormatException e) {
            System.out.println("PORT variable not found. Starting on default port " + port);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new BodyHandler());
        server.createContext("/image", new ImageHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Image Application started in port " + port);
    }

    static class BodyHandler implements HttpHandler {
        boolean firstRequest = true;
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            if (!new File("/usr/src/app/files/image.png").isFile()) {
                downloadImage();
                lastDownloadedTime = System.currentTimeMillis();
            } else if (lastDownloadedTime == 0) {
                lastDownloadedTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastDownloadedTime > 10 * 60 * 1000) {
                if (!firstRequest) {
                    System.out.println("Time exceeded. Downloading file...");
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
                    "<img src=\"http://localhost:8081/image\" alt=\"Lorem Picsum\" style=\"width:500px;\">" +

                    "<form>" +
                    "  <input type=\"todoinput\" id=\"todoinput\" />" +
                    "  <button>Create ToDo</button>" +
                    "</form>" +
                    "<ul>" +
                    "  <li>Learn JavaScript</li>" +
                    "  <li>Learn React</li>" +
                    "  <li>Build a Project</li>" +
                    "</ul>" +
                    "<p>DevOps with Kubernetes 2025</p>" +
                    "</body>" +
                    "</html>" +
                    "<script>" +
                    "const todo = document.getElementById(\"todoinput\");" +
                    "todo.addEventListener(\"input\", (event) => {" +
                    "  todo.setCustomValidity(\"\");" +
                    "  if (!todo.validity.valid) {" +
                    "    return;" +
                    "  }" +
                    "  if (todo.value.length > 140) {" +
                    "    todo.setCustomValidity(\"Characters more than 140 are not accepted\");" +
                    "  }" +
                    "});"+
                    "</script>";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void downloadImage() {
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://picsum.photos/1200").openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream("/usr/src/app/files/image.png")) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                lastDownloadedTime = System.currentTimeMillis();
            } catch (IOException e) {
                System.out.println("Error occurred during image download.");
                e.printStackTrace();
            }
        }
    }

    static class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            File file = new File("/usr/src/app/files/image.png");
            exchange.sendResponseHeaders(200, file.length());
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", "image/png");
            OutputStream outputStream = exchange.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
        }
    }
}
