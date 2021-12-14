package io.github.lightman314.lightmanscurrency.crafting;

import com.google.gson.JsonElement;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.core.ModRecipes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CoinMintRecipe implements IRecipe<IInventory>{

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
	
	private ResourceLocation id;
	private MintType type;
	private Ingredient ingredient;
	private Item result;
	
	public CoinMintRecipe(ResourceLocation id, MintType type, Ingredient ingredient, IItemProvider result)
	{
		this.id = id;
		this.type = type;
		this.ingredient = ingredient;
		this.result = result.asItem();
	}
	
	public Ingredient getIngredient() { return this.ingredient; }
	public ItemStack getResult() { if(this.isValid()) return new ItemStack(this.result); return ItemStack.EMPTY; }
	public MintType getMintType() { return this.type; }
	
	public boolean allowed()
	{
		if(this.type == MintType.MINT)
		{
			return Config.canMint() && Config.canMint(this.result);
		}
		else if(this.type == MintType.MELT)
		{
			try {
				return Config.canMelt() && Config.canMelt(this.ingredient.getMatchingStacks()[0].getItem());
			} catch(Exception e) { return false; }
		}
		return true;
	}
	
	public boolean isValid()
	{
		if(this.ingredient.hasNoMatchingItems() || this.result.asItem() == Items.AIR || !this.allowed())
			return false;
		return true;
	}
	
	@Override
	public boolean matches(IInventory inventory, World worldIn) {
		if(!this.isValid())
			return false;
		ItemStack firstStack = inventory.getStackInSlot(0);
		if(this.ingredient.test(firstStack))
			return true;
		return false;
	}
	
	@Override
	public ItemStack getCraftingResult(IInventory inventory) {
		return this.getResult();
	}
	
	@Override
	public boolean canFit(int width, int height) {
		return true;
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return this.getResult();
	}
	@Override
	public ResourceLocation getId() {
		return this.id;
	}
	@Override
	public IRecipeSerializer<?> getSerializer() {
		return ModRecipes.COIN_MINT;
	}
	@Override
	public IRecipeType<?> getType() {
		return RecipeType.COIN_MINT;
	}
	
}
