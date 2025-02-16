package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.RotatableBlock;

public class CoinMintBlock extends RotatableBlock implements IEasyEntityBlock {
	
	public CoinMintBlock(Properties properties) { super(properties, box(1d,0d,1d,15d,16d,15d)); }

	@Nonnull
	@Override
	public Collection<BlockEntityType<?>> getAllowedTypes() { return ImmutableList.of(ModBlockEntities.COIN_MINT.get()); }

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new CoinMintBlockEntity(pos, state); }
	
	@Nonnull
	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			BlockEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof CoinMintBlockEntity mint)
			{
				NetworkHooks.openScreen((ServerPlayer)player, new CoinMintMenuProvider(mint), pos);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.SUCCESS;
			
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(@Nonnull BlockState state, Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof CoinMintBlockEntity mintEntity)
			mintEntity.dumpContents(level, pos);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	private record CoinMintMenuProvider(CoinMintBlockEntity blockEntity) implements MenuProvider {
		@Override
		public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inventory, @Nonnull Player player) { return new MintMenu(id, inventory, this.blockEntity); }
		@Nonnull
		@Override
		public Component getDisplayName() { return LCText.GUI_COIN_MINT_TITLE.get(); }
	}

}
