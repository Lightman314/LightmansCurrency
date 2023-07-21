package io.github.lightman314.lightmanscurrency.datagen.common.crafting;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.crafting.condition.LCCraftingConditions;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders.MintRecipeBuilder;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.builders.WalletUpgradeRecipeBuilder;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.util.ColorHelper;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.util.WoodData;
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
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MACHINE_ATM.get())
                .unlockedBy("money", MoneyKnowledge())
                .pattern("igi")
                .pattern("igi")
                .pattern("iri")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('g', Tags.Items.GLASS_PANES_COLORLESS)
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .save(consumer, ItemID(ModBlocks.MACHINE_ATM));
        //ATM <-> Portable ATM
        GenerateSwapRecipes(consumer, ModBlocks.MACHINE_ATM.get(), ModItems.PORTABLE_ATM.get(), Lists.newArrayList(Pair.of("money", MoneyKnowledge())));

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
                Pair.of(Ingredient.of(Tags.Items.INGOTS_NETHERITE),ModItems.WALLET_NETHERITE)
        ));

        //Coin Recipes
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_COPPER, ModBlocks.COINPILE_COPPER, ModBlocks.COINBLOCK_COPPER);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_IRON, ModBlocks.COINPILE_IRON, ModBlocks.COINBLOCK_IRON);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_GOLD, ModBlocks.COINPILE_GOLD, ModBlocks.COINBLOCK_GOLD);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_EMERALD, ModBlocks.COINPILE_EMERALD, ModBlocks.COINBLOCK_EMERALD);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_DIAMOND, ModBlocks.COINPILE_DIAMOND, ModBlocks.COINBLOCK_DIAMOND);
        GenerateCoinBlockRecipes(consumer, ModItems.COIN_NETHERITE, ModBlocks.COINPILE_NETHERITE, ModBlocks.COINBLOCK_NETHERITE);

        //Utility Blocks
        //Coin Mint
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MACHINE_MINT.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("material", LazyTrigger(LCTags.Items.COIN_MINTING_MATERIAL))
                .pattern("ipi")
                .pattern("i i")
                .pattern("sss")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('p', Items.PISTON)
                .define('s', Items.SMOOTH_STONE)
                .save(consumer, ItemID(ModBlocks.MACHINE_MINT));

        //Ticket Station
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TICKET_STATION.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("ticket_trader", LazyTrigger(ModBlocks.TICKET_KIOSK))
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
                .define('e', Items.ENDER_PEARL)
                .define('d', Items.DROPPER)
                .save(consumer, ItemID(ModBlocks.CASH_REGISTER));

        //Coin Chest
        ConditionalRecipe.Builder conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.CoinChest.INSTANCE);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.COIN_CHEST.get())
                .unlockedBy("money", MoneyKnowledge())
                .pattern("ppp")
                .pattern("prp")
                .pattern("ppp")
                .define('p', ItemTags.PLANKS)
                .define('r', Items.COMPARATOR)
                .save(conditional::addRecipe, ItemID(ModBlocks.COIN_CHEST));
        conditional.generateAdvancement(ItemID(ADV_PREFIX, ModBlocks.COIN_CHEST)).build(consumer, ItemID(ModBlocks.COIN_CHEST));

        //Trader Recipes
        //Display Case
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DISPLAY_CASE.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("g")
                .pattern("x")
                .pattern("w")
                .define('x', ModItems.TRADING_CORE.get())
                .define('g', Tags.Items.GLASS_COLORLESS)
                .define('w', Items.WHITE_WOOL)
                .save(consumer, ItemID("traders/", ModBlocks.DISPLAY_CASE));



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
        for(WoodType woodType : WoodType.validValues())
        {
            conditional = woodType.isVanilla() ? null : ConditionalRecipe.builder().addCondition(new ModLoadedCondition(woodType.getModID()));
            Consumer<FinishedRecipe> c = conditional == null ? consumer : conditional::addRecipe;
            WoodData data = woodType.getData();
            ItemLike slab = data == null ? null : data.slabBlock.get();
            if(slab == null)
            {
                LightmansCurrency.LogDebug("Could not generate shelf recipe for WoodType '" + woodType.name + "' as it has no defined slab item.");
                continue;
            }
            ResourceLocation id = WoodID("traders/shelf/", woodType);
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SHELF.get(woodType))
                    .group("shelf_trader")
                    .unlockedBy("money", MoneyKnowledge())
                    .unlockedBy("trader", TraderKnowledge())
                    .pattern("x").pattern("s")
                    .define('x', ModItems.TRADING_CORE.get())
                    .define('s', slab)
                    .save(c, id);
            if(conditional != null)
                conditional.generateAdvancement(id.withPrefix(ADV_PREFIX)).build(consumer, id);
        }

        //Card Display
        for(WoodType woodType : WoodType.validValues())
        {
            conditional = woodType.isVanilla() ? null : ConditionalRecipe.builder().addCondition(new ModLoadedCondition(woodType.getModID()));
            Consumer<FinishedRecipe> c = conditional == null ? consumer : conditional::addRecipe;
            WoodData data = woodType.getData();
            ItemLike log = data == null ? null : data.logBlock.get();
            if(log == null)
            {
                LightmansCurrency.LogDebug("Could not generate card display recipe for WoodType '" + woodType.name + "' as it has no defined log item.");
                continue;
            }
            ResourceLocation id = WoodID("traders/card_display/", woodType);
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CARD_DISPLAY.get(woodType))
                    .group("card_display_trader")
                    .unlockedBy("money", MoneyKnowledge())
                    .unlockedBy("trader", TraderKnowledge())
                    .pattern("  w")
                    .pattern(" xl")
                    .pattern("llc")
                    .define('x', ModItems.TRADING_CORE.get())
                    .define('l', log)
                    .define('w', Items.RED_WOOL)
                    .define('c', Tags.Items.CHESTS_WOODEN)
                    .save(c, id);
            if(conditional != null)
                conditional.generateAdvancement(id.withPrefix(ADV_PREFIX)).build(consumer, id);
        }

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
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.NetworkTrader.INSTANCE);
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
                .save(conditional::addRecipe);
        conditional.generateAdvancement(ID("network/item_network_trader_1").withPrefix(ADV_PREFIX)).build(consumer, ID("network/item_network_trader_1"));

        //T2
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.NetworkTrader.INSTANCE);
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
                .save(conditional::addRecipe);
        conditional.generateAdvancement(ID("network/item_network_trader_2").withPrefix(ADV_PREFIX)).build(consumer, ID("network/item_network_trader_2"));

        //T3
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.NetworkTrader.INSTANCE);
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
                .save(conditional::addRecipe);
        conditional.generateAdvancement(ID("network/item_network_trader_3").withPrefix(ADV_PREFIX)).build(consumer, ID("network/item_network_trader_3"));

        //T4
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.NetworkTrader.INSTANCE);
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
                .save(conditional::addRecipe);
        conditional.generateAdvancement(ID("network/item_network_trader_4").withPrefix(ADV_PREFIX)).build(consumer, ID("network/item_network_trader_4"));

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
        for(WoodType woodType : WoodType.validValues())
        {
            conditional = woodType.isVanilla() ? null : ConditionalRecipe.builder().addCondition(new ModLoadedCondition(woodType.getModID()));
            Consumer<FinishedRecipe> c = conditional == null ? consumer : conditional::addRecipe;
            WoodData data = woodType.getData();
            ItemLike plank = data == null ? null : data.plankBlock.get();
            ItemLike slab = data == null ? null : data.slabBlock.get();
            if(plank == null || slab == null)
            {
                LightmansCurrency.LogDebug("Could not generate bookshelf recipe for WoodType '" + woodType.name + "' as it has no defined plank and/or slab item.");
                continue;
            }
            ResourceLocation id = WoodID("traders/bookshelf/", woodType);
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BOOKSHELF_TRADER.get(woodType))
                    .group("bookshelf_trader")
                    .unlockedBy("money", MoneyKnowledge())
                    .unlockedBy("trader", TraderKnowledge())
                    .pattern("ppp")
                    .pattern("sxs")
                    .pattern("ppp")
                    .define('x', ModItems.TRADING_CORE.get())
                    .define('p', plank)
                    .define('s', slab)
                    .save(c, id);
            if(conditional != null)
                conditional.generateAdvancement(id.withPrefix(ADV_PREFIX)).build(consumer, id);
        }

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
                .unlockedBy("ticket", LazyTrigger(LCTags.Items.TICKET))
                .pattern("iri")
                .pattern("ixi")
                .pattern("iti")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('t', Items.REDSTONE_TORCH)
                .define('r', Items.REDSTONE_BLOCK)
                .define('x', ModItems.TRADING_CORE.get())
                .save(consumer, ItemID("traders/", ModBlocks.PAYGATE));

        //Item Trader Interface
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.TraderInterface.INSTANCE);
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
                .save(conditional::addRecipe);
        conditional.generateAdvancement(ItemID(ModBlocks.ITEM_TRADER_INTERFACE).withPrefix(ADV_PREFIX)).build(consumer, ItemID(ModBlocks.ITEM_TRADER_INTERFACE));

        //Auction Stands
        for(WoodType woodType : WoodType.validValues())
        {
            conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.AuctionStand.INSTANCE);
            if(woodType.isModded())
                conditional.addCondition(new ModLoadedCondition(woodType.getModID()));
            WoodData data = woodType.getData();
            ItemLike log = data == null ? null : data.logBlock.get();
            if(log == null)
            {
                LightmansCurrency.LogDebug("Could not generate auction stand recipe for WoodType '" + woodType.name + "' as it has no defined log item.");
                continue;
            }
            ResourceLocation id = WoodID("traders/auction_stand/", woodType);
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.AUCTION_STAND.get(woodType))
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
                    .save(conditional::addRecipe, id);
            conditional.generateAdvancement(id.withPrefix(ADV_PREFIX)).build(consumer, id);
        }

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
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_COPPER, Tags.Items.INGOTS_COPPER, Items.COPPER_INGOT);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_IRON, Tags.Items.INGOTS_IRON, Items.IRON_INGOT);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_GOLD, Tags.Items.INGOTS_GOLD, Items.GOLD_INGOT);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_EMERALD, Tags.Items.GEMS_EMERALD, Items.EMERALD);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_DIAMOND, Tags.Items.GEMS_DIAMOND, Items.DIAMOND);
        GenerateMintAndMeltRecipes(consumer, ModItems.COIN_NETHERITE, Tags.Items.INGOTS_NETHERITE, Items.NETHERITE_INGOT);

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
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("trader", TraderKnowledge())
                .pattern("nnn")
                .pattern("ntn")
                .pattern("nnn")
                .define('t', ModItems.UPGRADE_SMITHING_TEMPLATE.get())
                .define('n', Tags.Items.NUGGETS_IRON)
                .save(consumer, ID("upgrades/copy_template"));

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
                .unlocks("coin_mint", LazyTrigger(ModBlocks.MACHINE_MINT))
                .save(consumer, ItemID("upgrades/", ModItems.SPEED_UPGRADE_1));
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.SPEED_UPGRADE_1.get()),
                        Ingredient.of(Tags.Items.INGOTS_GOLD),
                        RecipeCategory.MISC,
                        ModItems.SPEED_UPGRADE_2.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("trader_interface", LazyTrigger(LCTags.Items.TRADER_INTERFACE))
                .unlocks("coin_mint", LazyTrigger(ModBlocks.MACHINE_MINT))
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
                .unlocks("coin_mint", LazyTrigger(ModBlocks.MACHINE_MINT))
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
                .unlocks("coin_mint", LazyTrigger(ModBlocks.MACHINE_MINT))
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
                .unlocks("coin_mint", LazyTrigger(ModBlocks.MACHINE_MINT))
                .unlocks("previous", LazyTrigger(ModItems.SPEED_UPGRADE_4))
                .save(consumer, ItemID("upgrades/", ModItems.SPEED_UPGRADE_5));

        //Coin Chest Upgrades
        //Exchange Upgrade
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.CoinChestUpgradeExchange.INSTANCE);
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(ModBlocks.MACHINE_ATM.get()),
                Ingredient.of(Tags.Items.DUSTS_REDSTONE),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_EXCHANGE_UPGRADE.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .save(conditional::addRecipe, "null:null");
        conditional.generateAdvancement(ItemID("upgrades/", ModItems.COIN_CHEST_EXCHANGE_UPGRADE).withPrefix(ADV_PREFIX)).build(consumer, ItemID("upgrades/", ModItems.COIN_CHEST_EXCHANGE_UPGRADE));

        //Bank Upgrade (WIP)
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.CoinChestUpgradeBank.INSTANCE);
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(ModBlocks.MACHINE_ATM.get()),
                Ingredient.of(Items.ENDER_PEARL),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_BANK_UPGRADE.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .save(conditional::addRecipe, "null:null");
        conditional.generateAdvancement(ItemID("upgrades/", ModItems.COIN_CHEST_BANK_UPGRADE).withPrefix(ADV_PREFIX)).build(consumer, ItemID("upgrades/", ModItems.COIN_CHEST_BANK_UPGRADE));

        //Magnet Upgrades
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.CoinChestUpgradeMagnet.INSTANCE);
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(Items.ENDER_PEARL),
                Ingredient.of(Tags.Items.INGOTS_COPPER),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_MAGNET_UPGRADE_1.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .save(conditional::addRecipe, "null:null");
        conditional.generateAdvancement(ItemID("upgrades/", ModItems.COIN_CHEST_MAGNET_UPGRADE_1).withPrefix(ADV_PREFIX)).build(consumer, ItemID("upgrades/", ModItems.COIN_CHEST_MAGNET_UPGRADE_1));
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.CoinChestUpgradeMagnet.INSTANCE);
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(ModItems.COIN_CHEST_MAGNET_UPGRADE_1.get()),
                Ingredient.of(Tags.Items.INGOTS_IRON),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_MAGNET_UPGRADE_2.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .unlocks("previous", LazyTrigger(ModItems.COIN_CHEST_MAGNET_UPGRADE_1))
                .save(conditional::addRecipe, "null:null");
        conditional.generateAdvancement(ItemID("upgrades/", ModItems.COIN_CHEST_MAGNET_UPGRADE_2).withPrefix(ADV_PREFIX)).build(consumer, ItemID("upgrades/", ModItems.COIN_CHEST_MAGNET_UPGRADE_2));
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.CoinChestUpgradeMagnet.INSTANCE);
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.COIN_CHEST_MAGNET_UPGRADE_2.get()),
                        Ingredient.of(Tags.Items.INGOTS_GOLD),
                        RecipeCategory.MISC,
                        ModItems.COIN_CHEST_MAGNET_UPGRADE_3.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .unlocks("previous", LazyTrigger(ModItems.COIN_CHEST_MAGNET_UPGRADE_2))
                .save(conditional::addRecipe, "null:null");
        conditional.generateAdvancement(ItemID("upgrades/", ModItems.COIN_CHEST_MAGNET_UPGRADE_3).withPrefix(ADV_PREFIX)).build(consumer, ItemID("upgrades/", ModItems.COIN_CHEST_MAGNET_UPGRADE_3));
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.CoinChestUpgradeMagnet.INSTANCE);
        SmithingTransformRecipeBuilder.smithing(
                        TEMPLATE,
                        Ingredient.of(ModItems.COIN_CHEST_MAGNET_UPGRADE_3.get()),
                        Ingredient.of(Tags.Items.GEMS_EMERALD),
                        RecipeCategory.MISC,
                        ModItems.COIN_CHEST_MAGNET_UPGRADE_4.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .unlocks("previous", LazyTrigger(ModItems.COIN_CHEST_MAGNET_UPGRADE_3))
                .save(conditional::addRecipe, "null:null");
        conditional.generateAdvancement(ItemID("upgrades/", ModItems.COIN_CHEST_MAGNET_UPGRADE_4).withPrefix(ADV_PREFIX)).build(consumer, ItemID("upgrades/", ModItems.COIN_CHEST_MAGNET_UPGRADE_4));

        //Security Upgrade
        conditional = ConditionalRecipe.builder().addCondition(LCCraftingConditions.CoinChestUpgradeSecurity.INSTANCE);
        SmithingTransformRecipeBuilder.smithing(
                TEMPLATE,
                Ingredient.of(Items.OBSIDIAN),
                Ingredient.of(Tags.Items.GEMS_DIAMOND),
                RecipeCategory.MISC,
                ModItems.COIN_CHEST_SECURITY_UPGRADE.get())
                .unlocks("money", MoneyKnowledge())
                .unlocks("coin_chest", LazyTrigger(ModBlocks.COIN_CHEST))
                .save(conditional::addRecipe, "null:null");
        conditional.generateAdvancement(ItemID("upgrades/", ModItems.COIN_CHEST_SECURITY_UPGRADE).withPrefix(ADV_PREFIX)).build(consumer, ItemID("upgrades/", ModItems.COIN_CHEST_SECURITY_UPGRADE));


    }

    private static void GenerateWalletRecipes(@Nonnull Consumer<FinishedRecipe> consumer, List<Pair<Ingredient, RegistryObject<? extends ItemLike>>> ingredientWalletPairs)
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
            b.save(consumer, ID("wallet/" + ItemPath(wallet)));
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

    private static void GenerateSwapRecipes(@Nonnull Consumer<FinishedRecipe> consumer, ItemLike item1, ItemLike item2, List<Pair<String, CriterionTriggerInstance>> criteria)
    {
        String group = ItemPath(item2) + "_swap";
        GenerateSwapRecipe(consumer, item1, item2, group, criteria);
        GenerateSwapRecipe(consumer, item2, item1, group, criteria);
    }

    private static void GenerateSwapRecipe(@Nonnull Consumer<FinishedRecipe> consumer, ItemLike item1, ItemLike item2, @Nullable String group, List<Pair<String, CriterionTriggerInstance>> criteria)
    {
        ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item2)
                .group(group);
        ApplyCriteria(b, criteria);
        b.unlockedBy("other", LazyTrigger(item1))
                .requires(item1)
                .save(consumer, ID(ItemPath(item1) + "_swap"));
    }

    private static void GenerateCoinBlockRecipes(@Nonnull Consumer<FinishedRecipe> consumer, RegistryObject<? extends ItemLike> coin, RegistryObject<? extends ItemLike> coinPile, RegistryObject<? extends ItemLike> coinBlock)
    {
        //Coin -> Pile
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, coinPile.get())
                .group("coin_pile_from_coin")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("coin", LazyTrigger(coin))
                .requires(coin.get(), 9)
                .save(consumer, ID("coins/" + ItemPath(coinPile) + "_from_coin"));
        //Pile -> Block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, coinBlock.get())
                .group("coin_block_from_pile")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("pile", LazyTrigger(coinPile))
                .pattern("xx").pattern("xx")
                .define('x', coinPile.get())
                .save(consumer, ID("coins/" + ItemPath(coinBlock) + "_from_pile"));
        //Block -> Pile
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, coinPile.get(), 4)
                .group("coin_pile_from_block")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("block", LazyTrigger(coinBlock))
                .requires(coinBlock.get())
                .save(consumer, ID("coins/" + ItemPath(coinPile) + "_from_block"));
        //Pile -> Coin
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, coin.get(), 9)
                .group("coin_from_pile")
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("pile", LazyTrigger(coinPile))
                .requires(coinPile.get())
                .save(consumer, ID("coins/" + ItemPath(coin) + "_from_pile"));
    }

    private static void GenerateColoredDyeAndWashRecipes(@Nonnull Consumer<FinishedRecipe> consumer, RegistryObjectBundle<? extends ItemLike,Color> bundle, ItemLike cleanItem, @Nullable String dyeGroup, String prefix, List<Pair<String, CriterionTriggerInstance>> criteria)
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

    private static void GenerateMintAndMeltRecipes(@Nonnull Consumer<FinishedRecipe> consumer, RegistryObject<? extends ItemLike> coin, TagKey<Item> materialTag, ItemLike materialItem)
    {
        MintRecipeBuilder.mint(coin.get())
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("coin_mint", LazyTrigger(ModBlocks.MACHINE_MINT))
                .accepts(materialTag)
                .save(consumer, ItemID("coin_mint/mint_", coin));
        MintRecipeBuilder.melt(materialItem)
                .unlockedBy("money", MoneyKnowledge())
                .unlockedBy("coin_mint", LazyTrigger(ModBlocks.MACHINE_MINT))
                .accepts(coin.get())
                .save(consumer, ItemID("coin_mint/melt_", coin));
    }

    private static CriterionTriggerInstance MoneyKnowledge() { return LazyTrigger(LCTags.Items.COINS); }
    private static CriterionTriggerInstance TraderKnowledge() { return LazyTrigger(LCTags.Items.TRADER); }
    private static CriterionTriggerInstance TerminalKnowledge() { return LazyTrigger(LCTags.Items.NETWORK_TERMINAL); }

    private static CriterionTriggerInstance LazyTrigger(ItemLike item) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(item).build()); }
    private static CriterionTriggerInstance LazyTrigger(List<? extends ItemLike> items) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items.toArray(new ItemLike[0])).build()); }
    private static CriterionTriggerInstance LazyTrigger(RegistryObject<? extends ItemLike> item) { return LazyTrigger(item.get()); }
    private static CriterionTriggerInstance LazyTrigger(TagKey<Item> tag) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build()); }

    private static void ApplyCriteria(ShapelessRecipeBuilder builder, List<Pair<String,CriterionTriggerInstance>> criteria)
    {
        for(Pair<String,CriterionTriggerInstance> c : criteria)
            builder.unlockedBy(c.getFirst(), c.getSecond());
    }

    private static String ItemPath(ItemLike item) { return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath(); }
    private static String ItemPath(RegistryObject<? extends ItemLike> item) { return ItemPath(item.get()); }

    private static ResourceLocation ItemID(ItemLike item) { return ID(ItemPath(item)); }
    private static ResourceLocation ItemID(String prefix, ItemLike item) { return ID(prefix + ItemPath(item)); }
    private static ResourceLocation ItemID(RegistryObject<? extends ItemLike> item) { return ID(ItemPath(item)); }
    private static ResourceLocation ItemID(String prefix, RegistryObject<? extends ItemLike> item) { return ID(prefix + ItemPath(item)); }
    private static ResourceLocation WoodID(String prefix, WoodType woodType) { return ID(prefix + (woodType.isModded() ? woodType.getModID() + "/" : "") + woodType.name); }
    private static ResourceLocation ID(String path) { return new ResourceLocation(LightmansCurrency.MODID, path); }


}
