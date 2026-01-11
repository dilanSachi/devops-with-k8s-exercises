package fi.devopswithk8s.exercises;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import io.nats.service.*;
import io.nats.client.*;
import javax.net.ssl.HttpsURLConnection;

public class BroadcasterApp {

    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
        Connection natsConnection = Nats.connect(System.getenv("NATS_URL"));
        Dispatcher dispatcher = natsConnection.createDispatcher();
        System.out.println("Connected to nats");

        Subscription todoSubscription = dispatcher.subscribe(System.getenv("NATS_TODO_TOPIC"),
                System.getenv("NATS_DONE_QUEUE"), msg -> {
            log("INFO", "Subscription received message " + new String(msg.getData(), StandardCharsets.UTF_8));
            try {
                sendToDiscord("A task was added.\\n\\n" + new String(msg.getData(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Subscription doneScription = dispatcher.subscribe(System.getenv("NATS_DONE_TOPIC"),
                System.getenv("NATS_DONE_QUEUE"), msg -> {
            log("INFO", "Subscription received message " + new String(msg.getData(), StandardCharsets.UTF_8));
            try {
                sendToDiscord("A task was updated.\\n\\n" + new String(msg.getData(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void sendToDiscord(String message) throws IOException, URISyntaxException {
        String webhookUrl = "";
        try {
            InputStream fileInputStream = new FileInputStream("/etc/secret-volume/DISCORD_WEBHOOK_URL");
            webhookUrl = new String(fileInputStream.readAllBytes());
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            log("SEVERE", "Failed to read webhook url");
            e.printStackTrace();
        }
        URI uri = new URI(webhookUrl);
        HttpsURLConnection connection = (HttpsURLConnection) uri.toURL().openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setDoOutput(true);
        try (OutputStream stream = connection.getOutputStream()) {
            String content = "{\"content\": \"" + message.replace("\"", "\\\"") + "\"}";
            log("INFO", content);
            stream.write(content.getBytes(StandardCharsets.UTF_8));
            stream.flush();
        }
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_NO_CONTENT) {
            log("INFO", "Sent to discord.");
        } else {
            log("SEVERE", "Failed to send to discord: " + responseCode);
            log("SEVERE", connection.getResponseMessage());
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(
                    responseCode == HttpsURLConnection.HTTP_NOT_FOUND ? connection.getErrorStream() : connection.getInputStream()))) {

                String errorInputLine;
                StringBuilder errorResponse = new StringBuilder();

                while ((errorInputLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorInputLine);
                }
                log("SEVERE", errorResponse.toString());
            }
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
