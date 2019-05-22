package de.glueckscrew.gluecksroulette.physics;

import javafx.scene.Group;
import javafx.scene.shape.Sphere;
import lombok.Setter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to calculate the movement of different physic-objects and handles their collisions.
 * It is implemented as a singleton, not because we need this as a global variable but to prevent two interfering physics
 *
 * @author Paul Weisser
 */

public class LuckyPhysics {
    private static Logger logger = Logger.getLogger(LuckyPhysics.class.getSimpleName());
    private static LuckyPhysics instance;

    @Setter
    private Group frame;
    @Setter
    private Group wheel;
    @Setter
    private Sphere sphere;

    //private constructor to prevent a second instantiation
    private LuckyPhysics() {}

    //access to the class
    public static LuckyPhysics getInstance() {
        if (LuckyPhysics.instance == null) {
            LuckyPhysics.instance = new LuckyPhysics();
        }
        return LuckyPhysics.instance;
    }

    // calls tick(int steps) with a value of 1
    public int tick() {return this.tick(1);}

    /**
     * the tick-method is used to perform a given amount steps moving the roulette-physic-objects around and handling collision
     */

    public int tick(int steps) {

        if (steps > 0) {
            for (int i = 0; i <= steps; i++) {
                //TODO: ticking
            }
        } else {
            logger.log(Level.WARNING, "Physics asked to tick 0 units, skipping!");
            return 1;
        }
        return 0;
    }
    public int spin(){
        //TODO: start
        logger.log(Level.INFO, "Spin started!");
        return 0;
    }

    public int reset(){
        //TODO: reset
        logger.log(Level.INFO, "Sphere reset!");
        return 0;
    }
}
