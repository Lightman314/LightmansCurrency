package io.github.lightman314.lightmanscurrency.blocks;

//import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.containers.MintContainer;
import io.github.lightman314.lightmanscurrency.tileentity.CoinMintTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;

public class CoinMintBlock extends RotatableBlock implements EntityBlock{

	private static final TranslatableComponent TITLE = new TranslatableComponent("gui.lightmanscurrency.coinmint.title");
	
	public CoinMintBlock(Properties properties)
	{
		super(properties, box(1d,0d,1d,15d,16d,15d));
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new CoinMintTileEntity(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			BlockEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof CoinMintTileEntity && Config.canMint() || Config.canMelt())
			{
				NetworkHooks.openGui((ServerPlayer)player, new CoinMintMenuProvider((CoinMintTileEntity)tileEntity), pos);
				return InteractionResult.SUCCESS;
			}
		}
		
		return InteractionResult.PASS;
			
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof CoinMintTileEntity)
		{
			CoinMintTileEntity mintEntity = (CoinMintTileEntity)blockEntity;
			mintEntity.dumpContents(level, pos);
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
	
	private static class CoinMintMenuProvider implements MenuProvider
	{
		private final CoinMintTileEntity tileEntity;
		public CoinMintMenuProvider(CoinMintTileEntity tileEntity) { this.tileEntity = tileEntity; }
		@Override
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new MintContainer(id, inventory, this.tileEntity); }
		@Override
		public Component getDisplayName() { return TITLE; }
	}
	
	
}
