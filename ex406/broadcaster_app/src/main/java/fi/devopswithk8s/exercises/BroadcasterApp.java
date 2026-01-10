package fi.devopswithk8s.exercises;

import java.io.*;
import io.nats.service.*;
import io.nats.client.*;

public class BroadcasterApp {

    public static void main(String[] args) throws InterruptedException, IOException {
        Connection natsConnection = Nats.connect(System.getenv("NATS_URL"));
        Dispatcher dispatcher = natsConnection.createDispatcher();
        System.out.println("Connected to nats");
        while (true) {
            Subscription subscription = dispatcher.subscribe("test-subject", msg -> log("INFO", "Subscription received message " + msg));
            Thread.sleep(2000);
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
