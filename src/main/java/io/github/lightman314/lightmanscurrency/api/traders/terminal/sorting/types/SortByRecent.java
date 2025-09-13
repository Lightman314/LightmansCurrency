package io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types;

import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SortByRecent extends TerminalSortType {

    public static final SortByRecent INSTANCE = new SortByRecent();
    private SortByRecent() { super(VersionUtil.lcResource("recent")); }

    @Override
    protected int sort(TraderData a, TraderData b) {
        //Inverted sorting so that bigger number goes first
        return Long.compare(b.getLastInteractionTime(),a.getLastInteractionTime());
    }

}
