package io.github.lightman314.lightmanscurrency.datagen.common.crafting;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.conditions.ConfigCraftingCondition;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.LCCraftingConditions;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders.*;
import io.github.lightman314.lightmanscurrency.datagen.util.ColorHelper;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LCRecipeProvider extends RecipeProvider {

    public LCRecipeProvider(PackOutput output) { super(output); }

    private static final String ADV_PREFIX = "recipes/misc/";

    @Override
    protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {

        //Register Crafting Conditions
        LCCraftingConditions.register();

        //Trading Core
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TRADING_CORE.get())
                .unlockedBy("money", MoneyKnowledge())
                .pattern("rqr")
                .pattern("rdr")
                .pattern("rpr")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('q', Tags.Items.GEMS_QUARTZ)
                .define('d', Items.DROPPER)
                .define('p', Items.COMPARATOR)
                .save(consumer, ItemID(ModItems.TRADING_CORE));

        //ATM
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ATM.get())
                .unlockedBy("money", MoneyKnowledge())
                .pattern("igi")
                .pattern("igi")
                .pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('g', Tags.Items.GLASS_PANES_COLORLESS)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .save(consumer, ItemID(ModBlocks.ATM));
        //ATM <-> Portable ATM
        GenerateSwapRecipes(consumer, ModBlocks.ATM.get(), ModItems.PORTABLE_ATM.get(), Lists.newArrayList(Pair.of("money", MoneyKnowledge())));

        //Trading Terminal
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TERMINAL.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("sgs")
                .pattern("sgs")
                .pattern("iei")
                .define('e', Items.ENDER_EYE)
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', Tags.Items.STONE)
                .save(consumer, ItemID(ModBlocks.TERMINAL));
        //Terminal <-> Portable Terminal
        GenerateSwapRecipes(consumer, ModBlocks.TERMINAL.get(), ModItems.PORTABLE_TERMINAL.get(), Lists.newArrayList(Pair.of("money", MoneyKnowledge()),Pair.of("trader", TraderKnowledge())));

        //Gem Terminal
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GEM_TERMINAL.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("aaa")
                .pattern("aea")
                .pattern("asa")
                .define('e', Items.ENDER_EYE)
                .define('a', Tags.Items.GEMS_AMETHYST)
                .define('s', Tags.Items.STONE)
                .save(consumer, ItemID(ModBlocks.GEM_TERMINAL));
        //Gem Terminal <-> Portable Gem Terminal
        GenerateSwapRecipes(consumer, ModBlocks.GEM_TERMINAL.get(), ModItems.PORTABLE_GEM_TERMINAL.get(), Lists.newArrayList(Pair.of("money", MoneyKnowledge()),Pair.of("trader", TraderKnowledge())));

        //Wallet Recipes
        GenerateWalletRecipes(consumer, Lists.newArrayList(
                Pair.of(Ingredient.of(Tags.Items.INGOTS_COPPER),ModItems.WALLET_COPPER),
                Pair.of(Ingredient.of(Tags.Items.INGOTS_IRON),ModItems.WALLET_IRON),
                Pair.of(Ingredient.of(Tags.Items.INGOTS_GOLD),ModItems.WALLET_GOLD),
                Pair.of(Ingredient.of(Tags.Items.GEMS_EMERALD),ModItems.WALLET_EMERALD),
                Pair.of(Ingredient.of(Tags.Items.GEMS_DIAMOND),ModItems.WALLET_DIAMOND),
                Pair.of(Ingredient.of(Tags.Items.INGOTS_NETHERITE),ModItems.WALLET_NETHERITE),
                //2.2.3.2
                Pair.of(Ingredient.of(Tags.Items.NETHER_STARS),ModItems.WALLET_NETHER_STAR)
        ));

        //Leather Wallet Recipe
        //2.2.4.0
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,ModItems.WALLET_LEATHER.get())
                .group("wallet_crafting")
                .unlockedBy("coin",MoneyKnowledge())
                .unlockedBy("wallet",LazyTrigger(LCTags.Items.WALLET))
                .requires(Tags.Items.LEATHER)
                .requires(Tags.Items.STRING)
                .requires(Tags.Items.LEATHER)
                .save(consumer,ItemID("wallet/",ModItems.WALLET_LEATHER));
        WalletUpgradeRecipeBuilder.shapeless(RecipeCategory.MISC,ModItems.WALLET_IRON.get())
                .group("wallet_crafting")
                .unlockedBy("coin",MoneyKnowledge())
                .unlockedBy("wallet",LazyTrigger(LCTags.Items.WALLET))
                .requires(ModItems.WALLET_LEATHER.get())
                .requires(Tags.Items.INGOTS_IRON)
                .save(consumer,ItemID("wallet/upgrade_leather_wallet_to",ModItems.WALLET_IRON));

        //Coin Recipes
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_COPPER, ModBlocks.COINPILE_COPPER, ModBlocks.COINBLOCK_COPPER);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_IRON, ModBlocks.COINPILE_IRON, ModBlocks.COINBLOCK_IRON);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_GOLD, ModBlocks.COINPILE_GOLD, ModBlocks.COINBLOCK_GOLD);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_EMERALD, ModBlocks.COINPILE_EMERALD, ModBlocks.COINBLOCK_EMERALD);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_DIAMOND, ModBlocks.COINPILE_DIAMOND, ModBlocks.COINBLOCK_DIAMOND);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_NETHERITE, ModBlocks.COINPILE_NETHERITE, ModBlocks.COINBLOCK_NETHERITE);
        //Chocolate Coin Recipes
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_CHOCOLATE_COPPER, ModBlocks.COINPILE_CHOCOLATE_COPPER, ModBlocks.COINBLOCK_CHOCOLATE_COPPER,"chocolate_",LazyTrigger(LCTags.Items.EVENT_COIN_CHOCOLATE));
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_CHOCOLATE_IRON, ModBlocks.COINPILE_CHOCOLATE_IRON, ModBlocks.COINBLOCK_CHOCOLATE_IRON,"chocolate_",LazyTrigger(LCTags.Items.EVENT_COIN_CHOCOLATE));
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_CHOCOLATE_GOLD, ModBlocks.COINPILE_CHOCOLATE_GOLD, ModBlocks.COINBLOCK_CHOCOLATE_GOLD,"chocolate_",LazyTrigger(LCTags.Items.EVENT_COIN_CHOCOLATE));
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_CHOCOLATE_EMERALD, ModBlocks.COINPILE_CHOCOLATE_EMERALD, ModBlocks.COINBLOCK_CHOCOLATE_EMERALD,"chocolate_",LazyTrigger(LCTags.Items.EVENT_COIN_CHOCOLATE));
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_CHOCOLATE_DIAMOND, ModBlocks.COINPILE_CHOCOLATE_DIAMOND, ModBlocks.COINBLOCK_CHOCOLATE_DIAMOND,"chocolate_",LazyTrigger(LCTags.Items.EVENT_COIN_CHOCOLATE));
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_CHOCOLATE_NETHERITE, ModBlocks.COINPILE_CHOCOLATE_NETHERITE, ModBlocks.COINBLOCK_CHOCOLATE_NETHERITE,"chocolate_",LazyTrigger(LCTags.Items.EVENT_COIN_CHOCOLATE));

        //Utility Blocks
        //Coin Mint

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.COIN_MINT.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("material", LazyTrigger(LCTags.Items.COIN_MINTING_MATERIAL))
                .pattern("ipi")
                .pattern("i i")
                .pattern("sss")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('p', Items.PISTON)
                .define('s', Items.SMOOTH_STONE)
                .save(lazyConditional(consumer,ItemID(ModBlocks.COIN_MINT),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinMint)));

        //Ticket Station
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TICKET_STATION.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("ticket_trader", LazyTrigger(ModBlocks.TICKET_KIOSK))
                .unlockedBy("ticket", LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material", LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .pattern("igi")
                .pattern("igi")
                .pattern("rrr")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('g', Items.INK_SAC)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .save(consumer, ID("ticket_station"));


        //Cash Register
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CASH_REGISTER.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("iii")
                .pattern("ede")
                .pattern("iii")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('e', Tags.Items.ENDER_PEARLS)
                .define('d', Items.DROPPER)
                .save(consumer, ItemID(ModBlocks.CASH_REGISTER));

        //Coin Chest
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.COIN_CHEST.get())
                .unlockedBy("money", MoneyKnowledge())
                .pattern("ppp")
                .pattern("prp")
                .pattern("ppp")
                .define('p', ItemTags.PLANKS)
                .define('r', Items.COMPARATOR)
                .save(lazyConditional(consumer,ItemID(ModBlocks.COIN_CHEST),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinChest)));

        //Trader Recipes
        //Display Case
        //Updated in v2.2.5.1
        ModBlocks.DISPLAY_CASE.forEach((color,display) ->
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, display.get())
                    .unlockedBy("money",MoneyKnowledge())
                    .unlockedBy("trader",TraderKnowledge())
                    .group("display_case")
                    .pattern("g")
                    .pattern("x")
                    .pattern("w")
                    .define('x',ModItems.TRADING_CORE.get())
                    .define('g',Tags.Items.GLASS_COLORLESS)
                    .define('w',ColorHelper.GetWoolOfColor(color))
                    .save(consumer,ID("traders/display_case/" + color.getResourceSafeName()))
        );

        //Vending Machine
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VENDING_MACHINE.get(Color.WHITE))
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("igi")
                .pattern("igi")
                .pattern("cxc")
                .define('x', ModItems.TRADING_CORE.get())
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .save(consumer, ItemID("traders/", ModBlocks.VENDING_MACHINE.get(Color.WHITE)));
        //Colored Vending Machines
        GenerateColoredDyeAndWashRecipes(consumer, ModBlocks.VENDING_MACHINE, ModBlocks.VENDING_MACHINE.get(Color.WHITE), "vending_machine_dyes", "traders/vending_machine/", Lists.newArrayList(Pair.of("money", MoneyKnowledge()), Pair.of("trader", TraderKnowledge())));

        //Large Vending Machine
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VENDING_MACHINE_LARGE.get(Color.WHITE))
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("vending_machine", LazyTrigger(ModBlocks.VENDING_MACHINE.get(Color.WHITE)))
                .pattern("igi")
                .pattern("igi")
                .pattern("cxc")
                .define('x', ModBlocks.VENDING_MACHINE.get(Color.WHITE))
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .save(consumer, ItemID("traders/", ModBlocks.VENDING_MACHINE_LARGE.get(Color.WHITE)));
        //Colored Large Vending Machine
        GenerateColoredDyeAndWashRecipes(consumer, ModBlocks.VENDING_MACHINE_LARGE, ModBlocks.VENDING_MACHINE_LARGE.get(Color.WHITE), "vending_machine_large_dyes", "traders/large_vending_machine/", Lists.newArrayList(Pair.of("money", MoneyKnowledge()), Pair.of("trader", TraderKnowledge())));

        //Shelf
        ModBlocks.SHELF.forEach((woodType,shelf) -> {
            List<ICondition> conditions = new ArrayList<>();
            if(woodType.isModded())
                conditions.add(new ModLoadedCondition(woodType.getModID()));
            WoodData data = woodType.getData();
            Item slab = data == null ? null : data.getSlab();
            if(slab != null)
            {
                ResourceLocation id = WoodID("traders/shelf/", woodType);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, shelf.get())
                        .group("shelf_trader")
                        .unlockedBy("money", MoneyKnowledge())
                        .unlockedBy("trader", TraderKnowledge())
                        .pattern("x").pattern("s")
                        .define('x', ModItems.TRADING_CORE.get())
                        .define('s', slab)
                        .save(lazyConditional(consumer,id,conditions), id);
            }
            else
                LightmansCurrency.LogDebug("Could not generate shelf recipe for WoodType '" + woodType.id + "' as it has no defined slab item.");

        });

        //Shelf
        ModBlocks.SHELF_2x2.forEach((woodType,shelf) -> {
            List<ICondition> conditions = new ArrayList<>();
            if(woodType.isModded())
                conditions.add(new ModLoadedCondition(woodType.getModID()));
            WoodData data = woodType.getData();
            Item slab = data == null ? null : data.getSlab();
            ItemLike trader = ModBlocks.SHELF.get(woodType);
            if(slab != null && trader != null)
            {
                ResourceLocation id = WoodID("traders/shelf2/", woodType);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, shelf.get())
                        .group("shelf_trader_2x2")
                        .unlockedBy("money", MoneyKnowledge())
                        .unlockedBy("trader", TraderKnowledge())
                        .unlockedBy("shelf", LazyTrigger(trader))
                        .pattern("c").pattern("x").pattern("s")
                        .define('c', Tags.Items.CHESTS_WOODEN)
                        .define('x', trader)
                        .define('s', slab)
                        .save(lazyConditional(consumer,id,conditions), id);
            }
            else
                LightmansCurrency.LogDebug("Could not generate 2x2 shelf recipe for WoodType '" + woodType.id + "' as it has no defined slab item.");

        });

        //Card Display Crafting
        ModBlocks.CARD_DISPLAY.forEach((woodType,color,card_display) -> {
            List<ICondition> conditions = new ArrayList<>();
            if(woodType.isModded())
                conditions.add(new ModLoadedCondition(woodType.getModID()));
            WoodData data = woodType.getData();
            Item log = data == null ? null : data.getLog();
            if(log != null)
            {
                ResourceLocation id = ColoredWoodID("traders/card_display/", woodType, color);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, card_display.get())
                        .group("card_display_trader_" + woodType.id)
                        .unlockedBy("money", MoneyKnowledge())
                        .unlockedBy("trader", TraderKnowledge())
                        .pattern("  w")
                        .pattern(" xl")
                        .pattern("llc")
                        .define('x', ModItems.TRADING_CORE.get())
                        .define('l', log)
                        .define('w', ColorHelper.GetWoolOfColor(color))
                        .define('c', Tags.Items.CHESTS_WOODEN)
                        .save(lazyConditional(consumer,id,conditions), id);
            }
            else
                LightmansCurrency.LogDebug("Could not generate card display recipe for WoodType '" + woodType.id + "' as it has no defined log item.");
        });

        //Freezer
        for(Color c : Color.values())
        {
            ItemLike cp = ColorHelper.GetConcretePowderOfColor(c);
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FREEZER.get(c))
                    .group("freezer_crafting")
                    .unlockedBy("money", MoneyKnowledge())
                    .unlockedBy("trader", TraderKnowledge())
                    .pattern("igi")
                    .pattern("igi")
                    .pattern("cxc")
                    .define('x', ModItems.TRADING_CORE.get())
                    .define('g', Tags.Items.GLASS_COLORLESS)
                    .define('i', cp)
                    .define('c', Tags.Items.CHESTS_WOODEN)
                    .save(consumer, ID("traders/freezer/" + c.getResourceSafeName()));
        }

        //Network Traders
        //T1
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_NETWORK_TRADER_1.get())
                .group("item_network_trader")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                .pattern("ici")
                .pattern("ixi")
                .pattern("iei")
                .define('x', ModItems.TRADING_CORE.get())
                .define('e', Items.ENDER_EYE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .save(lazyConditional(consumer,ID("network/item_network_trader_1"),LCCraftingConditions.NETWORK_TRADER));

        //T2
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_NETWORK_TRADER_2.get())
                .group("item_network_trader")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                .unlockedBy("previous", LazyTrigger(ModBlocks.ITEM_NETWORK_TRADER_1))
                .pattern("c")
                .pattern("x")
                .pattern("i")
                .define('x', ModBlocks.ITEM_NETWORK_TRADER_1.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .save(lazyConditional(consumer,ID("network/item_network_trader_2"),LCCraftingConditions.NETWORK_TRADER));

        //T3
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_NETWORK_TRADER_3.get())
                .group("item_network_trader")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                .unlockedBy("previous", LazyTrigger(ModBlocks.ITEM_NETWORK_TRADER_2))
                .pattern("c")
                .pattern("x")
                .pattern("i")
                .define('x', ModBlocks.ITEM_NETWORK_TRADER_2.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .save(lazyConditional(consumer,ID("network/item_network_trader_3"),LCCraftingConditions.NETWORK_TRADER));

        //T4
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_NETWORK_TRADER_4.get())
                .group("item_network_trader")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                .unlockedBy("previous", LazyTrigger(ModBlocks.ITEM_NETWORK_TRADER_3))
                .pattern("c")
                .pattern("x")
                .pattern("i")
                .define('x', ModBlocks.ITEM_NETWORK_TRADER_3.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .save(lazyConditional(consumer,ID("network/item_network_trader_4"),LCCraftingConditions.NETWORK_TRADER));

        //Specialty Traders
        //Armor Display
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ARMOR_DISPLAY.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("ggg")
                .pattern("gag")
                .pattern("ixi")
                .define('x', ModItems.TRADING_CORE.get())
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('a', Items.ARMOR_STAND)
                .save(consumer, ItemID("traders/", ModBlocks.ARMOR_DISPLAY));

        //Ticket Kiosk
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TICKET_KIOSK.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("iii")
                .pattern("igi")
                .pattern("rxr")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('g', Items.INK_SAC)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('x', ModItems.TRADING_CORE.get())
                .save(consumer, ItemID("traders/", ModBlocks.TICKET_KIOSK));

        //Bookshelves
        ModBlocks.BOOKSHELF_TRADER.forEach((woodType,bookshelf) -> {
            List<ICondition> conditions = new ArrayList<>();
            if(woodType.isModded())
                conditions.add(new ModLoadedCondition(woodType.getModID()));
            WoodData data = woodType.getData();
            Item plank = data == null ? null : data.getPlank();
            Item slab = data == null ? null : data.getSlab();
            if(plank != null && slab != null)
            {
                ResourceLocation id = WoodID("traders/bookshelf/", woodType);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, bookshelf.get())
                        .group("bookshelf_trader")
                        .unlockedBy("money", MoneyKnowledge())
                        .unlockedBy("trader", TraderKnowledge())
                        .pattern("ppp")
                        .pattern("sxs")
                        .pattern("ppp")
                        .define('x', ModItems.TRADING_CORE.get())
                        .define('p', plank)
                        .define('s', slab)
                        .save(lazyConditional(consumer,id,conditions), id);
            }
            else
                LightmansCurrency.LogDebug("Could not generate bookshelf recipe for WoodType '" + woodType.id + "' as it has no defined plank and/or slab item.");
        });

        //Slot Machine
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SLOT_MACHINE.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("igi")
                .pattern("idi")
                .pattern("rxr")
                .define('x', ModItems.TRADING_CORE.get())
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('r', Items.COMPARATOR)
                .define('d', Tags.Items.DUSTS_GLOWSTONE)
                .save(consumer, ItemID("traders/", ModBlocks.SLOT_MACHINE));

        //Paygate
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.PAYGATE.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("ticket", LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material", LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .pattern("iri")
                .pattern("ixi")
                .pattern("iti")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('t', Items.REDSTONE_TORCH)
                .define('r', Items.REDSTONE_BLOCK)
                .define('x', ModItems.TRADING_CORE.get())
                .save(consumer, ItemID("traders/", ModBlocks.PAYGATE));

        //Item Trader Interface
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ITEM_TRADER_INTERFACE.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("terminal", LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                .pattern("ici")
                .pattern("iti")
                .pattern("ici")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('t', LCTags.Items.NETWORK_TERMINAL)
                .define('c', Tags.Items.CHESTS_WOODEN)
                .save(lazyConditional(consumer,ItemID(ModBlocks.ITEM_TRADER_INTERFACE),LCCraftingConditions.TRADER_INTERFACE));

        //Auction Stands
        ModBlocks.AUCTION_STAND.forEach((woodType, auction_stand) -> {
            List<ICondition> conditions = Lists.newArrayList(ConfigCraftingCondition.of(LCConfig.COMMON.canCraftAuctionStands));
            if(woodType.isModded())
                conditions.add(new ModLoadedCondition(woodType.getModID()));
            WoodData data = woodType.getData();
            Item log = data == null ? null : data.getLog();
            if(log != null)
            {
                ResourceLocation id = WoodID("auction_stand/", woodType);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, auction_stand.get())
                        .group("auction_stand")
                        .unlockedBy("money", MoneyKnowledge())
                        .unlockedBy("trader", TraderKnowledge())
                        .unlockedBy("terminal", LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                        .pattern("g")
                        .pattern("x")
                        .pattern("l")
                        .define('x', ModItems.TRADING_CORE.get())
                        .define('g', Tags.Items.GLASS_COLORLESS)
                        .define('l', log)
                        .save(lazyConditional(consumer,id,conditions), id);
            }
            else
                LightmansCurrency.LogDebug("Could not generate auction stand recipe for WoodType '" + woodType.id + "' as it has no defined log item.");
        });

        //Coin Jars
        //Piggy Bank
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PIGGY_BANK.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("wallet", LazyTrigger(LCTags.Items.WALLET))
                .pattern("b  ")
                .pattern("bbb")
                .pattern("bdb")
                .define('b', Tags.Items.INGOTS_BRICK)
                .define('d', Tags.Items.DYES_PINK)
                .save(consumer, ItemID("coin_jar/", ModBlocks.PIGGY_BANK));

        //Blue
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.COINJAR_BLUE.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("wallet", LazyTrigger(LCTags.Items.WALLET))
                .pattern("b b")
                .pattern("bdb")
                .pattern(" b ")
                .define('b', Tags.Items.INGOTS_BRICK)
                .define('d', Tags.Items.DYES_BLUE)
                .save(consumer, ID("coin_jar/blue"));

        //Coin Mint Recipes
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_COPPER, Tags.Items.INGOTS_COPPER, Items.COPPER_INGOT, LCConfig.COMMON.coinMintMintableCopper, LCConfig.COMMON.coinMintMeltableCopper);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_IRON, Tags.Items.INGOTS_IRON, Items.IRON_INGOT, LCConfig.COMMON.coinMintMintableIron, LCConfig.COMMON.coinMintMeltableIron);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_GOLD, Tags.Items.INGOTS_GOLD, Items.GOLD_INGOT, LCConfig.COMMON.coinMintMintableGold, LCConfig.COMMON.coinMintMeltableGold);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_EMERALD, Tags.Items.GEMS_EMERALD, Items.EMERALD, LCConfig.COMMON.coinMintMintableEmerald, LCConfig.COMMON.coinMintMeltableEmerald);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_DIAMOND, Tags.Items.GEMS_DIAMOND, Items.DIAMOND, LCConfig.COMMON.coinMintMintableDiamond, LCConfig.COMMON.coinMintMeltableDiamond);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_NETHERITE, Tags.Items.INGOTS_NETHERITE, Items.NETHERITE_INGOT, LCConfig.COMMON.coinMintMintableNetherite, LCConfig.COMMON.coinMintMeltableNetherite);

        //Upgrade Recipes
        //Template
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.UPGRADE_SMITHING_TEMPLATE.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("nnn")
                .pattern("ncn")
                .pattern("nnn")
                .define('c', ModItems.TRADING_CORE.get())
                .define('n', Tags.Items.NUGGETS_IRON)
                .save(consumer, ID("upgrades/create_template"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.UPGRADE_SMITHING_TEMPLATE.get(), 2)
                .group("lightmans_template_copy")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .unlockedBy("template",LazyTrigger(ModItems.UPGRADE_SMITHING_TEMPLATE.get()))
                .pattern("nnn")
                .pattern("ntn")
                .pattern("nnn")
                .define('t', ModItems.UPGRADE_SMITHING_TEMPLATE.get())
                .define('n', Tags.Items.NUGGETS_IRON)
                .save(consumer, ID("upgrades/copy_template"));
        //Added in v2.2.5.2
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.UPGRADE_SMITHING_TEMPLATE.get(),10)
                .group("lightmans_template_copy")
                .unlockedBy("money",MoneyKnowledge())
                .unlockedBy("trader",TraderKnowledge())
                .unlockedBy("template",LazyTrigger(ModItems.UPGRADE_SMITHING_TEMPLATE.get()))
                .pattern("iii")
                .pattern("iti")
                .pattern("iii")
                .define('t',ModItems.UPGRADE_SMITHING_TEMPLATE.get())
                .define('i',Tags.Items.INGOTS_IRON)
                .save(consumer,ID("upgrades/copy_template_bulk"));

        final Ingredient TEMPLATE = Ingredient.of(ModItems.UPGRADE_SMITHING_TEMPLATE.get());
        //Item Capacity Upgrades
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(Tags.Items.CHESTS_WOODEN),
                        Ingredient.of(Tags.Items.INGOTS_IRON),
                        RecipeCategory.MISC,
                        ModItems.ITEM_CAPACITY_UPGRADE_1.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader", TraderKnowledge())
                .save(consumer, ItemID("upgrades/", ModItems.ITEM_CAPACITY_UPGRADE_1));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.ITEM_CAPACITY_UPGRADE_1.get()),
                        Ingredient.of(Tags.Items.INGOTS_GOLD),
                        RecipeCategory.MISC,
                        ModItems.ITEM_CAPACITY_UPGRADE_2.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader", TraderKnowledge())
                .unlocks("previous", LazyTrigger(ModItems.ITEM_CAPACITY_UPGRADE_1))
                .save(consumer, ItemID("upgrades/", ModItems.ITEM_CAPACITY_UPGRADE_2));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.ITEM_CAPACITY_UPGRADE_2.get()),
                        Ingredient.of(Tags.Items.GEMS_DIAMOND),
                        RecipeCategory.MISC,
                        ModItems.ITEM_CAPACITY_UPGRADE_3.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader", TraderKnowledge())
                .unlocks("previous", LazyTrigger(ModItems.ITEM_CAPACITY_UPGRADE_2))
                .save(consumer, ItemID("upgrades/", ModItems.ITEM_CAPACITY_UPGRADE_3));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.ITEM_CAPACITY_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        ModItems.ITEM_CAPACITY_UPGRADE_4.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader", TraderKnowledge())
                .unlocks("previous", LazyTrigger(ModItems.ITEM_CAPACITY_UPGRADE_3))
                .save(consumer, ItemID("upgrades/", ModItems.ITEM_CAPACITY_UPGRADE_4));

        //Network Upgrade
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(Items.ENDER_EYE),
                        Ingredient.of(Tags.Items.INGOTS_GOLD),
                        RecipeCategory.MISC,
                        ModItems.NETWORK_UPGRADE.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader", TraderKnowledge())
                .unlocks("terminal", TerminalKnowledge())
                .save(consumer, ItemID("upgrades/", ModItems.NETWORK_UPGRADE));

        //Trader Interface Upgrades
        //Hopper Upgrade
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(Items.HOPPER),
                Ingredient.of(Tags.Items.INGOTS_GOLD),
                RecipeCategory.MISC,
                ModItems.HOPPER_UPGRADE.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader_interface", LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .save(consumer, ItemID("upgrades/", ModItems.HOPPER_UPGRADE));

        //Speed Upgrades
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(Items.CLOCK),
                        Ingredient.of(Tags.Items.INGOTS_IRON),
                        RecipeCategory.MISC,
                        ModItems.SPEED_UPGRADE_1.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader_interface", LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("coin_mint", LazyTrigger(ModBlocks.COIN_MINT))
                .save(consumer, ItemID("upgrades/", ModItems.SPEED_UPGRADE_1));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.SPEED_UPGRADE_1.get()),
                        Ingredient.of(Tags.Items.INGOTS_GOLD),
                        RecipeCategory.MISC,
                        ModItems.SPEED_UPGRADE_2.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader_interface", LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("coin_mint", LazyTrigger(ModBlocks.COIN_MINT))
                .unlocks("previous", LazyTrigger(ModItems.SPEED_UPGRADE_1))
                .save(consumer, ItemID("upgrades/", ModItems.SPEED_UPGRADE_2));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.SPEED_UPGRADE_2.get()),
                        Ingredient.of(Tags.Items.GEMS_EMERALD),
                        RecipeCategory.MISC,
                        ModItems.SPEED_UPGRADE_3.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader_interface", LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("coin_mint", LazyTrigger(ModBlocks.COIN_MINT))
                .unlocks("previous", LazyTrigger(ModItems.SPEED_UPGRADE_2))
                .save(consumer, ItemID("upgrades/", ModItems.SPEED_UPGRADE_3));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.SPEED_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.GEMS_DIAMOND),
                        RecipeCategory.MISC,
                        ModItems.SPEED_UPGRADE_4.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader_interface", LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("coin_mint", LazyTrigger(ModBlocks.COIN_MINT))
                .unlocks("previous", LazyTrigger(ModItems.SPEED_UPGRADE_3))
                .save(consumer, ItemID("upgrades/", ModItems.SPEED_UPGRADE_4));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.SPEED_UPGRADE_4.get()),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        ModItems.SPEED_UPGRADE_5.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader_interface", LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("coin_mint", LazyTrigger(ModBlocks.COIN_MINT))
                .unlocks("previous", LazyTrigger(ModItems.SPEED_UPGRADE_4))
                .save(consumer, ItemID("upgrades/", ModItems.SPEED_UPGRADE_5));

        //Coin Chest Upgrades
        //Exchange Upgrade
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(LCTags.Items.ATM),
                Ingredient.of(Tags.Items.DUSTS_REDSTONE),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_EXCHANGE_UPGRADE.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .save(lazyConditional(consumer,ItemID("upgrades/",ModItems.COIN_CHEST_EXCHANGE_UPGRADE),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinChestUpgradeExchange)), "null:null");
        //Magnet Upgrades
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(Tags.Items.ENDER_PEARLS),
                Ingredient.of(Tags.Items.INGOTS_COPPER),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_MAGNET_UPGRADE_1.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .save(lazyConditional(consumer,ItemID("upgrades/",ModItems.COIN_CHEST_MAGNET_UPGRADE_1),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinChestUpgradeMagnet)), "null:null");
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(ModItems.COIN_CHEST_MAGNET_UPGRADE_1.get()),
                Ingredient.of(Tags.Items.INGOTS_IRON),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_MAGNET_UPGRADE_2.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .unlocks("previous", LazyTrigger(ModItems.COIN_CHEST_MAGNET_UPGRADE_1))
                .save(lazyConditional(consumer,ItemID("upgrades/",ModItems.COIN_CHEST_MAGNET_UPGRADE_2),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinChestUpgradeMagnet)), "null:null");
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.COIN_CHEST_MAGNET_UPGRADE_2.get()),
                        Ingredient.of(Tags.Items.INGOTS_GOLD),
                        RecipeCategory.MISC,
                        ModItems.COIN_CHEST_MAGNET_UPGRADE_3.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .unlocks("previous", LazyTrigger(ModItems.COIN_CHEST_MAGNET_UPGRADE_2))
                .save(lazyConditional(consumer,ItemID("upgrades/",ModItems.COIN_CHEST_MAGNET_UPGRADE_3),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinChestUpgradeMagnet)), "null:null");
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.COIN_CHEST_MAGNET_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.GEMS_EMERALD),
                        RecipeCategory.MISC,
                        ModItems.COIN_CHEST_MAGNET_UPGRADE_4.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .unlocks("previous", LazyTrigger(ModItems.COIN_CHEST_MAGNET_UPGRADE_3))
                .save(lazyConditional(consumer,ItemID("upgrades/",ModItems.COIN_CHEST_MAGNET_UPGRADE_4),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinChestUpgradeMagnet)), "null:null");

        //Security Upgrade
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(Items.OBSIDIAN),
                Ingredient.of(Tags.Items.GEMS_DIAMOND),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_SECURITY_UPGRADE.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .save(lazyConditional(consumer,ItemID("upgrades/",ModItems.COIN_CHEST_SECURITY_UPGRADE),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinChestUpgradeSecurity)), "null:null");

        //2.1.2.2
        //The Jar of Sus
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.SUS_JAR.get())
                .requires(LCTags.Items.COIN_JAR_NORMAL)
                .requires(Items.SUSPICIOUS_STEW)
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("jar", LazyTrigger(LCTags.Items.COIN_JAR_ALL))
                .save(consumer, ItemID("coin_jar/", ModBlocks.SUS_JAR));

        //Tax Block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TAX_COLLECTOR.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("ghg")
                .pattern("nxn")
                .pattern("geg")
                .define('g', Tags.Items.INGOTS_GOLD)
                .define('n', Tags.Items.INGOTS_NETHERITE)
                .define('x', ModItems.TRADING_CORE.get())
                .define('h', Items.HOPPER)
                .define('e', Tags.Items.ENDER_PEARLS)
                .save(lazyConditional(consumer,ItemID(ModBlocks.TAX_COLLECTOR),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftTaxBlock)));

        //2.2.0.0
        //Ticket Station crafting as an actual recipe
        MasterTicketRecipeBuilder.of(LCTags.Items.TICKET_MATERIAL_PAPER)
                .withResult(ModItems.TICKET_MASTER)
                .unlockedBy("ticket_station", LazyTrigger(ModBlocks.TICKET_STATION))
                .unlockedBy("tickets", LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material", LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .save(consumer, ItemID("ticket_station/", ModItems.TICKET_MASTER));
        TicketRecipeBuilder.of(LCTags.Items.TICKET_MATERIAL_PAPER, ModItems.TICKET.get())
                .withMasterTicket(ModItems.TICKET_MASTER.get())
                .unlockedBy("ticket_station", LazyTrigger(ModBlocks.TICKET_STATION))
                .unlockedBy("tickets", LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material", LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .save(consumer, ItemID("ticket_station/", ModItems.TICKET));
        TicketRecipeBuilder.of(LCTags.Items.TICKET_MATERIAL_PAPER, ModItems.TICKET_PASS.get())
                .withMasterTicket(ModItems.TICKET_MASTER.get())
                .withDurabilityRange(true,2,99)
                .unlockedBy("ticket_station", LazyTrigger(ModBlocks.TICKET_STATION))
                .unlockedBy("tickets", LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material", LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .save(consumer, ItemID("ticket_station/", ModItems.TICKET_PASS));

        //2.2.1.2
        //Golden Tickets
        MasterTicketRecipeBuilder.of(LCTags.Items.TICKET_MATERIAL_GOLD)
                .withResult(ModItems.GOLDEN_TICKET_MASTER)
                .unlockedBy("ticket_station", LazyTrigger(ModBlocks.TICKET_STATION))
                .unlockedBy("tickets", LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material", LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .save(consumer, ItemID("ticket_station/", ModItems.GOLDEN_TICKET_MASTER));
        TicketRecipeBuilder.of(LCTags.Items.TICKET_MATERIAL_GOLD, ModItems.GOLDEN_TICKET.get())
                .withMasterTicket(ModItems.GOLDEN_TICKET_MASTER.get())
                .unlockedBy("ticket_station", LazyTrigger(ModBlocks.TICKET_STATION))
                .unlockedBy("tickets", LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material", LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .save(consumer, ItemID("ticket_station/", ModItems.GOLDEN_TICKET));
        TicketRecipeBuilder.of(LCTags.Items.TICKET_MATERIAL_GOLD, ModItems.GOLDEN_TICKET_PASS.get())
                .withMasterTicket(ModItems.GOLDEN_TICKET_MASTER.get())
                .withDurabilityRange(true,2,99)
                .unlockedBy("ticket_station", LazyTrigger(ModBlocks.TICKET_STATION))
                .unlockedBy("tickets", LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material", LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .save(consumer, ItemID("ticket_station/", ModItems.GOLDEN_TICKET_PASS));

        //2.2.2.5
        //Offer Upgrade
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.TRADING_CORE.get()),
                        Ingredient.of(Tags.Items.INGOTS_COPPER),
                        RecipeCategory.MISC,
                        ModItems.OFFER_UPGRADE_1.get())
                .unlocks("money",MoneyKnowledge())
                .unlocks("trader",TraderKnowledge())
                .save(consumer,ItemID("upgrades/",ModItems.OFFER_UPGRADE_1));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.OFFER_UPGRADE_1.get()),
                        Ingredient.of(Tags.Items.INGOTS_IRON),
                        RecipeCategory.MISC,
                        ModItems.OFFER_UPGRADE_2.get())
                .unlocks("money",MoneyKnowledge())
                .unlocks("trader",TraderKnowledge())
                .unlocks("previous",LazyTrigger(ModItems.OFFER_UPGRADE_1))
                .save(consumer,ItemID("upgrades/",ModItems.OFFER_UPGRADE_2));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.OFFER_UPGRADE_2.get()),
                        Ingredient.of(Tags.Items.INGOTS_GOLD),
                        RecipeCategory.MISC,
                        ModItems.OFFER_UPGRADE_3.get())
                .unlocks("money",MoneyKnowledge())
                .unlocks("trader",TraderKnowledge())
                .unlocks("previous",LazyTrigger(ModItems.OFFER_UPGRADE_2))
                .save(consumer,ItemID("upgrades/",ModItems.OFFER_UPGRADE_3));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.OFFER_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.GEMS_EMERALD),
                        RecipeCategory.MISC,
                        ModItems.OFFER_UPGRADE_4.get())
                .unlocks("money",MoneyKnowledge())
                .unlocks("trader",TraderKnowledge())
                .unlocks("previous",LazyTrigger(ModItems.OFFER_UPGRADE_3))
                .save(consumer,ItemID("upgrades/",ModItems.OFFER_UPGRADE_4));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.OFFER_UPGRADE_4.get()),
                        Ingredient.of(Tags.Items.GEMS_DIAMOND),
                        RecipeCategory.MISC,
                        ModItems.OFFER_UPGRADE_5.get())
                .unlocks("money",MoneyKnowledge())
                .unlocks("trader",TraderKnowledge())
                .unlocks("previous",LazyTrigger(ModItems.OFFER_UPGRADE_4))
                .save(consumer,ItemID("upgrades/",ModItems.OFFER_UPGRADE_5));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.OFFER_UPGRADE_5.get()),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        ModItems.OFFER_UPGRADE_6.get())
                .unlocks("money",MoneyKnowledge())
                .unlocks("trader",TraderKnowledge())
                .unlocks("previous",LazyTrigger(ModItems.OFFER_UPGRADE_5))
                .save(consumer,ItemID("upgrades/",ModItems.OFFER_UPGRADE_6));


        //2.2.2.6
        //Bank Upgrade
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(LCTags.Items.ATM),
                        Ingredient.of(Tags.Items.GEMS_DIAMOND),
                        RecipeCategory.MISC,
                        ModItems.COIN_CHEST_BANK_UPGRADE.get())
                .unlocks("money",MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .unlocks("atm",LazyTrigger(LCTags.Items.ATM))
                .save(lazyConditional(consumer,ItemID("upgrades/",ModItems.COIN_CHEST_BANK_UPGRADE),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinChestUpgradeBank)),"null:null");

        //2.2.3.2
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,ModItems.ATM_CARD.get())
                .unlockedBy("money",MoneyKnowledge())
                .unlockedBy("atm",LazyTrigger(LCTags.Items.ATM))
                .requires(Items.PAPER)
                .requires(Tags.Items.ENDER_PEARLS)
                .requires(Tags.Items.NUGGETS_IRON)
                .save(lazyConditional(consumer,ItemID(ModItems.ATM_CARD),ConfigCraftingCondition.of(LCConfig.COMMON.canCraftATMCard)),"null:null");

        //2.2.4.1
        //Interaction Upgrades
        SmithingTransformRecipeBuilder.smithing(TEMPLATE,
                Ingredient.of(ModItems.NETWORK_UPGRADE.get()),
                Ingredient.of(Tags.Items.GEMS_EMERALD),
                RecipeCategory.MISC,
                ModItems.INTERACTION_UPGRADE_1.get())
                .unlocks("trader_interface",LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("terminal",LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                .unlocks("network_trader",LazyTrigger(LCTags.Items.TRADER_NETWORK))
                .unlocks("previous",LazyTrigger(ModItems.NETWORK_UPGRADE))
                .save(consumer,ItemID("upgrades/",ModItems.INTERACTION_UPGRADE_1));
        SmithingTransformRecipeBuilder.smithing(TEMPLATE,
                        Ingredient.of(ModItems.INTERACTION_UPGRADE_1.get()),
                        Ingredient.of(Tags.Items.GEMS_DIAMOND),
                        RecipeCategory.MISC,
                        ModItems.INTERACTION_UPGRADE_2.get())
                .unlocks("trader_interface",LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("terminal",LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                .unlocks("network_trader",LazyTrigger(LCTags.Items.TRADER_NETWORK))
                .unlocks("previous",LazyTrigger(ModItems.INTERACTION_UPGRADE_1))
                .save(consumer,ItemID("upgrades/",ModItems.INTERACTION_UPGRADE_2));
        SmithingTransformRecipeBuilder.smithing(TEMPLATE,
                        Ingredient.of(ModItems.INTERACTION_UPGRADE_2.get()),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        ModItems.INTERACTION_UPGRADE_3.get())
                .unlocks("trader_interface",LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("terminal",LazyTrigger(LCTags.Items.NETWORK_TERMINAL))
                .unlocks("network_trader",LazyTrigger(LCTags.Items.TRADER_NETWORK))
                .unlocks("previous",LazyTrigger(ModItems.INTERACTION_UPGRADE_2))
                .save(consumer,ItemID("upgrades/",ModItems.INTERACTION_UPGRADE_3));

        //2.2.4.4
        //Gacha Machine
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModBlocks.GACHA_MACHINE.get(Color.WHITE))
                .unlockedBy("money",MoneyKnowledge())
                .unlockedBy("trader",TraderKnowledge())
                .pattern("ggg")
                .pattern("gxg")
                .pattern("iii")
                .define('x',ModItems.TRADING_CORE.get())
                .define('g',Tags.Items.GLASS_COLORLESS)
                .define('i',Tags.Items.INGOTS_IRON)
                .save(consumer,ID("traders/gacha_machine"));
        GenerateColoredDyeAndWashRecipes(consumer,ModBlocks.GACHA_MACHINE,ModBlocks.GACHA_MACHINE.get(Color.WHITE),"gacha_dyes","traders/gacha_machine/",Lists.newArrayList(Pair.of("money",MoneyKnowledge()),Pair.of("trader",TraderKnowledge())));

        //2.2.5.1
        //Money Bag
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModBlocks.MONEY_BAG.get())
                .unlockedBy("money",MoneyKnowledge())
                .unlockedBy("wallet",LazyTrigger(LCTags.Items.WALLET))
                .pattern(" s ")
                .pattern("lgl")
                .pattern("lll")
                .define('s',Tags.Items.STRING)
                .define('l',Tags.Items.LEATHER)
                .define('g',Tags.Items.NUGGETS_GOLD)
                .save(consumer);

        //2.2.5.2
        //Variant Wand
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.VARIANT_WAND.get())
                .unlockedBy("trader",TraderKnowledge())
                .pattern("i i")
                .pattern("ntn")
                .pattern(" s ")
                .define('i',Tags.Items.INGOTS_IRON)
                .define('n',Tags.Items.NUGGETS_IRON)
                .define('t',ModItems.UPGRADE_SMITHING_TEMPLATE.get())
                .define('s',Items.STICK)
                .save(consumer);

        //2.2.6.1
        //Coupon
        CouponRecipeBuilder.of(LCTags.Items.TICKET_MATERIAL_PAPER)
                .unlockedBy("ticket_station",LazyTrigger(ModBlocks.TICKET_STATION))
                .unlockedBy("tickets",LazyTrigger(LCTags.Items.TICKETS))
                .unlockedBy("ticket_material",LazyTrigger(LCTags.Items.TICKET_MATERIAL))
                .withResult(ModItems.COUPON)
                .save(consumer,ItemID("ticket_station/",ModItems.COUPON));


        //2.2.6.4
        //Item Trade Filter
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.ITEM_TRADE_FILTER.get())
                .unlockedBy("paper",LazyTrigger(Items.PAPER))
                .unlockedBy("traders",TraderKnowledge())
                .pattern("  n")
                .pattern(" p ")
                .pattern("n  ")
                .define('n',Tags.Items.NUGGETS_IRON)
                .define('p',Items.PAPER)
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC,ModItems.ITEM_TRADE_FILTER.get())
                .unlockedBy("paper",LazyTrigger(Items.PAPER))
                .unlockedBy("traders",TraderKnowledge())
                .unlockedBy("filter",LazyTrigger(ModItems.ITEM_TRADE_FILTER))
                .requires(ModItems.ITEM_TRADE_FILTER.get())
                .save(consumer,VersionUtil.lcResource("item_trade_filter_reset"));

    }

    protected static void GenerateWalletRecipes(@Nonnull Consumer<FinishedRecipe> consumer, List<Pair<Ingredient, Supplier<? extends ItemLike>>> ingredientWalletPairs)
    {
        Ingredient leather = Ingredient.of(Tags.Items.LEATHER);
        List<Ingredient> ingredients = ingredientWalletPairs.stream().map(Pair::getFirst).toList();
        List<? extends ItemLike> wallets = ingredientWalletPairs.stream().map(p -> p.getSecond().get()).toList();
        //Default Wallet Recipes
        for(int w = 0; w < wallets.size(); ++w)
        {
            ItemLike wallet = wallets.get(w);
            ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, wallets.get(w))
                    .group("wallet_crafting")
                    .unlockedBy("coin", MoneyKnowledge())
                    .unlockedBy("wallet", LazyTrigger(LCTags.Items.WALLET))
                    .requires(leather);
            for(int i = 0; i < ingredients.size() && i <= w; ++i)
                b.requires(ingredients.get(i));
            b.requires(leather);
            b.save(consumer, ItemID("wallet/",wallet));
        }

        //Upgrade Wallet Recipes
        for(int w = 0; w < wallets.size() - 1; ++w)
        {
            for(int w2 = w + 1; w2 < wallets.size(); ++w2)
            {
                ItemLike first = wallets.get(w);
                ItemLike result = wallets.get(w2);
                WalletUpgradeRecipeBuilder b = WalletUpgradeRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                        .group("wallet_upgrading")
                        .unlockedBy("coin", MoneyKnowledge())
                        .unlockedBy("wallet", LazyTrigger(LCTags.Items.WALLET))
                        .requires(first);
                for(int i = w +1; i < ingredients.size() && i <= w2; ++i)
                    b.requires(ingredients.get(i));
                b.save(consumer, ID("wallet/upgrade_" + ItemPath(first) + "_to_" + ItemPath(result)));
            }
        }
    }

    protected static void GenerateSwapRecipes(@Nonnull Consumer<FinishedRecipe> consumer, ItemLike item1, ItemLike item2, List<Pair<String, CriterionTriggerInstance>> criteria)
    {
        String group = ItemPath(item2) + "_swap";
        GenerateSwapRecipe(consumer, item1, item2, group, criteria);
        GenerateSwapRecipe(consumer, item2, item1, group, criteria);
    }

    protected static void GenerateSwapRecipe(@Nonnull Consumer<FinishedRecipe> consumer, ItemLike item1, ItemLike item2, @Nullable String group, List<Pair<String, CriterionTriggerInstance>> criteria)
    {
        ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item2)
                .group(group);
        ApplyCriteria(b, criteria);
        b.unlockedBy("other", LazyTrigger(item1))
                .requires(item1)
                .save(consumer, ID(ItemPath(item1) + "_swap"));
    }

    protected static void GenerateCoinBlockRecipes(@Nonnull Consumer<FinishedRecipe> consumer, Supplier<? extends ItemLike> coin, Supplier<? extends ItemLike> coinPile, Supplier<? extends ItemLike> coinBlock) { GenerateCoinBlockRecipes(consumer,coin,coinPile,coinBlock,"",MoneyKnowledge()); }
    protected static void GenerateCoinBlockRecipes(@Nonnull Consumer<FinishedRecipe> consumer, Supplier<? extends ItemLike> coin, Supplier<? extends ItemLike> coinPile, Supplier<? extends ItemLike> coinBlock, @Nonnull String prefix, @Nonnull CriterionTriggerInstance moneyKnowledge)
    {
        //Coin -> Pile
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, coinPile.get())
                .group(prefix + "coin_pile_from_coin")
                .unlockedBy("money", moneyKnowledge)
                .unlockedBy("coin", LazyTrigger(coin))
                .requires(coin.get(), 9)
                .save(consumer, ID("coins/" + ItemPath(coinPile) + "_from_coin"));
        //Pile -> Block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, coinBlock.get())
                .group(prefix + "coin_block_from_pile")
                .unlockedBy("money", moneyKnowledge)
                .unlockedBy("pile", LazyTrigger(coinPile))
                .pattern("xx").pattern("xx")
                .define('x', coinPile.get())
                .save(consumer, ID("coins/" + ItemPath(coinBlock) + "_from_pile"));
        //Block -> Pile
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, coinPile.get(), 4)
                .group(prefix + "coin_pile_from_block")
                .unlockedBy("money", moneyKnowledge)
                .unlockedBy("block", LazyTrigger(coinBlock))
                .requires(coinBlock.get())
                .save(consumer, ID("coins/" + ItemPath(coinPile) + "_from_block"));
        //Pile -> Coin
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, coin.get(), 9)
                .group(prefix + "coin_from_pile")
                .unlockedBy("money", moneyKnowledge)
                .unlockedBy("pile", LazyTrigger(coinPile))
                .requires(coinPile.get())
                .save(consumer, ID("coins/" + ItemPath(coin) + "_from_pile"));
    }

    protected static void GenerateColoredDyeAndWashRecipes(@Nonnull Consumer<FinishedRecipe> consumer, RegistryObjectBundle<? extends ItemLike,Color> bundle, ItemLike cleanItem, @Nullable String dyeGroup, String prefix, List<Pair<String, CriterionTriggerInstance>> criteria)
    {
        List<ItemLike> coloredSet = new ArrayList<>();
        for(Color color : Color.values())
        {
            ItemLike result = bundle.get(color);
            if(result.asItem() == cleanItem.asItem())
                continue;
            coloredSet.add(result);
            ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                    .group(dyeGroup)
                    .unlockedBy("cleanitem", LazyTrigger(cleanItem))
                    .requires(cleanItem)
                    .requires(color.dyeTag);
            ApplyCriteria(builder, criteria);
            builder.save(consumer, ID(prefix + "dye_" + color.getResourceSafeName()));
        }
        //Washing Recipe
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, cleanItem);
        ApplyCriteria(builder, criteria);
        builder.unlockedBy("colored", LazyTrigger(coloredSet))
                .requires(Ingredient.of(coloredSet.stream().map(ItemStack::new)))
                .requires(Items.WATER_BUCKET)
                .save(consumer, ID(prefix + "washing"));
    }

    protected static void GenerateMintAndMeltRecipes(@Nonnull Consumer<FinishedRecipe> consumer, Supplier<? extends ItemLike> coin, TagKey<Item> materialTag, ItemLike materialItem) {GenerateMintAndMeltRecipes(consumer,coin,materialTag,materialItem,null,null); }
    protected static void GenerateMintAndMeltRecipes(@Nonnull Consumer<FinishedRecipe> consumer, Supplier<? extends ItemLike> coin, TagKey<Item> materialTag, ItemLike materialItem, @Nullable BooleanOption mintConfig, @Nullable BooleanOption meltConfig)
    {
        MintRecipeBuilder.create(materialTag,coin.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("coin_mint", LazyTrigger(ModBlocks.COIN_MINT))
                .save(lazyConditional(consumer,ItemID("coin_mint/mint_",coin),ConfigCraftingCondition.of(LCConfig.COMMON.coinMintCanMint),mintConfig == null ? null : ConfigCraftingCondition.of(mintConfig)), ItemID("coin_mint/mint_", coin));
        MintRecipeBuilder.create(coin.get(),materialItem)
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("coin_mint", LazyTrigger(ModBlocks.COIN_MINT))
                .save(lazyConditional(consumer,ItemID("coin_mint/melt_",coin),ConfigCraftingCondition.of(LCConfig.COMMON.coinMintCanMelt),meltConfig == null ? null : ConfigCraftingCondition.of(meltConfig)), ItemID("coin_mint/melt_", coin));
    }

    protected static CriterionTriggerInstance MoneyKnowledge() { return LazyTrigger(LCTags.Items.COINS); }
    protected static CriterionTriggerInstance TraderKnowledge() { return LazyTrigger(LCTags.Items.TRADER); }
    protected static CriterionTriggerInstance TerminalKnowledge() { return LazyTrigger(LCTags.Items.NETWORK_TERMINAL); }

    protected static CriterionTriggerInstance LazyTrigger(ItemLike item) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(item).build()); }
    protected static CriterionTriggerInstance LazyTrigger(List<? extends ItemLike> items) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items.toArray(new ItemLike[0])).build()); }
    protected static CriterionTriggerInstance LazyTrigger(Supplier<? extends ItemLike> item) { return LazyTrigger(item.get()); }
    protected static CriterionTriggerInstance LazyTrigger(TagKey<Item> tag) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build()); }

    protected static void ApplyCriteria(ShapelessRecipeBuilder builder, List<Pair<String,CriterionTriggerInstance>> criteria)
    {
        for(Pair<String,CriterionTriggerInstance> c : criteria)
            builder.unlockedBy(c.getFirst(), c.getSecond());
    }

    protected static String ItemPath(ItemLike item) { return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath(); }
    protected static String ItemPath(Supplier<? extends ItemLike> item) { return ItemPath(item.get()); }
    protected static ResourceLocation ItemID(String prefix, ItemLike item) { return ID(prefix + ItemPath(item)); }
    protected static ResourceLocation ItemID(Supplier<? extends ItemLike> item) { return ID(ItemPath(item)); }
    protected static ResourceLocation ItemID(String prefix, Supplier<? extends ItemLike> item) { return ID(prefix + ItemPath(item)); }
    protected static ResourceLocation WoodID(String prefix, WoodType woodType) { return ID(woodType.generateResourceLocation(prefix)); }
    protected static ResourceLocation WoodID(String prefix, WoodType woodType, String postfix) { return ID(woodType.generateResourceLocation(prefix, postfix)); }
    protected static ResourceLocation ColoredWoodID(String prefix, WoodType woodType, Color color) { return WoodID(prefix, woodType, "/" + color.getResourceSafeName()); }
    protected static ResourceLocation ID(String path) { return VersionUtil.lcResource(path); }

    protected static Consumer<FinishedRecipe> lazyConditional(Consumer<FinishedRecipe> consumer, ResourceLocation recipeID, ICondition... conditions) { return lazyConditional(consumer,recipeID,Lists.newArrayList(conditions)); }
    protected static Consumer<FinishedRecipe> lazyConditional(Consumer<FinishedRecipe> consumer, ResourceLocation recipeID, List<ICondition> conditions)
    {
        return recipe -> {
            if(conditions.isEmpty())
            {
                consumer.accept(recipe);
                return;
            }
            ConditionalRecipe.Builder builder = ConditionalRecipe.builder();
            for(ICondition condition : conditions)
            {
                if(condition != null)
                    builder.addCondition(condition);
            }
            builder.addRecipe(recipe);
            builder.generateAdvancement(recipeID.withPrefix(ADV_PREFIX)).build(consumer,recipeID);
        };
    }

}
