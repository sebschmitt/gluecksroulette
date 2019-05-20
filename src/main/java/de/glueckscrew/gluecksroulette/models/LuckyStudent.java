package de.glueckscrew.gluecksroulette.models;

import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Florian Dahlitz
 */
public class LuckyStudent {
    private static Logger logger = Logger.getLogger(LuckyStudent.class.getSimpleName());

    @Getter @Setter private String name;
    @Getter @Setter private double probability;

    public LuckyStudent(String name) {
        this(name, 1.0);
    }

    public LuckyStudent(String name, double probability) {
        this.name = name;
        this.probability = probability;
    }

    @Override
    public String toString() {
        return String.format("%s(name: %s, probability: %s)", this.getClass().getSimpleName(), this.getName(), this.getProbability());
    }
}
