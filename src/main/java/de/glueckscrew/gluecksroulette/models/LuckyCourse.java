package de.glueckscrew.gluecksroulette.models;

import java.util.List;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Florian Dahlitz
 */
public class LuckyCourse {
    private static Logger logger = Logger.getLogger(LuckyCourse.class.getSimpleName());

    @Getter @Setter private String identifier;
    @Getter @Setter private List<LuckyStudent> students;

    public LuckyCourse(String identifier, List<LuckyStudent> students) {
        this.identifier = identifier;
        this.students = students;
    }

    @Override
    public String toString(){
        return String.format("identifier: %s\nstudents: %s", this.identifier, this.students.toString());
    }
}
