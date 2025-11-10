package fi.devopswithk8s.exercises;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogWriter {

    static Logger logger = Logger.getLogger(LogWriter.class.getName());
    final static String randomString = UUID.randomUUID().toString();

    public static void main(String[] args) throws InterruptedException, IOException {
        while (true) {
            String text = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date()) + ": " + randomString + "\n";
            logger.log(Level.INFO, text);
            FileOutputStream outputStream = new FileOutputStream("/usr/src/app/files/log.txt", true);
            byte[] strToBytes = text.getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
            Thread.sleep(5000);
        }
    }
}