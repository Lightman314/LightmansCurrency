package io.github.lightman314.lightmanscurrency.common.blockentity;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.auction.SMessageSyncAuctionStandDisplay;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class AuctionStandBlockEntity extends TileEntity {

    public AuctionStandBlockEntity() { super(ModBlockEntities.AUCTION_STAND.get()); }

    private static ImmutableList<ItemStack> displayItems = ImmutableList.of(ItemStack.EMPTY);

    @SubscribeEvent(priority = EventPriority.LOWEST) //Set to low priority so that it doesn't run before the coin list is loaded and makes the persistent traders fail to load properly.
    public static void serverStart(FMLServerStartedEvent event) {
        if(AuctionHouseTrader.isEnabled())
            RandomizeDisplayItems();
    }

    @SubscribeEvent
    public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(AuctionHouseTrader.isEnabled() && event.getEntity() instanceof ServerPlayerEntity)
            LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)event.getEntity()), new SMessageSyncAuctionStandDisplay(displayItems));
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event)
    {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(AuctionHouseTrader.isEnabled() && server != null && server.getTickCount() % 1200 == 0)
            RandomizeDisplayItems();
    }

    private static void RandomizeDisplayItems()
    {
        TraderData trader = TraderSaveData.GetAuctionHouse(false);
        if(trader instanceof AuctionHouseTrader)
        {
            AuctionHouseTrader ah = (AuctionHouseTrader)trader;
            if(ah.getTradeCount() > 0)
            {
                AuctionTradeData trade = ah.getTrade(new Random().nextInt(ah.getTradeCount()));
                if(trade != null)
                {
                    setDisplayItems(trade.getAuctionItems());
                    return;
                }
            }
        }
        setDefaultDisplayItem();
    }

    public static ImmutableList<ItemStack> getDisplayItems() { return displayItems; }

    private static void setDefaultDisplayItem() { setDisplayItems(ImmutableList.of(new ItemStack(ModItems.TRADING_CORE.get()))); }

    private static void setDisplayItems(List<ItemStack> items)
    {
        displayItems = ImmutableList.copyOf(InventoryUtil.copyList(items));
        LightmansCurrencyPacketHandler.instance.send(PacketDistributor.ALL.noArg(), new SMessageSyncAuctionStandDisplay(displayItems));
    }

    public static void syncItemsFromServer(List<ItemStack> items) {
        displayItems = ImmutableList.copyOf(items);
    }

}