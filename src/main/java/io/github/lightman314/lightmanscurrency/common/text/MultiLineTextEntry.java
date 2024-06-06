package io.github.lightman314.lightmanscurrency.common.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class MultiLineTextEntry {

    private final String key;
    public MultiLineTextEntry(@Nonnull String key) { this.key = key; }

    public String getKey(int index) { return this.key + "." + (index + 1); }

    @Nonnull
    public NonNullSupplier<List<Component>> asSupplier(Object... objects) { return () -> this.get(objects); }
    public NonNullSupplier<List<Component>> asTooltip(Object... objects) { return () -> this.getWithStyle(ChatFormatting.GRAY, objects); }
    @Nonnull
    public List<Component> get(Object... objects) { return getWithStyle(c -> {}, objects); }
    public void tooltip(@Nonnull List<Component> tooltip, Object... objects) { tooltip.addAll(this.get(objects)); }
    @Nonnull
    public List<Component> getWithStyle(@Nonnull ChatFormatting format, Object... objects) { return this.getWithStyle(c -> c.withStyle(format), objects); }
    @Nonnull
    public List<Component> getWithStyle(@Nonnull Consumer<MutableComponent> action, Object... objects)
    {
        List<Component> result = new ArrayList<>();
        int i = 0;
        while(i < Integer.MAX_VALUE)
        {
            String key = this.getKey(i++);
            MutableComponent line = Component.translatable(key,objects);
            if(line.getString().equals(key))
                break;
            action.accept(line);
            result.add(line);
        }
        return result;
    }

    @Nonnull
    public static MultiLineTextEntry tooltip(@Nonnull String modid, @Nonnull String key) { return new MultiLineTextEntry("tooltip." + modid + "." + key); }
    @Nonnull
    public static MultiLineTextEntry message(@Nonnull String modid, @Nonnull String key) { return new MultiLineTextEntry("message." + modid + "." + key); }

}
