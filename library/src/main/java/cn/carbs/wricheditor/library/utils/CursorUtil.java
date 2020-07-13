package cn.carbs.wricheditor.library.utils;

public class CursorUtil {

    // TODO 先忽略 editor 切更换的问题
    private static int sLastTextLength;
    private static int sLastCursorLocation;
    private static String sLastWRichEditorContentDescription;

    public static void markLastTextLength(int lastTextLength) {
        sLastTextLength = lastTextLength;
    }

    public static void markLastCursorLocation(int lastCursorLocation) {
        sLastCursorLocation = lastCursorLocation;
    }

    public static void markLastWRichEditorContentDescription(String lastWRichEditorContentDescription) {
        sLastWRichEditorContentDescription = lastWRichEditorContentDescription;
    }

    public static int getLastTextLength() {
        return sLastTextLength;
    }

    public static int getLastCursorLocation() {
        return sLastCursorLocation;
    }

    public static String getLastWRichEditorContentDescription() {
        return sLastWRichEditorContentDescription;
    }

    public static boolean isCursorChangedAutomaticallyByTextChange(int currentTextLength,
                                                                   int currentCursorLocation,
                                                                   String contentDescription) {
        int deltaTextLength = currentTextLength - getLastTextLength();
        int deltaCursorLocation = currentCursorLocation - getLastCursorLocation();
        if (deltaTextLength == deltaCursorLocation
                && stringEqual(contentDescription, getLastWRichEditorContentDescription())) {
            return true;
        }
        return false;
    }

    public static boolean stringEqual(String a, String b) {
        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return a.equals(b);
        }
    }

    public static synchronized String getNewContentDescriptionForWRichEditor() {
        String ret = "0";
        if (sLastWRichEditorContentDescription == null) {
            return ret;
        }
        try {
            int i = Integer.valueOf(sLastWRichEditorContentDescription);
            ret = String.valueOf((i + 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}