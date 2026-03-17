package io.github.lightman314.lightmanscurrency.api.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record DataContext<T>(DynamicOps<T> ops, HolderLookup.Provider registryAccess) {

    public static <T> DataContext<T> create(DynamicOps<T> ops,HolderLookup.Provider registryAccess) { return new DataContext<>(RegistryOps.create(ops,registryAccess),registryAccess); }
    public static DataContext<JsonElement> createJson(HolderLookup.Provider registryAccess) { return create(JsonOps.INSTANCE,registryAccess); }
    public static DataContext<Tag> createNBT(HolderLookup.Provider registryAccess) { return create(NbtOps.INSTANCE,registryAccess); }

    public <X> DataContext<X> changeType(DynamicOps<X> ops) { return create(ops,this.registryAccess); }

    public <C> T write(C value, Codec<C> codec) { return codec.encodeStart(this.ops,value).getOrThrow(); }
    @Nullable
    public <C> C read(T data,Codec<C> codec)
    {
        try { return codec.parse(this.ops,data).getOrThrow();
        } catch (IllegalStateException ignored) { return null; }
    }
    public <C> C readOrThrow(T data,Codec<C> codec) throws JsonSyntaxException { return this.readOrThrow(data,codec,JsonSyntaxException::new); }
    public <C,E extends Throwable> C readOrThrow(T data,Codec<C> codec,Function<String,E> exception) throws E { return codec.parse(this.ops,data).getOrThrow(exception); }
    public <C> C readOrDefault(T data,Codec<C> codec,C defaultValue) { return this.readOrDefault2(data,codec,() -> defaultValue); }
    public <C> C readOrDefault2(T data, Codec<C> codec,Supplier<C> defaultValue) {
        C result = this.read(data,codec);
        return result == null ? defaultValue.get() : result;
    }

    public <C> List<C> safeReadList(T data, Codec<C> codec, Consumer<String> error)
    {
        List<C> list = new ArrayList<>();
        Function<String,IllegalStateException> exceptionHandler = s -> {
            error.accept(s);
            return new IllegalStateException(s);
        };
        this.ops.getList(data).getOrThrow(exceptionHandler).accept(entry ->
            codec.parse(this.ops,entry).resultOrPartial(error).ifPresent(list::add)
        );
        return list;
    }

    public <K,C> Map<K,C> safeReadMap(T data,Codec<K> keyCodec,Codec<C> valueCodec,Consumer<String> error)
    {
        Map<K,C> map = new HashMap<>();
        Function<String,IllegalStateException> exceptionHandler = s -> {
            error.accept(s);
            return new IllegalStateException(s);
        };
        this.ops.getMap(data).getOrThrow(exceptionHandler).entries().forEach(pair -> {
            try {
                K key = keyCodec.decode(this.ops,pair.getFirst()).getOrThrow(exceptionHandler).getFirst();
                C value = valueCodec.decode(this.ops,pair.getSecond()).getOrThrow(exceptionHandler).getFirst();
                map.put(key,value);
            } catch (IllegalStateException ignored) {}
        });
        return map;
    }

    public T write(Function<HolderLookup.Provider,? extends T> writer) { return writer.apply(this.registryAccess); }
    public void apply(Consumer<HolderLookup.Provider> action) { action.accept(this.registryAccess); }

    public String writeComponent(Component text) { return Component.Serializer.toJson(text,this.registryAccess); }
    public Component readComponent(String string) { return Component.Serializer.fromJson(string,this.registryAccess); }

}
