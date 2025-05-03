package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.loot.functions.ModelVariantLootFunction;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.SimpleSubProvider;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BlockDropLoot extends SimpleSubProvider {

    public BlockDropLoot(HolderLookup.Provider lookup) { super(lookup); }

    @Override
    protected void generate() {

        this.tallBlock(ModBlocks.ATM);
        this.simpleBlock(ModBlocks.COIN_CHEST);
        this.simpleBlock(ModBlocks.CASH_REGISTER);
        this.simpleBlock(ModBlocks.COIN_MINT);
        this.simpleBlock(ModBlocks.GEM_TERMINAL);
        this.simpleBlock(ModBlocks.TERMINAL);
        this.simpleBlock(ModBlocks.TICKET_STATION);

        //Coin Blocks/Piles
        this.coinPilesAndBlocks(ModItems.COIN_COPPER, ModBlocks.COINPILE_COPPER, ModBlocks.COINBLOCK_COPPER);
        this.coinPilesAndBlocks(ModItems.COIN_IRON, ModBlocks.COINPILE_IRON, ModBlocks.COINBLOCK_IRON);
        this.coinPilesAndBlocks(ModItems.COIN_GOLD, ModBlocks.COINPILE_GOLD, ModBlocks.COINBLOCK_GOLD);
        this.coinPilesAndBlocks(ModItems.COIN_EMERALD, ModBlocks.COINPILE_EMERALD, ModBlocks.COINBLOCK_EMERALD);
        this.coinPilesAndBlocks(ModItems.COIN_DIAMOND, ModBlocks.COINPILE_DIAMOND, ModBlocks.COINBLOCK_DIAMOND);
        this.coinPilesAndBlocks(ModItems.COIN_NETHERITE, ModBlocks.COINPILE_NETHERITE, ModBlocks.COINBLOCK_NETHERITE);

        this.coinPilesAndBlocks(ModItems.COIN_CHOCOLATE_COPPER, ModBlocks.COINPILE_CHOCOLATE_COPPER, ModBlocks.COINBLOCK_CHOCOLATE_COPPER);
        this.coinPilesAndBlocks(ModItems.COIN_CHOCOLATE_IRON, ModBlocks.COINPILE_CHOCOLATE_IRON, ModBlocks.COINBLOCK_CHOCOLATE_IRON);
        this.coinPilesAndBlocks(ModItems.COIN_CHOCOLATE_GOLD, ModBlocks.COINPILE_CHOCOLATE_GOLD, ModBlocks.COINBLOCK_CHOCOLATE_GOLD);
        this.coinPilesAndBlocks(ModItems.COIN_CHOCOLATE_EMERALD, ModBlocks.COINPILE_CHOCOLATE_EMERALD, ModBlocks.COINBLOCK_CHOCOLATE_EMERALD);
        this.coinPilesAndBlocks(ModItems.COIN_CHOCOLATE_DIAMOND, ModBlocks.COINPILE_CHOCOLATE_DIAMOND, ModBlocks.COINBLOCK_CHOCOLATE_DIAMOND);
        this.coinPilesAndBlocks(ModItems.COIN_CHOCOLATE_NETHERITE, ModBlocks.COINPILE_CHOCOLATE_NETHERITE, ModBlocks.COINBLOCK_CHOCOLATE_NETHERITE);
    }

    protected ResourceLocation getBlockTable(@Nonnull Block block)
    {
        ResourceLocation blockID = BuiltInRegistries.BLOCK.getKey(block);
        return blockID.withPrefix("blocks/");
    }

    protected void simpleBlock(@Nonnull Supplier<? extends Block> block) { this.simpleBlock(block.get()); }
    protected void simpleBlock(@Nonnull Block block) {
        this.register(this.getBlockTable(block), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(block)
                                .apply(ModelVariantLootFunction.builder()))));
    }
    protected void tallBlock(@Nonnull Supplier<? extends Block> block) { this.tallBlock(block.get()); }
    protected void tallBlock(@Nonnull Block block)
    {
        this.register(this.getBlockTable(block),LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(block)
                                .apply(ModelVariantLootFunction.builder())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ITallBlock.ISBOTTOM,true))))
                        .when(ExplosionCondition.survivesExplosion())));
    }

    protected void coinPilesAndBlocks(@Nonnull Supplier<? extends ItemLike> coin, @Nonnull Supplier<? extends Block> pile, @Nonnull Supplier<? extends Block> block) { this.coinPilesAndBlocks(coin.get().asItem(), pile.get(), block.get());}
    protected void coinPilesAndBlocks(@Nonnull Item coin, @Nonnull Block pile, @Nonnull Block block)
    {
        //Coin Pile loot table
        this.register(this.getBlockTable(pile),LootTable.lootTable()
                .withPool(LootPool.lootPool().add(AlternativesEntry.alternatives(
                        LootItem.lootTableItem(pile)
                                .when(this.hasSilkTouch()),
                        LootItem.lootTableItem(coin)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(9)))
                ))));
        this.register(this.getBlockTable(block),LootTable.lootTable()
                .withPool(LootPool.lootPool().add(AlternativesEntry.alternatives(
                        LootItem.lootTableItem(block)
                                .when(this.hasSilkTouch()),
                        LootItem.lootTableItem(pile)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4)))
                ))));
    }

}
