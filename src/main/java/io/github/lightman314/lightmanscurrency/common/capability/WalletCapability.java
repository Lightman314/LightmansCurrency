package io.github.lightman314.lightmanscurrency.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class WalletCapability {

	public static void register() {
		CapabilityManager.INSTANCE.register(IWalletHandler.class,  new Capability.IStorage<IWalletHandler>() {

			@Override
			public INBT writeNBT(Capability<IWalletHandler> capability, IWalletHandler instance, Direction side) {
				return instance.save();
			}

			@Override
			public void readNBT(Capability<IWalletHandler> capability, IWalletHandler instance, Direction side,
					INBT nbt) {
				if(nbt instanceof CompoundNBT)
				{
					instance.load((CompoundNBT)nbt);
				}
			}
		}, WalletHandler::new);
	}
	
	public static LazyOptional<IWalletHandler> getWalletHandler(@Nonnull final Entity entity) {
		return entity.getCapability(CurrencyCapabilities.WALLET);
	}
	
	public static ICapabilityProvider createProvider(final PlayerEntity playerEntity)
	{
		return new Provider(playerEntity);
	}
	
	public static class WalletHandler implements IWalletHandler
	{
		
		final LivingEntity entity;
		ItemStack backupWallet;
		final Inventory walletInventory;
		
		public WalletHandler() {
			this(null);
		}
		
		public WalletHandler(LivingEntity entity) {
			this.entity = entity;
			this.backupWallet = ItemStack.EMPTY;
			this.walletInventory = new Inventory(1);
		}
		
		@Override
		public IInventory getInventory()
		{
			return this.walletInventory;
		}
		
		@Override
		public ItemStack getWallet() {
			return this.walletInventory.getStackInSlot(0);
		}

		@Override
		public void setWallet(ItemStack walletStack) {
			this.walletInventory.setInventorySlotContents(0, walletStack);
			if(!(walletStack.getItem() instanceof WalletItem))
				LightmansCurrency.LogWarning("Equipped a non-wallet to the players wallet slot.");
		}

		@Override
		public LivingEntity getEntity() {
			return this.entity;
		}
		
		@Override
		public boolean isDirty() {
			return !InventoryUtil.ItemMatches(this.backupWallet, this.getWallet()) || this.backupWallet.getCount() != this.getWallet().getCount();
		}
		
		@Override
		public void clean() { this.backupWallet = this.getWallet().copy(); }
		
		@Override
		public CompoundNBT save() {
			CompoundNBT compound = new CompoundNBT();
			CompoundNBT walletItem = this.getWallet().write(new CompoundNBT());
			compound.put("Wallet", walletItem);
			return compound;
		}
		
		@Override
		public void load(CompoundNBT compound) {
			ItemStack wallet = ItemStack.read(compound.getCompound("Wallet"));
			this.setWallet(wallet);
			this.clean();
		}
		
	}
	
	public static class Provider implements ICapabilitySerializable<INBT>{
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
		public INBT serializeNBT() {
			return CurrencyCapabilities.WALLET.writeNBT(this.handler, null);
		}
		
		@Override
		public void deserializeNBT(INBT nbt) {
			CurrencyCapabilities.WALLET.readNBT(this.handler, null, nbt);
		}
		
	}
	
}
