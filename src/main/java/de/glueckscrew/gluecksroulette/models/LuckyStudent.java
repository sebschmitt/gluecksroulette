package de.glueckscrew.gluecksroulette.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Dahlitz
 */
@Data
public class LuckyStudent implements Cloneable {
    private static final Logger LOGGER = Logger.getLogger(LuckyStudent.class.getSimpleName());

    private static final String SERIALIZE_DELIMITER = ",";

    @Getter
    private String name;
    @Getter
    private String shortName;
    @Getter
    @Setter
    private double weight;

    private LuckyStudent() {
        this("", 1.0);
    }

    public LuckyStudent(String name) {
        this(name, 1.0);
    }

    public LuckyStudent(String name, double weight) {
        setName(name);
        this.weight = weight;
    }

    public String serialize() {
        return String.format(Locale.US, "%s%s%f", this.getName(), SERIALIZE_DELIMITER, this.getWeight());
    }

    public static LuckyStudent deserialize(String data) {
        String[] studentData = data.split(SERIALIZE_DELIMITER);

        if (studentData.length < 1 || studentData.length > 2) {
            LOGGER.log(Level.SEVERE, String.format(
                "Student data doesn't match the following pattern: name[,weight]%nData: %s",
                data));
            return null;
        }

        double weight = 1.0;

        try {
            weight = Double.parseDouble(studentData[1]);
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.SEVERE,
                String.format("Found an invalid value for weight. Full Trace Back:%n"),
                nfe);
            return null;
        } catch (IndexOutOfBoundsException ioobe) {
            LOGGER.log(Level.FINER, "No value for weight found. Use default value 1.0");
        }

        return new LuckyStudent(studentData[0], weight);
    }

    @Override
    public LuckyStudent clone() {
        return new LuckyStudent(name, weight);
    }

    public void setName(String name) {
        this.name = name;

        int lastSpace = name.lastIndexOf(" ");
        if (lastSpace > 0) {
            this.shortName = name.substring(0, lastSpace).replaceAll("([A-Za-z])[A-Za-z]*", "$1.") + name.substring(lastSpace);
        } else {
            this.shortName = name;
        }
    }
}
