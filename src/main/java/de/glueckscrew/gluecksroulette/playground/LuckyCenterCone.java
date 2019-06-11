package de.glueckscrew.gluecksroulette.playground;

import javafx.scene.shape.DrawMode;
import javafx.scene.shape.TriangleMesh;

public class LuckyCenterCone extends LuckyCone {
    public LuckyCenterCone() {
        setDrawMode(DrawMode.FILL);

        double radius = 0.25 * LuckyPlayground.WHEEL_RADIUS;
        double height = Math.tan(45) * radius;
        setMesh(createMesh((float) radius, (float) height));
        setTranslateY(-height / 2);
    }


    private static TriangleMesh createMesh(float radius, float height) {
        int divisions = 200;

        final int nPonits = divisions * 2 + 2;
        final int tcCount = (divisions + 1) * 4 + 1; // 2 cap tex
        final int faceCount = divisions * 4;

        float textureDelta = 1.f / 256;

        float dA = 1.f / divisions;
        height *= .5f;

        float points[] = new float[nPonits * 3];
        float tPoints[] = new float[tcCount * 2];
        int faces[] = new int[faceCount * 6];
        int smoothing[] = new int[faceCount];

        int pPos = 0, tPos = 0;

        for (int i = 0; i < divisions; ++i) {
            double a = dA * i * 2 * Math.PI;

            points[pPos + 0] = (float) (Math.sin(a) * radius);
            points[pPos + 2] = (float) (Math.cos(a) * radius);
            points[pPos + 1] = height;
            tPoints[tPos + 0] = 1 - dA * i;
            tPoints[tPos + 1] = 1 - textureDelta;
            pPos += 3;
            tPos += 2;
        }

        // top edge
        tPoints[tPos + 0] = 0;
        tPoints[tPos + 1] = 1 - textureDelta;
        tPos += 2;

        for (int i = 0; i < divisions; ++i) {
            points[pPos + 0] = (float) (0);
            points[pPos + 2] = (float) (-0);
            points[pPos + 1] =  -height;
            tPoints[tPos + 0] = 1 - dA * i;
            tPoints[tPos + 1] = textureDelta;
            pPos += 3;
            tPos += 2;
        }



        // add cap central points
        points[pPos + 0] = 0;
        points[pPos + 1] = height;
        points[pPos + 2] = 0;
        points[pPos + 3] = 0;
        points[pPos + 4] = -height;
        points[pPos + 5] = 0;
        pPos += 6;

        // add cap central points

        // top cap
        for (int i = 0; i <= divisions; ++i) {
            double a = (i < divisions) ? (dA * i * 2) * Math.PI : 0;
            tPoints[tPos + 0] = 0.5f + (float) (Math.sin(a) * 0.5f);
            tPoints[tPos + 1] = 0.5f - (float) (Math.cos(a) * 0.5f);
            tPos += 2;
        }

        tPoints[tPos + 0] = .5f;
        tPoints[tPos + 1] = .5f;
        tPos += 2;

        int fIndex = 0;

        // build body faces
        for (int p0 = 0; p0 < divisions; ++p0) {
            int p1 = p0 + 1;
            int p2 = p0 + divisions;
            int p3 = p1 + divisions;

            // add p0, p1, p2
            faces[fIndex + 0] = p0;
            faces[fIndex + 1] = p0;
            faces[fIndex + 2] = p2;
            faces[fIndex + 3] = p2 + 1;
            faces[fIndex + 4] = p1 == divisions ? 0 : p1;
            faces[fIndex + 5] = p1;
            fIndex += 6;

            // add p3, p2, p1
            // *faces++ = SmFace(p3,p1,p2, p3,p1,p2, 1);
            faces[fIndex + 0] = p3 % divisions == 0 ? p3 - divisions : p3;
            faces[fIndex + 1] = p3 + 1;
            faces[fIndex + 2] = p1 == divisions ? 0 : p1;
            faces[fIndex + 3] = p1;
            faces[fIndex + 4] = p2;
            faces[fIndex + 5] = p2 + 1;
            fIndex += 6;

        }
        // build cap faces
        int tStart = (divisions + 1) * 2;
        int t1 = (divisions + 1) * 4;
        int p1 = divisions * 2;


        p1 = divisions * 2 + 1;
        tStart = (divisions + 1) * 3;

        // top cap
        for (int p0 = 0; p0 < divisions; ++p0) {
            int p2 = p0 + 1 + divisions;
            int t0 = tStart + p0;
            int t2 = t0 + 1;

            //*faces++ = SmFace(p0+div+1,p1,p2, t0,t1,t2, 2);
            faces[fIndex + 0] = p0 + divisions;
            faces[fIndex + 1] = t0;
            faces[fIndex + 2] = p1;
            faces[fIndex + 3] = t1;
            faces[fIndex + 4] = p2 % divisions == 0 ? p2 - divisions : p2;
            faces[fIndex + 5] = t2;
            fIndex += 6;
        }

        for (int i = 0; i < divisions * 2; ++i) {
            smoothing[i] = 1;
        }
        for (int i = divisions * 2; i < divisions * 4; ++i) {
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
