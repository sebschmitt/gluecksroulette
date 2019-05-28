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


public class LuckySphere extends Sphere {
    @Getter @Setter
    private Vec3d velocity;

    public LuckySphere (){
        super();
        this.velocity = new Vec3d();
    }
    public LuckySphere(Vec3d velocity, double radius){
        super(radius);
        this.velocity = velocity;
    }
    public LuckySphere (Vec3d velocity){
        super();
        this.velocity = new Vec3d();
    }
}
