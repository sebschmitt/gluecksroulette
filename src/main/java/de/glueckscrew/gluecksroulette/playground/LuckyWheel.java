package de.glueckscrew.gluecksroulette.playground;


import javafx.scene.Group;
import lombok.Getter;
import lombok.Setter;

/**
 * This class extends the javafx-group-class by rotationSpeed.
 *
 * @author Paul Weisser
 */


public class LuckyWheel extends Group {
    @Getter
    @Setter
    private double rotationSpeed = 0;

    public LuckyWheel() {

    }

    public LuckyWheel(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public LuckyWheel(double rotationSpeed, Group group) {
        super(group);
        this.rotationSpeed = rotationSpeed;
    }

}
