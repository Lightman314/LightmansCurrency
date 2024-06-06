package io.github.lightman314.lightmanscurrency.common.text;

import javax.annotation.Nonnull;

public class AdvancementTextEntry {

    public final TextEntry titleText;
    public final TextEntry descriptionText;
    private AdvancementTextEntry(@Nonnull String name)
    {
        this.titleText = new TextEntry("advancements." + name + ".title");
        this.descriptionText = new TextEntry("advancements." + name + ".description");
    }

    public static AdvancementTextEntry of(@Nonnull String name) { return new AdvancementTextEntry(name); }

}
