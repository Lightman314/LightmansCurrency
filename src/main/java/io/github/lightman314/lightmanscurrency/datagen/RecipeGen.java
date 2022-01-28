package io.github.lightman314.lightmanscurrency.datagen;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe.MintType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

public class RecipeGen extends RecipeProvider{

	public RecipeGen(DataGenerator generator)
	{
		super(generator);
	}
	
	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
	{
		
		//Coin recipes
		//Copper Coin (empty, as no copper ingot is present)
		//Iron Coin
		mint(consumer, Items.IRON_INGOT, ModItems.COIN_IRON);
		melt(consumer, Items.IRON_INGOT, ModItems.COIN_IRON);
		//Gold Coin
		mint(consumer, Items.GOLD_INGOT, ModItems.COIN_GOLD);
		melt(consumer, Items.GOLD_INGOT, ModItems.COIN_GOLD);
		//Emerald Coin
		mint(consumer, Items.EMERALD, ModItems.COIN_EMERALD);
		melt(consumer, Items.EMERALD, ModItems.COIN_EMERALD);
		//Diamond Coin
		mint(consumer, Items.DIAMOND, ModItems.COIN_DIAMOND);
		melt(consumer, Items.DIAMOND, ModItems.COIN_DIAMOND);
		//Netherite Coin
		mint(consumer, Items.NETHERITE_INGOT, ModItems.COIN_NETHERITE);
		melt(consumer, Items.NETHERITE_INGOT, ModItems.COIN_NETHERITE);
		
		//Wallet Upgrades
		//Copper -> Iron
		upgrade(consumer, ModItems.WALLET_COPPER, ModItems.WALLET_IRON, Ingredient.fromItems(ModItems.COIN_IRON));
		//Iron -> Gold (redstone material)
		upgrade(consumer, ModItems.WALLET_IRON, ModItems.WALLET_GOLD, Ingredient.fromItems(ModItems.COIN_GOLD));
		//Gold -> Emerald (ender pearl material)
		upgrade(consumer, ModItems.WALLET_GOLD, ModItems.WALLET_EMERALD, Ingredient.fromItems(ModItems.COIN_EMERALD));
		//Emerald -> Diamond
		upgrade(consumer, ModItems.WALLET_EMERALD, ModItems.WALLET_DIAMOND, Ingredient.fromItems(ModItems.COIN_DIAMOND));
		//Diamond -> Netherite
		upgrade(consumer, ModItems.WALLET_DIAMOND, ModItems.WALLET_NETHERITE, Ingredient.fromItems(ModItems.COIN_NETHERITE));
		
		
	}
	
	private static void mint(Consumer<IFinishedRecipe> consumer, IItemProvider material, IItemProvider coin)
	{
		CoinMintRecipeBuilder.minting(Ingredient.fromItems(material), coin).save(consumer, recipeId(MintType.MINT, coin));
	}
	
	private static void melt(Consumer<IFinishedRecipe> consumer, IItemProvider material, IItemProvider coin)
	{
		CoinMintRecipeBuilder.melting(Ingredient.fromItems(coin), material).save(consumer, recipeId(MintType.MELT, coin));
	}
	
	private static final String UPGRADE_GROUP = "wallet_upgrades";
	
	private static void upgrade(Consumer<IFinishedRecipe> consumer, IItemProvider walletIn, IItemProvider walletOut, Ingredient... materials)
	{
		WalletUpgradeRecipeBuilder.walletUpgrade(walletIn, walletOut, UPGRADE_GROUP, materials).save(consumer, upgradeId(walletOut));
	}
	
	protected static ResourceLocation recipeId(MintType type, IItemProvider coin)
	{
		String prefix = "coinmint_";
		if(type == MintType.MINT)
			prefix = "mint_";
		else if(type == MintType.MELT)
			prefix = "melt_";
		return recipeId(prefix, coin);
	}
	
	protected static ResourceLocation recipeId(String prefix, IItemProvider coin)
	{
		ResourceLocation coinItemID = coin.asItem().getRegistryName();
		return new ResourceLocation(coinItemID.getNamespace(), prefix + coinItemID.getPath());
	}
	
	protected static ResourceLocation upgradeId(IItemProvider wallet)
	{
		ResourceLocation walletItemID = wallet.asItem().getRegistryName();
		return new ResourceLocation(walletItemID.getNamespace(), "upgraded_" + walletItemID.getPath());
	}
	
}
