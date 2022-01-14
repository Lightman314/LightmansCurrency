package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
	
}
