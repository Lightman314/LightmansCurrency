package io.github.lightman314.lightmanscurrency.api.text;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.api.helpers.system.Consumer3;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;

public final class TextEntryBundle2<K1, K2> {

    private final Map<K1,Map<K2,TextEntry>> entryMap;

    public TextEntryBundle2(Map<K1,Map<K2,TextEntry>> map)
    {
        Map<K1,Map<K2,TextEntry>> temp = new HashMap<>();
        map.forEach((key1,m) -> temp.put(key1, ImmutableMap.copyOf(m)));
        this.entryMap = ImmutableMap.copyOf(temp);
    }

    public static <K1,K2,A extends ItemLike,B extends A> TextEntryBundle2<K1,K2> of(DeferredHolderBundle2<K1,K2,A,B> bundle)
    {
        Map<K1,Map<K2,TextEntry>> temp1 = new HashMap<>();
        bundle.forEachHolder((key1,key2,holder) -> {
            Map<K2,TextEntry> temp2 = temp1.getOrDefault(key1,new HashMap<>());
            temp2.put(key2,TextEntry.item(holder));
            temp1.put(key1,temp2);
        });
        return new TextEntryBundle2<>(temp1);
    }

    public TextEntry get(K1 key1, K2 key2)
    {
        Map<K2,TextEntry> map = this.entryMap.getOrDefault(key1, new HashMap<>());
        return map.get(key2);
    }

    public void forEach(Consumer3<K1,K2,TextEntry> consumer)
    {
        this.entryMap.forEach((key1,map) -> map.forEach((key2,entry) -> consumer.accept(key1,key2,entry)));
    }

}