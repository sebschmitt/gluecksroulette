package de.glueckscrew.gluecksroulette.config;

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

    public int getInt(Key key) {
        if (values.containsKey(key))
            return (int) values.get(key);

        return (int) key.getDefaultValue();
    }

    public int getDefaultInt(Key key) {
        return (int) key.getDefaultValue();
    }

    public String getString(Key key) {
        if (values.containsKey(key))
            return (String) values.get(key);

        return (String) key.getDefaultValue();
    }

    public String getDefaultString(Key key) {
        return (String) key.getDefaultValue();
    }

    public boolean getBool(Key key) {
        if (values.containsKey(key))
            return (boolean) values.get(key);

        return (boolean) key.getDefaultValue();
    }

    public boolean getDefaultBool(Key key) {
        return (boolean) key.getDefaultValue();
    }

    public LuckyHotKey getHotKey(Key key) {
        if (values.containsKey(key))
            return (LuckyHotKey) values.get(key);

        return (LuckyHotKey) key.getDefaultValue();
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