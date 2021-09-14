package io.github.lightman314.lightmanscurrency.blocks;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.containers.TicketMachineContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class TicketMachineBlock extends RotatableBlock{

	private static final VoxelShape SHAPE_NORTH = makeCuboidShape(4d,0d,0d,12d,16d,8d);
	private static final VoxelShape SHAPE_SOUTH = makeCuboidShape(4d,0d,8d,12d,16d,16d);
	private static final VoxelShape SHAPE_EAST = makeCuboidShape(8d,0d,4d,16d,16d,12d);
	private static final VoxelShape SHAPE_WEST = makeCuboidShape(0d,0d,4d,8d,16d,12d);
	
	private static final TranslationTextComponent TITLE = new TranslationTextComponent("gui.lightmanscurrency.ticket_machine.title");
	
	public TicketMachineBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		playerEntity.openContainer(state.getContainer(world, pos));
		return ActionResultType.SUCCESS;
	}
	
	@Nullable
	@Override
	public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos)
	{
		return new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) -> { return new TicketMachineContainer(windowId, playerInventory, IWorldPosCallable.of(world,pos));}, TITLE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext contect)
	{
		if(state.get(FACING) == Direction.NORTH)
			return SHAPE_NORTH;
		else if(state.get(FACING) == Direction.SOUTH)
			return SHAPE_SOUTH;
		else if(state.get(FACING) == Direction.EAST)
			return SHAPE_EAST;
		return SHAPE_WEST;
	}
	
}
