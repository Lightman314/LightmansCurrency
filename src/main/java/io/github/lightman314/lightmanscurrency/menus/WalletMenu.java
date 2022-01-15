package io.github.lightman314.lightmanscurrency.menus;

import io.github.lightman314.lightmanscurrency.core.ModContainers;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.containers.SuppliedContainer;
import io.github.lightman314.lightmanscurrency.menus.slots.BlacklistSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.menus.slots.DisplaySlot;
import io.github.lightman314.lightmanscurrency.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.util.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WalletMenu extends AbstractContainerMenu{
	
	private static int maxWalletSlots = 0;
	public static void updateMaxWalletSlots(int slotCount)
	{
		if(slotCount > maxWalletSlots)
			maxWalletSlots = slotCount;
	}
	
	private Container dummyInventory = new SimpleContainer(1);
	
	private final int walletStackIndex;
	public int getWalletIndex()
	{
		return this.walletStackIndex;
	}
	
	private final Inventory inventory;
	
	public boolean hasWallet() { ItemStack wallet = this.getWallet(); return !wallet.isEmpty() && wallet.getItem() instanceof WalletItem; }
	private ItemStack getWallet()
	{
		if(this.walletStackIndex < 0)
			return LightmansCurrency.getWalletStack(this.inventory.player);
		return this.inventory.getItem(this.walletStackIndex);
	}
	
	private final List<IWalletMenuListener> listeners = Lists.newArrayList();
	private boolean walletSlotChanged = false;
	
	private final Container walletInventory;
	private Container coinInput;
	
	private WalletItem walletItem;
	public Component getTitle() { ItemStack wallet = getWallet(); if(wallet.isEmpty()) return new TextComponent(""); return wallet.getHoverName(); }
	
	boolean autoConvert = false;
	
	public WalletMenu(int windowId, Inventory inventory, int walletStackIndex)
	{
		
		super(ModContainers.WALLET, windowId);
		
		this.walletStackIndex = walletStackIndex;
		this.inventory = inventory;
		
		this.walletInventory = new SuppliedContainer(() -> {
			AtomicReference<Container> container  = new AtomicReference<Container>(null);
			WalletCapability.getWalletHandler(this.inventory.player).ifPresent(walletHandler -> container.set(walletHandler.getInventory()));
			return container.get();
		});
		
		this.init();
		
		//Register at the end to ensure that the player inventory & walletSlotIndex have been set.
		MinecraftForge.EVENT_BUS.register(this);
		
	}
	
	private void init()
	{
		
		NonNullList<Slot> newSlots = NonNullList.create();
		
		Item item = this.getWallet().getItem();
		if(item instanceof WalletItem)
			this.walletItem = (WalletItem)item;
		else
			this.walletItem = null;
		
		this.coinInput = new SimpleContainer(WalletItem.InventorySize(this.walletItem));
		NonNullList<ItemStack> walletInventory = WalletItem.getWalletInventory(getWallet());
		for(int i = 0; i < this.coinInput.getContainerSize() && i < walletInventory.size(); i++)
		{
			this.coinInput.setItem(i, walletInventory.get(i));
		}
		
		//Wallet Slot
		WalletSlot walletSlot = new WalletSlot(this.walletInventory, 0, -22, 6).addListener(this::onWalletSlotChanged);
		if(this.walletStackIndex >= 0)
			walletSlot.setBlacklist(this.inventory, this.walletStackIndex);
		newSlots.add(walletSlot);
		
		//Player Inventory before coin slots for desync safety.
		//Should make the Player Inventory slot indexes constant regardless of the wallet state.
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				int index = x + (y * 9) + 9;
				if(index == this.walletStackIndex)
					newSlots.add(new DisplaySlot(this.inventory, index, 8 + x * 18, 32 + (y + getRowCount()) * 18));
				else
					newSlots.add(new BlacklistSlot(this.inventory, index, 8 + x * 18, 32 + (y + getRowCount()) * 18, this.inventory, this.walletStackIndex));
			}
		}
		
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			if(x == this.walletStackIndex)
				newSlots.add(new DisplaySlot(this.inventory, x, 8 + x * 18, 90 + getRowCount() * 18));
			else
				newSlots.add(new BlacklistSlot(this.inventory, x, 8 + x * 18, 90 + getRowCount() * 18, this.inventory, this.walletStackIndex));
		}
		
		//Coin Slots last as they may vary between client and server at times.
		for(int y = 0; (y * 9) < this.coinInput.getContainerSize(); y++)
		{
			for(int x = 0; x < 9 && (x + y * 9) < this.coinInput.getContainerSize(); x++)
			{
				newSlots.add(new CoinSlot(this.coinInput, x + y * 9, 8 + x * 18, 18 + y * 18));
			}
		}
		
		while(newSlots.size() < 37 + maxWalletSlots)
		{
			newSlots.add(new DisplaySlot(dummyInventory, 0, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2));
		}
		
		this.autoConvert = WalletItem.getAutoConvert(getWallet());
		
		this.listeners.forEach(listener -> listener.onReload());
		
		//Reset the slots at the very end to prevent conflict with any for loops that may or may not be going on atm.
		this.setSlots(newSlots);
		
	}
	
	protected void setSlots(List<Slot> newSlots)
	{
		this.slots.clear();
		newSlots.forEach(newSlot -> this.addSlot(newSlot));
	}
	
	private void onWalletSlotChanged() {
		this.walletSlotChanged = true;
	}
	
	public void addListener(IWalletMenuListener listener)
	{
		if(!this.listeners.contains(listener))
			listeners.add(listener);
	}
	
	public int getRowCount()
	{
		return 1 + ((this.coinInput.getContainerSize() - 1)/9);
	}
	
	public int getSlotCount()
	{
		return this.coinInput.getContainerSize();
	}
	
	@Override
	public boolean stillValid(Player playerIn) { return true; }
	
	@Override
	public void removed(Player playerIn)
	{
		
		super.removed(playerIn);
		
		//Clear the dummy inventory, just in case an item somehow got put inside of it.
		this.clearContainer(playerIn, this.dummyInventory);
		
		this.saveWalletContents();
		
		MinecraftForge.EVENT_BUS.unregister(this);
		
	}
	
	@SubscribeEvent
	public void onTick(WorldTickEvent event)
	{
		if(event.side.isClient() || event.phase != TickEvent.Phase.START)
			return;
		if(this.walletSlotChanged)
		{
			this.walletSlotChanged = false;
			this.init();
		}
		if(this.inventory == null)
			return;
		this.saveWalletContents();
	}
	
	public void clientTick()
	{
		if(this.walletSlotChanged)
		{
			this.walletSlotChanged = false;
			this.init();
		}
	}
	
	public void saveWalletContents()
	{
		if(!this.hasWallet())
			return;
		//Write the bag contents back into the item stack
		NonNullList<ItemStack> walletInventory = NonNullList.withSize(WalletItem.InventorySize(this.walletItem), ItemStack.EMPTY);
		for(int i = 0; i < walletInventory.size() && i < this.coinInput.getContainerSize(); i++)
		{
			walletInventory.set(i, this.coinInput.getItem(i));
		}
		WalletItem.putWalletInventory(this.getWallet(), walletInventory);
		if(this.autoConvert != WalletItem.getAutoConvert(getWallet()))
			WalletItem.toggleAutoConvert(getWallet());
	}

	public boolean canConvert()
	{
		return WalletItem.CanConvert(walletItem);
	}
	
	public boolean canPickup()
	{
		return WalletItem.CanPickup(walletItem);
	}
	
	public boolean getAutoConvert()
	{
		return this.autoConvert;//WalletItem.getAutoConvert(this.getWallet());
	}
	
	public void ToggleAutoConvert()
	{
		this.autoConvert = !this.autoConvert;
		//WalletItem.toggleAutoConvert(this.getWallet());
	}
	
	@Override
	public ItemStack quickMoveStack(Player playerEntity, int index)
	{
		
		if(index + this.coinInput.getContainerSize() == this.walletStackIndex)
			return ItemStack.EMPTY;
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			if(index == 0)
			{
				if(!this.moveItemStackTo(slotStack, 1, 37, false))
					return ItemStack.EMPTY;
			}
			else if(index < 37)
			{
				if(clickedStack.getItem() instanceof WalletItem)
				{
					if(!this.moveItemStackTo(slotStack, 0, 1, true))
						return ItemStack.EMPTY;
				}
				if(!this.moveItemStackTo(slotStack, 37, this.slots.size(), false))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 1, 37, true))
			{
				return ItemStack.EMPTY;
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
		
	}
	
	public void ConvertCoins()
	{
		MoneyUtil.ConvertAllCoinsUp(this.coinInput);
		MoneyUtil.SortCoins(this.coinInput);
	}
	
	public ItemStack PickupCoins(ItemStack stack)
	{
		
		ItemStack returnValue = stack.copy();
		
		for(int i = 0; i < coinInput.getContainerSize() && !returnValue.isEmpty(); i++)
		{
			ItemStack thisStack = coinInput.getItem(i);
			if(thisStack.isEmpty())
			{
				coinInput.setItem(i, returnValue.copy());
				returnValue = ItemStack.EMPTY;
			}
			else if(thisStack.getItem() == returnValue.getItem())
			{
				int amountToAdd = MathUtil.clamp(returnValue.getCount(), 0, thisStack.getMaxStackSize() - thisStack.getCount());
				thisStack.setCount(thisStack.getCount() + amountToAdd);
				returnValue.setCount(returnValue.getCount() - amountToAdd);
			}
		}
		
		if(this.autoConvert)
			ConvertCoins();
		
		return returnValue;
	}
	
	public interface IWalletMenuListener {
		public void onReload();
	}
	
}
