package io.github.lightman314.lightmanscurrency.api.client.gui.tabbed;

import java.util.Map;

public interface ISortableTab {

    int getSortPriority();

    static int sortEntries(Map.Entry<Integer,?> entryA, Map.Entry<Integer,?> entryB)
    {
        return Integer.compare(getSortKey(entryA),getSortKey(entryB));
    }

    private static <T> int getSortKey(Map.Entry<Integer,T> entry)
    {
        return entry.getValue() instanceof ISortableTab s ? s.getSortPriority() : entry.getKey();
    }

}
