package io.github.lightman314.lightmanscurrency.datagen.common;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.config.data.ConfigCraftingCondition;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.api.helpers.ColorHelper;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.CoinMintRecipeBuilder;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.WalletUpgradeRecipeBuilder;
import io.github.lightman314.lightmanscurrency.datagen.helpers.WoodHelper;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LCRecipeProvider extends RecipeProvider {

    protected final String modid;
    protected LCRecipeProvider(HolderLookup.Provider registries,RecipeOutput output) { this(registries,output,LCApi.MODID); }
    protected LCRecipeProvider(HolderLookup.Provider registries,RecipeOutput output,String modid) {
        super(registries, output);
        this.modid = modid;
    }

    protected final HolderGetter<Item> itemHolderGetter() { return this.registries.lookupOrThrow(Registries.ITEM); }

    private static class LCRunner extends Runner
    {
        protected LCRunner(PackOutput packOutput,CompletableFuture<HolderLookup.Provider> registries) { super(packOutput, registries); }
        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) { return new LCRecipeProvider(registries,output); }
        @Override
        public String getName() { return "Lightman's Currency Recipes"; }
    }

    public static Runner create(PackOutput output, CompletableFuture<HolderLookup.Provider> registryFuture) { return new LCRunner(output,registryFuture); }

    @Override
    protected void buildRecipes() {

        //Trading Core
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCItems.TRADING_CORE)
                .unlockedBy("money", moneyKnowledge())
                .pattern("rqr").pattern("rdr").pattern("rpr")
                .define('r',Tags.Items.DUSTS_REDSTONE)
                .define('q',Tags.Items.GEMS_QUARTZ)
                .define('d',Items.DROPPER)
                .define('p',Items.COMPARATOR)
                .save(this.output);

        //Wallets
        List<Pair<Ingredient,DeferredItem<?>>> walletList = List.of(Pair.of(this.ingredient(Tags.Items.INGOTS_COPPER),LCItems.WALLET_COPPER),
                Pair.of(this.ingredient(Tags.Items.INGOTS_IRON),LCItems.WALLET_IRON),
                Pair.of(this.ingredient(Tags.Items.INGOTS_GOLD),LCItems.WALLET_GOLD),
                Pair.of(this.ingredient(Tags.Items.GEMS_EMERALD),LCItems.WALLET_EMERALD),
                Pair.of(this.ingredient(Tags.Items.GEMS_DIAMOND),LCItems.WALLET_DIAMOND),
                Pair.of(this.ingredient(Tags.Items.INGOTS_NETHERITE),LCItems.WALLET_NETHERITE),
                Pair.of(Ingredient.of(Items.NETHER_STAR),LCItems.WALLET_NETHER_STAR));

        this.generateWalletRecipes(walletList);
        this.generateWalletRecipeTail(ListHelper.replaceLast(walletList,Pair.of(Ingredient.of(Items.DRAGON_HEAD),LCItems.WALLET_ENDER_DRAGON)));

        //Machines
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.COIN_MINT)
                .unlockedBy("money",this.moneyKnowledge())
                .unlockedBy("material",this.simpleRequirement(LCTags.Items.COIN_MINTING_MATERIAL))
                .pattern("ipi").pattern("i i").pattern("sss")
                .define('i',Tags.Items.INGOTS_IRON)
                .define('p',Items.PISTON)
                .define('s',Items.SMOOTH_STONE)
                .save(this.output.withConditions(ConfigCraftingCondition.of(LCConfig.COMMON.canCraftCoinMint)));

        //ATM
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.ATM)
                .unlockedBy("money",this.moneyKnowledge())
                .unlockedBy("wallet",this.simpleRequirement(LCTags.Items.WALLET))
                .pattern("igi").pattern("igi").pattern("iri")
                .define('i',Tags.Items.INGOTS_IRON)
                .define('g',Tags.Items.GLASS_PANES_COLORLESS)
                .define('r',Tags.Items.DUSTS_REDSTONE)
                .save(this.output);
        //Portable ATM Swap Recipe
        this.generateSwapRecipes(LCItems.ATM_PORTABLE,LCBlocks.ATM);

        //Trading Terminal
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.TRADING_TERMINAL)
                .unlockedBy("money",this.moneyKnowledge())
                .unlockedBy("trader",this.traderKnowledge())
                .unlockedBy("ender",this.simpleRequirement(Items.ENDER_EYE))
                .pattern("sgs").pattern("sgs").pattern("iei")
                .define('e',Items.ENDER_EYE)
                .define('g',Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('i',Tags.Items.INGOTS_IRON)
                .define('s',Tags.Items.STONES)
                .save(this.output);
        //Portable Trading Terminal Swap Recipe
        this.generateSwapRecipes(LCItems.TRADING_TERMINAL_PORTABLE,LCBlocks.TRADING_TERMINAL);

        //Coin Mint Recipes
        this.mintAndMeltRecipe(Tags.Items.INGOTS_COPPER,Items.COPPER_INGOT,LCItems.COIN_COPPER,LCConfig.COMMON.coinMintMintableCopper,LCConfig.COMMON.coinMintMeltableCopper);
        this.mintAndMeltRecipe(Tags.Items.INGOTS_IRON,Items.IRON_INGOT,LCItems.COIN_IRON,LCConfig.COMMON.coinMintMintableIron,LCConfig.COMMON.coinMintMeltableIron);
        this.mintAndMeltRecipe(Tags.Items.INGOTS_GOLD,Items.GOLD_INGOT,LCItems.COIN_GOLD,LCConfig.COMMON.coinMintMintableGold,LCConfig.COMMON.coinMintMeltableGold);
        this.mintAndMeltRecipe(Tags.Items.GEMS_EMERALD,Items.EMERALD,LCItems.COIN_EMERALD,LCConfig.COMMON.coinMintMintableEmerald,LCConfig.COMMON.coinMintMeltableEmerald);
        this.mintAndMeltRecipe(Tags.Items.GEMS_DIAMOND,Items.DIAMOND,LCItems.COIN_DIAMOND,LCConfig.COMMON.coinMintMintableDiamond,LCConfig.COMMON.coinMintMeltableDiamond);
        this.mintAndMeltRecipe(Tags.Items.INGOTS_NETHERITE,Items.NETHERITE_INGOT,LCItems.COIN_NETHERITE,LCConfig.COMMON.coinMintMintableNetherite,LCConfig.COMMON.coinMintMeltableNetherite);

        //Coin Recipes
        this.generateCoinBlockRecipe(LCItems.COIN_COPPER,LCBlocks.COIN_PILE_COPPER,LCBlocks.COIN_BLOCK_COPPER);
        this.generateCoinBlockRecipe(LCItems.COIN_IRON,LCBlocks.COIN_PILE_IRON,LCBlocks.COIN_BLOCK_IRON);
        this.generateCoinBlockRecipe(LCItems.COIN_GOLD,LCBlocks.COIN_PILE_GOLD,LCBlocks.COIN_BLOCK_GOLD);
        this.generateCoinBlockRecipe(LCItems.COIN_EMERALD,LCBlocks.COIN_PILE_EMERALD,LCBlocks.COIN_BLOCK_EMERALD);
        this.generateCoinBlockRecipe(LCItems.COIN_DIAMOND,LCBlocks.COIN_PILE_DIAMOND,LCBlocks.COIN_BLOCK_DIAMOND);
        this.generateCoinBlockRecipe(LCItems.COIN_NETHERITE,LCBlocks.COIN_PILE_NETHERITE,LCBlocks.COIN_BLOCK_NETHERITE);
        //Chocolate Coin Recipes
        this.generateCoinBlockRecipe(LCItems.COIN_CHOCOLATE_COPPER,LCBlocks.COIN_PILE_CHOCOLATE_COPPER,LCBlocks.COIN_BLOCK_CHOCOLATE_COPPER,"chocolate_","money",this.simpleRequirement(LCTags.Items.MONEY_CHOCOLATE_COINS));
        this.generateCoinBlockRecipe(LCItems.COIN_CHOCOLATE_IRON,LCBlocks.COIN_PILE_CHOCOLATE_IRON,LCBlocks.COIN_BLOCK_CHOCOLATE_IRON,"chocolate_","money",this.simpleRequirement(LCTags.Items.MONEY_CHOCOLATE_COINS));
        this.generateCoinBlockRecipe(LCItems.COIN_CHOCOLATE_GOLD,LCBlocks.COIN_PILE_CHOCOLATE_GOLD,LCBlocks.COIN_BLOCK_CHOCOLATE_GOLD,"chocolate_","money",this.simpleRequirement(LCTags.Items.MONEY_CHOCOLATE_COINS));
        this.generateCoinBlockRecipe(LCItems.COIN_CHOCOLATE_EMERALD,LCBlocks.COIN_PILE_CHOCOLATE_EMERALD,LCBlocks.COIN_BLOCK_CHOCOLATE_EMERALD,"chocolate_","money",this.simpleRequirement(LCTags.Items.MONEY_CHOCOLATE_COINS));
        this.generateCoinBlockRecipe(LCItems.COIN_CHOCOLATE_DIAMOND,LCBlocks.COIN_PILE_CHOCOLATE_DIAMOND,LCBlocks.COIN_BLOCK_CHOCOLATE_DIAMOND,"chocolate_","money",this.simpleRequirement(LCTags.Items.MONEY_CHOCOLATE_COINS));
        this.generateCoinBlockRecipe(LCItems.COIN_CHOCOLATE_NETHERITE,LCBlocks.COIN_PILE_CHOCOLATE_NETHERITE,LCBlocks.COIN_BLOCK_CHOCOLATE_NETHERITE,"chocolate_","money",this.simpleRequirement(LCTags.Items.MONEY_CHOCOLATE_COINS));

        //Smithing Template
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCItems.UPGRADE_SMITHING_TEMPLATE)
                .unlockedBy("trader",traderKnowledge())
                .unlockedBy("core",simpleRequirement(LCItems.TRADING_CORE))
                .pattern("nnn").pattern("ncn").pattern("nnn")
                .define('n',Tags.Items.NUGGETS_IRON)
                .define('c',LCItems.TRADING_CORE)
                .save(this.output);
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCItems.UPGRADE_SMITHING_TEMPLATE,2)
                .unlockedBy("trader",traderKnowledge())
                .unlockedBy("core",simpleRequirement(LCItems.TRADING_CORE))
                .pattern("nnn").pattern("ntn").pattern("nnn")
                .define('n',Tags.Items.NUGGETS_IRON)
                .define('t',LCItems.UPGRADE_SMITHING_TEMPLATE)
                .save(this.output,id(LCItems.UPGRADE_SMITHING_TEMPLATE,"_clone_1"));
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCItems.UPGRADE_SMITHING_TEMPLATE,10)
                .unlockedBy("trader",traderKnowledge())
                .unlockedBy("core",simpleRequirement(LCItems.TRADING_CORE))
                .pattern("iii").pattern("iti").pattern("iii")
                .define('i',Tags.Items.INGOTS_IRON)
                .define('t',LCItems.UPGRADE_SMITHING_TEMPLATE)
                .save(this.output,id(LCItems.UPGRADE_SMITHING_TEMPLATE,"_clone_2"));

        //Upgrades
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(LCItems.UPGRADE_SMITHING_TEMPLATE),this.ingredient(Tags.Items.CHESTS_WOODEN),this.ingredient(Tags.Items.INGOTS_IRON),RecipeCategory.MISC,LCItems.ITEM_CAPACITY_UPGRADE_1.get())
                .unlocks("trader",traderKnowledge())
                .unlocks("template",simpleRequirement(LCItems.UPGRADE_SMITHING_TEMPLATE))
                .save(this.output,this.id("upgrades/",LCItems.ITEM_CAPACITY_UPGRADE_1));
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(LCItems.UPGRADE_SMITHING_TEMPLATE),Ingredient.of(LCItems.ITEM_CAPACITY_UPGRADE_1),this.ingredient(Tags.Items.INGOTS_GOLD),RecipeCategory.MISC,LCItems.ITEM_CAPACITY_UPGRADE_2.get())
                .unlocks("trader",traderKnowledge())
                .unlocks("template",simpleRequirement(LCItems.UPGRADE_SMITHING_TEMPLATE))
                .unlocks("previous",simpleRequirement(LCItems.ITEM_CAPACITY_UPGRADE_1))
                .save(this.output,this.id("upgrades/",LCItems.ITEM_CAPACITY_UPGRADE_2));
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(LCItems.UPGRADE_SMITHING_TEMPLATE),Ingredient.of(LCItems.ITEM_CAPACITY_UPGRADE_2),this.ingredient(Tags.Items.GEMS_DIAMOND),RecipeCategory.MISC,LCItems.ITEM_CAPACITY_UPGRADE_3.get())
                .unlocks("trader",traderKnowledge())
                .unlocks("template",simpleRequirement(LCItems.UPGRADE_SMITHING_TEMPLATE))
                .unlocks("previous",simpleRequirement(LCItems.ITEM_CAPACITY_UPGRADE_2))
                .save(this.output,this.id("upgrades/",LCItems.ITEM_CAPACITY_UPGRADE_3));
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(LCItems.UPGRADE_SMITHING_TEMPLATE),Ingredient.of(LCItems.ITEM_CAPACITY_UPGRADE_3),this.ingredient(Tags.Items.INGOTS_NETHERITE),RecipeCategory.MISC,LCItems.ITEM_CAPACITY_UPGRADE_4.get())
                .unlocks("trader",traderKnowledge())
                .unlocks("template",simpleRequirement(LCItems.UPGRADE_SMITHING_TEMPLATE))
                .unlocks("previous",simpleRequirement(LCItems.ITEM_CAPACITY_UPGRADE_3))
                .save(this.output,this.id("upgrades/",LCItems.ITEM_CAPACITY_UPGRADE_4));

        SmithingTransformRecipeBuilder.smithing(Ingredient.of(LCItems.UPGRADE_SMITHING_TEMPLATE),Ingredient.of(Items.ENDER_EYE),tag(Tags.Items.INGOTS_GOLD),RecipeCategory.MISC,LCItems.NETWORK_UPGRADE.get())
                .unlocks("trader",traderKnowledge())
                .unlocks("terminal",terminalKnowledge())
                .save(this.output,this.id("upgrades/",LCItems.NETWORK_UPGRADE));

        //Traders
        LCBlocks.DISPLAY_CASE.forEach((color,display) ->
            ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,display)
                    .group("trader_display_case")
                    .unlockedBy("money", moneyKnowledge())
                    .unlockedBy("trader", traderKnowledge())
                    .pattern("g").pattern("x").pattern("w")
                    .define('x',LCItems.TRADING_CORE)
                    .define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
                    .define('w',ColorHelper.getWoolBlock(color))
                    .save(this.output,id("traders/display_case/" + color.getResourceSafeName())));

        LCBlocks.SINGLE_SHELF.forEach((type,shelf) -> {
            ICondition condition = type.isVanilla() ? null : new ModLoadedCondition(type.getModID());
            WoodHelper.getSlab(type).ifPresent(slab ->
                ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,shelf)
                        .group("trader_single_shelf")
                        .unlockedBy("money",moneyKnowledge())
                        .unlockedBy("trader",traderKnowledge())
                        .pattern("x").pattern("s")
                        .define('x',LCItems.TRADING_CORE)
                        .define('s',slab)
                        .save(this.output(condition),this.id(type.generatePath("traders/single_shelf/")))
            );
        });

        LCBlocks.DOUBLE_SHELF.forEach((type,shelf) -> {
            ICondition condition = type.isVanilla() ? null : new ModLoadedCondition(type.getModID());
            ItemLike trader = LCBlocks.SINGLE_SHELF.get(type);
            if(trader == null)
                return;
            WoodHelper.getSlab(type).ifPresent(slab ->
                    ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,shelf)
                            .group("trader_double_shelf")
                            .unlockedBy("money",moneyKnowledge())
                            .unlockedBy("trader",traderKnowledge())
                            .unlockedBy("shelf",simpleRequirement(LCTags.Items.GROUP_SINGLE_SHELF))
                            .pattern("c").pattern("x").pattern("s")
                            .define('c',Tags.Items.CHESTS_WOODEN)
                            .define('x',trader)
                            .define('s',slab)
                            .save(this.output(condition),this.id(type.generatePath("traders/double_shelf/"))));
        });

        LCBlocks.CARD_DISPLAY.forEach((type,color,display) -> {
            ICondition condition = type.isVanilla() ? null : new ModLoadedCondition(type.getModID());
            WoodHelper.getLogAndPlank(type).ifPresent(pair ->
                ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,display)
                        .unlockedBy("money",moneyKnowledge())
                        .unlockedBy("trader",traderKnowledge())
                        .pattern("  w").pattern(" xl").pattern("llc")
                        .define('x',LCItems.TRADING_CORE)
                        .define('l',pair.getFirst())
                        .define('w',ColorHelper.getWoolBlock(color))
                        .define('c',Tags.Items.CHESTS_WOODEN)
                        .save(this.output(condition),
                                this.id(type.generatePath("traders/card_display/","/" + color.getResourceSafeName())))
            );
        });

        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.VENDING_MACHINE.get(VanillaColor.WHITE))
                .unlockedBy("money",moneyKnowledge())
                .unlockedBy("trader",traderKnowledge())
                .pattern("igi").pattern("igi").pattern("cxc")
                .define('x',LCItems.TRADING_CORE)
                .define('g',Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('i',Tags.Items.INGOTS_IRON)
                .define('c',Tags.Items.CHESTS_WOODEN)
                .save(this.output,this.id("traders/vending_machine/create"));
        this.generateColoredDyeAndWashRecipes(LCBlocks.VENDING_MACHINE,"vending_machine_dyes","traders/vending_machine/",Pair.of("money",moneyKnowledge()),Pair.of("trader",traderKnowledge()));

        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.LARGE_VENDING_MACHINE.get(VanillaColor.WHITE))
                .unlockedBy("money",moneyKnowledge())
                .unlockedBy("trader",traderKnowledge())
                .unlockedBy("vending_machine",simpleRequirement(LCTags.Items.GROUP_VENDING_MACHINE))
                .pattern("igi").pattern("igi").pattern("cxc")
                .define('x',LCBlocks.VENDING_MACHINE.get(VanillaColor.WHITE))
                .define('g',Tags.Items.GLASS_BLOCKS_COLORLESS)
                .define('i',Tags.Items.INGOTS_IRON)
                .define('c',Tags.Items.CHESTS_WOODEN)
                .save(this.output,this.id("traders/large_vending_machine/create"));
        this.generateColoredDyeAndWashRecipes(LCBlocks.LARGE_VENDING_MACHINE,"large_vending_machine_dyes","traders/large_vending_machine/",Pair.of("money",moneyKnowledge()),Pair.of("trader",traderKnowledge()));

        //Item Network Traders
        ICondition networkTraderCondition = ConfigCraftingCondition.of(LCConfig.COMMON.canCraftNetworkTraders);
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.ITEM_NETWORK_TRADER.get(1))
                .group("item_network_trader")
                .unlockedBy("money",moneyKnowledge())
                .unlockedBy("trader",traderKnowledge())
                .unlockedBy("terminal",terminalKnowledge())
                .pattern("ici").pattern("ixi").pattern("iei")
                .define('x',LCItems.TRADING_CORE)
                .define('e',Items.ENDER_EYE)
                .define('i',Tags.Items.INGOTS_IRON)
                .define('c',Tags.Items.CHESTS_WOODEN)
                .save(this.output.withConditions(networkTraderCondition),this.id("traders/network/",LCBlocks.ITEM_NETWORK_TRADER.get(1)));

        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.ITEM_NETWORK_TRADER.get(2))
                .group("item_network_trader")
                .unlockedBy("money",moneyKnowledge())
                .unlockedBy("trader",traderKnowledge())
                .unlockedBy("terminal",terminalKnowledge())
                .unlockedBy("previous",simpleRequirement(LCBlocks.ITEM_NETWORK_TRADER.get(1)))
                .pattern("c").pattern("x").pattern("i")
                .define('x',LCBlocks.ITEM_NETWORK_TRADER.get(1))
                .define('i',Tags.Items.INGOTS_IRON)
                .define('c',Tags.Items.CHESTS_WOODEN)
                .save(this.output.withConditions(networkTraderCondition),this.id("traders/network/",LCBlocks.ITEM_NETWORK_TRADER.get(2)));

        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.ITEM_NETWORK_TRADER.get(3))
                .group("item_network_trader")
                .unlockedBy("money",moneyKnowledge())
                .unlockedBy("trader",traderKnowledge())
                .unlockedBy("terminal",terminalKnowledge())
                .unlockedBy("previous",simpleRequirement(LCBlocks.ITEM_NETWORK_TRADER.get(2)))
                .pattern("c").pattern("x").pattern("i")
                .define('x',LCBlocks.ITEM_NETWORK_TRADER.get(2))
                .define('i',Tags.Items.INGOTS_IRON)
                .define('c',Tags.Items.CHESTS_WOODEN)
                .save(this.output.withConditions(networkTraderCondition),this.id("traders/network/",LCBlocks.ITEM_NETWORK_TRADER.get(3)));

        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,LCBlocks.ITEM_NETWORK_TRADER.get(4))
                .group("item_network_trader")
                .unlockedBy("money",moneyKnowledge())
                .unlockedBy("trader",traderKnowledge())
                .unlockedBy("terminal",terminalKnowledge())
                .unlockedBy("previous",simpleRequirement(LCBlocks.ITEM_NETWORK_TRADER.get(3)))
                .pattern("c").pattern("x").pattern("i")
                .define('x',LCBlocks.ITEM_NETWORK_TRADER.get(3))
                .define('i',Tags.Items.INGOTS_IRON)
                .define('c',Tags.Items.CHESTS_WOODEN)
                .save(this.output.withConditions(networkTraderCondition),this.id("traders/network/",LCBlocks.ITEM_NETWORK_TRADER.get(4)));

    }

    protected final void generateWalletRecipes(List<Pair<Ingredient,DeferredItem<?>>> ingredientWalletPairs) {
        Ingredient leather = this.ingredient(Tags.Items.LEATHERS);
        var ingredients = ingredientWalletPairs.stream().map(Pair::getFirst).toList();
        var wallets = ingredientWalletPairs.stream().map(Pair::getSecond).toList();
        //Default Wallet Recipes
        for(int w = 0; w < wallets.size(); ++w)
        {
            DeferredItem<?> wallet = wallets.get(w);
            ShapelessRecipeBuilder b = ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,wallet)
                    .group("wallet_crafting")
                    .unlockedBy("coin", moneyKnowledge())
                    .unlockedBy("wallet",simpleRequirement(LCTags.Items.WALLET))
                    .requires(leather);
            for(int i = 0; i < ingredients.size() && i <= w; ++i)
                b.requires(ingredients.get(i));
            b.requires(leather).save(this.output,id("wallet/",wallet));
        }

        //Upgrade Wallet Recipes
        for(int w = 0; w < wallets.size() - 1; ++w)
        {
            for(int w2 = w + 1; w2 < wallets.size(); ++w2)
            {
                DeferredItem<?> first = wallets.get(w);
                DeferredItem<?> result = wallets.get(w2);
                WalletUpgradeRecipeBuilder b = WalletUpgradeRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,result)
                        .group("wallet_upgrading")
                        .unlockedBy("coin",moneyKnowledge())
                        .unlockedBy("wallet",simpleRequirement(LCTags.Items.WALLET))
                        .requires(first);
                for(int i = w + 1; i < ingredients.size() && i <= w2; ++i)
                    b.requires(ingredients.get(i));
                b.save(this.output,id("wallet/upgrading/" + this.path(first) + "_to_" + this.path(result)));
            }
        }
    }

    /**
     * Same as {@link #generateWalletRecipes(List)} but it assumes all wallets before it have already had their recipes generated,
     * and thus only generates the full recipe for the final wallet, and only generates upgrades from each wallet TO the final wallet
     */
    protected final void generateWalletRecipeTail(List<Pair<Ingredient,DeferredItem<?>>> ingredientWalletPairs) {
        Ingredient leather = this.ingredient(Tags.Items.LEATHERS);
        var ingredients = ingredientWalletPairs.stream().map(Pair::getFirst).toList();
        var wallets = ingredientWalletPairs.stream().map(Pair::getSecond).toList();
        //Default Wallet Recipe
        DeferredItem<?> result = wallets.getLast();
        ShapelessRecipeBuilder b1 = ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,result)
                .group("wallet_crafting")
                .unlockedBy("coin",moneyKnowledge())
                .unlockedBy("wallet",simpleRequirement(LCTags.Items.WALLET))
                .requires(leather);
        for(Ingredient ingredient : ingredients)
            b1.requires(ingredient);
        b1.requires(leather).save(this.output,id("wallet/",result));

        String resultPath = this.path(result);
        //Upgrade Recipes
        for(int w = 0; w < wallets.size() - 1; ++w)
        {
            DeferredItem<?> first = wallets.get(w);
            WalletUpgradeRecipeBuilder b = WalletUpgradeRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,result)
                    .group("wallet_upgrading")
                    .unlockedBy("coin",moneyKnowledge())
                    .unlockedBy("wallet",simpleRequirement(LCTags.Items.WALLET))
                    .requires(first);
            for(int i = w + 1; i < ingredients.size(); ++i)
                b.requires(ingredients.get(i));
            b.save(this.output,id("wallet/upgrading/" + this.path(first) + "_to_" + resultPath));
        }
    }

    /**
     * Same as {@link #generateWalletRecipes(List)} but it assumes all wallets other than the first one have already had their recipes generated,
     * and thus only generates the full recipe for the first wallet, and only generates upgrades from the first wallet to each wallet above
     */
    protected final void generateWalletRecipeHead(List<Pair<Ingredient,DeferredItem<?>>> ingredientWalletPairs) {
        Ingredient leather = this.ingredient(Tags.Items.LEATHERS);
        var ingredients = ingredientWalletPairs.stream().map(Pair::getFirst).toList();
        var wallets = ingredientWalletPairs.stream().map(Pair::getSecond).toList();
        //Default Wallet Recipe
        DeferredItem<?> first = wallets.getFirst();
        ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,first)
                .group("wallet_crafting")
                .unlockedBy("coin",moneyKnowledge())
                .unlockedBy("wallet",simpleRequirement(LCTags.Items.WALLET))
                .requires(leather)
                .requires(ingredients.getFirst())
                .requires(leather)
                .save(this.output,id("wallet/",first));

        String firstPath = this.path(first);
        //Upgrade Recipes
        for(int w = 1; w < wallets.size(); ++w)
        {
            DeferredItem<?> target = wallets.get(w);
            WalletUpgradeRecipeBuilder b = WalletUpgradeRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,target)
                    .group("wallet_upgrading")
                    .unlockedBy("coin",moneyKnowledge())
                    .unlockedBy("wallet",simpleRequirement(LCTags.Items.WALLET))
                    .requires(first);
            for(int i = 1; i < w && i < ingredients.size(); ++i)
                b.requires(ingredients.get(i));
            b.save(this.output,id("wallet/upgrading/" + firstPath + "_to_" + this.path(target)));
        }
    }

    protected final void mintAndMeltRecipe(TagKey<Item> materialTag,ItemLike material,ItemLike coin,BooleanOption canMint,BooleanOption canMelt) {
        CoinMintRecipeBuilder.create(this.itemHolderGetter(),coin)
                .requires(materialTag)
                .group("coin_minting")
                .unlockedBy("coins",this.moneyKnowledge())
                .unlockedBy("material",this.simpleRequirement(materialTag))
                .save(this.output.withConditions(ConfigCraftingCondition.of(LCConfig.COMMON.coinMintCanMint),ConfigCraftingCondition.of(canMint)),this.id("coin_mint/mint_",coin));

        CoinMintRecipeBuilder.create(this.itemHolderGetter(),material)
                .requires(coin)
                .group("coin_melting")
                .unlockedBy("coins",this.moneyKnowledge())
                .unlockedBy("material",this.simpleRequirement(coin))
                .save(this.output.withConditions(ConfigCraftingCondition.of(LCConfig.COMMON.coinMintCanMelt),ConfigCraftingCondition.of(canMelt)),this.id("coin_mint/melt_",coin));
    }

    protected final void generateCoinBlockRecipe(ItemLike coin,ItemLike pile,ItemLike block) { this.generateCoinBlockRecipe(coin,pile,block,""); }
    protected final void generateCoinBlockRecipe(ItemLike coin,ItemLike pile,ItemLike block,String groupPrefix) { this.generateCoinBlockRecipe(coin,pile,block,groupPrefix,"money",this.moneyKnowledge()); }
    protected final void generateCoinBlockRecipe(ItemLike coin,ItemLike pile,ItemLike block,String groupPrefix,String criterionName,Criterion<?> criterion) {

        //Coin -> Pile
        ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,pile)
                .group(groupPrefix + "coin_pile_from_coin")
                .unlockedBy(criterionName,criterion)
                .unlockedBy("coin",this.simpleRequirement(coin))
                .requires(coin,9)
                .save(this.output,this.id("coins/" + this.path(pile) + "_from_coin"));
        //Pile -> Block
        ShapedRecipeBuilder.shaped(this.itemHolderGetter(),RecipeCategory.MISC,block)
                .group(groupPrefix + "coin_block_from_pile")
                .unlockedBy(criterionName,criterion)
                .unlockedBy("pile",this.simpleRequirement(pile))
                .pattern("xx").pattern("xx")
                .define('x',pile)
                .save(this.output,this.id("coins/" + this.path(block) + "_from_pile"));
        //Block -> Pile
        ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,pile,4)
                .group(groupPrefix + "coin_pile_from_block")
                .unlockedBy(criterionName,criterion)
                .unlockedBy("block",this.simpleRequirement(block))
                .requires(block)
                .save(this.output,this.id("coins/" + this.path(pile) + "_from_block"));
        //Pile -> Coin
        ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,coin,9)
                .group(groupPrefix + "coin_from_pile")
                .unlockedBy(criterionName,criterion)
                .unlockedBy("pile",this.simpleRequirement(pile))
                .requires(pile)
                .save(this.output,this.id("coins/" + this.path(coin) + "_from_pile"));

    }

    protected final void generateSwapRecipes(ItemLike itemA,ItemLike itemB) {
        String itemAName = this.path(itemA);
        String itemBName = this.path(itemB);
        String group = "swap_" + itemAName + "_and_" + itemBName;
        //Craft B from A
        ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,itemB)
                .group(group)
                .unlockedBy("other",this.simpleRequirement(itemA))
                .requires(itemA)
                .save(this.output,this.id(this.itemID(itemB).withPrefix("swap_").withSuffix("_with_" + itemAName)));
        //Craft A from B
        ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,itemA)
                .group(group)
                .unlockedBy("other",this.simpleRequirement(itemB))
                .requires(itemB)
                .save(this.output,this.id(this.itemID(itemA).withPrefix("swap_").withSuffix("_with_" + itemBName)));
    }

    protected final <X extends ItemLike,T extends X>void generateColoredDyeAndWashRecipes(DeferredHolderBundle<VanillaColor,X,T> bundle,String group,String idPrefix,Pair<String,Criterion<?>>... requirements) {
        //Dye Recipe
        ItemLike white = bundle.get(VanillaColor.WHITE);
        List<ItemLike> colored = new ArrayList<>();
        for(VanillaColor color : VanillaColor.values()) {
            if(color == VanillaColor.WHITE)
                continue;
            ItemLike result = bundle.get(color);
            ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,result)
                    .group(group)
                    .unlockedBy("uncolored",this.simpleRequirement(white))
                    .requires(white)
                    .requires(color.getDyeTag());
            for(var crit : requirements)
                builder.unlockedBy(crit.getFirst(),crit.getSecond());
            builder.save(this.output,this.id(idPrefix + "dye_" + color.getResourceSafeName()));
            colored.add(result);
        }
        //Generate Washing Recipe
        ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(this.itemHolderGetter(),RecipeCategory.MISC,white)
                .group(group)
                .unlockedBy("colored",this.simpleRequirement(colored))
                .requires(Ingredient.of(colored.toArray(ItemLike[]::new)))
                .requires(Items.WATER_BUCKET);
        for(var crit : requirements)
            builder.unlockedBy(crit.getFirst(),crit.getSecond());
        builder.save(this.output,this.id(idPrefix + "washing"));
    }

    protected final Criterion<?> moneyKnowledge() { return this.simpleRequirement(LCTags.Items.MONEY); }
    protected final Criterion<?> traderKnowledge() { return this.simpleRequirement(LCTags.Items.TRADERS); }
    protected final Criterion<?> terminalKnowledge() { return this.simpleRequirement(LCTags.Items.NETWORK_TERMINAL); }

    protected final Criterion<?> simpleRequirement(ItemLike item) { return InventoryChangeTrigger.TriggerInstance.hasItems(item); }
    protected final Criterion<?> simpleRequirement(List<ItemLike> itemList) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(this.itemHolderGetter(),itemList.toArray(ItemLike[]::new))); }
    protected final Criterion<?> simpleRequirement(TagKey<Item> itemTag) { return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(this.itemHolderGetter(),itemTag)); }

    protected final HolderSet<Item> set(TagKey<Item> tag) { return this.itemHolderGetter().getOrThrow(tag); }
    protected final Ingredient ingredient(TagKey<Item> tag) { return Ingredient.of(this.set(tag)); }

    protected final RecipeOutput output(@Nullable ICondition condition) { return condition == null ? this.output : this.output.withConditions(condition); }

    protected final String path(ItemLike item) { return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath(); }
    protected final Identifier itemID(ItemLike item) { return BuiltInRegistries.ITEM.getKey(item.asItem()); }
    protected final ResourceKey<Recipe<?>> id(ItemLike item) { return id(this.itemID(item)); }
    protected final ResourceKey<Recipe<?>> id(String prefix,ItemLike  item) { return id(this.itemID(item).withPrefix(prefix)); }
    protected final ResourceKey<Recipe<?>> id(ItemLike item,String suffix) { return id(this.itemID(item).withSuffix(suffix)); }
    protected final ResourceKey<Recipe<?>> id(String prefix,ItemLike item,String suffix) { return id(this.itemID(item).withSuffix(suffix).withPrefix(prefix)); }
    protected final ResourceKey<Recipe<?>> id(Identifier id) { return ResourceKey.create(Registries.RECIPE,id); }
    protected final ResourceKey<Recipe<?>> id(String path) { return id(Identifier.fromNamespaceAndPath(this.modid,path)); }

}