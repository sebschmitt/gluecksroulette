package de.glueckscrew.gluecksroulette;

/**
 * This class represents the central control component
 * It also contains the main method
 *
 * @author Sebastian Schmitt
 */
public class LuckyControl {

    /**
     * private to make sure we get only called from {@link #main(String[])}
     */
    private LuckyControl() {

    }

    public static void main(String[] args) {
        new LuckyControl();
    }
}
