package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.blockentity.TicketMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.menus.TicketMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class TicketMachineBlock extends RotatableBlock implements EntityBlock{

	private static final MutableComponent TITLE = EasyText.translatable("gui.lightmanscurrency.ticket_machine.title");

	public TicketMachineBlock(Properties properties) { super(properties, Block.box(0d,0d,0d,16d,14d,16d)); }

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new TicketMachineBlockEntity(pos, state); }

	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result)
	{
		if(!level.isClientSide)
			NetworkHooks.openGui((ServerPlayer)player, this.getMenuProvider(state, level, pos), pos);
		return InteractionResult.SUCCESS;
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos)
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> new TicketMachineMenu(windowId, playerInventory, (TicketMachineBlockEntity)world.getBlockEntity(pos)), TITLE);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.TICKET_MACHINE);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

}