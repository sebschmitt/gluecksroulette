package de.glueckscrew.gluecksroulette.playground;

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
import java.util.concurrent.ThreadLocalRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Sebastian Schmitt
 */
public class LuckyPlayground extends SubScene implements LuckyPhysicsListener {
    public static final int WHEEL_RADIUS = 400;
    public static final double COLON_RADIUS = 0.25d * WHEEL_RADIUS;

    private static final long MIN_TICK_DURATION = TimeUnit.MILLISECONDS.toNanos(14);
    private static final int CAMERA_DEFAULT_X = -WHEEL_RADIUS;
    private static final int CAMERA_DEFAULT_Y = 150;
    private static final int CAMERA_DEFAULT_Z = 0;

    private static final double WHEEL_DEFAULT_Y = 470.5;

    private static final LuckyCourse DUMMY_COURSE = new LuckyCourse("", new ArrayList<LuckyStudent>() {{
        add(new LuckyStudent("lucky roulette"));
        add(new LuckyStudent("lucky roulette"));
        add(new LuckyStudent("lucky roulette"));
    }});

    private LuckyPhysics physics;
    private LuckyWheel wheel;
    private LuckyBall ball;

    private List<LuckyStudentSegment> segments;
    private LuckyStudentSegment activeSegment;

    @Getter private LuckyCourse currentCourse;
    private LuckyCourse initState;

    private LuckyStudentSegment lastChangedSegment;
    private double lastProbabilityChange;

    public LuckyPlayground(LuckyConfig config) {
        super(new Group(), config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT), true, SceneAntialiasing.BALANCED);

        setFill(Color.BLACK);

        PerspectiveCamera camera = new PerspectiveCamera(false);
        camera.getTransforms().add(new Rotate(-25, Rotate.X_AXIS));
        setCamera(camera);

        camera.setTranslateX(CAMERA_DEFAULT_X +
                (config.getDefaultInt(LuckyConfig.Key.WINDOW_WIDTH) -   config.getInt(LuckyConfig.Key.WINDOW_WIDTH)) / 2);
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

        physics = LuckyPhysics.getInstance();
        physics.setFrame(new Group());
        physics.setWheel(wheel);
        physics.setLuckyBall(ball);
        physics.setListener(this);


        LuckyFrame frame = new LuckyFrame(WHEEL_RADIUS);
        PhongMaterial frameMat = new PhongMaterial(LuckyStudentSegment.RED);
        frameMat.setSpecularColor(Color.WHITE);
        frame.setMaterial(frameMat);
        //wheel.getChildren().add(frame);

        LuckyCone cone = new LuckyCone(COLON_RADIUS);
        PhongMaterial coneMat = new PhongMaterial(LuckyStudentSegment.RED);
        coneMat.setSpecularColor(Color.WHITE);
        cone.setMaterial(coneMat);
        wheel.getChildren().add(cone);

        setCurrentCourse(DUMMY_COURSE);
    }

    @Override
    public void onBallStopped() {
        activeSegment = getSegmentWithBall();
        // set new probability p for selected student to p/n, where n is size of course
        activeSegment.getLuckyStudent().setProbability(activeSegment.getLuckyStudent().getProbability()/segments.size());
        resizeSegments();
    }

    private LuckyStudentSegment getSegmentWithBall() {
        return segments.get(ThreadLocalRandom.current().nextInt(0, segments.size()));
    }

    public void setCurrentCourse(LuckyCourse currentCourse) {
        this.currentCourse = currentCourse;
        this.initState = currentCourse.clone();

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
        if (lastChangedSegment == null) return;

        lastChangedSegment.getLuckyStudent().setWeight(
                lastChangedSegment.getLuckyStudent().getWeight() + lastProbabilityChange);

    }

    public void hardReset() {
        if (initState != null)
            setCurrentCourse(initState);
    }

    public void spin() {
        physics.spin();
    }
}
