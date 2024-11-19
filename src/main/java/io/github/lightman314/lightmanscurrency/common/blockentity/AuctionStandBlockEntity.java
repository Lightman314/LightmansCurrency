package io.github.lightman314.lightmanscurrency.common.blockentity;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.misc.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.network.message.auction.SPacketSyncAuctionStandDisplay;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Random;

@EventBusSubscriber
public class AuctionStandBlockEntity extends EasyBlockEntity {

    public boolean dropItem = true;

    public AuctionStandBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.AUCTION_STAND.get(), pos, state); }

    private static ImmutableList<ItemStack> displayItems = ImmutableList.of(ItemStack.EMPTY);

    @SubscribeEvent(priority = EventPriority.LOWEST) //Set to low priority so that it doesn't run before the coin list is loaded and makes the persistent traders fail to load properly.
    public static void serverStart(ServerStartedEvent event) {
        if(AuctionHouseTrader.isEnabled())
            AuctionStandBlockEntity.RandomizeDisplayItems();
    }

    @SubscribeEvent
    public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(AuctionHouseTrader.isEnabled() && event.getEntity() instanceof ServerPlayer sp)
            new SPacketSyncAuctionStandDisplay(displayItems).sendTo(sp);
    }

    private static boolean randomizeNextOpportunity = false;

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Pre event)
    {
        if(AuctionHouseTrader.isEnabled() && event.getServer().getTickCount() % 1200 == 0)
        {
            if(event.hasTime())
                RandomizeDisplayItems();
            else
                randomizeNextOpportunity = true;
        }
        else if(event.hasTime() && randomizeNextOpportunity)
        {
            randomizeNextOpportunity = false;
            RandomizeDisplayItems();
        }
    }

    private static void RandomizeDisplayItems()
    {
        randomizeNextOpportunity = false;
        TraderData trader = TraderSaveData.GetAuctionHouse(false);
        if(trader instanceof AuctionHouseTrader ah && ah.getTradeCount() > 0)
        {
            AuctionTradeData trade = ah.getTrade(new Random().nextInt(ah.getTradeCount()));
            if(trade != null)
            {
                setDisplayItems(trade.getAuctionItems());
                return;
            }
        }
        setDefaultDisplayItem();
    }

    public static ImmutableList<ItemStack> getDisplayItems() { return displayItems; }

    private static void setDefaultDisplayItem() { setDisplayItems(ImmutableList.of(new ItemStack(ModItems.TRADING_CORE.get()))); }


    private static void setDisplayItems(List<ItemStack> items)
    {
        displayItems = ImmutableList.copyOf(InventoryUtil.copyList(items));
        new SPacketSyncAuctionStandDisplay(displayItems).sendToAll();
    }

    public static void syncItemsFromServer(List<ItemStack> items) { displayItems = ImmutableList.copyOf(items); }


}
