package io.github.lightman314.lightmanscurrency.common.event_coins;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.events.ChainDataReloadedEvent;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.CoinInputType;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.builtin.NumberDisplay;
import io.github.lightman314.lightmanscurrency.common.advancements.date.DatePredicate;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.data.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.loot.modifier.ILootModifier;
import io.github.lightman314.lightmanscurrency.common.loot.modifier.SimpleLootModifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.List;

@Mod.EventBusSubscriber
public final class ChocolateEventCoins {

    public static final String CHAIN = "chocolate_coins";

    public static final ILootModifier LOOT_MODIFIER = new ChocolateLootModifier();

    private static ChainData CHAIN_DATA = null;

    private ChocolateEventCoins() {}

    private static final DatePredicate christmasStart = new DatePredicate(12,1);
    private static final DatePredicate christmasEnd = new DatePredicate(12,31);

    public static boolean isEventActive() { return DatePredicate.isInRange(christmasStart,christmasEnd); }

    public static ChainData getChainData()
    {
        if(CHAIN_DATA == null)
        {
            CHAIN_DATA = ChainData.builder(CHAIN, EasyText.translatable("lightmanscurrency.money.chain.chocolate_coins"))
                    //Chain
                    .withCoreChain(ModItems.COIN_CHOCOLATE_COPPER)
                    .withCoin(ModItems.COIN_CHOCOLATE_IRON, 10)
                    .withCoin(ModItems.COIN_CHOCOLATE_GOLD, 10)
                    .withCoin(ModItems.COIN_CHOCOLATE_EMERALD, 10)
                    .withCoin(ModItems.COIN_CHOCOLATE_DIAMOND, 10)
                    .withCoin(ModItems.COIN_CHOCOLATE_NETHERITE, 10).back()
                    //ATM Data
                    .atmBuilder().accept(ATMExchangeButtonData::generateChocolate).back()
                    .withDisplay(new NumberDisplay(EasyText.translatable("lightmanscurrency.money.chain.chocolate_coins.display"),EasyText.translatable("lightmanscurrency.money.chain.chocolate_coins.display.wordy"), ModItems.COIN_CHOCOLATE_COPPER.get()))
                    .withInputType(CoinInputType.TEXT)
                    .asEvent().build();
        }
        return CHAIN_DATA;
    }
    @SubscribeEvent
    public static void registerChain(@Nonnull ChainDataReloadedEvent.Pre event) {
        if(LCConfig.SERVER.chocolateEventCoins.get())
            event.addEntry(getChainData(),true);
    }

    private static class ChocolateLootModifier extends SimpleLootModifier
    {

        @Override
        protected boolean isEnabled() { return isEventActive(); }
        @Override
        protected double getSuccessChance() { return LCConfig.SERVER.chocolateCoinDropRate.get(); }
        @Override
        public void replaceLoot(@Nonnull RandomSource random, @Nonnull List<ItemStack> loot) {
            this.replaceRandomItems(random, loot, ModItems.COIN_COPPER.get(), ModItems.COIN_CHOCOLATE_COPPER.get());
            this.replaceRandomItems(random, loot, ModItems.COIN_IRON.get(), ModItems.COIN_CHOCOLATE_IRON.get());
            this.replaceRandomItems(random, loot, ModItems.COIN_GOLD.get(), ModItems.COIN_CHOCOLATE_GOLD.get());
            this.replaceRandomItems(random, loot, ModItems.COIN_EMERALD.get(), ModItems.COIN_CHOCOLATE_EMERALD.get());
            this.replaceRandomItems(random, loot, ModItems.COIN_DIAMOND.get(), ModItems.COIN_CHOCOLATE_DIAMOND.get());
            this.replaceRandomItems(random, loot, ModItems.COIN_NETHERITE.get(), ModItems.COIN_CHOCOLATE_NETHERITE.get());
        }

    }

}