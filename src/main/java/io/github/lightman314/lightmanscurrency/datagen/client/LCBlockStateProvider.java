package io.github.lightman314.lightmanscurrency.datagen.client;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.FileNotFoundException;

public class LCBlockStateProvider extends BlockStateProvider {

    public LCBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, LightmansCurrency.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        try {
            this.registerRotatable(ModBlocks.TAX_BLOCK, "tax_block");
        } catch(FileNotFoundException fnfe) { LightmansCurrency.LogError("Error generating block states!", fnfe); }
    }

    //ITEM MODEL REGISTRATION
    private void registerBasicItem(RegistryObject<? extends ItemLike> item) { this.itemModels().basicItem(item.get().asItem()); }
    private void registerLayeredItem(RegistryObject<? extends ItemLike> item) {
        ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(item.get().asItem());
        this.itemModels().basicItem(itemID).texture("layer1", new ResourceLocation(itemID.getNamespace(), "item/" + itemID.getPath() + "_overlay"));
    }

    private void registerBlockItemModel(RegistryObject<? extends Block> block, String itemModel) { this.registerBlockItemModel(block, this.lazyBlockModel(itemModel)); }
    private void registerBlockItemModel(RegistryObject<? extends Block> block, ModelFile itemModel) { this.itemModels().getBuilder(ForgeRegistries.ITEMS.getKey(block.get().asItem()).toString()).parent(itemModel); }

    //BLOCK STATE REGISTRATION
    private void registerSimpleState(RegistryObject<? extends Block> block, String modelID) throws FileNotFoundException { this.getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(this.lazyBlockModel(modelID)).build()); }

    private <T extends Block & IRotatableBlock> void registerRotatable(RegistryObject<T> block, String modelID)  throws FileNotFoundException
    {
        ModelFile model = this.lazyBlockModel(modelID);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(model).rotationY(this.getRotationY(state)).build());
        this.registerBlockItemModel(block, modelID);
    }

    private <T extends Block & IRotatableBlock & ITallBlock> void registerTallRotatable(RegistryObject<T> block, String topModelID, String bottomModelID, String itemModelID) throws FileNotFoundException {
        ModelFile top = this.lazyBlockModel(topModelID);
        ModelFile bottom = this.lazyBlockModel(bottomModelID);
        this.getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(this.getTopBottomModel(state, top, bottom)).rotationX(this.getRotationY(state)).build());
        this.itemModels().getBuilder(ForgeRegistries.ITEMS.getKey(block.get().asItem()).toString()).parent(this.lazyBlockModel(itemModelID));
    }

    private ResourceLocation lazyBlockModelID(String modelID) { return new ResourceLocation(LightmansCurrency.MODID, "block/" + modelID); }

    private ModelFile lazyBlockModel(String modelID) { return new ModelFile.ExistingModelFile(this.lazyBlockModelID(modelID), this.models().existingFileHelper); }

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

}
