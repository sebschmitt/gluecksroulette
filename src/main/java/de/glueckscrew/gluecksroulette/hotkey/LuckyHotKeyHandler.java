package de.glueckscrew.gluecksroulette.hotkey;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Schmitt
 */
public class LuckyHotKeyHandler {
    private HashMap<KeyCode, Boolean> keyStates;
    private HashMap<LuckyHotKey, Callback> hotKeys;
    private EventHandler<? super KeyEvent> keyPressed;
    private EventHandler<? super KeyEvent> keyReleased;

    public LuckyHotKeyHandler() {
        keyStates = new HashMap<>();
        hotKeys = new HashMap<>();


        keyPressed = (EventHandler<KeyEvent>) event -> {
            keyStates.put(event.getCode(), true);
            handle();
        };

        keyReleased = (EventHandler<KeyEvent>) event -> keyStates.put(event.getCode(), false);
    }

    public void register(LuckyHotKey hotKey, Callback callback) {
        hotKeys.put(hotKey, callback);
    }

    private boolean isPressed(KeyCode keyCode) {
        return keyStates.getOrDefault(keyCode, false);
    }

    private void handle() {
        for (Map.Entry<LuckyHotKey, Callback> hotKey : hotKeys.entrySet())
            if (hotKey.getKey().getKeyCodes().stream().allMatch(this::isPressed))
                hotKey.getValue().handleHotKey();
    }


    public EventHandler<? super KeyEvent> keyPressed() {
        return keyPressed;
    }

    public EventHandler<? super KeyEvent> keyReleased() {
        return keyReleased;
    }

    public interface Callback {
        void handleHotKey();
    }
}
