package io.github.lightman314.lightmanscurrency.api.text;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public final class TextHelper {

    private TextHelper() {}

    @Nullable
    public static MutableComponent translatableOrNull(String translationKey,Object... children)
    {
        Language lang = Language.getInstance();
        if(lang.has(translationKey))
            return Component.translatableEscape(translationKey,children);
        return null;
    }

}
