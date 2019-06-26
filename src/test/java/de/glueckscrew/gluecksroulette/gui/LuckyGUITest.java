package de.glueckscrew.gluecksroulette.gui;

import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * @author Sebastian Schmitt
 */
public class LuckyGUITest extends Application {
    @Override
    public void start(Stage primaryStage) {
        LuckyConfig config = new LuckyConfig();

        SubScene scene = new SubScene(new Group(),
                config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT), true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);

        //Setting camera
        PerspectiveCamera camera = new PerspectiveCamera(false);
        camera.setTranslateX(-500);
        camera.setTranslateY(100);
        camera.setTranslateZ(500);
        camera.getTransforms().add(new Rotate(-35, Rotate.X_AXIS));
        camera.getTransforms().add(new Rotate(0, Rotate.Y_AXIS));
        scene.setCamera(camera);


        LuckyGUI gui = new LuckyGUI(scene, config);
        Scene mainScene = new Scene(gui, config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT));
        primaryStage.setScene(mainScene);


        mainScene.setOnKeyPressed(event -> gui.toggleHints());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
