package io.github.lightman314.lightmanscurrency.core;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.ColorHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AdvancedDeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AdvancedDeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import io.github.lightman314.lightmanscurrency.features.atm.ATMBlock;
import io.github.lightman314.lightmanscurrency.features.coin_mint.CoinMintBlock;
import io.github.lightman314.lightmanscurrency.features.coins.FallingCoinBlock;
import io.github.lightman314.lightmanscurrency.features.coins.FallingCoinPile;
import io.github.lightman314.lightmanscurrency.features.colors.ColorDisplay;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific.CardDisplayBlock;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.specific.DisplayCaseBlock;
import net.minecraft.util.Util;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.*;

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

    private static final SoundType CHOCOLATE_SOUND = SoundType.MUD_BRICKS;

    public static final DeferredBlock<FallingCoinPile> COIN_PILE_CHOCOLATE_COPPER = register("chocolate_copper_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.COLOR_ORANGE)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_CHOCOLATE_IRON = register("chocolate_iron_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.METAL)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_CHOCOLATE_GOLD = register("chocolate_gold_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.GOLD)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_CHOCOLATE_EMERALD = register("chocolate_emerald_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.EMERALD)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_CHOCOLATE_DIAMOND = register("chocolate_diamond_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.DIAMOND)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinPile> COIN_PILE_CHOCOLATE_NETHERITE = register("chocolate_netherite_coin_pile",FallingCoinPile::new,p ->
            p.mapColor(MapColor.COLOR_BLACK)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));

    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_CHOCOLATE_COPPER = register("chocolate_copper_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.COLOR_ORANGE)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_CHOCOLATE_IRON = register("chocolate_iron_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.METAL)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_CHOCOLATE_GOLD = register("chocolate_gold_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.GOLD)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_CHOCOLATE_EMERALD = register("chocolate_emerald_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.EMERALD)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_CHOCOLATE_DIAMOND = register("chocolate_diamond_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.DIAMOND)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));
    public static final DeferredBlock<FallingCoinBlock> COIN_BLOCK_CHOCOLATE_NETHERITE = register("chocolate_netherite_coin_block",FallingCoinBlock::new,p ->
            p.mapColor(MapColor.COLOR_BLACK)
                    .strength(3f,6f)
                    .sound(CHOCOLATE_SOUND));


    //Machines
    public static final DeferredBlock<CoinMintBlock> COIN_MINT = register("coin_mint",CoinMintBlock::new,p ->
            p.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.5f)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    public static final DeferredBlock<ATMBlock> ATM = register("atm",ATMBlock::new,p ->
            p.mapColor(MapColor.COLOR_GRAY)
                    .strength(3f,6f)
                    .sound(SoundType.METAL));

    //Traders
    public static final DeferredHolderBundle<VanillaColor,Block,DisplayCaseBlock> DISPLAY_CASE = registerColored("display_case",DisplayCaseBlock::new,(p, color) ->
            p.mapColor(color.color)
                    .strength(2.0f,Float.POSITIVE_INFINITY)
                    .sound(SoundType.GLASS)
                    .noOcclusion());

    public static final DeferredHolderBundle2<WoodType,VanillaColor,Block,CardDisplayBlock> CARD_DISPLAY = registerWoodenAndColored("card_display",CardDisplayBlock::new,(p,t,c) ->
            p.mapColor(t.mapColor)
                    .strength(2.0f,Float.POSITIVE_INFINITY)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .overrideDescription(Util.makeDescriptionId("block",LCApi.id(t.generateID("card_display"))))
            ,WoodType.Attributes.NEEDS_LOG_AND_PLANKS,
            (b,p,t,c) -> new BlockItem(b,p),
            (p,type,c) -> p.overrideDescription(Util.makeDescriptionId("block",LCApi.id(type.generateID("card_display"))))
                    .component(LCDataComponents.COLOR_DISPLAY.get(),ColorDisplay.of(c)));

    public static <T extends Block> DeferredHolderBundle<VanillaColor,Block,T> registerColored(String name,BiFunction<BlockBehaviour.Properties,VanillaColor,T> factory,BiFunction<BlockBehaviour.Properties,VanillaColor,BlockBehaviour.Properties> properties) {
        return registerColored(name,factory,properties,(b,p,c) -> new BlockItem(b,p),(p,c) -> p);
    }
    public static <T extends Block> DeferredHolderBundle<VanillaColor,Block,T> registerColored(String name,BiFunction<BlockBehaviour.Properties,VanillaColor,T> factory,BiFunction<BlockBehaviour.Properties,VanillaColor,BlockBehaviour.Properties> properties,Function3<Block,Item.Properties,VanillaColor,Item> itemFactory,BiFunction<Item.Properties,VanillaColor,Item.Properties> itemProperties) {
        DeferredHolderBundle<VanillaColor,Block,T> bundle = new DeferredHolderBundle<>(ColorHelper.COLOR_SORTER);
        for(VanillaColor color : VanillaColor.values())
        {
            DeferredBlock<T> holder = register(name + "_" + EnumHelper.resourceSafeName(color), p -> factory.apply(p,color), p -> properties.apply(p,color),(b,p) -> itemFactory.apply(b,p,color),p -> itemProperties.apply(p,color));
            bundle.put(color,holder);
        }
        return bundle.lock();
    }

    public static <T extends Block> AdvancedDeferredHolderBundle<WoodType,Block,T> registerWooden(String name,BiFunction<BlockBehaviour.Properties,WoodType,T> factory,BiFunction<BlockBehaviour.Properties,WoodType,BlockBehaviour.Properties> properties,Predicate<WoodType.Attributes> requirement) {
        return registerWooden(name,factory,properties,requirement,(b,p,t) -> new BlockItem(b,p),(p,t) -> p);
    }
    public static <T extends Block> AdvancedDeferredHolderBundle<WoodType,Block,T> registerWooden(String name,BiFunction<BlockBehaviour.Properties,WoodType,T> factory,BiFunction<BlockBehaviour.Properties,WoodType,BlockBehaviour.Properties> properties,Predicate<WoodType.Attributes> requirement,Function3<Block,Item.Properties,WoodType,Item> itemFactory,BiFunction<Item.Properties,WoodType,Item.Properties> itemProperties) {
        AdvancedDeferredHolderBundle<WoodType,Block,T> bundle = new AdvancedDeferredHolderBundle<>(WoodType::sortByWood,type -> register(type.generateID(name),p -> factory.apply(p,type),p -> properties.apply(p,type),(b,p) -> itemFactory.apply(b,p,type),p -> itemProperties.apply(p,type)));
        Consumer<WoodType> consumer = WoodType.Attributes.filteredConsumer(bundle::registerKey,requirement);
        //Register already known types
        WoodType.validValues().forEach(consumer);
        //Register listener so that any new types will be registered as well
        WoodType.registerListener(consumer);
        return bundle;
    }

    public static <T extends Block>AdvancedDeferredHolderBundle2<WoodType,VanillaColor,Block,T> registerWoodenAndColored(String name, Function3<BlockBehaviour.Properties,WoodType,VanillaColor,T> factory, Function3<BlockBehaviour.Properties,WoodType,VanillaColor,BlockBehaviour.Properties> properties,Predicate<WoodType.Attributes> requirement) {
        return registerWoodenAndColored(name,factory,properties,requirement,(b,p,t,c) -> new BlockItem(b,p),(p,t,c) -> p);
    }
    public static <T extends Block>AdvancedDeferredHolderBundle2<WoodType,VanillaColor,Block,T> registerWoodenAndColored(String name, Function3<BlockBehaviour.Properties,WoodType,VanillaColor,T> factory, Function3<BlockBehaviour.Properties,WoodType,VanillaColor,BlockBehaviour.Properties> properties, Predicate<WoodType.Attributes> requirement,Function4<Block,Item.Properties,WoodType,VanillaColor,Item> itemFactory, Function3<Item.Properties,WoodType,VanillaColor,Item.Properties> itemProperties) {
        AdvancedDeferredHolderBundle2<WoodType,VanillaColor,Block,T> bundle = new AdvancedDeferredHolderBundle2<>(WoodType::sortByWood,ColorHelper.COLOR_SORTER, ImmutableList.copyOf(VanillaColor.values()),(type, color) ->
                register(type.generateID(name) + "_" + EnumHelper.resourceSafeName(color),p -> factory.apply(p,type,color),p -> properties.apply(p,type,color),(b,p) -> itemFactory.apply(b,p,type,color),p -> itemProperties.apply(p,type,color))
        );
        Consumer<WoodType> consumer = WoodType.Attributes.filteredConsumer(bundle::registerKey,requirement);
        //Register already known types
        WoodType.validValues().forEach(consumer);
        //Register listener so that any new types will be registered as well
        WoodType.registerListener(consumer);
        return bundle;
    }

    public static <T extends Block> DeferredBlock<T> register(String name,Function<BlockBehaviour.Properties,T> factory,UnaryOperator<BlockBehaviour.Properties> properties) { return register(name,factory,properties,UnaryOperator.identity()); }
    public static <T extends Block> DeferredBlock<T> register(String name,Function<BlockBehaviour.Properties,T> factory,UnaryOperator<BlockBehaviour.Properties> properties,UnaryOperator<Item.Properties> itemProperties) { return register(name,factory,properties,BlockItem::new,itemProperties); }
    public static <T extends Block> DeferredBlock<T> register(String name,Function<BlockBehaviour.Properties,T> factory,UnaryOperator<BlockBehaviour.Properties> properties,BiFunction<Block,Item.Properties,Item> itemFactory,UnaryOperator<Item.Properties> itemProperties)
    {
        DeferredBlock<T> result = REGISTER.registerBlock(name,factory,properties);
        LCItems.register(name, p -> itemFactory.apply(result.get(),p), p -> itemProperties.apply(p.useBlockDescriptionPrefix()));
        return result;
    }

}
