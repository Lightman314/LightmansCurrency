package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessagePlayPickupSound;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;
import io.github.lightman314.lightmanscurrency.util.WalletUtil;
import io.github.lightman314.lightmanscurrency.util.WalletUtil.PlayerWallets;
import io.github.lightman314.lightmanscurrency.blockentity.IOwnableBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.PaygateBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventHandler {

	//Pickup Event for wallet coin collection functionality
	@SubscribeEvent
	public static void pickupItem(EntityItemPickupEvent event)
	{
		
		ItemStack pickupItem = event.getItem().getItem();
		CoinData coinData = MoneyUtil.getData(pickupItem.getItem());
		if(coinData == null || coinData.isHidden())
			return;
		
		Player player = event.getPlayer();
		ItemStack coinStack = event.getItem().getItem();
		WalletContainer activeContainer = null;
		
		//Check if the open container is a wallet that is pickup capable
		if(player.containerMenu instanceof WalletContainer)
		{
			//CurrencyMod.LOGGER.info("Wallet Container was open. Adding to the wallet using this method.");
			WalletContainer container = (WalletContainer)player.containerMenu;
			if(container.canPickup())
				activeContainer = container;
		}
		
		boolean cancelEvent = false;
		
		//Get the currently equipped wallet
		PlayerWallets wallet = WalletUtil.getWallets(player);
		if(wallet.hasWallet())
		{
			if(wallet.canPickup())
			{
				cancelEvent = true;
				if(activeContainer != null && activeContainer.getWalletIndex() < 0 && activeContainer.canPickup())
				{
					coinStack = activeContainer.PickupCoins(coinStack);
				}
				else
				{
					coinStack = wallet.PickupCoin(coinStack);
				}
			}
		}
		
		if(event.isCancelable() && cancelEvent)
		{
			//CurrencyMod.LOGGER.info("Canceling the event as the pickup item is being handled by this event instead of vanilla means.");
			event.getItem().setItem(ItemStack.EMPTY);
			if(!coinStack.isEmpty())
			{
				if(!player.getInventory().add(coinStack))
				{
					//Spawn the leftovers into the world
					ItemEntity itemEntity = new ItemEntity(player.level, player.getBlockX(), player.getBlockY(), player.getBlockZ(), coinStack);
					itemEntity.setPickUpDelay(40);
					player.level.addFreshEntity(itemEntity);
				}
			}
			if(!player.level.isClientSide)
				LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessagePlayPickupSound());
			event.setCanceled(true);
		}
		
	}
	
	//Block break event for trader protection functionality
	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event)
	{
		
		LevelAccessor level = event.getWorld();
		BlockState state = level.getBlockState(event.getPos());
		
		if(state.getBlock() instanceof ITraderBlock)
		{
			//CurrencyMod.LOGGER.info("onBlockBreak-Block is a trader block!");
			ITraderBlock block = (ITraderBlock)state.getBlock();
			BlockEntity tileEntity = block.getTileEntity(state, level, event.getPos());
			if(tileEntity instanceof IOwnableBlockEntity)
			{
				IOwnableBlockEntity traderEntity = (IOwnableBlockEntity)tileEntity;
				if(!traderEntity.canBreak(event.getPlayer()))
				{
					//CurrencyMod.LOGGER.info("onBlockBreak-Non-owner attempted to break a trader block. Aborting event!");
					event.setCanceled(true);
				}
			}
		}
		else if(state.getBlock() instanceof PaygateBlock)
		{
			BlockEntity blockEntity = level.getBlockEntity(event.getPos());
			if(blockEntity instanceof PaygateBlockEntity)
			{
				PaygateBlockEntity paygateEntity = (PaygateBlockEntity)blockEntity;
				if(!paygateEntity.canBreak(event.getPlayer()))
				{
					event.setCanceled(true);
				}
			}
		
		}
		
	}
	
	//Crafting event for wallet upgrade crafting recipes
	@SubscribeEvent
	public static void onItemCrafted(ItemCraftedEvent event)
	{
		if(event.getCrafting().getItem() instanceof WalletItem)
		{
			//LightmansCurrency.LOGGER.info("Player is crafting a wallet.");
			//Store the output wallet
			ItemStack walletOut = event.getCrafting();
			ItemStack walletIn = ItemStack.EMPTY;
			//Search the crafting matrix for a wallet.
			for(int i = 0; i < event.getInventory().getContainerSize() && walletIn.isEmpty(); i++)
			{
				if(event.getInventory().getItem(i).getItem() instanceof WalletItem)
					walletIn = event.getInventory().getItem(i);
			}
			if(walletIn.isEmpty())
			{
				//A wallet wasn't used to craft this one, so nothing needs to be done.
				//LightmansCurrency.LOGGER.info("No wallet is consumed in the crafting of this wallet. Nothing needs to be done.");
				return;
			}
			//Copy the wallet output's inventory contents to the newly crafted wallet.
			//LightmansCurrency.LOGGER.info("Copying wallet storage from the old wallet to the new wallet.");
			WalletItem.CopyWalletContents(walletIn, walletOut);
			
			//Copy the wallets display name if one is present.
			if(walletIn.hasCustomHoverName())
				walletOut.setHoverName(walletIn.getDisplayName());
			
			//Copy the wallets enchantments if any are present
			if(walletIn.isEnchanted())
			{
				EnchantmentHelper.getEnchantments(walletIn).forEach((enchantment, level) ->{
					walletOut.enchant(enchantment, level);
				});
			}
			
		}
		//else
			//LightmansCurrency.LOGGER.info("Player is NOT crafting a wallet.");
	}
	
	//Player death event for equipped wallet handling
	/*public static void onPlayerDeath(LivingDeathEvent event)
	{
		if(event.getEntityLiving() instanceof Player)
		{
			Player player = (Player)event.getEntityLiving();
			PlayerWallets wallets = WalletUtil.getWallets(player);
			if(!wallets.hasWallet())
				return; //Nothing to do if there's no wallet
			WalletDropMode dropMode = Config.SERVER.walletDropMode.get();
			if(dropMode == WalletDropMode.DROP_COINS)
			{
				long coinsToDrop = (long)(wallets.getStoredMoney() * Config.SERVER.coinDropPercent.get());
				wallets.extractMoney(coinsToDrop);
			}
			else if(dropMode == WalletDropMode.DROP_WALLET && !player.level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).get())
			{
				if(!wallets.hasEquippedWallet())
					return;
				ItemStack wallet = WalletUtil.extractEquippedWallet(player);
				InventoryUtil.spawnItemStack(player.level, player.position().x, player.position().y, player.position().z, wallet);
			}
		}
	}*/
	
}
