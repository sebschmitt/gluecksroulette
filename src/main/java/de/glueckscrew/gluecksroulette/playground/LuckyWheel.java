package de.glueckscrew.gluecksroulette.playground;

import javafx.scene.Group;
import lombok.Getter;
import lombok.Setter;

/**
 * This class extends the javafx-group-class by rotationSpeed.
 * implemented as singleton
 *
 * @author Paul Weisser
 */


public class LuckyWheel extends Group {
    private static LuckyWheel instance;

    @Getter
    @Setter
    private double rotationSpeed = 0;

    private LuckyWheel() {

    }

    //access to the class
    public static LuckyWheel getInstance() {
        if (LuckyWheel.instance == null) {
            LuckyWheel.instance = new LuckyWheel();
        }
        return LuckyWheel.instance;
    }

    public LuckyWheel(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public LuckyWheel(double rotationSpeed, Group group) {
        super(group);
        this.rotationSpeed = rotationSpeed;
    }

}
