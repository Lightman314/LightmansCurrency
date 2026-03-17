package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;

public class SortByName extends TerminalSortType {

    public static final SortByName INSTANCE = new SortByName();
    private SortByName() { super(LightmansCurrency.id("name")); }
    @Override
    public int sortPriority() { return 100; }
    @Override
    protected int sort(TraderData a, TraderData b) {
        //Sort by trader name
        int sort = a.getName().getString().toLowerCase().compareTo(b.getName().getString().toLowerCase());
        //Sort by owner name if trader name is equal
        if (sort == 0)
            sort = a.getOwner().getName().getString().compareToIgnoreCase(b.getOwner().getName().getString());
        return sort;
    }

}
