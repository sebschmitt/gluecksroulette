package de.glueckscrew.gluecksroulette.playground;

import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import javafx.scene.*;
import javafx.scene.paint.Color;

/**
 * @author Sebastian Schmitt
 */
public class LuckyPlayGround extends SubScene {
    private Camera camera;

    public LuckyPlayGround(LuckyConfig config) {
        super(new Group(), config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT), true, SceneAntialiasing.BALANCED);
        setFill(Color.BLACK);

        camera = new PerspectiveCamera(false);
        setCamera(camera);
    }
}