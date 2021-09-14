package io.github.lightman314.lightmanscurrency.blocks;

import io.github.lightman314.lightmanscurrency.containers.MintContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import io.github.lightman314.lightmanscurrency.Config;

public class CoinMintBlock extends RotatableBlock{

	private static final TranslatableComponent TITLE = new TranslatableComponent("gui.lightmanscurrency.coinmint.title");
	
	public CoinMintBlock(Properties properties)
	{
		super(properties, box(1d,0d,1d,15d,16d,15d));
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result)
	{
		if(Config.canMint() || Config.canMelt())
		{
			player.openMenu(state.getMenuProvider(level, pos));
			return InteractionResult.SUCCESS;
		}
		else
		{
			//CurrencyMod.LOGGER.info("Coin minting is not allowed. Aborting coin mint screen.");
			return InteractionResult.PASS;
		}
			
	}
	
	@Override
	public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos)
	{
		return new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> { return new MintContainer(windowId, playerInventory);}, TITLE);
	}
	
	
	
}
