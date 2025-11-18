package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
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

import javax.annotation.Nonnull;

public class CashRegisterBlock extends RotatableBlock implements EntityBlock, IVariantBlock {

	public CashRegisterBlock(Properties properties) { super(properties); }
	
	public CashRegisterBlock(Properties properties, VoxelShape shape)
	{
		super(properties,shape);
	}
	
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new CashRegisterBlockEntity(pos, state); }
	
	@Override
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		if(!level.isClientSide)
		{
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CashRegisterBlockEntity register)
                register.loadDataFromItems(stack);
		}
		this.tryCopyVariant(level,pos,stack);
	}

	@Nonnull
	@Override
	public InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			//Open UI
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CashRegisterBlockEntity register)
			{
                BlockEntityUtil.sendUpdatePacket(blockEntity);
				register.OpenContainer(player);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
}
