package io.github.lightman314.lightmanscurrency.api.variants.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantBlockWrapper implements IVariantBlock {

    private final Supplier<Block> block;
    public Block getBlock() { return this.block.get(); }
    protected VariantBlockWrapper(Supplier<Block> block) { this.block = block; }
    protected VariantBlockWrapper(Block block) { this(() -> block); }

    @Override
    public final ResourceLocation getBlockID() { return BuiltInRegistries.BLOCK.getKey(this.getBlock()); }
    @Override
    public final ResourceLocation getItemID() { return BuiltInRegistries.ITEM.getKey(this.getBlock().asItem()); }

}
