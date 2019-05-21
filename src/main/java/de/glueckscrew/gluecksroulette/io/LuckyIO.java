package de.glueckscrew.gluecksroulette.io;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Dahlitz
 */
public class LuckyIO {

    private static Logger logger = Logger.getLogger(LuckyIO.class.getSimpleName());

    private static final Charset ENCODING = UTF_8;

    private LuckyIO() {
    }

    public static boolean write(File file, String data) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] bytesArray = data.getBytes();

            fos.write(bytesArray);
            fos.flush();

            return true;
        } catch (SecurityException | IOException e) {
            logger.log(Level.SEVERE, String.format("We are unable to create/write file %s. See full stacktrace:%n", file.getAbsolutePath()), e);
            return false;
        }
    }

    public static String read(FileInputStream fis) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, ENCODING))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE,
                    String.format("Couldn't read from FileInputStream. See full stacktrace:%n"), ioe);
            return "";
        }
    }
}
