package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.types.SortByID;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TerminalSorter {

    private TerminalSorter() {}

    public static SortingOptions options() { return new SortingOptions(); }

    public static final class SortingOptions
    {
        private boolean creativeAtTop = false;
        private boolean auctionHouseAtTop = false;
        private boolean emptyAtBottom = false;
        private boolean unnamedAtBottom = false;
        private SortingOptions() {}
        public SortingOptions withCreativePriority(boolean priority) { this.creativeAtTop = priority; return this; }
        public SortingOptions withAuctionHousePriority(boolean priority) { this.auctionHouseAtTop = priority; return this; }
        public SortingOptions withEmptyLowPriority(boolean priority) { this.emptyAtBottom = priority; return this; }
        public SortingOptions withUnnamedLowPriority(boolean priority) { this.unnamedAtBottom = priority; return this; }
        public SortingOptions withUnnamedLowPriorityFromConfig() { return this.withUnnamedLowPriority(LCConfig.SERVER.moveUnnamedTradersToBottom.get()); }
    }

    public static Comparator<TraderData> getDefaultSorter() { return getDefaultSorter(SortByID.INSTANCE); }
    public static Comparator<TraderData> getDefaultSorter(TerminalSortType sorter) { return getSorter(options().withCreativePriority(true).withAuctionHousePriority(true).withEmptyLowPriority(true).withUnnamedLowPriorityFromConfig(),sorter); }

    public static Comparator<TraderData> getSorter(SortingOptions options) { return getSorter(options,SortByID.INSTANCE); }
    public static Comparator<TraderData> getSorter(SortingOptions options,TerminalSortType sorter) { return new TraderSorter(options,sorter); }

    private record TraderSorter(SortingOptions options,TerminalSortType sorter) implements Comparator<TraderData> {

        @Override
        public int compare(TraderData a, TraderData b) {
            try {

                if (this.options.auctionHouseAtTop) {
                    boolean ahA = a instanceof AuctionHouseTrader;
                    boolean ahB = b instanceof AuctionHouseTrader;
                    if (ahA && !ahB)
                        return -1;
                    else if (ahB && !ahA)
                        return 1;
                }

                if (this.options.emptyAtBottom) {
                    boolean emptyA = !a.hasValidTrade();
                    boolean emptyB = !b.hasValidTrade();
                    if (emptyA != emptyB)
                        return emptyA ? 1 : -1;
                }

                if (this.options.creativeAtTop) {
                    //Prioritize creative traders at the top of the list
                    if (a.isCreative() && !b.isCreative())
                        return -1;
                    else if (b.isCreative() && !a.isCreative())
                        return 1;
                }

                if(this.options.unnamedAtBottom) {
                    if(a.hasCustomName() && !b.hasCustomName())
                        return -1;
                    if(b.hasCustomName() && !a.hasCustomName())
                        return 1;
                }

                //Sort by selected sort type
                return Objects.requireNonNullElse(this.sorter,SortByID.INSTANCE).compare(a, b);

            } catch (Throwable t) { return 0; }
        }
    }

}