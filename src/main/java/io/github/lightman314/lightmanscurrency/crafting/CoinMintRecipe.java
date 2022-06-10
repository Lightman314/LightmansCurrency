package io.github.lightman314.lightmanscurrency.crafting;

import com.google.gson.JsonElement;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.core.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class CoinMintRecipe implements Recipe<Container>{

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
	
	public CoinMintRecipe(ResourceLocation id, MintType type, Ingredient ingredient, ItemLike result)
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
			return Config.SERVER.allowCoinMinting.get() && Config.canMint(this.result);
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
		if(this.ingredient.isEmpty() || this.result.asItem() == Items.AIR || !this.allowed())
			return false;
		return true;
	}
	
	@Override
	public boolean matches(Container inventory, Level level) {
		if(!this.isValid())
			return false;
		ItemStack firstStack = inventory.getItem(0);
		if(this.ingredient.test(firstStack))
			return true;
		return false;
	}
	
	@Override
	public ItemStack assemble(Container inventory) {
		return this.getResult();
	}
	
	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}
	
	@Override
	public ItemStack getResultItem() {
		return this.getResult();
	}
	
	@Override
	public ResourceLocation getId() {
		return this.id;
	}
	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.COIN_MINT.get();
	}
	@Override
	public RecipeType<?> getType() {
		return RecipeTypes.COIN_MINT.get();
	}
	
}
