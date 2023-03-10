package io.github.lightman314.lightmanscurrency.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.concurrent.Callable;

public class WalletCapability {

	public static void register() {
		CapabilityManager.INSTANCE.register(IWalletHandler.class,  new Capability.IStorage<IWalletHandler>() {

			@Override
			public INBT writeNBT(Capability<IWalletHandler> capability, IWalletHandler instance, Direction side) {
				return instance.save();
			}

			@Override
			public void readNBT(Capability<IWalletHandler> capability, IWalletHandler instance, Direction side, INBT nbt) {
				if(nbt instanceof CompoundNBT)
				{
					instance.load((CompoundNBT)nbt);
				}
			}
		}, (Callable<? extends IWalletHandler>) WalletHandler::new);
	}

	@Deprecated
	public static LazyOptional<IWalletHandler> getWalletHandler(@Nonnull final Entity entity) {
		return entity.getCapability(CurrencyCapabilities.WALLET);
	}

	@Nullable
	public static IWalletHandler lazyGetWalletHandler(@Nonnull final Entity entity) {
		LazyOptional<IWalletHandler> optional = entity.getCapability(CurrencyCapabilities.WALLET);
		if(optional.isPresent())
			return optional.orElseGet(() -> { throw new RuntimeException("Unexpected error occurred!"); });
		return null;
	}

	public static CoinValue getWalletMoney(@Nonnull final Entity entity) {
		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
		if(walletHandler != null)
		{
			ItemStack wallet = walletHandler.getWallet();
			if(WalletItem.isWallet(wallet.getItem()))
				return MoneyUtil.getCoinValue(WalletItem.getWalletInventory(wallet));
		}
		return CoinValue.EMPTY;
	}
	
	public static ICapabilityProvider createProvider(final PlayerEntity playerEntity)
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

		private WalletHandler() { this(null); }

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
		public CompoundNBT save() {
			CompoundNBT compound = new CompoundNBT();
			CompoundNBT walletItem = this.walletItem.save(new CompoundNBT());
			compound.put("Wallet", walletItem);
			compound.putBoolean("Visible", this.visible);
			return compound;
		}
		
		@Override
		public void load(CompoundNBT compound)
		{
			this.walletItem = ItemStack.of(compound.getCompound("Wallet"));
			if(compound.contains("Visible"))
				this.visible = compound.getBoolean("Visible");

			this.clean();
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
	
	private static class Provider implements ICapabilitySerializable<INBT>{
		final LazyOptional<IWalletHandler> optional;
		final IWalletHandler handler;
		Provider(final PlayerEntity playerEntity)
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
		public INBT serializeNBT() { return handler.save(); }
		@Override
		public void deserializeNBT(INBT tag) {
			if(tag instanceof CompoundNBT)
				handler.load((CompoundNBT)tag);
		}
	}
	
	public static void WalletSlotInteraction(PlayerEntity player, int clickedSlot, boolean heldShift, ItemStack heldItem)
	{
		
		if(LightmansCurrency.isCuriosValid(player))
			return;

		boolean creative = player.isCreative() && !player.level.isClientSide;
		if(!creative)
			heldItem = player.inventory.getCarried();
		IWalletHandler walletHandler = lazyGetWalletHandler(player);
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
				if(player.inventory.getFreeSlot() >= 0)
				{
					if(!creative)
						player.inventory.add(wallet);
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
						player.inventory.setCarried(wallet);
				}
			}
		}
		else if(heldShift)
		{
			PlayerInventory inventory = player.inventory;
			//Try to shift-click the hovered slot into the wallet slot
			if(clickedSlot >= inventory.getContainerSize())
			{
				LightmansCurrency.LogWarning("Clicked on slot " + clickedSlot + " of " + player.inventory.getContainerSize() + " on the " + DebugUtil.getSideText(player));
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
