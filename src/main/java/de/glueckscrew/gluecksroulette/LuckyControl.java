package de.glueckscrew.gluecksroulette;

import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import de.glueckscrew.gluecksroulette.gui.LuckyGUI;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKey;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKeyHandler;
import de.glueckscrew.gluecksroulette.io.LuckyIO;
import de.glueckscrew.gluecksroulette.models.LuckyCourse;
import de.glueckscrew.gluecksroulette.playground.LuckyPlayground;
import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the central control component
 * It also contains the main method
 *
 * @author Sebastian Schmitt
 */
public class LuckyControl extends Application {
    private static final Logger logger = Logger.getLogger(LuckyControl.class.getSimpleName());

    private LuckyPlayground playground;
    private LuckyGUI gui;
    private LuckyConfig config;

    @Override
    public void start(Stage primaryStage) {
        logger.info("start");

        config = new LuckyConfig();

        playground = new LuckyPlayground(config);
        gui = new LuckyGUI(playground, config);

        Scene mainScene = new Scene(gui, config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT));
        primaryStage.setScene(mainScene);

        playground.heightProperty().bind(mainScene.heightProperty());
        playground.widthProperty().bind(mainScene.widthProperty());


        LuckyHotKeyHandler hotKeyHandler = new LuckyHotKeyHandler();

        mainScene.setOnKeyPressed(hotKeyHandler.keyPressed());
        mainScene.setOnKeyReleased(hotKeyHandler.keyReleased());

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_TOGGLE_HELP), gui::toggleHints);
        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_SPIN), () -> playground.spin());

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_HARD_RESET), () -> playground.hardReset());

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_SOFT_RESET), () -> playground.softReset());

        hotKeyHandler.register(new LuckyHotKey(KeyCode.O), () -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Course File");
            File file = fileChooser.showOpenDialog(primaryStage);

            if (file == null) return;

                try {
                    LuckyCourse course = LuckyCourse.deserialize(LuckyIO.read(new FileInputStream(file)), file.getName());

                    playground.setCurrentCourse(course);
                    primaryStage.setTitle(course.getIdentifier());
                } catch (FileNotFoundException e) {
                    logger.log(Level.SEVERE, String.format("FileNotFound Exception thrown. Relying on default values%n"), e);
                } catch (SecurityException e) {
                    logger.log(Level.SEVERE, String.format("We're not allowed to read %s. Relying on default values%n",
                            file.getAbsolutePath()), e);
                }
        });


        hotKeyHandler.register(new LuckyHotKey(KeyCode.S, KeyCode.CONTROL), () -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Course File");
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file == null) return;

            LuckyIO.write(file, playground.getCurrentCourse().serialize());
        });


        Camera camera = playground.getCamera();
        mainScene.widthProperty().addListener((obs, oldVal, newVal) -> {
            config.set(LuckyConfig.Key.WINDOW_WIDTH, newVal.intValue());
            gui.update();

            camera.setTranslateX(camera.getTranslateX() + (oldVal.doubleValue() - newVal.doubleValue()) / 2);
            camera.setTranslateZ(camera.getTranslateZ() - (oldVal.doubleValue() - newVal.doubleValue()) / 4);
        });

        mainScene.heightProperty().addListener((obs, oldVal, newVal) -> {
            config.set(LuckyConfig.Key.WINDOW_HEIGHT, newVal.intValue());
            gui.update();

            camera.setTranslateY(camera.getTranslateY() + (oldVal.doubleValue() - newVal.doubleValue()) / 2);
            camera.setTranslateZ(camera.getTranslateZ() - (oldVal.doubleValue() - newVal.doubleValue()) / 4);
        });


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

    @Override
    public void stop() throws Exception {
        super.stop();

        config.save();
        logger.info("Good Bye!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
