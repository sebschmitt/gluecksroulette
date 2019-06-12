package de.glueckscrew.gluecksroulette.config;

import de.glueckscrew.gluecksroulette.LuckyMode;
import de.glueckscrew.gluecksroulette.hotkey.LuckyHotKey;
import de.glueckscrew.gluecksroulette.io.LuckyIO;
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
    private static final Logger logger = Logger.getLogger(LuckyConfig.class.getSimpleName());

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
            logger.log(Level.SEVERE, String.format("FileNotFound Exception thrown. Relying on default values%n"), e);
            return;
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, String.format("We're not allowed to read %s. Relying on default values%n", configFile.getAbsolutePath()), e);
            return;
        }

        if (fileContent.isEmpty()) return;


        for (String line : fileContent.split(KEY_SEPERATOR)) {
            String[] parts = line.split(KEY_VALUE_SEPERATOR);

            if (parts.length != 2) {
                logger.warning(String.format("Config line:%n%s%n is invalid. Ignoring", line));
                continue;
            }

            String uppercaseKey = parts[0].toUpperCase();
            Key key;

            try {
                key = Key.valueOf(uppercaseKey);
            } catch (IllegalArgumentException e) {
                logger.warning(String.format("Config key \"%s\" is invalid. Ignoring", uppercaseKey));
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
                    logger.warning(String.format("Config value \"%s\" for key \"%s\" is not an integer. Ignoring",
                            valueString, key.toString()));
                    continue;
                }
            } else if (typeClass == Boolean.class) {
                value = Boolean.parseBoolean(valueString);
            } else if (typeClass == LuckyHotKey.class) {
                value = LuckyHotKey.deserialize(valueString);
            } else if (typeClass == LuckyMode.class) {
                value = LuckyMode.valueOf(valueString);
            }


            if (value == null) {
                logger.warning(String.format("Value \"%s\" has invalid type. Ignoring", valueString));
                continue;
            }

            if (values.containsKey(key))
                logger.warning(String.format("Value for key %s is already present, replacing.", key.toString()));

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
        logger.info("saving config");
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
        HOTKEY_SAVE_COURSE_FILE(LuckyHotKey.class, new LuckyHotKey(KeyCode.S, KeyCode.CONTROL)),

        HOTKEY_FOCUS_CHANGE_TOGGLE(LuckyHotKey.class, new LuckyHotKey(KeyCode.F)),
        FOCUS_CHANGE_WINDOW_NAME(String.class, "powerpoint"),
        FOCUS_CHANGE_TIMEOUT_SECONDS(Integer.class, 10),
        FOCUS_CHANGE_ACTIVE(Boolean.class, true),

        HOTKEY_TOGGLE_MODE(LuckyHotKey.class, new LuckyHotKey(KeyCode.M)),
        MODE(LuckyMode.class, LuckyMode.CONSERVING),

        HOTKEY_REDUCE(LuckyHotKey.class, new LuckyHotKey(KeyCode.SLASH)),
        HOTKEY_ENLARGE(LuckyHotKey.class, new LuckyHotKey(KeyCode.CLOSE_BRACKET)),


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