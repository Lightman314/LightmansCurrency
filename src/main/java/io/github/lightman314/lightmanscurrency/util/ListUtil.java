package io.github.lightman314.lightmanscurrency.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class ListUtil {

    public static <T> List<T> convertList(List<? extends T> list) {
        return new ArrayList<>(list);
    }

    public static List<Integer> createList(int[] array)
    {
        List<Integer> list = new ArrayList<>();
        for(int val : array)
            list.add(val);
        return list;
    }

    @Nonnull
    public static <T> T randomItemFromList(@Nonnull List<T> list, @Nonnull T emptyEntry) { return randomItemFromList(list,(Supplier<T>)() -> emptyEntry); }

    @Nonnull
    public static <T> T randomItemFromList(@Nonnull List<T> list, @Nonnull Supplier<T> emptyEntry)
    {
        if(list.isEmpty())
            return emptyEntry.get();
        int displayIndex = (int)(TimeUtil.getCurrentTime() / 2000 % list.size());
        return list.get(displayIndex);
    }

    @Nonnull
    public static <T> T randomItemFromCollection(@Nonnull Collection<T> collection, @Nonnull T emptyEntry) { return randomItemFromCollection(collection,(Supplier<T>)() -> emptyEntry); }

    @Nonnull
    public static <T> T randomItemFromCollection(@Nonnull Collection<T> collection, @Nonnull Supplier<T> emptyEntry) { return randomItemFromList(collection.stream().toList(),emptyEntry); }

    @Nullable
    public static <T> T weightedRandomItemFromList(List<T> list, Function<T,Integer> weightGetter)
    {
        int totalWeight = 0;
        for(T entry : list)
            totalWeight += weightGetter.apply(entry);
        if(totalWeight <= 0)
            return null;
        int random = new Random().nextInt(totalWeight);
        int index = 0;
        while(index < list.size())
        {
            T entry = list.get(index++);
            random -= weightGetter.apply(entry);
            if(random < 0)
                return entry;
        }
        return null;
    }

}
