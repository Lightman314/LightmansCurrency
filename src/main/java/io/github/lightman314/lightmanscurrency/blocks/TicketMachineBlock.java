package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.TicketMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.blocks.util.LazyShapes;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.menus.TicketMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public class TicketMachineBlock extends RotatableBlock implements EntityBlock{

	private static final VoxelShape SHAPE_NORTH = box(4d,0d,0d,12d,16d,8d);
	private static final VoxelShape SHAPE_SOUTH = box(4d,0d,8d,12d,16d,16d);
	private static final VoxelShape SHAPE_EAST = box(8d,0d,4d,16d,16d,12d);
	private static final VoxelShape SHAPE_WEST = box(0d,0d,4d,8d,16d,12d);
	
	private static final TranslatableComponent TITLE = new TranslatableComponent("gui.lightmanscurrency.ticket_machine.title");
	
	public TicketMachineBlock(Properties properties)
	{
		super(properties, LazyShapes.lazyDirectionalShape(SHAPE_NORTH, SHAPE_EAST, SHAPE_SOUTH, SHAPE_WEST));
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new TicketMachineBlockEntity(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
			NetworkHooks.openGui((ServerPlayer)player, this.getMenuProvider(state, level, pos), pos);
		return InteractionResult.SUCCESS;
	}
	
	@Nullable
	@Override
	public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos)
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> { return new TicketMachineMenu(windowId, playerInventory, (TicketMachineBlockEntity)world.getBlockEntity(pos));}, TITLE);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.TICKET_MACHINE);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}
