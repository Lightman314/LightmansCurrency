package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class CoinMintRecipe implements Recipe<SingleRecipeInput>{

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

	private final MintType type;
	private final int duration;
	private final Ingredient ingredient;
	public final int ingredientCount;
	private final ItemStack result;
	
	private CoinMintRecipe(String type, int duration, Ingredient ingredient, int ingredientCount, ItemStack result) { this(readType(type),duration,ingredient,ingredientCount,result); }
	public CoinMintRecipe(MintType type, int duration, Ingredient ingredient, int ingredientCount, ItemStack result)
	{
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
	public boolean matches(@Nonnull SingleRecipeInput inventory, @Nonnull Level level) {
		if(!this.isValid())
			return false;
		ItemStack firstStack = inventory.getItem(0);
		return this.ingredient.test(firstStack);
	}
	
	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull SingleRecipeInput inventory, @Nonnull HolderLookup.Provider lookup) { return this.getResultItem(lookup); }
	
	@Override
	public boolean canCraftInDimensions(int width, int height) { return true; }

	public ItemStack getOutputItem() { return this.result.copy(); }

	@Override
	@Nonnull
	public ItemStack getResultItem(@Nonnull HolderLookup.Provider registryAccess) { if(this.isValid()) return this.result.copy(); return ItemStack.EMPTY; }

	public int getInternalDuration() { return this.duration; }
	public int getDuration() { return this.duration > 0 ? this.duration : LCConfig.SERVER.coinMintDefaultDuration.get(); }

	@Override
	@Nonnull
	public RecipeSerializer<?> getSerializer() { return ModRecipes.COIN_MINT.get(); }
	@Override
	@Nonnull
	public RecipeType<?> getType() { return RecipeTypes.COIN_MINT.get(); }

	@Nonnull
	@Override
	public ItemStack getToastSymbol() { return new ItemStack(ModBlocks.COIN_MINT.get()); }

	public static class Serializer implements RecipeSerializer<CoinMintRecipe>{

		@Nonnull
		private static CoinMintRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf buffer) {
			CoinMintRecipe.MintType type = CoinMintRecipe.readType(buffer.readUtf());
			Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			int ingredientCount = buffer.readInt();
			ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
			int duration = buffer.readInt();
			return new CoinMintRecipe(type, duration, ingredient, ingredientCount, result);
		}

		private static void toNetwork(@Nonnull RegistryFriendlyByteBuf buffer, @Nonnull CoinMintRecipe recipe) {
			buffer.writeUtf(recipe.getMintType().name());
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getIngredient());
			buffer.writeInt(recipe.ingredientCount);
			ItemStack.STREAM_CODEC.encode(buffer, recipe.getOutputItem());
			buffer.writeInt(recipe.getInternalDuration());
		}

		@Nonnull
		@Override
		public MapCodec<CoinMintRecipe> codec() {
			return RecordCodecBuilder.mapCodec(builder -> builder.group(
					Codec.STRING.optionalFieldOf("mintType","OTHER").forGetter(r -> r.type.name()),
					Codec.INT.optionalFieldOf("duration",0).forGetter(r -> r.duration),
					Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CoinMintRecipe::getIngredient),
					Codec.INT.optionalFieldOf("count",1).forGetter(r -> r.ingredientCount),
					ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result)
					).apply(builder,CoinMintRecipe::new)
		 	);
		}

		@Nonnull
		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CoinMintRecipe> streamCodec() { return StreamCodec.of(Serializer::toNetwork,Serializer::fromNetwork); }

	}
	
}
