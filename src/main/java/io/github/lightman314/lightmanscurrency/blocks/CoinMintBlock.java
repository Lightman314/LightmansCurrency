package io.github.lightman314.lightmanscurrency.blocks;

import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.menus.MintMenu;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.blockentity.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.items.tooltips.LCTooltips;

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
		return new CoinMintBlockEntity(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(!level.isClientSide)
		{
			BlockEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof CoinMintBlockEntity && Config.SERVER.allowCoinMinting.get() || Config.SERVER.allowCoinMelting.get())
			{
				NetworkHooks.openGui((ServerPlayer)player, new CoinMintMenuProvider((CoinMintBlockEntity)tileEntity), pos);
				return InteractionResult.SUCCESS;
			}
		}
		if(Config.SERVER.allowCoinMinting.get() || Config.SERVER.allowCoinMelting.get())
			return InteractionResult.SUCCESS;
		return InteractionResult.SUCCESS;
			
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
	{
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof CoinMintBlockEntity)
		{
			CoinMintBlockEntity mintEntity = (CoinMintBlockEntity)blockEntity;
			mintEntity.dumpContents(level, pos);
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.COIN_MINT);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
	private static class CoinMintMenuProvider implements MenuProvider
	{
		private final CoinMintBlockEntity tileEntity;
		public CoinMintMenuProvider(CoinMintBlockEntity tileEntity) { this.tileEntity = tileEntity; }
		@Override
		public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new MintMenu(id, inventory, this.tileEntity); }
		@Override
		public Component getDisplayName() { return TITLE; }
	}
	
	
}
