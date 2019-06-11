package de.glueckscrew.gluecksroulette.util;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.text.Text;

public class LuckyTextUtil {
    public static double getTextLength(Text text) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(text.getText(), text.getFont());
    }
}
