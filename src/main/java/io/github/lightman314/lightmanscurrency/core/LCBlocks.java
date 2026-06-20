package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.ColorHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.features.coins.FallingCoinBlock;
import io.github.lightman314.lightmanscurrency.features.coins.FallingCoinPile;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific.DisplayCaseBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class LCBlocks {

    private LCBlocks() {}

    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(LCApi.MODID);

    //Coin Piles
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_COPPER = register("copper_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.COLOR_ORANGE)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_IRON = register("iron_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.METAL)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_GOLD = register("gold_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.GOLD)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_EMERALD = register("emerald_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.EMERALD)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_DIAMOND = register("diamond_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.DIAMOND)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_NETHERITE = register("netherite_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.COLOR_BLACK)
                    .strength(3f,6f)
                    .sound(SoundType.METAL),
            Item.Properties::fireResistant);

    //Coin Blocks
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_COPPER = register("copper_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.COLOR_ORANGE)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_IRON = register("iron_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.METAL)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_GOLD = register("gold_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.GOLD)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_EMERALD = register("emerald_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.EMERALD)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_DIAMOND = register("diamond_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.DIAMOND)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_NETHERITE = register("netherite_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.COLOR_BLACK)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));

    public static final DeferredHolderBundle<DyeColor,Block,DisplayCaseBlock> DISPLAY_CASE = registerColored("display_case",DisplayCaseBlock::new,(p,color) ->
            p.mapColor(color)
                    .strength(2.0f,Float.POSITIVE_INFINITY)
                    .sound(SoundType.GLASS)
                    .noOcclusion());

    public static <T extends Block> DeferredHolderBundle<DyeColor,Block,T> registerColored(String name,BiFunction<BlockBehaviour.Properties,DyeColor,T> factory,BiFunction<BlockBehaviour.Properties,DyeColor,BlockBehaviour.Properties> properties) {
        DeferredHolderBundle<DyeColor,Block,T> bundle = new DeferredHolderBundle<>(ColorHelper.COLOR_SORTER);
        for(DyeColor color : DyeColor.values())
        {
            DeferredBlock<T> holder = register(name + "_" + EnumHelper.resourceSafeName(color), p -> factory.apply(p,color), p -> properties.apply(p,color));
            bundle.put(color,holder);
        }
        return bundle.lock();
    }

    public static <T extends Block> DeferredBlock<T> register(String name,Function<BlockBehaviour.Properties,T> factory,UnaryOperator<BlockBehaviour.Properties> properties) { return register(name,factory,properties,UnaryOperator.identity()); }
    public static <T extends Block> DeferredBlock<T> register(String name,Function<BlockBehaviour.Properties,T> factory,UnaryOperator<BlockBehaviour.Properties> properties,UnaryOperator<Item.Properties> itemProperties) { return register(name,factory,properties,BlockItem::new,itemProperties); }
    public static <T extends Block> DeferredBlock<T> register(String name,Function<BlockBehaviour.Properties,T> factory,UnaryOperator<BlockBehaviour.Properties> properties,BiFunction<Block,Item.Properties,Item> itemFactory,UnaryOperator<Item.Properties> itemProperties)
    {
        DeferredBlock<T> result = REGISTER.registerBlock(name,factory,properties);
        LCItems.register(name, p -> itemFactory.apply(result.get(),p), p -> itemProperties.apply(p).useBlockDescriptionPrefix());
        return result;
    }

}
