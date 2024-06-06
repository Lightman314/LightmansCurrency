package io.github.lightman314.lightmanscurrency.api.traders.blocks;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public abstract class TraderBlockRotatable extends TraderBlockBase implements IRotatableBlock{

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private final Function<Direction,VoxelShape> shape;
	
	protected TraderBlockRotatable(Properties properties) { this(properties, LazyShapes.BOX); }
	
	protected TraderBlockRotatable(Properties properties, VoxelShape shape) { this(properties, LazyShapes.lazySingleShape(shape)); }
	
	protected TraderBlockRotatable(Properties properties, Function<Direction,VoxelShape> shape)
	{
		super(properties);
		this.shape = shape;
	}
	
	@Override
	public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
	
	@Override
	protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}
	
	@Override
	public @Nonnull VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context)
	{
		return this.shape.apply(this.getFacing(state));
	}
	
	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderData trader) { }
	
	
}
