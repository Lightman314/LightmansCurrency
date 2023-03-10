package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.TallRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import io.github.lightman314.lightmanscurrency.common.menus.ATMMenu;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ATMBlock extends TallRotatableBlock{
	
	public ATMBlock(Properties properties)
	{
		super(properties);
	}
	
	@Nonnull
	@Override
	public ActionResultType use(BlockState state, @Nonnull World level, @Nonnull BlockPos pos, PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
	{
		player.openMenu(state.getMenuProvider(level, pos));
		return ActionResultType.SUCCESS;
	}
	
	@Nullable
	@Override
	public INamedContainerProvider getMenuProvider(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos)
	{
		return new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) -> new ATMMenu(windowId, playerInventory), EasyText.empty());
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.ATM);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
	
}
