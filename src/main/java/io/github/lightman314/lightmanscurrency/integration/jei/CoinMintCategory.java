package io.github.lightman314.lightmanscurrency.integration.jei;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.lightman314.lightmanscurrency.client.features.coin_mint.CoinMintScreen;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import io.github.lightman314.lightmanscurrency.features.coin_mint.CoinMintRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class CoinMintCategory implements IRecipeCategory<RecipeHolder<CoinMintRecipe>> {

    public static final IRecipeType<RecipeHolder<CoinMintRecipe>> TYPE = IRecipeType.create(LCRecipeTypes.COIN_MINT.get());

    private final IDrawableStatic background;
    private final IDrawable icon;
    private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;

    public CoinMintCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(CoinMintScreen.GUI_TEXTURE,47,8,98,42);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(LCBlocks.COIN_MINT));
        this.cachedArrows = CacheBuilder.newBuilder().maximumSize(25L).build(new CacheLoader<>() {
            @Override
            public IDrawableAnimated load(Integer mintTime) {
                return guiHelper.drawableBuilder(CoinMintScreen.ARROW.sprite().withPrefix("textures/gui/sprites/").withSuffix(".png"),0,0,CoinMintScreen.ARROW.width(),CoinMintScreen.ARROW.height())
                        .setTextureSize(CoinMintScreen.ARROW.width(),CoinMintScreen.ARROW.height())
                        .buildAnimated(mintTime,IDrawableAnimated.StartDirection.LEFT,false);
            }
        });
    }

    protected IDrawableAnimated getArrow(RecipeHolder<CoinMintRecipe> recipe) {
        int mintTime = recipe.value().getDuration();
        if(mintTime <= 0)
            mintTime = 100;
        return this.cachedArrows.getUnchecked(mintTime);
    }

    @Override
    public IRecipeType<RecipeHolder<CoinMintRecipe>> getRecipeType() { return TYPE; }

    @Override
    public Component getTitle() { return LCBlocks.COIN_MINT.get().getName(); }

    @Override
    public int getWidth() { return 98; }
    @Override
    public int getHeight() { return 42; }
    @Override
    @Nullable
    public IDrawable getIcon() { return this.icon; }

    @Override
    public void draw(RecipeHolder<CoinMintRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
        IDrawableAnimated arrow = this.getArrow(recipe);
        arrow.draw(guiGraphics,33,13);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CoinMintRecipe> holder, IFocusGroup focuses) {
        CoinMintRecipe recipe = holder.value();
        IRecipeSlotBuilder inputSlot = builder.addInputSlot(9,13);
        inputSlot.addIngredients(VanillaTypes.ITEM_STACK,this.ingredientsOfCount(recipe.getIngredient(),recipe.getIngredientCount()));
        IRecipeSlotBuilder outputSlot = builder.addOutputSlot(69,13);
        outputSlot.addIngredients(VanillaTypes.ITEM_STACK,List.of(recipe.getResult().create()));
    }

    @SuppressWarnings("deprecation")
    private List<ItemStack> ingredientsOfCount(Ingredient ingredient,int stackSize) {
        return ingredient.items().map(holder -> new ItemStack(holder,stackSize)).toList();
    }

}
