package io.github.lightman314.lightmanscurrency.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

import java.util.concurrent.atomic.AtomicLong;

public class WalletCapability {

	@Deprecated
	public static LazyOptional<IWalletHandler> getWalletHandler(@Nonnull final Entity entity) {
		return entity.getCapability(CurrencyCapabilities.WALLET);
	}

	@Nullable
	public static IWalletHandler lazyGetWalletHandler(@Nonnull final Entity entity) {
		LazyOptional<IWalletHandler> optional = getWalletHandler(entity);
		if(optional.isPresent())
			return optional.orElseGet(() -> {throw new RuntimeException("Unexpected error occurred!");});
		return null;
	}

	public static CoinValue getWalletMoney(@Nonnull final Entity entity) {
		final AtomicLong walletFunds = new AtomicLong(0);
		WalletCapability.getWalletHandler(entity).ifPresent(walletHandler ->{
			ItemStack wallet = walletHandler.getWallet();
			if(WalletItem.isWallet(wallet.getItem()))
				walletFunds.set(MoneyUtil.getValue(WalletItem.getWalletInventory(wallet)));
		});
		return new CoinValue(walletFunds.get());
	}
	
	public static ICapabilityProvider createProvider(final Player playerEntity)
	{
		return new Provider(playerEntity);
	}
	
	public static class WalletHandler implements IWalletHandler
	{
		
		final LivingEntity entity;
		//Wallet
		ItemStack walletItem;
		ItemStack backupWallet;
		//Visibility
		boolean visible;
		boolean wasVisible;
		
		public WalletHandler(LivingEntity entity) {
			this.entity = entity;
			this.backupWallet = ItemStack.EMPTY;
			this.walletItem = ItemStack.EMPTY;
			this.visible = true;
			this.wasVisible = true;
		}
		
		@Override
		public ItemStack getWallet() {
			
			//Curios hook for consistent access
			if(LightmansCurrency.isCuriosValid(this.entity))
				return LCCurios.getCuriosWalletContents(this.entity);
			
			return this.walletItem;
		}

		@Override
		public void setWallet(ItemStack walletStack) {
			
			if(LightmansCurrency.isCuriosValid(this.entity))
			{
				LCCurios.setCuriosWalletContents(this.entity, walletStack);
				return;
			}
			
			this.walletItem = walletStack;
			if(!(walletStack.getItem() instanceof WalletItem) && !walletStack.isEmpty())
				LightmansCurrency.LogWarning("Equipped a non-wallet to the players wallet slot.");
			
		}
		
		@Override
		public void syncWallet(ItemStack walletStack) {
			this.walletItem = walletStack;
		}
		
		@Override
		public boolean visible() {
			if(LightmansCurrency.isCuriosValid(this.entity))
				return LCCurios.getCuriosWalletVisibility(this.entity);
			return this.visible;
		}
		
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
			this.backupWallet = this.walletItem.copy();
			this.wasVisible = this.visible;
		}
		
		@Override
		public Tag writeTag() {
			CompoundTag compound = new CompoundTag();
			CompoundTag walletItem = this.walletItem.save(new CompoundTag());
			compound.put("Wallet", walletItem);
			compound.putBoolean("Visible", this.visible);
			return compound;
		}
		
		@Override
		public void readTag(Tag tag)
		{
			if(tag instanceof CompoundTag compound)
			{
				this.walletItem = ItemStack.of(compound.getCompound("Wallet"));
				if(compound.contains("Visible"))
					this.visible = compound.getBoolean("Visible");
				
				this.clean();
			}
		}
		
		@Override
		public void tick() {
			
			if(LightmansCurrency.isCuriosValid(this.entity) && !this.walletItem.isEmpty())
			{
				LightmansCurrency.LogInfo("Curios detected. Moving wallet from Lightman's Currency wallet slot into the curios wallet slot.");
				LCCurios.setCuriosWalletContents(this.entity, this.walletItem);
				this.walletItem = ItemStack.EMPTY;
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
		
		if(LightmansCurrency.isCuriosValid(player))
			return;
		
		//LightmansCurrency.LogInfo("Wallet Slot interaction for slot " + clickedSlot + " (shift " + (heldShift ? "held" : "not held") + ") on the " + DebugUtil.getSideText(player));
		AbstractContainerMenu menu = player.containerMenu;
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
			ItemStack wallet = walletHandler.getWallet();
			if(heldShift)
			{
				//Quick-move the wallet to the players inventory
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
