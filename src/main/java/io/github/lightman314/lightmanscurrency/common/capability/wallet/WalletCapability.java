package io.github.lightman314.lightmanscurrency.common.capability.wallet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.common.menus.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.integration.curios.wallet.CuriosWalletHandler;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
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

public class WalletCapability {

	@Deprecated
	public static LazyOptional<IWalletHandler> getWalletHandler(@Nonnull final Entity entity) {
		return entity.getCapability(CurrencyCapabilities.WALLET);
	}

	@Nullable
	public static IWalletHandler lazyGetWalletHandler(@Nonnull final Entity entity) {
		LazyOptional<IWalletHandler> optional = entity.getCapability(CurrencyCapabilities.WALLET);
		if(optional.isPresent())
			return optional.orElseThrow(() -> new RuntimeException("Unexpected error occurred!"));
		return null;
	}

	/**
	 * Gets the Wallet Handler used for wallet rendering.
	 * Creates a Curios Wallet Handler if curios is installed
	 * to allow rendering of the wallet on non-player entities
	 * that have a wallet Curios slot.
	 */
	@Nullable
	public static IWalletHandler getRenderWalletHandler(@Nonnull final Entity entity) {
		if(LightmansCurrency.isCuriosLoaded() && entity instanceof LivingEntity le)
			return new CuriosWalletHandler(le);
		return lazyGetWalletHandler(entity);
	}

	@Nonnull
	public static MoneyView getWalletMoney(@Nonnull final Entity entity) {
		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
		if(walletHandler != null)
		{
			ItemStack wallet = walletHandler.getWallet();
			IMoneyViewer moneyViewer = CapabilityMoneyViewer.getCapability(wallet);
			if(moneyViewer == null)
				return MoneyView.empty();
			return moneyViewer.getStoredMoney();
		}
		return MoneyView.builder().build();
	}

	public static ICapabilityProvider createProvider(final Player playerEntity)
	{
		return new Provider(playerEntity);
	}
	
	private static class Provider implements ICapabilitySerializable<Tag>{
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
			return this.handler.save();
		}

		@Override
		public void deserializeNBT(Tag tag) {
			if(tag instanceof CompoundTag compound)
				this.handler.load(compound);
		}
		
	}
	
	public static void WalletSlotInteraction(Player player, int clickedSlot, boolean heldShift, ItemStack heldItem)
	{
		
		if(LightmansCurrency.isCuriosValid(player))
			return;
		
		//LightmansCurrency.LogInfo("Wallet Slot interaction for slot " + clickedSlot + " (shift " + (heldShift ? "held" : "not held") + ") on the " + DebugUtil.getSideText(player));
		AbstractContainerMenu menu = player.containerMenu;
		boolean creative = player.isCreative() && !player.level().isClientSide;
		if(!creative)
			heldItem = menu.getCarried();
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
