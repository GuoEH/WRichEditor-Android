package cn.carbs.wricheditor.library.utils;

import android.text.Editable;

import java.util.Set;

import cn.carbs.wricheditor.library.types.RichType;

public class StrategyUtil {

    public static boolean sStrongSet;

    // TODO
    // 应该有这么几种方式：
    // 1. 用户强设置：点击了 B I 等等格式按键，称之为 Strong Set Rich Types。
    //      1.1 无论任何位置，只要用户进行了强设置，则采用设置后的样式进行输出；
    // 2. 用户主动将光标移动至文字的中间（不包含最后位置）
    //      2.1 如果在移动之前没有进行强设置，则按照后面字符的 Rich Types 来处理，称之为 Backward Rich Types。
    //      2.2 如果在移动之前是强设置相关操作（如点击了 B I 等后，紧接着移动光标），则：
    //          2.2.1 如果Set<RichType>不为空，则按照强设置的字符格式来处理；
    //          2.2.2 如果Set<RichType>为空，则按照 Backward Rich Types 来处理；
    // 3. 光标在最后位置：
    //      3.1 如果用户在输入之前进行了强设置，则按照强设置进行；
    //      3.2 如果用户在输入之前没有进行强设置，则按照前面一个字符的格式进行设置；
    public static void getFriendlyRichTypes(Set<RichType> strongRichTypes, Set<RichType> output, Editable editable, int cursorLocation) {



    }

}
