package io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.templates;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
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
import org.jetbrains.annotations.NotNull;

public abstract class TraderBlockRotatable extends TraderBlockBase implements IRotatableBlock{

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private final Function<Direction,VoxelShape> shape;
	
	protected TraderBlockRotatable(Properties properties) { this(properties, LazyShapes.BOX_T); }
	
	protected TraderBlockRotatable(Properties properties, VoxelShape shape) { this(properties, LazyShapes.lazySingleShape(shape)); }
	
	protected TraderBlockRotatable(Properties properties, Function<Direction,VoxelShape> shape)
	{
		super(properties);
		this.shape = shape;
	}
	
	@Override
	public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection());
	}
	
	@Override
	public @NotNull BlockState rotate(BlockState state, Rotation rotation)
	{
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}
	
	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}
	
	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
	{
		return this.shape.apply(this.getFacing(state));
	}
	
	@Override
	public Direction getFacing(BlockState state)
	{
		return state.getValue(FACING);
	}
	
	@Override
	protected void onInvalidRemoval(BlockState state, Level level, BlockPos pos, TraderData trader) { }
	
	
}