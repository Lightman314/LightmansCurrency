package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessagePlayPickupSound;
import io.github.lightman314.lightmanscurrency.tileentity.IOwnableTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.PaygateTileEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
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
		
		PlayerEntity player = event.getPlayer();
		ItemStack coinStack = event.getItem().getItem();
		WalletContainer activeContainer = null;
		
		//Check if the open container is a wallet that is pickup capable
		if(player.openContainer instanceof WalletContainer)
		{
			//CurrencyMod.LOGGER.info("Wallet Container was open. Adding to the wallet using this method.");
			WalletContainer container = (WalletContainer)player.openContainer;
			if(container.canPickup())
				activeContainer = container;
		}
		
		boolean cancelEvent = false;
		
		//Get the currently equipped wallet
		ItemStack wallet = LightmansCurrency.getWalletStack(player);
		if(!wallet.isEmpty())
		{
			WalletItem walletItem = (WalletItem)wallet.getItem();
			if(WalletItem.CanPickup(walletItem))
			{
				cancelEvent = true;
				if(activeContainer != null && activeContainer.getWalletIndex() < 0)
				{
					coinStack = activeContainer.PickupCoins(coinStack);
				}
				else
				{
					coinStack = WalletItem.PickupCoin(wallet, coinStack);
				}
			}
		}
		
		if(event.isCancelable() && cancelEvent)
		{
			//CurrencyMod.LOGGER.info("Canceling the event as the pickup item is being handled by this event instead of vanilla means.");
			event.getItem().setItem(ItemStack.EMPTY);
			if(!coinStack.isEmpty())
			{
				if(!player.inventory.addItemStackToInventory(coinStack))
				{
					//Spawn the leftovers into the world
					ItemEntity itemEntity = new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), coinStack);
					itemEntity.setPickupDelay(40);
					player.world.addEntity(itemEntity);
				}
			}
			if(!player.world.isRemote)
				LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new MessagePlayPickupSound());
			event.setCanceled(true);
			
		}
		
	}
	
	//Block break event for trader protection functionality
	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event)
	{
		
		IWorld world = event.getWorld();
		BlockState state = world.getBlockState(event.getPos());
		
		if(state.getBlock() instanceof ITraderBlock)
		{
			//CurrencyMod.LOGGER.info("onBlockBreak-Block is a trader block!");
			ITraderBlock block = (ITraderBlock)state.getBlock();
			TileEntity tileEntity = block.getTileEntity(state, world, event.getPos());
			if(tileEntity instanceof IOwnableTileEntity)
			{
				IOwnableTileEntity traderEntity = (IOwnableTileEntity)tileEntity;
				if(!traderEntity.canBreak(event.getPlayer()))
				{
					//CurrencyMod.LOGGER.info("onBlockBreak-Non-owner attempted to break a trader block. Aborting event!");
					event.setCanceled(true);
				}
			}
		}
		else if(state.getBlock() instanceof PaygateBlock)
		{
			TileEntity tileEntity = world.getTileEntity(event.getPos());
			if(tileEntity instanceof PaygateTileEntity)
			{
				PaygateTileEntity paygateEntity = (PaygateTileEntity)tileEntity;
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
			for(int i = 0; i < event.getInventory().getSizeInventory() && walletIn.isEmpty(); i++)
			{
				if(event.getInventory().getStackInSlot(i).getItem() instanceof WalletItem)
					walletIn = event.getInventory().getStackInSlot(i);
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
			if(walletIn.hasDisplayName())
				walletOut.setDisplayName(walletIn.getDisplayName());
			
			//Copy the wallets enchantments if any are present
			if(walletIn.isEnchanted())
			{
				EnchantmentHelper.getEnchantments(walletIn).forEach((enchantment, level) ->{
					walletOut.addEnchantment(enchantment, level);
				});
			}
			
		}
		//else
			//LightmansCurrency.LOGGER.info("Player is NOT crafting a wallet.");
	}
	
	//Item Tooltip event for special lore for item trader items
	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event)
	{
		ItemStack item = event.getItemStack();
		if(item.hasTag())
		{
			CompoundNBT itemTag = item.getTag();
			if(itemTag.contains("LC_DisplayItem") && itemTag.getBoolean("LC_DisplayItem"))
			{
				
				List<ITextComponent> addedTags = new ArrayList<>();
				
				//Add original name info
				if(itemTag.contains("LC_CustomName", Constants.NBT.TAG_STRING))
				{
					ITextComponent originalName = event.getToolTip().get(0);
					event.getToolTip().set(0, new StringTextComponent("§6" + itemTag.getString("LC_CustomName")));
					addedTags.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.originalname", originalName));
				}
				//Add stock info
				if(itemTag.contains("LC_StockAmount", Constants.NBT.TAG_INT))
				{
					int stockAmount = itemTag.getInt("LC_StockAmount");
					addedTags.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.stock", stockAmount >= 0 ? new StringTextComponent("§6" + stockAmount) : new TranslationTextComponent("tooltip.lightmanscurrency.trader.stock.infinite")));
				}
				
				if(addedTags.size() > 0)
				{
					event.getToolTip().add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.info"));
					addedTags.forEach(tag -> event.getToolTip().add(tag));
				}
			}
		}
	}
	
}
