package de.glueckscrew.gluecksroulette.config;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sebastian Schmitt
 */
public class LuckyConfig {
    private static Logger logger = Logger.getLogger(LuckyConfig.class.getSimpleName());

    private HashMap<Key, Object> values;


    public LuckyConfig() {
        values = new HashMap<>();

        String fileContent = "\n"; // Magically coming from Mr dahlitz

        for (String line : fileContent.split("\n")) {
            String[] parts = line.split("=");

            if (parts.length != 2) {
                logger.log(Level.WARNING, String.format("Config line:\n%s\n is invalid. Ignoring", line));
                continue;
            }

            String uppercaseKey = parts[0].toUpperCase();
            Key key;

            try {
                key = Key.valueOf(uppercaseKey);
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, String.format("Config key \"%s\" is invalid. Ignoring", uppercaseKey));
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
                    logger.log(Level.WARNING, String.format("Config value \"%s\" for key \"%s\" is not an integer. Ignoring",
                            valueString, key.toString()));
                    continue;
                }
            } else if (typeClass == Boolean.class) {
                value = Boolean.parseBoolean(valueString);
            }


            if (value == null) {
                logger.log(Level.WARNING, "Value \"%s\" has invalid type. Ignoring", valueString);
                continue;
            }

            if (values.containsKey(key))
                logger.log(Level.WARNING, String.format("Value for key %s is already present, replacing.", key.toString()));

            values.put(key, value);
        }

    }

    public int getInt(Key key) {
        if (values.containsKey(key))
            return (int) values.get(key);

        return (int) key.getDefaultValue();
    }

    public String getString(Key key) {
        if (values.containsKey(key))
            return (String) values.get(key);

        return (String) key.getDefaultValue();
    }

    public boolean getBool(Key key) {
        if (values.containsKey(key))
            return (boolean) values.get(key);

        return (boolean) key.getDefaultValue();
    }

    public void set(Key key, Object value) {
        if (!key.getTypeClass().isInstance(value))
            throw new IllegalArgumentException("Value has wrong type!");

        values.put(key, value);
    }

    public void save() {
        StringBuilder configString = new StringBuilder();

        for (Map.Entry<Key, Object> entry : values.entrySet()) {
            configString.append(entry.getKey().toString());
            configString.append("=");
            configString.append(entry.getValue());
            configString.append("\n");
        }

        // Magic methods coming from mr. dahlitz
    }

    /**
     * Defines a Config Key with additional default Value
     */
    public enum Key {
        WINDOW_WIDTH(Integer.class, 800),
        WINDOW_HEIGHT(Integer.class, 600),
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