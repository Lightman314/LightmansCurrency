package io.github.lightman314.lightmanscurrency.features.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.loot.tiers.ChestPoolLevel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class ConfiguredCoinLootModifier implements IGlobalLootModifier {

    public static final MapCodec<ConfiguredCoinLootModifier> CODEC = Codec.INT.optionalFieldOf("priority",0)
            .xmap(ConfiguredCoinLootModifier::new,m -> m.priority);

    private final int priority;
    public ConfiguredCoinLootModifier() { this(0); }
    public ConfiguredCoinLootModifier(int priority) { this.priority = priority; }

    @Override
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot,LootContext context) {
        //If chest loot is disabled, do nothing
        if(LCConfig.COMMON.enableChestLoot.get())
            return generatedLoot;
        ChestPoolLevel lootLevel = LootManager.INSTANCE.getChestPoolLevel(context.getQueriedLootTableId());
        if(lootLevel != null) {
            try{
                generatedLoot.addAll(LootManager.INSTANCE.getLoot(lootLevel.lootTable,context));
            } catch (Exception ignored) {} //Emergency catch just in case someone configured a non-chest loot table and it made things explode
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }

    @Override
    public int priority() { return this.priority; }

}
