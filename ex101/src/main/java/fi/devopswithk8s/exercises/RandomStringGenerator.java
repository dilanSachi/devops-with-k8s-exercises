package fi.devopswithk8s.exercises;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomStringGenerator {
    static Logger logger = Logger.getLogger(RandomStringGenerator.class.getName());

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            logger.log(Level.INFO, UUID.randomUUID().toString());
            Thread.sleep(5000);
        }
    }
}
