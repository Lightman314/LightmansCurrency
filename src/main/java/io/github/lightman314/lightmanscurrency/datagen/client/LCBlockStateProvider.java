package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.MoneyBagBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.items.AncientCoinItem;
import io.github.lightman314.lightmanscurrency.common.items.MoneyBagItem;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.datagen.util.ColorHelper;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class LCBlockStateProvider extends BlockStateProvider {

    public LCBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, LightmansCurrency.MODID, exFileHelper);
    }

    protected final ModelFile EMPTY_MODEL = this.lazyBlockModel("empty",true);

    private static final ResourceLocation UPGRADE_TIER_COPPER = VersionUtil.lcResource("item/upgrade_tier/copper");
    private static final ResourceLocation UPGRADE_TIER_IRON = VersionUtil.lcResource("item/upgrade_tier/iron");
    private static final ResourceLocation UPGRADE_TIER_GOLD = VersionUtil.lcResource("item/upgrade_tier/gold");
    private static final ResourceLocation UPGRADE_TIER_EMERALD = VersionUtil.lcResource("item/upgrade_tier/emerald");
    private static final ResourceLocation UPGRADE_TIER_DIAMOND = VersionUtil.lcResource("item/upgrade_tier/diamond");
    private static final ResourceLocation UPGRADE_TIER_NETHERITE = VersionUtil.lcResource("item/upgrade_tier/netherite");

    private static final ResourceLocation UPGRADE_ITEM_CAPACITY = VersionUtil.lcResource("item/item_capacity_upgrade");
    private static final ResourceLocation UPGRADE_TRADE_OFFER = VersionUtil.lcResource("item/trading_core");
    private static final ResourceLocation UPGRADE_NETWORK = VersionUtil.vanillaResource("item/ender_eye");
    private static final ResourceLocation UPGRADE_VOID = VersionUtil.vanillaResource("item/barrier");
    private static final ResourceLocation UPGRADE_SPEED = VersionUtil.vanillaResource("item/clock_00");
    private static final ResourceLocation UPGRADE_HOPPER = VersionUtil.vanillaResource("item/hopper");
    private static final ResourceLocation UPGRADE_CC_SECURITY = VersionUtil.lcResource("item/coin_chest_security_upgrade");
    private static final ResourceLocation UPGRADE_CC_BANK = VersionUtil.lcResource("item/coin_chest_bank_upgrade");
    private static final ResourceLocation UPGRADE_CC_EXCHANGE = VersionUtil.lcResource("item/coin_chest_exchange_upgrade");
    private static final ResourceLocation UPGRADE_CC_MAGNET = VersionUtil.vanillaResource("item/ender_pearl");

    private static final ResourceLocation WALLET_MODEL_BASE = WalletItem.lazyModel("wallet_base");

    @Override
    protected void registerStatesAndModels() {
        //Coins
        this.registerBasicItem(ModItems.COIN_COPPER);
        this.registerBasicItem(ModItems.COIN_IRON);
        this.registerBasicItem(ModItems.COIN_GOLD);
        this.registerBasicItem(ModItems.COIN_EMERALD);
        this.registerBasicItem(ModItems.COIN_DIAMOND);
        this.registerBasicItem(ModItems.COIN_NETHERITE);
        //Chocolate Coins
        this.registerBasicItem(ModItems.COIN_CHOCOLATE_COPPER);
        this.registerBasicItem(ModItems.COIN_CHOCOLATE_IRON);
        this.registerBasicItem(ModItems.COIN_CHOCOLATE_GOLD);
        this.registerBasicItem(ModItems.COIN_CHOCOLATE_EMERALD);
        this.registerBasicItem(ModItems.COIN_CHOCOLATE_DIAMOND);
        this.registerBasicItem(ModItems.COIN_CHOCOLATE_NETHERITE);
        //Coin Piles
        this.registerCoinPile(ModBlocks.COINPILE_COPPER);
        this.registerCoinPile(ModBlocks.COINPILE_IRON);
        this.registerCoinPile(ModBlocks.COINPILE_GOLD);
        this.registerCoinPile(ModBlocks.COINPILE_EMERALD);
        this.registerCoinPile(ModBlocks.COINPILE_DIAMOND);
        this.registerCoinPile(ModBlocks.COINPILE_NETHERITE);
        //Chocolate Coin Piles
        this.registerCoinPile(ModBlocks.COINPILE_CHOCOLATE_COPPER);
        this.registerCoinPile(ModBlocks.COINPILE_CHOCOLATE_IRON);
        this.registerCoinPile(ModBlocks.COINPILE_CHOCOLATE_GOLD);
        this.registerCoinPile(ModBlocks.COINPILE_CHOCOLATE_EMERALD);
        this.registerCoinPile(ModBlocks.COINPILE_CHOCOLATE_DIAMOND);
        this.registerCoinPile(ModBlocks.COINPILE_CHOCOLATE_NETHERITE);
        //Coin Blocks
        this.registerCoinBlock(ModBlocks.COINBLOCK_COPPER);
        this.registerCoinBlock(ModBlocks.COINBLOCK_IRON);
        this.registerCoinBlock(ModBlocks.COINBLOCK_GOLD);
        this.registerCoinBlock(ModBlocks.COINBLOCK_EMERALD);
        this.registerCoinBlock(ModBlocks.COINBLOCK_DIAMOND);
        this.registerCoinBlock(ModBlocks.COINBLOCK_NETHERITE);
        //Chocolate Coin Blocks
        this.registerCoinBlock(ModBlocks.COINBLOCK_CHOCOLATE_COPPER);
        this.registerCoinBlock(ModBlocks.COINBLOCK_CHOCOLATE_IRON);
        this.registerCoinBlock(ModBlocks.COINBLOCK_CHOCOLATE_GOLD);
        this.registerCoinBlock(ModBlocks.COINBLOCK_CHOCOLATE_EMERALD);
        this.registerCoinBlock(ModBlocks.COINBLOCK_CHOCOLATE_DIAMOND);
        this.registerCoinBlock(ModBlocks.COINBLOCK_CHOCOLATE_NETHERITE);

        //Ancient Coins
        this.registerAncientCoin(ModItems.COIN_ANCIENT);

        //Trading Core
        this.registerBasicItem(ModItems.TRADING_CORE);

        //Wallets
        this.registerWalletItem(ModItems.WALLET_LEATHER);
        this.registerWalletItem(ModItems.WALLET_COPPER);
        this.registerWalletItem(ModItems.WALLET_IRON);
        this.registerWalletItem(ModItems.WALLET_GOLD);
        this.registerWalletItem(ModItems.WALLET_EMERALD);
        this.registerWalletItem(ModItems.WALLET_DIAMOND);
        this.registerWalletItem(ModItems.WALLET_NETHERITE);
        this.registerWalletItem(ModItems.WALLET_NETHER_STAR);
        //Ender Dragon Wallet has its own model, so we don't need to datagen a generic wallet model
        this.registerBasicItem(ModItems.WALLET_ENDER_DRAGON);

        //ATM
        this.registerTallRotatable(ModBlocks.ATM, "atm_top", "atm_bottom", "atm", true);
        //this.registerBasicItem(ModItems.PORTABLE_ATM);

        //ATM Card
        this.registerLayeredItem(ModItems.ATM_CARD);
        //Prepaid Card
        this.registerLayeredItem(ModItems.PREPAID_CARD);

        //Coin Mint
        this.registerRotatable(ModBlocks.COIN_MINT);

        //Cash Register
        this.registerRotatable(ModBlocks.CASH_REGISTER, "cash_register_modern", true);

        //Display Case
        ModBlocks.DISPLAY_CASE.forEach((color,block) -> {
            ResourceLocation woolTexture = ColorHelper.GetWoolTextureOfColor(color);
            String modelID = this.lazyColoredID("block/display_case/",color);
            //Generate the models
            this.models().getBuilder(modelID).parent(this.lazyBlockModel("display_case/base",true))
                    .texture("wool",woolTexture);
            //Generate the block state & item model
            this.registerSimpleState(block,modelID);
        });

        //Vending Machines
        ModBlocks.VENDING_MACHINE.forEach((color, block) -> {
            //Collect IDs and Textures
            ResourceLocation interiorTexture = VersionUtil.lcResource(this.lazyColoredID("block/vending_machine/", color, "_interior"));
            ResourceLocation exteriorTexture = VersionUtil.lcResource(this.lazyColoredID("block/vending_machine/", color, "_exterior"));
            String topID = this.lazyColoredID("block/vending_machine/", color, "_top");
            String bottomID = this.lazyColoredID("block/vending_machine/", color, "_bottom");
            String itemID = this.lazyColoredID("block/vending_machine/", color, "_item");
            //Generate the models
            this.models().getBuilder(topID).parent(this.lazyBlockModel("vending_machine/base_top", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(bottomID).parent(this.lazyBlockModel("vending_machine/base_bottom", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(itemID).parent(this.lazyBlockModel("vending_machine/base_item", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            //Generate the block state
            this.registerTallRotatable(block, topID, bottomID, itemID, false);
        });

        //Large Vending Machines
        ModBlocks.VENDING_MACHINE_LARGE.forEach((color,block) -> {
            //Collect IDs and Textures
            ResourceLocation interiorTexture = VersionUtil.lcResource(this.lazyColoredID("block/large_vending_machine/", color, "_interior"));
            ResourceLocation exteriorTexture = VersionUtil.lcResource(this.lazyColoredID("block/large_vending_machine/", color, "_exterior"));
            String topLeftID = this.lazyColoredID("block/large_vending_machine/", color, "_top_left");
            String topRightID = this.lazyColoredID("block/large_vending_machine/", color, "_top_right");
            String bottomLeftID = this.lazyColoredID("block/large_vending_machine/", color, "_bottom_left");
            String bottomRightID = this.lazyColoredID("block/large_vending_machine/", color, "_bottom_right");
            String itemID = this.lazyColoredID("block/large_vending_machine/", color, "_item");
            //Generate the models
            this.models().getBuilder(topLeftID).parent(this.lazyBlockModel("large_vending_machine/base_top_left", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(topRightID).parent(this.lazyBlockModel("large_vending_machine/base_top_right", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(bottomLeftID).parent(this.lazyBlockModel("large_vending_machine/base_bottom_left", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(bottomRightID).parent(this.lazyBlockModel("large_vending_machine/base_bottom_right", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(itemID).parent(this.lazyBlockModel("large_vending_machine/base_item", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            //Generate the block state
            this.registerTallWideRotatable(block, topLeftID, topRightID, bottomLeftID, bottomRightID, itemID, false);
        });

        //Shelf
        ModBlocks.SHELF.forEach((type,block) -> {
            String modelID = this.lazyWoodenID("block/shelf/", type);
            //Collect the WoodData
            WoodData data = type.getData();
            if(data != null)
            {
                //Build the model
                this.models().getBuilder(modelID).parent(this.lazyBlockModel("shelf/base", true))
                        .texture("main", data.plankTexture);
            }
            else
                LightmansCurrency.LogWarning("Could not generate models for wood type '" + type.id + "' as it has no wood data!");
            //Generate the block state
            this.registerRotatable(block, modelID, false);
        });

        ModBlocks.SHELF_2x2.forEach((type,block) -> {
            String modelID = this.lazyWoodenID("block/shelf2/", type);
            //Collect the WoodData
            WoodData data = type.getData();
            if(data != null)
            {
                //Build the model
                this.models().getBuilder(modelID).parent(this.lazyBlockModel("shelf2/base", true))
                        .texture("main", data.plankTexture);
            }
            else
                LightmansCurrency.LogWarning("Could not generate models for wood type '" + type.id + "' as it has no wood data!");
            //Generate the block state
            this.registerRotatable(block, modelID, false);
        });

        //Card Display
        ModBlocks.CARD_DISPLAY.forEach((type,color,block) -> {
            String modelID = this.lazyWoodenID("block/card_display/", type, "/" + color.getResourceSafeName());
            //Collect the WoodData
            WoodData data = type.getData();
            if(data != null)
            {
                //Build the model
                this.models().getBuilder(modelID).parent(this.lazyBlockModel("card_display/base", true))
                        .texture("log", data.logSideTexture)
                        .texture("logtop", data.logTopTexture)
                        .texture("plank", data.plankTexture)
                        .texture("wool", ColorHelper.GetWoolTextureOfColor(color));
            }
            //Generate the block state
            this.registerRotatable(block, modelID, false);
        });

        //Freezer
        ModBlocks.FREEZER.forEach((color,block) -> {
            //Collect IDs and Textures
            ResourceLocation concreteTexture = ColorHelper.GetConcreteTextureOfColor(color);
            String topModelID = this.lazyColoredID("block/freezer/", color, "_top");
            String bottomModelID = this.lazyColoredID("block/freezer/", color, "_bottom");
            String itemModelID = this.lazyColoredID("block/freezer/", color, "_item");
            String doorModelID = this.lazyColoredID("block/freezer/doors/", color);
            //Generate the models
            this.models().getBuilder(topModelID).parent(this.lazyBlockModel("freezer/base_top", true))
                    .texture("concrete", concreteTexture);
            this.models().getBuilder(bottomModelID).parent(this.lazyBlockModel("freezer/base_bottom", true))
                    .texture("concrete", concreteTexture);
            this.models().getBuilder(itemModelID).parent(this.lazyBlockModel("freezer/base_item", true))
                    .texture("concrete", concreteTexture);
            this.models().getBuilder(doorModelID).parent(this.lazyBlockModel("freezer/doors/base", true))
                    .texture("concrete", concreteTexture);
            //Generate the block state
            this.registerTallRotatable(block, topModelID, bottomModelID, itemModelID, false);
        });

        //Armor Display
        this.registerTallRotatable(ModBlocks.ARMOR_DISPLAY, "armor_display/top", "armor_display/bottom", "armor_display/item", true);

        //Ticket Kiosk
        this.registerTallRotatable(ModBlocks.TICKET_KIOSK, "ticket_kiosk_top", "ticket_kiosk_bottom", "ticket_kiosk", true);

        //Bookshelf
        ModBlocks.BOOKSHELF_TRADER.forEach((type,block) -> {
            String modelID = this.lazyWoodenID("block/bookshelf_trader/", type);
            //Collect the WoodData
            WoodData data = type.getData();
            if(data != null)
            {
                //Build the model
                this.models().getBuilder(modelID).parent(this.lazyBlockModel("bookshelf_trader/base", true))
                        .texture("main", data.plankTexture);
            }
            //Generate the block state
            this.registerRotatable(block, modelID, false);
        });

        //Slot Machine
        this.registerTallRotatable(ModBlocks.SLOT_MACHINE, "slot_machine/top", "slot_machine/bottom", "slot_machine/item", true);

        //Gatcha Machine
        ModBlocks.GACHA_MACHINE.forEach((color,block) -> {
            String modelID = this.lazyColoredID("block/gacha_machine/",color);
            //Build the model
            this.models().getBuilder(modelID).parent(this.lazyBlockModel("gacha_machine/base",true))
                    .texture("main",VersionUtil.lcResource("block/gacha_machine/" + color.getResourceSafeName()));
            //Generate the block state
            this.registerRotatable(block,modelID,false);
        });

        //Paygate
        this.registerPaygate(ModBlocks.PAYGATE, "paygate_powered", "paygate_unpowered");

        //Item Network Traders
        this.registerRotatable(ModBlocks.ITEM_NETWORK_TRADER_1, "item_network_trader_1", true);
        this.registerRotatable(ModBlocks.ITEM_NETWORK_TRADER_2, "item_network_trader_2", true);
        this.registerRotatable(ModBlocks.ITEM_NETWORK_TRADER_3, "item_network_trader_3", true);
        this.registerRotatable(ModBlocks.ITEM_NETWORK_TRADER_4, "item_network_trader_4", true);

        //Command Trader
        this.registerRotatable(ModBlocks.COMMAND_TRADER);

        //Item Trader Interface
        this.registerRotatable(ModBlocks.ITEM_TRADER_INTERFACE);

        //Terminal
        this.registerRotatable(ModBlocks.TERMINAL);
        //this.registerBasicItem(ModItems.PORTABLE_TERMINAL);

        //Gem Terminal
        this.registerRotatable(ModBlocks.GEM_TERMINAL);
        this.registerBasicItem(ModItems.PORTABLE_GEM_TERMINAL);

        //Ticket Station
        this.registerRotatable(ModBlocks.TICKET_STATION);
        //Tickets
        this.registerBasicItem(ModItems.TICKET);
        this.registerLayeredItem(ModItems.TICKET_MASTER);
        this.registerLayeredItem(ModItems.TICKET_PASS);
        this.registerBasicItem(ModItems.TICKET_STUB);
        //Golden Tickets
        this.registerBasicItem(ModItems.GOLDEN_TICKET);
        this.registerLayeredItem(ModItems.GOLDEN_TICKET_MASTER);
        this.registerLayeredItem(ModItems.GOLDEN_TICKET_PASS);
        this.registerBasicItem(ModItems.GOLDEN_TICKET_STUB);

        //Auction Stands
        ModBlocks.AUCTION_STAND.forEach((type,block) -> {
            String modelID = this.lazyWoodenID("block/auction_stand/", type);
            //Collect Wood Data
            WoodData data = type.getData();
            if(data != null)
            {
                //Generate the model
                this.models().getBuilder(modelID).parent(this.lazyBlockModel("auction_stand/base", true))
                        .texture("log", data.logSideTexture)
                        .texture("log_top", data.logTopTexture);
            }
            this.registerSimpleState(block, modelID);
        });

        //Coin Chest
        this.getVariantBuilder(ModBlocks.COIN_CHEST.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(new ModelFile.ExistingModelFile(VersionUtil.vanillaResource("block/chest"), this.models().existingFileHelper)).build());
        this.registerBlockItemModel(ModBlocks.COIN_CHEST, new ModelFile.ExistingModelFile(VersionUtil.vanillaResource("item/chest"), this.models().existingFileHelper));
        //Gatcha Ball
        this.itemModels().getBuilder("item/gacha_ball").parent(new ModelFile.ExistingModelFile(VersionUtil.vanillaResource("item/chest"), this.models().existingFileHelper));
        //Coin Jars
        this.registerRotatable(ModBlocks.PIGGY_BANK, "jars/piggy_bank", true);
        this.registerRotatable(ModBlocks.COINJAR_BLUE, "jars/coinjar_blue", true);
        this.registerMoneyBag(ModBlocks.MONEY_BAG,"money_bag");

        //Upgrades
        this.registerUpgradeItem(ModItems.ITEM_CAPACITY_UPGRADE_1,UPGRADE_ITEM_CAPACITY,UPGRADE_TIER_IRON);
        this.registerUpgradeItem(ModItems.ITEM_CAPACITY_UPGRADE_2,UPGRADE_ITEM_CAPACITY,UPGRADE_TIER_GOLD);
        this.registerUpgradeItem(ModItems.ITEM_CAPACITY_UPGRADE_3,UPGRADE_ITEM_CAPACITY,UPGRADE_TIER_DIAMOND);
        this.registerUpgradeItem(ModItems.ITEM_CAPACITY_UPGRADE_4,UPGRADE_ITEM_CAPACITY,UPGRADE_TIER_NETHERITE);

        this.registerUpgradeItem(ModItems.SPEED_UPGRADE_1,UPGRADE_SPEED,UPGRADE_TIER_IRON);
        this.registerUpgradeItem(ModItems.SPEED_UPGRADE_2,UPGRADE_SPEED,UPGRADE_TIER_GOLD);
        this.registerUpgradeItem(ModItems.SPEED_UPGRADE_3,UPGRADE_SPEED,UPGRADE_TIER_EMERALD);
        this.registerUpgradeItem(ModItems.SPEED_UPGRADE_4,UPGRADE_SPEED,UPGRADE_TIER_DIAMOND);
        this.registerUpgradeItem(ModItems.SPEED_UPGRADE_5,UPGRADE_SPEED,UPGRADE_TIER_NETHERITE);

        this.registerUpgradeItem(ModItems.OFFER_UPGRADE_1,UPGRADE_TRADE_OFFER,UPGRADE_TIER_COPPER);
        this.registerUpgradeItem(ModItems.OFFER_UPGRADE_2,UPGRADE_TRADE_OFFER,UPGRADE_TIER_IRON);
        this.registerUpgradeItem(ModItems.OFFER_UPGRADE_3,UPGRADE_TRADE_OFFER,UPGRADE_TIER_GOLD);
        this.registerUpgradeItem(ModItems.OFFER_UPGRADE_4,UPGRADE_TRADE_OFFER,UPGRADE_TIER_EMERALD);
        this.registerUpgradeItem(ModItems.OFFER_UPGRADE_5,UPGRADE_TRADE_OFFER,UPGRADE_TIER_DIAMOND);
        this.registerUpgradeItem(ModItems.OFFER_UPGRADE_6,UPGRADE_TRADE_OFFER,UPGRADE_TIER_NETHERITE);

        this.registerUpgradeItem(ModItems.NETWORK_UPGRADE,UPGRADE_NETWORK,UPGRADE_TIER_GOLD);

        this.registerUpgradeItem(ModItems.VOID_UPGRADE,UPGRADE_VOID,UPGRADE_TIER_GOLD);

        this.registerUpgradeItem(ModItems.HOPPER_UPGRADE,UPGRADE_HOPPER,UPGRADE_TIER_GOLD);

        this.registerUpgradeItem(ModItems.COIN_CHEST_EXCHANGE_UPGRADE,UPGRADE_CC_EXCHANGE,UPGRADE_TIER_IRON);
        this.registerUpgradeItem(ModItems.COIN_CHEST_MAGNET_UPGRADE_1,UPGRADE_CC_MAGNET,UPGRADE_TIER_COPPER);
        this.registerUpgradeItem(ModItems.COIN_CHEST_MAGNET_UPGRADE_2,UPGRADE_CC_MAGNET,UPGRADE_TIER_IRON);
        this.registerUpgradeItem(ModItems.COIN_CHEST_MAGNET_UPGRADE_3,UPGRADE_CC_MAGNET,UPGRADE_TIER_GOLD);
        this.registerUpgradeItem(ModItems.COIN_CHEST_MAGNET_UPGRADE_4,UPGRADE_CC_MAGNET,UPGRADE_TIER_EMERALD);
        this.registerUpgradeItem(ModItems.COIN_CHEST_BANK_UPGRADE,UPGRADE_CC_BANK,UPGRADE_TIER_DIAMOND);
        this.registerUpgradeItem(ModItems.COIN_CHEST_SECURITY_UPGRADE,UPGRADE_CC_SECURITY,UPGRADE_TIER_DIAMOND);

        //1.20 ONLY
        this.registerBasicItem(ModItems.UPGRADE_SMITHING_TEMPLATE);

        //2.1.2.2
        //Tax Block
        this.registerRotatable(ModBlocks.TAX_COLLECTOR);

        //2.1.2.3
        this.registerRotatable(ModBlocks.SUS_JAR, "jars/sus_jar", true);

        //2.2.4.1
        this.registerUpgradeItem(ModItems.INTERACTION_UPGRADE_1,UPGRADE_NETWORK,UPGRADE_TIER_EMERALD);
        this.registerUpgradeItem(ModItems.INTERACTION_UPGRADE_2,UPGRADE_NETWORK,UPGRADE_TIER_DIAMOND);
        this.registerUpgradeItem(ModItems.INTERACTION_UPGRADE_3,UPGRADE_NETWORK,UPGRADE_TIER_NETHERITE);

        //2.2.5.2
        //Variant Wand
        this.registerHandheldItem(ModItems.VARIANT_WAND);

        //Display Case Variants
        for(Color color : Color.values())
        {
            ResourceLocation woolTexture = ColorHelper.GetWoolTextureOfColor(color);
            String modelID = this.lazyColoredID("block/display_case/glassless/",color);
            //Generate the models
            this.models().getBuilder(modelID).parent(this.lazyBlockModel("display_case/glassless/base",true))
                    .texture("wool",woolTexture);
        }

        //Vending Machine Variants
        for(Color color : Color.values())
        {
            //Collect IDs and Textures
            ResourceLocation interiorTexture = VersionUtil.lcResource(this.lazyColoredID("block/vending_machine/footless/", color, "_interior"));
            ResourceLocation exteriorTexture = VersionUtil.lcResource(this.lazyColoredID("block/vending_machine/footless/", color, "_exterior"));
            String topID = this.lazyColoredID("block/vending_machine/footless/", color, "_top");
            String bottomID = this.lazyColoredID("block/vending_machine/footless/", color, "_bottom");
            String itemID = this.lazyColoredID("block/vending_machine/footless/", color, "_item");
            //Generate the models
            this.models().getBuilder(topID).parent(this.lazyBlockModel("vending_machine/footless/base_top", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(bottomID).parent(this.lazyBlockModel("vending_machine/footless/base_bottom", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(itemID).parent(this.lazyBlockModel("vending_machine/footless/base_item", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
        }

        //Large Vending Machine Variants
        for(Color color : Color.values())
        {
            //Collect IDs and Textures
            ResourceLocation interiorTexture = VersionUtil.lcResource(this.lazyColoredID("block/large_vending_machine/footless/", color, "_interior"));
            ResourceLocation exteriorTexture = VersionUtil.lcResource(this.lazyColoredID("block/large_vending_machine/footless/", color, "_exterior"));
            String topLeftID = this.lazyColoredID("block/large_vending_machine/footless/", color, "_top_left");
            String topRightID = this.lazyColoredID("block/large_vending_machine/footless/", color, "_top_right");
            String bottomLeftID = this.lazyColoredID("block/large_vending_machine/footless/", color, "_bottom_left");
            String bottomRightID = this.lazyColoredID("block/large_vending_machine/footless/", color, "_bottom_right");
            String itemID = this.lazyColoredID("block/large_vending_machine/footless/", color, "_item");
            //Generate the models
            this.models().getBuilder(topLeftID).parent(this.lazyBlockModel("large_vending_machine/footless/base_top_left", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(topRightID).parent(this.lazyBlockModel("large_vending_machine/footless/base_top_right", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(bottomLeftID).parent(this.lazyBlockModel("large_vending_machine/footless/base_bottom_left", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(bottomRightID).parent(this.lazyBlockModel("large_vending_machine/footless/base_bottom_right", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
            this.models().getBuilder(itemID).parent(this.lazyBlockModel("large_vending_machine/footless/base_item", true))
                    .texture("exterior", exteriorTexture)
                    .texture("interior", interiorTexture);
        }

    }

    //ITEM MODEL REGISTRATION
    private void registerBasicItem(Supplier<? extends ItemLike> item) { this.itemModels().basicItem(item.get().asItem()); }
    private void registerHandheldItem(Supplier<? extends ItemLike> item) { this.itemModels().handheldItem(item.get().asItem()); }
    private void registerLayeredItem(Supplier<? extends ItemLike> item) {
        ResourceLocation itemID = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.get().asItem()));
        this.itemModels().basicItem(itemID).texture("layer1", VersionUtil.modResource(itemID.getNamespace(), "item/" + itemID.getPath() + "_overlay"));
    }

    private void registerUpgradeItem(@Nonnull Supplier<? extends ItemLike> item, @Nonnull ResourceLocation base, @Nonnull ResourceLocation tier)
    {
        ResourceLocation itemID = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.get().asItem()));
        this.itemModels().getBuilder(itemID.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0",base)
                .texture("layer1",tier);
    }

    private void registerWalletItem(@Nonnull Supplier<? extends ItemLike> item)
    {
        this.registerBasicItem(item);
        ResourceLocation hipModel = item.get().asItem().components().get(ModDataComponents.WALLET_MODEL.get());
        if(hipModel != null)
        {
            ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(item.get().asItem());
            this.itemModels().getBuilder(hipModel.toString())
                    .parent(new ModelFile.ExistingModelFile(WALLET_MODEL_BASE,this.models().existingFileHelper))
                    .texture("main",itemID.withPrefix("item/wallet_hip/"));
        }
    }

    private void registerBlockItemModel(Supplier<? extends Block> block, String itemModel, boolean check) { this.registerBlockItemModel(block, this.lazyBlockModel(itemModel, check)); }
    private void registerBlockItemModel(Supplier<? extends Block> block, ModelFile itemModel) { this.itemModels().getBuilder(BuiltInRegistries.ITEM.getKey(block.get().asItem()).toString()).parent(itemModel); }

    //BLOCK STATE REGISTRATION
    private void registerSimpleState(Supplier<? extends Block> block) { this.registerSimpleState(block, this.lazyBlockID(block)); }
    private void registerSimpleState(Supplier<? extends Block> block, String modelID) {
        ModelFile model = this.lazyBlockModel(modelID, false);
        this.buildState(block,state -> ConfiguredModel.builder().modelFile(model).build());
        this.registerBlockItemModel(block, model);
    }

    private void registerCoinPile(Supplier<? extends Block> block)
    {
        String modelID = this.lazyBlockID(block);
        ResourceLocation texture = BuiltInRegistries.BLOCK.getKey(block.get()).withPrefix("block/");
        this.models().getBuilder(modelID).parent(this.lazyBlockModel("coin_pile", true)).texture("main", texture);
        ModelFile model = this.lazyBlockModel(modelID, false);
        this.buildState(block,state ->
                ConfiguredModel.builder().modelFile(model).rotationY(this.getRotationY(state)).build());
        this.registerBasicItem(block);
    }
    private void registerCoinBlock(Supplier<? extends Block> block)
    {
        String modelID = this.lazyBlockID(block);
        ResourceLocation texture = BuiltInRegistries.BLOCK.getKey(block.get()).withPrefix("block/");
        this.models().getBuilder(modelID).parent(this.lazyBlockModel("coin_block", true)).texture("main", texture);
        this.registerSimpleState(block, modelID);
    }
    private void registerAncientCoin(Supplier<? extends Item> item)
    {
        ResourceLocation itemModel = lazyItemModelID(lazyItemID(item));
        ItemModelBuilder builder = this.itemModels().getBuilder(itemModel.toString());
        for(AncientCoinType type : AncientCoinType.values())
        {
            ResourceLocation location = type.texture();
            this.itemModels().getBuilder(location.toString())
                    .parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0",location);
            builder.override()
                    .predicate(AncientCoinItem.PROPERTY,type.ordinal() + 1f)
                    .model(new ModelFile.UncheckedModelFile(location));
        }

    }
    private void registerPaygate(Supplier<? extends Block> block, String poweredModelID, String unpoweredModelID)
    {
        ModelFile powered = this.lazyBlockModel(poweredModelID, true);
        ModelFile unpowered = this.lazyBlockModel(unpoweredModelID, true);
        this.buildState(block,state ->
                ConfiguredModel.builder().modelFile(state.getValue(PaygateBlock.POWER_LEVEL) > 0 ? powered : unpowered).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, powered);
    }
    private void registerMoneyBag(Supplier<? extends Block> block, String folder)
    {
        //Block Models
        List<ModelFile> models = new ArrayList<>();
        for(int i = 0; i <= 3; ++i)
            models.add(this.lazyBlockModel(folder + "/" + i,true));
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(models.get(state.getValue(MoneyBagBlock.SIZE))).rotationY(this.getRotationY(state)).build());
        //Item Models
        ResourceLocation itemModel = this.lazyItemModelID(lazyItemID(block));
        ItemModelBuilder builder = this.itemModels().getBuilder(itemModel.toString());
        for(int i = 0; i <= 3; ++i)
        {
            builder.override()
                    .predicate(MoneyBagItem.PROPERTY,i)
                    .model(models.get(i));
        }
    }
    private void registerRotatable(Supplier<? extends Block> block) { this.registerRotatable(block, this.lazyBlockID(block), true); }
    private void registerRotatable(Supplier<? extends Block> block, String modelID, boolean check)
    {
        ModelFile model = this.lazyBlockModel(modelID, check);
        this.buildState(block,state ->
                ConfiguredModel.builder().modelFile(model).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, model);
    }

    private void registerTallRotatable(Supplier<? extends Block> block, String topModelID, String bottomModelID, String itemModelID, boolean check) {
        ModelFile top = this.lazyBlockModel(topModelID, check);
        ModelFile bottom = this.lazyBlockModel(bottomModelID, check);
        this.buildState(block,state ->
                ConfiguredModel.builder().modelFile(this.getTopBottomModel(state, top, bottom)).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, itemModelID, check);
    }

    private void registerTallWideRotatable(Supplier<? extends Block> block, String topLeftModelID, String topRightModelID, String bottomLeftModelID, String bottomRightModelID, String itemModelID, boolean check) {
        ModelFile topLeft = this.lazyBlockModel(topLeftModelID, check);
        ModelFile topRight = this.lazyBlockModel(topRightModelID, check);
        ModelFile bottomLeft = this.lazyBlockModel(bottomLeftModelID, check);
        ModelFile bottomRight = this.lazyBlockModel(bottomRightModelID, check);
        this.buildState(block,state ->
                ConfiguredModel.builder().modelFile(this.getTopBottomLeftRightModel(state, topLeft, topRight, bottomLeft, bottomRight)).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, itemModelID, check);
    }

    private String lazyColoredID(String prefix, Color color) { return this.lazyColoredID(prefix, color, ""); }
    private String lazyColoredID(String prefix, Color color, String postFix) { return prefix + color.getResourceSafeName() + postFix; }
    private String lazyWoodenID(String prefix, WoodType type) { return type.generateResourceLocation(prefix); }
    private String lazyWoodenID(String prefix, WoodType type, String postFix) { return type.generateResourceLocation(prefix, postFix); }

    private String lazyItemID(Supplier<? extends ItemLike> item) { return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath(); }
    private String lazyBlockID(Supplier<? extends Block> block) { return BuiltInRegistries.BLOCK.getKey(block.get()).getPath(); }

    private ResourceLocation lazyItemModelID(String modelID) { return VersionUtil.lcResource(modelID.startsWith("item/") ? modelID : "item/" + modelID); }
    private ResourceLocation lazyBlockModelID(String modelID) { return VersionUtil.lcResource(modelID.startsWith("block/") ? modelID : "block/" + modelID); }

    private ModelFile lazyBlockModel(String modelID, boolean check) { return check ? new ModelFile.ExistingModelFile(this.lazyBlockModelID(modelID), this.models().existingFileHelper) : new ModelFile.UncheckedModelFile(this.lazyBlockModelID(modelID)); }

    private int getRotationY(BlockState state)
    {
        if(state.getBlock() instanceof IRotatableBlock rb)
            return rb.getRotationY(state);
        return 0;
    }

    private ModelFile getTopBottomModel(BlockState state, ModelFile top, ModelFile bottom) {
        return state.getValue(ITallBlock.ISBOTTOM) ? bottom : top;
    }

    private ModelFile getTopBottomLeftRightModel(BlockState state, ModelFile topLeft, ModelFile topRight, ModelFile bottomLeft, ModelFile bottomRight) {
        if(state.getValue(ITallBlock.ISBOTTOM))
            return state.getValue(IWideBlock.ISLEFT) ? bottomLeft : bottomRight;
        return state.getValue(IWideBlock.ISLEFT) ? topLeft : topRight;
    }

    protected final void buildState(Supplier<? extends Block> block, Function<BlockState,ConfiguredModel[]> mapper) { this.buildState(block.get(),mapper); }
    protected final void buildState(Block block, Function<BlockState,ConfiguredModel[]> mapper)
    {
        VariantBlockStateBuilder builder = this.getVariantBuilder(block);
        if(block instanceof IVariantBlock) //Ignore the variant property when building the block states. It's only purpose is to forcibly make the block transparent
            builder.forAllStatesExcept(mapper,IVariantBlock.VARIANT);
        else
            builder.forAllStates(mapper);
    }

}
