package de.glueckscrew.gluecksroulette.hotkey;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Sebastian Schmitt
 */
public class LuckyHotKeyHandler {
    private static HashMap<KeyCode, KeyCode> US_DE_TRANSLATION;
    private static boolean useTranslation;

    static {
        useTranslation = Locale.getDefault().equals(Locale.GERMAN) || Locale.getDefault().equals(Locale.GERMANY);

        if (useTranslation) {
            US_DE_TRANSLATION = new HashMap<KeyCode, KeyCode>() {{
                put(KeyCode.BACK_SLASH, KeyCode.NUMBER_SIGN);
                put(KeyCode.CLOSE_BRACKET, KeyCode.PLUS);
                put(KeyCode.SLASH, KeyCode.MINUS);
            }};
        }
    }

    private HashMap<KeyCode, Boolean> keyStates;
    private HashMap<LuckyHotKey, Callback> hotKeys;
    private EventHandler<? super KeyEvent> keyPressed;
    private EventHandler<? super KeyEvent> keyReleased;

    public LuckyHotKeyHandler() {
        keyStates = new HashMap<>();
        hotKeys = new HashMap<>();


        keyPressed = (EventHandler<KeyEvent>) event -> {
            if (useTranslation && US_DE_TRANSLATION.containsKey(event.getCode()))
                keyStates.put(US_DE_TRANSLATION.get(event.getCode()), true);
            else
                keyStates.put(event.getCode(), true);
            handle();
        };

        keyReleased = (EventHandler<KeyEvent>) event -> {
            if (useTranslation && US_DE_TRANSLATION.containsKey(event.getCode()))
                keyStates.put(US_DE_TRANSLATION.get(event.getCode()), false);
            else
                keyStates.put(event.getCode(), false);
        };
    }

    public void releaseAll() {
        keyStates.clear();
    }

    public void register(LuckyHotKey hotKey, Callback callback) {
        hotKeys.put(hotKey, callback);
    }

    private boolean isPressed(KeyCode keyCode) {
        return keyStates.getOrDefault(keyCode, false);
    }

    private void handle() {
        for (Map.Entry<LuckyHotKey, Callback> hotKey : hotKeys.entrySet())
            if (hotKey.getKey().getKeyCodes().stream().allMatch(this::isPressed) &&
                    hotKey.getKey().getKeyCodes().size() == keyStates.keySet().stream().filter(this::isPressed).count())
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
