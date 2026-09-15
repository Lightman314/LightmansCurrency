package io.github.lightman314.lightmanscurrency.api.loot;

import io.github.lightman314.lightmanscurrency.api.loot.modifiers.IBonusLootModifier;
import io.github.lightman314.lightmanscurrency.api.loot.tiers.ChestPoolLevel;
import io.github.lightman314.lightmanscurrency.api.loot.tiers.EntityPoolLevel;
import io.github.lightman314.lightmanscurrency.features.loot.LootManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import javax.annotation.Nullable;
import java.util.List;

public interface LootAPI {

    static LootAPI get() { return LootManager.INSTANCE; }

    void addLootModifier(IBonusLootModifier modifier);
    void removeLootModifier(IBonusLootModifier modifier);

    List<ItemStack> getLoot(Identifier lootTable, LootContext context);

    void applyLootModifiers(MinecraftServer server,List<ItemStack> loot);

    @Nullable
    ChestPoolLevel getChestPoolLevel(Identifier table);
    @Nullable
    EntityPoolLevel getEntityPoolLevel(EntityType<?> entity);

}