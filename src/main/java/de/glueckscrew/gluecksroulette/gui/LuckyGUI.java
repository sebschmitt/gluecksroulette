package de.glueckscrew.gluecksroulette.gui;

import com.sun.javafx.PlatformUtil;
import de.glueckscrew.gluecksroulette.config.LuckyConfig;
import de.glueckscrew.gluecksroulette.util.LuckyTextUtil;
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
     * Shows state of auto focus change
     */
    private Text focusChangeActiveHint;
    /**
     * Shows current mode
     */
    private Text currentModeHint;

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

        focusChangeActiveHint = new Text("Focus change");
        if (config.getBool(LuckyConfig.Key.FOCUS_CHANGE_ACTIVE))
            focusChangeActiveHint.setFill(Color.GREEN);
        else
            focusChangeActiveHint.setFill(Color.RED);

        /*
        Hint for current mode
         */
        currentModeHint = new Text(config.getMode(LuckyConfig.Key.MODE).toString());
        currentModeHint.setFill(Color.WHITE);

        /*
        Hint how to spin the roulette
         */
        toggleableHints.add(createToggleableHint(String.format("Press %s to spin the roulette!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_SPIN).toPrettyString())));

        /*
        Hint for soft reset
         */
        toggleableHints.add(createToggleableHint(String.format("Press %s for soft reset!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_SOFT_RESET).toPrettyString())));

        /*
        Hint for hard reset
         */
        toggleableHints.add(createToggleableHint(String.format("Press %s for hard reset!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_HARD_RESET).toPrettyString())));
        /*
        Hint for open course file
         */
        toggleableHints.add(createToggleableHint(String.format("Press %s to open course!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_OPEN_COURSE_FILE).toPrettyString())));

        /*
        Hint for saving course file
         */
        toggleableHints.add(createToggleableHint(String.format("Press %s to save course!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_SAVE_COURSE_FILE).toPrettyString())));

        /*
        Hint for toggling focus change
         */
        if (PlatformUtil.isWindows())
            toggleableHints.add(createToggleableHint(String.format("Press %s to toggle focus change!",
                config.getHotKey(LuckyConfig.Key.HOTKEY_FOCUS_CHANGE_TOGGLE).toPrettyString())));

        /*  Playground needs to be first */
        getChildren().add(playground);

        getChildren().addAll(toggleHint);
        getChildren().add(currentModeHint);
        if (PlatformUtil.isWindows())
            getChildren().add(focusChangeActiveHint);
        getChildren().addAll(toggleableHints);

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

        currentModeHint.xProperty().set(config.getInt(LuckyConfig.Key.WINDOW_WIDTH) - LuckyTextUtil.getTextLength(currentModeHint) - HINT_BLOCK_OFFSET);
        currentModeHint.yProperty().set(HINT_TEXT_OFFSET);

        focusChangeActiveHint.xProperty().set(config.getInt(LuckyConfig.Key.WINDOW_WIDTH) - LuckyTextUtil.getTextLength(focusChangeActiveHint) - HINT_BLOCK_OFFSET);
        focusChangeActiveHint.yProperty().set(2 * HINT_TEXT_OFFSET);
    }

    public void updateMode() {
        currentModeHint.setText(config.getMode(LuckyConfig.Key.MODE).toString());
        currentModeHint.xProperty().set(config.getInt(LuckyConfig.Key.WINDOW_WIDTH) - LuckyTextUtil.getTextLength(currentModeHint) - HINT_BLOCK_OFFSET);
        currentModeHint.yProperty().set(HINT_TEXT_OFFSET);
    }

    public void updateFocusChange() {
        if (config.getBool(LuckyConfig.Key.FOCUS_CHANGE_ACTIVE))
            focusChangeActiveHint.setFill(Color.GREEN);
        else
            focusChangeActiveHint.setFill(Color.RED);
    }

    public void toggleHints() {
        showHints = !showHints;

        for (Text text : toggleableHints)
            text.setVisible(showHints);
    }

    private Text createToggleableHint(String text) {
        Text hint = new Text(text);
        hint.setFill(Color.WHITE);
        hint.setVisible(false);

        return hint;
    }
}