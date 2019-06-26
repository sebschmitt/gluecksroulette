package de.glueckscrew.gluecksroulette.playground;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import lombok.Getter;

/**
 * Cone in center of wheel
 *
 * @author Sebastian Schmitt
 */
public class LuckyCone extends MeshView {
    private static final double ANGLE_IN_RADIAN = Math.toRadians(45);

    // "resolution" of our cone
    private static final int MESH_DIVISIONS = 100;

    @Getter private double height;

    public LuckyCone(double radius) {
        height = Math.tan(ANGLE_IN_RADIAN) * radius;
        setMesh(createMesh((float) radius, (float) height));
        setTranslateY(-height / 2);
    }


    private static TriangleMesh createMesh(float radius, float height) {
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

            points[pPos] = (float) (Math.sin(a) * radius);
            points[pPos + 2] = (float) (Math.cos(a) * radius);
            points[pPos + 1] = height;
            tPoints[tPos] = 1 - dA * i;
            tPoints[tPos + 1] = 1 - textureDelta;
            pPos += 3;
            tPos += 2;
        }

        // top edge
        tPoints[tPos] = 0;
        tPoints[tPos + 1] = 1 - textureDelta;
        tPos += 2;

        // This is our spire
        for (int i = 0; i < MESH_DIVISIONS; ++i) {
            points[pPos] = 0f;
            points[pPos + 2] = 0f;
            points[pPos + 1] =  -height;
            tPoints[tPos] = 1 - dA * i;
            tPoints[tPos + 1] = textureDelta;
            pPos += 3;
            tPos += 2;
        }

        // add cap central points
        points[pPos] = 0;
        points[pPos + 1] = height;
        points[pPos + 2] = 0;
        points[pPos + 3] = 0;
        points[pPos + 4] = -height;
        points[pPos + 5] = 0;

        // top cap
        for (int i = 0; i <= MESH_DIVISIONS; ++i) {
            double a = (i < MESH_DIVISIONS) ? (dA * i * 2) * Math.PI : 0;
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
        // build cap faces;
        int t1 = (MESH_DIVISIONS + 1) * 4;
        int p1 = MESH_DIVISIONS * 2 + 1;
        int tStart = (MESH_DIVISIONS + 1) * 3;

        // top cap
        for (int p0 = 0; p0 < MESH_DIVISIONS; ++p0) {
            int p2 = p0 + 1 + MESH_DIVISIONS;
            int t0 = tStart + p0;
            int t2 = t0 + 1;

            faces[fIndex] = p0 + MESH_DIVISIONS;
            faces[fIndex + 1] = t0;
            faces[fIndex + 2] = p1;
            faces[fIndex + 3] = t1;
            faces[fIndex + 4] = p2 % MESH_DIVISIONS == 0 ? p2 - MESH_DIVISIONS : p2;
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
