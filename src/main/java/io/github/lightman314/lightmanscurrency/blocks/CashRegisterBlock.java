package io.github.lightman314.lightmanscurrency.blocks;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.tileentity.CashRegisterTileEntity;
import io.github.lightman314.lightmanscurrency.util.TileEntityUtil;
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

public class CashRegisterBlock extends RotatableBlock {

	public CashRegisterBlock(Properties properties)
	{
		super(properties);
	}
	
	public CashRegisterBlock(Properties properties, VoxelShape shape)
	{
		super(properties,shape);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new CashRegisterTileEntity();
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity player, ItemStack stack)
	{
		if(!worldIn.isRemote())
		{
			CashRegisterTileEntity tileEntity = (CashRegisterTileEntity)worldIn.getTileEntity(pos);
			if(tileEntity != null)
			{
				tileEntity.loadDataFromItems(stack.getTag());
			}
		}
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote())
		{
			//Open UI
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof CashRegisterTileEntity)
			{
				CashRegisterTileEntity register = (CashRegisterTileEntity)tileEntity;
				TileEntityUtil.sendUpdatePacket(tileEntity);
				register.OpenContainer(playerEntity);
			}
		}
		return ActionResultType.SUCCESS;
	}
	
}
