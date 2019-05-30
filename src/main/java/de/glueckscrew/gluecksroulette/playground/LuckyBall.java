package de.glueckscrew.gluecksroulette.playground;

import com.sun.javafx.geom.Vec3d;
import javafx.scene.shape.Sphere;
import lombok.Getter;
import lombok.Setter;

/**
 * This class extends the javafx-sphere-class by velocity.
 *
 * @author Paul Weisser
 */


public class LuckyBall extends Sphere {
    @Getter
    @Setter
    Vec3d velocity;

    public LuckyBall() {
        this.velocity = new Vec3d();
    }

    public LuckyBall(Vec3d velocity, double radius) {
        super(radius);
        this.velocity = velocity;
    }

    public LuckyBall(Vec3d velocity) {
        this.velocity = new Vec3d();
    }
}
