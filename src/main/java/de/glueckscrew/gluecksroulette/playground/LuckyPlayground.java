package de.glueckscrew.gluecksroulette.playground;

import de.glueckscrew.gluecksroulette.LuckyMode;
import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import de.glueckscrew.gluecksroulette.models.LuckyCourse;
import de.glueckscrew.gluecksroulette.models.LuckyStudent;
import de.glueckscrew.gluecksroulette.physics.LuckyPhysics;
import de.glueckscrew.gluecksroulette.physics.LuckyPhysicsListener;
import javafx.animation.AnimationTimer;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sebastian Schmitt, Dominique Lasserre
 */
public class LuckyPlayground extends SubScene implements LuckyPhysicsListener {
    private static final Logger LOGGER = Logger.getLogger(LuckyPlayground.class.getSimpleName());

    public static final int WHEEL_RADIUS = 400;
    public static final double COLON_RADIUS = 0.25d * WHEEL_RADIUS;

    private static final long MIN_TICK_DURATION = TimeUnit.MILLISECONDS.toNanos(9);
    private static final int CAMERA_DEFAULT_X = -WHEEL_RADIUS;
    private static final int CAMERA_DEFAULT_Y = 150;
    private static final int CAMERA_DEFAULT_Z = 0;

    private static final double WHEEL_DEFAULT_Y = 470.5;

    private static final LuckyCourse DUMMY_COURSE = new LuckyCourse("", new ArrayList<LuckyStudent>() {{
        add(new LuckyStudent("lucky student 1"));
        add(new LuckyStudent("lucky student 2"));
        add(new LuckyStudent("lucky student 3"));
    }});

    private LuckyPhysics physics;
    private LuckyWheel wheel;
    private LuckyBall ball;

    private LuckyConfig config;

    private List<LuckyStudentSegment> segments;

    @Setter
    private LuckyPlaygroundListener listener;

    @Getter
    private LuckyCourse currentCourse;

    private LuckyStudentSegment lastChangedSegment;
    private double lastProbabilityChange;

    public LuckyPlayground(LuckyConfig config) {
        super(new Group(), config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT), true, SceneAntialiasing.BALANCED);

        this.config = config;

        setFill(Color.BLACK);

        PerspectiveCamera camera = new PerspectiveCamera(false);
        camera.getTransforms().add(new Rotate(-25, Rotate.X_AXIS));
        setCamera(camera);

        camera.setTranslateX(CAMERA_DEFAULT_X +
                (config.getDefaultInt(LuckyConfig.Key.WINDOW_WIDTH) - config.getInt(LuckyConfig.Key.WINDOW_WIDTH)) / 2);
        camera.setTranslateY(CAMERA_DEFAULT_Y +
                (config.getDefaultInt(LuckyConfig.Key.WINDOW_HEIGHT) - config.getInt(LuckyConfig.Key.WINDOW_HEIGHT)) / 2);
        camera.setTranslateZ(CAMERA_DEFAULT_Z -
                (config.getDefaultInt(LuckyConfig.Key.WINDOW_HEIGHT) - config.getInt(LuckyConfig.Key.WINDOW_HEIGHT)) / 4);

        Group rootGroup = (Group) getRoot();

        PointLight light = new PointLight(Color.WHITE);
        light.setPickOnBounds(true);
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(0);
        rootGroup.getChildren().add(light);

        ball = LuckyBall.getInstance();
        ball.setTranslateX(WHEEL_RADIUS * 0.9);
        ball.setTranslateY(460);
        ball.setTranslateZ(0);
        ball.setRadius(10);
        rootGroup.getChildren().add(ball);

        wheel = LuckyWheel.getInstance();
        wheel.setTranslateY(WHEEL_DEFAULT_Y);
        rootGroup.getChildren().add(wheel);
        segments = new ArrayList<>();

        LuckyFrame frame = LuckyFrame.getInstance();
        frame.setInnerRadius(WHEEL_RADIUS);
        frame.setTranslateY(WHEEL_DEFAULT_Y - frame.getHeight() * .5);
        PhongMaterial frameMat = new PhongMaterial(LuckyStudentSegment.RED);
        frameMat.setSpecularColor(Color.WHITE);
        frame.setMaterial(frameMat);

        physics = LuckyPhysics.getInstance();
        physics.setWheel(wheel);
        physics.setBall(ball);
        physics.setFrame(frame);
        physics.setListener(this);

        rootGroup.getChildren().add(frame);

        LuckyCone cone = new LuckyCone(COLON_RADIUS);
        PhongMaterial coneMat = new PhongMaterial(LuckyStudentSegment.RED);
        coneMat.setSpecularColor(Color.WHITE);
        cone.setMaterial(coneMat);
        wheel.getChildren().add(cone);

        setCurrentCourse(DUMMY_COURSE);

        new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < MIN_TICK_DURATION) return;

                physics.tick();
                lastUpdate = now;
            }
        }.start();
    }

    public LuckyStudent getSelectedStudent() {
        if (lastChangedSegment == null) return null;
        return lastChangedSegment.getLuckyStudent();
    }

    @Override
    public void onBallStopped() {
        lastChangedSegment = getSegmentWithBall();

        if (config.getMode(LuckyConfig.Key.MODE) == LuckyMode.THINNING) {
            reduceSelected(true, 0);
        } else if (lastChangedSegment != null) {
            lastProbabilityChange = lastChangedSegment.getLuckyStudent().getWeight();
        }

        if (listener != null)
            listener.onSpinStop();
    }

    public boolean reduceSelected(boolean saveLast, int manualWeight) {
        if (lastChangedSegment == null) return false;
        if (physics.isSpinning()) return false;

        LuckyStudent student = lastChangedSegment.getLuckyStudent();
        if (saveLast) {
            lastProbabilityChange = currentCourse.reduce(student, 0);
        } else {
            currentCourse.reduce(student, manualWeight);
        }
        resizeSegments();
        turnSegmentToBall(lastChangedSegment);
        return true;
    }

    public boolean enlargeSelected(boolean saveLast, int manualWeight) {
        if (lastChangedSegment == null) return false;
        if (physics.isSpinning()) return false;

        LuckyStudent student = lastChangedSegment.getLuckyStudent();
        if (saveLast) {
            lastProbabilityChange = currentCourse.enlarge(student, 0);
        } else {
            currentCourse.enlarge(student, manualWeight);
        }
        resizeSegments();
        return true;
    }

    public void turnSegmentToBall(LuckyStudentSegment segment) {
        double startDeg = (segment.getOffset()) * 360;
        double endDeg = (segment.getOffset() + segment.getStep()) * 360;

        Rotate rotate = (Rotate) wheel.getTransforms().get(0);

        // set angle required to turn center of segment to where the ball is
        rotate.setAngle((360 + checkBallPosition() - (startDeg + endDeg) / 2) % 360);
    }

    private LuckyStudentSegment getSegmentWithBall() {
        // get wheel rotation
        Rotate rotate = (Rotate) wheel.getTransforms().get(0);
        // rotation angle along positive y
        double wheelDeg = rotate.getAngle();

        // get ball angle relative to wheel rotation
        // add 360 so we get positive (modulus) over possibly negative remainder
        double deg = (360 + checkBallPosition() - wheelDeg) % 360;

        for (LuckyStudentSegment segment : segments) {
            double startDeg = (segment.getOffset()) * 360;
            double endDeg = (segment.getOffset() + segment.getStep()) * 360;
            if (startDeg <= deg && deg < endDeg) {
                return segment;
            }
        }
        LOGGER.log(Level.SEVERE, "no segment containing the ball found, skipping!");
        return null;
    }

    private double checkBallPosition() {
        double ballX = ball.getTranslateX();
        double ballZ = ball.getTranslateZ();
        double rad = Math.atan2(ballZ, ballX);
        // resulting angle is from 0->pi and -pi->0, so make sure it goes the full circle 0->2pi
        if (rad < 0) {
            rad = 2*Math.PI + rad;
        }
        return 360 - Math.toDegrees(rad);  // return clockwise rotation angle
    }

    public void setCurrentCourse(LuckyCourse currentCourse) {
        this.currentCourse = currentCourse;

        wheel.getChildren().removeAll(segments);
        segments.clear();
        for (int i = 0; i < currentCourse.getStudents().size(); i++) {
            Color color = i % 2 == 0 ? LuckyStudentSegment.BLACK : LuckyStudentSegment.RED;
            if (i == currentCourse.getStudents().size() - 1 && currentCourse.getStudents().size() % 2 == 1)
                color = LuckyStudentSegment.GREEN;

            LuckyStudentSegment segment = new LuckyStudentSegment(currentCourse.getStudents().get(i), color);

            segments.add(segment);
            wheel.getChildren().add(segment);
        }


        resizeSegments();
    }

    private void resizeSegments() {
        double lowest = currentCourse.getStudents().stream().mapToDouble(LuckyStudent::getWeight).min().orElse(1);
        double sum = currentCourse.getStudents().stream().mapToDouble(LuckyStudent::getWeight).sum();

        double factor = 1 / (sum / lowest);
        double offset = 0;

        for (LuckyStudentSegment segment : segments) {
            double segmentStep = segment.getLuckyStudent().getWeight() / lowest * factor;

            segment.setStep(segmentStep);
            segment.setOffset(offset);
            segment.update();

            offset += segmentStep;
        }
    }

    public void softReset() {
        if (physics.isSpinning()) {
            physics.reset();
            return;
        }

        if (lastChangedSegment == null) return;

        LuckyStudent student = lastChangedSegment.getLuckyStudent();
        double oldWeight = student.getWeight();
        boolean turnToBall = oldWeight > lastProbabilityChange;
        lastProbabilityChange = currentCourse.setStudentWeight(student, lastProbabilityChange);
        resizeSegments();

        if (turnToBall) {
            turnSegmentToBall(lastChangedSegment);
        }
    }

    public void hardReset() {
        if (physics.isSpinning()) {
            physics.reset();
            return;
        }

        lastChangedSegment = null;
        currentCourse.resetWeights();
        resizeSegments();
    }

    public void spin() {
        physics.spin();
    }
}
