package io.github.lightman314.lightmanscurrency.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipeSerializers;
import io.github.lightman314.lightmanscurrency.common.core.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CoinMintRecipe implements Recipe<SingleRecipeInput>{

	public static final MapCodec<CoinMintRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
					Codec.INT.optionalFieldOf("duration",0).forGetter(r -> r.duration),
					Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CoinMintRecipe::getIngredient),
					Codec.INT.optionalFieldOf("count",1).forGetter(r -> r.ingredientCount),
					ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result)
			).apply(builder,CoinMintRecipe::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf,CoinMintRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,r -> r.duration,
            Ingredient.CONTENTS_STREAM_CODEC,r -> r.ingredient,
            ByteBufCodecs.INT,r -> r.ingredientCount,
            ItemStack.STREAM_CODEC,r -> r.result,
            CoinMintRecipe::new);

	private final int duration;
	private final Ingredient ingredient;
	public final int ingredientCount;
	private final ItemStack result;

	public CoinMintRecipe(int duration, Ingredient ingredient, int ingredientCount, ItemStack result)
	{
		this.duration = duration;
		this.ingredient = ingredient;
		this.ingredientCount = Math.max(ingredientCount,1); //Force count to be > 0
		this.result = result;
	}
	
	public Ingredient getIngredient() { return this.ingredient; }
	
	public boolean isValid() { return !this.ingredient.isEmpty() && this.result.getItem() != Items.AIR; }
	
	@Override
	public boolean matches(SingleRecipeInput inventory, Level level) {
		if(!this.isValid())
			return false;
		ItemStack firstStack = inventory.getItem(0);
		return this.ingredient.test(firstStack);
	}
	
	
	@Override
	public ItemStack assemble(SingleRecipeInput inventory, HolderLookup.Provider lookup) { return this.getResultItem(lookup); }
	
	@Override
	public boolean canCraftInDimensions(int width, int height) { return true; }

	public ItemStack getOutputItem() { return this.result.copy(); }

	@Override
	
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) { if(this.isValid()) return this.result.copy(); return ItemStack.EMPTY; }

	public int getInternalDuration() { return this.duration; }
	public int getDuration() { return this.duration > 0 ? this.duration : LCConfig.SERVER.coinMintDefaultDuration.get(); }

	@Override
	public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.COIN_MINT.get(); }
	@Override
	public RecipeType<?> getType() { return ModRecipeTypes.COIN_MINT.get(); }
	@Override
	public ItemStack getToastSymbol() { return new ItemStack(ModBlocks.COIN_MINT.get()); }
	
}
