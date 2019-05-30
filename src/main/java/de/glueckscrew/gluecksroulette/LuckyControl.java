package de.glueckscrew.gluecksroulette;

import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import de.glueckscrew.gluecksroulette.gui.LuckyGUI;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKey;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKeyHandler;
import de.glueckscrew.gluecksroulette.playground.LuckyPlayground;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * This class represents the central control component
 * It also contains the main method
 *
 * @author Sebastian Schmitt
 */
public class LuckyControl extends Application {
    private LuckyPlayground playground;

    @Override
    public void start(Stage primaryStage) {
        LuckyConfig config = new LuckyConfig();

        playground = new LuckyPlayground(config);
        LuckyGUI gui = new LuckyGUI(playground, config);

        Scene mainScene = new Scene(gui, config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT));
        primaryStage.setScene(mainScene);
        mainScene.setFill(Color.GREEN);

        playground.heightProperty().bind(mainScene.heightProperty());
        playground.widthProperty().bind(mainScene.widthProperty());


        LuckyHotKeyHandler hotKeyHandler = new LuckyHotKeyHandler();

        mainScene.setOnKeyPressed(hotKeyHandler.keyPressed());
        mainScene.setOnKeyReleased(hotKeyHandler.keyReleased());

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_TOGGLE_HELP), gui::toggleHints);

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_SPIN), () -> playground.spin());

        mainScene.widthProperty().addListener((obs, oldVal, newVal) -> {
            config.set(LuckyConfig.Key.WINDOW_WIDTH, newVal.intValue());
            gui.update();

            playground.getCamera().setTranslateX(playground.getCamera().getTranslateX() + (oldVal.doubleValue() - newVal.doubleValue()) / 2);
        });

        mainScene.heightProperty().addListener((obs, oldVal, newVal) -> {
            config.set(LuckyConfig.Key.WINDOW_HEIGHT, newVal.intValue());
            gui.update();

            playground.getCamera().setTranslateY(playground.getCamera().getTranslateY() + (oldVal.doubleValue() - newVal.doubleValue()));
        });


        Camera camera = playground.getCamera();
        hotKeyHandler.register(new LuckyHotKey(KeyCode.UP, KeyCode.SHIFT), () -> camera.getTransforms().add(new Rotate(-1, Rotate.X_AXIS)));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.DOWN, KeyCode.SHIFT), () -> camera.getTransforms().add(new Rotate(1, Rotate.X_AXIS)));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.LEFT, KeyCode.SHIFT), () -> camera.getTransforms().add(new Rotate(1, Rotate.Y_AXIS)));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.RIGHT, KeyCode.SHIFT), () -> camera.getTransforms().add(new Rotate(-1, Rotate.Y_AXIS)));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.UP), () -> camera.setTranslateY(camera.getTranslateY() - 10));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.DOWN), () -> camera.setTranslateY(camera.getTranslateY() + 10));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.LEFT), () -> camera.setTranslateX(camera.getTranslateX() - 10));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.RIGHT), () -> camera.setTranslateX(camera.getTranslateX() + 10));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.W), () -> camera.setTranslateZ(camera.getTranslateZ() + 10));
        hotKeyHandler.register(new LuckyHotKey(KeyCode.S), () -> camera.setTranslateZ(camera.getTranslateZ() - 10));


        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
