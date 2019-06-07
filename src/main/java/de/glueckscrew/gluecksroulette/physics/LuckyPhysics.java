package de.glueckscrew.gluecksroulette.physics;

import com.sun.javafx.geom.Vec3d;
import de.glueckscrew.gluecksroulette.playground.LuckyBall;
import de.glueckscrew.gluecksroulette.playground.LuckyPlayground;
import de.glueckscrew.gluecksroulette.playground.LuckyWheel;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import lombok.Setter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to calculate the movement of different physic-objects and handles their collisions.
 * It is implemented as a singleton, not because we need this as a global variable but to prevent two interfering physics
 * Greetings to Mr. Kruse at this point :)
 *
 * @author Paul Weisser
 */

public class LuckyPhysics {
    private static Logger logger = Logger.getLogger(LuckyPhysics.class.getSimpleName());
    private static LuckyPhysics instance;

    private final static double COLLISION_REDUCTION = .98;
    private final static double AIR_RESISTANCE = .999;
    private final static double VERTICAL_BOUNCINESS = .3;
    private final static double WHEEL_ROTATION_REDUCTION = .005;
    private final static double GRAVITY = .1;
    private final static double MINIMAL_WHEEL_ROTATION = .001;
    private final static double WHEEL_MOMENTUM = .7;
    private final static double BASE_WHEEL_SPEED = 2;

    @Setter
    private Group frame;
    @Setter
    private LuckyWheel wheel;
    @Setter
    private LuckyBall luckyBall;

    //private constructor to prevent a second instantiation
    private LuckyPhysics() {
    }

    //access to the class
    public static LuckyPhysics getInstance() {
        if (LuckyPhysics.instance == null) {
            LuckyPhysics.instance = new LuckyPhysics();
        }
        return LuckyPhysics.instance;
    }

    // calls tick(int steps) with a value of 1
    public int tick() {
        return this.tick(1);
    }

    /**
     * the tick-method is used to perform a given amount steps moving the roulette-physic-objects
     * around and handling collision
     */

    public int tick(int steps) {

        if (steps > 0) {
            for (int i = 0; i < steps; i++) {
                //TODO if frame != null
                boolean collidesBaseWheel = false;
                boolean collidesColonWheel = false;
                boolean collidesFrame = false;

                if (luckyBall != null && wheel != null && !wheel.getChildren().isEmpty()) {
                    luckyBall.setTranslateY(luckyBall.getTranslateY() + luckyBall.getVelocity().y);
                    luckyBall.setTranslateX(luckyBall.getTranslateX() + luckyBall.getVelocity().x);
                    luckyBall.setTranslateZ(luckyBall.getTranslateZ() + luckyBall.getVelocity().z);

                    //rotate the wheel, unless its rotating slower than minimum-rotation-speed
                    if (wheel.getRotationSpeed() > MINIMAL_WHEEL_ROTATION) {
                        wheel.setRotationSpeed(wheel.getRotationSpeed() - WHEEL_ROTATION_REDUCTION);
                        if (wheel.getTransforms().isEmpty())
                            wheel.getTransforms().add(new Rotate(0, 0, 0, 0, Rotate.Y_AXIS));
                        try {
                            Rotate rotate = (Rotate) wheel.getTransforms().get(0);
                            rotate.setAngle((rotate.getAngle() + wheel.getRotationSpeed()) % 360);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Could not find/apply a transform in wheel, skipping! Full trace back: %n", e);
                        }
                    } else {
                        wheel.setRotationSpeed(0);
                    }

                    // simulate gravity and air resistance
                    luckyBall.getVelocity().y += GRAVITY;
                    luckyBall.getVelocity().y *= AIR_RESISTANCE;
                    luckyBall.getVelocity().z *= AIR_RESISTANCE;
                    luckyBall.getVelocity().x *= AIR_RESISTANCE;



                        Vec3d distanceCentre = new Vec3d(luckyBall.getTranslateX(), 0, luckyBall.getTranslateZ());
                    if (LuckyPlayground.WHEEL_RADIUS > distanceCentre.length() + luckyBall.getRadius()
                            && wheel.getTranslateY() <= luckyBall.getTranslateY() + luckyBall.getRadius()) {
                        //if (luckyBall.getBoundsInParent().intersects(wheel.getBoundsInParent())) {
                        //Since we need to use the intersect function with getBoundsInLocal in the collision detection,
                        // the wheel is somehow treated as a square, preventing errors we only handle the collision if
                        // it really appeared
                        if (distanceCentre.length() <= LuckyPlayground.WHEEL_RADIUS + luckyBall.getRadius()) {
                            //TODO adjust this depending on collision with other objects than the wheel
                            luckyBall.setTranslateZ(luckyBall.getTranslateZ() - luckyBall.getVelocity().z);
                            luckyBall.setTranslateY(luckyBall.getTranslateY() - luckyBall.getVelocity().y);
                            luckyBall.setTranslateX(luckyBall.getTranslateX() - luckyBall.getVelocity().x);

                            //get second vector pointing to y-axis and rotate it
                            Vec3d distanceCentreRotated = new Vec3d(luckyBall.getTranslateX(), 0, luckyBall.getTranslateZ());
                            rotateVecY(distanceCentreRotated, wheel.getRotationSpeed());

                            //calc vector which describes the change on the ball's velocity
                            distanceCentreRotated.sub(distanceCentre);
                            distanceCentreRotated.normalize();
                            distanceCentreRotated.mul(wheel.getRotationSpeed() * WHEEL_MOMENTUM);

                            //apply the change
                            luckyBall.getVelocity().add(distanceCentreRotated);
                            reflectVecOnGround(luckyBall.getVelocity());

                            luckyBall.getVelocity().y *= VERTICAL_BOUNCINESS;
                            luckyBall.getVelocity().z *= COLLISION_REDUCTION;
                            luckyBall.getVelocity().x *= COLLISION_REDUCTION;
                        }

                    }
                    return 0;

                } else {
                    if (luckyBall == null)
                        logger.log(Level.SEVERE, "No luckyBall found, skipping!");

                    if (wheel == null)
                        logger.log(Level.SEVERE, "No luckyWheel found, skipping!");

                    if (wheel.getChildren().isEmpty())
                        logger.log(Level.SEVERE, "No elements in the luckyWheel found, skipping!");

                    return 1;
                    // TODO frame-check
                }
            }
        } else {
            logger.log(Level.WARNING, "LuckyPhysics asked to tick 0 units, skipping!");
            return 1;
        }
        return 0;
    }

    public int spin() {
        //TODO: proper start
        luckyBall.setVelocity(new Vec3d(-1, 0, -1));
        luckyBall.setTranslateX(LuckyPlayground.WHEEL_RADIUS * 0.5);
        luckyBall.setTranslateZ(LuckyPlayground.WHEEL_RADIUS * 0.5);
        luckyBall.setTranslateY(400);
        wheel.setRotationSpeed(wheel.getRotationSpeed() + BASE_WHEEL_SPEED);
        logger.log(Level.INFO, "Spin started!");
        return 0;
    }

    public int reset() {
        //TODO: reset
        logger.log(Level.INFO, "LuckyBall reset!");
        return 0;
    }


    /**
     * helper function to rotate a vector around the y-axis
     */
    private void rotateVecY(Vec3d vec, double angle) {
        double tmpx = vec.x;
        vec.x = vec.x * Math.cos(angle) + vec.z * Math.sin(angle);
        vec.z = -tmpx * Math.sin(angle) + vec.z * Math.cos(angle);
    }

    /**
     * reflects a vector on the floor
     */
    private void reflectVecOnGround(Vec3d input) {
        Vec3d tmpVec = new Vec3d(input);

        //represents a normal-vector relative to the wheel
        Vec3d tmpVecNormal = new Vec3d(0, 1, 0);

        //calculate reflected vector
        double scalar = tmpVec.dot(tmpVecNormal);
        scalar *= 2;
        tmpVecNormal.mul(scalar);
        input.sub(tmpVecNormal);
    }
}