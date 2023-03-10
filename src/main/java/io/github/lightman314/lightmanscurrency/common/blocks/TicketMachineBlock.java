package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.blockentity.TicketMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.menus.TicketMachineMenu;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class TicketMachineBlock extends RotatableBlock {

	private static final IFormattableTextComponent TITLE = EasyText.translatable("gui.lightmanscurrency.ticket_machine.title");

	public TicketMachineBlock(Properties properties) { super(properties, box(0d,0d,0d,16d,14d,16d)); }

	@Override
	public boolean hasTileEntity(BlockState state) { return true; }

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader level) { return new TicketMachineBlockEntity(); }

	@Override
	public @Nonnull ActionResultType use(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
	{
		if(!level.isClientSide)
			NetworkHooks.openGui((ServerPlayerEntity) player, this.getMenuProvider(state, level, pos), pos);
		return ActionResultType.SUCCESS;
	}

	@Nullable
	@Override
	public INamedContainerProvider getMenuProvider(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos)
	{
		return new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) -> new TicketMachineMenu(windowId, playerInventory, (TicketMachineBlockEntity)world.getBlockEntity(pos)), TITLE);
	}

	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.TICKET_MACHINE);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}

}