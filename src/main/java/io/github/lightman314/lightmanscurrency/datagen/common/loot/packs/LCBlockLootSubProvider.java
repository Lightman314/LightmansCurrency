package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public class LCBlockLootSubProvider extends BlockLootSubProvider {

    private static final Set<Item> EXPLOSION_RESISTANT = Set.of();

    public LCBlockLootSubProvider(HolderLookup.Provider registries) { this(EXPLOSION_RESISTANT,FeatureFlags.REGISTRY.allFlags(),registries); }
    protected LCBlockLootSubProvider(Set<Item> explosionResistant, FeatureFlagSet enabledFeatures,HolderLookup.Provider registries) {
        super(explosionResistant,enabledFeatures,registries);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() { return this.getKnownBlocks(LCBlocks.REGISTER); }

    protected final Iterable<Block> getKnownBlocks(DeferredRegister<Block> register) { return register.getEntries().stream().map(Holder::value).toList(); }

    @Override
    protected void generate() {
        //Coin Block Loot
        this.coinPileAndBlock(LCItems.COIN_COPPER,LCBlocks.COIN_PILE_COPPER,LCBlocks.COIN_BLOCK_COPPER);
        this.coinPileAndBlock(LCItems.COIN_IRON,LCBlocks.COIN_PILE_IRON,LCBlocks.COIN_BLOCK_IRON);
        this.coinPileAndBlock(LCItems.COIN_GOLD,LCBlocks.COIN_PILE_GOLD,LCBlocks.COIN_BLOCK_GOLD);
        this.coinPileAndBlock(LCItems.COIN_EMERALD,LCBlocks.COIN_PILE_EMERALD,LCBlocks.COIN_BLOCK_EMERALD);
        this.coinPileAndBlock(LCItems.COIN_DIAMOND,LCBlocks.COIN_PILE_DIAMOND,LCBlocks.COIN_BLOCK_DIAMOND);
        this.coinPileAndBlock(LCItems.COIN_NETHERITE,LCBlocks.COIN_PILE_NETHERITE,LCBlocks.COIN_BLOCK_NETHERITE);
        //Chocolate Coin Block Loot
        this.coinPileAndBlock(LCItems.COIN_CHOCOLATE_COPPER,LCBlocks.COIN_PILE_CHOCOLATE_COPPER,LCBlocks.COIN_BLOCK_CHOCOLATE_COPPER);
        this.coinPileAndBlock(LCItems.COIN_CHOCOLATE_IRON,LCBlocks.COIN_PILE_CHOCOLATE_IRON,LCBlocks.COIN_BLOCK_CHOCOLATE_IRON);
        this.coinPileAndBlock(LCItems.COIN_CHOCOLATE_GOLD,LCBlocks.COIN_PILE_CHOCOLATE_GOLD,LCBlocks.COIN_BLOCK_CHOCOLATE_GOLD);
        this.coinPileAndBlock(LCItems.COIN_CHOCOLATE_EMERALD,LCBlocks.COIN_PILE_CHOCOLATE_EMERALD,LCBlocks.COIN_BLOCK_CHOCOLATE_EMERALD);
        this.coinPileAndBlock(LCItems.COIN_CHOCOLATE_DIAMOND,LCBlocks.COIN_PILE_CHOCOLATE_DIAMOND,LCBlocks.COIN_BLOCK_CHOCOLATE_DIAMOND);
        this.coinPileAndBlock(LCItems.COIN_CHOCOLATE_NETHERITE,LCBlocks.COIN_PILE_CHOCOLATE_NETHERITE,LCBlocks.COIN_BLOCK_CHOCOLATE_NETHERITE);
        //Coin Mint
        this.dropSelf(LCBlocks.COIN_MINT.get());
        //ATM
        this.tallDropSelf(LCBlocks.ATM.get());
        //Trading Terminal
        this.dropSelf(LCBlocks.TRADING_TERMINAL.get());
    }

    protected final void coinPileAndBlock(ItemLike coin,Holder<Block> pile,Holder<Block> block) { this.coinPileAndBlock(coin,pile.value(),block.value()); }
    protected final void coinPileAndBlock(ItemLike coin,Block pile,Block block) {
        this.add(pile,this.createSingleItemTableWithSilkTouch(pile,coin,ConstantValue.exactly(9)));
        this.add(block,this.createSingleItemTableWithSilkTouch(block,pile,ConstantValue.exactly(4)));
    }

    protected final void tallDropSelf(Block block) {
        this.add(block,LootTable.lootTable()
                .withPool(this.applyExplosionCondition(block,LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1f))
                        .add(LootItem.lootTableItem(block))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ITallBlock.ISBOTTOM,true)))))
        );
    }

}
