package io.github.lightman314.lightmanscurrency.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class CoinMintRecipe implements Recipe<Container>{

	public enum MintType { MINT, MELT, OTHER }
	
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
	private final int duration;
	private final Ingredient ingredient;
	public final int ingredientCount;
	private final ItemStack result;
	
	public CoinMintRecipe(ResourceLocation id, MintType type, int duration, Ingredient ingredient, int ingredientCount, ItemStack result)
	{
		this.id = id;
		this.type = type;
		this.duration = duration;
		this.ingredient = ingredient;
		this.ingredientCount = Math.max(ingredientCount,1); //Force count to be > 0
		this.result = result;
	}
	
	public Ingredient getIngredient() { return this.ingredient; }
	public MintType getMintType() { return this.type; }
	
	public boolean allowed()
	{
		return LCConfig.SERVER.allowCoinMintRecipe(this);
	}
	
	public boolean isValid() { return !this.ingredient.isEmpty() && this.result.getItem() != Items.AIR && this.allowed(); }
	
	@Override
	public boolean matches(@Nonnull Container inventory, @Nonnull Level level) {
		if(!this.isValid())
			return false;
		ItemStack firstStack = inventory.getItem(0);
		return this.ingredient.test(firstStack);
	}
	
	@Override
	public @Nonnull ItemStack assemble(@Nonnull Container inventory, @Nonnull RegistryAccess registryAccess) { return this.getResultItem(registryAccess); }
	
	@Override
	public boolean canCraftInDimensions(int width, int height) { return true; }

	public ItemStack getOutputItem() { return this.result.copy(); }

	@Override
	public @Nonnull ItemStack getResultItem(@Nonnull RegistryAccess registryAccess) { if(this.isValid()) return this.result.copy(); return ItemStack.EMPTY; }

	public int getInternalDuration() { return this.duration; }
	public int getDuration() { return this.duration > 0 ? this.duration : LCConfig.SERVER.coinMintDefaultDuration.get(); }

	@Override
	public @Nonnull ResourceLocation getId() { return this.id; }

	@Override
	public @Nonnull RecipeSerializer<?> getSerializer() { return ModRecipes.COIN_MINT.get(); }
	@Override
	public @Nonnull RecipeType<?> getType() { return RecipeTypes.COIN_MINT.get(); }

	public static class Serializer implements RecipeSerializer<CoinMintRecipe>{

		@Nonnull
		@Override
		public CoinMintRecipe fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
			Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
			int ingredientCount = GsonHelper.getAsInt(json, "count", 1);

			ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
			if(result.isEmpty())
				throw new JsonSyntaxException("Result is empty.");
			MintType type = MintType.OTHER;
			if(json.has("mintType"))
				type = CoinMintRecipe.readType(GsonHelper.getAsString(json, "mintType", "OTHER"));

			int duration = GsonHelper.getAsInt(json, "duration", 0);

			return new CoinMintRecipe(recipeId, type, duration, ingredient, ingredientCount, result);
		}

		@Override
		public CoinMintRecipe fromNetwork(@Nonnull ResourceLocation recipeId, FriendlyByteBuf buffer) {
			CoinMintRecipe.MintType type = CoinMintRecipe.readType(buffer.readUtf());
			Ingredient ingredient = Ingredient.fromNetwork(buffer);
			int ingredientCount = buffer.readInt();
			ItemStack result = buffer.readItem();
			int duration = buffer.readInt();
			return new CoinMintRecipe(recipeId, type, duration, ingredient, ingredientCount, result);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, CoinMintRecipe recipe) {
			buffer.writeUtf(recipe.getMintType().name());
			recipe.getIngredient().toNetwork(buffer);
			buffer.writeInt(recipe.ingredientCount);
			buffer.writeItemStack(recipe.getOutputItem(), false);
			buffer.writeInt(recipe.getInternalDuration());
		}

	}
	
}
