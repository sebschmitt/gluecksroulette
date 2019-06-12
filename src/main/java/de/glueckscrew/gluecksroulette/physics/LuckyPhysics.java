package de.glueckscrew.gluecksroulette.physics;

import com.sun.javafx.geom.Vec3d;
import de.glueckscrew.gluecksroulette.playground.LuckyBall;
import de.glueckscrew.gluecksroulette.playground.LuckyFrame;
import de.glueckscrew.gluecksroulette.playground.LuckyPlayground;
import de.glueckscrew.gluecksroulette.playground.LuckyWheel;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;
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
    private static final Logger LOGGER = Logger.getLogger(LuckyPhysics.class.getSimpleName());
    private static LuckyPhysics instance;


    //physics constants
    private final static double COLLISION_REDUCTION = .7;
    private final static double VERTICAL_BOUNCINESS = .5;
    private final static double WHEEL_ROTATION_REDUCTION = .025;
    private final static double GRAVITY = .1;
    private final static double MINIMAL_WHEEL_ROTATION = .001;
    private final static double WHEEL_MOMENTUM = .8;
    private final static double BASE_WHEEL_SPEED = 7.5;
    private final static double MINIMAL_BALL_SPEED = GRAVITY * 4; //smallest speed the ball should reach is one gravity-tick
    private final static double MAX_BALL_SPEED = 30;
    private final static double MINIMAL_BALL_STARTING_SPEED = 10;
    private final static double MAX_WHEEL_SPEED = 4 * BASE_WHEEL_SPEED;

    private final static int TICKS_UNTIL_BALL_COUNTS_AS_STOPPED = 15;

    @Setter
    private LuckyFrame frame;
    @Setter
    private LuckyWheel wheel;
    @Setter
    private LuckyBall ball;

    @Setter
    private LuckyPhysicsListener listener;
    @Getter
    private boolean spinning;

    //private constructor to prevent a second instantiation
    private LuckyPhysics() {
        this.spinning = false;
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

    private static int cntTicksBallBelowMinSpeed = 0;

    public int tick(int steps) {

        if (steps <= 0) {
            LOGGER.log(Level.WARNING, "LuckyPhysics asked to tick 0 units, skipping!");
            return 1;
        }
        if (ball == null) {
            LOGGER.log(Level.SEVERE, "No ball found, skipping!");
            return 1;
        }
        if (wheel == null) {
            LOGGER.log(Level.SEVERE, "No wheel found, skipping!");
            return 1;
        }
        if (wheel.getChildren().isEmpty()) {
            LOGGER.log(Level.SEVERE, "No elements in the luckyWheel found, skipping!");
            return 1;
        }
        if (frame == null) {
            LOGGER.log(Level.SEVERE, "No frame found, skipping!");
            return 1;
        }

        //execute the ticks
        for (int i = 0; i < steps; i++) {
            //check if the ball was below its min speed for 3 ticks, if it was set its velocity to 0
            //this prevents the ball from "wobbeling" forever
            cntTicksBallBelowMinSpeed = ball.getVelocity().length() <= MINIMAL_BALL_SPEED ? cntTicksBallBelowMinSpeed + 1 : 0;
            if (cntTicksBallBelowMinSpeed >= TICKS_UNTIL_BALL_COUNTS_AS_STOPPED) {
                ball.getVelocity().x = 0;
                ball.getVelocity().y = 0;
                ball.getVelocity().z = 0;

                if (this.spinning) {
                    this.spinning = false;
                    if (listener != null) listener.onBallStopped();
                }
            }


            //move the ball
            ball.setTranslateY(ball.getTranslateY() + ball.getVelocity().y);
            ball.setTranslateX(ball.getTranslateX() + ball.getVelocity().x);
            ball.setTranslateZ(ball.getTranslateZ() + ball.getVelocity().z);

            //rotate the wheel, unless its rotating slower than minimum-rotation-speed
            if (wheel.getRotationSpeed() > MINIMAL_WHEEL_ROTATION) {
                wheel.setRotationSpeed(wheel.getRotationSpeed() - WHEEL_ROTATION_REDUCTION);
                if (wheel.getTransforms().isEmpty())
                    wheel.getTransforms().add(new Rotate(0, 0, 0, 0, Rotate.Y_AXIS));
                try {
                    Rotate rotate = (Rotate) wheel.getTransforms().get(0);
                    rotate.setAngle((rotate.getAngle() + wheel.getRotationSpeed()) % 360);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not find/apply a transform in wheel, skipping! Full trace back: %n", e);
                }
            } else {
                wheel.setRotationSpeed(0);
            }


            //apply gravity
            ball.getVelocity().y += GRAVITY;

            //distance of the ball to the center
            Vec3d distanceCenter = new Vec3d(ball.getTranslateX(), 0, ball.getTranslateZ());
            double distanceCenterD = distanceCenter.length() - ball.getRadius();

            //check for all possible intersections
            boolean collidesBaseWheel = LuckyPlayground.WHEEL_RADIUS > distanceCenterD
                    && wheel.getTranslateY() <= ball.getTranslateY() + ball.getRadius()
                    && !(distanceCenterD < LuckyPlayground.COLON_RADIUS);

            boolean collidesCenterColon = distanceCenterD <= LuckyPlayground.COLON_RADIUS && ballCollidesCenterColon();

            boolean collidesFrame = !collidesCenterColon
                    && distanceCenterD >= LuckyPlayground.WHEEL_RADIUS - ball.getRadius() * 2
                    && ballCollidesFrame();
            boolean collidesBorder = distanceCenterD > LuckyPlayground.WHEEL_RADIUS + frame.getHeight() - ball.getRadius();
            System.out.println();

            boolean collides = collidesBaseWheel || collidesBorder || collidesFrame || collidesCenterColon;

            //revert the last step
            if (collides) {
                ball.setTranslateZ(ball.getTranslateZ() - ball.getVelocity().z);
                ball.setTranslateY(ball.getTranslateY() - ball.getVelocity().y);
                ball.setTranslateX(ball.getTranslateX() - ball.getVelocity().x);
            }

            //execute matching intersection-function
            if (collidesFrame)
                reflectLuckyBallVelocityOnFrame();
            if (collidesBorder)
                reflectLuckyBallVelocityOnBorder();
            if (collidesCenterColon)
                reflectLuckyBallVelocityOnCenterColon();
            if (collidesBaseWheel)
                reflectLuckyBallVelocityOnGround();

            if (collidesBaseWheel || collidesCenterColon)
                addMomentumToBall();

            //reduce balls velocity by the matching cost
            if (collides) {
                ball.getVelocity().y *= VERTICAL_BOUNCINESS;
                ball.getVelocity().z *= COLLISION_REDUCTION;
                ball.getVelocity().x *= COLLISION_REDUCTION;
            }

            //under massively unlucky circumstances, the ball can clip through the frame and fall into oblivion
            //this resets its position, so the user does not have to restart the entire program
            if (ball.getTranslateY() - ball.getRadius() * 3 > wheel.getTranslateY()) {
                ball.setTranslateX(0);
                ball.setTranslateY(wheel.getTranslateY() - LuckyPlayground.COLON_RADIUS - ball.getRadius() * 5);
                ball.setTranslateZ(0);
            }
        }
        return 0;
    }

    public int spin() {
        //generates random Values for the x- and z-velocity of the ball and set it
        Random r = new Random();
        double randomValue = MINIMAL_BALL_STARTING_SPEED + (MAX_BALL_SPEED - MINIMAL_BALL_STARTING_SPEED) * r.nextDouble();
        boolean randomBoolean = r.nextBoolean();

        ball.getVelocity().x = randomBoolean ? randomValue : -randomValue;

        randomValue = MINIMAL_BALL_STARTING_SPEED + (MAX_BALL_SPEED - MINIMAL_BALL_STARTING_SPEED) * r.nextDouble();
        randomBoolean = r.nextBoolean();
        ball.getVelocity().z = randomBoolean ? randomValue : -randomValue;


        //set fixed wheel rotation speed
        wheel.setRotationSpeed(wheel.getRotationSpeed() + BASE_WHEEL_SPEED > MAX_WHEEL_SPEED ? MAX_WHEEL_SPEED : wheel.getRotationSpeed() + BASE_WHEEL_SPEED);
        this.spinning = true;
        LOGGER.log(Level.INFO, "Spin started!");
        return 0;
    }

    /**
     * stops moving objects and resets the balls position
     */

    public int reset() {
        cntTicksBallBelowMinSpeed = TICKS_UNTIL_BALL_COUNTS_AS_STOPPED;
        ball.setVelocity(new Vec3d(0, 0, 0));
        ball.setTranslateX(0);
        ball.setTranslateZ(0);
        ball.setTranslateY(wheel.getTranslateY() - LuckyPlayground.COLON_RADIUS - ball.getRadius() * 3);
        wheel.setRotationSpeed(0);
        this.spinning = false;

        LOGGER.log(Level.INFO, "LuckyBall reset!");
        return 0;
    }

    /**
     * adds momentum to the ball based on the wheels rotation speed
     */
    private void addMomentumToBall() {
        Vec3d distanceCenter = new Vec3d(ball.getTranslateX(), 0, ball.getTranslateZ());
        //get second vector pointing from y-axis to the balls xz-pos and rotate it
        Vec3d distanceCenterRotated = new Vec3d(ball.getTranslateX(), -.0001, ball.getTranslateZ());
        rotateVecY(distanceCenterRotated, wheel.getRotationSpeed());

        //calc vector which describes the change on the ball's velocity
        distanceCenterRotated.sub(distanceCenter);
        distanceCenterRotated.normalize();
        distanceCenterRotated.mul(wheel.getRotationSpeed() * WHEEL_MOMENTUM);

        //apply the change
        ball.getVelocity().add(distanceCenterRotated);
    }


    /**
     * helper function to rotate a vector around the y-axis
     */
    private void rotateVecY(Vec3d vec, double angle) {
        double tmpx = vec.x;
        vec.x = vec.x * Math.cos(Math.toRadians(angle)) + vec.z * Math.sin(Math.toRadians(angle));
        vec.z = -tmpx * Math.sin(Math.toRadians(angle)) + vec.z * Math.cos(Math.toRadians(angle));
    }

    /**
     * reflects a ball velocity on the outer border
     */

    private void reflectLuckyBallVelocityOnBorder() {
        Vec3d normal = new Vec3d(-ball.getTranslateX(), 0, -ball.getTranslateZ());
        normal.normalize();
        reflectVec(ball.getVelocity(), normal);
    }

    /**
     * reflects a ball velocity on the floor
     */
    private void reflectLuckyBallVelocityOnGround() {
        ball.getVelocity().y *= -1;
    }

    /**
     * reflects ball on the centre colon, provides that its angle is 45 deg
     */

    private void reflectLuckyBallVelocityOnCenterColon() {
        //vec pointing up on the y-axis
        Vec3d tmpVec = new Vec3d(0, -1, 0);

        //base-edge of the triangle
        Vec3d normal = new Vec3d(ball.getTranslateX(), 0, ball.getTranslateZ());
        normal.normalize();

        //this should get normal vector relative to the hypotenuse
        normal.add(tmpVec);
        normal.normalize();

        reflectVec(ball.getVelocity(), normal);
    }

    /**
     * reflects ball on the frame, provides that its angle is 45 deg
     */
    private void reflectLuckyBallVelocityOnFrame() {
        //vec pointing up on the y-axis
        Vec3d tmpVec = new Vec3d(0, -1, 0);

        //base-edge of the triangle
        Vec3d normal = new Vec3d(-ball.getTranslateX(), 0, -ball.getTranslateZ());
        normal.normalize();

        //this should get normal vector relative to the hypotenuse
        normal.add(tmpVec);
        normal.normalize();

        reflectVec(ball.getVelocity(), normal);
    }

    /**
     * reflects vec input on vec normal
     */

    private void reflectVec(Vec3d input, Vec3d normal) {
        Vec3d tmpVec = new Vec3d(input);

        //calculate reflected vector
        double scalar = tmpVec.dot(normal);
        scalar *= 2;
        normal.mul(scalar);
        input.sub(normal);
    }

    /**
     * check if ball intersects the center colon of the wheel
     * generates a ray which represents the closest line of the colon to the ball
     */
    private boolean ballCollidesCenterColon() {
        Vec3d upperCorner = new Vec3d(0, wheel.getTranslateY() - LuckyPlayground.COLON_RADIUS, 0);

        Vec3d lowerCorner = new Vec3d(ball.getTranslateX(), 0, ball.getTranslateZ());
        lowerCorner.normalize();
        lowerCorner.mul(LuckyPlayground.COLON_RADIUS);
        lowerCorner.y = wheel.getTranslateY();

        Vec3d d = new Vec3d(lowerCorner);
        d.sub(upperCorner);
        Vec3d oc = new Vec3d(upperCorner);
        oc.sub(new Vec3d(ball.getTranslateX(), ball.getTranslateY(), ball.getTranslateZ()));

        return sphereCollidesRay(d, oc);
    }

    /**
     * calculates if the ball collides the frame, analog to ballCollidesCenterColon
     **/

    private boolean ballCollidesFrame() {
        Vec3d upperCorner = new Vec3d(ball.getTranslateX(), 0, ball.getTranslateZ());
        upperCorner.normalize();
        upperCorner.mul(LuckyPlayground.WHEEL_RADIUS + frame.getHeight());
        upperCorner.y = wheel.getTranslateY() - frame.getHeight();

        Vec3d lowerCorner = new Vec3d(ball.getTranslateX(), 0, ball.getTranslateZ());
        lowerCorner.normalize();
        lowerCorner.mul(LuckyPlayground.WHEEL_RADIUS);
        lowerCorner.y = wheel.getTranslateY();

        Vec3d d = new Vec3d(lowerCorner);
        d.sub(upperCorner);
        Vec3d oc = new Vec3d(upperCorner);
        oc.sub(new Vec3d(ball.getTranslateX(), ball.getTranslateY(), ball.getTranslateZ()));

        return sphereCollidesRay(d, oc);
    }


    /**
     * function to check if a sphere collides a ray,
     * Vec3d originToCenter should point from the origin of the ray to the center of the ball
     * Vec3d directionRay points from the origin towards its destination
     */
    private boolean sphereCollidesRay(Vec3d directionRay, Vec3d originToCenter) {
        double a = directionRay.dot(directionRay);
        double b = 2.0 * originToCenter.dot(directionRay);
        double c = originToCenter.dot(originToCenter) - ball.getRadius() * ball.getRadius();
        double discriminant = b * b - 4 * a * c;
        return (discriminant >= 0);
    }


}
