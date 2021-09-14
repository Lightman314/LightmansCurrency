package io.github.lightman314.lightmanscurrency.blocks;

//import java.util.List;

import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.containers.MintContainer;
import io.github.lightman314.lightmanscurrency.Config;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class CoinMintBlock extends RotatableBlock{

	private static final TranslationTextComponent TITLE = new TranslationTextComponent("gui.lightmanscurrency.coinmint.title");
	
	public CoinMintBlock(Properties properties)
	{
		super(properties, makeCuboidShape(1d,0d,1d,15d,16d,15d));
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result)
	{
		if(Config.canMint() || Config.canMelt())
		{
			player.openContainer(state.getContainer(world, pos));
			return ActionResultType.SUCCESS;
		}
		else
		{
			//CurrencyMod.LOGGER.info("Coin minting is not allowed. Aborting coin mint screen.");
			return ActionResultType.PASS;
		}
			
	}
	
	@Nullable
	@Override
	public INamedContainerProvider getContainer(BlockState state, World world, BlockPos pos)
	{
		return new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) -> { return new MintContainer(windowId, playerInventory, IWorldPosCallable.of(world,pos));}, TITLE);
	}
	
	
	
}
