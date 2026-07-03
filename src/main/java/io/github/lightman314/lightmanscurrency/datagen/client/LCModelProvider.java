package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IColoredBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
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

        //Coin Blocks
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_COPPER);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_IRON);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_GOLD);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_EMERALD);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_DIAMOND);
        this.generateCoinBlock(blockModels,LCBlocks.COIN_BLOCK_NETHERITE);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_COPPER);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_IRON);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_GOLD);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_EMERALD);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_DIAMOND);
        this.generateCoinPile(blockModels,itemModels,LCBlocks.COIN_PILE_NETHERITE);

        //Display Case Models
        this.generateColoredBlock(blockModels,itemModels,LCBlocks.DISPLAY_CASE,LCTexturedModels.DISPLAY_CASE);


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

    protected final void simpleBlock(BlockModelGenerators blockModels, Supplier<? extends Block> block)
    {
        Block b = block.get();
        Variant variant = new Variant(BuiltInRegistries.BLOCK.getKey(b));
        this.simpleBlock(blockModels,b,variant);
    }

    protected final void simpleBlock(BlockModelGenerators blockModels,Block block,Variant variant)
    {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,BlockModelGenerators.variant(variant)));
    }

    protected final void simpleBlockWithItem(BlockModelGenerators blockModels,ItemModelGenerators itemModels,Block block,Variant variant)
    {
        this.simpleBlock(blockModels,block,variant);
        itemModels.itemModelOutput.accept(block.asItem(),ItemModelUtils.plainModel(variant.modelLocation()));
    }

    protected final <T extends Block & IRotatableBlock> void rotatableBlock(BlockModelGenerators blockModels,Supplier<T> block)
    {
        T b = block.get();
        Variant variant = new Variant(this.getBlockID(b));
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

    protected final <T extends Block & IColoredBlock> void generateColoredBlock(BlockModelGenerators blockModels,ItemModelGenerators itemModels,DeferredHolderBundle<DyeColor,Block,T> block, TexturedModel.Provider model)
    {
        for(DyeColor color : DyeColor.values())
        {
            T b = block.get(color);
            Variant variant = new Variant(model.create(b,blockModels.modelOutput));
            this.simpleBlockWithItem(blockModels,itemModels,b,variant);
        }
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

    protected final Identifier getItemID(ItemLike block) { return BuiltInRegistries.ITEM.getKey(block.asItem()); }
}
