package de.glueckscrew.gluecksroulette.models;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;

/**
 * @author Florian Dahlitz
 */
@Data
public class LuckyStudent {

    private static Logger logger = Logger.getLogger(LuckyStudent.class.getSimpleName());

    private static final String SERIALIZE_DELIMITER = ",";

    private String name;
    private double probability;

    private LuckyStudent() {
        this("", 1.0);
    }

    public LuckyStudent(String name) {
        this(name, 1.0);
    }

    public LuckyStudent(String name, double probability) {
        this.name = name;
        this.probability = probability;
    }

    public String serialize() {
        return String.format("%s%s%f", this.getName(), SERIALIZE_DELIMITER, this.getProbability());
    }

    public static LuckyStudent deserialize(String data) {
        String[] studentData = data.split(SERIALIZE_DELIMITER);

        if (studentData.length < 1 || studentData.length > 2) {
            logger.log(Level.SEVERE, String.format(
                "Student data doesn't match the following pattern: name[,probability]%nData: %s",
                data));
            return null;
        }

        double probability = 1.0;

        try {
            probability = Double.parseDouble(studentData[1]);
        } catch (NumberFormatException nfe) {
            logger.log(Level.SEVERE,
                String.format("Found an invalid value for probability. Full Trace Back:%n"),
                nfe);
            return null;
        } catch (IndexOutOfBoundsException ioobe) {
            logger.log(Level.FINER, "No value for probability found. Use default value 1.0");
        }

        return new LuckyStudent(studentData[0], probability);
    }
}
