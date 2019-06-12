package de.glueckscrew.gluecksroulette.playground;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import lombok.Getter;


/**
 * Frame around the wheel
 *
 * @author Sebastian Schmitt
 */
public class LuckyFrame extends MeshView {
    private static final double ANGLE_IN_RADIAN = Math.toRadians(45);
    private static final double RESIZE_FACTOR = 1.25;

    // "resolution" of our frame
    private static final int MESH_DIVISIONS = 100;

    @Getter
    private double height;


    public LuckyFrame(double radius) {
        double largeRadius = RESIZE_FACTOR * radius;

        double diff = largeRadius - radius;
        height = Math.tan(ANGLE_IN_RADIAN) * diff;

        setMesh(createMesh((float) largeRadius, (float) radius, (float) height));
        setTranslateY(-height / 2);
    }


    private static TriangleMesh createMesh(float largeRadius, float radius, float height) {
        final int nPonits = MESH_DIVISIONS * 2 + 2;
        final int tcCount = (MESH_DIVISIONS + 1) * 4 + 1; // 2 cap tex
        final int faceCount = MESH_DIVISIONS * 4;

        float textureDelta = 1.f / 256;

        float dA = 1.f / MESH_DIVISIONS;
        height *= .5f;

        float[] points = new float[nPonits * 3];
        float[] tPoints = new float[tcCount * 2];
        int[] faces = new int[faceCount * 6];
        int[] smoothing = new int[faceCount];

        int pPos = 0, tPos = 0;

        for (int i = 0; i < MESH_DIVISIONS; ++i) {
            // angle in radian
            double a = dA * i * 2 * Math.PI;

            points[pPos] = (float) (Math.sin(a) * largeRadius);
            points[pPos + 2] = (float) (Math.cos(a) * largeRadius);
            points[pPos + 1] = -height;
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
            // angle in radian
            double a = dA * i * 2 * Math.PI;
            points[pPos] = (float) (Math.sin(a) * radius);
            points[pPos + 2] = (float) (Math.cos(a) * radius);
            points[pPos + 1] = height;
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
        points[pPos + 1] = height;
        points[pPos + 2] = 0;
        points[pPos + 3] = 0;
        points[pPos + 4] = -height;
        points[pPos + 5] = 0;

        // add cap central points
        // bottom cap
        for (int i = 0; i <= MESH_DIVISIONS; ++i) {
            double a = (i < MESH_DIVISIONS) ? (dA * i * 2) * Math.PI : 0;
            tPoints[tPos] = (float) (Math.sin(a) * 0.5f) + 0.5f;
            tPoints[tPos + 1] = (float) (Math.cos(a) * 0.5f) + 0.5f;
            tPos += 2;
        }

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


