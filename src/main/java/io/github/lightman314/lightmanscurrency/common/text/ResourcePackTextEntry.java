package io.github.lightman314.lightmanscurrency.common.text;

import javax.annotation.Nonnull;

public class ResourcePackTextEntry {

    public final TextEntry nameText;
    public final TextEntry descriptionText;
    private ResourcePackTextEntry(String modid, String name)
    {
        this.nameText = new TextEntry("resourcepack." + modid + "." + name);
        this.descriptionText = new TextEntry("resourcepack." + modid + "." + name + ".description");
    }

    public static ResourcePackTextEntry of(@Nonnull String modid, @Nonnull String name) { return new ResourcePackTextEntry(modid,name); }

}