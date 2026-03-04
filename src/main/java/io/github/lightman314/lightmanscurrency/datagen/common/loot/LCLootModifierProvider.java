package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.loot.glm.CoinsInChestsModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class LCLootModifierProvider extends GlobalLootModifierProvider {

    public LCLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) { super(output, registries, LightmansCurrency.MODID); }

    @Override
    protected void start() {

        this.add("coins_in_chests",CoinsInChestsModifier.INSTANCE);

        this.add("additions/netherite_wallet_in_bastions", new AddTableLootModifier(
                new LootItemCondition[] { LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/bastion_treasure")).build() },
                ResourceKey.create(Registries.LOOT_TABLE,LightmansCurrency.id("chests/additions/bastion_treasure"))));

        this.add("additions/ender_dragon_wallet_in_end_cities",new AddTableLootModifier(
                new LootItemCondition[] { LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("chests/end_city_treasure")).build() },
                ResourceKey.create(Registries.LOOT_TABLE,LightmansCurrency.id("chests/additions/end_city_treasure"))));

    }

}
