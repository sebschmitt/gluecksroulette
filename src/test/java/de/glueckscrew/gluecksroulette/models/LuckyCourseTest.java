package de.glueckscrew.gluecksroulette.models;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class LuckyCourseTest {

    @Test
    public void testGetter() {
        // given
        List<LuckyStudent> expectedStudents = new ArrayList<>();
        expectedStudents.add(new LuckyStudent("Bob"));
        expectedStudents.add(new LuckyStudent("Alice", 2.6));

        String expectedIdentifier = "Sample Course";
        LuckyCourse course = new LuckyCourse(expectedIdentifier, expectedStudents);

        // when
        String actualIdentifier = course.getIdentifier();
        List<LuckyStudent> actualStudents = course.getStudents();

        // then
        assertEquals(expectedIdentifier, actualIdentifier);
        assertEquals(expectedStudents, actualStudents);
    }

    @Test
    public void testSetter() {
        // given
        String expectedIdentifier = "Sample Course";
        List<LuckyStudent> expectedStudents = new ArrayList<>();
        expectedStudents.add(new LuckyStudent("Bob"));
        expectedStudents.add(new LuckyStudent("Alice", 2.6));

        List<LuckyStudent> students = new ArrayList<>();
        LuckyCourse course = new LuckyCourse("Course", students);

        // when
        course.setIdentifier(expectedIdentifier);
        course.setStudents(expectedStudents);

        // then
        assertEquals(expectedIdentifier, course.getIdentifier());
        assertEquals(expectedStudents, course.getStudents());
    }

    @Test
    public void testSerialize() {
        // given
        List<LuckyStudent> students = new ArrayList<>();
        students.add(new LuckyStudent("Bob"));
        students.add(new LuckyStudent("Alice", 2.6));

        LuckyCourse course = new LuckyCourse("Sample Course", students);

        String expected = "Bob,1.000000\nAlice,2.600000\n";

        // when
        String actual = course.serialize();

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testDeserialize() {
        // given
        List<LuckyStudent> students = new ArrayList<>();
        students.add(new LuckyStudent("Bob"));
        students.add(new LuckyStudent("Alice", 2.6));

        LuckyCourse expected = new LuckyCourse("Sample Course", students);

        String data = "Bob,1.000000\nAlice,2.600000\n";

        // when
        LuckyCourse actual = LuckyCourse.deserialize(data, "Sample Course");

        // then
        assertEquals(expected, actual);
    }
}
