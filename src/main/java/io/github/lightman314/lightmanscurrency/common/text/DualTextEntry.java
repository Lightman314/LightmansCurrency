package io.github.lightman314.lightmanscurrency.common.text;

import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

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

    public static DualTextEntry terminalSortType(TerminalSortType type) { return terminalSortType(type.getID()); }
    public static DualTextEntry terminalSortType(ResourceLocation type) { return terminalSortType(type.getNamespace(),type.getPath()); }
    public static DualTextEntry terminalSortType(String modid, String name) {
        String key = "gui." + modid + ".terminal.sort_type." + name;
        return new DualTextEntry(key,key + ".inverted");
    }

}
