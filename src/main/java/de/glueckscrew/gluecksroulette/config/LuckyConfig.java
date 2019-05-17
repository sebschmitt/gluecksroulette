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

    private HashMap<Key, ValueHolder> values;


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
            ValueHolder valueHolder = null;

            if (typeClass == String.class) {
                valueHolder = new ValueHolder<>(valueString);
            } else if (typeClass == Integer.class) {
                try {
                    int intValue = Integer.parseInt(valueString);
                    valueHolder = new ValueHolder<>(intValue);

                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, String.format("Config value \"%s\" for key \"%s\" is not an integer. Ignoring",
                            valueString, key.toString()));
                    continue;
                }
            } else if (typeClass == Boolean.class) {
                boolean boolValue = Boolean.parseBoolean(valueString);
                valueHolder = new ValueHolder<>(boolValue);
            }


            if (valueHolder == null) {
                logger.log(Level.WARNING, "Value \"%s\" has invalid type. Ignoring", valueString);
                continue;
            }

            if (values.containsKey(key))
                logger.log(Level.WARNING, String.format("Value for key %s is already present, replacing.", key.toString()));
            values.put(key, valueHolder);
        }

    }

    public ValueHolder get(Key key) {
        if (values.containsKey(key))
            return values.get(key);

        logger.log(Level.WARNING, String.format("No Value for key %s, relying on default", key.toString()));

        return key.getDefaultValue();
    }

    public void set(Key key, Object value) {
        if (!key.getTypeClass().isInstance(value))
            throw new IllegalArgumentException("Value has wrong type!");

        values.put(key, new ValueHolder<>(value));
    }

    public void save() {
        StringBuilder configString = new StringBuilder();

        for (Map.Entry<Key, ValueHolder> entry : values.entrySet()) {
            configString.append(entry.getKey().toString());
            configString.append("=");
            configString.append(entry.getValue().getValue());
            configString.append("\n");
        }

        // Magic methods coming from mr. dahlitz
    }


    /**
     *
     */
    public enum Key {
        WINDOW_WIDTH(Integer.class, new ValueHolder<>(800)),
        WINDOW_HEIGHT(Integer.class, new ValueHolder<>(600)),
        ;

        @Getter
        private Class typeClass;
        @Getter
        private ValueHolder defaultValue;

        Key(Class typeClass, ValueHolder defaultValue) {
            this.typeClass = typeClass;
            this.defaultValue = defaultValue;
        }
    }

    /**
     *
     */
    public static class ValueHolder<T> {
        @Getter
        private T value;

        private ValueHolder(T value) {
            this.value = value;
        }
    }
}