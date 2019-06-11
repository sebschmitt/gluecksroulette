package de.glueckscrew.gluecksroulette.models;

import lombok.Data;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Dahlitz
 */
@Data
public class LuckyStudent implements Cloneable {

    private static Logger logger = Logger.getLogger(LuckyStudent.class.getSimpleName());

    private static final String SERIALIZE_DELIMITER = ",";

    private String name;
    private double weight;

    private LuckyStudent() {
        this("", 1.0);
    }

    public LuckyStudent(String name) {
        this(name, 1.0);
    }

    public LuckyStudent(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

    public String serialize() {
        return String.format(Locale.US, "%s%s%f", this.getName(), SERIALIZE_DELIMITER, this.getWeight());
    }

    public static LuckyStudent deserialize(String data) {
        String[] studentData = data.split(SERIALIZE_DELIMITER);

        if (studentData.length < 1 || studentData.length > 2) {
            logger.log(Level.SEVERE, String.format(
                "Student data doesn't match the following pattern: name[,weight]%nData: %s",
                data));
            return null;
        }

        double weight = 1.0;

        try {
            weight = Double.parseDouble(studentData[1]);
        } catch (NumberFormatException nfe) {
            logger.log(Level.SEVERE,
                String.format("Found an invalid value for weight. Full Trace Back:%n"),
                nfe);
            return null;
        } catch (IndexOutOfBoundsException ioobe) {
            logger.log(Level.FINER, "No value for weight found. Use default value 1.0");
        }

        return new LuckyStudent(studentData[0], weight);
    }

    @Override
    public LuckyStudent clone() {
        return new LuckyStudent(name, weight);
    }
}
