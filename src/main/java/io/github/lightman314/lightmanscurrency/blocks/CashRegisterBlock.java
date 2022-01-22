package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CashRegisterBlock extends RotatableBlock implements EntityBlock{

	public CashRegisterBlock(Properties properties)
	{
		super(properties);
	}
	
	public CashRegisterBlock(Properties properties, VoxelShape shape)
	{
		super(properties,shape);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new CashRegisterBlockEntity(pos, state);
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CashRegisterBlockEntity)
			{
				CashRegisterBlockEntity register = (CashRegisterBlockEntity)blockEntity;
				register.loadDataFromItems(stack.getTag());
			}
		}
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			//Open UI
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CashRegisterBlockEntity)
			{
				CashRegisterBlockEntity register = (CashRegisterBlockEntity)blockEntity;
				BlockEntityUtil.sendUpdatePacket(blockEntity);
				register.OpenContainer(player);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
}
