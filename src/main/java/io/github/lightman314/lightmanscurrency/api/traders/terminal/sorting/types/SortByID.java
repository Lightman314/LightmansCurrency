package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

public class SortByID extends TerminalSortType {

    public static final SortByID INSTANCE = new SortByID();
    private SortByID() { super(LightmansCurrency.id("id")); }
    @Override
    protected int sort(TraderData a, TraderData b) { return Long.compare(a.getID(),b.getID()); }

}
