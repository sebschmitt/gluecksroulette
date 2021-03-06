package de.glueckscrew.gluecksroulette.hotkey;

import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sebastian Schmitt
 */
public class LuckyHotKey {
    private static final Logger LOGGER = Logger.getLogger(LuckyHotKey.class.getSimpleName());

    /**
     * Delimiter used for String serialization / deserialization
     */
    private static final String SERIALIZE_DELIMITER = " ";

    /**
     * List of all keyCodes that belong to this hotkey
     */
    private List<KeyCode> keyCodes;


    public LuckyHotKey(List<KeyCode> keyCodes) {
        this.keyCodes = keyCodes;
    }

    public LuckyHotKey(KeyCode... keyCodes) {
        this.keyCodes = Arrays.asList(keyCodes);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < keyCodes.size(); i++) {
            sb.append(keyCodes.get(i).getName());
            if (i < keyCodes.size() - 1)
                sb.append(SERIALIZE_DELIMITER);
        }

        return sb.toString();
    }

    public String toPrettyString() {
        keyCodes.sort((o1, o2) -> {
            if (o1.isModifierKey() && !o2.isModifierKey())
                return -1;
            if (!o1.isModifierKey() && o2.isModifierKey())
                return 1;
            return o1.getName().compareTo(o2.getName());
        });

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < keyCodes.size(); i++) {
            sb.append(keyCodes.get(i).getName());
            if (i < keyCodes.size() - 1)
                sb.append(" + ");
        }

        return sb.toString();
    }

    public List<KeyCode> getKeyCodes() {
        return new ArrayList<>(keyCodes);
    }


    /**
     * Creates HotKey from given string  in form "KeyCode.name delimiter KeyCode.name .."
     *
     * @param data serialized hot key
     * @return deserialized LuckyHotKey object
     */
    public static LuckyHotKey deserialize(String data) {
        String[] keyNames = data.split(SERIALIZE_DELIMITER);

        List<KeyCode> keyCodes = new ArrayList<>();

        for (String keyName : keyNames) {
            KeyCode keyCode = KeyCode.getKeyCode(keyName);

            if (keyCode == null) {
                LOGGER.log(Level.WARNING, String.format("Invalid KeyCode %s. Ignoring", keyName));
                return null;
            }

            keyCodes.add(keyCode);
        }

        return new LuckyHotKey(keyCodes);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LuckyHotKey that = (LuckyHotKey) o;
        return keyCodes.equals(that.keyCodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCodes);
    }
}
