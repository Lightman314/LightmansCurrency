package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.loot.glm.CoinsInChestsModifier;
import io.github.lightman314.lightmanscurrency.common.loot.glm.BonusItemModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class LCLootModifierProvider extends GlobalLootModifierProvider {

    public LCLootModifierProvider(@Nonnull PackOutput output, @Nonnull CompletableFuture<HolderLookup.Provider> registries) { super(output, registries, LightmansCurrency.MODID); }

    @Override
    protected void start() {

        this.add("coins_in_chests", CoinsInChestsModifier.INSTANCE);
        this.add("additions/netherite_wallet_in_bastions", BonusItemModifier.builder(ModItems.WALLET_NETHERITE.get(), 0.1f)
                .withTarget(BuiltInLootTables.BASTION_TREASURE)
                .build());

    }

}
