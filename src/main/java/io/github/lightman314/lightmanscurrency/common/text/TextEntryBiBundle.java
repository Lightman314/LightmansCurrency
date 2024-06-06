package io.github.lightman314.lightmanscurrency.common.text;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.util.TriConsumer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public final class TextEntryBiBundle<S,T> {

    private final Map<S, Map<T,TextEntry>> entryMap;

    public TextEntryBiBundle(@Nonnull Map<S,Map<T,TextEntry>> map)
    {
        Map<S,Map<T,TextEntry>> temp = new HashMap<>();
        map.forEach((key1,m) -> temp.put(key1, ImmutableMap.copyOf(m)));
        this.entryMap = ImmutableMap.copyOf(temp);
    }

    public static <S,T> TextEntryBiBundle<S,T> of(@Nonnull RegistryObjectBiBundle<? extends ItemLike,S,T> bundle)
    {
        Map<S,Map<T,TextEntry>> temp1 = new HashMap<>();
        bundle.forEach((key1, key2,item) -> {
            Map<T,TextEntry> temp2 = temp1.getOrDefault(key1,new HashMap<>());
            temp2.put(key2,TextEntry.item(item));
            temp1.put(key1,temp2);
        });
        return new TextEntryBiBundle<>(temp1);
    }

    public TextEntry get(@Nonnull S key1, @Nonnull T key2)
    {
        Map<T,TextEntry> map = this.entryMap.getOrDefault(key1, new HashMap<>());
        return map.get(key2);
    }

    public void forEach(@Nonnull TriConsumer<S,T,TextEntry> consumer)
    {
        this.entryMap.forEach((key1,map) -> map.forEach((key2,entry) -> consumer.accept(key1,key2,entry)));
    }

}
