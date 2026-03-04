package io.github.lightman314.lightmanscurrency.common.core;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.crafting.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, LightmansCurrency.MODID);
	
	public static final Supplier<RecipeSerializer<WalletUpgradeRecipe>> WALLET_UPGRADE = register("crafting_wallet_upgrade",() -> WalletUpgradeRecipe.MAP_CODEC,() -> WalletUpgradeRecipe.STREAM_CODEC);
	public static final Supplier<RecipeSerializer<CoinMintRecipe>> COIN_MINT = register("coin_mint",() -> CoinMintRecipe.MAP_CODEC,() -> CoinMintRecipe.STREAM_CODEC);

	public static final Supplier<RecipeSerializer<TicketRecipe>> TICKET = register("ticket",() -> TicketRecipe.MAP_CODEC,() -> TicketRecipe.STREAM_CODEC);
	public static final Supplier<RecipeSerializer<MasterTicketRecipe>> TICKET_MASTER = register("ticket_master",() -> MasterTicketRecipe.MAP_CODEC,() -> MasterTicketRecipe.STREAM_CODEC);
	public static final Supplier<RecipeSerializer<CouponRecipe>> COUPON = register("coupon",() -> CouponRecipe.MAP_CODEC,() -> CouponRecipe.STREAM_CODEC);

    public static <T extends Recipe<?>> DeferredHolder<RecipeSerializer<?>,RecipeSerializer<T>> register(String id,Supplier<MapCodec<T>> codec,Supplier<StreamCodec<RegistryFriendlyByteBuf,T>> streamCodec) {
        return register(id,() -> new EasyRecipeSerializer<>(codec.get(),streamCodec.get()));
    }
    public static <T extends Recipe<?>> DeferredHolder<RecipeSerializer<?>,RecipeSerializer<T>> register(String id, Supplier<RecipeSerializer<T>> factory) {
        return REGISTER.register(id,factory);
    }

    private record EasyRecipeSerializer<T extends Recipe<?>>(MapCodec<T> codec,StreamCodec<RegistryFriendlyByteBuf,T> streamCodec) implements RecipeSerializer<T> { }

}
