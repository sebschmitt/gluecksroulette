package de.glueckscrew.gluecksroulette.config;

import de.glueckscrew.gluecksroulette.LuckyMode;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKey;
import de.glueckscrew.gluecksroulette.io.LuckyIO;
import de.glueckscrew.gluecksroulette.playground.LuckyPlayground;
import javafx.scene.input.KeyCode;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sebastian Schmitt
 */
public class LuckyConfig {
    private static final Logger LOGGER = Logger.getLogger(LuckyConfig.class.getSimpleName());

    private static final String KEY_VALUE_SEPERATOR = "=";

    private static final String KEY_SEPERATOR = "\n";

    /**
     * File name of LuckyConfig Configuration File
     */
    private static final String CONFIG_FILE_NAME = "config.luck";

    private File configFile;

    private HashMap<Key, Object> values;

    public LuckyConfig() {
        values = new HashMap<>();

        configFile = new File(CONFIG_FILE_NAME);

        if (!configFile.exists() || !configFile.isFile()) return;

        String fileContent;

        try {
            fileContent = LuckyIO.read(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, String.format("FileNotFound Exception thrown. Relying on default values%n"), e);
            return;
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, String.format("We're not allowed to read %s. Relying on default values%n", configFile.getAbsolutePath()), e);
            return;
        }

        if (fileContent.isEmpty()) return;


        for (String line : fileContent.split(KEY_SEPERATOR)) {
            String[] parts = line.split(KEY_VALUE_SEPERATOR, 2);

            if (parts.length < 2) {
                LOGGER.warning(String.format("Config line:%n%s%n is invalid. Ignoring", line));
                continue;
            }

            String uppercaseKey = parts[0].toUpperCase();
            Key key;

            try {
                key = Key.valueOf(uppercaseKey);
            } catch (IllegalArgumentException e) {
                LOGGER.warning(String.format("Config key \"%s\" is invalid. Ignoring", uppercaseKey));
                continue;
            }

            String valueString = parts[1];
            Class typeClass = key.getTypeClass();
            Object value = null;

            if (typeClass == String.class) {
                value = valueString;

            } else if (typeClass == Integer.class) {
                try {
                    value = Integer.parseInt(valueString);

                } catch (NumberFormatException e) {
                    LOGGER.warning(String.format("Config value \"%s\" for key \"%s\" is not an integer. Ignoring",
                            valueString, key.toString()));
                    continue;
                }
            } else if (typeClass == Boolean.class) {
                value = Boolean.parseBoolean(valueString);
            } else if (typeClass == LuckyHotKey.class) {
                value = LuckyHotKey.deserialize(valueString);
            } else if (typeClass == LuckyMode.class) {
                value = LuckyMode.valueOf(valueString);
            } else if (typeClass == Double.class) {
                value = Double.parseDouble(valueString);
            }


            if (value == null) {
                LOGGER.warning(String.format("Value \"%s\" has invalid type. Ignoring", valueString));
                continue;
            }

            if (values.containsKey(key))
                LOGGER.warning(String.format("Value for key %s is already present, replacing.", key.toString()));

            values.put(key, value);
        }

    }

    @SuppressWarnings("unchecked")
    private <T> T get(Key key, Class<T> type) {
        if (values.containsKey(key))
            return (T) values.get(key);

        return getDefault(key, type);
    }

    @SuppressWarnings("unchecked")
    private <T> T getDefault(Key key, Class<T> type) {
        return (T) key.getDefaultValue();
    }

    public int getInt(Key key) {
        return get(key, Integer.class);
    }

    public int getDefaultInt(Key key) {
        return getDefault(key, Integer.class);
    }

    public double getDouble(Key key) {
        return get(key, Double.class);
    }

    public double getDefaultDouble(Key key) {
        return getDefault(key, Double.class);
    }

    public String getString(Key key) {
        return get(key, String.class);
    }

    public String getDefaultString(Key key) {
        return getDefault(key, String.class);
    }

    public boolean getBool(Key key) {
        return get(key, Boolean.class);
    }

    public boolean getDefaultBool(Key key) {
        return getDefault(key, Boolean.class);
    }

    public LuckyHotKey getHotKey(Key key) {
        return get(key, LuckyHotKey.class);
    }

    public LuckyMode getMode(Key key) {
        return get(key, LuckyMode.class);
    }

    public void set(Key key, Object value) {
        if (!key.getTypeClass().isInstance(value))
            throw new IllegalArgumentException("Value has wrong type!");

        values.put(key, value);
    }

    public boolean save() {
        LOGGER.info("saving config");
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<Key, Object> entry : values.entrySet()) {
            sb.append(entry.getKey().toString());
            sb.append(KEY_VALUE_SEPERATOR);
            sb.append(entry.getValue().toString());
            sb.append(KEY_SEPERATOR);
        }

        for (Key key : Key.values()) {
            if (values.containsKey(key)) continue;

            sb.append(key.toString());
            sb.append(KEY_VALUE_SEPERATOR);
            sb.append(key.getDefaultValue().toString());
            sb.append(KEY_SEPERATOR);
        }

        return LuckyIO.write(configFile, sb.toString());
    }

    /**
     * Defines a Config Key with additional default Value
     */
    public enum Key {
        WINDOW_WIDTH(Integer.class, 800),
        WINDOW_HEIGHT(Integer.class, 600),

        HOTKEY_TOGGLE_HELP(LuckyHotKey.class, new LuckyHotKey(KeyCode.F1)),
        HOTKEY_SPIN(LuckyHotKey.class, new LuckyHotKey(KeyCode.SPACE)),
        HOTKEY_HARD_RESET(LuckyHotKey.class, new LuckyHotKey(KeyCode.R, KeyCode.CONTROL)),
        HOTKEY_SOFT_RESET(LuckyHotKey.class, new LuckyHotKey(KeyCode.R)),
        HOTKEY_OPEN_COURSE_FILE(LuckyHotKey.class, new LuckyHotKey(KeyCode.O, KeyCode.CONTROL)),

        HOTKEY_FOCUS_CHANGE_TOGGLE(LuckyHotKey.class, new LuckyHotKey(KeyCode.F)),
        FOCUS_CHANGE_WINDOW_NAME(String.class, "powerpoint"),
        FOCUS_CHANGE_TIMEOUT_SECONDS(Integer.class, 5),
        FOCUS_CHANGE_ACTIVE(Boolean.class, true),

        HOTKEY_TOGGLE_MODE(LuckyHotKey.class, new LuckyHotKey(KeyCode.M)),
        MODE(LuckyMode.class, LuckyMode.THINNING),

        HOTKEY_REDUCE(LuckyHotKey.class, new LuckyHotKey(KeyCode.MINUS)),
        HOTKEY_ENLARGE(LuckyHotKey.class, new LuckyHotKey(KeyCode.PLUS)),

        LAST_COURSE(String.class, ""),
        MANUAL_WEIGHT_CHANGE(Integer.class, 2),

        HOTKEY_CAMERA_UP(LuckyHotKey.class, new LuckyHotKey(KeyCode.UP)),
        HOTKEY_CAMERA_DOWN(LuckyHotKey.class, new LuckyHotKey(KeyCode.DOWN)),
        HOTKEY_CAMERA_LEFT(LuckyHotKey.class, new LuckyHotKey(KeyCode.LEFT)),
        HOTKEY_CAMERA_RIGHT(LuckyHotKey.class, new LuckyHotKey(KeyCode.RIGHT)),
        HOTKEY_CAMERA_FORWARD(LuckyHotKey.class, new LuckyHotKey(KeyCode.W)),
        HOTKEY_CAMERA_BACKWARD(LuckyHotKey.class, new LuckyHotKey(KeyCode.S)),

        HOTKEY_CAMERA_TURN_UP(LuckyHotKey.class, new LuckyHotKey(KeyCode.UP, KeyCode.SHIFT)),
        HOTKEY_CAMERA_TURN_DOWN(LuckyHotKey.class, new LuckyHotKey(KeyCode.DOWN, KeyCode.SHIFT)),
        HOTKEY_CAMERA_TURN_LEFT(LuckyHotKey.class, new LuckyHotKey(KeyCode.LEFT, KeyCode.SHIFT)),
        HOTKEY_CAMERA_TURN_RIGHT(LuckyHotKey.class, new LuckyHotKey(KeyCode.RIGHT, KeyCode.SHIFT)),


        CAMERA_MOVE_STEP(Double.class, 10d),
        CAMERA_ROT_STEP(Double.class, 1d),
        CAMERA_X(Double.class, (double) -LuckyPlayground.WHEEL_RADIUS),
        CAMERA_Y(Double.class, 150d),
        CAMERA_Z(Double.class, 0d),
        CAMERA_ROT_X(Double.class, -25d),
        CAMERA_ROT_Y(Double.class, 0d),

        ;

        @Getter
        private Class typeClass;
        @Getter
        private Object defaultValue;

        Key(Class typeClass, Object defaultValue) {
            this.typeClass = typeClass;
            this.defaultValue = defaultValue;
        }
    }
}