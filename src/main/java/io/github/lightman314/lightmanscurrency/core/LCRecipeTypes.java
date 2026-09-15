package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.coin_mint.CoinMintRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCRecipeTypes {
    private LCRecipeTypes() {}

    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE,LCApi.MODID);

    public static final DeferredHolder<RecipeType<?>,RecipeType<CoinMintRecipe>> COIN_MINT = register("coin_mint");

    public static <T extends Recipe<?>> DeferredHolder<RecipeType<?>,RecipeType<T>> register(String name) {
        return REGISTER.register(name,() -> new RecipeType<>() {
            @Override
            public String toString() {
                return name;
            }
        });
    }

}