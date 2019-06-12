package de.glueckscrew.gluecksroulette;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;

/**
 * This class represents the central control component
 * It also contains the main method
 *
 * @author Sebastian Schmitt
 */
public class LuckyEnumWindowNames {

    public static void main(String[] args) {
        User32.INSTANCE.EnumWindows((hWnd, userData) -> {
            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
            String wText = Native.toString(windowText);
            wText = (wText.isEmpty()) ? "" : "; text: " + wText;
            System.out.println(wText);
            return true;
        }, null);
    }
}
