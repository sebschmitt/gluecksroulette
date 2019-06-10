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
    private static final int FONT_SIZE = 37;
    private static final double TEXT_Y = -1.5;
    private static final double REDUCTION_FACTOR = 0.9;

    // Resolution of mesh
    private static final int MESH_DIVISIONS = 100;
    private static final float MESH_HEIGHT = 1f * 0.5f;


    @Getter
    private LuckyStudent luckyStudent;


    @Getter
    @Setter
    private double offset;
    @Getter
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
        text.setTranslateY(TEXT_Y);
        text.setFont(new Font(FONT_SIZE));
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

        Pair<Double, Double> position = calculatePosition(offset + step,
                REDUCTION_FACTOR - LuckyTextUtil.getTextLength(text) / (double) LuckyPlayground.WHEEL_RADIUS);
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
        float radius = LuckyPlayground.WHEEL_RADIUS;

        final int nPonits = MESH_DIVISIONS * 2 + 2;
        final int tcCount = (MESH_DIVISIONS + 1) * 4 + 1; // 2 cap tex
        final int faceCount = MESH_DIVISIONS * 4;

        float textureDelta = 1.f / 256;

        float dA = 1.f / MESH_DIVISIONS;

        float[] points = new float[nPonits * 3];
        float[] tPoints = new float[tcCount * 2];
        int[] faces = new int[faceCount * 6];
        int[] smoothing = new int[faceCount];

        int pPos = 0, tPos = 0;

        for (int i = 0; i < MESH_DIVISIONS; ++i) {
            double a = dA * i * Math.PI * segmentSize;

            points[pPos] = (float) (Math.sin(a) * radius);
            points[pPos + 2] = (float) (Math.cos(a) * radius);
            points[pPos + 1] = MESH_HEIGHT;
            tPoints[tPos] = 1 - dA * i;
            tPoints[tPos + 1] = 1 - textureDelta;
            pPos += 3;
            tPos += 2;
        }

        // top edge
        tPoints[tPos] = 0;
        tPoints[tPos + 1] = 1 - textureDelta;
        tPos += 2;

        for (int i = 0; i < MESH_DIVISIONS; ++i) {
            double a = dA * i * Math.PI * segmentSize;
            points[pPos] = (float) (Math.sin(a) * radius);
            points[pPos + 2] = (float) (Math.cos(a) * radius);
            points[pPos + 1] = -MESH_HEIGHT;
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
        points[pPos + 1] = MESH_HEIGHT;
        points[pPos + 2] = 0;
        points[pPos + 3] = 0;
        points[pPos + 4] = -MESH_HEIGHT;
        points[pPos + 5] = 0;

        // add cap central points
        // bottom cap
        for (int i = 0; i <= MESH_DIVISIONS; ++i) {
            double a = (i < MESH_DIVISIONS) ? (dA * i * segmentSize) * Math.PI : 0;
            tPoints[tPos] = (float) (Math.sin(a) * 0.5f) + 0.5f;
            tPoints[tPos + 1] = (float) (Math.cos(a) * 0.5f) + 0.5f;
            tPos += 2;
        }

        // top cap
        for (int i = 0; i <= MESH_DIVISIONS; ++i) {
            double a = (i < MESH_DIVISIONS) ? (dA * i * segmentSize) * Math.PI : 0;
            tPoints[tPos] = 0.5f + (float) (Math.sin(a) * 0.5f);
            tPoints[tPos + 1] = 0.5f - (float) (Math.cos(a) * 0.5f);
            tPos += 2;
        }

        tPoints[tPos] = .5f;
        tPoints[tPos + 1] = .5f;

        int fIndex = 0;

        // build body faces
        for (int p0 = 0; p0 < MESH_DIVISIONS; ++p0) {
            int p1 = p0 + 1;
            int p2 = p0 + MESH_DIVISIONS;
            int p3 = p1 + MESH_DIVISIONS;

            // add p0, p1, p2
            faces[fIndex] = p0;
            faces[fIndex + 1] = p0;
            faces[fIndex + 2] = p2;
            faces[fIndex + 3] = p2 + 1;
            faces[fIndex + 4] = p1 == MESH_DIVISIONS ? 0 : p1;
            faces[fIndex + 5] = p1;
            fIndex += 6;

            // add p3, p2, p1
            // *faces++ = SmFace(p3,p1,p2, p3,p1,p2, 1);
            faces[fIndex] = p3 % MESH_DIVISIONS == 0 ? p3 - MESH_DIVISIONS : p3;
            faces[fIndex + 1] = p3 + 1;
            faces[fIndex + 2] = p1 == MESH_DIVISIONS ? 0 : p1;
            faces[fIndex + 3] = p1;
            faces[fIndex + 4] = p2;
            faces[fIndex + 5] = p2 + 1;
            fIndex += 6;

        }
        // build cap faces
        int tStart = (MESH_DIVISIONS + 1) * 2;
        int t1 = (MESH_DIVISIONS + 1) * 4;
        int p1 = MESH_DIVISIONS * 2;

        // bottom cap
        for (int p0 = 0; p0 < MESH_DIVISIONS; ++p0) {
            int p2 = p0 + 1;
            int t0 = tStart + p0;
            int t2 = t0 + 1;

            // add p0, p1, p2
            faces[fIndex] = p0;
            faces[fIndex + 1] = t0;
            faces[fIndex + 2] = p2 == MESH_DIVISIONS ? 0 : p2;
            faces[fIndex + 3] = t2;
            faces[fIndex + 4] = p1;
            faces[fIndex + 5] = t1;
            fIndex += 6;
        }

        p1 = MESH_DIVISIONS * 2 + 1;
        tStart = (MESH_DIVISIONS + 1) * 3;

        // top cap
        for (int p0 = 0; p0 < MESH_DIVISIONS; ++p0) {
            int p2 = p0 + 1 + MESH_DIVISIONS;
            int t0 = tStart + p0;
            int t2 = t0 + 1;

            //*faces++ = SmFace(p0+div+1,p1,p2, t0,t1,t2, 2);
            faces[fIndex] = p0 + MESH_DIVISIONS;
            faces[fIndex + 1] = t0;
            faces[fIndex + 2] = p1;
            faces[fIndex + 3] = t1;
            faces[fIndex + 4] = segmentSize == 2 && p2 % MESH_DIVISIONS == 0 ? p2 - MESH_DIVISIONS : p2;
            faces[fIndex + 5] = t2;
            fIndex += 6;
        }

        for (int i = 0; i < MESH_DIVISIONS * 2; ++i) {
            smoothing[i] = 1;
        }
        for (int i = MESH_DIVISIONS * 2; i < MESH_DIVISIONS * 4; ++i) {
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
