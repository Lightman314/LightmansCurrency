package io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.patchouli.common.item.PatchouliItems;
import vazkii.patchouli.common.recipe.ShapelessBookRecipe;

public class BookRecipeBuilder extends CraftingRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final ResourceLocation bookID;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Advancement.Builder advancement = Builder.recipeAdvancement();
    @Nullable
    private String group;

    public BookRecipeBuilder(RecipeCategory p_250837_, ItemLike p_251897_, int p_252227_, ResourceLocation bookID) {
        this.category = p_250837_;
        this.result = p_251897_.asItem();
        this.count = p_252227_;
        this.bookID = bookID;
    }

    public static BookRecipeBuilder shapeless(RecipeCategory p_250714_, ResourceLocation bookID) {
        return new BookRecipeBuilder(p_250714_, PatchouliItems.BOOK, 1,bookID);
    }

    public BookRecipeBuilder requires(TagKey<Item> p_206420_) {
        return this.requires(Ingredient.of(p_206420_));
    }

    public BookRecipeBuilder requires(ItemLike p_126210_) {
        return this.requires(p_126210_, 1);
    }

    public BookRecipeBuilder requires(ItemLike p_126212_, int p_126213_) {
        for(int $$2 = 0; $$2 < p_126213_; ++$$2) {
            this.requires(Ingredient.of(p_126212_));
        }

        return this;
    }

    public BookRecipeBuilder requires(Ingredient p_126185_) {
        return this.requires(p_126185_, 1);
    }

    public BookRecipeBuilder requires(Ingredient p_126187_, int p_126188_) {
        for(int $$2 = 0; $$2 < p_126188_; ++$$2) {
            this.ingredients.add(p_126187_);
        }

        return this;
    }

    public BookRecipeBuilder unlockedBy(String p_126197_, CriterionTriggerInstance p_126198_) {
        this.advancement.addCriterion(p_126197_, p_126198_);
        return this;
    }

    public BookRecipeBuilder group(@Nullable String p_126195_) {
        this.group = p_126195_;
        return this;
    }

    public Item getResult() {
        return this.result;
    }

    public void save(Consumer<FinishedRecipe> p_126205_, ResourceLocation p_126206_) {
        this.ensureValid(p_126206_);
        this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(p_126206_)).rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(p_126206_)).requirements(RequirementsStrategy.OR);
        p_126205_.accept(new Result(p_126206_, this.result, this.count, this.group == null ? "" : this.group, determineBookCategory(this.category), this.ingredients, this.bookID, this.advancement, p_126206_.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }

    private void ensureValid(ResourceLocation p_126208_) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + p_126208_);
        }
    }

    public static class Result extends CraftingRecipeBuilder.CraftingResult {
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String group;
        private final List<Ingredient> ingredients;
        private final ResourceLocation bookID;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ResourceLocation p_249007_, Item p_248667_, int p_249014_, String p_248592_, CraftingBookCategory p_249485_, List<Ingredient> p_252312_, ResourceLocation bookID, Advancement.Builder p_249909_, ResourceLocation p_249109_) {
            super(p_249485_);
            this.id = p_249007_;
            this.result = p_248667_;
            this.count = p_249014_;
            this.group = p_248592_;
            this.ingredients = p_252312_;
            this.bookID = bookID;
            this.advancement = p_249909_;
            this.advancementId = p_249109_;
        }

        public void serializeRecipeData(JsonObject p_126230_) {
            super.serializeRecipeData(p_126230_);
            if (!this.group.isEmpty()) {
                p_126230_.addProperty("group", this.group);
            }

            JsonArray $$1 = new JsonArray();

            for(Ingredient $$2 : this.ingredients) {
                $$1.add($$2.toJson());
            }

            p_126230_.add("ingredients", $$1);
            JsonObject $$3 = new JsonObject();
            $$3.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result).toString());
            if (this.count > 1) {
                $$3.addProperty("count", this.count);
            }

            p_126230_.add("result", $$3);

            p_126230_.addProperty("book",this.bookID.toString());
        }

        public RecipeSerializer<?> getType() {
            return ShapelessBookRecipe.SERIALIZER;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        public JsonObject serializeAdvancement() {
            return this.advancement.serializeToJson();
        }

        @Nullable
        public ResourceLocation getAdvancementId() {
            return this.advancementId;
        }
    }
}