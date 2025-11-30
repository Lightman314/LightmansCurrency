package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.menus.providers.TerminalMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TerminalBlock extends RotatableBlock implements IVariantBlock {

	public TerminalBlock(Properties properties) { super(properties); }
	
	public TerminalBlock(Properties properties, VoxelShape shape) { super(properties, shape); }

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Override
	@Nonnull
	public InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
			TerminalMenuProvider.OpenMenu(player, BlockValidator.of(pos, this));
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_TERMINAL.asTooltip());
		super.appendHoverText(stack, context, tooltip, flagIn);
	}
	
}
