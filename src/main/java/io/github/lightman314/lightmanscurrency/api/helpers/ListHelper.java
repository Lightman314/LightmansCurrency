package io.github.lightman314.lightmanscurrency.api.helpers;

import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class ListHelper {
    private ListHelper() {}

    @Nullable
    public static <T> T getOrNull(List<T> list,int index) {
        if(index >= 0 && index < list.size())
            return list.get(index);
        return null;
    }
    public static <T> T getOrDefault(List<T> list,int index,T defaultValue) { return Objects.requireNonNullElse(getOrNull(list,index),defaultValue); }

    /**
     * Obtains a time-based cycling item from the list, moving on to the next entry every 2 seconds
     */
    public static <T> T cyclingValueFromList(List<T> list,T emptyEntry) { return cyclingValueFromList(list,(Supplier<T>)() -> emptyEntry); }
    /**
     * Obtains a time-based cycling item from the list, moving on to the next entry every 2 seconds
     */
    public static <T> T cyclingValueFromList(List<T> list, Supplier<T> emptyEntry) {
        if(list.isEmpty())
            return emptyEntry.get();
        int displayIndex = (int)(System.currentTimeMillis() / 2000 % list.size());
        return list.get(displayIndex);
    }

    public static <A,B> Pair<List<A>,List<B>> seperateLists(List<Pair<A,B>> list) {
        List<A> listA = new ArrayList<>();
        List<B> listB = new ArrayList<>();
        for(Pair<A,B> val : list) {
            listA.add(val.getFirst());
            listB.add(val.getSecond());
        }
        return Pair.of(listA,listB);
    }

    public static <T> List<T> replaceLast(List<T> list,T replacement) {
        List<T> newList = new ArrayList<>(list);
        newList.set(newList.size() - 1,replacement);
        return newList;
    }
    public static <T> List<T> replaceFirst(List<T> list,T replacement) {
        List<T> newList = new ArrayList<>(list);
        newList.set(0,replacement);
        return newList;
    }

    public static <T> void forceListSize(List<T> list,int size,Supplier<T> factory) { forceListSize(list,size,factory,e -> {});}
    public static <T> void forceListSize(List<T> list,int size,Supplier<T> factory,Consumer<T> onRemove) {
        while(list.size() > size)
            onRemove.accept(list.removeLast());
        while(list.size() < size)
            list.add(factory.get());
    }

    public static <T> List<T> copyList(List<T> list,UnaryOperator<T> copier) {
        List<T> result = new ArrayList<>();
        for(T val : list)
            result.add(copier.apply(val));
        return result;
    }

    public static <T> boolean listEquals(List<T> list1,List<T> list2) { return listEquals(list1,list2,Objects::equals); }
    public static <T> boolean listEquals(List<T> list1,List<T> list2,BiFunction<T,T,Boolean> test) {
        if(list1.size() != list2.size())
            return false;
        for(int i = 0; i < list1.size() && i < list2.size(); ++i) {
            if(!list1.get(i).equals(list2.get(i)))
                return false;
        }
        return true;
    }

}
