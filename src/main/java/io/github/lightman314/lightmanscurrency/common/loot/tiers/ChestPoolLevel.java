package io.github.lightman314.lightmanscurrency.common.loot.tiers;

import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import net.minecraft.resources.ResourceLocation;

public enum ChestPoolLevel {
    T1(LCLootTables.CHEST_DROPS_T1),
    T2(LCLootTables.CHEST_DROPS_T2),
    T3(LCLootTables.CHEST_DROPS_T3),
    T4(LCLootTables.CHEST_DROPS_T4),
    T5(LCLootTables.CHEST_DROPS_T5),
    T6(LCLootTables.CHEST_DROPS_T6);
    public final ResourceLocation lootTable;
    ChestPoolLevel(ResourceLocation lootTable) { this.lootTable = lootTable; }
}
