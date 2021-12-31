package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.blocks.interfaces.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.events.WalletDropEvent;
import io.github.lightman314.lightmanscurrency.gamerule.ModGameRules;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessagePlayPickupSound;
import io.github.lightman314.lightmanscurrency.network.message.walletslot.SPacketSyncWallet;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil.CoinData;
import io.github.lightman314.lightmanscurrency.menus.WalletMenu;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
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
import net.minecraftforge.network.PacketDistributor;

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
		WalletMenu activeContainer = null;
		
		//Check if the open container is a wallet that is pickup capable
		if(player.containerMenu instanceof WalletMenu)
		{
			//CurrencyMod.LOGGER.info("Wallet Container was open. Adding to the wallet using this method.");
			WalletMenu container = (WalletMenu)player.containerMenu;
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
				if(!player.addItem(coinStack))
				{
					//Spawn the leftovers into the world
					ItemEntity itemEntity = new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), coinStack);
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
		
		LevelAccessor world = event.getWorld();
		BlockState state = world.getBlockState(event.getPos());
		
		if(state.getBlock() instanceof IOwnableBlock)
		{
			IOwnableBlock block = (IOwnableBlock)state.getBlock();
			if(!block.canBreak(event.getPlayer(), world, event.getPos(), state))
			{
				//CurrencyMod.LOGGER.info("onBlockBreak-Non-owner attempted to break a trader block. Aborting event!");
				event.setCanceled(true);
			}
		}
		
	}
	
	//Adds the wallet capability to the player
	@SubscribeEvent
	public static void attachEntitiesCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		//Don't attach the wallet capability if curios is loaded
		if(event.getObject() instanceof Player)
		{
			event.addCapability(CurrencyCapabilities.ID_WALLET, WalletCapability.createProvider((Player)event.getObject()));
		}
	}
	
	//Sync wallet when the player logs in
	@SubscribeEvent
	public static void playerLogin(PlayerLoggedInEvent event)
	{
		if(event.getPlayer().level.isClientSide)
			return;
		WalletCapability.getWalletHandler(event.getPlayer()).ifPresent(walletHandler ->{
			LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(event.getPlayer()), new SPacketSyncWallet(event.getPlayer().getId(), walletHandler.getWallet()));
		});
	}
	
	//Sync wallet contents for newly loaded entities
	@SubscribeEvent
	public static void playerStartTracking(PlayerEvent.StartTracking event)
	{
		Entity target = event.getTarget();
		Player player = event.getPlayer();
		if(!player.level.isClientSide)
		{
			WalletCapability.getWalletHandler(target).ifPresent(walletHandler ->{
				LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new SPacketSyncWallet(target.getId(), walletHandler.getWallet()));
			});
		}
	}
	
	//Copy the wallet over to the new player
	@SubscribeEvent
	public static void playerClone(PlayerEvent.Clone event)
	{
		Player player = event.getPlayer();
		if(player.level.isClientSide) //Do nothing client-side
			return;
		
		Player oldPlayer = event.getOriginal();
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
		LivingEntity livingEntity = event.getEntityLiving();
		if(livingEntity.level.isClientSide) //Do nothing client side
			return;
		
		if(!livingEntity.isSpectator())
		{
			WalletCapability.getWalletHandler(livingEntity).ifPresent(walletHandler ->{
				
				ItemStack walletStack = walletHandler.getWallet();
				
				if(walletStack.isEmpty())
					return;
				
				Collection<ItemEntity> walletDrops = Lists.newArrayList();
				if(livingEntity instanceof Player) //Only worry about gamerules on players. Otherwise it always drops the wallet.
				{
					boolean keepInventory = livingEntity.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
					GameRules.BooleanValue keepWalletVal = ModGameRules.getCustomValue(livingEntity.level, ModGameRules.KEEP_WALLET);
					GameRules.IntegerValue coinDropPercentVal = ModGameRules.getCustomValue(livingEntity.level, ModGameRules.COIN_DROP_PERCENT);
					boolean keepWallet = (keepWalletVal == null ? false : keepWalletVal.get()) || keepInventory;
					int coinDropPercent = coinDropPercentVal == null ? 0 : coinDropPercentVal.get();
					
					if(keepWallet && coinDropPercent <= 0)
						return;
					if(keepWallet) //Drop the wallet
					{
						
						Collection<ItemEntity> d = getWalletDrops(livingEntity, walletStack, coinDropPercent);
						
						//Spawn the coin drops
						walletDrops.addAll(d);
						
						//Post the Wallet Drop Event
						WalletDropEvent e = new WalletDropEvent((Player)livingEntity, walletHandler, event.getSource(), walletDrops, keepWallet, coinDropPercent);
						if(MinecraftForge.EVENT_BUS.post(e))
							return;
						walletDrops = e.getDrops();
						
					}
					else
					{
						
						walletDrops.add(getDrop(livingEntity,walletStack));
						walletHandler.setWallet(ItemStack.EMPTY);
						
						//Post the Wallet Drop Event
						WalletDropEvent e = new WalletDropEvent((Player)livingEntity, walletHandler, event.getSource(), walletDrops, keepWallet, coinDropPercent);
						if(MinecraftForge.EVENT_BUS.post(e))
							return;
						walletDrops = e.getDrops();
						
					}
					
				}
				else
				{
					walletDrops.add(getDrop(livingEntity,walletStack));
					walletHandler.setWallet(ItemStack.EMPTY);
				}
				
				event.getDrops().addAll(walletDrops);
				
			});
			
		}
	}
	
	private static ItemEntity getDrop(LivingEntity entity, ItemStack stack)
	{
        return new ItemEntity(entity.level, entity.position().x, entity.position().y, entity.position().z, stack);
	}
	
	private static List<ItemEntity> getWalletDrops(LivingEntity entity, ItemStack walletStack, int coinDropPercent)
	{
		
		double coinPercentage = MathUtil.clamp((double)coinDropPercent / 100d, 0d, 1d);
		NonNullList<ItemStack> walletList = WalletItem.getWalletInventory(walletStack);
		long walletContents = new MoneyUtil.CoinValue(walletList).getRawValue();
		
		long droppedAmount = (long)((double)walletContents * coinPercentage);
		if(droppedAmount < 1)
			return Lists.newArrayList();
		
		Container walletInventory = InventoryUtil.buildInventory(walletList);
		List<ItemEntity> drops = Lists.newArrayList();
		//Remove the dropped coins from the wallet
		long extra = MoneyUtil.takeObjectsOfValue(droppedAmount, walletInventory, true);
		if(extra < 0)
		{
			List<ItemStack> extraCoins = MoneyUtil.getCoinsOfValue(-extra);
			for(int i = 0; i < extraCoins.size(); i++)
			{
				ItemStack coinStack = InventoryUtil.TryPutItemStack(walletInventory, extraCoins.get(i));
				//Drop anything that wasn't able to fit back into the wallet
				if(!coinStack.isEmpty())
					drops.add(getDrop(entity,coinStack));
			}
		}
		
		//Update the wallet stacks contents
		WalletItem.putWalletInventory(walletStack, InventoryUtil.buildList(walletInventory));
		
		//Drop the expected coins
		drops.addAll(getCoinDrops(entity, droppedAmount));
		
		return drops;
		
	}
	
	private static Collection<ItemEntity> getCoinDrops(LivingEntity entity, long coinValue)
	{
		List<ItemEntity> drops = Lists.newArrayList();
		List<ItemStack> coinsOfValue = MoneyUtil.getCoinsOfValue(coinValue);
		for(int i = 0; i < coinsOfValue.size(); i++)
		{
			ItemStack coinStack = coinsOfValue.get(i);
			for(int count = 0; count < coinStack.getCount(); count++)
			{
				ItemStack coin = coinStack.copy();
				coin.setCount(1);
				drops.add(getDrop(entity,coin));
			}
		}
		return drops;
	}
	
	//Check for wallet updates, and send update packets
	@SubscribeEvent
	public static void entityTick(LivingEvent.LivingUpdateEvent event) {
		LivingEntity livingEntity = event.getEntityLiving();
		if(livingEntity.level.isClientSide) //Do nothing client side
			return;
		
		WalletCapability.getWalletHandler(livingEntity).ifPresent(walletHandler ->{
			if(walletHandler.isDirty())
			{
				LightmansCurrencyPacketHandler.instance.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity), new SPacketSyncWallet(livingEntity.getId(), walletHandler.getWallet()));
				walletHandler.clean();
			}
		});
	}
}
