package de.glueckscrew.gluecksroulette.playground;

import com.sun.javafx.tk.Toolkit;
import de.glueckscrew.gluecksroulette.models.LuckyStudent;
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

import java.util.ArrayList;

/**
 * @author Sebastian Schmitt
 */
public class LuckyStudentSegment extends Group {
    private LuckyStudent luckyStudent;



    private ArrayList<String> colors = new ArrayList<String>() {{
        add("#1abc9c");
        add("#2ecc71");
        add("#3498db");
        add("#9b59b6");
        add("#34495e");
        add("#16a085");
        add("#27ae60");
        add("#2980b9");
        add("#8e44ad");
        add("#2c3e50");
        add("#f1c40f");
        add("#e67e22");
        add("#e74c3c");
        add("#ecf0f1");
        add("#95a5a6");
        add("#f39c12");
        add("#d35400");
        add("#c0392b");
        add("#bdc3c7");
        add("#7f8c8d");
    }};


    public LuckyStudentSegment(LuckyStudent luckyStudent, int offset, int courseSize) {
        this.luckyStudent = luckyStudent;

        Text text = new Text(luckyStudent.getName());
        text.setTranslateY(469);
        text.setFont(new Font(30));

        double step = offset / (double) courseSize;

        Pair<Double, Double> position = calculatePosition(step, 0.96 - getTextLength(text) / (double) LuckyPlayground.WHEEL_RADIUS);
        text.setTranslateX(position.getKey());
        text.setTranslateZ(position.getValue());

        text.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));
        text.getTransforms().add(new Rotate((offset / (double) courseSize) * 360d, Rotate.Z_AXIS));
        text.setSmooth(true);

        getChildren().add(text);

        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);

        double halfSize = (2 * Math.PI * (float) LuckyPlayground.WHEEL_RADIUS) / (double) courseSize;
        halfSize /= 2;


        mesh.getPoints().addAll(
                (float) -halfSize, 0f, 0f, // FIRST on EDGE
                (float) -halfSize, 1f, 0f, // FIRST on EDGE lower

                (float) halfSize, 0f, 0f, // SECOND on EDGE
                (float) halfSize, 1f, 0f, // Second on EDGE lower

                0f, 0f, - (float) LuckyPlayground.WHEEL_RADIUS, // Center
                0f, 1f, - (float) LuckyPlayground.WHEEL_RADIUS // Center lower
        );

        mesh.getFaces().addAll(
                0, 0, 4, 0, 2, 0
        );

        MeshView meshView = new MeshView(mesh);


        meshView.setDrawMode(DrawMode.FILL);
        meshView.setMaterial(new PhongMaterial(Color.valueOf(colors.get(offset))));
        meshView.setTranslateY(469.5);

        position = calculatePosition(step - getTextHeight(text), 1);
        meshView.setTranslateX(position.getKey());
        meshView.setTranslateZ(position.getValue());


        double x = ((offset / (double) courseSize)  - getTextHeight(text)) * 360;
        x += 90;
        meshView.getTransforms().add(new Rotate(x, Rotate.Y_AXIS));

        getChildren().add(meshView);
    }


    private static Pair<Double, Double> calculatePosition(double step, double length) {
        double t = (-step * 360) / 180d * Math.PI;
        double x = Math.cos(t) * LuckyPlayground.WHEEL_RADIUS * length;
        double y = Math.sin(t) * LuckyPlayground.WHEEL_RADIUS * length;

        return new Pair<>(x, y);
    }

    private static double getTextLength(Text text) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(text.getText(), text.getFont());
    }

    private static double getTextHeight(Text text) {
        return Toolkit.getToolkit().getFontLoader().getFontMetrics(text.getFont()).getMaxAscent();
    }
}
