package de.glueckscrew.gluecksroulette.gui;

import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * LuckyGUI class handles displayed hint texts on screen
 *
 * @author Sebastian Schmitt
 */
public class LuckyGUI extends Group {
    private static Logger logger = Logger.getLogger(LuckyGUI.class.getSimpleName());

    /**
     * Hint Block offset (left and bottom)
     */
    private static final int HINT_BLOCK_OFFSET = 5;

    /**
     * Hint Text offset
     */
    private static final int HINT_TEXT_OFFSET = 12;

    /**
     * List of toggleable hints
     */
    private List<Text> toggleableHints;

    /**
     * Indicates whether we show full hints or only toggle hint
     */
    private boolean showHints = false;

    /**
     * Text that tells the user how to toggle the key hints
     */
    private Text toggleHint;

    /**
     * Instance of the config
     */
    private LuckyConfig config;


    public LuckyGUI(SubScene playground, LuckyConfig config) {
        this.config = config;

        toggleableHints = new ArrayList<>();

        toggleHint = new Text(String.format("Press %s to toggle hints!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_TOGGLE_HELP).toPrettyString()));
        toggleHint.setFill(Color.WHITE);

        /*
         * Hint how to spin the roulette
         */
        Text spinHint = new Text(String.format("Press %s to spin the roulette!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_SPIN).toPrettyString()));
        spinHint.setFill(Color.WHITE);
        spinHint.setVisible(false);
        toggleableHints.add(spinHint);

        /*
         * Hint for soft reset
         */
        Text softResetHint = new Text(String.format("Press %s for soft reset!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_SOFT_RESET).toPrettyString()));
        softResetHint.setFill(Color.WHITE);
        softResetHint.setVisible(false);
        toggleableHints.add(softResetHint);

        /*
         * Hint for hard reset
         */
        Text hardResetHint = new Text(String.format("Press %s for hard reset!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_HARD_RESET).toPrettyString()));
        hardResetHint.setFill(Color.WHITE);
        hardResetHint.setVisible(false);
        toggleableHints.add(hardResetHint);

        /*  Playground needs to be first */
        getChildren().add(playground);

        getChildren().add(toggleHint);
        getChildren().add(spinHint);
        getChildren().add(softResetHint);
        getChildren().add(hardResetHint);

        update();
    }

    public void update() {
        int x = HINT_BLOCK_OFFSET;
        int y = config.getInt(LuckyConfig.Key.WINDOW_HEIGHT) - toggleableHints.size() * HINT_TEXT_OFFSET - HINT_BLOCK_OFFSET;

        for (Text text : toggleableHints) {
            text.xProperty().set(x);
            text.yProperty().set(y);

            y += HINT_TEXT_OFFSET;
        }

        toggleHint.xProperty().set(x);
        toggleHint.yProperty().set(y);
    }

    public void toggleHints() {
        showHints = !showHints;

        for (Text text : toggleableHints)
            text.setVisible(showHints);
    }
}