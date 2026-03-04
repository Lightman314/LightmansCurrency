package io.github.lightman314.lightmanscurrency.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ListUtil {

    public static <T> List<T> convertList(List<? extends T> list) {
        return new ArrayList<>(list);
    }

    public static <T> List<T> copyList(List<T> list,UnaryOperator<T> copyFunction) {
        List<T> result = new ArrayList<>(list);
        result.replaceAll(copyFunction);
        return result;
    }

    public static List<Integer> createList(int[] array)
    {
        List<Integer> list = new ArrayList<>();
        for(int val : array)
            list.add(val);
        return list;
    }

    public static <T> List<T> createList(Iterable<T> iterable)
    {
        List<T> list = new ArrayList<>();
        for(T e : iterable)
            list.add(e);
        return list;
    }

    public static <T,X> List<T> mapList(List<X> oldList,Function<X,T> mapper) { return new ArrayList<>(oldList.stream().map(mapper).toList()); }

    public static <T> T randomItemFromList(List<T> list, T emptyEntry) { return randomItemFromList(list,(Supplier<T>)() -> emptyEntry); }

    public static <T> T randomItemFromList(List<T> list, Supplier<T> emptyEntry)
    {
        if(list.isEmpty())
            return emptyEntry.get();
        int displayIndex = (int)(TimeUtil.getCurrentTime() / 2000 % list.size());
        return list.get(displayIndex);
    }

    public static <T> T randomItemFromCollection(Collection<T> collection, T emptyEntry) { return randomItemFromCollection(collection,(Supplier<T>)() -> emptyEntry); }

    public static <T> T randomItemFromCollection(Collection<T> collection, Supplier<T> emptyEntry) { return randomItemFromList(collection.stream().toList(),emptyEntry); }

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
