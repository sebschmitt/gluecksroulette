package de.glueckscrew.gluecksroulette.playground;

import de.glueckscrew.gluecksroulette.models.LuckyStudent;
import de.glueckscrew.gluecksroulette.util.LuckyTextUtil;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sebastian Schmitt
 */
public class LuckyStudentSegment extends Group {
    public static final Color RED = Color.valueOf("#e74c3c");
    public static final Color BLACK = Color.valueOf("#2c3e50");
    public static final Color GREEN = Color.valueOf("#2ecc71");

    private static final Color TEXT_COLOR = Color.valueOf("#ecf0f1");

    @Getter
    private LuckyStudent luckyStudent;

    @Setter
    private double offset;
    @Setter
    private double step;
    @Setter
    private Color color;
    private Text text;
    @Getter
    private MeshView meshView;

    public LuckyStudentSegment(LuckyStudent luckyStudent, Color color) {
        this.luckyStudent = luckyStudent;
        this.color = color;

        text = new Text();
        text.setTranslateY(-1.5);
        text.setFont(new Font(37));
        text.setFill(TEXT_COLOR);
        text.setSmooth(true);
        getChildren().add(text);

        meshView = new MeshView();
        meshView.setDrawMode(DrawMode.FILL);
        meshView.setTranslateY(0);
        meshView.setTranslateX(0);
        meshView.setTranslateZ(0);
        getChildren().add(meshView);

        update();
    }

    public void update() {
        text.setText(luckyStudent.getName());

        Pair<Double, Double> position = calculatePosition(offset + step, 0.9 - LuckyTextUtil.getTextLength(text) / (double) LuckyPlayground.WHEEL_RADIUS);
        text.setTranslateX(position.getKey());
        text.setTranslateZ(position.getValue());

        text.getTransforms().clear();
        text.getTransforms().add(new Rotate((offset + step) * 360, Rotate.Y_AXIS));
        text.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));


        meshView.setMesh(createMesh(step * 2));

        meshView.getTransforms().clear();
        meshView.getTransforms().add(new Rotate((offset * 360) + 90, Rotate.Y_AXIS));

        PhongMaterial mat = new PhongMaterial(color);
        mat.setSpecularColor(Color.WHITE);
        meshView.setMaterial(mat);
    }


    private static Pair<Double, Double> calculatePosition(double step, double length) {
        double t = ((-step) * 360) / 180 * Math.PI;
        double x = Math.cos(t) * LuckyPlayground.WHEEL_RADIUS * length;
        double y = Math.sin(t) * LuckyPlayground.WHEEL_RADIUS * length;

        return new Pair<>(x, y);
    }

    private static TriangleMesh createMesh(double segmentSize) {
        float h = 1;
        int div = 200;
        float r = LuckyPlayground.WHEEL_RADIUS;

        final int nPonits = div * 2 + 2;
        final int tcCount = (div + 1) * 4 + 1; // 2 cap tex
        final int faceCount = div * 4;

        float textureDelta = 1.f / 256;

        float dA = 1.f / div;
        h *= .5f;

        float[] points = new float[nPonits * 3];
        float[] tPoints = new float[tcCount * 2];
        int[] faces = new int[faceCount * 6];
        int[] smoothing = new int[faceCount];

        int pPos = 0, tPos = 0;

        for (int i = 0; i < div; ++i) {
            double a = dA * i * Math.PI * segmentSize;

            points[pPos] = (float) (Math.sin(a) * r);
            points[pPos + 2] = (float) (Math.cos(a) * r);
            points[pPos + 1] = h;
            tPoints[tPos] = 1 - dA * i;
            tPoints[tPos + 1] = 1 - textureDelta;
            pPos += 3;
            tPos += 2;
        }

        // top edge
        tPoints[tPos] = 0;
        tPoints[tPos + 1] = 1 - textureDelta;
        tPos += 2;

        for (int i = 0; i < div; ++i) {
            double a = dA * i * Math.PI * segmentSize;
            points[pPos] = (float) (Math.sin(a) * r);
            points[pPos + 2] = (float) (Math.cos(a) * r);
            points[pPos + 1] = -h;
            tPoints[tPos] = 1 - dA * i;
            tPoints[tPos + 1] = textureDelta;
            pPos += 3;
            tPos += 2;
        }

        // bottom edge
        tPoints[tPos] = 0;
        tPoints[tPos + 1] = textureDelta;
        tPos += 2;

        // add cap central points
        points[pPos] = 0;
        points[pPos + 1] = h;
        points[pPos + 2] = 0;
        points[pPos + 3] = 0;
        points[pPos + 4] = -h;
        points[pPos + 5] = 0;

        // add cap central points
        // bottom cap
        for (int i = 0; i <= div; ++i) {
            double a = (i < div) ? (dA * i * segmentSize) * Math.PI : 0;
            tPoints[tPos] = (float) (Math.sin(a) * 0.5f) + 0.5f;
            tPoints[tPos + 1] = (float) (Math.cos(a) * 0.5f) + 0.5f;
            tPos += 2;
        }

        // top cap
        for (int i = 0; i <= div; ++i) {
            double a = (i < div) ? (dA * i * segmentSize) * Math.PI : 0;
            tPoints[tPos] = 0.5f + (float) (Math.sin(a) * 0.5f);
            tPoints[tPos + 1] = 0.5f - (float) (Math.cos(a) * 0.5f);
            tPos += 2;
        }

        tPoints[tPos] = .5f;
        tPoints[tPos + 1] = .5f;

        int fIndex = 0;

        // build body faces
        for (int p0 = 0; p0 < div; ++p0) {
            int p1 = p0 + 1;
            int p2 = p0 + div;
            int p3 = p1 + div;

            // add p0, p1, p2
            faces[fIndex] = p0;
            faces[fIndex + 1] = p0;
            faces[fIndex + 2] = p2;
            faces[fIndex + 3] = p2 + 1;
            faces[fIndex + 4] = p1 == div ? 0 : p1;
            faces[fIndex + 5] = p1;
            fIndex += 6;

            // add p3, p2, p1
            // *faces++ = SmFace(p3,p1,p2, p3,p1,p2, 1);
            faces[fIndex] = p3 % div == 0 ? p3 - div : p3;
            faces[fIndex + 1] = p3 + 1;
            faces[fIndex + 2] = p1 == div ? 0 : p1;
            faces[fIndex + 3] = p1;
            faces[fIndex + 4] = p2;
            faces[fIndex + 5] = p2 + 1;
            fIndex += 6;

        }
        // build cap faces
        int tStart = (div + 1) * 2;
        int t1 = (div + 1) * 4;
        int p1 = div * 2;

        // bottom cap
        for (int p0 = 0; p0 < div; ++p0) {
            int p2 = p0 + 1;
            int t0 = tStart + p0;
            int t2 = t0 + 1;

            // add p0, p1, p2
            faces[fIndex] = p0;
            faces[fIndex + 1] = t0;
            faces[fIndex + 2] = p2 == div ? 0 : p2;
            faces[fIndex + 3] = t2;
            faces[fIndex + 4] = p1;
            faces[fIndex + 5] = t1;
            fIndex += 6;
        }

        p1 = div * 2 + 1;
        tStart = (div + 1) * 3;

        // top cap
        for (int p0 = 0; p0 < div; ++p0) {
            int p2 = p0 + 1 + div;
            int t0 = tStart + p0;
            int t2 = t0 + 1;

            //*faces++ = SmFace(p0+div+1,p1,p2, t0,t1,t2, 2);
            faces[fIndex] = p0 + div;
            faces[fIndex + 1] = t0;
            faces[fIndex + 2] = p1;
            faces[fIndex + 3] = t1;
            faces[fIndex + 4] = segmentSize == 2 && p2 % div == 0 ? p2 - div : p2;
            faces[fIndex + 5] = t2;
            fIndex += 6;
        }

        for (int i = 0; i < div * 2; ++i) {
            smoothing[i] = 1;
        }
        for (int i = div * 2; i < div * 4; ++i) {
            smoothing[i] = 2;
        }

        TriangleMesh m = new TriangleMesh();
        m.getPoints().setAll(points);
        m.getTexCoords().setAll(tPoints);
        m.getFaces().setAll(faces);
        m.getFaceSmoothingGroups().setAll(smoothing);

        return m;
    }
}
