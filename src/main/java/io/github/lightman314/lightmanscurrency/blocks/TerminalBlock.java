package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;

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
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		if(world.isRemote)
		{
			LightmansCurrency.PROXY.openTerminalScreen(playerEntity);
		}
		return ActionResultType.SUCCESS;
	}
	
}
