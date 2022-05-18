package io.github.lightman314.lightmanscurrency.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WalletCapability {
	
	public static LazyOptional<IWalletHandler> getWalletHandler(@Nonnull final Entity entity) {
		return entity.getCapability(CurrencyCapabilities.WALLET);
	}
	
	public static ICapabilityProvider createProvider(final Player playerEntity)
	{
		return new Provider(playerEntity);
	}
	
	public static class WalletHandler implements IWalletHandler
	{
		
		final LivingEntity entity;
		ItemStack backupWallet;
		boolean visible;
		boolean wasVisible;
		final Container walletInventory;
		
		public WalletHandler() { this(null); }
		
		public WalletHandler(LivingEntity entity) {
			this.entity = entity;
			this.backupWallet = ItemStack.EMPTY;
			this.walletInventory = new SimpleContainer(1);
			this.visible = true;
			this.wasVisible = true;
		}
		
		@Override
		public Container getInventory()
		{
			return this.walletInventory;
		}
		
		@Override
		public ItemStack getWallet() {
			return this.walletInventory.getItem(0);
		}

		@Override
		public void setWallet(ItemStack walletStack) {
			this.walletInventory.setItem(0, walletStack);
			if(!(walletStack.getItem() instanceof WalletItem) && !walletStack.isEmpty())
				LightmansCurrency.LogWarning("Equipped a non-wallet to the players wallet slot.");
		}
		
		@Override
		public boolean visible() { return this.visible; }
		
		@Override
		public void setVisible(boolean visible) { this.visible = visible; }

		@Override
		public LivingEntity getEntity() { return this.entity; }
		
		@Override
		public boolean isDirty() {
			return !InventoryUtil.ItemMatches(this.backupWallet, this.getWallet()) || this.backupWallet.getCount() != this.getWallet().getCount() || this.wasVisible != this.visible;
		}
		
		@Override
		public void clean() {
			this.backupWallet = this.getWallet().copy();
			this.wasVisible = this.visible;
		}
		
		@Override
		public Tag writeTag() {
			CompoundTag compound = new CompoundTag();
			CompoundTag walletItem = this.getWallet().save(new CompoundTag());
			compound.put("Wallet", walletItem);
			compound.putBoolean("Visible", this.visible);
			return compound;
		}
		
		@Override
		public void readTag(Tag tag)
		{
			if(tag instanceof CompoundTag)
			{
				CompoundTag compound = (CompoundTag)tag;
				ItemStack wallet = ItemStack.of(compound.getCompound("Wallet"));
				this.setWallet(wallet);
				if(compound.contains("Visible"))
					this.visible = compound.getBoolean("Visible");
					
				this.clean();
			}
		}
		
	}
	
	public static class Provider implements ICapabilitySerializable<Tag>{
		final LazyOptional<IWalletHandler> optional;
		final IWalletHandler handler;
		Provider(final Player playerEntity)
		{
			this.handler = new WalletHandler(playerEntity);
			this.optional = LazyOptional.of(() -> this.handler);
		}
		
		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
			return CurrencyCapabilities.WALLET.orEmpty(capability, this.optional);
		}

		@Override
		public Tag serializeNBT() {
			return handler.writeTag();
		}

		@Override
		public void deserializeNBT(Tag tag) {
			handler.readTag(tag);
		}
		
	}
	
	public static void WalletSlotInteraction(Player player, int clickedSlot, boolean heldShift, ItemStack heldItem)
	{
		//LightmansCurrency.LogInfo("Wallet Slot interaction for slot " + clickedSlot + " (shift " + (heldShift ? "held" : "not held") + ") on the " + DebugUtil.getSideText(player));
		AbstractContainerMenu menu = player.containerMenu;
		if(menu == null)
			return;
		boolean creative = player.isCreative() && !player.level.isClientSide;
		if(!creative)
			heldItem = menu.getCarried();
		IWalletHandler walletHandler = getWalletHandler(player).orElse(null);
		if(walletHandler == null)
		{
			LightmansCurrency.LogWarning("Attempted to do a wallet slot interaction, but the player has no wallet handler.");
			return;
		}
		if(clickedSlot < 0)
		{
			//Wallet slot clicked
			if(heldShift)
			{
				//Quick-move the wallet to the players inventory
				ItemStack wallet = walletHandler.getWallet();
				if(wallet.isEmpty())
					return;
				//If we were able to move the wallet into the players inventory, empty the wallet slot
				if(player.getInventory().getFreeSlot() >= 0)
				{
					if(!creative)
						player.getInventory().add(wallet);
					walletHandler.setWallet(ItemStack.EMPTY);
					//LightmansCurrency.LogInfo("Successfully moved the wallet into the players inventory on the " + DebugUtil.getSideText(player));
				}
			}
			else
			{
				//Swap the held item with the wallet item
				ItemStack wallet = walletHandler.getWallet();
				if(wallet.isEmpty() && heldItem.isEmpty())
					return;
				if(WalletSlot.isValidWallet(heldItem) || heldItem.isEmpty())
				{
					walletHandler.setWallet(heldItem);
					if(!creative)
						menu.setCarried(wallet);
				}
			}
		}
		else if(heldShift)
		{
			Inventory inventory = player.getInventory();
			//Try to shift-click the hovered slot into the wallet slot
			if(clickedSlot >= inventory.getContainerSize())
			{
				LightmansCurrency.LogWarning("Clicked on slot " + clickedSlot + " of " + player.getInventory().getContainerSize() + " on the " + DebugUtil.getSideText(player));
				return;
			}	
			ItemStack slotItem = inventory.getItem(clickedSlot);
			//LightmansCurrency.LogInfo("Clicked on slot " + clickedSlot + " of " + inventory.getContainerSize() + " on the " + DebugUtil.getSideText(player));
			//LightmansCurrency.LogInfo("Slot had " + slotItem.getCount() + "x " + slotItem.getItem().getRegistryName().toString());
			if(WalletSlot.isValidWallet(slotItem) && walletHandler.getWallet().isEmpty())
			{
				//Remove the item from inventory
				if(!creative)
				{
					if(slotItem.getCount() > 1)
						inventory.removeItem(clickedSlot, 1);
					else
						inventory.setItem(clickedSlot, ItemStack.EMPTY);
				}
				//Move the wallet into the wallet slot
				ItemStack newWallet = slotItem.copy();
				newWallet.setCount(1);
				walletHandler.setWallet(newWallet);
			}
		}

	}
	
}
