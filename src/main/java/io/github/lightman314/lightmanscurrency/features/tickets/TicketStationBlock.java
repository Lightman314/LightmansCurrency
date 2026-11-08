package io.github.lightman314.lightmanscurrency.features.tickets;

import io.github.lightman314.lightmanscurrency.api.world.block.RotatableBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.ShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TicketStationBlock extends RotatableBlock implements EntityBlock {

    private static final VoxelShape EDGE_NORTH = box(0d,8d,15d,16d,14d,16d);
    private static final VoxelShape EDGE_WEST = box(15d,8d,0d,16d,14d,16d);
    private static final VoxelShape EDGE_SOUTH = box(0d,8d,0d,16d,14d,1d);
    private static final VoxelShape EDGE_EAST = box(0d,8d,0d,1d,14d,16d);

    private static final Function<Direction,VoxelShape> DEFAULT_SHAPE = ShapeHelper.directionalShapeAddition(ShapeHelper.SLAB,EDGE_NORTH,EDGE_EAST,EDGE_SOUTH,EDGE_WEST);

    public TicketStationBlock(Properties properties) { this(properties,DEFAULT_SHAPE); }
    public TicketStationBlock(Properties properties,VoxelShape shape) { super(properties,shape); }
    public TicketStationBlock(Properties properties,Function<Direction, VoxelShape> shape) { super(properties, shape); }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) { return new TicketStationBlockEntity(worldPosition,blockState); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof TicketStationBlockEntity be)
            player.openMenu(be);
        return InteractionResult.SUCCESS;
    }

}