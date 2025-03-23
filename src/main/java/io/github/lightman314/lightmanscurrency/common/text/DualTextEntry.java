package io.github.lightman314.lightmanscurrency.common.text;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DualTextEntry {

    public final TextEntry first;
    public final TextEntry second;
    public DualTextEntry(String key1, String key2)
    {
        this.first = new TextEntry(key1);
        this.second = new TextEntry(key2);
    }

    public static DualTextEntry advancement(String name) { return new DualTextEntry("advancements." + name + ".title","advancements." + name + ".description"); }

    public static DualTextEntry resourcePack(String modid, String name) { return new DualTextEntry("resourcepack." + modid + "." + name,"resourcepack." + modid + "." + name + ".description"); }

    public static DualTextEntry permission(String permission) { return new DualTextEntry("permission.lightmanscurrency." + permission,"permission.lightmanscurrency." + permission + ".tooltip"); }

}
