package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

@EventBusSubscriber
public final class LCCreativeGroups {
    private LCCreativeGroups() {}

    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB,LCApi.MODID);

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> COINS = register("coins",builder ->
            builder.title(LCText.Resources.CREATIVE_GROUP_COINS.get())
                    .icon(LCItems.COIN_GOLD::toStack)
                    .displayItems((parameters,p) -> {
                        //Coins
                        p.accept(LCItems.COIN_COPPER);
                        p.accept(LCBlocks.COIN_PILE_COPPER);
                        p.accept(LCBlocks.COIN_BLOCK_COPPER);
                        p.accept(LCItems.COIN_IRON);
                        p.accept(LCBlocks.COIN_PILE_IRON);
                        p.accept(LCBlocks.COIN_BLOCK_IRON);
                        p.accept(LCItems.COIN_GOLD);
                        p.accept(LCBlocks.COIN_PILE_GOLD);
                        p.accept(LCBlocks.COIN_BLOCK_GOLD);
                        p.accept(LCItems.COIN_EMERALD);
                        p.accept(LCBlocks.COIN_PILE_EMERALD);
                        p.accept(LCBlocks.COIN_BLOCK_EMERALD);
                        p.accept(LCItems.COIN_DIAMOND);
                        p.accept(LCBlocks.COIN_PILE_DIAMOND);
                        p.accept(LCBlocks.COIN_BLOCK_DIAMOND);
                        p.accept(LCItems.COIN_NETHERITE);
                        p.accept(LCBlocks.COIN_PILE_NETHERITE);
                        p.accept(LCBlocks.COIN_BLOCK_NETHERITE);
                        //Chocolate Coins
                        //Coins
                        p.accept(LCItems.COIN_CHOCOLATE_COPPER);
                        p.accept(LCBlocks.COIN_PILE_CHOCOLATE_COPPER);
                        p.accept(LCBlocks.COIN_BLOCK_CHOCOLATE_COPPER);
                        p.accept(LCItems.COIN_CHOCOLATE_IRON);
                        p.accept(LCBlocks.COIN_PILE_CHOCOLATE_IRON);
                        p.accept(LCBlocks.COIN_BLOCK_CHOCOLATE_IRON);
                        p.accept(LCItems.COIN_CHOCOLATE_GOLD);
                        p.accept(LCBlocks.COIN_PILE_CHOCOLATE_GOLD);
                        p.accept(LCBlocks.COIN_BLOCK_CHOCOLATE_GOLD);
                        p.accept(LCItems.COIN_CHOCOLATE_EMERALD);
                        p.accept(LCBlocks.COIN_PILE_CHOCOLATE_EMERALD);
                        p.accept(LCBlocks.COIN_BLOCK_CHOCOLATE_EMERALD);
                        p.accept(LCItems.COIN_CHOCOLATE_DIAMOND);
                        p.accept(LCBlocks.COIN_PILE_CHOCOLATE_DIAMOND);
                        p.accept(LCBlocks.COIN_BLOCK_CHOCOLATE_DIAMOND);
                        p.accept(LCItems.COIN_CHOCOLATE_NETHERITE);
                        p.accept(LCBlocks.COIN_PILE_CHOCOLATE_NETHERITE);
                        p.accept(LCBlocks.COIN_BLOCK_CHOCOLATE_NETHERITE);
                        //Wallets
                        p.accept(LCItems.WALLET_COPPER);
                        p.accept(LCItems.WALLET_IRON);
                        p.accept(LCItems.WALLET_GOLD);
                        p.accept(LCItems.WALLET_EMERALD);
                        p.accept(LCItems.WALLET_DIAMOND);
                        p.accept(LCItems.WALLET_NETHERITE);
                        p.accept(LCItems.WALLET_NETHER_STAR);
                        p.accept(LCItems.WALLET_ENDER_DRAGON);
                        //Trading Core
                        p.accept(LCItems.TRADING_CORE);
                    }));

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> MACHINES = register("machines",builder ->
            builder.title(LCText.Resources.CREATIVE_GROUP_MACHINES.get())
                    .icon(LCBlocks.ATM::toStack)
                    .displayItems((parameters,p) -> {
                        //ATM
                        p.accept(LCBlocks.ATM);
                        p.accept(LCItems.ATM_PORTABLE);
                        //Coin Mint
                        p.accept(LCBlocks.COIN_MINT);
                        //TODO Stuff lol
                    }));

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> TRADERS = register("traders",builder ->
            builder.title(LCText.Resources.CREATIVE_GROUP_TRADERS.get())
                    .icon(LCItems.TRADING_CORE::toStack)
                    .displayItems(((parameters, p) -> {
                        ezPop(p,LCBlocks.DISPLAY_CASE);
                        ezPop(p,LCBlocks.CARD_DISPLAY);
                    })));

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> UPGRADES = register("upgrades",builder ->
            builder.title(LCText.Resources.CREATIVE_GROUP_UPGRADES.get())
                    .icon(LCItems.UPGRADE_SMITHING_TEMPLATE::toStack)
                    .displayItems(((parameters, p) -> {
                        p.accept(LCItems.UPGRADE_SMITHING_TEMPLATE);
                        p.accept(LCItems.ITEM_CAPACITY_UPGRADE_1);
                        p.accept(LCItems.ITEM_CAPACITY_UPGRADE_2);
                        p.accept(LCItems.ITEM_CAPACITY_UPGRADE_3);
                        p.accept(LCItems.ITEM_CAPACITY_UPGRADE_4);
                        p.accept(LCItems.NETWORK_UPGRADE);
                    })));

    private static DeferredHolder<CreativeModeTab,CreativeModeTab> register(String name, UnaryOperator<CreativeModeTab.Builder> builder) { return REGISTER.register(name,() -> builder.apply(CreativeModeTab.builder()).build()); }

    public static <T extends ItemLike,X extends T> void ezPop(CreativeModeTab.Output output, DeferredHolderBundle<?,T,X> bundle) {
        for(X value : bundle.getAllSorted())
            output.accept(value);
    }

    public static <T extends ItemLike,X extends T> void ezPop(CreativeModeTab.Output output, DeferredHolderBundle2<?,?,T,X> bundle) {
        for(X value : bundle.getAllSorted())
            output.accept(value);
    }

    @SubscribeEvent
    private static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS)
        {
            //Add colored blocks
            ezPop(event,LCBlocks.DISPLAY_CASE);
        }
    }

}