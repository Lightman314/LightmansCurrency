package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.SlotMachineTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.blocks.TraderBlockTallRotatable;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineBlock extends TraderBlockTallRotatable implements IVariantBlock {

    public static final ResourceLocation LIGHT_MODEL_LOCATION = VersionUtil.lcResource( "block/slot_machine/lights");

    public static final VoxelShape SHAPE_SOUTH = Shapes.or(box(0d,14d,-1d, 16d, 16d, 16d), box(0d, 0d, 3d, 16d, 32d, 16d));
    public static final VoxelShape SHAPE_NORTH = Shapes.or(box(0d,14d,0d, 16d, 16d, 17d), box(0d,0d,0d,16d,32d,13d));
    public static final VoxelShape SHAPE_EAST = Shapes.or(box(-1d,14d,0d, 16d, 16d, 16d), box(3d,0d,0d,16d,32d,16d));
    public static final VoxelShape SHAPE_WEST = Shapes.or(box(0d,14d,0d, 17d, 16d, 16d), box(0d,0d,0d,13d,32d,16d));


    public SlotMachineBlock(Properties properties) { super(properties, LazyShapes.lazyTallDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST)); }

    @Override
    public int getRotationY(Direction facing) { return this.getRotationYInv(facing); }

    @Override
    public int requiredModels() { return IVariantBlock.super.requiredModels() + 1; }

    @Override
    protected boolean isBlockOpaque() { return false; }

    @Override
    protected BlockEntity makeTrader(BlockPos pos, BlockState state) { return new SlotMachineTraderBlockEntity(pos, state); }

    @Override
    protected BlockEntityType<?> traderType() { return ModBlockEntities.SLOT_MACHINE_TRADER.get(); }

    @Nullable
    public ResourceLocation getLightModel() { return LIGHT_MODEL_LOCATION; }

    @Override
    protected Supplier<List<Component>> getItemTooltips() { return LCText.TOOLTIP_SLOT_MACHINE.asTooltip(); }

}
