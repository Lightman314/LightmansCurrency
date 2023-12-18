package io.github.lightman314.lightmanscurrency.api.traders.terminal;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;

import javax.annotation.Nonnull;
import java.util.Comparator;

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
        public SortingOptions withUnnamedLowPriorityFromConfig() { return this.withUnnamedLowPriority(Config.SERVER.moveUnnamedTradersToBottom.get()); }
    }

    @Nonnull
    public static Comparator<TraderData> getDefaultSorter() { return getSorter(options().withCreativePriority(true).withAuctionHousePriority(true).withEmptyLowPriority(true).withUnnamedLowPriorityFromConfig()); }

    @Nonnull
    public static Comparator<TraderData> getSorter(@Nonnull SortingOptions options) { return new TraderSorter(options); }

    private record TraderSorter(@Nonnull SortingOptions options) implements Comparator<TraderData> {

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

                //Sort by trader name
                int sort = a.getName().getString().toLowerCase().compareTo(b.getName().getString().toLowerCase());
                //Sort by owner name if trader name is equal
                if (sort == 0)
                    sort = a.getOwner().getOwnerName(true).compareToIgnoreCase(b.getOwner().getOwnerName(true));

                return sort;

            } catch (Throwable t) { return 0; }
        }
    }

}
