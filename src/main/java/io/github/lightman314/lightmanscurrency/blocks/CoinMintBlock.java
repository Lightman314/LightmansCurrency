package io.github.lightman314.lightmanscurrency.blocks;

//import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.containers.MintContainer;
import io.github.lightman314.lightmanscurrency.tileentity.CoinMintTileEntity;
import io.github.lightman314.lightmanscurrency.Config;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class CoinMintBlock extends RotatableBlock{

	private static final TranslationTextComponent TITLE = new TranslationTextComponent("gui.lightmanscurrency.coinmint.title");
	
	public CoinMintBlock(Properties properties)
	{
		super(properties, makeCuboidShape(1d,0d,1d,15d,16d,15d));
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) { return true; }
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new CoinMintTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof CoinMintTileEntity && Config.canMint() || Config.canMelt())
			{
				NetworkHooks.openGui((ServerPlayerEntity)player, new CoinMintMenuProvider((CoinMintTileEntity)tileEntity), pos);
				return ActionResultType.SUCCESS;
			}
		}
		
		return ActionResultType.PASS;
			
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if(tileEntity instanceof CoinMintTileEntity)
		{
			CoinMintTileEntity mintEntity = (CoinMintTileEntity)tileEntity;
			mintEntity.dumpContents(worldIn, pos);
		}
		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}
	
	private static class CoinMintMenuProvider implements INamedContainerProvider
	{
		private final CoinMintTileEntity tileEntity;
		public CoinMintMenuProvider(CoinMintTileEntity tileEntity) { this.tileEntity = tileEntity; }
		@Override
		public Container createMenu(int id, PlayerInventory inventory, PlayerEntity entity) { return new MintContainer(id, inventory, this.tileEntity); }
		@Override
		public ITextComponent getDisplayName() { return TITLE; }
	}
	
	
}
