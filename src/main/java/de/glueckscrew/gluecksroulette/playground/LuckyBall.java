package de.glueckscrew.gluecksroulette.playground;

import com.sun.javafx.geom.Vec3d;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import lombok.Getter;
import lombok.Setter;

/**
 * This class extends the javafx-sphere-class by velocity.
 * implemented as a singleton
 *
 * @author Paul Weisser
 */
public class LuckyBall extends Sphere {
    private static PhongMaterial material;
    private static LuckyBall instance;

    static {
        material = new PhongMaterial(Color.WHITE);
        material.setSpecularColor(Color.WHITE);
    }

    @Getter
    @Setter
    Vec3d velocity;

    //access to the class
    public static LuckyBall getInstance() {
        if (LuckyBall.instance == null) {
            LuckyBall.instance = new LuckyBall();
        }
        return LuckyBall.instance;
    }

    private LuckyBall() {
        this.velocity = new Vec3d();
        setMaterial(material);
    }

    public LuckyBall(Vec3d velocity, double radius) {
        super(radius);
        this.velocity = velocity;
    }

    public LuckyBall(Vec3d velocity) {
        this.velocity = new Vec3d();
    }
}
