package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCRecipeBookCategories {
    private LCRecipeBookCategories() {}

    public static final DeferredRegister<RecipeBookCategory> REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_BOOK_CATEGORY,LCApi.MODID);

    public static DeferredHolder<RecipeBookCategory,RecipeBookCategory> COIN_MINT = register("coin_mint");

    public static DeferredHolder<RecipeBookCategory,RecipeBookCategory> register(String name) {
        return REGISTER.register(name,RecipeBookCategory::new);
    }

}
