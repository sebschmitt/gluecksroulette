package de.glueckscrew.gluecksroulette.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test class to test the LuckyStudent model.
 *
 * @author Florian Dahlitz
 */
public class LuckyStudentTest {
    private static final double DELTA = 0.0002;

    @Test
    public void testGetter() {
        // given
        LuckyStudent student = new LuckyStudent("Bob");

        // when
        String name = student.getName();
        double weight = student.getWeight();

        // then
        assertEquals("Bob", name);
        assertEquals(1.0, weight, DELTA);
    }

    @Test
    public void testSetter() {
        // given
        LuckyStudent student = new LuckyStudent("Bob");

        // when
        student.setName("Alice");
        student.setWeight(2.0);

        // then
        assertEquals("Alice", student.getName());
        assertEquals(2.0, student.getWeight(), DELTA);
    }

    @Test
    public void testRequiredArgsConstructor() {
        // given

        // when
        LuckyStudent student = new LuckyStudent("Bob");

        // then
        assertEquals("Bob", student.getName());
        assertEquals(1.0, student.getWeight(), DELTA);
    }

    @Test
    public void testAllArgsConstructor() {
        // given

        // when
        LuckyStudent student = new LuckyStudent("Bob", 2.5);

        // then
        assertEquals("Bob", student.getName());
        assertEquals(2.5, student.getWeight(), DELTA);
    }

    @Test
    public void testSerialize() {
        // given
        LuckyStudent student = new LuckyStudent("Bob");
        String expected = "Bob,1.000000";

        // when
        String actual = student.serialize();

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testLeadingAndTrailingSpaces() {
        // given
        LuckyStudent expected = new LuckyStudent("Bob", 1.0);
        String data = " Bob  ,      1.0  ";

        // when
        LuckyStudent actual = LuckyStudent.deserialize(data);

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testDeserializePrecisionOne() {
        // given
        LuckyStudent expected = new LuckyStudent("Bob", 1.0);
        String data = "Bob,1.0";

        // when
        LuckyStudent actual = LuckyStudent.deserialize(data);

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testDeserializePrecisionSix() {
        // given
        LuckyStudent expected = new LuckyStudent("Bob", 1.0);
        String data = "Bob,1.000000";

        // when
        LuckyStudent actual = LuckyStudent.deserialize(data);

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testNotTooLongNameWithOneSpace() {
        // given
        LuckyStudent student = new LuckyStudent("Joe Muller");
        String expected = "J. Muller";

        // when
        String actual = student.getShortName();

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testTooLongNameWithOneSpace() {
        // given
        LuckyStudent student = new LuckyStudent("Davenport Longbottom");
        String expected = "D. Longbottom";

        // when
        String actual = student.getShortName();

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testTooLongNameWithMoreThanOneSpace() {
        // given
        LuckyStudent student = new LuckyStudent("Julian-Jack Christopher Evans");
        String expected = "J.-J. C. Evans";

        // when
        String actual = student.getShortName();

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testTooLongNameWithoutSpaces() {
        // given
        LuckyStudent student = new LuckyStudent("Superlongreallyreallyextensivelongname");
        String expected = "Superlongreallyreallyextensivelongname";

        // when
        String actual = student.getShortName();

        // then
        assertEquals(expected, actual);
    }
}
