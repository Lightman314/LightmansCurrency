package io.github.lightman314.lightmanscurrency.api.helpers;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ListHelper {
    private ListHelper() {}

    /**
     * Runs each value of the list through the consumer<br>
     * Useful as a {@linkplain List#addAll(Collection)} alternative where a Consumer is given instead of a List
     */
    public static <T> void consumeAll(Consumer<T> consumer,List<T> list)
    {
        for(T entry : list)
            consumer.accept(entry);
    }

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

}
