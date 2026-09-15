package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.coin_mint.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.features.wallet.crafting.WalletUpgradeRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCRecipeSerializers {
    private LCRecipeSerializers() {}

    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER,LCApi.MODID);

    static {
        register("wallet_upgrade",WalletUpgradeRecipe.SERIALIZER);
        register("coin_mint",CoinMintRecipe.SERIALIZER);
    }

    private static void register(String name,RecipeSerializer<?> serializer) {
        REGISTER.register(name,() -> serializer);
    }

}