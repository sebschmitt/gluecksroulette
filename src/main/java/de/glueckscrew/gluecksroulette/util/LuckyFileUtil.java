package de.glueckscrew.gluecksroulette.util;

import de.glueckscrew.gluecksroulette.io.LuckyIO;
import de.glueckscrew.gluecksroulette.models.LuckyCourse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for File objects
 *
 * @author Sebastian Schmitt
 */
public class LuckyFileUtil {
    public static LuckyCourse loadCourse(File file, Logger LOGGER) {
        if (file != null) {
            try {
                return LuckyCourse.deserialize(LuckyIO.read(new FileInputStream(file)), file.getName());
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE, String.format("FileNotFoundException thrown. Relying on default values%n"), e);
            } catch (SecurityException e) {
                LOGGER.log(Level.SEVERE, String.format("We're not allowed to read %s. Relying on default values%n",
                        file.getAbsolutePath()), e);
            }
        }
        return null;
    }
}
