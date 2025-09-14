package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SortByID extends TerminalSortType {

    public static final SortByID INSTANCE = new SortByID();
    private SortByID() { super(VersionUtil.lcResource("id")); }
    @Override
    protected int sort(TraderData a, TraderData b) { return Long.compare(a.getID(),b.getID()); }

}