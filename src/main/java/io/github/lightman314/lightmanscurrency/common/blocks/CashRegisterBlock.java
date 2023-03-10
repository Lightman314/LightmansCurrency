package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class CashRegisterBlock extends RotatableBlock {

	public CashRegisterBlock(Properties properties) { super(properties); }
	
	public CashRegisterBlock(Properties properties, VoxelShape shape)
	{
		super(properties,shape);
	}

	@Override
	public boolean hasTileEntity(BlockState state) { return true; }

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader level) { return new CashRegisterBlockEntity(); }
	
	@Override
	public void setPlacedBy(World level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		if(!level.isClientSide)
		{
			TileEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CashRegisterBlockEntity)
			{
				CashRegisterBlockEntity register = (CashRegisterBlockEntity)blockEntity;
				register.loadDataFromItems(stack.getTag());
			}
		}
	}
	
	@Nonnull
	@Override
	public ActionResultType use(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
	{
		if(!level.isClientSide)
		{
			//Open UI
			TileEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CashRegisterBlockEntity)
			{
				CashRegisterBlockEntity register = (CashRegisterBlockEntity)blockEntity;
				BlockEntityUtil.sendUpdatePacket(blockEntity);
				register.OpenContainer(player);
			}
		}
		return ActionResultType.SUCCESS;
	}
	
}
