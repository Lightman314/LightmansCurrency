package io.github.lightman314.lightmanscurrency.datagen.common.loot.packs;

import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import io.github.lightman314.lightmanscurrency.common.loot.entries.AncientCoinLoot;
import io.github.lightman314.lightmanscurrency.datagen.common.loot.LootTableProviderTemplate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class MiscellaneousLoot extends LootTableProviderTemplate {

    @Override
    protected void generateLootTables() {

        this.falling(ModBlocks.COINPILE_COPPER, ModItems.COIN_COPPER, 9);
        this.falling(ModBlocks.COINPILE_IRON, ModItems.COIN_IRON, 9);
        this.falling(ModBlocks.COINPILE_GOLD, ModItems.COIN_GOLD, 9);
        this.falling(ModBlocks.COINPILE_EMERALD, ModItems.COIN_EMERALD, 9);
        this.falling(ModBlocks.COINPILE_DIAMOND, ModItems.COIN_DIAMOND, 9);
        this.falling(ModBlocks.COINPILE_NETHERITE, ModItems.COIN_NETHERITE, 9);

        this.falling(ModBlocks.COINBLOCK_COPPER, ModItems.COIN_COPPER, 36);
        this.falling(ModBlocks.COINBLOCK_IRON, ModItems.COIN_IRON, 36);
        this.falling(ModBlocks.COINBLOCK_GOLD, ModItems.COIN_GOLD, 36);
        this.falling(ModBlocks.COINBLOCK_EMERALD, ModItems.COIN_EMERALD, 36);
        this.falling(ModBlocks.COINBLOCK_DIAMOND, ModItems.COIN_DIAMOND, 36);
        this.falling(ModBlocks.COINBLOCK_NETHERITE, ModItems.COIN_NETHERITE, 36);

        this.falling(ModBlocks.COINPILE_CHOCOLATE_COPPER, ModItems.COIN_CHOCOLATE_COPPER, 9);
        this.falling(ModBlocks.COINPILE_CHOCOLATE_IRON, ModItems.COIN_CHOCOLATE_IRON, 9);
        this.falling(ModBlocks.COINPILE_CHOCOLATE_GOLD, ModItems.COIN_CHOCOLATE_GOLD, 9);
        this.falling(ModBlocks.COINPILE_CHOCOLATE_EMERALD, ModItems.COIN_CHOCOLATE_EMERALD, 9);
        this.falling(ModBlocks.COINPILE_CHOCOLATE_DIAMOND, ModItems.COIN_CHOCOLATE_DIAMOND, 9);
        this.falling(ModBlocks.COINPILE_CHOCOLATE_NETHERITE, ModItems.COIN_CHOCOLATE_NETHERITE, 9);

        this.falling(ModBlocks.COINBLOCK_CHOCOLATE_COPPER, ModItems.COIN_CHOCOLATE_COPPER, 36);
        this.falling(ModBlocks.COINBLOCK_CHOCOLATE_IRON, ModItems.COIN_CHOCOLATE_IRON, 36);
        this.falling(ModBlocks.COINBLOCK_CHOCOLATE_GOLD, ModItems.COIN_CHOCOLATE_GOLD, 36);
        this.falling(ModBlocks.COINBLOCK_CHOCOLATE_EMERALD, ModItems.COIN_CHOCOLATE_EMERALD, 36);
        this.falling(ModBlocks.COINBLOCK_CHOCOLATE_DIAMOND, ModItems.COIN_CHOCOLATE_DIAMOND, 36);
        this.falling(ModBlocks.COINBLOCK_CHOCOLATE_NETHERITE, ModItems.COIN_CHOCOLATE_NETHERITE, 36);

        //Ancient Netherite Coins as a Loot Table for easier weighting elsewhere
        this.define(LCLootTables.MISC_ANCIENT_NETHERITE_COINS,LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_H))
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_E1))
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_R1))
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_O))
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_B))
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_R2))
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_I))
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_N))
                        .add(AncientCoinLoot.ancientCoin(AncientCoinType.NETHERITE_E2))
                ));

    }

    protected void falling(@Nonnull Supplier<? extends Block> block, @Nonnull Supplier<? extends ItemLike> item, int count)
    {
        ResourceLocation blockID = ForgeRegistries.BLOCKS.getKey(block.get());
        ResourceLocation tableID = blockID.withPrefix("blocks/falling/");
        this.define(tableID, LootTable.lootTable().withPool(
                LootPool.lootPool().add(
                        LootItem.lootTableItem(item.get())
                ).setRolls(ConstantValue.exactly(count))
        ));
    }

}