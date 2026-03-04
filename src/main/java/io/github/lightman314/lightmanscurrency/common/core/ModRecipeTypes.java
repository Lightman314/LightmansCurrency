package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE,LightmansCurrency.MODID);
	
	public static final Supplier<RecipeType<CoinMintRecipe>> COIN_MINT = register("coin_mint");

	public static final Supplier<RecipeType<TicketStationRecipe>> TICKET = register("ticket");

    public static <T extends Recipe<?>> DeferredHolder<RecipeType<?>,RecipeType<T>> register(String id) {
        return register(id,() -> RecipeType.simple(LightmansCurrency.id(id)));
    }
    public static <T extends Recipe<?>> DeferredHolder<RecipeType<?>,RecipeType<T>> register(String id,Supplier<RecipeType<T>> factory) {
        return REGISTER.register(id,factory);
    }
}
