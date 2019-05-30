package de.glueckscrew.gluecksroulette.playground;

import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKeyHandler;
import de.glueckscrew.gluecksroulette.models.LuckyStudent;
import de.glueckscrew.gluecksroulette.physics.LuckyPhysics;
import javafx.animation.AnimationTimer;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Sebastian Schmitt
 */
public class LuckyPlayground extends SubScene {
    public static final int WHEEL_RADIUS = 400;
    private PerspectiveCamera camera;

    private AnimationTimer timer;

    public LuckyPlayground(LuckyConfig config) {
        super(new Group(), config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT), true, SceneAntialiasing.BALANCED);
        setFill(Color.BLACK);


        camera = new PerspectiveCamera(false);
        camera.getTransforms().add(new Rotate(-25, Rotate.X_AXIS));
        camera.setTranslateY(100);

        setCamera(camera);

        camera.setTranslateX(-WHEEL_RADIUS);


        Group rootGroup = (Group) getRoot();


        PointLight light = new PointLight(Color.WHITE);
        light.setPickOnBounds(true);
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(-150);
        rootGroup.getChildren().add(light);


        LuckySphere sphere = new LuckySphere();
        sphere.setTranslateX(WHEEL_RADIUS * 0.9);
        sphere.setTranslateY(460);
        sphere.setTranslateZ(0);
        sphere.setRadius(10);
        rootGroup.getChildren().add(sphere);

        ArrayList<LuckyStudent> texts = new ArrayList<LuckyStudent>() {{
            add(new LuckyStudent("Dave"));
            add(new LuckyStudent("Dennis Willers"));
            add(new LuckyStudent("Dominique Lasserre"));
            add(new LuckyStudent("Florian Dahlitz"));
            add(new LuckyStudent("Jakob Naucke"));
            add(new LuckyStudent("Jan Maier", 0.5));
            add(new LuckyStudent("Jan Niklas Fichte", 0.5));
            add(new LuckyStudent("Johann Becker", 0.5));
            add(new LuckyStudent("Justus-Jonas Erker", 0.5));
            add(new LuckyStudent("Marianne Huber", 0.5));
            add(new LuckyStudent("Marvin Kaiser", 0.5));
            add(new LuckyStudent("Moritz Heinz", 0.5));
            add(new LuckyStudent("Paul Weißer", 0.5));
            add(new LuckyStudent("Sebastian Schmitt", 0.5));
            add(new LuckyStudent("Tim Leuschner", 0.5));
        }};


        Group wheel = new Group();

        List<LuckyStudentSegment> segments = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            LuckyStudentSegment segment = new LuckyStudentSegment(texts.get(i), i, texts.size());
            segments.add(segment);

            wheel.getChildren().add(segment);
        }

        rootGroup.getChildren().addAll(segments);


        LuckyPhysics physics = LuckyPhysics.getInstance();
        physics.setFrame(new Group());
        physics.setWheel(wheel);
        physics.setSphere(sphere);



        new AnimationTimer() {
            long lastUpdate = 0;

            public void handle(long currentNanoTime) {
                // TODO adjust.
                if (currentNanoTime - lastUpdate < 14_000_000) {
                    return;
                }
                lastUpdate = currentNanoTime;

                physics.tick();
            }

         };//.start();


        sphere.getTransforms().add(new Rotate(0, -WHEEL_RADIUS * 0.9 , 0, 0, Rotate.Y_AXIS));
        segments.forEach(segment -> segment.getTransforms().add(new Rotate(0, 0, 0, 0, Rotate.Y_AXIS)));

        timer = new AnimationTimer() {
            private Random random = new Random();
            double start_speed = 10;
            double reduction = 0;
            double reduction_factor = 0.008;
            double min_speed = 0.02;
            double start_speed_wheel = 1;
            double reduction_wheel = 0;
            double reduction_factor_wheel = 0;

            long lastUpdate = 0;

            @Override
            public void start() {
                reduction_wheel = 0;
                reduction = 0;

                super.start();
            }

            public void handle(long currentNanoTime) {
                if (currentNanoTime - lastUpdate < 14_000_000) {
                    return;
                }

                lastUpdate = currentNanoTime;
                double speed = start_speed - reduction;
                double speed_wheel = start_speed_wheel - reduction_wheel;

                Rotate rotate = (Rotate) sphere.getTransforms().get(0);

                if (speed > min_speed) {
                    reduction += speed * reduction_factor;

                    rotate.setAngle(rotate.getAngle() - (speed - speed_wheel));
                } else if (speed_wheel > min_speed) {
                    rotate.setAngle(rotate.getAngle() + speed_wheel);
                }
                if (speed_wheel > min_speed) {
                    reduction_wheel += speed_wheel * reduction_factor_wheel;

                    segments.forEach(segment -> {
                        Rotate rot = (Rotate) segment.getTransforms().get(0);
                        rot.setAngle(rot.getAngle() + speed_wheel);
                    });
                }
            }

        };
        timer.start();
    }

    public void spin() {
        timer.stop();
        timer.start();
    }
}