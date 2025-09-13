package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SortByName extends TerminalSortType {

    public static final SortByName INSTANCE = new SortByName();
    private SortByName() { super(VersionUtil.lcResource("name")); }
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
