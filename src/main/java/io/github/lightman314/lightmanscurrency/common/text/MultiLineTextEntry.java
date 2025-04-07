package io.github.lightman314.lightmanscurrency.common.text;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class MultiLineTextEntry {

    private final String key;
    public MultiLineTextEntry(String key) { this.key = key; }

    public String getKey(int index) { return this.key + "." + (index + 1); }

    public Supplier<List<Component>> asSupplier(Object... objects) { return () -> this.get(objects); }
    public Supplier<List<Component>> asTooltip(Object... objects) { return () -> TooltipHelper.splitTooltips(get(objects),ChatFormatting.GRAY); }
    public List<Component> get(Object... objects) { return getWithStyle(c -> {}, objects); }
    public void tooltip(List<Component> tooltip, Object... objects) { tooltip.addAll(this.get(objects)); }
    public List<Component> getWithStyle(ChatFormatting format, Object... objects) { return this.getWithStyle(c -> c.withStyle(format), objects); }
    public List<Component> getWithStyle(Consumer<MutableComponent> action, Object... objects)
    {
        List<Component> result = new ArrayList<>();
        int i = 0;
        while(i < Integer.MAX_VALUE)
        {
            String key = this.getKey(i++);
            MutableComponent line = EasyText.translatableOrNull(key);
            if(line == null || line.getString().equals(key))
                break;
            action.accept(line);
            result.add(line);
        }
        return result;
    }

    public static MultiLineTextEntry tooltip(String modid, String key) { return new MultiLineTextEntry("tooltip." + modid + "." + key); }
    
    public static MultiLineTextEntry gui(String modid, String key) { return new MultiLineTextEntry("gui." + modid + "." + key); }
    
    public static MultiLineTextEntry message(String modid, String key) { return new MultiLineTextEntry("message." + modid + "." + key); }

}
