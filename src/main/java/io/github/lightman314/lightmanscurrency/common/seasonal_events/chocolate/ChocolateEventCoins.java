package io.github.lightman314.lightmanscurrency.common.seasonal_events.chocolate;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.events.ChainDataReloadedEvent;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin.NumberDisplay;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.loot.ConfigItemTier;
import io.github.lightman314.lightmanscurrency.common.seasonal_events.data.EventData;
import io.github.lightman314.lightmanscurrency.common.seasonal_events.data.EventRange;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public final class ChocolateEventCoins {

    public static final String CHAIN = "chocolate_coins";

    private static ChainData CHAIN_DATA = null;

    private ChocolateEventCoins() {}

    //Christmas event for all of december
    public static final EventRange CHRISTMAS = EventRange.create(12,1,12,31);
    //Valentine's event for the day before to the day after
    public static final EventRange VALENTINES = EventRange.create(2,13,2,15);

    public static ChainData getChainData()
    {
        if(CHAIN_DATA == null)
        {
            CHAIN_DATA = ChainData.builder(CHAIN, LCText.COIN_CHAIN_CHOCOLATE)
                    //Chain
                    .withCoreChain(ModItems.COIN_CHOCOLATE_COPPER)
                    .withCoin(ModItems.COIN_CHOCOLATE_IRON, 10)
                    .withCoin(ModItems.COIN_CHOCOLATE_GOLD, 10)
                    .withCoin(ModItems.COIN_CHOCOLATE_EMERALD, 10)
                    .withCoin(ModItems.COIN_CHOCOLATE_DIAMOND, 10)
                    .withCoin(ModItems.COIN_CHOCOLATE_NETHERITE, 10).back()
                    //Side Chains
                    .withSideChain(ModBlocks.COINPILE_CHOCOLATE_COPPER,9,ModItems.COIN_CHOCOLATE_COPPER)
                    .withCoin(ModBlocks.COINBLOCK_CHOCOLATE_COPPER,4).back()
                    .withSideChain(ModBlocks.COINPILE_CHOCOLATE_IRON,9,ModItems.COIN_CHOCOLATE_IRON)
                    .withCoin(ModBlocks.COINBLOCK_CHOCOLATE_IRON,4).back()
                    .withSideChain(ModBlocks.COINPILE_CHOCOLATE_GOLD,9,ModItems.COIN_CHOCOLATE_GOLD)
                    .withCoin(ModBlocks.COINBLOCK_CHOCOLATE_GOLD,4).back()
                    .withSideChain(ModBlocks.COINPILE_CHOCOLATE_EMERALD,9,ModItems.COIN_CHOCOLATE_EMERALD)
                    .withCoin(ModBlocks.COINBLOCK_CHOCOLATE_EMERALD,4).back()
                    .withSideChain(ModBlocks.COINPILE_CHOCOLATE_DIAMOND,9,ModItems.COIN_CHOCOLATE_DIAMOND)
                    .withCoin(ModBlocks.COINBLOCK_CHOCOLATE_DIAMOND,4).back()
                    .withSideChain(ModBlocks.COINPILE_CHOCOLATE_NETHERITE,9,ModItems.COIN_CHOCOLATE_NETHERITE)
                    .withCoin(ModBlocks.COINBLOCK_CHOCOLATE_NETHERITE,4).back()
                    //ATM Data
                    .atmBuilder().accept(ATMExchangeButtonData::generateChocolate).back()
                    .withDisplay(new NumberDisplay(LCText.COIN_CHAIN_CHOCOLATE_DISPLAY,LCText.COIN_CHAIN_CHOCOLATE_DISPLAY_WORDY, ModItems.COIN_CHOCOLATE_COPPER.get()))
                    .withInputType(CoinInputType.TEXT)
                    .asEvent().build();
        }
        return CHAIN_DATA;
    }
    @SubscribeEvent
    public static void registerChain(@Nonnull ChainDataReloadedEvent.Pre event) {
        if(LCConfig.COMMON.chocolateEventCoins.get() && !event.chainExists(CHAIN))
            event.addEntry(getChainData());
    }

    public static EventData lazyEvent(EventRange range, String eventID) { return lazyEvent(range,eventID,null); }
    public static EventData lazyEvent(EventRange range, String eventID, @Nullable Component rewardMessage) {
        return EventData.builder(eventID)
                .dateRange(range)
                .replacementRate(0.1d)
                .replacementItem(ConfigItemTier.T1,ModItems.COIN_CHOCOLATE_COPPER.get())
                .replacementItem(ConfigItemTier.T2,ModItems.COIN_CHOCOLATE_IRON.get())
                .replacementItem(ConfigItemTier.T3,ModItems.COIN_CHOCOLATE_GOLD.get())
                .replacementItem(ConfigItemTier.T4,ModItems.COIN_CHOCOLATE_EMERALD.get())
                .replacementItem(ConfigItemTier.T5,ModItems.COIN_CHOCOLATE_DIAMOND.get())
                .replacementItem(ConfigItemTier.T6,ModItems.COIN_CHOCOLATE_NETHERITE.get())
                .startingReward(ModItems.COIN_CHOCOLATE_GOLD.get(),32)
                .startingRewardMessage(rewardMessage)
                .build();
    }

}