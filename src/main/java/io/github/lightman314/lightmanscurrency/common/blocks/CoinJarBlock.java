package io.github.lightman314.lightmanscurrency.common.blocks;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinJarBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class CoinJarBlock extends RotatableBlock implements EntityBlock{

	public CoinJarBlock(Properties properties)
	{
		super(properties);
	}
	
	public CoinJarBlock(Properties properties, VoxelShape shape)
	{
		super(properties, shape);
	}
	
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new CoinJarBlockEntity(pos, state); }
	
	@Override
	public void setPlacedBy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof CoinJarBlockEntity jar)
			jar.readItemTag(stack);
	}
	
	@Nonnull
	@Override
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			ItemStack coinStack = player.getItemInHand(hand);
			if(!MoneyUtil.isCoin(coinStack, false))
				return InteractionResult.SUCCESS;
			//Add coins to the bank
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof CoinJarBlockEntity jar)
			{
				if(jar.addCoin(coinStack))
					coinStack.shrink(1);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void playerWillDestroy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player)
	{
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		BlockEntity tileEntity = level.getBlockEntity(pos);
		if(tileEntity instanceof CoinJarBlockEntity jarEntity)
		{
			if(EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player) > 0)
			{
				//Drop the item for this block, with the JarData in it.
				@SuppressWarnings("deprecation")
				ItemStack dropStack = new ItemStack(Item.byBlock(this), 1);
				if(jarEntity.getStorage().size() > 0)
					jarEntity.writeItemTag(dropStack);
				Block.popResource(level, pos, dropStack);
			}
			else
			{
				//Only drop the coins within the jar
				jarEntity.getStorage().forEach(coin -> Block.popResource(level, pos, coin));
			}
		}
		
		super.playerWillDestroy(level, pos, state, player);
		
	}
	
}
