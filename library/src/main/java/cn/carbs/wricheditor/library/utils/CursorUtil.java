package cn.carbs.wricheditor.library.utils;

public class CursorUtil {

    // TODO 先忽略 editor 切更换的问题
    private static int sLastTextLength;
    private static int sLastCursorLocation;

    public static void markLastTextLength(int lastTextLength) {
        sLastTextLength = lastTextLength;
    }

    public static void markLastCursorLocation(int lastCursorLocation) {
        sLastCursorLocation = lastCursorLocation;
    }

    public static int getLastTextLength() {
        return sLastTextLength;
    }

    public static int getLastCursorLocation() {
        return sLastCursorLocation;
    }

    public static boolean isCursorChangedAutomaticallyByTextChange(int currentTextLength, int currentCursorLocation) {
        int deltaTextLength = currentTextLength - getLastTextLength();
        int deltaCursorLocation = currentCursorLocation - getLastCursorLocation();
        if (deltaTextLength == deltaCursorLocation) {
            return true;
        }
        return false;
    }

}