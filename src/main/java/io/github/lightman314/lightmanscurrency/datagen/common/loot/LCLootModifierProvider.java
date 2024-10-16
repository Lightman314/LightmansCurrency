package io.github.lightman314.lightmanscurrency.datagen.common.loot;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.loot.glm.BonusItemModifier;
import io.github.lightman314.lightmanscurrency.common.loot.glm.CoinsInChestsModifier;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class LCLootModifierProvider extends GlobalLootModifierProvider {

    public LCLootModifierProvider(PackOutput output) { super(output, LightmansCurrency.MODID); }

    @Override
    protected void start() {

        this.add("coins_in_chests", CoinsInChestsModifier.INSTANCE);
        this.add("additions/netherite_wallet_in_bastions", BonusItemModifier.builder(ModItems.WALLET_NETHERITE.get(), 0.1f)
                .withTarget(BuiltInLootTables.BASTION_TREASURE)
                .build());

    }

}
