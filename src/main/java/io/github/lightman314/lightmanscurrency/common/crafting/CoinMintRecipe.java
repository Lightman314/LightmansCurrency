package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonElement;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class CoinMintRecipe implements IRecipe<IInventory> {

	public enum MintType { MINT, MELT, OTHER }


	public static MintType readType(JsonElement json)
	{
		try {
			return readType(json.getAsString());
		} catch(Exception e) { e.printStackTrace(); return MintType.OTHER; }
	}

	public static MintType readType(String typeName)
	{
		for(MintType type : MintType.values())
		{
			if(type.name().equals(typeName))
				return type;
		}
		return MintType.OTHER;
	}

	private final ResourceLocation id;
	private final MintType type;
	private final Ingredient ingredient;
	public final int ingredientCount;
	private final ItemStack result;

	public CoinMintRecipe(ResourceLocation id, MintType type, Ingredient ingredient, int ingredientCount, ItemStack result)
	{
		this.id = id;
		this.type = type;
		this.ingredient = ingredient;
		this.ingredientCount = Math.max(ingredientCount,1); //Force count to be > 0
		this.result = result;
	}

	public Ingredient getIngredient() { return this.ingredient; }
	public MintType getMintType() { return this.type; }

	public boolean allowed()
	{
		if(this.type == MintType.MINT)
		{
			return Config.SERVER.allowCoinMinting.get() && Config.canMint(this.result.getItem());
		}
		else if(this.type == MintType.MELT)
		{
			try {
				return Config.SERVER.allowCoinMelting.get() && Config.canMelt(this.ingredient.getItems()[0].getItem());
			} catch(Exception e) { return false; }
		}
		return true;
	}

	public boolean isValid()
	{
		return !this.ingredient.isEmpty() && this.result.getItem() != Items.AIR && this.allowed();
	}

	@Override
	public boolean matches(@Nonnull IInventory inventory, @Nonnull World level) {
		if(!this.isValid())
			return false;
		ItemStack firstStack = inventory.getItem(0);
		return this.ingredient.test(firstStack);
	}

	@Override
	public @Nonnull ItemStack assemble(@Nonnull IInventory inventory) {
		return this.getResultItem();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) { return true; }

	@Override
	public @Nonnull ItemStack getResultItem() { if(this.isValid()) return this.result.copy(); return ItemStack.EMPTY; }

	@Override
	public @Nonnull ResourceLocation getId() { return this.id; }

	@Override
	public @Nonnull IRecipeSerializer<?> getSerializer() { return ModRecipes.COIN_MINT.get(); }
	@Override
	public @Nonnull IRecipeType<?> getType() { return RecipeTypes.COIN_MINT; }

}