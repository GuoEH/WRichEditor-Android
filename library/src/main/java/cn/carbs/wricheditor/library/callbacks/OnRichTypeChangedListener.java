package cn.carbs.wricheditor.library.callbacks;

import java.util.Set;

import cn.carbs.wricheditor.library.types.RichType;

public interface OnRichTypeChangedListener {

    void onRichTypeChanged(Set<RichType> oldTypes, Set<RichType> newTypes);

}
