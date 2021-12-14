package io.github.lightman314.lightmanscurrency.core;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipeSerializer;
import io.github.lightman314.lightmanscurrency.crafting.WalletUpgradeRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipes {

	private static final List<IRecipeSerializer<?>> SERIALIZERS = new ArrayList<>();
	
	public static final IRecipeSerializer<WalletUpgradeRecipe> WALLET_UPGRADE = register("crafting_wallet_upgrade", new WalletUpgradeRecipe.Serializer());
	public static final IRecipeSerializer<CoinMintRecipe> COIN_MINT = register("coin_mint", new CoinMintRecipeSerializer());
	
	
	private static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(String name, S recipeSerializer)
	{
		if(recipeSerializer instanceof ForgeRegistryEntry<?>)
		{
			((ForgeRegistryEntry<?>)recipeSerializer).setRegistryName(name);
		}
		else
			recipeSerializer.setRegistryName(new ResourceLocation(name));
		SERIALIZERS.add(recipeSerializer);
		return recipeSerializer;
	}
	
	@SubscribeEvent
	public static void registerRecipeSerializers(final RegistryEvent.Register<IRecipeSerializer<?>> event)
	{
		SERIALIZERS.forEach(recipe -> event.getRegistry().register(recipe));
		SERIALIZERS.clear();
	}
	
}
