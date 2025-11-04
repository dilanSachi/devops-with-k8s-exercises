package fi.devopswithk8s.exercises;

import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomStringGenerator {
    static Logger logger = Logger.getLogger(RandomStringGenerator.class.getName());
    final static String randomString = UUID.randomUUID().toString();

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            logger.log(Level.INFO, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date()) + ": " + randomString);
            Thread.sleep(5000);
        }
    }
}
