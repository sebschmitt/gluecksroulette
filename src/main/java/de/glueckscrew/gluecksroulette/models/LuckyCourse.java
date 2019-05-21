package de.glueckscrew.gluecksroulette.models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import lombok.Data;

/**
 * @author Florian Dahlitz
 */
@Data
public class LuckyCourse {

    private static Logger logger = Logger.getLogger(LuckyCourse.class.getSimpleName());

    private static final String SERIALIZE_DELIMITER = "\n";

    private String identifier;
    private List<LuckyStudent> students;

    public LuckyCourse(String identifier, List<LuckyStudent> students) {
        this.identifier = identifier;
        this.students = students;
    }

    public String serialize() {
        String serializedCourse = "";

        for (LuckyStudent student : this.getStudents()) {
            serializedCourse += student.serialize() + "\n";
        }

        return serializedCourse;
    }

    public static LuckyCourse deserialize(String data, String identifier) {
        List<LuckyStudent> students = new ArrayList<>();

        for (String studentData : data.split(SERIALIZE_DELIMITER)) {
            students.add(LuckyStudent.deserialize(studentData));
        }

        return new LuckyCourse(identifier, new ArrayList<>(students));
    }
}
