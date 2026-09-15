package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.SimpleSubProvider;
import io.github.lightman314.lightmanscurrency.features.coins.FallingCoinBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.function.Supplier;

public class LCFallingBlockLootSubProvider extends SimpleSubProvider {

    public LCFallingBlockLootSubProvider(HolderLookup.Provider registries) { super(registries); }

    @Override
    protected void generate() {
        //Coin Pile
        this.falling(LCBlocks.COIN_PILE_COPPER,LCItems.COIN_COPPER,9);
        this.falling(LCBlocks.COIN_PILE_IRON,LCItems.COIN_IRON,9);
        this.falling(LCBlocks.COIN_PILE_GOLD,LCItems.COIN_GOLD,9);
        this.falling(LCBlocks.COIN_PILE_EMERALD,LCItems.COIN_EMERALD,9);
        this.falling(LCBlocks.COIN_PILE_DIAMOND,LCItems.COIN_DIAMOND,9);
        this.falling(LCBlocks.COIN_PILE_NETHERITE,LCItems.COIN_NETHERITE,9);
        //Coin Block
        this.falling(LCBlocks.COIN_BLOCK_COPPER,LCItems.COIN_COPPER,36);
        this.falling(LCBlocks.COIN_BLOCK_IRON,LCItems.COIN_IRON,36);
        this.falling(LCBlocks.COIN_BLOCK_GOLD,LCItems.COIN_GOLD,36);
        this.falling(LCBlocks.COIN_BLOCK_EMERALD,LCItems.COIN_EMERALD,36);
        this.falling(LCBlocks.COIN_BLOCK_DIAMOND,LCItems.COIN_DIAMOND,36);
        this.falling(LCBlocks.COIN_BLOCK_NETHERITE,LCItems.COIN_NETHERITE,36);
        //Chocolate Coin Pile
        this.falling(LCBlocks.COIN_PILE_CHOCOLATE_COPPER,LCItems.COIN_CHOCOLATE_COPPER,9);
        this.falling(LCBlocks.COIN_PILE_CHOCOLATE_IRON,LCItems.COIN_CHOCOLATE_IRON,9);
        this.falling(LCBlocks.COIN_PILE_CHOCOLATE_GOLD,LCItems.COIN_CHOCOLATE_GOLD,9);
        this.falling(LCBlocks.COIN_PILE_CHOCOLATE_EMERALD,LCItems.COIN_CHOCOLATE_EMERALD,9);
        this.falling(LCBlocks.COIN_PILE_CHOCOLATE_DIAMOND,LCItems.COIN_CHOCOLATE_DIAMOND,9);
        this.falling(LCBlocks.COIN_PILE_CHOCOLATE_NETHERITE,LCItems.COIN_CHOCOLATE_NETHERITE,9);
        //Coin Block
        this.falling(LCBlocks.COIN_BLOCK_CHOCOLATE_COPPER,LCItems.COIN_CHOCOLATE_COPPER,36);
        this.falling(LCBlocks.COIN_BLOCK_CHOCOLATE_IRON,LCItems.COIN_CHOCOLATE_IRON,36);
        this.falling(LCBlocks.COIN_BLOCK_CHOCOLATE_GOLD,LCItems.COIN_CHOCOLATE_GOLD,36);
        this.falling(LCBlocks.COIN_BLOCK_CHOCOLATE_EMERALD,LCItems.COIN_CHOCOLATE_EMERALD,36);
        this.falling(LCBlocks.COIN_BLOCK_CHOCOLATE_DIAMOND,LCItems.COIN_CHOCOLATE_DIAMOND,36);
        this.falling(LCBlocks.COIN_BLOCK_CHOCOLATE_NETHERITE,LCItems.COIN_CHOCOLATE_NETHERITE,36);
    }

    protected final void falling(Supplier<? extends FallingCoinBlock> block,ItemLike item,int count) { this.falling(block.get(),item,count); }
    protected final void falling(Supplier<? extends FallingCoinBlock> block,ItemLike item,NumberProvider count) { this.falling(block.get(),item,count); }
    protected final void falling(FallingCoinBlock block,ItemLike item,int count) { this.falling(block,item,ConstantValue.exactly(count));}
    protected final void falling(FallingCoinBlock block,ItemLike item,NumberProvider count) {
        Identifier tableID = BuiltInRegistries.BLOCK.getKey(block).withPrefix("blocks/falling/");
        this.add(tableID, LootTable.lootTable().withPool(
                LootPool.lootPool().add(
                        LootItem.lootTableItem(item)
                ).setRolls(count)));
    }

}
