package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
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
            builder.title(LCText.Other.CREATIVE_GROUP_COINS.get())
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
                        //Wallets
                        p.accept(LCItems.WALLET_COPPER);
                        p.accept(LCItems.WALLET_IRON);
                        p.accept(LCItems.WALLET_GOLD);
                        p.accept(LCItems.WALLET_EMERALD);
                        p.accept(LCItems.WALLET_DIAMOND);
                        p.accept(LCItems.WALLET_NETHERITE);
                        p.accept(LCItems.WALLET_NETHER_STAR);
                        p.accept(LCItems.WALLET_ENDER_DRAGON);
                    }));

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> TRADERS = register("traders",builder ->
            builder.title(LCText.Other.CREATIVE_GROUP_TRADERS.get())
                    .icon(LCItems.TRADING_CORE::toStack)
                    .displayItems(((parameters, p) -> {
                        ezPop(p,LCBlocks.DISPLAY_CASE);
                    })));

    private static DeferredHolder<CreativeModeTab,CreativeModeTab> register(String name, UnaryOperator<CreativeModeTab.Builder> builder) { return REGISTER.register(name,() -> builder.apply(CreativeModeTab.builder()).build()); }

    public static <T extends ItemLike,X extends T> void ezPop(CreativeModeTab.Output output, DeferredHolderBundle<?,T,X> bundle) {
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