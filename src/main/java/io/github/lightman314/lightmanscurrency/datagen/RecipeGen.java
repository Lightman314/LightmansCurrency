package io.github.lightman314.lightmanscurrency.datagen;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe.MintType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeGen extends RecipeProvider{

	public RecipeGen(DataGenerator generator)
	{
		super(generator);
	}
	
	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer)
	{
		
		//Coin recipes
		//Copper Coin
		mint(consumer, Items.COPPER_INGOT, ModItems.COIN_COPPER.get());
		melt(consumer, Items.COPPER_INGOT, ModItems.COIN_COPPER.get());
		//Iron Coin
		mint(consumer, Items.IRON_INGOT, ModItems.COIN_IRON.get());
		melt(consumer, Items.IRON_INGOT, ModItems.COIN_IRON.get());
		//Gold Coin
		mint(consumer, Items.GOLD_INGOT, ModItems.COIN_GOLD.get());
		melt(consumer, Items.GOLD_INGOT, ModItems.COIN_GOLD.get());
		//Emerald Coin
		mint(consumer, Items.EMERALD, ModItems.COIN_EMERALD.get());
		melt(consumer, Items.EMERALD, ModItems.COIN_EMERALD.get());
		//Diamond Coin
		mint(consumer, Items.DIAMOND, ModItems.COIN_DIAMOND.get());
		melt(consumer, Items.DIAMOND, ModItems.COIN_DIAMOND.get());
		//Netherite Coin
		mint(consumer, Items.NETHERITE_INGOT, ModItems.COIN_NETHERITE.get());
		melt(consumer, Items.NETHERITE_INGOT, ModItems.COIN_NETHERITE.get());
		
		//Wallet Upgrades
		//Copper -> Iron
		upgrade(consumer, ModItems.WALLET_COPPER.get(), ModItems.WALLET_IRON.get(), Ingredient.of(ModItems.COIN_IRON.get()));
		//Iron -> Gold (redstone material)
		upgrade(consumer, ModItems.WALLET_IRON.get(), ModItems.WALLET_GOLD.get(), Ingredient.of(ModItems.COIN_GOLD.get()));
		//Gold -> Emerald (ender pearl material)
		upgrade(consumer, ModItems.WALLET_GOLD.get(), ModItems.WALLET_EMERALD.get(), Ingredient.of(ModItems.COIN_EMERALD.get()));
		//Emerald -> Diamond
		upgrade(consumer, ModItems.WALLET_EMERALD.get(), ModItems.WALLET_DIAMOND.get(), Ingredient.of(ModItems.COIN_DIAMOND.get()));
		//Diamond -> Netherite
		upgrade(consumer, ModItems.WALLET_DIAMOND.get(), ModItems.WALLET_NETHERITE.get(), Ingredient.of(ModItems.COIN_NETHERITE.get()));
		
		
	}
	
	private static void mint(Consumer<FinishedRecipe> consumer, ItemLike material, ItemLike coin)
	{
		CoinMintRecipeBuilder.minting(Ingredient.of(material), coin).save(consumer, recipeId(MintType.MINT, coin));
	}
	
	private static void melt(Consumer<FinishedRecipe> consumer, ItemLike material, ItemLike coin)
	{
		CoinMintRecipeBuilder.melting(Ingredient.of(coin), material).save(consumer, recipeId(MintType.MELT, coin));
	}
	
	private static final String UPGRADE_GROUP = "wallet_upgrades";
	
	private static void upgrade(Consumer<FinishedRecipe> consumer, ItemLike walletIn, ItemLike walletOut, Ingredient... materials)
	{
		WalletUpgradeRecipeBuilder.walletUpgrade(walletIn, walletOut, UPGRADE_GROUP, materials).save(consumer, upgradeId(walletOut));
	}
	
	protected static ResourceLocation recipeId(MintType type, ItemLike coin)
	{
		String prefix = "coinmint_";
		if(type == MintType.MINT)
			prefix = "mint_";
		else if(type == MintType.MELT)
			prefix = "melt_";
		return recipeId(prefix, coin);
	}
	
	protected static ResourceLocation recipeId(String prefix, ItemLike coin)
	{
		ResourceLocation coinItemID = ForgeRegistries.ITEMS.getKey(coin.asItem());
		return new ResourceLocation(coinItemID.getNamespace(), prefix + coinItemID.getPath());
	}
	
	protected static ResourceLocation upgradeId(ItemLike wallet)
	{
		ResourceLocation walletItemID = ForgeRegistries.ITEMS.getKey(wallet.asItem());
		return new ResourceLocation(walletItemID.getNamespace(), "upgraded_" + walletItemID.getPath());
	}
	
}
