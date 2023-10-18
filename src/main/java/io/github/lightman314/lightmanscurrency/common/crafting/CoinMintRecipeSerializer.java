package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;

public class CoinMintRecipeSerializer implements RecipeSerializer<CoinMintRecipe>{

	private static final Codec<CoinMintRecipe> CODEC = RecordCodecBuilder.create((b) ->
			b.group(
					ExtraCodecs.strictOptionalField(Codec.STRING, "mintType", "OTHER").forGetter(CoinMintRecipe::getMintTypeString),
					ExtraCodecs.strictOptionalField(Codec.INT, "duration", 0).forGetter(CoinMintRecipe::getInternalDuration),
					Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CoinMintRecipe::getIngredient),
					ExtraCodecs.strictOptionalField(Codec.INT, "count", 1).forGetter(CoinMintRecipe::getIngredientCount),
					CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(CoinMintRecipe::getOutputItem))
					.apply(b, (type,duration,ingredient,ingredientcount,result) -> new CoinMintRecipe(CoinMintRecipe.readType(type),duration,ingredient,ingredientcount,result)));

	@Nonnull
	@Override
	public Codec<CoinMintRecipe> codec() { return CODEC; }

	@Override
	public CoinMintRecipe fromNetwork(FriendlyByteBuf buffer) {
		CoinMintRecipe.MintType type = CoinMintRecipe.readType(buffer.readUtf());
		Ingredient ingredient = Ingredient.fromNetwork(buffer);
		int ingredientCount = buffer.readInt();
		ItemStack result = buffer.readItem();
		int duration = buffer.readInt();
		return new CoinMintRecipe(type, duration, ingredient, ingredientCount, result);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CoinMintRecipe recipe) {
		buffer.writeUtf(recipe.getMintType().name());
		recipe.getIngredient().toNetwork(buffer);
		buffer.writeInt(recipe.getIngredientCount());
		buffer.writeItemStack(recipe.getOutputItem(), false);
		buffer.writeInt(recipe.getInternalDuration());
	}

	
	
}
