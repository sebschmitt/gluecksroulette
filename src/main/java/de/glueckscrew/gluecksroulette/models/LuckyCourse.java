package de.glueckscrew.gluecksroulette.models;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Florian Dahlitz
 */
@Data
public class LuckyCourse implements Cloneable {

    private static Logger logger = Logger.getLogger(LuckyCourse.class.getSimpleName());

    private static final String SERIALIZE_DELIMITER = "\n";

    private String identifier;
    @Getter
    private List<LuckyStudent> students;

    private int countStudentWeightLow;
    private double studentWeightLowest;

    public LuckyCourse(String identifier, List<LuckyStudent> students) {
        this.identifier = identifier;
        this.students = students;
        initWeights();
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

    public void select(LuckyStudent student) {
        // set new weight p for selected student to p/n, where n is size of course
        double oldWeight = student.getWeight();
        double newWeight = oldWeight / students.size();
        student.setWeight(newWeight);
        if (oldWeight >= 1 && newWeight < 1) {
            ++countStudentWeightLow;
        }
        if (studentWeightLowest > newWeight) {
            studentWeightLowest = newWeight;
        }

        if (countStudentWeightLow == students.size()) {
            normalizeWeights();
        }
        logger.log(Level.INFO, "selected student: " + student.getName());
    }

    private void normalizeWeights() {
        double factor = 1/studentWeightLowest;
        for (LuckyStudent student : students) {
            student.setWeight(student.getWeight()*factor);
        }
        countStudentWeightLow = 0;
    }

    private void initWeights() {
        countStudentWeightLow = 0;
        studentWeightLowest = 1;

        for (LuckyStudent student : students) {
            double weight = student.getWeight();
            if (weight < 1) {
                ++countStudentWeightLow;
                if (studentWeightLowest > weight) {
                    studentWeightLowest = weight;
                }
            }
        }
        if (countStudentWeightLow == students.size()) {
            normalizeWeights();
        }
    }

    public void resetWeights() {
        countStudentWeightLow = 0;
        studentWeightLowest = 1;

        students.forEach(student -> student.setWeight(1));
    }

    @Override
    public LuckyCourse clone() {
        List<LuckyStudent> clonedStudents = new ArrayList<>();

        students.forEach(student -> clonedStudents.add(student.clone()));

        return new LuckyCourse(identifier, clonedStudents);
    }
}
