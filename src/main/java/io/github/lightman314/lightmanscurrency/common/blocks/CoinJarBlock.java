package io.github.lightman314.lightmanscurrency.common.blocks;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinJarBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Collection;

public class CoinJarBlock extends RotatableBlock implements IEasyEntityBlock {

	public CoinJarBlock(Properties properties) { super(properties); }
	
	public CoinJarBlock(Properties properties, VoxelShape shape) { super(properties, shape); }

	@Override
	protected boolean isBlockOpaque() { return false; }

	@Nonnull
	@Override
	public Collection<BlockEntityType<?>> getAllowedTypes() { return ImmutableList.of(ModBlockEntities.COIN_JAR.get()); }

	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new CoinJarBlockEntity(pos, state); }
	
	@Override
	public void setPlacedBy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity player, @Nonnull ItemStack stack)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof CoinJarBlockEntity jar)
			jar.readItemData(stack);
	}

	@Nonnull
	@Override
	protected ItemInteractionResult useItemOn(@Nonnull ItemStack item, @Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit)
	{
		if(!level.isClientSide)
		{
			ItemStack coinStack = player.getItemInHand(hand);
			if(!CoinAPI.API.IsAllowedInCoinContainer(coinStack, false))
				return ItemInteractionResult.SUCCESS;
			//Add coins to the bank
			if(level.getBlockEntity(pos) instanceof CoinJarBlockEntity jar)
			{
				if(jar.addCoin(coinStack))
					coinStack.shrink(1);
			}
		}
		return ItemInteractionResult.SUCCESS;
	}
	
	@Nonnull
	@Override
	public BlockState playerWillDestroy(Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Player player)
	{
		
		//Prevent client-side multi-block destruction & breaking animations if they aren't allowed to break this trader
		BlockEntity tileEntity = level.getBlockEntity(pos);
		if(tileEntity instanceof CoinJarBlockEntity jarEntity)
		{
			Holder<Enchantment> silkTouch = LookupHelper.lookupEnchantment(player.registryAccess(),Enchantments.SILK_TOUCH);
			if(silkTouch != null && EnchantmentHelper.getEnchantmentLevel(silkTouch, player) > 0)
			{
				//Drop the item for this block, with the JarData in it.
				ItemStack dropStack = new ItemStack(this, 1);
				jarEntity.addFullData(dropStack);
				jarEntity.clearStorage();
				Block.popResource(level, pos, dropStack);
			}
		}
		
		return super.playerWillDestroy(level, pos, state, player);
		
	}

	@Override
	public void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean flag) {
		if(state.is(newState.getBlock()))
		{
			super.onRemove(state, level, pos, newState, flag);
			return;
		}
		//Drop the jars contents
		if(level.getBlockEntity(pos) instanceof CoinJarBlockEntity jarEntity)
		{
			jarEntity.getStorage().forEach(coin -> Block.popResource(level, pos, coin));
			jarEntity.clearStorage();
		}
	}


	@Nonnull
	@Override
	public ItemStack getCloneItemStack(@Nonnull BlockState state, @Nonnull HitResult target, @Nonnull LevelReader level, @Nonnull BlockPos pos, @Nonnull Player player) {
		ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);
		if(level.getBlockEntity(pos) instanceof CoinJarBlockEntity jarBlock)
		{
			if(player.isCreative() && player.isCrouching())
				jarBlock.addFullData(stack);
			else
				jarBlock.addSimpleData(stack);
		}
		return stack;
	}

}
