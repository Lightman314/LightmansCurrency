package io.github.lightman314.lightmanscurrency.features.wallet.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class WalletUpgradeRecipe extends NormalCraftingRecipe {

    public static final MapCodec<WalletUpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            CommonInfo.MAP_CODEC.forGetter(r -> r.commonInfo),
            CraftingBookInfo.MAP_CODEC.forGetter(r -> r.bookInfo),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(r -> r.result),
            com.mojang.serialization.Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1,9)).fieldOf("ingredients").forGetter(o -> o.ingredients)
    ).apply(builder,WalletUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,WalletUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
            CommonInfo.STREAM_CODEC,r -> r.commonInfo,
            CraftingBookInfo.STREAM_CODEC,r -> r.bookInfo,
            ByteBufCodecs.registry(Registries.ITEM),r -> r.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),r -> r.ingredients,
            WalletUpgradeRecipe::new);

    public static final RecipeSerializer<WalletUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC,STREAM_CODEC);

    private final Item result;
    private final List<Ingredient> ingredients;
    public List<Ingredient> getIngredients() { return this.ingredients; }
    private final boolean isSimple;
    public WalletUpgradeRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,Item result,List<Ingredient> ingredients) {
        super(commonInfo,bookInfo);
        this.result = result;
        this.ingredients = ImmutableList.copyOf(ingredients);
        this.isSimple = this.ingredients.stream().allMatch(Ingredient::isSimple);
    }

    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size() || this.findWalletToCopy(input).isEmpty()) {
            return false;
        } else if (!isSimple) {
            var nonEmptyItems = new java.util.ArrayList<ItemStack>(input.ingredientCount());
            for (var item : input.items())
                if (!item.isEmpty())
                    nonEmptyItems.add(item);
            return net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1
                    ? this.ingredients.getFirst().test(input.getItem(0))
                    : input.stackedContents().canCraft(this, null);
        }
    }

    protected ItemStack findWalletToCopy(CraftingInput input) {
        ItemStack walletToCopy = ItemStack.EMPTY;
        for(int i = 0; i < input.size() && walletToCopy.isEmpty(); ++i)
        {
            if(WalletItem.isWallet(input.getItem(i)))
                walletToCopy = input.getItem(i);
        }
        return walletToCopy;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack walletToCopy = this.findWalletToCopy(input);
        if(walletToCopy.isEmpty())
            return ItemStack.EMPTY;
        return walletToCopy.transmuteCopy(this.result);
    }

    @Override
    public RecipeSerializer<? extends NormalCraftingRecipe> getSerializer() { return SERIALIZER; }

    @Override
    protected PlacementInfo createPlacementInfo() { return PlacementInfo.create(this.ingredients); }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
                new ShapelessCraftingRecipeDisplay(
                        this.ingredients.stream().map(Ingredient::display).toList(),
                        new SlotDisplay.ItemSlotDisplay(this.result),
                        new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
                )
        );
    }

}