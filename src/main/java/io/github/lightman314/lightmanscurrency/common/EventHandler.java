package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.blocks.ITraderBlock;
import io.github.lightman314.lightmanscurrency.blocks.PaygateBlock;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.containers.WalletContainer;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessagePlayPickupSound;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.SPacketSyncWallet;
import io.github.lightman314.lightmanscurrency.tileentity.IOwnableTileEntity;
import io.github.lightman314.lightmanscurrency.tileentity.PaygateTileEntity;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;

import java.util.Collection;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

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
	
	//Adds the wallet capability to the player
	@SubscribeEvent
	public static void attachEntitiesCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		//Don't attach the wallet capability if curios is loaded
		if(LightmansCurrency.isCuriosLoaded())
			return;
		if(event.getObject() instanceof PlayerEntity)
		{
			event.addCapability(CurrencyCapabilities.ID_WALLET, WalletCapability.createProvider((PlayerEntity)event.getObject()));
		}
	}
	
	//Sync wallet when the player logs in
	@SubscribeEvent
	public static void playerLogin(PlayerLoggedInEvent event)
	{
		if(LightmansCurrency.isCuriosLoaded())
			return;
		if(event.getPlayer().world.isRemote)
			return;
		WalletCapability.getWalletHandler(event.getPlayer()).ifPresent(walletHandler ->{
			LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(event.getPlayer()), new SPacketSyncWallet(event.getPlayer().getEntityId(), walletHandler.getWallet()));
			//LightmansCurrency.LogInfo("Sending wallet update packet as the player logged in.");
		});
	}
	
	//Sync wallet contents for newly loaded entities
	@SubscribeEvent
	public static void playerStartTracking(PlayerEvent.StartTracking event)
	{
		if(LightmansCurrency.isCuriosLoaded())
			return;
		Entity target = event.getTarget();
		PlayerEntity player = event.getPlayer();
		if(!player.world.isRemote)
		{
			WalletCapability.getWalletHandler(target).ifPresent(walletHandler ->{
				LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new SPacketSyncWallet(target.getEntityId(), walletHandler.getWallet()));
				//LightmansCurrency.LogInfo("Sent wallet update packet as the entity is now being tracked.");
			});
		}
	}
	
	//Copy the wallet over to the new player
	@SubscribeEvent
	public static void playerClone(PlayerEvent.Clone event)
	{
		if(LightmansCurrency.isCuriosLoaded())
			return;
		PlayerEntity player = event.getPlayer();
		if(player.world.isRemote) //Do nothing client-side
			return;
		
		PlayerEntity oldPlayer = event.getOriginal();
		oldPlayer.revive();
		LazyOptional<IWalletHandler> oldHandler = WalletCapability.getWalletHandler(oldPlayer);
		LazyOptional<IWalletHandler> newHandler = WalletCapability.getWalletHandler(player);
		
		oldHandler.ifPresent(oldWallet -> newHandler.ifPresent(newWallet ->{
			newWallet.setWallet(oldWallet.getWallet());
		}));
	}
	
	//Drop the wallet if keep inventory isn't on.
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void playerDrops(LivingDropsEvent event)
	{
		if(LightmansCurrency.isCuriosLoaded())
			return;
		LivingEntity livingEntity = event.getEntityLiving();
		if(livingEntity.world.isRemote) //Do nothing client side
			return;
		
		if(!livingEntity.isSpectator())
		{
			WalletCapability.getWalletHandler(livingEntity).ifPresent(walletHandler ->{
				Collection<ItemEntity> drops = event.getDrops();
				
				if(livingEntity.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY))
					return;
				
				if(walletHandler.getWallet().isEmpty())
					return;
				
				ItemEntity walletDrop = new ItemEntity(livingEntity.world, livingEntity.getPosX(), livingEntity.getPosX(), livingEntity.getPosX(), walletHandler.getWallet());
				drops.add(walletDrop);
				
			});
		}
	}
	
	//Check for wallet updates, and send update packets
	@SubscribeEvent
	public static void entityTick(LivingEvent.LivingUpdateEvent event) {
		if(LightmansCurrency.isCuriosLoaded())
			return;
		LivingEntity livingEntity = event.getEntityLiving();
		if(livingEntity.world.isRemote) //Do nothing client side
			return;
		
		WalletCapability.getWalletHandler(livingEntity).ifPresent(walletHandler ->{
			if(walletHandler.isDirty())
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity), new SPacketSyncWallet(livingEntity.getEntityId(), walletHandler.getWallet()));
				walletHandler.clean();
				//LightmansCurrency.LogInfo("Sending wallet packet as the wallet has been changed.");
			}
		});
	}
}
