package de.glueckscrew.gluecksroulette;

import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import de.glueckscrew.gluecksroulette.gui.LuckyGUI;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKeyHandler;
import de.glueckscrew.gluecksroulette.playground.LuckyPlayGround;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class represents the central control component
 * It also contains the main method
 *
 * @author Sebastian Schmitt
 */
public class LuckyControl extends Application {
    private LuckyPlayGround playground;

    @Override
    public void start(Stage primaryStage) {
        LuckyConfig config = new LuckyConfig();

        playground = new LuckyPlayGround(config);
        LuckyGUI gui = new LuckyGUI(playground, config);

        Scene mainScene = new Scene(gui, config.getInt(LuckyConfig.Key.WINDOW_WIDTH),
                config.getInt(LuckyConfig.Key.WINDOW_HEIGHT));
        primaryStage.setScene(mainScene);

        LuckyHotKeyHandler hotKeyHandler = new LuckyHotKeyHandler();

        mainScene.setOnKeyPressed(hotKeyHandler.keyPressed());
        mainScene.setOnKeyReleased(hotKeyHandler.keyReleased());

        hotKeyHandler.register(config.getHotKey(LuckyConfig.Key.HOTKEY_TOGGLE_HELP), gui::toggleHints);

        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
