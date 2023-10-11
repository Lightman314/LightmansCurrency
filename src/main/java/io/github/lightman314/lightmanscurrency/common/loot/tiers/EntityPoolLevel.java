package io.github.lightman314.lightmanscurrency.common.loot.tiers;

import io.github.lightman314.lightmanscurrency.common.loot.LCLootTables;
import net.minecraft.resources.ResourceLocation;

public enum EntityPoolLevel
{
    T1(false, LCLootTables.ENTITY_DROPS_T1),
    T2(false, LCLootTables.ENTITY_DROPS_T2),
    T3(false, LCLootTables.ENTITY_DROPS_T3),
    T4(false, LCLootTables.ENTITY_DROPS_T4),
    T5(false, LCLootTables.ENTITY_DROPS_T5),
    T6(false, LCLootTables.ENTITY_DROPS_T6),
    BOSS_T1(true, LCLootTables.BOSS_DROPS_T1),
    BOSS_T2(true, LCLootTables.BOSS_DROPS_T2),
    BOSS_T3(true, LCLootTables.BOSS_DROPS_T3),
    BOSS_T4(true, LCLootTables.BOSS_DROPS_T4),
    BOSS_T5(true, LCLootTables.BOSS_DROPS_T5),
    BOSS_T6(true, LCLootTables.BOSS_DROPS_T6);

    public final boolean isBoss;
    public final ResourceLocation lootTable;
    EntityPoolLevel(boolean isBoss, ResourceLocation lootTable) { this.isBoss = isBoss; this.lootTable = lootTable; }

}
