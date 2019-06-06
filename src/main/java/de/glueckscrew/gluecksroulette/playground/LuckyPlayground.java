package de.glueckscrew.gluecksroulette.playground;

import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import de.glueckscrew.gluecksroulette.models.LuckyCourse;
import de.glueckscrew.gluecksroulette.models.LuckyStudent;
import de.glueckscrew.gluecksroulette.physics.LuckyPhysics;
import javafx.animation.AnimationTimer;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Schmitt
 */
public class LuckyPlayground extends SubScene {
    public static final int WHEEL_RADIUS = 400;

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

        ball = new LuckyBall();
        ball.setTranslateX(WHEEL_RADIUS * 0.9);
        ball.setTranslateY(460);
        ball.setTranslateZ(0);
        ball.setRadius(10);
        rootGroup.getChildren().add(ball);

        wheel = new LuckyWheel();
        wheel.setTranslateY(WHEEL_DEFAULT_Y);
        rootGroup.getChildren().add(wheel);
        segments = new ArrayList<>();

        MeshView meshView = new MeshView(createToroidMesh(WHEEL_RADIUS, WHEEL_RADIUS * 0.05f));
        meshView.setDrawMode(DrawMode.FILL);
        meshView.setMaterial(new PhongMaterial(Color.WHITE));
        meshView.setTranslateY(470.5);
        meshView.setTranslateX(0);
        meshView.setTranslateZ(0);
        meshView.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));

        PhongMaterial mat = new PhongMaterial(Color.valueOf("#cd8500"));
        mat.setSpecularColor(Color.WHITE);
        meshView.setMaterial(mat);

        rootGroup.getChildren().add(meshView);

        physics = LuckyPhysics.getInstance();
        physics.setFrame(new Group());
        physics.setWheel(wheel);
        physics.setLuckyBall(ball);

        ball.getTransforms().add(new Rotate(0, -WHEEL_RADIUS * 0.9, 0, 0, Rotate.Y_AXIS));
        segments.forEach(segment -> segment.getTransforms().add(new Rotate(0, 0, 0, 0, Rotate.Y_AXIS)));

        setCurrentCourse(DUMMY_COURSE);
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
                if (now - lastUpdate < 14_000_000) return;

                physics.tick();
                lastUpdate = now;
            }
        }.start();
    }

    private void resizeSegments() {
        double lowest = currentCourse.getStudents().stream().mapToDouble(LuckyStudent::getProbability).min().orElse(1);
        double sum = currentCourse.getStudents().stream().mapToDouble(LuckyStudent::getProbability).sum();

        double factor = 1 / (sum / lowest);
        double offset = 0;

        for (LuckyStudentSegment segment : segments) {
            double segmentStep = segment.getLuckyStudent().getProbability() / lowest * factor;

            segment.setStep(segmentStep);
            segment.setOffset(offset);
            segment.update();

            offset += segmentStep;
        }
    }

    public void softReset() {
        if (lastChangedSegment == null) return;

        lastChangedSegment.getLuckyStudent().setProbability(
                lastChangedSegment.getLuckyStudent().getProbability() + lastProbabilityChange);

    }

    public void hardReset() {
        if (initState != null)
            setCurrentCourse(initState);
    }

    public void spin() {
        physics.spin();
    }


    /*
    Taken from https://stackoverflow.com/a/24565474

    Let the radius from the center of the hole to the center of the torus tube be "c",
    and the radius of the tube be "a".
    Then the equation in Cartesian coordinates for a torus azimuthally symmetric about the z-axis is
    (c-sqrt(x^2+y^2))^2+z^2=a^2
    and the parametric equations are
    x = (c + a * cos(v)) * cos(u)
    y = (c + a * cos(v)) * sin(u)
    z =  a * sin(v)
    (for u,v in [0,2pi).

    Three types of torus, known as the standard tori, are possible,
    depending on the relative sizes of a and c. c>a corresponds to the ring torus (shown above),
    c=a corresponds to a horn torus which is tangent to itself at the point (0, 0, 0),
    and c<a corresponds to a self-intersecting spindle torus (Pinkall 1986).
    */
    private static TriangleMesh createToroidMesh(float radius, float tRadius) {
        int tubeDivisions = 100, radiusDivisions = 100;

        int POINT_SIZE = 3, TEXCOORD_SIZE = 2, FACE_SIZE = 6;
        int numVerts = tubeDivisions * radiusDivisions;
        int faceCount = numVerts * 2;
        float[] points = new float[numVerts * POINT_SIZE],
                texCoords = new float[numVerts * TEXCOORD_SIZE];
        int[] faces = new int[faceCount * FACE_SIZE];

        int pointIndex = 0, texIndex = 0, faceIndex = 0;
        double tubeFraction = 1.0f / tubeDivisions;
        double radiusFraction = 1.0f / radiusDivisions;

        int p0, p1, p2, p3, t0, t1, t2, t3;

        // create points
        for (int tubeIndex = 0; tubeIndex < tubeDivisions; tubeIndex++) {

            double radian = tubeFraction * tubeIndex * 2.0f * Math.PI;

            for (int radiusIndex = 0; radiusIndex < radiusDivisions; radiusIndex++) {

                double localRadian = radiusFraction * radiusIndex * 2.0f * Math.PI;

                points[pointIndex] = (radius + tRadius * ((float) Math.cos(radian))) * ((float) Math.cos(localRadian));
                points[pointIndex + 1] = (radius + tRadius * ((float) Math.cos(radian))) * ((float) Math.sin(localRadian));
                points[pointIndex + 2] = (tRadius * (float) Math.sin(radian));

                pointIndex += 3;

                double r = radiusIndex < tubeDivisions ? tubeFraction * radiusIndex * 2.0F * Math.PI : 0.0f;
                texCoords[texIndex] = (0.5F + (float) (Math.sin(r) * 0.5D));
                ;
                texCoords[texIndex + 1] = ((float) (Math.cos(r) * 0.5D) + 0.5F);

                texIndex += 2;

            }

        }
        //create faces
        for (int point = 0; point < tubeDivisions; point++) {
            for (int crossSection = 0; crossSection < radiusDivisions; crossSection++) {
                p0 = point * radiusDivisions + crossSection;
                p1 = p0 >= 0 ? p0 + 1 : p0 - radiusDivisions;
                p1 = p1 % radiusDivisions != 0 ? p0 + 1 : p0 - (radiusDivisions - 1);
                p2 = (p0 + radiusDivisions) < ((tubeDivisions * radiusDivisions)) ? p0 + radiusDivisions : p0 - (tubeDivisions * radiusDivisions) + radiusDivisions;
                p3 = p2 < ((tubeDivisions * radiusDivisions) - 1) ? p2 + 1 : p2 - (tubeDivisions * radiusDivisions) + 1;
                p3 = p3 % (radiusDivisions) != 0 ? p2 + 1 : p2 - (radiusDivisions - 1);

                t0 = point * radiusDivisions + crossSection;
                t1 = t0 >= 0 ? t0 + 1 : t0 - radiusDivisions;
                t1 = t1 % radiusDivisions != 0 ? t0 + 1 : t0 - (radiusDivisions - 1);
                t2 = (t0 + radiusDivisions) < ((tubeDivisions * radiusDivisions)) ? t0 + radiusDivisions : t0 - (tubeDivisions * radiusDivisions) + radiusDivisions;
                t3 = t2 < ((tubeDivisions * radiusDivisions) - 1) ? t2 + 1 : t2 - (tubeDivisions * radiusDivisions) + 1;
                t3 = t3 % (radiusDivisions) != 0 ? t2 + 1 : t2 - (radiusDivisions - 1);

                try {
                    faces[faceIndex] = (p2);
                    faces[faceIndex + 1] = (t3);
                    faces[faceIndex + 2] = (p0);
                    faces[faceIndex + 3] = (t2);
                    faces[faceIndex + 4] = (p1);
                    faces[faceIndex + 5] = (t0);

                    faceIndex += FACE_SIZE;

                    faces[faceIndex] = (p2);
                    faces[faceIndex + 1] = (t3);
                    faces[faceIndex + 2] = (p1);
                    faces[faceIndex + 3] = (t0);
                    faces[faceIndex + 4] = (p3);
                    faces[faceIndex + 5] = (t1);
                    faceIndex += FACE_SIZE;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        TriangleMesh localTriangleMesh = new TriangleMesh();
        localTriangleMesh.getPoints().setAll(points);
        localTriangleMesh.getTexCoords().setAll(texCoords);
        localTriangleMesh.getFaces().setAll(faces);


        return localTriangleMesh;
    }
}