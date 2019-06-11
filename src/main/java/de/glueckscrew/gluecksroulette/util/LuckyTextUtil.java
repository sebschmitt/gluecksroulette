package de.glueckscrew.gluecksroulette.util;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.text.Text;

/**
 * Utility for Text objects
 *
 * @author Sebastian Schmitt
 */
public class LuckyTextUtil {
    public static double getTextLength(Text text) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(text.getText(), text.getFont());
    }
}
