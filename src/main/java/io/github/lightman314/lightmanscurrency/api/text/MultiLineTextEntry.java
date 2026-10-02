package io.github.lightman314.lightmanscurrency.api.text;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.data.BonusTooltip;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MultiLineTextEntry {

    public static final Codec<MultiLineTextEntry> CODEC = Codec.STRING.xmap(MultiLineTextEntry::new,MultiLineTextEntry::getInitialKey);
    public static final StreamCodec<ByteBuf,MultiLineTextEntry> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(MultiLineTextEntry::new,MultiLineTextEntry::getInitialKey);

    private final Lazy<String> key;
    public MultiLineTextEntry(String key) { this.key = Lazy.of(() -> key); }
    public MultiLineTextEntry(Supplier<String> key) { this.key = Lazy.of(key); }

    private String getInitialKey() { return this.key.get(); }

    public String getKey(int index) { return this.key.get() + "." + (index + 1); }

    public List<Component> get(Object... objects) { return getWithStyle(c -> {}, objects); }
    public List<Component> getWithStyle(ChatFormatting format, Object... objects) { return this.getWithStyle(c -> c.withStyle(format), objects); }
    public List<Component> getWithStyle(Consumer<MutableComponent> action, Object... objects)
    {
        List<Component> result = new ArrayList<>();
        int i = 0;
        while(i < Integer.MAX_VALUE)
        {
            String key = this.getKey(i++);
            MutableComponent line = TextHelper.translatableOrNull(key,objects);
            if(line == null || line.getString().equals(key))
                break;
            action.accept(line);
            result.add(line);
        }
        return result;
    }

    public BonusTooltip asTooltip(Object... args) {
        List<String> result = new ArrayList<>();
        for(Object o : args)
            result.add(o.toString());
        return new BonusTooltip(this,List.copyOf(result));
    }

    public static MultiLineTextEntry tooltip(String modid, String key) { return new MultiLineTextEntry("tooltip." + modid + "." + key); }

    public static MultiLineTextEntry gui(String modid, String key) { return new MultiLineTextEntry("gui." + modid + "." + key); }

    public static MultiLineTextEntry message(String modid, String key) { return new MultiLineTextEntry("message." + modid + "." + key); }

    //public static MultiLineTextEntry notification(NotificationType<?> type) { return notification(() -> LCRegistries.NOTIFICATION_TYPES.getKey(type)); }
    public static MultiLineTextEntry notification(Identifier type) { return notification(() -> type); }
    public static MultiLineTextEntry notification(Supplier<Identifier> type) { return delayed(type, key -> "notification." + key.getNamespace() + "." + key.getPath()); }

    private static MultiLineTextEntry delayed(Supplier<Identifier> key, Function<Identifier,String> factory) { return new MultiLineTextEntry(() -> factory.apply(key.get())); }

}