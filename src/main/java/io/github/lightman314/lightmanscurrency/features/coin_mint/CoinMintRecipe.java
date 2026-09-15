package io.github.lightman314.lightmanscurrency.features.coin_mint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.core.LCRecipeBookCategories;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class CoinMintRecipe implements Recipe<SingleRecipeInput> {

    private static final MapCodec<CoinMintRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.optionalFieldOf("group","").forGetter(r -> r.group),
            Codec.intRange(1,Integer.MAX_VALUE).optionalFieldOf("duration").forGetter(r -> r.duration),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            Codec.intRange(1,Item.ABSOLUTE_MAX_STACK_SIZE).optionalFieldOf("count",1).forGetter(r -> r.ingredientCount),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(builder,CoinMintRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf,CoinMintRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,r -> r.group,
            ByteBufCodecs.optional(ByteBufCodecs.INT),r -> r.duration,
            Ingredient.CONTENTS_STREAM_CODEC,r -> r.ingredient,
            ByteBufCodecs.INT,r -> r.ingredientCount,
            ItemStackTemplate.STREAM_CODEC,r -> r.result,
            CoinMintRecipe::new);

    public static final RecipeSerializer<CoinMintRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,STREAM_CODEC);

    private final String group;
    private final Optional<Integer> duration;
    public final int getDuration() { return this.duration.orElseGet(LCConfig.SERVER.coinMintDefaultDuration); }
    private final Ingredient ingredient;
    public Ingredient getIngredient() { return this.ingredient; }
    private final int ingredientCount;
    public final int getIngredientCount() { return this.ingredientCount; }
    private final ItemStackTemplate result;
    public ItemStackTemplate getResult() { return this.result; }

    public CoinMintRecipe(String group,int duration,Ingredient ingredient,int ingredientCount,ItemStackTemplate result) { this(group,duration <= 0 ? Optional.empty() : Optional.of(duration),ingredient,ingredientCount,result); }
    public CoinMintRecipe(String group,Optional<Integer> duration,Ingredient ingredient,int ingredientCount,ItemStackTemplate result) {
        this.duration = duration;
        this.group = group;
        this.ingredient = ingredient;
        this.ingredientCount = Math.max(ingredientCount,1);
        this.result = result;
    }

    @Override
    public boolean matches(SingleRecipeInput input,Level level) {
        return this.ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) { return this.result.create(); }

    @Override
    public boolean showNotification() { return true; }

    @Override
    public String group() { return this.group; }

    @Override
    public RecipeSerializer<CoinMintRecipe> getSerializer() { return SERIALIZER; }

    @Override
    public RecipeType<CoinMintRecipe> getType() { return LCRecipeTypes.COIN_MINT.get(); }

    @Override
    public PlacementInfo placementInfo() { return PlacementInfo.create(this.ingredient); }

    @Override
    public RecipeBookCategory recipeBookCategory() { return LCRecipeBookCategories.COIN_MINT.get(); }

}
