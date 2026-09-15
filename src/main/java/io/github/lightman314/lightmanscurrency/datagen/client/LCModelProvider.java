package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.VanillaColor;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.*;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.datagen.client.models.LCModelTemplates;
import io.github.lightman314.lightmanscurrency.datagen.client.models.LCTextureSlots;
import io.github.lightman314.lightmanscurrency.datagen.client.models.LCTexturedModels;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class LCModelProvider extends ModelProvider {

    public LCModelProvider(PackOutput output) { super(output,LCApi.MODID); }

    protected static final Identifier UPGRADE_TIER_COPPER = LCApi.id("item/upgrade_tier/copper");
    protected static final Identifier UPGRADE_TIER_IRON = LCApi.id("item/upgrade_tier/iron");
    protected static final Identifier UPGRADE_TIER_GOLD = LCApi.id("item/upgrade_tier/gold");
    protected static final Identifier UPGRADE_TIER_EMERALD = LCApi.id("item/upgrade_tier/emerald");
    protected static final Identifier UPGRADE_TIER_DIAMOND = LCApi.id("item/upgrade_tier/diamond");
    protected static final Identifier UPGRADE_TIER_NETHERITE = LCApi.id("item/upgrade_tier/netherite");

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() { return LCBlocks.REGISTER.getEntries().stream(); }
    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() { return LCItems.REGISTER.getEntries().stream(); }

    @Override
    protected void registerModels(BlockModelGenerators blockModels,ItemModelGenerators itemModels) {

        //Coin Items
        itemModels.generateFlatItem(LCItems.COIN_COPPER.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_IRON.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_GOLD.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_EMERALD.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_DIAMOND.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_NETHERITE.get(),ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(LCItems.COIN_CHOCOLATE_COPPER.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_CHOCOLATE_IRON.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_CHOCOLATE_GOLD.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_CHOCOLATE_EMERALD.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_CHOCOLATE_DIAMOND.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.COIN_CHOCOLATE_NETHERITE.get(),ModelTemplates.FLAT_ITEM);

        //Wallet Items
        this.simpleWallet(itemModels,LCItems.WALLET_COPPER);
        this.simpleWallet(itemModels,LCItems.WALLET_IRON);
        this.simpleWallet(itemModels,LCItems.WALLET_GOLD);
        this.simpleWallet(itemModels,LCItems.WALLET_EMERALD);
        this.simpleWallet(itemModels,LCItems.WALLET_DIAMOND);
        this.simpleWallet(itemModels,LCItems.WALLET_NETHERITE);
        this.simpleWallet(itemModels,LCItems.WALLET_NETHER_STAR);
        this.simpleWallet(itemModels,LCItems.WALLET_ENDER_DRAGON,false);

        itemModels.generateFlatItem(LCItems.TRADING_CORE.get(),ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(LCItems.UPGRADE_SMITHING_TEMPLATE.get(),ModelTemplates.FLAT_ITEM);

        Identifier baseTexture = LCApi.id("item/item_capacity_upgrade");
        //Upgrade Items
        this.dualLayeredItem(itemModels,LCItems.ITEM_CAPACITY_UPGRADE_1,baseTexture,UPGRADE_TIER_IRON);
        this.dualLayeredItem(itemModels,LCItems.ITEM_CAPACITY_UPGRADE_2,baseTexture,UPGRADE_TIER_GOLD);
        this.dualLayeredItem(itemModels,LCItems.ITEM_CAPACITY_UPGRADE_3,baseTexture,UPGRADE_TIER_DIAMOND);
        this.dualLayeredItem(itemModels,LCItems.ITEM_CAPACITY_UPGRADE_4,baseTexture,UPGRADE_TIER_NETHERITE);
        this.dualLayeredItem(itemModels,LCItems.NETWORK_UPGRADE,Identifier.withDefaultNamespace("item/ender_eye"),UPGRADE_TIER_GOLD);

        //Coin Blocks
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_COPPER);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_IRON);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_GOLD);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_EMERALD);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_DIAMOND);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_NETHERITE);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_COPPER);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_IRON);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_GOLD);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_EMERALD);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_DIAMOND);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_NETHERITE);

        //Chocolate Coin Blocks
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_CHOCOLATE_COPPER);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_CHOCOLATE_IRON);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_CHOCOLATE_GOLD);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_CHOCOLATE_EMERALD);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_CHOCOLATE_DIAMOND);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_CHOCOLATE_NETHERITE);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_CHOCOLATE_COPPER);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_CHOCOLATE_IRON);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_CHOCOLATE_GOLD);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_CHOCOLATE_EMERALD);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_CHOCOLATE_DIAMOND);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_CHOCOLATE_NETHERITE);

        //Coin Mint
        this.rotatableBlockWithItem(blockModels,itemModels,LCBlocks.COIN_MINT);
        //ATM
        this.tallRotatableBlockWithItem(blockModels,itemModels,LCBlocks.ATM);
        itemModels.itemModelOutput.accept(LCItems.ATM_PORTABLE.get(),ItemModelUtils.plainModel(this.getItemModelID(LCItems.ATM_PORTABLE)));


        //Display Case Models
        this.generateColoredBlock(blockModels,itemModels,LCBlocks.DISPLAY_CASE,LCTexturedModels.DISPLAY_CASE);

        //Card Display Models
        this.generateRotatableWoodenAndColoredBlock(blockModels,itemModels,LCBlocks.CARD_DISPLAY,LCTexturedModels.CARD_DISPLAY);


    }

    protected final void simpleWallet(ItemModelGenerators itemModels,Supplier<? extends WalletItem> wallet) { this.simpleWallet(itemModels,wallet,true); }
    protected final void simpleWallet(ItemModelGenerators itemModels,Supplier<? extends WalletItem> wallet,boolean buildModel) {
        Identifier walletID = this.getItemID(wallet.get());
        //Assume a default model location since components are not bound during datagen
        Identifier hipModel = WalletItem.model(walletID);
        if(buildModel) //Build a basic hip model
            LCModelTemplates.WALLET_HIP.create(hipModel.withPrefix("item/"),TextureMapping.singleSlot(LCTextureSlots.MAIN,new Material(walletID.withPrefix("item/wallet_hip/"))),itemModels.modelOutput);
        itemModels.generateFlatItem(wallet.get(),ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.register(hipModel,new ClientItem(ItemModelUtils.plainModel(hipModel.withPrefix("item/")),ClientItem.Properties.DEFAULT));
    }

    protected final void dualLayeredItem(ItemModelGenerators itemModels,Supplier<? extends ItemLike> item,Identifier layer0,Identifier layer1) {
        Identifier itemID = this.getItemID(item.get());
        Identifier model = itemID.withPrefix("item/");
        ModelTemplates.TWO_LAYERED_ITEM.create(model,TextureMapping.layer0(new Material(layer0)).put(TextureSlot.LAYER1,new Material(layer1)),itemModels.modelOutput);
        itemModels.itemModelOutput.register(item.get().asItem(),new ClientItem(ItemModelUtils.plainModel(model),ClientItem.Properties.DEFAULT));
    }

    protected final void simpleBlock(BlockModelGenerators blockModels, Supplier<? extends Block> block)
    {
        Block b = block.get();
        Variant variant = new Variant(this.getBlockModelID(b));
        this.simpleBlock(blockModels,b,variant);
    }

    protected final void simpleBlock(BlockModelGenerators blockModels,Block block,Variant variant)
    {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,BlockModelGenerators.variant(variant)));
    }

    protected final void simpleBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,Block block,Variant variant)
    {
        this.simpleBlock(blockModels,block,variant);
        this.simpleBlockItem(itemModels,block,variant);
    }

    protected final void simpleBlockItem(ItemModelGenerators itemModels,Block block,Variant variant) {
        itemModels.itemModelOutput.accept(block.asItem(),ItemModelUtils.plainModel(variant.modelLocation()));
    }

    protected final <T extends Block & IRotatableBlock> void rotatableBlock(BlockModelGenerators blockModels,Supplier<T> block)
    {
        T b = block.get();
        Variant variant = new Variant(this.getBlockModelID(b));
        this.rotatableBlock(blockModels,b,variant);
    }

    protected final <T extends Block & IRotatableBlock> void rotatableBlock(BlockModelGenerators blockModels,T b,Variant variant)
    {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b,BlockModelGenerators.variant(variant))
                .with(PropertyDispatch.modify(IRotatableBlock.FACING)
                        .select(Direction.NORTH,this.getMutator(b,Direction.NORTH))
                        .select(Direction.SOUTH,this.getMutator(b,Direction.SOUTH))
                        .select(Direction.EAST,this.getMutator(b,Direction.EAST))
                        .select(Direction.WEST,this.getMutator(b,Direction.WEST))
                ));
    }

    protected final <T extends Block & IRotatableBlock> void rotatableBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,Supplier<T> block) {
        this.rotatableBlockWithItem(blockModels,itemModels,block.get());
    }
    protected final <T extends Block & IRotatableBlock> void rotatableBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,T block) {
        this.rotatableBlockWithItem(blockModels,itemModels,block,new Variant(this.getBlockModelID(block)));
    }
    protected final <T extends Block & IRotatableBlock> void rotatableBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,Supplier<T> block,Variant variant) {
        this.rotatableBlockWithItem(blockModels,itemModels,block.get(),variant);
    }
    protected final <T extends Block & IRotatableBlock> void rotatableBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,T block,Variant variant) {
        this.rotatableBlock(blockModels,block,variant);
        this.simpleBlockItem(itemModels,block,variant);
    }

    protected final <T extends Block & IRotatableBlock & ITallBlock> void tallRotatableBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,Supplier<T> block) { this.tallRotatableBlockWithItem(blockModels,itemModels,block.get()); }
    protected final <T extends Block & IRotatableBlock & ITallBlock> void tallRotatableBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,T block) {
        Identifier modelID = this.getBlockModelID(block);
        Variant top = new Variant(modelID.withSuffix("_top"));
        Variant bottom = new Variant(modelID.withSuffix("_bottom"));
        Variant item = new Variant(modelID);
        this.tallRotatableBlockWithItem(blockModels,itemModels,block,top,bottom,item);
    }
    protected final <T extends Block & IRotatableBlock & ITallBlock> void tallRotatableBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,Supplier<T> block,Variant top,Variant bottom,Variant item) { this.tallRotatableBlockWithItem(blockModels,itemModels,block.get(),top,bottom,item); }
    protected final <T extends Block & IRotatableBlock & ITallBlock> void tallRotatableBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,T block,Variant top,Variant bottom,Variant item) {
        this.tallRotatableBlock(blockModels,block,top,bottom);
        this.simpleBlockItem(itemModels,block,item);
    }

    protected final <T extends Block & IRotatableBlock & ITallBlock> void tallRotatableBlock(BlockModelGenerators blockModels,Supplier<T> block,Variant top,Variant bottom) { this.tallRotatableBlock(blockModels,block.get(),top,bottom); }
    protected final <T extends Block & IRotatableBlock & ITallBlock> void tallRotatableBlock(BlockModelGenerators blockModels,T block,Variant top,Variant bottom) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(ITallBlock.ISBOTTOM)
                        .select(true,BlockModelGenerators.variant(bottom))
                        .select(false,BlockModelGenerators.variant(top)))
                .with(PropertyDispatch.modify(IRotatableBlock.FACING)
                        .select(Direction.NORTH,this.getMutator(block,Direction.NORTH))
                        .select(Direction.SOUTH,this.getMutator(block,Direction.SOUTH))
                        .select(Direction.EAST,this.getMutator(block,Direction.EAST))
                        .select(Direction.WEST,this.getMutator(block,Direction.WEST))
                ));
    }

    protected void generateCoinBlock(BlockModelGenerators blockModels,Supplier<? extends Block> block)
    {
        Block b = block.get();
        Variant variant = new Variant(LCTexturedModels.COIN_BLOCK.create(b,blockModels.modelOutput));
        this.simpleBlock(blockModels,b,variant);
    }

    protected final <T extends Block & IRotatableBlock> void generateCoinPile(BlockModelGenerators blockModels,ItemModelGenerators itemModels,Supplier<T> block)
    {
        T b = block.get();
        Variant variant = new Variant(LCTexturedModels.COIN_PILE.create(b,blockModels.modelOutput));
        this.rotatableBlock(blockModels,b,variant);
        itemModels.generateFlatItem(b.asItem(),ModelTemplates.FLAT_ITEM);
    }

    protected final <T extends Block & IColoredBlock> void generateColoredBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, DeferredHolderBundle<VanillaColor,Block,T> block, TexturedModel.Provider model)
    {
        for(VanillaColor color : VanillaColor.values())
        {
            T b = block.get(color);
            Variant variant = new Variant(model.create(b,blockModels.modelOutput));
            this.simpleBlockWithItem(blockModels,itemModels,b,variant);
        }
    }

    protected final <T extends Block & IColoredBlock & IWoodenBlock> void generateWoodenAndColoredBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, DeferredHolderBundle2<WoodType,DyeColor,Block,T> block, TexturedModel.Provider model) {
        block.forEachKey1(type -> {
            for(DyeColor color : DyeColor.values()) {
                T b = block.get(type,color);
                Variant variant = new Variant(model.create(b,blockModels.modelOutput));
                this.simpleBlockWithItem(blockModels,itemModels,b,variant);
            }
        });
    }

    protected final <T extends Block & IColoredBlock & IWoodenBlock & IRotatableBlock> void generateRotatableWoodenAndColoredBlock(BlockModelGenerators blockModels, ItemModelGenerators itemModels, DeferredHolderBundle2<WoodType,VanillaColor,Block,T> block, TexturedModel.Provider model) {
        block.forEachKey1(type -> {
            for(VanillaColor color : VanillaColor.values()) {
                T b = block.get(type,color);
                Variant variant = new Variant(model.create(b,blockModels.modelOutput));
                this.rotatableBlock(blockModels,b,variant);
                this.simpleBlockItem(itemModels,b,variant);
            }
        });
    }

    protected VariantMutator getMutator(IRotatableBlock rb,Direction facing)
    {
        return switch (rb.getRotationY(facing)) {
            case 90 -> BlockModelGenerators.Y_ROT_90;
            case 180 -> BlockModelGenerators.Y_ROT_180;
            case 270 -> BlockModelGenerators.Y_ROT_270;
            default -> BlockModelGenerators.NOP;
        };
    }

    protected final Identifier getBlockID(Block block) { return BuiltInRegistries.BLOCK.getKey(block); }
    protected final Identifier getBlockModelID(Block block) { return this.getBlockID(block).withPrefix("block/"); }

    protected final Identifier getItemID(ItemLike item) { return BuiltInRegistries.ITEM.getKey(item.asItem()); }
    protected final Identifier getItemModelID(ItemLike item) { return this.getItemID(item).withPrefix("item/"); }
}
