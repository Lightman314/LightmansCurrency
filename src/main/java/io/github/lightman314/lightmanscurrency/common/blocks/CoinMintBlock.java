package io.github.lightman314.lightmanscurrency.common.blocks;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.blockentity.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.common.items.tooltips.LCTooltips;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class CoinMintBlock extends RotatableBlock {

	private static final IFormattableTextComponent TITLE = EasyText.translatable("gui.lightmanscurrency.coinmint.title");
	
	public CoinMintBlock(Properties properties)
	{
		super(properties, box(1d,0d,1d,15d,16d,15d));
	}

	@Override
	public boolean hasTileEntity(BlockState state) { return true; }

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader level) { return new CoinMintBlockEntity(); }
	
	@Nonnull
	@Override
	public ActionResultType use(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result)
	{
		if(!level.isClientSide)
		{
			TileEntity tileEntity = level.getBlockEntity(pos);
			if(tileEntity instanceof CoinMintBlockEntity && Config.SERVER.allowCoinMinting.get() || Config.SERVER.allowCoinMelting.get())
			{
				NetworkHooks.openGui((ServerPlayerEntity)player, new CoinMintMenuProvider((CoinMintBlockEntity)tileEntity), pos);
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.SUCCESS;
			
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(@Nonnull BlockState state, World level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving)
	{
		TileEntity blockEntity = level.getBlockEntity(pos);
		if(blockEntity instanceof CoinMintBlockEntity)
		{
			CoinMintBlockEntity mintEntity = (CoinMintBlockEntity)blockEntity;
			mintEntity.dumpContents(level, pos);
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		TooltipItem.addTooltip(tooltip, LCTooltips.COIN_MINT);
		super.appendHoverText(stack, level, tooltip, flagIn);
	}
	
	private static class CoinMintMenuProvider implements INamedContainerProvider
	{
		private final CoinMintBlockEntity tileEntity;
		public CoinMintMenuProvider(CoinMintBlockEntity tileEntity) { this.tileEntity = tileEntity; }
		@Override
		public Container createMenu(int id, @Nonnull PlayerInventory inventory, @Nonnull PlayerEntity player) { return new MintMenu(id, inventory, this.tileEntity); }
		@Nonnull
		@Override
		public ITextComponent getDisplayName() { return TITLE; }
	}
	
}
