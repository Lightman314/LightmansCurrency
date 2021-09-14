package io.github.lightman314.lightmanscurrency.blocks;

import javax.annotation.Nullable;

import com.google.common.base.Function;

import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.containers.TicketMachineContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TicketMachineBlock extends RotatableBlock{

	private static final VoxelShape SHAPE_NORTH = box(4d,0d,0d,12d,16d,8d);
	private static final VoxelShape SHAPE_SOUTH = box(4d,0d,8d,12d,16d,16d);
	private static final VoxelShape SHAPE_EAST = box(8d,0d,4d,16d,16d,12d);
	private static final VoxelShape SHAPE_WEST = box(0d,0d,4d,8d,16d,12d);
	private static final Function<Direction,VoxelShape> LAZY_SHAPE = LazyShapes.lazyDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST);
	
	private static final TranslatableComponent TITLE = new TranslatableComponent("gui.lightmanscurrency.ticket_machine.title");
	
	public TicketMachineBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		player.openMenu(state.getMenuProvider(level, pos));
		return InteractionResult.SUCCESS;
	}
	
	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos)
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> { return new TicketMachineContainer(windowId, playerInventory);}, TITLE);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return LAZY_SHAPE.apply(state.getValue(FACING));
	}
	
}
