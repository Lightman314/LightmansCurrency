package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TerminalBlock extends RotatableBlock{

	public TerminalBlock(Properties properties)
	{
		super(properties);
	}
	
	public TerminalBlock(Properties properties, VoxelShape shape)
	{
		super(properties, shape);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		LightmansCurrency.PROXY.openTerminalScreen();
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.TERMINAL);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
}
