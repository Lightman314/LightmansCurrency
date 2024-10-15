package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.datagen.util.ColorHelper;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

public class LCBlockStateProvider extends BlockStateProvider {

    public LCBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, LightmansCurrency.MODID, exFileHelper);
    }

    private static final ResourceLocation UPGRADE_TIER_COPPER = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/upgrade_tier/copper");
    private static final ResourceLocation UPGRADE_TIER_IRON = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/upgrade_tier/iron");
    private static final ResourceLocation UPGRADE_TIER_GOLD = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/upgrade_tier/gold");
    private static final ResourceLocation UPGRADE_TIER_EMERALD = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/upgrade_tier/emerald");
    private static final ResourceLocation UPGRADE_TIER_DIAMOND = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/upgrade_tier/diamond");
    private static final ResourceLocation UPGRADE_TIER_NETHERITE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/upgrade_tier/netherite");

    private static final ResourceLocation UPGRADE_ITEM_CAPACITY = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/item_capacity_upgrade");
    private static final ResourceLocation UPGRADE_TRADE_OFFER = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/trading_core");
    private static final ResourceLocation UPGRADE_NETWORK = ResourceLocation.withDefaultNamespace("item/ender_eye");
    private static final ResourceLocation UPGRADE_SPEED = ResourceLocation.withDefaultNamespace("item/clock_00");
    private static final ResourceLocation UPGRADE_HOPPER = ResourceLocation.withDefaultNamespace("item/hopper");
    private static final ResourceLocation UPGRADE_CC_SECURITY = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/coin_chest_security_upgrade");
    private static final ResourceLocation UPGRADE_CC_BANK = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/coin_chest_bank_upgrade");
    private static final ResourceLocation UPGRADE_CC_EXCHANGE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"item/coin_chest_exchange_upgrade");
    private static final ResourceLocation UPGRADE_CC_MAGNET = ResourceLocation.withDefaultNamespace("item/ender_pearl");

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

        //Trading Core
        this.registerBasicItem(ModItems.TRADING_CORE);

        //Wallets
        this.registerWalletItem(ModItems.WALLET_COPPER);
        this.registerWalletItem(ModItems.WALLET_IRON);
        this.registerWalletItem(ModItems.WALLET_GOLD);
        this.registerWalletItem(ModItems.WALLET_EMERALD);
        this.registerWalletItem(ModItems.WALLET_DIAMOND);
        this.registerWalletItem(ModItems.WALLET_NETHERITE);
        this.registerWalletItem(ModItems.WALLET_NETHER_STAR);

        //ATM
        this.registerTallRotatable(ModBlocks.ATM, "atm_top", "atm_bottom", "atm", true);
        this.registerBasicItem(ModItems.PORTABLE_ATM);

        //ATM Card
        this.registerLayeredItem(ModItems.ATM_CARD);
        //Prepaid Card
        this.registerLayeredItem(ModItems.PREPAID_CARD);

        //Coin Mint
        this.registerRotatable(ModBlocks.COIN_MINT);

        //Cash Register
        this.registerRotatable(ModBlocks.CASH_REGISTER, "cash_register_modern", true);

        //Display Case
        this.registerSimpleState(ModBlocks.DISPLAY_CASE);

        //Vending Machines
        ModBlocks.VENDING_MACHINE.forEach((color, block) -> {
            //Collect IDs and Textures
            ResourceLocation interiorTexture = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, this.lazyColoredID("block/vending_machine/", color, "_interior"));
            ResourceLocation exteriorTexture = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, this.lazyColoredID("block/vending_machine/", color, "_exterior"));
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
            ResourceLocation interiorTexture = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, this.lazyColoredID("block/large_vending_machine/", color, "_interior"));
            ResourceLocation exteriorTexture = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, this.lazyColoredID("block/large_vending_machine/", color, "_exterior"));
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
            ResourceLocation concreteTexture = ResourceLocation.withDefaultNamespace(this.lazyColoredID("block/", color, "_concrete_powder"));
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
        this.registerTallRotatable(ModBlocks.ARMOR_DISPLAY, "armor_display_top", "armor_display_bottom", "armor_display", true);

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
        this.registerTallRotatableInv(ModBlocks.SLOT_MACHINE, "slot_machine/top", "slot_machine/bottom", "slot_machine/item", true);

        //Paygate
        this.registerPaygate(ModBlocks.PAYGATE, "paygate_powered", "paygate_unpowered");

        //Item Network Traders
        this.registerRotatable(ModBlocks.ITEM_NETWORK_TRADER_1, "item_network_trader_1", true);
        this.registerRotatable(ModBlocks.ITEM_NETWORK_TRADER_2, "item_network_trader_2", true);
        this.registerRotatable(ModBlocks.ITEM_NETWORK_TRADER_3, "item_network_trader_3", true);
        this.registerRotatable(ModBlocks.ITEM_NETWORK_TRADER_4, "item_network_trader_4", true);

        //Item Trader Interface
        this.registerRotatable(ModBlocks.ITEM_TRADER_INTERFACE);

        //Terminal
        this.registerRotatable(ModBlocks.TERMINAL);
        this.registerBasicItem(ModItems.PORTABLE_TERMINAL);

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
        this.getVariantBuilder(ModBlocks.COIN_CHEST.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(new ModelFile.ExistingModelFile(ResourceLocation.withDefaultNamespace("block/chest"), this.models().existingFileHelper)).build());
        this.registerBlockItemModel(ModBlocks.COIN_CHEST, new ModelFile.ExistingModelFile(ResourceLocation.withDefaultNamespace("item/chest"), this.models().existingFileHelper));
        //Coin Jars
        this.registerRotatable(ModBlocks.PIGGY_BANK, "jars/piggy_bank", true);
        this.registerRotatable(ModBlocks.COINJAR_BLUE, "jars/coinjar_blue", true);

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
        this.registerRotatableInv(ModBlocks.SUS_JAR, "jars/sus_jar", true);


    }

    //ITEM MODEL REGISTRATION
    private void registerBasicItem(Supplier<? extends ItemLike> item) { this.itemModels().basicItem(item.get().asItem()); }
    private void registerLayeredItem(Supplier<? extends ItemLike> item) {
        ResourceLocation itemID = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.get().asItem()));
        this.itemModels().basicItem(itemID).texture("layer1", ResourceLocation.fromNamespaceAndPath(itemID.getNamespace(), "item/" + itemID.getPath() + "_overlay"));
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
    private void registerSimpleState(Supplier<? extends Block> block) { this.registerSimpleState(block, this.lazyModelID(block)); }
    private void registerSimpleState(Supplier<? extends Block> block, String modelID) {
        ModelFile model = this.lazyBlockModel(modelID, true);
        this.getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
        this.registerBlockItemModel(block, model);
    }

    private void registerCoinPile(Supplier<? extends Block> block)
    {
        String modelID = this.lazyModelID(block);
        ResourceLocation texture = BuiltInRegistries.BLOCK.getKey(block.get()).withPrefix("block/");
        this.models().getBuilder(modelID).parent(this.lazyBlockModel("coin_pile", true)).texture("main", texture);
        ModelFile model = this.lazyBlockModel(modelID, false);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(model).rotationY(this.getRotationY(state)).build());
        this.registerBasicItem(block);
    }
    private void registerCoinBlock(Supplier<? extends Block> block)
    {
        String modelID = this.lazyModelID(block);
        ResourceLocation texture = BuiltInRegistries.BLOCK.getKey(block.get()).withPrefix("block/");
        this.models().getBuilder(modelID).parent(this.lazyBlockModel("coin_block", true)).texture("main", texture);
        this.registerSimpleState(block, modelID);
    }
    private void registerPaygate(Supplier<? extends Block> block, String poweredModelID, String unpoweredModelID)
    {
        ModelFile powered = this.lazyBlockModel(poweredModelID, true);
        ModelFile unpowered = this.lazyBlockModel(unpoweredModelID, true);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(state.getValue(PaygateBlock.POWERED) ? powered : unpowered).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, powered);
    }
    private void registerRotatable(Supplier<? extends Block> block) { this.registerRotatable(block, this.lazyModelID(block), true); }
    private void registerRotatable(Supplier<? extends Block> block, String modelID, boolean check)
    {
        ModelFile model = this.lazyBlockModel(modelID, check);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(model).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, model);
    }
    private void registerRotatableInv(Supplier<? extends Block> block, String modelID, boolean check)
    {
        ModelFile model = this.lazyBlockModel(modelID, check);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(model).rotationY(this.getRotationYInv(state)).build());
        this.registerBlockItemModel(block, model);
    }

    private void registerTallRotatableInv(Supplier<? extends Block> block, String topModelID, String bottomModelID, String itemModelID, boolean check) {
        ModelFile top = this.lazyBlockModel(topModelID, check);
        ModelFile bottom = this.lazyBlockModel(bottomModelID, check);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(this.getTopBottomModel(state, top, bottom)).rotationY(this.getRotationYInv(state)).build());
        this.registerBlockItemModel(block, itemModelID, check);
    }
    private void registerTallRotatable(Supplier<? extends Block> block, String topModelID, String bottomModelID, String itemModelID, boolean check) {
        ModelFile top = this.lazyBlockModel(topModelID, check);
        ModelFile bottom = this.lazyBlockModel(bottomModelID, check);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(this.getTopBottomModel(state, top, bottom)).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, itemModelID, check);
    }

    private void registerTallWideRotatable(Supplier<? extends Block> block, String topLeftModelID, String topRightModelID, String bottomLeftModelID, String bottomRightModelID, String itemModelID, boolean check) {
        ModelFile topLeft = this.lazyBlockModel(topLeftModelID, check);
        ModelFile topRight = this.lazyBlockModel(topRightModelID, check);
        ModelFile bottomLeft = this.lazyBlockModel(bottomLeftModelID, check);
        ModelFile bottomRight = this.lazyBlockModel(bottomRightModelID, check);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(this.getTopBottomLeftRightModel(state, topLeft, topRight, bottomLeft, bottomRight)).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, itemModelID, check);
    }

    private String lazyColoredID(String prefix, Color color) { return this.lazyColoredID(prefix, color, ""); }
    private String lazyColoredID(String prefix, Color color, String postFix) { return prefix + color.getResourceSafeName() + postFix; }
    private String lazyWoodenID(String prefix, WoodType type) { return type.generateResourceLocation(prefix); }
    private String lazyWoodenID(String prefix, WoodType type, String postFix) { return type.generateResourceLocation(prefix, postFix); }

    private String lazyModelID(Supplier<? extends Block> block) { return BuiltInRegistries.BLOCK.getKey(block.get()).getPath(); }

    private ResourceLocation lazyBlockModelID(String modelID) { return ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, modelID.startsWith("block/") ? modelID : "block/" + modelID); }

    private ModelFile lazyBlockModel(String modelID, boolean check) { return check ? new ModelFile.ExistingModelFile(this.lazyBlockModelID(modelID), this.models().existingFileHelper) : new ModelFile.UncheckedModelFile(this.lazyBlockModelID(modelID)); }

    private int getRotationYInv(BlockState state) {
        return switch (state.getValue(IRotatableBlock.FACING)) {
            case WEST -> 90;
            case NORTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }

    private int getRotationY(BlockState state) {
        return switch (state.getValue(IRotatableBlock.FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private ModelFile getTopBottomModel(BlockState state, ModelFile top, ModelFile bottom) {
        return state.getValue(ITallBlock.ISBOTTOM) ? bottom : top;
    }

    private ModelFile getTopBottomLeftRightModel(BlockState state, ModelFile topLeft, ModelFile topRight, ModelFile bottomLeft, ModelFile bottomRight) {
        if(state.getValue(ITallBlock.ISBOTTOM))
            return state.getValue(IWideBlock.ISLEFT) ? bottomLeft : bottomRight;
        return state.getValue(IWideBlock.ISLEFT) ? topLeft : topRight;
    }

}
