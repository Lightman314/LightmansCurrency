package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.TicketStationBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

public class TicketStationBlock extends RotatableBlock implements EntityBlock, IVariantBlock {

	public TicketStationBlock(Properties properties) { super(properties, Block.box(0d,0d,0d,16d,14d,16d)); }
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new TicketStationBlockEntity(pos, state); }
	
	@Override
	@Nonnull
	@SuppressWarnings("deprecation")
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
			NetworkHooks.openScreen((ServerPlayer)player, this.getMenuProvider(state, level, pos), pos);
		return InteractionResult.SUCCESS;
	}
	
	@Nullable
	@Override
	@SuppressWarnings("deprecation")
	public MenuProvider getMenuProvider(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos)
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> new TicketStationMenu(windowId, playerInventory, (TicketStationBlockEntity)world.getBlockEntity(pos)), LCText.GUI_TICKET_STATION_TITLE.get());
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_TICKET_STATION.asTooltip());
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}
